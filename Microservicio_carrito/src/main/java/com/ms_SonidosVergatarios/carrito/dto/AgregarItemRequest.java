package com.ms_SonidosVergatarios.carrito.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** La cantidad es opcional: si no viene, se agrega una unidad. */
public record AgregarItemRequest(
        @NotNull(message = "Indica el producto.") Long productoId,
        @Positive(message = "La cantidad debe ser mayor que cero.") Integer cantidad) {
}
