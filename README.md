<h1>Controle de Lançamentos e Consolidação Diária - Microsserviços</h1>


![image](https://github.com/user-attachments/assets/00b4d0be-f17a-4f24-95e8-cd6882136e5a)

<p>Este repositório contém a implementação dos serviços de Controle de Lançamentos e Consolidação Diária, seguindo uma arquitetura de microsserviços com tecnologias como Spring Boot, Kafka, Redis, PostgreSQL,MySql,Kibana, Docker,Kubernets.A aplicação é dividida em modulos separados  como :api-gateway,lancamentos-service,redis-cache,kafka-service,monitoring. Cada serviço tem uma função específica, e utilizamos diversas ferramentas para garantir alta disponibilidade, escalabilidade e desempenho.</p>

<h2>1. Estrutura dos Microsserviços</h2>
<p>A seguir, a estrutura de diretórios e as funcionalidades principais de cada serviço.</p>
Perfeito! Com base nas informações que você forneceu, aqui está um resumo da motivação e das circunstâncias de criação das Stored Procedures (SPs) mencionadas, bem como o impacto delas no fluxo Javaweb e na rotina Java batch FES.REPASSE:
Excelente! Vamos detalhar a FES.FESSPU20_VINCULA_LIBERACAO no formato solicitado:
FES.FESSPU20_VINCULA_LIBERACAO

    Objetivo: Vincular as liberações de contrato a seus respectivos aditamentos ou contratos iniciais, marcando o tipo de transação e a participação do candidato. Além disso, a SP insere retenções para liberações que não conseguem ser vinculadas.

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail; // Para testar exceções

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException; // Importar para testar Liminar não encontrada
import javax.persistence.NonUniqueResultException; // Considerar caso de retorno múltiplo
import javax.persistence.Query;

import br.gov.fnde.siem.util.MensagemUtil; // Assumindo este pacote para MensagemUtil
import br.gov.fnde.siem.util.Constantes; // Assumindo este pacote para Constantes
// Importe suas classes TO, Beans e Exceptions
import br.gov.fnde.siem.domain.LiminarRecompraTO;
import br.gov.fnde.siem.domain.ParametroEmailLiminarTO;
import br.gov.fnde.siem.domain.CadastroLiminarHistorico;
import br.gov.fnde.siem.bean.AuditoriaBean; // Assumindo o nome do Bean
import br.gov.fnde.siem.bean.Retorno; // Assumindo o nome da classe Retorno
import br.gov.fnde.siem.exception.FESException; // Assumindo o nome da sua exceção
// Assumindo a classe que contém atestarLiminar é LiminarBean
import br.gov.fnde.siem.bean.LiminarBean; // Ou o nome real da sua classe Bean

import org.modelmapper.ModelMapper; // Se você usa ModelMapper

import java.util.logging.Level;
import java.util.logging.Logger;


@RunWith(MockitoJUnitRunner.class)
public class LiminarBeanAtestarLiminarTest {

    @InjectMocks
    @Spy // Use @Spy para métodos da própria classe serem chamados de verdade se não mockados
    private LiminarBean liminarBean = new LiminarBean(); // Instancie o Bean

    @Mock
    private EntityManager em;

    @Mock
    private AuditoriaBean auditoriaBean; // Mock do AuditoriaBean

    @Mock
    private ModelMapper modelMapper; // Mock do ModelMapper, se usado

    @Mock
    private Logger logger; // Mock do logger para verificar logs de erro

    // Constantes do método, se existirem na sua classe real
    private static final String CODIGO_TRANSACAO = "codigoProcesso"; // ou o nome real da constante
    private static final String TABELA_LIMINAR = "FESTB_LIMINAR"; // Exemplo, use a constante real


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); // Inicializa os mocks
        // É uma boa prática definir o logger para evitar NullPointerException se ele for chamado
        // Mas para verificar logs de erro, ele precisa ser um mock
        // Se LiminarBean tiver um logger estático, você terá que usar PowerMockito ou métodos estáticos do Mockito
        // ou injetar o logger de alguma forma. Para este exemplo, vou assumir que ele pode ser mockado.
        // Se o logger for estático, esta linha abaixo pode não ser necessária, mas o teste para erro de log não funcionaria.
        // Mockito.doNothing().when(logger).log(any(Level.class), anyString(), any(Throwable.class));
    }

    // --- Métodos Auxiliares para Mocks Comuns ---
    private LiminarRecompraTO criarLiminarRecompraTO(String codigoTransacao, String usuario) {
        LiminarRecompraTO liminarRecompraTO = new LiminarRecompraTO();
        liminarRecompraTO.setUsuario(usuario);
        liminarRecompraTO.setCodigoTransacao(codigoTransacao);
        // Adicione outros campos relevantes que o método atestarLiminar possa usar
        return liminarRecompraTO;
    }

    private ParametroEmailLiminarTO criarParametroEmailLiminarTO() {
        ParametroEmailLiminarTO parametroEmailLiminarTO = new ParametroEmailLiminarTO();
        // Configure parâmetros de e-mail necessários para o teste
        return parametroEmailLiminarTO;
    }

    private void mockBuscaLiminar(LiminarRecompraTO liminar) {
        Query query = mock(Query.class);
        when(em.createNamedQuery(LiminarRecompraTO.QUERY_FIND_BY_CODIGO_TRANSACAO)).thenReturn(query);
        when(query.setParameter(eq(CODIGO_TRANSACAO), anyString())).thenReturn(query); // Mock do setParameter
        when(query.getSingleResult()).thenReturn(liminar);
    }

    private void mockRetornaUltimaConfiguracaoEmailValida(ParametroEmailLiminarTO parametro) throws FESException {
        // Usa doReturn().when() porque retornaUltimaConfiguracaoEmailValida é um método do @Spy (liminarBean)
        doReturn(parametro).when(liminarBean).retornaUltimaConfiguracaoEmailValida();
    }

    private void mockVerificaSeUsuarioAtualPodeAtestarLiminar(boolean podeAtestar) throws FESException {
        if (podeAtestar) {
            doNothing().when(liminarBean).verificaSeUsuarioAtualPodeAtestarLiminar(any(LiminarRecompraTO.class), anyString());
        } else {
            doThrow(new FESException("Usuário sem permissão para atestar liminar."))
                    .when(liminarBean).verificaSeUsuarioAtualPodeAtestarLiminar(any(LiminarRecompraTO.class), anyString());
        }
    }

    private void mockAtestarLiminarEgravarHistorico() throws Exception {
        doNothing().when(liminarBean).atestarLiminarEgravarHistorico(any(LiminarRecompraTO.class), anyString(), anyString());
        // Se atestarLiminarEgravarHistorico usar ModelMapper, você precisa mockar a conversão
        when(modelMapper.map(any(), eq(CadastroLiminarHistorico.class))).thenReturn(new CadastroLiminarHistorico());
    }

    private void mockEnviarEmailNotificacaoLiminar() throws Exception {
        doNothing().when(liminarBean).enviarEmailNotificacaoLiminar(anyString(), anyString(), anyString(), any(ParametroEmailLiminarTO.class));
    }


    // --- Cenários de Teste ---

    @Test
    public void deveAtestarLiminarComSucesso() throws Exception {
        // Cenário: Liminar encontrada, usuário com permissão, todas as dependências funcionam
        String codigoProcesso = "123";
        String usuario = "usuarioTeste";
        String ip = "192.168.1.1";
        LiminarRecompraTO liminarEsperada = criarLiminarRecompraTO("12345678901234567890", "outroUsuario");
        ParametroEmailLiminarTO paramEmail = criarParametroEmailLiminarTO();

        // Mocks das dependências
        mockBuscaLiminar(liminarEsperada);
        mockRetornaUltimaConfiguracaoEmailValida(paramEmail);
        mockVerificaSeUsuarioAtualPodeAtestarLiminar(true); // Usuário tem permissão
        mockAtestarLiminarEgravarHistorico();
        mockEnviarEmailNotificacaoLiminar();
        doNothing().when(auditoriaBean).gravaAuditoria(anyString(), anyString(), anyString(), any()); // Mock da auditoria

        // Executa o método
        Retorno retorno = liminarBean.atestarLiminar(codigoProcesso, usuario, ip);

        // Verifica as asserções
        assertEquals(Long.valueOf(0L), retorno.getCodigo());
        assertEquals("Liminar Atestada com sucesso.", retorno.getMensagem());
        assertEquals(MensagemUtil.TipoMensagem.SUCESSO, retorno.getTipoMensagem());

        // Verifica se os métodos foram chamados corretamente
        verify(liminarBean, times(1)).retornaUltimaConfiguracaoEmailValida();
        verify(liminarBean, times(1)).verificaSeUsuarioAtualPodeAtestarLiminar(liminarEsperada, usuario);
        verify(liminarBean, times(1)).atestarLiminarEgravarHistorico(liminarEsperada, usuario, ip);
        verify(liminarBean, times(1)).enviarEmailNotificacaoLiminar(liminarEsperada.getCodigoTransacao(), usuario, "ATESTAR", paramEmail);
        verify(auditoriaBean, times(1)).gravaAuditoria(usuario, TABELA_LIMINAR, Constantes.ALTERACAO, liminarEsperada);
    }

    @Test
    public void deveLancarFESExceptionQuandoLiminarNaoEncontrada() throws Exception {
        // Cenário: getSingleResult() retorna null ou joga NoResultException
        String codigoProcesso = "liminarNaoExiste";
        String usuario = "usuarioTeste";
        String ip = "192.168.1.1";

        // Mocks da busca da liminar para não retornar nada
        Query query = mock(Query.class);
        when(em.createNamedQuery(LiminarRecompraTO.QUERY_FIND_BY_CODIGO_TRANSACAO)).thenReturn(query);
        when(query.setParameter(eq(CODIGO_TRANSACAO), anyString())).thenReturn(query);
        // Cenário 1: getSingleResult retorna null
        when(query.getSingleResult()).thenReturn(null);

        try {
            liminarBean.atestarLiminar(codigoProcesso, usuario, ip);
            fail("Esperava uma FESException para liminar não encontrada.");
        } catch (FESException e) {
            assertEquals("Liminar não encontrada!", e.getMessage());
        }

        // Cenário 2: getSingleResult joga NoResultException (comum em JPA/Hibernate quando não encontra)
        // Resetar o mock para o segundo cenário de teste
        reset(query);
        when(em.createNamedQuery(LiminarRecompraTO.QUERY_FIND_BY_CODIGO_TRANSACAO)).thenReturn(query);
        when(query.setParameter(eq(CODIGO_TRANSACAO), anyString())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException("No result for query"));

        try {
            liminarBean.atestarLiminar(codigoProcesso, usuario, ip);
            fail("Esperava uma FESException para liminar não encontrada (NoResultException).");
        } catch (FESException e) {
            assertEquals("Liminar não encontrada!", e.getMessage());
        }

        // Verifica que nenhum outro método foi chamado após o erro de busca da liminar
        verify(liminarBean, never()).retornaUltimaConfiguracaoEmailValida();
        verify(liminarBean, never()).verificaSeUsuarioAtualPodeAtestarLiminar(any(LiminarRecompraTO.class), anyString());
        verify(liminarBean, never()).atestarLiminarEgravarHistorico(any(LiminarRecompraTO.class), anyString(), anyString());
        verify(liminarBean, never()).enviarEmailNotificacaoLiminar(anyString(), anyString(), anyString(), any(ParametroEmailLiminarTO.class));
        verify(auditoriaBean, never()).gravaAuditoria(anyString(), anyString(), anyString(), any());
    }

    @Test
    public void deveLancarFESExceptionQuandoUsuarioNaoPodeAtestar() throws Exception {
        // Cenário: Liminar encontrada, mas usuário sem permissão para atestar
        String codigoProcesso = "123";
        String usuario = "usuarioSemPermissao";
        String ip = "192.168.1.1";
        LiminarRecompraTO liminarEsperada = criarLiminarRecompraTO("12345678901234567890", "outroUsuario");
        ParametroEmailLiminarTO paramEmail = criarParametroEmailLiminarTO();

        // Mocks
        mockBuscaLiminar(liminarEsperada);
        mockRetornaUltimaConfiguracaoEmailValida(paramEmail);
        mockVerificaSeUsuarioAtualPodeAtestarLiminar(false); // Usuário SEM permissão (lançará FESException)

        try {
            liminarBean.atestarLiminar(codigoProcesso, usuario, ip);
            fail("Esperava uma FESException devido à falta de permissão do usuário.");
        } catch (FESException e) {
            assertEquals("Usuário sem permissão para atestar liminar.", e.getMessage());
        }

        // Verifica que os métodos seguintes não foram chamados
        verify(liminarBean, times(1)).retornaUltimaConfiguracaoEmailValida(); // Este é chamado antes da verificação
        verify(liminarBean, times(1)).verificaSeUsuarioAtualPodeAtestarLiminar(liminarEsperada, usuario);
        verify(liminarBean, never()).atestarLiminarEgravarHistorico(any(LiminarRecompraTO.class), anyString(), anyString());
        verify(liminarBean, never()).enviarEmailNotificacaoLiminar(anyString(), anyString(), anyString(), any(ParametroEmailLiminarTO.class));
        verify(auditoriaBean, never()).gravaAuditoria(anyString(), anyString(), anyString(), any());
    }

    @Test
    public void deveLancarFESExceptionQuandoFalhaAoGravarHistorico() throws Exception {
        // Cenário: Falha durante o atestado da liminar ou gravação do histórico
        String codigoProcesso = "123";
        String usuario = "usuarioTeste";
        String ip = "192.168.1.1";
        LiminarRecompraTO liminarEsperada = criarLiminarRecompraTO("12345678901234567890", "outroUsuario");
        ParametroEmailLiminarTO paramEmail = criarParametroEmailLiminarTO();

        // Mocks
        mockBuscaLiminar(liminarEsperada);
        mockRetornaUltimaConfiguracaoEmailValida(paramEmail);
        mockVerificaSeUsuarioAtualPodeAtestarLiminar(true);
        // Simula uma exceção no método atestarLiminarEgravarHistorico
        doThrow(new RuntimeException("Falha na persistência do histórico"))
                .when(liminarBean).atestarLiminarEgravarHistorico(any(LiminarRecompraTO.class), anyString(), anyString());

        try {
            liminarBean.atestarLiminar(codigoProcesso, usuario, ip);
            fail("Esperava uma FESException devido à falha na gravação do histórico.");
        } catch (FESException e) {
            assertEquals("Falha na persistência do histórico", e.getMessage());
        }

        // Verifica que métodos chamados até o ponto do erro foram de fato chamados
        verify(liminarBean, times(1)).retornaUltimaConfiguracaoEmailValida();
        verify(liminarBean, times(1)).verificaSeUsuarioAtualPodeAtestarLiminar(liminarEsperada, usuario);
        verify(liminarBean, times(1)).atestarLiminarEgravarHistorico(liminarEsperada, usuario, ip);
        // Verifica que os métodos seguintes não foram chamados
        verify(liminarBean, never()).enviarEmailNotificacaoLiminar(anyString(), anyString(), anyString(), any(ParametroEmailLiminarTO.class));
        verify(auditoriaBean, never()).gravaAuditoria(anyString(), anyString(), anyString(), any());
        verify(logger, times(1)).log(eq(Level.SEVERE), anyString(), any(Throwable.class));
    }

    @Test
    public void deveLancarFESExceptionQuandoFalhaAoEnviarEmail() throws Exception {
        // Cenário: Tudo ok até o envio de e-mail, que falha
        String codigoProcesso = "123";
        String usuario = "usuarioTeste";
        String ip = "192.168.1.1";
        LiminarRecompraTO liminarEsperada = criarLiminarRecompraTO("12345678901234567890", "outroUsuario");
        ParametroEmailLiminarTO paramEmail = criarParametroEmailLiminarTO();

        // Mocks
        mockBuscaLiminar(liminarEsperada);
        mockRetornaUltimaConfiguracaoEmailValida(paramEmail);
        mockVerificaSeUsuarioAtualPodeAtestarLiminar(true);
        mockAtestarLiminarEgravarHistorico();
        // Simula uma exceção no método enviarEmailNotificacaoLiminar
        doThrow(new RuntimeException("Falha no envio do e-mail"))
                .when(liminarBean).enviarEmailNotificacaoLiminar(anyString(), anyString(), anyString(), any(ParametroEmailLiminarTO.class));

        try {
            liminarBean.atestarLiminar(codigoProcesso, usuario, ip);
            fail("Esperava uma FESException devido à falha no envio de e-mail.");
        } catch (FESException e) {
            assertEquals("Falha no envio do e-mail", e.getMessage());
        }

        // Verifica que os métodos chamados até o ponto do erro foram de fato chamados
        verify(liminarBean, times(1)).retornaUltimaConfiguracaoEmailValida();
        verify(liminarBean, times(1)).verificaSeUsuarioAtualPodeAtestarLiminar(liminarEsperada, usuario);
        verify(liminarBean, times(1)).atestarLiminarEgravarHistorico(liminarEsperada, usuario, ip);
        verify(liminarBean, times(1)).enviarEmailNotificacaoLiminar(liminarEsperada.getCodigoTransacao(), usuario, "ATESTAR", paramEmail);
        // Verifica que o método seguinte não foi chamado
        verify(auditoriaBean, never()).gravaAuditoria(anyString(), anyString(), anyString(), any());
        verify(logger, times(1)).log(eq(Level.SEVERE), anyString(), any(Throwable.class));
    }

    @Test
    public void deveLancarFESExceptionQuandoFalhaAoGravarAuditoria() throws Exception {
        // Cenário: Tudo ok até a gravação da auditoria, que falha
        String codigoProcesso = "123";
        String usuario = "usuarioTeste";
        String ip = "192.168.1.1";
        LiminarRecompraTO liminarEsperada = criarLiminarRecompraTO("12345678901234567890", "outroUsuario");
        ParametroEmailLiminarTO paramEmail = criarParametroEmailLiminarTO();

        // Mocks
        mockBuscaLiminar(liminarEsperada);
        mockRetornaUltimaConfiguracaoEmailValida(paramEmail);
        mockVerificaSeUsuarioAtualPodeAtestarLiminar(true);
        mockAtestarLiminarEgravarHistorico();
        mockEnviarEmailNotificacaoLiminar();
        // Simula uma exceção no método gravaAuditoria
        doThrow(new RuntimeException("Falha na gravação da auditoria"))
                .when(auditoriaBean).gravaAuditoria(anyString(), anyString(), anyString(), any());

        try {
            liminarBean.atestarLiminar(codigoProcesso, usuario, ip);
            fail("Esperava uma FESException devido à falha na gravação da auditoria.");
        } catch (FESException e) {
            assertEquals("Falha na gravação da auditoria", e.getMessage());
        }

        // Verifica que todos os métodos anteriores foram chamados
        verify(liminarBean, times(1)).retornaUltimaConfiguracaoEmailValida();
        verify(liminarBean, times(1)).verificaSeUsuarioAtualPodeAtestarLiminar(liminarEsperada, usuario);
        verify(liminarBean, times(1)).atestarLiminarEgravarHistorico(liminarEsperada, usuario, ip);
        verify(liminarBean, times(1)).enviarEmailNotificacaoLiminar(liminarEsperada.getCodigoTransacao(), usuario, "ATESTAR", paramEmail);
        verify(auditoriaBean, times(1)).gravaAuditoria(usuario, TABELA_LIMINAR, Constantes.ALTERACAO, liminarEsperada);
        verify(logger, times(1)).log(eq(Level.SEVERE), anyString(), any(Throwable.class));
    }

    @Test
    public void deveLancarFESExceptionParaExcecoesGeraisNaoMapeadas() throws Exception {
        // Cenário: Uma RuntimeException inesperada ocorre em algum ponto antes das chamadas mockadas.
        // Por exemplo, na criação da query ou no setParameter, se não fossem mockados.
        String codigoProcesso = "123";
        String usuario = "usuarioTeste";
        String ip = "192.168.1.1";

        // Simula uma RuntimeException na criação da named query
        when(em.createNamedQuery(LiminarRecompraTO.QUERY_FIND_BY_CODIGO_TRANSACAO)).thenThrow(new RuntimeException("Erro inesperado no DB."));

        try {
            liminarBean.atestarLiminar(codigoProcesso, usuario, ip);
            fail("Esperava uma FESException para erro geral inesperado.");
        } catch (FESException e) {
            assertEquals("Erro inesperado no DB.", e.getMessage());
        }

        // Verifica que o logger registrou o erro
        verify(logger, times(1)).log(eq(Level.SEVERE), anyString(), any(Throwable.class));
        // Verifica que nenhum dos métodos de lógica de negócio foi chamado
        verify(liminarBean, never()).retornaUltimaConfiguracaoEmailValida();
        verify(liminarBean, never()).verificaSeUsuarioAtualPodeAtestarLiminar(any(LiminarRecompraTO.class), anyString());
        verify(liminarBean, never()).atestarLiminarEgravarHistorico(any(LiminarRecompraTO.class), anyString(), anyString());
        verify(liminarBean, never()).enviarEmailNotificacaoLiminar(anyString(), anyString(), anyString(), any(ParametroEmailLiminarTO.class));
        verify(auditoriaBean, never()).gravaAuditoria(anyString(), anyString(), anyString(), any());
    }

    @Test
    public void deveLancarFESExceptionQuandoMultiplosResultadosParaLiminar() throws Exception {
        // Cenário: getSingleResult() joga NonUniqueResultException
        String codigoProcesso = "duplicado";
        String usuario = "usuarioTeste";
        String ip = "192.168.1.1";

        Query query = mock(Query.class);
        when(em.createNamedQuery(LiminarRecompraTO.QUERY_FIND_BY_CODIGO_TRANSACAO)).thenReturn(query);
        when(query.setParameter(eq(CODIGO_TRANSACAO), anyString())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NonUniqueResultException("Mais de uma liminar encontrada!"));

        try {
            liminarBean.atestarLiminar(codigoProcesso, usuario, ip);
            fail("Esperava uma FESException para múltiplos resultados.");
        } catch (FESException e) {
            // A mensagem de erro será a da NonUniqueResultException
            assertEquals("Mais de uma liminar encontrada!", e.getMessage());
        }

        verify(logger, times(1)).log(eq(Level.SEVERE), anyString(), any(Throwable.class));
        verify(liminarBean, never()).retornaUltimaConfiguracaoEmailValida();
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
