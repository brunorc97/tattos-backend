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

    @GetMapping("/clientes")
    public ResponseEntity<List<UsuarioResponseDTO>> listarClientes() {
        List<UsuarioResponseDTO> clientes = usuarioRepository.findClientesAtivos()
                .stream()
                .map(UsuarioResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
