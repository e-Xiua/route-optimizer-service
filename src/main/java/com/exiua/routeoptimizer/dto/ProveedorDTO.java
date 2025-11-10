package com.exiua.routeoptimizer.dto;

import lombok.Data;

@Data
public class ProveedorDTO {
    private Long id;  // From UsuarioResponseDTO
    private Long idProveedor;
    private String nombre;
    private String nombre_empresa;
    private String cargoContacto;
    private String telefono;
    private String telefonoEmpresa;
    private String coordenadaX;
    private String coordenadaY;
    
    // Nested ProveedorInfo from UsuarioResponseDTO
    private ProveedorInfo proveedorInfo;
    
    @Data
    public static class ProveedorInfo {
        private Long id;
        private String nombreEmpresa;
        private String nombre_empresa;
        private String coordenadaX;
        private String coordenadaY;
        private String cargoContacto;
        private String telefono;
        private String telefonoEmpresa;
    }
    
    // Helper methods to get coordinates from nested structure or flat structure
    public String getCoordenadaX() {
        if (proveedorInfo != null && proveedorInfo.getCoordenadaX() != null) {
            return proveedorInfo.getCoordenadaX();
        }
        return coordenadaX;
    }
    
    public String getCoordenadaY() {
        if (proveedorInfo != null && proveedorInfo.getCoordenadaY() != null) {
            return proveedorInfo.getCoordenadaY();
        }
        return coordenadaY;
    }
    
    public Long getIdProveedor() {
        if (proveedorInfo != null && proveedorInfo.getId() != null) {
            return proveedorInfo.getId();
        }
        if (id != null) {
            return id;
        }
        return idProveedor;
    }
    
    public String getNombre_empresa() {
        if (proveedorInfo != null && proveedorInfo.getNombre_empresa() != null) {
            return proveedorInfo.getNombre_empresa();
        }
        if (proveedorInfo != null && proveedorInfo.getNombreEmpresa() != null) {
            return proveedorInfo.getNombreEmpresa();
        }
        return nombre_empresa;
    }
}
