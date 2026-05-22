package com.transecotec.v0.repositories;

import com.transecotec.v0.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    List<Usuario> findByEstadoVerificacion(String estadoVerificacion);
}