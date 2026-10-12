package com.tattos.service;

import org.springframework.stereotype.Service;

/**
 * Serviço de e‑mail simples (placeholder). Em produção será substituído por um provedor real
 * (Spring Mail, SendGrid, etc.). Por enquanto apenas registra no log que o e‑mail seria enviado.
 */
@Service
public class EmailService {

    public void logAssinaturaEnviada(Long fichaId) {
        // Simple log indicating that a signature email was sent (placeholder)
        System.out.println("[EmailService] Assinatura digital de ficha ID " + fichaId + " enviada por e‑mail.");
    }
}
