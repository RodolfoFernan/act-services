<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise do Bloco de Código: Alteração de Suspensão e Vigência
CREATE OR REPLACE PROCEDURE FES.FESSPZ57_CRISE2019_CORR_VLRS
--F620600  27/05/2021 15:16:26
    --C077033  21/05/2021 17:08:00
--C077033  23/04/2021 19:15:00
--C077033  17/01/2021 20:33:00
--C077033  29/10/2020 15:25:00

AS
    QT_COMPENSACAO_CRIADA NUMERIC(10) := 0;
    SQL_QUERY VARCHAR2(500) := NULL;
    V_NU_SQNCL_COMPENSACAO_REPASSE NUMERIC(12);
    vr_repasse NUMERIC(18,2) := NULL;
    mm_repasse integer;
    MM_LIBERACAO NUMERIC(2);
    COUNT_1 NUMERIC(5) := 0;

BEGIN

    DBMS_OUTPUT.ENABLE (buffer_size => NULL);

    DBMS_OUTPUT.PUT_LINE(' ************* INICIO DA FESSPZ57_CRISE2019_CORR_VLRS - CORRIGE VALORES DE REPASSE ************* ');


    SELECT MAX(NU_SQNCL_COMPENSACAO_REPASSE) INTO V_NU_SQNCL_COMPENSACAO_REPASSE FROM FES.FESTB812_CMPSO_RPSE_INDVO;

    --COMPENSACAO, E NOVO REPASSE, DE LIBERACOES DE SEMESTRES COM PROBLEMA DE DESLOCAMENTO NO VALOR DO REPASSE
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            A.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
            L.NU_SEQ_CANDIDATO,
            L.AA_REFERENCIA_LIBERACAO,
            L.MM_REFERENCIA_LIBERACAO,
            R.NU_SQNCL_RLTRO_CTRTO_ANALITICO AS SQNCL_COMPENSADO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN (
            SELECT
                NU_CANDIDATO_FK36,
                AA_ADITAMENTO,
                NU_SEM_ADITAMENTO,
                VR_ADITAMENTO
            FROM FES.FESTB712_LIBERACAO_CONTRATO
                     INNER JOIN FES.FESTB038_ADTMO_CONTRATO
                                ON NU_SEQ_CANDIDATO = NU_CANDIDATO_FK36
                                    AND AA_REFERENCIA_LIBERACAO = AA_ADITAMENTO
                                    AND CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = NU_SEM_ADITAMENTO
                                    AND NU_STATUS_ADITAMENTO > 3
                                    AND VR_ADITAMENTO > 0
            WHERE NU_SEQ_CANDIDATO > 20000000
              AND MM_REFERENCIA_LIBERACAO > 0
            GROUP BY
                NU_CANDIDATO_FK36,
                VR_ADITAMENTO,
                AA_ADITAMENTO,
                NU_SEM_ADITAMENTO
            HAVING (
                               COUNT(VR_REPASSE) = 6
                           AND
                               SUM(VR_REPASSE) > 0
                           AND
                               VR_ADITAMENTO > SUM(VR_REPASSE)
                           AND
                               (
                                           MOD( ROUND( ( SUM(VR_REPASSE) / VR_ADITAMENTO ), 1 ), 10 ) = 0
                                       OR
                                           MOD( ROUND( ( VR_ADITAMENTO / SUM(VR_REPASSE) ), 1 ), 10 ) = 0
                                       OR
                                           VR_ADITAMENTO / SUM(VR_REPASSE) >= 100
                                   )
                       )
        ) D
                            ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK36
                                AND L.AA_REFERENCIA_LIBERACAO = D.AA_ADITAMENTO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.NU_SEM_ADITAMENTO
                 INNER JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO A
                            ON L.NU_SQNCL_LIBERACAO_CONTRATO = A.NU_SQNCL_LIBERACAO_CONTRATO
                                AND L.NU_SEQ_CANDIDATO = A.NU_SEQ_CANDIDATO
                                AND L.VR_REPASSE = A.VR_REPASSE
                 LEFT OUTER JOIN FES.FESTB812_CMPSO_RPSE_INDVO R
                                 ON R.NU_SQNCL_RLTRO_CTRTO_ANALITICO = A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
        WHERE L.IC_SITUACAO_LIBERACAO IN ('R', 'NE')
        )
        LOOP
            IF X.SQNCL_COMPENSADO IS NULL THEN
                QT_COMPENSACAO_CRIADA := QT_COMPENSACAO_CRIADA + 1;
                V_NU_SQNCL_COMPENSACAO_REPASSE := V_NU_SQNCL_COMPENSACAO_REPASSE + 1;

                SQL_QUERY := 'INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (' ||
                             'NU_SQNCL_COMPENSACAO_REPASSE, NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_TIPO_ACERTO, TS_INCLUSAO, CO_USUARIO_INCLUSAO)' ||
                             ' VALUES (' || V_NU_SQNCL_COMPENSACAO_REPASSE || ', ' || X.NU_SQNCL_RLTRO_CTRTO_ANALITICO || ', 5,''' || SYSDATE || ''', ''CRISE19'')';

                DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                EXECUTE IMMEDIATE SQL_QUERY;
            END IF;

            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET IC_SITUACAO_LIBERACAO = 'NR',
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO
              AND NU_SEQ_CANDIDATO = X.NU_SEQ_CANDIDATO
              AND AA_REFERENCIA_LIBERACAO = X.AA_REFERENCIA_LIBERACAO
              AND MM_REFERENCIA_LIBERACAO = X.MM_REFERENCIA_LIBERACAO;
        END LOOP;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE(' ************* QUANTIDADE DE COMPENSACOES CRIADAS: ' || QT_COMPENSACAO_CRIADA || ' ************* ');


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
