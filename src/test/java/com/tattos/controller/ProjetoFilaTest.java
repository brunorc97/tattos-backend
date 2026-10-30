package com.tattos.controller;

import com.tattos.model.Cliente;
import com.tattos.model.Projeto;
import com.tattos.model.Tatuador;
import com.tattos.model.enums.StatusAprovacaoProjeto;
import com.tattos.repository.ProjetoRepository;
import com.tattos.repository.UsuarioRepository;
import com.tattos.security.JwtTokenProvider;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProjetoFilaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Tatuador tatA;
    private Tatuador tatB;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        projetoRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Fila").email("fila.c@tattos.com").senha("h").ativo(true).build());
        tatA = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Fila A").email("filaa.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        tatB = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Fila B").email("filab.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        // tatA: um aprovado (antigo) e dois pendentes
        projetoRepository.save(Projeto.builder().nome("Aprovado A").cliente(cliente).tatuador(tatA)
                .statusAprovacao(StatusAprovacaoProjeto.APROVADO).build());
        projetoRepository.save(Projeto.builder().nome("Pendente A1").cliente(cliente).tatuador(tatA).build());
        projetoRepository.save(Projeto.builder().nome("Pendente A2").cliente(cliente).tatuador(tatA).build());
        projetoRepository.save(Projeto.builder().nome("Pendente B").cliente(cliente).tatuador(tatB).build());
    }

    @Test
    @DisplayName("Fila do tatuador retorna somente os projetos dele")
    void filaTatuadorRetornaSomenteSeusProjetos() throws Exception {
        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(tatA);
        mockMvc.perform(get("/api/v1/projetos/fila").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.tatuadorId != " + tatA.getId() + ")]").isEmpty());
    }

    @Test
    @DisplayName("Fila ordena pendentes primeiro, depois mais antigos")
    void filaOrdenaPendentesPrimeiroDepoisMaisAntigos() throws Exception {
        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(tatA);
        mockMvc.perform(get("/api/v1/projetos/fila").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pendente A1"))
                .andExpect(jsonPath("$[1].nome").value("Pendente A2"))
                .andExpect(jsonPath("$[2].nome").value("Aprovado A"));
    }

    @Test
    @DisplayName("Gestor filtra a fila por tatuadorId")
    @WithMockUser(roles = "GESTOR")
    void filaGestorFiltraPorTatuadorId() throws Exception {
        mockMvc.perform(get("/api/v1/projetos/fila").param("tatuadorId", tatB.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Pendente B"));
    }

    @Test
    @DisplayName("Cliente não acessa a fila (403)")
    void clienteNaoAcessaFilaRetorna403() throws Exception {
        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(cliente);
        mockMvc.perform(get("/api/v1/projetos/fila").header("Authorization", token))
                .andExpect(status().isForbidden());
    }
}
