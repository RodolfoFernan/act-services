
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
