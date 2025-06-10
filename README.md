<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Procedimento FES.FESSPZ41_CRISE19_FIM_RETENC_2

Este procedimento tem como objetivo principal encerrar retenções em liberações que atendem a critérios específicos, principalmente relacionados a transferências e aditamentos de contratos.
Inicialização do Procedimento

O procedimento começa com a definição de uma variável SQL_QUERY para armazenar comandos SQL dinâmicos. Em seguida, ele redefine os formatos de data e timestamp da sessão, o que é crucial para garantir que as operações de data sejam executadas corretamente, independentemente das configurações padrão do banco de dados.

    Comandos de Sessão:
        ALTER SESSION SET NLS_DATE_FORMAT = 'DD/MM/YYYY'
        ALTER SESSION SET NLS_TIMESTAMP_FORMAT = 'DD/MM/YYYY'

Etapa 19: Encerramento de Retenções por Transferência (Situação 'NR')

Objetivo: Identificar e finalizar retenções de liberações que estavam na situação 'NR' (Não Repassada) devido a uma transferência de contrato concluída (status 5), e que já possuem um aditamento ou foram originadas em um determinado semestre de admissão. Isso permite que essas liberações sejam processadas no próximo repasse.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            IC_SITUACAO_LIBERACAO
            NU_IES
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 2 para transferência)
            DT_FIM_RETENCAO
        FES.FESTB049_TRANSFERENCIA (aliás T)
            NU_CANDIDATO_FK10
            NU_STATUS_TRANSFERENCIA (filtrado por 5 para status concluído)
            NU_CAMPUS_ORIGEM_FK161
            NU_CAMPUS_DESTINO_FK161
            AA_REFERENCIA
            NU_SEM_REFERENCIA
            NU_SEQ_TRANSFERENCIA
            DT_INCLUSAO
        FES.FESTB154_CAMPUS_INEP (aliás C para campus de origem, I para campus de destino)
            NU_CAMPUS
            NU_IES_FK155
        FES.FESTB038_ADTMO_CONTRATO (aliás A, LEFT OUTER JOIN)
            NU_CANDIDATO_FK36
            DT_ADITAMENTO
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
        FES.FESTB010_CANDIDATO (aliás CA, LEFT OUTER JOIN)
            NU_SEQ_CANDIDATO
            DT_ADMISSAO_CANDIDATO
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Etapa 20: Encerramento de Retenções por Transferência (Situação Diferente de 'NR')

Objetivo: Complementar a etapa anterior, finalizando as retenções de liberações cujo motivo é transferência (motivo 2), mas que já não estão mais na situação 'NR' (Não Repassada). Isso assegura que nenhuma retenção de transferência permaneça ativa em liberações que já foram reprocessadas ou tiveram sua situação alterada por outros meios.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            IC_SITUACAO_LIBERACAO (filtrado para ser diferente de 'NR')
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 2 para transferência)
            DT_FIM_RETENCAO
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)


Você me forneceu mais um trecho de código PL/SQL, que continua o procedimento FES.FESSPZ41_CRISE19_FIM_RETENC_2. Este trecho foca no encerramento de retenções por motivos de suspensão e ausência de aditamento válido.

Vamos analisar as próximas etapas:
Etapa 21: Encerramento de Retenções por Suspensão (Completa)

Objetivo: Identificar e finalizar retenções de liberações que estavam suspensas, mas que não possuem mais uma ocorrência de suspensão ativa (NU_STATUS_OCORRENCIA = 11) para o semestre de referência da liberação. Isso indica que a suspensão foi resolvida, permitindo que a liberação prossiga.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 3 para suspensão)
            DT_FIM_RETENCAO
        FES.FESTB057_OCRRA_CONTRATO (aliás O, LEFT OUTER JOIN)
            NU_CANDIDATO_FK36
            IC_TIPO_OCORRENCIA (filtrado por 'S' para suspensão)
            NU_STATUS_OCORRENCIA (filtrado por 11 para status de ocorrência)
            AA_REFERENCIA
            NU_SEMESTRE_REFERENCIA
    Lógica: O LEFT OUTER JOIN com FESTB057_OCRRA_CONTRATO e a condição WHERE O.NU_CANDIDATO_FK36 IS NULL são cruciais aqui. Eles buscam liberações que possuem uma retenção de suspensão ativa (R.DT_FIM_RETENCAO IS NULL e R.NU_MOTIVO_RETENCAO_LIBERACAO = 3), mas que não têm mais uma ocorrência de suspensão correspondente e ativa. Isso significa que a condição que causou a retenção (a suspensão) foi removida ou finalizada em outra parte do sistema.
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Etapa 22: Encerramento de Retenções por Suspensão Parcial e Atualização da Situação

