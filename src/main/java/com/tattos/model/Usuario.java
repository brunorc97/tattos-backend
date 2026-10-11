package com.tattos.model;

import com.tattos.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("ROLE_USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email em formato inválido")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Column(nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", insertable = false, updatable = false)
    protected UserRole role;

    @Column(length = 30)
    private String telefone;

    @Column(length = 255)
    private String endereco;

    // Atributos específicos de Tatuador
    @Column(length = 100)
    private String especialidade;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // Atributos específicos de Gestor
    @Column(length = 100)
    private String cargo;

    @Builder.Default
    @Column(name = "comissao_percentual", precision = 5, scale = 2)
    private BigDecimal comissaoPercentual = new BigDecimal("70.00");

    @Column(name = "codigo_indicacao", length = 20, unique = true)
    private String codigoIndicacao;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PostLoad
    public void syncRole() {
        if (this instanceof Tatuador) {
            this.role = UserRole.ROLE_TATUADOR;
        } else if (this instanceof Gestor) {
            this.role = UserRole.ROLE_GESTOR;
        } else if (this instanceof Cliente) {
            this.role = UserRole.ROLE_CLIENTE;
        } else if (this.role == null) {
            this.role = UserRole.ROLE_CLIENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Usuario criarPorRole(UserRole role) {
        if (role == null) {
            return new Cliente();
        }
        return switch (role) {
            case ROLE_GESTOR -> new Gestor();
            case ROLE_TATUADOR -> new Tatuador();
            case ROLE_CLIENTE -> new Cliente();
        };
    }

    // Métodos utilitários de perfil
    @Transient
    public boolean isGestor() {
        return UserRole.ROLE_GESTOR.equals(this.getRole());
    }

    @Transient
    public boolean isTatuador() {
        return UserRole.ROLE_TATUADOR.equals(this.getRole());
    }

    @Transient
    public boolean isCliente() {
        return UserRole.ROLE_CLIENTE.equals(this.getRole());
    }

    // Implementação de UserDetails do Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        UserRole currentRole = getRole() != null ? getRole() : UserRole.ROLE_CLIENTE;
        return List.of(new SimpleGrantedAuthority(currentRole.name()));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.ativo;
    }
}
