package com.exiua.routeoptimizer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.exiua.routeoptimizer.config.FeignClientInterceptor;
import com.exiua.routeoptimizer.dto.ProveedorDTO;


@FeignClient(
    name = "admin-users-service", 
    url = "${feign.client.proveedor.url}",
    configuration = FeignClientInterceptor.class
)
public interface ProviderApiClient {

    @GetMapping("/perfil-publico/{idUsuario}")
    ProveedorDTO  obtenerProveedor(@PathVariable("idUsuario") Long idUsuario);
    
}
