<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:

Algumas diretrizes para a compensação de valores:
1 - A dinâmica de compensação dos valores é realizada por intermédio da fesRepasse quando da apuração em moeda dos valores a serem repassados às Mantenedoras;
2 – O direcionamento dos valores, ou repasses, a serem compensados é realizado por intermédio da inserção dos sequenciais dos repasses na tabela FESTB812_CMPSO_RPSE_INDVO, destinada para este fim;
3 - Cabe ressaltar que o que se compensa são os repasses das liberações e não as liberações propriamente dita;
4 - Os valores, ou repasses, a serem compensados se resumem aos repasses realizados anteriormente, e indevidamente, às Mantenedoras;
5 - Negocialmente, um semestre contratual para um candidato contém 6 (seis) parcelas a serem repassadas à Mantenedora. Dessa forma, não deve existir mais de um repasse para cada parcela de um mesmo período/semestre;
6 - Sendo assim, as compensações geralmente se devem à recuperação de valores/repasses realizados em multiplicidade;
7 - Outra forma preconizada se deve à necessidade de correção do valor, a maior ou a menor, repassado à Mantenedora. Nessa modalidade se compensa o valor repassado anteriormente e realiza-se um novo repasse com os valores adequados.
8 – Não se deve direcionar o mesmo sequencial de repasse mais de uma vez à tabela FESTB812_CMPSO_RPSE_INDVO sob pena de compensar o mesmo repasse reiteradamente.
 
As diretrizes acima devem ser observadas quando da construção de uma solução para a realização de compensações e a seleção dos valores a serem compensados deve ser discutida com os gestores, haja vista as particularidades que cada problema de repasses indevidos apresenta.

Ótimo. Você estruturou muito bem o problema e reuniu uma grande quantidade de informações relevantes. Abaixo eu organizei a análise do cenário, validei o seu entendimento, e proponho uma abordagem técnica para a solução, conforme solicitado:

Algumas diretrizes para a compensação de valores:
1 - A dinâmica de compensação dos valores é realizada por intermédio da fesRepasse quando da apuração em moeda dos valores a serem repassados às Mantenedoras;
2 – O direcionamento dos valores, ou repasses, a serem compensados é realizado por intermédio da inserção dos sequenciais dos repasses na tabela FESTB812_CMPSO_RPSE_INDVO, destinada para este fim;
3 - Cabe ressaltar que o que se compensa são os repasses das liberações e não as liberações propriamente dita;
4 - Os valores, ou repasses, a serem compensados se resumem aos repasses realizados anteriormente, e indevidamente, às Mantenedoras;
5 - Negocialmente, um semestre contratual para um candidato contém 6 (seis) parcelas a serem repassadas à Mantenedora. Dessa forma, não deve existir mais de um repasse para cada parcela de um mesmo período/semestre;
6 - Sendo assim, as compensações geralmente se devem à recuperação de valores/repasses realizados em multiplicidade;
7 - Outra forma preconizada se deve à necessidade de correção do valor, a maior ou a menor, repassado à Mantenedora. Nessa modalidade se compensa o valor repassado anteriormente e realiza-se um novo repasse com os valores adequados.
8 – Não se deve direcionar o mesmo sequencial de repasse mais de uma vez à tabela FESTB812_CMPSO_RPSE_INDVO sob pena de compensar o mesmo repasse reiteradamente.
 
As diretrizes acima devem ser observadas quando da construção de uma solução para a realização de compensações e a seleção dos valores a serem compensados deve ser discutida com os gestores, haja vista as particularidades que cada problema de repasses indevidos apresenta.

=====================================================================================================================
oque é Aditamento Renovação Semestral ? :
      Contratação de Renovação semestral pelo aluno junto ao Ies, o ato do Aluno Renovar o contrato 

Oque seria Aditamento Liberação ?==================================================
      Liberação são os dados VRrepasse + dados contrato + datas + NU_SQNCL_LIBERACAO_CONTRATO , possibilitando as IES receberem os valores de acordo com contrato e parcelas correspondetes.
e 
Como é gerado as Liberações ?======================================================
      As liberações são gerados a partir de aditamentos , no caso semestral para o contrato 


Como é feito o repasse ?===========================================================
      O repasse é feito buscando informações do contrato e Idunico,  Vr repasse (calculo) validações pela processo Batch
         Validar regras de cálculos e validações feitas no repasse e apuração repasse , para que seja autorizado o repasse
oque é relatório analítico ================================================
         O relatório Analítico é criado após o repasse Buscando informações da 712 (Liberação) e criando um Histórico na 711(Analitico) com um novo NU_SQNCL_RLTRO_CTRTO_ANALITICO +informações contrato + Tipo transação + vr repasse ...
         Sendo gerado um estrato 
==================================
---------------------------------------Entender o porque ouve a exclusão ? são duas etapaz a exlusão e recriação com outro Id----------------------------------------------
   toda vez que acontecia o aditamento renovação para aquele contrato era verificado a existência , no código quando ele iria salvar um novo aditamento para aquele contrato 
ele chamava o método que verificava se existia aditamento para aquele contrato (id)chamava remover filhos de aditamento que por sua vez removia Liberação e retenção , apagava o sequencial de Liberação e o da retenção, ai criava outro e salvava "AditamentoTO salvarAditamentoMenuPendencia" 

Pelo que entendi entendi é rodado uma rotina que verificava se aquele Id para o contrato estava na tabela 
Robledo  pelo que entendi ali na SP ela avalia a 711 e caso o mesmo registro não esteja correspondente na 812 então uma nova compensação é criada ai um INSERT   é executado na 712

Resumir via codigo o Aditamento e regras do Aditamento Renovação semestral codigo :


                             ************EM QUAIS LUGARES ESSE ID QUE FOI APAGADO ERA USADO ? QUAIS OS RELACIONAMENTOS E IMPACTOS ?************
parece que ele passa quais os tipos de transações ?:
ADITAMENTO

Foram todos os Aditamentos ?

-----------------------       Como é feito a execução da funcionalidade atual ---------------


Agora como faz ?
removerFilhosAditamentoLiberacao não existe mais !
LiberacaoContratoBean é onde no sistema faz a gravação das liberações 
no método : public void processarRepasse gravando na 712
duas questões aqui nesse método os dois tem assinatura :
se a operação for igual a 
    ESTORNAR_REPASSE_IES = permite reverter operações já realizadas " tem um calculo "
      

    GRAVAR_REPASSE_IES  = se for ele verifica se já existe liberações se for igual a zero ele então inclui liberações , se já existe ele visita e atualiza 
como funciona o Atualza liberações ?


Analise da solução

Foram criadas uma recomposição de tabela 712 , onde tinha sido apagadas os dados da coluna 
NU_SQNCL_LIBERACAO_CONTRATO e  criando uma nova tabela a 909 , com os dados que foram comparados da 712 e 47 auditoria, para identificar os dados duplicados 
ou seja que foram pagas duas vezes .

oque preciso fazer criar uma procedure para compensar os  dados que já foram pagos , fazendo o estorno dos valores , ou seja vamos dizer que exista 6 parcelas , ou alguma parcela que foi paga para os dados da 909, eu preciso identificar e reter o próximo pagamento , para compensar oque já foi pago duplicado

o primeiro passo seria identificar a parcela e valor que foi pago
depois eu preciso reter ou colocar como cancelado (batch) ou , no processo gravar como storno .

oque acontece quando é feito o estorno ? qual o processo 

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
