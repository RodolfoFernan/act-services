<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise do Bloco de Código: Alteração de Suspensão e Vigência
Análise da Procedure FES.FESSPZ57_CRISE2019_CORR_VLRS (Continuação)

Os próximos blocos da procedure focam na adequação dos valores de repasse em diferentes cenários de inconsistência e na correção da situação das liberações associadas.
Adequação do Valor das Liberações com Problema de Deslocamento no Repasse

Análise Detalhada da Procedure FES.FESSPZ57_CRISE2019_CORR_VLRS por Etapas

Vamos detalhar o processo da procedure FES.FESSPZ57_CRISE2019_CORR_VLRS passo a passo, focando nas tabelas e campos envolvidos em cada operação.
Etapa 1: Atualização do Sequencial do Aditamento na Tabela de Liberação (FESTB712_LIBERACAO_CONTRATO)

Este bloco tem a finalidade de sincronizar o identificador do aditamento entre a tabela de liberações de contrato e a tabela de aditamentos.

    Processo:
        Seleção de Dados:
            Um cursor (FOR X IN (...)) busca dados das tabelas FES.FESTB712_LIBERACAO_CONTRATO (liberações) e FES.FESTB038_ADTMO_CONTRATO (aditamentos).
            Tabelas de Leitura:
                FES.FESTB712_LIBERACAO_CONTRATO
                FES.FESTB038_ADTMO_CONTRATO
            Campos Lidos (no SELECT do cursor):
                FES.FESTB712_LIBERACAO_CONTRATO: NU_SQNCL_LIBERACAO_CONTRATO, NU_SEQ_CANDIDATO, AA_REFERENCIA_LIBERACAO, MM_REFERENCIA_LIBERACAO, NU_SQNCL_ADITAMENTO
                FES.FESTB038_ADTMO_CONTRATO: NU_CANDIDATO_FK36, AA_ADITAMENTO, NU_SEM_ADITAMENTO, NU_SEQ_ADITAMENTO
            Condições de Junção (INNER JOIN):
                NU_SEQ_CANDIDATO (de FESTB712) = NU_CANDIDATO_FK36 (de FESTB038)
                AA_REFERENCIA_LIBERACAO (de FESTB712) = AA_ADITAMENTO (de FESTB038)
                CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END (semestre da liberação) = NU_SEM_ADITAMENTO (semestre do aditamento)
            Filtros (WHERE):
                NU_SEQ_CANDIDATO > 20000000 (filtros para IDs de candidatos específicos)
                ( NU_SQNCL_ADITAMENTO <> NU_SEQ_ADITAMENTO OR NU_SQNCL_ADITAMENTO IS NULL ) (liberações onde o sequencial do aditamento está incorreto ou ausente)
                NU_SEQ_ADITAMENTO IS NOT NULL (garante que o aditamento associado tenha um sequencial válido)
        Atualização dos Registros:
            Para cada registro selecionado pelo cursor, um comando UPDATE é executado.
            Tabela de Atualização: FES.FESTB712_LIBERACAO_CONTRATO
            Campos Atualizados:
                NU_SQNCL_ADITAMENTO é definido com o valor de X.NU_SEQ_ADITAMENTO (o sequencial correto do aditamento).
                DT_ATUALIZACAO é definido como SYSDATE (data e hora atuais).
            Condição de Atualização (WHERE): NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO (garante que apenas a liberação atual do loop seja modificada).
        Confirmação:
            COMMIT; executa a confirmação de todas as atualizações realizadas dentro do loop, tornando as mudanças permanentes no banco de dados.

Etapa 2: Tratamento de Divergência de Valores entre Repasse e Contratação (Inserção de Retenções)

Este bloco identifica liberações onde há uma discrepância significativa entre a soma dos valores de repasse e o valor original do contrato e aplica uma retenção (bloqueio) nessas liberações.

    Processo:
        Identificação de Divergências (Sub-query D):
            Uma sub-query aninhada (SELECT ... FROM FES.FESTB712_LIBERACAO_CONTRATO L INNER JOIN ...) busca liberações de contrato que demonstram divergência.
            Tabelas de Leitura:
                FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
                FES.FESTB036_CONTRATO_FIES (aliás A)
                FES.FESTB010_CANDIDATO (aliás C)
            Campos Lidos (no SELECT da sub-query D):
                FES.FESTB036_CONTRATO_FIES: NU_CANDIDATO_FK11, VR_CONTRATO
                FES.FESTB010_CANDIDATO: DT_ADMISSAO_CANDIDATO (usado para derivar ANO e SEMESTRE), CO_CPF (usado no GROUP BY)
                FES.FESTB712_LIBERACAO_CONTRATO: VR_REPASSE (usado no SUM)
            Condições de Junção (INNER JOIN):
                L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK11
                A.NU_STATUS_CONTRATO > 3 (garante que apenas contratos com status finalizado ou similar sejam considerados)
                C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO
                L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY')
                (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
            Filtros (WHERE):
                L.NU_SEQ_CANDIDATO > 20000000
                L.MM_REFERENCIA_LIBERACAO > 0
            Agrupamento (GROUP BY): Por CO_CPF, NU_CANDIDATO_FK11, VR_CONTRATO, ano e semestre de admissão.
            Condições de Agrupamento (HAVING):
                ( SUM(VR_REPASSE) - VR_CONTRATO > 1 OR VR_CONTRATO - SUM(VR_REPASSE) > 1 ) (a diferença entre a soma dos repasses e o valor do contrato é maior que 1, em qualquer direção).
                COUNT(L.VR_REPASSE) = 6 (confirma que há 6 repasses para o semestre).
        Seleção Principal para Retenção:
            Um cursor principal (FOR X IN (...)) seleciona as NU_SQNCL_LIBERACAO_CONTRATO das liberações que correspondem às divergências identificadas na sub-query D.
            Tabelas de Leitura:
                FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
                A sub-query D (como uma tabela temporária)
            Campos Lidos (no SELECT do cursor):
                L.NU_SQNCL_LIBERACAO_CONTRATO
            Condições de Junção (INNER JOIN):
                L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK11
                L.AA_REFERENCIA_LIBERACAO = D.ANO
                CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.SEMESTRE
            Filtro (WHERE NOT EXISTS): Garante que a liberação ainda não tenha uma retenção ativa (DT_FIM_RETENCAO IS NULL) com o NU_MOTIVO_RETENCAO_LIBERACAO = 9 na tabela FES.FESTB817_RETENCAO_LIBERACAO.
        Inserção de Retenção:
            Para cada NU_SQNCL_LIBERACAO_CONTRATO selecionado que atende aos critérios, um comando INSERT dinâmico (EXECUTE IMMEDIATE SQL_QUERY) é executado.
            Tabela de Inserção: FES.FESTB817_RETENCAO_LIBERACAO
            Campos Inseridos:
                NU_SQNCL_LIBERACAO_CONTRATO (o sequencial da liberação com divergência).
                NU_MOTIVO_RETENCAO_LIBERACAO é definido como 9 (motivo específico para divergência de valores).
                DT_INICIO_RETENCAO é definido como SYSDATE (data e hora de início da retenção).
        Confirmação:
            COMMIT; executa a confirmação de todas as novas retenções, tornando-as permanentes no banco de dados.




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
