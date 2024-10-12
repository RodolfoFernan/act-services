# Controle de Lançamentos e Consolidação Diária - Microsserviços

Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com Spring Boot, RabbitMQ, Redis, PostgreSQL, Docker, Kubernetes e diversas outras tecnologias.

## 1. Etapas do Desenvolvimento

### 1.1 Planejamento e Desenho da Solução
<ul>
 <li><b>Funcionalidades:</b> Desenvolver dois serviços principais: Controle de Lançamentos e Consolidação Diária.</li>
 <li><b>Arquitetura:</b> Arquitetura baseada em microsserviços.</li>
 <li><b>Tecnologias:</b> Escolha de frameworks como Spring Boot para o backend e PostgreSQL para banco de dados.</li>
</ul>

### 1.2 Configuração do Ambiente de Desenvolvimento
<ul>
 <li><b>Instalar:</b> Java 8+, Spring Boot, Docker, Kubernetes.</li>
 <li><b>Configurar banco de dados:</b> PostgreSQL/MySQL.</li>
 <li><b>Ferramentas adicionais:</b>
 <ul>
 <li>Git para controle de versionamento.</li>
 <li>Maven ou Gradle para controle de dependências.</li>
 <li>Jenkins para automação de CI/CD.</li>
 </ul>
 </li>
</ul>

### 1.3 Desenvolvimento do Serviço de Controle de Lançamentos
<ul>
 <li><b>API REST:</b> Gerenciar débitos e créditos.</li>
 <li><b>Banco de Dados:</b> Integração com PostgreSQL para persistência de lançamentos.</li>
 <li><b>Cache:</b> Uso de Redis/Memcached para otimização de consultas.</li>
</ul>

### 1.4 Desenvolvimento do Serviço de Consolidação Diária
<ul>
 <li><b>Serviço:</b> Consolidação diária dos saldos.</li>
 <li><b>Integração:</b> RabbitMQ/Kafka para comunicação entre microsserviços.</li>
 <li><b>Cache:</b> Uso de Redis para otimização de consultas.</li>
</ul>

### 1.5 Segurança
<ul>
 <li><b>Autenticação e autorização:</b> Configurar OAuth 2.0.</li>
 <li><b>Segurança:</b> Implementação de TLS/SSL para comunicação segura.</li>
 <li><b>Proteção:</b> Rate limiting, firewalls, WAFs.</li>
</ul>

### 1.6 Observabilidade e Monitoramento
<ul>
 <li><b>Monitoramento:</b> Prometheus e Grafana.</li>
 <li><b>Logs centralizados:</b> ELK Stack (Elasticsearch, Logstash, Kibana).</li>
</ul>

---

## 2. Tecnologias Utilizadas

### Backend:
<ul>
 <li><b>Java (Spring Boot):</b> Para o desenvolvimento do backend.</li>
 <li><b>RabbitMQ/Kafka:</b> Comunicação entre microsserviços.</li>
 <li><b>OAuth 2.0:</b> Autenticação/autorização.</li>
 <li><b>Swagger:</b> Documentação de APIs.</li>
</ul>

### Banco de Dados:
<ul>
 <li><b>PostgreSQL/MySQL:</b> Para persistência de dados.</li>
 <li><b>Redis/Memcached:</b> Para otimização de cache.</li>
</ul>

### Infraestrutura:
<ul>
 <li><b>Docker:</b> Para contêinerização.</li>
 <li><b>Kubernetes:</b> Orquestração dos microsserviços.</li>
 <li><b>Jenkins:</b> Para CI/CD.</li>
</ul>

---

## 4. Passos para Execução

### Pré-requisitos:
<ul>
 <li>Java 8+</li>
 <li>Maven</li>
 <li>Docker e Kubernetes</li>
 <li>PostgreSQL/MySQL</li>
 <li>Redis</li>
 <li>RabbitMQ ou Kafka</li>
</ul>

