<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:
Excelente! Vamos detalhar a FES.FESSPU20_VINCULA_LIBERACAO no formato solicitado:
FES.FESSPU20_VINCULA_LIBERACAO

    Objetivo: Vincular as liberações de contrato a seus respectivos aditamentos ou contratos iniciais, marcando o tipo de transação e a participação do candidato. Além disso, a SP insere retenções para liberações que não conseguem ser vinculadas.

Entendendo as Tabelas e o Processo de Integralização

O problema que você teve ao carregar um índice da FESTB069_EXTRATO_SIAPI durante a execução da rotina fesTotal é um sintoma claro de uma dependência. Mesmo que a fesTotal não faça algo "diretamente" com a tabela, se ela dropa e recria índices, e o FESTB069_EXTRATO_SIAPI é fundamental para um processo de batch que roda em paralelo ou logo depois, a performance e a integridade dos dados podem ser comprometidas.

Vamos detalhar cada tabela e os métodos mencionados:
1. FESTB062_CONTRATO_SIAPI

    Finalidade da Tabela: Esta tabela é central para armazenar informações de contratos SIAPI. O SIAPI (Sistema Integrado de Acompanhamento e Pagamento de Incentivos) é provavelmente o sistema que gerencia os pagamentos, aditamentos e o status geral dos contratos de financiamento estudantil. Nela você encontraria detalhes como número do contrato, valores, datas de assinatura, prazos, dados das mantenedoras e dos estudantes vinculados a cada contrato. É a fonte primária de dados contratuais.
    Conexão com fesTotal: A fesTotal pode não manipular dados diretamente nesta tabela, mas ao drop e recriar índices, ela afeta a performance de qualquer consulta ou operação que use esses contratos, incluindo o processo de cálculo de integralização.
    CalculaPercentualIntegralizacaoImpl: Esta classe é responsável por implementar a lógica de cálculo do percentual de integralização. Ela, muito provavelmente, consulta a FESTB062_CONTRATO_SIAPI para obter os dados base de cada contrato que serão usados nos cálculos de índices de inadimplência e evasão.

        isAtualizarPercentualMantenedora:
            Uso: Pelo contexto do método calculoPercentualMantenedoras e o debug que você forneceu, essa função determina se o percentual de integralização de uma mantenedora específica deve ser atualizado no momento. Isso é crucial porque o percentual pode não precisar ser recalculado para todas as mantenedoras em todos os semestres.
            Necessidade: Ela verifica a posição do ano da mantenedora (anoPosicaoMantenedora) em relação ao semestre de adesão e ao semestre de referência atual (SEMESTRE, ANO). Isso indica que existem regras específicas sobre quando um percentual de integralização é válido para atualização, evitando recálculos desnecessários ou indevidos. Por exemplo, pode haver uma lógica de que o percentual só é atualizado após um certo número de meses ou anos da adesão inicial da mantenedora.

        consultaValoresCoparticipacao:
            Uso: Conforme o comentário no código, este método busca os valores necessários para calcular o índice de inadimplência (c). Ele obtém o "Saldo de Coparticipação em atraso superior a 90 (noventa) dias" e o "Saldo Total de Coparticipação".
            COPARTICIPACAO ADIMPLENTE: Este termo parece ser uma query ou constante que define a lógica para buscar os valores de coparticipação (a parcela que o estudante paga diretamente à instituição de ensino). "Adimplente" se refere a pagamentos em dia, enquanto "inadimplente" se refere a pagamentos atrasados. A proporção desses valores define o índice 'c'.

2. FESTB069_EXTRATO_SIAPI

    Finalidade da Tabela: Esta tabela armazena dados de extrato de movimentações financeiras relacionadas aos contratos SIAPI. Cada registro representa uma transação, pagamento, liberação, ou outro evento financeiro de um contrato. É aqui que se rastreia o histórico de pagamentos e dívidas.

    Problema de Carregamento: O fato de não ter carregado o índice durante o fesTotal aponta para um problema sério. Se o índice não foi recriado ou foi corrompido, consultas que dependem dele (como a de inadimplência) terão performance degradada ou poderão falhar.

    Utilização em CalculaPercentualIntegralizacaoImpl:
        consultaValoresCoparticipacao: Conforme o problema da fesTotal, esta é a principal conexão. A query consulta.qtde.inadimplencia que você forneceu claramente usa a FESTB069_EXTRATO_SIAPI (T69) para contar a quantidade de CPFs/CNPJs inadimplentes.
        COPARTICIPACAO ADIMPLENTE: Reforça o uso desta tabela para segregar dados de coparticipação em dia ou em atraso.

    Necessidade no Processo de Cálculo de Integralização: Esta tabela é CRÍTICA para calcular o índice de inadimplência (c). Ela fornece os dados sobre quais estudantes estão com saldo de coparticipação em atraso. Sem dados consistentes e acessíveis via índice da FESTB069_EXTRATO_SIAPI, o cálculo do índice 'c' será incorreto ou ineficiente, impactando diretamente o Percentual Integralização das Mantenedoras.

3. FESTB071_LIBERACAO_SIAPI

    Finalidade da Tabela: Esta tabela provavelmente registra as liberações de valores ou parcelas dos financiamentos SIAPI. Pode conter informações sobre as datas de liberação, os valores liberados e o contrato associado.
    Conexão com fesTotal e Batch: A anotação "Nada no Batch" e "Nada no Batch" significa que esta tabela não está diretamente envolvida nos processos de batch ou na rotina calculoPercentualMantenedoras que você detalhou. Se a fesTotal está dropando e recriando índices dela, mas ela não é usada nos processos que você está investigando, pode ser uma tabela de um módulo diferente que é afetado pela rotina de manutenção de índices.
    Necessidade no Processo: Pelas informações fornecidas, a FESTB071_LIBERACAO_SIAPI não é necessária para o cálculo do percentual de integralização que você descreveu. No entanto, ela pode ser vital para outros processos de acompanhamento do financiamento, como o pagamento de parcelas ou a visualização do histórico de desembolsos para o estudante ou a IES.

