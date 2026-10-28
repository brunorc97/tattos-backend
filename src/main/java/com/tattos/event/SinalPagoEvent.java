package com.tattos.event;

/** Publicado quando o sinal (PIX) de um agendamento é confirmado como pago. */
public record SinalPagoEvent(Long agendamentoId) {
}
