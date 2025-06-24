<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:
Excelente! Vamos detalhar a FES.FESSPU20_VINCULA_LIBERACAO no formato solicitado:
FES.FESSPU20_VINCULA_LIBERACAO

    Objetivo: Vincular as liberações de contrato a seus respectivos aditamentos ou contratos iniciais, marcando o tipo de transação e a participação do candidato. Além disso, a SP insere retenções para liberações que não conseguem ser vinculadas.

Quando Rodamos a rotina fesTotal (ela dropa e recria os Índices de algumas tabelas ) , porem tivemos problemas em carregar um indeci especifico de uma tabela mencionada a baixo , porém essa oitra tabela é um usado em um processo de batch que não sabíamos a conexão com a festotal , o processo de calculo de integralização
essas são as três tabelas e gostariasmo de entender para que ela é usada e oque o método faz , qual a sua necessidade no processo . 
Tabelas:
FESTB062_CONTRATO_SIAPI
CalculaPercentualIntegralizacaoImpl
  isAtualizarPercentualMantenedora-----usado no calculo Percentual Mantenedoras que serve para ?
  consultaValoresCoparticipacao
  COPARTICIPACAO ADIMPLENTE


=================================
FESTB069_EXTRATO_SIAPI-----Não carregou ontem quando rodou com 
utilizada em :
 CalculaPercentualIntegralizacaoImpl
   consultaValoresCoparticipacao
     COPARTICIPACAO ADIMPLENTE



consulta.qtde.inadimplencia=SELECT COUNT(DISTINCT T69.CO_CPF_CNPJ) FROM FES.FESTB069_EXTRATO_SIAPI T69, FES.FESTB036_CONTRATO_FIES T36, FES.FESTB011_CNDDO_CURSO_IES T11, FES.FESVW003_IES_MANTENEDORA_CMPS T03 WHERE T69.NU_SUREG_AGENCIA_CNTRO_FK62 = T36.NU_SUREG_AGENCIA_CONTRATO AND T69.NU_UNIDADE_CONTRATO_FK62 = T36.NU_UNIDADE_CONTRATO_FK25 AND T69.NU_OPERACAO_SIAPI_FK62 = T36.NU_OPERACAO_SIAPI AND T69.NU_CONTRATO_FK62 = T36.NU_CONTRATO AND T69.NU_DV_CONTRATO_FK62 = T36.NU_DV_CONTRATO AND T36.NU_CANDIDATO_FK11 = T11.NU_CANDIDATO_FK10 AND T11.NU_CAMPUS_FK170 = T03.NU_CAMPUS AND T69.NU_SITUACAO_EXTRATO_FK82 = 1 AND T03.NU_MANTENEDORA = ? AND T36.NU_OPERACAO_SIAPI = 187

===================================
FESTB071_LIBERACAO_SIAPI ==========Nada no Batch



