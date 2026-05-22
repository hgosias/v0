package com.transecotec.v0.repositories;

import com.transecotec.v0.models.Carga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargaRepository extends JpaRepository<Carga, Long> {
    List<Carga> findByUsuario_IdUsuario(Long idUsuario);
    List<Carga> findByEstado(String estado);
}