<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Etapa 1: Identificar e Inativar Retenções de Liberações para Contratos Transferidos/Aditados com Divergência

Objetivo: Localizar liberações com retenções ativas e inativá-las caso o contrato associado tenha sido transferido ou aditado, ou se a retenção não for mais necessária para o ajuste de valores.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            NU_IES
            IC_SITUACAO_LIBERACAO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO
            DT_FIM_RETENCAO
        FES.FESTB049_TRANSFERENCIA (aliás T)
            NU_CANDIDATO_FK10
            NU_SEQ_TRANSFERENCIA
            AA_REFERENCIA
            NU_SEM_REFERENCIA
            NU_CAMPUS_ORIGEM_FK161
            NU_CAMPUS_DESTINO_FK161
            DT_INCLUSAO
            NU_STATUS_TRANSFERENCIA
        FES.FESTB154_CAMPUS_INEP (aliás C, I)
            NU_CAMPUS
            NU_IES_FK155
        FES.FESTB155_IES_INEP (aliás E, N)
            NU_IES
            NU_MANTENEDORA_FK156
        FES.FESTB038_ADTMO_CONTRATO (aliás A)
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            DT_ADITAMENTO
            NU_STATUS_ADITAMENTO
        FES.FESTB010_CANDIDATO (aliás CA)
            NU_SEQ_CANDIDATO
            DT_ADMISSAO_CANDIDATO

Etapa 2: Atualizar a Situação de Liberações Efetivadas para 'NR' (Não Repassado)

Objetivo: Alterar o status de liberações que já foram "Efetivadas" ('E') para "Não Repassadas" ('NR') se elas pertencem a semestres onde há divergência entre o valor repassado e o contrato.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            IC_SITUACAO_LIBERACAO
            VR_REPASSE
        FES.FESTB036_CONTRATO_FIES (aliás A)
            NU_CANDIDATO_FK11
            VR_CONTRATO
            NU_STATUS_CONTRATO
        FES.FESTB010_CANDIDATO (aliás C)
            DT_ADMISSAO_CANDIDATO
    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO

Etapa 3: Compensação e Novo Repasse de Liberações com Divergência entre Repasse e Contrato (Criação de Compensação e Atualização de Situação)

Objetivo: Registrar compensações para liberações com divergência entre o valor repassado e o contrato, e ajustar a situação da liberação para permitir um novo processamento.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            IC_SITUACAO_LIBERACAO
            VR_REPASSE
        FES.FESTB036_CONTRATO_FIES (aliás A, usado na sub-query D)
            NU_CANDIDATO_FK11
            VR_CONTRATO
            NU_STATUS_CONTRATO
        FES.FESTB010_CANDIDATO (aliás C, usado na sub-query D)
            CO_CPF
            DT_ADMISSAO_CANDIDATO
        FES.FESTB711_RLTRO_CTRTO_ANLTO (aliás A, no join principal)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
        FES.FESTB812_CMPSO_RPSE_INDVO (aliás R)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
    Tabelas Afetadas (Escrita):
        FES.FESTB812_CMPSO_RPSE_INDVO (Inserção)
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)

Etapa 4: Adequação do Valor das Liberações com Problema de Divergência entre Repasse e Contrato

Objetivo: Recalcular e ajustar os valores de repasse para as 6 parcelas de um semestre, garantindo que a soma total dos repasses de um semestre seja igual ao VR_CONTRATO.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
        FES.FESTB036_CONTRATO_FIES (aliás A)
            NU_CANDIDATO_FK11
            VR_CONTRATO
            NU_STATUS_CONTRATO
        FES.FESTB010_CANDIDATO (aliás C)
            DT_ADMISSAO_CANDIDATO
    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)

Etapa 5: Tratamento de Divergência de Valores entre Repasse e Aditamento (Inserção de Retenções)

Objetivo: Identificar liberações onde a soma dos repasses difere do valor do aditamento e registrar uma retenção para essas liberações.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (sem alias no SELECT principal, mas com alias L no FROM)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
        FES.FESTB038_ADTMO_CONTRATO (sem alias no SELECT principal, mas com alias A na subquery e no join)
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            VR_ADITAMENTO
            NU_STATUS_ADITAMENTO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO
            DT_FIM_RETENCAO
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Inserção)

