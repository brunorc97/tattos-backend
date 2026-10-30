package com.tattos.repository;

import com.tattos.model.StudioInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudioInfoRepository extends JpaRepository<StudioInfo, Long> {

    Optional<StudioInfo> findFirstByOrderByIdAsc();
}
