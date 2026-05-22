package com.transecotec.v0.repositories;

import com.transecotec.v0.models.OfertaAcuerdo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OfertaRepository extends JpaRepository<OfertaAcuerdo, Long> {

    @Query("SELECT o FROM OfertaAcuerdo o LEFT JOIN o.ruta r LEFT JOIN o.carga c WHERE o.usuarioEmisor.idUsuario = :idUsuario OR r.usuario.idUsuario = :idUsuario OR c.usuario.idUsuario = :idUsuario")
    List<OfertaAcuerdo> findOfertasByUsuarioId(@Param("idUsuario") Long idUsuario);

    // Borrar las ofertas asociadas a una ruta antes de eliminarla
    @Modifying
    @Transactional
    @Query("DELETE FROM OfertaAcuerdo o WHERE o.ruta.idRuta = :idRuta")
    void deleteByRutaId(@Param("idRuta") Long idRuta);

    // Comprobar si ya existe una petición de ese usuario para esa ruta
    boolean existsByUsuarioEmisor_IdUsuarioAndRuta_IdRuta(Long idUsuarioEmisor, Long idRuta);

    // Comprobar si ya existe una petición de ese usuario para esa carga
    boolean existsByUsuarioEmisor_IdUsuarioAndCarga_IdCarga(Long idUsuarioEmisor, Long idCarga);

    @Modifying
    @Transactional
    @Query("DELETE FROM OfertaAcuerdo o WHERE o.carga.idCarga = :idCarga")
    void borrarOfertasPorCarga(@Param("idCarga") Long idCarga);

    @Modifying
    @Transactional
    @Query("DELETE FROM OfertaAcuerdo o WHERE o.ruta.idRuta = :idRuta")
    void borrarOfertasPorRuta(@Param("idRuta") Long idRuta);

}