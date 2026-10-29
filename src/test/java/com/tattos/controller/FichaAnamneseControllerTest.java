package com.tattos.controller;

import com.tattos.model.Cliente;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.FichaAnamneseRepository;
import com.tattos.repository.UsuarioRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FichaAnamneseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FichaAnamneseRepository fichaRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        fichaRepository.deleteAll();
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cliente Ficha").email("ficha@tattos.com").senha("hash").ativo(true).build());
    }

    @Test
    @DisplayName("POST /api/v1/fichas-anamnese como GESTOR cria ficha para cliente existente")
    @WithMockUser(roles = "GESTOR")
    void gestorCriaFicha() throws Exception {
        String payload = """
                {"clienteId": %d, "historicoSaude": "Sem histórico", "alergias": "Nenhuma"}
                """.formatted(cliente.getId());

        mockMvc.perform(post("/api/v1/fichas-anamnese")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST ficha como GESTOR para cliente inexistente retorna 404")
    @WithMockUser(roles = "GESTOR")
    void gestorCriaParaClienteInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/fichas-anamnese")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": 99999}"))
                .andExpect(status().isNotFound());
    }

    @Autowired
    private com.tattos.security.JwtTokenProvider jwtTokenProvider;

    private String tokenDe(com.tattos.model.Usuario u) {
        return "Bearer " + jwtTokenProvider.generateTokenForUser(u);
    }

    private com.tattos.model.FichaAnamnese novaFicha(com.tattos.model.Usuario c) {
        return fichaRepository.save(com.tattos.model.FichaAnamnese.builder()
                .cliente(c).historicoSaude("ok").termoAssinado(false).build());
    }

    @Test
    @DisplayName("POST ficha como CLIENTE força o próprio clienteId mesmo se body indicar outro")
    void clienteCriaApenasParaSi() throws Exception {
        String token = jwtTokenProvider.generateTokenForUser(cliente);

        mockMvc.perform(post("/api/v1/fichas-anamnese")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": 99999}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()));
    }

    @Test
    @DisplayName("POST ficha sem autenticação retorna 401")
    void semAutenticacao() throws Exception {
        mockMvc.perform(post("/api/v1/fichas-anamnese")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": 1}"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Regras de visualização (Task 5) ─────────────────────────────────────

    @Test
    @DisplayName("Gestor lista fichas com filtro por cliente")
    @WithMockUser(roles = "GESTOR")
    void gestorFiltraPorCliente() throws Exception {
        novaFicha(cliente);
        Cliente outro = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Outro Cliente").email("outro@tattos.com").senha("h").ativo(true).build());
        novaFicha(outro);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/fichas-anamnese").param("clienteId", cliente.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(cliente.getId()));
    }

    @Test
    @DisplayName("Tatuador lista apenas fichas de clientes com agendamento com ele")
    void tatuadorVeApenasSeusClientes() throws Exception {
        com.tattos.model.Tatuador tatA = (com.tattos.model.Tatuador) usuarioRepository.save(
                com.tattos.model.Tatuador.builder().nome("Tat A").email("tata@tattos.com").senha("h")
                        .comissaoPercentual(new java.math.BigDecimal("60")).ativo(true).build());
        com.tattos.model.Tatuador tatB = (com.tattos.model.Tatuador) usuarioRepository.save(
                com.tattos.model.Tatuador.builder().nome("Tat B").email("tatb@tattos.com").senha("h")
                        .comissaoPercentual(new java.math.BigDecimal("60")).ativo(true).build());
        Cliente cliB = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cliente B").email("clib@tattos.com").senha("h").ativo(true).build());

        agendamentoRepository.save(com.tattos.model.Agendamento.builder()
                .cliente(cliente).tatuador(tatA)
                .dataHora(java.time.LocalDateTime.now().plusDays(1)).cabine("C1")
                .valor(new java.math.BigDecimal("500")).build());
        agendamentoRepository.save(com.tattos.model.Agendamento.builder()
                .cliente(cliB).tatuador(tatB)
                .dataHora(java.time.LocalDateTime.now().plusDays(1)).cabine("C2")
                .valor(new java.math.BigDecimal("500")).build());

        novaFicha(cliente); // cliente do tatuador A
        novaFicha(cliB);    // cliente do tatuador B

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/fichas-anamnese")
                        .header("Authorization", tokenDe(tatA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(cliente.getId()));
    }

    @Test
    @DisplayName("Cliente listando fichas de outro cliente recebe 403")
    void clienteNaoVeFichaDeOutro() throws Exception {
        Cliente outro = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Outro").email("outro2@tattos.com").senha("h").ativo(true).build());
        novaFicha(outro);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/fichas-anamnese/cliente/" + outro.getId())
                        .header("Authorization", tokenDe(cliente)))
                .andExpect(status().isForbidden());
    }
}
