package com.ms_SonidosVergatarios.carrito.service;

import com.ms_SonidosVergatarios.carrito.client.DescuentoRespuestaDto;
import com.ms_SonidosVergatarios.carrito.client.LineaDescuentoDto;
import com.ms_SonidosVergatarios.carrito.exception.ApiException;
import com.ms_SonidosVergatarios.carrito.model.Compra;
import com.ms_SonidosVergatarios.carrito.model.CompraLinea;
import com.ms_SonidosVergatarios.carrito.model.EstadoCompra;
import com.ms_SonidosVergatarios.carrito.model.ItemCarrito;
import com.ms_SonidosVergatarios.carrito.repository.CompraRepository;
import com.ms_SonidosVergatarios.carrito.repository.ItemCarritoRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pasos de base de datos de una compra. Cada método es una transacción corta e independiente;
 * así CompraService puede llamar al microservicio de productos entre un paso y otro sin
 * mantener una transacción abierta durante la llamada HTTP.
 */
@Component
public class CompraRegistro {

    private final ItemCarritoRepository items;
    private final CompraRepository compras;

    public CompraRegistro(ItemCarritoRepository items, CompraRepository compras) {
        this.items = items;
        this.compras = compras;
    }

    /** Paso 1: traslada el carrito a una compra PENDIENTE (el carrito queda vacío). */
    @Transactional
    public Compra iniciar(Long usuarioId) {
        List<ItemCarrito> lista = items.bloquearPorUsuario(usuarioId);
        if (lista.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "El carrito está vacío.");
        }
        Compra compra = new Compra();
        compra.setUsuarioId(usuarioId);
        compra.setReferencia(UUID.randomUUID().toString());
        compra.setEstado(EstadoCompra.PENDIENTE);
        for (ItemCarrito i : lista) {
            compra.getLineas().add(new CompraLinea(i.getProductoId(), "", 0, i.getCantidad()));
        }
        items.deleteAll(lista);
        return compras.save(compra);
    }

    /** Paso 3 (éxito): guarda precios y nombres reales y marca la compra como CONFIRMADA. */
    @Transactional
    public Compra confirmar(Long compraId, DescuentoRespuestaDto descuento, int envio) {
        Compra compra = compras.findById(compraId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Compra no encontrada."));
        compra.getLineas().clear();
        long subtotal = 0;
        for (LineaDescuentoDto l : descuento.lineas()) {
            compra.getLineas().add(new CompraLinea(l.productoId(), l.nombre(), l.precioUnitario(), l.cantidad()));
            subtotal += (long) l.precioUnitario() * l.cantidad();
        }
        compra.setSubtotal(subtotal);
        compra.setEnvio(envio);
        compra.setTotal(subtotal + envio);
        compra.setEstado(EstadoCompra.CONFIRMADA);
        return compra;
    }

    /** Paso 3 (fallo): marca la compra RECHAZADA y devuelve los productos al carrito del usuario. */
    @Transactional
    public void rechazar(Long compraId) {
        Compra compra = compras.findById(compraId).orElse(null);
        if (compra == null || compra.getEstado() != EstadoCompra.PENDIENTE) {
            return;
        }
        compra.setEstado(EstadoCompra.RECHAZADA);
        for (CompraLinea l : compra.getLineas()) {
            ItemCarrito item = items.findByUsuarioIdAndProductoId(compra.getUsuarioId(), l.getProductoId())
                    .orElseGet(() -> {
                        ItemCarrito nuevo = new ItemCarrito();
                        nuevo.setUsuarioId(compra.getUsuarioId());
                        nuevo.setProductoId(l.getProductoId());
                        nuevo.setCantidad(0);
                        return nuevo;
                    });
            item.setCantidad(item.getCantidad() + l.getCantidad());
            items.save(item);
        }
    }
}
