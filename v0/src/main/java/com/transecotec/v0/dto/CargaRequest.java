package com.transecotec.v0.dto;

import lombok.Data;

@Data
public class CargaRequest {
    private Long idUsuario;
    private String origen;
    private String destino;
    private Double pesoVolumen;
    private String descripcion;
    private String estado;
}
