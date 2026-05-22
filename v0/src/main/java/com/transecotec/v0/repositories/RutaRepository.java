package com.transecotec.v0.repositories;

import com.transecotec.v0.models.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {

    List<Ruta> findByUsuario_IdUsuario(Long idUsuario);
    List<Ruta> findByEstado(String estado);
}