Objetivo: Identificar liberações que estão retidas por suspensão parcial e, dependendo da sua situação atual, atualizar o IC_SITUACAO_LIBERACAO para 'S' (Suspenso) e finalizar a retenção. Isso é aplicado a liberações que deveriam ter sido suspensas parcialmente durante um período específico.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            IC_SITUACAO_LIBERACAO
            DT_LIBERACAO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 3 para suspensão)
            DT_FIM_RETENCAO
        FES.FESTB057_OCRRA_CONTRATO (aliás O)
            NU_CANDIDATO_FK36
            IC_TIPO_OCORRENCIA (filtrado por 'S' para suspensão)
            NU_STATUS_OCORRENCIA (filtrado por 11 para status de ocorrência)
            IC_TIPO_SUSPENSAO (filtrado por 'P' para suspensão parcial)
            AA_REFERENCIA
            NU_SEMESTRE_REFERENCIA
            DT_OCORRENCIA
            DT_INICIO_VIGENCIA
            DT_FIM_VIGENCIA
    Lógica:
        O cursor seleciona liberações com retenção de motivo 3 (suspensão) e sem data de fim de retenção.
        Ele junta com ocorrências de contrato que são suspensões parciais (IC_TIPO_SUSPENSAO = 'P') com status 11 e que se alinham com o ano/semestre da liberação.
        Há uma verificação de datas complexa para o DT_OCORRENCIA e DT_FIM_VIGENCIA da ocorrência, garantindo que as datas correspondam aos semestres.
        A condição DT_INICIO_VIGENCIA = LAST_DAY(DT_OCORRENCIA) + 1 parece indicar que a vigência da suspensão parcial começa no dia 1 do mês seguinte ao da ocorrência.
        Dentro do loop, se a liberação está na situação 'NR' e sua DT_LIBERACAO está entre DT_INICIO_VIGENCIA e DT_FIM_VIGENCIA da ocorrência de suspensão, a situação da liberação (IC_SITUACAO_LIBERACAO) é atualizada para 'S' (Suspenso).
        Finalmente, a retenção de suspensão para essa liberação é finalizada.
    Tabelas Afetadas (Escrita):
        FES.FESTB712_LIBERACAO_CONTRATO (Atualização do IC_SITUACAO_LIBERACAO e DT_ATUALIZACAO)
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Etapa 23: Encerramento de Retenções por Falha de Vinculação

Objetivo: Finalizar retenções de liberações cujo motivo é falha na vinculação entre tabelas (NU_MOTIVO_RETENCAO_LIBERACAO = 4), mas que já possuem um NU_TIPO_TRANSACAO preenchido. Isso sugere que a vinculação foi corrigida, permitindo o prosseguimento da liberação.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_TIPO_TRANSACAO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 4 para falha de vinculação)
            DT_FIM_RETENCAO
    Lógica: O cursor busca liberações que têm uma retenção ativa por "falha na vinculação entre tabelas" e verifica se o campo NU_TIPO_TRANSACAO da liberação já não é NULL. Se NU_TIPO_TRANSACAO não é NULL, presume-se que a falha foi resolvida, e a retenção é finalizada.
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Etapa 24: Encerramento de Retenções por Ausência de Aditamento Válido

Objetivo: Finalizar retenções de liberações que foram retidas por ausência de aditamento válido (NU_MOTIVO_RETENCAO_LIBERACAO = 7), mas que agora possuem um aditamento válido ou um contrato FIES com status adequado.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 7 para ausência de aditamento)
            DT_FIM_RETENCAO
        FES.FESTB038_ADTMO_CONTRATO (aliás A, LEFT OUTER JOIN)
            NU_CANDIDATO_FK36
            NU_STATUS_ADITAMENTO (filtrado por > 3 para aditamento válido)
            DT_ADITAMENTO
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
        FES.FESTB010_CANDIDATO (aliás CA, LEFT OUTER JOIN)
            NU_SEQ_CANDIDATO
            DT_ADMISSAO_CANDIDATO
        FES.FESTB036_CONTRATO_FIES (aliás F, LEFT OUTER JOIN)
            NU_CANDIDATO_FK11
            NU_STATUS_CONTRATO (filtrado por > 3 para contrato válido)
            DT_ASSINATURA
    Lógica: O cursor busca liberações com retenção ativa por "ausência de aditamento válido". A condição WHERE verifica se existe um aditamento válido (A.NU_CANDIDATO_FK36 IS NOT NULL) OU se existe um registro de candidato com data de admissão e um contrato FIES válido associado (CA.NU_SEQ_CANDIDATO IS NOT NULL AND F.NU_CANDIDATO_FK11 IS NOT NULL). Se alguma dessas condições for verdadeira, a retenção é finalizada.
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Cada bloco de código termina com um COMMIT;, o que significa que as alterações são persistidas no banco de dados após a conclusão de cada loop de atualização.

