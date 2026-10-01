package com.tattos.controller;

import com.tattos.dto.response.UsuarioResponseDTO;
import com.tattos.model.enums.UserRole;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/usuarios", "/api/artists", "/api/tatuadores"})
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

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
