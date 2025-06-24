<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:
Excelente! Vamos detalhar a FES.FESSPU20_VINCULA_LIBERACAO no formato solicitado:
FES.FESSPU20_VINCULA_LIBERACAO

    Objetivo: Vincular as liberações de contrato a seus respectivos aditamentos ou contratos iniciais, marcando o tipo de transação e a participação do candidato. Além disso, a SP insere retenções para liberações que não conseguem ser vinculadas.

openapi: 3.0.1
info:
  version: 1.0.0
  title: API Aditamento transferencia curso  - SIFES
  description: >-
    ## *Orientações*

    API utilizada para permitir aos estudantes inscritos no programa de
    financiamento estudantil FIES realizarem o aditamento de dilatação de seus
    contratos junto à Caixa.


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


    `- Nº do RTC de Validação do Swagger: `**20961817**
     
  contact:
    name: Equipe de Desenvolvimento (cesob220@caixa.gov.br)
    email: sudeXXX@caixa.gov.br
servers:
  - url: >-
      https://api.des.caixa:8446/fes-web/servicos/contratacao/encerramentoContrato
    description: ''
paths:
  '/v1/buscar-estudante-transferencia/{cpf}':
    get:
      summary: Busca informações do contrato FIES do estudante por CPF (via path parameter)
      description: |
        Este endpoint permite consultar os detalhes do contrato do Fundo de Financiamento Estudantil (FIES)
        de um estudante específico, utilizando o seu número de Cadastro de Pessoa Física (CPF).
        Os parâmetros são enviados na URL como path parameters.
      parameters:
        - in: path # Manteve 'path' como correto, assumindo que é a intenção real para '{cpf}'
          name: cpf
          schema:
            type: string
            pattern: '^[0-9]{11}$'
          description: CPF do estudante a ser consultado (apenas números).
          required: true
          example: "70966798120"
      
      # Se você tiver segurança, ela viria AQUI (fora de responses):
      # security:
      #   - bearerAuth: [] # Exemplo, troque 'bearerAuth' pelo nome do seu securityScheme
      
      responses:
        '200':
          description: Resposta bem-sucedida com os detalhes do contrato do estudante.
          content:
            application/json:
              schema:
                # Corrigido: Usando $ref para um schema definido em components
                $ref: '#/components/schemas/EstudanteTransferenciaResponse'
              examples: # Este 'examples' DEVE ficar aqui, um nível acima de 'schema'
                sucessoBuscaEstudante:
                  value: # O valor deve corresponder à estrutura de EstudanteTransferenciaResponse
                    mensagem: ""
                    codigo: null
                    tipo: null
                    editavel: true
                    agencia: 4736
                    estudante:
                      cpf: "70966798120"
                      nome: "Fulano de Tal"
                      dataNascimento: "1990-01-01"
                      ric: "123456789"
                      nacionalidade: "Brasileira"
                      identidade: "9876543"
                      estadoCivil: "Solteiro"
                      regimeBens: "Comunhão Parcial"
                      endereco:
                        logradouro: "Rua Exemplo"
                        numero: "123"
                        bairro: "Centro"
                        cidade: "São Paulo"
                        uf: "SP"
                        cep: "01000-000"
                      contato:
                        email: "fulano@exemplo.com"
                        telefone: "11987654321"
                      vinculacao: "Ativo"
                      codigoFies: "F001"
                      sexo: "M"
                      pis: "123.45678.90-1"
                      conjuge: null
                      responsavelLegal: null
                      emancipado: false
                      nomeCandidato: "Fulano de Tal"
                      nomeCurso: "Engenharia de Software"
                      idCampus: 100
                      nomeCampus: "Campus Central"
                      numeroCandidato: "CAND001"
                      descricaoMunicipio: "São Paulo"
                      nomeIes: "Universidade Fictícia"
                      ufCampus: "SP"
                      contaCorrente: # Exemplo de estrutura para contaCorrente
                        agencia: "1234"
                        numeroConta: "56789-0"
                      permiteLiquidar: true
                      voucher: "VOUCHER123"
                      dataValidadeVoucher: "2025-12-31"
                      motivoImpeditivo: null
                      inadimplente: false
                      atrasado: false
                      liquidado: false
                      rendaFamiliar: 5000.00
                      recebeSms: true
                      vinculoSolidario: "Não"
                      contratoEstudante:
                        ies: # Array de IES
                          - nome: "Universidade XYZ"
                            codigo: "001"
                        codigoStatusContrato: "01"
                        numeroOperacaoSIAPI: "OP001"
                        statusContrato: "Ativo"
                        situacaoContrato: "Regular"
                        dataLimiteContratacao: "2026-06-30"
                        valorMensalidade: 1500.00
                        valorContrato: 90000.00
                        dataAssinatura: "2020-08-01"
                        percentualFinanciamento: 0.8
                        numeroContrato: "CONTRATO001"
                        diaVencimento: 5
                        codigoTipoGarantia: "FG"
                        descricaoTipoGarantia: "Fiança Garantida"
                        valorGarantia: 10000.00
                        codCurso: "ESW"
                        semestreCursados: 8
                        estudanteCurso: # Exemplo de estrutura para estudanteCurso
                          nome: "Engenharia"
                        valorAditamento: 5000.00
                        unidadeCaixa: "Agência Central"
                        prazoContratoMec: "48"
                        semestreReferencia: "2025.1"
                        anoReferencia: 2025
                        bloqueioMec: false
                        permiteContratacao: true
                        recebeInformacao: true
                        localExtrato: "Online"
                        prouni: false
                        quantidadeAditamentos: 2
                        quantidadePreAditamentos: 1
                        sipesListaBanco: ["Banco do Brasil", "Caixa Econômica"]
                        idSeguradora: "SEG001"
                        indContratoNovoFies: true
                        taxaJuros: 0.03
                        existeTarifaContrato: false
                        vrCoParticipacao: 100.00
                        valorSeguro: 50.00
                        numeroProcessoSeletivo: "PS2020_1"


