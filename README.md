# Route Optimizer Service

Microservicio Java Spring Boot que implementa el patrón **Request-Response with Status Polling** para optimización de rutas turísticas asíncronas. Actúa como intermediario entre el frontend Angular y el servicio Python MRL-AMIS.

## Características Principales

- ✅ **Patrón Request-Response with Status Polling** para procesos de larga duración
- ✅ **Arquitectura Asíncrona** con Spring `@Async`
- ✅ **Base de Datos H2 en Memoria** para tracking de trabajos
- ✅ **Datos Mock de Costa Rica** para pruebas
- ✅ **Documentación OpenAPI/Swagger**
- ✅ **Inyección de Constructor** (mejores prácticas)
- ✅ **Configuración CORS** para integración con Angular

## Arquitectura

```
Frontend Angular → Java Service → Python MRL-AMIS Service
                     ↓
                  H2 Database (Job Tracking)
```

## Stack Tecnológico

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database**
- **WebFlux** (Cliente HTTP reactivo)
- **Maven**
- **OpenAPI 3** (Swagger)

## Estructura del Proyecto

```
src/main/java/com/exiua/routeoptimizer/
├── RouteOptimizerServiceApplication.java
├── config/
│   ├── JobConfigurationProperties.java
│   ├── MockDataConfigurationProperties.java  
│   ├── OptimizationConfigurationProperties.java
│   └── WebConfig.java
├── controller/
│   └── RouteOptimizationController.java
├── dto/
│   ├── JobStatusResponseDTO.java
│   └── JobSubmissionResponseDTO.java
├── model/
│   ├── OptimizationJob.java
│   ├── POI.java
│   └── RouteOptimizationRequest.java
├── repository/
│   └── OptimizationJobRepository.java
└── service/
    ├── MockDataService.java
    └── RouteOptimizationService.java
```

## API Endpoints

### 1. Enviar Solicitud de Optimización (202 Accepted)
```http
POST /api/v1/routes/optimize
Content-Type: application/json

{
  "routeId": "ruta-aventura-123",
  "pois": [
    {
      "id": 1,
      "name": "Volcán Arenal",
      "latitude": 10.4628,
      "longitude": -84.7037,
      "category": "nature",
      "visitDuration": 120
    }
  ],
  "preferences": {
    "startTime": "09:00",
    "maxTravelTime": 480,
    "preferredCategories": ["nature", "adventure"]
  }
}
```

**Respuesta (202 Accepted):**
```json
{
  "jobId": "job-12345-abcde",
  "status": "PENDING",
  "message": "Route optimization request submitted successfully",
  "pollingUrl": "/api/v1/jobs/job-12345-abcde/status",
  "retryAfterSeconds": 2
}
```

### 2. Consultar Estado del Trabajo (Polling)
```http
GET /api/v1/jobs/{jobId}/status
```

**Respuesta (202 - Procesando):**
```json
{
  "jobId": "job-12345-abcde",
  "status": "PROCESSING",
  "progress": 45,
  "message": "Optimizing route using MRL-AMIS algorithm...",
  "retryAfterSeconds": 2
}
```

**Respuesta (200 - Completado):**
```json
{
  "jobId": "job-12345-abcde",
  "status": "COMPLETED",
  "progress": 100,
  "result": {
    "optimizedRouteId": "opt-route-67890",
    "optimizedSequence": [
      {
        "poiId": 1,
        "visitOrder": 1,
        "estimatedVisitTime": 120
      }
    ],
    "totalDistanceKm": 78.5,
    "totalTimeMinutes": 420,
    "optimizationAlgorithm": "MRL-AMIS",
    "optimizationScore": 0.92
  }
}
```

### 3. Cancelar Trabajo
```http
DELETE /api/v1/jobs/{jobId}
```

### 4. Obtener POIs Mock (Pruebas)
```http
GET /api/v1/pois/mock?routeType=adventure&count=8
GET /api/v1/pois/all
```

### 5. Health Check
```http
GET /api/v1/health
```

## Configuración

### application.properties
```properties
# Server
server.port=8080

# Database H2
spring.datasource.url=jdbc:h2:mem:routedb
spring.h2.console.enabled=true

# Async Configuration
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=20

# Custom Properties
job.default-timeout-minutes=10
job.polling-interval-seconds=2
optimization.simulation-delay-seconds=5
mock.data.enabled=true
```

## Datos Mock de Costa Rica

El servicio incluye 24 POIs (Puntos de Interés) reales de Costa Rica:

- **Natura**: Volcán Arenal, Monteverde, Manuel Antonio, Tortuguero
- **Aventura**: Zip-lining, Rafting, Surf, Canyoning  
- **Cultural**: Teatro Nacional, Mercados, Museos
- **Playa**: Guanacaste, Tamarindo, Jacó

## Cómo Ejecutar

### 1. Prerequisitos
- Java 21+
- Maven 3.8+

### 2. Compilar y Ejecutar
```bash
cd route-optimizer-service
mvn clean compile
mvn spring-boot:run
```

### 3. Acceder a la API
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/api/v1/health

## Integración con Frontend Angular

### Servicio Angular
```typescript
@Injectable()
export class RouteOptimizationService {
  private apiUrl = 'http://localhost:8080/api/v1';

  // 1. Enviar solicitud
  async submitOptimization(request: RouteOptimizationRequest) {
    const response = await this.http.post<JobSubmissionResponse>(
      `${this.apiUrl}/routes/optimize`, 
      request
    ).toPromise();
    
    // 2. Iniciar polling
    return this.pollJobStatus(response.jobId);
  }

  // 3. Polling con intervalos
  private pollJobStatus(jobId: string): Observable<JobStatusResponse> {
    return interval(2000).pipe(
      switchMap(() => this.http.get<JobStatusResponse>(
        `${this.apiUrl}/jobs/${jobId}/status`
      )),
      takeWhile(response => response.status !== 'COMPLETED', true)
    );
  }
}
```

## Estados de Trabajo

| Estado | Código HTTP | Descripción |
|--------|-------------|-------------|
| `PENDING` | 202 | Trabajo enviado, esperando procesamiento |
| `PROCESSING` | 202 | Algoritmo MRL-AMIS ejecutándose |
| `COMPLETED` | 200 | Optimización completada exitosamente |
| `FAILED` | 500 | Error durante el procesamiento |
| `CANCELLED` | 410 | Trabajo cancelado por el usuario |

## Patrón Request-Response with Status Polling

1. **Cliente** envía solicitud → **202 Accepted** + Job ID
2. **Cliente** hace polling cada 2 segundos → **202 Processing** o **200 Completed**
3. **Servidor** procesa asíncronamente con `@Async`
4. **Base de datos** rastrea el estado y progreso
5. **Cliente** obtiene resultado final cuando está listo



---

**Desarrollado para e-Xiua Tourism Platform** 🌴
