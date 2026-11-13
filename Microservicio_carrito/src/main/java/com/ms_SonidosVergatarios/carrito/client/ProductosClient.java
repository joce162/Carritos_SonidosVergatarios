package com.ms_SonidosVergatarios.carrito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Cliente HTTP declarativo hacia el microservicio de productos (comunicación directa, sin Gateway). */
@FeignClient(name = "productos", url = "${app.productos-url}")
public interface ProductosClient {

    @GetMapping("/api/productos/{id}")
    ProductoDto obtener(@PathVariable("id") Long id);

    @PostMapping("/internal/stock/descontar")
    DescuentoRespuestaDto descontar(@RequestBody DescuentoSolicitudDto cuerpo);

    @PostMapping("/internal/stock/reponer/{referencia}")
    void reponer(@PathVariable("referencia") String referencia);
}
