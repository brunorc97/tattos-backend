package com.tattos.controller;

import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.SolicitacaoAgendamentoRepository;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SolicitacaoAgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private SolicitacaoAgendamentoRepository solicitacaoRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Cliente cliente;
    private Tatuador tatuador;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        solicitacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cliente Solic").email("solic@tattos.com").senha("h").ativo(true).build());
        tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Solic").email("tatsolic@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
    }

    private String bearer(com.tattos.model.Usuario u) {
        return "Bearer " + jwtTokenProvider.generateTokenForUser(u);
    }

    private String bearerGestor() {
        return bearer(usuarioRepository.findByEmail("gestor.solic@tattos.com")
                .orElseGet(() -> usuarioRepository.save(
                        com.tattos.model.Gestor.builder().nome("Gestor Solic").email("gestor.solic@tattos.com")
                                .senha("h").cargo("Gestor").ativo(true).build())));
    }

    @Test
    @DisplayName("Cliente cria solicitação com ideia; clienteId vem do token mesmo se body trouxer outro")
    void clienteCriaSolicitacao() throws Exception {
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ideia\": \"Dragão oriental nas costas\", \"clienteId\": 99999, \"estilo\": \"Oriental\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    @DisplayName("Cliente lista apenas as próprias solicitações")
    void clienteListaApenasProprias() throws Exception {
        Cliente outro = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Outro").email("outro.s@tattos.com").senha("h").ativo(true).build());

        mockMvc.perform(post("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ideia\": \"Minha ideia\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(outro))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ideia\": \"Ideia alheia\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].ideia").value("Minha ideia"));
    }

    @Test
    @DisplayName("Gestor aceita solicitação: cria agendamento e marca ACEITA")
    void gestorAceitaSolicitacao() throws Exception {
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ideia\": \"Floral no braço\", \"tatuadorId\": " + tatuador.getId() + "}"))
                .andExpect(status().isCreated());
        Long solicitacaoId = solicitacaoRepository.findAll().get(0).getId();

        String body = """
                {"dataHora": "2026-12-01T14:00:00", "cabine": "Cabine 01", "valor": 800.00, "duracaoMinutos": 120}
                """;
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento/" + solicitacaoId + "/aceitar")
                        .header("Authorization", bearerGestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACEITA"))
                .andExpect(jsonPath("$.agendamentoId").exists());

        org.assertj.core.api.Assertions.assertThat(agendamentoRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Gestor recusa solicitação com motivo")
    void gestorRecusaSolicitacao() throws Exception {
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ideia\": \"Cobertura grande\"}"))
                .andExpect(status().isCreated());
        Long id = solicitacaoRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/v1/solicitacoes-agendamento/" + id + "/recusar")
                        .header("Authorization", bearerGestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Agenda cheia no período\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECUSADA"))
                .andExpect(jsonPath("$.motivoRecusa").value("Agenda cheia no período"));
    }

    @Test
    @DisplayName("Aceitar solicitação inexistente retorna 404")
    void aceitarInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento/99999/aceitar")
                        .header("Authorization", bearerGestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataHora\": \"2026-12-01T14:00:00\", \"cabine\": \"C1\", \"valor\": 100.00}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Gestor aceita solicitação sem tatuador informando tatuadorId")
    void gestorAceitaSemTatuadorComOverride() throws Exception {
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ideia\": \"Sem tatuador indicado\"}"))
                .andExpect(status().isCreated());
        Long id = solicitacaoRepository.findAll().get(0).getId();

        // sem tatuadorId → 400 (regra de negócio)
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento/" + id + "/aceitar")
                        .header("Authorization", bearerGestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataHora\": \"2026-12-10T10:00:00\", \"cabine\": \"C1\", \"valor\": 500.00}"))
                .andExpect(status().isBadRequest());

        // com tatuadorId → 201
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento/" + id + "/aceitar")
                        .header("Authorization", bearerGestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataHora\": \"2026-12-10T10:00:00\", \"cabine\": \"C1\", \"valor\": 500.00, \"tatuadorId\": " + tatuador.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACEITA"));
    }

    @Test
    @DisplayName("Tatuador não pode aceitar solicitação designada a outro tatuador")
    void tatuadorNaoAceitaDeOutro() throws Exception {
        Tatuador outro = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Outro").email("outro.solic@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        mockMvc.perform(post("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ideia\": \"Para o tatuador A\", \"tatuadorId\": " + tatuador.getId() + "}"))
                .andExpect(status().isCreated());
        Long id = solicitacaoRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/v1/solicitacoes-agendamento/" + id + "/aceitar")
                        .header("Authorization", bearer(outro))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataHora\": \"2026-12-10T10:00:00\", \"cabine\": \"C1\", \"valor\": 500.00}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cliente não pode indicar um gestor como tatuador da solicitação")
    void clienteNaoIndicaGestorComoTatuador() throws Exception {
        com.tattos.model.Usuario gestor = usuarioRepository.findByEmail("gestor.solic@tattos.com")
                .orElseGet(() -> usuarioRepository.save(com.tattos.model.Gestor.builder()
                        .nome("G").email("gestor.solic@tattos.com").senha("h").cargo("Gestor").ativo(true).build()));

        mockMvc.perform(post("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ideia\": \"X\", \"tatuadorId\": " + gestor.getId() + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cliente não anexa imagem de outro usuário")
    void clienteNaoAnexaImagemAlheia() throws Exception {
        Cliente outro = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Dono Img").email("img.dono@tattos.com").senha("h").ativo(true).build());
        // upload como outro
        org.springframework.mock.web.MockMultipartFile img = new org.springframework.mock.web.MockMultipartFile(
                "file", "ref.png", "image/png", new byte[]{1, 2, 3});
        com.tattos.model.Arquivo arquivo = fileStorageService.salvar(img, outro);

        mockMvc.perform(post("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ideia\": \"Com imagem alheia\", \"imagemReferenciaId\": " + arquivo.getId() + "}"))
                .andExpect(status().isBadRequest());
    }

    @Autowired
    private com.tattos.service.FileStorageService fileStorageService;

    @Test
    @DisplayName("Aceitar solicitação já aceita retorna 409")
    void aceitarDuasVezes() throws Exception {
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ideia\": \"X\", \"tatuadorId\": " + tatuador.getId() + "}"))
                .andExpect(status().isCreated());
        Long id = solicitacaoRepository.findAll().get(0).getId();
        String body = "{\"dataHora\": \"2026-12-05T10:00:00\", \"cabine\": \"C1\", \"valor\": 300.00}";

        mockMvc.perform(post("/api/v1/solicitacoes-agendamento/" + id + "/aceitar")
                        .header("Authorization", bearerGestor())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/solicitacoes-agendamento/" + id + "/aceitar")
                        .header("Authorization", bearerGestor())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }
}