Há mais algum trecho desse procedimento, ou algum outro procedimento que você gostaria de analisar?



tapa 25: Encerramento de Retenções por Ausência de Aditamento Válido (Situações Específicas)

Objetivo: Finalizar retenções de liberações que foram marcadas com motivo 7 (Ausência de Aditamento Válido) e cuja situação atual da liberação (IC_SITUACAO_LIBERACAO) não seja 'NR' (Não Repassada) ou 'R' (Repassada).

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            IC_SITUACAO_LIBERACAO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 7)
            DT_FIM_RETENCAO
    Lógica: Este bloco busca especificamente por retenções de motivo 7 que ainda estão ativas (DT_FIM_RETENCAO IS NULL). A condição crucial é L.IC_SITUACAO_LIBERACAO NOT IN ('NR', 'R'). Isso implica que se a liberação não está mais em um status de "Não Repassada" ou "Repassada" (ou seja, ela foi processada ou teve sua situação alterada de alguma outra forma), a retenção de "Ausência de Aditamento Válido" não é mais relevante e deve ser encerrada.
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Etapa 26: Encerramento de Retenções por Análise de Liberações a Estornar (Situação Diferente de 'NE')

Objetivo: Finalizar retenções de liberações que estavam sob análise para estorno (motivo 5), mas que não estão mais na situação 'NE' (Não Enviada). Isso sugere que a análise foi concluída ou que a liberação mudou de estado, tornando a retenção desnecessária.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            IC_SITUACAO_LIBERACAO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 5)
            DT_FIM_RETENCAO
    Lógica: Este bloco visa liberações com retenção ativa de motivo 5 (Análise de Liberações a Estornar). Se a situação da liberação (IC_SITUACAO_LIBERACAO) não for mais 'NE', a retenção é finalizada.
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Etapa 27: Encerramento de Retenções por Análise de Liberações a Estornar (Condições Complexas)

Objetivo: Finalizar retenções de liberações que estavam sob análise para estorno (motivo 5) e que estão na situação 'NE' (Não Enviada), mas que satisfazem um conjunto complexo de condições sobre a existência ou ausência de aditamentos, contratos FIES, ou ocorrências de suspensão/estorno.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            IC_SITUACAO_LIBERACAO (filtrado por 'NE')
            DT_LIBERACAO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 5)
            DT_FIM_RETENCAO
        FES.FESTB038_ADTMO_CONTRATO (aliás A, LEFT OUTER JOIN)
            NU_CANDIDATO_FK36
            NU_STATUS_ADITAMENTO (filtrado por > 3)
        FES.FESTB010_CANDIDATO (aliás C, LEFT OUTER JOIN)
            NU_SEQ_CANDIDATO
        FES.FESTB036_CONTRATO_FIES (aliás F, LEFT OUTER JOIN)
            NU_CANDIDATO_FK11
            NU_STATUS_CONTRATO (filtrado por > 3)
        FES.FESTB057_OCRRA_CONTRATO (aliás O e OC, LEFT OUTER JOIN, com diferentes filtros para suspensão parcial 'S' e estorno 'E')
            NU_CANDIDATO_FK36
            IC_TIPO_OCORRENCIA
            IC_TIPO_SUSPENSAO
            NU_STATUS_OCORRENCIA (filtrado por 11)
            AA_REFERENCIA
            NU_SEMESTRE_REFERENCIA
            DT_OCORRENCIA
            DT_FIM_VIGENCIA
            DT_INICIO_VIGENCIA

    Lógica: Este é o bloco mais complexo até agora. Ele busca liberações que estão na situação 'NE' e que ainda têm uma retenção ativa por motivo 5. A condição WHERE avalia se:
        Não há um aditamento válido E não há um registro de candidato válido, OU
        Existe um registro de candidato, mas não há um contrato FIES válido, OU
        Existe uma ocorrência de suspensão parcial (O.NU_CANDIDATO_FK36 IS NOT NULL), OU
        Existe uma ocorrência de estorno (OC.NU_CANDIDATO_FK36 IS NOT NULL).

    Se qualquer uma dessas condições (que parecem indicar que a liberação ainda não está "pronta" para ser estornada ou que há uma pendência a ser resolvida que a impede de ser processada normalmente) for verdadeira, a retenção de análise de estorno é finalizada. Isso pode parecer contraintuitivo, mas o objetivo de "finalizar a retenção" aqui é, provavelmente, remover a marca de "análise de estorno" porque outra condição (ou a ausência de condições necessárias) já indica que a liberação não será estornada ou que a análise não é mais aplicável sob aquelas circunstâncias.

    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Todos esses blocos operam em conjunto para gerenciar o ciclo de vida das retenções de liberações, garantindo que elas sejam encerradas quando as condições que as originaram não são mais válidas ou quando outras situações determinam que a retenção deve ser desfeita.

