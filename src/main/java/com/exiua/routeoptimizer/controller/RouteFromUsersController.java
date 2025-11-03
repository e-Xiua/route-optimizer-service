package com.exiua.routeoptimizer.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exiua.routeoptimizer.dto.CompletedRouteResponseDTO;
import com.exiua.routeoptimizer.service.RoutesFromUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller implementing Request-Response with Status Polling pattern
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Getting Routes", description = "Get Routes from Users")
public class RouteFromUsersController {

    private static final Logger logger = LoggerFactory.getLogger(RouteFromUsersController.class);
    private final RoutesFromUserService routesFromUserService;

    public RouteFromUsersController(RoutesFromUserService routesFromUserService) {
        this.routesFromUserService = routesFromUserService;
    }

    /**
     * Get all completed route optimizations
     */
    @GetMapping("/routes/completed")
    @Operation(summary = "Get all completed routes", 
               description = "Returns all route optimizations that have been completed successfully")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved completed routes"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CompletedRouteResponseDTO>> getCompletedRoutes(
            @Parameter(description = "Filter by user ID (optional)")
            @RequestParam(required = false) String userId) {
        
        try {
            List<CompletedRouteResponseDTO> completedRoutes;
            if (userId != null && !userId.isEmpty()) {
                logger.info("Fetching completed routes for user: {}", userId);
                completedRoutes = routesFromUserService.getCompletedRoutesByUser(userId);
            } else {
                logger.info("Fetching all completed routes");
                completedRoutes = routesFromUserService.getCompletedRoutes();
            }
            return ResponseEntity.ok(completedRoutes);
        } catch (Exception e) {
            logger.error("Error fetching completed routes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}
