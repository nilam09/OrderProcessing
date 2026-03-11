# Order Processing System

An e-commerce order processing application built with Spring Boot, providing REST APIs for managing orders, products, and user authentication.

## Overview

This is a Spring Boot 4.0.3 application designed to handle order management for e-commerce platforms. It includes:
- RESTful APIs for order operations
- User authentication and authorization with Spring Security
- JPA/Hibernate ORM for database operations
- PostgreSQL database for data persistence
- Swagger/OpenAPI documentation
- Health checks and metrics with Spring Boot Actuator

## Prerequisites

- **Java 21** or higher
- **Maven 3.9.6** or higher
- **PostgreSQL 12+** (for database)

## Installation

### 1. Clone/Setup the Project

```bash
cd /OrderProcessing
```

### 2. Install Dependencies (if using Maven)

```bash
mvn clean install
```

## Running the Application

### Using Maven

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home \
/tmp/apache-maven-3.9.6/bin/mvn spring-boot:run
```

Or if Maven is installed globally:

```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

### Using Java Directly (From Built JAR)

```bash
java -jar target/order-processing-0.0.1-SNAPSHOT.jar
```

## Configuration

Application properties are defined in `src/main/resources/application.properties`:

| Property | Value | Description |
|----------|-------|-------------|
| `server.port` | 8080 | Server port |
| `spring.datasource.url` | PostgreSQL URL | Database connection |
| `spring.jpa.hibernate.ddl-auto` | update | Auto-create/update database schema |
| `spring.jpa.show-sql` | true | Show SQL queries in logs |

## Project Structure

```
OrderProcessing/
├── src/
│   ├── main/
│   │   ├── java/com/example/orderprocessing/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST API endpoints
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── model/           # Entity models (JPA)
│   │   │   └── repository/      # Data access layer
│   │   └── resources/
│   │       └── application.properties  # App configuration
│   └── test/                    # Unit and integration tests
├── target/                      # Compiled artifacts
├── pom.xml                      # Maven configuration
└── README.md                    # This file
```

## API Documentation

Once the application is running:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Available Endpoints

### Health & Metrics (Spring Boot Actuator)
- `GET /actuator/health` - Application health status
- `GET /actuator/metrics` - Available metrics
- `GET /actuator/info` - Application information
- `GET /actuator/prometheus` - Prometheus metrics

## Database

The application uses PostgreSQL with:
- **Host**: dpg-d6lvoevafjfc738prf70-a.oregon-postgres.render.com
- **Port**: 5432
- **Database**: orderprocessingdb_719k
- **Hibernate DDL**: `update` (auto-creates/updates schema)

### Database Initialization

SQL initialization is disabled (`spring.sql.init.mode=never`). Hibernate manages the schema automatically based on entity annotations.

## Logging

The application includes detailed logging for debugging:
- **Hibernate SQL Logging**: Enabled with TRACE level for parameter binding
- **Hibernate Statistics**: Enabled to monitor N+1 query issues

## Testing

Run tests with Maven Surefire plugin:

```bash
mvn test
```

Test dependencies include:
- JUnit 5
- Spring Security Test
- H2 in-memory database for testing

## Build & Deployment

### Build WAR/JAR

```bash
mvn clean package
```

This creates a JAR file in `target/` directory.

### Run Built Application

```bash
java -jar target/order-processing-0.0.1-SNAPSHOT.jar
```

## Dependencies

Key dependencies used:
- **Spring Boot 4.0.3**: Web framework and ORM
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database abstraction
- **PostgreSQL Driver**: Database connectivity
- **SpringDoc OpenAPI 2.8.0**: Swagger documentation
- **Jackson**: JSON processing with Java 8 time and Hibernate support

For complete dependency list, see `pom.xml`.

## Troubleshooting

### Maven Not Found
If `mvn` command is not found, use the full path to the downloaded Maven:
```bash
/tmp/apache-maven-3.9.6/bin/mvn spring-boot:run
```

### Database Connection Issues
- Ensure PostgreSQL is running and accessible
- Verify connection URL, username, and password in `application.properties`
- Check network connectivity to the database server

### Port Already in Use
If port 8080 is already in use, change it in `application.properties`:
```properties
server.port=8081
```

## Support & Documentation

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Last Updated**: March 2026
