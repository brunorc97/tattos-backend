package com.tattos.repository;

import com.tattos.model.AssinaturaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssinaturaLogRepository extends JpaRepository<AssinaturaLog, Long> {
    // No custom methods needed now
}
