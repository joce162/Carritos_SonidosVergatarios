package com.ms_SonidosVergatarios.carrito.dto;

import com.ms_SonidosVergatarios.carrito.model.Compra;
import java.time.Instant;
import java.util.List;

public record CompraResponse(Long id, String referencia, String estado, List<CompraLineaResponse> lineas,
                             long subtotal, int envio, long total, Instant creadaEn) {

    public static CompraResponse desde(Compra c) {
        List<CompraLineaResponse> lineas = c.getLineas().stream()
                .map(l -> new CompraLineaResponse(l.getProductoId(), l.getNombre(), l.getPrecioUnitario(),
                        l.getCantidad(), (long) l.getPrecioUnitario() * l.getCantidad()))
                .toList();
        return new CompraResponse(c.getId(), c.getReferencia(), c.getEstado().name(), lineas,
                c.getSubtotal(), c.getEnvio(), c.getTotal(), c.getCreadaEn());
    }
}
