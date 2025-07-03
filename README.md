<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:
=========================================================================================================
DECLARE
    -- Dados de entrada (o único "parâmetro" inicial fixo por enquanto)
    v_nu_sqncl_liberacao_contrato NUMBER := 141622;

    -- Variáveis para armazenar os dados lidos da FESTB712
    v_nu_seq_candidato_lido      FES.FESTB712_LIBERACAO_CONTRATO.NU_SEQ_CANDIDATO%TYPE;
    v_nu_ies_lido                FES.FESTB712_LIBERACAO_CONTRATO.NU_IES%TYPE;
    v_nu_campus_lido             FES.FESTB712_LIBERACAO_CONTRATO.NU_CAMPUS%TYPE;
    v_mm_referencia_lib_lido     FES.FESTB712_LIBERACAO_CONTRATO.MM_REFERENCIA_LIBERACAO%TYPE;
    v_aa_referencia_lib_lido     FES.FESTB712_LIBERACAO_CONTRATO.AA_REFERENCIA_LIBERACAO%TYPE;
    v_vr_repasse_lib_lido        FES.FESTB712_LIBERACAO_CONTRATO.VR_REPASSE%TYPE;

    -- Variáveis para a lógica de busca e existência
    v_nu_sqncl_rl_analitico_encontrado NUMBER;
    v_existe_na_812 NUMBER;

    -- Usaremos o NU_SQNCL_COMPENSACAO_REPASSE de teste.
    v_nu_sqncl_compensacao_repasse NUMBER := 68;

    -- Usuário de execução e tipo de acerto para a inserção
    v_co_usuario_execucao VARCHAR2(8) := 'RdfoSp67';
    v_nu_tipo_acerto_compensacao CONSTANT NUMBER := 1;

