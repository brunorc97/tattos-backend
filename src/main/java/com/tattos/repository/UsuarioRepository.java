package com.tattos.repository;

import com.tattos.model.Usuario;
import com.tattos.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT u FROM Usuario u WHERE u.role = :role AND u.ativo = true "
            + "AND LOWER(u.especialidade) LIKE LOWER(CONCAT('%', :estilo, '%'))")
    List<Usuario> findTatuadoresPorEstilo(@Param("role") UserRole role, @Param("estilo") String estilo);

    @Query("SELECT u FROM Usuario u WHERE u.role = com.tattos.model.enums.UserRole.ROLE_CLIENTE AND u.ativo = true")
    List<Usuario> findClientesAtivos();

    @Query("SELECT u FROM Usuario u WHERE u.role = com.tattos.model.enums.UserRole.ROLE_CLIENTE AND u.ativo = true")
    org.springframework.data.domain.Page<Usuario> findClientesAtivos(org.springframework.data.domain.Pageable pageable);

    /** Clientes ativos com pelo menos um agendamento com o tatuador informado. */
    @Query("""
            SELECT u FROM Usuario u
            WHERE u.role = com.tattos.model.enums.UserRole.ROLE_CLIENTE
              AND u.ativo = true
              AND EXISTS (SELECT a FROM Agendamento a WHERE a.cliente.id = u.id AND a.tatuador.id = :tatuadorId)
              AND (:query IS NULL
                   OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR (:queryDigits IS NOT NULL
                       AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(u.telefone, '(', ''), ')', ''), '-', ''), ' ', ''), '.', '')
                          LIKE CONCAT('%', :queryDigits, '%')))
            """)
    org.springframework.data.domain.Page<Usuario> buscarClientesDoTatuador(
            @Param("tatuadorId") Long tatuadorId,
            @Param("query") String query,
            @Param("queryDigits") String queryDigits,
            org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT u FROM Usuario u
            WHERE u.role = com.tattos.model.enums.UserRole.ROLE_CLIENTE
              AND u.ativo = true
              AND (:query IS NOT NULL
                   AND (LOWER(u.nome) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR (:queryDigits IS NOT NULL
                            AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(u.telefone, '(', ''), ')', ''), '-', ''), ' ', ''), '.', '')
                               LIKE CONCAT('%', :queryDigits, '%'))))
            """)
    org.springframework.data.domain.Page<Usuario> buscarClientes(
            @Param("query") String query,
            @Param("queryDigits") String queryDigits,
            org.springframework.data.domain.Pageable pageable);
}
