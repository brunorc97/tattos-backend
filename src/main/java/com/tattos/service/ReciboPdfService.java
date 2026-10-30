package com.tattos.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tattos.model.PagamentoComissao;
import com.tattos.model.PagamentoComissaoItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Gera o recibo detalhado do pagamento de comissão em PDF (OpenHTMLtoPDF).
 */
@Slf4j
@Service
public class ReciboPdfService {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final NumberFormat BRL = NumberFormat.getCurrencyInstance(PT_BR);

    public byte[] gerarReciboPagamento(PagamentoComissao p) {
        String html = montarHtml(p);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Falha ao gerar recibo PDF do pagamento {}", p.getId(), e);
            throw new IllegalStateException("Não foi possível gerar o recibo em PDF.");
        }
    }

    private String montarHtml(PagamentoComissao p) {
        StringBuilder itens = new StringBuilder();
        for (PagamentoComissaoItem i : p.getItens()) {
            itens.append("<tr><td>#").append(i.getTransacao().getId()).append("</td>")
                 .append("<td>").append(escape(i.getTransacao().getDescricao())).append("</td>")
                 .append("<td style='text-align:right'>").append(BRL.format(i.getValorRepasse())).append("</td></tr>");
        }

        String dataPagamento = p.getPagoEm() != null
                ? p.getPagoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "—";

        DateTimeFormatter dia = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String linhaPeriodo = p.getPeriodoInicio() != null && p.getPeriodoFim() != null
                ? "<br/><strong>Período:</strong> " + p.getPeriodoInicio().format(dia)
                        + " – " + p.getPeriodoFim().format(dia)
                : "";

        return """
            <html><head><style>
              body { font-family: sans-serif; color: #222; margin: 32px; }
              h1 { font-size: 18px; } h2 { font-size: 13px; color: #666; font-weight: normal; }
              table { width: 100%%; border-collapse: collapse; margin-top: 12px; font-size: 12px; }
              td, th { border-bottom: 1px solid #ddd; padding: 6px; text-align: left; }
              .totais td { border: none; padding: 2px 0; }
              .box { border: 1px solid #ccc; padding: 12px; margin-top: 16px; font-size: 12px; }
            </style></head><body>
              <h1>Recibo de Pagamento de Comissão — TattOS</h1>
              <h2>Recibo #%d · %s</h2>
              <div class="box">
                <strong>Tatuador:</strong> %s<br/>
                <strong>Forma de pagamento:</strong> %s<br/>
                <strong>Data:</strong> %s%s
              </div>
              <table>
                <tr><th>Transação</th><th>Descrição</th><th style='text-align:right'>Repasse</th></tr>
                %s
              </table>
              <table class="totais">
                <tr><td>Total bruto (comissões)</td><td style='text-align:right'>%s</td></tr>
                <tr><td>Adiantamentos abatidos</td><td style='text-align:right'>− %s</td></tr>
                <tr><td><strong>Total pago</strong></td><td style='text-align:right'><strong>%s</strong></td></tr>
              </table>
            </body></html>
            """.formatted(p.getId(), dataPagamento,
                escape(p.getTatuador().getNome()), escape(p.getFormaPagamento()), dataPagamento, linhaPeriodo,
                itens,
                BRL.format(p.getTotalBruto()),
                BRL.format(p.getTotalAdiantamentosAbatidos()),
                BRL.format(p.getTotalLiquido()));
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
