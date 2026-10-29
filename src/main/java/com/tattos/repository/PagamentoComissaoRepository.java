package com.tattos.repository;

import com.tattos.model.PagamentoComissao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagamentoComissaoRepository extends JpaRepository<PagamentoComissao, Long> {

    Page<PagamentoComissao> findByTatuadorIdOrderByCreatedAtDesc(Long tatuadorId, Pageable pageable);

    Page<PagamentoComissao> findByTatuadorIdAndStatusOrderByCreatedAtDesc(
            Long tatuadorId, PagamentoComissao.Status status, Pageable pageable);
}
