package com.tattos.repository;

import com.tattos.model.Insumo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    @Query("SELECT i FROM Insumo i WHERE i.quantidadeAtual <= i.nivelCritico")
    List<Insumo> findInsumosAbaixoNivelCritico();

    List<Insumo> findByCategoriaIgnoreCase(String categoria);

    List<Insumo> findByNomeContainingIgnoreCase(String nome);

    Page<Insumo> findByCategoriaIgnoreCase(String categoria, Pageable pageable);

    Page<Insumo> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Optional<Insumo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
