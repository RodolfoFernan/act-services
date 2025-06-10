<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise do Bloco de Código: Alteração de Suspensão e Vigência

Este segmento do código faz parte de uma procedure maior (provavelmente FES.FESSPZ55_CRISE2019_TRATA_SUSP, como no exemplo anterior) e foca em ajustar o tipo de suspensão e a data de início de vigência para determinadas ocorrências de contrato.
Etapa 1: Alteração do Tipo de Suspensão de Integral para Parcial

Esta etapa identifica ocorrências de suspensão que são integrais (ou nulas) e que correspondem ao semestre de contratação do candidato, alterando-as para suspensão parcial.

    Objetivo: Modificar o tipo de suspensão para 'P' (Parcial) em registros de ocorrência de contrato que originalmente eram 'I' (Integral) ou nulo, mas que se enquadram no semestre de admissão do candidato.
    Lógica Principal: Um cursor FOR X IN (...) seleciona ocorrências de contrato (FES.FESTB057_OCRRA_CONTRATO) que são do tipo 'S' (Suspensão), com status 11 (ativo), e que são do semestre de contratação do candidato (FES.FESTB010_CANDIDATO), além de terem o tipo de suspensão 'I' ou nulo e a data de ocorrência dentro do semestre de referência. Para cada registro encontrado, um UPDATE é executado na tabela FES.FESTB057_OCRRA_CONTRATO.
    Tabelas Consultadas:
        FES.FESTB057_OCRRA_CONTRATO (aliás O)
        FES.FESTB010_CANDIDATO (aliás C)
    Campos Consultados:
        De FES.FESTB057_OCRRA_CONTRATO (O):
            NU_CANDIDATO_FK36
            AA_REFERENCIA
            NU_SEMESTRE_REFERENCIA
            IC_TIPO_OCORRENCIA
            IC_TIPO_SUSPENSAO
            NU_STATUS_OCORRENCIA
            DT_OCORRENCIA
        De FES.FESTB010_CANDIDATO (C):
            NU_SEQ_CANDIDATO
            DT_ADMISSAO_CANDIDATO
    Tabela Atualizada:
        FES.FESTB057_OCRRA_CONTRATO
    Campos Atualizados:
        IC_TIPO_SUSPENSAO (para 'P')
        DT_ATUALIZACAO (definido para SYSDATE)
    Campos Usados na Cláusula WHERE da atualização:
        NU_CANDIDATO_FK36 (do X.NU_CANDIDATO_FK36)
        AA_REFERENCIA (do X.AA_REFERENCIA)
        NU_SEMESTRE_REFERENCIA (do X.NU_SEMESTRE_REFERENCIA)
        IC_TIPO_OCORRENCIA (fixo em 'S')
        NU_STATUS_OCORRENCIA (fixo em 11)

Etapa 2: Alteração da Data de Início de Vigência das Suspensões Parciais

Esta etapa atualiza a data de início de vigência de ocorrências de suspensão que já estão classificadas como parciais.

    Objetivo: Ajustar a DT_INICIO_VIGENCIA para o primeiro dia do mês seguinte à DT_OCORRENCIA para todas as suspensões parciais ativas que estão dentro dos semestres específicos e que ainda não possuem essa data de vigência correta ou está nula.
    Lógica Principal: Um único comando UPDATE direto na tabela FES.FESTB057_OCRRA_CONTRATO, sem a necessidade de um cursor, pois a lógica pode ser aplicada a todos os registros que atendem às condições.
    Tabela Atualizada:
        FES.FESTB057_OCRRA_CONTRATO
    Campos Atualizados:
        DT_INICIO_VIGENCIA (definido para LAST_DAY(DT_OCORRENCIA) + 1)
        DT_ATUALIZACAO (definido para SYSDATE)
    Campos Usados na Cláusula WHERE:
        NU_CANDIDATO_FK36 (maior que 20000000)
        IC_TIPO_OCORRENCIA (fixo em 'S')
        IC_TIPO_SUSPENSAO (fixo em 'P')
        NU_STATUS_OCORRENCIA (fixo em 11)
        DT_OCORRENCIA (usado para TO_CHAR(DT_OCORRENCIA, 'YYYY') = AA_REFERENCIA)
        AA_REFERENCIA
        NU_SEMESTRE_REFERENCIA

 Vamos destrinchar este último trecho da sua procedure, focando nas suas etapas, tabelas e campos envolvidos.
Análise do Bloco de Código: Ajuste de Datas de Suspensão e Situação de Liberação

