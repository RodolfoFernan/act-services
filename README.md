<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>

    

responses:
      '200':
        description: Sucesso - Retorna os detalhes do estudante.
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
                  example: 0
                tipo:
                  type: string
                  nullable: true
                  example: null
                editavel:
                  nullable: true
                  example: null
                idTransferencia:
                  type: string
                  nullable: true
                  example: null
                codFies:
                  type: integer
                  example: 20242515
                cpfCandidato:
                  type: string
                  example: "70966798120"
                nomeCandidato:
                  type: string
                  example: "LUANA GARCIA FERREIRA"
                tipoTransferencia:
                  type: string
                  nullable: true
                  example: null
                idIes:
                  type: integer
                  example: 1113
                nuMantenedora:
                  type: integer
                  example: 770
                nuCampus:
                  type: integer
                  example: 27693
                nuCurso:
                  type: integer
                  example: 73537
                nuTurno:
                  type: integer
                  example: 1
                nomeIes:
                  type: string
                  example: "CENTRO UNIVERSITÁRIO EURO-AMERICANO"
                nomeMantenedora:
                  type: string
                  example: "Instituto Euro Americano De Educacao Ciencia Tecnologia"
                turnoDescDestino:
                  type: string
                  nullable: true
                  example: "Matutino"
                uf:
                  type: string
                  example: "DF"
                municipio:
                  type: string
                  example: "BRASILIA"
                endereco:
                  type: string
                  example: "SCES Trecho 0 - Conjunto 5"
                nomeCampus:
                  type: string
                  example: "Centro Universitário Euro-Americano - Unidade Asa Sul"
                nomeCurso:
                  type: string
                  example: "ENFERMAGEM"
                duracaoRegularCurso:
                  type: integer
                  example: 10
                nuSemestresCursados:
                  type: integer
                  example: 1
                qtSemestresDilatado:
                  type: integer
                  example: 0
                qtSemestresSuspenso:
                  type: integer
                  example: 0
                iesDestino:
                  type: string
                  nullable: true
                  example: null
                nuMantenedoraDestino:
                  type: integer
                  nullable: true
                  example: null
                campusDestino:
                  type: integer
                  nullable: true
                  example: null
                cursoDestino:
                  type: integer
                  nullable: true
                  example: null
                turnoDestino:
                  type: integer
                  nullable: true
                  example: null
                nomeIesDestino:
                  type: string
                  nullable: true
                  example: null
                nomeMantenedoraDestino:
                  type: string
                  nullable: true
                  example: null
                ufDestino:
                  type: string
                  nullable: true
                  example: null
                municipioDestino:
                  type: string
                  nullable: true
                  example: null
                enderecoDestino:
                  type: string
                  nullable: true
                  example: null
                nomeCampusDestino:
                  type: string
                  nullable: true
                  example: null
                nomeCursoDestino:
                  type: string
                  nullable: true
                  example: null
                transferenciasRealizadas:
                  type: array
                  items:
                    type: string # Ajuste o tipo se souber a estrutura dos itens
                  example: []
                icCondicaoFuncionamento:
                  type: string
                  example: "N"
                icSituacaoContrato:
                  type: string
                  example: "U"
                icSituacaoIES:
                  type: string
                  example: "L"
                nuOperacaoSiapi:
                  type: integer
                  example: 187
                totalSemestresContratados:
                  type: integer
                  example: 7
                totalSemestresUtilizados:
                  type: integer
                  example: 2
                totalSemestresDestino:
                  type: integer
                  nullable: true
                  example: null
                habilitarSolicitacao:
                  type: boolean
                  example: true
                numeroSemestresCursar:
                  type: integer
                  example: 7
                descTunoOrigem:
                  type: string
                  nullable: true
                  example: "Matutino"
                semestreReferencia:
                  type: integer
                  example: 1
                anoReferencia:
                  type: integer
                  example: 2025
                notaEnemCandidato:
                  type: number
                  example: 495.34
                anoReferenciaNotaEnem:
                  type: integer
                  example: 2020
                jsonRetornoConsultaEnem:
                  type: string
                  example: "{\"nuCpf\":\"70966798120\",\"vlNotaEnemConsiderada\":\"495.34\",\"nuSemestreReferencia\":\"22020\",\"coInscricao\":6614627,\"nuAnoEnem\":\"2019\"}"
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
