<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise Detalhada da Procedure FES.FESSPZ57_CRISE2019_CORR_VLRS por Etapas (Continuação)

Análise Detalhada da Procedure FES.FESSPZ57_CRISE2019_CORR_VLRS por Etapas (Continuação e Finalização)

Vamos detalhar as etapas restantes da procedure FES.FESSPZ57_CRISE2019_CORR_VLRS, que focam na divergência de valores entre o repasse e o aditamento, similar ao tratamento de divergência com o contrato, mas agora utilizando os dados dos aditamentos.
Etapa 5: Tratamento de Divergência de Valores entre Repasse e Aditamento (Inserção de Retenções)

Este bloco tem a finalidade de identificar e registrar retenções para liberações onde a soma dos repasses difere significativamente do valor do aditamento correspondente.

    Processo:
        Identificação de Divergências (Sub-query D):
            Um sub-query aninhada (SELECT ... FROM FES.FESTB712_LIBERACAO_CONTRATO INNER JOIN FES.FESTB038_ADTMO_CONTRATO ...) busca os semestres que apresentam divergência entre o valor total repassado e o valor do aditamento.
            Tabelas de Leitura:
                FES.FESTB712_LIBERACAO_CONTRATO
                FES.FESTB038_ADTMO_CONTRATO
            Campos Lidos (no SELECT da sub-query D):
                NU_CANDIDATO_FK36 (do aditamento)
                AA_ADITAMENTO (do aditamento)
                NU_SEM_ADITAMENTO (do aditamento)
                VR_ADITAMENTO (do aditamento)
                VR_REPASSE (da liberação, usado no SUM)
            Condições de Junção (INNER JOIN):
                NU_SEQ_CANDIDATO (de FESTB712) = NU_CANDIDATO_FK36 (de FESTB038)
                AA_REFERENCIA_LIBERACAO (de FESTB712) = AA_ADITAMENTO (de FESTB038)
                CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END (semestre da liberação) = NU_SEM_ADITAMENTO (semestre do aditamento)
                NU_STATUS_ADITAMENTO > 3 (apenas aditamentos com status finalizado ou similar).
            Filtros (WHERE):
                NU_SEQ_CANDIDATO > 20000000
                MM_REFERENCIA_LIBERACAO > 0
            Agrupamento (GROUP BY): Por NU_CANDIDATO_FK36, AA_ADITAMENTO, NU_SEM_ADITAMENTO, VR_ADITAMENTO.
            Condições de Agrupamento (HAVING):
                ( SUM(VR_REPASSE) - VR_ADITAMENTO >= 1 OR VR_ADITAMENTO - SUM(VR_REPASSE) >= 1 ): A diferença absoluta entre a soma dos repasses e o valor do aditamento é maior ou igual a 1.
                COUNT(VR_REPASSE) = 6: Garante que todas as 6 parcelas do semestre foram consideradas.
        Seleção Principal para Retenção:
            Um cursor (FOR X IN (...)) seleciona as NU_SQNCL_LIBERACAO_CONTRATO das liberações que correspondem às divergências identificadas na sub-query D.
            Tabelas de Leitura:
                FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
                A sub-query D (como uma tabela temporária).
            Campos Lidos (no SELECT do cursor):
                L.NU_SQNCL_LIBERACAO_CONTRATO
            Condições de Junção (INNER JOIN):
                L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK36
                L.AA_REFERENCIA_LIBERACAO = D.AA_ADITAMENTO
                CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.NU_SEM_ADITAMENTO
            Filtro (WHERE NOT EXISTS): Garante que a liberação ainda não tenha uma retenção ativa (DT_FIM_RETENCAO IS NULL) com o NU_MOTIVO_RETENCAO_LIBERACAO = 9 na tabela FES.FESTB817_RETENCAO_LIBERACAO.
        Inserção de Retenção:
            Para cada NU_SQNCL_LIBERACAO_CONTRATO selecionado pelo cursor, um comando INSERT dinâmico é executado.
            Tabela de Inserção: FES.FESTB817_RETENCAO_LIBERACAO
            Campos Inseridos:
                NU_SQNCL_LIBERACAO_CONTRATO: Sequencial da liberação.
                NU_MOTIVO_RETENCAO_LIBERACAO: Definido como 9 (motivo específico para divergência de valores).
                DT_INICIO_RETENCAO: SYSDATE (data e hora de início da retenção).
        Confirmação:
            COMMIT; executa a confirmação das novas retenções no banco de dados.

Etapa 6: Compensação e Novo Repasse de Liberações com Divergência de Valores (Entre Repasse e Aditamento)

