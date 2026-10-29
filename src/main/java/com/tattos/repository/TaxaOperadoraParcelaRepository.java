package com.tattos.repository;

import com.tattos.model.TaxaOperadoraParcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxaOperadoraParcelaRepository extends JpaRepository<TaxaOperadoraParcela, Long> {

    Optional<TaxaOperadoraParcela> findByTaxaOperadoraIdAndParcelas(Long taxaOperadoraId, Integer parcelas);
}
