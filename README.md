<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise Detalhada da Procedure FES.FESSPZ57_CRISE2019_CORR_VLRS por Etapas (Continuação)

Vamos continuar a análise do script PL/SQL, focando nos próximos blocos que lidam com compensação e novo repasse para liberações com divergência de valores e a adequação do valor das liberações nestes casos.
Etapa 3: Compensação e Novo Repasse de Liberações com Divergência de Valores

Este bloco tem o objetivo de registrar uma compensação para repasses que tiveram divergência de valores em relação ao contrato e, em seguida, redefinir a situação da liberação para permitir um novo repasse.

    Variáveis Utilizadas:
        QT_COMPENSACAO_CRIADA: Contador para o número de compensações criadas neste bloco.
        V_NU_SQNCL_COMPENSACAO_REPASSE: Variável para armazenar o próximo sequencial de compensação. (É assumido que esta variável é inicializada em outro lugar da procedure, possivelmente obtendo o MAX da tabela FESTB812_CMPSO_RPSE_INDVO e incrementando-o).

    Processo:
        Seleção de Liberações com Divergência:
            Um cursor (FOR X IN (...)) busca as liberações de contrato que possuem divergência entre o valor do repasse e o valor do contrato.
            Tabelas de Leitura:
                FES.FESTB712_LIBERACAO_CONTRATO (aliás L): Tabela principal de liberações.
                FES.FESTB711_RLTRO_CTRTO_ANLTO (aliás A): Relatório analítico do contrato.
                FES.FESTB812_CMPSO_RPSE_INDVO (aliás R): Tabela de compensação de repasse individual (usada com LEFT OUTER JOIN para verificar se já existe uma compensação).
                Sub-query D (interna ao FROM): Identifica os semestres com divergência entre a soma dos repasses e o valor do contrato.
                    Tabelas da Sub-query D: FES.FESTB712_LIBERACAO_CONTRATO, FES.FESTB036_CONTRATO_FIES, FES.FESTB010_CANDIDATO.
                    Condições de Junção da Sub-query D:
                        L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK11 (Liberação com Contrato FIES).
                        C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO (Candidato com Liberação).
                        L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY') e semestre da liberação = semestre de admissão do candidato.
                    Filtros da Sub-query D (WHERE):
                        L.NU_SEQ_CANDIDATO > 20000000
                        L.MM_REFERENCIA_LIBERACAO > 0
                        A.NU_STATUS_CONTRATO > 3 (apenas contratos com status válidos).
                    Agrupamento da Sub-query D (GROUP BY): Por CO_CPF, NU_CANDIDATO_FK11, VR_CONTRATO, ano e semestre de admissão.
                    Condições de Agrupamento da Sub-query D (HAVING):
                        VR_CONTRATO >= 1000 (Considera apenas contratos com valor mínimo de R$ 1000).
                        ( SUM(VR_REPASSE) - VR_CONTRATO > 1 OR VR_CONTRATO - SUM(VR_REPASSE) > 1 ) (diferença absoluta maior que R$ 1,00).
                        COUNT(L.VR_REPASSE) = 6 (garante que todas as 6 parcelas do semestre foram consideradas).
            Condições de Junção do Cursor Principal:
                L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK11, L.AA_REFERENCIA_LIBERACAO = D.ANO, semestre da liberação = D.SEMESTRE (junção da liberação com os semestres identificados na sub-query D).
                L.NU_SQNCL_LIBERACAO_CONTRATO = A.NU_SQNCL_LIBERACAO_CONTRATO, L.NU_SEQ_CANDIDATO = A.NU_SEQ_CANDIDATO, L.VR_REPASSE = A.VR_REPASSE (junção da liberação com o relatório analítico).
                LEFT OUTER JOIN FES.FESTB812_CMPSO_RPSE_INDVO R ON R.NU_SQNCL_RLTRO_CTRTO_ANALITICO = A.NU_SQNCL_RLTRO_CTRTO_ANALITICO (verifica se já existe uma compensação para aquele item do relatório analítico).
            Filtros do Cursor Principal (WHERE):
                L.IC_SITUACAO_LIBERACAO IN ('R', 'NE') (apenas liberações com situação 'Repassado' ou 'Não Efetivado').
            Campos Lidos (no SELECT do cursor X):
                L.NU_SQNCL_LIBERACAO_CONTRATO
                A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
                L.NU_SEQ_CANDIDATO
                L.AA_REFERENCIA_LIBERACAO
                L.MM_REFERENCIA_LIBERACAO
                L.IC_SITUACAO_LIBERACAO
                R.NU_SQNCL_RLTRO_CTRTO_ANALITICO (apelidado como SQNCL_COMPENSADO para verificar a existência de compensação).
        Processamento no Loop (FOR X IN (...) LOOP):
            Criação de Compensação:
                IF X.SQNCL_COMPENSADO IS NULL THEN: Verifica se não existe uma compensação prévia para este registro analítico.
                QT_COMPENSACAO_CRIADA := QT_COMPENSACAO_CRIADA + 1;: Incrementa o contador de compensações criadas.
                V_NU_SQNCL_COMPENSACAO_REPASSE := V_NU_SQNCL_COMPENSACAO_REPASSE + 1;: Incrementa o sequencial da compensação.
                INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO: Insere um novo registro na tabela de compensação.
                    Campos Inseridos:
                        NU_SQNCL_COMPENSACAO_REPASSE: Novo sequencial da compensação.
                        NU_SQNCL_RLTRO_CTRTO_ANALITICO: O sequencial do relatório analítico relacionado.
                        NU_TIPO_ACERTO: Definido como 7 (presumivelmente um código para este tipo de acerto/compensação).
                        TS_INCLUSAO: SYSDATE (timestamp de inclusão).
                        CO_USUARIO_INCLUSAO: 'CRISE19' (usuário que realizou a operação).
                    A query é gerada dinamicamente (SQL_QUERY) e executada (EXECUTE IMMEDIATE).
            Atualização da Situação da Liberação:
                IF X.IC_SITUACAO_LIBERACAO = 'R' THEN: Se a situação atual da liberação é 'Repassado'.
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET IC_SITUACAO_LIBERACAO = 'NR': Altera a situação para 'Não Repassado'.
                    DT_ATUALIZACAO = SYSDATE.
                ELSE (ou seja, X.IC_SITUACAO_LIBERACAO = 'NE'): Se a situação atual é 'Não Efetivado'.
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET IC_SITUACAO_LIBERACAO = 'S': Altera a situação para 'Suspenso' (ou 'Sem Repasse', dependendo do significado de 'S').
                    DT_ATUALIZACAO = SYSDATE.
                Condição de Atualização (WHERE): A atualização é feita com base na chave primária da tabela FESTB712_LIBERACAO_CONTRATO para garantir a modificação do registro correto.
        Confirmação:
            COMMIT; executa a confirmação de todas as alterações (inserções de compensação e atualizações de situação) para cada liberação processada no loop principal.
        Mensagem de Saída:
            DBMS_OUTPUT.PUT_LINE(' ************* QUANTIDADE DE COMPENSACOES CRIADAS: ' || QT_COMPENSACAO_CRIADA || ' ************* '); exibe o total de compensações geradas.

