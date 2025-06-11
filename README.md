<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:
CREATE OR REPLACE PROCEDURE FES.FESSPU20_VINCULA_LIBERACAO
--F620600  26/04/2021 10:59:24
    --C077033  14/04/2021 13:15:00
--C077033  17/01/2021 21:02:00
--F620600  18/02/2020 11:01:19
--F620600  20/12/2019 10:57:04
--F620600  06/12/2019 22:30:40
--F620600  26/11/2019 14:35:50
AS
    wDT_INICIAL_SEM DATE := TO_DATE('01/01/2018', 'DD/MM/YYYY');
    wDT_FINAL_SEM DATE := TO_DATE('01/07/2018', 'DD/MM/YYYY');
BEGIN
    DBMS_OUTPUT.PUT_LINE('inicio execucao FESSPU20_VINCULA_LIBERACAO ');

    WHILE wDT_INICIAL_SEM < TO_DATE('01/07/2021', 'DD/MM/YYYY') LOOP
            DBMS_OUTPUT.PUT_LINE('executa update aditamento periodo ' || TO_CHAR(wDT_INICIAL_SEM, 'DD/MM/YYYY') || ' - ' || TO_CHAR(wDT_FINAL_SEM, 'DD/MM/YYYY'));
            BEGIN
                UPDATE FES.FESTB712_LIBERACAO_CONTRATO T712
                SET T712.NU_SQNCL_ADITAMENTO = (
                    SELECT T038.NU_SEQ_ADITAMENTO
                    FROM FES.FESTB038_ADTMO_CONTRATO T038
                    WHERE T038.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO
                      AND T712.DT_LIBERACAO >= (CASE WHEN T038.NU_SEM_ADITAMENTO = 1 THEN TO_DATE('01/01' || T038.AA_ADITAMENTO, 'DD/MM/YYYY') ELSE TO_DATE('01/07' || TO_CHAR(T038.AA_ADITAMENTO), 'DD/MM/YYYY') END)
                      AND T712.DT_LIBERACAO < (CASE WHEN T038.NU_SEM_ADITAMENTO = 1 THEN TO_DATE('01/07' || T038.AA_ADITAMENTO, 'DD/MM/YYYY') ELSE TO_DATE('01/01' || TO_CHAR(T038.AA_ADITAMENTO + 1), 'DD/MM/YYYY') END)
                      AND T712.NU_TIPO_TRANSACAO IS NULL
                    --AND T038.NU_STATUS_ADITAMENTO IN (4, 5)
                ),
                    T712.NU_TIPO_TRANSACAO = 2,
                    T712.NU_PARTICIPACAO_CANDIDATO = 1
                WHERE TO_CHAR(T712.DT_LIBERACAO, 'YYYYMMDD') >= TO_CHAR(wDT_INICIAL_SEM ,'YYYYMMDD')
                  AND TO_CHAR(T712.DT_LIBERACAO, 'YYYYMMDD') < TO_CHAR(wDT_FINAL_SEM ,'YYYYMMDD')
                  AND T712.NU_TIPO_TRANSACAO IS NULL
                  AND T712.NU_SQNCL_LIBERACAO_CONTRATO IN (
                    SELECT T712A.NU_SQNCL_LIBERACAO_CONTRATO
                    FROM FES.FESTB712_LIBERACAO_CONTRATO T712A
                             JOIN FES.FESTB038_ADTMO_CONTRATO T038
                                  ON T038.NU_CANDIDATO_FK36 = T712A.NU_SEQ_CANDIDATO
                    WHERE T712A.DT_LIBERACAO >= (CASE WHEN T038.NU_SEM_ADITAMENTO = 1 THEN TO_DATE('01/01' || T038.AA_ADITAMENTO, 'DD/MM/YYYY') ELSE TO_DATE('01/07' || TO_CHAR(T038.AA_ADITAMENTO), 'DD/MM/YYYY') END)
                      AND T712A.DT_LIBERACAO < (CASE WHEN T038.NU_SEM_ADITAMENTO = 1 THEN TO_DATE('01/07' || T038.AA_ADITAMENTO, 'DD/MM/YYYY') ELSE TO_DATE('01/01' || TO_CHAR(T038.AA_ADITAMENTO + 1), 'DD/MM/YYYY') END)
                      AND T712A.NU_TIPO_TRANSACAO IS NULL
                    --AND T038.NU_STATUS_ADITAMENTO IN (4, 5)
                );

                COMMIT;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    NULL;
                WHEN OTHERS THEN
                    raise_application_error(SQLCODE, SQLERRM,true);
            END;

            DBMS_OUTPUT.PUT_LINE('executa update contrato periodo ' || TO_CHAR(wDT_INICIAL_SEM, 'DD/MM/YYYY') || ' - ' || TO_CHAR(wDT_FINAL_SEM, 'DD/MM/YYYY'));
            BEGIN
                UPDATE FES.FESTB712_LIBERACAO_CONTRATO T712
                SET T712.NU_TIPO_TRANSACAO = 1,
                    T712.NU_PARTICIPACAO_CANDIDATO = 1
                WHERE TO_CHAR(T712.DT_LIBERACAO, 'YYYYMMDD') >= TO_CHAR(wDT_INICIAL_SEM ,'YYYYMMDD')
                  AND TO_CHAR(T712.DT_LIBERACAO, 'YYYYMMDD') < TO_CHAR(wDT_FINAL_SEM ,'YYYYMMDD')
                  AND T712.NU_TIPO_TRANSACAO IS NULL
                  AND T712.NU_SQNCL_LIBERACAO_CONTRATO IN (
                    SELECT T712A.NU_SQNCL_LIBERACAO_CONTRATO
                    FROM FES.FESTB712_LIBERACAO_CONTRATO T712A
                             JOIN FES.FESTB010_CANDIDATO T010
                                  ON T010.NU_SEQ_CANDIDATO = T712A.NU_SEQ_CANDIDATO
                    WHERE T712A.DT_LIBERACAO >= (CASE WHEN TO_NUMBER(TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'MM')) < 7 THEN TO_DATE('01/01' || TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'YYYY'), 'DD/MM/YYYY') ELSE TO_DATE('01/07' || TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'YYYY'), 'DD/MM/YYYY') END)
                      AND T712A.DT_LIBERACAO < (CASE WHEN TO_NUMBER(TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'MM')) < 7 THEN TO_DATE('01/07' || TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'YYYY'), 'DD/MM/YYYY') ELSE TO_DATE('01/01' || TO_NUMBER(TO_CHAR(T010.DT_ADMISSAO_CANDIDATO, 'YYYY') + 1), 'DD/MM/YYYY') END)
                      AND T712A.NU_TIPO_TRANSACAO IS NULL
                );

                COMMIT;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    NULL;
                WHEN OTHERS THEN
                    raise_application_error(SQLCODE, SQLERRM,true);
            END;

            SELECT ADD_MONTHS(wDT_INICIAL_SEM, 6),
                   ADD_MONTHS(wDT_FINAL_SEM, 6)
            INTO wDT_INICIAL_SEM, wDT_FINAL_SEM
            FROM DUAL;

        END LOOP;

    DBMS_OUTPUT.PUT_LINE('INCLUI RETENCOES TIPO 4 PARA AS LIBERACOES QUE NAO POSSUEM VINCULACAO');
    INSERT INTO FES.FESTB817_RETENCAO_LIBERACAO
    (NU_SQNCL_LIBERACAO_CONTRATO, NU_MOTIVO_RETENCAO_LIBERACAO, DT_INICIO_RETENCAO)
    SELECT NU_SQNCL_LIBERACAO_CONTRATO, '4', SYSDATE
    FROM FES.FESTB712_LIBERACAO_CONTRATO T712
    WHERE NU_TIPO_TRANSACAO IS NULL
      AND NOT EXISTS (SELECT 1 FROM FES.FESTB817_RETENCAO_LIBERACAO T817
                      WHERE T817.NU_SQNCL_LIBERACAO_CONTRATO = T712.NU_SQNCL_LIBERACAO_CONTRATO
                        AND T817.NU_MOTIVO_RETENCAO_LIBERACAO = '4'
                        AND T817.DT_FIM_RETENCAO IS NULL);

    DBMS_OUTPUT.PUT_LINE('fim execucao FESSPU20_VINCULA_LIBERACAO');
END;










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
