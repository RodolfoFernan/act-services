<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>

    

 emprest/transferenciaContrato/buscarEstudante:
  get:
    summary: Busca informações do estudante para transferência de contrato.
    description: Retorna detalhes do estudante com base no Código FIES e CPF.
    parameters:
      - in: query
        name: codFies
        schema:
          type: string
        description: Código FIES do estudante.
        required: true
        example: "20242515"
      - in: query
        name: cpf
        schema:
          type: string
          pattern: '^[0-9]{11}$'
        description: CPF do estudante (apenas números).
        required: true
        example: "70966798120"
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
                codigo:
                  type: integer
                tipo:
                  type: string
                  nullable: true
                editavel:
                  nullable: true
                idTransferencia:
                  nullable: true
                codFies:
                  type: integer
                cpfCandidato:
                  type: string
                nomeCandidato:
                  type: string
                tipoTransferencia:
                  nullable: true
                idIes:
                  type: integer
                nuMantenedora:
                  type: integer
                nuCampus:
                  type: integer
                nuCurso:
                  type: integer
                nuTurno:
                  type: integer
                nomeIes:
                  type: string
                nomeMantenedora:
                  type: string
                turnoDescDestino:
                  type: string
                uf:
                  type: string
                municipio:
                  type: string
                endereco:
                  type: string
                nomeCampus:
                  type: string
                nomeCurso:
                  type: string
                duracaoRegularCurso:
                  type: integer
                nuSemestresCursados:
                  type: integer
                qtSemestresDilatado:
                  type: integer
                qtSemestresSuspenso:
                  type: integer
                iesDestino:
                  nullable: true
                nuMantenedoraDestino:
                  nullable: true
                campusDestino:
                  nullable: true
                cursoDestino:
                  nullable: true
                turnoDestino:
                  nullable: true
                nomeIesDestino:
                  nullable: true
                nomeMantenedoraDestino:
                  nullable: true
                ufDestino:
                  nullable: true
                municipioDestino:
                  nullable: true
                enderecoDestino:
                  nullable: true
                nomeCampusDestino:
                  nullable: true
                nomeCursoDestino:
                  nullable: true
                transferenciasRealizadas:
                  type: array
                  items:
                    type: object # Você pode definir a estrutura dos itens se souber
                icCondicaoFuncionamento:
                  type: string
                icSituacaoContrato:
                  type: string
                icSituacaoIES:
                  type: string
                nuOperacaoSiapi:
                  type: integer
                totalSemestresContratados:
                  type: integer
                totalSemestresUtilizados:
                  type: integer
                totalSemestresDestino:
                  nullable: true
                habilitarSolicitacao:
                  type: boolean
                numeroSemestresCursar:
                  type: integer
                descTunoOrigem:
                  type: string
                semestreReferencia:
                  type: integer
                anoReferencia:
                  type: integer
                  format: int32
                notaEnemCandidato:
                  type: number
                anoReferenciaNotaEnem:
                  type: integer
                  format: int32
                jsonRetornoConsultaEnem:
                  type: string
                estudantePodeTransfCurso:
                  type: string
                totalSemestresDisponiveis:
                  type: integer
      '400':
        description: Requisição inválida - Algum parâmetro está ausente ou incorreto.
      '500':
        description: Erro interno do servidor.




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
