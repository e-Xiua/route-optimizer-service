package com.exiua.routeoptimizer.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.exiua.routeoptimizer.dto.ServicioDTO;

@FeignClient(name = "servicio-ms", url = "http://localhost:8080/api/servicio")

public interface ServicioApiClient {

    @GetMapping("/{idProveedor}/servicios")
    List<ServicioDTO> obtenerServiciosPorProveedor(@PathVariable("idProveedor") Long idProveedor);

}
