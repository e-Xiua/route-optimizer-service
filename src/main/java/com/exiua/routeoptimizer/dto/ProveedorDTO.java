package com.exiua.routeoptimizer.dto;

import lombok.Data;

@Data
public class ProveedorDTO {
    private Long idProveedor;
    private String nombre;
    private String nombre_empresa;
    private String cargoContacto;
    private String telefono;
    private String telefonoEmpresa;
    private String coordenadaX;
    private String coordenadaY;
}
