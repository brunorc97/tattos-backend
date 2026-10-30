package com.tattos.repository;

import com.tattos.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    @org.springframework.data.jpa.repository.Query("""
            SELECT p FROM Projeto p
            WHERE (:tatuadorId IS NULL OR p.tatuador.id = :tatuadorId)
              AND (:status IS NULL OR p.statusAprovacao = :status)
              AND (:clienteId IS NULL OR p.cliente.id = :clienteId)
            ORDER BY p.dataCriacao DESC
            """)
    java.util.List<Projeto> buscarComFiltros(
            @org.springframework.data.repository.query.Param("tatuadorId") Long tatuadorId,
            @org.springframework.data.repository.query.Param("status") com.tattos.model.enums.StatusAprovacaoProjeto status,
            @org.springframework.data.repository.query.Param("clienteId") Long clienteId);

    /** Fila do tatuador: pendentes primeiro, depois os mais antigos. */
    @org.springframework.data.jpa.repository.Query("""
            SELECT p FROM Projeto p
            WHERE (:tatuadorId IS NULL OR p.tatuador.id = :tatuadorId)
            ORDER BY CASE WHEN p.statusAprovacao = com.tattos.model.enums.StatusAprovacaoProjeto.PENDENTE THEN 0 ELSE 1 END,
                     p.dataCriacao ASC
            """)
    java.util.List<Projeto> filaPorTatuador(
            @org.springframework.data.repository.query.Param("tatuadorId") Long tatuadorId);
}
