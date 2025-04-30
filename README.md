<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>

    
Segue abaixo um parecer técnico consolidado referente ao Item de Backlog 22771690 - NOVO FIES - AGENTE OPERADOR - REPASSE DUPLICADO, com organização das informações técnicas e contexto dos eventos:
🧾 Resumo do Problema

Item de Backlog: 22771690
Título: NOVO FIES - AGENTE OPERADOR - REPASSE DUPLICADO
Resumo Técnico:
Foram identificadas liberações que foram indevidamente repassadas duas vezes no repasse de fevereiro/2025 (02/2025). Essas liberações já haviam sido repassadas anteriormente, ocasionando duplicidade de repasse.
🔎 Causa Raiz

A duplicidade está relacionada à demanda anterior 22278713, na qual aditamentos revalidados pelo app causaram o sumiço da última liberação do semestre 1/2024. Como correção, liberações foram recriadas manualmente, mas não houve controle de que essas liberações já haviam sido repassadas anteriormente, gerando repasses duplicados.
📌 Demanda Original Relacionada

Item de Backlog: 22278713
Título: NOVO FIES - ADITAMENTO REVALIDADO PELO APP SUMIU COM ÚLTIMA LIBERAÇÃO
Problema: A revalidação de aditamentos já contratados fez com que a última liberação desaparecesse, gerando necessidade de recriação.
Exemplos de casos:

    CPF: 10361402961, 441.844.118-27

    Outros CPFs:

        143.231.746-67

        134.280.946-70

        154.806.536-60

        165.237.576-76

        059.015.631-40

        088.401.083-00

        718.450.694-47

        082.379.504-70

        104.018.975-08

        086.786.894-55

        021.235.666-67

🛠️ Correção Aplicada

Stored Procedure Criada: FES.FESSPZA0_COMPENSA_RPSE_INDEVIDO()
Objetivo:
Compensar automaticamente os repasses duplicados, criando registros de retenção para serem tratados na próxima execução do fluxo de repasse.

Execução da Procedure:

    Início da execução: Jair Jose dos Santos

    Ambiente: TGE

    Data: 21/02/2025

    Quantidade de compensações criadas: 16.017

📂 Evidências de Teste (Clear Case)

    SIFES_RM_22771690_LIBERACOES_APURADAS.xlsx

    SIFES_RM_22771690_LIBERACOES_CRIADAS.xlsx

    SIFES_RM_22771690_LIBERACOES_ALTERADAS.xlsx

    SIFES_RM_22771690_COMPENSACOES_CRIADAS.xlsx

🔄 Plano de Retorno (Caso Necessário)

Requisição de Mudança: 22777223
Caso seja necessário rollback:

    Restaurar backups das tabelas:

        FES.FESTB817_RETENCAO_LIBERACAO

        FES.FESTB812_CMPSO_RPSE_INDVO

    A procedure não exige ação adicional para rollback.

📎 Anexos Técnicos Relevantes

    RETENCOES CRIADAS FESSPZA0.xlsx

    LIBERACOES CRIADAS.xlsx

    LIBERACOES ALTERADAS - POS EXECUCAO SPs REPASSE.xlsx

    LIBERACOES APURADAS - POS EXECUCAO ROTINA FESREPASSE.xlsx

    COMPENSACOES CRIADAS FESSPZA0.xlsx

✅ Status Atual

    Procedure executada com sucesso.

    Requisição RTC 22771690 virada para "Pronto".

    Evidências de execução coletadas.

    Item pronto para validação de qualidade.

Se desejar, posso gerar um documento formal (PDF ou Word) com esse parecer. Deseja que eu faça isso?




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
