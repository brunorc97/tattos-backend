package com.tattos.exception;

import com.tattos.dto.response.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que violações de integridade são traduzidas em respostas limpas:
 * duplicidade → 409, exclusão com vínculo (FK) → 422, sem vazar mensagem
 * bruta do banco (nome de constraint, "Duplicate entry", etc.).
 */
class DataIntegrityHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        handler = new GlobalExceptionHandler(messageSource);
        request = new MockHttpServletRequest();
    }

    @Test
    @DisplayName("Chave duplicada deve retornar 409 sem vazar detalhes do banco")
    void duplicidadeRetorna409SemVazarDetalhes() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement [Duplicate entry 'user@x.com' for key 'usuario.UK_email']");

        ResponseEntity<ErrorResponseDTO> response = handler.handleDataIntegrity(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).doesNotContain("Duplicate entry", "UK_email", "usuario");
    }

    @Test
    @DisplayName("Exclusão de registro com vínculo (FK) deve retornar 422")
    void exclusaoComVinculoRetorna422() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Cannot delete or update a parent row: a foreign key constraint fails (`tattos`.`agendamento`, CONSTRAINT `FK_tatuador`)");

        ResponseEntity<ErrorResponseDTO> response = handler.handleDataIntegrity(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(response.getBody().getMessage()).doesNotContain("FK_tatuador", "constraint fails");
    }

    @Test
    @DisplayName("Violação genérica deve retornar 409 com mensagem padrão")
    void violacaoGenericaRetorna409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("alguma outra violacao");

        ResponseEntity<ErrorResponseDTO> response = handler.handleDataIntegrity(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @DisplayName("Upload acima do limite deve retornar 413 com mensagem amigável")
    void uploadMuitoGrandeRetorna413() {
        org.springframework.web.multipart.MaxUploadSizeExceededException ex =
                new org.springframework.web.multipart.MaxUploadSizeExceededException(10_485_760);

        ResponseEntity<ErrorResponseDTO> response = handler.handleMaxUploadSize(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(413);
        assertThat(response.getBody().getMessage()).isEqualTo("O arquivo excede o limite de 10 MB.");
    }
}
