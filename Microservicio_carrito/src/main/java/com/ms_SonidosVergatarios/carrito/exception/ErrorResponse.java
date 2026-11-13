package com.ms_SonidosVergatarios.carrito.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(int status, String error, String mensaje, Map<String, String> errores, Instant timestamp) {
}
