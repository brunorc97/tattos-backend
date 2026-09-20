package com.tattos.service;

import com.tattos.dto.request.LoginRequestDTO;
import com.tattos.dto.request.RegisterRequestDTO;
import com.tattos.dto.response.LoginResponseDTO;
import com.tattos.dto.response.UserResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.User;
import com.tattos.repository.UserRepository;
import com.tattos.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + request.username()));

        return LoginResponseDTO.of(token, UserResponseDTO.fromEntity(user));
    }

    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Nome de usuário já em uso: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("E-mail já cadastrado: " + request.email());
        }

        User user = User.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .name(request.name())
            .email(request.email())
            .role(request.role())
            .active(true)
            .build();

        User saved = userRepository.save(user);
        return UserResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + username));
        return UserResponseDTO.fromEntity(user);
    }
}
