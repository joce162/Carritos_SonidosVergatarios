package com.ms_SonidosVergatarios.carrito.service;

import com.ms_SonidosVergatarios.carrito.client.DescuentoRespuestaDto;
import com.ms_SonidosVergatarios.carrito.client.ItemDescuentoDto;
import com.ms_SonidosVergatarios.carrito.client.ProductosApi;
import com.ms_SonidosVergatarios.carrito.dto.CompraResponse;
import com.ms_SonidosVergatarios.carrito.exception.ApiException;
import com.ms_SonidosVergatarios.carrito.model.Compra;
import com.ms_SonidosVergatarios.carrito.repository.CompraRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confirmación de compra entre dos microservicios, cada uno con su propia base de datos.
 * No existe una transacción distribuida, por eso se usa una saga con compensación:
 *   1. Carrito pasa los ítems a una compra PENDIENTE (transacción local).
 *   2. Productos descuenta el stock en una transacción local, identificada por la referencia.
 *   3. Si todo sale bien, Carrito confirma la compra; si algo falla, se repone el stock y la
 *      compra queda RECHAZADA con los productos de vuelta en el carrito.
 */
@Service
public class CompraService {

    public static final int COSTO_ENVIO = 3990;
    private static final Logger LOG = LoggerFactory.getLogger(CompraService.class);

    private final CompraRegistro registro;
    private final ProductosApi productos;
    private final CompraRepository compras;

    public CompraService(CompraRegistro registro, ProductosApi productos, CompraRepository compras) {
        this.registro = registro;
        this.productos = productos;
        this.compras = compras;
    }

    public CompraResponse finalizar(Long usuarioId) {
        Compra compra = registro.iniciar(usuarioId);
        String referencia = compra.getReferencia();
        List<ItemDescuentoDto> pedido = compra.getLineas().stream()
                .map(l -> new ItemDescuentoDto(l.getProductoId(), l.getCantidad())).toList();

        DescuentoRespuestaDto descuento;
        try {
            descuento = productos.descontar(referencia, pedido);
        } catch (RuntimeException e) {
            // Si no hubo respuesta, quizá productos alcanzó a descontar: se intenta devolver el stock.
            if (e instanceof ApiException api && api.getEstado().value() == 503) {
                intentarReponer(referencia);
            }
            registro.rechazar(compra.getId());
            throw e;
        }

        try {
            return CompraResponse.desde(registro.confirmar(compra.getId(), descuento, COSTO_ENVIO));
        } catch (RuntimeException e) {
            intentarReponer(referencia);
            registro.rechazar(compra.getId());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<CompraResponse> historial(Long usuarioId) {
        return compras.findByUsuarioIdOrderByIdDesc(usuarioId).stream().map(CompraResponse::desde).toList();
    }

    private void intentarReponer(String referencia) {
        try {
            productos.reponer(referencia);
        } catch (RuntimeException e) {
            LOG.error("No se pudo reponer el stock de la operación {}. Requiere revisión manual.", referencia, e);
        }
    }
}
