package com.transecotec.v0.dto;

import lombok.Data;
import java.time.LocalDate;

@Data // Lombok genera los getters y setters automáticamente
public class RutaRequest {
    private Long idUsuario;
    private String origen;
    private String destino;
    private LocalDate fechaSalida;
    private Double capacidadLibre;
    private String estado;
}

