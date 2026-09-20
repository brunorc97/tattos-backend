package com.tattos.controller;

import com.tattos.dto.request.ArtistRequestDTO;
import com.tattos.dto.response.ArtistResponseDTO;
import com.tattos.model.enums.ArtistTag;
import com.tattos.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    public ResponseEntity<List<ArtistResponseDTO>> listAll(@RequestParam(required = false) ArtistTag tag) {
        return ResponseEntity.ok(artistService.findAll(tag));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.findResponseById(id));
    }

    @PostMapping
    public ResponseEntity<ArtistResponseDTO> create(@Valid @RequestBody ArtistRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(artistService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArtistResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ArtistRequestDTO dto) {
        return ResponseEntity.ok(artistService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
