<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise Detalhada da Procedure FES.FESSPZ57_CRISE2019_CORR_VLRS por Etapas (Continuação)

Etapa 1:Identificar os registros de FESTB712_LIBERACAO_CONTRATO ser avaliados para atualização.

 Tabelas Consultadas (com seus aliases no SELECT):
 FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
 FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
 FES.FESTB049_TRANSFERENCIA (aliás T)
 FES.FESTB154_CAMPUS_INEP (aliás C e I)
 FES.FESTB155_IES_INEP (aliás E e N)
 FES.FESTB038_ADTMO_CONTRATO (aliás A)
 FES.FESTB010_CANDIDATO (aliás CA)
 Campos Consultados:
 De FES.FESTB712_LIBERACAO_CONTRATO (L):
 NU_SQNCL_LIBERACAO_CONTRATO
 NU_SEQ_CANDIDATO
 AA_REFERENCIA_LIBERACAO
 MM_REFERENCIA_LIBERACAO
 NU_IES
 IC_SITUACAO_LIBERACAO
 De FES.FESTB817_RETENCAO_LIBERACAO (R):
 NU_SQNCL_LIBERACAO_CONTRATO
 NU_MOTIVO_RETENCAO_LIBERACAO
 DT_FIM_RETENCAO
 De FES.FESTB049_TRANSFERENCIA (T):
 NU_CANDIDATO_FK10
 NU_SEQ_TRANSFERENCIA
 AA_REFERENCIA
 NU_SEM_REFERENCIA
 NU_CAMPUS_ORIGEM_FK161
 NU_CAMPUS_DESTINO_FK161
 DT_INCLUSAO
 NU_STATUS_TRANSFERENCIA
 De FES.FESTB154_CAMPUS_INEP (C e I):
 NU_CAMPUS
 NU_IES_FK155
 De FES.FESTB155_IES_INEP (E e N):
 NU_IES
 NU_MANTENEDORA_FK156
 De FES.FESTB038_ADTMO_CONTRATO (A):
 NU_CANDIDATO_FK36
 AA_ADITAMENTO
 NU_SEM_ADITAMENTO
 DT_ADITAMENTO
 NU_STATUS_ADITAMENTO (implicitamente usado na condição A.DT_ADITAMENTO IS NOT NULL, embora não projetado)
 De FES.FESTB010_CANDIDATO (CA):
 NU_SEQ_CANDIDATO
 DT_ADMISSAO_CANDIDATO











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
