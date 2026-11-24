package com.ms_SonidosVergatarios.carrito.client;

import java.util.List;

public record DescuentoSolicitudDto(String referencia, List<ItemDescuentoDto> items) {
}
