package com.transecotec.v0.controllers;

import com.transecotec.v0.dto.RutaRequest;
import com.transecotec.v0.models.Ruta;
import com.transecotec.v0.models.Usuario;
import com.transecotec.v0.repositories.OfertaRepository;
import com.transecotec.v0.repositories.RutaRepository;
import com.transecotec.v0.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rutas")
@CrossOrigin(origins = "*") // Permite la conexión desde tu frontend
public class RutaController {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    // 1. PUBLICAR UNA NUEVA RUTA (Con validación de seguridad integrada)
    @PostMapping
    public ResponseEntity<?> crearRuta(@RequestBody RutaRequest rutaRequest) {

        // Primero, comprobamos que el usuario que intenta publicar existe
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(rutaRequest.getIdUsuario());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        // --- VALIDACIÓN DE SEGURIDAD ---
        if ("Rechazado".equals(usuario.getEstadoVerificacion())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Tu cuenta ha sido rechazada. No puedes publicar rutas.\"}");
        }
        // -------------------------------

        // Creamos la nueva ruta y le pasamos los datos del DTO
        Ruta nuevaRuta = new Ruta();
        nuevaRuta.setUsuario(usuario); // Asignamos el usuario real
        nuevaRuta.setOrigen(rutaRequest.getOrigen());
        nuevaRuta.setDestino(rutaRequest.getDestino());
        nuevaRuta.setFechaSalida(rutaRequest.getFechaSalida());
        nuevaRuta.setCapacidadLibre(rutaRequest.getCapacidadLibre());

        if(rutaRequest.getEstado() != null) {
            nuevaRuta.setEstado(rutaRequest.getEstado());
        }

        // Guardamos en la Base de Datos
        Ruta rutaGuardada = rutaRepository.save(nuevaRuta);

        return ResponseEntity.ok(rutaGuardada);
    }

    // OBTENER TODAS LAS RUTAS (Para el buscador de las Empresas - Solo Publicadas)
    @GetMapping
    public ResponseEntity<List<Ruta>> getAllRutas() {
        // En lugar de un findAll() genérico, filtramos solo por las que siguen libres
        return ResponseEntity.ok(rutaRepository.findByEstado("Publicada"));
    }

    // 2. OBTENER LAS RUTAS DE UN USUARIO (GET /api/rutas/usuario/{id})
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Ruta>> getRutasPorUsuario(@PathVariable Long idUsuario) {
        List<Ruta> misRutas = rutaRepository.findByUsuario_IdUsuario(idUsuario);
        return ResponseEntity.ok(misRutas);
    }

    // 3. EDITAR UNA RUTA (PUT /api/rutas/{id})
    @PutMapping("/{idRuta}")
    public ResponseEntity<?> actualizarRuta(@PathVariable Long idRuta, @RequestBody RutaRequest rutaRequest) {
        Optional<Ruta> rutaOpt = rutaRepository.findById(idRuta);

        if (rutaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ruta no encontrada");
        }

        Ruta rutaExistente = rutaOpt.get();
        // Actualizamos los datos
        rutaExistente.setOrigen(rutaRequest.getOrigen());
        rutaExistente.setDestino(rutaRequest.getDestino());
        rutaExistente.setFechaSalida(rutaRequest.getFechaSalida());
        rutaExistente.setCapacidadLibre(rutaRequest.getCapacidadLibre());

        if(rutaRequest.getEstado() != null) {
            rutaExistente.setEstado(rutaRequest.getEstado());
        }

        Ruta rutaActualizada = rutaRepository.save(rutaExistente);
        return ResponseEntity.ok(rutaActualizada);
    }

    // ELIMINAR UNA RUTA Y SUS ACUERDOS VINCULADOS
    @DeleteMapping("/{idRuta}")
    public ResponseEntity<?> eliminarRuta(@PathVariable Long idRuta) {
        if (!rutaRepository.existsById(idRuta)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ruta no encontrada");
        }

        // 1. Primero borramos los acuerdos/peticiones que apuntan a esta ruta
        ofertaRepository.deleteByRutaId(idRuta);

        // 2. Ahora sí, borramos la ruta limpiamente
        rutaRepository.deleteById(idRuta);

        return ResponseEntity.ok().body("{\"mensaje\": \"Ruta eliminada correctamente por el administrador\"}");
    }
}