Este segmento da sua procedure complementa os ajustes nas suspensões, primeiro atualizando a data de fim de vigência para garantir que esteja alinhada ao semestre e, em seguida, ajustando a situação das liberações com base na vigência dessas suspensões.
Etapa 1: Atualização da Data de Fim de Vigência da Suspensão

Esta etapa garante que a data de fim de vigência de suspensões parciais ativas seja o último dia do semestre de referência.

    Objetivo: Padronizar a DT_FIM_VIGENCIA das ocorrências de suspensão parcial para o final do semestre de referência (30/06 para o 1º semestre e 31/12 para o 2º semestre) se a data atual estiver incorreta ou nula.
    Lógica Principal: Um único comando UPDATE que verifica ocorrências de contrato (FES.FESTB057_OCRRA_CONTRATO) do tipo 'S' (Suspensão), classificadas como 'P' (Parcial), com status 11 (ativo), e cuja DT_INICIO_VIGENCIA é posterior à DT_OCORRENCIA (já ajustada na etapa anterior), além de a DT_OCORRENCIA estar dentro de períodos específicos (não em junho ou dezembro, o que sugere um ajuste para essas datas). A atualização só ocorre se a DT_FIM_VIGENCIA não corresponder ao mês final do semestre.
    Tabelas Consultadas: Nenhuma explicitamente, o UPDATE atua sobre a tabela FES.FESTB057_OCRRA_CONTRATO e seus próprios campos.
    Tabela Atualizada:
        FES.FESTB057_OCRRA_CONTRATO
    Campos Atualizados:
        DT_FIM_VIGENCIA (definido por uma CASE expression: TO_DATE('0630' || TO_CHAR(DT_OCORRENCIA, 'YYYY'), 'MMDDYYYY') para 1º semestre ou TO_DATE('1231' || TO_CHAR(DT_OCORRENCIA, 'YYYY'), 'MMDDYYYY') para 2º semestre)
    Campos Usados na Cláusula WHERE:
        NU_CANDIDATO_FK36 (maior que 20000000)
        IC_TIPO_OCORRENCIA (fixo em 'S')
        IC_TIPO_SUSPENSAO (fixo em 'P')
        NU_STATUS_OCORRENCIA (fixo em 11)
        TO_CHAR(DT_OCORRENCIA, 'YYYY')
        AA_REFERENCIA
        DT_INICIO_VIGENCIA
        DT_OCORRENCIA
        NU_SEMESTRE_REFERENCIA
        DT_FIM_VIGENCIA

Etapa 2: Atualização da Situação da Liberação Conforme Vigência da Suspensão

Esta etapa ajusta a situação de liberações de contrato com base nas datas de início e fim de vigência das suspensões parciais associadas.

    Objetivo: Modificar o IC_SITUACAO_LIBERACAO de liberações para refletir corretamente o status de suspensão, considerando o período de vigência da ocorrência de suspensão parcial. Liberações dentro do período de suspensão se tornam 'S' (Suspenso) ou 'NE' (Não Estornado), e liberações fora do período voltam a ser 'NR' (Não Repassado) ou 'R' (Repassado).
    Lógica Principal: Um cursor FOR X IN (...) seleciona liberações (FES.FESTB712_LIBERACAO_CONTRATO) que estão relacionadas a ocorrências de suspensão parcial (FES.FESTB057_OCRRA_CONTRATO) com vigências ajustadas. Para cada liberação, a lógica verifica se a DT_LIBERACAO cai dentro ou antes do período de vigência da suspensão e atualiza a IC_SITUACAO_LIBERACAO apropriadamente.
    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
        FES.FESTB057_OCRRA_CONTRATO (aliás O)
    Campos Consultados:
        De FES.FESTB712_LIBERACAO_CONTRATO (L):
            NU_SQNCL_LIBERACAO_CONTRATO
            IC_SITUACAO_LIBERACAO
            DT_LIBERACAO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
        De FES.FESTB057_OCRRA_CONTRATO (O):
            NU_CANDIDATO_FK36
            AA_REFERENCIA
            NU_SEMESTRE_REFERENCIA
            IC_TIPO_OCORRENCIA
            IC_TIPO_SUSPENSAO
            NU_STATUS_OCORRENCIA
            DT_OCORRENCIA
            DT_INICIO_VIGENCIA
            DT_FIM_VIGENCIA
    Tabela Atualizada:
        FES.FESTB712_LIBERACAO_CONTRATO
    Campos Atualizados:
        IC_SITUACAO_LIBERACAO (para 'S', 'NE', 'NR' ou 'R', dependendo das condições)
        DT_ATUALIZACAO (definido para SYSDATE)
    Campos Usados na Cláusula WHERE da atualização:
        NU_SQNCL_LIBERACAO_CONTRATO (do X.NU_SQNCL_LIBERACAO_CONTRATO)

