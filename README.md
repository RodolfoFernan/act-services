<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise do Bloco de Código: Alteração de Suspensão e Vigência
 --ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DESLOCAMENTO NO VALOR DO REPASSE
    FOR X IN
        (
        SELECT
            A.NU_CANDIDATO_FK36,
            A.AA_ADITAMENTO,
            A.NU_SEM_ADITAMENTO,
            A.VR_ADITAMENTO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB038_ADTMO_CONTRATO A
                            ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK36
                                AND L.AA_REFERENCIA_LIBERACAO = A.AA_ADITAMENTO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = A.NU_SEM_ADITAMENTO
                                AND A.NU_STATUS_ADITAMENTO > 3
                                AND A.VR_ADITAMENTO > 0
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND L.MM_REFERENCIA_LIBERACAO > 0
        GROUP BY
            A.NU_CANDIDATO_FK36,
            A.VR_ADITAMENTO,
            A.AA_ADITAMENTO,
            A.NU_SEM_ADITAMENTO
        HAVING (
                           COUNT(L.VR_REPASSE) = 6
                       AND
                           SUM(L.VR_REPASSE) > 0
                       AND
                           VR_ADITAMENTO > SUM(VR_REPASSE)
                       AND
                           (
                                       MOD( ROUND( ( SUM(L.VR_REPASSE) / A.VR_ADITAMENTO ), 1 ), 10 ) = 0
                                   OR
                                       MOD( ROUND( ( A.VR_ADITAMENTO / SUM(L.VR_REPASSE) ), 1 ), 10 ) = 0
                                   OR
                                       A.VR_ADITAMENTO / SUM(L.VR_REPASSE) >= 100
                               )
                   )
        )
        LOOP
            -- Loop para tratar as 6 parcelas do semestre de cada candidato
            FOR Lcntr IN 1..6
                LOOP
                    -- Definindo o vr_repasse de cada liberacao
                    IF (Lcntr IN (6)) THEN
                        vr_repasse := (X.VR_ADITAMENTO - (TRUNC(X.VR_ADITAMENTO / 6, 2) * 5) );
                    ELSE
                        vr_repasse := TRUNC(X.VR_ADITAMENTO / 6, 2);
                    END IF;

                    -- Definindo o mes de repasse
                    IF (X.NU_SEM_ADITAMENTO in (1)) THEN
                        mm_repasse := Lcntr;
                    ELSE
                        mm_repasse := Lcntr + 6;
                    END IF;

                    MM_LIBERACAO := mm_repasse;

                    SQL_QUERY := 'UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET VR_REPASSE = ''' ||
                                 TO_CHAR(vr_repasse,'FM999999990.00') || ''', DT_ATUALIZACAO = ''' ||
                                 SYSDATE || ''' WHERE NU_SEQ_CANDIDATO = ' ||
                                 X.NU_CANDIDATO_FK36 || ' AND AA_REFERENCIA_LIBERACAO = ' ||
                                 X.AA_ADITAMENTO || ' AND MM_REFERENCIA_LIBERACAO = ' || MM_LIBERACAO;

                    DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                    EXECUTE IMMEDIATE SQL_QUERY;

                END LOOP;

            COMMIT; -- Commit para cada candidato (a cada 6 liberacoes a rotina executa o commit)

        END LOOP;
    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DESLOCAMENTO NO VALOR DO REPASSE ************* ');


    --ADEQUACAO NO VALOR DAS LIBERACOES DE SEMESTRES COM VALOR DE REPASSE ZERADO
    FOR X IN
        (
        SELECT
            A.NU_CANDIDATO_FK36,
            A.AA_ADITAMENTO,
            A.NU_SEM_ADITAMENTO,
            A.VR_ADITAMENTO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB038_ADTMO_CONTRATO A
                            ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK36
                                AND L.AA_REFERENCIA_LIBERACAO = A.AA_ADITAMENTO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = A.NU_SEM_ADITAMENTO
                                AND A.NU_STATUS_ADITAMENTO > 3
                                AND A.VR_ADITAMENTO > 0
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND L.MM_REFERENCIA_LIBERACAO > 0
        GROUP BY
            A.NU_CANDIDATO_FK36,
            A.AA_ADITAMENTO,
            A.NU_SEM_ADITAMENTO,
            A.VR_ADITAMENTO
        HAVING ( COUNT(L.VR_REPASSE) = 6 AND SUM(L.VR_REPASSE) = 0 )
        )
        LOOP
            -- Loop para tratar as 6 parcelas do semestre de cada candidato
            FOR Lcntr IN 1..6
                LOOP
                    -- Definindo o vr_repasse de cada liberacao
                    IF (Lcntr IN (6)) THEN
                        vr_repasse := (X.VR_ADITAMENTO - (TRUNC(X.VR_ADITAMENTO / 6, 2) * 5) );
                    ELSE
                        vr_repasse := TRUNC(X.VR_ADITAMENTO / 6, 2);
                    END IF;

                    -- Definindo o mes de repasse
                    IF (X.NU_SEM_ADITAMENTO in (1)) THEN
                        mm_repasse := Lcntr;
                    ELSE
                        mm_repasse := Lcntr + 6;
                    END IF;

                    MM_LIBERACAO := mm_repasse;

                    SQL_QUERY := 'UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET VR_REPASSE = ''' ||
                                 TO_CHAR(vr_repasse,'FM999999990.00') || ''', DT_ATUALIZACAO = ''' ||
                                 SYSDATE || ''' WHERE NU_SEQ_CANDIDATO = ' ||
                                 X.NU_CANDIDATO_FK36 || ' AND AA_REFERENCIA_LIBERACAO = ' ||
                                 X.AA_ADITAMENTO || ' AND MM_REFERENCIA_LIBERACAO = ' || MM_LIBERACAO;

                    DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                    EXECUTE IMMEDIATE SQL_QUERY;

                END LOOP;

            COMMIT; -- Commit para cada candidato (a cada 6 liberacoes a rotina executa o commit)

            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET IC_SITUACAO_LIBERACAO = 'NR',
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SEQ_CANDIDATO = X.NU_CANDIDATO_FK36
              AND AA_REFERENCIA_LIBERACAO = X.AA_ADITAMENTO
              AND CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = X.NU_SEM_ADITAMENTO
              AND IC_SITUACAO_LIBERACAO IN ('R', 'NE', 'E');

            COMMIT;
        END LOOP;
    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA ADEQUACAO NO VALOR DAS LIBERACOES DE SEMESTRES COM VALOR DE REPASSE ZERADO ************* ');




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
