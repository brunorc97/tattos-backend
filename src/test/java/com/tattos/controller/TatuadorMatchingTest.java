package com.tattos.controller;

import com.tattos.model.Tatuador;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TatuadorMatchingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        usuarioRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        usuarioRepository.save(Tatuador.builder().nome("Ana Fine").email("ana.match@tattos.com")
                .senha("h").especialidade("Fineline & Microrealismo")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        usuarioRepository.save(Tatuador.builder().nome("Beto Old").email("beto.match@tattos.com")
                .senha("h").especialidade("Old School & Bold Line")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        usuarioRepository.save(Tatuador.builder().nome("Inativo X").email("inativo.match@tattos.com")
                .senha("h").especialidade("Fineline")
                .comissaoPercentual(new BigDecimal("60")).ativo(false).build());
    }

    @Test
    @DisplayName("Sugestão por estilo retorna tatuadores ativos com especialidade compatível")
    @WithMockUser(roles = "CLIENTE")
    void sugerePorEstilo() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/tatuadores/sugerir").param("estilo", "fine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Ana Fine"));
    }

    @Test
    @DisplayName("Sem match retorna lista vazia 200")
    @WithMockUser(roles = "CLIENTE")
    void semMatch() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/tatuadores/sugerir").param("estilo", "aquarela-x"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
