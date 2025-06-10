<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Jose Rodolfo Lima da Silva precisamos que você e um analista de negocio, levante esse fluxo Javaweb e os reflexos quando vai para o repasse SP's abaixo e a rotina Java batch FES.REPASSE: 

 Avaliar quais tabelas Impactadas e parâmetros. 
 

FES.FESSPU20_VINCULA_LIBERACAO  
Serviço Técnico Especializado 12476268 
Visão Negocial.:  

(Vincula liberações ao contrato ou aditamento) 
TABELAS ENVOLVIDAS 
FES.FESTB038_ADTMO_CONTRATO 

FES.FESTB712_LIBERACAO_CONTRATO 
FES.FESTB817_RETENCAO_LIBERACAO 

 
 

FES.FESSPZ57_CRISE2019_CORR_VLRS 
Item de Backlog 20972958 
Visão Negocial.: 
As rotinas inerentes aos acertos pertinentes à apuração do repasse de Maio de 2023 foram encaminhadas, e atendidas, para execução por intermédio da solicitação REQ000065985851 na data de 22/05/2023. 

 

Após execução das mesmas e análise aos logs de saída disponibilizados identificamos que a execução da FESSPZ57_CRISE2019_CORR_VLRS apresentou erro, conforme abaixo: 

 

UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET VR_REPASSE = '7163.95', DT_ATUALIZACAO = '22/05/23' WHERE NU_SEQ_CANDIDATO = 20288376 AND AA_REFERENCIA_LIBERACAO = 2022 AND MM_REFERENCIA_LIBERACAO = 1 

*** ERRO VERIFICADO: -1722 - ORA-01722: número inválido 

*** INSTRUCAO : UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET VR_REPASSE = '7163.95', DT_ATUALIZACAO = '22/05/23' WHERE NU_SEQ_CANDIDATO = 20288376 AND AA_REFERENCIA_LIBERACAO = 2022 AND MM_REFERENCIA_LIBERACAO = 1 

 

Haja vista a constatação do erro identificado ser reincidente da não configuração correta do setvar, incorrendo em um formato de data rejeitada pelo comando de Update na rotina, solicitamos que todos os Jobs fossem reexecutados na mesma ordem, observando a correção deste em questão para a contemplação da configuração do setvar esperada. 

 

TABELAS ENVOLVIDAS 
FESTB812_CMPSO_RPSE_INDVO 

FESTB712_LIBERACAO_CONTRATO 

FESTB038_ADTMO_CONTRATO 

FESTB711_RLTRO_CTRTO_ANLTO 

 

 FES.FESSPZ55_CRISE2019_TRATA_SUSP 
Item de Backlog 20808327 
Visão Negocial 
Visando à análise ao requerido, no que concerne aos impactos no repasse provenientes da inclusão e exclusão das Suspensões Tácitas indevidas, seguem algumas observações: 

 

1ª Etapa 

 

- Foram incluídas 78.591 suspensões tácitas do período 01/2022, obedecendo o público pré-selecionado, conforme registrado na tabela FESTB835_MVMTO_TACITO_CONTRATO, objeto dos resultados apresentado do relatório em tela; 

 

- As suspensões foram incluídas sem restrições de critérios, ou seja, sem verificação de pertinência para as mesmas, visando à reprodução do ocorrido quando da execução da funcionalidade; 

 

- Os resultados podem ser observados, conforme arquivo Suspensoes Tacita Incluidas 01_2022, em anexo; 

 

- Foram realizados os procedimentos para suspensão ( S ) dos repasses não realizados ( NR ) e estorno ( NE ) dos repasses realizados ( R ); 

 

- Foram totalizadas 217.542 Liberações concomitantes às Suspensões Tácitas incluídas. Desse total 91 (noventa e uma) já se encontravam suspensas (S) ou (NE), por outros expedientes. 

 

 

2ª Etapa 

 

- Foi realizado procedimento de exclusão de todas as suspensões tácitas incluídas para o período de 01/2022, conforme resultado apresentado acima; 

 

- Foi executado o procedimento FESSPZ55_CRISE2019_TRATA_SUSP, parte inerente aos processos executados mensalmente quando da iminência da apuração do repasse em Produção. 

 

- Foi observado que para as Liberações do período cuja a Suspensão Tácita foi excluída o status das mesmas foi revertido. 

 

