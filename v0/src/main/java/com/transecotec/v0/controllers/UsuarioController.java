package com.transecotec.v0.controllers;

import com.transecotec.v0.dto.LoginRequest;
import com.transecotec.v0.models.Carga;
import com.transecotec.v0.models.Ruta;
import com.transecotec.v0.models.Usuario;
import com.transecotec.v0.repositories.CargaRepository;
import com.transecotec.v0.repositories.OfertaRepository;
import com.transecotec.v0.repositories.RutaRepository;
import com.transecotec.v0.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*") // Permite peticiones desde tu frontend
public class UsuarioController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private CargaRepository cargaRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    // 1. Obtener todos los usuarios
    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    // 2. Crear un nuevo usuario - Registro
    @PostMapping
    public ResponseEntity<?> createUsuario(@RequestBody Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo ya está registrado");
        }

        String passwordPlana = usuario.getContrasena();
        String passwordCifrada = passwordEncoder.encode(passwordPlana);
        usuario.setContrasena(passwordCifrada);

        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.ok(nuevoUsuario);
    }

    // 3. Iniciar Sesión (Login) con validación segura
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail());

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Correo o contraseña incorrectos");
        }

        boolean passwordCorrecta = passwordEncoder.matches(loginRequest.getContrasena(), usuario.getContrasena());

        if (passwordCorrecta) {
            usuario.setContrasena(null);
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Correo o contraseña incorrectos");
        }
    }

    // 4. ACTUALIZAR PERFIL DE USUARIO
    @PutMapping("/{idUsuario}")
    public ResponseEntity<?> actualizarPerfil(@PathVariable Long idUsuario, @RequestBody Usuario datosActualizados) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuarioExistente = usuarioOpt.get();

        usuarioExistente.setNombre(datosActualizados.getNombre());
        usuarioExistente.setEmail(datosActualizados.getEmail());
        usuarioExistente.setCifDni(datosActualizados.getCifDni());

        if (datosActualizados.getContrasena() != null && !datosActualizados.getContrasena().trim().isEmpty()) {
            String nuevaContrasenaCifrada = passwordEncoder.encode(datosActualizados.getContrasena());
            usuarioExistente.setContrasena(nuevaContrasenaCifrada);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuarioExistente);
        usuarioGuardado.setContrasena(null);

        return ResponseEntity.ok(usuarioGuardado);
    }

    // --- MÉTODOS EXCLUSIVOS DEL ADMINISTRADOR ---

    // 1. Obtener lista de usuarios pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<List<Usuario>> getUsuariosPendientes() {
        List<Usuario> pendientes = usuarioRepository.findByEstadoVerificacion("Pendiente");
        return ResponseEntity.ok(pendientes);
    }

    // 2. Cambiar el estado a Verificado o Rechazado
    @PutMapping("/{id}/verificar")
    public ResponseEntity<?> verificarUsuario(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setEstadoVerificacion(body.get("estado"));

        return ResponseEntity.ok(usuarioRepository.save(usuario));
    }

    // 3. Eliminar usuario permanentemente (Administrador) LIMPIEZA TOTAL
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        // 1. Borrar todas las ofertas que este usuario haya ENVIADO a otros
        ofertaRepository.borrarOfertasPorUsuarioEmisor(id);

        // 2. Borrar las cargas del usuario (y las ofertas que las apuntan)
        List<Carga> cargas = cargaRepository.findByUsuario_IdUsuario(id);
        for (Carga carga : cargas) {
            ofertaRepository.borrarOfertasPorCarga(carga.getIdCarga());
            cargaRepository.delete(carga);
        }

        // 3. Borrar las rutas del usuario(y las ofertas que las apuntan)
        List<Ruta> rutas = rutaRepository.findByUsuario_IdUsuario(id);
        for (Ruta ruta : rutas) {
            ofertaRepository.deleteByRutaId(ruta.getIdRuta()); // CORREGIDO AQUÍ
            rutaRepository.delete(ruta);
        }

        // 4. Finalmente, borrar al usuario
        usuarioRepository.deleteById(id);

        return ResponseEntity.ok().body("{\"mensaje\": \"Usuario y todos sus datos eliminados con éxito\"}");
    }
}