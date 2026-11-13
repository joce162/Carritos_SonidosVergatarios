package com.ms_SonidosVergatarios.carrito.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    /** Agrega la clave de servicio a cada llamada hacia el microservicio de productos. */
    @Bean
    public RequestInterceptor claveInterna(@Value("${app.internal-key}") String clave) {
        return plantilla -> plantilla.header("X-Internal-Key", clave);
    }
}
