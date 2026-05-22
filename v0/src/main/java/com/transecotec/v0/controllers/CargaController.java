package com.transecotec.v0.controllers;

import com.transecotec.v0.dto.CargaRequest;
import com.transecotec.v0.models.Carga;
import com.transecotec.v0.models.Usuario;
import com.transecotec.v0.repositories.CargaRepository;
import com.transecotec.v0.repositories.OfertaRepository;
import com.transecotec.v0.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cargas")
@CrossOrigin(origins = "*") // CORS explícito por si acaso
public class CargaController {

    @Autowired
    private CargaRepository cargaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    // CREAR CARGA (Con validación de seguridad integrada)
    @PostMapping
    public ResponseEntity<?> crearCarga(@RequestBody CargaRequest cargaRequest) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(cargaRequest.getIdUsuario());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        // --- VALIDACIÓN DE SEGURIDAD ---
        if ("Rechazado".equals(usuario.getEstadoVerificacion())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Tu cuenta ha sido rechazada. No puedes publicar cargas.\"}");
        }
        // -------------------------------

        Carga nuevaCarga = new Carga();
        nuevaCarga.setUsuario(usuario);
        nuevaCarga.setOrigen(cargaRequest.getOrigen());
        nuevaCarga.setDestino(cargaRequest.getDestino());
        nuevaCarga.setPesoVolumen(cargaRequest.getPesoVolumen());
        nuevaCarga.setDescripcion(cargaRequest.getDescripcion());

        if(cargaRequest.getEstado() != null) nuevaCarga.setEstado(cargaRequest.getEstado());

        return ResponseEntity.ok(cargaRepository.save(nuevaCarga));
    }

    // OBTENER TODAS LAS CARGAS (Para el buscador de los Transportistas - Solo Publicadas)
    @GetMapping
    public ResponseEntity<List<Carga>> getAllCargas() {
        // Filtramos para que no aparezcan las cargas con estado "Asignada"
        return ResponseEntity.ok(cargaRepository.findByEstado("Publicada"));
    }

    // OBTENER LAS CARGAS DE UN USUARIO ESPECÍFICO
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Carga>> getCargasPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(cargaRepository.findByUsuario_IdUsuario(idUsuario));
    }

    // ACTUALIZAR UNA CARGA
    @PutMapping("/{idCarga}")
    public ResponseEntity<?> actualizarCarga(@PathVariable Long idCarga, @RequestBody CargaRequest cargaRequest) {
        Optional<Carga> cargaOpt = cargaRepository.findById(idCarga);
        if (cargaOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carga no encontrada");

        Carga cargaExistente = cargaOpt.get();
        cargaExistente.setOrigen(cargaRequest.getOrigen());
        cargaExistente.setDestino(cargaRequest.getDestino());
        cargaExistente.setPesoVolumen(cargaRequest.getPesoVolumen());
        cargaExistente.setDescripcion(cargaRequest.getDescripcion());
        if(cargaRequest.getEstado() != null) cargaExistente.setEstado(cargaRequest.getEstado());

        return ResponseEntity.ok(cargaRepository.save(cargaExistente));
    }

    // ELIMINAR UNA CARGA
    @DeleteMapping("/{idCarga}")
    public ResponseEntity<?> eliminarCarga(@PathVariable Long idCarga) {
        if (!cargaRepository.existsById(idCarga)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carga no encontrada");
        }

        // 1. Limpiamos primero las ofertas vinculadas (usando nuestro nuevo método seguro)
        ofertaRepository.borrarOfertasPorCarga(idCarga);

        // 2. Ahora sí, borramos la carga libremente
        cargaRepository.deleteById(idCarga);

        return ResponseEntity.ok().body("{\"mensaje\": \"Carga eliminada\"}");
    }
}