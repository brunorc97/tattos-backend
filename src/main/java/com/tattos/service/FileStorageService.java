package com.tattos.service;

import com.tattos.exception.BusinessException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Arquivo;
import com.tattos.repository.ArquivoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Armazenamento simples em disco dos arquivos enviados (comprovantes, recibos,
 * fotos de trabalhos). Metadados na tabela arquivos; binário em upload-dir.
 */
@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "application/pdf", "image/png", "image/jpeg"
    );

    private final ArquivoRepository arquivoRepository;
    private final Path uploadDir;

    public FileStorageService(ArquivoRepository arquivoRepository,
                              @Value("${app.storage.upload-dir:./uploads}") String uploadDir) throws IOException {
        this.arquivoRepository = arquivoRepository;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    @Transactional
    public Arquivo salvar(MultipartFile file, com.tattos.model.Usuario dono) {
        if (file.isEmpty()) {
            throw new BusinessException("O arquivo enviado está vazio.");
        }
        if (!TIPOS_PERMITIDOS.contains(file.getContentType())) {
            throw new BusinessException("Tipo de arquivo não permitido: " + file.getContentType()
                    + ". Aceitos: PDF, PNG, JPEG.");
        }

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "arquivo";
        String extensao = original.contains(".")
                ? original.substring(original.lastIndexOf('.')).toLowerCase().replaceAll("[^a-z0-9.]", "")
                : "";
        String nomeArmazenado = UUID.randomUUID() + extensao;
        Path destino = uploadDir.resolve(nomeArmazenado).normalize();
        if (!destino.startsWith(uploadDir)) {
            throw new BusinessException("Nome de arquivo inválido.");
        }

        try {
            Files.copy(file.getInputStream(), destino);
        } catch (IOException e) {
            log.error("Falha ao salvar arquivo {}", file.getOriginalFilename(), e);
            throw new BusinessException("Falha ao armazenar o arquivo.");
        }

        return arquivoRepository.save(Arquivo.builder()
                .nomeOriginal(original)
                .contentType(file.getContentType())
                .tamanhoBytes(file.getSize())
                .caminho(destino.toString())
                .dono(dono)
                .build());
    }

    @Transactional(readOnly = true)
    public Arquivo buscar(Long id) {
        return arquivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arquivo não encontrado com ID: " + id));
    }

    public Resource carregarComoRecurso(Arquivo arquivo) {
        Path path = Paths.get(arquivo.getCaminho());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Arquivo físico não encontrado: " + arquivo.getNomeOriginal());
        }
        return new FileSystemResource(path);
    }
}
