package com.tattos.event;

/** Publicado quando um lembrete de agendamento (24h) deve ser entregue ao cliente. */
public record LembreteAgendamentoEvent(Long agendamentoId) {
}
