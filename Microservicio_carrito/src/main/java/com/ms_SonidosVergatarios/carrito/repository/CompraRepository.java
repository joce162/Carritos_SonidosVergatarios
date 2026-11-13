package com.ms_SonidosVergatarios.carrito.repository;

import com.ms_SonidosVergatarios.carrito.model.Compra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByUsuarioIdOrderByIdDesc(Long usuarioId);
}
