package com.ms_SonidosVergatarios.carrito.client;

import com.ms_SonidosVergatarios.carrito.exception.ApiException;
import feign.FeignException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Envuelve el cliente Feign y traduce sus errores a ApiException (404, 409 o 503). */
@Component
public class ProductosApi {

    private static final Pattern MENSAJE = Pattern.compile("\"mensaje\"\\s*:\\s*\"([^\"]*)\"");

    private final ProductosClient cliente;

    public ProductosApi(ProductosClient cliente) {
        this.cliente = cliente;
    }

    public ProductoDto obtener(Long id) {
        try {
            return cliente.obtener(id);
        } catch (FeignException.NotFound e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "El producto ya no existe.");
        } catch (FeignException e) {
            throw sinServicio();
        }
    }

    public DescuentoRespuestaDto descontar(String referencia, List<ItemDescuentoDto> items) {
        try {
            return cliente.descontar(new DescuentoSolicitudDto(referencia, items));
        } catch (FeignException.Conflict e) {
            throw new ApiException(HttpStatus.CONFLICT, mensajeDe(e));
        } catch (FeignException e) {
            throw sinServicio();
        }
    }

    public void reponer(String referencia) {
        try {
            cliente.reponer(referencia);
        } catch (FeignException e) {
            throw sinServicio();
        }
    }

    private static ApiException sinServicio() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de productos no está disponible. Intenta nuevamente en unos minutos.");
    }

    private static String mensajeDe(FeignException e) {
        Matcher m = MENSAJE.matcher(e.contentUTF8());
        return m.find() ? m.group(1) : "No hay stock suficiente para completar la compra.";
    }
}
