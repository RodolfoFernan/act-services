<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
post:
  summary: Confirmar solicitação de transferência de contrato
  description: >
    Esse endpoint confirma a solicitação de Transferência.
  requestBody:
    description: Dados da solicitação de Transferência.
    required: true
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
      editavel:
        type: string
        example: null
      idTransferencia:
        type: integer
        example: null
      codFies:
        type: integer
        example: 20360669
      cpfCandidato:
        type: string
        example: "06741761209"
      nomeCandidato:
        type: string
        example: "Candidato_20360669"
      tipoTransferencia:
        type: string
        example: null
      idIes:
        type: integer
        example: 14901
      nuMantenedora:
        type: integer
        example: 14228
      nuCampus:
        type: integer
        example: 1080012
      nuCurso:
        type: integer
        example: 1109229
      nuTurno:
        type: integer
        example: 3
      nomeIes:
        type: string
        example: "FACULDADE INTEGRADA CARAJÁS"
      nomeMantenedora:
        type: string
        example: "Faculdades Integradas Carajas S/C Ltda - Epp"
      turnoDescDestino:
        type: string
        example: "Noturno"
      uf:
        type: string
        example: "PA"
      municipio:
        type: string
        example: "REDENCAO"
      endereco:
        type: string
        example: "BR 155, km 03"
      nomeCampus:
        type: string
        example: "Faculdade Integrada Carajás"
      nomeCurso:
        type: string
        example: "ENFERMAGEM"
      duracaoRegularCurso:
        type: integer
        example: 10
      nuSemestresCursados:
        type: integer
        example: 8
      qtSemestresDilatado:
        type: integer
        example: 0
      qtSemestresSuspenso:
        type: integer
        example: 0
      iesDestino:
        type: string
        example: null
      nuMantenedoraDestino:
        type: integer
        example: null
      campusDestino:
        type: string
        example: null
      cursoDestino:
        type: string
        example: null
      turnoDestino:
        type: string
        example: null
      nomeIesDestino:
        type: string
        example: null
      nomeMantenedoraDestino:
        type: string
        example: null
      ufDestino:
        type: string
        example: null
      municipioDestino:
        type: string
        example: null
      enderecoDestino:
        type: string
        example: null
      nomeCampusDestino:
        type: string
        example: null
      nomeCursoDestino:
        type: string
        example: null
      transferenciasRealizadas:
        type: string
        example: null
      icCondicaoFuncionamento:
        type: string
        example: "N"
      icSituacaoContrato:
        type: string
        example: "U"
      icSituacaoIES:
        type: string
        example: null
      nuOperacaoSiapi:
        type: integer
        example: 187
      totalSemestresContratados:
        type: integer
        example: 6
      totalSemestresUtilizados:
        type: integer
        example: 1
      totalSemestresDestino:
        type: integer
        example: null
      habilitarSolicitacao:
        type: boolean
        example: true
      numeroSemestresCursar:
        type: integer
        example: 10
      descTunoOrigem:
        type: string
        example: "Noturno"
      semestreReferencia:
        type: integer
        example: 1
      anoReferencia:
        type: integer
        example: 2025
      notaEnemCandidato:
        type: string
        example: null
      anoReferenciaNotaEnem:
        type: string
        example: null
      jsonRetornoConsultaEnem:
        type: string
        example: null
      estudantePodeTransfCurso:
        type: string
        example: "S"
      totalSemestresDisponiveis:
        type: integer
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
