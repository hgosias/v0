package com.transecotec.v0.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "oferta_acuerdo")
public class OfertaAcuerdo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta")
    private Long idOferta;

    // Relación con el usuario que envía el correo (Transportista o Empresa)
    @ManyToOne
    @JoinColumn(name = "id_usuario_emisor", nullable = false)
    private Usuario usuarioEmisor;

    // Relación con la ruta (Se llena si una Empresa contacta a un Transportista)
    @ManyToOne
    @JoinColumn(name = "id_ruta")
    private Ruta ruta;

    // Relación con la carga (Se llena si un Transportista contacta a una Empresa)
    @ManyToOne
    @JoinColumn(name = "id_carga")
    private Carga carga;

    // Guardamos el correo electrónico de contacto
    @Column(name = "email_contacto", nullable = false)
    private String emailContacto;

    // Estado por defecto
    private String estado = "Pendiente";

    // Fecha automática generada por MySQL
    @Column(name = "fecha_oferta", insertable = false, updatable = false)
    private LocalDateTime fechaOferta;
}