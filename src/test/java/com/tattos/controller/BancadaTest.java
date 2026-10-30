package com.tattos.controller;

import com.tattos.model.Agendamento;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.repository.AgendamentoRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BancadaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private com.tattos.repository.BancadaRepository bancadaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private LocalDate dia;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();

        dia = LocalDate.now().plusDays(3);
        Cliente cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Bancada").email("bancada.c@tattos.com").senha("h").ativo(true).build());
        Tatuador tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Bancada").email("bancada.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        // ocupa 14h–16h na Cabine 01
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatuador).cabine("Cabine 01")
                .dataHora(dia.atTime(14, 0)).duracaoMinutos(120)
                .valor(new BigDecimal("500")).build());

        // cancelado não bloqueia (10h–11h na Cabine 01)
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatuador).cabine("Cabine 01")
                .dataHora(dia.atTime(10, 0)).duracaoMinutos(60)
                .valor(new BigDecimal("300")).status(StatusAgendamento.CANCELADO).build());
    }

    @Test
    @DisplayName("Disponibilidade marca slots ocupados e livres; cancelado não bloqueia")
    @WithMockUser(roles = "TATUADOR")
    void disponibilidadePorDia() throws Exception {
        mockMvc.perform(get("/api/v1/bancadas/disponibilidade")
                        .param("cabine", "Cabine 01")
                        .param("data", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cabine").value("Cabine 01"))
                .andExpect(jsonPath("$.horarios[?(@.inicio == '14:00')].livre").value(false))
                .andExpect(jsonPath("$.horarios[?(@.inicio == '15:00')].livre").value(false))
                .andExpect(jsonPath("$.horarios[?(@.inicio == '10:00')].livre").value(true))
                .andExpect(jsonPath("$.horarios[?(@.inicio == '09:00')].livre").value(true));
    }

    @Test
    @DisplayName("Cliente não acessa disponibilidade de bancada (403)")
    @WithMockUser(roles = "CLIENTE")
    void clienteProibido() throws Exception {
        mockMvc.perform(get("/api/v1/bancadas/disponibilidade")
                        .param("cabine", "Cabine 01")
                        .param("data", dia.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Disponibilidade por bancadaId mostra slots so daquela bancada")
    @WithMockUser(roles = "GESTOR")
    void disponibilidadePorBancadaId() throws Exception {
        com.tattos.model.Bancada b1 = bancadaRepository.save(
                com.tattos.model.Bancada.builder().nome("Cabine 01").build());
        bancadaRepository.save(com.tattos.model.Bancada.builder().nome("Outra Bancada").build());

        mockMvc.perform(get("/api/v1/bancadas/disponibilidade")
                        .param("bancadaId", b1.getId().toString())
                        .param("data", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bancadaId").value(b1.getId()))
                .andExpect(jsonPath("$.bancadaNome").value("Cabine 01"))
                .andExpect(jsonPath("$.horarios[?(@.inicio == '14:00')].livre").value(false))
                .andExpect(jsonPath("$.horarios[?(@.inicio == '09:00')].livre").value(true));
    }

    @Test
    @DisplayName("Disponibilidade por bancadaId inexistente retorna 404")
    @WithMockUser(roles = "GESTOR")
    void disponibilidadeBancadaInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/bancadas/disponibilidade")
                        .param("bancadaId", "999999")
                        .param("data", dia.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Conflito de cabine/horário na criação retorna 409")
    @WithMockUser(roles = "GESTOR")
    void conflitoCabine() throws Exception {
        // validarConflitos existente valida por tatuador; este pin cobre sobreposição de cabine
        // (criação no mesmo slot/cabine com outro tatuador deve falhar quando a regra de cabine existir)
        Tatuador outro = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Outro").email("bancada.o@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        Cliente cli2 = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Dois").email("bancada.c2@tattos.com").senha("h").ativo(true).build());

        String body = """
                {"clienteId": %d, "tatuadorId": %d, "dataHora": "%sT15:00:00", "cabine": "Cabine 01", "valor": 400.00}
                """.formatted(cli2.getId(), outro.getId(), dia);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/agendamentos")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