BEGIN
    DBMS_OUTPUT.PUT_LINE('--- Início da Evolução da Lógica: Lendo da FESTB712 ---');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Parâmetro inicial: NU_SQNCL_LIBERACAO_CONTRATO = ' || v_nu_sqncl_liberacao_contrato);
    DBMS_OUTPUT.PUT_LINE('----------------------------------------------------');

    -- PASSO 0: Buscar os dados da liberação na FESTB712
    DBMS_OUTPUT.PUT_LINE(CHR(10) || '>> PASSO 0: Buscando detalhes da liberação ' || v_nu_sqncl_liberacao_contrato || ' na FESTB712...');
    BEGIN
        SELECT NU_SEQ_CANDIDATO, NU_IES, NU_CAMPUS, MM_REFERENCIA_LIBERACAO, AA_REFERENCIA_LIBERACAO, VR_REPASSE
        INTO v_nu_seq_candidato_lido, v_nu_ies_lido, v_nu_campus_lido, v_mm_referencia_lib_lido, v_aa_referencia_lib_lido, v_vr_repasse_lib_lido
        FROM FES.FESTB712_LIBERACAO_CONTRATO
        WHERE NU_SQNCL_LIBERACAO_CONTRATO = v_nu_sqncl_liberacao_contrato;

        DBMS_OUTPUT.PUT_LINE('   RESULTADO PASSO 0: SUCESSO!');
        DBMS_OUTPUT.PUT_LINE('     -> Dados da FESTB712 lidos:');
        DBMS_OUTPUT.PUT_LINE('        Candidato: ' || v_nu_seq_candidato_lido);
        DBMS_OUTPUT.PUT_LINE('        IES: ' || v_nu_ies_lido);
        DBMS_OUTPUT.PUT_LINE('        Campus: ' || v_nu_campus_lido);
        DBMS_OUTPUT.PUT_LINE('        MM_REFERENCIA_LIBERACAO: ' || v_mm_referencia_lib_lido);
        DBMS_OUTPUT.PUT_LINE('        AA_REFERENCIA_LIBERACAO: ' || v_aa_referencia_lib_lido);
        DBMS_OUTPUT.PUT_LINE('        VR_REPASSE: ' || v_vr_repasse_lib_lido);

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            DBMS_OUTPUT.PUT_LINE('   RESULTADO PASSO 0: ERRO (NO_DATA_FOUND)!');
            DBMS_OUTPUT.PUT_LINE('     -> Liberação ' || v_nu_sqncl_liberacao_contrato || ' NÃO encontrada na FESTB712. Impossível prosseguir.');
            GOTO end_logic;
        WHEN TOO_MANY_ROWS THEN
            DBMS_OUTPUT.PUT_LINE('   RESULTADO PASSO 0: ERRO (TOO_MANY_ROWS)!');
            DBMS_OUTPUT.PUT_LINE('     -> Múltiplas liberações ' || v_nu_sqncl_liberacao_contrato || ' encontradas na FESTB712. Esperado apenas uma. Impossível prosseguir.');
            GOTO end_logic;
    END;

    -- PASSO 1: Tentar encontrar o NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB711
    DBMS_OUTPUT.PUT_LINE(CHR(10) || '>> PASSO 1: Buscando o NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB711 usando dados lidos da FESTB712...');
    BEGIN
        SELECT T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO
        INTO v_nu_sqncl_rl_analitico_encontrado
        FROM FES.FESTB711_RLTRO_CTRTO_ANLTO T711
        WHERE T711.NU_SQNCL_LIBERACAO_CONTRATO = v_nu_sqncl_liberacao_contrato
        AND T711.NU_SEQ_CANDIDATO = v_nu_seq_candidato_lido    -- Usando variável lida da 712
        AND T711.NU_IES = v_nu_ies_lido                      -- Usando variável lida da 712
        AND T711.NU_CAMPUS = v_nu_campus_lido                -- Usando variável lida da 712
        AND T711.MM_REFERENCIA = v_mm_referencia_lib_lido    -- Usando variável lida da 712
        AND T711.AA_REFERENCIA = v_aa_referencia_lib_lido    -- Usando variável lida da 712
        AND T711.VR_REPASSE = v_vr_repasse_lib_lido          -- Usando variável lida da 712
        ORDER BY T711.TS_APURACAO_RELATORIO DESC
        FETCH FIRST 1 ROW ONLY;

        DBMS_OUTPUT.PUT_LINE('   RESULTADO PASSO 1: SUCESSO!');
        DBMS_OUTPUT.PUT_LINE('     -> NU_SQNCL_RLTRO_CTRTO_ANALITICO encontrado na FESTB711: ' || v_nu_sqncl_rl_analitico_encontrado);

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_nu_sqncl_rl_analitico_encontrado := NULL;
            DBMS_OUTPUT.PUT_LINE('   RESULTADO PASSO 1: ALERTA (NO_DATA_FOUND)!');
            DBMS_OUTPUT.PUT_LINE('     -> Não foi encontrado um NU_SQNCL_RLTRO_CTRTO_ANALITICO correspondente na FESTB711 para os dados da liberação lidos da FESTB712.');
            DBMS_OUTPUT.PUT_LINE('     -> A INSERÇÃO na FESTB812 NÃO será executada neste caso.');
            GOTO end_logic;
        WHEN TOO_MANY_ROWS THEN
            DBMS_OUTPUT.PUT_LINE('   RESULTADO PASSO 1: AVISO (TOO_MANY_ROWS)!');
            DBMS_OUTPUT.PUT_LINE('     -> Múltiplos NU_SQNCL_RLTRO_CTRTO_ANALITICO encontrados na FESTB711 para os critérios exatos.');
            DBMS_OUTPUT.PUT_LINE('     -> O script selecionou o mais recente (ORDER BY TS_APURACAO_RELATORIO DESC).');
    END;

    -- PASSO 2: Se o analítico foi encontrado, verificar sua existência na FESTB812
    IF v_nu_sqncl_rl_analitico_encontrado IS NOT NULL THEN
        DBMS_OUTPUT.PUT_LINE(CHR(10) || '>> PASSO 2: Verificando a existência do analítico (' || v_nu_sqncl_rl_analitico_encontrado || ') na FESTB812...');
        SELECT COUNT(1)
        INTO v_existe_na_812
        FROM FES.FESTB812_CMPSO_RPSE_INDVO
        WHERE NU_SQNCL_RLTRO_CTRTO_ANALITICO = v_nu_sqncl_rl_analitico_encontrado
          AND NU_SQNCL_COMPENSACAO_REPASSE = v_nu_sqncl_compensacao_repasse;

        DBMS_OUTPUT.PUT_LINE('   RESULTADO PASSO 2: Checagem concluída.');
        DBMS_OUTPUT.PUT_LINE('     -> Quantidade de registros existentes na FESTB812 para (Analítico: ' || v_nu_sqncl_rl_analitico_encontrado || ', Seq. Comp.: ' || v_nu_sqncl_compensacao_repasse || '): ' || v_existe_na_812);

        IF v_existe_na_812 = 0 THEN
            DBMS_OUTPUT.PUT_LINE('   DECISÃO: O registro (Analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' e Sequencial ' || v_nu_sqncl_compensacao_repasse || ') NÃO existe na FESTB812.');
            DBMS_OUTPUT.PUT_LINE('     -> **Realizando a INSERÇÃO na FESTB812 agora...**');

            BEGIN
                INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (
                    NU_SQNCL_COMPENSACAO_REPASSE,
                    NU_SQNCL_RLTRO_CTRTO_ANALITICO,
                    NU_TIPO_ACERTO,
                    TS_INCLUSAO,
                    CO_USUARIO_INCLUSAO,
                    IC_COMPENSADO
                ) VALUES (
                    v_nu_sqncl_compensacao_repasse,
                    v_nu_sqncl_rl_analitico_encontrado,
                    v_nu_tipo_acerto_compensacao,
                    SYSTIMESTAMP,
                    v_co_usuario_execucao,
                    'N'
                );
                DBMS_OUTPUT.PUT_LINE('   SUCESSO: Inserido analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' na FESTB812 com NU_SQNCL_COMPENSACAO_REPASSE = ' || v_nu_sqncl_compensacao_repasse || '.');
            EXCEPTION
                WHEN DUP_VAL_ON_INDEX THEN
                    DBMS_OUTPUT.PUT_LINE('   ERRO (DUP_VAL_ON_INDEX): Falha na inserção para analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' e sequencial ' || v_nu_sqncl_compensacao_repasse || '. Registro já existe (possível concorrência).');
                WHEN OTHERS THEN
                    DBMS_OUTPUT.PUT_LINE('   ERRO INESPERADO NA INSERÇÃO: Falha ao inserir analítico ' || v_nu_sqncl_rl_analitico_encontrado || ': ' || SQLERRM);
                    RAISE;
            END;

        ELSE
            DBMS_OUTPUT.PUT_LINE('   DECISÃO: O registro (Analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' e Sequencial ' || v_nu_sqncl_compensacao_repasse || ') JÁ existe na FESTB812.');
            DBMS_OUTPUT.PUT_LINE('     -> **A INSERÇÃO NÃO será realizada para evitar duplicidade.**');
        END IF;
    ELSE
        DBMS_OUTPUT.PUT_LINE(CHR(10) || '>> PASSO 2: Não executado. O analítico não foi encontrado no Passo 1.');
    END IF;

    <<end_logic>>
    COMMIT;
    DBMS_OUTPUT.PUT_LINE(CHR(10) || '--- Fim do Exercício. Transação COMITADA. ---');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE(CHR(10) || 'ERRO FATAL GERAL (Transação ROLLED BACK): ' || SQLERRM);
END;

===========================================================================================================
caminho :https://editor.swagger.io/

Sobre swagger , e Api , eles podem ficar separados por negocio e agrupados no nosso Api manager
de forma que um Aditamento pode conter
esta dentro da pasta negocio Aditamnetos :
 tecnologias que trabalhei na Caixa :
 Javaweb e App
 Front:
 Html ,javascripte, css
 Java:
 Sprint
 springBatch
 servidor:
 wiflay 
 control-m
 servidor linux
 ======
 Azure devops
 scripts deexecução
 chamados :
 ----------------------------------------------------configuração de ambientes ==================================
 ---------------PRIMEIRA EXECUÇAO: / TROCA DE SENHA DE REDE

- baixar ambiente dividido em 4+ partes .7z (~1.8GB por parte, sendo a ultima parte menor)
- remover tudo que estiver na pasta C:\AMBIENTE_SIFES_GW_2023\
- extrair os arquivos com o .7z na pasta C:\AMBIENTE_SIFES_GW_2023\
- entrar na pasta repositori_GIT
- logar na VPN
- abrir powershell / git bash
- digitar comandos:
git init
git remote add devops https://devops.caixa/projetos/caixa/_git/sifes
git config --global http.sslverify false
git reset --hard
git fetch devops
git pull
git checkout release/master
git pull
git reset --hard

------ QUANDO TROCA DE SENHA DE REDE:

- entrar na pasta -> C:\AMBIENTE_SIFES_GW_2023\JBOSS-EAP-7.1\STANDALONE\CONFIGURATION
	ABRIR OS ARQUIVOS STANDALONE-FULL_INTRANET.XML E STANDALONE-FULL_INTERNET.XML
			ALTERAR LOGIN E SENHA NAS LINHAS 213 E 214 DO ARQUIVO STANDALONE-FULL_INTRANET.XML
			ALTERAR LOGIN E SENHA NAS LINHAS 317 E 318 DO ARQUIVO STANDALONE-FULL_INTERNET.XML
			ALTERAR LOGIN E SENHA NAS LINHAS 91 E 92 DO ARQUIVO STANDALONE.XML




---------------CASO NECESSARIO - INCLUSIVE SE OCORRER ERROS NA TROCA DE BRANCH:

- abrir o eclipse
------ remover o projeto do eclipse (sem deletar nas pastas)
- fechar o eclipse
------ deletar tudo que estiver na pasta C:\AMBIENTE_SIFES_GW_2023\repositorio_GIT
- executar os comandos a seguir

git reset --hard
git pull
git checkout release/master (ou sua branch criada)

- abrir o eclipse
- esperar tudo finalizar (aba progress)
- importar projeto maven
- escolher o diretório C:\AMBIENTE_SIFES_GW_2023\repositorio_GIT
- selecionar TODAS as pastas do projeto
- botao direito -> refresh
- aguardar finalizar tudo (visão melhor no Progress, do lado do Console)
- Project -> clean -> tudo marcado -> Clean
- botão verde do play abaixo do menu Help -> flecha lateral -> clicar em 1 clean install
- aguardar finalizar tudo (visão melhor no Progress, do lado do Console)
- Project -> clean -> tudo marcado -> Clean




---------------CONFIGURANDO AMBIENTE INTERNET ou INTRANET

- abra eclipse
- esperar tudo finalizar (aba progress)
- clicar com o botão direito no SERVER (aba Servers)
--- clicar no botão Add and Remove...
---- remover tudo que estiver de servidor
- clicar com o botao direito no SERVER -> CLEAN
- selecionar TODAS as pastas do projeto
- botao direito -> refresh
- aguardar finalizar tudo (visão melhor no Progress, do lado do Console)
- Project -> clean -> tudo marcado -> Clean
- clicar 2x no SERVER
- clicar no botão Runtime Environment
- clicar no botão "Browse..." onde estiver escrito standalone-full_INTRANET.xml / standalone-full_INTERNET.xml
- selecionar arquivo standalone-full_(...).xml desejado
- clicar no botão Finish
- salvar o SERVER
- fechar a aba SERVER
- clicar com o botão direito no SERVER -> CLEAN
- clicar com o botão direito no SERVER (aba Servers)
--- clicar no botão Add and Remove...
---- adicionar o projeto no servidor
- botao direito -> refresh
- aguardar finalizar tudo (visão melhor no Progress, do lado do Console)
- Project -> clean -> tudo marcado -> Clean
- botão verde do play abaixo do menu Help -> flecha lateral -> clicar em 1 clean install
- aguardar finalizar tudo (visão melhor no Progress, do lado do Console)
- Project -> clean -> tudo marcado -> Clean
- clicar com o botao direito no SERVER -> CLEAN
 
 ----------------------------------------------------------------------------------------------------------------------
 Dilatação:
 --------------
 1-Preciso evoluir a geração dele automaticamente atraves de anotações 
 2-A escrita do Rest e todo o processo---
    Rest---->service-----Bean----dto------> Banco
 
==============================================Modelo de Swagger==========================================================
openapi: 3.0.1
info:
  version: 1.0.0
  title: API Aditamento de Transferência - SIFES
  description: >-
    ## *Orientações*

    API utilizada para permitir aos estudantes inscritos no programa de
    financiamento estudantil FIES realizarem a transferência de IES ou Curso junto ao sistema .


    Para cada um dos paths desta API, além dos escopos (`scopes`) indicados
    existem (`permissions`) que deverão ser observadas:


    ### `/personal/identifications`
      - permissions:
        - GET: **CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ**
    ### `/personal/qualifications`
      - permissions: **CUSTOMERS_PERSONAL_ADITTIONALINFO_READ**
    ### `/personal/financial-relations`


    ### `- API Segurança Nível III`


    `- Timeout no API Manager:` **3 segundos**


    `- Timeout no Middleware` **_____ milissegundos**


    `- Timeout no Backend:`** 865 milissegundos**


    `- Equipe de Desenvolvimento Responsável: `**CESOB220**


    `- Equipe Gestora Negocial (Dono do Produto): `**GEFET**


    `- Nº do RTC de Validação do Swagger: `**22663166**
     
  contact:
    name: Equipe de Desenvolvimento (cesob220@caixa.gov.br)
    email: sudeXXX@caixa.gov.br
servers:
  - url: https://api.des.caixa:8446/financiamentoestudantil/aditamentotransferencia/
    description: ''
    
paths:
  '/v1/detalhar-dados-estudante/{cpf}': 
    description: 'Aditamento de Transferência'
    get:
      tags:
        - Aditamento de Transferência
      summary: >- 
       Esse serviço deve permitir consultar as informações relacionadas ao estudante.
      description: Esse serviço deve permitir consultar as informações relacionadas ao estudante.
      operationId: detalhar-dados-estudante
      parameters:
        - name: cpf
          in: path
          required: true
          schema:
            type: string
          example: "03392645001"
          description: CPF do estudante
      responses:
        '200':
          description: Informações do estudante retornadas com sucesso.
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
                    example: 1
                  tipo:
                    type: string
                    example: null
                  habilitarCancelarEstudante:
                    type: boolean
                    example: false
                  tipoDesc:
                    type: string
                    example: "IES"
                  statusDesc:
                    type: string
                    example: "Cancelado"
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
                      type: object
                      properties:
                        idTransferencia:
                          type: string
                          example: null
                        dataSolicitacao:
                          type: integer
                          example: 1747228434000
                        tipoTransferencia:
                          type: integer
                          example: 2
                        status:
                          type: integer
                          example: 10  
                        mensagem:
                          type: string
                          nullable: true
                          example: ""
                        codigo:
                          type: string
                          nullable: true
                          example: null
                        tipo:
                          type: string
                          nullable: true
                          example: null
                        editavel:
                          nullable: true
                          example: null
                        habilitarCancelarEstudante:
                          type: boolean
                          example: false
                        tipoDesc:
                          type: string
                          example: "IES"
                        statusDesc:
                          type: string
                          example: "Cancelado"
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
              
                
        '401':
          description: 
            Identificação provida pelo token aponta para usuário não autorizado
            a utilizar a API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 401
                    mensagem: O token fornecido para acesso à API é inválido.
                    tipo: Erro
                    editavel: false
        '404':
          description: Página Não Encontrada.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: 
                      Página Não Encontrada.
                    tipo: Erro
                    editavel: false
        '412':
          description: Erro negocial ou estrutural na chamada da API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 412
                    mensagem: Os dados fornecidos não são válidos.
                    tipo: Erro
                    editavel: false
        '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 500
                    mensagem: Erro na execução da funcionalidade no backend.
                    tipo: Erro
                    editavel: false
      security:
        - Internet:
            - scope
          APIKey: []
  '/v1/detalhar-contrato/{cpf}': 
    get:
      tags:
        - Aditamento de Transferência
      summary: >- 
       Esse serviço deve permitir consultar as informações do contrato do estudante.
      description: Esse serviço deve permitir consultar as informações do contrato do estudante.
      operationId: detalhar-contrato
      parameters:
        - name: cpf
          in: path
          required: true
          schema:
            type: string
          example: "03392645001"
          description: CPF do estudante
      responses:
        '200':
          description: Informações do estudante retornadas com sucesso.
          content:
            application/json:
              schema:
                type: object
                properties:
                  mensagem:
                    type: string
                    description: Mensagem informativa (geralmente vazia em caso de sucesso).
                    nullable: true
                    example: ""
                  codigo:
                    type: string
                    description: Código de retorno (geralmente nulo em caso de sucesso).
                    nullable: true
                    example: null
                  tipo:
                    type: string
                    description: Tipo da mensagem (geralmente nulo em caso de sucesso).
                    nullable: true
                    example: null
                  editavel:
                    type: boolean
                    description: Indica se os dados são editáveis.
                    nullable: true
                    example: null
                  agencia:
                    type: integer
                    description: Código da agência bancária do contrato.
                    example: 4736

                  estudante:
                    type: object
                    items:
                      type: object
                      properties:
                        mensagem:
                          type: string
                          nullable: true
                          example: ""
                        codigo:
                          type: string
                          nullable: true
                          example: null
                        tipo:
                          type: string
                          nullable: true
                          example: null
                        editavel:
                          nullable: true
                          example: null
                        cpf:
                          type: string
                          description: CPF do estudante.
                          example: "70966798120"
                        dependenteCPF:
                          type: integer
                          description: CPF do dependente (se houver).
                          example: 0
                        nome:
                          type: string
                          description: Nome completo do estudante.
                          example: "LUANA GARCIA FERREIRA"
                        dataNascimento:
                          type: string
                          format: date
                          description: Data de nascimento do estudante (DD/MM/AAAA).
                          example: "20/02/2002"
                        ric:
                          type: string
                          nullable: true
                          description: Registro de Identidade Civil (RIC).
                          example: null
                        nacionalidade:
                          type: string
                          nullable: true
                          description: Nacionalidade do estudante.
                          example: null  
                        identidade:
                          type: object
                          items:
                            type: object
                            properties: 
                              identidade:
                                type: string
                                description: Número da identidade.
                                example: "4116034"
                              orgaoExpedidor:
                                type: object
                                properties:
                                  codigo:
                                    type: integer
                                    example: 10
                                  nome:
                                    type: string
                                    example: "Secretaria de Segurança Pública(SSP)"
                                  uf:
                                    type: object
                                    properties:
                                      mensagem:
                                        type: string
                                        nullable: true
                                        example: ""
                                      codigo:
                                        type: string
                                        nullable: true
                                        example: null
                                      tipo:
                                        type: string
                                        nullable: true
                                        example: null
                                      editavel:
                                        type: boolean
                                        nullable: true
                                        example: null
                                      sigla:
                                        type: string
                                        example: "GO"
                                      descricao:
                                        type: string
                                        example: ""
                                      regiao:
                                        type: string
                                        nullable: true
                                        example: null
                                  dataExpedicaoIdentidade:
                                    type: string
                                    format: date
                                    example: "16/03/2017"
                        estadoCivil:
                          type: object
                          properties:
                            codigo:
                              type: integer
                              example: 1
                            nome:
                              type: string
                              example: "Solteiro(a)"
                            possuiConjuge:
                              type: boolean
                              example: false
                        regimeBens:
                          type: string
                          nullable: true
                          example: null
                        endereco:      
                          type: object
                          properties:
                            endereco:
                              type: string
                              example: "Rua 02qd B LT 03 00"
                            numero:
                              type: string
                              nullable: true
                              example: null 
                            bairro:
                              type: string
                              example: "boa vista"
                            cep:
                              type: string
                              example: "75620000"
                            cidade:
                              type: object
                              properties:
                                codigoCidade:
                                  type: integer
                                  example: 1770
                                nome:
                                  type: string
                                  example: "BRASILIA"
                                uf:
                                  type: object
                                  properties:
                                     mensagem:
                                        type: string
                                        nullable: true
                                        example: ""
                                     codigo:
                                        type: string
                                        nullable: true
                                        example: null
                                     tipo:
                                        type: string
                                        nullable: true
                                        example: null
                                     editavel:
                                        type: boolean
                                        nullable: true
                                        example: null
                                     sigla:
                                        type: string
                                        example: "GO"
                                     descricao:
                                        type: string
                                        example: ""
                                     regiao:
                                        type: string
                                        nullable: true
                                        example: null
                        contato:
                          type: object
                          properties:
                            email:
                              type: string
                              format: email
                              example: "priscilaini@yahoo.com.br"
                            telefoneResidencial:
                              type: object
                              properties:
                                ddd:
                                  type: string
                                  example: "62"
                                numero:
                                  type: string
                                  example: "99930009"      
                            telefoneCelular:
                              type: object
                              properties:
                                ddd:
                                  type: string
                                  example: "61"
                                numero:
                                  type: string
                                  example: "999930007"
                            telefoneComercial:
                              type: object
                              properties:
                                ddd:
                                  type: string
                                  nullable: true
                                  example: null
                                numero:
                                  type: string
                                  example: "(61)3445-5888"  
                        vinculacao:
                          type: string
                          nullable: true
                          example: null
                        codigoFies:
                          type: integer
                          example: 20242515
                        sexo:
                          type: object
                          properties:
                            sexo:
                              type: string
                              example: "M"
                            sexoDetalhe:
                              type: string
                              example: "Masculino"
                        pis:
                          type: string
                          example: ""
                        conjuge:
                          type: string
                          nullable: true
                          example: null
                        responsavelLegal:
                          type: string
                          nullable: true
                          example: null
                        emancipado:
                          type: object
                          properties:
                            codigo:
                              type: string
                              example: ""
                            descricao:
                              type: string
                              nullable: true
                              example: null
                            nome:
                              type: string
                              example: ""
                        nomeCandidato:
                          type: string
                          nullable: true
                          example: null
                        nomeCurso:
                          type: string
                          example: "ENFERMAGEM"
                        idCampus:
                          type: integer
                          example: 27693
                        nomeCampus:
                          type: string
                          example: "Centro Universitário Euro-Americano - Unidade Asa Sul"
                        numeroCandidato:
                          type: string
                          nullable: true
                          example: null
                        descricaoMunicipio:
                          type: string
                          nullable: true
                          example: null
                        nomeIes:
                          type: string
                          example: "CENTRO UNIVERSITÁRIO EURO-AMERICANO"
                        ufCampus:
                          type: string
                          nullable: true
                          example: null
                        contaCorrente:
                          type: string
                          nullable: true
                          example: null
                        permiteLiquidar:
                          type: string
                          example: "N"
                        voucher:
                          type: string
                          nullable: true
                          example: null
                        dataValidadeVoucher:
                          type: string
                          nullable: true
                          example: null
                        motivoImpeditivo:
                          type: string
                          nullable: true
                          example: null
                        inadimplente:
                          type: string
                          nullable: true
                          example: null
                        atrasado:
                          type: string
                          nullable: true
                          example: null
                        liquidado:
                          type: string
                          nullable: true
                          example: null
                        rendaFamiliar:
                          type: string
                          nullable: true
                          example: null
                        recebeSms:
                          type: string
                          nullable: true
                          example: null
                        vinculoSolidario:
                          type: integer
                          example: 0
                        contratoEstudante:
                          type: string
                          nullable: true
                          example: null      
                  ies:
                    type: object
                    description: Informações da Instituição de Ensino Superior (IES).
                  codigoStatusContrato:
                    type: integer
                    description: Código do status do contrato.
                    example: 5
                  numeroOperacaoSIAPI:
                    type: integer
                    description: Número da operação no SIAPI.
                    example: 187
                  statusContrato:
                    type: string
                    description: Status do contrato.
                    example: "CONTRATO ENVIADO AO SIAPI"
                  situacaoContrato:
                    type: string
                    description: Situação do contrato.
                    example: ""
                  dataLimiteContratacao:
                    type: string
                    format: date
                    description: Data limite para contratação.
                    example: "04/12/2020"
                  valorMensalidade:
                    type: number
                    format: float
                    description: Valor da mensalidade.
                    example: 635.74
                  valorContrato:
                    type: number
                    format: float
                    description: Valor total do contrato.
                    example: 3814.45
                  dataAssinatura:
                    type: string
                    format: date
                    description: Data de assinatura do contrato.
                    example: "01/01/2024"
                  percentualFinanciamento:
                    type: integer
                    description: Percentual de financiamento.
                    example: 50
                  numeroContrato:
                    type: string
                    description: Número do contrato.
                    example: "08.4736.187.0000058-00"
                  diaVencimento:
                    type: string
                    description: Dia do vencimento da parcela.
                    example: "15"
                  codigoTipoGarantia:
                    type: integer
                    description: Código do tipo de garantia.
                    example: 81
                  descricaoTipoGarantia:
                    type: string
                    description: Descrição do tipo de garantia.
                    example: "Fiança Simples/FG-FIES"
                  valorGarantia:
                    type: number
                    format: float
                    description: Valor da garantia.
                    example: 3814.45
                  codCurso:
                    type: string
                    nullable: true
                    description: Código do curso.
                    example: null
                  semestreCursados:
                    type: integer
                    description: Semestres já cursados.
                    example: 1
                  estudanteCurso:
                    type: object
                    description: Informações do estudante no curso.
                    
                  valorAditamento:
                    type: integer
                    description: Valor do aditamento.
                    example: 0
                  unidadeCaixa:
                    type: string
                    nullable: true
                    description: Unidade da Caixa.
                    example: null
                  prazoContratoMec:
                    type: integer
                    description: Prazo do contrato no MEC.
                    example: 7
                  semestreReferencia:
                    type: integer
                    description: Semestre de referência.
                    example: 2
                  anoReferencia:
                    type: integer
                    description: Ano de referência.
                    example: 2023
                  bloqueioMec:
                    type: integer
                    description: Código de bloqueio no MEC.
                    example: 0
                  permiteContratacao:
                    type: string
                    description: Indica se permite contratação.
                    example: "S"
                  recebeInformacao:
                    type: string
                    description: Indica se recebe informação.
                    example: ""
                  recebeSms:
                    type: string
                    description: Indica se recebe SMS.
                    example: "A"
                  localExtrato:
                    type: integer
                    description: Local do extrato.
                    example: 3
                  prouni:
                    type: string
                    description: Indica se é PROUNI.
                    example: "N"
                  contaCorrente:
                    type: object
                    description: Detalhes da conta corrente.
                    properties:
                      agencia:
                        type: integer
                        example: 4736
                      operacao:
                        type: integer
                        example: 13
                      dv:
                        type: integer
                        example: 1
                      nsgd:
                        type: string
                        nullable: true
                        example: null
                      contaCorrente:
                        type: integer
                        example: 6365
                  quantidadeAditamentos:
                    type: integer
                    description: Quantidade de aditamentos.
                    example: 1            
                  quantidadePreAditamentos:
                    type: integer
                    description: Quantidade de pré-aditamentos.
                    example: 0
                  sipesListaBanco:
                    type: array
                    items:
                      type: object
                      properties:
                        cpf:
                          type: string
                          example: "709.667.981-20"
                        tipo:
                          type: string
                          example: "C"
                        dataPesquisa:
                          type: string
                          nullable: true
                          example: null
                        restricao:
                          type: string
                          nullable: true
                          example: "N"
                  idSeguradora:
                    type: integer
                    description: ID da seguradora.
                    example: 104
                  indContratoNovoFies:
                    type: boolean
                    description: Indica se é um contrato novo FIES.
                    example: true
                  taxaJuros:
                    type: integer
                    description: Taxa de juros.
                    example: 0
                  existeTarifaContrato:
                    type: boolean
                    description: Indica se existe tarifa de contrato.
                    example: true
                  vrCoParticipacao:
                    type: number
                    format: float
                    description: Valor da co-participação.
                    example: 144.21
                  valorSeguro:
                    type: number
                    format: float
                    description: Valor do seguro.
                    example: 4.6
                  numeroProcessoSeletivo:
                    type: integer            
                              
                  
              
        '401':
          description: >-
            Identificação provida pelo token aponta para usuário não autorizado
            a utilizar a API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 401
                    mensagem: O token fornecido para acesso à API é inválido.
                    tipo: Erro
                    editavel: false
        '404':
          description: Página Não Encontrada.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: 
                      Página Não Encontrada.
                    tipo: Erro
                    editavel: false
        '412':
          description: Erro negocial ou estrutural na chamada da API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 412
                    mensagem: Os dados fornecidos não são válidos.
                    tipo: Erro
                    editavel: false
        '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 500
                    mensagem: Erro na execução da funcionalidade no backend.
                    tipo: Erro
                    editavel: false
      security:
        - Internet:
            - scope
          APIKey: []
  
  '/v1/listar-ies':
   get:
    tags:
        - Aditamento de Transferência
    summary: >-
     Esse serviço retorna uma lista de IES informações de Instituições de Ensino.
    description: Retorna uma lista de IES com base nos critérios de busca.
    operationId: listar-ies
    parameters:
      - in: query
        name: nomeIes
        schema:
          type: string
        description: Filtra IES por nome (parcial ou completo)..
        required: true
      
    responses:
      '200':
        description: Sucesso - Retorna os detalhes do curso de destino.
        content:
          application/json:
            schema:
              type: object
              properties:
                codigo:
                  type: string
                  nullable: true
                mensagem:
                  type: string
                  nullable: true
                tipo:
                  type: string
                  nullable: true
                listaRetorno:
                  type: array
                  items:
                    type: object
                    properties:
                      mensagem:
                        type: string
                        nullable: true
                      codigo:
                        type: string
                        nullable: true
                        example: null
                      tipo:
                        type: string
                        nullable: true
                        example: null
                      editavel:
                        type: boolean
                        nullable: true
                      dominioCombo:
                        type: string
                        nullable: true
                      id:
                        type: string
                        example: "2565"
                      descricao:
                        type: string
                        example: "2565 - ABEU - CENTRO UNIVERSITÁRIO"
                      atributo:
                        type: string
                        example: null
      '401':
        description: 
            Identificação provida pelo token aponta para usuário não autorizado
            a utilizar a API.
        content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 401
                    mensagem: O token fornecido para acesso à API é inválido.
                    tipo: Erro
                    editavel: false
      '404':
          description: Página Não Encontrada.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: 
                      Página Não Encontrada.
                    tipo: Erro
                    editavel: false
      '412':
          description: Erro negocial ou estrutural na chamada da API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 412
                    mensagem: Os dados fornecidos não são válidos.
                    tipo: Erro
                    editavel: false              
                    
      '500':
        description: Erro interno do servidor.
        content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 500
                    mensagem: Erro na execução da funcionalidade no backend.
                    tipo: Erro
                    editavel: false 
        
  '/v1/listar-campus':
   get:
    tags:
        - Aditamento de Transferência
    summary: >-
     Esse serviço retorna uma lista dos campus disponiveis para a IES informada.
    description: Esse serviço retorna uma lista dos campus disponiveis para a IES informada.
    operationId: listar-campus
    parameters:
      - in: query
        name: ies
        schema:
          type: string
        description: Código da Instituição de Ensino (IES).
        required: true
      - in: query
        name: semestreReferencia
        schema:
          type: integer
          enum: [1, 2] 
        description: Semestre de referência para a busca.
        required: true
      - in: query
        name: anoReferencia
        schema:
          type: integer
          format: int32
        description: Ano de referência para a busca.
        required: true
    responses:
      '200':
        description: Sucesso - Retorna os detalhes do curso de destino.
        content:
          application/json:
            schema:
              type: object
              properties:
                
                mensagem:
                  type: string
                  nullable: true
                codigo:
                  type: string
                  nullable: true
                tipo:
                  type: string
                  nullable: true
                editavel:
                  type: boolean
                  nullable: true
                dominioCombo:
                  type: string
                  nullable: true
                id:
                  type: string
                  example: "677"
                descricao:
                  type: string
                  example: "677 - Campus 2 / Nilópolis"
                atributo:
                  type: string
                  example : null
      '401':
          description: >-
            Identificação provida pelo token aponta para usuário não autorizado
            a utilizar a API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 401
                    mensagem: O token fornecido para acesso à API é inválido.
                    tipo: Erro
                    editavel: false
      '404':
          description: Página Não Encontrada.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: 
                      Página Não Encontrada.
                    tipo: Erro
                    editavel: false
      '412':
          description: Erro negocial ou estrutural na chamada da API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 412
                    mensagem: Os dados fornecidos não são válidos.
                    tipo: Erro
                    editavel: false
      '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 500
                    mensagem: Erro na execução da funcionalidade no backend.
                    tipo: Erro
                    editavel: false
   
  '/v1/consultar-turnos':
   get:
    tags:
        - Aditamento de Transferência
    summary: >-
     Esse serviço deve retorna os turnos disponíveis para um curso em um campus informado.
    description: Esse serviço deve retorna os turnos disponíveis para um curso em um campus informado.
    operationId: consultar-turnos
    parameters:
      - in: query
        name: nuCampus
        schema:
          type: string
        description: Número do campus de oferta opcional.
        required: true
      - in: query
        name: nuCurso
        schema:
          type: string
        description: Número do curso de oferta opcional.
        required: true
      - in: query
        name: nuTurno
        schema:
          type: string
        description: Número do turno de oferta opcional.
        required: true
      - in: query
        name: coCurso
        schema:
          type: string
        description: Código do curso de destino.
        required: true
      - in: query
        name: nuCampusDestino
        schema:
          type: string
        description: Número do campus de destino.
        required: true
      - in: query
        name: semestre
        schema:
          type: integer
          enum: [1, 2] # Assumindo que os semestres são 1 ou 2
        description: Semestre de referência.
        required: true
      - in: query
        name: ano
        schema:
          type: integer
          format: int32
        description: Ano de referência.
        required: true
    responses:
      '200':
        description: Sucesso - Retorna os detalhes do curso de destino.
        content:
          application/json:
            schema:
              type: object
              properties:
                codigo:
                  type: string
                  nullable: true
                mensagem:
                  type: string
                  nullable: true
                tipo:
                  type: string
                  nullable: true
                listaRetorno:
                  type: array
                  items:
                    type: object
                    properties:
                      mensagem:
                        type: string
                        nullable: true
                      codigo:
                        type: string
                        nullable: true
                      tipo:
                        type: string
                        nullable: true
                      editavel:
                        type: boolean
                        nullable: true
                      dominioCombo:
                        type: string
                        nullable: true
                      id:
                        type: string
                        example: "1"
                      descricao:
                        type: string
                        example: "Matutino"
                      atributo:
                        type: string
                        nullable: true
      '401':
          description: 
            Identificação provida pelo token aponta para usuário não autorizado
            a utilizar a API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 401
                    mensagem: O token fornecido para acesso à API é inválido.
                    tipo: Erro
                    editavel: false
      '404':
          description: Página Não Encontrada.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: 
                      Página Não Encontrada.
                    tipo: Erro
                    editavel: false
      '412':
          description: Erro negocial ou estrutural na chamada da API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 412
                    mensagem: Os dados fornecidos não são válidos.
                    tipo: Erro
                    editavel: false
      '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 500
                    mensagem: Erro na execução da funcionalidade no backend.
                    tipo: Erro
                    editavel: false 
  '/v1/buscar-curso-destino':
   get:
    tags:
        - Aditamento de Transferência
    summary: >-
     Busca informações do curso de destino para transferência de contrato.
    description: Retorna detalhes sobre o curso de destino com base nos parâmetros fornecidos.
    operationId: buscar-curso-destino
    parameters:
      - in: query
        name: cpf
        schema:
          type: string
        description: CPF do estudante.
        required: true
      - in: query
        name: nuCampus
        schema:
          type: string
        description: Número do campus de origem.
        required: true
      - in: query
        name: nuCurso
        schema:
          type: string
        description: Número do curso de origem.
        required: true
      - in: query
        name: nuTurno
        schema:
          type: integer
          
        description: Número do turno de origem.
        required: true
      - in: query
        name: nuCampusDestino
        schema:
          type: string
        description: Número do campus de destino (pode ser '0' para indicar algum critério).
        required: true
      - in: query
        name: semestrePendencia
        schema:
          type: integer
          enum: [1, 2] 
        description: Semestre da pendência.
        required: true
      - in: query
        name: anoPendencia
        schema:
          type: integer
          format: int32
        description: Ano da pendência.
        required: true
    responses:
      '200':
        description: Sucesso - Retorna os detalhes do curso de destino.
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
                  example: 1
                tipo:
                  type: string
                  example: null
                campusDest:
                  type: string
                  example: "Centro Universitário Euro-Americano - Unidade Aguas Claras"
                cursoDest:
                  type: string
                  example: "FISIOTERAPIA"
                mantenedoraDest:
                  type: string
                  example: "Instituto Euro Americano De Educacao Ciencia Tecnologia"
                ufDest:
                  type: string
                  nullable: true
                  example: "DF"
                municipioDest:
                  type: string
                  example: "BRASILIA"
                iesDest:
                  type: string
                  example: "CENTRO UNIVERSITÁRIO EURO-AMERICANO"  
                numeroIesDestino:
                  type: integer
                  example: 1113
                enderecoDest:
                  type: string
                  example: "Avenida Castanheira"
                duracaoCursoDest:
                  type: integer
                  example: 10
                codigoCursoHabilitacao:
                  type: string
                  example: "90571"
                numeroCursoDestino:
                  type: integer
                  example: 90571
                semestreReferenciaDest:
                  type: string
                  example: "1º/2025"
                numeroCampusDestino:
                  type: integer
                  example: 26548
                numeroMantenedoraDestino:
                  type: integer
                  example: 770
                numeroTurnoDestino:
                  type: integer
                  example:  1
                descTurnoDestino:
                  type: string
                  example: "Matutino"    
                notaEnem:
                  type: integer
                  example: 493.54
                jsonRetornoConsultaEnem:
                    type: string
                    example: "{\"turma\":{\"coNotaTransferencia\":65631,\"coIes\":1113,\"coCurso\":90571,\"coTurno\":10067,\"vlNotaTransferencia\":\"493.54\",\"nuSemestreReferencia\":\"12024\",\"coSemestre\":72},\"estudante\":{\"nuCpf\":\"70966798120\",\"vlNotaEnemConsiderada\":\"495.34\",\"nuSemestreReferencia\":\"22020\",\"coInscricao\":6614627,\"nuAnoEnem\":\"2019\"},\"transferencia\":{\"coInscricao\":6614627,\"coIes\":1113,\"coCurso\":90571,\"coTurno\":10067,\"coSemestre\":72,\"coNotaTransferencia\":65631,\"stResultado\":\"S\"}}"
                possuiLiminarNotaDeCorte:
                  type: boolean
                  example: false
                
      '401':
          description: 
            Identificação provida pelo token aponta para usuário não autorizado
            a utilizar a API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 401
                    mensagem: O token fornecido para acesso à API é inválido.
                    tipo: Erro
                    editavel: false
      '404':
          description: Página Não Encontrada.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: 
                      Página Não Encontrada.
                    tipo: Erro
                    editavel: false
      '412':
          description: Erro negocial ou estrutural na chamada da API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 412
                    mensagem: Os dados fornecidos não são válidos.
                    tipo: Erro
                    editavel: false
      '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 500
                    mensagem: Erro na execução da funcionalidade no backend.
                    tipo: Erro
                    editavel: false        
  

  '/v1/confirmar-solicitacao-estudante':
    post:
      tags:
        - Aditamento de Transferência
      summary: >- 
       Esse serviço permite confirmar a solicitação de transferencia.
      description: Esse serviço permite confirmar a solicitação de transferencia.
      operationId: confirmar-solicitacao-estudante
      requestBody:
        description: Dados da solicitação de Transferência.
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RetornoSucesso'
            example:  
              numeroSemestresCursar:
                type: integer
                description:  Número de semestres a cursar.
                example: 
              dtDesligamento:
                type: string
                description: Data de desligamento (formato DD/MM/YYYY).
                example: "01/02/2025"
              tipoTransferencia:
                type: integer
                description: Tipo da transferência.
                example: 2
              codFies:
                type: integer
                description: Código FIES do estudante.
                example: 20005266
              nuCurso:
                type: string
                description: Número do curso de origem.
                example: "0339"
              numeroCursoDestino:
                type: string
                description: Número do curso de destino.
                example: "2354"
              numeroCampusDestino:
                type: string
                description: Número do campus de destino.
                example: "20005266"
              nuCampus:
                type: string
                description: Número do campus de origem.
                example: "02645001"
              nuTurno:
                type: integer
                description: Número do turno de origem.
                example: 2
              campusDestino:
                type: string
                description: Código do campus de destino.
                example: "20005266"
              cursoDestino:
                type: string
                description: Código do curso de destino.
                example: "05001"
              turnoDestino:
                type: integer
                description: Código do turno de destino.
                example: 2
      responses:       
        '200':
          description: Solicitação de Transferência confirmada com sucesso.
          content:
            application/json:
              schema:
                type: object
                properties:
                  mensagem:
                    type: string
                    example: "Operação realizada com sucesso. Prazo para a IES validar o Transferência até 28/10/2024."
                  codigo:
                    type: integer
                    example: 200
                  tipo:
                    type: string
                    example: "alert-success"
                description: Confirmação da Transferência.
        '401':
          description: 
            Identificação provida pelo token aponta para usuário não autorizado
            a utilizar a API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 401
                    mensagem: O token fornecido para acesso à API é inválido.
                    tipo: Erro
                    editavel: false
        '404':
          description: Página Não Encontrada.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: 
                      Página Não Encontrada.
                    tipo: Erro
                    editavel: false
        '412':
          description: Erro negocial ou estrutural na chamada da API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 412
                    mensagem: Os dados fornecidos não são válidos.
                    tipo: Erro
                    editavel: false
        '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 500
                    mensagem: Erro na execução da funcionalidade no backend.
                    tipo: Erro
                    editavel: false
      security:
        - Internet:
            - scope
          APIKey: []
  '/v1/cancelar-solicitacao-transferencia':
    post:
      tags:
        - Aditamento de Transferência
      summary: >- 
       Esse serviço permite o cancelamento da solicitação de transferencia.
      description: Esse serviço permite o cancelamento da solicitação de transferencia.
      operationId: cancelar-solicitacao-transferencia
      requestBody:
        description: Dados da solicitação de Transferência.
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RetornoSucesso'
            example:
              codFies:
                type: string
                description: Código FIES do estudante.
                example: "20242515"
              idSolicitacao:
                type: integer
                description: ID da solicitação de Transferência.
                example: 57984
      responses:
        '200':
          description: operação confirmada com sucesso.
          content:
            application/json:
              schema:
                type: object
                properties:
                  mensagem:
                    type: string
                    example: "Operação realizada com sucesso."
                  codigo:
                    type: integer
                    example: 200
                  tipo:
                    type: string
                    example: "alert-success"
                description: Confirmação da Transferência.
        '401':
          description: 
            Identificação provida pelo token aponta para usuário não autorizado
            a utilizar a API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 401
                    mensagem: O token fornecido para acesso à API é inválido.
                    tipo: Erro
                    editavel: false
        '404':
          description: Página Não Encontrada.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: 
                      Página Não Encontrada.
                    tipo: Erro
                    editavel: false
        '412':
          description: Erro negocial ou estrutural na chamada da API.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 412
                    mensagem: Os dados fornecidos não são válidos.
                    tipo: Erro
                    editavel: false
        '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 500
                    mensagem: Erro na execução da funcionalidade no backend.
                    tipo: Erro
                    editavel: false
      security:
        - Internet:
            - scope
          APIKey: []
components:
  schemas:
    RetornoErro:
      title: RetornoErro
      description: Mensagens de retorno relacionadas a situações de erro.
      type: object
      properties:
        mensagem:
          type: string
        codigo:
          type: integer
          format: int64
        tipo:
          type: string
        editavel:
          type: boolean
          default: false
      example:
        mensagem: O token fornecido para acesso à API é inválido.
        codigo: 401
        tipo: Erro
        editavel: false
    RetornoSucesso:
      title: RetornoSucesso
      description: Mensagens de retorno relacionadas a situações de sucesso.
      type: object
      properties:
        mensagem:
          type: string
        codigo:
          type: integer
          format: int64
        tipo:
          type: string
        editavel:
          type: boolean
          default: false
      example:
        mensagem: Solicitação de Transferência confirmada com sucesso..
        codigo: 200
        tipo: Sucesso
        editavel: false
    
  securitySchemes:
    Internet:
      flows:
        password:
          tokenUrl: >-
            https://logindes.caixa/auth/realms/intranet/protocol/openid-connect/token
          refreshUrl: >-
            https://logindes.caixa/auth/realms/intranet/protocol/openid-connect/token
          scopes:
            scope: ''
      type: oauth2
      description: Tokens emitidos pelo realm INTRANET
    APIKey:
      type: apiKey
      description: API Key do sistema que está chamando esta API.
      name: APIKey
      in: header
security:
  - APIKey: []
    Internet:
      - scope


==================================================Querys=======================================================================
DECLARE
-- Dados da liberação da FESTB712
v_nu_sqncl_liberacao_contrato NUMBER := 141622;
v_nu_seq_candidato NUMBER := 20026852;
v_nu_ies NUMBER := 1276;
v_nu_campus NUMBER := 1058775;
v_mm_referencia_liberacao NUMBER := 6; -- Da FESTB712
v_aa_referencia_liberacao NUMBER := 2018; -- Da FESTB712
v_vr_repasse_liberacao NUMBER := 148.34; -- Da FESTB712
 
-- Variáveis para a lógica
v_nu_sqncl_rl_analitico_encontrado NUMBER;
v_existe_na_812 NUMBER;
 
-- NU_SQNCL_COMPENSACAO_REPASSE de teste.
v_nu_sqncl_compensacao_repasse NUMBER := 68;
 
-- Usuário de execução e tipo de acerto para a inserção
v_co_usuario_execucao VARCHAR2(8) := 'RdfoSp67';
v_nu_tipo_acerto_compensacao CONSTANT NUMBER := 1;
 
BEGIN
DBMS_OUTPUT.PUT_LINE('--- Início do Exercício de Lógica com INSERÇÃO REAL ---');
DBMS_OUTPUT.PUT_LINE('----------------------------------------------------');
DBMS_OUTPUT.PUT_LINE('Dados de entrada da Liberação (FESTB712):');
DBMS_OUTPUT.PUT_LINE(' NU_SQNCL_LIBERACAO_CONTRATO: ' || v_nu_sqncl_liberacao_contrato);
DBMS_OUTPUT.PUT_LINE(' NU_SEQ_CANDIDATO: ' || v_nu_seq_candidato);
DBMS_OUTPUT.PUT_LINE(' NU_IES: ' || v_nu_ies);
DBMS_OUTPUT.PUT_LINE(' NU_CAMPUS: ' || v_nu_campus);
DBMS_OUTPUT.PUT_LINE(' MM_REFERENCIA_LIBERACAO (da 712): ' || v_mm_referencia_liberacao);
DBMS_OUTPUT.PUT_LINE(' AA_REFERENCIA_LIBERACAO (da 712): ' || v_aa_referencia_liberacao);
DBMS_OUTPUT.PUT_LINE(' VR_REPASSE (da 712): ' || v_vr_repasse_liberacao);
DBMS_OUTPUT.PUT_LINE('----------------------------------------------------');
DBMS_OUTPUT.PUT_LINE('NU_SQNCL_COMPENSACAO_REPASSE (valor de teste para PK/UK da 812): ' || v_nu_sqncl_compensacao_repasse);
DBMS_OUTPUT.PUT_LINE('CO_USUARIO_INCLUSAO: ' || v_co_usuario_execucao);
DBMS_OUTPUT.PUT_LINE('----------------------------------------------------');
 
-- PASSO 1: Tentar encontrar o NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB711
DBMS_OUTPUT.PUT_LINE(CHR(10) || '>> PASSO 1: Buscando o NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB711 usando dados da FESTB712...');
BEGIN
SELECT T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO
INTO v_nu_sqncl_rl_analitico_encontrado
FROM FES.FESTB711_RLTRO_CTRTO_ANLTO T711
WHERE T711.NU_SQNCL_LIBERACAO_CONTRATO = v_nu_sqncl_liberacao_contrato
AND T711.NU_SEQ_CANDIDATO = v_nu_seq_candidato
AND T711.NU_IES = v_nu_ies
AND T711.NU_CAMPUS = v_nu_campus
AND T711.MM_REFERENCIA = v_mm_referencia_liberacao
AND T711.AA_REFERENCIA = v_aa_referencia_liberacao
AND T711.VR_REPASSE = v_vr_repasse_liberacao
ORDER BY T711.TS_APURACAO_RELATORIO DESC
FETCH FIRST 1 ROW ONLY;
 
DBMS_OUTPUT.PUT_LINE(' RESULTADO PASSO 1: SUCESSO!');
DBMS_OUTPUT.PUT_LINE(' -> NU_SQNCL_RLTRO_CTRTO_ANALITICO encontrado na FESTB711: ' || v_nu_sqncl_rl_analitico_encontrado);
 
EXCEPTION
WHEN NO_DATA_FOUND THEN
v_nu_sqncl_rl_analitico_encontrado := NULL;
DBMS_OUTPUT.PUT_LINE(' RESULTADO PASSO 1: ALERTA (NO_DATA_FOUND)!');
DBMS_OUTPUT.PUT_LINE(' -> Não foi encontrado um NU_SQNCL_RLTRO_CTRTO_ANALITICO correspondente na FESTB711 para os dados da liberação fornecidos.');
DBMS_OUTPUT.PUT_LINE(' -> A INSERÇÃO na FESTB812 NÃO será executada neste caso.');
GOTO end_logic;
WHEN TOO_MANY_ROWS THEN
DBMS_OUTPUT.PUT_LINE(' RESULTADO PASPO 1: AVISO (TOO_MANY_ROWS)!');
DBMS_OUTPUT.PUT_LINE(' -> Múltiplos NU_SQNCL_RLTRO_CTRTO_ANALITICO encontrados na FESTB711 para os critérios exatos.');
DBMS_OUTPUT.PUT_LINE(' -> O script selecionou o mais recente (ORDER BY TS_APURACAO_RELATORIO DESC).');
END;
 
-- PASSO 2: Se o analítico foi encontrado, verificar sua existência na FESTB812
IF v_nu_sqncl_rl_analitico_encontrado IS NOT NULL THEN
DBMS_OUTPUT.PUT_LINE(CHR(10) || '>> PASSO 2: Verificando a existência do analítico (' || v_nu_sqncl_rl_analitico_encontrado || ') na FESTB812...');
SELECT COUNT(1)
INTO v_existe_na_812
FROM FES.FESTB812_CMPSO_RPSE_INDVO
WHERE NU_SQNCL_RLTRO_CTRTO_ANALITICO = v_nu_sqncl_rl_analitico_encontrado
AND NU_SQNCL_COMPENSACAO_REPASSE = v_nu_sqncl_compensacao_repasse;
 
DBMS_OUTPUT.PUT_LINE(' RESULTADO PASSO 2: Checagem concluída.');
DBMS_OUTPUT.PUT_LINE(' -> Quantidade de registros existentes na FESTB812 para (Analítico: ' || v_nu_sqncl_rl_analitico_encontrado || ', Seq. Comp.: ' || v_nu_sqncl_compensacao_repasse || '): ' || v_existe_na_812);
 
IF v_existe_na_812 = 0 THEN
DBMS_OUTPUT.PUT_LINE(' DECISÃO: O registro (Analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' e Sequencial ' || v_nu_sqncl_compensacao_repasse || ') NÃO existe na FESTB812.');
DBMS_OUTPUT.PUT_LINE(' -> **Realizando a INSERÇÃO na FESTB812 agora...**');
 
-- ----------------------------------------------------------------------
-- *** AQUI É ONDE O COMANDO INSERT É ADICIONADO ***
-- ----------------------------------------------------------------------
BEGIN
INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (
NU_SQNCL_COMPENSACAO_REPASSE,
NU_SQNCL_RLTRO_CTRTO_ANALITICO,
NU_TIPO_ACERTO,
TS_INCLUSAO,
CO_USUARIO_INCLUSAO,
IC_COMPENSADO
) VALUES (
v_nu_sqncl_compensacao_repasse,
v_nu_sqncl_rl_analitico_encontrado,
v_nu_tipo_acerto_compensacao,
SYSTIMESTAMP,
v_co_usuario_execucao,
'N'
);
DBMS_OUTPUT.PUT_LINE(' SUCESSO: Inserido analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' na FESTB812 com NU_SQNCL_COMPENSACAO_REPASSE = ' || v_nu_sqncl_compensacao_repasse || '.');
EXCEPTION
WHEN DUP_VAL_ON_INDEX THEN
DBMS_OUTPUT.PUT_LINE(' ERRO (DUP_VAL_ON_INDEX): Falha na inserção para analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' e sequencial ' || v_nu_sqncl_compensacao_repasse || '. Registro já existe (possível concorrência).');
WHEN OTHERS THEN
DBMS_OUTPUT.PUT_LINE(' ERRO INESPERADO NA INSERÇÃO: Falha ao inserir analítico ' || v_nu_sqncl_rl_analitico_encontrado || ': ' || SQLERRM);
RAISE; -- Re-lança a exceção para que o bloco externo a capture
END;
-- ----------------------------------------------------------------------
 
ELSE
DBMS_OUTPUT.PUT_LINE(' DECISÃO: O registro (Analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' e Sequencial ' || v_nu_sqncl_compensacao_repasse || ') JÁ existe na FESTB812.');
DBMS_OUTPUT.PUT_LINE(' -> **A INSERÇÃO NÃO será realizada para evitar duplicidade.**');
END IF;
ELSE
DBMS_OUTPUT.PUT_LINE(CHR(10) || '>> PASSO 2: Não executado. O analítico não foi encontrado no Passo 1.');
END IF;
 
<<end_logic>>
-- IMPORTANTE: Em um processo batch, o COMMIT é geralmente feito no final do ciclo ou da transação.
-- Para este teste individual, um COMMIT manual é adequado para ver o efeito.
COMMIT;
DBMS_OUTPUT.PUT_LINE(CHR(10) || '--- Fim do Exercício de Lógica com INSERÇÃO. Transação COMITADA. ---');
 
EXCEPTION
WHEN OTHERS THEN
ROLLBACK; -- Garante que qualquer inserção parcial seja desfeita em caso de erro.
DBMS_OUTPUT.PUT_LINE(CHR(10) || 'ERRO FATAL GERAL (Transação ROLLED BACK): ' || SQLERRM);
END;
-----------------       testes ----------------------------
 
 
SELECT*FROM  FES.FESTB712_LIBERACAO_CONTRATO
WHERE NU_SQNCL_LIBERACAO_CONTRATO = 141622 ;
 
RESULTADO :
141622	20026852	12526	1276	1058775	6	6	2018	148.34	R 	2018-06-15 00:00:00.000	2018-04-26 00:00:00.000	2020-04-28 17:32:29.000	1		1		S
 
SELECT*FROM  FES.FESTB711_RLTRO_CTRTO_ANLTO
WHERE NU_SQNCL_LIBERACAO_CONTRATO = 141622 ;
 
Veja que nesse momento ele me traz
para os mesmos contratos (nusequencial 712) varios nusequencial contratato analitico
mas o mes de referencia muda , assim como o valor :
24367	1058775	2	851	1276	20026852	6	2019	185.69	0018-04-26 00:00:00.000	141622		
27399	1058775	1	851	1276	20026852	11	2019	-185.69	0018-04-26 00:00:00.000	141622	2019-12-07 20:06:01.758	24367
53287	1058775	1	12526	1276	20026852	6	2018	148.34	2018-04-26 00:00:00.000	141622	2020-04-28 17:32:29.292	
 
SELECT*FROM  FES.FESTB812_CMPSO_RPSE_INDVO flcr
WHERE NU_SQNCL_LIBERACAO_CONTRATO = 53287 ;
 
 
Possivel filtro pelo mes
pelo vr repasse :
5 parcelas com o mesmo valor
a ultima acrscenta um resto , diferente .
 
 
 
 
 
---Primeiro testes
---  1 .ja existe dados na 812 o dado da massa
--- rodar a SP para e ver se vai duplicar
      --- rodar sp
      ---Rodar verificação de duplicidade
---  2 . Verificação de criação dos dados
      --- rodar exclusão da 812
      --- rodar sp para insert
---  3 . Verificaçao de contratos sem o analitico
 
---  4 .verificação caminho de quenecial 712/909---> 711analitico---->analitico812
 
-------------------------------------------
INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (
NU_SQNCL_COMPENSACAO_REPASSE,
NU_SQNCL_RLTRO_CTRTO_ANALITICO,
NU_TIPO_ACERTO,
TS_INCLUSAO,
CO_USUARIO_INCLUSAO,
IC_COMPENSADO
) VALUES (
68, -- Pega o próximo valor da sequência
53287, -- Nosso NU_SQNCL_RLTRO_CTRTO_ANALITICO de teste
1, -- Exemplo de NU_TIPO_ACERTO, ajuste se for outro
SYSTIMESTAMP, -- Data e hora atual da inclusão
'RdfoSp67', -- Seu usuário de execução
'N' -- 'Não Compensado'
);
----------------Verica duplicidade
SELECT
NU_SQNCL_RLTRO_CTRTO_ANALITICO,
COUNT(*) AS QUANTIDADE_REGISTROS
FROM
FES.FESTB812_CMPSO_RPSE_INDVO
GROUP BY
NU_SQNCL_RLTRO_CTRTO_ANALITICO
HAVING
COUNT(*) > 1;
----------------------- Delete
DELETE FROM FES.FESTB812_CMPSO_RPSE_INDVO
WHERE NU_SQNCL_RLTRO_CTRTO_ANALITICO = 53287;
 
---SELECT PARA VER A NÃO EXIXTENCIA MAIS ---ok
SELECT NU_SQNCL_RLTRO_CTRTO_ANALITICO FROM
FES.FESTB812_CMPSO_RPSE_INDVO fcri
WHERE
NU_SQNCL_RLTRO_CTRTO_ANALITICO = 53287;
--------------------------------------Validação da logica entre tabelas ---------------------
SELECT
-- Dados da FESTB712 (Novo ponto de partida para validação)
T712.NU_SQNCL_LIBERACAO_CONTRATO AS Liberacao_Contrato_712,
T712.NU_SEQ_CANDIDATO AS Candidato_712,
T712.VR_REPASSE AS VR_Repasse_712,
T712.MM_REFERENCIA_LIBERACAO AS Mes_Ref_712,
T712.AA_REFERENCIA_LIBERACAO AS Ano_Ref_712,
T712.NU_IES AS IES_712,
T712.NU_CAMPUS AS Campus_712,
 
-- Dados da FESTB711 (o relatório analítico, vinculado à 712)
T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO AS Sequencial_Analitico_711,
T711.VR_REPASSE AS VR_Repasse_711,
T711.MM_REFERENCIA AS Mes_Ref_711,
T711.AA_REFERENCIA AS Ano_Ref_711,
 
-- Dados da FESTB812 (a tabela de compensação, verificando a existência)
T812.NU_SQNCL_COMPENSACAO_REPASSE AS Sequencial_Compensacao_812,
T812.IC_COMPENSADO AS Compensado_812,
T812.TS_INCLUSAO AS TS_Inclusao_812
FROM
FES.FESTB712_LIBERACAO_CONTRATO T712
LEFT JOIN
FES.FESTB711_RLTRO_CTRTO_ANLTO T711
ON
T711.NU_SQNCL_LIBERACAO_CONTRATO = T712.NU_SQNCL_LIBERACAO_CONTRATO
AND T711.NU_SEQ_CANDIDATO = T712.NU_SEQ_CANDIDATO
AND T711.NU_IES = T712.NU_IES
AND T711.NU_CAMPUS = T712.NU_CAMPUS
AND T711.MM_REFERENCIA = T712.MM_REFERENCIA_LIBERACAO
AND T711.AA_REFERENCIA = T712.AA_REFERENCIA_LIBERACAO
AND T711.VR_REPASSE = T712.VR_REPASSE
LEFT JOIN
FES.FESTB812_CMPSO_RPSE_INDVO T812
ON
T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO
WHERE
T712.NU_SQNCL_LIBERACAO_CONTRATO = 141622; -- Use seu sequencial de contrato específico aqui
-----------------------------------------------
INSERT INTO FES.FESTB909_LIBERACAO_CONTRATO_RE (
NU_SQNCL_LIBERACAO_CONTRATO,
NU_SEQ_CANDIDATO,
NU_MANTENEDORA,
NU_IES,
NU_CAMPUS,
NU_PARCELA,
MM_REFERENCIA_LIBERACAO,
AA_REFERENCIA_LIBERACAO,
VR_REPASSE,
IC_SITUACAO_LIBERACAO,
DT_LIBERACAO,
DT_INCLUSAO,
DT_ATUALIZACAO,
NU_PARTICIPACAO_CANDIDATO,
NU_SQNCL_ADITAMENTO,
NU_TIPO_TRANSACAO,
NU_TIPO_ACERTO,
IC_APTO_LIBERACAO_REPASSE
) VALUES (
141622, -- NU_SQNCL_LIBERACAO_CONTRATO (do SELECT)
20026852, -- NU_SEQ_CANDIDATO (do SELECT)
12526, -- NU_MANTENEDORA (do SELECT)
1276, -- NU_IES (do SELECT)
1058775, -- NU_CAMPUS (do SELECT)
6, -- NU_PARCELA (do SELECT)
6, -- MM_REFERENCIA_LIBERACAO (do SELECT)
2019, -- AA_REFERENCIA_LIBERACAO (do SELECT)
-185.69, -- VR_REPASSE (do SELECT)
'R', -- IC_SITUACAO_LIBERACAO (do SELECT)
'18-06-15',
'18-04-26',
'20-04-28',
1, -- NU_PARTICIPACAO_CANDIDATO (do SELECT)
NULL, -- NU_SQNCL_ADITAMENTO (do SELECT)
1, -- NU_TIPO_TRANSACAO (do SELECT)
NULL, -- NU_TIPO_ACERTO (do SELECT)
'S' -- IC_APTO_LIBERACAO_REPASSE (do SELECT)
);
-----------------------------TRAZ MINHA MASSA VALIDA -----------------------------------------------
-- Para me trazer os dados com nusequencial Analitico e Contratual mais condição de repasse
SELECT
-- Dados da FESTB812
T812.NU_SQNCL_COMPENSACAO_REPASSE,
T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
T812.NU_TIPO_ACERTO,
T812.IC_COMPENSADO,
-- Dados da FESTB711
T711.NU_SEQ_CANDIDATO,
T711.NU_IES,
T711.NU_CAMPUS,
T711.VR_REPASSE,
T711.MM_REFERENCIA AS MM_REFERENCIA_711,
T711.AA_REFERENCIA AS AA_REFERENCIA_711,
T711.DT_ASSINATURA,
T711.NU_SQNCL_LIBERACAO_CONTRATO, -- Este será o link para a FESTB712
-- Dados da FESTB712
T712.NU_MANTENEDORA,
T712.NU_PARCELA,
T712.MM_REFERENCIA_LIBERACAO,
T712.AA_REFERENCIA_LIBERACAO,
T712.IC_SITUACAO_LIBERACAO,
T712.DT_LIBERACAO,
T712.DT_INCLUSAO AS DT_INCLUSAO_712,
T712.DT_ATUALIZACAO AS DT_ATUALIZACAO_712,
T712.NU_PARTICIPACAO_CANDIDATO,
T712.NU_SQNCL_ADITAMENTO,
T712.NU_TIPO_TRANSACAO,
T712.NU_TIPO_ACERTO AS NU_TIPO_ACERTO_712,
T712.IC_APTO_LIBERACAO_REPASSE
FROM
JOIN FES.FESTB812_CMPSO_RPSE_INDVO T812
FES.FESTB711_RLTRO_CTRTO_ANLTO T711
ON T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO
JOIN FES.FESTB712_LIBERACAO_CONTRATO T712
ON T712.NU_SQNCL_LIBERACAO_CONTRATO = T711.NU_SQNCL_LIBERACAO_CONTRATO
WHERE T812.IC_COMPENSADO = 'N'
FETCH FIRST 1 ROW ONLY;
 
2	24367	1	N	20026852	1276	1058775	185.69	6	2019	0018-04-26 00:00:00.000	141622	12526	6	6	2018	R 	2018-06-15 00:00:00.000	2018-04-26 00:00:00.000	2020-04-28 17:32:29.000	1		1		S
 
---------------------------Decompondo esse select para mostrar a logica -------------------------------------------------
--Validando Nu sequencial contrato --ok
SELECT
T812.NU_SQNCL_COMPENSACAO_REPASSE,
T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
T812.NU_TIPO_ACERTO,
T812.IC_COMPENSADO,
T812.TS_INCLUSAO AS TS_INCLUSAO_812,
T812.CO_USUARIO_INCLUSAO AS USUARIO_INCLUSAO_812
FROM
FES.FESTB812_CMPSO_RPSE_INDVO T812
WHERE
T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO = 27352;
 
RESULTADO : sem informações
 
68	27352	6	N	2025-06-03 00:00:00.000	CRISE19
---------------------------Verificando os SequencialAnalitico--------------------
 
SELECT
T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
T711.NU_SEQ_CANDIDATO,
T711.NU_IES,
T711.NU_CAMPUS,
T711.VR_REPASSE,
T711.MM_REFERENCIA AS MM_REFERENCIA_711,
T711.AA_REFERENCIA AS AA_REFERENCIA_711,
T711.DT_ASSINATURA,
T711.NU_SQNCL_LIBERACAO_CONTRATO, -- AQUI ESTÁ O ID DA 712!
T711.TS_APURACAO_RELATORIO
FROM
FES.FESTB711_RLTRO_CTRTO_ANLTO T711
WHERE
T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO = 27399;
 
resultado :
27399	20026852	1276	1058775	-185.69	11	2019	0018-04-26 00:00:00.000	141622	2019-12-07 20:06:01.758
--------------------------Verificando o sequencialContrato correspondente------------------
SELECT
T712.NU_SQNCL_LIBERACAO_CONTRATO,
T712.NU_MANTENEDORA,
T712.NU_PARCELA,
T712.MM_REFERENCIA_LIBERACAO,
T712.AA_REFERENCIA_LIBERACAO,
T712.IC_SITUACAO_LIBERACAO,
T712.DT_LIBERACAO,
T712.DT_INCLUSAO AS DT_INCLUSAO_712,
T712.DT_ATUALIZACAO AS DT_ATUALIZACAO_712,
T712.NU_PARTICIPACAO_CANDIDATO,
T712.NU_SQNCL_ADITAMENTO,
T712.NU_TIPO_TRANSACAO,
T712.NU_TIPO_ACERTO AS NU_TIPO_ACERTO_712,
T712.IC_APTO_LIBERACAO_REPASSE
FROM
FES.FESTB712_LIBERACAO_CONTRATO T712
WHERE
T712.NU_SQNCL_LIBERACAO_CONTRATO = 141622;
 
 
141622	12526	6	6	2018	R 	2018-06-15 00:00:00.000	2018-04-26 00:00:00.000	2020-04-28 17:32:29.000	1		1		S
 
============================================
 
 
 
DELETE FROM FES.FESTB909_LIBERACAO_CONTRATO_RE
WHERE NU_SQNCL_LIBERACAO_CONTRATO = 141622;
COMMIT; -- Confirme a exclusão
 
 
INSERT INTO FES.FESTB909_LIBERACAO_CONTRATO_RE (
NU_SQNCL_LIBERACAO_CONTRATO,
NU_SEQ_CANDIDATO,
NU_MANTENEDORA,
NU_IES,
NU_CAMPUS,
NU_PARCELA,
MM_REFERENCIA_LIBERACAO, -- Este precisa ser 11 (igual ao MM_REFERENCIA da 711)
AA_REFERENCIA_LIBERACAO, -- Este precisa ser 2019 (igual ao AA_REFERENCIA da 711)
VR_REPASSE, -- Este precisa ser -185.69 (igual ao VR_REPASSE da 711)
IC_SITUACAO_LIBERACAO,
DT_LIBERACAO,
DT_INCLUSAO,
DT_ATUALIZACAO,
NU_PARTICIPACAO_CANDIDATO,
NU_SQNCL_ADITAMENTO,
NU_TIPO_TRANSACAO,
NU_TIPO_ACERTO,
IC_APTO_LIBERACAO_REPASSE
)
SELECT
T712.NU_SQNCL_LIBERACAO_CONTRATO,
T712.NU_SEQ_CANDIDATO,
T712.NU_MANTENEDORA,
T712.NU_IES,
T712.NU_CAMPUS,
T712.NU_PARCELA,
-- **** AQUI ESTÃO OS AJUSTES CHAVE! USANDO OS VALORES CORRETOS DA FESTB711 ****
11 AS MM_REFERENCIA_LIBERACAO, -- Ajustado para o mês da FESTB711
2019 AS AA_REFERENCIA_LIBERACAO, -- Ajustado para o ano da FESTB711
-185.69 AS VR_REPASSE, -- Ajustado para o VR_REPASSE da FESTB711
----------------------------------------------------------------------------------
T712.IC_SITUACAO_LIBERACAO,
T712.DT_LIBERACAO,
SYSTIMESTAMP,
SYSTIMESTAMP,
T712.NU_PARTICIPACAO_CANDIDATO,
T712.NU_SQNCL_ADITAMENTO,
T712.NU_TIPO_TRANSACAO,
T712.NU_TIPO_ACERTO,
T712.IC_APTO_LIBERACAO_REPASSE
FROM
FES.FESTB712_LIBERACAO_CONTRATO T712 -- Usamos a 712 para pegar o resto dos dados originais
WHERE
T712.NU_SQNCL_LIBERACAO_CONTRATO = 141622; -- Pegue o registro da liberação original
 
COMMIT; -- Confirme a inserção
================================================================================================
 
INSERT INTO FES.FESTB909_LIBERACAO_CONTRATO_RE (
NU_SQNCL_LIBERACAO_CONTRATO,
NU_SEQ_CANDIDATO,
NU_MANTENEDORA,
NU_IES,
NU_CAMPUS,
NU_PARCELA,
MM_REFERENCIA_LIBERACAO,
AA_REFERENCIA_LIBERACAO,
VR_REPASSE,
IC_SITUACAO_LIBERACAO,
DT_LIBERACAO,
DT_INCLUSAO,
DT_ATUALIZACAO,
NU_PARTICIPACAO_CANDIDATO,
NU_SQNCL_ADITAMENTO,
NU_TIPO_TRANSACAO,
NU_TIPO_ACERTO,
IC_APTO_LIBERACAO_REPASSE
) VALUES (
863,
20000001,
1965,
3035,
1061025,
2,
2,
2018,
1325.82,
'S',
'18-02-15',
'18-03-15',
'25-06-03',
1,
NULL,
1,
NULL,
'S'
);
 
 
 
 
=====================================INSERTS PARA TESTES ========================================
 
---------------------------------------------------------------
INSERT INTO FES.FESTB711_RLTRO_CTRTO_ANLTO (
    NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_CAMPUS, NU_TIPO_TRANSACAO, NU_MANTENEDORA, NU_IES, NU_SEQ_CANDIDATO,
    MM_REFERENCIA, AA_REFERENCIA, VR_REPASSE, DT_ASSINATURA, NU_SQNCL_LIBERACAO_CONTRATO,
    TS_APURACAO_RELATORIO, NU_SQNCL_CTRTO_ANLTO_CMPNO
) VALUES (
    90000011,      -- R9002: ID do repasse analítico duplicado
    1061025,       -- Campus
    1,             -- Tipo de transação
    1965,          -- Mantenedora
    3034,          -- IES
    90000000,      -- Candidato de teste
    3,             -- Mês de referência
    2024,          -- Ano de referência
    1325.82,       -- Valor do repasse
    TO_DATE('2024-03-15', 'YYYY-MM-DD'), -- Data de assinatura
    90000002,      -- L9002: Referencia a liberação duplicada da 712
    TO_TIMESTAMP('2024-03-15 15:01:26.000', 'YYYY-MM-DD HH24:MI:SS.FF3'), -- Timestamp de apuração
    NULL           -- Não compensado por compensação interna
);
-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Assumindo que você tem acesso à sequence para NU_SQNCL_COMPENSACAO_REPASSE ou pode inserir um valor alto fictício.
-- Se você ainda não tem o nome da sequence, use um valor como 90000000 para este campo, por exemplo.
INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (
    NU_SQNCL_COMPENSACAO_REPASSE, NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_TIPO_ACERTO,
    TS_INCLUSAO, CO_USUARIO_INCLUSAO, IC_COMPENSADO
) VALUES (
    90000000,      -- ID Fictício de Compensação (substituir pela sequence real se disponível)
    90000011,      -- R9002: NU_SQNCL_RLTRO_CTRTO_ANALITICO do repasse duplicado (mesmo da FESTB711)
    1,             -- Tipo de acerto (1 para duplicidade)
    SYSTIMESTAMP,  -- Data de inclusão
    'C000000',     -- Usuário de inclusão
    'N'            -- Indicador de compensado ('N' para pendente)
);
 
 
 
 
 
=============================FESSPU19 AJUSTES VALIDAR 909 e 712 ================================
FES.FESSPU19_ROTINA_REPASSE
 
-- SELECT DA COMPENSACAO POR MANTENEDORA, NAO COMPENSADOS----eu  quero fazer essa mesma validação / sera que é necessária , ou só fazer entre a tabela 909 712 e 812
 
 
		OPEN RC1 FOR
		SELECT A.* FROM (
			SELECT T812.NU_SQNCL_COMPENSACAO_REPASSE, T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
				   T711.NU_SEQ_CANDIDATO, T711.NU_IES, T711.NU_CAMPUS, T711.VR_REPASSE,
				   T711.DT_ASSINATURA, T712.NU_TIPO_TRANSACAO, T712.NU_SQNCL_LIBERACAO_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 1 THEN NVL(T36.VR_CONTRATO, 0) ELSE 0 END AS VR_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 2 THEN NVL(T38.VR_ADITAMENTO, 0) ELSE 0 END AS VR_ADITAMENTO,
				   T812.NU_TIPO_ACERTO
			  FROM FES.FESTB812_CMPSO_RPSE_INDVO T812
			  JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO T711
			    ON T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO
			  JOIN FES.FESTB712_LIBERACAO_CONTRATO T712
			    ON T712.NU_SQNCL_LIBERACAO_CONTRATO = T711.NU_SQNCL_LIBERACAO_CONTRATO
			  LEFT JOIN FES.FESTB036_CONTRATO_FIES T36
			    ON T36.NU_CANDIDATO_FK11 = T712.NU_SEQ_CANDIDATO
			   AND T36.NU_PARTICIPACAO_FK11 = T712.NU_PARTICIPACAO_CANDIDATO
			  LEFT JOIN FES.FESTB038_ADTMO_CONTRATO T38
			    ON T38.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO
			   AND T38.NU_PARTICIPACAO_FK36 = T712.NU_PARTICIPACAO_CANDIDATO
			   AND T38.NU_SEQ_ADITAMENTO = T712.NU_SQNCL_ADITAMENTO
			 WHERE T812.IC_COMPENSADO = 'N'
			   AND T711.NU_MANTENEDORA = pNU_MANTENEDORA
			UNION ALL
			SELECT T812.NU_SQNCL_COMPENSACAO_REPASSE, T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
				   T711.NU_SEQ_CANDIDATO, T711.NU_IES, T711.NU_CAMPUS, T711.VR_REPASSE,
				   T711.DT_ASSINATURA, T712.NU_TIPO_TRANSACAO, T712.NU_SQNCL_LIBERACAO_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 1 THEN NVL(T36.VR_CONTRATO, 0) ELSE 0 END AS VR_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 2 THEN NVL(T38.VR_ADITAMENTO, 0) ELSE 0 END AS VR_ADITAMENTO,
				   T812.NU_TIPO_ACERTO
			  FROM FES.FESTB816_MIGRACAO_IES T816
			  JOIN FES.FESTB712_LIBERACAO_CONTRATO T712
			    ON T712.NU_IES = T816.NU_IES
			  JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO T711
			    ON T711.NU_SQNCL_LIBERACAO_CONTRATO = T712.NU_SQNCL_LIBERACAO_CONTRATO
			   AND T711.NU_MANTENEDORA <> T816.NU_MANTENEDORA_ADQUIRENTE -- LIBERACOES QUE FORAM REPASSADAS INCORRETAMENTE JA NA ADQUIRENTE
			  JOIN FES.FESTB812_CMPSO_RPSE_INDVO T812
			    ON T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO
			   AND T812.IC_COMPENSADO = 'N'
			  LEFT JOIN FES.FESTB036_CONTRATO_FIES T36
			    ON T36.NU_CANDIDATO_FK11 = T712.NU_SEQ_CANDIDATO
			   AND T36.NU_PARTICIPACAO_FK11 = T712.NU_PARTICIPACAO_CANDIDATO
			  LEFT JOIN FES.FESTB038_ADTMO_CONTRATO T38
			    ON T38.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO
			   AND T38.NU_PARTICIPACAO_FK36 = T712.NU_PARTICIPACAO_CANDIDATO
			   AND T38.NU_SEQ_ADITAMENTO = T712.NU_SQNCL_ADITAMENTO
			 WHERE T816.NU_MANTENEDORA_ADQUIRENTE = pNU_MANTENEDORA
			   AND T816.IC_STCO_AUTORIZACAO_MIGRACAO = '1' -- AUTORIZADA
			   AND T816.DT_FIM_AQUISICAO_IES IS NULL -- VIGENTE
			   AND T816.IC_MIGRACAO_IES ='1' -- INCORPORACAO
		) A
		ORDER BY A.VR_REPASSE;
	END IF;
 
END;













=============================================================================
DECLARE
-- Dados da liberação da FESTB712
v_nu_sqncl_liberacao_contrato NUMBER := 141622;
v_nu_seq_candidato NUMBER := 20026852;
v_nu_ies NUMBER := 1276;
v_nu_campus NUMBER := 1058775;
v_mm_referencia_liberacao NUMBER := 6; -- Da FESTB712
v_aa_referencia_liberacao NUMBER := 2018; -- Da FESTB712
v_vr_repasse_liberacao NUMBER := 148.34; -- Da FESTB712
 
-- Variáveis para a lógica
v_nu_sqncl_rl_analitico_encontrado NUMBER;
v_existe_na_812 NUMBER;
 
-- NU_SQNCL_COMPENSACAO_REPASSE de teste.
v_nu_sqncl_compensacao_repasse NUMBER := 68;
 
-- Usuário de execução e tipo de acerto para a inserção
v_co_usuario_execucao VARCHAR2(8) := 'RdfoSp67';
v_nu_tipo_acerto_compensacao CONSTANT NUMBER := 1;
 
BEGIN
DBMS_OUTPUT.PUT_LINE('--- Início do Exercício de Lógica com INSERÇÃO REAL ---');
DBMS_OUTPUT.PUT_LINE('----------------------------------------------------');
DBMS_OUTPUT.PUT_LINE('Dados de entrada da Liberação (FESTB712):');
DBMS_OUTPUT.PUT_LINE(' NU_SQNCL_LIBERACAO_CONTRATO: ' || v_nu_sqncl_liberacao_contrato);
DBMS_OUTPUT.PUT_LINE(' NU_SEQ_CANDIDATO: ' || v_nu_seq_candidato);
DBMS_OUTPUT.PUT_LINE(' NU_IES: ' || v_nu_ies);
DBMS_OUTPUT.PUT_LINE(' NU_CAMPUS: ' || v_nu_campus);
DBMS_OUTPUT.PUT_LINE(' MM_REFERENCIA_LIBERACAO (da 712): ' || v_mm_referencia_liberacao);
DBMS_OUTPUT.PUT_LINE(' AA_REFERENCIA_LIBERACAO (da 712): ' || v_aa_referencia_liberacao);
DBMS_OUTPUT.PUT_LINE(' VR_REPASSE (da 712): ' || v_vr_repasse_liberacao);
DBMS_OUTPUT.PUT_LINE('----------------------------------------------------');
DBMS_OUTPUT.PUT_LINE('NU_SQNCL_COMPENSACAO_REPASSE (valor de teste para PK/UK da 812): ' || v_nu_sqncl_compensacao_repasse);
DBMS_OUTPUT.PUT_LINE('CO_USUARIO_INCLUSAO: ' || v_co_usuario_execucao);
DBMS_OUTPUT.PUT_LINE('----------------------------------------------------');
 
-- PASSO 1: Tentar encontrar o NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB711
DBMS_OUTPUT.PUT_LINE(CHR(10) || '>> PASSO 1: Buscando o NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB711 usando dados da FESTB712...');
BEGIN
SELECT T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO
INTO v_nu_sqncl_rl_analitico_encontrado
FROM FES.FESTB711_RLTRO_CTRTO_ANLTO T711
WHERE T711.NU_SQNCL_LIBERACAO_CONTRATO = v_nu_sqncl_liberacao_contrato
AND T711.NU_SEQ_CANDIDATO = v_nu_seq_candidato
AND T711.NU_IES = v_nu_ies
AND T711.NU_CAMPUS = v_nu_campus
AND T711.MM_REFERENCIA = v_mm_referencia_liberacao
AND T711.AA_REFERENCIA = v_aa_referencia_liberacao
AND T711.VR_REPASSE = v_vr_repasse_liberacao
ORDER BY T711.TS_APURACAO_RELATORIO DESC
FETCH FIRST 1 ROW ONLY;
 
DBMS_OUTPUT.PUT_LINE(' RESULTADO PASSO 1: SUCESSO!');
DBMS_OUTPUT.PUT_LINE(' -> NU_SQNCL_RLTRO_CTRTO_ANALITICO encontrado na FESTB711: ' || v_nu_sqncl_rl_analitico_encontrado);
 
EXCEPTION
WHEN NO_DATA_FOUND THEN
v_nu_sqncl_rl_analitico_encontrado := NULL;
DBMS_OUTPUT.PUT_LINE(' RESULTADO PASSO 1: ALERTA (NO_DATA_FOUND)!');
DBMS_OUTPUT.PUT_LINE(' -> Não foi encontrado um NU_SQNCL_RLTRO_CTRTO_ANALITICO correspondente na FESTB711 para os dados da liberação fornecidos.');
DBMS_OUTPUT.PUT_LINE(' -> A INSERÇÃO na FESTB812 NÃO será executada neste caso.');
GOTO end_logic;
WHEN TOO_MANY_ROWS THEN
DBMS_OUTPUT.PUT_LINE(' RESULTADO PASPO 1: AVISO (TOO_MANY_ROWS)!');
DBMS_OUTPUT.PUT_LINE(' -> Múltiplos NU_SQNCL_RLTRO_CTRTO_ANALITICO encontrados na FESTB711 para os critérios exatos.');
DBMS_OUTPUT.PUT_LINE(' -> O script selecionou o mais recente (ORDER BY TS_APURACAO_RELATORIO DESC).');
END;
 
-- PASSO 2: Se o analítico foi encontrado, verificar sua existência na FESTB812
IF v_nu_sqncl_rl_analitico_encontrado IS NOT NULL THEN
DBMS_OUTPUT.PUT_LINE(CHR(10) || '>> PASSO 2: Verificando a existência do analítico (' || v_nu_sqncl_rl_analitico_encontrado || ') na FESTB812...');
SELECT COUNT(1)
INTO v_existe_na_812
FROM FES.FESTB812_CMPSO_RPSE_INDVO
WHERE NU_SQNCL_RLTRO_CTRTO_ANALITICO = v_nu_sqncl_rl_analitico_encontrado
AND NU_SQNCL_COMPENSACAO_REPASSE = v_nu_sqncl_compensacao_repasse;
 
DBMS_OUTPUT.PUT_LINE(' RESULTADO PASSO 2: Checagem concluída.');
DBMS_OUTPUT.PUT_LINE(' -> Quantidade de registros existentes na FESTB812 para (Analítico: ' || v_nu_sqncl_rl_analitico_encontrado || ', Seq. Comp.: ' || v_nu_sqncl_compensacao_repasse || '): ' || v_existe_na_812);
 
IF v_existe_na_812 = 0 THEN
DBMS_OUTPUT.PUT_LINE(' DECISÃO: O registro (Analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' e Sequencial ' || v_nu_sqncl_compensacao_repasse || ') NÃO existe na FESTB812.');
DBMS_OUTPUT.PUT_LINE(' -> **Realizando a INSERÇÃO na FESTB812 agora...**');
 
-- ----------------------------------------------------------------------
-- *** AQUI É ONDE O COMANDO INSERT É ADICIONADO ***
-- ----------------------------------------------------------------------
BEGIN
INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (
NU_SQNCL_COMPENSACAO_REPASSE,
NU_SQNCL_RLTRO_CTRTO_ANALITICO,
NU_TIPO_ACERTO,
TS_INCLUSAO,
CO_USUARIO_INCLUSAO,
IC_COMPENSADO
) VALUES (
v_nu_sqncl_compensacao_repasse,
v_nu_sqncl_rl_analitico_encontrado,
v_nu_tipo_acerto_compensacao,
SYSTIMESTAMP,
v_co_usuario_execucao,
'N'
);
DBMS_OUTPUT.PUT_LINE(' SUCESSO: Inserido analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' na FESTB812 com NU_SQNCL_COMPENSACAO_REPASSE = ' || v_nu_sqncl_compensacao_repasse || '.');
EXCEPTION
WHEN DUP_VAL_ON_INDEX THEN
DBMS_OUTPUT.PUT_LINE(' ERRO (DUP_VAL_ON_INDEX): Falha na inserção para analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' e sequencial ' || v_nu_sqncl_compensacao_repasse || '. Registro já existe (possível concorrência).');
WHEN OTHERS THEN
DBMS_OUTPUT.PUT_LINE(' ERRO INESPERADO NA INSERÇÃO: Falha ao inserir analítico ' || v_nu_sqncl_rl_analitico_encontrado || ': ' || SQLERRM);
RAISE; -- Re-lança a exceção para que o bloco externo a capture
END;
-- ----------------------------------------------------------------------
 
ELSE
DBMS_OUTPUT.PUT_LINE(' DECISÃO: O registro (Analítico ' || v_nu_sqncl_rl_analitico_encontrado || ' e Sequencial ' || v_nu_sqncl_compensacao_repasse || ') JÁ existe na FESTB812.');
DBMS_OUTPUT.PUT_LINE(' -> **A INSERÇÃO NÃO será realizada para evitar duplicidade.**');
END IF;
ELSE
DBMS_OUTPUT.PUT_LINE(CHR(10) || '>> PASSO 2: Não executado. O analítico não foi encontrado no Passo 1.');
END IF;
 
<<end_logic>>
-- IMPORTANTE: Em um processo batch, o COMMIT é geralmente feito no final do ciclo ou da transação.
-- Para este teste individual, um COMMIT manual é adequado para ver o efeito.
COMMIT;
DBMS_OUTPUT.PUT_LINE(CHR(10) || '--- Fim do Exercício de Lógica com INSERÇÃO. Transação COMITADA. ---');
 
EXCEPTION
WHEN OTHERS THEN
ROLLBACK; -- Garante que qualquer inserção parcial seja desfeita em caso de erro.
DBMS_OUTPUT.PUT_LINE(CHR(10) || 'ERRO FATAL GERAL (Transação ROLLED BACK): ' || SQLERRM);
END;
-----------------       testes ----------------------------
 
 
SELECT*FROM  FES.FESTB712_LIBERACAO_CONTRATO
WHERE NU_SQNCL_LIBERACAO_CONTRATO = 141622 ;
 
RESULTADO :
141622	20026852	12526	1276	1058775	6	6	2018	148.34	R 	2018-06-15 00:00:00.000	2018-04-26 00:00:00.000	2020-04-28 17:32:29.000	1		1		S
 
SELECT*FROM  FES.FESTB711_RLTRO_CTRTO_ANLTO
WHERE NU_SQNCL_LIBERACAO_CONTRATO = 141622 ;
 
Veja que nesse momento ele me traz
para os mesmos contratos (nusequencial 712) varios nusequencial contratato analitico
mas o mes de referencia muda , assim como o valor :
24367	1058775	2	851	1276	20026852	6	2019	185.69	0018-04-26 00:00:00.000	141622		
27399	1058775	1	851	1276	20026852	11	2019	-185.69	0018-04-26 00:00:00.000	141622	2019-12-07 20:06:01.758	24367
53287	1058775	1	12526	1276	20026852	6	2018	148.34	2018-04-26 00:00:00.000	141622	2020-04-28 17:32:29.292	
 
SELECT*FROM  FES.FESTB812_CMPSO_RPSE_INDVO flcr
WHERE NU_SQNCL_LIBERACAO_CONTRATO = 53287 ;
 
 
Possivel filtro pelo mes
pelo vr repasse :
5 parcelas com o mesmo valor
a ultima acrscenta um resto , diferente .
 
 
 
 
 
---Primeiro testes
---  1 .ja existe dados na 812 o dado da massa
--- rodar a SP para e ver se vai duplicar
      --- rodar sp
      ---Rodar verificação de duplicidade
---  2 . Verificação de criação dos dados
      --- rodar exclusão da 812
      --- rodar sp para insert
---  3 . Verificaçao de contratos sem o analitico
 
---  4 .verificação caminho de quenecial 712/909---> 711analitico---->analitico812
 
-------------------------------------------
INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (
NU_SQNCL_COMPENSACAO_REPASSE,
NU_SQNCL_RLTRO_CTRTO_ANALITICO,
NU_TIPO_ACERTO,
TS_INCLUSAO,
CO_USUARIO_INCLUSAO,
IC_COMPENSADO
) VALUES (
68, -- Pega o próximo valor da sequência
53287, -- Nosso NU_SQNCL_RLTRO_CTRTO_ANALITICO de teste
1, -- Exemplo de NU_TIPO_ACERTO, ajuste se for outro
SYSTIMESTAMP, -- Data e hora atual da inclusão
'RdfoSp67', -- Seu usuário de execução
'N' -- 'Não Compensado'
);
----------------Verica duplicidade
SELECT
NU_SQNCL_RLTRO_CTRTO_ANALITICO,
COUNT(*) AS QUANTIDADE_REGISTROS
FROM
FES.FESTB812_CMPSO_RPSE_INDVO
GROUP BY
NU_SQNCL_RLTRO_CTRTO_ANALITICO
HAVING
COUNT(*) > 1;
----------------------- Delete
DELETE FROM FES.FESTB812_CMPSO_RPSE_INDVO
WHERE NU_SQNCL_RLTRO_CTRTO_ANALITICO = 53287;
 
---SELECT PARA VER A NÃO EXIXTENCIA MAIS ---ok
SELECT NU_SQNCL_RLTRO_CTRTO_ANALITICO FROM
FES.FESTB812_CMPSO_RPSE_INDVO fcri
WHERE
NU_SQNCL_RLTRO_CTRTO_ANALITICO = 53287;
--------------------------------------Validação da logica entre tabelas ---------------------
SELECT
-- Dados da FESTB712 (Novo ponto de partida para validação)
T712.NU_SQNCL_LIBERACAO_CONTRATO AS Liberacao_Contrato_712,
T712.NU_SEQ_CANDIDATO AS Candidato_712,
T712.VR_REPASSE AS VR_Repasse_712,
T712.MM_REFERENCIA_LIBERACAO AS Mes_Ref_712,
T712.AA_REFERENCIA_LIBERACAO AS Ano_Ref_712,
T712.NU_IES AS IES_712,
T712.NU_CAMPUS AS Campus_712,
 
-- Dados da FESTB711 (o relatório analítico, vinculado à 712)
T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO AS Sequencial_Analitico_711,
T711.VR_REPASSE AS VR_Repasse_711,
T711.MM_REFERENCIA AS Mes_Ref_711,
T711.AA_REFERENCIA AS Ano_Ref_711,
 
-- Dados da FESTB812 (a tabela de compensação, verificando a existência)
T812.NU_SQNCL_COMPENSACAO_REPASSE AS Sequencial_Compensacao_812,
T812.IC_COMPENSADO AS Compensado_812,
T812.TS_INCLUSAO AS TS_Inclusao_812
FROM
FES.FESTB712_LIBERACAO_CONTRATO T712
LEFT JOIN
FES.FESTB711_RLTRO_CTRTO_ANLTO T711
ON
T711.NU_SQNCL_LIBERACAO_CONTRATO = T712.NU_SQNCL_LIBERACAO_CONTRATO
AND T711.NU_SEQ_CANDIDATO = T712.NU_SEQ_CANDIDATO
AND T711.NU_IES = T712.NU_IES
AND T711.NU_CAMPUS = T712.NU_CAMPUS
AND T711.MM_REFERENCIA = T712.MM_REFERENCIA_LIBERACAO
AND T711.AA_REFERENCIA = T712.AA_REFERENCIA_LIBERACAO
AND T711.VR_REPASSE = T712.VR_REPASSE
LEFT JOIN
FES.FESTB812_CMPSO_RPSE_INDVO T812
ON
T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO
WHERE
T712.NU_SQNCL_LIBERACAO_CONTRATO = 141622; -- Use seu sequencial de contrato específico aqui
-----------------------------------------------
INSERT INTO FES.FESTB909_LIBERACAO_CONTRATO_RE (
NU_SQNCL_LIBERACAO_CONTRATO,
NU_SEQ_CANDIDATO,
NU_MANTENEDORA,
NU_IES,
NU_CAMPUS,
NU_PARCELA,
MM_REFERENCIA_LIBERACAO,
AA_REFERENCIA_LIBERACAO,
VR_REPASSE,
IC_SITUACAO_LIBERACAO,
DT_LIBERACAO,
DT_INCLUSAO,
DT_ATUALIZACAO,
NU_PARTICIPACAO_CANDIDATO,
NU_SQNCL_ADITAMENTO,
NU_TIPO_TRANSACAO,
NU_TIPO_ACERTO,
IC_APTO_LIBERACAO_REPASSE
) VALUES (
141622, -- NU_SQNCL_LIBERACAO_CONTRATO (do SELECT)
20026852, -- NU_SEQ_CANDIDATO (do SELECT)
12526, -- NU_MANTENEDORA (do SELECT)
1276, -- NU_IES (do SELECT)
1058775, -- NU_CAMPUS (do SELECT)
6, -- NU_PARCELA (do SELECT)
6, -- MM_REFERENCIA_LIBERACAO (do SELECT)
2019, -- AA_REFERENCIA_LIBERACAO (do SELECT)
-185.69, -- VR_REPASSE (do SELECT)
'R', -- IC_SITUACAO_LIBERACAO (do SELECT)
'18-06-15',
'18-04-26',
'20-04-28',
1, -- NU_PARTICIPACAO_CANDIDATO (do SELECT)
NULL, -- NU_SQNCL_ADITAMENTO (do SELECT)
1, -- NU_TIPO_TRANSACAO (do SELECT)
NULL, -- NU_TIPO_ACERTO (do SELECT)
'S' -- IC_APTO_LIBERACAO_REPASSE (do SELECT)
);
-----------------------------TRAZ MINHA MASSA VALIDA -----------------------------------------------
-- Para me trazer os dados com nusequencial Analitico e Contratual mais condição de repasse
SELECT
-- Dados da FESTB812
T812.NU_SQNCL_COMPENSACAO_REPASSE,
T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
T812.NU_TIPO_ACERTO,
T812.IC_COMPENSADO,
-- Dados da FESTB711
T711.NU_SEQ_CANDIDATO,
T711.NU_IES,
T711.NU_CAMPUS,
T711.VR_REPASSE,
T711.MM_REFERENCIA AS MM_REFERENCIA_711,
T711.AA_REFERENCIA AS AA_REFERENCIA_711,
T711.DT_ASSINATURA,
T711.NU_SQNCL_LIBERACAO_CONTRATO, -- Este será o link para a FESTB712
-- Dados da FESTB712
T712.NU_MANTENEDORA,
T712.NU_PARCELA,
T712.MM_REFERENCIA_LIBERACAO,
T712.AA_REFERENCIA_LIBERACAO,
T712.IC_SITUACAO_LIBERACAO,
T712.DT_LIBERACAO,
T712.DT_INCLUSAO AS DT_INCLUSAO_712,
T712.DT_ATUALIZACAO AS DT_ATUALIZACAO_712,
T712.NU_PARTICIPACAO_CANDIDATO,
T712.NU_SQNCL_ADITAMENTO,
T712.NU_TIPO_TRANSACAO,
T712.NU_TIPO_ACERTO AS NU_TIPO_ACERTO_712,
T712.IC_APTO_LIBERACAO_REPASSE
FROM
JOIN FES.FESTB812_CMPSO_RPSE_INDVO T812
FES.FESTB711_RLTRO_CTRTO_ANLTO T711
ON T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO
JOIN FES.FESTB712_LIBERACAO_CONTRATO T712
ON T712.NU_SQNCL_LIBERACAO_CONTRATO = T711.NU_SQNCL_LIBERACAO_CONTRATO
WHERE T812.IC_COMPENSADO = 'N'
FETCH FIRST 1 ROW ONLY;
 
2	24367	1	N	20026852	1276	1058775	185.69	6	2019	0018-04-26 00:00:00.000	141622	12526	6	6	2018	R 	2018-06-15 00:00:00.000	2018-04-26 00:00:00.000	2020-04-28 17:32:29.000	1		1		S
 
---------------------------Decompondo esse select para mostrar a logica -------------------------------------------------
--Validando Nu sequencial contrato --ok
SELECT
T812.NU_SQNCL_COMPENSACAO_REPASSE,
T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
T812.NU_TIPO_ACERTO,
T812.IC_COMPENSADO,
T812.TS_INCLUSAO AS TS_INCLUSAO_812,
T812.CO_USUARIO_INCLUSAO AS USUARIO_INCLUSAO_812
FROM
FES.FESTB812_CMPSO_RPSE_INDVO T812
WHERE
T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO = 27352;
 
RESULTADO : sem informações
 
68	27352	6	N	2025-06-03 00:00:00.000	CRISE19
---------------------------Verificando os SequencialAnalitico--------------------
 
SELECT
T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
T711.NU_SEQ_CANDIDATO,
T711.NU_IES,
T711.NU_CAMPUS,
T711.VR_REPASSE,
T711.MM_REFERENCIA AS MM_REFERENCIA_711,
T711.AA_REFERENCIA AS AA_REFERENCIA_711,
T711.DT_ASSINATURA,
T711.NU_SQNCL_LIBERACAO_CONTRATO, -- AQUI ESTÁ O ID DA 712!
T711.TS_APURACAO_RELATORIO
FROM
FES.FESTB711_RLTRO_CTRTO_ANLTO T711
WHERE
T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO = 27399;
 
resultado :
27399	20026852	1276	1058775	-185.69	11	2019	0018-04-26 00:00:00.000	141622	2019-12-07 20:06:01.758
--------------------------Verificando o sequencialContrato correspondente------------------
SELECT
T712.NU_SQNCL_LIBERACAO_CONTRATO,
T712.NU_MANTENEDORA,
T712.NU_PARCELA,
T712.MM_REFERENCIA_LIBERACAO,
T712.AA_REFERENCIA_LIBERACAO,
T712.IC_SITUACAO_LIBERACAO,
T712.DT_LIBERACAO,
T712.DT_INCLUSAO AS DT_INCLUSAO_712,
T712.DT_ATUALIZACAO AS DT_ATUALIZACAO_712,
T712.NU_PARTICIPACAO_CANDIDATO,
T712.NU_SQNCL_ADITAMENTO,
T712.NU_TIPO_TRANSACAO,
T712.NU_TIPO_ACERTO AS NU_TIPO_ACERTO_712,
T712.IC_APTO_LIBERACAO_REPASSE
FROM
FES.FESTB712_LIBERACAO_CONTRATO T712
WHERE
T712.NU_SQNCL_LIBERACAO_CONTRATO = 141622;
 
 
141622	12526	6	6	2018	R 	2018-06-15 00:00:00.000	2018-04-26 00:00:00.000	2020-04-28 17:32:29.000	1		1		S
 
============================================
 
 
 
DELETE FROM FES.FESTB909_LIBERACAO_CONTRATO_RE
WHERE NU_SQNCL_LIBERACAO_CONTRATO = 141622;
COMMIT; -- Confirme a exclusão
 
 
INSERT INTO FES.FESTB909_LIBERACAO_CONTRATO_RE (
NU_SQNCL_LIBERACAO_CONTRATO,
NU_SEQ_CANDIDATO,
NU_MANTENEDORA,
NU_IES,
NU_CAMPUS,
NU_PARCELA,
MM_REFERENCIA_LIBERACAO, -- Este precisa ser 11 (igual ao MM_REFERENCIA da 711)
AA_REFERENCIA_LIBERACAO, -- Este precisa ser 2019 (igual ao AA_REFERENCIA da 711)
VR_REPASSE, -- Este precisa ser -185.69 (igual ao VR_REPASSE da 711)
IC_SITUACAO_LIBERACAO,
DT_LIBERACAO,
DT_INCLUSAO,
DT_ATUALIZACAO,
NU_PARTICIPACAO_CANDIDATO,
NU_SQNCL_ADITAMENTO,
NU_TIPO_TRANSACAO,
NU_TIPO_ACERTO,
IC_APTO_LIBERACAO_REPASSE
)
SELECT
T712.NU_SQNCL_LIBERACAO_CONTRATO,
T712.NU_SEQ_CANDIDATO,
T712.NU_MANTENEDORA,
T712.NU_IES,
T712.NU_CAMPUS,
T712.NU_PARCELA,
-- **** AQUI ESTÃO OS AJUSTES CHAVE! USANDO OS VALORES CORRETOS DA FESTB711 ****
11 AS MM_REFERENCIA_LIBERACAO, -- Ajustado para o mês da FESTB711
2019 AS AA_REFERENCIA_LIBERACAO, -- Ajustado para o ano da FESTB711
-185.69 AS VR_REPASSE, -- Ajustado para o VR_REPASSE da FESTB711
----------------------------------------------------------------------------------
T712.IC_SITUACAO_LIBERACAO,
T712.DT_LIBERACAO,
SYSTIMESTAMP,
SYSTIMESTAMP,
T712.NU_PARTICIPACAO_CANDIDATO,
T712.NU_SQNCL_ADITAMENTO,
T712.NU_TIPO_TRANSACAO,
T712.NU_TIPO_ACERTO,
T712.IC_APTO_LIBERACAO_REPASSE
FROM
FES.FESTB712_LIBERACAO_CONTRATO T712 -- Usamos a 712 para pegar o resto dos dados originais
WHERE
T712.NU_SQNCL_LIBERACAO_CONTRATO = 141622; -- Pegue o registro da liberação original
 
COMMIT; -- Confirme a inserção
================================================================================================
 
INSERT INTO FES.FESTB909_LIBERACAO_CONTRATO_RE (
NU_SQNCL_LIBERACAO_CONTRATO,
NU_SEQ_CANDIDATO,
NU_MANTENEDORA,
NU_IES,
NU_CAMPUS,
NU_PARCELA,
MM_REFERENCIA_LIBERACAO,
AA_REFERENCIA_LIBERACAO,
VR_REPASSE,
IC_SITUACAO_LIBERACAO,
DT_LIBERACAO,
DT_INCLUSAO,
DT_ATUALIZACAO,
NU_PARTICIPACAO_CANDIDATO,
NU_SQNCL_ADITAMENTO,
NU_TIPO_TRANSACAO,
NU_TIPO_ACERTO,
IC_APTO_LIBERACAO_REPASSE
) VALUES (
863,
20000001,
1965,
3035,
1061025,
2,
2,
2018,
1325.82,
'S',
'18-02-15',
'18-03-15',
'25-06-03',
1,
NULL,
1,
NULL,
'S'
);
 
 
 
 
=====================================INSERTS PARA TESTES ========================================
 
---------------------------------------------------------------
INSERT INTO FES.FESTB711_RLTRO_CTRTO_ANLTO (
    NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_CAMPUS, NU_TIPO_TRANSACAO, NU_MANTENEDORA, NU_IES, NU_SEQ_CANDIDATO,
    MM_REFERENCIA, AA_REFERENCIA, VR_REPASSE, DT_ASSINATURA, NU_SQNCL_LIBERACAO_CONTRATO,
    TS_APURACAO_RELATORIO, NU_SQNCL_CTRTO_ANLTO_CMPNO
) VALUES (
    90000011,      -- R9002: ID do repasse analítico duplicado
    1061025,       -- Campus
    1,             -- Tipo de transação
    1965,          -- Mantenedora
    3034,          -- IES
    90000000,      -- Candidato de teste
    3,             -- Mês de referência
    2024,          -- Ano de referência
    1325.82,       -- Valor do repasse
    TO_DATE('2024-03-15', 'YYYY-MM-DD'), -- Data de assinatura
    90000002,      -- L9002: Referencia a liberação duplicada da 712
    TO_TIMESTAMP('2024-03-15 15:01:26.000', 'YYYY-MM-DD HH24:MI:SS.FF3'), -- Timestamp de apuração
    NULL           -- Não compensado por compensação interna
);
-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Assumindo que você tem acesso à sequence para NU_SQNCL_COMPENSACAO_REPASSE ou pode inserir um valor alto fictício.
-- Se você ainda não tem o nome da sequence, use um valor como 90000000 para este campo, por exemplo.
INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (
    NU_SQNCL_COMPENSACAO_REPASSE, NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_TIPO_ACERTO,
    TS_INCLUSAO, CO_USUARIO_INCLUSAO, IC_COMPENSADO
) VALUES (
    90000000,      -- ID Fictício de Compensação (substituir pela sequence real se disponível)
    90000011,      -- R9002: NU_SQNCL_RLTRO_CTRTO_ANALITICO do repasse duplicado (mesmo da FESTB711)
    1,             -- Tipo de acerto (1 para duplicidade)
    SYSTIMESTAMP,  -- Data de inclusão
    'C000000',     -- Usuário de inclusão
    'N'            -- Indicador de compensado ('N' para pendente)
);
 
 
 
 
 
=============================FESSPU19 AJUSTES VALIDAR 909 e 712 ================================
FES.FESSPU19_ROTINA_REPASSE
 
-- SELECT DA COMPENSACAO POR MANTENEDORA, NAO COMPENSADOS----eu  quero fazer essa mesma validação / sera que é necessária , ou só fazer entre a tabela 909 712 e 812
 
 
		OPEN RC1 FOR
		SELECT A.* FROM (
			SELECT T812.NU_SQNCL_COMPENSACAO_REPASSE, T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
				   T711.NU_SEQ_CANDIDATO, T711.NU_IES, T711.NU_CAMPUS, T711.VR_REPASSE,
				   T711.DT_ASSINATURA, T712.NU_TIPO_TRANSACAO, T712.NU_SQNCL_LIBERACAO_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 1 THEN NVL(T36.VR_CONTRATO, 0) ELSE 0 END AS VR_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 2 THEN NVL(T38.VR_ADITAMENTO, 0) ELSE 0 END AS VR_ADITAMENTO,
				   T812.NU_TIPO_ACERTO
			  FROM FES.FESTB812_CMPSO_RPSE_INDVO T812
			  JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO T711
			    ON T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO
			  JOIN FES.FESTB712_LIBERACAO_CONTRATO T712
			    ON T712.NU_SQNCL_LIBERACAO_CONTRATO = T711.NU_SQNCL_LIBERACAO_CONTRATO
			  LEFT JOIN FES.FESTB036_CONTRATO_FIES T36
			    ON T36.NU_CANDIDATO_FK11 = T712.NU_SEQ_CANDIDATO
			   AND T36.NU_PARTICIPACAO_FK11 = T712.NU_PARTICIPACAO_CANDIDATO
			  LEFT JOIN FES.FESTB038_ADTMO_CONTRATO T38
			    ON T38.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO
			   AND T38.NU_PARTICIPACAO_FK36 = T712.NU_PARTICIPACAO_CANDIDATO
			   AND T38.NU_SEQ_ADITAMENTO = T712.NU_SQNCL_ADITAMENTO
			 WHERE T812.IC_COMPENSADO = 'N'
			   AND T711.NU_MANTENEDORA = pNU_MANTENEDORA
			UNION ALL
			SELECT T812.NU_SQNCL_COMPENSACAO_REPASSE, T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
				   T711.NU_SEQ_CANDIDATO, T711.NU_IES, T711.NU_CAMPUS, T711.VR_REPASSE,
				   T711.DT_ASSINATURA, T712.NU_TIPO_TRANSACAO, T712.NU_SQNCL_LIBERACAO_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 1 THEN NVL(T36.VR_CONTRATO, 0) ELSE 0 END AS VR_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 2 THEN NVL(T38.VR_ADITAMENTO, 0) ELSE 0 END AS VR_ADITAMENTO,
				   T812.NU_TIPO_ACERTO
			  FROM FES.FESTB816_MIGRACAO_IES T816
			  JOIN FES.FESTB712_LIBERACAO_CONTRATO T712
			    ON T712.NU_IES = T816.NU_IES
			  JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO T711
			    ON T711.NU_SQNCL_LIBERACAO_CONTRATO = T712.NU_SQNCL_LIBERACAO_CONTRATO
			   AND T711.NU_MANTENEDORA <> T816.NU_MANTENEDORA_ADQUIRENTE -- LIBERACOES QUE FORAM REPASSADAS INCORRETAMENTE JA NA ADQUIRENTE
			  JOIN FES.FESTB812_CMPSO_RPSE_INDVO T812
			    ON T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO
			   AND T812.IC_COMPENSADO = 'N'
			  LEFT JOIN FES.FESTB036_CONTRATO_FIES T36
			    ON T36.NU_CANDIDATO_FK11 = T712.NU_SEQ_CANDIDATO
			   AND T36.NU_PARTICIPACAO_FK11 = T712.NU_PARTICIPACAO_CANDIDATO
			  LEFT JOIN FES.FESTB038_ADTMO_CONTRATO T38
			    ON T38.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO
			   AND T38.NU_PARTICIPACAO_FK36 = T712.NU_PARTICIPACAO_CANDIDATO
			   AND T38.NU_SEQ_ADITAMENTO = T712.NU_SQNCL_ADITAMENTO
			 WHERE T816.NU_MANTENEDORA_ADQUIRENTE = pNU_MANTENEDORA
			   AND T816.IC_STCO_AUTORIZACAO_MIGRACAO = '1' -- AUTORIZADA
			   AND T816.DT_FIM_AQUISICAO_IES IS NULL -- VIGENTE
			   AND T816.IC_MIGRACAO_IES ='1' -- INCORPORACAO
		) A
		ORDER BY A.VR_REPASSE;
	END IF;
 
END;
Algumas diretrizes para a compensação de valores:
1 - A dinâmica de compensação dos valores é realizada por intermédio da fesRepasse quando da apuração em moeda dos valores a serem repassados às Mantenedoras;
2 – O direcionamento dos valores, ou repasses, a serem compensados é realizado por intermédio da inserção dos sequenciais dos repasses na tabela FESTB812_CMPSO_RPSE_INDVO, destinada para este fim;
3 - Cabe ressaltar que o que se compensa são os repasses das liberações e não as liberações propriamente dita;
4 - Os valores, ou repasses, a serem compensados se resumem aos repasses realizados anteriormente, e indevidamente, às Mantenedoras;
5 - Negocialmente, um semestre contratual para um candidato contém 6 (seis) parcelas a serem repassadas à Mantenedora. Dessa forma, não deve existir mais de um repasse para cada parcela de um mesmo período/semestre;
6 - Sendo assim, as compensações geralmente se devem à recuperação de valores/repasses realizados em multiplicidade;
7 - Outra forma preconizada se deve à necessidade de correção do valor, a maior ou a menor, repassado à Mantenedora. Nessa modalidade se compensa o valor repassado anteriormente e realiza-se um novo repasse com os valores adequados.
8 – Não se deve direcionar o mesmo sequencial de repasse mais de uma vez à tabela FESTB812_CMPSO_RPSE_INDVO sob pena de compensar o mesmo repasse reiteradamente.
 
1. Entendimento do Problema – RESUMO FUNCIONAL
Você está lidando com repasses indevidos feitos a mantenedoras, causados por:

Exclusão e recriação de Aditamentos de Renovação Semestral;

Isso apagou Liberações (712) e suas Retenções (817) filhas;

A rotina de repasse (batch) gerou novas liberações e repasses, duplicando pagamentos já identificado na 909;

Foi necessário reconstruir os dados da 712 em uma tabela nova (909), comparando com a 47 (auditoria), para identificar duplicidade.

✅ 2. Validação do Seu Entendimento
Você escreveu:
“Antes de criar novo Aditamento, o sistema removia as liberações e retenções vinculadas ao aditamento anterior.”

✔ Correto. Esse processo é comum para evitar dados órfãos, mas a lógica falhou ao não prevenir duplicidade de repasses.

Você escreveu:
“A rotina agendada identificava que os registros da 711 (analítico) não estavam na 812 (compensação), então criava novo repasse.”

✔ Correto. A 711 registra o extrato analítico, e a falta de correspondência com a 812 implica que nenhuma compensação foi feita, levando à criação de novo repasse — duplicando o pagamento.

Você escreveu:
“Agora preciso criar uma procedure para compensar valores já pagos, estornando parcelas futuras.”

✔ A lógica faz sentido. Você precisa:

Detectar as parcelas pagas em duplicidade (base 909);

Compensar isso nas próximas liberações/repasses;

Ou aplicar um estorno, ajustando o histórico e evitando novo pagamento.

======= É ESSENCIAL ENTENDER AQUI================

Ciclo do Aditamento:
Um Aditamento (Renovação Semestral) gera Liberações (712);

Cada Liberação gera Retenções (817) e depois Repasses (via Batch com ajustes SPs Jair);

O Analítico (711) é um espelho da liberação/repasses realizados;

A Compensação (812) é uma forma de corrigir pagamentos indevidos;

Se não houver referência na 812, a rotina supõe que ainda deve repassar.

💡 PROPOSTA DE SOLUÇÃO – Técnica e Funcional - seria via SP a principio FESSPZ55_CRISE2019_TRATA_SUSP
usando como base as consultas, já que em uma das sua etapas

✅ 1. Detectar Duplicidades como base os dados que já estão na  (base: tabela 909)
Use os dados recuperados da 909 para saber:

essas informações podemos pegar da 909 -- mas precisamos comparar para não inserir duplicação de chave na 812 não pode haver doi sequenciais iguais :
NU_SQNCL_LIBERACAO_CONTRATO 
Qual NU_SQNCL_LIBERACAO_CONTRATO foi recriado;

Qual parcela (NU_PARCELA), valor (VR_REPASSE), e contrato (NU_CONTRATO) já foi pago duas vezes;

Você pode gerar uma tabela temporária (ex: TMP_LIB_DUPLICADA) com:


NU_CONTRATO | NU_PARCELA | VR_REPASSE_DUPLICADO | DT_PAGAMENTO_ORIGINAL | DT_PAGAMENTO_DUPLICADO

2. Criar a lógica de compensação
Opção A – Compensar próximo repasse
Ao rodar o batch de repasse:



Verificar se o contrato/parcela existe na 909 e na 812(verificar se é a mesma chave  );

Se sim, calcular VR_COMPENSACAO = VR_REPASSE_DUPLICADO;

Inserir o NU_SEQ_REPASSE correspondente na FESTB812_CMPSO_RPSE_INDVO (compensação individual);

O batch vai considerar isso e abater o valor da próxima liberação automaticamente. ou simplesmente cancelar o repasse da próxima  parcela se equivale a o valor 
==================================================
oque eu preciso validar é quais informações eu preciso para inserir na 812, porque as vezes são colunas e dados vindos de tabelas diferentes  

essa tabela aqui tb711 é histórico de repasses feitos:
tb711.NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_CAMPUS,NU_TIPO_TRANSACAO,NU_MANTENEDORA,NU_IES,NU_SEQ_CANDIDATO,MM_REFERENCIA,AA_REFERENCIA,VR_REPASSE,DT_ASSINATURA,
NU_SQNCL_LIBERACAO_CONTRATO,TS_APURACAO_RELATORIO,NU_SQNCL_CTRTO_ANLTO_CMPNO

Essa tabela é consultada para liberação do repasse ou parcela do repasse 
tb.712 liberação----onde foi deletado  Nu sequencial foi deletado de determinado contrato fazendo que criada uma novo repasse 
DT_INCLUSAO ,DT_LIBERACAO,IC_APTO_LIBERACAO_REPASSE,IC_SITUACAO_LIBERACAO
MM_REFERENCIA_LIBERACAO,NU_CAMPUS,NU_IES,NU_MANTENEDORANU_PARCELA,NU_PARTICIPACAO_CANDIDATO,NU_SEQ_CANDIDATO,NU_SQNCL_ADITAMENTO,NU_SQNCL_LIBERACAO_CONTRATO,NU_TIPO_ACERTO,NU_TIPO_TRANSACAO,VR_REPASSE

essa tabela foi criada porque algumas informações da 712 foi apagada mas foi recuperado o numeroSequencial e informações e feito essa tabela , que deverar ter as informações que são inseridas na 812.
tb.909----Nova criada contem oque foi apagado que inclui a diferencça entre auditoria agora tem eles , tem apenas dados que foram deletados e publico a ser descontado a compensação
DT_INCLUSAO ,DT_LIBERACAO,IC_APTO_LIBERACAO_REPASSE,IC_SITUACAO_LIBERACAO
MM_REFERENCIA_LIBERACAO,NU_CAMPUS,NU_IES,NU_MANTENEDORANU_PARCELA,NU_PARTICIPACAO_CANDIDATO,NU_SEQ_CANDIDATO,NU_SQNCL_ADITAMENTO,NU_SQNCL_LIBERACAO_CONTRATO,NU_TIPO_ACERTO,NU_TIPO_TRANSACAO,VR_REPASSE

------os dados serão inserids nessa tabela que é a da compensação: essa tabela contem os dados de repasses indevidos ( como foi nosso caso ) as informações que estão nela é consultado em outro processo do repasse , e quando os dados estão ai é feito a compesação de repasse indevido 
tb.812-contem os repasses indevidos e deve ser inserido os dados da 909 ( Nusequencial )para ser compensado
 NU_SQNCL_COMPENSACAO_REPASSE,NU_SQNCL_RLTRO_CTRTO_ANALITICO,NU_TIPO_ACERTO,
TS_INCLUSAO,CO_USUARIO_INCLUSAO,IC_COMPENSADO

                                      |-----Estorno
                                      |-----Compensação(812)
                                   |Repasses
contrato(36)---Aditamentos(038)--Liberações(712) ---------Analítico(711)----sintético(710)
                 |---Auditoria (047)  |---retenções (817)

 Agora eu quero criar uma Sp que vamos criar para inserir na 812 , além de todas as validações se for necessária antes de fazer o insert desses repasses indevidos para que depois seja feito a compensação , então veja essa rotina tem a intenção de identificar as parcelas repassadas e inseirir essas mesmas na 812 para compesação, sobservando que não pode haver chave duplicada  :

FESSPZ67_CRISE2025_COMPENSA_DUPLCD

NESSE TRECHO DESSA SP ELE JÁ IDENTIFICA OS REPASSES QUE AINDA NÃO OCORREMA NAS TABELAS MENCIONADAS 
Pode ser ultil para entender  as validações 

 FES.FESSPU19_ROTINA_REPASSE

-- SELECT DA COMPENSACAO POR MANTENEDORA, NAO COMPENSADOS----eu  quero fazer essa mesma validação / sera que é necessária , ou só fazer entre a tabela 909 712 e 812


		OPEN RC1 FOR
		SELECT A.* FROM (
			SELECT T812.NU_SQNCL_COMPENSACAO_REPASSE, T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
				   T711.NU_SEQ_CANDIDATO, T711.NU_IES, T711.NU_CAMPUS, T711.VR_REPASSE,
				   T711.DT_ASSINATURA, T712.NU_TIPO_TRANSACAO, T712.NU_SQNCL_LIBERACAO_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 1 THEN NVL(T36.VR_CONTRATO, 0) ELSE 0 END AS VR_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 2 THEN NVL(T38.VR_ADITAMENTO, 0) ELSE 0 END AS VR_ADITAMENTO,
				   T812.NU_TIPO_ACERTO
			  FROM FES.FESTB812_CMPSO_RPSE_INDVO T812
			  JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO T711
			    ON T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO
			  JOIN FES.FESTB712_LIBERACAO_CONTRATO T712
			    ON T712.NU_SQNCL_LIBERACAO_CONTRATO = T711.NU_SQNCL_LIBERACAO_CONTRATO
			  LEFT JOIN FES.FESTB036_CONTRATO_FIES T36
			    ON T36.NU_CANDIDATO_FK11 = T712.NU_SEQ_CANDIDATO
			   AND T36.NU_PARTICIPACAO_FK11 = T712.NU_PARTICIPACAO_CANDIDATO
			  LEFT JOIN FES.FESTB038_ADTMO_CONTRATO T38
			    ON T38.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO
			   AND T38.NU_PARTICIPACAO_FK36 = T712.NU_PARTICIPACAO_CANDIDATO
			   AND T38.NU_SEQ_ADITAMENTO = T712.NU_SQNCL_ADITAMENTO
			 WHERE T812.IC_COMPENSADO = 'N'
			   AND T711.NU_MANTENEDORA = pNU_MANTENEDORA
			UNION ALL
			SELECT T812.NU_SQNCL_COMPENSACAO_REPASSE, T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
				   T711.NU_SEQ_CANDIDATO, T711.NU_IES, T711.NU_CAMPUS, T711.VR_REPASSE,
				   T711.DT_ASSINATURA, T712.NU_TIPO_TRANSACAO, T712.NU_SQNCL_LIBERACAO_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 1 THEN NVL(T36.VR_CONTRATO, 0) ELSE 0 END AS VR_CONTRATO,
				   CASE WHEN T712.NU_TIPO_TRANSACAO = 2 THEN NVL(T38.VR_ADITAMENTO, 0) ELSE 0 END AS VR_ADITAMENTO,
				   T812.NU_TIPO_ACERTO
			  FROM FES.FESTB816_MIGRACAO_IES T816
			  JOIN FES.FESTB712_LIBERACAO_CONTRATO T712
			    ON T712.NU_IES = T816.NU_IES
			  JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO T711
			    ON T711.NU_SQNCL_LIBERACAO_CONTRATO = T712.NU_SQNCL_LIBERACAO_CONTRATO
			   AND T711.NU_MANTENEDORA <> T816.NU_MANTENEDORA_ADQUIRENTE -- LIBERACOES QUE FORAM REPASSADAS INCORRETAMENTE JA NA ADQUIRENTE
			  JOIN FES.FESTB812_CMPSO_RPSE_INDVO T812
			    ON T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO
			   AND T812.IC_COMPENSADO = 'N'
			  LEFT JOIN FES.FESTB036_CONTRATO_FIES T36
			    ON T36.NU_CANDIDATO_FK11 = T712.NU_SEQ_CANDIDATO
			   AND T36.NU_PARTICIPACAO_FK11 = T712.NU_PARTICIPACAO_CANDIDATO
			  LEFT JOIN FES.FESTB038_ADTMO_CONTRATO T38
			    ON T38.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO
			   AND T38.NU_PARTICIPACAO_FK36 = T712.NU_PARTICIPACAO_CANDIDATO
			   AND T38.NU_SEQ_ADITAMENTO = T712.NU_SQNCL_ADITAMENTO
			 WHERE T816.NU_MANTENEDORA_ADQUIRENTE = pNU_MANTENEDORA
			   AND T816.IC_STCO_AUTORIZACAO_MIGRACAO = '1' -- AUTORIZADA
			   AND T816.DT_FIM_AQUISICAO_IES IS NULL -- VIGENTE
			   AND T816.IC_MIGRACAO_IES ='1' -- INCORPORACAO
		) A
		ORDER BY A.VR_REPASSE;
	END IF;

END;
----------------------------------------------------------------------


=======================================================Sp base ========================================
CREATE OR REPLACE PROCEDURE FESSPZ67_CRISE2025_COMPENSA_DUPLCD (
    p_co_usuario_execucao IN VARCHAR2 -- Usuário que executa a SP, e.g., 'SP_FES_CRISIS'
)
AS
    -- Variáveis locais
    v_nu_sqncl_rl_analitico_duplicado NUMBER;
    v_existe_na_812                   NUMBER;

    -- Cursor para iterar sobre os registros já identificados na 909
    -- As colunas selecionadas devem refletir exatamente o que está na sua FESTB909_RECOMP_712.
    -- Certifique-se de que NU_SEQ_CANDIDATO na 909 corresponde ao NU_SEQ_CANDIDATO na 711.
    CURSOR c_duplicidades IS
        SELECT
            T909.NU_SQNCL_LIBERACAO_CONTRATO,
            T909.NU_SEQ_CANDIDATO,          -- Assumindo que este é o campo de contrato na 909 (como na 711)
            T909.NU_IES,
            T909.NU_CAMPUS,
            T909.MM_REFERENCIA_LIBERACAO AS MM_REFERENCIA, -- Alias para casar com a 711
            T909.AA_REFERENCIA_LIBERACAO AS AA_REFERENCIA, -- Alias para casar com a 711
            T909.NU_PARCELA,
            T909.VR_REPASSE,                -- O VR_REPASSE da liberação duplicada
            -- Se a 909 tiver uma coluna de timestamp que indique o momento da duplicidade,
            -- inclua-a aqui para ajudar a filtrar na 711. Ex: T909.DT_OCORRENCIA_DUPLICIDADE
            T909.DT_INCLUSAO AS DT_DUPLICIDADE_909 -- Usando DT_INCLUSAO da 909 como possível critério de data
        FROM FES.FESTB909_RECOMP_712 T909;

    -- ** Importante: CONFIRMAR O VALOR CORRETO PARA O NU_TIPO_ACERTO **
    -- Com base nas suas amostras da 812, o valor '1' foi usado.
    -- Precisamos confirmar se '1' significa "Compensação por Repasse Indevido/Duplicação".
    -- Se não for '1' para o seu caso específico, substitua pelo valor correto.
    v_nu_tipo_acerto_compensacao CONSTANT NUMBER := 1; -- ** VALOR A SER VALIDADO COM O NEGÓCIO! **

    -- Assumindo o nome da sequence, **CONFIRMAR ESSE NOME**
    v_seq_compensacao_repasses VARCHAR2(30) := 'SEQ_FESTB812_CMPSO_RPSE_INDVO'; -- Exemplo: FES.FESTB812_SQNCL

BEGIN
    -- Habilita a saída de mensagens para debug
    DBMS_OUTPUT.ENABLE(NULL);
    DBMS_OUTPUT.PUT_LINE('Início da FESSPZ67_CRISE2025_COMPENSA_DUPLCD em ' || TO_CHAR(SYSTIMESTAMP, 'DD-MON-YYYY HH24:MI:SS'));

    FOR r_duplicidade IN c_duplicidades LOOP
        v_nu_sqncl_rl_analitico_duplicado := NULL; -- Reset para cada iteração
        v_existe_na_812 := 0;

        -- 1. Tentar encontrar o NU_SQNCL_RLTRO_CTRTO_ANALITICO correspondente na FESTB711
        BEGIN
            SELECT T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO
            INTO v_nu_sqncl_rl_analitico_duplicado
            FROM FES.FESTB711_RLTRO_CTRTO_ANLTO T711
            WHERE T711.NU_SQNCL_LIBERACAO_CONTRATO = r_duplicidade.NU_SQNCL_LIBERACAO_CONTRATO
            AND T711.NU_SEQ_CANDIDATO = r_duplicidade.NU_SEQ_CANDIDATO
            AND T711.NU_IES = r_duplicidade.NU_IES
            AND T711.NU_CAMPUS = r_duplicidade.NU_CAMPUS
            AND T711.MM_REFERENCIA = r_duplicidade.MM_REFERENCIA
            AND T711.AA_REFERENCIA = r_duplicidade.AA_REFERENCIA
            AND T711.NU_PARCELA = r_duplicidade.NU_PARCELA
            AND T711.VR_REPASSE = r_duplicidade.VR_REPASSE -- Adicionado para maior especificidade
            -- ** Critério para pegar o registro DUPLICADO na 711 **
            -- Assumindo que o repasse duplicado é o MAIS RECENTE para o mesmo conjunto de dados
            -- A 909 deve conter o NU_SQNCL_LIBERACAO_CONTRATO da liberação que *gerou* o repasse duplicado.
            -- Se múltiplos registros na 711 correspondem, a ordenação abaixo pega o mais recente.
            ORDER BY T711.TS_APURACAO_RELATORIO DESC -- O timestamp de apuração da 711
            FETCH FIRST 1 ROW ONLY; -- Para Oracle 12c+. Para 11g, use ROWNUM = 1

        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                DBMS_OUTPUT.PUT_LINE('ALERTA: Não encontrado NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB711 para liberação da 909 (ID: ' || r_duplicidade.NU_SQNCL_LIBERACAO_CONTRATO || ', Contrato: ' || r_duplicidade.NU_SEQ_CANDIDATO || ', Parcela: ' || r_duplicidade.NU_PARCELA || ').');
                CONTINUE; -- Pula para a próxima duplicidade
            WHEN TOO_MANY_ROWS THEN
                -- Isso indica que mesmo com todos os critérios, ainda há mais de um registro.
                -- A cláusula FETCH FIRST 1 ROW ONLY já resolve isso pegando o mais recente.
                -- Mantemos o alerta para ciência, caso haja necessidade de refinar o critério de busca.
                DBMS_OUTPUT.PUT_LINE('AVISO: Múltiplos NU_SQNCL_RLTRO_CTRTO_ANALITICO encontrados na FESTB711 para liberação da 909 (ID: ' || r_duplicidade.NU_SQNCL_LIBERACAO_CONTRATO || ', Contrato: ' || r_duplicidade.NU_SEQ_CANDIDATO || ', Parcela: ' || r_duplicidade.NU_PARCELA || '). Selecionando o mais recente.');
        END;

        -- Se encontramos um NU_SQNCL_RLTRO_CTRTO_ANALITICO na 711
        IF v_nu_sqncl_rl_analitico_duplicado IS NOT NULL THEN
            -- 2. Verificar se o sequencial já existe na FESTB812 (Diretriz 8)
            SELECT COUNT(1)
            INTO v_existe_na_812
            FROM FES.FESTB812_CMPSO_RPSE_INDVO
            WHERE NU_SQNCL_RLTRO_CTRTO_ANALITICO = v_nu_sqncl_rl_analitico_duplicado;

            IF v_existe_na_812 = 0 THEN
                -- 3. Inserir na FESTB812
                -- A geração do NU_SQNCL_COMPENSACAO_REPASSE via sequence é comum.
                -- Confirme o nome da sequence real para o seu ambiente.
                INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (
                    NU_SQNCL_COMPENSACAO_REPASSE,
                    NU_SQNCL_RLTRO_CTRTO_ANALITICO,
                    NU_TIPO_ACERTO,
                    TS_INCLUSAO,
                    CO_USUARIO_INCLUSAO,
                    IC_COMPENSADO
                ) VALUES (
                    -- Para obter o próximo valor de uma sequence: <NOME_DA_SEQUENCE>.NEXTVAL
                    -- Exemplo: FES.SQ_FESTB812_CMP_RPSE_INDVO.NEXTVAL
                    -- ** CONFIRMAR O NOME DA SEQUENCE AQUI **
                    (SELECT FES.SQ_FESTB812_CMPSO_RPSE_INDVO.NEXTVAL FROM DUAL), -- Exemplo comum em Oracle
                    v_nu_sqncl_rl_analitico_duplicado,
                    v_nu_tipo_acerto_compensacao, -- Usar o valor de '1' (a ser confirmado)
                    SYSTIMESTAMP,
                    p_co_usuario_execucao,
                    'N'
                );
                DBMS_OUTPUT.PUT_LINE('SUCESSO: Inserido NU_SQNCL_RLTRO_CTRTO_ANALITICO ' || v_nu_sqncl_rl_analitico_duplicado || ' na FESTB812 para compensação.');
            ELSE
                DBMS_OUTPUT.PUT_LINE('AVISO: NU_SQNCL_RLTRO_CTRTO_ANALITICO ' || v_nu_sqncl_rl_analitico_duplicado || ' já existe na FESTB812. Ignorando inserção (Diretriz 8).');
            END IF;
        END IF;

    END LOOP;

    COMMIT; -- Confirma todas as inserções bem-sucedidas
    DBMS_OUTPUT.PUT_LINE('Fim da FESSPZ67_CRISE2025_COMPENSA_DUPLCD. Processo concluído com sucesso.');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK; -- Em caso de qualquer erro, desfaz todas as operações
        DBMS_OUTPUT.PUT_LINE('ERRO FATAL na FESSPZ67_CRISE2025_COMPENSA_DUPLCD: ' || SQLERRM);
        RAISE; -- Re-lança o erro para o chamador
END;
-----------------------------------------------------

Sugestão critica :
SELECT T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO
INTO v_nu_sqncl_rl_analitico_duplicado
FROM FES.FESTB711_RLTRO_CTRTO_ANLTO T711
WHERE T711.NU_SQNCL_LIBERACAO_CONTRATO = r_duplicidade.NU_SQNCL_LIBERACAO_CONTRATO -- Use o ID da 909
AND T711.NU_SEQ_CANDIDATO = r_duplicidade.NU_SEQ_CANDIDATO -- OU NU_CONTRATO se for o caso
AND T711.NU_IES = r_duplicidade.NU_IES
AND T711.NU_CAMPUS = r_duplicidade.NU_CAMPUS
AND T711.MM_REFERENCIA = r_duplicidade.MM_REFERENCIA_LIBERACAO
AND T711.AA_REFERENCIA = r_duplicidade.AA_REFERENCIA_LIBERACAO
AND T711.NU_PARCELA = r_duplicidade.NU_PARCELA
-- E, para diferenciar se houver mais de um registro na 711 para o mesmo contrato/parcela:
-- ORDER BY T711.TS_APURACAO_RELATORIO DESC -- Assume que o duplicado é o mais recente
-- FETCH FIRST 1 ROW ONLY; -- Para Oracle 12c+
-- Ou ROWNUM = 1 para Oracle 11g-

--------------------------
Pontos Cruciais para Você Validar Antes de Executar:

    NU_TIPO_ACERTO (Valor 1):
        Você viu que nas amostras da FESTB812 o NU_TIPO_ACERTO é 1. É fundamental confirmar com os gestores ou documentação se 1 realmente significa "Compensação de Repasse Indevido por Duplicação" para o seu cenário. Se não for, a SP vai funcionar, mas a categorização da compensação estará errada.

    Nome da Sequence (NU_SQNCL_COMPENSACAO_REPASSE):
        No INSERT, eu usei (SELECT FES.SQ_FESTB812_CMPSO_RPSE_INDVO.NEXTVAL FROM DUAL). Você precisa substituir FES.SQ_FESTB812_CMPSO_RPSE_INDVO pelo nome exato da SEQUENCE no seu banco de dados que é usada para gerar o NU_SQNCL_COMPENSACAO_REPASSE da tabela FESTB812_CMPSO_RPSE_INDVO. Se você não souber, procure um DBA ou em scripts de criação da tabela.

    Dados da FESTB909_RECOMP_712:
        A SP agora espera que a FESTB909 tenha as colunas: NU_SQNCL_LIBERACAO_CONTRATO, NU_SEQ_CANDIDATO, NU_IES, NU_CAMPUS, MM_REFERENCIA_LIBERACAO, AA_REFERENCIA_LIBERACAO, NU_PARCELA, VR_REPASSE, DT_INCLUSAO.
        Quando a FESTB909 for populada, é crucial que ela contenha os NU_SQNCL_LIBERACAO_CONTRATO das liberações que geraram o repasse duplicado na FESTB711.

    Critério ORDER BY T711.TS_APURACAO_RELATORIO DESC FETCH FIRST 1 ROW ONLY:
        Esta é a suposição de que o registro do repasse duplicado na FESTB711 é o mais recente para aquele conjunto de critérios (contrato, parcela, etc.). É a lógica mais comum para identificar o "duplicado" se houver múltiplos registros. Mas sempre é bom validar essa premissa com os dados reais ou com os gestores, se houver um critério mais específico.

Estamos no caminho certo! Com as confirmações do NU_TIPO_ACERTO e da SEQUENCE, a SP estará pronta para um teste real.
=========================================================================================================================================

================================Amostragem de dados======================================================================================
FESTB712_LIBERACAO_CONTRATO
colunas:
NU_SQNCL_LIBERACAO_CONTRATO	NU_SEQ_CANDIDATO	NU_MANTENEDORA	NU_IES	NU_CAMPUS	NU_PARCELA	MM_REFERENCIA_LIBERACAO	AA_REFERENCIA_LIBERACAO	VR_REPASSE	IC_SITUACAO_LIBERACAO	DT_LIBERACAO	DT_INCLUSAO	DT_ATUALIZACAO	NU_PARTICIPACAO_CANDIDATO	NU_SQNCL_ADITAMENTO	NU_TIPO_TRANSACAO	NU_TIPO_ACERTO	IC_APTO_LIBERACAO_REPASSE
Dados1:
863	20000001	1965	3034	1061025	3	3	2018	1325.82	S 	2018-03-15 00:00:00.000	2018-03-15 15:01:20.000	2025-06-03 16:50:12.000	1		1		S
Dados2:
1288	20000084	2776	322	2643	2	2	2018	659.69	E 	2018-10-01 00:00:00.000	2018-03-15 15:08:07.000	2021-03-05 14:15:04.000	1		1		S
FESTB711_RLTRO_CTRTO_ANLTO:
COLUNAS :
NU_SQNCL_RLTRO_CTRTO_ANALITICO	NU_CAMPUS	NU_TIPO_TRANSACAO	NU_MANTENEDORA	NU_IES	NU_SEQ_CANDIDATO	MM_REFERENCIA	AA_REFERENCIA	VR_REPASSE
	DT_ASSINATURA	NU_SQNCL_LIBERACAO_CONTRATO	TS_APURACAO_RELATORIO	NU_SQNCL_CTRTO_ANLTO_CMPNO
Dados1:
27347,	600114,	1,	15990,	1292,	20000010,	5,	2019,	1417.8,	2018-04-09 00:00:00.000,	910,	2019-12-07 20:06:00.633,	27347 

Dados2:
27354	658468	1	656	1422	20000019	11	2019	7261.76	2018-01-29 00:00:00.000	935	2019-12-07 20:06:00.842	 null
 
FES.FESTB812_CMPSO_RPSE_INDVO:
NU_SQNCL_COMPENSACAO_REPASSE,	NU_SQNCL_RLTRO_CTRTO_ANALITICO,	NU_TIPO_ACERTO	TS_INCLUSAO,	CO_USUARIO_INCLUSAO,	IC_COMPENSADO
Dados:
14 ,27396,	1,	2020-12-09 18:10:26.000,	C000000,	S
outro :
19,	24249	,1,	2020-12-09 18:10:26.000,	C000000,	N

============================================Processo Batch questão =======================================================================================
Analise de como é feito a apuração a compensar :

private void apurarAnaliticoCompensacao() {

		HashMap<Long, Integer> mapConvenio = Utils.converteListaConvenioToMap(consultarPercentualCompensacaoMantenedoras());
		List<SomatorioContratacaoAnaliticoTO> somatorioMantenedoras = consultarSomatorioAnalitico();

		for (SomatorioContratacaoAnaliticoTO somatorio: somatorioMantenedoras) {
			BigDecimal valorRepasse = somatorio.getValorRepasse();
			Long nuMantenedora = somatorio.getNuMantenedora();

			if (valorRepasse.compareTo(BigDecimal.ZERO) <= 0) {
				logger.debug("apurarAnaliticoCompensacao, mantendora {}, valor de repasse menor ou igual a zero, não possui saldo para compensar! ", nuMantenedora);
				continue;
			}

			// recupera o percentual maximo a compensar
			BigDecimal percentualCompensacao = new BigDecimal( mapConvenio.getOrDefault(nuMantenedora, PERCENTUAL_DEFAULT_CONVENIO) );

			// calcula valores liquidos da mantenedora
			ApuracaoRepasseTO apuracaoRepasseTO = getApuracaoRepasse(new RelatorioContratacaoSinteticoTO(somatorio, mesReferencia, anoReferencia));

			inserirAnaliticoCompensacaoMantenedora(nuMantenedora, valorRepasse, percentualCompensacao, apuracaoRepasseTO);
		}

	}

-------------------->
@Entity
@Table(name = "ConsultaCompesacaoMantenedoraTO")
@NamedNativeQuery(name = ConsultaCompesacaoMantenedoraTO.QUERY_CONSULTA_MANTENEDORA, query = "{call FES.FESSPU19_ROTINA_REPASSE( ?, :1, NULL, NULL, :2 ) }",
				  resultClass = ConsultaCompesacaoMantenedoraTO.class, hints = { @javax.persistence.QueryHint(name = "org.hibernate.callable", value = "true") })
public class ConsultaCompesacaoMantenedoraTO implements Serializable {

	public static final String QUERY_CONSULTA_MANTENEDORA = "ConsultaCompesacaoMantenedoraTO.consultaMantenedora";
	public static final int OPCAO_CONS_COMPENSACAO_MANTENEDORA = 2;

	private static final long serialVersionUID = -979381930813632391L;
	
	@Id
	@Column(name = "NU_SQNCL_COMPENSACAO_REPASSE")
	private Long numeroCompensacao;
	
	@Column(name = "NU_SQNCL_RLTRO_CTRTO_ANALITICO")
	private Long numeroAnaliticoACompensar;
	
	@Column(name = "NU_SQNCL_LIBERACAO_CONTRATO")
	private Long numeroLiberacao;
	
	@Column(name = "NU_IES")
	private Long nuIES;
	
	@Column(name = "NU_CAMPUS")
	private Long nuCampus;
	
	@Column(name = "NU_SEQ_CANDIDATO")
	private Long nuCandidato;
	
	@Column(name = "VR_REPASSE")
	private BigDecimal valorRepasse;
	
	@Column(name = "VR_CONTRATO")
	private BigDecimal valorContrato;
	
	@Column(name = "DT_ASSINATURA")
	private Date dataAssinatura;
	
	@Column(name = "VR_ADITAMENTO")
	private BigDecimal valorAditamento;
	
	@Column(name = "NU_TIPO_TRANSACAO")
	private Integer numeroTipoTransacao;
	
	@Column(name = "NU_TIPO_ACERTO")
	private Integer numeroTipoAcerto;
	
	-------------------------->
@SuppressWarnings("unchecked")
	private List<SomatorioContratacaoAnaliticoTO> consultarSomatorioAnalitico() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T711.NU_MANTENEDORA, T156.NO_MANTENEDORA, T156.SG_UF_FK08, ");
		sb.append("       SUM(CASE WHEN T711.NU_SQNCL_CTRTO_ANLTO_CMPNO IS NULL THEN NVL(T711.VR_REPASSE,0) ELSE 0 END) AS VR_REPASSE, ");
		sb.append("       COUNT(1) AS QT_REPASSE, ");
		sb.append("       SUM(CASE WHEN T712.NU_TIPO_TRANSACAO = 1 THEN NVL(T36.VR_CONTRATO,0) ELSE 0 END) AS VR_CONTRATO, ");
		sb.append("       COUNT(CASE WHEN T712.NU_TIPO_TRANSACAO = 1 THEN 1 END) AS QT_CONTRATO, ");
		sb.append("       SUM(CASE WHEN T712.NU_TIPO_TRANSACAO = 2 THEN NVL(T38.VR_ADITAMENTO,0) ELSE 0 END) AS VR_ADITAMENTO, ");
		sb.append("       COUNT(CASE WHEN T712.NU_TIPO_TRANSACAO = 2 THEN 1 END) AS QT_ADITAMENTO, ");
		sb.append("       SUM(CASE WHEN T711.NU_SQNCL_CTRTO_ANLTO_CMPNO IS NOT NULL THEN NVL(T711.VR_REPASSE,0) ELSE 0 END) AS VR_COMPENSACAO ");
		sb.append("  FROM FES.FESTB711_RLTRO_CTRTO_ANLTO T711 ");
		sb.append("  JOIN FES.FESTB156_MANTENEDORA_INEP T156 ");
		sb.append("    ON T156.NU_MANTENEDORA = T711.NU_MANTENEDORA ");
		sb.append("  JOIN FES.FESTB712_LIBERACAO_CONTRATO T712 ");
		sb.append("    ON T712.NU_SQNCL_LIBERACAO_CONTRATO = T711.NU_SQNCL_LIBERACAO_CONTRATO ");
		sb.append("  LEFT JOIN FES.FESTB036_CONTRATO_FIES T36 ");
		sb.append("    ON T36.NU_CANDIDATO_FK11 = T712.NU_SEQ_CANDIDATO ");
		sb.append("   AND T36.NU_PARTICIPACAO_FK11 = T712.NU_PARTICIPACAO_CANDIDATO ");
		sb.append("  LEFT JOIN FES.FESTB038_ADTMO_CONTRATO T38 ");
		sb.append("    ON T38.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO ");
		sb.append("   AND T38.NU_PARTICIPACAO_FK36 = T712.NU_PARTICIPACAO_CANDIDATO ");
		sb.append("   AND T38.NU_SEQ_ADITAMENTO = T712.NU_SQNCL_ADITAMENTO ");
		sb.append(" WHERE T711.MM_REFERENCIA = :mes ");
		sb.append("   AND T711.AA_REFERENCIA = :ano ");
		sb.append(" GROUP BY T711.NU_MANTENEDORA, T156.NO_MANTENEDORA, T156.SG_UF_FK08 ");
		sb.append(" ORDER BY T711.NU_MANTENEDORA ");

		Query qr = entityManager.createNativeQuery(sb.toString());
		qr.setParameter("mes", this.mesReferencia);
		qr.setParameter("ano", this.anoReferencia);

		List<SomatorioContratacaoAnaliticoTO> listaRetorno = new ArrayList<>();
		List<Object[]> list = qr.getResultList();
		for (Object[] objArray: list) {
			listaRetorno.add( new SomatorioContratacaoAnaliticoTO(objArray) );
		}

		return listaRetorno;
	}
	--------------------->
 private void inserirAnaliticoCompensacaoMantenedora(Long nuMantenedora, BigDecimal valorRepasseBruto, BigDecimal percentualCompensacao, ApuracaoRepasseTO apuracaoRepasseTO) {
		List<ConsultaCompesacaoMantenedoraTO> listCompensarMantenedora = consultaCompensacoesMantenedora(nuMantenedora);
		BigDecimal valorCompensado = new BigDecimal(0);
		
		if (listCompensarMantenedora.isEmpty()) {
			logger.debug("inserirAnaliticoCompensacaoMantenedora - mantenedora {} nao possui nada para compensar!", nuMantenedora);
			return;
		}

		BigDecimal valorACompensarBruto = valorRepasseBruto.multiply(percentualCompensacao).divide(new BigDecimal(100));

		BigDecimal valorRepasseLiquido = apuracaoRepasseTO.getValorRepasse();
		BigDecimal valorACompensarLiquido = valorRepasseLiquido.multiply(percentualCompensacao).divide(new BigDecimal(100));

		logger.debug("apurarAnaliticoCompensacao, mantendora {}, valor maximo a compensar bruto {}, liquido {}", nuMantenedora, valorACompensarBruto, valorACompensarLiquido);

		BigDecimal valorIntegralizacaoRepasse = apuracaoRepasseTO.getValorIntegralizacao();
		BigDecimal valorIntegralizacaoTotalCompensacao = BigDecimal.ZERO;

		for (ConsultaCompesacaoMantenedoraTO analiticoCompensarTO : listCompensarMantenedora) {
			String log = ""; // se der exceção, mostra detalhe da liberacao em q ocorreu o erro
			
			try {
				// busca o valor da integralizacao a devolver do compensacao atual
				BigDecimal valorIntegralizacaoCompensacao = consultarIntegralizacaoDevolverAnalitico(analiticoCompensarTO.getNumeroAnaliticoACompensar());
				BigDecimal valorCompensadoLiquido = valorCompensado.add(analiticoCompensarTO.getValorRepasse());
				BigDecimal valorTaxaAdmIntegralizacao = valorCompensadoLiquido.multiply(TAXA_DEFAULT);
				valorCompensadoLiquido = valorCompensadoLiquido.subtract(valorIntegralizacaoCompensacao).subtract(valorTaxaAdmIntegralizacao);

				//se o valor compensado mais o valor de repasse do proximo candidato superar o valor a compensar
				// ou o valor total de integralizacao mais o valor de integralizacao da proxima compensacao superar o valor da intregralizacao do repasse
				// ou o valor compensado liquido superar o valor a compensar liquido
				// entao encerra o processamento
				if (valorCompensado.add(analiticoCompensarTO.getValorRepasse()).compareTo(valorACompensarBruto) > 0 ||
					valorIntegralizacaoTotalCompensacao.add(valorIntegralizacaoCompensacao).compareTo(valorIntegralizacaoRepasse) > 0 ||
					valorCompensadoLiquido.compareTo(valorACompensarLiquido) > 0) {
					break;
				}

				transacaoService.inserirAnaliticoCompensacaoMantenedora(nuMantenedora, analiticoCompensarTO, this.mesReferencia, this.anoReferencia, log);
				valorCompensado = valorCompensado.add(analiticoCompensarTO.getValorRepasse());
				valorIntegralizacaoTotalCompensacao = valorIntegralizacaoTotalCompensacao.add(valorIntegralizacaoCompensacao);
			} catch(Exception e) {
				logger.warn("{} exception: {}", log, e.getMessage(), e);
			}
		}
	}

 ==========================LOgicas das SPs===========================================================================================================
 Sua lógica está quase perfeita e aponta para o cerne do problema! Você está pensando de forma muito acertada sobre a persistência dos dados e como as SPs se relacionam.

Análise da Sua Lógica e Próximos Passos

Vamos analisar sua proposta ponto a ponto:

Sua SP FESSPZ67: O Papel de Alimentar a FESTB812

    Validação 909 e 711 para 812: Você está totalmente correto ao afirmar que sua FESSPZ67 deve ser responsável por comparar os dados da FESTB909 com a FESTB711 para identificar os NU_SQNCL_RLTRO_CTRTO_ANALITICO dos repasses a serem compensados e inseri-los na FESTB812. Este é o objetivo principal da sua SP e ela está bem encaminhada para isso.

O Cenário do NU_SQNCL_LIBERACAO_CONTRATO Apagado

Aqui é onde a sua percepção é crucial:

    Se o NU_SQNCL_LIBERACAO_CONTRATO que gerou o repasse duplicado (identificado na FESTB909) foi apagado da FESTB712 e agora só existe na FESTB909 como referência de um problema:

        Então, a FESSPU19_ROTINA_REPASSE, ao tentar fazer o JOIN de FESTB812 -> FESTB711 -> FESTB712, terá problemas para encontrar os detalhes da liberação na FESTB712.

        A FESSPU19 precisa desses detalhes (NU_IES, NU_CAMPUS, NU_SEQ_CANDIDATO, NU_TIPO_TRANSACAO, VR_CONTRATO, VR_ADITAMENTO, etc.) para popular o objeto ConsultaCompesacaoMantenedoraTO que o batch Java espera.
===========================Ajustes na Sp 19 ==========================================================================================================


Análise da Sua Lógica e Próximos Passos

Vamos analisar sua proposta ponto a ponto:
Sua SP FESSPZ67: O Papel de Alimentar a FESTB812

    Validação 909 e 711 para 812: Você está totalmente correto ao afirmar que sua FESSPZ67 deve ser responsável por comparar os dados da FESTB909 com a FESTB711 para identificar os NU_SQNCL_RLTRO_CTRTO_ANALITICO dos repasses a serem compensados e inseri-los na FESTB812. Este é o objetivo principal da sua SP e ela está bem encaminhada para isso.

O Cenário do NU_SQNCL_LIBERACAO_CONTRATO Apagado

Aqui é onde a sua percepção é crucial:

    Se o NU_SQNCL_LIBERACAO_CONTRATO que gerou o repasse duplicado (identificado na FESTB909) foi apagado da FESTB712 e agora só existe na FESTB909 como referência de um problema:
        Então, a FESSPU19_ROTINA_REPASSE, ao tentar fazer o JOIN de FESTB812 -> FESTB711 -> FESTB712, terá problemas para encontrar os detalhes da liberação na FESTB712.
        A FESSPU19 precisa desses detalhes (NU_IES, NU_CAMPUS, NU_SEQ_CANDIDATO, NU_TIPO_TRANSACAO, VR_CONTRATO, VR_ADITAMENTO, etc.) para popular o objeto ConsultaCompesacaoMantenedoraTO que o batch Java espera.

Sua Proposta de Ajuste na FESSPU19: Você Está Certo!

    "No caso da 19 que chamado para o processo Batch ela teria que fazer uma validação além da tabela 712 na tabela 909, porque se o registro NU_SQNCL_RLTRO_CTRTO_ANALITICO esta na 812 e 711, agora ela tem que buscar o NU_SQNCL_LIBERACAO_CONTRATO que podera esta na 712 ou 909."

Sim, sua lógica está correta! Se a FESTB909 é a "fonte da verdade" para os NU_SQNCL_LIBERACAO_CONTRATO que foram apagados mas que ainda são relevantes para rastrear o problema da duplicidade, então a FESSPU19_ROTINA_REPASSE precisa ser ajustada.

Ela precisará de uma lógica condicional ou de um LEFT JOIN com a FESTB909 (ou até um UNION ALL dependendo da complexidade) para garantir que, caso o NU_SQNCL_LIBERACAO_CONTRATO não seja encontrado na FESTB712 (porque foi apagado), ela possa buscar as informações complementares (NU_IES, NU_CAMPUS, etc.) na FESTB909.
Como o Ajuste na FESSPU19 Poderia Ser (Conceitualmente):
SQL

-- Parte simplificada da FESSPU19_ROTINA_REPASSE
SELECT
    T812.NU_SQNCL_COMPENSACAO_REPASSE,
    T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
    T711.NU_SEQ_CANDIDATO,
    -- ... outras colunas que a FESSPU19 já seleciona
    COALESCE(T712.NU_IES, T909.NU_IES) AS NU_IES_FINAL,       -- Tentar da 712, senão da 909
    COALESCE(T712.NU_CAMPUS, T909.NU_CAMPUS) AS NU_CAMPUS_FINAL,
    COALESCE(T712.NU_TIPO_TRANSACAO, T909.NU_TIPO_TRANSACAO) AS NU_TIPO_TRANSACAO_FINAL, -- Se a 909 tiver
    -- ... e assim por diante para todas as colunas necessárias pelo TO Java

FROM FES.FESTB812_CMPSO_RPSE_INDVO T812
JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO T711
    ON T711.NU_SQNCL_RLTRO_CTRTO_ANALITICO = T812.NU_SQNCL_RLTRO_CTRTO_ANALITICO
LEFT JOIN FES.FESTB712_LIBERACAO_CONTRATO T712
    ON T712.NU_SQNCL_LIBERACAO_CONTRATO = T711.NU_SQNCL_LIBERACAO_CONTRATO
LEFT JOIN FES.FESTB909_RECOMP_712 T909 -- Novo LEFT JOIN
    ON T909.NU_SQNCL_LIBERACAO_CONTRATO = T711.NU_SQNCL_LIBERACAO_CONTRATO -- Ou algum outro critério se T711.NU_SQNCL_LIBERACAO_CONTRATO for o ID apagado
WHERE T812.IC_COMPENSADO = 'N'
-- ... e as condições que já existem na FESSPU19

Este ajuste é vital. Sem ele, mesmo que sua SP FESSPZ67 insira perfeitamente na FESTB812, o batch de compensação pode falhar ou ignorar os registros se a FESSPU19 não conseguir preencher todos os campos do ConsultaCompesacaoMantenedoraTO devido a dados ausentes na FESTB712.
Próximos Passos:

    Confirmação Crucial (Amanhã): Sua ação de confirmar amanhã se a FESTB909 realmente guarda o NU_SQNCL_LIBERACAO_CONTRATO da liberação original/apagada ou da recriada é o ponto de partida.
        Se for a liberação recriada: A SP FESSPU19 NÃO precisará de alteração para buscar na FESTB909. Ela continuará buscando na FESTB712 e encontrará, porque o NU_SQNCL_LIBERACAO_CONTRATO referenciado ainda existirá.
        Se for a liberação apagada (a mais provável, pelo contexto): Então, sim, a FESSPU19_ROTINA_REPASSE precisará de um ajuste para consultar a FESTB909 como um fallback para obter os detalhes do NU_SQNCL_LIBERACAO_CONTRATO que foi apagado.

    Nome da Sequence: Não se esqueça de me passar o nome exato da sequence para a FESTB812.

Assim que tiver essa confirmação sobre qual NU_SQNCL_LIBERACAO_CONTRATO a FESTB909 contém, podemos finalizar a estratégia e até mesmo esboçar o ajuste na FESSPU19 se for necessário.

Estamos no caminho certo para resolver esse desafio complexo!

=================================================Regras da Compensação =================================================================
Com essa correção, a lógica se torna a seguinte:

    A FESTB909_RECOMP_712 contém o NU_SQNCL_LIBERACAO_CONTRATO (vamos chamá-lo de ID_LIBERACAO_PROBLEMA) que foi gerado, apagado da FESTB712, e que é a raiz da duplicidade.

        Este ID_LIBERACAO_PROBLEMA não existirá mais na FESTB712.

        No entanto, este ID_LIBERACAO_PROBLEMA é o que ainda está referenciado no NU_SQNCL_LIBERACAO_CONTRATO do FESTB711_RLTRO_CTRTO_ANLTO do repasse que precisa ser compensado.

    Sua FESSPZ67 precisa:

        Ler o ID_LIBERACAO_PROBLEMA da FESTB909.

        Usar esse ID_LIBERACAO_PROBLEMA para encontrar o NU_SQNCL_RLTRO_CTRTO_ANALITICO correspondente na FESTB711.

        Inserir esse NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB812.

    A FESSPU19_ROTINA_REPASSE (chamada pelo Batch) é o ponto crítico:

        Quando a FESSPU19 for consultada, ela receberá um NU_SQNCL_RLTRO_CTRTO_ANALITICO da FESTB812.

        Ela fará o JOIN com FESTB711 para obter o NU_SQNCL_LIBERACAO_CONTRATO associado.

        Este NU_SQNCL_LIBERACAO_CONTRATO (o ID_LIBERACAO_PROBLEMA) NÃO EXISTE na FESTB712.

        Portanto, a FESSPU19 não conseguirá obter os detalhes necessários (NU_IES, NU_CAMPUS, NU_SEQ_CANDIDATO, VR_CONTRATO, VR_ADITAMENTO, NU_TIPO_TRANSACAO, etc.) que ela precisa para popular o ConsultaCompesacaoMantenedoraTO e que o batch espera.





                                      |-----Estorno                                           |1---FESSPZ67_CRISE2025_COMPENSA_DUPLCD
                                      |-----Compensação(812)----------------------------------|2---FES.FESSPU19_ROTINA_REPASSE
                                   |Repasses                                                  |3---INICIO PROCESSO BATCH--------------- |apurarAnaliticoCompensacao()
contrato(36)---Aditamentos(038)--Liberações(712) ---------Analítico(711)----sintético(710)                                              |consultarPercentualCompensacaoMantenedoras()
                 |---Auditoria (047)  |---retenções (817)                                                                               |consultarSomatorioAnalitico()
                                                                                                                                        |getApuracaoRepasse()
                                                                                                                                        |inserirAnaliticoCompensacaoMantenedora() 
                                                                                                                                               |
                                                                                                                                         FES.FESSPU19_ROTINA_REPASSE            
                                                                                                                                       




 

INICIA O Processo no repasse de moeda :

      onde ele vai verificar :

1.Os repasses
    liberações 
    Aditamentos 
2.estorno
3.compensação 
    O Batch calcula o valor total bruto e líquido a ser repassado a cada mantenedora para o mês atual.
    Ele busca todas as compensações pendentes (IC_COMPENSADO='N') na FESTB812 para cada mantenedora, usando a FESSPU19 para obter os detalhes necessários (inclusive da FESTB711 e FESTB712).
    Ele aplica um limite percentual sobre o valor total do repasse devido no mês.
    Para cada compensação pendente:
        Calcula o valor líquido da compensação (abatendo taxas e integralizações).
        Verifica se a compensação total (bruta, líquida e de integralização) já atingiu o limite mensal.
        Se não atingiu, aplica a compensação (registra e marca o item na FESTB812 como compensado).
    Compensações que excedem o limite do mês ou o valor devido ficam para os meses seguintes.
    


==================================================================================================================================
Análise Detalhada do Fluxo de Repasse e Regras de Negócio do Batch

Vamos juntar todas as peças para ter uma visão clara do fluxo completo de compensação.
O processo Batch então se inicia com a 
1. apurarAnaliticoCompensacao() (Visão Geral do Processo)
    consultarPercentualCompensacaoMantenedoras()
    consultarSomatorioAnalitico()------ soma os VR_REPASSE da FESTB711 para um dado mês/ano, agrupando por mantenedora
    getApuracaoRepasse()
    inserirAnaliticoCompensacaoMantenedora()


    
    1.consultarPercentualCompensacaoMantenedoras(): Busca um percentual máximo de compensação para cada mantenedora (ou um PERCENTUAL_DEFAULT_CONVENIO se não houver um específico). Isso significa que  não irá reter 100% dos repasses de um mês para compensar dívidas antigas, garantindo que a mantenedora receba uma parte. Regra de Negócio: Existe um limite percentual de quanto pode ser compensado do valor bruto do repasse mensal devido.

    2.consultarSomatorioAnalitico(): Agrega o valor bruto do repasse devido a cada mantenedora para o mesReferencia e anoReferencia atuais. Ele filtra T711.NU_SQNCL_CTRTO_ANLTO_CMPNO IS NULL, o que significa que ele considera apenas os repasses que não foram previamente marcados como compensados por alguma outra compensação interna da FESTB711.
    Loop por Mantenedora: Para cada mantenedora com valor de repasse positivo:
        Cálculo dos Limites de Compensação: valorACompensarBruto e valorACompensarLiquido são calculados usando o percentualCompensacao. Estes são os valores máximos que podem ser abatidos dos repasses da mantenedora naquele mês.
        
    3.getApuracaoRepasse(): Este método (não fornecido) provavelmente calcula o valor líquido total do repasse, já considerando outros descontos (como integralização, taxas, etc.).

    4.inserirAnaliticoCompensacaoMantenedora(): Este é o método que tenta aplicar as compensações efetivamente, que veremos a seguir.

2. consultarSomatorioAnalitico() (Cálculo do Repasse Devido)==============talvez essa 

    SQL Completo: Esta query soma os VR_REPASSE da FESTB711 para um dado mês/ano, agrupando por mantenedora.
    JOINs: Ela faz JOIN com FESTB156 (Mantenedora), FESTB712 (Liberação), FESTB036 (Contrato FIES) e FESTB038 (Aditamento Contrato). Isso mostra que os cálculos consideram a origem do repasse (contrato vs. aditamento) e os dados de valor desses contratos/aditamentos.
    Valores de Contrato/Aditamento: As colunas VR_CONTRATO e VR_ADITAMENTO são buscadas da FESTB712 (via T712.NU_TIPO_TRANSACAO). Isso é importante porque a ConsultaCompesacaoMantenedoraTO precisa desses campos, e sua SP FESSPZ67 apenas insere o NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB812. A SP FESSPU19 (chamada pelo NamedNativeQuery) será responsável por fazer esses JOINs e buscar esses valores.

3. inserirAnaliticoCompensacaoMantenedora() (Aplicação da Compensação)

Este método é a implementação da lógica de compensação propriamente dita.

    List<ConsultaCompesacaoMantenedoraTO> listCompensarMantenedora = consultaCompensacoesMantenedora(nuMantenedora);
        Ponto Mais Importante: Este método (que chama a FESSPU19_ROTINA_REPASSE via NamedNativeQuery) é o que consulta a FESTB812! Ele traz todos os registros da FESTB812 para aquela mantenedora que ainda estão com IC_COMPENSADO = 'N'.
        Os objetos ConsultaCompesacaoMantenedoraTO retornados conterão NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_SQNCL_LIBERACAO_CONTRATO, NU_IES, NU_CAMPUS, NU_SEQ_CANDIDATO, VR_REPASSE (do analítico a ser compensado), NU_TIPO_ACERTO, e os valores de VR_CONTRATO e VR_ADITAMENTO que vêm da FESTB712/FESTB036/FESTB038 através da FESSPU19.
    Loop de Compensação: O código itera sobre cada registro de compensação pendente (analiticoCompensarTO) para a mantenedora.
    valorIntegralizacaoCompensacao = consultarIntegralizacaoDevolverAnalitico(analiticoCompensarTO.getNumeroAnaliticoACompensar());:
        Isso indica que, além do VR_REPASSE duplicado, pode haver um valor de "integralização a devolver" associado a esse repasse que também precisa ser compensado.
    Cálculo valorCompensadoLiquido: valorCompensadoLiquido = valorCompensadoLiquido.subtract(valorIntegralizacaoCompensacao).subtract(valorTaxaAdmIntegralizacao);
        Regra de Negócio: Existe uma TAXA_DEFAULT administrativa que é descontada do valor a compensar, e também um valor de integralização. O valor que de fato é "compensado" é o VR_REPASSE do analítico menos essas deduções.
    Condições de Parada da Compensação (if statement):
        valorCompensado.add(analiticoCompensarTO.getValorRepasse()).compareTo(valorACompensarBruto) > 0
            Regra de Negócio: Se a soma do valor já compensado mais o valor do próximo item a compensar for maior que o limite bruto de compensação para o mês atual, a compensação para.
        valorIntegralizacaoTotalCompensacao.add(valorIntegralizacaoCompensacao).compareTo(valorIntegralizacaoRepasse) > 0
            Regra de Negócio: Se a soma total das integralizações a compensar exceder o valor de integralização que a mantenedora tem a receber naquele mês, a compensação para.
        valorCompensadoLiquido.compareTo(valorACompensarLiquido) > 0
            Regra de Negócio: Se o valor total compensado (líquido de taxas e integralização) exceder o limite líquido de compensação para o mês atual, a compensação para.
        break;: Se qualquer uma dessas condições for satisfeita, o loop de compensação é interrompido. Isso significa que nem todos os registros da FESTB812 (com IC_COMPENSADO='N') de uma mantenedora podem ser compensados em um único mês. Os restantes ficam pendentes para os meses seguintes.
    transacaoService.inserirAnaliticoCompensacaoMantenedora(...):
        Este é o ponto final, onde a compensação é registrada. É aqui que o IC_COMPENSADO na FESTB812 é provavelmente atualizado para 'S', marcando o item como compensado. Além disso, pode haver inserções em tabelas de auditoria ou financeiras que registram a compensação aplicada.



Entendendo a Lógica Corrigida e Seus Impactos

Com essa correção, a lógica se torna a seguinte:

    A FESTB909_RECOMP_712 contém o NU_SQNCL_LIBERACAO_CONTRATO (vamos chamá-lo de ID_LIBERACAO_PROBLEMA) que foi gerado, apagado da FESTB712, e que é a raiz da duplicidade.

        Este ID_LIBERACAO_PROBLEMA não existirá mais na FESTB712.

        No entanto, este ID_LIBERACAO_PROBLEMA é o que ainda está referenciado no NU_SQNCL_LIBERACAO_CONTRATO do FESTB711_RLTRO_CTRTO_ANLTO do repasse que precisa ser compensado.

    Sua FESSPZ67 precisa:

        Ler o ID_LIBERACAO_PROBLEMA da FESTB909.

        Usar esse ID_LIBERACAO_PROBLEMA para encontrar o NU_SQNCL_RLTRO_CTRTO_ANALITICO correspondente na FESTB711.

        Inserir esse NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB812.

    A FESSPU19_ROTINA_REPASSE (chamada pelo Batch) é o ponto crítico:

        Quando a FESSPU19 for consultada, ela receberá um NU_SQNCL_RLTRO_CTRTO_ANALITICO da FESTB812.

        Ela fará o JOIN com FESTB711 para obter o NU_SQNCL_LIBERACAO_CONTRATO associado.

        Este NU_SQNCL_LIBERACAO_CONTRATO (o ID_LIBERACAO_PROBLEMA) NÃO EXISTE na FESTB712.

        Portanto, a FESSPU19 não conseguirá obter os detalhes necessários (NU_IES, NU_CAMPUS, NU_SEQ_CANDIDATO, VR_CONTRATO, VR_ADITAMENTO, NU_TIPO_TRANSACAO, etc.) que ela precisa para popular o ConsultaCompesacaoMantenedoraTO e que o batch espera.
---------------------------------------------
Análise Detalhada do Fluxo de Repasse e Regras de Negócio do Batch

Vamos juntar todas as peças para ter uma visão clara do fluxo completo de compensação.
1. apurarAnaliticoCompensacao() (Visão Geral do Processo)

    Objetivo: Este é o método que coordena o cálculo do valor total de repasse devido a cada mantenedora e inicia o processo de aplicação das compensações.
    consultarPercentualCompensacaoMantenedoras(): Busca um percentual máximo de compensação para cada mantenedora (ou um PERCENTUAL_DEFAULT_CONVENIO se não houver um específico). Isso significa que a instituição não irá reter 100% dos repasses de um mês para compensar dívidas antigas, garantindo que a mantenedora receba uma parte. Regra de Negócio: Existe um limite percentual de quanto pode ser compensado do valor bruto do repasse mensal devido.
    consultarSomatorioAnalitico(): Agrega o valor bruto do repasse devido a cada mantenedora para o mesReferencia e anoReferencia atuais. Ele filtra T711.NU_SQNCL_CTRTO_ANLTO_CMPNO IS NULL, o que significa que ele considera apenas os repasses que não foram previamente marcados como compensados por alguma outra compensação interna da FESTB711.
    Loop por Mantenedora: Para cada mantenedora com valor de repasse positivo:
        Cálculo dos Limites de Compensação: valorACompensarBruto e valorACompensarLiquido são calculados usando o percentualCompensacao. Estes são os valores máximos que podem ser abatidos dos repasses da mantenedora naquele mês.
        getApuracaoRepasse(): Este método (não fornecido) provavelmente calcula o valor líquido total do repasse, já considerando outros descontos (como integralização, taxas, etc.).
        inserirAnaliticoCompensacaoMantenedora(): Este é o método que tenta aplicar as compensações efetivamente, que veremos a seguir.

2. consultarSomatorioAnalitico() (Cálculo do Repasse Devido)

    SQL Completo: Esta query soma os VR_REPASSE da FESTB711 para um dado mês/ano, agrupando por mantenedora.
    JOINs: Ela faz JOIN com FESTB156 (Mantenedora), FESTB712 (Liberação), FESTB036 (Contrato FIES) e FESTB038 (Aditamento Contrato). Isso mostra que os cálculos consideram a origem do repasse (contrato vs. aditamento) e os dados de valor desses contratos/aditamentos.
    Valores de Contrato/Aditamento: As colunas VR_CONTRATO e VR_ADITAMENTO são buscadas da FESTB712 (via T712.NU_TIPO_TRANSACAO). Isso é importante porque a ConsultaCompesacaoMantenedoraTO precisa desses campos, e sua SP FESSPZ67 apenas insere o NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB812. A SP FESSPU19 (chamada pelo NamedNativeQuery) será responsável por fazer esses JOINs e buscar esses valores.

3. inserirAnaliticoCompensacaoMantenedora() (Aplicação da Compensação)

Este método é a implementação da lógica de compensação propriamente dita.

    List<ConsultaCompesacaoMantenedoraTO> listCompensarMantenedora = consultaCompensacoesMantenedora(nuMantenedora);
        Ponto Mais Importante: Este método (que chama a FESSPU19_ROTINA_REPASSE via NamedNativeQuery) é o que consulta a FESTB812! Ele traz todos os registros da FESTB812 para aquela mantenedora que ainda estão com IC_COMPENSADO = 'N'.
        Os objetos ConsultaCompesacaoMantenedoraTO retornados conterão NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_SQNCL_LIBERACAO_CONTRATO, NU_IES, NU_CAMPUS, NU_SEQ_CANDIDATO, VR_REPASSE (do analítico a ser compensado), NU_TIPO_ACERTO, e os valores de VR_CONTRATO e VR_ADITAMENTO que vêm da FESTB712/FESTB036/FESTB038 através da FESSPU19.
    Loop de Compensação: O código itera sobre cada registro de compensação pendente (analiticoCompensarTO) para a mantenedora.
    valorIntegralizacaoCompensacao = consultarIntegralizacaoDevolverAnalitico(analiticoCompensarTO.getNumeroAnaliticoACompensar());:
        Isso indica que, além do VR_REPASSE duplicado, pode haver um valor de "integralização a devolver" associado a esse repasse que também precisa ser compensado.
    Cálculo valorCompensadoLiquido: valorCompensadoLiquido = valorCompensadoLiquido.subtract(valorIntegralizacaoCompensacao).subtract(valorTaxaAdmIntegralizacao);
        Regra de Negócio: Existe uma TAXA_DEFAULT administrativa que é descontada do valor a compensar, e também um valor de integralização. O valor que de fato é "compensado" é o VR_REPASSE do analítico menos essas deduções.
    Condições de Parada da Compensação (if statement):
        valorCompensado.add(analiticoCompensarTO.getValorRepasse()).compareTo(valorACompensarBruto) > 0
            Regra de Negócio: Se a soma do valor já compensado mais o valor do próximo item a compensar for maior que o limite bruto de compensação para o mês atual, a compensação para.
        valorIntegralizacaoTotalCompensacao.add(valorIntegralizacaoCompensacao).compareTo(valorIntegralizacaoRepasse) > 0
            Regra de Negócio: Se a soma total das integralizações a compensar exceder o valor de integralização que a mantenedora tem a receber naquele mês, a compensação para.
        valorCompensadoLiquido.compareTo(valorACompensarLiquido) > 0
            Regra de Negócio: Se o valor total compensado (líquido de taxas e integralização) exceder o limite líquido de compensação para o mês atual, a compensação para.
        break;: Se qualquer uma dessas condições for satisfeita, o loop de compensação é interrompido. Isso significa que nem todos os registros da FESTB812 (com IC_COMPENSADO='N') de uma mantenedora podem ser compensados em um único mês. Os restantes ficam pendentes para os meses seguintes.
    transacaoService.inserirAnaliticoCompensacaoMantenedora(...):
        Este é o ponto final, onde a compensação é registrada. É aqui que o IC_COMPENSADO na FESTB812 é provavelmente atualizado para 'S', marcando o item como compensado. Além disso, pode haver inserções em tabelas de auditoria ou financeiras que registram a compensação aplicada.

Resumo do Fluxo do Repasse e Regras de Negócio:

    O Batch calcula o valor total bruto e líquido a ser repassado a cada mantenedora para o mês atual.
    Ele busca todas as compensações pendentes (IC_COMPENSADO='N') na FESTB812 para cada mantenedora, usando a FESSPU19 para obter os detalhes necessários (inclusive da FESTB711 e FESTB712).
    Ele aplica um limite percentual sobre o valor total do repasse devido no mês.
    Para cada compensação pendente:
        Calcula o valor líquido da compensação (abatendo taxas e integralizações).
        Verifica se a compensação total (bruta, líquida e de integralização) já atingiu o limite mensal.
        Se não atingiu, aplica a compensação (registra e marca o item na FESTB812 como compensado).
    Compensações que excedem o limite do mês ou o valor devido ficam para os meses seguintes.
=========================================================================================================================================
INSERT INTO FES.FESTB712_LIBERACAO_CONTRATO (
    NU_SQNCL_LIBERACAO_CONTRATO, NU_SEQ_CANDIDATO, NU_MANTENEDORA, NU_IES, NU_CAMPUS, NU_PARCELA,
    MM_REFERENCIA_LIBERACAO, AA_REFERENCIA_LIBERACAO, VR_REPASSE, IC_SITUACAO_LIBERACAO, DT_LIBERACAO,
    DT_INCLUSAO, DT_ATUALIZACAO, NU_PARTICIPACAO_CANDIDATO, NU_SQNCL_ADITAMENTO, NU_TIPO_TRANSACAO,
    IC_APTO_LIBERACAO_REPASSE
) VALUES (
    90000002,      -- L9002: ID da liberação que gerou o repasse duplicado e está na 712
    90000000,      -- Candidato de teste
    1965,          -- Mantenedora de teste
    3034,          -- IES de teste
    1061025,       -- Campus de teste
    3,             -- Parcela 3
    3,             -- Mês de referência
    2024,          -- Ano de referência (alterado para um ano mais recente)
    1325.82,       -- Valor do repasse
    'S',           -- Situação da liberação
    TO_DATE('2024-03-15', 'YYYY-MM-DD'), -- Data da liberação
    TO_TIMESTAMP('2024-03-15 15:01:21.000', 'YYYY-MM-DD HH24:MI:SS.FF3'), -- Data de inclusão (mais recente)
    SYSTIMESTAMP,  -- Data de atualização
    1,             -- Participação
    1,             -- Sequencial de aditamento (se houver)
    1,             -- Tipo de transação (e.g., 1 para Contrato)
    'S'            -- Apto para liberação
);
---------------------------------------------------------------
INSERT INTO FES.FESTB711_RLTRO_CTRTO_ANLTO (
    NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_CAMPUS, NU_TIPO_TRANSACAO, NU_MANTENEDORA, NU_IES, NU_SEQ_CANDIDATO,
    MM_REFERENCIA, AA_REFERENCIA, VR_REPASSE, DT_ASSINATURA, NU_SQNCL_LIBERACAO_CONTRATO,
    TS_APURACAO_RELATORIO, NU_SQNCL_CTRTO_ANLTO_CMPNO
) VALUES (
    90000011,      -- R9002: ID do repasse analítico duplicado
    1061025,       -- Campus
    1,             -- Tipo de transação
    1965,          -- Mantenedora
    3034,          -- IES
    90000000,      -- Candidato de teste
    3,             -- Mês de referência
    2024,          -- Ano de referência
    1325.82,       -- Valor do repasse
    TO_DATE('2024-03-15', 'YYYY-MM-DD'), -- Data de assinatura
    90000002,      -- L9002: Referencia a liberação duplicada da 712
    TO_TIMESTAMP('2024-03-15 15:01:26.000', 'YYYY-MM-DD HH24:MI:SS.FF3'), -- Timestamp de apuração
    NULL           -- Não compensado por compensação interna
);
-----------------------------------------------------------------------------
INSERT INTO FES.FESTB909_RECOMP_712 (
    NU_SQNCL_LIBERACAO_CONTRATO, NU_SEQ_CANDIDATO, NU_IES, NU_CAMPUS, NU_PARCELA,
    MM_REFERENCIA_LIBERACAO, AA_REFERENCIA_LIBERACAO, VR_REPASSE,
    TS_INCLUSAO, CO_USUARIO_INCLUSAO
) VALUES (
    90000002,      -- L9002: NU_SQNCL_LIBERACAO_CONTRATO da liberação duplicada
    90000000,      -- Candidato
    3034,          -- IES
    1061025,       -- Campus
    3,             -- Parcela
    3,             -- Mês de referência
    2024,          -- Ano de referência
    1325.82,       -- Valor do repasse
    SYSTIMESTAMP,  -- Carimbo de data/hora atual
    'TESTE_SP67'   -- Usuário de inclusão
);
--------------------------------------------------------------------------------------
-- Assumindo que você tem acesso à sequence para NU_SQNCL_COMPENSACAO_REPASSE ou pode inserir um valor alto fictício.
-- Se você ainda não tem o nome da sequence, use um valor como 90000000 para este campo, por exemplo.
INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (
    NU_SQNCL_COMPENSACAO_REPASSE, NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_TIPO_ACERTO,
    TS_INCLUSAO, CO_USUARIO_INCLUSAO, IC_COMPENSADO
) VALUES (
    90000000,      -- ID Fictício de Compensação (substituir pela sequence real se disponível)
    90000011,      -- R9002: NU_SQNCL_RLTRO_CTRTO_ANALITICO do repasse duplicado (mesmo da FESTB711)
    1,             -- Tipo de acerto (1 para duplicidade)
    SYSTIMESTAMP,  -- Data de inclusão
    'C000000',     -- Usuário de inclusão
    'N'            -- Indicador de compensado ('N' para pendente)
);
--------------------------------------------------------------------------------------


=========================================================================================================================================
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
