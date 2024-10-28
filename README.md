<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>

<pre>

openapi: 3.0.1
info:
  version: 1.0.0
  title: API Aditamento de Dilatação - SIFES
  description: |
    ## *Orientações*
    API utilizada para permitir aos estudantes inscritos no programa de financiamento estudantil FIES realizarem o aditamento de dilatação de seus contratos junto à Caixa.
    
    Para cada um dos paths desta API, além dos escopos (`scopes`) indicados, existem (`permissions`) que deverão ser observadas:
    
    ### `/personal/identifications`
    - permissions:
      - GET: **CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ**
    ### `/personal/qualifications`
      - permissions: **CUSTOMERS_PERSONAL_ADITTIONALINFO_READ**
    ### `/personal/financial-relations`
    
    **Configurações adicionais:**
    - API Segurança Nível III
    - Timeout no API Manager: **3 segundos**
    - Timeout no Middleware: **_____ milissegundos**
    - Timeout no Backend: **865 milissegundos**
    - Equipe de Desenvolvimento Responsável: **CESOB220**
    - Equipe Gestora Negocial (Dono do Produto): **GEFET**
    - Nº do RTC de Validação do Swagger: **99999999**
    
contact:
  name: Equipe de Desenvolvimento (cesob220@caixa.gov.br)
  email: sudeXXX@caixa.gov.br

servers:
  - url: 'https://api.des.caixa:8446/financiamentoestudantil/aditamentodilatacao'
    description: ''

paths:
  /v1/validarCriteriosDilatacaoApp:
    post:
      summary: Buscar dados do estudante para aditamento de dilatação
      description: Esse endpoint busca as informações do estudante para verificar se ele pode solicitar uma dilatação de contrato no semestre corrente.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                cpf:
                  type: string
                  example: "03392645001"
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
                    example: ""
                  codigo:
                    type: integer
                    example: null
                  tipo:
                    type: string
                    example: null
                  qtdDilatacoes:
                    type: integer
                    example: 0
                  possuiDilatacaoAberta:
                    type: boolean
                    example: false
                  justificativa:
                    type: string
                    example: null
                  dilatacoesRealizadas:
                    type: array
                    items:
                      type: object
                      properties:
                        statusDilatacao:
                          type: string
                          example: null
                        uf:
                          type: string
                          example: "MG"
                        duracaoRegularCurso:
                          type: integer
                          example: 12
                        diasAprovacaoIes:
                          type: integer
                          example: null
                        totalSemestreContratado:
                          type: integer
                          example: 6
                        localOferta:
                          type: string
                          example: "Unidade I - Rua Euridamas Avelino de Barros"
                        municipio:
                          type: string
                          example: "PARACATU"
                        nuIes:
                          type: integer
                          example: 2579
                        nomeIes:
                          type: string
                          example: "CENTRO UNIVERSITÁRIO ATENAS"
                        situacao:
                          type: string
                          example: "U"
                        cpf:
                          type: string
                          example: "03392645001"
                        nome:
                          type: string
                          example: "CANDIDATO_20005266"
                        descCurso:
                          type: string
                          example: "MEDICINA"
                        fies:
                          type: integer
                          example: 20005266
                        nuOperacaoSiapi:
                          type: integer
                          example: 187
                        dataLimiteMask:
                          type: string
                          example: "28/10/2024"
              examples:
                OperacaoIndisponivelContrato:
                  summary: Operação não disponível para este contrato.
                  value:
                    mensagem: "Já existe solicitação de dilatação para este semestre."
                    codigo: 2
                    possuiDilatacaoAberta: true
                    uf: "MG"
                CalendarioFechado:
                  summary: Operação não disponível para este contrato. (calendario fechado)
                  value:
                    mensagem: "Operação não disponível para este contrato."
                    codigo: 2
                    possuiDilatacaoAberta: false
                    uf: "MG"
                PeriodoEmUtilizacao:
                  summary: Dilatação não permitida, ainda existe período de utilização
                  value:
                    mensagem: "Operação não disponível para este contrato."
                    codigo: 2
                    possuiDilatacaoAberta: false
                    uf: "MG"
                    
        '401':
          description: |
            Identificação provida pelo token aponta para usuário não autorizado a utilizar a API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 401
                    mensagem: O token fornecido para acesso à API é inválido.
                    tipo: Erro
                    editavel: false
        '404':
          description: Não foi localizado o contrato do estudante solicitado.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: Não foi localizado o contrato do estudante solicitado.
                    tipo: Erro
                    editavel: false
        '412':
          description: Erro estrutural na chamada da API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 412
                    mensagem: Erro na chamada da API.
                    tipo: Erro
                    editavel: false
        '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 500
                    mensagem: Erro na execução da funcionalidade no backend.
                    tipo: Erro
                    editavel: false
security:
  - Internet: []
  - APIKey: []


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