Etapa 6: Compensação e Novo Repasse de Liberações com Divergência entre Repasse e Aditamento (Criação de Compensação e Atualização de Situação)

Objetivo: Registrar compensações para liberações com divergência entre o valor repassado e o aditamento, e ajustar a situação da liberação para permitir um novo processamento.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            IC_SITUACAO_LIBERACAO
            VR_REPASSE
        FES.FESTB038_ADTMO_CONTRATO (usado na sub-query D)
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            VR_ADITAMENTO
            NU_STATUS_ADITAMENTO
        FES.FESTB711_RLTRO_CTRTO_ANLTO (aliás A, no join principal)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
        FES.FESTB812_CMPSO_RPSE_INDVO (aliás R)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
    Tabelas Afetadas (Escrita):
        FES.FESTB812_CMPSO_RPSE_INDVO (Inserção)
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)

Etapa 7: Adequação do Valor das Liberações com Problema de Divergência entre Repasse e Aditamento

Objetivo: Recalcular e ajustar os valores de repasse para as 6 parcelas de um semestre, garantindo que a soma total dos repasses de um semestre seja igual ao VR_ADITAMENTO.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
        FES.FESTB038_ADTMO_CONTRATO (aliás A)
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            VR_ADITAMENTO
            NU_STATUS_ADITAMENTO
    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)

Etapa 8: Compensação e Readequação de Liberações com Deslocamento no Valor do Repasse (em relação ao Aditamento)

Objetivo: Identificar semestres onde o somatório dos repasses está significativamente menor que o valor do aditamento, e registrar compensações para essas liberações, redefinindo o status para "Não Repassado" ('NR'). Este caso trata de um "deslocamento" no valor.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
            IC_SITUACAO_LIBERACAO
        FES.FESTB038_ADTMO_CONTRATO (usado na sub-query D)
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            VR_ADITAMENTO
            NU_STATUS_ADITAMENTO
        FES.FESTB711_RLTRO_CTRTO_ANLTO (aliás A)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
        FES.FESTB812_CMPSO_RPSE_INDVO (aliás R)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
    Tabelas Afetadas (Escrita):
        FES.FESTB812_CMPSO_RPSE_INDVO (Inserção)
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)

Etapa 9: Adequação do Valor das Liberações com Deslocamento no Repasse (Baseado em Aditamento)

Objetivo: Recalcular e ajustar os valores de repasse para as 6 parcelas de liberações que apresentam um "deslocamento" no valor total repassado em relação ao VR_ADITAMENTO do aditamento, garantindo que a soma dos repasses por semestre seja igual ao VR_ADITAMENTO.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
        FES.FESTB038_ADTMO_CONTRATO (aliás A)
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            VR_ADITAMENTO
            NU_STATUS_ADITAMENTO
    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)

Etapa 10: Adequação do Valor das Liberações com Repasse Zerado (Baseado em Aditamento)

Objetivo: Recalcular e ajustar os valores de repasse para as 6 parcelas de liberações onde o somatório dos repasses para um semestre está zerado, apesar de haver um aditamento com valor positivo. Após o ajuste, o status da liberação é alterado para "Não Repassado" ('NR').

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
            IC_SITUACAO_LIBERACAO
        FES.FESTB038_ADTMO_CONTRATO (aliás A)
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            VR_ADITAMENTO
            NU_STATUS_ADITAMENTO
    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)

Etapa 11: Atualização do Sequencial de Aditamento em Liberações

Objetivo: Sincronizar o número sequencial do aditamento (NU_SQNCL_ADITAMENTO) na tabela de liberações de contrato (FESTB712_LIBERACAO_CONTRATO) com o valor correspondente na tabela de aditamentos (FESTB038_ADTMO_CONTRATO), caso haja divergência ou o campo esteja nulo.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            NU_SQNCL_ADITAMENTO
            DT_ATUALIZACAO
        FES.FESTB038_ADTMO_CONTRATO
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            NU_SEQ_ADITAMENTO
    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)

Etapa 12: Inserção de Retenções por Divergência entre Repasse e Contrato

