package com.ms_SonidosVergatarios.carrito.service;

import com.ms_SonidosVergatarios.carrito.client.ProductoDto;
import com.ms_SonidosVergatarios.carrito.client.ProductosApi;
import com.ms_SonidosVergatarios.carrito.dto.CarritoResponse;
import com.ms_SonidosVergatarios.carrito.dto.ItemResponse;
import com.ms_SonidosVergatarios.carrito.exception.ApiException;
import com.ms_SonidosVergatarios.carrito.model.ItemCarrito;
import com.ms_SonidosVergatarios.carrito.repository.ItemCarritoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestión del carrito de cada usuario. Valida existencia y stock consultando al microservicio de productos. */
@Service
public class CarritoService {

    private final ItemCarritoRepository items;
    private final ProductosApi productos;

    public CarritoService(ItemCarritoRepository items, ProductosApi productos) {
        this.items = items;
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public CarritoResponse obtener(Long usuarioId) {
        return armar(items.findByUsuarioIdOrderByIdAsc(usuarioId));
    }

    @Transactional
    public CarritoResponse agregar(Long usuarioId, Long productoId, int cantidad) {
        ProductoDto producto = productos.obtener(productoId);
        ItemCarrito item = items.findByUsuarioIdAndProductoId(usuarioId, productoId)
                .orElseGet(() -> nuevo(usuarioId, productoId));
        int total = item.getCantidad() + cantidad;
        validarStock(producto, total);
        item.setCantidad(total);
        items.save(item);
        return obtenerActual(usuarioId);
    }

    @Transactional
    public CarritoResponse cambiarCantidad(Long usuarioId, Long productoId, int cantidad) {
        if (cantidad == 0) {
            items.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
            return obtenerActual(usuarioId);
        }
        ProductoDto producto = productos.obtener(productoId);
        validarStock(producto, cantidad);
        ItemCarrito item = items.findByUsuarioIdAndProductoId(usuarioId, productoId)
                .orElseGet(() -> nuevo(usuarioId, productoId));
        item.setCantidad(cantidad);
        items.save(item);
        return obtenerActual(usuarioId);
    }

    @Transactional
    public CarritoResponse quitar(Long usuarioId, Long productoId) {
        items.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
        return obtenerActual(usuarioId);
    }

    @Transactional
    public CarritoResponse vaciar(Long usuarioId) {
        items.deleteByUsuarioId(usuarioId);
        return obtenerActual(usuarioId);
    }

    private CarritoResponse obtenerActual(Long usuarioId) {
        return armar(items.findByUsuarioIdOrderByIdAsc(usuarioId));
    }

    private void validarStock(ProductoDto producto, int cantidad) {
        if (cantidad > producto.stock()) {
            throw new ApiException(HttpStatus.CONFLICT, "No hay más stock disponible de ese producto.");
        }
    }

    private ItemCarrito nuevo(Long usuarioId, Long productoId) {
        ItemCarrito i = new ItemCarrito();
        i.setUsuarioId(usuarioId);
        i.setProductoId(productoId);
        i.setCantidad(0);
        return i;
    }

    private CarritoResponse armar(List<ItemCarrito> lista) {
        List<ItemResponse> respuesta = lista.stream()
                .map(i -> new ItemResponse(i.getProductoId(), i.getCantidad())).toList();
        int unidades = respuesta.stream().mapToInt(ItemResponse::cantidad).sum();
        return new CarritoResponse(respuesta, unidades);
    }
}
