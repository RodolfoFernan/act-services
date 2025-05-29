<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>

    
responses:
      '200':
        description: Resposta bem-sucedida com os detalhes do contrato do estudante e dados .
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
                    codigo: 0
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

  /v1/contrato/buscar-estudante/{cpf}/{codigoFies}:
   get:
    summary: Busca informações do contrato FIES do estudante por CPF (via query parameters) traz histórico.
    description: |
      Este endpoint permite consultar os detalhes do contrato do Fundo de Financiamento Estudantil  trazendo informações como histórico
      
    parameters:
      - in: path
        name: cpf
        schema:
          type: string
          pattern: '^[0-9]{11}$'
        description: CPF do estudante a ser consultado (apenas números).
        required: true
        example: "70966798120"
      - in: path
        name: codigoFies
        schema:
          type: string
        required: true
        description: Código FIES do estudante (opcional).
        example: "213645"
      
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
                  example: ""
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