O Processo de Cálculo de Integralização (calculoPercentualMantenedoras)

O método calculoPercentualMantenedoras que você compartilhou é a orquestração central para a qual todas essas tabelas e métodos convergem.

    Iteração por Mantenedora: O método percorre uma listaAdesaoMantenedoras, o que significa que o cálculo é feito individualmente para cada mantenedora que aderiu ao programa.
    Determinação do Ano/Semestre: Calcula o anoPosicaoMantenedora com base na diferença de meses entre o semestre de adesão da mantenedora e o semestre de referência atual. Isso é fundamental para aplicar regras de cálculo específicas para cada "ano de vida" da adesão da mantenedora.
    isAtualizarPercentualMantenedora: Decide se o cálculo será persistido ou não, baseado na elegibilidade da mantenedora para atualização naquele momento. Isso evita que os percentuais sejam atualizados com muita frequência ou em momentos inadequados.
    Cálculo do Índice de Inadimplência (c): Chama consultaValoresCoparticipacao (que usa FESTB069_EXTRATO_SIAPI) para obter os dados de coparticipação em atraso e total, e com isso calcula o indiceInadimplencia.
    Cálculo do Índice de Evasão (e): Chama consultaQuantidadeContratos (provavelmente usando FESTB062_CONTRATO_SIAPI ou tabelas relacionadas a alunos/cursos) para obter a "quantidade de estudantes com parcela em atraso não aditados" e a "quantidade total de contratos". Com isso, calcula o indiceEvasao.
    Cálculo do Valor x: Aplica uma fórmula x = α x c + ß * e, onde α (alfa) e ß (beta) são pesos definidos nos ParametrosIntegralizacaoTO. Este valor x é um indicador composto da saúde financeira da mantenedora em relação aos financiamentos.
    Persistência (FESTB842): O IntegralizacaoAdesaoMantenedoraTO é persistido/atualizado na tabela FESTB842. Esta tabela armazena os resultados dos cálculos para cada mantenedora em cada período, incluindo o valor de x e o percentual de integralização final. O anoPosicaoMantenedora negativo para "controle" é uma flag para indicar que não é um cálculo oficial para acompanhamento da tela.
    Cálculo de Média e Desvio Padrão de x: Após calcular x para todas as mantenedoras, o sistema calcula a média e o desvio padrão de x para o conjunto. Isso pode ser usado para normalizar ou comparar o desempenho das mantenedoras.
    Cálculo e Atualização do Percentual de Integralização Final:
        Para o primeiro ano, o percentual é fixo (PC_INTEGRALIZACAO_PRIMEIRO_ANO).
        Para os anos 2 a 5, calcularPercentualIntegralizacao é chamado.
        Para os anos 6 a 7, calcularPercentualIntegralizacao6ao7Ano é chamado.
        O percentual calculado é então atualizado na FESTB842 (via entityManager.merge) e também na FESTB156 (via atualizarPercentualMantenedora), que provavelmente é uma tabela de cadastro da mantenedora que precisa ter o percentual atualizado.

Conexão fesTotal e o Índice FESTB069_EXTRATO_SIAPI

A fesTotal é uma rotina de manutenção crítica. Se ela dropa e recria índices, e o índice da FESTB069_EXTRATO_SIAPI não está sendo recriado corretamente ou há um conflito de concorrência com o batch de integralização, isso explica por que você teve problemas.

Motivo do problema:
O processo de cálculo de integralização (seu batch) é altamente dependente da FESTB069_EXTRATO_SIAPI para o cálculo da inadimplência. Se a fesTotal deixa essa tabela sem um índice ou com um índice corrompido, as consultas dela se tornam extremamente lentas ou falham (ex: NoResultException, TooManyRowsException ou apenas timeouts de query), impactando o cálculo subsequente.

Necessidade da fesTotal para o processo:
A rotina fesTotal é necessária para a saúde e performance geral do banco de dados. Índices ficam fragmentados, dados mudam, e a recriação deles é uma prática comum de DBA para otimizar o acesso. A questão não é se ela deve rodar, mas como ela interage com processos dependentes.

Solução para o problema:
É fundamental investigar por que o índice da FESTB069_EXTRATO_SIAPI não está sendo carregado corretamente pela fesTotal ou por que há um conflito. As possíveis causas podem ser:

    Erro na fesTotal: Bug na rotina de recriação de índice para essa tabela específica.
    Concorrência: O batch de cálculo de integralização pode estar tentando acessar a tabela enquanto a fesTotal está manipulando os índices, causando bloqueios ou leituras incompletas.
    Recursos: Falta de recursos (memória, CPU, I/O) no servidor de banco de dados durante a execução da fesTotal, impedindo a conclusão da recriação do índice.

Recomendação:
Verifique os logs da execução da fesTotal especificamente para a FESTB069_EXTRATO_SIAPI. Coordenar os horários de execução do fesTotal e do batch de integralização pode ser necessário, talvez rodando o fesTotal em um período de menor carga ou garantindo que o batch só inicie após a conclusão da manutenção de índices.

Espero que essa análise ajude a clarear as conexões e a importância de cada parte do seu sistema!



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
