package com.ms_SonidosVergatarios.carrito.client;

/** Lo que el carrito necesita saber de un producto. */
public record ProductoDto(Long id, String nombre, int precio, int stock) {
}
