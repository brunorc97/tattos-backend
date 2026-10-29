package com.tattos.repository;

import com.tattos.model.SolicitacaoAgendamento;
import com.tattos.model.enums.StatusSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitacaoAgendamentoRepository extends JpaRepository<SolicitacaoAgendamento, Long> {

    Page<SolicitacaoAgendamento> findByClienteId(Long clienteId, Pageable pageable);

    Page<SolicitacaoAgendamento> findByTatuadorIdOrTatuadorIsNull(Long tatuadorId, Pageable pageable);

    Page<SolicitacaoAgendamento> findByStatus(StatusSolicitacao status, Pageable pageable);
}
