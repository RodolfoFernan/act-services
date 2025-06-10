<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
  --COMPENSACAO, E NOVO REPASSE, DE LIBERACOES DE SEMESTRES COM PROBLEMA DE DIVERGENCIA NO VALOR ENTRE REPASSE E A CONTRATACAO
    QT_COMPENSACAO_CRIADA := 0;
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            A.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
            L.NU_SEQ_CANDIDATO,
            L.AA_REFERENCIA_LIBERACAO,
            L.MM_REFERENCIA_LIBERACAO,
            L.IC_SITUACAO_LIBERACAO,
            R.NU_SQNCL_RLTRO_CTRTO_ANALITICO AS SQNCL_COMPENSADO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN (
            SELECT
                A.NU_CANDIDATO_FK11,
                TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY') AS ANO,
                (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END) AS SEMESTRE,
                A.VR_CONTRATO,
                SUM(L.VR_REPASSE)
            FROM FES.FESTB712_LIBERACAO_CONTRATO L
                     INNER JOIN FES.FESTB036_CONTRATO_FIES A
                                ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK11
                                    AND A.NU_STATUS_CONTRATO > 3
                     INNER JOIN FES.FESTB010_CANDIDATO C
                                ON C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO
                                    AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY')
                                    AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
            WHERE L.NU_SEQ_CANDIDATO > 20000000
              AND L.MM_REFERENCIA_LIBERACAO > 0
            GROUP BY
                CO_CPF,
                A.NU_CANDIDATO_FK11,
                A.VR_CONTRATO,
                TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY'),
                (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
            HAVING
                (
                            VR_CONTRATO >= 1000
                        AND
                            ( SUM(VR_REPASSE) - VR_CONTRATO > 1 OR VR_CONTRATO - SUM(VR_REPASSE) > 1 )
                        AND
                            COUNT(L.VR_REPASSE) = 6
                    )
        ) D
                            ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK11
                                AND L.AA_REFERENCIA_LIBERACAO = D.ANO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.SEMESTRE
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
                             ' VALUES (' || V_NU_SQNCL_COMPENSACAO_REPASSE || ', ' || X.NU_SQNCL_RLTRO_CTRTO_ANALITICO || ', 7,''' || SYSDATE || ''', ''CRISE19'')';

                DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                EXECUTE IMMEDIATE SQL_QUERY;
            END IF;


            IF X.IC_SITUACAO_LIBERACAO = 'R' THEN
                UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                SET IC_SITUACAO_LIBERACAO = 'NR',
                    DT_ATUALIZACAO = SYSDATE
                WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO
                  AND NU_SEQ_CANDIDATO = X.NU_SEQ_CANDIDATO
                  AND AA_REFERENCIA_LIBERACAO = X.AA_REFERENCIA_LIBERACAO
                  AND MM_REFERENCIA_LIBERACAO = X.MM_REFERENCIA_LIBERACAO;
            ELSE
                UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                SET IC_SITUACAO_LIBERACAO = 'S',
                    DT_ATUALIZACAO = SYSDATE
                WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO
                  AND NU_SEQ_CANDIDATO = X.NU_SEQ_CANDIDATO
                  AND AA_REFERENCIA_LIBERACAO = X.AA_REFERENCIA_LIBERACAO
                  AND MM_REFERENCIA_LIBERACAO = X.MM_REFERENCIA_LIBERACAO;
            END IF;

        END LOOP;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE(' ************* QUANTIDADE DE COMPENSACOES CRIADAS: ' || QT_COMPENSACAO_CRIADA || ' ************* ');


    --ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DIVERGENCIA NO VALOR ENTRE REPASSE E A CONTRATACAO
    FOR X IN
        (
        SELECT
            A.NU_CANDIDATO_FK11,
            TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY') AS ANO,
            (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END) AS SEMESTRE,
            A.VR_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB036_CONTRATO_FIES A
                            ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK11
                                AND A.NU_STATUS_CONTRATO > 3
                 INNER JOIN FES.FESTB010_CANDIDATO C
                            ON C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO
                                AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY')
                                AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND L.MM_REFERENCIA_LIBERACAO > 0
        GROUP BY
            A.NU_CANDIDATO_FK11,
            A.VR_CONTRATO,
            TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY'),
            (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
        HAVING
            (
                        VR_CONTRATO >= 1000
                    AND
                        ( SUM(VR_REPASSE) - VR_CONTRATO > 1 OR VR_CONTRATO - SUM(VR_REPASSE) > 1 )
                    AND
                        COUNT(L.VR_REPASSE) = 6
                )
        )
        LOOP
            -- Loop para tratar as 6 parcelas do semestre de cada candidato
            FOR Lcntr IN 1..6
                LOOP
                    -- Definindo o vr_repasse de cada liberacao
                    IF (Lcntr IN (6)) THEN
                        vr_repasse := (X.VR_CONTRATO - (TRUNC(X.VR_CONTRATO / 6, 2) * 5) );
                    ELSE
                        vr_repasse := TRUNC(X.VR_CONTRATO / 6, 2);
                    END IF;

                    -- Definindo o mes de repasse
                    IF (X.SEMESTRE in (1)) THEN
                        mm_repasse := Lcntr;
                    ELSE
                        mm_repasse := Lcntr + 6;
                    END IF;

                    MM_LIBERACAO := mm_repasse;

                    SQL_QUERY := 'UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET VR_REPASSE = ''' ||
                                 TO_CHAR(vr_repasse,'FM999999990.00') || ''', DT_ATUALIZACAO = ''' ||
                                 SYSDATE || ''' WHERE NU_SEQ_CANDIDATO = ' ||
                                 X.NU_CANDIDATO_FK11 || ' AND AA_REFERENCIA_LIBERACAO = ' ||
                                 X.ANO || ' AND MM_REFERENCIA_LIBERACAO = ' || MM_LIBERACAO;

                    DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                    EXECUTE IMMEDIATE SQL_QUERY;

                END LOOP;

            COMMIT; -- Commit para cada candidato (a cada 6 liberacoes a rotina executa o commit)

        END LOOP;
    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DIVERGENCIA NO VALOR ENTRE REPASSE E CONTRATO ************* ');









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
