<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:
openapi: 3.0.0
info:
  title: API de Busca de Estudante para Transferência FIES
  version: 1.0.0
  description: API para buscar informações de estudante e detalhes do contrato FIES para processo de transferência.
======================================================================respostas==================
Executando a classe BATCH .jar 
25/06 (08:00:00)
#--------------------------------------------------------------------#

,------.                 ,------.                                                   
|  .---'  ,---.   ,---.  |  .--. '  ,---.   ,---.   ,--,--.  ,---.   ,---.   ,---.  
|  `--,  | .-. : (  .-'  |  '--'.' | .-. : | .-. | ' ,-.  | (  .-'  (  .-'  | .-. : 
|  |`    \   --. .-'  `) |  |\  \  \   --. | '-' ' \ '-'  | .-'  `) .-'  `) \   --. 
`--'      `----' `----'  `--' '--'  `----' |  |-'   `--`--' `----'  `----'   `----'  
spring-boot  (v1.5.7.RELEASE)

2025-06-25 08:00:35.139  INFO 61044 --- [           main] br.gov.caixa.fes.Application             : Starting Application on cbrsvitrlx018.intra.caixa.gov.br with PID 61044 (/producao/ibmbcp/FESDB001/bin/fesRepasse/fesRepasse.jar started by sfesbp01 in /home/sfesbp01)
2025-06-25 08:00:35.267 DEBUG 61044 --- [           main] br.gov.caixa.fes.Application             : Running with Spring Boot v1.5.7.RELEASE, Spring v4.3.11.RELEASE
2025-06-25 08:00:35.267  INFO 61044 --- [           main] br.gov.caixa.fes.Application             : No active profile set, falling back to default profiles: default
2025-06-25 08:00:36.948  INFO 61044 --- [           main] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@46fbb2c1: startup date [Wed Jun 25 08:00:36 GMT-03:00 2025]; root of context hierarchy
2025-06-25 08:00:51.997  INFO 61044 --- [           main] o.s.b.f.s.DefaultListableBeanFactory     : Overriding bean definition for bean 'taskExecutor' with a different definition: replacing [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=apuraRepasse; factoryMethodName=taskExecutor; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [br/gov/caixa/fes/transacao/ApuraRepasse.class]] with [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=apuraRepasseTitulo; factoryMethodName=taskExecutor; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [br/gov/caixa/fes/transacao/ApuraRepasseTitulo.class]]
2025-06-25 08:00:51.998  INFO 61044 --- [           main] o.s.b.f.s.DefaultListableBeanFactory     : Overriding bean definition for bean 'taskExecutor' with a different definition: replacing [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=apuraRepasseTitulo; factoryMethodName=taskExecutor; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [br/gov/caixa/fes/transacao/ApuraRepasseTitulo.class]] with [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=calculaPercentualIntegralizacao; factoryMethodName=taskExecutor; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [br/gov/caixa/fes/transacao/CalculaPercentualIntegralizacao.class]]
2025-06-25 08:00:51.999  INFO 61044 --- [           main] o.s.b.f.s.DefaultListableBeanFactory     : Overriding bean definition for bean 'taskExecutor' with a different definition: replacing [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=calculaPercentualIntegralizacao; factoryMethodName=taskExecutor; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [br/gov/caixa/fes/transacao/CalculaPercentualIntegralizacao.class]] with [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=cancelamentoRepasse; factoryMethodName=taskExecutor; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [br/gov/caixa/fes/transacao/CancelamentoRepasse.class]]
2025-06-25 08:01:10.376 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : HikariCP pool SpringBootHikariCP configuration:
2025-06-25 08:01:10.415 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : allowPoolSuspension.............false
2025-06-25 08:01:10.416 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : autoCommit......................true
2025-06-25 08:01:10.416 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : catalog.........................
2025-06-25 08:01:10.416 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : connectionCustomizer............com.zaxxer.hikari.AbstractHikariConfig$1@5649fd9b
2025-06-25 08:01:10.417 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : connectionCustomizerClassName...
2025-06-25 08:01:10.417 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : connectionInitSql...............
2025-06-25 08:01:10.417 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : connectionTestQuery.............select count(1) from FES.FESTB711_RLTRO_CTRTO_ANLTO where rownum < 2
2025-06-25 08:01:10.418 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : connectionTimeout...............60000
2025-06-25 08:01:10.418 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : dataSource......................
2025-06-25 08:01:10.418 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : dataSourceClassName.............oracle.jdbc.pool.OracleDataSource
2025-06-25 08:01:10.418 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : dataSourceJNDI..................
2025-06-25 08:01:10.419 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : dataSourceProperties............{user=prdus_aplicacao, password=<masked>, url=jdbc:oracle:thin:@//10.120.82.82:1521/orap01ng}
2025-06-25 08:01:10.419 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : driverClassName.................
2025-06-25 08:01:10.419 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : healthCheckProperties...........{}
2025-06-25 08:01:10.420 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : healthCheckRegistry.............
2025-06-25 08:01:10.420 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : idleTimeout.....................60000
2025-06-25 08:01:10.420 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : initializationFailFast..........true
2025-06-25 08:01:10.421 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : isolateInternalQueries..........false
2025-06-25 08:01:10.421 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : jdbc4ConnectionTest.............false
2025-06-25 08:01:10.421 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : jdbcUrl.........................
2025-06-25 08:01:10.422 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : leakDetectionThreshold..........0
2025-06-25 08:01:10.422 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : maxLifetime.....................1800000
2025-06-25 08:01:10.422 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : maximumPoolSize.................3
2025-06-25 08:01:10.423 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : metricRegistry..................
2025-06-25 08:01:10.423 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : minimumIdle.....................3
2025-06-25 08:01:10.423 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : password........................<masked>
2025-06-25 08:01:10.423 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : poolName........................SpringBootHikariCP
2025-06-25 08:01:10.424 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : readOnly........................false
2025-06-25 08:01:10.424 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : registerMbeans..................false
2025-06-25 08:01:10.424 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : threadFactory...................
2025-06-25 08:01:10.424 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : transactionIsolation............
2025-06-25 08:01:10.425 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : username........................
2025-06-25 08:01:10.425 DEBUG 61044 --- [           main] com.zaxxer.hikari.HikariConfig           : validationTimeout...............5000
2025-06-25 08:01:10.429  INFO 61044 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariCP pool SpringBootHikariCP is starting.
2025-06-25 08:01:19.687  INFO 61044 --- [           main] j.LocalContainerEntityManagerFactoryBean : Building JPA container EntityManagerFactory for persistence unit 'default'
2025-06-25 08:01:20.130  INFO 61044 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [
	name: default
	...]
2025-06-25 08:01:24.213  INFO 61044 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate Core {5.0.12.Final}
2025-06-25 08:01:24.235  INFO 61044 --- [           main] org.hibernate.cfg.Environment            : HHH000206: hibernate.properties not found
2025-06-25 08:01:24.270  INFO 61044 --- [           main] org.hibernate.cfg.Environment            : HHH000021: Bytecode provider name : javassist
2025-06-25 08:01:26.368  INFO 61044 --- [           main] o.hibernate.annotations.common.Version   : HCANN000001: Hibernate Commons Annotations {5.0.1.Final}
2025-06-25 08:01:32.622  INFO 61044 --- [           main] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.dialect.Oracle9iDialect
2025-06-25 08:01:33.256  WARN 61044 --- [           main] o.h.e.j.e.i.JdbcEnvironmentInitiator     : HHH000341: Could not obtain connection metadata : Unsupported feature
2025-06-25 08:01:33.257  INFO 61044 --- [           main] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.dialect.Oracle9iDialect
2025-06-25 08:01:33.499  INFO 61044 --- [           main] o.h.e.j.e.i.LobCreatorBuilderImpl        : HHH000422: Disabling contextual LOB creation as connection was null
2025-06-25 08:01:40.055  WARN 61044 --- [           main] org.hibernate.orm.deprecation            : HHH90000014: Found use of deprecated [org.hibernate.id.SequenceHiLoGenerator] sequence-based id generator; use org.hibernate.id.enhanced.SequenceStyleGenerator instead.  See Hibernate Domain Model Mapping Guide for details.
2025-06-25 08:01:40.122  WARN 61044 --- [           main] org.hibernate.orm.deprecation            : HHH90000014: Found use of deprecated [org.hibernate.id.SequenceHiLoGenerator] sequence-based id generator; use org.hibernate.id.enhanced.SequenceStyleGenerator instead.  See Hibernate Domain Model Mapping Guide for details.
2025-06-25 08:01:40.123  WARN 61044 --- [           main] org.hibernate.orm.deprecation            : HHH90000014: Found use of deprecated [org.hibernate.id.SequenceHiLoGenerator] sequence-based id generator; use org.hibernate.id.enhanced.SequenceStyleGenerator instead.  See Hibernate Domain Model Mapping Guide for details.
2025-06-25 08:01:40.123  WARN 61044 --- [           main] org.hibernate.orm.deprecation            : HHH90000014: Found use of deprecated [org.hibernate.id.SequenceHiLoGenerator] sequence-based id generator; use org.hibernate.id.enhanced.SequenceStyleGenerator instead.  See Hibernate Domain Model Mapping Guide for details.
2025-06-25 08:01:40.123  WARN 61044 --- [           main] org.hibernate.orm.deprecation            : HHH90000014: Found use of deprecated [org.hibernate.id.SequenceHiLoGenerator] sequence-based id generator; use org.hibernate.id.enhanced.SequenceStyleGenerator instead.  See Hibernate Domain Model Mapping Guide for details.
2025-06-25 08:01:40.124  WARN 61044 --- [           main] org.hibernate.orm.deprecation            : HHH90000014: Found use of deprecated [org.hibernate.id.SequenceHiLoGenerator] sequence-based id generator; use org.hibernate.id.enhanced.SequenceStyleGenerator instead.  See Hibernate Domain Model Mapping Guide for details.
2025-06-25 08:01:40.124  WARN 61044 --- [           main] org.hibernate.orm.deprecation            : HHH90000014: Found use of deprecated [org.hibernate.id.SequenceHiLoGenerator] sequence-based id generator; use org.hibernate.id.enhanced.SequenceStyleGenerator instead.  See Hibernate Domain Model Mapping Guide for details.
2025-06-25 08:01:40.125  WARN 61044 --- [           main] org.hibernate.orm.deprecation            : HHH90000014: Found use of deprecated [org.hibernate.id.SequenceHiLoGenerator] sequence-based id generator; use org.hibernate.id.enhanced.SequenceStyleGenerator instead.  See Hibernate Domain Model Mapping Guide for details.
2025-06-25 08:01:40.125  WARN 61044 --- [           main] org.hibernate.orm.deprecation            : HHH90000014: Found use of deprecated [org.hibernate.id.SequenceHiLoGenerator] sequence-based id generator; use org.hibernate.id.enhanced.SequenceStyleGenerator instead.  See Hibernate Domain Model Mapping Guide for details.
2025-06-25 08:01:40.126  WARN 61044 --- [           main] org.hibernate.orm.deprecation            : HHH90000014: Found use of deprecated [org.hibernate.id.SequenceHiLoGenerator] sequence-based id generator; use org.hibernate.id.enhanced.SequenceStyleGenerator instead.  See Hibernate Domain Model Mapping Guide for details.
2025-06-25 08:01:41.624  WARN 61044 --- [           main] org.hibernate.mapping.RootClass          : HHH000038: Composite-id class does not override equals(): br.gov.caixa.fes.dominio.RelatorioArrecadacoSinteticoPK
2025-06-25 08:01:41.625  WARN 61044 --- [           main] org.hibernate.mapping.RootClass          : HHH000039: Composite-id class does not override hashCode(): br.gov.caixa.fes.dominio.RelatorioArrecadacoSinteticoPK
2025-06-25 08:01:41.626  WARN 61044 --- [           main] org.hibernate.mapping.RootClass          : HHH000038: Composite-id class does not override equals(): br.gov.caixa.fes.dominio.LogExecucaoTO
2025-06-25 08:01:41.626  WARN 61044 --- [           main] org.hibernate.mapping.RootClass          : HHH000039: Composite-id class does not override hashCode(): br.gov.caixa.fes.dominio.LogExecucaoTO
2025-06-25 08:01:41.627  WARN 61044 --- [           main] org.hibernate.mapping.RootClass          : HHH000038: Composite-id class does not override equals(): br.gov.caixa.fes.dominio.RelatorioArrecadacoAnaliticoPK
2025-06-25 08:01:41.628  WARN 61044 --- [           main] org.hibernate.mapping.RootClass          : HHH000039: Composite-id class does not override hashCode(): br.gov.caixa.fes.dominio.RelatorioArrecadacoAnaliticoPK
2025-06-25 08:01:41.628  WARN 61044 --- [           main] org.hibernate.mapping.RootClass          : HHH000038: Composite-id class does not override equals(): br.gov.caixa.fes.dominio.tributos.CronogramaFinanceiroTO
2025-06-25 08:01:41.630  WARN 61044 --- [           main] org.hibernate.mapping.RootClass          : HHH000039: Composite-id class does not override hashCode(): br.gov.caixa.fes.dominio.tributos.CronogramaFinanceiroTO
2025-06-25 08:01:49.107  INFO 61044 --- [           main] o.h.h.i.QueryTranslatorFactoryInitiator  : HHH000397: Using ASTQueryTranslatorFactory
2025-06-25 08:01:51.720  WARN 61044 --- [           main] o.h.dialect.function.TemplateRenderer    : HHH000174: Function template anticipated 4 arguments, but 1 arguments encountered
2025-06-25 08:01:53.889  INFO 61044 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2025-06-25 08:01:55.007 DEBUG 61044 --- [           main] b.gov.caixa.fes.transacao.GerenciarLote  : inciando atualizacao...
2025-06-25 08:01:59.048 DEBUG 61044 --- [           main] b.g.c.f.negocio.lote.GerenciarLoteImpl   : encontrados 399 lotes para atualizar
2025-06-25 08:01:59.382 DEBUG 61044 --- [           main] b.gov.caixa.fes.transacao.GerenciarLote  : atualizacao finalizada
2025-06-25 08:01:59.388 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.TrocaMantencaImpl  : Inicio execução rotina troca mantença Wed Jun 25 08:01:59 GMT-03:00 2025
2025-06-25 08:01:59.389 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.TrocaMantencaImpl  : Execução incluiRetencaoTrocaMantenca Wed Jun 25 08:01:59 GMT-03:00 2025
2025-06-25 08:02:05.045 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.TrocaMantencaImpl  : Execução incluiRetencaoTrocaMantenca finalizada Wed Jun 25 08:02:05 GMT-03:00 2025
2025-06-25 08:02:05.046 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.TrocaMantencaImpl  : Execução atualizaLiberacaoTrocaMantenca Wed Jun 25 08:02:05 GMT-03:00 2025
2025-06-25 08:02:34.518 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.TrocaMantencaImpl  : Execução atualizaLiberacaoTrocaMantenca finalizada Wed Jun 25 08:02:34 GMT-03:00 2025
2025-06-25 08:02:34.519 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.TrocaMantencaImpl  : Execução removeRetencaoTrocaMantenca Wed Jun 25 08:02:34 GMT-03:00 2025
2025-06-25 08:02:35.358 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.TrocaMantencaImpl  : Execução removeRetencaoTrocaMantenca finalizada Wed Jun 25 08:02:35 GMT-03:00 2025
2025-06-25 08:02:35.358 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.TrocaMantencaImpl  : Fim execução rotina troca mantença Wed Jun 25 08:02:35 GMT-03:00 2025
2025-06-25 08:02:35.367 DEBUG 61044 --- [           main] b.g.c.fes.negocio.ValidarLiberacaoImpl   : Inicio execução rotina ValidarLiberacaoImpl Wed Jun 25 08:02:35 GMT-03:00 2025
2025-06-25 08:02:35.368 DEBUG 61044 --- [           main] b.g.c.fes.negocio.ValidarLiberacaoImpl   : Cria lib aditamento...
2025-06-25 08:02:55.377 DEBUG 61044 --- [           main] b.g.c.fes.negocio.ValidarLiberacaoImpl   : Cria lib contrato...
2025-06-25 08:03:26.342 DEBUG 61044 --- [           main] b.g.c.fes.negocio.ValidarLiberacaoImpl   : Fim execução rotina ValidarLiberacaoImpl Wed Jun 25 08:03:26 GMT-03:00 2025
2025-06-25 08:03:28.362  INFO 61044 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2025-06-25 08:03:28.455 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.AgendaServiceImpl  : verificando agenda para operacoes de APURACAO_REPASSE
2025-06-25 08:03:28.861 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.AgendaServiceImpl  : verificando agenda para operacoes de REPASSE_TITULO
2025-06-25 08:03:28.865  WARN 61044 --- [           main] b.g.caixa.fes.negocio.AgendaServiceImpl  : Nenhuma agenda encontrada para a operacao REPASSE_TITULO
2025-06-25 08:03:28.867 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.AgendaServiceImpl  : verificando agenda para operacoes de CALCULAR_INTEGRALIZACAO_MANTENEDORAS
2025-06-25 08:03:28.871  WARN 61044 --- [           main] b.g.caixa.fes.negocio.AgendaServiceImpl  : Nenhuma agenda encontrada para a operacao CALCULAR_INTEGRALIZACAO_MANTENEDORAS
2025-06-25 08:03:28.873 DEBUG 61044 --- [           main] b.g.caixa.fes.negocio.AgendaServiceImpl  : verificando agenda para operacoes de CANCELAMENTO_REPASSE
2025-06-25 08:03:28.876  WARN 61044 --- [           main] b.g.caixa.fes.negocio.AgendaServiceImpl  : Nenhuma agenda encontrada para a operacao CANCELAMENTO_REPASSE
2025-06-25 08:03:29.001 DEBUG 61044 --- [           main] br.gov.caixa.fes.transacao.ApuraRepasse  : APURACAO EM MOEDA CORRENTE AGENDADO PARA Wed Jun 25 09:00:00 GMT-03:00 2025
2025-06-25 08:03:29.105  INFO 61044 --- [           main] br.gov.caixa.fes.Application             : Started Application in 188.806 seconds (JVM running for 208.338)
2025-06-25 09:00:00.002 DEBUG 61044 --- [pool-1-thread-1] br.gov.caixa.fes.transacao.ApuraRepasse  : iniciando geracao de arquivo com os dados do repasse atual
2025-06-25 09:00:00.011 DEBUG 61044 --- [pool-1-thread-1] b.g.c.fes.negocio.ApuracaoServiceImpl    : iniciando geracao de arquivo 
2025-06-25 09:00:00.012 DEBUG 61044 --- [pool-1-thread-1] b.g.c.fes.negocio.ApuracaoServiceImpl    : data liberacao Sun Jun 15 00:00:00 GMT-03:00 2025
2025-06-25 09:00:22.186 DEBUG 61044 --- [pool-1-thread-1] b.g.c.fes.negocio.ApuracaoServiceImpl    : encontrados 162578 contratos
2025-06-25 09:00:23.039 DEBUG 61044 --- [pool-1-thread-1] b.g.c.fes.negocio.ApuracaoServiceImpl    : file path /home/sfesbp01/ibmbcp/FESDB001/bin/fesRepasse/relatorioRepasse_6_2025.csv
2025-06-25 09:00:23.367 DEBUG 61044 --- [pool-1-thread-1] br.gov.caixa.fes.transacao.ApuraRepasse  : arquivo gerado!
2025-06-25 09:00:23.371 DEBUG 61044 --- [pool-1-thread-1] br.gov.caixa.fes.util.Utils              : Data Apuracao Fri Jun 20 00:00:00 GMT-03:00 2025
2025-06-25 09:00:23.372 DEBUG 61044 --- [pool-1-thread-1] br.gov.caixa.fes.util.Utils              : Data Liberacao Sat Jun 14 00:00:00 GMT-03:00 2025
2025-06-25 09:00:24.289 DEBUG 61044 --- [pool-1-thread-1] b.g.caixa.fes.negocio.RepasseMoedaImpl   : Iniciando consulta de registro existente na 900
2025-06-25 09:00:24.847 DEBUG 61044 --- [pool-1-thread-1] b.g.caixa.fes.negocio.RepasseMoedaImpl   : Fim de consulta de registro existente na 900
2025-06-25 09:00:24.848 DEBUG 61044 --- [pool-1-thread-1] b.g.caixa.fes.negocio.RepasseMoedaImpl   : Iniciando gravacao na 900 para o exercicio atual
2025-06-25 09:00:25.166  INFO 61044 --- [pool-1-thread-1] o.h.e.j.b.internal.AbstractBatchImpl     : HHH000010: On release of batch it still contained JDBC statements
2025-06-25 09:00:25.254 ERROR 61044 --- [pool-1-thread-1] br.gov.caixa.fes.transacao.ApuraRepasse  : erro inesperado!

org.springframework.orm.jpa.JpaSystemException: Batch update returned unexpected row count from update [0]; actual row count: 49; expected: 1; nested exception is org.hibernate.jdbc.BatchedTooManyRowsAffectedException: Batch update returned unexpected row count from update [0]; actual row count: 49; expected: 1
	at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:333) ~[spring-orm-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.orm.jpa.vendor.HibernateJpaDialect.translateExceptionIfPossible(HibernateJpaDialect.java:244) ~[spring-orm-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.translateExceptionIfPossible(AbstractEntityManagerFactoryBean.java:488) ~[spring-orm-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.dao.support.ChainedPersistenceExceptionTranslator.translateExceptionIfPossible(ChainedPersistenceExceptionTranslator.java:59) ~[spring-tx-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.dao.support.DataAccessUtils.translateIfNecessary(DataAccessUtils.java:213) ~[spring-tx-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:147) ~[spring-tx-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.transaction.interceptor.TransactionInterceptor$1.proceedWithInvocation(TransactionInterceptor.java:99) ~[spring-tx-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:282) ~[spring-tx-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:96) ~[spring-tx-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:673) ~[spring-aop-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at br.gov.caixa.fes.negocio.RepasseMoedaImpl$$EnhancerBySpringCGLIB$$1bfdb3e4.inserirPcIntegMantenedoras(<generated>) ~[classes!/:na]
	at br.gov.caixa.fes.transacao.ApuraRepasse.executaRotina(ApuraRepasse.java:80) ~[classes!/:na]
	at br.gov.caixa.fes.transacao.ApuraRepasse$1.run(ApuraRepasse.java:115) [classes!/:na]
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) [spring-context-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.scheduling.concurrent.ReschedulingRunnable.run(ReschedulingRunnable.java:81) [spring-context-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [na:1.8.0_271]
	at java.util.concurrent.FutureTask.run(FutureTask.java:266) [na:1.8.0_271]
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$201(ScheduledThreadPoolExecutor.java:180) [na:1.8.0_271]
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:293) [na:1.8.0_271]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149) [na:1.8.0_271]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624) [na:1.8.0_271]
	at java.lang.Thread.run(Thread.java:748) [na:1.8.0_271]
Caused by: org.hibernate.jdbc.BatchedTooManyRowsAffectedException: Batch update returned unexpected row count from update [0]; actual row count: 49; expected: 1
	at org.hibernate.jdbc.Expectations$BasicExpectation.checkBatched(Expectations.java:77) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.jdbc.Expectations$BasicExpectation.verifyOutcome(Expectations.java:54) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.engine.jdbc.batch.internal.NonBatchingBatch.addToBatch(NonBatchingBatch.java:46) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.persister.entity.AbstractEntityPersister.delete(AbstractEntityPersister.java:3261) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.persister.entity.AbstractEntityPersister.delete(AbstractEntityPersister.java:3498) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.action.internal.EntityDeleteAction.execute(EntityDeleteAction.java:98) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.engine.spi.ActionQueue.executeActions(ActionQueue.java:582) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.engine.spi.ActionQueue.executeActions(ActionQueue.java:456) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.event.internal.AbstractFlushingEventListener.performExecutions(AbstractFlushingEventListener.java:337) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.event.internal.DefaultFlushEventListener.onFlush(DefaultFlushEventListener.java:39) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.internal.SessionImpl.flush(SessionImpl.java:1282) ~[hibernate-core-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.jpa.spi.AbstractEntityManagerImpl.flush(AbstractEntityManagerImpl.java:1300) ~[hibernate-entitymanager-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.jpa.internal.QueryImpl.beforeQuery(QueryImpl.java:518) ~[hibernate-entitymanager-5.0.12.Final.jar!/:5.0.12.Final]
	at org.hibernate.jpa.internal.QueryImpl.getResultList(QueryImpl.java:481) ~[hibernate-entitymanager-5.0.12.Final.jar!/:5.0.12.Final]
	at br.gov.caixa.fes.negocio.RepasseMoedaImpl.inserirPcIntegMantenedoras(RepasseMoedaImpl.java:306) ~[classes!/:na]
	at br.gov.caixa.fes.negocio.RepasseMoedaImpl$$FastClassBySpringCGLIB$$e7770d75.invoke(<generated>) ~[classes!/:na]
	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:204) ~[spring-core-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:738) ~[spring-aop-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:157) ~[spring-aop-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:136) ~[spring-tx-4.3.11.RELEASE.jar!/:4.3.11.RELEASE]
	... 18 common frames omitted

2025-06-25 09:00:25.256  INFO 61044 --- [       Thread-8] s.c.a.AnnotationConfigApplicationContext : Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@46fbb2c1: startup date [Wed Jun 25 08:00:36 GMT-03:00 2025]; root of context hierarchy
2025-06-25 09:00:25.259  INFO 61044 --- [       Thread-8] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans on shutdown
2025-06-25 09:00:25.260  INFO 61044 --- [       Thread-8] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2025-06-25 09:00:25.261  INFO 61044 --- [       Thread-8] HikariPool                               : HikariCP pool SpringBootHikariCP is shutting down.
#-************************************************************-#

ERRO na execucao da classe JAVA jar !

Favor entrar capturar este log e enviar para a CEDES !

#-************************************************************-#
==================================================================================================
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