- Cabe salientar neste ponto que o procedimento FESSPZ55_CRISE2019_TRATA_SUSP verifica a existência de lançamentos que sustentem, ou fundamentem, o status de Suspensão ( S ) ou Estorno ( NE ) das Liberações do período. Dessa forma, ainda que para a inclusão das Suspensões Tácitas não foram observados critérios de pertinência, e consequentemente reflexo na alteração dos status da Liberações correlatas, para a adequação, conforme esse procedimento, são considerados critérios de validação: inexistência de ocorrências de suspensão ou encerramento (status 11 ou 18); Aditamento de Renovação contratado (status 4 ou 5); Contrato Finalizado (status 4 ou 5). 

 

- O ressaltado acima pode ser observado no arquivo Liberações status S ou NE permanentes, em anexo. 
TABELAS ENVOLVIDAS 
FESTB812_CMPSO_RPSE_INDVO 

FESTB712_LIBERACAO_CONTRATO 

FESTB010_CANDIDATO 

FESTB711_RLTRO_CTRTO_ANLTO 

FESTB817_RETENCAO_LIBERACAO 

FESTB057_OCRRA_CONTRATO 

 
FES.FESSPZA6_COMPENSACAO_REPASSE 
Serviço Técnico Especializado 18389570 
Visão Negocial.: 
Conforme evidências apresentadas, informamos que as 1145 compensações previstas na extração das tabelas de liberação do ambiente de produção foram identificadas também em extrações do ambiente TGE/EXADATA, marcadas com o status IC_COMPENSACAO "N". 

Quanto às retenções previstas em Produção, identificamos que das 130 liberações, 111 receberam indicativo de retenção após a execução da FESSPZA6_COMPENSACAO_REPASSE. As demais retenções não foram encontradas com a situação a repassar quando da avaliação do extrato analítico das parcelas aptas a repassar, o que pode indicar que as liberações ausentes tenham sido geradas após a data de atualização da base. 

Diante disso, homologamos a presente solução sob as perspectivas avaliadas, supracitadas 

TABELAS ENVOLVIDAS 
FESTB812_CMPSO_RPSE_INDVO 

FESTB712_LIBERACAO_CONTRATO 
FESTB818_MOTIVO_RETENCAO_LBRCO 

FESTB711_RLTRO_CTRTO_ANLTO 

FESTB817_RETENCAO_LIBERACAO 

 

Serviço Técnico Especializado 12476268 

Visão Negocial.: 
(atualiza para não apto nas situações: troca mantença, suspensão, estornado e transferência) 

 
FES.FESSPZ45_CRISE2019_ALTER_LIB_2 
Serviço Técnico Especializado 14707163 
Visão Negocial.: 
À 
CEDESBR251 
 
Segue proposta para regularização de situações do repasse a ser realizada em junho: 
 
1.   Ajuste nas liberações a repassar e repassadas – inconsistência no percentual de financiamento 
 
Deverá ser realizado análise de todas as liberações geradas, repassadas ou não repassadas, verificando se o valor das liberações corresponde ao valor contratado no aditamento renovação. 
Se o valor das liberações não corresponder ao valor do aditamento renovação, então deverá ser realizado o ajuste no valor. 
 
Para os casos onde as liberações estejam repassadas, o valor repassado deverá ser incluído na compensação e o valor da liberação corrigida e alterada para situação “a repassar”, para que seja repassado no valor correto. 
 
Para os casos onde as liberações não foram repassadas, seu valor deverá ser ajustado para que corresponda ao valor do aditamento renovação. As liberações enquadradas nesse caso, que tiverem retenções por liberações a estornar, deverão ter a retenção finalizada após o ajuste no valor. 
 
 
2        Ajuste na IES da liberação para estudantes com transferência 
 
Para contratos com transferência realizada (status 5) deverá ser verificado se a IES da liberação dos semestres anteriores a transferência é a IES de origem e se as liberações referentes ao mesmo semestre da transferência e posteriores são da IES de destino. 
 
Caso a IES da liberação não esteja obedecendo a regra acima, a liberação deverá ter a IES ajustada para a IES correta. No caso das liberações já repassadas, o valor deverá ser incluído na compensação e as liberações correspondentes deverão ter a IES ajustada a situação alterada para “não repassada”. 
 
