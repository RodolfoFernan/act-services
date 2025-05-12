<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>

    
/transferenciaContrato/buscarEstudante:
    get:
      summary: Busca informações do estudante para transferência de contrato (via corpo da requisição)
      description: |
        Este endpoint permite buscar os detalhes de um estudante específico para iniciar o processo
        de transferência do contrato do Fundo de Financiamento Estudantil (FIES).
        Os parâmetros (codFies, cpf) são enviados no corpo da requisição.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                codFies:
                  type: integer
                  description: Código FIES do estudante.
                  example: 20242515
                cpf:
                  type: string
                  pattern: '^[0-9]{11}$'
                  description: CPF do estudante a ser consultado (apenas números).
                  example: "70966798120"
                _:
                  type: integer
                  required: false
                  description: Timestamp para evitar cache (gerado dinamicamente).
                  example: 1747058495355
      responses:
        '200':
          description: Resposta bem-sucedida com os detalhes do estudante para transferência.
          content:
            application/json:
              schema:
                type: object
                properties:
                  mensagem:
                    type: string
                    description: Mensagem informativa (geralmente vazia em caso de sucesso).
                    nullable: true
                    example: ""
                  codigo:
                    type: integer
                    description: Código de retorno (0 em caso de sucesso).
                    nullable: true
                    example: 0
                  tipo:
                    type: string
                    description: Tipo da mensagem (geralmente nulo em caso de sucesso).
                    nullable: true
                    example: null
                  editavel:
                    type: boolean
                    description: Indica se os dados são editáveis.
                    nullable: true
                    example: null
                  idTransferencia:
                    type: integer
                    description: Identificador da transferência (se aplicável).
                    nullable: true
                    example: null
                  codFies:
                    type: integer
                    description: Código FIES do estudante.
                    example: 20242515
                  cpfCandidato:
                    type: string
                    description: CPF do candidato.
                    example: "70966798120"
                  nomeCandidato:
                    type: string
                    description: Nome completo do candidato.
                    example: "LUANA GARCIA FERREIRA"
                  tipoTransferencia:
                    type: string
                    nullable: true
                    description: Tipo da transferência (se aplicável).
                    example: null
                  idIes:
                    type: integer
                    description: ID da Instituição de Ensino Superior (IES) de origem.
                    example: 1113
                  nuMantenedora:
                    type: integer
                    description: Número da mantenedora da IES de origem.
                    example: 770
                  nuCampus:
                    type: integer
                    description: Número do campus da IES de origem.
                    example: 27693
                  nuCurso:
                    type: integer
                    description: Número do curso na IES de origem.
                    example: 73537
                  nuTurno:
                    type: integer
                    description: Número do turno do curso na IES de origem.
                    example: 1
                  nomeIes:
                    type: string
                    description: Nome da IES de origem.
                    example: "CENTRO UNIVERSITÁRIO EURO-AMERICANO"
                  nomeMantenedora:
                    type: string
                    description: Nome da mantenedora da IES de origem.
                    example: "Instituto Euro Americano De Educacao Ciencia Tecnologia"
                  turnoDescDestino:
                    type: string
                    description: Descrição do turno de destino (se aplicável).
                    example: "Matutino"
                  uf:
                    type: string
                    description: Unidade Federativa da IES de origem.
                    example: "DF"
                  municipio:
                    type: string
                    description: Município da IES de origem.
                    example: "BRASILIA"
                  endereco:
                    type: string
                    description: Endereço da IES de origem.
                    example: "SCES Trecho 0 - Conjunto 5"
                  nomeCampus:
                    type: string
                    description: Nome do campus da IES de origem.
                    example: "Centro Universitário Euro-Americano - Unidade Asa Sul"
                  nomeCurso:
                    type: string
                    description: Nome do curso de origem.
                    example: "ENFERMAGEM"
                  duracaoRegularCurso:
                    type: integer
                    description: Duração regular do curso (em semestres).
                    example: 10
                  nuSemestresCursados:
                    type: integer
                    description: Número de semestres já cursados.
                    example: 1
                  qtSemestresDilatado:
                    type: integer
                    description: Quantidade de semestres dilatados.
                    example: 0
                  qtSemestresSuspenso:
                    type: integer
                    description: Quantidade de semestres suspensos.
                    example: 0
                  iesDestino:
                    type: integer
                    nullable: true
                    description: ID da IES de destino (se aplicável).
                    example: null
                  nuMantenedoraDestino:
                    type: integer
                    nullable: true
                    description: Número da mantenedora da IES de destino (se aplicável).
                    example: null
                  campusDestino:
                    type: integer
                    nullable: true
                    description: Número do campus da IES de destino (se aplicável).
                    example: null
                  cursoDestino:
                    type: integer
                    nullable: true
                    description: Número do curso na IES de destino (se aplicável).
                    example: null
                  turnoDestino:
                    type: integer
                    nullable: true
                    description: Número do turno do curso na IES de destino (se aplicável).
                    example: null
                  nomeIesDestino:
                    type: string
                    nullable: true
                    description: Nome da IES de destino (se aplicável).
                    example: null
                  nomeMantenedoraDestino:
                    type: string
                    nullable: true
                    description: Nome da mantenedora da IES de destino (se aplicável).
                    example: null
                  ufDestino:
                    type: string
                    nullable: true
                    description: Unidade Federativa da IES de destino (se aplicável).
                    example: null
                  municipioDestino:
                    type: string
                    nullable: true
                    description: Município da IES de destino (se aplicável).
                    example: null
                  enderecoDestino:
                    type: string
                    nullable: true
                    description: Endereço da IES de destino (se aplicável).
                    example: null
                  nomeCampusDestino:
                    type: string
                    nullable: true
                    description: Nome do campus da IES de destino (se aplicável).
                    example: null
                  nomeCursoDestino:
                    type: string
                    nullable: true
                    description: Nome do curso de destino (se aplicável).
                    example: null
                  transferenciasRealizadas:
                    type: array
                    items:
                      type: string
                    description: Lista de transferências realizadas (se houver).
                    example: []
                  icCondicaoFuncionamento:
                    type: string
                    description: Condição de funcionamento da IES.
                    example: "N"
                  icSituacaoContrato:
                    type: string
                    description: Situação do contrato.
                    example: "U"
                  icSituacaoIES:
                    type: string
                    description: Situação da IES.
                    example: "L"
                  nuOperacaoSiapi:
                    type: integer
                    description: Número da operação no SIAPI.
                    example: 187
                  totalSemestresContratados:
                    type: integer
                    description: Total de semestres contratados.
                    example: 7
                  totalSemestresUtilizados:
                    type: integer
                    description: Total de semestres utilizados.
                    example: 2
                  totalSemestresDestino:
                    type: integer
                    nullable: true
                    description: Total de semestres no curso de destino (se aplicável).
                    example: null
                  habilitarSolicitacao:
                    type: boolean
                    description: Indica se a solicitação de transferência está habilitada.
                    example: true
                  numeroSemestresCursar:
                    type: integer
                    description: Número de semestres a cursar no destino (se aplicável).
                    example: 7
                  descTunoOrigem:
                    type: string
                    description: Descrição do turno de origem.
                    example: "Matutino"
                  semestreReferencia:
                    type: integer
                    description: Semestre de referência.
                    example: 1
                  anoReferencia:
                    type: integer
                    description: Ano de referência.
                    example: 2025
                  notaEnemCandidato:
                    type: number
                    format: float
                    description: Nota do ENEM do candidato.
                    example: 495.34
                  anoReferenciaNotaEnem:
                    type: integer
                    description: Ano de referência da nota do ENEM.
                    example: 2020
                  jsonRetornoConsultaEnem:
                    type: string
                    description: JSON de retorno da consulta ENEM.
                    example: "{\"nuCpf\":\"70966798120\",\"vlNotaEnemConsiderada\":\"495.34\",\"nuSemestreReferencia\":\"22020\",\"coInscricao\":6614627,\"nuAnoEnem\":\"2019\"}"
                  estudantePodeTransfCurso:
                    type: string
                    description: Indica se o estudante pode transferir de curso.
                    example: "S"
                  totalSemestresDisponiveis:
                    type: integer
                    description: Total de semestres disponíveis para financiamento.
                    example: 5







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