Você tem mais algum trecho desse procedimento ou outro que gostaria de analisar?


O Gemini pode cometer erros, por isso, verifique as informações



Etapa 28: Encerramento de Retenções por Divergência entre Repasse e Aditamento

Objetivo: Finalizar retenções de liberações que foram marcadas com motivo 9 (Divergência entre Repasse e Aditamento) e que, após uma reavaliação, mostram que a soma dos repasses está muito próxima do valor do aditamento (dentro de uma margem de $1) para o semestre em questão.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
        FES.FESTB038_ADTMO_CONTRATO (unido em uma subconsulta 'D')
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            NU_STATUS_ADITAMENTO (filtrado por > 3 para aditamento válido)
            VR_ADITAMENTO
        FES.FESTB817_RETENCAO_LIBERACAO (aliás R, em uma subconsulta EXISTS)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 9)
            DT_FIM_RETENCAO
    Lógica:
        A subconsulta D calcula a soma dos VR_REPASSE por candidato, ano e semestre, e compara com o VR_ADITAMENTO do aditamento correspondente.
        A condição HAVING é crucial: ( ( SUM(VR_REPASSE) - VR_ADITAMENTO ) BETWEEN 0 AND 1 OR ( VR_ADITAMENTO - SUM(VR_REPASSE) ) BETWEEN 0 AND 1 ). Isso significa que a diferença entre o total repassado e o valor do aditamento deve ser de no máximo $1 (para cima ou para baixo), indicando que a divergência foi resolvida ou é insignificante.
        A condição COUNT(VR_REPASSE) = 6 sugere que a liberação deve ter 6 parcelas de repasse para ser considerada.
        O INNER JOIN com a subconsulta D filtra as liberações que atendem a essa condição de "quase igualdade" de valores.
        O EXISTS verifica se a liberação atualmente tem uma retenção ativa de motivo 9.
        Se todas as condições forem satisfeitas, a retenção é finalizada.
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Etapa 29: Encerramento de Retenções por Divergência entre Repasse e Contratação

Objetivo: Finalizar retenções de liberações que foram marcadas com motivo 9 (Divergência entre Repasse e Contratação) e que, após uma reavaliação, mostram que a soma dos repasses está muito próxima do valor do contrato (dentro de uma margem de $1) para o semestre de contratação.

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás L)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            VR_REPASSE
        FES.FESTB036_CONTRATO_FIES (aliás A, unido em uma subconsulta 'D')
            NU_CANDIDATO_FK11
            NU_STATUS_CONTRATO (filtrado por > 3 para contrato válido)
            VR_CONTRATO
        FES.FESTB010_CANDIDATO (aliás C, unido em uma subconsulta 'D')
            NU_SEQ_CANDIDATO
            DT_ADMISSAO_CANDIDATO
        FES.FESTB711_RLTRO_CTRTO_ANLTO (aliás A, LEFT OUTER JOIN)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            VR_REPASSE
        FES.FESTB812_CMPSO_RPSE_INDVO (aliás R, LEFT OUTER JOIN)
            NU_SQNCL_RLTRO_CTRTO_ANALITICO
            NU_TIPO_ACERTO (filtrado por 7)
            IC_COMPENSADO (filtrado por 'N')
    Lógica:
        A subconsulta D é similar à anterior, mas compara a soma dos VR_REPASSE com o VR_CONTRATO para o ano e semestre de admissão do candidato.
        As condições HAVING (( SUM(VR_REPASSE) - VR_CONTRATO ) BETWEEN 0 AND 1 OR ( VR_CONTRATO - SUM(VR_REPASSE) ) BETWEEN 0 AND 1) e COUNT(L.VR_REPASSE) = 6 são as mesmas da etapa anterior, mas aplicadas ao contrato.
        Os LEFT OUTER JOINs com FESTB711_RLTRO_CTRTO_ANLTO e FESTB812_CMPSO_RPSE_INDVO e a condição ( A.NU_SQNCL_LIBERACAO_CONTRATO IS NULL OR R.NU_SQNCL_RLTRO_CTRTO_ANALITICO IS NULL ) sugerem que a retenção é finalizada se não houver um registro analítico de repasse ou um registro de compensação de repasse individual que indique uma pendência.
        O EXISTS verifica a presença de uma retenção ativa de motivo 9.
        Se as condições indicarem que a divergência foi resolvida ou não é mais um problema, a retenção é finalizada.
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Etapa 30: Encerramento de Retenções por Ausência de Finalização no Processo de Aditamento

