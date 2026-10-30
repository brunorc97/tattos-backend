package com.tattos.repository;

import com.tattos.model.Bancada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BancadaRepository extends JpaRepository<Bancada, Long> {

    Optional<Bancada> findByNome(String nome);

    boolean existsByNome(String nome);
}
