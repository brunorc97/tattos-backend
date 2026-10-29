package com.tattos.controller;

import com.tattos.model.Agendamento;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.UsuarioRepository;
import com.tattos.security.JwtTokenProvider;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClienteCrmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Cliente cliente;
    private Tatuador tatuador;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli CRM").email("crm.c@tattos.com").senha("h").ativo(true).build());
        tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat CRM").email("crm.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
    }

    private String bearer(com.tattos.model.Usuario u) {
        return "Bearer " + jwtTokenProvider.generateTokenForUser(u);
    }

    @Test
    @DisplayName("Gestor registra contato e lista ordenado por data desc")
    @WithMockUser(roles = "GESTOR")
    void gestorRegistraContato() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/" + cliente.getId() + "/contatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\": \"WHATSAPP\", \"resumo\": \"Confirmou interesse no orçamento\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("WHATSAPP"));

        mockMvc.perform(get("/api/v1/clientes/" + cliente.getId() + "/contatos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].resumo").value("Confirmou interesse no orçamento"));
    }

    @Test
    @DisplayName("Tatuador sem vínculo com o cliente não registra contato (403)")
    void tatuadorSemVinculo() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/" + cliente.getId() + "/contatos")
                        .header("Authorization", bearer(tatuador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\": \"EMAIL\", \"resumo\": \"Tentativa\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tatuador com vínculo registra contato")
    void tatuadorComVinculo() throws Exception {
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatuador).cabine("C1")
                .dataHora(LocalDateTime.now().plusDays(1)).valor(new BigDecimal("400")).build());

        mockMvc.perform(post("/api/v1/clientes/" + cliente.getId() + "/contatos")
                        .header("Authorization", bearer(tatuador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\": \"PRESENCIAL\", \"resumo\": \"Conversou após sessão\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Cliente inexistente retorna 404")
    @WithMockUser(roles = "GESTOR")
    void clienteInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/99999/contatos"))
                .andExpect(status().isNotFound());
    }

    // ─── Perfil 360° (Task 3) ───────────────────────────────────────────────

    @Autowired
    private com.tattos.repository.ProjetoRepository projetoRepository;

    @Autowired
    private com.tattos.repository.FichaAnamneseRepository fichaRepository;

    @Test
    @DisplayName("Gestor obtém perfil 360° agregado do cliente")
    @WithMockUser(roles = "GESTOR")
    void perfil360() throws Exception {
        // agendamento realizado (trabalho)
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatuador).cabine("C1")
                .dataHora(LocalDateTime.now().minusDays(10)).valor(new BigDecimal("800"))
                .status(com.tattos.model.enums.StatusAgendamento.REALIZADO).build());
        // projeto recusado
        var p = projetoRepository.save(com.tattos.model.Projeto.builder()
                .nome("Projeto recusado").cliente(cliente).build());
        p.setStatusAprovacao(com.tattos.model.enums.StatusAprovacaoProjeto.NAO_APROVADO);
        p.setMotivoRecusa(com.tattos.model.enums.MotivoRecusaProjeto.VALOR);
        projetoRepository.save(p);
        // ficha com alergia
        fichaRepository.save(com.tattos.model.FichaAnamnese.builder()
                .cliente(cliente).alergias("Níquel").historicoSaude("ok").termoAssinado(true).build());

        mockMvc.perform(get("/api/v1/clientes/" + cliente.getId() + "/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.id").value(cliente.getId()))
                .andExpect(jsonPath("$.totalTrabalhos").value(1))
                .andExpect(jsonPath("$.projetos.total").value(1))
                .andExpect(jsonPath("$.projetos.naoAprovados[0].motivoRecusa").value("VALOR"))
                .andExpect(jsonPath("$.alergias").value("Níquel"));
    }

    @Test
    @DisplayName("Tatuador sem vínculo não acessa perfil (403)")
    void perfilSemVinculo() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/" + cliente.getId() + "/perfil")
                        .header("Authorization", bearer(tatuador)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Perfil de cliente inexistente retorna 404")
    @WithMockUser(roles = "GESTOR")
    void perfilInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/99999/perfil"))
                .andExpect(status().isNotFound());
    }

    // ─── Trabalhos com foto (Task 4) ────────────────────────────────────────

    @Autowired
    private com.tattos.service.FileStorageService fileStorageService;

    @Test
    @DisplayName("Gestor registra trabalho com foto e lista no cliente")
    @WithMockUser(roles = "GESTOR")
    void registraTrabalhoComFoto() throws Exception {
        org.springframework.mock.web.MockMultipartFile foto = new org.springframework.mock.web.MockMultipartFile(
                "file", "trabalho.png", "image/png", new byte[]{1, 2, 3});
        Long imagemId = fileStorageService.salvar(foto, null).getId();

        mockMvc.perform(post("/api/v1/clientes/" + cliente.getId() + "/trabalhos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"Fechamento braço\", \"tatuadorId\": " + tatuador.getId()
                                + ", \"dataTrabalho\": \"2026-10-20\", \"imagemId\": " + imagemId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        mockMvc.perform(get("/api/v1/clientes/" + cliente.getId() + "/trabalhos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Fechamento braço"))
                .andExpect(jsonPath("$[0].imagemId").value(imagemId));
    }

    @Test
    @DisplayName("Trabalho com tatuador inexistente retorna 404")
    @WithMockUser(roles = "GESTOR")
    void trabalhoTatuadorInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/" + cliente.getId() + "/trabalhos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tatuadorId\": 99999, \"dataTrabalho\": \"2026-10-20\"}"))
                .andExpect(status().isNotFound());
    }

    // ─── Fixes do review ────────────────────────────────────────────────────

    @Test
    @DisplayName("Listar contatos retorna DTO sem expor entidade (sem senha do autor)")
    @WithMockUser(roles = "GESTOR")
    void contatosNaoExpoemEntidade() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/" + cliente.getId() + "/contatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\": \"EMAIL\", \"resumo\": \"Teste de exposição\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/clientes/" + cliente.getId() + "/contatos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tipo").value("EMAIL"))
                .andExpect(jsonPath("$.content[0].senha").doesNotExist())
                .andExpect(jsonPath("$.content[0].cliente").doesNotExist())
                .andExpect(jsonPath("$.content[0].autor").doesNotExist());
    }

    @Test
    @DisplayName("Trabalho com arquivo não-imagem é rejeitado (400)")
    @WithMockUser(roles = "GESTOR")
    void trabalhoComPdfRejeitado() throws Exception {
        org.springframework.mock.web.MockMultipartFile pdf = new org.springframework.mock.web.MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "%PDF".getBytes());
        Long pdfId = fileStorageService.salvar(pdf, null).getId();

        mockMvc.perform(post("/api/v1/clientes/" + cliente.getId() + "/trabalhos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tatuadorId\": " + tatuador.getId()
                                + ", \"dataTrabalho\": \"2026-10-20\", \"imagemId\": " + pdfId + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Tatuador não registra trabalho em nome de outro tatuador (403)")
    void tatuadorNaoRegistraEmNomeAlheio() throws Exception {
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatuador).cabine("C1")
                .dataHora(LocalDateTime.now().plusDays(1)).valor(new BigDecimal("400")).build());
        Tatuador outro = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Outro Tat").email("crm.outro@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        mockMvc.perform(post("/api/v1/clientes/" + cliente.getId() + "/trabalhos")
                        .header("Authorization", bearer(tatuador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tatuadorId\": " + outro.getId() + ", \"dataTrabalho\": \"2026-10-20\"}"))
                .andExpect(status().isForbidden());
    }
}
