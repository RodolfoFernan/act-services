<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
openapi: 3.0.1
info:
  version: 1.0.0
  title: API Aditamento de Transferência - SIFES
  description: |
    ## *Orientações*
    
    API utilizada para permitir aos estudantes inscritos no programa de financiamento estudantil FIES realizarem a Transferência de IES e Curso junto à Caixa.
    
    Para cada um dos paths desta API, além dos escopos (`scopes`) indicados, existem (`permissions`) que deverão ser observadas:
    
    ### Permissões
    
    **`/personal/identifications`**
    - GET: **CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ**
    
    **`/personal/qualifications`**
    - GET: **CUSTOMERS_PERSONAL_ADDITIONALINFO_READ**
    
    **`/personal/financial-relations`**
    
    ### Segurança
    - API Segurança Nível III
    - Timeout no API Manager: **3 segundos**
    - Timeout no Middleware: **_____ milissegundos**
    - Timeout no Backend: **865 milissegundos**
    
    ### Equipes Responsáveis
    - Equipe de Desenvolvimento: **CESOB220**
    - Equipe Gestora Negocial (Dono do Produto): **GEFET**
    - Nº do RTC de Validação do Swagger: **20961817**

contact:
  name: Equipe de Desenvolvimento
  email: cesob220@caixa.gov.br

servers:
  - url: https://api.des.caixa:8446/financiamentoestudantil/transferenciaContrato

paths:
  /v1/validar-criterios-transfer/{cpf}:
    get:
      summary: Buscar dados do estudante para aditamento de transferência
      description: |
        Esse endpoint busca as informações do estudante para verificar se ele pode solicitar uma Transferência.
      parameters:
        - name: cpf
          in: path
          required: true
          schema:
            type: string
          example: '06741761209'
          description: CPF do estudante
      responses:
        '200':
          description: Informações do estudante retornadas com sucesso.
          content:
            application/json:
              schema:
                type: object
                properties:
                  mensagem:
                    type: string
                    example: "Operação não disponível para este contrato."
                  codigo:
                    type: integer
                    example: 2
                  tipo:
                    type: string
                    example: null
                  habilitarSolicitacao:
                    type: boolean
                    example: true
        '401':
          description: Usuário não autorizado a utilizar a API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
        '404':
          description: Não foi localizado um contrato para o código Fies fornecido.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
        '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'

  /v1/confirmar-solicitacao-app:
    post:
      summary: Confirmar solicitação de transferência de contrato
      description: |
        Esse endpoint confirma a solicitação de Transferência.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                numeroSemestresCursar:
                  type: integer
                  description: Número de semestres a cursar.
                  example: 3
                dtDesligamento:
                  type: string
                  format: date
                  description: Data de desligamento.
                  example: "12/01/2024"
                codFies:
                  type: integer
                  description: Código FIES do estudante.
                  example: 20360669
      responses:
        '200':
          description: Solicitação de Transferência confirmada com sucesso.
          content:
            application/json:
              schema:
                type: object
                properties:
                  mensagem:
                    type: string
                    example: "Operação realizada com sucesso."
                  codigo:
                    type: integer
                    example: 200
        '401':
          description: Usuário não autorizado a utilizar a API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
        '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'

components:
  schemas:
    RetornoErro:
      type: object
      properties:
        codigo:
          type: integer
        mensagem:
          type: string
        tipo:
          type: string
        editavel:
          type: boolean

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
