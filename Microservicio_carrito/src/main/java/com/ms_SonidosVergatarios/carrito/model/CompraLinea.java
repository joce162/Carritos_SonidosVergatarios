package com.ms_SonidosVergatarios.carrito.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Línea de una compra con el nombre y precio vigentes al comprar (no cambian si el catálogo cambia). */
@Embeddable
public class CompraLinea {

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "precio_unitario", nullable = false)
    private int precioUnitario;

    @Column(nullable = false)
    private int cantidad;

    protected CompraLinea() { }

    public CompraLinea(Long productoId, String nombre, int precioUnitario, int cantidad) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public Long getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
    public int getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
}
