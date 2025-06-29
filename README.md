<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:

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

=======================================================Sp base ========================================
CREATE OR REPLACE PROCEDURE FESSPZ67_CRISE2025_COMPENSA_DUPLCD (
    p_co_usuario_execucao IN VARCHAR2 -- Usuário que executa a SP, e.g., 'SP_FES_CRISIS'
)
AS
    -- Variáveis locais
    v_nu_sqncl_rl_analitico_duplicado NUMBER;
    v_existe_na_812                   NUMBER;

    -- Cursor para iterar sobre os registros já identificados na 909
    -- Adapte as colunas do SELECT para refletir exatamente o que está na sua FESTB909
    CURSOR c_duplicidades IS
        SELECT
            T909.NU_SQNCL_LIBERACAO_CONTRATO,
            T909.NU_SEQ_CANDIDATO,          -- Se na 909 o contrato for NU_SEQ_CANDIDATO
            T909.NU_IES,
            T909.NU_CAMPUS,
            T909.MM_REFERENCIA_LIBERACAO AS MM_REFERENCIA,
            T909.AA_REFERENCIA_LIBERACAO AS AA_REFERENCIA,
            T909.NU_PARCELA,
            T909.VR_REPASSE                 -- O VR_REPASSE da liberação duplicada
        FROM FES.FESTB909_RECOMP_712 T909;

    -- ** AQUI VOCÊ DEVE CONFIRMAR O VALOR CORRETO PARA O NU_TIPO_ACERTO **
    -- Exemplo: v_nu_tipo_acerto_compensacao CONSTANT NUMBER := 5; -- Ou outro valor definido pelo negócio
    v_nu_tipo_acerto_compensacao CONSTANT NUMBER := <CONFIRMAR_VALOR_COM_GESTOR_OU_SP_EXISTENTE>;

BEGIN
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
            AND T711.NU_SEQ_CANDIDATO = r_duplicidade.NU_SEQ_CANDIDATO -- Ou NU_CONTRATO se for o campo na 711
            AND T711.NU_IES = r_duplicidade.NU_IES
            AND T711.NU_CAMPUS = r_duplicidade.NU_CAMPUS
            AND T711.MM_REFERENCIA = r_duplicidade.MM_REFERENCIA
            AND T711.AA_REFERENCIA = r_duplicidade.AA_REFERENCIA
            AND T711.NU_PARCELA = r_duplicidade.NU_PARCELA
            -- Adicionar critério para pegar o registro DUPLICADO na 711, se houver mais de um.
            -- Por exemplo, se a 909 identifica o ID da liberação que *resultou* no repasse duplicado,
            -- este JOIN já deve ser suficiente. Caso contrário, adicione:
            -- AND T711.VR_REPASSE = r_duplicidade.VR_REPASSE -- para ser mais específico
            ORDER BY T711.TS_APURACAO_RELATORIO DESC -- Geralmente o duplicado é o mais recente
            FETCH FIRST 1 ROW ONLY; -- Para Oracle 12c+. Para 11g, use ROWNUM = 1

        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                DBMS_OUTPUT.PUT_LINE('ALERTA: Não encontrado NU_SQNCL_RLTRO_CTRTO_ANALITICO na FESTB711 para liberação da 909 (ID: ' || r_duplicidade.NU_SQNCL_LIBERACAO_CONTRATO || ', Contrato: ' || r_duplicidade.NU_SEQ_CANDIDATO || ').');
                CONTINUE; -- Pula para a próxima duplicidade
            WHEN TOO_MANY_ROWS THEN
                DBMS_OUTPUT.PUT_LINE('ERRO: Múltiplos NU_SQNCL_RLTRO_CTRTO_ANALITICO encontrados na FESTB711 para liberação da 909 (ID: ' || r_duplicidade.NU_SQNCL_LIBERACAO_CONTRATO || ', Contrato: ' || r_duplicidade.NU_SEQ_CANDIDATO || '). Verifique critério de desempate.');
                CONTINUE; -- Pula para a próxima duplicidade (necessita ajuste se for recorrente)
        END;

        -- 2. Verificar se o sequencial já existe na FESTB812 (Diretriz 8)
        SELECT COUNT(1)
        INTO v_existe_na_812
        FROM FES.FESTB812_CMPSO_RPSE_INDVO
        WHERE NU_SQNCL_RLTRO_CTRTO_ANALITICO = v_nu_sqncl_rl_analitico_duplicado;

        IF v_existe_na_812 = 0 THEN
            -- 3. Inserir na FESTB812
            INSERT INTO FES.FESTB812_CMPSO_RPSE_INDVO (
                NU_SQNCL_COMPENSACAO_REPASSE,
                NU_SQNCL_RLTRO_CTRTO_ANALITICO,
                NU_TIPO_ACERTO,
                TS_INCLUSAO,
                CO_USUARIO_INCLUSAO,
                IC_COMPENSADO
            ) VALUES (
                -- ASSUMIR QUE EXISTE UMA SEQUENCE, EX: SEQ_FESTB812.NEXTVAL
                SEQ_FESTB812.NEXTVAL,
                v_nu_sqncl_rl_analitico_duplicado,
                v_nu_tipo_acerto_compensacao, -- Usar o valor confirmado
                SYSTIMESTAMP,
                p_co_usuario_execucao,
                'N'
            );
            DBMS_OUTPUT.PUT_LINE('SUCESSO: Inserido NU_SQNCL_RLTRO_CTRTO_ANALITICO ' || v_nu_sqncl_rl_analitico_duplicado || ' na FESTB812 para compensação.');
        ELSE
            DBMS_OUTPUT.PUT_LINE('AVISO: NU_SQNCL_RLTRO_CTRTO_ANALITICO ' || v_nu_sqncl_rl_analitico_duplicado || ' já existe na FESTB812. Ignorando inserção (Diretriz 8).');
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
/
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