Este bloco, semelhante ao da divergência com o contrato, visa registrar compensações e ajustar a situação das liberações de semestres que apresentam divergência entre o repasse e o valor do aditamento.

    Variáveis Utilizadas:
        QT_COMPENSACAO_CRIADA: Contador para o número de compensações criadas neste bloco.
        V_NU_SQNCL_COMPENSACAO_REPASSE: Variável para o próximo sequencial de compensação.

    Processo:
        Seleção de Liberações com Divergência:
            Um cursor (FOR X IN (...)) busca as liberações de contrato que possuem divergência entre o valor do repasse e o valor do aditamento.
            Tabelas de Leitura:
                FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
                FES.FESTB711_RLTRO_CTRTO_ANLTO (aliás A)
                FES.FESTB812_CMPSO_RPSE_INDVO (aliás R)
                Sub-query D (interna ao FROM): Identifica os semestres com divergência.
                    Tabelas da Sub-query D: FES.FESTB712_LIBERACAO_CONTRATO, FES.FESTB038_ADTMO_CONTRATO.
                    Condições de Junção da Sub-query D: As mesmas da Etapa 5, conectando liberações e aditamentos.
                    Filtros da Sub-query D (WHERE): Os mesmos da Etapa 5.
                    Agrupamento da Sub-query D (GROUP BY): Os mesmos da Etapa 5, mas incluindo VR_ADITAMENTO.
                    Condições de Agrupamento da Sub-query D (HAVING):
                        VR_ADITAMENTO > 0
                        ( ( SUM(VR_REPASSE) - VR_ADITAMENTO >= 1 AND SUM(VR_REPASSE) / VR_ADITAMENTO <= 5 ) OR VR_ADITAMENTO - SUM(VR_REPASSE) >= 1 ): Critério de divergência mais complexo, considerando a relação entre a soma dos repasses e o valor do aditamento, e a diferença absoluta.
                        COUNT(VR_REPASSE) = 6: Garante que as 6 parcelas foram consideradas.
            Condições de Junção do Cursor Principal:
                L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK36, L.AA_REFERENCIA_LIBERACAO = D.AA_ADITAMENTO, semestre da liberação = D.NU_SEM_ADITAMENTO (junção da liberação com os semestres divergentes).
                L.NU_SQNCL_LIBERACAO_CONTRATO = A.NU_SQNCL_LIBERACAO_CONTRATO, L.NU_SEQ_CANDIDATO = A.NU_SEQ_CANDIDATO, L.VR_REPASSE = A.VR_REPASSE (junção da liberação com o relatório analítico).
                LEFT OUTER JOIN FES.FESTB812_CMPSO_RPSE_INDVO R ON R.NU_SQNCL_RLTRO_CTRTO_ANALITICO = A.NU_SQNCL_RLTRO_CTRTO_ANALITICO (verifica se já existe uma compensação).
            Filtros do Cursor Principal (WHERE):
                L.IC_SITUACAO_LIBERACAO IN ('R', 'NE') (liberações repassadas ou não efetivadas).
            Campos Lidos (no SELECT do cursor X):
                L.NU_SQNCL_LIBERACAO_CONTRATO, A.NU_SQNCL_RLTRO_CTRTO_ANALITICO, L.NU_SEQ_CANDIDATO, L.AA_REFERENCIA_LIBERACAO, L.MM_REFERENCIA_LIBERACAO, IC_SITUACAO_LIBERACAO, R.NU_SQNCL_RLTRO_CTRTO_ANALITICO (apelidado como SQNCL_COMPENSADO).
        Processamento no Loop (FOR X IN (...) LOOP):
            Criação de Compensação: (Lógica idêntica à Etapa 3)
                Se X.SQNCL_COMPENSADO IS NULL, incrementa QT_COMPENSACAO_CRIADA e V_NU_SQNCL_COMPENSACAO_REPASSE.
                Insere um registro na tabela FES.FESTB812_CMPSO_RPSE_INDVO com o novo sequencial, o NU_SQNCL_RLTRO_CTRTO_ANALITICO relacionado, NU_TIPO_ACERTO = 7, TS_INCLUSAO = SYSDATE e CO_USUARIO_INCLUSAO = 'CRISE19'.
                A query é gerada dinamicamente e executada.
            Atualização da Situação da Liberação: (Lógica idêntica à Etapa 3)
                Se X.IC_SITUACAO_LIBERACAO = 'R', atualiza para 'NR' (Não Repassado).
                Caso contrário (se 'NE'), atualiza para 'S' (Suspenso ou Sem Repasse).
                Define DT_ATUALIZACAO = SYSDATE.
                A atualização é feita com base na chave de identificação da liberação.
        Confirmação:
            COMMIT; executa a confirmação de todas as alterações (inserções e atualizações) para cada liberação processada no loop principal.
        Mensagem de Saída:
            DBMS_OUTPUT.PUT_LINE(' ************* QUANTIDADE DE COMPENSACOES CRIADAS: ' || QT_COMPENSACAO_CRIADA || ' ************* ');

