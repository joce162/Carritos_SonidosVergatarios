package com.ms_SonidosVergatarios.carrito.dto;

import java.util.List;

public record CarritoResponse(List<ItemResponse> items, int unidades) {
}