### Passos para Execução:
1. Clone o repositório:
 ```bash
 git clone https://github.com/seu_usuario/repositorio.git
Ver perfil de José Rodolfo José Rodolfo Lima
José Rodolfo Lima 21:02

<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com Spring Boot, RabbitMQ, Redis, PostgreSQL, Docker, Kubernetes e diversas outras tecnologias.</p>

<h2>1. Etapas do Desenvolvimento</h2>

<h3>1.1 Planejamento e Desenho da Solução</h3>
<ul>
 <li><b>Funcionalidades:</b> Desenvolver dois serviços principais: Controle de Lançamentos e Consolidação Diária.</li>
 <li><b>Arquitetura:</b> Arquitetura baseada em microsserviços.</li>
 <li><b>Tecnologias:</b> Escolha de frameworks como Spring Boot para o backend e PostgreSQL para banco de dados.</li>
</ul>

<h3>1.2 Configuração do Ambiente de Desenvolvimento</h3>
<ul>
 <li><b>Instalar:</b> Java 8+, Spring Boot, Docker, Kubernetes.</li>
 <li><b>Configurar banco de dados:</b> PostgreSQL/MySQL.</li>
 <li><b>Ferramentas adicionais:</b>
 <ul>
 <li>Git para controle de versionamento.</li>
 <li>Maven ou Gradle para controle de dependências.</li>
 <li>Jenkins para automação de CI/CD.</li>
 </ul>
 </li>
</ul>

<h3>1.3 Desenvolvimento do Serviço de Controle de Lançamentos</h3>
<ul>
 <li><b>API REST:</b> Gerenciar débitos e créditos.</li>
 <li><b>Banco de Dados:</b> Integração com PostgreSQL para persistência de lançamentos.</li>
 <li><b>Cache:</b> Uso de Redis/Memcached para otimização de consultas.</li>
</ul>

<h3>1.4 Desenvolvimento do Serviço de Consolidação Diária</h3>
<ul>
 <li><b>Serviço:</b> Consolidação diária dos saldos.</li>
 <li><b>Integração:</b> RabbitMQ/Kafka para comunicação entre microsserviços.</li>
 <li><b>Cache:</b> Uso de Redis para otimização de consultas.</li>
</ul>

<h3>1.5 Segurança</h3>
<ul>
 <li><b>Autenticação e autorização:</b> Configurar OAuth 2.0.</li>
 <li><b>Segurança:</b> Implementação de TLS/SSL para comunicação segura.</li>
 <li><b>Proteção:</b> Rate limiting, firewalls, WAFs.</li>
</ul>

<h3>1.6 Observabilidade e Monitoramento</h3>
<ul>
 <li><b>Monitoramento:</b> Prometheus e Grafana.</li>
 <li><b>Logs centralizados:</b> ELK Stack (Elasticsearch, Logstash, Kibana).</li>
</ul>

<hr>

<h2>2. Tecnologias Utilizadas</h2>

<h3>Backend:</h3>
<ul>
 <li><b>Java (Spring Boot):</b> Para o desenvolvimento do backend.</li>
 <li><b>RabbitMQ/Kafka:</b> Comunicação entre microsserviços.</li>
 <li><b>OAuth 2.0:</b> Autenticação/autorização.</li>
 <li><b>Swagger:</b> Documentação de APIs.</li>
</ul>

<h3>Banco de Dados:</h3>
<ul>
 <li><b>PostgreSQL/MySQL:</b> Para persistência de dados.</li>
 <li><b>Redis/Memcached:</b> Para otimização de cache.</li>
</ul>

<h3>Infraestrutura:</h3>
<ul>
 <li><b>Docker:</b> Para contêinerização.</li>
 <li><b>Kubernetes:</b> Orquestração dos microsserviços.</li>
 <li><b>Jenkins:</b> Para CI/CD.</li>
</ul>

<hr>

<h2>4. Passos para Execução</h2>

<h3>Pré-requisitos:</h3>
<ul>
 <li>Java 8+</li>
 <li>Maven</li>
 <li>Docker e Kubernetes</li>
 <li>PostgreSQL/MySQL</li>
 <li>Redis</li>
 <li>RabbitMQ ou Kafka</li>
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

<h3>Acessar a Documentação da API:</h3>
<p>Após iniciar os serviços, você pode acessar a documentação da API no Swagger:</p>
<ul>
 <li><b>Controle de Lançamentos:</b> <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a></li>
 <li><b>Consolidação Diária:</b> <a href="http://localhost:8081/swagger-ui.html">http://localhost:8081/swagger-ui.html</a></li>
</ul>

<hr>

<h2>5. Monitoramento e Observabilidade</h2>
<ul>
 <li><b>Prometheus e Grafana:</b> Configurar dashboards para monitorar métricas de sistema.</li>
 <li><b>Logs:</b> Centralizados com a ELK Stack.</li>
</ul>

<hr>

<h2>6. Contribuições</h2>
<p>Sinta-se à vontade para enviar Pull Requests e contribuir com melhorias para o projeto!</p>

<hr>

<h2>7. Licença</h2>
<p>Este projeto está sob a licença MIT.</p>
