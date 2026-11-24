package com.ms_SonidosVergatarios.carrito.dto;

public record CompraLineaResponse(Long productoId, String nombre, int precioUnitario, int cantidad, long subtotal) {
}
