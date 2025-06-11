<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:
Excelente! Vamos detalhar a FES.FESSPU20_VINCULA_LIBERACAO no formato solicitado:
FES.FESSPU20_VINCULA_LIBERACAO

    Objetivo: Vincular as liberações de contrato a seus respectivos aditamentos ou contratos iniciais, marcando o tipo de transação e a participação do candidato. Além disso, a SP insere retenções para liberações que não conseguem ser vinculadas.

Etapa 1: Processamento de Liberações por Aditamento (Iterativo por Semestre)

    Objetivo: Para cada semestre dentro de um período pré-definido, tentar vincular as liberações existentes a um aditamento de contrato válido.

    Lógica:
        A SP itera semestralmente (de 01/01/2018 até 01/07/2021), definindo uma janela de tempo (wDT_INICIAL_SEM e wDT_FINAL_SEM).
        Para cada semestre, tenta atualizar as liberações (FESTB712_LIBERACAO_CONTRATO) que ainda não têm um tipo de transação definido (NU_TIPO_TRANSACAO IS NULL).
        A atualização define NU_SQNCL_ADITAMENTO com o sequencial do aditamento correspondente, NU_TIPO_TRANSACAO como 2 (indicando aditamento) e NU_PARTICIPACAO_CANDIDATO como 1.
        A vinculação ocorre se a data da liberação (DT_LIBERACAO) estiver dentro do período de vigência de um aditamento (FESTB038_ADTMO_CONTRATO) para o mesmo candidato.
        Há um COMMIT após cada atualização semestral para persistir as alterações.
        Tratamento de exceções: NO_DATA_FOUND é ignorado, mas outras exceções são levantadas para interromper o processo.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás T712)
            Campos Consultados:
                NU_SQNCL_LIBERACAO_CONTRATO
                NU_SEQ_CANDIDATO
                DT_LIBERACAO
                NU_TIPO_TRANSACAO
            Filtros Aplicados:
                TO_CHAR(T712.DT_LIBERACAO, 'YYYYMMDD') >= TO_CHAR(wDT_INICIAL_SEM ,'YYYYMMDD')
                TO_CHAR(T712.DT_LIBERACAO, 'YYYYMMDD') < TO_CHAR(wDT_FINAL_SEM ,'YYYYMMDD')
                T712.NU_TIPO_TRANSACAO IS NULL
        FES.FESTB038_ADTMO_CONTRATO (aliás T038 - usado na subconsulta para buscar NU_SEQ_ADITAMENTO e na subconsulta IN)
            Campos Consultados:
                NU_SEQ_ADITAMENTO
                NU_CANDIDATO_FK36
                AA_ADITAMENTO
                NU_SEM_ADITAMENTO
                NU_STATUS_ADITAMENTO (comentado, mas indica uma intenção de filtrar por status 4 ou 5)
            Filtros Aplicados:
                T038.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO
                T712.DT_LIBERACAO >= (CASE WHEN T038.NU_SEM_ADITAMENTO = 1 THEN TO_DATE('01/01' || T038.AA_ADITAMENTO, 'DD/MM/YYYY') ELSE TO_DATE('01/07' || TO_CHAR(T038.AA_ADITAMENTO), 'DD/MM/YYYY') END)
                T712.DT_LIBERACAO < (CASE WHEN T038.NU_SEM_ADITAMENTO = 1 THEN TO_DATE('01/07' || T038.AA_ADITAMENTO, 'DD/MM/YYYY') ELSE TO_DATE('01/01' || TO_CHAR(T038.AA_ADITAMENTO + 1), 'DD/MM/YYYY') END)
                T712.NU_TIPO_TRANSACAO IS NULL (na subconsulta IN)

    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO
            Campos Atualizados:
                NU_SQNCL_ADITAMENTO
                NU_TIPO_TRANSACAO
                NU_PARTICIPACAO_CANDIDATO

