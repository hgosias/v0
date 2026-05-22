package com.transecotec.v0.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    private String nombre;
    private String email;
    private String contrasena;
    private String rol;

    @Column(name = "cif_dni")
    private String cifDni;

    @Column(name = "estado_verificacion")
    private String estadoVerificacion = "Pendiente";

    @Lob
    @Column(name = "documento_base64", columnDefinition = "LONGTEXT")
    private String documentoBase64;
}