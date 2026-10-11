package com.tattos.repository;

import com.tattos.model.Usuario;
import com.tattos.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByCodigoIndicacao(String codigoIndicacao);

    boolean existsByCodigoIndicacao(String codigoIndicacao);

    List<Usuario> findByRole(UserRole role);

    List<Usuario> findByRoleAndAtivoTrue(UserRole role);

    @Query("SELECT u FROM Usuario u WHERE u.role = com.tattos.model.enums.UserRole.ROLE_TATUADOR AND u.ativo = true")
    List<Usuario> findTatuadoresAtivos();

    @Query("SELECT u FROM Usuario u WHERE u.role = com.tattos.model.enums.UserRole.ROLE_CLIENTE AND u.ativo = true")
    List<Usuario> findClientesAtivos();
}