Etapa 2: Processamento de Liberações por Contrato Inicial (Iterativo por Semestre)

    Objetivo: Para cada semestre dentro do mesmo período pré-definido, tentar vincular as liberações remanescentes (aquelas não vinculadas a aditamentos na etapa anterior) a um contrato inicial (através da data de admissão do candidato).

    Lógica:
        Continua a iteração semestral.
        Atualiza as liberações (FESTB712_LIBERACAO_CONTRATO) que ainda não têm um tipo de transação definido (NU_TIPO_TRANSACAO IS NULL).
        A atualização define NU_TIPO_TRANSACAO como 1 (indicando contrato inicial) e NU_PARTICIPACAO_CANDIDATO como 1.
        A vinculação ocorre se a data da liberação (DT_LIBERACAO) estiver dentro do semestre de admissão do candidato (FESTB010_CANDIDATO).
        Há um COMMIT após cada atualização semestral.
        Tratamento de exceções: Similar à Etapa 1.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás T712)
            Campos Consultados:
                NU_SQNCL_LIBERACAO_CONTRATO
                NU_SEQ_CANDIDATO
                DT_LIBERACAO
                NU_TIPO_TRANSACAO
            Filtros Aplicados:
                TO_CHAR(T712.DT_LIBERACAO, 'YYYYMMDD') >= TO_CHAR(wDT_INICIAL_SEM ,'YYYYMMDD')
                TO_CHAR(T712.DT_LIBERACAO, 'YYYYMMDD') < TO_CHAR(wDT_FINAL_SEM ,'YYYYMMDD')
                T712.NU_TIPO_TRANSACAO IS NULL
        FES.FESTB010_CANDIDATO (aliás T010 - usado na subconsulta IN)
            Campos Consultados:
                NU_SEQ_CANDIDATO
                DT_ADMISSAO_CANDIDATO
            Filtros Aplicados:
                T010.NU_SEQ_CANDIDATO = T712A.NU_SEQ_CANDIDATO
                T712A.DT_LIBERACAO >= (CASE WHEN TO_NUMBER(TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'MM')) < 7 THEN TO_DATE('01/01' || TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'YYYY'), 'DD/MM/YYYY') ELSE TO_DATE('01/07' || TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'YYYY'), 'DD/MM/YYYY') END)
                T712A.DT_LIBERACAO < (CASE WHEN TO_NUMBER(TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'MM')) < 7 THEN TO_DATE('01/07' || TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'YYYY'), 'DD/MM/YYYY') ELSE TO_DATE('01/01' || TO_NUMBER(TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'YYYY') + 1), 'DD/MM/YYYY') END)
                T712A.NU_TIPO_TRANSACAO IS NULL

    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO
            Campos Atualizados:
                NU_TIPO_TRANSACAO
                NU_PARTICIPACAO_CANDIDATO

Etapa 3: Inclusão de Retenções para Liberações Não Vinculadas

    Objetivo: Identificar as liberações que, após as etapas 1 e 2, ainda não foram vinculadas (ou seja, NU_TIPO_TRANSACAO IS NULL) e criar uma retenção de motivo 4 (que provavelmente significa "Ausência de Vinculação") para elas.

    Lógica:
        Insere novos registros na tabela FESTB817_RETENCAO_LIBERACAO.
        Seleciona NU_SQNCL_LIBERACAO_CONTRATO da FESTB712_LIBERACAO_CONTRATO onde NU_TIPO_TRANSACAO ainda é nulo.
        Garante que não seja inserida uma retenção duplicada para o mesmo motivo e liberação se já houver uma retenção ativa (DT_FIM_RETENCAO IS NULL).

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás T712)
            Campos Consultados:
                NU_SQNCL_LIBERACAO_CONTRATO
                NU_TIPO_TRANSACAO
            Filtros Aplicados:
                NU_TIPO_TRANSACAO IS NULL
        FES.FESTB817_RETENCAO_LIBERACAO (aliás T817 - usada na subconsulta NOT EXISTS)
            Campos Consultados:
                NU_SQNCL_LIBERACAO_CONTRATO
                NU_MOTIVO_RETENCAO_LIBERACAO
                DT_FIM_RETENCAO
            Filtros Aplicados:
                T817.NU_SQNCL_LIBERACAO_CONTRATO = T712.NU_SQNCL_LIBERACAO_CONTRATO
                T817.NU_MOTIVO_RETENCAO_LIBERACAO = '4'
                T817.DT_FIM_RETENCAO IS NULL

    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO
            Campos Inseridos:
                NU_SQNCL_LIBERACAO_CONTRATO
                NU_MOTIVO_RETENCAO_LIBERACAO (valor '4')
                DT_INICIO_RETENCAO (valor SYSDATE)










