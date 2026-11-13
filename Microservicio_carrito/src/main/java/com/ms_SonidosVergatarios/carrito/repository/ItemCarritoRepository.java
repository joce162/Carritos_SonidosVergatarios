package com.ms_SonidosVergatarios.carrito.repository;

import com.ms_SonidosVergatarios.carrito.model.ItemCarrito;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    List<ItemCarrito> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    Optional<ItemCarrito> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    void deleteByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    void deleteByUsuarioId(Long usuarioId);

    /** Bloquea el carrito del usuario: dos "finalizar compra" simultáneos no pueden usar los mismos ítems. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ItemCarrito i where i.usuarioId = :usuarioId order by i.id")
    List<ItemCarrito> bloquearPorUsuario(@Param("usuarioId") Long usuarioId);
}
