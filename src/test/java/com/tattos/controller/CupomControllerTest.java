package com.tattos.controller;

import com.tattos.model.Tatuador;
import com.tattos.repository.CupomRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CupomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CupomRepository cupomRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Tatuador tatuador;

    @BeforeEach
    void setUp() {
        cupomRepository.deleteAll();
        usuarioRepository.deleteAll();
        tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Cupom").email("cupom.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
    }

    @Test
    @DisplayName("Gestor cria cupom studio-wide (sem tatuador)")
    @WithMockUser(roles = "GESTOR")
    void gestorCriaStudioWide() throws Exception {
        mockMvc.perform(post("/api/v1/cupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"BEMVINDO10\", \"tipo\": \"PORCENTAGEM\", \"valor\": 10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("BEMVINDO10"))
                .andExpect(jsonPath("$.tatuadorId").doesNotExist());
    }

    @Test
    @DisplayName("Código duplicado retorna 409")
    @WithMockUser(roles = "GESTOR")
    void codigoDuplicado() throws Exception {
        String body = "{\"codigo\": \"PROMO\", \"tipo\": \"VALOR_FIXO\", \"valor\": 50}";
        mockMvc.perform(post("/api/v1/cupons").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/cupons").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Tatuador criando cupom para outro tatuador recebe 403")
    void tatuadorNaoCriaParaOutro() throws Exception {
        Tatuador outro = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Outro").email("cupom.o@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(tatuador);
        mockMvc.perform(post("/api/v1/cupons")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"ALHEIO\", \"tipo\": \"PORCENTAGEM\", \"valor\": 5, \"tatuadorId\": " + outro.getId() + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tatuador lista os próprios cupons + studio-wide (não os de outro tatuador)")
    void tatuadorListaEscopo() throws Exception {
        Tatuador outro = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Outro2").email("cupom.o2@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        // studio-wide (gestor)
        cupomRepository.save(com.tattos.model.Cupom.builder()
                .codigo("STUDIO20").tipo(com.tattos.model.Cupom.Tipo.PORCENTAGEM)
                .valor(new BigDecimal("20")).build());
        // do próprio
        cupomRepository.save(com.tattos.model.Cupom.builder()
                .codigo("MEU5").tipo(com.tattos.model.Cupom.Tipo.PORCENTAGEM)
                .valor(new BigDecimal("5")).tatuador(tatuador).build());
        // de outro
        cupomRepository.save(com.tattos.model.Cupom.builder()
                .codigo("DELE10").tipo(com.tattos.model.Cupom.Tipo.PORCENTAGEM)
                .valor(new BigDecimal("10")).tatuador(outro).build());

        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(tatuador);
        mockMvc.perform(get("/api/v1/cupons").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
