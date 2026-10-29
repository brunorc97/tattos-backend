package com.tattos.controller;

import com.tattos.repository.TaxaOperadoraRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaxaOperadoraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaxaOperadoraRepository taxaRepository;

    @BeforeEach
    void setUp() {
        taxaRepository.deleteAll();
    }

    @Test
    @DisplayName("Gestor cadastra taxa por operadora/modalidade")
    @WithMockUser(roles = "GESTOR")
    void criarTaxa() throws Exception {
        mockMvc.perform(post("/api/v1/financeiro/taxas-operadoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operadora\": \"Stone\", \"modalidade\": \"CREDITO_PARCELADO\", \"taxaPercentual\": 3.49}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.operadora").value("Stone"))
                .andExpect(jsonPath("$.taxaPercentual").value(3.49));
    }

    @Test
    @DisplayName("Duplicado operadora+modalidade retorna 409")
    @WithMockUser(roles = "GESTOR")
    void duplicado() throws Exception {
        String body = "{\"operadora\": \"Rede\", \"modalidade\": \"DEBITO\", \"taxaPercentual\": 1.50}";
        mockMvc.perform(post("/api/v1/financeiro/taxas-operadoras")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/financeiro/taxas-operadoras")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Listar retorna apenas ativas")
    @WithMockUser(roles = "GESTOR")
    void listarApenasAtivas() throws Exception {
        mockMvc.perform(post("/api/v1/financeiro/taxas-operadoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operadora\": \"PagSeguro\", \"modalidade\": \"DEBITO\", \"taxaPercentual\": 1.19}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/financeiro/taxas-operadoras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Tatuador não cadastra taxa (403)")
    @WithMockUser(roles = "TATUADOR")
    void tatuadorProibido() throws Exception {
        mockMvc.perform(post("/api/v1/financeiro/taxas-operadoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operadora\": \"X\", \"modalidade\": \"DEBITO\", \"taxaPercentual\": 1.0}"))
                .andExpect(status().isForbidden());
    }
}
