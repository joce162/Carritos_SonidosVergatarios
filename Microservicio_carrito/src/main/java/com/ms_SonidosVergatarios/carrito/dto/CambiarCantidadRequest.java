package com.ms_SonidosVergatarios.carrito.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Cantidad total deseada. Con 0 el producto sale del carrito. */
public record CambiarCantidadRequest(
        @NotNull(message = "Indica la cantidad.")
        @PositiveOrZero(message = "La cantidad debe ser un entero mayor o igual a cero.") Integer cantidad) {
}
