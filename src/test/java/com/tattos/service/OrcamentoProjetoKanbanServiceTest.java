package com.tattos.service;

import com.tattos.dto.request.KanbanCardRequestDTO;
import com.tattos.dto.request.OrcamentoRequestDTO;
import com.tattos.dto.request.ProjetoRequestDTO;
import com.tattos.dto.response.KanbanCardResponseDTO;
import com.tattos.dto.response.OrcamentoResponseDTO;
import com.tattos.dto.response.ProjetoResponseDTO;
import com.tattos.repository.KanbanCardRepository;
import com.tattos.repository.OrcamentoRepository;
import com.tattos.repository.ProjetoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrcamentoProjetoKanbanServiceTest {

    @Autowired
    private OrcamentoService orcamentoService;

    @Autowired
    private ProjetoService projetoService;

    @Autowired
    private KanbanCardService kanbanCardService;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private KanbanCardRepository kanbanCardRepository;

    @BeforeEach
    void setUp() {
        kanbanCardRepository.deleteAll();
        orcamentoRepository.deleteAll();
        projetoRepository.deleteAll();
    }

    // ─── Projeto ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar projeto com campos corretos")
    void deveCriarProjeto() {
        ProjetoRequestDTO dto = ProjetoRequestDTO.builder()
                .nome("Projeto Capa de Costas")
                .descricao("Arte japonesa full back")
                .aprovado(false)
                .build();

        ProjetoResponseDTO response = projetoService.criarProjeto(dto);

        assertNotNull(response.getId());
        assertEquals("Projeto Capa de Costas", response.getNome());
        assertEquals("Arte japonesa full back", response.getDescricao());
        assertFalse(response.isAprovado());
    }

    @Test
    @DisplayName("Deve criar projeto não aprovado por padrão quando aprovado é null")
    void deveCriarProjetoNaoAprovadoPorPadrao() {
        ProjetoRequestDTO dto = ProjetoRequestDTO.builder()
                .nome("Projeto Manga")
                .descricao("Sleeve completo")
                .build(); // aprovado = null

        ProjetoResponseDTO response = projetoService.criarProjeto(dto);

        assertFalse(response.isAprovado(), "Projeto deve ser não aprovado por padrão");
    }

    @Test
    @DisplayName("Deve aprovar projeto existente")
    void deveAprovarProjeto() {
        ProjetoRequestDTO dto = ProjetoRequestDTO.builder()
                .nome("Projeto Flash Day")
                .build();

        ProjetoResponseDTO criado = projetoService.criarProjeto(dto);
        assertFalse(criado.isAprovado());

        ProjetoResponseDTO aprovado = projetoService.aprovarProjeto(criado.getId());
        assertTrue(aprovado.isAprovado(), "Projeto deve estar aprovado após chamada de aprovação");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar aprovar projeto inexistente")
    void deveLancarExcecaoAoAprovarProjetoInexistente() {
        assertThrows(com.tattos.exception.ResourceNotFoundException.class,
                () -> projetoService.aprovarProjeto(9999L));
    }

    @Test
    @DisplayName("Deve listar todos os projetos criados")
    void deveListarTodosOsProjetos() {
        projetoService.criarProjeto(ProjetoRequestDTO.builder().nome("Projeto A").build());
        projetoService.criarProjeto(ProjetoRequestDTO.builder().nome("Projeto B").build());

        List<ProjetoResponseDTO> lista = projetoService.listarProjetos();

        assertEquals(2, lista.size());
    }

    // ─── Orçamento ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar orçamento sem vínculo a projeto")
    void deveCriarOrcamentoSemProjeto() {
        OrcamentoRequestDTO dto = OrcamentoRequestDTO.builder()
                .nome("Orçamento Tatuagem Colorida")
                .valor(new BigDecimal("850.00"))
                .build();

        OrcamentoResponseDTO response = orcamentoService.criarOrcamento(dto);

        assertNotNull(response.getId());
        assertEquals("Orçamento Tatuagem Colorida", response.getNome());
        assertEquals(new BigDecimal("850.00"), response.getValor());
        assertNull(response.getProjetoId());
    }

    @Test
    @DisplayName("Deve criar orçamento vinculado a um projeto existente")
    void deveCriarOrcamentoVinculadoAProjeto() {
        ProjetoResponseDTO projeto = projetoService.criarProjeto(
                ProjetoRequestDTO.builder().nome("Projeto Sleeve").build());

        OrcamentoRequestDTO dto = OrcamentoRequestDTO.builder()
                .nome("Orçamento Sleeve Completo")
                .valor(new BigDecimal("5000.00"))
                .projetoId(projeto.getId())
                .build();

        OrcamentoResponseDTO response = orcamentoService.criarOrcamento(dto);

        assertNotNull(response.getId());
        assertEquals(projeto.getId(), response.getProjetoId());
        assertEquals(new BigDecimal("5000.00"), response.getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar orçamento com projeto inexistente")
    void deveLancarExcecaoComProjetoInexistente() {
        OrcamentoRequestDTO dto = OrcamentoRequestDTO.builder()
                .nome("Orçamento Inválido")
                .valor(new BigDecimal("300.00"))
                .projetoId(9999L)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> orcamentoService.criarOrcamento(dto));
    }

    @Test
    @DisplayName("Deve listar todos os orçamentos")
    void deveListarTodosOsOrcamentos() {
        orcamentoService.criarOrcamento(OrcamentoRequestDTO.builder()
                .nome("Orçamento 1").valor(new BigDecimal("100.00")).build());
        orcamentoService.criarOrcamento(OrcamentoRequestDTO.builder()
                .nome("Orçamento 2").valor(new BigDecimal("200.00")).build());

        List<OrcamentoResponseDTO> lista = orcamentoService.listarOrcamentos();

        assertEquals(2, lista.size());
    }

    // ─── KanbanCard ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar kanban card sem vínculos")
    void deveCriarKanbanCardSemVinculos() {
        KanbanCardRequestDTO dto = KanbanCardRequestDTO.builder()
                .titulo("Aguardando Confirmação de Horário")
                .descricao("Cliente precisa confirmar disponibilidade")
                .status("A_FAZER")
                .ordem(1)
                .build();

        KanbanCardResponseDTO response = kanbanCardService.criarKanbanCard(dto);

        assertNotNull(response.getId());
        assertEquals("Aguardando Confirmação de Horário", response.getTitulo());
        assertEquals("A_FAZER", response.getStatus());
        assertEquals(1, response.getOrdem());
        assertNull(response.getOrcamentoId());
        assertNull(response.getProjetoId());
    }

    @Test
    @DisplayName("Deve criar kanban card vinculado a orçamento e projeto")
    void deveCriarKanbanCardComVinculos() {
        ProjetoResponseDTO projeto = projetoService.criarProjeto(
                ProjetoRequestDTO.builder().nome("Projeto Costas").build());

        OrcamentoResponseDTO orcamento = orcamentoService.criarOrcamento(
                OrcamentoRequestDTO.builder()
                        .nome("Orçamento Costas")
                        .valor(new BigDecimal("2000.00"))
                        .projetoId(projeto.getId())
                        .build());

        KanbanCardRequestDTO dto = KanbanCardRequestDTO.builder()
                .titulo("Sessão 1 de 3 — Rascunho")
                .status("EM_PROGRESSO")
                .ordem(1)
                .orcamentoId(orcamento.getId())
                .projetoId(projeto.getId())
                .build();

        KanbanCardResponseDTO response = kanbanCardService.criarKanbanCard(dto);

        assertNotNull(response.getId());
        assertEquals(orcamento.getId(), response.getOrcamentoId());
        assertEquals(projeto.getId(), response.getProjetoId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar kanban card com orçamento inexistente")
    void deveLancarExcecaoComOrcamentoInexistente() {
        KanbanCardRequestDTO dto = KanbanCardRequestDTO.builder()
                .titulo("Card Inválido")
                .status("A_FAZER")
                .ordem(1)
                .orcamentoId(9999L)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> kanbanCardService.criarKanbanCard(dto));
    }

    @Test
    @DisplayName("Deve listar todos os kanban cards")
    void deveListarTodosOsKanbanCards() {
        kanbanCardService.criarKanbanCard(KanbanCardRequestDTO.builder()
                .titulo("Card 1").status("A_FAZER").ordem(1).build());
        kanbanCardService.criarKanbanCard(KanbanCardRequestDTO.builder()
                .titulo("Card 2").status("CONCLUIDO").ordem(2).build());

        List<KanbanCardResponseDTO> lista = kanbanCardService.listarKanbanCards();

        assertEquals(2, lista.size());
    }
}
