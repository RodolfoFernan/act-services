<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>

    
Classe: RepasseTituloServiceImpl

Descrição: Classe responsável pelo processo batch de repasse de títulos no sistema SIFES, que inclui a apuração, cálculo, verificação de saldo e integralização no FG-FIES, além da persistência das movimentações e atualização de históricos.
Principais Tabelas Utilizadas:

    ApuracaoRepasseTO: Contém os dados de apuração de repasses de títulos.
    PrecoUnitarioTO: Armazena os preços unitários utilizados nos cálculos.
    MovimentacaoTituloTO: Registra os movimentos de repasse de títulos.
    RelatorioContratacaoSinteticoTO: Utilizado para obter informações sintéticas das mantenedoras.

Métodos Principais:
1. apuraRepasseTitulos(Integer mesRef, Integer anoRef)

Descrição: Inicia o processo de apuração e cálculo do repasse de títulos.

Fluxo:

    Define o mês e ano de referência do período anterior.
    Executa uma consulta para buscar registros de apuração no banco.
    Caso não haja apuração, o processo é interrompido.
    Busca o preço unitário para o próximo mês.
    Se o preço unitário não estiver cadastrado, interrompe o processo.
    Para cada mantenedora encontrada, realiza os cálculos de repasse.

2. Cálculo do Repasse de Títulos para a Mantenedora

Descrição: Obtém o valor residual do último movimento da mantenedora, soma esse valor ao repasse calculado e converte o valor final para títulos.

Fluxo:

    Consulta do Valor Residual:
        Chama getResiduoUltimoMovimentoMantenedora(nuMantenedora) para obter o resíduo financeiro do último repasse.
    Cálculo do Valor Total de Repasse:
        Obtém o valor do repasse já calculado e adiciona o valor residual.
    Conversão para Quantidade de Títulos:
        Divide o valor total de repasse pelo preço unitário (valorPU) e converte para long.
    Cálculo do Valor Residual Financeiro:
        Calcula o valor do repasse sem resíduo e o valor financeiro não convertido em títulos.
    Verificação de Saldo:
        Verifica se há saldo suficiente para a quantidade de títulos calculada. Caso contrário, interrompe o processo.

3. Consolidação do Repasse e Registro na Base de Dados

Fluxo:

    Consulta do Título Associado ao Repasse:
        Chama consultaTitulo(repasseTitulos) para obter o ID do título.
    Registro de Integralização FGFIES:
        Chama realizaIntegralizacaoFGFies() para registrar a integralização dos valores no Fundo Garantidor (FGFIES).
    Preenchimento dos Dados do Repasse:
        Define os atributos de repasseTituloTO com dados como código do usuário, mantenedora, valor do movimento, número de títulos movimentados, valor residual e data de movimentação.
    Atualização do Saldo da Mantenedora:
        Chama atualizaSaldoMantenedoraFies() para ajustar o saldo da mantenedora no FIES.
    Gravação do Histórico:
        Chama gravarHistoricoTitulo() para registrar o histórico da movimentação.
    Persistência no Banco de Dados:
        Insere o registro na tabela MovimentacaoTituloTO e garante a efetivação da transação com entityManager.flush() e entityManager.clear().

Métodos Auxiliares:
4.1. consultaTitulo(Long repasseTitulos)

Descrição: Consulta o título associado ao repasse.

Fluxo:

    Utiliza a query MovimentacaoTituloTO.QUERY_CONSULTA_TITULO_REPASSE para buscar o título e retorna o ID do título associado ao repasse.

4.2. gravarHistoricoTitulo(MovimentacaoTituloTO repasseTituloTO, BigDecimal valorTotal, BigDecimal taxaAdm, BigDecimal vlrIntegralizacao)

Descrição: Persiste o histórico da movimentação dos títulos.

Fluxo:

    Consulta o ID da Apuração:
        Executa ApuracaoRepasseTO.QUERY_CONSULTA_ID para obter o ID da apuração.
    Consulta o Último ID de Apropriação:
        Obtém o último ID de apropriação com QUERY_MAX_ID_APROPRIACAO. Se não houver, inicia com 0L.
    Preenchimento dos Dados do Histórico:
        Define atributos como referência temporal, situação de apropriação e valores do repasse.
    Persiste o Histórico:
        Insere o histórico de movimentação na base de dados.

Pontos de Atenção na Análise:
1. Atualização de Saldo (atualizaSaldoMantenedoraFies)

    Certifique-se de que a multiplicação por -1 nos valores e cotas seja adequada às regras de negócio.

2. Verificação de Saldo (fiesPossuiSaldoParaRepasse)

    Verifique se o método está tratando corretamente valores nulos para evitar NullPointerException ao verificar saldo.

3. Integralização no FG-FIES (realizaIntegralizacaoFGFies)

    A integralização depende de pcIntegralizacao > 0.
    Verifique se a consulta de cotas retorna resultados válidos e trate os casos onde valorCota é 0.

4. Consulta de Cota (consultaCota)

    Assegure que os parâmetros de referência de mês e ano anterior estejam corretamente inicializados.

5. Adição de Mantenedora ao Fundo (adicionarMantenedoraNoFundo)

    Verifique se a conversão de CNPJ para Long está correta para evitar NumberFormatException.

6. Resíduo do Último Movimento (getResiduoUltimoMovimentoMantenedora)

    Certifique-se de que a conversão dos campos de referência de movimento está correta para evitar falhas na consulta.

Sugestões para Análise:

    Testes Unitários: Criar testes para cenários de repasse, saldo insuficiente e integralização.
    Verificação de Nulos: Melhorar o tratamento de valores nulos para evitar exceções inesperadas.
    Validação de Regras: Certificar-se de que os cálculos financeiros e integrações estão corretos e atendem às regras do FG-FIES.

Se precisar de mais detalhes ou ajustes, estou à disposição!




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
