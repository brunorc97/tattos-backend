package com.tattos.event;

/** Publicado quando um insumo atinge ou cai abaixo do nível crítico de estoque. */
public record EstoqueCriticoEvent(Long insumoId) {
}