Análise dos Blocos de Código: Ajustes Finais de Encerramento e Tratamento de Erros

Este segmento final da procedure conclui as operações de ajuste, focando nas datas de vigência de encerramentos, na situação das liberações relacionadas a esses encerramentos, e na adequação de situações de liberações que não possuem lançamentos pertinentes. Ele também inclui o bloco de tratamento de exceções.
Etapa 1: Alteração da Data de Início de Vigência dos Encerramentos

Esta etapa garante que a data de início de vigência para ocorrências de contrato do tipo 'E' (Encerramento) esteja padronizada para o primeiro dia do mês seguinte à DT_OCORRENCIA.

    Objetivo: Padronizar a DT_INICIO_VIGENCIA para ocorrências de encerramento (IC_TIPO_OCORRENCIA = 'E') ativas (NU_STATUS_OCORRENCIA = 11), onde a data de ocorrência está dentro do ano e semestre de referência, e a DT_INICIO_VIGENCIA ainda não está corretamente definida (ou é nula). A lógica também exige que haja um aditamento com status superior a 3 ou um candidato com data de admissão no mesmo semestre.
    Lógica Principal: Um cursor FOR X IN (...) seleciona ocorrências de encerramento que atendem às condições de ano e semestre, que têm a DT_INICIO_VIGENCIA desatualizada e que possuem um aditamento (FES.FESTB038_ADTMO_CONTRATO) ou um registro de candidato (FES.FESTB010_CANDIDATO) associado. Para cada ocorrência, o UPDATE define a DT_INICIO_VIGENCIA para o primeiro dia do mês subsequente à DT_OCORRENCIA.
    Tabelas Consultadas:
        FES.FESTB057_OCRRA_CONTRATO (aliás O)
        FES.FESTB038_ADTMO_CONTRATO (aliás A, LEFT OUTER JOIN)
        FES.FESTB010_CANDIDATO (aliás C, LEFT OUTER JOIN)
    Campos Consultados:
        De FES.FESTB057_OCRRA_CONTRATO (O):
            NU_CANDIDATO_FK36
            AA_REFERENCIA
            NU_SEMESTRE_REFERENCIA
            DT_OCORRENCIA
            IC_TIPO_OCORRENCIA
            NU_STATUS_OCORRENCIA
            DT_INICIO_VIGENCIA
        De FES.FESTB038_ADTMO_CONTRATO (A):
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            NU_STATUS_ADITAMENTO
        De FES.FESTB010_CANDIDATO (C):
            NU_SEQ_CANDIDATO
            DT_ADMISSAO_CANDIDATO
    Tabela Atualizada:
        FES.FESTB057_OCRRA_CONTRATO
    Campos Atualizados:
        DT_INICIO_VIGENCIA (para LAST_DAY(X.DT_OCORRENCIA) + 1)
        DT_ATUALIZACAO (para SYSDATE)

Etapa 2: Atualização da Situação da Liberação Conforme Vigência do Encerramento

Esta etapa ajusta a situação das liberações de contrato com base nas datas de início de vigência dos encerramentos associados.

    Objetivo: Alterar o IC_SITUACAO_LIBERACAO de liberações de contrato para refletir o status de encerramento, especificamente quando a data de liberação (DT_LIBERACAO) é posterior ou anterior à DT_INICIO_VIGENCIA de um encerramento.
    Lógica Principal: Um cursor FOR X IN (...) seleciona liberações (FES.FESTB712_LIBERACAO_CONTRATO) que correspondem a ocorrências de encerramento (FES.FESTB057_OCRRA_CONTRATO) com status 11 (ativo) e data de ocorrência dentro do ano/semestre de referência, e cuja DT_INICIO_VIGENCIA já foi ajustada (primeiro dia do mês seguinte à ocorrência).
        Se a DT_LIBERACAO for posterior à DT_INICIO_VIGENCIA:
            Se a situação atual for 'NR' (Não Repassado), muda para 'S' (Suspenso).
            Se for 'R' (Repassado), muda para 'NE' (Não Estornado).
        Se a DT_LIBERACAO for anterior à DT_INICIO_VIGENCIA:
            Se a situação atual for 'S' (Suspenso), muda para 'NR' (Não Repassado).
            Se for 'NE' (Não Estornado), muda para 'R' (Repassado).
    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
        FES.FESTB057_OCRRA_CONTRATO (aliás O)
    Campos Consultados:
        De FES.FESTB712_LIBERACAO_CONTRATO (L):
            NU_SQNCL_LIBERACAO_CONTRATO
            DT_LIBERACAO
            IC_SITUACAO_LIBERACAO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
        De FES.FESTB057_OCRRA_CONTRATO (O):
            NU_CANDIDATO_FK36
            AA_REFERENCIA
            NU_SEMESTRE_REFERENCIA
            IC_TIPO_OCORRENCIA
            NU_STATUS_OCORRENCIA
            DT_OCORRENCIA
            DT_INICIO_VIGENCIA
    Tabela Atualizada:
        FES.FESTB712_LIBERACAO_CONTRATO
    Campos Atualizados:
        IC_SITUACAO_LIBERACAO (alterado para 'S', 'NE', 'NR' ou 'R')
        DT_ATUALIZACAO (para SYSDATE)

