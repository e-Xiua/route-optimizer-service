package com.exiua.routeoptimizer.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.exiua.routeoptimizer.dto.ServicioXPreferenciaDTO;

@FeignClient(name = "preferencias-ms", url = "http://localhost:8081/api/servicioXPreferencia")
public interface PreferenciasApiClient {

    @GetMapping("/servicio/{idServicio}")
    List<ServicioXPreferenciaDTO> obtenerPreferenciasPorServicio(@PathVariable("idServicio") Long idServicio);
    
    

}
