package com.tattos.repository;

import com.tattos.model.TaxaOperadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxaOperadoraRepository extends JpaRepository<TaxaOperadora, Long> {

    List<TaxaOperadora> findByAtivoTrue();

    Optional<TaxaOperadora> findByOperadoraAndModalidade(String operadora, com.tattos.model.enums.ModalidadePagamento modalidade);
}
