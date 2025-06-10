<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise do Bloco de Código: Alteração de Suspensão e Vigência
Análise da Procedure FES.FESSPZ57_CRISE2019_CORR_VLRS (Continuação)

Os próximos blocos da procedure focam na adequação dos valores de repasse em diferentes cenários de inconsistência e na correção da situação das liberações associadas.
Adequação do Valor das Liberações com Problema de Deslocamento no Repasse

 --Atualização do sequencial do aditamento na TB712
    FOR X IN
        (
        SELECT
            NU_SQNCL_LIBERACAO_CONTRATO,
            NU_SEQ_ADITAMENTO
        FROM FES.FESTB712_LIBERACAO_CONTRATO
                 INNER JOIN FES.FESTB038_ADTMO_CONTRATO
                            ON NU_SEQ_CANDIDATO = NU_CANDIDATO_FK36
                                AND AA_REFERENCIA_LIBERACAO = AA_ADITAMENTO
                                AND CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = NU_SEM_ADITAMENTO
                                --AND NU_STATUS_ADITAMENTO > 3
                                AND NU_SEQ_ADITAMENTO IS NOT NULL
        WHERE NU_SEQ_CANDIDATO > 20000000
          AND ( NU_SQNCL_ADITAMENTO <> NU_SEQ_ADITAMENTO
            OR NU_SQNCL_ADITAMENTO IS NULL )
        )
        LOOP
            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET NU_SQNCL_ADITAMENTO = X.NU_SEQ_ADITAMENTO,
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
        END LOOP;

    COMMIT;


    /* ####################################################################
       TRATAMENTO DIVERGENCIA DE VALORES ENTRE O REPASSE E A CONTRATACAO
       ####################################################################
    */

    --CURSOR PARA INSERCAO DE RETENCAO PARA LIBERACOES POR DIVERGENCIA ENTRE REPASSE E A CONTRATACAO
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN
             (
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
                             ( SUM(VR_REPASSE) - VR_CONTRATO > 1 OR VR_CONTRATO - SUM(VR_REPASSE) > 1 )
                             AND
                             COUNT(L.VR_REPASSE) = 6
                         )
             ) D
             ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK11
                 AND L.AA_REFERENCIA_LIBERACAO = D.ANO
                 AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.SEMESTRE
        WHERE NOT EXISTS
            (
                SELECT 1
                FROM FES.FESTB817_RETENCAO_LIBERACAO R
                WHERE L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                  AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 9
                  AND R.DT_FIM_RETENCAO IS NULL
            )
        )
        LOOP
            --INSERE RETENCAO POR DIVERGENCIA ENTRE REPASSE E CONTRATO
            SQL_QUERY := 'INSERT INTO FES.FESTB817_RETENCAO_LIBERACAO ' ||
                         ' (NU_SQNCL_LIBERACAO_CONTRATO, NU_MOTIVO_RETENCAO_LIBERACAO, DT_INICIO_RETENCAO) values (' ||
                         X.NU_SQNCL_LIBERACAO_CONTRATO || ', ''9'',''' || SYSDATE || ''')';

            DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
            EXECUTE IMMEDIATE SQL_QUERY;
        END LOOP;

    COMMIT;





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
