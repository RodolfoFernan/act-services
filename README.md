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

servers:
  - url: http://localhost:8080/api

paths:
  /v1/buscar-estudante-transferencia/{cpf}:
    get:
      summary: Busca informações do contrato FIES do estudante por CPF
      description: |
        Este endpoint permite consultar os detalhes do contrato do Fundo de Financiamento Estudantil (FIES)
        de um estudante específico, utilizando o seu número de Cadastro de Pessoa Física (CPF).
      parameters:
        - in: path
          name: cpf
          schema:
            type: string
            pattern: '^[0-9]{11}$'
          required: true
          description: CPF do estudante a ser consultado (apenas números).
          example: "70966798120"
      responses:
        '200':
          description: Resposta bem-sucedida com os detalhes do contrato do estudante.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EstudanteFiesResponse'
              examples:
                successExample:
                  summary: Exemplo de resposta de sucesso para contrato FIES
                  value:
                    mensagem: ""
                    codigo: null
                    tipo: null
                    editavel: null
                    agencia: 4736
                    estudante:
                      mensagem: ""
                      codigo: null
                      tipo: null
                      editavel: null
                      cpf: "70966798120"
                      dependenteCPF: null
                      nome: "FULANO DE TAL DA SILVA"
                      dataNascimento: "1995-01-15"
                      ric: null
                      nacionalidade: "BRASILEIRA"
                      identidade: "MG-12345678"
                      estadoCivil: "SOLTEIRO"
                      regimeBens: null
                      endereco:
                        cep: "30000-000"
                        logradouro: "RUA PRINCIPAL"
                        numero: "123"
                        complemento: "APTO 101"
                        bairro: "CENTRO"
                        cidade: "BELO HORIZONTE"
                        uf: "MG"
                      contato:
                        email: "fulano.silva@email.com"
                        telefoneFixo: "3133333333"
                        telefoneCelular: "31999999999"
                      vinculacao: "NOVO_FIES"
                      codigoFies: "F0000000001"
                      sexo: "MASCULINO"
                      pis: "123.45678.90-1"
                      conjuge: null
                      responsavelLegal: null
                      emancipado: false
                      nomeCandidato: "FULANO DE TAL DA SILVA"
                      nomeCurso: "ENGENHARIA DE COMPUTAÇÃO"
                      idCampus: 1234
                      nomeCampus: "CAMPUS CENTRAL"
                      numeroCandidato: "C000001"
                      descricaoMunicipio: "BELO HORIZONTE"
                      nomeIes: "UNIVERSIDADE FICTICIA"
                      ufCampus: "MG"
                      contaCorrente:
                        codigoBanco: 104
                        nomeBanco: "CAIXA ECONOMICA FEDERAL"
                        agencia: "0400"
                        dvAgencia: "0"
                        conta: "0000000001"
                        dvConta: "0"
                        tipoConta: "CONTA_CORRENTE"
                      permiteLiquidar: true
                      voucher: null
                      dataValidadeVoucher: null
                      motivoImpeditivo: null
                      inadimplente: false
                      atrasado: false
                      liquidado: false
                      rendaFamiliar: 5000.00
                      recebeSms: true
                      vinculoSolidario: false
                      contratoEstudante:
                        ies: null
                        codigoStatusContrato: "ATIVO"
                        numeroOperacaoSIAPI: "1234567"
                        statusContrato: "ATIVO"
                        situacaoContrato: "ATIVO"
                        dataLimiteContratacao: "2025-12-31"
                        valorMensalidade: 800.00
                        valorContrato: 50000.00
                        dataAssinatura: "2023-08-01"
                        percentualFinanciamento: 0.8
                        numeroContrato: "C0000000001"
                        diaVencimento: 10
                        codigoTipoGarantia: "FGP"
                        descricaoTipoGarantia: "Fundo Garantidor de Operações"
                        valorGarantia: 1000.00
                        codCurso: 123
                        semestreCursados: 4
                        estudanteCurso: null
                        valorAditamento: 2000.00
                        unidadeCaixa: "UNIDADE CENTRAL"
                        prazoContratoMec: 8
                        semestreReferencia: "2023.2"
                        anoReferencia: 2023
                        bloqueioMec: false
                        permiteContratacao: true
                        recebeInformacao: true
                        recebeSms: true
                        localExtrato: "ONLINE"
                        prouni: false
                        contaCorrente: null
                        quantidadeAditamentos: 2
                        quantidadePreAditamentos: 1
                        sipesListaBanco: []
                        idSeguradora: null
                        indContratoNovoFies: true
                        taxaJuros: 0.03
                        existeTarifaContrato: false
                        vrCoParticipacao: 50.00
                        valorSeguro: 20.00
                        numeroProcessoSeletivo: "PS2023.2"

        '400':
          description: Requisição inválida (ex: CPF em formato incorreto).
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              examples:
                invalidCpf:
                  summary: Erro de CPF inválido
                  value:
                    message: "CPF inválido. O CPF deve conter 11 dígitos numéricos."
        '404':
          description: Estudante não encontrado.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              examples:
                notFound:
                  summary: Estudante não encontrado
                  value:
                    message: "Estudante com o CPF informado não foi encontrado."
        '500':
          description: Erro interno do servidor.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              examples:
                internalError:
                  summary: Erro interno do servidor
                  value:
                    message: "Ocorreu um erro inesperado no servidor."

