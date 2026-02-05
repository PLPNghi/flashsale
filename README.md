# Flash Sale System (Spring Boot)

## Overview

Flash Sale Service is a RESTful API backend application designed to handle flash sale programs with the following capabilities:

- **User Authentication**: Registration and login via email/phone with OTP verification
- **Flash Sale Management**: Configure and manage time-based flash sale campaigns
- **Order Processing**: Purchase flash sale items with inventory control and daily purchase limits
- **Security**: JWT authentication and Spring Security integration
- **Inventory Sync**: Comprehensive logging of all inventory changes

---

## Tech Stack

* **Java 17**
* **Spring Boot**
* **Spring Security + JWT**
* **Spring Data JPA (Hibernate)**
* **MySQL**
* **Maven**
* **JUnit 5 + Mockito**
* **Docker & Docker Compose**

---

## Project Structure

```text
flashsale/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── diagram.png
├── postman_collection.json
├── src
│   ├── main
│   │   ├── java/com/example/flashsale
│   │   │   ├── config/                # Security & application configs
│   │   │   ├── controller/            # REST Controllers
│   │   │   ├── dto/                   # Request / Response DTOs
│   │   │   ├── entity/                # JPA Entities
│   │   │   ├── repository/            # Spring Data JPA Repositories
│   │   │   ├── service/               # Service interfaces
│   │   │   └── service/impl/          # Business logic implementations
│   │   └── resources/
│   │       └── application.yml
│   │       └── schema.sql          # Database schema
│   │       └── data.sql            # Initial data
│   └── test
│       └── java/com/example/flashsale
│           └── service/impl/          # Unit tests (JUnit + Mockito)
```

---

## Database Design

### Main Tables

#### `user`

* Stores user authentication information
* Used with Spring Security + JWT

#### `product`

* Represents a product participating in flash sale

#### `flash_sale_config`

* Flash sale configuration (time window, limits, etc.)

#### `flash_sale_order`

* Stores successful purchase orders
* Used to prevent overselling

#### `otp_verification`

* OTP verification for authentication

#### `inventory_sync_log`

* Logs inventory synchronization actions
* Helps track consistency between order & inventory state

> Database schema and flow can be referenced in **diagram.png**

---

## Authentication Flow

1. User logs in via `AuthController`
2. System validates credentials
3. JWT token is generated and returned
4. All protected APIs require `Authorization: Bearer <token>`

---

## Flash Sale Purchase Flow

1. Client sends purchase request
2. System validates flash sale availability
3. Transaction starts
4. Order is created
5. Inventory sync is triggered
6. Transaction commits

---

## Get Current Flash Sale Flow

1. Client sends request to get current flash sale
2. Controller forwards request to `FlashSaleService`
3. Service queries database for flash sale configs where:
    * `startTime <= now`
    * `endTime >= now`
4. System selects the active flash sale (if exists)
5. Flash sale data is mapped to response DTO
6. Response is returned to client

---

## Run with Docker

### Build & Start Services

```bash
docker-compose up --build
```

This will start:

* MySQL
* Spring Boot application

### Application Ports

* Backend API: `http://localhost:8080`
* MySQL: `localhost:3306`

### Check Logs

```bash
docker-compose logs -f app
docker-compose logs -f mysql
```

### Stop Services

```bash
docker-compose down
```

---

## Application Configuration

Main configuration file:

```yaml
src/main/resources/application.yml
```

Contains:

* Database connection
* JPA configuration
* JWT settings

---

## Testing

Unit tests are written using:

* **JUnit 5**
* **Mockito**

Test focus:

* Business logic correctness
* Service-level testing (no controller tests)

Run tests:

```bash
mvn test
```

---

## API Testing

A Postman collection is provided:

```text
postman_collection.json
```

You can import it into Postman to test:

* Authentication
* Flash sale purchase APIs

**Notes: Needs to set 'access_token' as environment variable**
