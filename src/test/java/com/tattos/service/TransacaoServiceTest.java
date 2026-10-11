package com.tattos.service;

import com.tattos.dto.request.TransacaoRequestDTO;
import com.tattos.dto.response.BalancoFinanceiroDTO;
import com.tattos.dto.response.FechamentoComissaoDTO;
import com.tattos.dto.response.TransacaoResponseDTO;
import com.tattos.model.Tatuador;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.TransacaoFinanceiraRepository;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransacaoServiceTest {

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private TransacaoFinanceiraRepository transacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Tatuador tatuador;

    @BeforeEach
    void setUp() {
        transacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        tatuador = Tatuador.builder()
                .nome("Diego Ortiz")
                .email("diego@tattos.com")
                .senha("pass123")
                .comissaoPercentual(new BigDecimal("60.00")) // 60% repasse
                .ativo(true)
                .build();
        tatuador = (Tatuador) usuarioRepository.save(tatuador);
    }

    @Test
    @DisplayName("Deve calcular automaticamente o valor de repasse da comissão do tatuador na receita")
    void deveCalcularRepasseDeComissao() {
        TransacaoRequestDTO receita = TransacaoRequestDTO.builder()
                .descricao("Tatuagem Realismo Braço")
                .valor(new BigDecimal("2000.00"))
                .tipo(TipoTransacao.RECEITA)
                .data(LocalDate.now())
                .tatuadorId(tatuador.getId())
                .build();

        TransacaoResponseDTO response = transacaoService.criar(receita);

        assertEquals(new BigDecimal("2000.00"), response.getValor());
        assertEquals(new BigDecimal("60.00"), response.getComissaoPercentual());
        assertEquals(new BigDecimal("1200.00"), response.getValorRepasse()); // 60% de 2000 = 1200
    }

    @Test
    @DisplayName("Deve gerar o balanço gerencial 'Smile Now, Cry Later' com faturamento, despesas, repasses e lucro líquido")
    void deveCalcularBalancoMensalSmileNowCryLater() {
        LocalDate hoje = LocalDate.now();

        // 1. Receita 1: 3000.00 (Repasse 60% = 1800.00)
        transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Fechamento de Costas")
                .valor(new BigDecimal("3000.00"))
                .tipo(TipoTransacao.RECEITA)
                .data(hoje)
                .tatuadorId(tatuador.getId())
                .build());

        // 2. Receita 2: 1000.00 (Repasse 60% = 600.00)
        transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Flash Day Tattoo")
                .valor(new BigDecimal("1000.00"))
                .tipo(TipoTransacao.RECEITA)
                .data(hoje)
                .tatuadorId(tatuador.getId())
                .build());

        // Total Faturamento Bruto (Smile Now) = 4000.00
        // Total Repasses = 1800 + 600 = 2400.00

        // 3. Despesa 1: Aluguel do Estúdio = 800.00
        transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Aluguel Estúdio")
                .valor(new BigDecimal("800.00"))
                .tipo(TipoTransacao.DESPESA)
                .data(hoje)
                .build());

        // 4. Despesa 2: Compra Insumos = 200.00
        transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Compra Agulhas")
                .valor(new BigDecimal("200.00"))
                .tipo(TipoTransacao.DESPESA)
                .data(hoje)
                .build());

        // Total Despesas = 1000.00
        // Total Saídas (Cry Later) = 1000 (despesas) + 2400 (repasses) = 3400.00
        // Lucro Líquido = 4000 - 3400 = 600.00
        // Margem = 600 / 4000 = 15.00%

        BalancoFinanceiroDTO balanco = transacaoService.obterBalancoMensal(hoje.getMonthValue(), hoje.getYear());

        assertEquals(new BigDecimal("4000.00"), balanco.getFaturamentoBruto());
        assertEquals(new BigDecimal("1000.00"), balanco.getTotalDespesas());
        assertEquals(new BigDecimal("2400.00"), balanco.getTotalRepassesTatuadores());
        assertEquals(new BigDecimal("3400.00"), balanco.getTotalSaidas());
        assertEquals(new BigDecimal("600.00"), balanco.getLucroLiquido());
        assertEquals(new BigDecimal("15.00"), balanco.getMargemLucroPercentual());
    }
}
