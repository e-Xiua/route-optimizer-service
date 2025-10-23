package com.exiua.routeoptimizer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.exiua.routeoptimizer.config.FeignClientInterceptor;
import com.exiua.routeoptimizer.dto.ProveedorDTO;


@FeignClient(
    name = "proveedor-ms", 
    url = "http://localhost:8082/usuarios",
    configuration = FeignClientInterceptor.class
)
public interface ProviderApiClient {

    @GetMapping("/perfil-publico/{idUsuario}")
    ProveedorDTO  obtenerProveedor(@PathVariable("idUsuario") Long idUsuario);
    
}
