package com.tattos.repository;

import com.tattos.model.Agendamento;
import com.tattos.model.enums.StatusAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByTatuadorId(Long tatuadorId);

    List<Agendamento> findByClienteId(Long clienteId);

    List<Agendamento> findByStatus(StatusAgendamento status);

    long countByStatus(StatusAgendamento status);

    long countBySinalPagoTrue();

    long countByValorSinalGreaterThan(java.math.BigDecimal valor);

    List<Agendamento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT a FROM Agendamento a WHERE a.status IN :statusList " +
           "AND a.lembreteEnviado = false " +
           "AND a.dataHora BETWEEN :inicio AND :fim")
    List<Agendamento> findAgendamentosPendentesLembrete(
            @Param("statusList") List<StatusAgendamento> statusList,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    @Query("SELECT a FROM Agendamento a WHERE " +
           "(:tatuadorId IS NULL OR a.tatuador.id = :tatuadorId) AND " +
           "(:clienteId IS NULL OR a.cliente.id = :clienteId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:dataInicio IS NULL OR a.dataHora >= :dataInicio) AND " +
           "(:dataFim IS NULL OR a.dataHora <= :dataFim) " +
           "ORDER BY a.dataHora ASC")
    List<Agendamento> findComFiltros(
            @Param("tatuadorId") Long tatuadorId,
            @Param("clienteId") Long clienteId,
            @Param("status") StatusAgendamento status,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT a FROM Agendamento a WHERE " +
           "(:tatuadorId IS NULL OR a.tatuador.id = :tatuadorId) AND " +
           "(:clienteId IS NULL OR a.cliente.id = :clienteId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:dataInicio IS NULL OR a.dataHora >= :dataInicio) AND " +
           "(:dataFim IS NULL OR a.dataHora <= :dataFim) " +
           "ORDER BY a.dataHora ASC")
    Page<Agendamento> findComFiltrosPaginado(
            @Param("tatuadorId") Long tatuadorId,
            @Param("clienteId") Long clienteId,
            @Param("status") StatusAgendamento status,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);

    /**
     * Verifica se existe conflito de horário para o tatuador no intervalo informado.
     * Considera conflito quando: inicio < existenteFim E fim > existenteInicio.
     */
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.status != com.tattos.model.enums.StatusAgendamento.CANCELADO " +
           "AND (:id IS NULL OR a.id != :id) " +
           "AND a.tatuador.id = :tatuadorId " +
           "AND (a.dataHora < :fim AND a.dataHoraFim > :inicio)")
    boolean existeConflitoTatuador(
            @Param("tatuadorId") Long tatuadorId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("id") Long id);

    /**
     * Verifica se a cabine já está ocupada no intervalo informado.
     */
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.status != com.tattos.model.enums.StatusAgendamento.CANCELADO " +
           "AND (:id IS NULL OR a.id != :id) " +
           "AND LOWER(a.cabine) = LOWER(:cabine) " +
           "AND (a.dataHora < :fim AND a.dataHoraFim > :inicio)")
    boolean existeConflitoCabine(
            @Param("cabine") String cabine,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("id") Long id);
}