---
components:
  schemas:
    ErrorResponse:
      type: object
      properties:
        message:
          type: string
          description: Mensagem de erro.
          example: "Ocorreu um erro."

    EstudanteFiesResponse:
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
          $ref: '#/components/schemas/EstudanteFies'
      required:
        - estudante

    EstudanteFies:
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
        cpf:
          type: string
          description: CPF do estudante.
          example: "70966798120"
        dependenteCPF:
          type: string
          nullable: true
          example: null
        nome:
          type: string
          example: "FULANO DE TAL DA SILVA"
        dataNascimento:
          type: string
          format: date
          example: "1995-01-15"
        ric:
          type: string
          nullable: true
          example: null
        nacionalidade:
          type: string
          example: "BRASILEIRA"
        identidade:
          type: string
          example: "MG-12345678"
        estadoCivil:
          type: string
          example: "SOLTEIRO"
        regimeBens:
          type: string
          nullable: true
          example: null
        endereco:
          $ref: '#/components/schemas/Endereco'
        contato:
          $ref: '#/components/schemas/Contato'
        vinculacao:
          type: string
          example: "NOVO_FIES"
        codigoFies:
          type: string
          example: "F0000000001"
        sexo:
          type: string
          example: "MASCULINO"
        pis:
          type: string
          example: "123.45678.90-1"
        conjuge:
          $ref: '#/components/schemas/Conjuge'
          nullable: true
        responsavelLegal:
          $ref: '#/components/schemas/ResponsavelLegal'
          nullable: true
        emancipado:
          type: boolean
          example: false
        nomeCandidato:
          type: string
          example: "FULANO DE TAL DA SILVA"
        nomeCurso:
          type: string
          example: "ENGENHARIA DE COMPUTAÇÃO"
        idCampus:
          type: integer
          example: 1234
        nomeCampus:
          type: string
          example: "CAMPUS CENTRAL"
        numeroCandidato:
          type: string
          example: "C000001"
        descricaoMunicipio:
          type: string
          example: "BELO HORIZONTE"
        nomeIes:
          type: string
          example: "UNIVERSIDADE FICTICIA"
        ufCampus:
          type: string
          example: "MG"
        contaCorrente:
          $ref: '#/components/schemas/ContaCorrente'
        permiteLiquidar:
          type: boolean
          example: true
        voucher:
          type: string
          nullable: true
          example: null
        dataValidadeVoucher:
          type: string
          format: date
          nullable: true
          example: null
        motivoImpeditivo:
          type: string
          nullable: true
          example: null
        inadimplente:
          type: boolean
          example: false
        atrasado:
          type: boolean
          example: false
        liquidado:
          type: boolean
          example: false
        rendaFamiliar:
          type: number
          format: float
          example: 5000.00
        recebeSms:
          type: boolean
          example: true
        vinculoSolidario:
          type: boolean
          example: false
        contratoEstudante:
          $ref: '#/components/schemas/ContratoEstudante'
      required:
        - cpf
        - nome
        - dataNascimento
        - nacionalidade
        - identidade
        - estadoCivil
        - endereco
        - contato
        - vinculacao
        - codigoFies
        - sexo
        - pis
        - emancipado
        - nomeCandidato
        - nomeCurso
        - idCampus
        - nomeCampus
        - numeroCandidato
        - descricaoMunicipio
        - nomeIes
        - ufCampus
        - contaCorrente
        - permiteLiquidar
        - inadimplente
        - atrasado
        - liquidado
        - rendaFamiliar
        - recebeSms
        - vinculoSolidario
        - contratoEstudante

    Endereco:
      type: object
      properties:
        cep:
          type: string
          example: "30000-000"
        logradouro:
          type: string
          example: "RUA PRINCIPAL"
        numero:
          type: string
          example: "123"
        complemento:
          type: string
          nullable: true
          example: "APTO 101"
        bairro:
          type: string
          example: "CENTRO"
        cidade:
          type: string
          example: "BELO HORIZONTE"
        uf:
          type: string
          example: "MG"
      required:
        - cep
        - logradouro
        - numero
        - bairro
        - cidade
        - uf

    Contato:
      type: object
      properties:
        email:
          type: string
          format: email
          example: "fulano.silva@email.com"
        telefoneFixo:
          type: string
          nullable: true
          example: "3133333333"
        telefoneCelular:
          type: string
          example: "31999999999"
      required:
        - email
        - telefoneCelular

    Conjuge:
      type: object
      properties:
        cpf:
          type: string
          example: "11122233344"
        nome:
          type: string
          example: "CICLANA DE TAL"
        dataNascimento:
          type: string
          format: date
          example: "1996-05-20"
      required:
        - cpf
        - nome
        - dataNascimento

    ResponsavelLegal:
      type: object
      properties:
        cpf:
          type: string
          example: "55566677788"
        nome:
          type: string
          example: "BELTRANO DE TAL"
        tipo:
          type: string
          example: "PAI"
      required:
        - cpf
        - nome
        - tipo

    ContaCorrente:
      type: object
      properties:
        codigoBanco:
          type: integer
          example: 104
        nomeBanco:
          type: string
          example: "CAIXA ECONOMICA FEDERAL"
        agencia:
          type: string
          example: "0400"
        dvAgencia:
          type: string
          example: "0"
        conta:
          type: string
          example: "0000000001"
        dvConta:
          type: string
          example: "0"
        tipoConta:
          type: string
          enum: ["CONTA_CORRENTE", "CONTA_POUPANCA"]
          example: "CONTA_CORRENTE"
      required:
        - codigoBanco
        - nomeBanco
        - agencia
        - conta
        - dvConta
        - tipoConta

    ContratoEstudante:
      type: object
      properties:
        ies:
          type: string
          nullable: true
          example: null
        codigoStatusContrato:
          type: string
          example: "ATIVO"
        numeroOperacaoSIAPI:
          type: string
          example: "1234567"
        statusContrato:
          type: string
          example: "ATIVO"
        situacaoContrato:
          type: string
          example: "ATIVO"
        dataLimiteContratacao:
          type: string
          format: date
          example: "2025-12-31"
        valorMensalidade:
          type: number
          format: float
          example: 800.00
        valorContrato:
          type: number
          format: float
          example: 50000.00
        dataAssinatura:
          type: string
          format: date
          example: "2023-08-01"
        percentualFinanciamento:
          type: number
          format: float
          example: 0.8
        numeroContrato:
          type: string
          example: "C0000000001"
        diaVencimento:
          type: integer
          example: 10
        codigoTipoGarantia:
          type: string
          example: "FGP"
        descricaoTipoGarantia:
          type: string
          example: "Fundo Garantidor de Operações"
        valorGarantia:
          type: number
          format: float
          example: 1000.00
        codCurso:
          type: integer
          example: 123
        semestreCursados:
          type: integer
          example: 4
        estudanteCurso:
          type: string
          nullable: true
          example: null
        valorAditamento:
          type: number
          format: float
          example: 2000.00
        unidadeCaixa:
          type: string
          example: "UNIDADE CENTRAL"
        prazoContratoMec:
          type: integer
          example: 8
        semestreReferencia:
          type: string
          example: "2023.2"
        anoReferencia:
          type: integer
          example: 2023
        bloqueioMec:
          type: boolean
          example: false
        permiteContratacao:
          type: boolean
          example: true
        recebeInformacao:
          type: boolean
          example: true
        recebeSms:
          type: boolean
          example: true
        localExtrato:
          type: string
          example: "ONLINE"
        prouni:
          type: boolean
          example: false
        contaCorrente:
          # Se este campo for a mesma estrutura de ContaCorrente, referencie-o
          # Caso contrário, defina inline ou crie um novo schema
          type: string # Mantido como string no exemplo, mas poderia ser $ref: '#/components/schemas/ContaCorrente'
          nullable: true
          example: null
        quantidadeAditamentos:
          type: integer
          example: 2
        quantidadePreAditamentos:
          type: integer
          example: 1
        sipesListaBanco:
          type: array
          items:
            type: string # Exemplo: tipo de item na lista
          example: [] # Exemplo de lista vazia
        idSeguradora:
          type: string
          nullable: true
          example: null
        indContratoNovoFies:
          type: boolean
          example: true
        taxaJuros:
          type: number
          format: float
          example: 0.03
        existeTarifaContrato:
          type: boolean
          example: false
        vrCoParticipacao:
          type: number
          format: float
          example: 50.00
        valorSeguro:
          type: number
          format: float
          example: 20.00
        numeroProcessoSeletivo:
          type: string
          example: "PS2023.2"
      required:
        - codigoStatusContrato
        - numeroOperacaoSIAPI
        - statusContrato
        - situacaoContrato
        - dataLimiteContratacao
        - valorMensalidade
        - valorContrato
        - dataAssinatura
        - percentualFinanciamento
        - numeroContrato
        - diaVencimento
        - codigoTipoGarantia
        - descricaoTipoGarantia
        - valorGarantia
        - codCurso
        - semestreCursados
        - valorAditamento
        - unidadeCaixa
        - prazoContratoMec
        - semestreReferencia
        - anoReferencia
        - bloqueioMec
        - permiteContratacao
        - recebeInformacao
        - recebeSms
        - localExtrato
        - prouni
        - quantidadeAditamentos
        - quantidadePreAditamentos
        - sipesListaBanco
        - indContratoNovoFies
        - taxaJuros
        - existeTarifaContrato
        - vrCoParticipacao
        - valorSeguro
        - numeroProcessoSeletivo