Objetivo: Finalizar retenções de liberações que foram marcadas com motivo 10 (Ausência de Finalização no Processo de Aditamento), mas cujo processo de aditamento correspondente já está em um status finalizado (NU_SITUACAO_PROCESSO = 9).

    Tabelas Consultadas:
        FES.FESTB712_LIBERACAO_CONTRATO (aliás T712)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_SEQ_CANDIDATO
            AA_REFERENCIA_LIBERACAO
            MM_REFERENCIA_LIBERACAO
            IC_SITUACAO_LIBERACAO (filtrado por 'NR')
        FES.FESTB038_ADTMO_CONTRATO (aliás T38)
            NU_CANDIDATO_FK36
            AA_ADITAMENTO
            NU_SEM_ADITAMENTO
            NU_STATUS_ADITAMENTO (filtrado por IN (4, 5))
        FES.FESTB759_PROCESSO_ADITAMENTO (aliás T759)
            NU_CANDIDATO
            AA_REFERENCIA
            NU_SEMESTRE_REFERENCIA
            NU_SITUACAO_PROCESSO (filtrado por 9)
        FES.FESTB817_RETENCAO_LIBERACAO (aliás T817, em uma subconsulta EXISTS)
            NU_SQNCL_LIBERACAO_CONTRATO
            NU_MOTIVO_RETENCAO_LIBERACAO (filtrado por 10)
            DT_FIM_RETENCAO
    Lógica: Este bloco procura por liberações que ainda não foram repassadas (IC_SITUACAO_LIBERACAO = 'NR') e que possuem uma retenção ativa por motivo 10. A condição principal é que o processo de aditamento associado (FESTB759_PROCESSO_ADITAMENTO) esteja com NU_SITUACAO_PROCESSO = 9, indicando que o aditamento foi finalizado. Se o processo de aditamento está finalizado, a retenção baseada na "ausência de finalização" não faz mais sentido e é encerrada.
    Tabelas Afetadas (Escrita):
        FES.FESTB817_RETENCAO_LIBERACAO (Atualização do DT_FIM_RETENCAO)

Finalização e Tratamento de Exceções

    COMMIT;: Após cada bloco de loops, um COMMIT; é executado, garantindo que as atualizações de DT_FIM_RETENCAO (e IC_SITUACAO_LIBERACAO em um caso específico) sejam gravadas permanentemente no banco de dados.
    Mensagens DBMS_OUTPUT.PUT_LINE: São usadas para depuração e para indicar o progresso do procedimento, informando o início e o fim do processamento de cada tipo de retenção, e o fim geral do procedimento.
    EXCEPTION WHEN OTHERS THEN: Este é o bloco de tratamento de exceções. Se qualquer erro ocorrer durante a execução do procedimento, ele será capturado aqui.
        ROLLBACK;: Todas as alterações feitas desde o último COMMIT (ou desde o início do procedimento, se não houver COMMIT anterior) são desfeitas, garantindo a integridade dos dados.
        DBMS_OUTPUT.PUT_LINE: Mensagens de erro são exibidas, incluindo o código do erro (SQLCODE), a mensagem de erro (SQLERRM), e a instrução SQL que causou o problema (SQL_QUERY). Isso é extremamente útil para depuração em um ambiente de desenvolvimento ou para registro de logs em produção.









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
