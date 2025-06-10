<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Análise do Bloco de Código: Alteração de Suspensão e Vigência
Análise da Procedure FES.FESSPZ57_CRISE2019_CORR_VLRS (Continuação)

Os próximos blocos da procedure focam na adequação dos valores de repasse em diferentes cenários de inconsistência e na correção da situação das liberações associadas.
Adequação do Valor das Liberações com Problema de Deslocamento no Repasse

Este bloco visa corrigir os valores de repasse em liberações onde foi detectado um problema de "deslocamento" no montante repassado, redistribuindo o valor do aditamento entre as 6 parcelas do semestre.

    Objetivo: Recalcular e atualizar o VR_REPASSE para cada uma das 6 liberações mensais de um dado semestre, para aqueles candidatos que já foram identificados com problema de "deslocamento" no valor do repasse (ou seja, o valor do aditamento é maior que a soma dos repasses, e a proporção entre eles segue um padrão específico, como múltiplos de 10 ou um fator de 100).

    Lógica Principal:
        Cursor (FOR X IN (...)): Seleciona os NU_CANDIDATO_FK36, AA_ADITAMENTO, NU_SEM_ADITAMENTO e VR_ADITAMENTO de aditamentos válidos (status > 3, valor > 0) que estão relacionados a liberações de contrato.
        Cláusula HAVING: A condição HAVING é a mesma do bloco anterior, que identifica os semestres com as seguintes características:
            COUNT(L.VR_REPASSE) = 6: O aditamento tem exatamente 6 repasses associados (indicando um semestre completo).
            SUM(L.VR_REPASSE) > 0: A soma dos repasses não é zero.
            VR_ADITAMENTO > SUM(VR_REPASSE): O valor total do aditamento é maior que a soma dos repasses.
            A condição de proporção (usando MOD e ROUND) que indica um provável "deslocamento" de valor (ex: diferença de um fator de 10 ou 100).
        Loop Interno (FOR Lcntr IN 1..6): Para cada aditamento/semestre identificado (que satisfaz a condição de "deslocamento"), este loop itera 6 vezes, uma para cada mês do semestre.
            Cálculo de vr_repasse: O valor de repasse para cada mês é calculado dividindo o VR_ADITAMENTO por 6 e arredondando para duas casas decimais (TRUNC(X.VR_ADITAMENTO / 6, 2)). A última parcela (mês 6) recebe o valor residual para garantir que a soma dos 6 repasses seja exatamente igual ao VR_ADITAMENTO.
            Definição do mm_repasse: O mês de repasse é ajustado de acordo com o semestre (Lcntr para o 1º semestre, Lcntr + 6 para o 2º semestre).
            Atualização do VR_REPASSE: Uma instrução UPDATE dinâmica (EXECUTE IMMEDIATE SQL_QUERY) é gerada e executada para atualizar o VR_REPASSE e DT_ATUALIZACAO na tabela FES.FESTB712_LIBERACAO_CONTRATO para a liberação específica (candidato, ano e mês de referência).
        COMMIT por Candidato: Após processar as 6 parcelas de um candidato/semestre, um COMMIT é realizado. Isso garante que as alterações sejam salvas progressivamente e evita que transações grandes demais causem problemas de rollback em caso de falha.

    Tabelas Envolvidas:
        FES.FESTB712_LIBERACAO_CONTRATO (leitura e atualização)
        FES.FESTB038_ADTMO_CONTRATO (leitura)

    Campos Chave no SELECT do cursor:
        A.NU_CANDIDATO_FK36
        A.AA_ADITAMENTO
        A.NU_SEM_ADITAMENTO
        A.VR_ADITAMENTO

    Campos Atualizados no UPDATE (dinâmico):
        VR_REPASSE
        DT_ATUALIZACAO

Adequação do Valor das Liberações com Repasse Zerado

Este bloco é similar ao anterior, mas foca em outro tipo de inconsistência: semestres onde o valor de repasse está zerado.

    Objetivo: Recalcular e atualizar o VR_REPASSE para cada uma das 6 liberações mensais de um dado semestre, para aqueles candidatos que possuem liberações com VR_REPASSE igual a zero para o semestre, mas o aditamento associado tem um VR_ADITAMENTO positivo.

    Lógica Principal:
        Cursor (FOR X IN (...)): Seleciona os mesmos dados que o bloco anterior.
        Cláusula HAVING: A condição HAVING é simplificada:
            COUNT(L.VR_REPASSE) = 6: O aditamento tem exatamente 6 repasses associados.
            SUM(L.VR_REPASSE) = 0: A soma dos repasses é zero (indicando que não houve repasse efetivo).
        Loop Interno (FOR Lcntr IN 1..6): Para cada aditamento/semestre identificado com repasses zerados, este loop itera 6 vezes.
            Cálculo de vr_repasse: O cálculo do vr_repasse para cada mês é idêntico ao do bloco anterior, garantindo que o valor do aditamento seja distribuído igualmente entre as 6 parcelas, com o ajuste final na última parcela.
            Definição do mm_repasse: O mês de repasse é ajustado de acordo com o semestre.
            Atualização do VR_REPASSE: Uma instrução UPDATE dinâmica (EXECUTE IMMEDIATE SQL_QUERY) é gerada e executada para atualizar o VR_REPASSE e DT_ATUALIZACAO na tabela FES.FESTB712_LIBERACAO_CONTRATO.
        COMMIT por Candidato: Após processar as 6 parcelas de um candidato/semestre, um COMMIT é realizado.
        Atualização da Situação da Liberação: Diferentemente do bloco anterior, após o commit das atualizações de VR_REPASSE para todas as 6 parcelas, há um UPDATE adicional que muda o IC_SITUACAO_LIBERACAO para 'NR' (Não Repassado) para todas as liberações daquele candidato/semestre que estavam com status 'R', 'NE' ou 'E'. Isso garante que, uma vez que os valores foram corrigidos, a situação da liberação seja redefinida para "Não Repassado", indicando que um novo processo de repasse pode ser necessário ou que o status precisa ser reavaliado com base nos novos valores.
        COMMIT Final: Outro COMMIT é feito após a atualização da situação da liberação.

    Tabelas Envolvidas:
        FES.FESTB712_LIBERACAO_CONTRATO (leitura e atualização)
        FES.FESTB038_ADTMO_CONTRATO (leitura)

    Campos Chave no SELECT do cursor:
        A.NU_CANDIDATO_FK36
        A.AA_ADITAMENTO
        A.NU_SEM_ADITAMENTO
        A.VR_ADITAMENTO

    Campos Atualizados no UPDATE (dinâmico e final):
        VR_REPASSE
        DT_ATUALIZACAO
        IC_SITUACAO_LIBERACAO (para 'NR')




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
