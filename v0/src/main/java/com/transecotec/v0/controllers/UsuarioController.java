package com.transecotec.v0.controllers;

import com.transecotec.v0.dto.LoginRequest;
import com.transecotec.v0.models.Usuario;
import com.transecotec.v0.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*") // Permite peticiones desde tu frontend en local
public class UsuarioController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. Obtener todos los usuarios
    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    // 2. Crear un nuevo usuario - Registro (Restaurado el @PostMapping)
    @PostMapping
    public ResponseEntity<?> createUsuario(@RequestBody Usuario usuario) {
        // Validación básica para no duplicar correos
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo ya está registrado");
        }

        // --- CIFRADO DE CONTRASEÑA ---
        // Usamos getContrasena() para que coincida con la nomenclatura de tu modelo
        String passwordPlana = usuario.getContrasena();

        // La pasamos por el algoritmo BCrypt
        String passwordCifrada = passwordEncoder.encode(passwordPlana);

        // Reemplazamos la contraseña plana por la cifrada en el objeto
        usuario.setContrasena(passwordCifrada);
        // --------------------------------------

        // Ahora sí, guardamos el usuario en la base de datos con la contraseña segura
        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        return ResponseEntity.ok(nuevoUsuario);
    }

    // 3. NUEVO: Iniciar Sesión (Login) con validación segura
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Buscamos al usuario por su email
        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail());

        // Si el usuario no existe, devolvemos error
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Correo o contraseña incorrectos");
        }

        // --- VALIDACIÓN BCRYPT ---
        // Comparamos la contraseña escrita en el frontend con el hash de la BD usando matches()
        boolean passwordCorrecta = passwordEncoder.matches(loginRequest.getContrasena(), usuario.getContrasena());

        if (passwordCorrecta) {
            // Por seguridad, no devolvemos el hash al frontend
            usuario.setContrasena(null);

            // Devolvemos el usuario (con status 200 OK)
            return ResponseEntity.ok(usuario);
        } else {
            // Si la contraseña no coincide, devolvemos error 401
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

        // Actualizamos los datos básicos
        usuarioExistente.setNombre(datosActualizados.getNombre());
        usuarioExistente.setEmail(datosActualizados.getEmail());
        usuarioExistente.setCifDni(datosActualizados.getCifDni());

        // Solo actualizamos la contraseña si el usuario ha escrito una nueva
        if (datosActualizados.getContrasena() != null && !datosActualizados.getContrasena().trim().isEmpty()) {
            // ¡IMPORTANTE! Si el usuario cambia la contraseña, también la ciframos antes de guardar
            String nuevaContrasenaCifrada = passwordEncoder.encode(datosActualizados.getContrasena());
            usuarioExistente.setContrasena(nuevaContrasenaCifrada);
        }

        // Guardamos los cambios
        Usuario usuarioGuardado = usuarioRepository.save(usuarioExistente);

        // Por seguridad, no devolvemos la contraseña al frontend
        usuarioGuardado.setContrasena(null);

        return ResponseEntity.ok(usuarioGuardado);
    }

    // --- MÉTODOS EXCLUSIVOS DEL ADMINISTRADOR ---

    // 1. Obtener lista de usuarios pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<List<Usuario>> getUsuariosPendientes() {
        // Busca todos los usuarios cuyo estado sea "Pendiente"
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

    // 3. Eliminar usuario permanentemente (Administrador)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        usuarioRepository.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado con éxito");
    }
}