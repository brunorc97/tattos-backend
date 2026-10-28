package com.tattos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registrar a aceitação do termo de consentimento (assinatura digital) via flag no front‑end.
 * O cliente apenas confirma que leu/assinou; enviamos o e‑mail de registro para auditoria.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssinaturaRequestDTO {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    // Opcional: id do usuário que realizou a ação (pode ser o cliente ou gestor)
    private Long usuarioId;
}
