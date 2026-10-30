package com.tattos.controller;

import com.tattos.model.Agendamento;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.repository.AgendamentoRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BancadaCrudTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.tattos.repository.BancadaRepository bancadaRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        bancadaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Gestor cria, lista, atualiza e deleta bancada")
    @WithMockUser(roles = "GESTOR")
    void crudCompleto() throws Exception {
        mockMvc.perform(post("/api/v1/bancadas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Bancada 1\", \"descricao\": \"Perto da janela\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Bancada 1"))
                .andExpect(jsonPath("$.ativa").value(true));

        Long id = bancadaRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/v1/bancadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put("/api/v1/bancadas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Bancada 01\", \"ativa\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bancada 01"));

        mockMvc.perform(delete("/api/v1/bancadas/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Nome duplicado de bancada retorna 400")
    @WithMockUser(roles = "GESTOR")
    void nomeDuplicadoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/bancadas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"B1\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/bancadas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"B1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Tatuador nao pode criar bancada (403)")
    @WithMockUser(roles = "TATUADOR")
    void tatuadorNaoCriaBancada() throws Exception {
        mockMvc.perform(post("/api/v1/bancadas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"B2\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deletar bancada com agendamento futuro retorna erro de negocio")
    @WithMockUser(roles = "GESTOR")
    void deletarComAgendamentoFuturoRetornaErro() throws Exception {
        Cliente cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli B").email("clib@tattos.com").senha("h").ativo(true).build());
        Tatuador tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat B").email("tatb@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        mockMvc.perform(post("/api/v1/bancadas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Cabine X\"}"))
                .andExpect(status().isCreated());
        Long id = bancadaRepository.findAll().get(0).getId();

        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatuador).cabine("Cabine X")
                .dataHora(LocalDateTime.now().plusDays(5)).valor(new BigDecimal("500")).build());

        mockMvc.perform(delete("/api/v1/bancadas/" + id))
                .andExpect(status().isBadRequest());
    }
}
