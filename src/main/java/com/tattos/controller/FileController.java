package com.tattos.controller;

import com.tattos.model.Arquivo;
import com.tattos.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "Arquivos", description = "Upload e download de comprovantes, recibos e fotos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "Enviar arquivo", description = "Aceita PDF, PNG ou JPEG até 10MB.")
    @ApiResponse(responseCode = "201", description = "Arquivo armazenado")
    @ApiResponse(responseCode = "400", description = "Tipo não permitido ou arquivo vazio")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                      @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario usuario) {
        Arquivo arquivo = fileStorageService.salvar(file, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", arquivo.getId(),
                "nomeOriginal", arquivo.getNomeOriginal(),
                "contentType", arquivo.getContentType(),
                "tamanhoBytes", arquivo.getTamanhoBytes(),
                "url", "/api/v1/files/" + arquivo.getId()
        ));
    }

    @Operation(summary = "Baixar arquivo")
    @ApiResponse(responseCode = "200", description = "Conteúdo do arquivo")
    @ApiResponse(responseCode = "404", description = "Arquivo não encontrado")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario usuario) {
        Arquivo arquivo = fileStorageService.buscar(id);
        boolean isGestor = usuario != null && usuario.getRole() == com.tattos.model.enums.UserRole.ROLE_GESTOR;
        boolean isDono = usuario != null && arquivo.getDono() != null
                && arquivo.getDono().getId().equals(usuario.getId());
        if (!isGestor && !isDono) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Resource resource = fileStorageService.carregarComoRecurso(arquivo);
        String nomeSeguro = arquivo.getNomeOriginal().replace("\"", "");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + nomeSeguro + "\"; filename*=UTF-8''"
                                + java.net.URLEncoder.encode(arquivo.getNomeOriginal(), java.nio.charset.StandardCharsets.UTF_8))
                .body(resource);
    }
}
