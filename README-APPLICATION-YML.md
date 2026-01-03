# Application Configuration (application.yml)

This document describes the configuration settings in `application.yml` for the Drools v4 WebFlux application.

## Overview

The `application.yml` file contains the Spring Boot configuration for this reactive web application. The application uses:
- **Spring WebFlux** for reactive web programming
- **R2DBC** (Reactive Relational Database Connectivity) for reactive database access
- **MariaDB** as the database

## Configuration Sections

### Spring Application

```yaml
spring:
  application:
    name: drools_v4_webflux
```

- **Purpose**: Sets the application name used for service discovery and logging
- **Value**: `drools_v4_webflux` - The name of this Spring Boot application

### R2DBC Database Configuration

```yaml
spring:
  r2dbc:
    url: r2dbc:mariadb://localhost:3306/${MYSQL_DATABASE}
    username: ${MYSQL_USER}
    password: ${MYSQL_PASSWORD}
```

#### Connection URL
- **Format**: `r2dbc:mariadb://host:port/database`
- **Host**: `localhost` - Database server address
- **Port**: `3306` - Default MariaDB/MySQL port
- **Database**: `${MYSQL_DATABASE}` - Environment variable for database name

#### Authentication
- **Username**: `${MYSQL_USER}` - Environment variable for database user
- **Password**: `${MYSQL_PASSWORD}` - Environment variable for database password

> **Note**: These values use environment variables for security. Make sure to set them before running the application.

### Connection Pool Settings

```yaml
spring:
  r2dbc:
    pool:
      initial-size: 5
      max-size: 10
      max-idle-time: 30m
```

#### Pool Configuration
- **initial-size**: `5` - Number of connections created when the pool is initialized
- **max-size**: `10` - Maximum number of connections in the pool
- **max-idle-time**: `30m` - Maximum time a connection can remain idle before being closed

> **Note**: R2DBC uses reactive connection pooling, which is different from traditional blocking JDBC pools. These settings control the reactive connection pool behavior.

### Server Configuration

```yaml
server:
  port: 8081
```

- **Port**: `8081` - The port on which the Spring Boot application will listen for HTTP requests
- **Default**: If not specified, Spring Boot defaults to port `8080`

## Environment Variables

The application requires the following environment variables to be set:

| Variable | Description | Example |
|----------|-------------|---------|
| `MYSQL_DATABASE` | Name of the database to connect to | `drools_db` |
| `MYSQL_USER` | Database username | `drools_user` |
| `MYSQL_PASSWORD` | Database password | `secure_password` |

### Setting Environment Variables

#### Windows (PowerShell)
```powershell
$env:MYSQL_DATABASE="drools_db"
$env:MYSQL_USER="drools_user"
$env:MYSQL_PASSWORD="secure_password"
```

#### Windows (Command Prompt)
```cmd
set MYSQL_DATABASE=drools_db
set MYSQL_USER=drools_user
set MYSQL_PASSWORD=secure_password
```

#### Linux/macOS
```bash
export MYSQL_DATABASE=drools_db
export MYSQL_USER=drools_user
export MYSQL_PASSWORD=secure_password
```

#### Using .env file (with Docker Compose)
If you're using Docker Compose (see `docker-compose.yml`), create a `.env` file in the project root:

```env
MYSQL_ROOT_PASSWORD=root_password
MYSQL_DATABASE=drools_db
MYSQL_USER=drools_user
MYSQL_PASSWORD=secure_password
```

## Database Connection

### Using Docker Compose

The project includes a `docker-compose.yml` file that sets up a MariaDB container. To start the database:

```bash
docker-compose up -d
```

This will:
- Start MariaDB on port `3306`
- Create the database specified in `MYSQL_DATABASE`
- Create the user specified in `MYSQL_USER` with the password from `MYSQL_PASSWORD`

### Manual Database Setup

If you're running MariaDB/MySQL manually, ensure:
1. The database server is running on `localhost:3306`
2. The database specified in `MYSQL_DATABASE` exists
3. The user specified in `MYSQL_USER` has proper permissions

## Configuration for Different Environments

### Development
The current configuration is suitable for development. For production, consider:

```yaml
spring:
  r2dbc:
    pool:
      initial-size: 10
      max-size: 50
      max-idle-time: 20m
```

### Production
For production environments, you should:
1. Use externalized configuration (e.g., environment variables, secrets management)
2. Adjust connection pool sizes based on expected load
3. Use a proper database hostname instead of `localhost`
4. Enable SSL/TLS for database connections if required

Example production configuration:

```yaml
spring:
  r2dbc:
    url: r2dbc:mariadb://db.example.com:3306/${MYSQL_DATABASE}?sslMode=REQUIRED
    username: ${MYSQL_USER}
    password: ${MYSQL_PASSWORD}
    pool:
      initial-size: 10
      max-size: 50
      max-idle-time: 20m
```

## Important Notes

1. **R2DBC vs JPA**: This application uses R2DBC for reactive database access. R2DBC does not use JPA/Hibernate. The configuration file includes a comment about JPA, but it's not used in this reactive setup.

2. **Reactive Programming**: Since this is a WebFlux application, all database operations are non-blocking and reactive. Make sure your code uses reactive types (`Mono`, `Flux`) when working with the database.

3. **Connection Pooling**: R2DBC connection pooling is different from traditional JDBC pooling. The pool settings control reactive connection management.

4. **Security**: Never commit actual database credentials to version control. Always use environment variables or secure configuration management.

## Troubleshooting

### Connection Issues
- Verify MariaDB is running: `docker ps` (if using Docker) or check the service status
- Check environment variables are set correctly
- Verify database name, username, and password match your database setup
- Ensure the database exists and the user has proper permissions

### Port Conflicts
- If port `8081` is already in use, change it in the `server.port` configuration
- If port `3306` is already in use by another database, update the R2DBC URL

### Pool Exhaustion
- If you see connection pool errors, consider increasing `max-size`
- Monitor connection usage and adjust pool settings based on your application's needs

## Related Files

- `docker-compose.yml` - Docker Compose configuration for MariaDB
- `build.gradle` - Project dependencies including R2DBC and MariaDB drivers
- `README-DOCKER-COMPOSE.md` - Documentation for Docker Compose setup




