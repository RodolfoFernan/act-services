<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>

    

openapi: 3.0.1
info:
  version: 1.0.0
  title: API Aditamento de Dilatação - SIFES
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
  - url: https://api.des.caixa:8446/fes-web/servicos/contratacao/encerramentoContrato

    description: ''
paths:
  'v1/contrato/consulta-estudante':
    POST:
      summary: Busca informações do contrato FIES do estudante por CPF (via corpo da requisição)
      description: |
        Este endpoint permite consultar os detalhes do contrato do Fundo de Financiamento Estudantil (FIES)
        de um estudante específico, utilizando o seu número de Cadastro de Pessoa Física (CPF).
        Os parâmetros são enviados no corpo da requisição.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                cpf:
                  type: string
                  pattern: '^[0-9]{11}$'
                  description: CPF do estudante a ser consultado (apenas números).
                  example: "70966798120"
                codigoFies:
                  type: string
                  required: false
                  description: Código FIES do estudante (opcional).
                  example: "0000"
                agencia:
                  type: string
                  required: false
                  description: Código da agência bancária (opcional).
                  example: "0"
                _:
                  type: integer
                  required: false
                  description: Timestamp para evitar cache (gerado dinamicamente).
                  example: 1747058495351
      responses:
        '200':
          description: Resposta bem-sucedida com os detalhes do contrato do estudante.
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
                    description: Informações detalhadas do estudante.
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
                        description: Detalhes da identidade do estudante.
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
                    # (Detailed IES object structure - truncated for brevity, see full response for details)
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
                    # (Detailed estudanteCurso object structure - truncated for brevity, see full response for details)
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
              examples:
                OperacaoIndisponivelContrato:
                  summary: Operação não disponível para este contrato.
                  value:
                    mensagem: "Já existe solicitação de transferencia para este semestre."
                    tipo :
                    codigo: 2
                    possuiDilatacaoAberta: true
                    uf: "MG"
                    editavel : false 
                CalendarioFechado:
                  summary: Operação não disponível para este contrato. (calendario fechado)
                  value:
                    mensagem: "Operação não disponível para este contrato."
                    codigo: 2
                    possuiDilatacaoAberta: false
                    uf: "MG"
                PeriodoEmUtilizacao:
                  summary: Dilatação não permitida, ainda existe período de utilização
                  value:
                    mensagem: "Operação não disponível para este contrato."
                    codigo: 2
                    possuiDilatacaoAberta: false
                    uf: "MG"
                
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

  'v1/contrato/consultaEstudante':
    get:
      summary: Busca informações do contrato FIES do estudante por CPF (via corpo da requisição)
      description: |
        Este endpoint permite consultar os detalhes do contrato do Fundo de Financiamento Estudantil (FIES)
        de um estudante específico, utilizando o seu número de Cadastro de Pessoa Física (CPF).
        Os parâmetros são enviados no corpo da requisição.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                cpf:
                  type: string
                  pattern: '^[0-9]{11}$'
                  description: CPF do estudante a ser consultado (apenas números).
                  example: "70966798120"
                codigoFies:
                  type: string
                  required: false
                  description: Código FIES do estudante (opcional).
                  example: "0000"
                agencia:
                  type: string
                  required: false
                  description: Código da agência bancária (opcional).
                  example: "0"
                _:
                  type: integer
                  required: false
                  description: Timestamp para evitar cache (gerado dinamicamente).
                  example: 1747058495351
      responses:
        '200':
          description: Resposta bem-sucedida com os detalhes do contrato do estudante.
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
                    description: Informações detalhadas do estudante.
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
                        description: Detalhes da identidade do estudante.
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
                    # (Detailed IES object structure - truncated for brevity, see full response for details)
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
                    # (Detailed estudanteCurso object structure - truncated for brevity, see full response for details)
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
  'v1/emprest/combo/buscar-combo-todas-Ies':
  get:
    summary: Busca informações de Instituições de Ensino (IES)
    description: Retorna uma lista de IES com base nos critérios de busca.
    parameters:
      - in: query
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
  'V1/emprest/combo/consultar-campus-transferencia':
   get:
    summary: Busca campus de transferência para uma IES, semestre e ano de referência.
    description: Retorna uma lista de campus disponíveis para transferência com base nos parâmetros fornecidos.
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
          enum: [1, 2] # Assumindo que os semestres são 1 ou 2
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
  '/emprest/combo/consultar-Curso-PorCampusOfertaRes35':
   get:
    summary: Busca cursos disponíveis em um campus de oferta para transferência.
    description: Retorna uma lista de cursos disponíveis para transferência com base nos parâmetros fornecidos.
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
          # Você pode adicionar um enum se souber os valores possíveis para turno (ex: [1, 2, 3])
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
  'v1/emprest/combo/consultarTurnosRes35':
   get:
    summary: Busca turnos disponíveis para um curso em um campus de destino.
    description: Retorna uma lista de turnos disponíveis com base nos parâmetros fornecidos.
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
  'v1/emprest/transferenciaContrato/buscarCursoDestino':
   get:
    summary: Busca informações do curso de destino para transferência de contrato.
    description: Retorna detalhes sobre o curso de destino com base nos parâmetros fornecidos.
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
          # Você pode adicionar um enum se souber os valores possíveis para turno (ex: [1, 2, 3])
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
          enum: [1, 2] # Assumindo que os semestres são 1 ou 2
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
  '/emprest/transferenciaContrato/confirmarSolicitacaoEstudante':
   get:
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