private void calculoPercentualMantenedoras(ParametrosIntegralizacaoTO parametro2ao5ano, ParametrosIntegralizacaoTO parametro6ao7ano) {
        final List<IntegralizacaoAdesaoMantenedora> listaIntegralizacaoMantenedoras = new ArrayList<>();

        final List<AdesaoTO> listaAdesaoMantenedoras = consultaSemestreAdesaoMantenedora();
        for (final AdesaoTO adesao: listaAdesaoMantenedoras) {
            final Long numeroMantenedora = adesao.getNumeroMantenedora();
            logger.debug("***************************** Iniciando calculos mantenedora {} *****************************", numeroMantenedora);

            final Date dataInicioSemestreAdesao = Utils.getDataInicioSemestre(adesao.getSemestre(), adesao.getAno());
            final Date dataInicioSemestreRef = Utils.getDataInicioSemestre(SEMESTRE, ANO);
            final int diffMeses = Utils.diffMesesDatas(dataInicioSemestreAdesao, dataInicioSemestreRef);
            final int anoPosicaoMantenedora = getAnoPosicao(diffMeses);
            final boolean isAtualizarMantenedora = isAtualizarPercentualMantenedora(diffMeses);
            final IntegralizacaoAdesaoMantenedora integralizacao = new IntegralizacaoAdesaoMantenedora(anoPosicaoMantenedora, numeroMantenedora, adesao, parametro2ao5ano);

            logger.debug(
                "Semestre/ano adesao: {}/{}, ano posicao: {}, percentual vai atualizar: {}",
                adesao.getSemestre(), adesao.getAno(), anoPosicaoMantenedora, isAtualizarMantenedora
            );

            /* buscar os valores pra calcular o valor de c (indice inadimplencia)
             * c  =  __Saldo de Coparticipação em atraso superior a 90 (noventa) dias em DD/MM/AAAA__
             *                    Saldo Total de Coparticipação em DD/MM/AAAA
             */
            consultaValoresCoparticipacao(integralizacao);
            logger.debug(
                "valor coparticipacao atraso 90: {}, valor coparticipacao: {}",
                integralizacao.getValorSaldoCoparticipacaoInadimplente(), integralizacao.getValorSaldoCoparticipacao()
            );

            /* buscar os valores para calcular o valor de e (indice evasao)
             * e  =  __Qte. Estudantes (Semestre 0X/20XX) com parcela em atraso não aditados em DD/MM/AAAA)__
             *                                  Quantidade total de contratos
             */
            consultaQuantidadeContratos(integralizacao);
            logger.debug(
                "Quantidade Contatos Com Parcela Em Atraso sem adit: {}, Quantidade Total Contratos: {}",
                integralizacao.getQuantidadeContatosComParcelaEmAtraso(), integralizacao.getQuantidadeTotalContratos()
            );

            /* calcular o valor de x
             * x = α x c + ß * e
             */
            BigDecimal x = parametro2ao5ano.getValorPesoAlfa().multiply(integralizacao.getIndiceInadimplencia());
            x = x.add( parametro2ao5ano.getValorPesoBeta().multiply(integralizacao.getIndiceEvasao()) );
            integralizacao.setX(x);
            logger.debug(
                "indice inadimplencia (c): {}, indice evasao (e): {}, valor de x: {}",
                integralizacao.getIndiceInadimplencia(), integralizacao.getIndiceEvasao(), x
            );

            /* salvar na FESTB842
             * obs: se o cadastro da mantenedora for só para controle, salvar o ano posição negativo
             * esse ano posição negativo deve ser ignorado pela tela de consulta
             */
            IntegralizacaoAdesaoMantenedoraTO integralizacaoTO = new IntegralizacaoAdesaoMantenedoraTO(integralizacao, isAtualizarMantenedora);
            entityManager.merge(integralizacaoTO);

            listaIntegralizacaoMantenedoras.add(integralizacao);
        }

        // inclui no banco as alteracoes
        entityManager.flush();

        /* calcular média mantenedoras da data de movimento */
        /* calcular desvio padrão mantenedoras da data de movimento */
        cacularMediaEDesvioPadraoDeX(listaIntegralizacaoMantenedoras);
        logger.debug("Media valor de X: {}, Valor desvio padrao de X: {}", this.valorMedioX, this.valorDesvioPadraoX);

        for (IntegralizacaoAdesaoMantenedora integralizacao: listaIntegralizacaoMantenedoras) {
            /* calcular percentual integralização das mantenedoras do 2 ao 5 ano */
            if (integralizacao.getAnoPosicaoAdesaoMantenedora().equals(1)) {
                // percentual fixo no primeiro ano de integralizacao
                atualizarPercentualMantenedora(integralizacao.getNumeroMantenedora(), PC_INTEGRALIZACAO_PRIMEIRO_ANO);
            } else if (integralizacao.getAnoPosicaoAdesaoMantenedora().compareTo(2) >= 0 && integralizacao.getAnoPosicaoAdesaoMantenedora().compareTo(5) <= 0) {
                calcularPercentualIntegralizacao(integralizacao, parametro2ao5ano);
                IntegralizacaoAdesaoMantenedoraID id = new IntegralizacaoAdesaoMantenedoraID(integralizacao.getAnoPosicaoAdesaoMantenedora(), integralizacao.getNumeroMantenedora());
                IntegralizacaoAdesaoMantenedoraTO to = entityManager.find(IntegralizacaoAdesaoMantenedoraTO.class, id);

                to.setPercentualIntegralizacao(integralizacao.getPercentualIntegralizacao());
                entityManager.merge(to);

                // ATUALIZAR PERCENTUAL NA FESTB156
                atualizarPercentualMantenedora(integralizacao.getNumeroMantenedora(), integralizacao.getPercentualIntegralizacao());
            } else if (integralizacao.getAnoPosicaoAdesaoMantenedora().compareTo(6) >= 0 && integralizacao.getAnoPosicaoAdesaoMantenedora().compareTo(7) <= 0) {
                calcularPercentualIntegralizacao6ao7Ano(integralizacao, parametro6ao7ano);
                IntegralizacaoAdesaoMantenedoraID id = new IntegralizacaoAdesaoMantenedoraID(integralizacao.getAnoPosicaoAdesaoMantenedora(), integralizacao.getNumeroMantenedora());
                IntegralizacaoAdesaoMantenedoraTO to = entityManager.find(IntegralizacaoAdesaoMantenedoraTO.class, id);

                to.setPercentualIntegralizacao(integralizacao.getPercentualIntegralizacao());
                entityManager.merge(to);

                // ATUALIZAR PERCENTUAL NA FESTB156
                atualizarPercentualMantenedora(integralizacao.getNumeroMantenedora(), integralizacao.getPercentualIntegralizacao());
            }
        }

    }



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