Etapa 3: Adequação da Situação das Liberações 'NE' e 'S' sem Lançamentos Pertinentes

Essas duas subetapas finais ajustam as situações de liberações (IC_SITUACAO_LIBERACAO = 'NE' ou 'S') que, após as operações anteriores, não possuem um aditamento ativo, um contrato FIES ativo ou uma ocorrência de contrato ativa correspondente, revertendo-as para R (Repassado) ou NR (Não Repassado).

    Objetivo: Limpar inconsistências onde liberações estão marcadas como "Não Estornadas" ('NE') ou "Suspensas" ('S'), mas não há justificativa em outros registros relacionados (aditamentos, contratos FIES, ou ocorrências de contrato ativas).
    Lógica Principal:
        Para 'NE': Um cursor seleciona liberações em situação 'NE' que possuem um aditamento finalizado (status > 3) OU um candidato com contrato FIES ativo (status > 3), mas não possuem uma ocorrência de contrato (suspensão/encerramento) ativa (NU_STATUS_OCORRENCIA = 11). Essas são revertidas para 'R' (Repassado).
        Para 'S': Um cursor seleciona liberações em situação 'S' que possuem um aditamento finalizado OU um candidato com contrato FIES ativo, mas não possuem uma ocorrência de contrato (suspensão/encerramento) ativa (NU_STATUS_OCORRENCIA in (11, 18)). Essas são revertidas para 'NR' (Não Repassado).
    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
        FES.FESTB038_ADTMO_CONTRATO (aliás A, LEFT OUTER JOIN)
        FES.FESTB010_CANDIDATO (aliás C, LEFT OUTER JOIN)
        FES.FESTB036_CONTRATO_FIES (aliás F, LEFT OUTER JOIN)
        FES.FESTB057_OCRRA_CONTRATO (aliás O, LEFT OUTER JOIN)
    Campos Consultados:
        De FES.FESTB712_LIBERACAO_CONTRATO (L):
            NU_SQNCL_LIBERACAO_CONTRATO
            IC_SITUACAO_LIBERACAO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
        Das tabelas A, C, F, O: Seus respectivos IDs e status para as condições IS NOT NULL e IS NULL.
    Tabela Atualizada:
        FES.FESTB712_LIBERACAO_CONTRATO
    Campos Atualizados:
        IC_SITUACAO_LIBERACAO (para 'R' ou 'NR')
        DT_ATUALIZACAO (para SYSDATE)

Etapa 4: Finalização e Tratamento de Exceções

Este é o bloco de encerramento da procedure, que garante a consistência dos dados e o tratamento de erros.

    Objetivo: Confirmar todas as alterações realizadas durante a execução bem-sucedida da procedure e fornecer um mecanismo robusto para capturar e registrar erros, desfazendo quaisquer operações não confirmadas em caso de falha.
    Ações:
        Executa um COMMIT final após a conclusão bem-sucedida de todas as operações.
        Imprime mensagens de finalização para cada bloco de processamento e para a procedure como um todo.
        O bloco EXCEPTION WHEN OTHERS THEN captura qualquer erro não tratado durante a execução.
        Em caso de erro, executa um ROLLBACK para desfazer todas as transações pendentes e imprime o código e a mensagem de erro (SQLCODE, SQLERRM), além da última instrução SQL (SQL_QUERY) que estava sendo executada.
        Finaliza com uma mensagem de fim de procedure, mesmo em caso de erro.

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
