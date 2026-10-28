package com.tattos.service;

import com.tattos.dto.request.InsumoRequestDTO;
import com.tattos.dto.response.InsumoResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ConflictException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Insumo;
import com.tattos.repository.InsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsumoService {

    private final InsumoRepository insumoRepository;

    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> verificarEstoque() {
        return insumoRepository.findInsumosAbaixoNivelCritico().stream()
                .map(InsumoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public InsumoResponseDTO registrarEntrada(Long insumoId, int quantidade) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo não encontrado com ID: " + insumoId));

        insumo.adicionarEstoque(quantidade);
        Insumo salvo = insumoRepository.save(insumo);
        return InsumoResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public InsumoResponseDTO registrarSaida(Long insumoId, int quantidade) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo não encontrado com ID: " + insumoId));

        insumo.removerEstoque(quantidade);
        Insumo salvo = insumoRepository.save(insumo);
        return InsumoResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public InsumoResponseDTO criar(InsumoRequestDTO dto) {
        String codigo = dto.getCodigo();
        if (codigo != null && !codigo.trim().isEmpty()) {
            if (insumoRepository.existsByCodigo(codigo.trim())) {
                throw new ConflictException("Já existe um insumo cadastrado com o código: " + codigo);
            }
        } else {
            codigo = "INS-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        // Validação da data de validade: não pode ser data passada
        if (dto.getDataValidade() != null && dto.getDataValidade().isBefore(java.time.LocalDate.now())) {
            throw new BusinessException("Não é permitido cadastrar insumo com data de validade passada.");
        }
        Insumo insumo = Insumo.builder()
                .codigo(codigo)
                .nome(dto.getNome().trim())
                .quantidadeAtual(dto.getQuantidadeAtual())
                .nivelCritico(dto.getNivelCritico())
                .unidadeMedida(dto.getUnidadeMedida() != null ? dto.getUnidadeMedida().trim() : "UN")
                .categoria(dto.getCategoria() != null ? dto.getCategoria().trim() : null)
                .lote(dto.getLote())
                .dataValidade(dto.getDataValidade())
                .valorUnitario(dto.getValorUnitario())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Insumo salvo = insumoRepository.save(insumo);
        return InsumoResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public InsumoResponseDTO atualizar(Long id, InsumoRequestDTO dto) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo não encontrado com ID: " + id));

        insumo.setNome(dto.getNome().trim());
        insumo.setQuantidadeAtual(dto.getQuantidadeAtual());
        insumo.setNivelCritico(dto.getNivelCritico());
        if (dto.getUnidadeMedida() != null) {
            insumo.setUnidadeMedida(dto.getUnidadeMedida().trim());
        }
        insumo.setCategoria(dto.getCategoria());
        insumo.setLote(dto.getLote());
        insumo.setDataValidade(dto.getDataValidade());
        insumo.setValorUnitario(dto.getValorUnitario());

        Insumo salvo = insumoRepository.save(insumo);
        return InsumoResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public InsumoResponseDTO buscarPorId(Long id) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo não encontrado com ID: " + id));
        return InsumoResponseDTO.fromEntity(insumo);
    }

    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listar(String categoria, String query) {
        List<Insumo> lista;
        if (categoria != null && !categoria.trim().isEmpty()) {
            lista = insumoRepository.findByCategoriaIgnoreCase(categoria.trim());
        } else if (query != null && !query.trim().isEmpty()) {
            lista = insumoRepository.findByNomeContainingIgnoreCase(query.trim());
        } else {
            lista = insumoRepository.findAll();
        }
        return lista.stream().map(InsumoResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Page<InsumoResponseDTO> listarPaginado(String categoria, String query, Pageable pageable) {
        Page<Insumo> page;
        if (categoria != null && !categoria.trim().isEmpty()) {
            page = insumoRepository.findByCategoriaIgnoreCase(categoria.trim(), pageable);
        } else if (query != null && !query.trim().isEmpty()) {
            page = insumoRepository.findByNomeContainingIgnoreCase(query.trim(), pageable);
        } else {
            page = insumoRepository.findAll(pageable);
        }
        return page.map(InsumoResponseDTO::fromEntity);
    }

    @Transactional
    public void deletar(Long id) {
        if (!insumoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Insumo não encontrado com ID: " + id);
        }
        insumoRepository.deleteById(id);
    }
}