Objetivo: Identificar liberações onde a soma dos valores de repasse para um semestre apresenta uma divergência significativa (maior que R$ 1,00 para mais ou para menos) em relação ao VR_CONTRATO do contrato principal, e inserir uma retenção do tipo 9 (Motivo de Retenção) para essas liberações, caso ainda não exista uma retenção ativa com esse motivo.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
        FES.FESTB036_CONTRATO_FIES (aliás A, usado na sub-query D)
            NU_CANDIDATO_FK11
            VR_CONTRATO
            NU_STATUS_CONTRATO
        FES.FESTB010_CANDIDATO (aliás C, usado na sub-query D)
            CO_CPF
            DT_ADMISSAO_CANDIDATO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R, usado na cláusula NOT EXISTS)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO
            DT_FIM_RETENCAO
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Inserção)

Etapa 13: Compensação e Novo Repasse de Liberações com Divergência entre Repasse e Contrato

Objetivo: Registrar compensações para liberações que apresentam uma divergência significativa entre o valor repassado e o valor do contrato, e ajustar a situação da liberação para permitir um novo processamento. O tipo de acerto para a compensação é 7.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
            IC_SITUACAO_LIBERACAO
        FES.FESTB036_CONTRATO_FIES (usado na sub-query D)
            NU_CANDIDATO_FK11
            VR_CONTRATO
            NU_STATUS_CONTRATO
        FES.FESTB010_CANDIDATO (usado na sub-query D)
            CO_CPF
            DT_ADMISSAO_CANDIDATO
        FES.FESTB711_RLTRO_CTRTO_ANLTO (aliás A)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
        FES.FESTB812_CMPSO_RPSE_INDVO (aliás R)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
    Tabelas Afetadas (Escrita):
        FES.FESTB812_CMPSO_RPSE_INDVO (Inserção)
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)

Etapa 14: Adequação do Valor das Liberações com Divergência entre Repasse e Contrato

Objetivo: Recalcular e ajustar os valores de repasse para as 6 parcelas de liberações que apresentam uma divergência significativa (maior que R$ 1,00 para mais ou para menos) entre o valor total repassado em um semestre e o VR_CONTRATO do contrato principal. O ajuste garante que a soma dos repasses por semestre seja igual ao VR_CONTRATO.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
        FES.FESTB036_CONTRATO_FIES (aliás A)
            NU_CANDIDATO_FK11
            VR_CONTRATO
            NU_STATUS_CONTRATO
        FES.FESTB010_CANDIDATO (aliás C)
            DT_ADMISSAO_CANDIDATO
    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)

Etapa 15: Inserção de Retenções por Divergência entre Repasse e Aditamento

Objetivo: Identificar liberações onde a soma dos valores de repasse para um semestre apresenta uma divergência significativa (maior ou igual a R$ 1,00 para mais ou para menos) em relação ao VR_ADITAMENTO do aditamento, e inserir uma retenção do tipo 9 (Motivo de Retenção) para essas liberações, caso ainda não exista uma retenção ativa com esse motivo.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
        FES.FESTB038_ADTMO_CONTRATO (usado na sub-query D)
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            VR_ADITAMENTO
            NU_STATUS_ADITAMENTO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R, usado na cláusula NOT EXISTS)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO
            DT_FIM_RETENCAO
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Inserção)

Etapa 16: Compensação e Novo Repasse de Liberações com Divergência entre Repasse e Aditamento

Objetivo: Registrar compensações para liberações que apresentam uma divergência significativa entre o valor repassado e o valor do aditamento, e ajustar a situação da liberação para permitir um novo processamento. O tipo de acerto para a compensação é 7.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
            IC_SITUACAO_LIBERACAO
        FES.FESTB038_ADTMO_CONTRATO (usado na sub-query D)
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            VR_ADITAMENTO
            NU_STATUS_ADITAMENTO
        FES.FESTB711_RLTRO_CTRTO_ANLTO (aliás A)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
        FES.FESTB812_CMPSO_RPSE_INDVO (aliás R)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
    Tabelas Afetadas (Escrita):
        FES.FESTB812_CMPSO_RPSE_INDVO (Inserção)
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)










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
