package com.tattos.controller;

import com.tattos.dto.request.RegisterRequestDTO;
import com.tattos.dto.response.UsuarioResponseDTO;
import com.tattos.model.enums.UserRole;
import com.tattos.repository.UsuarioRepository;
import com.tattos.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuários", description = "Gestão de usuários e cadastro de equipe")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/v1/usuarios", "/api/v1/artists", "/api/v1/tatuadores", "/api/usuarios", "/api/artists", "/api/tatuadores"})
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    @Operation(summary = "Cadastrar funcionário (Gestor ou Tatuador)", description = "Permite que um gestor autenticado cadastre novos funcionários no sistema.")
    @ApiResponse(responseCode = "201", description = "Funcionário cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Acesso negado (exige perfil de GESTOR)")
    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<UsuarioResponseDTO> cadastrarFuncionario(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarFuncionario(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios(
            @RequestParam(required = false) UserRole role) {
        List<UsuarioResponseDTO> usuarios = (role != null ? usuarioRepository.findByRole(role) : usuarioRepository.findAll())
                .stream()
                .map(UsuarioResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping({"/tatuadores", "/residents", "/guests"})
    public ResponseEntity<List<UsuarioResponseDTO>> listarTatuadores() {
        List<UsuarioResponseDTO> tatuadores = usuarioRepository.findTatuadoresAtivos()
                .stream()
                .map(UsuarioResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(tatuadores);
    }

    @GetMapping("/tatuadores/sugerir")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UsuarioResponseDTO>> sugerirPorEstilo(@RequestParam String estilo) {
        return ResponseEntity.ok(usuarioRepository
                .findTatuadoresPorEstilo(com.tattos.model.enums.UserRole.ROLE_TATUADOR, estilo.trim())
                .stream().map(UsuarioResponseDTO::fromEntity).toList());
    }

    @GetMapping("/clientes")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<org.springframework.data.domain.Page<UsuarioResponseDTO>> listarClientes(
            @RequestParam(required = false) String query,
            @org.springframework.data.web.PageableDefault(size = 20, sort = "nome") org.springframework.data.domain.Pageable pageable,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario logado) {
        String q = (query != null && !query.isBlank()) ? query.trim() : null;
        String digits = q != null && q.matches(".*\\d.*") ? q.replaceAll("\\D", "") : null;

        // Tatuador: apenas clientes com agendamento com ele
        if (logado != null && logado.isTatuador()) {
            return ResponseEntity.ok(
                    usuarioRepository.buscarClientesDoTatuador(logado.getId(), q, digits, pageable)
                            .map(UsuarioResponseDTO::fromEntity));
        }
        if (q == null) {
            return ResponseEntity.ok(usuarioRepository.findClientesAtivos(pageable)
                    .map(UsuarioResponseDTO::fromEntity));
        }
        return ResponseEntity.ok(usuarioRepository.buscarClientes(q, digits, pageable)
                .map(UsuarioResponseDTO::fromEntity));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
