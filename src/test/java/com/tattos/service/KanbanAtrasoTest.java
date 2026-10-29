package com.tattos.service;

import com.tattos.dto.response.KanbanCardResponseDTO;
import com.tattos.model.Cliente;
import com.tattos.model.KanbanCard;
import com.tattos.model.Projeto;
import com.tattos.repository.KanbanCardRepository;
import com.tattos.repository.ProjetoRepository;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class KanbanAtrasoTest {

    @Autowired
    private KanbanCardService kanbanCardService;

    @Autowired
    private KanbanCardRepository kanbanCardRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        kanbanCardRepository.deleteAll();
        projetoRepository.deleteAll();
        usuarioRepository.deleteAll();
        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Kanban Atraso").email("kanban.atraso@tattos.com").senha("h").ativo(true).build());
    }

    private KanbanCard cardDoProjeto(Projeto p, String status) {
        return kanbanCardRepository.save(KanbanCard.builder()
                .titulo("Card " + status).status(status).ordem(1).projeto(p).build());
    }

    @Test
    @DisplayName("Card de projeto com prazo vencido e status não-final fica atrasado")
    void cardAtrasado() {
        Projeto p = projetoRepository.save(Projeto.builder()
                .nome("Atrasado").cliente(cliente).prazo(LocalDate.now().minusDays(1)).build());
        cardDoProjeto(p, "EM_ANDAMENTO");

        KanbanCardResponseDTO dto = kanbanCardService.listarKanbanCards().stream()
                .filter(k -> k.getTitulo().equals("Card EM_ANDAMENTO")).findFirst().orElseThrow();

        assertThat(dto.getAtrasado()).isTrue();
    }

    @Test
    @DisplayName("Card concluído com prazo vencido NÃO fica atrasado")
    void cardConcluidoNaoAtrasa() {
        Projeto p = projetoRepository.save(Projeto.builder()
                .nome("Concluído no atraso").cliente(cliente).prazo(LocalDate.now().minusDays(1)).build());
        cardDoProjeto(p, "CONCLUIDO");

        KanbanCardResponseDTO dto = kanbanCardService.listarKanbanCards().stream()
                .filter(k -> k.getTitulo().equals("Card CONCLUIDO")).findFirst().orElseThrow();

        assertThat(dto.getAtrasado()).isFalse();
    }

    @Test
    @DisplayName("Card com prazo futuro não fica atrasado")
    void cardNoPrazo() {
        Projeto p = projetoRepository.save(Projeto.builder()
                .nome("No prazo").cliente(cliente).prazo(LocalDate.now().plusDays(5)).build());
        cardDoProjeto(p, "EM_ANDAMENTO");

        KanbanCardResponseDTO dto = kanbanCardService.listarKanbanCards().stream()
                .filter(k -> k.getTitulo().equals("Card EM_ANDAMENTO")).findFirst().orElseThrow();

        assertThat(dto.getAtrasado()).isFalse();
    }
}
