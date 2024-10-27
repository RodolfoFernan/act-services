<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>

<pre>

 paths:
  /fes-web/emprest/dilatacaoContrato/buscarEstudante:
    post:
      summary: "Solicitar dilatação"
      description: "API para verificar a elegibilidade de dilatação do contrato do estudante."
      responses:
        '200':
          description: "Resposta da solicitação de dilatação"
          content:
            application/json:
              schema:
                type: object
                properties:
                  mensagem:
                    type: string
                    example: "Apto a dilatação" # ou "Já existe solicitação de dilatação para este semestre."
                  codigo:
                    type: integer
                    example: 2
                  tipo:
                    type: string
                    example: null
                  editavel:
                    type: boolean
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
                    type: string
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
                  seqOcorrencia:
                    type: integer
                    example: null
                  qtdSemestresSuspensos:
                    type: integer
                    example: 0
                  iesEncerrada:
                    type: boolean
                    example: false
                  contrato:
                    type: string
                    example: "040647187000001805"
                  situacao:
                    type: string
                    example: "U"
                  descSituacao:
                    type: string
                    example: null
                  cpf:
                    type: string
                    example: "03392645001"
                  nome:
                    type: string
                    example: "CANDIDATO_20005266"
                  nuCurso:
                    type: integer
                    example: 90059
                  descCurso:
                    type: string
                    example: "MEDICINA"
                  fies:
                    type: integer
                    example: 20005266
                  nuParticipacao:
                    type: integer
                    example: 0
                  dataSolicitacao:
                    type: string
                    format: date-time
                    example: null
                  dataLimite:
                    type: integer
                    example: 1730144586227
                  dataInicioVigencia:
                    type: string
                    format: date-time
                    example: null
                  dataFimVigencia:
                    type: string
                    format: date-time
                    example: null
                  dependente:
                    type: string
                    example: null
                  nuOperacaoSiapi:
                    type: integer
                    example: 187
                  diaVencimento:
                    type: integer
                    example: null
                  habilitarEstorno:
                    type: boolean
                    example: null
                  statusDesc:
                    type: string
                    example: null
                  complemento1:
                    type: string
                    example: "NAO INFORMADO"
                  nuStatusContrato:
                    type: integer
                    example: 5
                  nuSituacaoContrato:
                    type: integer
                    example: 1
                  nuCampus:
                    type: integer
                    example: 1038222
                  dtEstorno:
                    type: string
                    format: date-time
                    example: null
                  semestreAno:
                    type: string
                    example: null
                  finalidade:
                    type: string
                    example: null
                  turno:
                    type: string
                    example: "Integral"
                  matricula:
                    type: string
                    example: "3339"
                  semestreReferencia:
                    type: integer
                    example: 1
                  anoReferencia:
                    type: integer
                    example: 2024
                  semestreAnoReferencia:
                    type: string
                    example: "1º/2024"
                  cpfMask:
                    type: string
                    example: "033.926.450-01"
                  dataLimiteMask:
                    type: string
                    example: "28/10/2024"
          examples:
            CaminhoFeliz:
              summary: "Estudante apto a dilatação"
              value:
                mensagem: "Apto a dilatação"
                codigo: null
                possuiDilatacaoAberta: false
                uf: "MG"
                # Continua com os demais dados…
            SolicitacaoExistente:
              summary: "Solicitação de dilatação já existente"
              value:
                mensagem: "Já existe solicitação de dilatação para este semestre."
                codigo: 2
                possuiDilatacaoAberta: true
                uf: "MG"
                # Continua com os demais dados…

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
