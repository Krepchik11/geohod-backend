# Tasks: Geohod Backend

This document captures repetitive workflows to execute similar changes quickly and consistently. Follow these steps when performing the associated tasks.

## Add or Update v2 API Endpoint

Last verified: 2025-10-03

Files typically involved:
- Controller: src/main/java/me/geohod/geohodbackend/api/controller/v2/...
- DTOs:
  - Request: src/main/java/me/geohod/geohodbackend/api/dto/request/...
  - Response: src/main/java/me/geohod/geohodbackend/api/dto/response/...
- Mapper: src/main/java/me/geohod/geohodbackend/api/mapper/...
- Service interface: src/main/java/me/geohod/geohodbackend/service/I*.java
- Service implementation: src/main/java/me/geohod/geohodbackend/service/impl/*.java
- Repositories and projections if needed:
  - src/main/java/me/geohod/geohodbackend/data/model/repository/...
  - src/main/java/me/geohod/geohodbackend/data/dto/...
- ApiResponse wrapper: src/main/java/me/geohod/geohodbackend/api/response/ApiResponse.java
- OpenAPI docs auto-generated (check annotations if needed)

Steps:
1. Define request/response DTO records with validation annotations (e.g., `@NotNull`, `@Min`, `@Max` for fields like `amountOfParticipants`).
2. For optional request bodies, design DTOs with default values in service logic to ensure backward compatibility (e.g., default `amountOfParticipants` to 1 if body is absent).
3. Add or update MapStruct API mapper interface to translate between API DTOs and service layer DTOs/entities.
4. Add controller method in v2 controller:
   - Annotate with appropriate @GetMapping/@PostMapping/etc.
   - Accept optional request DTO (use `@RequestBody(required = false)` if needed) and map to service call.
   - Handle cases where body is absent by providing defaults.
   - Return ResponseEntity<ApiResponse<T>> with ApiResponse.success(data).
5. Extend service interface and implement in service/impl:
   - Encapsulate business logic, authorization checks, and transactions.
   - Apply defaults and validation in service methods (e.g., loop for multi-participant creation based on amount).
   - Use repositories and projections; do not access DB directly.
6. Update repositories or projections if a new query is required.
7. Run tests and verify OpenAPI (enable dev profile).
8. Security: confirm endpoint falls under /api/v2/** (already authenticated by SecurityConfiguration).

Gotchas:
- Keep v2 responses consistently wrapped with ApiResponse<T>.
- Ensure Telegram authentication is required (no extra permitAll on the path).
- Use projection DTOs for read queries to avoid N+1 and over-fetching.
- For optional bodies, test both with and without request body to confirm defaults work.