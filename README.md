<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:

Observação Geral: Todas as SPs mencionadas parecem estar relacionadas a correções e ajustes no processo de repasse do FIES, principalmente em cenários de crise ou situações atípicas, como a pandemia de COVID-19 (CRISE2019). O objetivo principal é garantir que os repasses sejam feitos de forma correta, considerando diversos fatores como aditamentos, transferências de alunos, suspensões, e inconsistências nos valores.
FES.FESSPU20_VINCULA_LIBERACAO

    Visão Negocial: Vincula liberações ao contrato ou aditamento.
    Motivação/Circunstâncias: Criada para associar as liberações de crédito do FIES aos contratos ou aditamentos correspondentes. Essa vinculação é essencial para o correto processamento dos repasses.
    Tabelas Impactadas:
        FES.FESTB038_ADTMO_CONTRATO
        FES.FESTB712_LIBERACAO_CONTRATO
        FES.FESTB817_RETENCAO_LIBERACAO
    Impacto no Fluxo Javaweb/FES.REPASSE: Essa SP é crucial para o fluxo de repasse, pois garante que cada liberação seja corretamente direcionada ao contrato ou aditamento, permitindo o cálculo correto dos valores a serem repassados.

FES.FESSPZ57_CRISE2019_CORR_VLRS

    Visão Negocial: Corrige valores de repasse.
    Motivação/Circunstâncias: Criada para corrigir erros nos valores de repasse, como o problema relatado onde o formato da data estava incorreto, causando falha na atualização dos valores. Essa SP foi utilizada para corrigir problemas específicos identificados nos repasses de Maio de 2023.
    Tabelas Impactadas:
        FESTB812_CMPSO_RPSE_INDVO
        FESTB712_LIBERACAO_CONTRATO
        FESTB038_ADTMO_CONTRATO
        FESTB711_RLTRO_CTRTO_ANLTO
    Impacto no Fluxo Javaweb/FES.REPASSE: Garante a correção dos valores de repasse, evitando pagamentos incorretos. A correção do formato da data é crucial para o funcionamento correto da rotina.

FES.FESSPZ55_CRISE2019_TRATA_SUSP

    Visão Negocial: Trata suspensões (tácitas e outras) nos repasses.
    Motivação/Circunstâncias: Criada para lidar com o impacto das suspensões (incluídas e excluídas) nos repasses. Essa SP garante que os repasses sejam ajustados corretamente em casos de suspensão, considerando critérios de validação como a inexistência de ocorrências de suspensão/encerramento, aditamentos de renovação, e contratos finalizados.
    Tabelas Impactadas:
        FESTB812_CMPSO_RPSE_INDVO
        FESTB712_LIBERACAO_CONTRATO
        FESTB010_CANDIDATO
        FESTB711_RLTRO_CTRTO_ANLTO
        FESTB817_RETENCAO_LIBERACAO
        FESTB057_OCRRA_CONTRATO
    Impacto no Fluxo Javaweb/FES.REPASSE: Essencial para o cálculo correto dos repasses em cenários de suspensão, garantindo que os valores sejam ajustados de acordo com os critérios de validação.

FES.FESSPZA6_COMPENSACAO_REPASSE

    Visão Negocial: Realiza a compensação de repasses.
    Motivação/Circunstâncias: Criada para gerenciar a compensação de valores de repasse, identificando e marcando as compensações como "N" (não compensado) no ambiente TGE/EXADATA. Também lida com retenções, garantindo que as liberações ausentes sejam corretamente processadas.
    Tabelas Impactadas:
        FESTB812_CMPSO_RPSE_INDVO
        FESTB712_LIBERACAO_CONTRATO
        FESTB818_MOTIVO_RETENCAO_LBRCO
        FESTB711_RLTRO_CTRTO_ANLTO
        FESTB817_RETENCAO_LIBERACAO
    Impacto no Fluxo Javaweb/FES.REPASSE: Permite o controle e a gestão das compensações de repasse, garantindo que os valores sejam ajustados corretamente.

FES.FESSPZ45_CRISE2019_ALTER_LIB_2

    Visão Negocial: Atualiza liberações para não apto (troca de mantenedora, suspensão, estorno e transferência).
    Motivação/Circunstâncias: Criada para regularizar situações de repasse, como inconsistências no percentual de financiamento e ajustes na IES da liberação para estudantes com transferência. Essa SP garante que os valores das liberações correspondam aos valores contratados no aditamento e que a IES correta seja associada a cada liberação, mesmo em casos de transferência.
    Tabelas Impactadas:
        FESTB812_CMPSO_RPSE_INDVO
        FESTB712_LIBERACAO_CONTRATO
        FESTB818_MOTIVO_RETENCAO_LBRCO
        FESTB711_RLTRO_CTRTO_ANLTO
        FESTB817_RETENCAO_LIBERACAO
        FESTB049_TRANSFERENCIA
        FESTB154_CAMPUS_INEP
        FESTB155_IES_INEP
        FESTB038_ADTMO_CONTRATO
        FESTB010_CANDIDATO
    Impacto no Fluxo Javaweb/FES.REPASSE: Garante a correção das liberações em casos de inconsistências de financiamento e transferência, permitindo o repasse correto dos valores.

FES.FESSPZ37_CRISE19_FIM_RETENCAO

    Visão Negocial: Finaliza retenções de liberações retidas por transferência ou suspensão.
    Motivação/Circunstâncias: Criada para definir e aplicar regras para a finalização de retenções de liberações retidas por transferência ou suspensão, permitindo o repasse de liberações consistentes.
    Tabelas Impactadas:
        FESTB049_TRANSFERENCIA
        FESTB038_ADTMO_CONTRATO
        FESTB010_CANDIDATO
    Impacto no Fluxo Javaweb/FES.REPASSE: Permite a liberação de repasses que estavam retidos devido a transferências ou suspensões, desde que as liberações estejam consistentes com as regras definidas.

FES.FESSPZ41_CRISE19_FIM_RETENC_2

    Visão Negocial: Propõe uma prévia de trabalho para a apuração de maio/2020, contendo regularizações como a não retenção de contratos com transferência e ajustes nas rotinas de retenção e liberação.
    Motivação/Circunstâncias: Criada para ajustar o processo de repasse em situações específicas, como a pandemia, garantindo que as liberações sejam processadas corretamente em casos de transferência e suspensão.
    Impacto no Fluxo Javaweb/FES.REPASSE: Essa SP é crucial para o correto processamento dos repasses, especialmente em cenários de crise, garantindo que as liberações sejam tratadas de acordo com as regras específicas para transferência e suspensão.











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
