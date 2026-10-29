package com.tattos.controller;

import com.tattos.model.Cliente;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClienteBuscaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        usuarioRepository.save(Cliente.builder().nome("Maria Silva").email("maria@x.com")
                .senha("h").telefone("(11) 91234-0001").ativo(true).build());
        usuarioRepository.save(Cliente.builder().nome("João Souza").email("joao@x.com")
                .senha("h").telefone("(11) 98765-0002").ativo(true).build());
        usuarioRepository.save(Cliente.builder().nome("Ana Inativa").email("ana@x.com")
                .senha("h").telefone("(11) 90000-0003").ativo(false).build());
    }

    @Test
    @DisplayName("Busca por nome parcial, case-insensitive")
    @WithMockUser(roles = "GESTOR")
    void buscaPorNome() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/clientes").param("query", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Maria Silva"));
    }

    @Test
    @DisplayName("Busca por trecho de e-mail")
    @WithMockUser(roles = "GESTOR")
    void buscaPorEmail() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/clientes").param("query", "joao@"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Busca por dígitos do telefone ignora formatação")
    @WithMockUser(roles = "GESTOR")
    void buscaPorTelefone() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/clientes").param("query", "11912340001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Maria Silva"));
    }

    @Test
    @DisplayName("Cliente inativo não aparece na busca")
    @WithMockUser(roles = "GESTOR")
    void inativoNaoAparece() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/clientes").param("query", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Sem query retorna todos os clientes ativos paginados")
    @WithMockUser(roles = "GESTOR")
    void semQueryRetornaTodos() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("Tatuador vê apenas clientes com agendamento com ele")
    void tatuadorVeApenasSeusClientes() throws Exception {
        com.tattos.model.Tatuador tatA = (com.tattos.model.Tatuador) usuarioRepository.save(
                com.tattos.model.Tatuador.builder().nome("Tat CRM A").email("crm.tata@tattos.com")
                        .senha("h").comissaoPercentual(new java.math.BigDecimal("60")).ativo(true).build());
        com.tattos.model.Tatuador tatB = (com.tattos.model.Tatuador) usuarioRepository.save(
                com.tattos.model.Tatuador.builder().nome("Tat CRM B").email("crm.tatb@tattos.com")
                        .senha("h").comissaoPercentual(new java.math.BigDecimal("60")).ativo(true).build());

        // Maria tem agendamento com A; João só com B
        var maria = usuarioRepository.findByEmail("maria@x.com").orElseThrow();
        var joao = usuarioRepository.findByEmail("joao@x.com").orElseThrow();
        agendamentoRepository.save(com.tattos.model.Agendamento.builder()
                .cliente(maria).tatuador(tatA).cabine("C1")
                .dataHora(java.time.LocalDateTime.now().plusDays(1))
                .valor(new java.math.BigDecimal("500")).build());
        agendamentoRepository.save(com.tattos.model.Agendamento.builder()
                .cliente(joao).tatuador(tatB).cabine("C2")
                .dataHora(java.time.LocalDateTime.now().plusDays(1))
                .valor(new java.math.BigDecimal("500")).build());

        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(tatA);
        mockMvc.perform(get("/api/v1/usuarios/clientes").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Maria Silva"));
    }

    @Autowired
    private com.tattos.repository.AgendamentoRepository agendamentoRepository;

    @Autowired
    private com.tattos.security.JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Sem query respeita page/size (sem dump completo)")
    @WithMockUser(roles = "GESTOR")
    void semQueryRespeitaPaginacao() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/clientes").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/v1/usuarios/clientes").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
