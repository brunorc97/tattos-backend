package com.tattos.service;

import com.tattos.dto.request.FichaAnamneseRequestDTO;
import com.tattos.dto.response.FichaAnamneseResponseDTO;
import com.tattos.dto.request.AssinaturaRequestDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Agendamento;
import com.tattos.model.FichaAnamnese;
import com.tattos.model.Usuario;
import com.tattos.model.AssinaturaLog;
import com.tattos.repository.FichaAnamneseRepository;
import com.tattos.repository.UsuarioRepository;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.AssinaturaLogRepository;
import com.tattos.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FichaAnamneseService {

    private final FichaAnamneseRepository fichaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AssinaturaLogRepository assinaturaLogRepository;
    private final EmailService emailService;

    @Transactional
    public void assinarTermo(Long fichaId, AssinaturaRequestDTO dto) {
        // Carrega a ficha
        FichaAnamnese ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha de anamnese não encontrada com ID: " + fichaId));
        // Marca o termo como assinado
        ficha.setTermoAssinado(true);
        // Persiste a alteração
        fichaRepository.save(ficha);
        // Cria registro de log
        AssinaturaLog log = AssinaturaLog.builder()
                .fichaAnamnese(ficha)
                .dataAssinatura(LocalDateTime.now())
                .email(dto.getEmail())
                .usuario(dto.getUsuarioId() != null ? usuarioRepository.findById(dto.getUsuarioId()).orElse(null) : null)
                .build();
        assinaturaLogRepository.save(log);
        // Log de e‑mail (placeholder)
        emailService.logAssinaturaEnviada(fichaId);
    }

    @Transactional
    public FichaAnamneseResponseDTO criar(FichaAnamneseRequestDTO dto) {
        return registrarFicha(dto);
    }

    @Transactional
    public FichaAnamneseResponseDTO registrarFicha(FichaAnamneseRequestDTO dto) {
        Usuario cliente = usuarioRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + dto.getClienteId()));

        Agendamento agendamento = null;
        if (dto.getAgendamentoId() != null) {
            agendamento = agendamentoRepository.findById(dto.getAgendamentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + dto.getAgendamentoId()));
        }

        if (Boolean.TRUE.equals(dto.getTermoAssinado()) && (dto.getAssinaturaDigital() == null || dto.getAssinaturaDigital().trim().isEmpty())) {
            throw new BusinessException("Para confirmar a assinatura do termo, os dados biométricos/assinatura digital são obrigatórios.");
        }

        FichaAnamnese ficha = FichaAnamnese.builder()
                .cliente(cliente)
                .agendamento(agendamento)
                .historicoSaude(dto.getHistoricoSaude())
                .alergias(dto.getAlergias())
                .termoAssinado(Boolean.TRUE.equals(dto.getTermoAssinado()))
                .assinaturaDigital(dto.getAssinaturaDigital())
                .diabetes(Boolean.TRUE.equals(dto.getDiabetes()))
                .hemofilia(Boolean.TRUE.equals(dto.getHemofilia()))
                .hipertensao(Boolean.TRUE.equals(dto.getHipertensao()))
                .gestanteLactante(Boolean.TRUE.equals(dto.getGestanteLactante()))
                .medicamentosUso(dto.getMedicamentosUso())
                .observacoes(dto.getObservacoes())
                .dataCriacao(LocalDateTime.now())
                .build();

        // Avaliação automática de alertas clínicos
        ficha.avaliarAlertasClinicos();

        FichaAnamnese salva = fichaRepository.save(ficha);
        return FichaAnamneseResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public FichaAnamneseResponseDTO buscarPorId(Long id) {
        FichaAnamnese ficha = fichaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha de anamnese não encontrada com ID: " + id));
        return FichaAnamneseResponseDTO.fromEntity(ficha);
    }

    @Transactional(readOnly = true)
    public List<FichaAnamneseResponseDTO> buscarPorCliente(Long clienteId) {
        return fichaRepository.findByClienteId(clienteId).stream()
                .map(FichaAnamneseResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public FichaAnamneseResponseDTO buscarPorAgendamento(Long agendamentoId) {
        FichaAnamnese ficha = fichaRepository.findByAgendamentoId(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha de anamnese não encontrada para o agendamento ID: " + agendamentoId));
        return FichaAnamneseResponseDTO.fromEntity(ficha);
    }

    @Transactional(readOnly = true)
    public List<FichaAnamneseResponseDTO> listarAlertasClinicos() {
        return fichaRepository.findByAlertaClinicoTrue().stream()
                .map(FichaAnamneseResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FichaAnamneseResponseDTO> listarTodas() {
        return fichaRepository.findAll().stream()
                .map(FichaAnamneseResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean validarAptidaoCliente(Long clienteId) {
        List<FichaAnamnese> fichas = fichaRepository.findByClienteId(clienteId);
        if (fichas.isEmpty()) {
            return false;
        }
        // Retorna false se houver alerta clínico ativo
        return fichas.stream().noneMatch(f -> Boolean.TRUE.equals(f.getAlertaClinico()));
    }
}
