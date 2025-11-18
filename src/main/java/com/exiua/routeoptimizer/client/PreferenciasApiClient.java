package com.exiua.routeoptimizer.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.exiua.routeoptimizer.dto.PreferenciasDTO;
import com.exiua.routeoptimizer.dto.ServicioXPreferenciaDTO;

@FeignClient(name = "preferencias-ms", url = "${feign.client.preferencias.url:http://localhost:8081}")
public interface PreferenciasApiClient {

    @GetMapping("/api/servicioXPreferencia/servicio/{idServicio}")
    List<ServicioXPreferenciaDTO> obtenerPreferenciasPorServicio(@PathVariable("idServicio") Long idServicio);
    
    @GetMapping("/api/preferencias/buscar/{id}")
    PreferenciasDTO obtenerPreferenciaPorId(@PathVariable("id") Long id);
    
}