Schema error at paths['/v1/buscar-estudante-transferencia/{cpf}'].get.responses['200']
should have required property '$ref'
missingProperty: $ref
Jump to line 70
Schema error at paths['/v1/buscar-estudante-transferencia/{cpf}'].get.responses['200']
should match exactly one schema in oneOf
Jump to line 70
Schema error at paths['/v1/buscar-estudante-transferencia/{cpf}'].get.responses['200'].content['application/json']
should have required property 'examples'
missingProperty: examples
Jump to line 73
Schema error at paths['/v1/buscar-estudante-transferencia/{cpf}'].get.responses['200'].content['application/json']
should match exactly one schema in oneOf
Jump to line 73
Schema error at paths['/v1/buscar-estudante-transferencia/{cpf}'].get.responses['200'].content['application/json'].schema
should NOT have additional properties
additionalProperty: $ref, examples
Jump to line 74
Schema error at paths['/v1/buscar-estudante-transferencia/{cpf}'].get.responses['200'].content['application/json'].schema.$ref
should be string
Jump to line 74
Parser error 
bad indentation of a mapping entry
Jump to line 76
Schema error at paths['/v1/buscar-estudante-transferencia/{cpf}'].get.responses['200'].content['application/json'].schema.properties['estudante']
should NOT have additional properties
additionalProperty: ies, codigoStatusContrato, numeroOperacaoSIAPI, statusContrato, situacaoContrato, dataLimiteContratacao, valorMensalidade, valorContrato, dataAssinatura, percentualFinanciamento, numeroContrato, diaVencimento, codigoTipoGarantia, descricaoTipoGarantia, valorGarantia, codCurso, semestreCursados, estudanteCurso, valorAditamento, unidadeCaixa, prazoContratoMec, semestreReferencia, anoReferencia, bloqueioMec, permiteContratacao, recebeInformacao, recebeSms, localExtrato, prouni, contaCorrente, quantidadeAditamentos, quantidadePreAditamentos, sipesListaBanco, idSeguradora, indContratoNovoFies, taxaJuros, existeTarifaContrato, vrCoParticipacao, valorSeguro, numeroProcessoSeletivo
Jump to line 102
Schema error at paths['/v1/buscar-estudante-transferencia/{cpf}'].get.responses['200'].content['application/json'].schema.properties['estudante'].properties['mensagem']
should NOT have additional properties
additionalProperty: codigo, tipo, editavel, cpf, dependenteCPF, nome, dataNascimento, ric, nacionalidade, identidade, estadoCivil, regimeBens, endereco, contato, vinculacao, codigoFies, sexo, pis, conjuge, responsavelLegal, emancipado, nomeCandidato, nomeCurso, idCampus, nomeCampus, numeroCandidato, descricaoMunicipio, nomeIes, ufCampus, contaCorrente, permiteLiquidar, voucher, dataValidadeVoucher, motivoImpeditivo, inadimplente, atrasado, liquidado, rendaFamiliar, recebeSms, vinculoSolidario, contratoEstudante
Jump to line 106
paths:
  '/v1/buscar-estudante-transferencia/{cpf}':
   get:
    summary: Busca informações do contrato FIES do estudante por CPF (via query parameters)
    description: |
      Este endpoint permite consultar os detalhes do contrato do Fundo de Financiamento Estudantil (FIES)
      de um estudante específico, utilizando o seu número de Cadastro de Pessoa Física (CPF).
      Os parâmetros são enviados na URL como query parameters.
    parameters:
      - in : path
        name: cpf
        schema:
          type: string
          pattern: '^[0-9]{11}$'
        description: CPF do estudante a ser consultado (apenas números).
        required: true
        example: "70966798120"
      
    responses:
      '200':
        description: Resposta bem-sucedida com os detalhes do contrato do estudante.
        content:
          application/json:
            schema:
              $ref:'#'
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
