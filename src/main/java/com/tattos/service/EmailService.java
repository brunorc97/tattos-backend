package com.tattos.service;

import org.springframework.stereotype.Service;

/**
 * Serviço de e‑mail simples (placeholder). Em produção será substituído por um provedor real
 * (Spring Mail, SendGrid, etc.). Por enquanto apenas registra no log que o e‑mail seria enviado.
 */
@Service
public class EmailService {

    public void sendEmail(String to, String subject, String body) {
        // Placeholder: em produção use JavaMailSender ou outra lib.
        System.out.println("[EmailService] Enviando e‑mail para " + to + " | Assunto: " + subject + "\n" + body);
    }
}
