package com.tattos.service;

import com.tattos.model.Cliente;
import com.tattos.model.Projeto;
import com.tattos.model.Tatuador;
import com.tattos.model.enums.StatusAprovacaoProjeto;
import com.tattos.repository.ProjetoRepository;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjetoServiceTest {

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Cliente cliente;
    private Tatuador tatuador;

    @BeforeEach
    void setUp() {
        projetoRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Projeto").email("proj.c@tattos.com").senha("h").ativo(true).build());
        tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Projeto").email("proj.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
    }

    @Test
    @DisplayName("Projeto persiste cliente, tatuador, prazo e status de aprovação")
    void persisteVinculosEPrazo() {
        Projeto p = projetoRepository.save(Projeto.builder()
                .nome("Capa de costas")
                .cliente(cliente)
                .tatuador(tatuador)
                .prazo(LocalDate.now().plusMonths(2))
                .statusAprovacao(StatusAprovacaoProjeto.PENDENTE)
                .build());

        Projeto lido = projetoRepository.findById(p.getId()).orElseThrow();
        assertThat(lido.getCliente().getId()).isEqualTo(cliente.getId());
        assertThat(lido.getTatuador().getId()).isEqualTo(tatuador.getId());
        assertThat(lido.getPrazo()).isNotNull();
        assertThat(lido.getStatusAprovacao()).isEqualTo(StatusAprovacaoProjeto.PENDENTE);
    }

    @Test
    @DisplayName("Novo projeto sem decisão fica PENDENTE por padrão")
    void padraoPendente() {
        Projeto p = projetoRepository.save(Projeto.builder()
                .nome("Sem decisão").cliente(cliente).build());

        assertThat(projetoRepository.findById(p.getId()).orElseThrow().getStatusAprovacao())
                .isEqualTo(StatusAprovacaoProjeto.PENDENTE);
    }
}
