package com.tattos.service;

import com.tattos.model.TransacaoFinanceira;
import com.tattos.repository.TransacaoFinanceiraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.StringJoiner;

/**
 * Serviço simples que gera um relatório de comissões em formato CSV.
 * Não utiliza bibliotecas externas – apenas concatena linhas com separador ';'.
 * Cada linha contém: data, valor, tatuadorId, tipo (RECEITA/DESPESA) e categoria.
 */
@Service
@RequiredArgsConstructor
public class ComissaoExportService {

    private final TransacaoFinanceiraRepository transacaoRepository;

    /**
     * Exporta as transações financeiras de um tatuador dentro do período informado.
     *
     * @param tatuadorId id do tatuador
     * @param inicio      data inicial (inclusive)
     * @param fim         data final (inclusive)
     * @return CSV string (UTF‑8) com cabeçalho.
     */
    @Transactional(readOnly = true)
    public String exportarCsv(Long tatuadorId, LocalDate inicio, LocalDate fim) {
        List<TransacaoFinanceira> transacoes = transacaoRepository
                .findComFiltros(null, tatuadorId, null, inicio, fim);

        StringJoiner sj = new StringJoiner("\n");
        // Cabeçalho
        sj.add("data;valor;tipo;categoria;tatuadorId");
        transacoes.forEach(t -> {
            String linha = String.format("%s;%.2f;%s;%s;%d",
                    t.getData(),
                    t.getValor(),
                    t.getTipo(),
                    t.getCategoria() != null ? t.getCategoria() : "",
                    t.getTatuador() != null ? t.getTatuador().getId() : 0);
            sj.add(linha);
        });
        return sj.toString();
    }
}
