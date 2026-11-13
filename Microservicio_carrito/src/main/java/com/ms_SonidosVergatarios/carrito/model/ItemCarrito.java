package com.ms_SonidosVergatarios.carrito.model;

import jakarta.persistence.*;

/** Una línea del carrito: solo guarda ids y cantidades; precio y stock siempre se consultan en productos. */
@Entity
@Table(name = "items_carrito",
        uniqueConstraints = @UniqueConstraint(name = "uk_item_usuario_producto", columnNames = {"usuario_id", "producto_id"}))
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private int cantidad;

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
