package com.tattos.controller;

import com.tattos.model.Avaliacao;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.model.Trabalho;
import com.tattos.repository.AvaliacaoRepository;
import com.tattos.repository.TrabalhoRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudioTatuadoresTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TrabalhoRepository trabalhoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    private Tatuador comAvaliacao;
    private Tatuador semAvaliacao;
    private Tatuador inativo;

    @BeforeEach
    void setUp() {
        avaliacaoRepository.deleteAll();
        trabalhoRepository.deleteAll();
        usuarioRepository.deleteAll();

        Cliente cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Vit").email("vit.c@tattos.com").senha("h").ativo(true).build());
        comAvaliacao = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Nota").email("vit.a@tattos.com").senha("h").especialidade("Fine Line")
                .bio("bio").comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        semAvaliacao = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Zero").email("vit.z@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        inativo = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Off").email("vit.off@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(false).build());

        Trabalho t1 = trabalhoRepository.save(Trabalho.builder()
                .tatuador(comAvaliacao).cliente(cliente).titulo("T1").data(LocalDate.now()).build());
        Trabalho t2 = trabalhoRepository.save(Trabalho.builder()
                .tatuador(comAvaliacao).cliente(cliente).titulo("T2").data(LocalDate.now()).build());

        // médias por trabalho: (5+4)/2 = 4.5 ; (3+2)/2 = 2.5 → média geral 3.5
        avaliacaoRepository.save(Avaliacao.builder().trabalho(t1).cliente(cliente)
                .tatuador(comAvaliacao).notaTatuagem(5).notaTatuador(4).data(LocalDate.now()).build());
        avaliacaoRepository.save(Avaliacao.builder().trabalho(t2).cliente(cliente)
                .tatuador(comAvaliacao).notaTatuagem(3).notaTatuador(2).data(LocalDate.now()).build());
    }

    @Test
    @DisplayName("Tatuador com avaliações retorna média de 1 casa e total")
    @WithMockUser(roles = "CLIENTE")
    void tatuadorComAvaliacao() throws Exception {
        mockMvc.perform(get("/api/v1/studio/tatuadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + comAvaliacao.getId() + ")].mediaAvaliacao").value(3.5))
                .andExpect(jsonPath("$[?(@.id == " + comAvaliacao.getId() + ")].totalAvaliacoes").value(2));
    }

    @Test
    @DisplayName("Tatuador sem avaliações retorna mediaAvaliacao null e total zero")
    @WithMockUser(roles = "CLIENTE")
    void tatuadorSemAvaliacao() throws Exception {
        mockMvc.perform(get("/api/v1/studio/tatuadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + semAvaliacao.getId() + ")].mediaAvaliacao").value((Object) null))
                .andExpect(jsonPath("$[?(@.id == " + semAvaliacao.getId() + ")].totalAvaliacoes").value(0));
    }

    @Test
    @DisplayName("Tatuador inativo nao aparece na vitrine")
    @WithMockUser(roles = "CLIENTE")
    void tatuadorInativoNaoAparece() throws Exception {
        mockMvc.perform(get("/api/v1/studio/tatuadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + inativo.getId() + ")]").isEmpty());
    }
}