Para os casos onde houver mais de uma transferência realizada para o estudante, a liberação estará gravada na mantenedora destino até o semestre imediatamente anterior à segunda transferência, devendo as demais estarem geradas para a nova mantenedora destino e assim por diante. 

TABELAS ENVOLVIDAS 
FESTB812_CMPSO_RPSE_INDVO 

FESTB712_LIBERACAO_CONTRATO 
FESTB818_MOTIVO_RETENCAO_LBRCO 

FESTB711_RLTRO_CTRTO_ANLTO 

FESTB817_RETENCAO_LIBERACAO 

FESTB049_TRANSFERENCIA 

FESTB154_CAMPUS_INEP 

FESTB155_IES_INEP 

FESTB038_ADTMO_CONTRATO 

FESTB010_CANDIDATO 

 
FES.FESSPZ37_CRISE19_FIM_RETENCAO 
Serviço Técnico Especializado 13854457 
 

Registramos aqui o andamento das análises e definição de regras construídas em conjunto pela GEFET e CEDES para finalização de retenções de liberações retidas por transferência ou suspensão. 
2. As regras deverão ser aplicadas para possibilitar o repasse das liberações que estiverem consistentes, conforme definições e regras registradas em anexo. 
2.1 Ressaltamos que as definições aqui registradas poderão sofrer alteração em razão da análise dos resultados em massa de testes. 
3. Agradecemos a colaboração e ficamos à disposição. 

 

TABELAS ENVOLVIDAS 
 

FESTB049_TRANSFERENCIA 

FESTB038_ADTMO_CONTRATO 

FESTB010_CANDIDATO 

 

FES.FESSPZ41_CRISE19_FIM_RETENC_2 
Serviço Técnico Especializado 14499423 
Visão Negocial 

1.       Conforme acordado, segue abaixo uma prévia da proposta de trabalho para a apuração de maio/2020, contendo as seguintes regularizações: 
 
• Inclusão de regra para não retenção de contratos com transferência realizada, cujas liberações estejam geradas para a mantendora correta. 
• Ajuste na rotina de retenção para que não reter os contratos que estejam enquadrados nas exceções para retenção de suspensão e transferência. 
• Ajuste na rotina eventual que gera liberações para contemplar as alterações nos processos de retenção de suspensão e transferência. 
 
2.       Para que possamos atingir o objetivo deste repasse, propomos extender o prazo da apuração de 20/05 para 31/05/2020, de maneira que haja tempo hábil para os ajustes. 
 
3.       Assim, seguem o esboço das regras a serem utilizadas nessa próxima etapa, para avaliação e complementação. 
 
3.1.   Contratos com transferência realizada 
3.1.1.Para contratos com transferência realizada (status 5) deverá ser finalizada a retenção ou não gerada a retenção nos casos em que as liberações referentes a semestres anteriores à transferência estejam geradas para a mantenedora de origem e as liberações geradas para o mesmo semestre da transferêcia e posteriores estejam geradas para a mantenedora destino. 
3.1.2.Para os casos onde houver mais de uma transferência realizada para o estudante, a liberação estará gravada na mantenedora destino até o semestre imediatamente anterior à segunda transferência, devendo as demais estarem geradas para a nova mantenedora destino e assim por diante. 
 
3.2.   Ajuste na rotina de retenção 
3.2.1.A rotina de retenção deverá ser ajustada para incluir as regras estabelecidas para suspensão e transferência, de modo que não gerem retenções para os casos onde a retenção não é devida, evitando, assim, a necessidade de se finalizar a retenção posteriormente. 
3.2.2.Dessa forma, deverá estar prevista nessa rotina de renteção as regras para tratamento de suspensão e transferência. 
 
3.3.   Ajuste na rotina eventual de liberação 
3.3.1.A rotina eventual que gera liberações deverá ser ajustada para contemplar as regras de tratamento de suspensão e transferência. 
3.3.2.A geração de liberações para inscrições com transferência realizada deverá obecer a regra descrita no item 3.2, ou seja, as liberações referentes a semestres anteriores à transferência devem ser geradas para a mantenedora de origem, e as liberações referentes ao semestre da transferência e posteriores deverão ser geradas para a mantenedora destino. 
3.3.3.Para os casos onde houver mais de uma transferência realizada para o estudante, a liberação deverá ser gerada para mantenedora destino até o semestre imediatamente anterior à a segunda transferência, devendo as demais estarem geradas para a nova mantenedora destino e assim por diante. 










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
