# VOTEchori Spring Boot Backend

## Stack
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- MySQL

## MySQL Configuration
Configured in application properties:
- Database: votechori
- User: root
- Password: 80134
- Port: 3306

File: src/main/resources/application.properties

## APIs
Base URL: http://localhost:8080/api

### Public
- GET /public/candidates
- GET /public/aadhaar/{aadhaarNumber}
- GET /public/health

### Auth
- POST /auth/register
- POST /auth/login
- GET /auth/me

### Citizen
- POST /votes

### Admin (observer merged in admin scope)
- GET /admin/stats
- GET /admin/reports
- GET /admin/audit-logs
- PATCH /admin/phase

### Reports
- POST /reports
- GET /reports/mine

## CORS
Allowed frontend origins are configured as:
- http://localhost:5173
- http://127.0.0.1:5173

## Seed Users
- admin@eci.gov.in / admin123 (ADMIN)
- observer@eci.gov.in / observer123 (mapped to ADMIN portal access)
- analyst@eci.gov.in / analyst123 (ANALYST)
- citizen@example.com / citizen123 (CITIZEN)

## Run
From backend folder:

1. Ensure Java 17 is installed
2. Ensure Maven is installed (or use mvnw if you add wrapper)
3. Start backend:
   - mvn spring-boot:run