# --- Nova Seção ---
components:
  schemas:
    # Definindo o schema completo da resposta 200
    EstudanteTransferenciaResponse:
      type: object
      properties:
        mensagem:
          type: string
          description: Mensagem informativa (geralmente vazia em caso de sucesso).
          nullable: true
        codigo:
          type: string
          description: Código de retorno (geralmente nulo em caso de sucesso).
          nullable: true
        tipo:
          type: string
          description: Tipo da mensagem (geralmente nulo em caso de sucesso).
          nullable: true
        editavel:
          type: boolean
          description: Indica se os dados são editáveis.
          nullable: true
        agencia:
          type: integer
          description: Código da agência bancária do contrato.
        estudante: # Este é o objeto 'estudante' que contém os detalhes
          $ref: '#/components/schemas/EstudanteDetalhesTransferencia' # Referencia o schema detalhado do estudante
      required:
        - estudante # Assumindo que o estudante é sempre retornado em caso de sucesso

    EstudanteDetalhesTransferencia:
      type: object
      description: Informações detalhadas do estudante para transferência.
      properties:
        cpf:
          type: string
        nome:
          type: string
        dataNascimento:
          type: string
          format: date
        ric:
          type: string
        nacionalidade:
          type: string
        identidade:
          type: string
        estadoCivil:
          type: string
        regimeBens:
          type: string
        endereco:
          type: object
          properties:
            logradouro:
              type: string
            numero:
              type: string
            bairro:
              type: string
            cidade:
              type: string
            uf:
              type: string
            cep:
              type: string
        contato:
          type: object
          properties:
            email:
              type: string
            telefone:
              type: string
        vinculacao:
          type: string
        codigoFies:
          type: string
        sexo:
          type: string
        pis:
          type: string
        conjuge:
          type: object
          # Adicione as propriedades do cônjuge aqui, se houver
          nullable: true
        responsavelLegal:
          type: object
          # Adicione as propriedades do responsável legal aqui, se houver
          nullable: true
        emancipado:
          type: boolean
        nomeCandidato:
          type: string
        nomeCurso:
          type: string
        idCampus:
          type: integer
        nomeCampus:
          type: string
        numeroCandidato:
          type: string
        descricaoMunicipio:
          type: string
        nomeIes:
          type: string
        ufCampus:
          type: string
        contaCorrente:
          type: object
          properties:
            agencia:
              type: string
            numeroConta:
              type: string
        permiteLiquidar:
          type: boolean
        voucher:
          type: string
          nullable: true
        dataValidadeVoucher:
          type: string
          format: date
          nullable: true
        motivoImpeditivo:
          type: string
          nullable: true
        inadimplente:
          type: boolean
        atrasado:
          type: boolean
        liquidado:
          type: boolean
        rendaFamiliar:
          type: number
          format: float
        recebeSms:
          type: boolean
        vinculoSolidario:
          type: string
        contratoEstudante: # Este é o objeto que contém as propriedades do contrato
          $ref: '#/components/schemas/ContratoEstudanteDetalhes'
      required: # Adicione aqui as propriedades obrigatórias de EstudanteDetalhesTransferencia
        - cpf
        - nome
        - dataNascimento

    ContratoEstudanteDetalhes:
      type: object
      description: Detalhes do contrato FIES do estudante.
      properties:
        ies:
          type: array
          items:
            type: object
            properties:
              nome:
                type: string
              codigo:
                type: string
        codigoStatusContrato:
          type: string
        numeroOperacaoSIAPI:
          type: string
        statusContrato:
          type: string
        situacaoContrato:
          type: string
        dataLimiteContratacao:
          type: string
          format: date
        valorMensalidade:
          type: number
          format: float
        valorContrato:
          type: number
          format: float
        dataAssinatura:
          type: string
          format: date
        percentualFinanciamento:
          type: number
          format: float
        numeroContrato:
          type: string
        diaVencimento:
          type: integer
        codigoTipoGarantia:
          type: string
        descricaoTipoGarantia:
          type: string
        valorGarantia:
          type: number
          format: float
        codCurso:
          type: string
        semestreCursados:
          type: integer
        estudanteCurso:
          type: object
          properties:
            nome:
              type: string
            # Adicione outras propriedades de estudanteCurso aqui
        valorAditamento:
          type: number
          format: float
        unidadeCaixa:
          type: string
        prazoContratoMec:
          type: string
        semestreReferencia:
          type: string
        anoReferencia:
          type: integer
        bloqueioMec:
          type: boolean
        permiteContratacao:
          type: boolean
        recebeInformacao:
          type: boolean
        localExtrato:
          type: string
        prouni:
          type: boolean
        quantidadeAditamentos:
          type: integer
        quantidadePreAditamentos:
          type: integer
        sipesListaBanco:
          type: array
          items:
            type: string
        idSeguradora:
          type: string
        indContratoNovoFies:
          type: boolean
        taxaJuros:
          type: number
          format: float
        existeTarifaContrato:
          type: boolean
        vrCoParticipacao:
          type: number
          format: float
        valorSeguro:
          type: number
          format: float
  '/v1/buscar-estudante/{CPF}':
    get:
      summary: >-
        Busca informações do contrato FIES do estudante por CPF (via query
        parameters)
      description: >
        Este endpoint permite consultar os detalhes do contrato do Fundo de
        Financiamento Estudantil (FIES)
      parameters:
        - in: path
          name: cpf
          schema:
            type: string
            pattern: '^[0-9]{11}$'
          description: CPF do estudante a ser consultado (apenas números).
          required: true
          example: '70966798120'
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
                    example: ''
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
                    example: '70966798120'
                  nomeCandidato:
                    type: string
                    example: LUANA GARCIA FERREIRA
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
                    example: CENTRO UNIVERSITÁRIO EURO-AMERICANO
                  nomeMantenedora:
                    type: string
                    example: Instituto Euro Americano De Educacao Ciencia Tecnologia
                  turnoDescDestino:
                    type: string
                    nullable: true
                    example: Matutino
                  uf:
                    type: string
                    example: DF
                  municipio:
                    type: string
                    example: BRASILIA
                  endereco:
                    type: string
                    example: SCES Trecho 0 - Conjunto 5
                  nomeCampus:
                    type: string
                    example: Centro Universitário Euro-Americano - Unidade Asa Sul
                  nomeCurso:
                    type: string
                    example: ENFERMAGEM
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
                          type: integer
                          example: 58024
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
                          example: ''
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
                          example: IES
                        statusDesc:
                          type: string
                          example: Cancelado
                  icCondicaoFuncionamento:
                    type: string
                    example: 'N'
                  icSituacaoContrato:
                    type: string
                    example: U
                  icSituacaoIES:
                    type: string
                    example: L
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
                    example: Matutino
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
                    example: >-
                      {"nuCpf":"70966798120","vlNotaEnemConsiderada":"495.34","nuSemestreReferencia":"22020","coInscricao":6614627,"nuAnoEnem":"2019"}
                  estudantePodeTransfCurso:
                    type: string
                    example: S
                  totalSemestresDisponiveis:
                    type: integer
                    example: 5
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
          description: Não foi localizado um contrato para o código Fies fornecido.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: >-
                      Não foi localizado um contrato para o código Fies
                      fornecido.
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
  '/v1/buscar-combo-todas-ies/{nomeIes}':
    get:
      summary: Busca informações de Instituições de Ensino (IES)
      description: Retorna uma lista de IES com base nos critérios de busca.
      parameters:
        - in: path
          name: nomeIes
          schema:
            type: string
          description: Filtra IES por nome (parcial ou completo).
      responses:
        '200':
          description: Sucesso - Retorna a lista de IES encontradas.
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
                          example: 9945
                        descricao:
                          type: string
                          example: 9945
                        atributo:
                          type: string
                          nullable: true
                          example: null
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
          description: Não foi localizado um contrato para o código Fies fornecido.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: >-
                      Não foi localizado um contrato para o código Fies
                      fornecido.
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
  '/v1/consultar-campus-transferencia{ies}/{semestreReferencia}/{anoReferencia}':
    get:
      summary: >-
        Busca campus de transferência para uma IES, semestre e ano de
        referência.
      description: >-
        Retorna uma lista de campus disponíveis para transferência com base nos
        parâmetros fornecidos.
      parameters:
        - in: path
          name: ies
          schema:
            type: string
          description: Código da Instituição de Ensino (IES).
          required: true
        - in: path
          name: semestreReferencia
          schema:
            type: integer
            enum:
              - 1
              - 2
          description: Semestre de referência para a busca.
          required: true
        - in: path
          name: anoReferencia
          schema:
            type: integer
            format: int32
          description: Ano de referência para a busca.
          required: true
      responses:
        '200':
          description: Sucesso - Retorna a lista de campus de transferência encontrados.
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
                        descricao:
                          type: string
                        atributo:
                          type: string
                          nullable: true
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
          description: Não foi localizado um contrato para o código Fies fornecido.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: >-
                      Não foi localizado um contrato para o código Fies
                      fornecido.
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
  /v1/consultar-curso-campus-oferta:
    get:
      summary: Busca cursos disponíveis em um campus de oferta para transferência.
      description: >-
        Retorna uma lista de cursos disponíveis para transferência com base nos
        parâmetros fornecidos.
      parameters:
        - in: query
          name: nuCampus
          schema:
            type: string
          description: Número do campus de oferta.
          required: true
        - in: query
          name: nuCurso
          schema:
            type: string
          description: Número do curso de oferta.
          required: true
        - in: query
          name: nuTurno
          schema:
            type: integer
          description: Número do turno do curso de oferta.
          required: true
        - in: query
          name: nuCampusDestino
          schema:
            type: string
          description: Número do campus de destino para a transferência.
          required: true
        - in: query
          name: semestre
          schema:
            type: integer
            enum:
              - 1
              - 2
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
          description: Sucesso - Retorna a lista de cursos disponíveis para transferência.
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
                        descricao:
                          type: string
                        atributo:
                          type: string
                          nullable: true
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
          description: Não foi localizado um contrato para o código Fies fornecido.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: >-
                      Não foi localizado um contrato para o código Fies
                      fornecido.
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
  /v1/consultar-turnos:
    get:
      summary: Busca turnos disponíveis para um curso em um campus de destino.
      description: >-
        Retorna uma lista de turnos disponíveis com base nos parâmetros
        fornecidos.
      parameters:
        - in: query
          name: nuCampus
          schema:
            type: string
          description: Número do campus de oferta (pode ser '0' para indicar todos).
          required: true
        - in: query
          name: nuCurso
          schema:
            type: string
          description: Número do curso de oferta (pode ser '0' para indicar todos).
          required: true
        - in: query
          name: nuTurno
          schema:
            type: string
          description: Número do turno de oferta (pode ser '0' para indicar todos).
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
            enum:
              - 1
              - 2
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
          description: Sucesso - Retorna a lista de turnos disponíveis.
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
                        descricao:
                          type: string
                        atributo:
                          type: string
                          nullable: true
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
          description: Não foi localizado um contrato para o código Fies fornecido.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: >-
                      Não foi localizado um contrato para o código Fies
                      fornecido.
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
  /v1/buscar-curso-destino:
    get:
      summary: Busca informações do curso de destino para transferência de contrato.
      description: >-
        Retorna detalhes sobre o curso de destino com base nos parâmetros
        fornecidos.
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
          description: >-
            Número do campus de destino (pode ser '0' para indicar algum
            critério).
          required: true
        - in: query
          name: semestrePendencia
          schema:
            type: integer
            enum:
              - 1
              - 2
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
                  codigo:
                    type: integer
                  tipo:
                    type: string
                    nullable: true
                  editavel:
                    type: boolean
                    nullable: true
                  campusDest:
                    type: string
                  cursoDest:
                    type: string
                  mantenedoraDest:
                    type: string
                  ufDest:
                    type: string
                  municipioDest:
                    type: string
                  iesDest:
                    type: string
                  numeroIesDestino:
                    type: integer
                  enderecoDest:
                    type: string
                  duracaoCursoDest:
                    type: integer
                  codigoCursoHabilitacao:
                    type: string
                  numeroCursoDestino:
                    type: integer
                  semestreReferenciaDest:
                    type: string
                  numeroCampusDestino:
                    type: integer
                  numeroMantenedoraDestino:
                    type: integer
                  numeroTurnoDestino:
                    type: integer
                  descTurnoDestino:
                    type: string
                  notaEnem:
                    type: number
                  jsonRetornoConsultaEnem:
                    type: string
                  possuiLiminarNotaDeCorte:
                    type: boolean
        '400':
          description: Requisição inválida - Algum parâmetro está ausente ou incorreto.
        '500':
          description: Erro interno do servidor.
  /v1/confirmar-solicitacao-estudante:
    post:
      summary: Confirma a solicitação de transferência do estudante.
      description: Confirma a solicitação de transferência com os dados fornecidos.
      parameters:
        - in: query
          name: numeroSemestresCursar
          schema:
            type: integer
          description: Número de semestres a cursar.
          required: true
        - in: query
          name: dtDesligamento
          schema:
            type: string
            format: date
          description: Data de desligamento (formato DD/MM/YYYY).
          required: true
        - in: query
          name: codFies
          schema:
            type: string
          description: Código FIES do contrato.
          required: true
        - in: query
          name: tipoTransferencia
          schema:
            type: integer
          description: Tipo da transferência.
          required: true
        - in: query
          name: nuCurso
          schema:
            type: string
          description: Número do curso de origem.
          required: true
        - in: query
          name: numeroCursoDestino
          schema:
            type: string
          description: Número do curso de destino.
          required: true
        - in: query
          name: numeroCampusDestino
          schema:
            type: string
          description: Número do campus de destino.
          required: true
        - in: query
          name: nuCampus
          schema:
            type: string
          description: Número do campus de origem.
          required: true
        - in: query
          name: nuTurno
          schema:
            type: integer
          description: Número do turno de origem.
          required: true
        - in: query
          name: campusDestino
          schema:
            type: string
          description: Código do campus de destino.
          required: true
        - in: query
          name: cursoDestino
          schema:
            type: string
          description: Código do curso de destino.
          required: true
        - in: query
          name: turnoDestino
          schema:
            type: integer
          description: Código do turno de destino.
          required: true
      responses:
        '200':
          description: Sucesso - Operação realizada com sucesso.
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
                  editavel:
                    nullable: true
        '400':
          description: Requisição inválida - Algum parâmetro está ausente ou incorreto.
        '500':
          description: Erro interno do servidor.
  /v1/cancelar-solicitacao-transferencia:
    post:
      summary: Busca turnos disponíveis para um curso em um campus de destino.
      description: >-
        Retorna uma lista de turnos disponíveis com base nos parâmetros
        fornecidos.
      parameters:
        - in: query
          name: codFies
          schema:
            type: string
          description: Código FIES do estudante.
          required: true
          example: '20242515'
        - in: query
          name: idSolicitacao
          schema:
            type: integer
          description: ID da solicitação de encerramento.
          required: true
          example: 57984
      responses:
        '200':
          description: Operação realizada com sucesso.
          content:
            application/json:
              schema:
                type: object
                properties:
                  mensagem:
                    type: string
                    example: Operação realizada com sucesso.
                  codigo:
                    type: integer
                    example: 0
                  tipo:
                    type: string
                    example: alert-success
                  editavel:
                    nullable: true
                    example: null
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
          description: Não foi localizado um contrato para o código Fies fornecido.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RetornoErro'
              examples:
                Exemplo:
                  value:
                    codigo: 404
                    mensagem: >-
                      Não foi localizado um contrato para o código Fies
                      fornecido.
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
        mensagem: Solicitação de dilatação confirmada com sucesso..
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
