<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Etapas do Processo e Tabelas/Campos Envolvidos na SP FES.FESSPZ37_CRISE19_FIM_RETENCAO

Esta Stored Procedure é dividida em duas grandes fases de processamento, uma para retenções de transferência e outra para retenções de suspensão, ambas com o objetivo de finalizar (encerrar) essas retenções.
Etapa 1: Início e Configuração

Esta etapa inicial lida com a exibição de mensagens e a preparação do ambiente da sessão.

    Objetivo: Informar o início da execução da SP e garantir que os formatos de data e hora sejam consistentes.
    Tabelas/Campos Consultados: Nenhuma tabela do banco de dados é consultada nesta etapa.
    Ações:
        Exibe mensagem inicial (DBMS_OUTPUT.PUT_LINE).
        Executa ALTER SESSION SET NLS_DATE_FORMAT = 'DD/MM/YYYY'.
        Executa ALTER SESSION SET NLS_TIMESTAMP_FORMAT = 'DD/MM/YYYY'.

CREATE OR REPLACE PROCEDURE FES.FESSPZ55_CRISE2019_TRATA_SUSP
--F620600  18/06/2021 20:11:44
    --C077033 18/06/2021 16:50:00
--C077033 21/05/2021 19:26:00
--C077033 23/01/2021 20:25:00
--C077033 17/01/2021 22:04:00
--C077033 24/11/2020 18:10:00
--C077033 01/10/2020 19:48:00

AS
    SQL_QUERY VARCHAR2(1000) := NULL;
    COUNT_1 NUMERIC(5) := 0;
    QT_COMPENSACAO_CRIADA NUMERIC(10) := 0;
    V_NU_SQNCL_COMPENSACAO_REPASSE NUMERIC(12);

