package com.tattos.repository;

import com.tattos.model.TrabalhoRealizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrabalhoRealizadoRepository extends JpaRepository<TrabalhoRealizado, Long> {

    List<TrabalhoRealizado> findByClienteIdOrderByDataTrabalhoDesc(Long clienteId);
}
