package com.transecotec.v0.controllers;

import com.transecotec.v0.dto.OfertaRequest;
import com.transecotec.v0.models.Carga;
import com.transecotec.v0.models.OfertaAcuerdo;
import com.transecotec.v0.models.Ruta;
import com.transecotec.v0.models.Usuario;
import com.transecotec.v0.repositories.CargaRepository;
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
@RequestMapping("/api/ofertas")
@CrossOrigin(origins = "*")
public class OfertaController {

    @Autowired
    private OfertaRepository ofertaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RutaRepository rutaRepository;
    @Autowired
    private CargaRepository cargaRepository;

    // 1. CREAR UNA NUEVA PETICIÓN DE CONTACTO
    @PostMapping
    public ResponseEntity<?> crearOferta(@RequestBody OfertaRequest request) {

        Optional<Usuario> emisorOpt = usuarioRepository.findById(request.getIdUsuarioEmisor());
        if (emisorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no encontrado");
        }

        Usuario emisor = emisorOpt.get();

        // --- VALIDACIÓN DE SEGURIDAD ---
        if ("Rechazado".equals(emisor.getEstadoVerificacion())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Tu cuenta ha sido rechazada. No puedes enviar peticiones de contacto.\"}");
        }
        // -------------------------------

        OfertaAcuerdo nuevaOferta = new OfertaAcuerdo();
        nuevaOferta.setUsuarioEmisor(emisor);
        nuevaOferta.setEmailContacto(request.getEmailContacto());
        nuevaOferta.setEstado("Pendiente");

        // Si la oferta va dirigida a una RUTA (Empresa contactando a Transportista)
        if (request.getIdRuta() != null) {

            // COMPROBACIÓN ANTISPAM
            if (ofertaRepository.existsByUsuarioEmisor_IdUsuarioAndRuta_IdRuta(emisor.getIdUsuario(), request.getIdRuta())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Ya has enviado una petición de contacto para esta ruta.\"}");
            }

            Optional<Ruta> rutaOpt = rutaRepository.findById(request.getIdRuta());
            if (rutaOpt.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Ruta no encontrada\"}");
            nuevaOferta.setRuta(rutaOpt.get());
        }
        // Si la oferta va dirigida a una CARGA (Transportista contactando a Empresa)
        else if (request.getIdCarga() != null) {

            // COMPROBACIÓN ANTISPAM
            if (ofertaRepository.existsByUsuarioEmisor_IdUsuarioAndCarga_IdCarga(emisor.getIdUsuario(), request.getIdCarga())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Ya has enviado una petición de contacto para esta carga.\"}");
            }

            Optional<Carga> cargaOpt = cargaRepository.findById(request.getIdCarga());
            if (cargaOpt.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Carga no encontrada\"}");
            nuevaOferta.setCarga(cargaOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Debe especificar una ruta o una carga\"}");
        }

        return ResponseEntity.ok(ofertaRepository.save(nuevaOferta));
    }

    // 2. OBTENER LAS OFERTAS DE UN USUARIO
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<OfertaAcuerdo>> getOfertasUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(ofertaRepository.findOfertasByUsuarioId(idUsuario));
    }

    // 3. CAMBIAR EL ESTADO (Aceptar o Rechazar) Y OCULTAR SI SE ACEPTA
    @PutMapping("/{idOferta}/estado")
    public ResponseEntity<?> cambiarEstadoOferta(@PathVariable Long idOferta, @RequestBody java.util.Map<String, String> body) {
        Optional<OfertaAcuerdo> ofertaOpt = ofertaRepository.findById(idOferta);
        if(ofertaOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Oferta no encontrada");

        OfertaAcuerdo oferta = ofertaOpt.get();
        String nuevoEstado = body.get("estado");
        oferta.setEstado(nuevoEstado);

        // --- LÓGICA DE OCULTACIÓN AUTOMÁTICA ---
        if ("Aceptada".equals(nuevoEstado)) {
            // Si la petición aceptada era para una Ruta, la marcamos como Asignada
            if (oferta.getRuta() != null) {
                Ruta ruta = oferta.getRuta();
                ruta.setEstado("Asignada");
                rutaRepository.save(ruta);
            }
            // Si la petición aceptada era para una Carga, la marcamos como Asignada
            else if (oferta.getCarga() != null) {
                Carga carga = oferta.getCarga();
                carga.setEstado("Asignada");
                cargaRepository.save(carga);
            }
        }
        return ResponseEntity.ok(ofertaRepository.save(oferta));
    }
}