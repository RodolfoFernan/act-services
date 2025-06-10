<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise Detalhada da Procedure FES.FESSPZ57_CRISE2019_CORR_VLRS por Etapas (Continuação)
Etapa 1: Identificar e Inativar Retenções de Liberações para Contratos Transferidos/Aditados com Divergência

Objetivo: Localizar liberações com retenções ativas e inativá-las caso o contrato associado tenha sido transferido ou aditado, ou se a retenção não for mais necessária para o ajuste de valores.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (L)
        FES.FESTB817_RETENCAO_LIBERACAO (R)
        FES.FESTB049_TRANSFERENCIA (T)
        FES.FESTB154_CAMPUS_INEP (C, I)
        FES.FESTB155_IES_INEP (E, N)
        FES.FESTB038_ADTMO_CONTRATO (A)
        FES.FESTB010_CANDIDATO (CA)
    Campos Relevantes:
        De FES.FESTB712_LIBERACAO_CONTRATO (L): NU_SQNCL_LIBERACAO_CONTRATO, NU_SEQ_CANDIDATO, AA_REFERENCIA_LIBERACAO, MM_REFERENCIA_LIBERACAO, NU_IES, IC_SITUACAO_LIBERACAO.
        De FES.FESTB817_RETENCAO_LIBERACAO (R): NU_SQNCL_LIBERACAO_CONTRATO, NU_MOTIVO_RETENCAO_LIBERACAO, DT_FIM_RETENCAO.
        De FES.FESTB049_TRANSFERENCIA (T): NU_CANDIDATO_FK10, NU_SEQ_TRANSFERENCIA, AA_REFERENCIA, NU_SEM_REFERENCIA, NU_CAMPUS_ORIGEM_FK161, NU_CAMPUS_DESTINO_FK161, DT_INCLUSAO, NU_STATUS_TRANSFERENCIA.
        De FES.FESTB154_CAMPUS_INEP (C, I): NU_CAMPUS, NU_IES_FK155.
        De FES.FESTB155_IES_INEP (E, N): NU_IES, NU_MANTENEDORA_FK156.
        De FES.FESTB038_ADTMO_CONTRATO (A): NU_CANDIDATO_FK36, AA_ADITAMENTO, NU_SEM_ADITAMENTO, DT_ADITAMENTO, NU_STATUS_ADITAMENTO.
        De FES.FESTB010_CANDIDATO (CA): NU_SEQ_CANDIDATO, DT_ADMISSAO_CANDIDATO.

Etapa 2: Atualizar a Situação de Liberações Efetivadas para 'NR' (Não Repassado)

Objetivo: Alterar o status de liberações que já foram "Efetivadas" ('E') para "Não Repassadas" ('NR') se elas pertencem a semestres onde há divergência entre o valor repassado e o contrato.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (L)
        FES.FESTB036_CONTRATO_FIES (A)
        FES.FESTB010_CANDIDATO (C)
    Campos Relevantes:
        De FES.FESTB712_LIBERACAO_CONTRATO (L): NU_SQNCL_LIBERACAO_CONTRATO, NU_SEQ_CANDIDATO, AA_REFERENCIA_LIBERACAO, MM_REFERENCIA_LIBERACAO, IC_SITUACAO_LIBERACAO, VR_REPASSE.
        De FES.FESTB036_CONTRATO_FIES (A): NU_CANDIDATO_FK11, VR_CONTRATO, NU_STATUS_CONTRATO.
        De FES.FESTB010_CANDIDATO (C): DT_ADMISSAO_CANDIDATO.

Etapa 3: Compensação e Novo Repasse de Liberações com Divergência entre Repasse e Contrato (Criação de Compensação e Atualização de Situação)

Objetivo: Registrar compensações para liberações com divergência entre o valor repassado e o contrato, e ajustar a situação da liberação para permitir um novo processamento.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (L)
        FES.FESTB036_CONTRATO_FIES (A, usado na sub-query D)
        FES.FESTB010_CANDIDATO (C, usado na sub-query D)
        FES.FESTB711_RLTRO_CTRTO_ANLTO (A, alias diferente para o join principal)
        FES.FESTB812_CMPSO_RPSE_INDVO (R)
    Tabelas Afetadas (Escrita):
        FES.FESTB812_CMPSO_RPSE_INDVO (Inserção)
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)
    Campos Relevantes:
        De FES.FESTB712_LIBERACAO_CONTRATO (L): NU_SQNCL_LIBERACAO_CONTRATO, NU_SEQ_CANDIDATO, AA_REFERENCIA_LIBERACAO, MM_REFERENCIA_LIBERACAO, IC_SITUACAO_LIBERACAO, VR_REPASSE.
        De FES.FESTB036_CONTRATO_FIES (A, sub-query D): NU_CANDIDATO_FK11, VR_CONTRATO, NU_STATUS_CONTRATO.
        De FES.FESTB010_CANDIDATO (C, sub-query D): CO_CPF, DT_ADMISSAO_CANDIDATO.
        De FES.FESTB711_RLTRO_CTRTO_ANLTO (A, join principal): NU_SQNCL_RLTRO_CTRTO_ANALITICO.
        De FES.FESTB812_CMPSO_RPSE_INDVO (R): NU_SQNCL_RLTRO_CTRTO_ANALITICO.

