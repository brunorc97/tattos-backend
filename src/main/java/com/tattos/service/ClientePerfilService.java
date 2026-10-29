package com.tattos.service;

import com.tattos.dto.response.ClientePerfilDTO;
import com.tattos.dto.response.UsuarioResponseDTO;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.ContatoCliente;
import com.tattos.model.FichaAnamnese;
import com.tattos.model.Projeto;
import com.tattos.model.Usuario;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.model.enums.StatusAprovacaoProjeto;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.ContatoClienteRepository;
import com.tattos.repository.FichaAnamneseRepository;
import com.tattos.repository.ProjetoRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Agregação read-only do perfil 360° do cliente a partir das fontes existentes
 * (agendamentos, projetos, fichas, contatos CRM).
 */
@Service
@RequiredArgsConstructor
public class ClientePerfilService {

    private final UsuarioRepository usuarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ProjetoRepository projetoRepository;
    private final FichaAnamneseRepository fichaRepository;
    private final ContatoClienteRepository contatoRepository;

    @Transactional(readOnly = true)
    public ClientePerfilDTO montar(Long clienteId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .filter(Usuario::isCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + clienteId));

        long totalTrabalhos = agendamentoRepository
                .countByClienteIdAndStatus(clienteId, StatusAgendamento.REALIZADO);

        List<Projeto> projetos = projetoRepository.buscarComFiltros(null, null, clienteId);
        List<ClientePerfilDTO.ProjetoRecusado> recusados = projetos.stream()
                .filter(p -> p.getStatusAprovacao() == StatusAprovacaoProjeto.NAO_APROVADO)
                .map(p -> ClientePerfilDTO.ProjetoRecusado.builder()
                        .id(p.getId())
                        .nome(p.getNome())
                        .motivoRecusa(p.getMotivoRecusa() != null ? p.getMotivoRecusa().name() : null)
                        .motivoRecusaDetalhe(p.getMotivoRecusaDetalhe())
                        .build())
                .toList();
        long aprovados = projetos.stream()
                .filter(p -> p.getStatusAprovacao() == StatusAprovacaoProjeto.APROVADO).count();

        List<FichaAnamnese> fichas = fichaRepository.findByClienteIdOrderByDataCriacaoDesc(clienteId);
        String alergias = fichas.stream()
                .filter(f -> f.getAlergias() != null && !f.getAlergias().isBlank())
                .findFirst() // mais recente primeiro
                .map(FichaAnamnese::getAlergias)
                .orElse(null);

        List<ContatoCliente> contatos = contatoRepository.findTop5ByClienteIdOrderByDataContatoDesc(clienteId);

        LocalDate proximoAniversario = null;
        if (cliente.getDataNascimento() != null) {
            LocalDate hoje = LocalDate.now();
            LocalDate desteAno = cliente.getDataNascimento().withYear(hoje.getYear());
            proximoAniversario = desteAno.isBefore(hoje) ? desteAno.plusYears(1) : desteAno;
        }

        return ClientePerfilDTO.builder()
                .cliente(UsuarioResponseDTO.fromEntity(cliente))
                .totalTrabalhos(totalTrabalhos)
                .projetos(ClientePerfilDTO.ProjetosResumo.builder()
                        .total(projetos.size())
                        .aprovados(aprovados)
                        .naoAprovados(recusados)
                        .build())
                .ultimasFichas(fichas.stream().limit(5).map(f -> ClientePerfilDTO.FichaResumo.builder()
                        .id(f.getId())
                        .data(f.getDataCriacao())
                        .alertaClinico(Boolean.TRUE.equals(f.getAlertaClinico()))
                        .build()).toList())
                .alergias(alergias)
                .ultimosContatos(contatos.stream().map(ct -> ClientePerfilDTO.ContatoResumo.builder()
                        .id(ct.getId())
                        .tipo(ct.getTipo().name())
                        .resumo(ct.getResumo())
                        .dataContato(ct.getDataContato())
                        .build()).toList())
                .proximoAniversario(proximoAniversario)
                .build();
    }
}
