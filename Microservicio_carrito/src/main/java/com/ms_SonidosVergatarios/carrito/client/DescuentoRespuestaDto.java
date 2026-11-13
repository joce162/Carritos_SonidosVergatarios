package com.ms_SonidosVergatarios.carrito.client;

import java.util.List;

public record DescuentoRespuestaDto(String referencia, List<LineaDescuentoDto> lineas, long subtotal) {
}
