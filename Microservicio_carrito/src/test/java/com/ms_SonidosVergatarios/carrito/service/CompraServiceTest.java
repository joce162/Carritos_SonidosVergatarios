package com.ms_SonidosVergatarios.carrito.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.ms_SonidosVergatarios.carrito.client.DescuentoRespuestaDto;
import com.ms_SonidosVergatarios.carrito.client.LineaDescuentoDto;
import com.ms_SonidosVergatarios.carrito.client.ProductosApi;
import com.ms_SonidosVergatarios.carrito.dto.CompraResponse;
import com.ms_SonidosVergatarios.carrito.exception.ApiException;
import com.ms_SonidosVergatarios.carrito.model.Compra;
import com.ms_SonidosVergatarios.carrito.model.CompraLinea;
import com.ms_SonidosVergatarios.carrito.model.EstadoCompra;
import com.ms_SonidosVergatarios.carrito.repository.CompraRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CompraServiceTest {

    private CompraRegistro registro;
    private ProductosApi productos;
    private CompraService servicio;
    private Compra compra;

    @BeforeEach
    void preparar() {
        registro = mock(CompraRegistro.class);
        productos = mock(ProductosApi.class);
        servicio = new CompraService(registro, productos, mock(CompraRepository.class));

        compra = new Compra();
        compra.setUsuarioId(7L);
        compra.setReferencia("ref-1");
        compra.getLineas().add(new CompraLinea(1L, "", 0, 2));
        when(registro.iniciar(7L)).thenReturn(compra);
    }

    @Test
    void siProductosRechazaPorStockLaCompraSeRechazaYSePropagaElError() {
        when(productos.descontar(anyString(), anyList()))
                .thenThrow(new ApiException(HttpStatus.CONFLICT, "Stock insuficiente"));

        ApiException e = assertThrows(ApiException.class, () -> servicio.finalizar(7L));

        assertEquals(HttpStatus.CONFLICT, e.getEstado());
        verify(registro).rechazar(any());
        verify(registro, never()).confirmar(any(), any(), anyInt());
        verify(productos, never()).reponer(anyString());
    }

    @Test
    void siProductosNoRespondeSeIntentaReponerYRechazar() {
        when(productos.descontar(anyString(), anyList()))
                .thenThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Sin servicio"));

        assertThrows(ApiException.class, () -> servicio.finalizar(7L));

        verify(productos).reponer("ref-1");
        verify(registro).rechazar(any());
    }

    @Test
    void siFallaLaConfirmacionLocalSeReponeElStock() {
        DescuentoRespuestaDto descuento = new DescuentoRespuestaDto("ref-1",
                List.of(new LineaDescuentoDto(1L, "Platillo", 1000, 2)), 2000);
        when(productos.descontar(anyString(), anyList())).thenReturn(descuento);
        when(registro.confirmar(any(), any(), anyInt())).thenThrow(new IllegalStateException("BD caída"));

        assertThrows(IllegalStateException.class, () -> servicio.finalizar(7L));

        verify(productos).reponer("ref-1");
        verify(registro).rechazar(any());
    }

    @Test
    void compraExitosaDevuelveElTotalConEnvio() {
        DescuentoRespuestaDto descuento = new DescuentoRespuestaDto("ref-1",
                List.of(new LineaDescuentoDto(1L, "Platillo", 1000, 2)), 2000);
        when(productos.descontar(anyString(), anyList())).thenReturn(descuento);
        when(registro.confirmar(any(), any(), anyInt())).thenAnswer(inv -> {
            compra.getLineas().clear();
            compra.getLineas().add(new CompraLinea(1L, "Platillo", 1000, 2));
            compra.setSubtotal(2000);
            compra.setEnvio(CompraService.COSTO_ENVIO);
            compra.setTotal(2000 + CompraService.COSTO_ENVIO);
            compra.setEstado(EstadoCompra.CONFIRMADA);
            return compra;
        });

        CompraResponse r = servicio.finalizar(7L);

        assertEquals("CONFIRMADA", r.estado());
        assertEquals(5990, r.total());
        assertEquals(2000, r.subtotal());
    }
}
