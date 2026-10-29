package com.tattos.repository;

import com.tattos.model.ContatoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContatoClienteRepository extends JpaRepository<ContatoCliente, Long> {

    Page<ContatoCliente> findByClienteIdOrderByDataContatoDesc(Long clienteId, Pageable pageable);

    List<ContatoCliente> findTop5ByClienteIdOrderByDataContatoDesc(Long clienteId);
}
