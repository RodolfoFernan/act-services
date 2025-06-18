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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import javax.persistence.Query;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LiminarBeanTest extends BaseSIFESTest {

    private static final String SUCESSO = "sucesso";

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

    private final Gson gson = new GsonBuilder().create();

    @InjectMocks
    private LiminarBean liminarBean;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        liminarBean.setEm(this.em);
        liminarBean.setGson(gson);
        liminarBean.setModelMapper(modelMapper);
    }

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
    }

    @Test
    public void deveSalvarRecompraAgenteOperador() throws Exception {
        LiminarRecompra liminarRecompra = new LiminarRecompra();
        liminarRecompra.setCodigoTransacao("134");
        liminarRecompra.setCodigo(0L);
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


        Retorno retorno = liminarBean.salvarRecompraAgenteOperador(liminarRecompra, "usuario1", "ip", securityKeycloakUtils);

        Assert.assertEquals(Long.valueOf(0L), retorno.getCodigo());
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

        when(this.em.find(eq(LiminarRecompraTO.class), anyString())).thenReturn(liminarRecompraTO);

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

        Retorno retorno = liminarBean.salvarRecompraAgenteOperador(liminarRecompra, "usuario2", "ip", securityKeycloakUtils);

        Assert.assertEquals(Long.valueOf(0L), retorno.getCodigo());
    }

    private void mockConsultaParametroEmailLiminar() {
        ParametroEmailLiminarTO parametroEmailLiminarTO = new ParametroEmailLiminarTO();

        Query queryParamEmail = mock(Query.class);
        when(this.em.createNamedQuery(ParametroEmailLiminarTO.QUERY_BUSCAR_ULTIMA_CONFIG_VALIDA)).thenReturn(queryParamEmail);
        when(queryParamEmail.getResultList()).thenReturn(Collections.singletonList(parametroEmailLiminarTO));
    }

    @Test
    public void deveAtestarLiminar() throws Exception {
        LiminarRecompraTO liminarRecompraTO = new LiminarRecompraTO();
        liminarRecompraTO.setUsuario("usuario2");
        liminarRecompraTO.setCodigoTransacao("12345678901234567890");

        this.mockConsultaLiminarSemDetalhes();

        Query query2 = mock(Query.class);
        when(this.em.createNamedQuery(LiminarRecompraTO.QUERY_FIND_BY_CODIGO_TRANSACAO)).thenReturn(query2);
        when(query2.getSingleResult()).thenReturn(liminarRecompraTO);

        this.mockConsultaParametroEmailLiminar();

        when(modelMapper.map(any(), eq(CadastroLiminarHistorico.class))).thenReturn(new CadastroLiminarHistorico());

        Retorno retorno = liminarBean.atestarLiminar("123", "usuario", "ip"); // Linha 212

        Assert.assertEquals(Long.valueOf(0L), retorno.getCodigo());
    }

    @Test
    public void deveConsultarLiminar() throws Exception {

        this.mockConsultaLiminarSemDetalhes();

        List<CadastroLiminar> cadastroLiminares = liminarBean.consultar("1234", false);

        Assert.assertNotNull(cadastroLiminares);
        Assert.assertFalse(cadastroLiminares.isEmpty());
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

        LiminarRecompraTO liminarRecompraTO = new LiminarRecompraTO();
        liminarRecompraTO.setCodigoTransacao("1234567");

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

        liminarRecompraTO.setLiminarArquivos(liminarArquivos);

        when(this.em.find(LiminarRecompraTO.class, "12345678901234567890")).thenReturn(liminarRecompraTO);
        when(this.em.find(LiminarRecompraTO.class, "12345678901234567891")).thenReturn(new LiminarRecompraTO());

        //when(liminarBean.montaEPreencheDocumentosLiminar(liminarArquivos)).thenReturn(Arrays.asList(new Documento(), new Documento()));

        List<CadastroLiminar> cadastroLiminares = liminarBean.consultar("123", true); // linha 267

        Assert.assertNotNull(cadastroLiminares);
        Assert.assertFalse(cadastroLiminares.isEmpty());
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