Etapa 4: Adequação do Valor das Liberações com Problema de Divergência no Valor entre Repasse e a Contratação (Novo Repasse)

Este bloco tem a finalidade de recalcular e ajustar o valor de repasse para as 6 parcelas de um semestre, quando o semestre foi identificado com uma divergência significativa entre a soma dos repasses e o valor do contrato FIES. Essencialmente, ele força o valor do repasse a ser o valor do contrato dividido por 6.

    Processo:
        Seleção de Semestres com Divergência:
            Um cursor (FOR X IN (...)) busca os detalhes dos semestres com divergência.
            Tabelas de Leitura:
                FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
                FES.FESTB036_CONTRATO_FIES (aliás A)
                FES.FESTB010_CANDIDATO (aliás C)
            Campos Lidos (no SELECT do cursor X):
                A.NU_CANDIDATO_FK11
                TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY') (apelidado como ANO)
                (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END) (apelidado como SEMESTRE)
                A.VR_CONTRATO
            Condições de Junção (INNER JOIN): As mesmas da sub-query D do bloco anterior, conectando liberações, contratos e candidatos.
            Filtros (WHERE): Os mesmos da sub-query D do bloco anterior.
            Agrupamento (GROUP BY): Os mesmos da sub-query D do bloco anterior, para garantir que estamos considerando os totais por semestre.
            Condições de Agrupamento (HAVING): As mesmas da sub-query D do bloco anterior, que identificam a divergência (contrato >= 1000, diferença absoluta > 1, e 6 repasses para o semestre).
        Processamento no Loop (FOR X IN (...) LOOP):
            Loop Interno (FOR Lcntr IN 1..6): Itera 6 vezes, uma para cada mês do semestre.
                Cálculo de vr_repasse:
                    IF (Lcntr IN (6)) THEN: Para a sexta parcela, calcula o valor residual para garantir que a soma total seja igual ao VR_CONTRATO.
                        vr_repasse := (X.VR_CONTRATO - (TRUNC(X.VR_CONTRATO / 6, 2) * 5) );
                    ELSE: Para as primeiras 5 parcelas, calcula a divisão truncada.
                        vr_repasse := TRUNC(X.VR_CONTRATO / 6, 2);
                Definição de mm_repasse: Determina o mês de referência da liberação (1 a 6 para o 1º semestre, 7 a 12 para o 2º semestre).
                MM_LIBERACAO := mm_repasse;
                Atualização do VR_REPASSE:
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO: Atualiza o VR_REPASSE e DT_ATUALIZACAO na tabela de liberações.
                    Campos Atualizados: VR_REPASSE, DT_ATUALIZACAO.
                    Condição de Atualização (WHERE): A atualização é feita com base no NU_SEQ_CANDIDATO, AA_REFERENCIA_LIBERACAO e MM_REFERENCIA_LIBERACAO para identificar a liberação específica a ser corrigida.
                    A query é gerada dinamicamente (SQL_QUERY) e executada (EXECUTE IMMEDIATE).
        Confirmação:
            COMMIT; executa a confirmação de todas as atualizações para cada candidato/semestre (a cada 6 liberações processadas).
        Mensagem de Saída:
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
