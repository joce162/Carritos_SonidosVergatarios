package com.ms_SonidosVergatarios.carrito.controller;

import com.ms_SonidosVergatarios.carrito.dto.AgregarItemRequest;
import com.ms_SonidosVergatarios.carrito.dto.CambiarCantidadRequest;
import com.ms_SonidosVergatarios.carrito.dto.CarritoResponse;
import com.ms_SonidosVergatarios.carrito.dto.CompraResponse;
import com.ms_SonidosVergatarios.carrito.service.CarritoService;
import com.ms_SonidosVergatarios.carrito.service.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** El usuario se obtiene siempre del JWT (subject): nunca viaja en la URL ni en el cuerpo. */
@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carrito y compras")
public class CarritoController {

    private final CarritoService carrito;
    private final CompraService compras;

    public CarritoController(CarritoService carrito, CompraService compras) {
        this.carrito = carrito;
        this.compras = compras;
    }

    @Operation(summary = "Devuelve el carrito del usuario autenticado")
    @GetMapping
    public CarritoResponse obtener(@AuthenticationPrincipal Jwt jwt) {
        return carrito.obtener(usuario(jwt));
    }

    @Operation(summary = "Agrega un producto (o suma unidades) validando el stock")
    @PostMapping("/items")
    public CarritoResponse agregar(@Valid @RequestBody AgregarItemRequest r, @AuthenticationPrincipal Jwt jwt) {
        int cantidad = r.cantidad() == null ? 1 : r.cantidad();
        return carrito.agregar(usuario(jwt), r.productoId(), cantidad);
    }

    @Operation(summary = "Fija la cantidad de un producto (0 lo quita del carrito)")
    @PutMapping("/items/{productoId}")
    public CarritoResponse cambiarCantidad(@PathVariable Long productoId,
                                           @Valid @RequestBody CambiarCantidadRequest r,
                                           @AuthenticationPrincipal Jwt jwt) {
        return carrito.cambiarCantidad(usuario(jwt), productoId, r.cantidad());
    }

    @Operation(summary = "Quita un producto del carrito")
    @DeleteMapping("/items/{productoId}")
    public CarritoResponse quitar(@PathVariable Long productoId, @AuthenticationPrincipal Jwt jwt) {
        return carrito.quitar(usuario(jwt), productoId);
    }

    @Operation(summary = "Vacía el carrito")
    @DeleteMapping
    public CarritoResponse vaciar(@AuthenticationPrincipal Jwt jwt) {
        return carrito.vaciar(usuario(jwt));
    }

    @Operation(summary = "Confirma la compra: descuenta el stock en productos y registra el pedido")
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public CompraResponse checkout(@AuthenticationPrincipal Jwt jwt) {
        return compras.finalizar(usuario(jwt));
    }

    @Operation(summary = "Historial de compras del usuario autenticado")
    @GetMapping("/compras")
    public List<CompraResponse> historial(@AuthenticationPrincipal Jwt jwt) {
        return compras.historial(usuario(jwt));
    }

    private Long usuario(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