Etapa 7: Adequação do Valor das Liberações com Problema de Divergência (Entre Repasse e Aditamento)

Similar à adequação com o contrato, este bloco recalcula e ajusta os valores de repasse para as 6 parcelas de um semestre, quando há divergência entre a soma dos repasses e o valor do aditamento.

    Processo:
        Seleção de Semestres com Divergência:
            Um cursor (FOR X IN (...)) busca os detalhes dos semestres com divergência em relação ao aditamento.
            Tabelas de Leitura:
                FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
                FES.FESTB038_ADTMO_CONTRATO (aliás A)
            Campos Lidos (no SELECT do cursor X):
                A.NU_CANDIDATO_FK36
                A.AA_ADITAMENTO
                A.NU_SEM_ADITAMENTO
                A.VR_ADITAMENTO
            Condições de Junção (INNER JOIN): As mesmas da sub-query D da Etapa 5, conectando liberações e aditamentos.
            Filtros (WHERE): Os mesmos da Etapa 5.
            Agrupamento (GROUP BY): Os mesmos da Etapa 5, para garantir que estamos considerando os totais por semestre.
            Condições de Agrupamento (HAVING): As mesmas da sub-query D da Etapa 6, que identificam a divergência entre repasse e aditamento.
        Processamento no Loop (FOR X IN (...) LOOP):
            Loop Interno (FOR Lcntr IN 1..6): Itera 6 vezes, uma para cada mês do semestre.
                Cálculo de vr_repasse: (Lógica idêntica à Etapa 4, mas utilizando X.VR_ADITAMENTO em vez de X.VR_CONTRATO)
                    Para a sexta parcela, calcula o valor residual para somar ao VR_ADITAMENTO.
                    Para as primeiras 5 parcelas, calcula a divisão truncada de VR_ADITAMENTO por 6.
                Definição de mm_repasse: (Lógica idêntica à Etapa 4, mas utilizando X.NU_SEM_ADITAMENTO em vez de X.SEMESTRE)
                MM_LIBERACAO := mm_repasse;
                Atualização do VR_REPASSE:
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO: Atualiza o VR_REPASSE e DT_ATUALIZACAO na tabela de liberações.
                    Campos Atualizados: VR_REPASSE, DT_ATUALIZACAO.
                    Condição de Atualização (WHERE): A atualização é feita com base no NU_SEQ_CANDIDATO (que agora é X.NU_CANDIDATO_FK36), AA_REFERENCIA_LIBERACAO (agora X.AA_ADITAMENTO) e MM_REFERENCIA_LIBERACAO (agora MM_LIBERACAO).
                    A query é gerada dinamicamente e executada.
        Confirmação:
            COMMIT; executa a confirmação de todas as atualizações para cada candidato/semestre (a cada 6 liberações processadas).
        Mensagem de Saída:
            DBMS_OUTPUT.PUT_LINE(' ************* FIM DA ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DIVERGENCIA NO VALOR ENTRE REPASSE E ADITAMENTO ************* ');

Finalização da Procedure e Tratamento de Exceções

    Mensagem de Conclusão:
        DBMS_OUTPUT.PUT_LINE(' ************* FIM DA FESSPZ57_CRISE2019_CORR_VLRS ************* ');
            Indica o término da execução da procedure.

    Bloco EXCEPTION:
        WHEN OTHERS THEN: Este é um bloco de tratamento de exceções genérico que captura qualquer erro que ocorra durante a execução do código.
        ROLLBACK;: Se um erro for detectado, todas as operações pendentes (que ainda não foram confirmadas por um COMMIT) são desfeitas. Isso garante a integridade dos dados, evitando que transações parciais sejam persistidas.
        DBMS_OUTPUT.PUT_LINE(' *** ERRO VERIFICADO: ' || SQLCODE || ' - ' || SUBSTR(SQLERRM, 1, 100));: Exibe o código do erro (SQLCODE) e uma parte da mensagem de erro (SQLERRM) para ajudar na depuração.
        DBMS_OUTPUT.PUT_LINE(' *** INSTRUCAO      : ' || SQL_QUERY);: Tenta exibir a última instrução SQL dinâmica que foi executada (se aplicável), o que pode ser útil para identificar onde o erro ocorreu.

Esta análise completa a quebra da procedure, cobrindo todas as suas etapas principais, as tabelas envolvidas e a lógica por trás de cada operação. A procedure é robusta em seu tratamento de divergências, garantindo a correção de dados tanto em relação ao contrato original quanto aos aditamentos, além de incluir um tratamento de erros para preservar a integridade do banco.

Você tem mais alguma parte do código que gostaria de explorar ou alguma dúvida sobre as etapas já analisadas?


O Gemini pode cometer erros, por isso, verifique as informações









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
