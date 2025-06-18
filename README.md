<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:
Excelente! Vamos detalhar a FES.FESSPU20_VINCULA_LIBERACAO no formato solicitado:
FES.FESSPU20_VINCULA_LIBERACAO

    Objetivo: Vincular as liberações de contrato a seus respectivos aditamentos ou contratos iniciais, marcando o tipo de transação e a participação do candidato. Além disso, a SP insere retenções para liberações que não conseguem ser vinculadas.

package br.gov.caixa.fes.negocio;

import br.gov.caixa.fes.BaseSIFESTest;
import br.gov.caixa.fes.dominio.*;
import br.gov.caixa.fes.dominio.documento.Documento;
import br.gov.caixa.fes.dominio.recompra.LiminarArquivo;
import br.gov.caixa.fes.dominio.recompra.LiminarRecompraTO;
import br.gov.caixa.fes.dominio.recompra.ParametroEmailLiminarTO;
import br.gov.caixa.fes.dominio.transicao.CadastroLiminarTO;
import br.gov.caixa.fes.dominio.transicao.MantenedoraNovoFiesTO;
import br.gov.caixa.fes.dto.contrato.EmailMessageTO;
import br.gov.caixa.fes.siecm.dominio.DocumentoSiecmService;
import br.gov.caixa.fes.siecm.dominio.model.*;
import br.gov.caixa.fes.siecm.dominio.model.Arquivo;
import br.gov.caixa.fes.siecm.dominio.model.Transacao;
import br.gov.caixa.fes.util.SecurityKeycloakUtils;
import br.gov.caixa.fes.negocio.exception.FESException; // Assumindo o pacote da sua FESException
import br.gov.caixa.fes.util.MensagemUtil; // Assumindo o pacote da sua MensagemUtil
import br.gov.caixa.fes.util.Constantes; // Assumindo o pacote da sua Constantes

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.SerializationUtils; // Import for SerializationUtils
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor; // To capture arguments passed to methods
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import javax.persistence.Query;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LiminarBeanTest extends BaseSIFESTest {

    private static final String SUCESSO = "sucesso";

    private static final String CODIGO_TRANSACAO_PARAM_NAME = "codigoProcesso";
    private static final String TABELA_LIMINAR = "FESTB_LIMINAR";

    @Mock
    private AuditoriaBean auditoriaBean;

    @Mock
    private EmailMessageOperadorService emailMessageOperadorService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SecurityKeycloakUtils securityKeycloakUtils;

    @Mock
    private DocumentoSiecmService documentoSiecmService;

    @Mock
    private Logger logger;

    private final Gson gson = new GsonBuilder().create();

    @InjectMocks
    @Spy
    private LiminarBean liminarBean;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        liminarBean.setEm(this.em);
        liminarBean.setGson(gson);
        liminarBean.setModelMapper(modelMapper);

        try {
            java.lang.reflect.Field loggerField = LiminarBean.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set(liminarBean, logger);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Log this if it's a critical error for your setup, otherwise ignore.
        }
    }

    // --- MÉTODOS AUXILIARES (mantidos e ajustados) ---

    private LiminarRecompraTO criarLiminarRecompraTO(String codigoTransacao, String usuarioAtestado) {
        LiminarRecompraTO liminar = new LiminarRecompraTO();
        liminar.setCodigoTransacao(codigoTransacao);
        liminar.setUsuario(usuarioAtestado);
        liminar.setTpSituacao("P"); // Exemplo: Estado pendente inicial
        return liminar;
    }

    // Adicionado para criar um CadastroLiminar (similar a LiminarRecompraTO, mas para o método consultar)
    private CadastroLiminar criarCadastroLiminar(String codigoTransacao, String tpSituacao) {
        CadastroLiminar liminar = new CadastroLiminar();
        liminar.setCodigoTransacao(codigoTransacao);
        liminar.setTpSituacao(tpSituacao);
        // Adicione outros campos necessários que o SerializationUtils possa precisar
        return liminar;
    }

    private ParametroEmailLiminarTO criarParametroEmailLiminarTO() {
        ParametroEmailLiminarTO parametroEmailLiminarTO = new ParametroEmailLiminarTO();
        return parametroEmailLiminarTO;
    }

    private void mockBuscaLiminarParaAtestar(LiminarRecompraTO retornoLiminar, Class<? extends Throwable> exceptionType) {
        Query queryMock = mock(Query.class);
        when(this.em.createNamedQuery(LiminarRecompraTO.QUERY_FIND_BY_CODIGO_TRANSACAO)).thenReturn(queryMock);
        when(queryMock.setParameter(eq(CODIGO_TRANSACAO_PARAM_NAME), anyString())).thenReturn(queryMock);

        if (exceptionType != null) {
            if (exceptionType.equals(NoResultException.class)) {
                when(queryMock.getSingleResult()).thenThrow(new NoResultException("Liminar não encontrada no banco de dados."));
            } else if (exceptionType.equals(NonUniqueResultException.class)) {
                when(queryMock.getSingleResult()).thenThrow(new NonUniqueResultException("Mais de uma liminar encontrada."));
            } else {
                try {
                    when(queryMock.getSingleResult()).thenThrow(exceptionType.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Erro ao instanciar exceção mock.", e);
                }
            }
        } else {
            when(queryMock.getSingleResult()).thenReturn(retornoLiminar);
        }
    }

    private void mockRetornaUltimaConfiguracaoEmailValida(ParametroEmailLiminarTO param) throws FESException {
        doReturn(param).when(liminarBean).retornaUltimaConfiguracaoEmailValida();
    }

    private void mockVerificaSeUsuarioAtualPodeAtestarLiminar(boolean podeAtestar) throws FESException {
        if (podeAtestar) {
            doNothing().when(liminarBean).verificaSeUsuarioAtualPodeAtestarLiminar(any(LiminarRecompraTO.class), anyString());
        } else {
            doThrow(new FESException("O mesmo usuário que criou a liminar não pode atesta-la."))
                .when(liminarBean).verificaSeUsuarioAtualPodeAtestarLiminar(any(LiminarRecompraTO.class), anyString());
        }
    }

    private void mockSalvaLiminarHistorico() throws Exception {
        doNothing().when(liminarBean).salvaLiminarHistorico(
            any(CadastroLiminar.class), any(CadastroLiminar.class), anyString(), anyString(), anyString()
        );
    }

    private void mockEnviarEmailNotificacaoLiminar() throws FESException {
        doNothing().when(liminarBean).enviarEmailNotificacaoLiminar(anyString(), anyString(), anyString(), any(ParametroEmailLiminarTO.class));
    }

    private void mockGravaAuditoria() throws FESException {
        doNothing().when(auditoriaBean).gravaAuditoria(anyString(), eq(TABELA_LIMINAR), eq(Constantes.ALTERACAO), any());
    }
    
    // Método auxiliar para mockar `this.consultar(codigoTransacao, false)`
    private void mockConsultarInterno(String codigoTransacao, CadastroLiminar retorno) throws Exception {
        // Mocking a call to `this.consultar` within the @Spy `liminarBean` instance
        // This is crucial because `atestarLiminarEgravarHistorico` calls `this.consultar`
        doReturn(Collections.singletonList(retorno))
            .when(liminarBean).consultar(eq(codigoTransacao), eq(false));
    }


    // --- TESTES DE CENÁRIOS PRINCIPAIS ---

    @Test
    public void deveAtestarLiminarComSucesso() throws Exception {
        String codigoProcesso = "12345678901234567890";
        String usuario = "usuarioTeste"; // Diferente de "usuario2" do objTO
        String ip = "192.168.1.1";

        LiminarRecompraTO liminarRecompraTO = criarLiminarRecompraTO(codigoProcesso, "usuarioCriador"); // Cria um TO para a busca inicial
        CadastroLiminar objOriginal = criarCadastroLiminar(codigoProcesso, "P"); // Cria um CadastroLiminar para o clone

        // 1. Configurar Mocks
        mockBuscaLiminarParaAtestar(liminarRecompraTO, null); // Simula LiminarRecompraTO encontrada
        mockRetornaUltimaConfiguracaoEmailValida(criarParametroEmailLiminarTO());
        mockVerificaSeUsuarioAtualPodeAtestarLiminar(true); // Permissão para atestar

        // MOCKS CRUCIAIS PARA atestarLiminarEgravarHistorico
        // Mock da chamada interna a consultar para retornar o objOriginal
        mockConsultarInterno(codigoProcesso, objOriginal);
        // Não mockar atestarLiminarEgravarHistorico com doNothing() se quisermos testar sua lógica interna
        // Certifique-se de que salvaLiminarHistorico e persist são mockados
        mockSalvaLiminarHistorico();
        doNothing().when(em).persist(any(LiminarRecompraTO.class)); // Mock do persist

        // Mocks para o resto do fluxo
        mockEnviarEmailNotificacaoLiminar();
        mockGravaAuditoria();

        // 2. Executar o método sob teste
        Retorno retorno = liminarBean.atestarLiminar(codigoProcesso, usuario, ip);

        // 3. Verificar as asserções do retorno
        Assert.assertEquals(Long.valueOf(0L), retorno.getCodigo());
        Assert.assertEquals("Liminar Atestada com sucesso.", retorno.getMensagem());
        Assert.assertEquals(MensagemUtil.TipoMensagem.SUCESSO, retorno.getTipoMensagem());

        // 4. Verificar as interações com os mocks
        verify(em, times(1)).createNamedQuery(LiminarRecompraTO.QUERY_FIND_BY_CODIGO_TRANSACAO);
        verify(liminarBean, times(1)).retornaUltimaConfiguracaoEmailValida();
        verify(liminarBean, times(1)).verificaSeUsuarioAtualPodeAtestarLiminar(liminarRecompraTO, usuario);

        // Verificações para atestarLiminarEgravarHistorico (já que não estamos mockando-o com doNothing())
        verify(liminarBean, times(1)).consultar(eq(codigoProcesso), eq(false)); // Chamada interna a consultar
        verify(em, times(1)).persist(liminarRecompraTO); // Verifica se o objTO foi persistido

        // Capturar argumentos para verificar o historico
        ArgumentCaptor<CadastroLiminar> originalCaptor = ArgumentCaptor.forClass(CadastroLiminar.class);
        ArgumentCaptor<CadastroLiminar> alteradoCaptor = ArgumentCaptor.forClass(CadastroLiminar.class);
        verify(liminarBean, times(1)).salvaLiminarHistorico(
            originalCaptor.capture(), alteradoCaptor.capture(), eq(usuario), eq(LiminarBean.OPERACAO_ALTERACAO), eq(ip)
        );

        CadastroLiminar historicoOriginal = originalCaptor.getValue();
        CadastroLiminar historicoAlterado = alteradoCaptor.getValue();

        // Verifique o estado do histórico:
        Assert.assertEquals("P", historicoOriginal.getTpSituacao()); // Original deve manter o status anterior
        Assert.assertEquals(LiminarRecompraTO.SITUACAO_ATESTADA, historicoAlterado.getTpSituacao()); // Alterado deve ter o novo status
        Assert.assertEquals(liminarRecompraTO.getTpSituacao(), LiminarRecompraTO.SITUACAO_ATESTADA); // Verifica que o objTO também foi atualizado

        verify(liminarBean, times(1)).enviarEmailNotificacaoLiminar(codigoProcesso, usuario, "ATESTAR", any(ParametroEmailLiminarTO.class));
        verify(auditoriaBean, times(1)).gravaAuditoria(usuario, TABELA_LIMINAR, Constantes.ALTERACAO, liminarRecompraTO);
        verifyNoMoreInteractions(logger); // No errors should be logged
    }

    @Test
    public void deveLancarFESExceptionQuandoUsuarioMesmoCriadorNaoPodeAtestar() throws Exception {
        // Cenário: O usuário que tenta atestar é o mesmo que criou a liminar.

        String codigoProcesso = "12345678901234567890";
        String usuario = "usuarioCriador"; // MESMO USUÁRIO
        String ip = "192.168.1.100";

        LiminarRecompraTO liminarMock = criarLiminarRecompraTO(codigoProcesso, usuario); // Usuario no TO é o mesmo

        mockBuscaLiminarParaAtestar(liminarMock, null);
        mockRetornaUltimaConfiguracaoEmailValida(criarParametroEmailLiminarTO());
        mockVerificaSeUsuarioAtualPodeAtestarLiminar(false); // FORÇA A EXCEÇÃO DE PERMISSÃO

        try {
            liminarBean.atestarLiminar(codigoProcesso, usuario, ip);
            Assert.fail("Esperava FESException 'O mesmo usuário que criou a liminar não pode atesta-la.' mas nenhuma foi lançada.");
        } catch (FESException e) {
            Assert.assertEquals("A mensagem da exceção deve indicar que o usuário é o criador.", "O mesmo usuário que criou a liminar não pode atesta-la.", e.getMessage());
        }

        // Verifica que o logger foi chamado com a exceção
        verify(logger, times(1)).log(eq(Level.SEVERE), eq("O mesmo usuário que criou a liminar não pode atesta-la."), any(FESException.class));
        // Verifica que os métodos seguintes não foram chamados
        verify(liminarBean, never()).atestarLiminarEgravarHistorico(any(LiminarRecompraTO.class), anyString(), anyString());
        verify(em, never()).persist(any());
        verify(liminarBean, never()).salvaLiminarHistorico(any(), any(), anyString(), anyString(), anyString());
        verify(liminarBean, never()).enviarEmailNotificacaoLiminar(anyString(), anyString(), anyString(), any(ParametroEmailLiminarTO.class));
        verify(auditoriaBean, never()).gravaAuditoria(anyString(), anyString(), anyString(), any());
    }

    // --- Outros testes da sua classe original (mantidos) ---
    // ... (rest of your test methods) ...

    @Test
    public void deveConsultarDadosOperador() throws Exception {
        CadastroLiminarConsulta liminarConsulta = new CadastroLiminarConsulta();
        CadastroLiminarTO liminarTO = new CadastroLiminarTO();
        liminarTO.setCodigoTransacao("1234");
        liminarTO.setTransacao("RCO");
        liminarTO.setTipoLiminar(22);
        liminarTO.setAbrangencia("N");
        liminarTO.setCodigoAbrangencia(5);

        List<CadastroLiminarTO> liminarTOS = new ArrayList<>();
        liminarTOS.add(liminarTO);

        MantenedoraNovoFiesTO mantenedoraNovoFiesTO = new MantenedoraNovoFiesTO(1L, "teste1");
        mantenedoraNovoFiesTO.setCnpj("05369688000103");

        LiminarRecompraTO liminarRecompraTO = new LiminarRecompraTO();
        liminarRecompraTO.setCodigoTransacao("1234");
        liminarRecompraTO.setMantenedoras(Arrays.asList(mantenedoraNovoFiesTO));

        Query query = mock(Query.class);
        when(this.em.createNamedQuery(CadastroLiminarTO.QUERY_NAME_OPERADOR)).thenReturn(query);
        when(query.getResultList()).thenReturn(liminarTOS);
        when(this.em.find(LiminarRecompraTO.class, "1234")).thenReturn(liminarRecompraTO);

        List<CadastroLiminar> resultado = liminarBean.consultar(liminarConsulta);

        Assert.assertNotNull(resultado);
        Assert.assertFalse(resultado.isEmpty());
        verify(em, times(1)).createNamedQuery(CadastroLiminarTO.QUERY_NAME_OPERADOR);
        verify(em, times(1)).find(LiminarRecompraTO.class, "1234");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void deveSalvarRecompraAgenteOperador() throws Exception {
        LiminarRecompra liminarRecompra = new LiminarRecompra();
        liminarRecompra.setCodigoTransacao("134");
        liminarRecompra.setCodigo(0L); // Código 0L indica nova liminar
        liminarRecompra.setTipoLiminar(23L);

        Documento doc1 = new Documento();
        doc1.setExtensaoArquivo("pdf");

        Documento doc2 = new Documento();
        doc2.setExtensaoArquivo("txt");

        liminarRecompra.setDocumentos(Arrays.asList(doc1, doc2));

        ParametroEmailLiminarTO parametroEmailLiminarTO = new ParametroEmailLiminarTO();

        Query queryParamEmail = mock(Query.class);
        when(this.em.createNamedQuery(ParametroEmailLiminarTO.QUERY_BUSCAR_ULTIMA_CONFIG_VALIDA)).thenReturn(queryParamEmail);
        when(queryParamEmail.getResultList()).thenReturn(Collections.singletonList(parametroEmailLiminarTO));

        Query queryConsultarSeLiminarExiste = mock(Query.class);
        when(this.em.createNamedQuery(LiminarRecompraTO.QUERY_FIND_BY_CODIGO_TRANSACAO_FETCHING_ALL_LISTS)).thenReturn(queryConsultarSeLiminarExiste);
        when(queryConsultarSeLiminarExiste.getResultList()).thenReturn(Collections.emptyList());

        doNothing().when(emailMessageOperadorService).enviarEmail(Matchers.<EmailMessageTO>any());

        DocumentoResponse documentoResponse = new DocumentoResponse();
        documentoResponse.setAtributos(new AtributosResponse());

        IncluirResponse response = new IncluirResponse();
        response.setCodigoRetorno(0);
        response.setMensagem(SUCESSO);
        response.setDocumento(documentoResponse);

        when(documentoSiecmService.getTransacao(Matchers.<Transacao>any(), anyString())).thenReturn(SUCESSO);
        when(documentoSiecmService.incluirDocumento(Matchers.<Arquivo>any(), anyString())).thenReturn(response);
        
        doNothing().when(auditoriaBean).gravaAuditoria(anyString(), anyString(), anyString(), any());

        Retorno retorno = liminarBean.salvarRecompraAgenteOperador(liminarRecompra, "usuario1", "ip", securityKeycloakUtils);

        Assert.assertEquals(Long.valueOf(0L), retorno.getCodigo());
        verify(emailMessageOperadorService, times(1)).enviarEmail(any(EmailMessageTO.class));
        verify(documentoSiecmService, times(1)).getTransacao(any(Transacao.class), anyString());
        verify(documentoSiecmService, times(liminarRecompra.getDocumentos().size())).incluirDocumento(any(Arquivo.class), anyString());
        verify(auditoriaBean, times(1)).gravaAuditoria(anyString(), anyString(), anyString(), any());
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void deveSalvarRecompraAgenteOperadorEditandoLiminar() throws Exception {
        LiminarRecompraTO liminarRecompraTO = new LiminarRecompraTO();
        liminarRecompraTO.setCodigoTransacao("134");
        liminarRecompraTO.setTipoLiminar(22L);

        LiminarRecompra liminarRecompra = new LiminarRecompra();
        liminarRecompra.setCodigoTransacao("134");
        liminarRecompra.setCodigo(134L);
        liminarRecompra.setTipoLiminar(23L);

        this.mockConsultaParametroEmailLiminar();

        when(this.em.find(eq(LiminarRecompraTO.class), anyLong())).thenReturn(liminarRecompraTO);
        doNothing().when(em).merge(any(LiminarRecompraTO.class));

        Query queryConsultarSeLiminarExiste = mock(Query.class);
        when(this.em.createNamedQuery(LiminarRecompraTO.QUERY_FIND_BY_CODIGO_TRANSACAO_FETCHING_ALL_LISTS)).thenReturn(queryConsultarSeLiminarExiste);
        when(queryConsultarSeLiminarExiste.getResultList()).thenReturn(Arrays.asList(liminarRecompraTO));

        doNothing().when(emailMessageOperadorService).enviarEmail(Matchers.<EmailMessageTO>any());

        DocumentoResponse documentoResponse = new DocumentoResponse();
        documentoResponse.setAtributos(new AtributosResponse());

        IncluirResponse response = new IncluirResponse();
        response.setCodigoRetorno(0);
        response.setMensagem(SUCESSO);
        response.setDocumento(documentoResponse);

        when(documentoSiecmService.getTransacao(Matchers.<Transacao>any(), anyString())).thenReturn(SUCESSO);
        when(documentoSiecmService.incluirDocumento(Matchers.<Arquivo>any(), anyString())).thenReturn(response);

        Query queryConsultaUltimaCodTransacao = mock(Query.class);
        when(this.em.createNativeQuery("SELECT MAX(FL.CO_PROCESSO_LIMINAR) FROM FES.FESTB228_LIMINAR fl " +
                "WHERE FL.CO_PROCESSO_LIMINAR LIKE :cod ")).thenReturn(queryConsultaUltimaCodTransacao);
        when(queryConsultaUltimaCodTransacao.getSingleResult()).thenReturn(null);
        
        doNothing().when(auditoriaBean).gravaAuditoria(anyString(), anyString(), anyString(), any());

        Retorno retorno = liminarBean.salvarRecompraAgenteOperador(liminarRecompra, "usuario2", "ip", securityKeycloakUtils);

        Assert.assertEquals(Long.valueOf(0L), retorno.getCodigo());
        verify(em, times(1)).find(eq(LiminarRecompraTO.class), eq(liminarRecompra.getCodigo()));
        verify(em, times(1)).merge(any(LiminarRecompraTO.class));
        verify(emailMessageOperadorService, times(1)).enviarEmail(any(EmailMessageTO.class));
        verify(documentoSiecmService, times(1)).getTransacao(any(Transacao.class), anyString());
        verify(auditoriaBean, times(1)).gravaAuditoria(anyString(), anyString(), anyString(), any());
        verifyNoMoreInteractions(logger);
    }

    private void mockConsultaParametroEmailLiminar() {
        ParametroEmailLiminarTO parametroEmailLiminarTO = new ParametroEmailLiminarTO();

        Query queryParamEmail = mock(Query.class);
        when(this.em.createNamedQuery(ParametroEmailLiminarTO.QUERY_BUSCAR_ULTIMA_CONFIG_VALIDA)).thenReturn(queryParamEmail);
        when(queryParamEmail.getResultList()).thenReturn(Collections.singletonList(parametroEmailLiminarTO));
    }


    @Test
    public void deveConsultarLiminar() throws Exception {
        this.mockConsultaLiminarSemDetalhes();

        List<CadastroLiminar> cadastroLiminares = liminarBean.consultar("1234", false);

        Assert.assertNotNull(cadastroLiminares);
        Assert.assertFalse(cadastroLiminares.isEmpty());
        verify(em, times(1)).createNamedQuery(CadastroLiminarTO.QUERY_NAME);
        verifyNoMoreInteractions(logger);
    }

    private void mockConsultaLiminarSemDetalhes() {
        Query query = mock(Query.class);
        when(this.em.createNamedQuery(CadastroLiminarTO.QUERY_NAME)).thenReturn(query);
        when(query.getResultList()).thenReturn(getCadastroLiminarTOS());
    }

    @Test
    public void deveConsultarPorCodigoTransacaoEDetalhado() throws Exception {
        List<CadastroLiminarTO> cadastroLiminarTOS = getCadastroLiminarTOS();

        Query query = mock(Query.class);
        when(this.em.createNamedQuery(CadastroLiminarTO.QUERY_NAME)).thenReturn(query);
        when(query.getResultList()).thenReturn(cadastroLiminarTOS);

        LiminarRecompraTO liminarRecompraTO_found = new LiminarRecompraTO();
        liminarRecompraTO_found.setCodigoTransacao("12345678901234567890");

        LiminarArquivo liminarArquivo = new LiminarArquivo();
        liminarArquivo.setCodigo(1);
        liminarArquivo.setNomeArquivo("arquivoTeste.txt");
        liminarArquivo.setEndereco("link-baixar-arquivo.site");

        LiminarArquivo liminarArquivo2 = new LiminarArquivo();
        liminarArquivo2.setCodigo(2);
        liminarArquivo2.setNomeArquivo("arquivoTeste2.txt");
        liminarArquivo2.setEndereco("link-baixar-arquivo2.site");

        Set<LiminarArquivo> liminarArquivos = new HashSet<>();
        liminarArquivos.add(liminarArquivo);
        liminarArquivos.add(liminarArquivo2);

        liminarRecompraTO_found.setLiminarArquivos(liminarArquivos);

        when(this.em.find(eq(LiminarRecompraTO.class), eq("12345678901234567890"))).thenReturn(liminarRecompraTO_found);
        when(this.em.find(eq(LiminarRecompraTO.class), eq("12345678901234567891"))).thenReturn(new LiminarRecompraTO());


        List<CadastroLiminar> cadastroLiminares = liminarBean.consultar("123", true);

        Assert.assertNotNull(cadastroLiminares);
        Assert.assertFalse(cadastroLiminares.isEmpty());
        Assert.assertEquals(2, cadastroLiminares.size());

        verify(em, times(1)).createNamedQuery(CadastroLiminarTO.QUERY_NAME);
        verify(em, times(2)).find(eq(LiminarRecompraTO.class), anyString());
        verifyNoMoreInteractions(logger);
    }

    private static List<CadastroLiminarTO> getCadastroLiminarTOS() {
        List<CadastroLiminarTO> cadastroLiminarTOS = new ArrayList<>();

        CadastroLiminarTO liminarTO = new CadastroLiminarTO();
        liminarTO.setCodigoTransacao("12345678901234567890");
        liminarTO.setTransacao("RCO");
        liminarTO.setTipoLiminar(22);
        liminarTO.setAbrangencia("N");
        liminarTO.setCodigoAbrangencia(5);

        CadastroLiminarTO liminarTO2 = new CadastroLiminarTO();
        liminarTO2.setCodigoTransacao("12345678901234567891");
        liminarTO2.setTransacao("RCO");
        liminarTO2.setTipoLiminar(22);
        liminarTO2.setAbrangencia("S");

        cadastroLiminarTOS.add(liminarTO);
        cadastroLiminarTOS.add(liminarTO2);
        return cadastroLiminarTOS;
    }
}


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
