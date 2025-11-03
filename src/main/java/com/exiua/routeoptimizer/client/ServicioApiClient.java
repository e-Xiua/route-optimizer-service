package com.exiua.routeoptimizer.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.exiua.routeoptimizer.dto.ServicioDTO;

@FeignClient(name = "servicio-ms", url = "${feign.client.servicio.url:http://localhost:8080}")
public interface ServicioApiClient {

    @GetMapping("/api/servicio/{idProveedor}/servicios")
    List<ServicioDTO> obtenerServiciosPorProveedor(@PathVariable("idProveedor") Long idProveedor);

}
