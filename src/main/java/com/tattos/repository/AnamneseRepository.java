package com.tattos.repository;

import com.tattos.model.Anamnese;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnamneseRepository extends JpaRepository<Anamnese, Long> {
    Optional<Anamnese> findByCode(String code);
    List<Anamnese> findByCpfOrderByDateDesc(String cpf);
    List<Anamnese> findAllByOrderByDateDescCreatedAtDesc();
}
