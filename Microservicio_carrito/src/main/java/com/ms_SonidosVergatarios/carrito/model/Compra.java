package com.ms_SonidosVergatarios.carrito.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /** Identifica la operación de inventario asociada (idempotencia con el microservicio de productos). */
    @Column(nullable = false, unique = true, length = 64)
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompra estado = EstadoCompra.PENDIENTE;

    @Column(nullable = false)
    private long subtotal;

    @Column(nullable = false)
    private int envio;

    @Column(nullable = false)
    private long total;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn = Instant.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "compra_lineas", joinColumns = @JoinColumn(name = "compra_id"))
    private List<CompraLinea> lineas = new ArrayList<>();

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public EstadoCompra getEstado() { return estado; }
    public void setEstado(EstadoCompra estado) { this.estado = estado; }
    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }
    public int getEnvio() { return envio; }
    public void setEnvio(int envio) { this.envio = envio; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public Instant getCreadaEn() { return creadaEn; }
    public List<CompraLinea> getLineas() { return lineas; }
}
