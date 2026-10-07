package com.tattos.service;

import com.tattos.dto.request.LoginRequestDTO;
import com.tattos.dto.request.RegisterRequestDTO;
import com.tattos.dto.response.LoginResponseDTO;
import com.tattos.dto.response.UsuarioResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ConflictException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Cliente;
import com.tattos.model.Gestor;
import com.tattos.model.Tatuador;
import com.tattos.model.Usuario;
import com.tattos.model.enums.UserRole;
import com.tattos.repository.UsuarioRepository;
import com.tattos.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UsuarioResponseDTO register(RegisterRequestDTO request) {
        UserRole role = request.getRole() != null ? request.getRole() : UserRole.ROLE_CLIENTE;

        // Impede o cadastro público de gestores e tatuadores por esta rota[span_2](start_span)[span_2](end_span)
        if (role == UserRole.ROLE_GESTOR || role == UserRole.ROLE_TATUADOR) {
            throw new BusinessException("Cadastros de gestores e tatuadores devem ser realizados por um gestor autenticado.");
        }

        return processarRegistro(request, role);
    }

    @Transactional
    public UsuarioResponseDTO registrarFuncionario(RegisterRequestDTO request) {
        UserRole role = request.getRole() != null ? request.getRole() : UserRole.ROLE_CLIENTE;
        return processarRegistro(request, role);
    }

    private UsuarioResponseDTO processarRegistro(RegisterRequestDTO request, UserRole role) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        if (email == null || email.isEmpty()) {
            throw new BusinessException("O email é obrigatório.");
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictException("Já existe um usuário cadastrado com este e-mail.");
        }

        String nome = request.getResolvedNome();
        if (nome.isEmpty()) {
            throw new BusinessException("O nome é obrigatório.");
        }

        String rawSenha = request.getResolvedSenha();
        if (rawSenha.isEmpty()) {
            throw new BusinessException("A senha é obrigatória.");
        }

        Usuario usuario = switch (role) {
            case ROLE_TATUADOR -> Tatuador.builder()
                    .especialidade(request.getResolvedEspecialidade())
                    .bio(request.getBio())
                    .comissaoPercentual(request.getComissaoPercentual())
                    .build();
            case ROLE_GESTOR -> Gestor.builder()
                    .cargo(request.getCargo() != null ? request.getCargo() : "Gestor")
                    .build();
            case ROLE_CLIENTE -> Cliente.builder().build();
        };

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(rawSenha));
        usuario.setTelefone(request.getResolvedTelefone());
        usuario.setEndereco(request.getResolvedEndereco());
        usuario.setAtivo(true);

        Usuario salvo = usuarioRepository.save(usuario);
        return UsuarioResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        String identifier = request.getLoginIdentifier();
        String rawPassword = request.getRawPassword();

        if (identifier.isEmpty() || rawPassword.isEmpty()) {
            throw new BadCredentialsException("Email/usuário e senha são obrigatórios.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, rawPassword)
        );

        Usuario usuario = usuarioRepository.findByEmail(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        String token = jwtTokenProvider.generateToken(authentication);

        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .tempoExpiracaoMs(jwtTokenProvider.getExpirationTime())
                .usuario(UsuarioResponseDTO.fromEntity(usuario))
                .build();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO getCurrentUser(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o e-mail: " + email));
        return UsuarioResponseDTO.fromEntity(usuario);
    }
}
