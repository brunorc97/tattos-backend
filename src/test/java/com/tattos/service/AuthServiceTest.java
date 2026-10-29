package com.tattos.service;

import com.tattos.dto.request.LoginRequestDTO;
import com.tattos.dto.request.RegisterRequestDTO;
import com.tattos.dto.response.LoginResponseDTO;
import com.tattos.dto.response.UsuarioResponseDTO;
import com.tattos.exception.ConflictException;
import com.tattos.model.enums.UserRole;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve registrar cliente com data de nascimento e retorná-la no DTO")
    void deveRegistrarClienteComDataNascimento() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .nome("Cliente Nascimento")
                .email("nascimento@tattos.com")
                .senha("segredo123")
                .role(UserRole.ROLE_CLIENTE)
                .dataNascimento(java.time.LocalDate.of(1990, 5, 17))
                .build();

        UsuarioResponseDTO response = authService.register(request);

        assertNotNull(response.getId());
        assertEquals(java.time.LocalDate.of(1990, 5, 17), response.getDataNascimento());
    }

    @Test
    @DisplayName("Deve registrar um tatuador com perfil e dados específicos com sucesso")
    void deveRegistrarTatuadorComSucesso() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .nome("Rodrigo Sanches")
                .email("rodrigo@tattos.com")
                .senha("segredo123")
                .role(UserRole.ROLE_TATUADOR)
                .telefone("(11) 98765-4321")
                .especialidade("Lettering & Realismo")
                .bio("Especialista em caligrafia")
                .comissaoPercentual(new BigDecimal("70.00"))
                .build();

        UsuarioResponseDTO response = authService.registrarFuncionario(request);

        assertNotNull(response.getId());
        assertEquals("Rodrigo Sanches", response.getNome());
        assertEquals("rodrigo@tattos.com", response.getEmail());
        assertEquals(UserRole.ROLE_TATUADOR, response.getRole());
        assertEquals("Lettering & Realismo", response.getEspecialidade());
        assertEquals(new BigDecimal("70.00"), response.getComissaoPercentual());
    }

    @Test
    @DisplayName("Tatuador registrado NÃO deve receber código de indicação")
    void tatuadorNaoRecebeCodigoIndicacao() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .nome("Sem Codigo")
                .email("semcodigo@tattos.com")
                .senha("segredo123")
                .role(UserRole.ROLE_TATUADOR)
                .comissaoPercentual(new BigDecimal("60.00"))
                .build();

        UsuarioResponseDTO response = authService.registrarFuncionario(request);

        assertNull(response.getCodigoIndicacao());
    }

    @Test
    @DisplayName("Deve gerar código único de indicação automaticamente para novo cliente")
    void deveGerarCodigoIndicacaoParaNovoCliente() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .nome("Maria Silva")
                .email("maria.silva@tattos.com")
                .senha("segredo123")
                .role(UserRole.ROLE_CLIENTE)
                .telefone("(11) 91234-5678")
                .build();

        UsuarioResponseDTO response = authService.register(request);

        assertNotNull(response.getId());
        assertNotNull(response.getCodigoIndicacao());
        assertTrue(response.getCodigoIndicacao().startsWith("TATTO-"));
        assertTrue(usuarioRepository.existsByCodigoIndicacao(response.getCodigoIndicacao()));
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com e-mail já existente")
    void deveLancarExcecaoQuandoEmailDuplicado() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .nome("Usuario Um")
                .email("duplicado@tattos.com")
                .senha("pass123")
                .role(UserRole.ROLE_CLIENTE)
                .build();

        authService.register(request);

        RegisterRequestDTO request2 = RegisterRequestDTO.builder()
                .nome("Usuario Dois")
                .email("duplicado@tattos.com")
                .senha("outrapass")
                .role(UserRole.ROLE_CLIENTE)
                .build();

        assertThrows(ConflictException.class, () -> authService.register(request2));
    }

    @Test
    @DisplayName("Deve realizar login com credenciais válidas e retornar token JWT")
    void deveFazerLoginComSucesso() {
        RegisterRequestDTO reg = RegisterRequestDTO.builder()
                .nome("Admin Teste")
                .email("admin.teste@tattos.com")
                .senha("adminPass123")
                .role(UserRole.ROLE_GESTOR)
                .cargo("Diretor Geral")
                .build();

        authService.registrarFuncionario(reg);

        LoginRequestDTO login = LoginRequestDTO.builder()
                .email("admin.teste@tattos.com")
                .senha("adminPass123")
                .build();

        LoginResponseDTO response = authService.login(login);

        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertNotNull(response.getUsuario());
        assertEquals("admin.teste@tattos.com", response.getUsuario().getEmail());
        assertEquals(UserRole.ROLE_GESTOR, response.getUsuario().getRole());
    }
}
