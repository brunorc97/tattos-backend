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

    @Autowired
    private com.tattos.repository.TaxaOperadoraRepository taxaOperadoraRepository;

    @Test
    @DisplayName("Receita em cartão calcula taxa e valor líquido da operadora")
    void receitaComTaxaCalculaLiquido() {
        var taxa = taxaOperadoraRepository.save(com.tattos.model.TaxaOperadora.builder()
                .operadora("Stone").modalidade(com.tattos.model.enums.ModalidadePagamento.DEBITO)
                .taxaPercentual(new BigDecimal("1.99")).build());

        TransacaoResponseDTO response = transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Sessão no débito")
                .valor(new BigDecimal("1000.00"))
                .tipo(TipoTransacao.RECEITA)
                .data(LocalDate.now())
                .tatuadorId(tatuador.getId())
                .taxaOperadoraId(taxa.getId())
                .build());

        assertEquals(new BigDecimal("19.90"), response.getTaxaValor());
        assertEquals(new BigDecimal("980.10"), response.getValorLiquido());
    }

    @Test
    @DisplayName("Receita PIX sem operadora tem líquido igual ao valor")
    void receitaSemTaxaLiquidoIgual() {
        TransacaoResponseDTO response = transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Sessão PIX")
                .valor(new BigDecimal("500.00"))
                .tipo(TipoTransacao.RECEITA)
                .data(LocalDate.now())
                .tatuadorId(tatuador.getId())
                .formaPagamento("PIX")
                .build());

        assertEquals(new BigDecimal("500.00"), response.getValorLiquido());
        assertEquals(0, response.getTaxaValor().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Despesa com taxaOperadoraId é rejeitada")
    void despesaComTaxaRejeitada() {
        var taxa = taxaOperadoraRepository.save(com.tattos.model.TaxaOperadora.builder()
                .operadora("Rede").modalidade(com.tattos.model.enums.ModalidadePagamento.DEBITO)
                .taxaPercentual(new BigDecimal("1.50")).build());

        assertThrows(com.tattos.exception.BusinessException.class, () -> transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Compra de insumos")
                .valor(new BigDecimal("200.00"))
                .tipo(TipoTransacao.DESPESA)
                .data(LocalDate.now())
                .taxaOperadoraId(taxa.getId())
                .build()));
    }

    @Test
    @DisplayName("Balanço exclui ADIANTAMENTO e expõe taxas + faturamento líquido")
    void balancoExcluiAdiantamentoEExpoemTaxas() {
        var taxa = taxaOperadoraRepository.save(com.tattos.model.TaxaOperadora.builder()
                .operadora("Stone").modalidade(com.tattos.model.enums.ModalidadePagamento.CREDITO_A_VISTA)
                .taxaPercentual(new BigDecimal("2.00")).build());

        // receita com taxa: bruto 1000, taxa 20, líquido 980
        transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Receita cartão").valor(new BigDecimal("1000.00"))
                .tipo(TipoTransacao.RECEITA).data(LocalDate.now())
                .taxaOperadoraId(taxa.getId()).build());
        // despesa
        transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Despesa").valor(new BigDecimal("200.00"))
                .tipo(TipoTransacao.DESPESA).data(LocalDate.now()).build());
        // adiantamento: NÃO entra em receita nem despesa
        transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Adiantamento").valor(new BigDecimal("500.00"))
                .tipo(TipoTransacao.ADIANTAMENTO).data(LocalDate.now())
                .tatuadorId(tatuador.getId()).build());

        var balanco = transacaoService.obterBalancoMensal(
                LocalDate.now().getMonthValue(), LocalDate.now().getYear());

        assertEquals(new BigDecimal("1000.00"), balanco.getFaturamentoBruto());
        assertEquals(new BigDecimal("980.00"), balanco.getFaturamentoLiquido());
        assertEquals(new BigDecimal("20.00"), balanco.getTotalTaxas());
        assertEquals(new BigDecimal("200.00"), balanco.getTotalDespesas());
    }

    @Test
    @DisplayName("Transação automática sem taxa explícita tem valorLiquido preenchido via PrePersist")
    void transacaoAutomaticaTemLiquido() {
        // simula o caminho do listener (sem taxaOperadora): salva direto pelo repository
        var t = transacaoRepository.save(com.tattos.model.TransacaoFinanceira.builder()
                .descricao("Sinal automático")
                .valor(new BigDecimal("300.00"))
                .tipo(TipoTransacao.RECEITA)
                .data(LocalDate.now())
                .tatuador(tatuador)
                .categoria("Sinal Reserva")
                .build());

        assertEquals(new BigDecimal("300.00"), t.getValorLiquido());
    }

    @Test
    @DisplayName("Taxa desativada não pode ser usada em nova transação")
    void taxaInativaRejeitada() {
        var taxa = taxaOperadoraRepository.save(com.tattos.model.TaxaOperadora.builder()
                .operadora("Velha").modalidade(com.tattos.model.enums.ModalidadePagamento.DEBITO)
                .taxaPercentual(new BigDecimal("2.50")).ativo(false).build());

        assertThrows(com.tattos.exception.BusinessException.class, () -> transacaoService.criar(TransacaoRequestDTO.builder()
                .descricao("Com taxa inativa")
                .valor(new BigDecimal("100.00"))
                .tipo(TipoTransacao.RECEITA)
                .data(LocalDate.now())
                .taxaOperadoraId(taxa.getId())
                .build()));
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
