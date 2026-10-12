package com.tattos.repository;

import com.tattos.model.GoogleAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GoogleAccountRepository extends JpaRepository<GoogleAccount, Long> {
    Optional<GoogleAccount> findByUsuarioId(Long usuarioId);
    Optional<GoogleAccount> findByGoogleUserId(String googleUserId);
}
