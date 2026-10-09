# Spring Security JWT MySQL

## Stack
- Java 21
- Spring Boot 3.4.5
- Spring Security 6
- Spring Data JPA / Hibernate
- MySQL 8
- JJWT 0.12.6
- Lombok
- Jakarta Bean Validation
- Maven

## 1. Create database

```sql
CREATE DATABASE securitydb;
```

Hibernate creates/updates the `users` table because:

```properties
spring.jpa.hibernate.ddl-auto=update
```

For production, prefer Flyway/Liquibase migrations and `ddl-auto=validate`.

## 2. Environment variables

Windows PowerShell:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-mysql-password"
$env:JWT_SECRET="replace-with-a-random-secret-at-least-32-characters"
$env:JWT_EXPIRATION="86400000"
```

Then:

```powershell
mvn clean spring-boot:run
```

Or build:

```powershell
mvn clean package
java -jar target/spring-security-jwt-mysql-0.0.1-SNAPSHOT.jar
```

## 3. APIs

### Register USER

POST `http://localhost:8080/api/auth/register`

```json
{
  "username": "prasanth",
  "email": "prasanth@gmail.com",
  "password": "password123",
  "role": "USER"
}
```

### Login

POST `http://localhost:8080/api/auth/login`

```json
{
  "username": "prasanth",
  "password": "password123"
}
```

Response:

```json
{
  "token": "eyJ...",
  "type": "Bearer",
  "username": "prasanth",
  "role": "USER"
}
```

### Profile

GET `http://localhost:8080/api/users/profile`

Header:

```text
Authorization: Bearer <JWT_TOKEN>
```

### User dashboard

GET `http://localhost:8080/api/user/dashboard`

Header:

```text
Authorization: Bearer <JWT_TOKEN>
```

### Admin dashboard

GET `http://localhost:8080/api/admin/dashboard`

Requires an ADMIN token.

### All users

GET `http://localhost:8080/api/users`

Requires an ADMIN token.

## Role mapping

The database stores:

```text
USER
ADMIN
```

Spring Security authorities are:

```text
ROLE_USER
ROLE_ADMIN
```

Therefore:

```java
hasRole("USER")
```

internally checks:

```text
ROLE_USER
```

while:

```java
hasAuthority("ROLE_USER")
```

checks the exact authority.

## Authentication flow

Register:

```text
Client
  -> AuthController
  -> AuthService
  -> BCrypt
  -> UserRepository
  -> MySQL
```

Login:

```text
Client
  -> AuthController
  -> AuthenticationManager
  -> DaoAuthenticationProvider
  -> UserDetailsService
  -> MySQL
  -> BCrypt password verification
  -> JwtService
  -> JWT
  -> Client
```

Protected request:

```text
Client
  -> Authorization: Bearer JWT
  -> JwtAuthenticationFilter
  -> JwtService validation
  -> UserDetailsService
  -> SecurityContextHolder
  -> Authorization
  -> Controller
  -> Service/Repository
  -> MySQL
```
