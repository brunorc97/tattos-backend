package com.tattos.service;

import com.tattos.dto.request.AgendamentoRequestDTO;
import com.tattos.dto.request.SolicitacaoAgendamentoRequestDTO;
import com.tattos.dto.response.AgendamentoResponseDTO;
import com.tattos.dto.response.SolicitacaoAgendamentoResponseDTO;
import com.tattos.exception.ConflictException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Agendamento;
import com.tattos.model.Arquivo;
import com.tattos.model.Projeto;
import com.tattos.model.SolicitacaoAgendamento;
import com.tattos.model.Usuario;
import com.tattos.model.enums.StatusSolicitacao;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.ArquivoRepository;
import com.tattos.repository.ProjetoRepository;
import com.tattos.repository.SolicitacaoAgendamentoRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolicitacaoAgendamentoService {

    private final SolicitacaoAgendamentoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoRepository projetoRepository;
    private final ArquivoRepository arquivoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoService agendamentoService;

    @Transactional
    public SolicitacaoAgendamentoResponseDTO criar(SolicitacaoAgendamentoRequestDTO dto, Usuario clienteLogado) {
        SolicitacaoAgendamento s = SolicitacaoAgendamento.builder()
                .cliente(clienteLogado)
                .ideia(dto.getIdeia())
                .estilo(dto.getEstilo())
                .diaPreferencia(dto.getDiaPreferencia())
                .semanaPreferencia(dto.getSemanaPreferencia())
                .build();

        if (dto.getTatuadorId() != null) {
            Usuario tatuador = usuarioRepository.findById(dto.getTatuadorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com ID: " + dto.getTatuadorId()));
            if (!tatuador.isTatuador() || !tatuador.isAtivo()) {
                throw new com.tattos.exception.BusinessException("O usuário informado não é um tatuador ativo do studio.");
            }
            s.setTatuador(tatuador);
        }
        if (dto.getProjetoId() != null) {
            Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
            s.setProjeto(projeto);
        }
        if (dto.getImagemReferenciaId() != null) {
            Arquivo imagem = arquivoRepository.findById(dto.getImagemReferenciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada com ID: " + dto.getImagemReferenciaId()));
            if (imagem.getDono() != null && !imagem.getDono().getId().equals(clienteLogado.getId())) {
                throw new com.tattos.exception.BusinessException("A imagem de referência não pertence a você.");
            }
            s.setImagemReferencia(imagem);
        }

        return SolicitacaoAgendamentoResponseDTO.fromEntity(solicitacaoRepository.save(s));
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoAgendamentoResponseDTO> listar(Usuario logado, Pageable pageable) {
        Page<SolicitacaoAgendamento> page;
        if (logado.isCliente()) {
            page = solicitacaoRepository.findByClienteId(logado.getId(), pageable);
        } else if (logado.isTatuador()) {
            page = solicitacaoRepository.findByTatuadorIdOrTatuadorIsNull(logado.getId(), pageable);
        } else {
            page = solicitacaoRepository.findAll(pageable);
        }
        return page.map(SolicitacaoAgendamentoResponseDTO::fromEntity);
    }

    @Transactional
    public SolicitacaoAgendamentoResponseDTO aceitar(Long id, java.time.LocalDateTime dataHora,
                                                     String cabine, java.math.BigDecimal valor,
                                                     Integer duracaoMinutos, Usuario staff,
                                                     Long tatuadorIdOverride) {
        SolicitacaoAgendamento s = buscar(id);
        if (s.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new ConflictException("A solicitação já foi " + s.getStatus().name().toLowerCase() + ".");
        }
        validarEscopoStaff(s, staff);

        Usuario tatuador;
        if (tatuadorIdOverride != null) {
            tatuador = usuarioRepository.findById(tatuadorIdOverride)
                    .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com ID: " + tatuadorIdOverride));
            if (!tatuador.isTatuador() || !tatuador.isAtivo()) {
                throw new com.tattos.exception.BusinessException("O usuário informado não é um tatuador ativo do studio.");
            }
        } else if (s.getTatuador() != null) {
            tatuador = s.getTatuador();
        } else if (staff != null && staff.isTatuador()) {
            tatuador = staff;
        } else {
            throw new com.tattos.exception.BusinessException(
                    "A solicitação não indica tatuador — informe o tatuadorId ao aceitar.");
        }

        AgendamentoRequestDTO agendamentoDto = AgendamentoRequestDTO.builder()
                .clienteId(s.getCliente().getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(duracaoMinutos)
                .cabine(cabine)
                .valor(valor)
                .observacoes("Solicitação #" + s.getId() + ": " + s.getIdeia())
                .build();
        AgendamentoResponseDTO criado = agendamentoService.criar(agendamentoDto);

        Agendamento agendamento = agendamentoRepository.findById(criado.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));
        s.setAgendamento(agendamento);
        s.setStatus(StatusSolicitacao.ACEITA);
        return SolicitacaoAgendamentoResponseDTO.fromEntity(solicitacaoRepository.save(s));
    }

    @Transactional
    public SolicitacaoAgendamentoResponseDTO recusar(Long id, String motivo, Usuario staff) {
        SolicitacaoAgendamento s = buscar(id);
        if (s.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new ConflictException("A solicitação já foi " + s.getStatus().name().toLowerCase() + ".");
        }
        validarEscopoStaff(s, staff);
        s.setMotivoRecusa(motivo);
        s.setStatus(StatusSolicitacao.RECUSADA);
        return SolicitacaoAgendamentoResponseDTO.fromEntity(solicitacaoRepository.save(s));
    }

    /** Tatuador só decide solicitações suas ou sem tatuador designado; gestor decide qualquer uma. */
    private void validarEscopoStaff(SolicitacaoAgendamento s, Usuario staff) {
        if (staff != null && staff.isTatuador()
                && s.getTatuador() != null
                && !s.getTatuador().getId().equals(staff.getId())) {
            throw new com.tattos.exception.BusinessException(
                    "Esta solicitação está designada a outro tatuador.");
        }
    }

    private SolicitacaoAgendamento buscar(Long id) {
        return solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada com ID: " + id));
    }
}
