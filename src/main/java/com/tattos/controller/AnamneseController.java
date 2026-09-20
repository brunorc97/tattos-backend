package com.tattos.controller;

import com.tattos.dto.request.AnamneseRequestDTO;
import com.tattos.dto.response.AnamneseResponseDTO;
import com.tattos.service.AnamneseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anamnese")
@RequiredArgsConstructor
public class AnamneseController {

    private final AnamneseService anamneseService;

    @GetMapping
    public ResponseEntity<List<AnamneseResponseDTO>> listAll() {
        return ResponseEntity.ok(anamneseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnamneseResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(anamneseService.findById(id));
    }

    @GetMapping("/client/{cpf}")
    public ResponseEntity<List<AnamneseResponseDTO>> getByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(anamneseService.findByCpf(cpf));
    }

    @PostMapping
    public ResponseEntity<AnamneseResponseDTO> create(@Valid @RequestBody AnamneseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anamneseService.create(dto));
    }
}
