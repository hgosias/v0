package com.transecotec.v0.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "carga")
public class Carga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carga")
    private Long idCarga;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    private String origen;
    private String destino;

    @Column(name = "peso_volumen")
    private Double pesoVolumen;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String estado = "Publicada";

    @Column(name = "fecha_publicacion", insertable = false, updatable = false)
    private LocalDateTime fechaPublicacion;
}