BEGIN

    DBMS_OUTPUT.PUT_LINE(' ************* INICIO DA FESSPZ55_CRISE2019_TRATA_SUSP');


    --INSERE TIPO DE ACERTO - 6 - REPASSE ANTERIOR CONTRATACAO - NA TB813
    SELECT COUNT(*) INTO COUNT_1
    FROM FES.FESTB813_TIPO_ACERTO_RPSE
    WHERE NU_TIPO_ACERTO = 6;

    IF COUNT_1 = 0
    THEN
        INSERT INTO FES.FESTB813_TIPO_ACERTO_RPSE (NU_TIPO_ACERTO, DE_TIPO_ACERTO)
        VALUES (6,'REPASSE ANTERIOR CONTRATACAO');
        COMMIT;
        DBMS_OUTPUT.PUT_LINE(' ************* INSERCAO TIPO DE ACERTO 6 - REPASSE ANTERIOR CONTRATACAO - NA TB813 ************* ');
    END IF;


    --COMPENSACAO E SUSPENSAO DE TODAS AS LIBERACOES EXISTENTES ANTERIORES AO SEMESTRE DE CONTRATACAO DO CANDIDATO
    SELECT MAX(NU_SQNCL_COMPENSACAO_REPASSE) INTO V_NU_SQNCL_COMPENSACAO_REPASSE FROM FES.FESTB812_CMPSO_RPSE_INDVO;

    FOR X IN
        (
        SELECT
            L.IC_SITUACAO_LIBERACAO,
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB010_CANDIDATO C
                            ON C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO
                                AND
                               (
                                           TO_CHAR(L.DT_LIBERACAO,'YYYY') < TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY')
                                       OR
                                           (
                                                       TO_CHAR(L.DT_LIBERACAO,'YYYY') = TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY')
                                                   AND
                                                       (CASE WHEN TO_CHAR(L.DT_LIBERACAO,'MM') < 7 THEN 1 ELSE 2 END) < (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
                                               )
                                   )
                 LEFT OUTER JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO A
                                 ON L.NU_SQNCL_LIBERACAO_CONTRATO = A.NU_SQNCL_LIBERACAO_CONTRATO
                                     AND L.NU_SEQ_CANDIDATO = A.NU_SEQ_CANDIDATO
                                     AND A.VR_REPASSE > 0
                 LEFT OUTER JOIN FES.FESTB812_CMPSO_RPSE_INDVO R
                                 ON R.NU_SQNCL_RLTRO_CTRTO_ANALITICO = A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND L.IC_SITUACAO_LIBERACAO <> 'S'
          AND R.NU_SQNCL_RLTRO_CTRTO_ANALITICO IS NULL
        )
        LOOP
            IF (X.IC_SITUACAO_LIBERACAO NOT IN ('NR', 'E') AND X.NU_SQNCL_RLTRO_CTRTO_ANALITICO IS NOT NULL) THEN
                QT_COMPENSACAO_CRIADA := QT_COMPENSACAO_CRIADA + 1;
                V_NU_SQNCL_COMPENSACAO_REPASSE := V_NU_SQNCL_COMPENSACAO_REPASSE + 1;

                SQL_QUERY := 'INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (' ||
                             'NU_SQNCL_COMPENSACAO_REPASSE, NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_TIPO_ACERTO, TS_INCLUSAO, CO_USUARIO_INCLUSAO)' ||
                             ' VALUES (' || V_NU_SQNCL_COMPENSACAO_REPASSE || ', ' || X.NU_SQNCL_RLTRO_CTRTO_ANALITICO || ', 6,''' || SYSDATE || ''', ''CRISE19'')';

                DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                EXECUTE IMMEDIATE SQL_QUERY;
            END IF;

            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET IC_SITUACAO_LIBERACAO = 'S',
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
        END LOOP;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA INSERCAO DE COMPENSACAO E SUSPENSAO DE LIBERACOES ANTERIORES AO SEMESTRE DA CONTRATACAO ************* ');


    --CURSOR PARA RETENCAO DE LIBERACOES DO SEMESTRE DA CONTRATACAO QUE POSSUI SUSPENSAO INTEGRAL
    --PARA VERIFICACAO DE CONFORMIDADE COM O SIAPI
    FOR X IN
        (
        SELECT
            L.NU_SEQ_CANDIDATO AS CANDIDATO,
            TO_CHAR(C.DT_ADMISSAO_CANDIDATO) AS ADM_CANDIDATO,
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            L.AA_REFERENCIA_LIBERACAO AS ANO_LIBERACAO,
            L.MM_REFERENCIA_LIBERACAO AS MES_LIBERACAO,
            L.IC_SITUACAO_LIBERACAO AS SITUACAO_LIBERACAO,
            L.VR_REPASSE,
            TO_CHAR(O.DT_OCORRENCIA) AS OCORRENCIA_SUSPENSAO,
            O.IC_TIPO_SUSPENSAO AS TP_SUSPENSAO,
            TO_CHAR(O.DT_INICIO_VIGENCIA) AS INICIO_VIGENCIA,
            TO_CHAR(O.DT_FIM_VIGENCIA) AS FIM_VIGENCIA
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB057_OCRRA_CONTRATO O
                            ON L.NU_SEQ_CANDIDATO = O.NU_CANDIDATO_FK36
                                AND O.IC_TIPO_OCORRENCIA = 'S'
                                AND	O.NU_STATUS_OCORRENCIA = 11
                                AND ( O.IC_TIPO_SUSPENSAO = 'I' OR O.IC_TIPO_SUSPENSAO IS NULL )
                                AND O.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
                                AND O.NU_SEMESTRE_REFERENCIA = CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
                                AND TO_CHAR(O.DT_OCORRENCIA, 'YYYY') = O.AA_REFERENCIA
                                AND (
                                       ( O.NU_SEMESTRE_REFERENCIA = 1 AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '06' )
                                       OR
                                       ( O.NU_SEMESTRE_REFERENCIA = 2 AND ( TO_CHAR(O.DT_OCORRENCIA, 'MM') > '06' AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '12' ) )
                                   )
                 INNER JOIN FES.FESTB010_CANDIDATO C
                            ON L.NU_SEQ_CANDIDATO = C.NU_SEQ_CANDIDATO
                                AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY')
                                AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND NOT EXISTS 	(
                SELECT 1
                FROM FES.FESTB817_RETENCAO_LIBERACAO R
                WHERE L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                  AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 8
            )
        )
        LOOP
            --INSERE RETENCAO POR CONFORMIDADE SUSPENSAO NO SIAPI
            SQL_QUERY := 'INSERT INTO FES.FESTB817_RETENCAO_LIBERACAO ' ||
                         ' (NU_SQNCL_LIBERACAO_CONTRATO, NU_MOTIVO_RETENCAO_LIBERACAO, DT_INICIO_RETENCAO) values (' ||
                         x.NU_SQNCL_LIBERACAO_CONTRATO || ', ''8'',''' || SYSDATE || ''')';

            DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
            EXECUTE IMMEDIATE SQL_QUERY;
        END LOOP;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA INSERCAO DE RETENCAO DE LIBERACOES DO SEMESTRE DA CONTRATACAO ************* ');
































===================================================================================================================
Excelente! Este é um trecho de código PL/SQL (Oracle), provavelmente parte de uma Stored Procedure ou um bloco anônimo, cujo objetivo principal é finalizar retenções de liberações de contrato baseadas em diversas condições e motivos. É um processo de saneamento ou reconciliação de dados, executado em lotes.

Vamos analisar cada bloco FOR...LOOP e suas regras.
Análise e Regras de Negócio

Este código opera sobre duas tabelas principais:

    FES.FESTB712_LIBERACAO_CONTRATO (L): Tabela de liberações de contrato.
    FES.FESTB817_RETENCAO_LIBERACAO (R): Tabela de retenções associadas às liberações.

Ele também faz LEFT OUTER JOIN com outras tabelas para obter informações contextuais:

    FES.FESTB038_ADTMO_CONTRATO (A): Aditamentos de contrato.
    FES.FESTB010_CANDIDATO (CA / C): Dados do candidato.
    FES.FESTB036_CONTRATO_FIES (F): Contratos FIES.
    FES.FESTB057_OCRRA_CONTRATO (O / OC): Ocorrências de contrato.
    FES.FESTB759_PROCESSO_ADITAMENTO (T759): Processos de aditamento.

Bloco 1: Atualização de Liberações e Retenções por Suspensão

Este bloco processa liberações com uma condição específica (IC_SITUACAO_LIBERACAO = 'NE') e um critério de data em relação à vigência de alguma "ocorrência" (provavelmente de suspensão, embora a origem O não esteja na FROM clause explícita do SELECT do FOR loop inicial - é um erro na cópia ou um contexto externo).
SQL

-- Primeiro LOOP (incompleto na parte SELECT do FOR IN - assume-se que carrega x.IC_SITUACAO_LIBERACAO, DT_LIBERACAO, DT_INICIO_VIGENCIA, DT_FIM_VIGENCIA, NU_SQNCL_LIBERACAO_CONTRATO)
-- O trecho fornecido para o primeiro LOOP começa de L.IC_SITUACAO_LIBERACAO = 'NE', mas a query do FOR IN não está completa.
-- Assumindo que 'O' se refere a FES.FESTB057_OCRRA_CONTRATO (Ocorrências de Contrato)

-- Condição de entrada no loop:
-- (L.IC_SITUACAO_LIBERACAO = 'NE' AND L.DT_LIBERACAO > O.DT_INICIO_VIGENCIA AND L.DT_LIBERACAO < O.DT_FIM_VIGENCIA)

Regras e Ações:

    Condição de Reclassificação da Liberação:
        IF x.IC_SITUACAO_LIBERACAO = 'NR' AND x.DT_LIBERACAO > x.DT_INICIO_VIGENCIA AND x.DT_LIBERACAO < x.DT_FIM_VIGENCIA THEN: Se a liberação tem status 'NR' (provavelmente "Não Repassada" ou "Não Registrada") E a data da liberação está dentro de um período de vigência (entre DT_INICIO_VIGENCIA e DT_FIM_VIGENCIA), ela será atualizada.
        Ação: UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET IC_SITUACAO_LIBERACAO = 'S', DT_ATUALIZACAO = SYSDATE
        Porquê: Isso sugere que liberações que estavam com um status de "não repassada" (NR) mas que agora se enquadram em um período de vigência válido (provavelmente após o término de uma suspensão ou correção de dados), são reclassificadas para 'S' (provavelmente "Sucesso", "Liberada" ou "Repassada"), marcando-as como aptas ou já processadas.

    Finalização de Retenção por Suspensão:
        Ação: UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = SYSDATE WHERE NU_SQNCL_LIBERACAO_CONTRATO = x.NU_SQNCL_LIBERACAO_CONTRATO AND NU_MOTIVO_RETENCAO_LIBERACAO = 3.
        Porquê: Isso encerra retenções do tipo 3 (Suspensão). Indica que se uma liberação tinha sido retida devido a uma suspensão, e a condição que levou à suspensão foi resolvida (ou a suspensão terminou), a retenção é finalizada para que a liberação possa prosseguir no próximo repasse. O DT_FIM_RETENCAO IS NULL é a condição implícita para "retenções ativas".

Bloco 2: Finalização de Retenções por Falha na Vinculação (Motivo 4)
SQL

-- Cursor com as liberacoes que possuem retencao por falha na vinculacao entre tabelas e
-- que deverao ser encaminhadas no proximo repasse.
FOR x IN
(
    SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
    FROM FES.FESTB712_LIBERACAO_CONTRATO L
        INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
            ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
            AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 4
            AND R.DT_FIM_RETENCAO IS NULL
    WHERE L.NU_TIPO_TRANSACAO IS NOT NULL
)
LOOP
    -- FINALIZA A RETENCAO DO TIPO FALHA NA VINCULACAO ENTRE TABELAS
    SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
                 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
                 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 4';
    EXECUTE IMMEDIATE SQL_QUERY;
END LOOP;

Regras e Ações:

    Identificação: Busca liberações (FESTB712_LIBERACAO_CONTRATO) que têm uma retenção ativa (DT_FIM_RETENCAO IS NULL) com o NU_MOTIVO_RETENCAO_LIBERACAO = 4 ("Falha na vinculação entre tabelas").
    WHERE L.NU_TIPO_TRANSACAO IS NOT NULL: Adicionalmente, verifica se o tipo de transação da liberação não é nulo.
    Ação: UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = SYSDATE ... WHERE NU_MOTIVO_RETENCAO_LIBERACAO = 4.
    Porquê: Se uma liberação estava retida por um problema de "vinculação entre tabelas" (ou seja, um erro de relacionamento de dados), e presume-se que essa falha foi corrigida (ou a regra de negócio determina que essa retenção deve ser finalizada para tentar o próximo repasse), a retenção é finalizada. Isso permite que essas liberações sejam processadas em um ciclo subsequente.

Bloco 3: Finalização de Retenções por Ausência de Aditamento Válido (Motivo 7) - Parte 1
SQL

-- Cursor com as liberacoes que possuem retencao por AUSENCIA DE ADITAMENTO VALIDO e devera ser finalizada.
FOR x IN
(
    SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
    FROM FES.FESTB712_LIBERACAO_CONTRATO L
        INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
            AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 7
            AND R.DT_FIM_RETENCAO IS NULL
        LEFT OUTER JOIN FES.FESTB038_ADTMO_CONTRATO A ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK36
            AND A.NU_STATUS_ADITAMENTO > 3 AND A.DT_ADITAMENTO IS NOT NULL
            AND L.AA_REFERENCIA_LIBERACAO = A.AA_ADITAMENTO
            AND ( CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END ) = A.NU_SEM_ADITAMENTO
        LEFT OUTER JOIN FES.FESTB010_CANDIDATO CA ON L.NU_SEQ_CANDIDATO = CA.NU_SEQ_CANDIDATO
            AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(CA.DT_ADMISSAO_CANDIDATO,'YYYY')
            AND ( CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END ) = ( CASE WHEN TO_CHAR(CA.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END )
        LEFT OUTER JOIN FES.FESTB036_CONTRATO_FIES F ON L.NU_SEQ_CANDIDATO = F.NU_CANDIDATO_FK11
            AND F.NU_STATUS_CONTRATO > 3 AND F.DT_ASSINATURA IS NOT NULL
    WHERE
        (
            A.NU_CANDIDATO_FK36 IS NOT NULL -- Tem aditamento válido
            OR
                (
                    CA.NU_SEQ_CANDIDATO IS NOT NULL -- É um candidato válido
                    AND F.NU_CANDIDATO_FK11 IS NOT NULL -- E tem um contrato FIES válido
                )
        )
)
LOOP
    -- FINALIZA A RETENCAO DO TIPO AUSENCIA DE ADITAMENTO VALIDO
    SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
                 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
                 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 7';
    EXECUTE IMMEDIATE SQL_QUERY;
END LOOP;

Regras e Ações:

    Identificação: Seleciona liberações com retenção ativa do NU_MOTIVO_RETENCAO_LIBERACAO = 7 ("Ausência de Aditamento Válido").
    Critério de Finalização: Uma retenção por "ausência de aditamento válido" é finalizada SE a liberação agora está associada a:
        Um aditamento válido (status > 3 e DT_ADITAMENTO IS NOT NULL) que corresponde ao candidato, ano e semestre da liberação;
        OU a um candidato válido COM um contrato FIES válido (status > 3 e DT_ASSINATURA IS NOT NULL) que corresponde ao candidato e à data de admissão/semestre.
    Ação: UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = SYSDATE ... WHERE NU_MOTIVO_RETENCAO_LIBERACAO = 7.
    Porquê: O sistema retém liberações quando não há um aditamento ou contrato válido para elas. Se, em uma execução posterior, a liberação agora possui um aditamento ou contrato válido associado (indicando que a situação foi regularizada), a retenção é finalizada para que a liberação possa ser processada.

Bloco 4: Finalização de Retenções por Ausência de Aditamento Válido (Motivo 7) - Parte 2
SQL

-- Cursor com as liberacoes que possuem retencao por AUSENCIA DE ADITAMENTO VALIDO COM IC_SITUACAO_LIBERACAO <> NR e R
-- e devem ser finalizadas
FOR X IN
(
    SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
    FROM FES.FESTB712_LIBERACAO_CONTRATO L
        INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
            ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
            AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 7
            AND R.DT_FIM_RETENCAO IS NULL
    WHERE L.IC_SITUACAO_LIBERACAO NOT IN ('NR', 'R')
)
LOOP
    -- FINALIZA A RETENCAO DO TIPO AUSENCIA DE ADITAMENTO VALIDO
    SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
                 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
                 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 7';
    EXECUTE IMMEDIATE SQL_QUERY;
END LOOP;

Regras e Ações:

    Identificação: Seleciona liberações com retenção ativa do NU_MOTIVO_RETENCAO_LIBERACAO = 7.
    Critério de Finalização: A liberação deve ter uma situação diferente de 'NR' (Não Repassada) e 'R' (Repassada).
    Ação: Finaliza a retenção do tipo 7.
    Porquê: Este bloco é uma condição adicional para finalizar retenções do tipo 7. Se a liberação já avançou para uma situação diferente de 'NR' ou 'R' (por exemplo, foi estornada, ou está em outro estado), mesmo que a retenção por "ausência de aditamento válido" ainda estivesse ativa, ela deve ser finalizada, pois a liberação já seguiu outro fluxo ou foi resolvida de outra forma.

Bloco 5: Finalização de Retenções por Análise de Liberações a Estornar (Motivo 5) - Parte 1
SQL

-- Cursor com as liberacoes que possuem retencao por ANALISE DE LIBERACOES A ESTORNAR COM IC_SITUACAO_LIBERACAO <> NE
-- e devem ser finalizadas
FOR X IN
(
    SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
    FROM FES.FESTB712_LIBERACAO_CONTRATO L
        INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
            ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
            AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 5
            AND R.DT_FIM_RETENCAO IS NULL
    WHERE L.IC_SITUACAO_LIBERACAO <> 'NE'
)
LOOP
    -- FINALIZA A RETENCAO DO TIPO ANALISE DE LIBERACOES A ESTORNAR
    SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
                 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
                 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 5';
    EXECUTE IMMEDIATE SQL_QUERY;
END LOOP;

Regras e Ações:

    Identificação: Seleciona liberações com retenção ativa do NU_MOTIVO_RETENCAO_LIBERACAO = 5 ("Análise de Liberações a Estornar").
    Critério de Finalização: A liberação deve ter uma situação diferente de 'NE' (provavelmente "Não Efetivada" ou "Não Estornada").
    Ação: Finaliza a retenção do tipo 5.
    Porquê: Se uma liberação está retida para "análise de estorno", mas sua situação atual (IC_SITUACAO_LIBERACAO) já mudou para algo diferente de 'NE' (ou seja, já foi estornada ou processada de outra forma), essa retenção de análise não é mais necessária e é finalizada.

Bloco 6: Finalização de Retenções por Análise de Liberações a Estornar (Motivo 5) - Parte 2
SQL

-- Cursor com as liberacoes que possuem retencao por ANALISE DE LIBERACOES A ESTORNAR e deverao ser finalizadas.
FOR x IN
(
    SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
    FROM FES.FESTB712_LIBERACAO_CONTRATO L
        INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
            ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
            AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 5
            AND R.DT_FIM_RETENCAO IS NULL
        -- JOINS complexos para verificar validade de aditamento, candidato, contrato e ocorrências de contrato
        LEFT OUTER JOIN FES.FESTB038_ADTMO_CONTRATO A ...
        LEFT OUTER JOIN FES.FESTB010_CANDIDATO C ...
        LEFT OUTER JOIN FES.FESTB036_CONTRATO_FIES F ...
        LEFT OUTER JOIN FES.FESTB057_OCRRA_CONTRATO O ... (tipo 'S' - Suspensão)
        LEFT OUTER JOIN FES.FESTB057_OCRRA_CONTRATO OC ... (tipo 'E' - Encerramento?)
    WHERE L.IC_SITUACAO_LIBERACAO = 'NE' -- Requisito específico para este bloco
    AND
        (
            ( A.NU_CANDIDATO_FK36 IS NULL AND C.NU_SEQ_CANDIDATO IS NULL ) -- Não tem aditamento ou candidato válido
            OR ( C.NU_SEQ_CANDIDATO IS NOT NULL AND F.NU_CANDIDATO_FK11 IS NULL ) -- Tem candidato, mas não tem contrato FIES
            OR O.NU_CANDIDATO_FK36 IS NOT NULL -- Possui ocorrência de Suspensão (O)
            OR OC.NU_CANDIDATO_FK36 IS NOT NULL -- Possui ocorrência de Encerramento/Outra (OC)
        )
)
LOOP
    -- FINALIZA A RETENCAO DO TIPO ANALISE DE LIBERACOES A ESTORNAR
    SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
                 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
                 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 5';
    EXECUTE IMMEDIATE SQL_QUERY;
END LOOP;

Regras e Ações:

    Identificação: Seleciona liberações com retenção ativa do NU_MOTIVO_RETENCAO_LIBERACAO = 5.
    Critério de Finalização: A liberação deve estar em situação 'NE' E satisfazer uma das seguintes condições (que justificam o estorno):
        Não ter um aditamento válido E não ter um candidato válido.
        Ter um candidato válido MAS não ter um contrato FIES válido.
        Ter uma ocorrência de suspensão (O) associada.
        Ter uma ocorrência de encerramento/outra (OC) associada.
        As datas das ocorrências também são validadas para o semestre e ano de referência.
    Ação: Finaliza a retenção do tipo 5.
    Porquê: Este é um bloco de saneamento de dados robusto. Se uma liberação estava marcada para "análise de estorno" (NU_MOTIVO_RETENCAO_LIBERACAO = 5) e ainda está na situação 'NE', mas a investigação revela que ela deveria ter sido estornada ou não deveria ser repassada devido a problemas de dados (falta de aditamento/candidato/contrato válido) ou ocorrências no contrato (suspensão/encerramento), então essa retenção é finalizada. Isso indica que a "análise" foi concluída e a liberação está em um estado que justifica o não-repasse ou a necessidade de estorno.

Bloco 7: Finalização de Retenções por Divergência entre Repasse e Aditamento (Motivo 9)
SQL

-- Cursor com as liberacoes que possuem retencao por DIVERGENCIA ENTRE REPASSE E ADITAMENTO e devera ser finalizada.
FOR x IN
(
    SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
    FROM FES.FESTB712_LIBERACAO_CONTRATO L
        INNER JOIN ( -- Subquery que calcula o total de repasse e verifica a divergência com o aditamento
            SELECT NU_CANDIDATO_FK36, AA_ADITAMENTO, NU_SEM_ADITAMENTO
            FROM FES.FESTB712_LIBERACAO_CONTRATO INNER JOIN FES.FESTB038_ADTMO_CONTRATO ON ...
            WHERE NU_SEQ_CANDIDATO > 20000000 AND MM_REFERENCIA_LIBERACAO > 0
            GROUP BY ... VR_ADITAMENTO
            HAVING ( ( SUM(VR_REPASSE) - VR_ADITAMENTO ) BETWEEN 0 AND 1 OR ( VR_ADITAMENTO - SUM(VR_REPASSE) ) BETWEEN 0 AND 1 ) -- Diferença de até 1 (centavo/unidade mínima)
            AND COUNT(VR_REPASSE) = 6 -- Existem exatamente 6 repasses
        ) D ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK36
            AND L.AA_REFERENCIA_LIBERACAO = D.AA_ADITAMENTO
            AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.NU_SEM_ADITAMENTO
    WHERE EXISTS ( -- Garante que a liberação tem uma retenção ativa do motivo 9
        SELECT 1
        FROM FES.FESTB817_RETENCAO_LIBERACAO R
        WHERE L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
        AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 9
        AND R.DT_FIM_RETENCAO IS NULL
    )
)
LOOP
    -- FINALIZA A RETENCAO DO TIPO DIVERGENCIA ENTRE REPASSE E ADITAMENTO
    SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
                 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
                 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 9';
    EXECUTE IMMEDIATE SQL_QUERY;
END LOOP;

Regras e Ações:

    Identificação: Seleciona liberações com retenção ativa do NU_MOTIVO_RETENCAO_LIBERACAO = 9 ("Divergência entre Repasse e Aditamento").
    Critério de Finalização (Subquery D): A retenção é finalizada se houver uma correspondência entre o valor total repassado (SUM(VR_REPASSE)) e o valor do aditamento (VR_ADITAMENTO) para o mesmo candidato, ano e semestre.
        A divergência é considerada resolvida se a diferença entre o total repassado e o valor do aditamento for entre 0 e 1 (provavelmente tolerância para arredondamento de centavos).
        Além disso, o COUNT(VR_REPASSE) = 6 indica que se espera que existam exatamente 6 liberações (parcelas) para aquele semestre.
    Ação: Finaliza a retenção do tipo 9.
    Porquê: Este bloco corrige inconsistências financeiras. Se um aditamento estava retido porque o total das liberações não batia com o valor do aditamento, e agora, após alguma correção ou reprocessamento, os valores se alinham (dentro da tolerância), a retenção é finalizada. Isso permite que o aditamento seja considerado regularizado.

Bloco 8: Finalização de Retenções por Divergência entre Repasse e Contratação (Motivo 9)
SQL

-- Cursor com as liberacoes que possuem retencao por DIVERGENCIA ENTRE REPASSE E CONTRATACAO e devera ser finalizada.
FOR X IN
(
    SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
    FROM FES.FESTB712_LIBERACAO_CONTRATO L
        INNER JOIN ( -- Subquery que calcula o total de repasse e verifica a divergência com o contrato
            SELECT A.NU_CANDIDATO_FK11, TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY') AS ANO,
                   ( CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END ) AS SEMESTRE,
                   A.VR_CONTRATO, SUM(L.VR_REPASSE)
            FROM FES.FESTB712_LIBERACAO_CONTRATO L
                INNER JOIN FES.FESTB036_CONTRATO_FIES A ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK11 AND A.NU_STATUS_CONTRATO > 3
                INNER JOIN FES.FESTB010_CANDIDATO C ON C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY')
                    AND ( CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END ) = ( CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END )
            WHERE L.NU_SEQ_CANDIDATO > 20000000 AND L.MM_REFERENCIA_LIBERACAO > 0
            GROUP BY ... VR_CONTRATO
            HAVING ( ( SUM(VR_REPASSE) - VR_CONTRATO ) BETWEEN 0 AND 1 OR ( VR_CONTRATO - SUM(VR_REPASSE) ) BETWEEN 0 AND 1 ) -- Diferença de até 1
            AND COUNT(L.VR_REPASSE) = 6 -- Existem exatamente 6 repasses
        ) D ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK11
            AND L.AA_REFERENCIA_LIBERACAO = D.ANO
            AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.SEMESTRE
        LEFT OUTER JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO A ON L.NU_SQNCL_LIBERACAO_CONTRATO = A.NU_SQNCL_LIBERACAO_CONTRATO AND L.NU_SEQ_CANDIDATO = A.NU_SEQ_CANDIDATO
        LEFT OUTER JOIN FES.FESTB812_CMPSO_RPSE_INDVO R ON R.NU_SQNCL_RLTRO_CTRTO_ANALITICO = A.NU_SQNCL_RLTRO_CTRTO_ANALITICO AND R.NU_TIPO_ACERTO = 7 AND R.IC_COMPENSADO = 'N' AND A.VR_REPASSE > L.VR_REPASSE
    WHERE EXISTS ( -- Garante que a liberação tem uma retenção ativa do motivo 9
        SELECT 1
        FROM FES.FESTB817_RETENCAO_LIBERACAO R
        WHERE L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
        AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 9
        AND R.DT_FIM_RETENCAO IS NULL
    )
    AND ( A.NU_SQNCL_LIBERACAO_CONTRATO IS NULL OR R.NU_SQNCL_RLTRO_CTRTO_ANALITICO IS NULL ) -- Condições adicionais
)
LOOP
    -- FINALIZA A RETENCAO DO TIPO DIVERGENCIA ENTRE REPASSE E CONTRATACAO
    SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
                 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
                 X.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 9';
    EXECUTE IMMEDIATE SQL_QUERY;
END LOOP;

Regras e Ações:

    Identificação: Seleciona liberações com retenção ativa do NU_MOTIVO_RETENCAO_LIBERACAO = 9 ("Divergência entre Repasse e Contratação"). É o mesmo motivo da seção anterior, mas com uma lógica de validação diferente na FROM e WHERE clause.
    Critério de Finalização (Subquery D): Similar ao bloco anterior, a retenção é finalizada se houver uma correspondência entre o SUM(VR_REPASSE) e o VR_CONTRATO.
        Novamente, a diferença tolerada é entre 0 e 1, e o COUNT(L.VR_REPASSE) = 6 é esperado.
        Condições Adicionais (WHERE): A liberação não deve ter um registro correspondente no relatório analítico (FESTB711_RLTRO_CTRTO_ANLTO) OU não ter um registro de compensação individual (FESTB812_CMPSO_RPSE_INDVO) não compensado e com valor de repasse maior. Isso sugere que, se o repasse já foi compensado ou não tem mais divergência de valores, a retenção deve ser finalizada.
    Ação: Finaliza a retenção do tipo 9.
    Porquê: Similar ao bloco de "divergência entre repasse e aditamento", este bloco resolve inconsistências, mas focando na divergência entre o repasse e o valor do contrato FIES inicial. Se a soma dos repasses agora bate com o valor do contrato e outras condições de compensação/relatório são atendidas, a retenção é encerrada, indicando que a situação está regularizada.

Bloco 9: Finalização de Retenções por Ausência de Finalização no Processo de Aditamento (Motivo 10)
SQL

-- CURSOR PARA SELECIONAR LIBERACOES A TEREM RETENCAO POR AUSENCIA DE FINALIZACAO NO PROCESSO DE ADITAMENTO FINALIZADAS
FOR X IN
(
    SELECT T712.NU_SQNCL_LIBERACAO_CONTRATO
    FROM FES.FESTB712_LIBERACAO_CONTRATO T712
        INNER JOIN FES.FESTB038_ADTMO_CONTRATO T38 ON T38.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO
            AND T38.AA_ADITAMENTO = T712.AA_REFERENCIA_LIBERACAO
            AND T38.NU_SEM_ADITAMENTO = CASE WHEN T712.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
            AND T38.NU_STATUS_ADITAMENTO IN (4, 5) -- Aditamento em status "Finalizado" ou similar
        INNER JOIN FES.FESTB759_PROCESSO_ADITAMENTO T759 ON T759.NU_CANDIDATO = T38.NU_CANDIDATO_FK36
            AND T759.AA_REFERENCIA = T38.AA_ADITAMENTO
            AND T759.NU_SEMESTRE_REFERENCIA = T38.NU_SEM_ADITAMENTO
            AND T759.NU_SITUACAO_PROCESSO = 9 -- Processo de aditamento em situação '9' (Finalizado/Concluído)
    WHERE T712.NU_SEQ_CANDIDATO > 20000000
    AND T712.IC_SITUACAO_LIBERACAO = 'NR' -- Liberação em status 'NR' (Não Repassada/Não Registrada)
    AND EXISTS (
        SELECT 1
        FROM FES.FESTB817_RETENCAO_LIBERACAO T817
        WHERE T817.NU_SQNCL_LIBERACAO_CONTRATO = T712.NU_SQNCL_LIBERACAO_CONTRATO
        AND T817.NU_MOTIVO_RETENCAO_LIBERACAO = 10 -- Retenção ativa do motivo 10
        AND T817.DT_FIM_RETENCAO IS NULL
    )
)
LOOP
    --FINALIZA RETENCAO POR AUSENCIA DE FINALIZACAO NO PROCESSO DE ADITAMENTO
    SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
                 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
                 X.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 10';
    EXECUTE IMMEDIATE SQL_QUERY;
END LOOP;

Regras e Ações:

    Identificação: Seleciona liberações com retenção ativa do NU_MOTIVO_RETENCAO_LIBERACAO = 10 ("Ausência de Finalização no Processo de Aditamento").
    Critério de Finalização: A retenção é finalizada se:
        A liberação está em situação NR (Não Repassada).
        Existe um aditamento (FESTB038_ADTMO_CONTRATO) com status 4 ou 5 (provavelmente "Finalizado" ou "Concluído") para o candidato, ano e semestre correspondentes.
        Existe um processo de aditamento (FESTB759_PROCESSO_ADITAMENTO) com situação 9 (também "Finalizado" ou "Concluído") para o mesmo candidato, ano e semestre.
    Ação: Finaliza a retenção do tipo 10.
    Porquê: Se uma liberação foi retida porque o processo de aditamento correspondente não estava finalizado, mas agora tanto o aditamento quanto seu processo estão em status de conclusão, a retenção é liberada. Isso permite que as liberações, que estavam esperando a regularização do aditamento, prossigam.

Visão Geral e Justificativa (Porquê)

Este script PL/SQL é um processo de saneamento e reconciliação de dados que roda periodicamente (provavelmente um batch noturno ou programado).

    O que faz: Ele percorre diversas liberações que estão com retenções ativas por diferentes motivos (suspensão, falhas de vínculo, ausência de aditamento válido, divergências de valores). Para cada tipo de retenção, ele aplica um conjunto específico de regras de negócio para verificar se a condição que causou a retenção foi resolvida ou se a retenção não é mais aplicável. Se as condições forem atendidas, ele finaliza a retenção (preenchendo DT_FIM_RETENCAO com a data atual). Em um caso específico (primeiro bloco), ele também pode atualizar o status da liberação (IC_SITUACAO_LIBERACAO).
    Porquê:
        Integridade e Consistência dos Dados: Garante que as retenções reflitam o estado atual e correto das liberações e contratos, evitando que liberações fiquem "travadas" desnecessariamente.
        Liberação para Repasse: Ao finalizar as retenções, libera as parcelas de repasse que agora estão em conformidade com as regras para serem incluídas nos próximos ciclos de processamento financeiro.
        Automação de Resolução de Pendências: Automatiza a resolução de cenários onde as liberações foram retidas, mas a causa raiz do problema foi corrigida em outro processo.
        Eficiência: Ao remover retenções desnecessárias, otimiza os próximos passos dos processos financeiros.
        Auditoria: O preenchimento da DT_FIM_RETENCAO e DT_ATUALIZACAO fornece um histórico claro de quando e por que uma retenção foi encerrada ou uma situação de liberação foi alterada.

Este é um script de manutenção essencial para um sistema financeiro de grande porte, garantindo que o fluxo de repasses permaneça saudável e as pendências sejam resolvidas automaticamente quando suas condições de retenção não se aplicam mais.
---------
CREATE OR REPLACE PROCEDURE FES.FESSPZ55_CRISE2019_TRATA_SUSP
--F620600  18/06/2021 20:11:44
    --C077033 18/06/2021 16:50:00
--C077033 21/05/2021 19:26:00
--C077033 23/01/2021 20:25:00
--C077033 17/01/2021 22:04:00
--C077033 24/11/2020 18:10:00
--C077033 01/10/2020 19:48:00

AS
    SQL_QUERY VARCHAR2(1000) := NULL;
    COUNT_1 NUMERIC(5) := 0;
    QT_COMPENSACAO_CRIADA NUMERIC(10) := 0;
    V_NU_SQNCL_COMPENSACAO_REPASSE NUMERIC(12);

BEGIN

    DBMS_OUTPUT.PUT_LINE(' ************* INICIO DA FESSPZ55_CRISE2019_TRATA_SUSP');


    --INSERE TIPO DE ACERTO - 6 - REPASSE ANTERIOR CONTRATACAO - NA TB813
    SELECT COUNT(*) INTO COUNT_1
    FROM FES.FESTB813_TIPO_ACERTO_RPSE
    WHERE NU_TIPO_ACERTO = 6;

    IF COUNT_1 = 0
    THEN
        INSERT INTO FES.FESTB813_TIPO_ACERTO_RPSE (NU_TIPO_ACERTO, DE_TIPO_ACERTO)
        VALUES (6,'REPASSE ANTERIOR CONTRATACAO');
        COMMIT;
        DBMS_OUTPUT.PUT_LINE(' ************* INSERCAO TIPO DE ACERTO 6 - REPASSE ANTERIOR CONTRATACAO - NA TB813 ************* ');
    END IF;


    --COMPENSACAO E SUSPENSAO DE TODAS AS LIBERACOES EXISTENTES ANTERIORES AO SEMESTRE DE CONTRATACAO DO CANDIDATO
    SELECT MAX(NU_SQNCL_COMPENSACAO_REPASSE) INTO V_NU_SQNCL_COMPENSACAO_REPASSE FROM FES.FESTB812_CMPSO_RPSE_INDVO;

    FOR X IN
        (
        SELECT
            L.IC_SITUACAO_LIBERACAO,
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB010_CANDIDATO C
                            ON C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO
                                AND
                               (
                                           TO_CHAR(L.DT_LIBERACAO,'YYYY') < TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY')
                                       OR
                                           (
                                                       TO_CHAR(L.DT_LIBERACAO,'YYYY') = TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY')
                                                   AND
                                                       (CASE WHEN TO_CHAR(L.DT_LIBERACAO,'MM') < 7 THEN 1 ELSE 2 END) < (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
                                               )
                                   )
                 LEFT OUTER JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO A
                                 ON L.NU_SQNCL_LIBERACAO_CONTRATO = A.NU_SQNCL_LIBERACAO_CONTRATO
                                     AND L.NU_SEQ_CANDIDATO = A.NU_SEQ_CANDIDATO
                                     AND A.VR_REPASSE > 0
                 LEFT OUTER JOIN FES.FESTB812_CMPSO_RPSE_INDVO R
                                 ON R.NU_SQNCL_RLTRO_CTRTO_ANALITICO = A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND L.IC_SITUACAO_LIBERACAO <> 'S'
          AND R.NU_SQNCL_RLTRO_CTRTO_ANALITICO IS NULL
        )
        LOOP
            IF (X.IC_SITUACAO_LIBERACAO NOT IN ('NR', 'E') AND X.NU_SQNCL_RLTRO_CTRTO_ANALITICO IS NOT NULL) THEN
                QT_COMPENSACAO_CRIADA := QT_COMPENSACAO_CRIADA + 1;
                V_NU_SQNCL_COMPENSACAO_REPASSE := V_NU_SQNCL_COMPENSACAO_REPASSE + 1;

                SQL_QUERY := 'INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (' ||
                             'NU_SQNCL_COMPENSACAO_REPASSE, NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_TIPO_ACERTO, TS_INCLUSAO, CO_USUARIO_INCLUSAO)' ||
                             ' VALUES (' || V_NU_SQNCL_COMPENSACAO_REPASSE || ', ' || X.NU_SQNCL_RLTRO_CTRTO_ANALITICO || ', 6,''' || SYSDATE || ''', ''CRISE19'')';

                DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                EXECUTE IMMEDIATE SQL_QUERY;
            END IF;

            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET IC_SITUACAO_LIBERACAO = 'S',
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
        END LOOP;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA INSERCAO DE COMPENSACAO E SUSPENSAO DE LIBERACOES ANTERIORES AO SEMESTRE DA CONTRATACAO ************* ');


    --CURSOR PARA RETENCAO DE LIBERACOES DO SEMESTRE DA CONTRATACAO QUE POSSUI SUSPENSAO INTEGRAL
    --PARA VERIFICACAO DE CONFORMIDADE COM O SIAPI
    FOR X IN
        (
        SELECT
            L.NU_SEQ_CANDIDATO AS CANDIDATO,
            TO_CHAR(C.DT_ADMISSAO_CANDIDATO) AS ADM_CANDIDATO,
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            L.AA_REFERENCIA_LIBERACAO AS ANO_LIBERACAO,
            L.MM_REFERENCIA_LIBERACAO AS MES_LIBERACAO,
            L.IC_SITUACAO_LIBERACAO AS SITUACAO_LIBERACAO,
            L.VR_REPASSE,
            TO_CHAR(O.DT_OCORRENCIA) AS OCORRENCIA_SUSPENSAO,
            O.IC_TIPO_SUSPENSAO AS TP_SUSPENSAO,
            TO_CHAR(O.DT_INICIO_VIGENCIA) AS INICIO_VIGENCIA,
            TO_CHAR(O.DT_FIM_VIGENCIA) AS FIM_VIGENCIA
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB057_OCRRA_CONTRATO O
                            ON L.NU_SEQ_CANDIDATO = O.NU_CANDIDATO_FK36
                                AND O.IC_TIPO_OCORRENCIA = 'S'
                                AND	O.NU_STATUS_OCORRENCIA = 11
                                AND ( O.IC_TIPO_SUSPENSAO = 'I' OR O.IC_TIPO_SUSPENSAO IS NULL )
                                AND O.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
                                AND O.NU_SEMESTRE_REFERENCIA = CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
                                AND TO_CHAR(O.DT_OCORRENCIA, 'YYYY') = O.AA_REFERENCIA
                                AND (
                                       ( O.NU_SEMESTRE_REFERENCIA = 1 AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '06' )
                                       OR
                                       ( O.NU_SEMESTRE_REFERENCIA = 2 AND ( TO_CHAR(O.DT_OCORRENCIA, 'MM') > '06' AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '12' ) )
                                   )
                 INNER JOIN FES.FESTB010_CANDIDATO C
                            ON L.NU_SEQ_CANDIDATO = C.NU_SEQ_CANDIDATO
                                AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY')
                                AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND NOT EXISTS 	(
                SELECT 1
                FROM FES.FESTB817_RETENCAO_LIBERACAO R
                WHERE L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                  AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 8
            )
        )
        LOOP
            --INSERE RETENCAO POR CONFORMIDADE SUSPENSAO NO SIAPI
            SQL_QUERY := 'INSERT INTO FES.FESTB817_RETENCAO_LIBERACAO ' ||
                         ' (NU_SQNCL_LIBERACAO_CONTRATO, NU_MOTIVO_RETENCAO_LIBERACAO, DT_INICIO_RETENCAO) values (' ||
                         x.NU_SQNCL_LIBERACAO_CONTRATO || ', ''8'',''' || SYSDATE || ''')';

            DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
            EXECUTE IMMEDIATE SQL_QUERY;
        END LOOP;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA INSERCAO DE RETENCAO DE LIBERACOES DO SEMESTRE DA CONTRATACAO ************* ');


    --ALTERACAO DO TIPO DE SUSPENSAO NO SEMESTRE DE CONTRATACAO DE INTEGRAL PARA PARCIAL
    FOR X IN
        (
        SELECT
            O.NU_CANDIDATO_FK36,
            O.AA_REFERENCIA,
            O.NU_SEMESTRE_REFERENCIA
        FROM FES.FESTB057_OCRRA_CONTRATO O
                 INNER JOIN FES.FESTB010_CANDIDATO C
                            ON C.NU_SEQ_CANDIDATO = O.NU_CANDIDATO_FK36
                                AND TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY') = O.AA_REFERENCIA
                                AND (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END) = O.NU_SEMESTRE_REFERENCIA
        WHERE O.NU_CANDIDATO_FK36 > 20000000
          AND O.IC_TIPO_OCORRENCIA = 'S'
          AND (O.IC_TIPO_SUSPENSAO = 'I' OR O.IC_TIPO_SUSPENSAO IS NULL)
          AND O.NU_STATUS_OCORRENCIA = 11
          AND TO_CHAR(O.DT_OCORRENCIA, 'YYYY') = O.AA_REFERENCIA
          AND (
                ( O.NU_SEMESTRE_REFERENCIA = 1 AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '06' )
                OR
                ( O.NU_SEMESTRE_REFERENCIA = 2 AND ( TO_CHAR(O.DT_OCORRENCIA, 'MM') > '06' AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '12' ) )
            )
        )
        LOOP
            UPDATE FES.FESTB057_OCRRA_CONTRATO
            SET IC_TIPO_SUSPENSAO = 'P',
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_CANDIDATO_FK36 = X.NU_CANDIDATO_FK36
              AND AA_REFERENCIA = X.AA_REFERENCIA
              AND NU_SEMESTRE_REFERENCIA = X.NU_SEMESTRE_REFERENCIA
              AND IC_TIPO_OCORRENCIA = 'S'
              AND NU_STATUS_OCORRENCIA = 11;
        END LOOP;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA ALTERACAO DO TIPO DE SUSPENSAO NO SEMESTRE DE CONTRATACAO ************* ');


    -- ALTERACAO DA DATA INICIO_VIGENCIA DAS SUSPENSOES PARCIAIS
    UPDATE FES.FESTB057_OCRRA_CONTRATO
    SET DT_INICIO_VIGENCIA = LAST_DAY(DT_OCORRENCIA) + 1,
        DT_ATUALIZACAO = SYSDATE
    WHERE NU_CANDIDATO_FK36 > 20000000
      AND IC_TIPO_OCORRENCIA = 'S'
      AND IC_TIPO_SUSPENSAO = 'P'
      AND NU_STATUS_OCORRENCIA = 11
      AND TO_CHAR(DT_OCORRENCIA, 'YYYY') = AA_REFERENCIA
      AND (DT_INICIO_VIGENCIA <> LAST_DAY(DT_OCORRENCIA) + 1 OR DT_INICIO_VIGENCIA IS NULL)
      AND (
            ( NU_SEMESTRE_REFERENCIA = 1 AND TO_CHAR(DT_OCORRENCIA, 'MM') < '06' )
            OR
            ( NU_SEMESTRE_REFERENCIA = 2 AND ( TO_CHAR(DT_OCORRENCIA, 'MM') > '06' AND TO_CHAR(DT_OCORRENCIA, 'MM') < '12' ) )
        );

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA ATUALIZACAO DE DATA INICIO DE VIGENCIA DA SUSPENSAO ************* ');


    --ATUALIZA DATA FIM DE VIGENCIA DA SUSPENSAO
    UPDATE FES.FESTB057_OCRRA_CONTRATO
    SET DT_FIM_VIGENCIA = CASE WHEN NU_SEMESTRE_REFERENCIA = 1 THEN TO_DATE('0630' || TO_CHAR(DT_OCORRENCIA, 'YYYY'), 'MMDDYYYY')
                               WHEN NU_SEMESTRE_REFERENCIA = 2 THEN TO_DATE('1231' || TO_CHAR(DT_OCORRENCIA, 'YYYY'), 'MMDDYYYY')
        END
    WHERE NU_CANDIDATO_FK36 > 20000000
      AND IC_TIPO_OCORRENCIA = 'S'
      AND IC_TIPO_SUSPENSAO = 'P'
      AND NU_STATUS_OCORRENCIA = 11
      AND TO_CHAR(DT_OCORRENCIA, 'YYYY') = AA_REFERENCIA
      AND DT_INICIO_VIGENCIA > DT_OCORRENCIA
      AND (TO_CHAR(DT_OCORRENCIA, 'MMDD') BETWEEN '0101' AND '0531' OR TO_CHAR(DT_OCORRENCIA, 'MMDD') BETWEEN'0701' AND '1130')
      AND (
            (NU_SEMESTRE_REFERENCIA = 1 AND TO_CHAR(DT_FIM_VIGENCIA, 'MM') <> '06')
            OR
            (NU_SEMESTRE_REFERENCIA = 2 AND TO_CHAR(DT_FIM_VIGENCIA, 'MM') <> '12')
        );

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA ATUALIZACAO DE DATA FIM DE VIGENCIA DA SUSPENSAO ************* ');


    -- ATUALIZACAO DA SITUACAO_LIBERACAO, CONFORME VIGENCIA DA SUSPENSAO
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            L.IC_SITUACAO_LIBERACAO,
            L.DT_LIBERACAO,
            O.DT_INICIO_VIGENCIA,
            O.DT_FIM_VIGENCIA
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB057_OCRRA_CONTRATO O
                            ON L.NU_SEQ_CANDIDATO = O.NU_CANDIDATO_FK36
                                AND O.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
                                AND O.NU_SEMESTRE_REFERENCIA = CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
                                AND O.IC_TIPO_OCORRENCIA = 'S'
                                AND O.IC_TIPO_SUSPENSAO = 'P'
                                AND	O.NU_STATUS_OCORRENCIA = 11
                                AND TO_CHAR(O.DT_OCORRENCIA, 'YYYY') = O.AA_REFERENCIA
                                AND (
                                       ( O.NU_SEMESTRE_REFERENCIA = 1 AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '06' AND TO_CHAR(O.DT_FIM_VIGENCIA, 'DDMM') = '3006' )
                                       OR
                                       ( O.NU_SEMESTRE_REFERENCIA = 2 AND ( TO_CHAR(O.DT_OCORRENCIA, 'MM') > '06' AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '12' ) AND TO_CHAR(O.DT_FIM_VIGENCIA, 'DDMM') = '3112' )
                                   )
                                AND O.DT_INICIO_VIGENCIA = LAST_DAY(O.DT_OCORRENCIA) + 1
        WHERE L.IC_SITUACAO_LIBERACAO IN ('NR', 'R', 'NE', 'S')
        )
        LOOP
            IF X.DT_LIBERACAO > X.DT_INICIO_VIGENCIA AND X.DT_LIBERACAO < X.DT_FIM_VIGENCIA THEN
                IF X.IC_SITUACAO_LIBERACAO = 'NR' THEN
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                    SET IC_SITUACAO_LIBERACAO = 'S',
                        DT_ATUALIZACAO = SYSDATE
                    WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
                ELSIF X.IC_SITUACAO_LIBERACAO = 'R' THEN
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                    SET IC_SITUACAO_LIBERACAO = 'NE',
                        DT_ATUALIZACAO = SYSDATE
                    WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
                END IF;
            ELSIF X.DT_LIBERACAO < X.DT_INICIO_VIGENCIA THEN
                IF X.IC_SITUACAO_LIBERACAO = 'S' THEN
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                    SET IC_SITUACAO_LIBERACAO = 'NR',
                        DT_ATUALIZACAO = SYSDATE
                    WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
                ELSIF X.IC_SITUACAO_LIBERACAO = 'NE' THEN
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                    SET IC_SITUACAO_LIBERACAO = 'R',
                        DT_ATUALIZACAO = SYSDATE
                    WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
                END IF;
            END IF;
        END LOOP;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA ATUALIZACAO DA SITUACAO_LIBERACAO,CONFORME SUSPENSAO ************* ');


    -- ALTERACAO DA DATA INICIO_VIGENCIA DOS ENCERRAMENTOS
    FOR X IN
        (
        SELECT
            O.NU_CANDIDATO_FK36,
            O.AA_REFERENCIA,
            O.NU_SEMESTRE_REFERENCIA,
            O.DT_OCORRENCIA
        FROM FES.FESTB057_OCRRA_CONTRATO O
                 LEFT OUTER JOIN FES.FESTB038_ADTMO_CONTRATO A
                                 ON A.NU_CANDIDATO_FK36 = O.NU_CANDIDATO_FK36
                                     AND A.AA_ADITAMENTO = O.AA_REFERENCIA
                                     AND A.NU_SEM_ADITAMENTO = O.NU_SEMESTRE_REFERENCIA
                                     AND A.NU_STATUS_ADITAMENTO > 3
                 LEFT OUTER JOIN FES.FESTB010_CANDIDATO C
                                 ON C.NU_SEQ_CANDIDATO = O.NU_CANDIDATO_FK36
                                     AND TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY') = O.AA_REFERENCIA
                                     AND (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END) = O.NU_SEMESTRE_REFERENCIA
        WHERE O.NU_CANDIDATO_FK36 > 20000000
          AND IC_TIPO_OCORRENCIA = 'E'
          AND NU_STATUS_OCORRENCIA = 11
          AND TO_CHAR(DT_OCORRENCIA, 'YYYY') = AA_REFERENCIA
          AND (DT_INICIO_VIGENCIA <> LAST_DAY(DT_OCORRENCIA) + 1 OR DT_INICIO_VIGENCIA IS NULL)
          AND (
                ( NU_SEMESTRE_REFERENCIA = 1 AND TO_CHAR(DT_OCORRENCIA, 'MM') < '06' )
                OR
                ( NU_SEMESTRE_REFERENCIA = 2 AND ( TO_CHAR(DT_OCORRENCIA, 'MM') > '06' AND TO_CHAR(DT_OCORRENCIA, 'MM') < '12' ) )
            )
          AND ( A.NU_CANDIDATO_FK36 IS NOT NULL OR C.NU_SEQ_CANDIDATO IS NOT NULL )
        )
        LOOP
            UPDATE FES.FESTB057_OCRRA_CONTRATO
            SET DT_INICIO_VIGENCIA = LAST_DAY(X.DT_OCORRENCIA) + 1,
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_CANDIDATO_FK36 = X.NU_CANDIDATO_FK36
              AND AA_REFERENCIA = X.AA_REFERENCIA
              AND NU_SEMESTRE_REFERENCIA = X.NU_SEMESTRE_REFERENCIA
              AND IC_TIPO_OCORRENCIA = 'E'
              AND NU_STATUS_OCORRENCIA = 11;
        END LOOP;
    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA ATUALIZACAO DE DATA INICIO DE VIGENCIA DO ENCERRAMENTO ************* ');



    -- ATUALIZACAO DA SITUACAO_LIBERACAO, CONFORME VIGENCIA DO ENCERRAMENTO
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            L.DT_LIBERACAO,
            L.IC_SITUACAO_LIBERACAO,
            O.DT_INICIO_VIGENCIA
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB057_OCRRA_CONTRATO O
                            ON L.NU_SEQ_CANDIDATO = O.NU_CANDIDATO_FK36
                                AND O.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
                                AND O.NU_SEMESTRE_REFERENCIA = CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
                                AND O.IC_TIPO_OCORRENCIA = 'E'
                                AND	O.NU_STATUS_OCORRENCIA = 11
                                AND TO_CHAR(O.DT_OCORRENCIA, 'YYYY') = O.AA_REFERENCIA
                                AND (
                                       ( O.NU_SEMESTRE_REFERENCIA = 1 AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '06' )
                                       OR
                                       ( O.NU_SEMESTRE_REFERENCIA = 2 AND TO_CHAR(O.DT_OCORRENCIA, 'MM') > '06' AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '12' )
                                   )
                                AND O.DT_INICIO_VIGENCIA = LAST_DAY(O.DT_OCORRENCIA) + 1
        WHERE L.IC_SITUACAO_LIBERACAO IN ('NR', 'R', 'NE', 'S')
        ORDER BY L.NU_SEQ_CANDIDATO, L.AA_REFERENCIA_LIBERACAO, L.MM_REFERENCIA_LIBERACAO
        )
        LOOP
            IF X.DT_LIBERACAO > X.DT_INICIO_VIGENCIA THEN
                IF X.IC_SITUACAO_LIBERACAO = 'NR' THEN
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                    SET IC_SITUACAO_LIBERACAO = 'S',
                        DT_ATUALIZACAO = SYSDATE
                    WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
                ELSIF X.IC_SITUACAO_LIBERACAO = 'R' THEN
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                    SET IC_SITUACAO_LIBERACAO = 'NE',
                        DT_ATUALIZACAO = SYSDATE
                    WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
                END IF;
            ELSIF X.DT_LIBERACAO < X.DT_INICIO_VIGENCIA THEN
                IF X.IC_SITUACAO_LIBERACAO = 'S' THEN
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                    SET IC_SITUACAO_LIBERACAO = 'NR',
                        DT_ATUALIZACAO = SYSDATE
                    WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
                ELSIF X.IC_SITUACAO_LIBERACAO = 'NE' THEN
                    UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                    SET IC_SITUACAO_LIBERACAO = 'R',
                        DT_ATUALIZACAO = SYSDATE
                    WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
                END IF;
            END IF;
        END LOOP;
    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA ATUALIZACAO DA SITUACAO_LIBERACAO, CONFORME ENCERRAMENTO ************* ');


    -- ADEQUACAO DA SITUACAO DAS LIBERACOES NE QUE NÃO POSSUEM LANCAMENTOS PERTINENTES
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 LEFT OUTER JOIN FES.FESTB038_ADTMO_CONTRATO A
                                 ON A.NU_CANDIDATO_FK36 = L.NU_SEQ_CANDIDATO
                                     AND A.AA_ADITAMENTO = L.AA_REFERENCIA_LIBERACAO
                                     AND A.NU_SEM_ADITAMENTO = CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
                                     AND A.NU_STATUS_ADITAMENTO > 3
                 LEFT OUTER JOIN FES.FESTB010_CANDIDATO C
                                 ON C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO
                                     AND TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY') = L.AA_REFERENCIA_LIBERACAO
                                     AND (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END) = CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
                 LEFT OUTER JOIN FES.FESTB036_CONTRATO_FIES F
                                 ON F.NU_CANDIDATO_FK11 = L.NU_SEQ_CANDIDATO
                                     AND F.NU_STATUS_CONTRATO > 3
                 LEFT OUTER JOIN FES.FESTB057_OCRRA_CONTRATO O
                                 ON O.NU_CANDIDATO_FK36 = L.NU_SEQ_CANDIDATO
                                     AND O.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
                                     AND O.NU_SEMESTRE_REFERENCIA = CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
                                     AND O.NU_STATUS_OCORRENCIA = 11
        WHERE L.IC_SITUACAO_LIBERACAO = 'NE'
          AND (
                    A.NU_CANDIDATO_FK36 IS NOT NULL
                OR
                    ( C.NU_SEQ_CANDIDATO IS NOT NULL AND F.NU_CANDIDATO_FK11 IS NOT NULL )
            )
          AND O.NU_CANDIDATO_FK36 IS NULL
        )
        LOOP
            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET IC_SITUACAO_LIBERACAO = 'R',
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
        END LOOP;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('FIM DO PROCESSAMENTO DE ADEQUACAO DA SITUACAO DAS LIBERACOES NE QUE NÃO POSSUEM LANCAMENTOS PERTINENTES');


    -- ADEQUACAO DA SITUACAO DAS LIBERACOES SUSPENSAS QUE NÃO POSSUEM LANCAMENTOS PERTINENTES
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 LEFT OUTER JOIN FES.FESTB038_ADTMO_CONTRATO A
                                 ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK36
                                     AND A.NU_STATUS_ADITAMENTO > 3
                                     AND L.AA_REFERENCIA_LIBERACAO = A.AA_ADITAMENTO
                                     AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = A.NU_SEM_ADITAMENTO
                 LEFT OUTER JOIN FES.FESTB010_CANDIDATO C
                                 ON L.NU_SEQ_CANDIDATO = C.NU_SEQ_CANDIDATO
                                     AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY')
                                     AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
                 LEFT OUTER JOIN FES.FESTB036_CONTRATO_FIES F
                                 ON L.NU_SEQ_CANDIDATO = F.NU_CANDIDATO_FK11
                                     AND F.NU_STATUS_CONTRATO > 3
                 LEFT OUTER JOIN FES.FESTB057_OCRRA_CONTRATO O
                                 ON L.NU_SEQ_CANDIDATO = O.NU_CANDIDATO_FK36
                                     AND O.NU_STATUS_OCORRENCIA IN (11, 18)
                                     AND O.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
                                     AND O.NU_SEMESTRE_REFERENCIA = (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END)
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND L.IC_SITUACAO_LIBERACAO = 'S'
          AND L.MM_REFERENCIA_LIBERACAO > 0
          AND (A.NU_CANDIDATO_FK36 IS NOT NULL OR C.NU_SEQ_CANDIDATO IS NOT NULL)
          AND F.NU_CANDIDATO_FK11 IS NOT NULL
          AND O.NU_CANDIDATO_FK36 IS NULL
        )
        LOOP
            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET IC_SITUACAO_LIBERACAO = 'NR',
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
        END LOOP;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('FIM DO PROCESSAMENTO DE ADEQUACAO DA SITUACAO DAS LIBERACOES SUSPENSAS QUE NÃO POSSUEM LANCAMENTOS PERTINENTES');

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA FESSPZ55_CRISE2019_TRATA_SUSP');

EXCEPTION  -- Inicio do tratamento de excessao
WHEN OTHERS THEN  -- Trata todo tipo de excessao
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE(' *** ERRO VERIFICADO: ' || SQLCODE || ' - ' || SUBSTR(SQLERRM, 1, 100));
    DBMS_OUTPUT.PUT_LINE(' *** INSTRUCAO      : ' || SQL_QUERY);

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA FESSPZ55_CRISE2019_TRATA_SUSP');

END;

---------
   CREATE OR REPLACE PROCEDURE FES.FESSPZ57_CRISE2019_CORR_VLRS
--F620600  27/05/2021 15:16:26
    --C077033  21/05/2021 17:08:00
--C077033  23/04/2021 19:15:00
--C077033  17/01/2021 20:33:00
--C077033  29/10/2020 15:25:00

AS
    QT_COMPENSACAO_CRIADA NUMERIC(10) := 0;
    SQL_QUERY VARCHAR2(500) := NULL;
    V_NU_SQNCL_COMPENSACAO_REPASSE NUMERIC(12);
    vr_repasse NUMERIC(18,2) := NULL;
    mm_repasse integer;
    MM_LIBERACAO NUMERIC(2);
    COUNT_1 NUMERIC(5) := 0;

BEGIN

    DBMS_OUTPUT.ENABLE (buffer_size => NULL);

    DBMS_OUTPUT.PUT_LINE(' ************* INICIO DA FESSPZ57_CRISE2019_CORR_VLRS - CORRIGE VALORES DE REPASSE ************* ');


    SELECT MAX(NU_SQNCL_COMPENSACAO_REPASSE) INTO V_NU_SQNCL_COMPENSACAO_REPASSE FROM FES.FESTB812_CMPSO_RPSE_INDVO;

    --COMPENSACAO, E NOVO REPASSE, DE LIBERACOES DE SEMESTRES COM PROBLEMA DE DESLOCAMENTO NO VALOR DO REPASSE
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            A.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
            L.NU_SEQ_CANDIDATO,
            L.AA_REFERENCIA_LIBERACAO,
            L.MM_REFERENCIA_LIBERACAO,
            R.NU_SQNCL_RLTRO_CTRTO_ANALITICO AS SQNCL_COMPENSADO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN (
            SELECT
                NU_CANDIDATO_FK36,
                AA_ADITAMENTO,
                NU_SEM_ADITAMENTO,
                VR_ADITAMENTO
            FROM FES.FESTB712_LIBERACAO_CONTRATO
                     INNER JOIN FES.FESTB038_ADTMO_CONTRATO
                                ON NU_SEQ_CANDIDATO = NU_CANDIDATO_FK36
                                    AND AA_REFERENCIA_LIBERACAO = AA_ADITAMENTO
                                    AND CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = NU_SEM_ADITAMENTO
                                    AND NU_STATUS_ADITAMENTO > 3
                                    AND VR_ADITAMENTO > 0
            WHERE NU_SEQ_CANDIDATO > 20000000
              AND MM_REFERENCIA_LIBERACAO > 0
            GROUP BY
                NU_CANDIDATO_FK36,
                VR_ADITAMENTO,
                AA_ADITAMENTO,
                NU_SEM_ADITAMENTO
            HAVING (
                               COUNT(VR_REPASSE) = 6
                           AND
                               SUM(VR_REPASSE) > 0
                           AND
                               VR_ADITAMENTO > SUM(VR_REPASSE)
                           AND
                               (
                                           MOD( ROUND( ( SUM(VR_REPASSE) / VR_ADITAMENTO ), 1 ), 10 ) = 0
                                       OR
                                           MOD( ROUND( ( VR_ADITAMENTO / SUM(VR_REPASSE) ), 1 ), 10 ) = 0
                                       OR
                                           VR_ADITAMENTO / SUM(VR_REPASSE) >= 100
                                   )
                       )
        ) D
                            ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK36
                                AND L.AA_REFERENCIA_LIBERACAO = D.AA_ADITAMENTO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.NU_SEM_ADITAMENTO
                 INNER JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO A
                            ON L.NU_SQNCL_LIBERACAO_CONTRATO = A.NU_SQNCL_LIBERACAO_CONTRATO
                                AND L.NU_SEQ_CANDIDATO = A.NU_SEQ_CANDIDATO
                                AND L.VR_REPASSE = A.VR_REPASSE
                 LEFT OUTER JOIN FES.FESTB812_CMPSO_RPSE_INDVO R
                                 ON R.NU_SQNCL_RLTRO_CTRTO_ANALITICO = A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
        WHERE L.IC_SITUACAO_LIBERACAO IN ('R', 'NE')
        )
        LOOP
            IF X.SQNCL_COMPENSADO IS NULL THEN
                QT_COMPENSACAO_CRIADA := QT_COMPENSACAO_CRIADA + 1;
                V_NU_SQNCL_COMPENSACAO_REPASSE := V_NU_SQNCL_COMPENSACAO_REPASSE + 1;

                SQL_QUERY := 'INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (' ||
                             'NU_SQNCL_COMPENSACAO_REPASSE, NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_TIPO_ACERTO, TS_INCLUSAO, CO_USUARIO_INCLUSAO)' ||
                             ' VALUES (' || V_NU_SQNCL_COMPENSACAO_REPASSE || ', ' || X.NU_SQNCL_RLTRO_CTRTO_ANALITICO || ', 5,''' || SYSDATE || ''', ''CRISE19'')';

                DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                EXECUTE IMMEDIATE SQL_QUERY;
            END IF;

            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET IC_SITUACAO_LIBERACAO = 'NR',
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO
              AND NU_SEQ_CANDIDATO = X.NU_SEQ_CANDIDATO
              AND AA_REFERENCIA_LIBERACAO = X.AA_REFERENCIA_LIBERACAO
              AND MM_REFERENCIA_LIBERACAO = X.MM_REFERENCIA_LIBERACAO;
        END LOOP;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE(' ************* QUANTIDADE DE COMPENSACOES CRIADAS: ' || QT_COMPENSACAO_CRIADA || ' ************* ');


    --ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DESLOCAMENTO NO VALOR DO REPASSE
    FOR X IN
        (
        SELECT
            A.NU_CANDIDATO_FK36,
            A.AA_ADITAMENTO,
            A.NU_SEM_ADITAMENTO,
            A.VR_ADITAMENTO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB038_ADTMO_CONTRATO A
                            ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK36
                                AND L.AA_REFERENCIA_LIBERACAO = A.AA_ADITAMENTO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = A.NU_SEM_ADITAMENTO
                                AND A.NU_STATUS_ADITAMENTO > 3
                                AND A.VR_ADITAMENTO > 0
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND L.MM_REFERENCIA_LIBERACAO > 0
        GROUP BY
            A.NU_CANDIDATO_FK36,
            A.VR_ADITAMENTO,
            A.AA_ADITAMENTO,
            A.NU_SEM_ADITAMENTO
        HAVING (
                           COUNT(L.VR_REPASSE) = 6
                       AND
                           SUM(L.VR_REPASSE) > 0
                       AND
                           VR_ADITAMENTO > SUM(VR_REPASSE)
                       AND
                           (
                                       MOD( ROUND( ( SUM(L.VR_REPASSE) / A.VR_ADITAMENTO ), 1 ), 10 ) = 0
                                   OR
                                       MOD( ROUND( ( A.VR_ADITAMENTO / SUM(L.VR_REPASSE) ), 1 ), 10 ) = 0
                                   OR
                                       A.VR_ADITAMENTO / SUM(L.VR_REPASSE) >= 100
                               )
                   )
        )
        LOOP
            -- Loop para tratar as 6 parcelas do semestre de cada candidato
            FOR Lcntr IN 1..6
                LOOP
                    -- Definindo o vr_repasse de cada liberacao
                    IF (Lcntr IN (6)) THEN
                        vr_repasse := (X.VR_ADITAMENTO - (TRUNC(X.VR_ADITAMENTO / 6, 2) * 5) );
                    ELSE
                        vr_repasse := TRUNC(X.VR_ADITAMENTO / 6, 2);
                    END IF;

                    -- Definindo o mes de repasse
                    IF (X.NU_SEM_ADITAMENTO in (1)) THEN
                        mm_repasse := Lcntr;
                    ELSE
                        mm_repasse := Lcntr + 6;
                    END IF;

                    MM_LIBERACAO := mm_repasse;

                    SQL_QUERY := 'UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET VR_REPASSE = ''' ||
                                 TO_CHAR(vr_repasse,'FM999999990.00') || ''', DT_ATUALIZACAO = ''' ||
                                 SYSDATE || ''' WHERE NU_SEQ_CANDIDATO = ' ||
                                 X.NU_CANDIDATO_FK36 || ' AND AA_REFERENCIA_LIBERACAO = ' ||
                                 X.AA_ADITAMENTO || ' AND MM_REFERENCIA_LIBERACAO = ' || MM_LIBERACAO;

                    DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                    EXECUTE IMMEDIATE SQL_QUERY;

                END LOOP;

            COMMIT; -- Commit para cada candidato (a cada 6 liberacoes a rotina executa o commit)

        END LOOP;
    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DESLOCAMENTO NO VALOR DO REPASSE ************* ');


    --ADEQUACAO NO VALOR DAS LIBERACOES DE SEMESTRES COM VALOR DE REPASSE ZERADO
    FOR X IN
        (
        SELECT
            A.NU_CANDIDATO_FK36,
            A.AA_ADITAMENTO,
            A.NU_SEM_ADITAMENTO,
            A.VR_ADITAMENTO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB038_ADTMO_CONTRATO A
                            ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK36
                                AND L.AA_REFERENCIA_LIBERACAO = A.AA_ADITAMENTO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = A.NU_SEM_ADITAMENTO
                                AND A.NU_STATUS_ADITAMENTO > 3
                                AND A.VR_ADITAMENTO > 0
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND L.MM_REFERENCIA_LIBERACAO > 0
        GROUP BY
            A.NU_CANDIDATO_FK36,
            A.AA_ADITAMENTO,
            A.NU_SEM_ADITAMENTO,
            A.VR_ADITAMENTO
        HAVING ( COUNT(L.VR_REPASSE) = 6 AND SUM(L.VR_REPASSE) = 0 )
        )
        LOOP
            -- Loop para tratar as 6 parcelas do semestre de cada candidato
            FOR Lcntr IN 1..6
                LOOP
                    -- Definindo o vr_repasse de cada liberacao
                    IF (Lcntr IN (6)) THEN
                        vr_repasse := (X.VR_ADITAMENTO - (TRUNC(X.VR_ADITAMENTO / 6, 2) * 5) );
                    ELSE
                        vr_repasse := TRUNC(X.VR_ADITAMENTO / 6, 2);
                    END IF;

                    -- Definindo o mes de repasse
                    IF (X.NU_SEM_ADITAMENTO in (1)) THEN
                        mm_repasse := Lcntr;
                    ELSE
                        mm_repasse := Lcntr + 6;
                    END IF;

                    MM_LIBERACAO := mm_repasse;

                    SQL_QUERY := 'UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET VR_REPASSE = ''' ||
                                 TO_CHAR(vr_repasse,'FM999999990.00') || ''', DT_ATUALIZACAO = ''' ||
                                 SYSDATE || ''' WHERE NU_SEQ_CANDIDATO = ' ||
                                 X.NU_CANDIDATO_FK36 || ' AND AA_REFERENCIA_LIBERACAO = ' ||
                                 X.AA_ADITAMENTO || ' AND MM_REFERENCIA_LIBERACAO = ' || MM_LIBERACAO;

                    DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                    EXECUTE IMMEDIATE SQL_QUERY;

                END LOOP;

            COMMIT; -- Commit para cada candidato (a cada 6 liberacoes a rotina executa o commit)

            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET IC_SITUACAO_LIBERACAO = 'NR',
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SEQ_CANDIDATO = X.NU_CANDIDATO_FK36
              AND AA_REFERENCIA_LIBERACAO = X.AA_ADITAMENTO
              AND CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = X.NU_SEM_ADITAMENTO
              AND IC_SITUACAO_LIBERACAO IN ('R', 'NE', 'E');

            COMMIT;
        END LOOP;
    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA ADEQUACAO NO VALOR DAS LIBERACOES DE SEMESTRES COM VALOR DE REPASSE ZERADO ************* ');


    --Atualização do sequencial do aditamento na TB712
    FOR X IN
        (
        SELECT
            NU_SQNCL_LIBERACAO_CONTRATO,
            NU_SEQ_ADITAMENTO
        FROM FES.FESTB712_LIBERACAO_CONTRATO
                 INNER JOIN FES.FESTB038_ADTMO_CONTRATO
                            ON NU_SEQ_CANDIDATO = NU_CANDIDATO_FK36
                                AND AA_REFERENCIA_LIBERACAO = AA_ADITAMENTO
                                AND CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = NU_SEM_ADITAMENTO
                                --AND NU_STATUS_ADITAMENTO > 3
                                AND NU_SEQ_ADITAMENTO IS NOT NULL
        WHERE NU_SEQ_CANDIDATO > 20000000
          AND ( NU_SQNCL_ADITAMENTO <> NU_SEQ_ADITAMENTO
            OR NU_SQNCL_ADITAMENTO IS NULL )
        )
        LOOP
            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET NU_SQNCL_ADITAMENTO = X.NU_SEQ_ADITAMENTO,
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO;
        END LOOP;

    COMMIT;


    /* ####################################################################
       TRATAMENTO DIVERGENCIA DE VALORES ENTRE O REPASSE E A CONTRATACAO
       ####################################################################
    */

    --CURSOR PARA INSERCAO DE RETENCAO PARA LIBERACOES POR DIVERGENCIA ENTRE REPASSE E A CONTRATACAO
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN
             (
                 SELECT
                     A.NU_CANDIDATO_FK11,
                     TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY') AS ANO,
                     (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END) AS SEMESTRE,
                     A.VR_CONTRATO,
                     SUM(L.VR_REPASSE)
                 FROM FES.FESTB712_LIBERACAO_CONTRATO L
                          INNER JOIN FES.FESTB036_CONTRATO_FIES A
                                     ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK11
                                         AND A.NU_STATUS_CONTRATO > 3
                          INNER JOIN FES.FESTB010_CANDIDATO C
                                     ON C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO
                                         AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY')
                                         AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
                 WHERE L.NU_SEQ_CANDIDATO > 20000000
                   AND L.MM_REFERENCIA_LIBERACAO > 0
                 GROUP BY
                     CO_CPF,
                     A.NU_CANDIDATO_FK11,
                     A.VR_CONTRATO,
                     TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY'),
                     (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
                 HAVING
                     (
                             ( SUM(VR_REPASSE) - VR_CONTRATO > 1 OR VR_CONTRATO - SUM(VR_REPASSE) > 1 )
                             AND
                             COUNT(L.VR_REPASSE) = 6
                         )
             ) D
             ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK11
                 AND L.AA_REFERENCIA_LIBERACAO = D.ANO
                 AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.SEMESTRE
        WHERE NOT EXISTS
            (
                SELECT 1
                FROM FES.FESTB817_RETENCAO_LIBERACAO R
                WHERE L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                  AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 9
                  AND R.DT_FIM_RETENCAO IS NULL
            )
        )
        LOOP
            --INSERE RETENCAO POR DIVERGENCIA ENTRE REPASSE E CONTRATO
            SQL_QUERY := 'INSERT INTO FES.FESTB817_RETENCAO_LIBERACAO ' ||
                         ' (NU_SQNCL_LIBERACAO_CONTRATO, NU_MOTIVO_RETENCAO_LIBERACAO, DT_INICIO_RETENCAO) values (' ||
                         X.NU_SQNCL_LIBERACAO_CONTRATO || ', ''9'',''' || SYSDATE || ''')';

            DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
            EXECUTE IMMEDIATE SQL_QUERY;
        END LOOP;

    COMMIT;


    --COMPENSACAO, E NOVO REPASSE, DE LIBERACOES DE SEMESTRES COM PROBLEMA DE DIVERGENCIA NO VALOR ENTRE REPASSE E A CONTRATACAO
    QT_COMPENSACAO_CRIADA := 0;
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            A.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
            L.NU_SEQ_CANDIDATO,
            L.AA_REFERENCIA_LIBERACAO,
            L.MM_REFERENCIA_LIBERACAO,
            L.IC_SITUACAO_LIBERACAO,
            R.NU_SQNCL_RLTRO_CTRTO_ANALITICO AS SQNCL_COMPENSADO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN (
            SELECT
                A.NU_CANDIDATO_FK11,
                TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY') AS ANO,
                (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END) AS SEMESTRE,
                A.VR_CONTRATO,
                SUM(L.VR_REPASSE)
            FROM FES.FESTB712_LIBERACAO_CONTRATO L
                     INNER JOIN FES.FESTB036_CONTRATO_FIES A
                                ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK11
                                    AND A.NU_STATUS_CONTRATO > 3
                     INNER JOIN FES.FESTB010_CANDIDATO C
                                ON C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO
                                    AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY')
                                    AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
            WHERE L.NU_SEQ_CANDIDATO > 20000000
              AND L.MM_REFERENCIA_LIBERACAO > 0
            GROUP BY
                CO_CPF,
                A.NU_CANDIDATO_FK11,
                A.VR_CONTRATO,
                TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY'),
                (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
            HAVING
                (
                            VR_CONTRATO >= 1000
                        AND
                            ( SUM(VR_REPASSE) - VR_CONTRATO > 1 OR VR_CONTRATO - SUM(VR_REPASSE) > 1 )
                        AND
                            COUNT(L.VR_REPASSE) = 6
                    )
        ) D
                            ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK11
                                AND L.AA_REFERENCIA_LIBERACAO = D.ANO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.SEMESTRE
                 INNER JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO A
                            ON L.NU_SQNCL_LIBERACAO_CONTRATO = A.NU_SQNCL_LIBERACAO_CONTRATO
                                AND L.NU_SEQ_CANDIDATO = A.NU_SEQ_CANDIDATO
                                AND L.VR_REPASSE = A.VR_REPASSE
                 LEFT OUTER JOIN FES.FESTB812_CMPSO_RPSE_INDVO R
                                 ON R.NU_SQNCL_RLTRO_CTRTO_ANALITICO = A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
        WHERE L.IC_SITUACAO_LIBERACAO IN ('R', 'NE')
        )
        LOOP
            IF X.SQNCL_COMPENSADO IS NULL THEN
                QT_COMPENSACAO_CRIADA := QT_COMPENSACAO_CRIADA + 1;
                V_NU_SQNCL_COMPENSACAO_REPASSE := V_NU_SQNCL_COMPENSACAO_REPASSE + 1;

                SQL_QUERY := 'INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (' ||
                             'NU_SQNCL_COMPENSACAO_REPASSE, NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_TIPO_ACERTO, TS_INCLUSAO, CO_USUARIO_INCLUSAO)' ||
                             ' VALUES (' || V_NU_SQNCL_COMPENSACAO_REPASSE || ', ' || X.NU_SQNCL_RLTRO_CTRTO_ANALITICO || ', 7,''' || SYSDATE || ''', ''CRISE19'')';

                DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                EXECUTE IMMEDIATE SQL_QUERY;
            END IF;


            IF X.IC_SITUACAO_LIBERACAO = 'R' THEN
                UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                SET IC_SITUACAO_LIBERACAO = 'NR',
                    DT_ATUALIZACAO = SYSDATE
                WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO
                  AND NU_SEQ_CANDIDATO = X.NU_SEQ_CANDIDATO
                  AND AA_REFERENCIA_LIBERACAO = X.AA_REFERENCIA_LIBERACAO
                  AND MM_REFERENCIA_LIBERACAO = X.MM_REFERENCIA_LIBERACAO;
            ELSE
                UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                SET IC_SITUACAO_LIBERACAO = 'S',
                    DT_ATUALIZACAO = SYSDATE
                WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO
                  AND NU_SEQ_CANDIDATO = X.NU_SEQ_CANDIDATO
                  AND AA_REFERENCIA_LIBERACAO = X.AA_REFERENCIA_LIBERACAO
                  AND MM_REFERENCIA_LIBERACAO = X.MM_REFERENCIA_LIBERACAO;
            END IF;

        END LOOP;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE(' ************* QUANTIDADE DE COMPENSACOES CRIADAS: ' || QT_COMPENSACAO_CRIADA || ' ************* ');


    --ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DIVERGENCIA NO VALOR ENTRE REPASSE E A CONTRATACAO
    FOR X IN
        (
        SELECT
            A.NU_CANDIDATO_FK11,
            TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY') AS ANO,
            (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END) AS SEMESTRE,
            A.VR_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB036_CONTRATO_FIES A
                            ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK11
                                AND A.NU_STATUS_CONTRATO > 3
                 INNER JOIN FES.FESTB010_CANDIDATO C
                            ON C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO
                                AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY')
                                AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND L.MM_REFERENCIA_LIBERACAO > 0
        GROUP BY
            A.NU_CANDIDATO_FK11,
            A.VR_CONTRATO,
            TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY'),
            (CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
        HAVING
            (
                        VR_CONTRATO >= 1000
                    AND
                        ( SUM(VR_REPASSE) - VR_CONTRATO > 1 OR VR_CONTRATO - SUM(VR_REPASSE) > 1 )
                    AND
                        COUNT(L.VR_REPASSE) = 6
                )
        )
        LOOP
            -- Loop para tratar as 6 parcelas do semestre de cada candidato
            FOR Lcntr IN 1..6
                LOOP
                    -- Definindo o vr_repasse de cada liberacao
                    IF (Lcntr IN (6)) THEN
                        vr_repasse := (X.VR_CONTRATO - (TRUNC(X.VR_CONTRATO / 6, 2) * 5) );
                    ELSE
                        vr_repasse := TRUNC(X.VR_CONTRATO / 6, 2);
                    END IF;

                    -- Definindo o mes de repasse
                    IF (X.SEMESTRE in (1)) THEN
                        mm_repasse := Lcntr;
                    ELSE
                        mm_repasse := Lcntr + 6;
                    END IF;

                    MM_LIBERACAO := mm_repasse;

                    SQL_QUERY := 'UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET VR_REPASSE = ''' ||
                                 TO_CHAR(vr_repasse,'FM999999990.00') || ''', DT_ATUALIZACAO = ''' ||
                                 SYSDATE || ''' WHERE NU_SEQ_CANDIDATO = ' ||
                                 X.NU_CANDIDATO_FK11 || ' AND AA_REFERENCIA_LIBERACAO = ' ||
                                 X.ANO || ' AND MM_REFERENCIA_LIBERACAO = ' || MM_LIBERACAO;

                    DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                    EXECUTE IMMEDIATE SQL_QUERY;

                END LOOP;

            COMMIT; -- Commit para cada candidato (a cada 6 liberacoes a rotina executa o commit)

        END LOOP;
    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DIVERGENCIA NO VALOR ENTRE REPASSE E CONTRATO ************* ');



    /* ####################################################################
       TRATAMENTO DIVERGENCIA DE VALORES ENTRE O REPASSE E O ADITAMENTO
       ####################################################################
    */

    --CURSOR PARA INSERCAO DE RETENCAO PARA LIBERACOES POR DIVERGENCIA ENTRE REPASSE E ADITAMENTO
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN (
            SELECT
                NU_CANDIDATO_FK36,
                AA_ADITAMENTO,
                NU_SEM_ADITAMENTO
            FROM FES.FESTB712_LIBERACAO_CONTRATO
                     INNER JOIN FES.FESTB038_ADTMO_CONTRATO
                                ON NU_SEQ_CANDIDATO = NU_CANDIDATO_FK36
                                    AND AA_REFERENCIA_LIBERACAO = AA_ADITAMENTO
                                    AND CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = NU_SEM_ADITAMENTO
                                    AND NU_STATUS_ADITAMENTO > 3
            WHERE NU_SEQ_CANDIDATO > 20000000
              AND MM_REFERENCIA_LIBERACAO > 0
            GROUP BY
                NU_CANDIDATO_FK36,
                AA_ADITAMENTO,
                NU_SEM_ADITAMENTO,
                VR_ADITAMENTO
            HAVING 	(
                              ( SUM(VR_REPASSE) - VR_ADITAMENTO >= 1 OR VR_ADITAMENTO - SUM(VR_REPASSE) >= 1 )
                              AND
                              COUNT(VR_REPASSE) = 6
                          )
        ) D
                            ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK36
                                AND L.AA_REFERENCIA_LIBERACAO = D.AA_ADITAMENTO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.NU_SEM_ADITAMENTO
        WHERE NOT EXISTS 	(
                SELECT 1
                FROM FES.FESTB817_RETENCAO_LIBERACAO R
                WHERE L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                  AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 9
                  AND R.DT_FIM_RETENCAO IS NULL
            )
        )
        LOOP
            --INSERE RETENCAO POR DIVERGENCIA ENTRE REPASSE E ADITAMENTO
            SQL_QUERY := 'INSERT INTO FES.FESTB817_RETENCAO_LIBERACAO ' ||
                         ' (NU_SQNCL_LIBERACAO_CONTRATO, NU_MOTIVO_RETENCAO_LIBERACAO, DT_INICIO_RETENCAO) values (' ||
                         X.NU_SQNCL_LIBERACAO_CONTRATO || ', ''9'',''' || SYSDATE || ''')';

            DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
            EXECUTE IMMEDIATE SQL_QUERY;
        END LOOP;

    COMMIT;


    --COMPENSACAO, E NOVO REPASSE, DE LIBERACOES DE SEMESTRES COM PROBLEMA DE DIVERGENCIA NO VALOR ENTRE REPASSE E ADITAMENTO
    QT_COMPENSACAO_CRIADA := 0;
    FOR X IN
        (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            A.NU_SQNCL_RLTRO_CTRTO_ANALITICO,
            L.NU_SEQ_CANDIDATO,
            L.AA_REFERENCIA_LIBERACAO,
            L.MM_REFERENCIA_LIBERACAO,
            IC_SITUACAO_LIBERACAO,
            R.NU_SQNCL_RLTRO_CTRTO_ANALITICO AS SQNCL_COMPENSADO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN (
            SELECT
                NU_CANDIDATO_FK36,
                AA_ADITAMENTO,
                NU_SEM_ADITAMENTO,
                VR_ADITAMENTO
            FROM FES.FESTB712_LIBERACAO_CONTRATO
                     INNER JOIN FES.FESTB038_ADTMO_CONTRATO
                                ON NU_SEQ_CANDIDATO = NU_CANDIDATO_FK36
                                    AND AA_REFERENCIA_LIBERACAO = AA_ADITAMENTO
                                    AND CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = NU_SEM_ADITAMENTO
                                    AND NU_STATUS_ADITAMENTO > 3
                                    AND VR_ADITAMENTO > 0
            WHERE NU_SEQ_CANDIDATO > 20000000
              AND MM_REFERENCIA_LIBERACAO > 0
            GROUP BY
                NU_CANDIDATO_FK36,
                VR_ADITAMENTO,
                AA_ADITAMENTO,
                NU_SEM_ADITAMENTO
            HAVING (
                               VR_ADITAMENTO > 0
                           AND
                               ( ( SUM(VR_REPASSE) - VR_ADITAMENTO >= 1 AND SUM(VR_REPASSE) / VR_ADITAMENTO <= 5 )
                                   OR VR_ADITAMENTO - SUM(VR_REPASSE) >= 1
                                   )
                           AND
                               COUNT(VR_REPASSE) = 6
                       )
        ) D
                            ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK36
                                AND L.AA_REFERENCIA_LIBERACAO = D.AA_ADITAMENTO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.NU_SEM_ADITAMENTO
                 INNER JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO A
                            ON L.NU_SQNCL_LIBERACAO_CONTRATO = A.NU_SQNCL_LIBERACAO_CONTRATO
                                AND L.NU_SEQ_CANDIDATO = A.NU_SEQ_CANDIDATO
                                AND L.VR_REPASSE = A.VR_REPASSE
                 LEFT OUTER JOIN FES.FESTB812_CMPSO_RPSE_INDVO R
                                 ON R.NU_SQNCL_RLTRO_CTRTO_ANALITICO = A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
        WHERE L.IC_SITUACAO_LIBERACAO IN ('R', 'NE')
        )
        LOOP
            IF X.SQNCL_COMPENSADO IS NULL THEN
                QT_COMPENSACAO_CRIADA := QT_COMPENSACAO_CRIADA + 1;
                V_NU_SQNCL_COMPENSACAO_REPASSE := V_NU_SQNCL_COMPENSACAO_REPASSE + 1;

                SQL_QUERY := 'INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (' ||
                             'NU_SQNCL_COMPENSACAO_REPASSE, NU_SQNCL_RLTRO_CTRTO_ANALITICO, NU_TIPO_ACERTO, TS_INCLUSAO, CO_USUARIO_INCLUSAO)' ||
                             ' VALUES (' || V_NU_SQNCL_COMPENSACAO_REPASSE || ', ' || X.NU_SQNCL_RLTRO_CTRTO_ANALITICO || ', 7,''' || SYSDATE || ''', ''CRISE19'')';

                DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                EXECUTE IMMEDIATE SQL_QUERY;
            END IF;


            IF X.IC_SITUACAO_LIBERACAO = 'R' THEN
                UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                SET IC_SITUACAO_LIBERACAO = 'NR',
                    DT_ATUALIZACAO = SYSDATE
                WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO
                  AND NU_SEQ_CANDIDATO = X.NU_SEQ_CANDIDATO
                  AND AA_REFERENCIA_LIBERACAO = X.AA_REFERENCIA_LIBERACAO
                  AND MM_REFERENCIA_LIBERACAO = X.MM_REFERENCIA_LIBERACAO;
            ELSE
                UPDATE FES.FESTB712_LIBERACAO_CONTRATO
                SET IC_SITUACAO_LIBERACAO = 'S',
                    DT_ATUALIZACAO = SYSDATE
                WHERE NU_SQNCL_LIBERACAO_CONTRATO = X.NU_SQNCL_LIBERACAO_CONTRATO
                  AND NU_SEQ_CANDIDATO = X.NU_SEQ_CANDIDATO
                  AND AA_REFERENCIA_LIBERACAO = X.AA_REFERENCIA_LIBERACAO
                  AND MM_REFERENCIA_LIBERACAO = X.MM_REFERENCIA_LIBERACAO;
            END IF;

        END LOOP;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE(' ************* QUANTIDADE DE COMPENSACOES CRIADAS: ' || QT_COMPENSACAO_CRIADA || ' ************* ');


    --ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DIVERGENCIA NO VALOR ENTRE REPASSE E ADITAMENTO
    FOR X IN
        (
        SELECT
            A.NU_CANDIDATO_FK36,
            A.AA_ADITAMENTO,
            A.NU_SEM_ADITAMENTO,
            A.VR_ADITAMENTO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
                 INNER JOIN FES.FESTB038_ADTMO_CONTRATO A
                            ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK36
                                AND L.AA_REFERENCIA_LIBERACAO = A.AA_ADITAMENTO
                                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = A.NU_SEM_ADITAMENTO
                                AND A.NU_STATUS_ADITAMENTO > 3
                                AND A.VR_ADITAMENTO > 0
        WHERE L.NU_SEQ_CANDIDATO > 20000000
          AND L.MM_REFERENCIA_LIBERACAO > 0
        GROUP BY
            A.NU_CANDIDATO_FK36,
            A.VR_ADITAMENTO,
            A.AA_ADITAMENTO,
            A.NU_SEM_ADITAMENTO
        HAVING (
                           VR_ADITAMENTO > 0
                       AND
                           ( ( SUM(VR_REPASSE) - VR_ADITAMENTO >= 1 AND SUM(VR_REPASSE) / VR_ADITAMENTO <= 5 )
                               OR VR_ADITAMENTO - SUM(VR_REPASSE) >= 1
                               )
                       AND
                           COUNT(VR_REPASSE) = 6
                   )
        )
        LOOP
            -- Loop para tratar as 6 parcelas do semestre de cada candidato
            FOR Lcntr IN 1..6
                LOOP
                    -- Definindo o vr_repasse de cada liberacao
                    IF (Lcntr IN (6)) THEN
                        vr_repasse := (X.VR_ADITAMENTO - (TRUNC(X.VR_ADITAMENTO / 6, 2) * 5) );
                    ELSE
                        vr_repasse := TRUNC(X.VR_ADITAMENTO / 6, 2);
                    END IF;

                    -- Definindo o mes de repasse
                    IF (X.NU_SEM_ADITAMENTO in (1)) THEN
                        mm_repasse := Lcntr;
                    ELSE
                        mm_repasse := Lcntr + 6;
                    END IF;

                    MM_LIBERACAO := mm_repasse;

                    SQL_QUERY := 'UPDATE FES.FESTB712_LIBERACAO_CONTRATO SET VR_REPASSE = ''' ||
                                 TO_CHAR(vr_repasse,'FM999999990.00') || ''', DT_ATUALIZACAO = ''' ||
                                 SYSDATE || ''' WHERE NU_SEQ_CANDIDATO = ' ||
                                 X.NU_CANDIDATO_FK36 || ' AND AA_REFERENCIA_LIBERACAO = ' ||
                                 X.AA_ADITAMENTO || ' AND MM_REFERENCIA_LIBERACAO = ' || MM_LIBERACAO;

                    DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
                    EXECUTE IMMEDIATE SQL_QUERY;

                END LOOP;

            COMMIT; -- Commit para cada candidato (a cada 6 liberacoes a rotina executa o commit)

        END LOOP;
    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA ADEQUACAO DO VALOR DAS LIBERACOES DE SEMESTRES COM PROBLEMA DE DIVERGENCIA NO VALOR ENTRE REPASSE E ADITAMENTO ************* ');



    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA FESSPZ57_CRISE2019_CORR_VLRS ************* ');

EXCEPTION  -- Inicio do tratamento de excessao
WHEN OTHERS THEN  -- Trata todo tipo de excessao
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE(' *** ERRO VERIFICADO: ' || SQLCODE || ' - ' || SUBSTR(SQLERRM, 1, 100));
    DBMS_OUTPUT.PUT_LINE(' *** INSTRUCAO      : ' || SQL_QUERY);
END;
-----------------------------------

CREATE OR REPLACE PROCEDURE FES.FESSPZ41_CRISE19_FIM_RETENC_2
--C077033  26/05/2022 16:29:58
--C077033  24/05/2022 14:00:00
--F620600  27/05/2021 15:18:23
--C077033  22/05/2021 12:32:00
--C077033  22/04/2021 12:40:00
--C077033  17/01/2021 20:33:00
--C077033  24/11/2020 18:10:00
--C077033  29/10/2020 16:33:00
--C077033  30/09/2020 16:20:00
--C077033  14/08/2020 15:00:00
--C077033  24/07/2020 12:00:00
--C077033  29/06/2020 15:00:00
--C077033  29/05/2020 10:00:00

AS
    SQL_QUERY VARCHAR2(30000) := NULL;
BEGIN

    DBMS_OUTPUT.PUT_LINE(' ************* INICIO DA FESSPZ41_CRISE19_FIM_RETENCAO - ENCERRA RETENCOES DE LIBERACOES ************* ');

    SQL_QUERY := 'ALTER SESSION SET NLS_DATE_FORMAT = ''DD/MM/YYYY''';
    DBMS_OUTPUT.PUT_LINE(' ************* ' || SQL_QUERY);
    EXECUTE IMMEDIATE SQL_QUERY;

    SQL_QUERY := 'ALTER SESSION SET NLS_TIMESTAMP_FORMAT = ''DD/MM/YYYY''';
    DBMS_OUTPUT.PUT_LINE(' ************* ' || SQL_QUERY);
    EXECUTE IMMEDIATE SQL_QUERY;

    -- Cursor com as liberacoes que possuem retencao por transferencia e
    -- que deverao ser encaminhadas no proximo repasse.
    FOR x IN
    (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
            INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
                ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
				AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 2
				AND R.DT_FIM_RETENCAO IS NULL
				AND L.IC_SITUACAO_LIBERACAO IN ('NR')
            INNER JOIN FES.FESTB049_TRANSFERENCIA T
                ON L.NU_SEQ_CANDIDATO = T.NU_CANDIDATO_FK10
                AND T.NU_STATUS_TRANSFERENCIA = 5
            INNER JOIN FES.FESTB154_CAMPUS_INEP C
                ON T.NU_CAMPUS_ORIGEM_FK161 = C.NU_CAMPUS
            INNER JOIN FES.FESTB154_CAMPUS_INEP I
                ON T.NU_CAMPUS_DESTINO_FK161 = I.NU_CAMPUS
            LEFT OUTER JOIN FES.FESTB038_ADTMO_CONTRATO A
                ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK36
				--AND A.NU_STATUS_ADITAMENTO > 3
				AND A.DT_ADITAMENTO IS NOT NULL
				AND L.AA_REFERENCIA_LIBERACAO = A.AA_ADITAMENTO
				AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = A.NU_SEM_ADITAMENTO
            LEFT OUTER JOIN FES.FESTB010_CANDIDATO CA
                ON L.NU_SEQ_CANDIDATO = CA.NU_SEQ_CANDIDATO
                AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(CA.DT_ADMISSAO_CANDIDATO,'YYYY')
                AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = (CASE WHEN TO_CHAR(CA.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
        WHERE ( A.NU_CANDIDATO_FK36 IS NOT NULL OR CA.NU_SEQ_CANDIDATO IS NOT NULL )
        AND (
                (
					I.NU_IES_FK155 = L.NU_IES 
					AND 
						(
							T.AA_REFERENCIA < L.AA_REFERENCIA_LIBERACAO
							OR
								(
									T.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
									AND T.NU_SEM_REFERENCIA < CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
								)
						)
						AND T.NU_CANDIDATO_FK10 NOT IN 
													(
														SELECT Q.NU_CANDIDATO_FK10
														FROM FES.FESTB049_TRANSFERENCIA Q
														WHERE Q.NU_CANDIDATO_FK10 > 20000000
														AND Q.NU_STATUS_TRANSFERENCIA = 5
														AND Q.NU_SEQ_TRANSFERENCIA > T.NU_SEQ_TRANSFERENCIA
														AND 
															( 
																Q.AA_REFERENCIA < L.AA_REFERENCIA_LIBERACAO
																OR 
																	(
																		Q.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
																		AND Q.NU_SEM_REFERENCIA <= CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
																	)
															)
													)
                )
                OR
					( 
						I.NU_IES_FK155 = L.NU_IES
						AND 
							(
								T.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO 
								AND T.NU_SEM_REFERENCIA = CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END 
							)
							AND 
								(
									(
										T.AA_REFERENCIA = A.AA_ADITAMENTO 
										AND T.NU_SEM_REFERENCIA = A.NU_SEM_ADITAMENTO 
										AND TO_CHAR(T.DT_INCLUSAO, 'YYYYMMDD') <= TO_CHAR(A.DT_ADITAMENTO, 'YYYYMMDD') 
									)
									OR
										(
											T.AA_REFERENCIA = TO_CHAR(CA.DT_ADMISSAO_CANDIDATO,'YYYY') 
											AND T.NU_SEM_REFERENCIA = (CASE WHEN TO_CHAR(CA.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
											AND T.DT_INCLUSAO < CA.DT_ADMISSAO_CANDIDATO 
										)
								)
					)
                OR
					( 
						C.NU_IES_FK155 = L.NU_IES
						AND 
							( 
								T.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO 
								AND T.NU_SEM_REFERENCIA = CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END 
							)
						AND 
							(  
								( 
									T.AA_REFERENCIA = A.AA_ADITAMENTO 
									AND T.NU_SEM_REFERENCIA = A.NU_SEM_ADITAMENTO 
									AND TO_CHAR(T.DT_INCLUSAO, 'YYYYMMDD') > TO_CHAR(A.DT_ADITAMENTO, 'YYYYMMDD') 
								)
								OR 
									( 
										T.AA_REFERENCIA = TO_CHAR(CA.DT_ADMISSAO_CANDIDATO,'YYYY') 
										AND T.NU_SEM_REFERENCIA = (CASE WHEN TO_CHAR(CA.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END)
										AND T.DT_INCLUSAO > CA.DT_ADMISSAO_CANDIDATO 
									) 
							) 
					)
                OR
					( 
						C.NU_IES_FK155 = L.NU_IES
						AND 
							( 
								T.AA_REFERENCIA > L.AA_REFERENCIA_LIBERACAO
								OR 
									( 
										T.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
										AND T.NU_SEM_REFERENCIA > (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END)
									)
							)
						AND T.NU_CANDIDATO_FK10 NOT IN 
														( 
															SELECT P.NU_CANDIDATO_FK10
															FROM FES.FESTB049_TRANSFERENCIA P
															WHERE P.NU_CANDIDATO_FK10 > 20000000
															AND P.NU_STATUS_TRANSFERENCIA = 5
															AND P.NU_SEQ_TRANSFERENCIA < T.NU_SEQ_TRANSFERENCIA
														)
                    )
            )
    )
    LOOP
		-- FINALIZA A RETENCAO DO TIPO TRANSFERENCIA
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
					 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
					 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 2';
        --DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
        EXECUTE IMMEDIATE SQL_QUERY;
    END LOOP;
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO


    -- Cursor com as liberacoes que possuem retencao por TRANSFERENCIA COM IC_SITUACAO_LIBERACAO <> NR e devem ser finalizadas
    FOR X IN
    (
        SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
            INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
                ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
				AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 2
				AND R.DT_FIM_RETENCAO IS NULL
        WHERE L.IC_SITUACAO_LIBERACAO <> 'NR'
    )
    LOOP
		-- FINALIZA A RETENCAO DO TIPO TRANSFERENCIA
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
					 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
					 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 2';
		--DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
        EXECUTE IMMEDIATE SQL_QUERY;
    END LOOP;
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO



    -- Cursor com as liberacoes que possuem retencao por SUSPENSAO e que deverao ser encaminhadas no proximo repasse.
    FOR x IN
    (
        SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
            INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
                ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 3
                AND R.DT_FIM_RETENCAO IS NULL
            LEFT OUTER JOIN FES.FESTB057_OCRRA_CONTRATO O
                ON L.NU_SEQ_CANDIDATO = O.NU_CANDIDATO_FK36
				AND O.IC_TIPO_OCORRENCIA = 'S'
				AND O.NU_STATUS_OCORRENCIA = 11
				AND O.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
				AND O.NU_SEMESTRE_REFERENCIA = CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
        WHERE O.NU_CANDIDATO_FK36 IS NULL
    )
    LOOP
		-- FINALIZA A RETENCAO DO TIPO SUSPENSAO
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
					 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
					 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 3';
		--DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
		EXECUTE IMMEDIATE SQL_QUERY;
    END LOOP;
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO


    -- Cursor com as liberacoes que possuem retencao por SUSPENSAO PARCIAL e que deverao ser encaminhadas no proximo repasse.
    FOR x IN
    (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO,
            L.IC_SITUACAO_LIBERACAO,
            L.DT_LIBERACAO,
            O.DT_INICIO_VIGENCIA,
            O.DT_FIM_VIGENCIA
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
            INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
                ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 3
                AND R.DT_FIM_RETENCAO IS NULL
            INNER JOIN FES.FESTB057_OCRRA_CONTRATO O
                ON L.NU_SEQ_CANDIDATO = O.NU_CANDIDATO_FK36
                AND O.IC_TIPO_OCORRENCIA = 'S'
                AND  O.NU_STATUS_OCORRENCIA = 11
                AND O.IC_TIPO_SUSPENSAO = 'P'
                AND O.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
                AND O.NU_SEMESTRE_REFERENCIA <= CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
                AND TO_CHAR(O.DT_OCORRENCIA, 'YYYY') = O.AA_REFERENCIA
                AND 
					( 
						( 
							O.NU_SEMESTRE_REFERENCIA = 1 
							AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '06' 
							AND TO_CHAR(O.DT_FIM_VIGENCIA, 'DDMM') = '3006' 
						)
                        OR
                            ( 
								O.NU_SEMESTRE_REFERENCIA = 2 
								AND 
									( 
										TO_CHAR(O.DT_OCORRENCIA, 'MM') > '06' 
										AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '12' 
									)
								AND TO_CHAR(O.DT_FIM_VIGENCIA, 'DDMM') = '3112' 
							)
                    )
                AND DT_INICIO_VIGENCIA = LAST_DAY(DT_OCORRENCIA) + 1
        WHERE L.IC_SITUACAO_LIBERACAO IN ('NR', 'S', 'R')
        OR 
			( 
				L.IC_SITUACAO_LIBERACAO = 'NE' 
				AND L.DT_LIBERACAO > O.DT_INICIO_VIGENCIA 
				AND L.DT_LIBERACAO < O.DT_FIM_VIGENCIA 
			)
    )
    LOOP
        IF 	x.IC_SITUACAO_LIBERACAO = 'NR' 
			AND x.DT_LIBERACAO > x.DT_INICIO_VIGENCIA 
			AND x.DT_LIBERACAO < x.DT_FIM_VIGENCIA
        THEN
            UPDATE FES.FESTB712_LIBERACAO_CONTRATO
            SET IC_SITUACAO_LIBERACAO = 'S',
                DT_ATUALIZACAO = SYSDATE
            WHERE NU_SQNCL_LIBERACAO_CONTRATO = x.NU_SQNCL_LIBERACAO_CONTRATO;
        END IF;

		-- FINALIZA A RETENCAO DO TIPO SUSPENSAO
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
					 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
					 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 3';
		--DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
		EXECUTE IMMEDIATE SQL_QUERY;
    END LOOP;
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO


    -- Cursor com as liberacoes que possuem retencao por falha na vinculacao entre tabelas e
    -- que deverao ser encaminhadas no proximo repasse.
    FOR x IN
    (
        SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
            INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
                ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 4
                AND R.DT_FIM_RETENCAO IS NULL
        WHERE L.NU_TIPO_TRANSACAO IS NOT NULL
    )
    LOOP
		-- FINALIZA A RETENCAO DO TIPO FALHA NA VINCULACAO ENTRE TABELAS
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
					 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
					 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 4';
		--DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
		EXECUTE IMMEDIATE SQL_QUERY;
	END LOOP;
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO


    -- Cursor com as liberacoes que possuem retencao por AUSENCIA DE ADITAMENTO VALIDO e devera ser finalizada.
    FOR x IN
	(
        SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
            INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
                ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 7
                AND R.DT_FIM_RETENCAO IS NULL
			LEFT OUTER JOIN FES.FESTB038_ADTMO_CONTRATO A
                ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK36
                AND A.NU_STATUS_ADITAMENTO > 3
                AND A.DT_ADITAMENTO IS NOT NULL
                AND L.AA_REFERENCIA_LIBERACAO = A.AA_ADITAMENTO
                AND ( CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END ) = A.NU_SEM_ADITAMENTO
            LEFT OUTER JOIN FES.FESTB010_CANDIDATO CA
                ON L.NU_SEQ_CANDIDATO = CA.NU_SEQ_CANDIDATO
                AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(CA.DT_ADMISSAO_CANDIDATO,'YYYY')
                AND ( CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END ) = ( CASE WHEN TO_CHAR(CA.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END )
            LEFT OUTER JOIN FES.FESTB036_CONTRATO_FIES F
                ON L.NU_SEQ_CANDIDATO = F.NU_CANDIDATO_FK11
                AND F.NU_STATUS_CONTRATO > 3
                AND F.DT_ASSINATURA IS NOT NULL
        WHERE
				(
                    A.NU_CANDIDATO_FK36 IS NOT NULL
                    OR
                        ( 
							CA.NU_SEQ_CANDIDATO IS NOT NULL 
							AND F.NU_CANDIDATO_FK11 IS NOT NULL 
						)
                )
	)
    LOOP
		-- FINALIZA A RETENCAO DO TIPO AUSENCIA DE ADITAMENTO VALIDO
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
					 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
					 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 7';
		--DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
		EXECUTE IMMEDIATE SQL_QUERY;
	END LOOP;
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO



    -- Cursor com as liberacoes que possuem retencao por AUSENCIA DE ADITAMENTO VALIDO COM IC_SITUACAO_LIBERACAO <> NR e R
    -- e devem ser finalizadas
    FOR X IN
    (
        SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
            INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
                ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 7
                AND R.DT_FIM_RETENCAO IS NULL
        WHERE L.IC_SITUACAO_LIBERACAO NOT IN ('NR', 'R')
	)
    LOOP
		-- FINALIZA A RETENCAO DO TIPO AUSENCIA DE ADITAMENTO VALIDO
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
					 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
					 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 7';
		--DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
		EXECUTE IMMEDIATE SQL_QUERY;
	END LOOP;
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO



    -- Cursor com as liberacoes que possuem retencao por ANALISE DE LIBERACOES A ESTORNAR COM IC_SITUACAO_LIBERACAO <> NE
    -- e devem ser finalizadas
    FOR X IN
   (
        SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
            INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
                ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 5
                AND R.DT_FIM_RETENCAO IS NULL
        WHERE L.IC_SITUACAO_LIBERACAO <> 'NE'
	)
    LOOP
		-- FINALIZA A RETENCAO DO TIPO ANALISE DE LIBERACOES A ESTORNAR
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
					 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
					 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 5';
		--DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
		EXECUTE IMMEDIATE SQL_QUERY;
	END LOOP;
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO



    -- Cursor com as liberacoes que possuem retencao por ANALISE DE LIBERACOES A ESTORNAR e deverao ser finalizadas.
    FOR x IN
    (
        SELECT L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
            INNER JOIN FES.FESTB817_RETENCAO_LIBERACAO R
                ON L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
                AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 5
                AND R.DT_FIM_RETENCAO IS NULL
            LEFT OUTER JOIN FES.FESTB038_ADTMO_CONTRATO A
                ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK36
                AND L.AA_REFERENCIA_LIBERACAO = A.AA_ADITAMENTO
                AND (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END) = A.NU_SEM_ADITAMENTO
                AND A.NU_STATUS_ADITAMENTO > 3
            LEFT OUTER JOIN FES.FESTB010_CANDIDATO C
                ON L.NU_SEQ_CANDIDATO = C.NU_SEQ_CANDIDATO
                AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'YYYY')
                AND ( CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END ) = ( CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END )
            LEFT OUTER JOIN FES.FESTB036_CONTRATO_FIES F
                ON L.NU_SEQ_CANDIDATO = F.NU_CANDIDATO_FK11
                AND F.NU_STATUS_CONTRATO > 3
            LEFT OUTER JOIN FES.FESTB057_OCRRA_CONTRATO O
                ON L.NU_SEQ_CANDIDATO = O.NU_CANDIDATO_FK36
                AND O.IC_TIPO_OCORRENCIA = 'S'
                AND O.IC_TIPO_SUSPENSAO = 'P'
                AND O.NU_STATUS_OCORRENCIA = 11
                AND O.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
                AND O.NU_SEMESTRE_REFERENCIA = ( CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END )
                AND TO_CHAR(O.DT_OCORRENCIA, 'YYYY') = O.AA_REFERENCIA
                AND 
					(
                        ( 
							O.NU_SEMESTRE_REFERENCIA = 1 
							AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '06' 
							AND TO_CHAR(O.DT_FIM_VIGENCIA, 'DDMM') = '3006' 
						)
                        OR
                            ( 
								O.NU_SEMESTRE_REFERENCIA = 2 
								AND ( TO_CHAR(O.DT_OCORRENCIA, 'MM') > '06' 
								AND TO_CHAR(O.DT_OCORRENCIA, 'MM') < '12' ) 
								AND TO_CHAR(O.DT_FIM_VIGENCIA, 'DDMM') = '3112' 
							)
                    )
                AND O.DT_INICIO_VIGENCIA = LAST_DAY(O.DT_OCORRENCIA) + 1
                AND L.DT_LIBERACAO > O.DT_INICIO_VIGENCIA
            LEFT OUTER JOIN FES.FESTB057_OCRRA_CONTRATO OC
                ON L.NU_SEQ_CANDIDATO = OC.NU_CANDIDATO_FK36
                AND OC.IC_TIPO_OCORRENCIA = 'E'
                AND OC.NU_STATUS_OCORRENCIA = 11
                AND OC.AA_REFERENCIA = L.AA_REFERENCIA_LIBERACAO
                AND OC.NU_SEMESTRE_REFERENCIA = (CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END)
                AND TO_CHAR(OC.DT_OCORRENCIA, 'YYYY') = OC.AA_REFERENCIA
                AND 
					(
                        ( 
							OC.NU_SEMESTRE_REFERENCIA = 1 
							AND TO_CHAR(OC.DT_OCORRENCIA, 'MM') < '06' 
						)
                        OR
							( 
								OC.NU_SEMESTRE_REFERENCIA = 2 
								AND TO_CHAR(OC.DT_OCORRENCIA, 'MM') > '06' 
								AND TO_CHAR(OC.DT_OCORRENCIA, 'MM') < '12' 
							)
                    )
                AND OC.DT_INICIO_VIGENCIA = LAST_DAY(OC.DT_OCORRENCIA) + 1
                AND L.DT_LIBERACAO > OC.DT_INICIO_VIGENCIA
        WHERE L.IC_SITUACAO_LIBERACAO = 'NE'
        AND 
			(
				(
					A.NU_CANDIDATO_FK36 IS NULL 
					AND C.NU_SEQ_CANDIDATO IS NULL
				)
                OR
					(
						C.NU_SEQ_CANDIDATO IS NOT NULL 
						AND F.NU_CANDIDATO_FK11 IS NULL
					)
                OR O.NU_CANDIDATO_FK36 IS NOT NULL
                OR OC.NU_CANDIDATO_FK36 IS NOT NULL
            )
    )
    LOOP
		-- FINALIZA A RETENCAO DO TIPO ANALISE DE LIBERACOES A ESTORNAR
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
					 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
					 x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 5';
		--DBMS_OUTPUT.PUT_LINE(SQL_QUERY);
		EXECUTE IMMEDIATE SQL_QUERY;
        END LOOP;
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO



    -- Cursor com as liberacoes que possuem retencao por DIVERGENCIA ENTRE REPASSE E ADITAMENTO e devera ser finalizada.
    FOR x IN
    (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
            INNER JOIN
            (
                SELECT
                    NU_CANDIDATO_FK36,
                    AA_ADITAMENTO,
                    NU_SEM_ADITAMENTO
                FROM FES.FESTB712_LIBERACAO_CONTRATO
                    INNER JOIN FES.FESTB038_ADTMO_CONTRATO
                        ON NU_SEQ_CANDIDATO = NU_CANDIDATO_FK36
                        AND AA_REFERENCIA_LIBERACAO = AA_ADITAMENTO
                        AND CASE WHEN MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = NU_SEM_ADITAMENTO
                        AND NU_STATUS_ADITAMENTO > 3
                WHERE NU_SEQ_CANDIDATO > 20000000
                AND MM_REFERENCIA_LIBERACAO > 0
                GROUP BY
                    NU_CANDIDATO_FK36,
                    AA_ADITAMENTO,
                    NU_SEM_ADITAMENTO,
                    VR_ADITAMENTO
                HAVING  (
                            (
                                ( SUM(VR_REPASSE) - VR_ADITAMENTO ) BETWEEN 0 AND 1
                                OR
                                ( VR_ADITAMENTO - SUM(VR_REPASSE) ) BETWEEN 0 AND 1
                            )
                            AND COUNT(VR_REPASSE) = 6
                        )
            ) D
				ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK36
                AND L.AA_REFERENCIA_LIBERACAO = D.AA_ADITAMENTO
                AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.NU_SEM_ADITAMENTO
        WHERE EXISTS
        (
            SELECT 1
            FROM FES.FESTB817_RETENCAO_LIBERACAO R
            WHERE L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
            AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 9
            AND R.DT_FIM_RETENCAO IS NULL
        )
    )
    LOOP
		-- FINALIZA A RETENCAO DO TIPO DIVERGENCIA ENTRE REPASSE E ADITAMENTO
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
						TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
						x.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 9';
		EXECUTE IMMEDIATE SQL_QUERY;
    END LOOP;
	
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO


    -- Cursor com as liberacoes que possuem retencao por DIVERGENCIA ENTRE REPASSE E CONTRATACAO e devera ser finalizada.
    FOR X IN
    (
        SELECT
            L.NU_SQNCL_LIBERACAO_CONTRATO
        FROM FES.FESTB712_LIBERACAO_CONTRATO L
			INNER JOIN
            (
                SELECT
					A.NU_CANDIDATO_FK11,
                    TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY') AS ANO,
                    ( CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END ) AS SEMESTRE,
                    A.VR_CONTRATO,
                    SUM(L.VR_REPASSE)
                FROM FES.FESTB712_LIBERACAO_CONTRATO L
					INNER JOIN FES.FESTB036_CONTRATO_FIES A
                        ON L.NU_SEQ_CANDIDATO = A.NU_CANDIDATO_FK11
                        AND A.NU_STATUS_CONTRATO > 3
                    INNER JOIN FES.FESTB010_CANDIDATO C
                        ON C.NU_SEQ_CANDIDATO = L.NU_SEQ_CANDIDATO
                        AND L.AA_REFERENCIA_LIBERACAO = TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY')
                        AND ( CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END ) = ( CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END )
                WHERE L.NU_SEQ_CANDIDATO > 20000000
                AND L.MM_REFERENCIA_LIBERACAO > 0
                GROUP BY
					CO_CPF,
                    A.NU_CANDIDATO_FK11,
                    A.VR_CONTRATO,
                    TO_CHAR(C.DT_ADMISSAO_CANDIDATO, 'YYYY'),
                    ( CASE WHEN TO_CHAR(C.DT_ADMISSAO_CANDIDATO,'MM') < 7 THEN 1 ELSE 2 END )
                HAVING
                (
                    (
                        ( SUM(VR_REPASSE) - VR_CONTRATO ) BETWEEN 0 AND 1
                        OR
                        ( VR_CONTRATO - SUM(VR_REPASSE) ) BETWEEN 0 AND 1
                    )
                    AND COUNT(L.VR_REPASSE) = 6
                )
            ) D
				ON L.NU_SEQ_CANDIDATO = D.NU_CANDIDATO_FK11
				AND L.AA_REFERENCIA_LIBERACAO = D.ANO
				AND CASE WHEN L.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END = D.SEMESTRE
            LEFT OUTER JOIN FES.FESTB711_RLTRO_CTRTO_ANLTO A
                ON L.NU_SQNCL_LIBERACAO_CONTRATO = A.NU_SQNCL_LIBERACAO_CONTRATO
                AND L.NU_SEQ_CANDIDATO = A.NU_SEQ_CANDIDATO
            LEFT OUTER JOIN FES.FESTB812_CMPSO_RPSE_INDVO R
                ON R.NU_SQNCL_RLTRO_CTRTO_ANALITICO = A.NU_SQNCL_RLTRO_CTRTO_ANALITICO
                AND R.NU_TIPO_ACERTO = 7
                AND R.IC_COMPENSADO = 'N'
                AND A.VR_REPASSE > L.VR_REPASSE
        WHERE EXISTS
        (
            SELECT 1
            FROM FES.FESTB817_RETENCAO_LIBERACAO R
            WHERE L.NU_SQNCL_LIBERACAO_CONTRATO = R.NU_SQNCL_LIBERACAO_CONTRATO
            AND R.NU_MOTIVO_RETENCAO_LIBERACAO = 9
            AND R.DT_FIM_RETENCAO IS NULL
        )
        AND ( A.NU_SQNCL_LIBERACAO_CONTRATO IS NULL OR R.NU_SQNCL_RLTRO_CTRTO_ANALITICO IS NULL )
    )
	LOOP
		-- FINALIZA A RETENCAO DO TIPO DIVERGENCIA ENTRE REPASSE E CONTRATACAO
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
					 TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
					 X.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 9';
		EXECUTE IMMEDIATE SQL_QUERY;
	END LOOP;
		
    COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO
	
	DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA FINALIZACAO DE RETENCOES POR DIVERGENCIA ENTRE REPASSE E CONTRATACAO ************* ');
	
	
	-- CURSOR PARA SELECIONAR LIBERACOES A TEREM RETENCAO POR AUSENCIA DE FINALIZACAO NO PROCESSO DE ADITAMENTO FINALIZADAS
	FOR X IN
	(
		SELECT
			T712.NU_SQNCL_LIBERACAO_CONTRATO
		FROM FES.FESTB712_LIBERACAO_CONTRATO T712
			INNER JOIN FES.FESTB038_ADTMO_CONTRATO T38
				ON T38.NU_CANDIDATO_FK36 = T712.NU_SEQ_CANDIDATO
				AND T38.AA_ADITAMENTO = T712.AA_REFERENCIA_LIBERACAO
				AND T38.NU_SEM_ADITAMENTO = CASE WHEN T712.MM_REFERENCIA_LIBERACAO < 7 THEN 1 ELSE 2 END
				AND T38.NU_STATUS_ADITAMENTO IN (4, 5)
			INNER JOIN FES.FESTB759_PROCESSO_ADITAMENTO T759
				ON T759.NU_CANDIDATO = T38.NU_CANDIDATO_FK36
				AND T759.AA_REFERENCIA = T38.AA_ADITAMENTO
				AND T759.NU_SEMESTRE_REFERENCIA = T38.NU_SEM_ADITAMENTO
				AND T759.NU_SITUACAO_PROCESSO = 9
		WHERE T712.NU_SEQ_CANDIDATO > 20000000
		AND T712.IC_SITUACAO_LIBERACAO = 'NR'
		AND EXISTS  (
						SELECT 1
						FROM FES.FESTB817_RETENCAO_LIBERACAO T817
						WHERE T817.NU_SQNCL_LIBERACAO_CONTRATO = T712.NU_SQNCL_LIBERACAO_CONTRATO
						AND T817.NU_MOTIVO_RETENCAO_LIBERACAO = 10
						AND T817.DT_FIM_RETENCAO IS NULL
					)
	)
	LOOP
		--FINALIZA RETENCAO POR AUSENCIA DE FINALIZACAO NO PROCESSO DE ADITAMENTO
		SQL_QUERY := 'UPDATE FES.FESTB817_RETENCAO_LIBERACAO SET DT_FIM_RETENCAO = ''' ||
						TO_CHAR(SYSDATE,'DD/MM/YYYY') || ''' WHERE NU_SQNCL_LIBERACAO_CONTRATO = ' ||
						X.NU_SQNCL_LIBERACAO_CONTRATO || ' AND NU_MOTIVO_RETENCAO_LIBERACAO = 10';

		EXECUTE IMMEDIATE SQL_QUERY;
	END LOOP;						
			
	COMMIT; -- COMMIT NO FINAL DA ATUALIZACAO

	DBMS_OUTPUT.PUT_LINE(' ************* FIM DO PROCESSAMENTO DA FINALIZACAO DE RETENCOES POR AUSENCIA DE FINALIZACAO NO PROCESSO DE ADITAMENTO ************* ');

    DBMS_OUTPUT.PUT_LINE(' ************* FIM DA FESSPZ41_CRISE19_FIM_RETENCAO ************* ');


EXCEPTION  -- Inicio do tratamento de excessao
WHEN OTHERS THEN  -- Trata todo tipo de excessao
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE(' *** ERRO VERIFICADO: ' || SQLCODE || ' - ' || SUBSTR(SQLERRM, 1, 100));
    DBMS_OUTPUT.PUT_LINE(' *** INSTRUCAO      : ' || SQL_QUERY);
END;
-----------------------------------------

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
