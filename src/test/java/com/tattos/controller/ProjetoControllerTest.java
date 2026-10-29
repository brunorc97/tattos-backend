package com.tattos.controller;

import com.tattos.model.Projeto;
import com.tattos.repository.ProjetoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProjetoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoRepository projetoRepository;

    @BeforeEach
    void setUp() {
        projetoRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/projetos deve retornar 200 com lista")
    @WithMockUser(roles = "GESTOR")
    void listar() throws Exception {
        projetoRepository.save(Projeto.builder().nome("Projeto Capa de Costas").build());

        mockMvc.perform(get("/api/projetos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Projeto Capa de Costas"));
    }

    @Test
    @DisplayName("POST /api/projetos válido deve retornar 201")
    @WithMockUser(roles = "GESTOR")
    void criarValido() throws Exception {
        var c = novoCliente("proj.oriental@tattos.com");
        mockMvc.perform(post("/api/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Projeto Oriental\", \"descricao\": \"Manga inteira\", \"clienteId\": " + c.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Projeto Oriental"));
    }

    @Test
    @DisplayName("POST /{id}/aprovar deve marcar o projeto como aprovado")
    @WithMockUser(roles = "GESTOR")
    void aprovarProjeto() throws Exception {
        Projeto projeto = projetoRepository.save(Projeto.builder().nome("Projeto X").build());

        mockMvc.perform(post("/api/projetos/" + projeto.getId() + "/aprovar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprovado").value(true));

        assertThat(projetoRepository.findById(projeto.getId())).get()
                .extracting(Projeto::isAprovado).isEqualTo(true);
    }

    @Test
    @DisplayName("POST /{id}/aprovar com id inexistente deve retornar 404")
    @WithMockUser(roles = "GESTOR")
    void aprovarProjetoInexistente() throws Exception {
        mockMvc.perform(post("/api/projetos/99999/aprovar"))
                .andExpect(status().isNotFound());
    }

    // ─── Vínculo e permissões (Task 3) ──────────────────────────────────────

    @Autowired
    private com.tattos.repository.UsuarioRepository usuarioRepository;

    @Autowired
    private com.tattos.security.JwtTokenProvider jwtTokenProvider;

    private com.tattos.model.Cliente novoCliente(String email) {
        return (com.tattos.model.Cliente) usuarioRepository.save(com.tattos.model.Cliente.builder()
                .nome("Cli " + email).email(email).senha("h").ativo(true).build());
    }

    private com.tattos.model.Tatuador novoTatuador(String email) {
        return (com.tattos.model.Tatuador) usuarioRepository.save(com.tattos.model.Tatuador.builder()
                .nome("Tat " + email).email(email).senha("h")
                .comissaoPercentual(new java.math.BigDecimal("60")).ativo(true).build());
    }

    @Test
    @DisplayName("POST /projetos sem clienteId retorna 400")
    @WithMockUser(roles = "GESTOR")
    void criarSemCliente() throws Exception {
        mockMvc.perform(post("/api/v1/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Sem cliente\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /projetos com clienteId persiste vínculo")
    @WithMockUser(roles = "GESTOR")
    void criarComCliente() throws Exception {
        var c = novoCliente("proj.cli1@tattos.com");

        mockMvc.perform(post("/api/v1/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Com cliente\", \"clienteId\": " + c.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(c.getId()));
    }

    @Test
    @DisplayName("Tatuador lista apenas os próprios projetos")
    void tatuadorVeApenasProprios() throws Exception {
        var c = novoCliente("proj.cli2@tattos.com");
        var tatA = novoTatuador("proj.tata@tattos.com");
        var tatB = novoTatuador("proj.tatb@tattos.com");

        projetoRepository.save(Projeto.builder().nome("Do A").cliente(c).tatuador(tatA).build());
        projetoRepository.save(Projeto.builder().nome("Do B").cliente(c).tatuador(tatB).build());

        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(tatA);
        mockMvc.perform(get("/api/v1/projetos").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Do A"));
    }

    @Test
    @DisplayName("Gestor filtra por statusAprovacao=NAO_APROVADO")
    @WithMockUser(roles = "GESTOR")
    void gestorFiltraStatus() throws Exception {
        var c = novoCliente("proj.cli3@tattos.com");
        Projeto rec = projetoRepository.save(Projeto.builder().nome("Recusado").cliente(c).build());
        rec.setStatusAprovacao(com.tattos.model.enums.StatusAprovacaoProjeto.NAO_APROVADO);
        projetoRepository.save(rec);
        projetoRepository.save(Projeto.builder().nome("Pendente").cliente(c).build());

        mockMvc.perform(get("/api/v1/projetos").param("statusAprovacao", "NAO_APROVADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Recusado"));
    }

    // ─── Sinal com comprovante (Task 4) ─────────────────────────────────────

    @Autowired
    private com.tattos.service.FileStorageService fileStorageService;

    @Autowired
    private com.tattos.repository.ArquivoRepository arquivoRepository;

    private Long uploadPdfComo(com.tattos.model.Usuario dono) {
        org.springframework.mock.web.MockMultipartFile pdf = new org.springframework.mock.web.MockMultipartFile(
                "file", "comprovante.pdf", "application/pdf", "%PDF-x".getBytes());
        return fileStorageService.salvar(pdf, dono).getId();
    }

    @Test
    @DisplayName("Cliente dono anexa comprovante PDF ao sinal do projeto")
    void clienteAnexaComprovante() throws Exception {
        var c = novoCliente("proj.sinal@tattos.com");
        Projeto p = projetoRepository.save(Projeto.builder()
                .nome("Com sinal").cliente(c).necessitaSinal(true)
                .valorSinal(new java.math.BigDecimal("200.00")).build());
        Long arquivoId = uploadPdfComo(c);

        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(c);
        mockMvc.perform(post("/api/v1/projetos/" + p.getId() + "/sinal/comprovante")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"arquivoId\": " + arquivoId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comprovanteSinalId").value(arquivoId));
    }

    @Test
    @DisplayName("Cliente de outro projeto não anexa comprovante (403)")
    void clienteAlheioNaoAnexa() throws Exception {
        var c = novoCliente("proj.sinal2@tattos.com");
        var outro = novoCliente("proj.outro@tattos.com");
        Projeto p = projetoRepository.save(Projeto.builder().nome("Do outro").cliente(outro).build());
        Long arquivoId = uploadPdfComo(c);

        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(c);
        mockMvc.perform(post("/api/v1/projetos/" + p.getId() + "/sinal/comprovante")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"arquivoId\": " + arquivoId + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Gestor confirma sinal com forma de pagamento")
    @WithMockUser(roles = "GESTOR")
    void gestorConfirmaSinal() throws Exception {
        var c = novoCliente("proj.sinal3@tattos.com");
        Projeto p = projetoRepository.save(Projeto.builder()
                .nome("Confirmar sinal").cliente(c).necessitaSinal(true)
                .valorSinal(new java.math.BigDecimal("150.00")).build());

        mockMvc.perform(post("/api/v1/projetos/" + p.getId() + "/sinal/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"formaPagamento\": \"PIX\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sinalPago").value(true))
                .andExpect(jsonPath("$.formaPagamentoSinal").value("PIX"));
    }

    // ─── Decisão com motivo (Task 2) ─────────────────────────────────────────

    @Test
    @DisplayName("POST /{id}/decisao aprovando marca APROVADO")
    @WithMockUser(roles = "GESTOR")
    void decisaoAprovar() throws Exception {
        Projeto p = projetoRepository.save(Projeto.builder().nome("Proj Decisao").build());

        mockMvc.perform(post("/api/v1/projetos/" + p.getId() + "/decisao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusAprovacao").value("APROVADO"));
    }

    @Test
    @DisplayName("POST /{id}/decisao recusando exige motivo e persiste NAO_APROVADO")
    @WithMockUser(roles = "GESTOR")
    void decisaoRecusar() throws Exception {
        Projeto p = projetoRepository.save(Projeto.builder().nome("Proj Recusa").build());

        // sem motivo → 400
        mockMvc.perform(post("/api/v1/projetos/" + p.getId() + "/decisao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": false}"))
                .andExpect(status().isBadRequest());

        // com motivo → 200
        mockMvc.perform(post("/api/v1/projetos/" + p.getId() + "/decisao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": false, \"motivoRecusa\": \"VALOR\", \"detalhe\": \"Acima do orçamento\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusAprovacao").value("NAO_APROVADO"))
                .andExpect(jsonPath("$.motivoRecusa").value("VALOR"));

        assertThat(projetoRepository.findById(p.getId())).get()
                .extracting(Projeto::getMotivoRecusaDetalhe).isEqualTo("Acima do orçamento");
    }

    @Test
    @DisplayName("POST /{id}/decisao em projeto inexistente retorna 404")
    @WithMockUser(roles = "GESTOR")
    void decisaoInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/projetos/99999/decisao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": true}"))
                .andExpect(status().isNotFound());
    }
}
