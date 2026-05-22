package com.transecotec.v0.dto;

import lombok.Data;

@Data
public class OfertaRequest {
    private Long idUsuarioEmisor;
    private Long idRuta;
    private Long idCarga;
    private String emailContacto;
}