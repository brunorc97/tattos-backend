package com.tattos.service;

import com.tattos.model.PagamentoComissao;
import com.tattos.model.Tatuador;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReciboPdfServiceTest {

    private final ReciboPdfService service = new ReciboPdfService();

    private String textoDo(byte[] pdf) throws Exception {
        try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(pdf))) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private PagamentoComissao pagamento(LocalDate inicio, LocalDate fim) {
        return PagamentoComissao.builder()
                .tatuador(Tatuador.builder().nome("Tat Recibo").build())
                .totalBruto(new BigDecimal("600.00"))
                .totalAdiantamentosAbatidos(BigDecimal.ZERO)
                .totalLiquido(new BigDecimal("600.00"))
                .formaPagamento("PIX")
                .itens(List.of())
                .periodoInicio(inicio)
                .periodoFim(fim)
                .build();
    }

    @Test
    void reciboIncluiPeriodoQuandoInformado() throws Exception {
        byte[] pdf = service.gerarReciboPagamento(
                pagamento(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)));
        String texto = textoDo(pdf);
        assertTrue(texto.contains("01/10/2026"), "deveria conter periodo inicio");
        assertTrue(texto.contains("31/10/2026"), "deveria conter periodo fim");
    }

    @Test
    void reciboSemPeriodoNaoQuebra() throws Exception {
        byte[] pdf = service.gerarReciboPagamento(pagamento(null, null));
        String texto = textoDo(pdf);
        assertTrue(texto.contains("Recibo de Pagamento"));
        assertFalse(texto.contains("Período"), "sem período não deve renderizar a linha");
    }
}
