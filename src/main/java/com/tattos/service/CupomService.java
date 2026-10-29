package com.tattos.service;

import com.tattos.exception.BusinessException;
import com.tattos.exception.ConflictException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Cupom;
import com.tattos.model.Usuario;
import com.tattos.repository.CupomRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CupomService {

    private final CupomRepository cupomRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Cupom criar(String codigo, Cupom.Tipo tipo, BigDecimal valor, Long tatuadorId,
                       LocalDate validade, Integer limiteUsos, Usuario autor) {
        if (tipo == Cupom.Tipo.PORCENTAGEM && valor.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("Cupom de porcentagem não pode ser maior que 100%.");
        }
        String codigoNorm = codigo.trim().toUpperCase();
        if (cupomRepository.existsByCodigoIgnoreCase(codigoNorm)) {
            throw new ConflictException("Já existe um cupom com o código: " + codigoNorm);
        }

        Usuario tatuador = null;
        if (autor != null && autor.isTatuador()) {
            // tatuador só cria cupom para si
            if (tatuadorId != null && !tatuadorId.equals(autor.getId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Você só pode criar cupons para si mesmo.");
            }
            tatuador = autor;
        } else if (tatuadorId != null) {
            tatuador = usuarioRepository.findById(tatuadorId)
                    .filter(Usuario::isTatuador)
                    .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com ID: " + tatuadorId));
        }

        return cupomRepository.save(Cupom.builder()
                .codigo(codigoNorm)
                .tipo(tipo)
                .valor(valor)
                .tatuador(tatuador)
                .validade(validade)
                .limiteUsos(limiteUsos)
                .criadoPor(autor)
                .build());
    }

    @Transactional(readOnly = true)
    public List<Cupom> listar(Usuario logado) {
        if (logado != null && logado.isTatuador()) {
            return cupomRepository.findAtivosVisiveisAoTatuador(logado.getId());
        }
        return cupomRepository.findByAtivoTrue();
    }

    @Transactional
    public void desativar(Long id, Usuario logado) {
        Cupom cupom = cupomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupom não encontrado com ID: " + id));
        if (logado != null && logado.isTatuador()
                && (cupom.getTatuador() == null || !cupom.getTatuador().getId().equals(logado.getId()))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você só pode desativar os seus próprios cupons.");
        }
        cupom.setAtivo(false);
        cupomRepository.save(cupom);
    }

    /**
     * Valida o cupom para o tatuador do agendamento e retorna o desconto.
     * Regras: ativo, validade, limite de usos, abrangência (studio ou o tatuador).
     * Nunca excede o valor base (cupom fixo trunca no valor).
     */
    @Transactional(readOnly = true)
    public BigDecimal validarECalcular(String codigo, Long tatuadorId, BigDecimal valorBase) {
        Cupom cupom = cupomRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new BusinessException("Cupom não encontrado: " + codigo));

        if (!cupom.isAtivo()) {
            throw new BusinessException("Cupom desativado.");
        }
        if (cupom.getValidade() != null && cupom.getValidade().isBefore(LocalDate.now())) {
            throw new BusinessException("Cupom expirado em " + cupom.getValidade() + ".");
        }
        if (cupom.getLimiteUsos() != null && cupom.getUsos() >= cupom.getLimiteUsos()) {
            throw new BusinessException("Cupom esgotado (limite de usos atingido).");
        }
        if (cupom.getTatuador() != null
                && (tatuadorId == null || !cupom.getTatuador().getId().equals(tatuadorId))) {
            throw new BusinessException("Este cupom é válido apenas para outro tatuador.");
        }

        BigDecimal desconto = cupom.getTipo() == Cupom.Tipo.PORCENTAGEM
                ? valorBase.multiply(cupom.getValor()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : cupom.getValor();
        return desconto.min(valorBase);
    }

    /** Registra o uso de forma atômica (chamado junto da criação do agendamento, mesma transação). */
    @Transactional
    public void registrarUso(Cupom cupom) {
        int atualizados = cupomRepository.incrementarUsoSeDisponivel(cupom.getId());
        if (atualizados == 0) {
            throw new BusinessException("Cupom esgotado (limite de usos atingido).");
        }
    }

    @Transactional(readOnly = true)
    public Cupom buscarPorCodigo(String codigo) {
        return cupomRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new BusinessException("Cupom não encontrado: " + codigo));
    }
}