<pre>

</pre>

<h2>2. Funcionalidades Principais</h2>
<ul>
 <li><b>Controle de Lançamentos:</b> Gerenciar débitos e créditos financeiros.</li>
 <li><b>Consolidação Diária:</b> Consolidação diária de saldos.</li>
 <li><b>API Gateway:</b> Controle de rotas e segurança para os microsserviços.</li>
 <li><b>Cache:</b> Uso de Redis/Memcached para otimização de consultas.</li>
 <li><b>Comunicação:</b> RabbitMQ/Kafka para comunicação assíncrona entre os microsserviços.</li>
 <li><b>Monitoramento:</b> Observabilidade com Prometheus, Grafana e a ELK Stack.</li>
</ul>

<h2>3. Etapas do Desenvolvimento</h2>
<h3>1.1 Planejamento e Desenho da Solução</h3>
<ul>
 <li><b>Microsserviços:</b> A solução foi desenhada com base em microsserviços, cada um responsável por uma função específica.</li>
 <li><b>Tecnologias:</b> Usamos Spring Boot para o backend, Kafka para mensageria, PostgreSQL para persistência de dados, Redis para caching e Docker/Kubernetes para orquestração de contêineres.</li>
</ul>

<h3>1.2 Configuração do Ambiente de Desenvolvimento</h3>
<ul>
 <li><b>Instalar:</b> Java 17, Spring Boot, Docker, Kubernetes.</li>
 <li><b>Configurar banco de dados:</b> PostgreSQL/MySQL.</li>
 <li><b>Ferramentas adicionais:</b> Git, Maven, Jenkins.</li>
</ul>

<h3>1.3 Desenvolvimento dos Serviços</h3>
<ul>
 <li><b>Controle de Lançamentos:</b> API REST para gerenciar lançamentos financeiros.</li>
 <li><b>Consolidação Diária:</b> Consolidação dos saldos financeiros diariamente.</li>
 <li><b>Comunicação:</b> Kafka para orquestrar as comunicações entre os microsserviços.</li>
</ul>

<h3>1.4 Segurança</h3>
<ul>
 <li><b>Autenticação e autorização:</b> OAuth 2.0.</li>
 <li><b>Segurança:</b> TLS/SSL, Rate Limiting, Firewalls.</li>
</ul>

<h3>1.5 Observabilidade e Monitoramento</h3>
<ul>
 <li><b>Prometheus e Grafana:</b> Monitoramento e visualização de métricas.</li>
 <li><b>Logs centralizados:</b> ELK Stack (Kibana).</li>
</ul>

<hr>

<h2>4. Tecnologias Utilizadas</h2>
<ul>
 <li><b>Backend:</b> Spring Boot, RabbitMQ/Kafka, Redis.</li>
 <li><b>Banco de Dados:</b> PostgreSQL, Redis.</li>
 <li><b>Infraestrutura:</b> Docker, Kubernetes, Jenkins.</li>
</ul>

<hr>

<h2>5. Passos para Execução</h2>
<h3>Pré-requisitos:</h3>
<ul>
 <li>Java 17</li>
 <li>Maven</li>
 <li>Docker e Kubernetes</li>
 <li>PostgreSQL/MySQL</li>
 <li>Redis</li>
 <li>Kafka</li>
</ul>

<h3>Passos para Execução:</h3>
<ol>
 <li>Clone o repositório: 
 <pre><code>git clone https://github.com/seu_usuario/repositorio.git</code></pre>
 </li>
 <li>Compile o projeto:
 <pre><code>mvn clean install</code></pre>
 </li>
 <li>Suba os contêineres Docker (se configurado):
 <pre><code>docker-compose up</code></pre>
 </li>
 <li>Execute os serviços:
 <pre><code>java -jar target/controle-lancamentos.jar 
java -jar target/consolidacao-diaria.jar</code></pre>
 </li>
</ol>