Etapa 4: Adequação do Valor das Liberações com Problema de Divergência entre Repasse e Contrato

Objetivo: Recalcular e ajustar os valores de repasse para as 6 parcelas de um semestre, garantindo que a soma total dos repasses de um semestre seja igual ao VR_CONTRATO.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (L)
        FES.FESTB036_CONTRATO_FIES (A)
        FES.FESTB010_CANDIDATO (C)
    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)
    Campos Relevantes:
        De FES.FESTB712_LIBERACAO_CONTRATO (L): NU_SEQ_CANDIDATO, AA_REFERENCIA_LIBERACAO, MM_REFERENCIA_LIBERACAO, VR_REPASSE.
        De FES.FESTB036_CONTRATO_FIES (A): NU_CANDIDATO_FK11, VR_CONTRATO, NU_STATUS_CONTRATO.
        De FES.FESTB010_CANDIDATO (C): DT_ADMISSAO_CANDIDATO.

Etapa 5: Tratamento de Divergência de Valores entre Repasse e Aditamento (Inserção de Retenções)

Objetivo: Identificar liberações onde a soma dos repasses difere do valor do aditamento e registrar uma retenção para essas liberações.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO
        FES.FESTB038_ADTMO_CONTRATO
        FES.FESTB817_RETENCAO_LIBERACAO (para verificar retenção existente)
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Inserção)
    Campos Relevantes:
        De FES.FESTB712_LIBERACAO_CONTRATO: NU_SQNCL_LIBERACAO_CONTRATO, NU_SEQ_CANDIDATO, AA_REFERENCIA_LIBERACAO, MM_REFERENCIA_LIBERACAO, VR_REPASSE.
        De FES.FESTB038_ADTMO_CONTRATO: NU_CANDIDATO_FK36, AA_ADITAMENTO, NU_SEM_ADITAMENTO, VR_ADITAMENTO, NU_STATUS_ADITAMENTO.
        De FES.FESTB817_RETENCAO_LIBERACAO: NU_SQNCL_LIBERACAO_CONTRATO, NU_MOTIVO_RETENCAO_LIBERACAO, DT_FIM_RETENCAO.

Etapa 6: Compensação e Novo Repasse de Liberações com Divergência entre Repasse e Aditamento (Criação de Compensação e Atualização de Situação)

Objetivo: Registrar compensações para liberações com divergência entre o valor repassado e o aditamento, e ajustar a situação da liberação para permitir um novo processamento.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (L)
        FES.FESTB038_ADTMO_CONTRATO (A, usado na sub-query D)
        FES.FESTB711_RLTRO_CTRTO_ANLTO (A, alias diferente para o join principal)
        FES.FESTB812_CMPSO_RPSE_INDVO (R)
    Tabelas Afetadas (Escrita):
        FES.FESTB812_CMPSO_RPSE_INDVO (Inserção)
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)
    Campos Relevantes:
        De FES.FESTB712_LIBERACAO_CONTRATO (L): NU_SQNCL_LIBERACAO_CONTRATO, NU_SEQ_CANDIDATO, AA_REFERENCIA_LIBERACAO, MM_REFERENCIA_LIBERACAO, IC_SITUACAO_LIBERACAO, VR_REPASSE.
        De FES.FESTB038_ADTMO_CONTRATO (D): NU_CANDIDATO_FK36, AA_ADITAMENTO, NU_SEM_ADITAMENTO, VR_ADITAMENTO, NU_STATUS_ADITAMENTO.
        De FES.FESTB711_RLTRO_CTRTO_ANLTO (A, join principal): NU_SQNCL_RLTRO_CTRTO_ANALITICO.
        De FES.FESTB812_CMPSO_RPSE_INDVO (R): NU_SQNCL_RLTRO_CTRTO_ANALITICO.

Etapa 7: Adequação do Valor das Liberações com Problema de Divergência entre Repasse e Aditamento

Objetivo: Recalcular e ajustar os valores de repasse para as 6 parcelas de um semestre, garantindo que a soma total dos repasses de um semestre seja igual ao VR_ADITAMENTO.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (L)
        FES.FESTB038_ADTMO_CONTRATO (A)
    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização)
    Campos Relevantes:
        De FES.FESTB712_LIBERACAO_CONTRATO (L): NU_SEQ_CANDIDATO, AA_REFERENCIA_LIBERACAO, MM_REFERENCIA_LIBERACAO, VR_REPASSE.
        De FES.FESTB038_ADTMO_CONTRATO (A): NU_CANDIDATO_FK36, AA_ADITAMENTO, NU_SEM_ADITAMENTO, VR_ADITAMENTO, NU_STATUS_ADITAMENTO.











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
