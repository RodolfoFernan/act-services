<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
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
