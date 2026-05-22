package com.transecotec.v0.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ruta")

public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Long idRuta;

    // Relación: Muchas Rutas pertenecen a Un Usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    private String origen;
    private String destino;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(name = "capacidad_libre")
    private Double capacidadLibre;

    private String estado = "Publicada";

    // Dejamos que MySQL ponga la fecha de publicación automáticamente
    @Column(name = "fecha_publicacion", insertable = false, updatable = false)
    private LocalDateTime fechaPublicacion;
}
