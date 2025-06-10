<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise do Bloco de Código: Alteração de Suspensão e Vigência
Análise da Procedure FES.FESSPZ57_CRISE2019_CORR_VLRS - Corrige Valores de Repasse

A procedure FES.FESSPZ57_CRISE2019_CORR_VLRS é responsável por corrigir problemas relacionados aos valores de repasse em liberações de contrato, especialmente aqueles com um "deslocamento" no valor. Ela faz isso criando registros de compensação para repasses incorretos e ajustando a situação das liberações afetadas.
Visão Geral da Procedure

    Nome: FES.FESSPZ57_CRISE2019_CORR_VLRS
    Propósito: Corrigir valores de repasse e a situação de liberações de contrato que apresentaram problemas de deslocamento ou inconsistência nos montantes repassados.
    Variáveis Locais:
        QT_COMPENSACAO_CRIADA NUMERIC(10): Contador para o número de compensações criadas.
        SQL_QUERY VARCHAR2(500): Usada para construir e executar SQL dinâmico.
        V_NU_SQNCL_COMPENSACAO_REPASSE NUMERIC(12): Armazena o maior número sequencial de compensação de repasse individual, que será usado para gerar novos IDs.
        vr_repasse NUMERIC(18,2): Não utilizada neste trecho.
        mm_repasse INTEGER: Não utilizada neste trecho.
        MM_LIBERACAO NUMERIC(2): Não utilizada neste trecho.
        COUNT_1 NUMERIC(5): Não utilizada neste trecho.

Início e Inicialização

O bloco começa habilitando a saída do DBMS_OUTPUT e exibindo uma mensagem de início. Em seguida, busca o maior NU_SQNCL_COMPENSACAO_REPASSE na tabela FES.FESTB812_CMPSO_RPSE_INDVO para garantir que os novos IDs de compensação sejam únicos.

    DBMS_OUTPUT.ENABLE (buffer_size => NULL);
    DBMS_OUTPUT.PUT_LINE(' ************* INICIO DA FESSPZ57_CRISE2019_CORR_VLRS - CORRIGE VALORES DE REPASSE ************* ');
    SELECT MAX(NU_SQNCL_COMPENSACAO_REPASSE) INTO V_NU_SQNCL_COMPENSACAO_REPASSE FROM FES.FESTB812_CMPSO_RPSE_INDVO;

Compensação e Novo Repasse para Liberações com Deslocamento de Valor

Esta é a parte central da procedure, que identifica liberações com problemas de valor e realiza as correções.

    Objetivo: Identificar liberações de contrato (FES.FESTB712_LIBERACAO_CONTRATO) que estão em situação de "Repassado" ('R') ou "Não Estornado" ('NE') e que, em conjunto com seus aditamentos, apresentam uma inconsistência nos valores de repasse. Para esses casos, um registro de compensação é criado, e a situação da liberação é alterada para "Não Repassado" ('NR').

    Lógica Principal:
        Sub-query D (Origem dos Problemas): Seleciona candidatos, valores de aditamento, ano e semestre de aditamento da tabela FES.FESTB712_LIBERACAO_CONTRATO (join com FES.FESTB038_ADTMO_CONTRATO). Filtra por candidatos com NU_SEQ_CANDIDATO > 20000000, MM_REFERENCIA_LIBERACAO > 0, NU_STATUS_ADITAMENTO > 3 e VR_ADITAMENTO > 0.
            Cláusula HAVING (Condição de Inconsistência):
                COUNT(VR_REPASSE) = 6: Indica que há 6 repasses para aquele aditamento (o que pode ser um padrão esperado para um semestre completo).
                SUM(VR_REPASSE) > 0: Garante que houve repasses.
                VR_ADITAMENTO > SUM(VR_REPASSE): O valor do aditamento é maior que a soma dos repasses (indicando que algo não foi totalmente repassado ou houve um "deslocamento").
                MOD( ROUND( ( SUM(VR_REPASSE) / VR_ADITAMENTO ), 1 ), 10 ) = 0 OR MOD( ROUND( ( VR_ADITAMENTO / SUM(VR_REPASSE) ), 1 ), 10 ) = 0 OR VR_ADITAMENTO / SUM(VR_REPASSE) >= 100: Esta é uma condição complexa que tenta identificar um "deslocamento" no valor. Basicamente, verifica se a proporção entre o valor do aditamento e a soma dos repasses (ou vice-versa), arredondada para uma casa decimal, resulta em um múltiplo de 10, ou se a proporção VR_ADITAMENTO / SUM(VR_REPASSE) é maior ou igual a 100. Isso sugere que o valor real foi repassado com um erro significativo (e.g., um zero a mais, ou um fator de 10 na diferença).
        Join Principal: A sub-query D é então joinada com:
            FES.FESTB712_LIBERACAO_CONTRATO (aliás L): A tabela principal de liberações.
            FES.FESTB711_RLTRO_CTRTO_ANLTO (aliás A): Tabela de relatórios analíticos de contrato, para obter o NU_SQNCL_RLTRO_CTRTO_ANALITICO da liberação.
            FES.FESTB812_CMPSO_RPSE_INDVO (aliás R, LEFT OUTER JOIN): Tabela de compensação de repasse individual, para verificar se o repasse já foi compensado.
        Filtro Final: O FOR X IN (...) itera sobre as liberações que têm status 'R' (Repassado) ou 'NE' (Não Estornado) e atendem às condições complexas da sub-query D.
        Processamento no Loop:
            Criação de Compensação: Se a liberação ainda não tiver um registro de compensação (X.SQNCL_COMPENSADO IS NULL), a procedure incrementa um contador, gera um novo NU_SQNCL_COMPENSACAO_REPASSE e insere um registro na tabela FES.FESTB812_CMPSO_RPSE_INDVO. A inserção é feita via EXECUTE IMMEDIATE, o que permite construir a string SQL dinamicamente. O NU_TIPO_ACERTO é definido como 5.
            Atualização da Liberação: A situação da liberação (IC_SITUACAO_LIBERACAO) é alterada para 'NR' (Não Repassado), e a DT_ATUALIZACAO é definida como SYSDATE.

    Tabelas Envolvidas:
        FES.FESTB712_LIBERACAO_CONTRATO (leitura e atualização)
        FES.FESTB038_ADTMO_CONTRATO (leitura)
        FES.FESTB711_RLTRO_CTRTO_ANLTO (leitura)
        FES.FESTB812_CMPSO_RPSE_INDVO (leitura e inserção)

    Campos Chave no SELECT do cursor:
        L.NU_SQNCL_LIBERACAO_CONTRATO
        A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
        L.NU_SEQ_CANDIDATO
        L.AA_REFERENCIA_LIBERACAO
        L.MM_REFERENCIA_LIBERACAO
        R.NU_SQNCL_RLTRO_CTRTO_ANALITICO AS SQNCL_COMPENSADO

    Campos Atualizados no UPDATE:
        IC_SITUACAO_LIBERACAO (para 'NR')
        DT_ATUALIZACAO (para SYSDATE)




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
