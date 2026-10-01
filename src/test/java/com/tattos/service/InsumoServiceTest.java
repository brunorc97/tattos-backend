package com.tattos.service;

import com.tattos.dto.request.InsumoRequestDTO;
import com.tattos.dto.response.InsumoResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.repository.InsumoRepository;
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
class InsumoServiceTest {

    @Autowired
    private InsumoService insumoService;

    @Autowired
    private InsumoRepository insumoRepository;

    @BeforeEach
    void setUp() {
        insumoRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve registrar entrada e saída de estoque corretamente")
    void deveMovimentarEstoqueComSucesso() {
        InsumoRequestDTO dto = InsumoRequestDTO.builder()
                .nome("Agulha 05RL")
                .quantidadeAtual(20)
                .nivelCritico(10)
                .unidadeMedida("CX")
                .valorUnitario(new BigDecimal("50.00"))
                .build();

        InsumoResponseDTO criado = insumoService.criar(dto);

        // Entrada de 15 caixas -> 35
        InsumoResponseDTO comEntrada = insumoService.registrarEntrada(criado.getId(), 15);
        assertEquals(35, comEntrada.getQuantidadeAtual());

        // Saída de 10 caixas -> 25
        InsumoResponseDTO comSaida = insumoService.registrarSaida(criado.getId(), 10);
        assertEquals(25, comSaida.getQuantidadeAtual());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar dar saída de quantidade superior ao estoque")
    void deveLancarExcecaoAoExcederEstoqueDisponivel() {
        InsumoRequestDTO dto = InsumoRequestDTO.builder()
                .nome("Tinta Vermelha")
                .quantidadeAtual(5)
                .nivelCritico(10)
                .build();

        InsumoResponseDTO criado = insumoService.criar(dto);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                insumoService.registrarSaida(criado.getId(), 10));

        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
    }

    @Test
    @DisplayName("Deve listar insumos que atingiram o nível crítico de ruptura")
    void deveListarInsumosAbaixoDoNivelCritico() {
        // Insumo Seguro: qtd 50, critico 15
        insumoService.criar(InsumoRequestDTO.builder()
                .nome("Luvas Nitrílicas P")
                .quantidadeAtual(50)
                .nivelCritico(15)
                .build());

        // Insumo Crítico: qtd 3, critico 10
        insumoService.criar(InsumoRequestDTO.builder()
                .nome("Tinta Preto Tribal")
                .quantidadeAtual(3)
                .nivelCritico(10)
                .build());

        // Insumo No Limite: qtd 5, critico 5
        insumoService.criar(InsumoRequestDTO.builder()
                .nome("Agulha 09RS")
                .quantidadeAtual(5)
                .nivelCritico(5)
                .build());

        List<InsumoResponseDTO> criticos = insumoService.verificarEstoque();

        assertEquals(2, criticos.size());
        assertTrue(criticos.stream().anyMatch(i -> i.getNome().equals("Tinta Preto Tribal")));
        assertTrue(criticos.stream().anyMatch(i -> i.getNome().equals("Agulha 09RS")));
    }
}
