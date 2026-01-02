# MariaDB Docker Compose Setup

This document provides a detailed explanation of the `docker-compose.yml` configuration for running MariaDB database in a Docker container.

## 📋 Table of Contents

- [Overview](#overview)
- [Configuration Breakdown](#configuration-breakdown)
- [Service Configuration Details](#service-configuration-details)
- [Usage Instructions](#usage-instructions)
- [Connection Details](#connection-details)
- [Common Operations](#common-operations)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

This Docker Compose file sets up a MariaDB database server that can be used for local development. MariaDB is a community-developed fork of MySQL and is fully compatible with MySQL.

## 📝 Configuration Breakdown

### Service: `mariadb`

The main service definition for the MariaDB database container.

#### Image
image: mariadb:latest- **Purpose**: Specifies the Docker image to use
- **Version**: `latest` tag pulls the most recent stable MariaDB version
- **Note**: For production, consider pinning to a specific version (e.g., `mariadb:10.11`)

#### Container Name
container_name: drools-mariadb- **Purpose**: Assigns a fixed name to the container
- **Benefit**: Makes it easier to reference the container in commands
- **Alternative**: If omitted, Docker Compose generates a name based on project and service name

#### Restart Policy
restart: unless-stopped- **Purpose**: Defines when the container should automatically restart
- **Options**:
  - `no`: Never restart (default)
  - `always`: Always restart, even if manually stopped
  - `on-failure`: Restart only on failure
  - `unless-stopped`: Restart always, except when manually stopped
- **Use Case**: Ensures database stays running after system reboots

#### Environment Variables
environment:
  MYSQL_ROOT_PASSWORD: rootpassword
  MYSQL_DATABASE: drools_db
  MYSQL_USER: drools_user
  MYSQL_PASSWORD: drools_password**Detailed Explanation:**

1. **MYSQL_ROOT_PASSWORD**
   - Sets the password for the root user
   - Root user has full administrative privileges
   - **Security Note**: Change this in production environments
   - **Default**: None (must be set)

2. **MYSQL_DATABASE**
   - Creates a database named `drools_db` on container initialization
   - Database is created if it doesn't exist
   - **Use Case**: Pre-configured database for your application

3. **MYSQL_USER**
   - Creates a new database user `drools_user`
   - User is granted privileges on the `MYSQL_DATABASE`
   - **Best Practice**: Use non-root user for application connections

4. **MYSQL_PASSWORD**
   - Sets the password for `MYSQL_USER`
   - Used for application database connections
   - **Security Note**: Keep this secure and change from default

#### Port Mappingaml
ports:
  - "3306:3306"- **Format**: `"HOST_PORT:CONTAINER_PORT"`
- **Purpose**: Maps container port 3306 to host port 3306
- **Access**: Database accessible at `localhost:3306` from host machine
- **Security**: Exposes database to host; consider firewall rules in production

#### Volumes
volumes:
  - mariadb_data:/var/lib/mysql- **Purpose**: Persists database data outside the container
- **Location**: `/var/lib/mysql` is MariaDB's default data directory
- **Benefit**: Data survives container removal and recreation
- **Volume Name**: `mariadb_data` (defined in volumes section)

#### Health Check
healthcheck:
  test: ["CMD", "healthcheck.sh", "--connect", "--innodb_initialized"]
  interval: 10s
  timeout: 5s
  retries: 5**Detailed Explanation:**

- **test**: Command to check container health
  - `healthcheck.sh`: MariaDB's built-in health check script
  - `--connect`: Tests database connection
  - `--innodb_initialized`: Ensures InnoDB storage engine is ready

- **interval**: Time between health checks (10 seconds)
  - Checks every 10 seconds if database is healthy

- **timeout**: Maximum time for health check to complete (5 seconds)
  - If check takes longer, it's considered failed

- **retries**: Number of consecutive failures before marking unhealthy (5)
  - Container marked unhealthy after 5 failed checks

- **Use Case**: Allows dependent services to wait for database readiness

#### Networks
networks:
  - drools-network- **Purpose**: Connects container to a custom Docker network
- **Benefit**: Isolation and communication between containers
- **Network Name**: `drools-network` (defined in networks section)

### Volumes Section
volumes:
  mariadb_data:
    driver: local- **Purpose**: Defines named volumes for data persistence
- **Volume Name**: `mariadb_data`
- **Driver**: `local` stores data on host filesystem
- **Location**: Managed by Docker (typically in Docker's data directory)
- **Alternative Drivers**: `nfs`, `cifs`, cloud storage drivers for distributed storage

### Networks Section
networks:
  drools-network:
    driver: bridge- **Purpose**: Defines a custom Docker network
- **Network Name**: `drools-network`
- **Driver**: `bridge` creates an internal network
- **Benefit**: Containers on same network can communicate by service name
- **Use Case**: Connect multiple services (app, database, cache, etc.)

## 🚀 Usage Instructions

### Prerequisites
- Docker installed and running
- Docker Compose installed (usually included with Docker Desktop)

### Starting the Database

1. **Start the container:**
   docker-compose up -d
      - `-d` flag runs containers in detached mode (background)
   - Downloads image if not present
   - Creates network and volume if needed
   - Starts the MariaDB container

2. **Verify container is running:**
   docker-compose ps
      - Shows status of all services
   - Check that `mariadb` service shows "Up" status

3. **Check container logs:**
   docker-compose logs mariadb
      - View startup logs
   - Monitor for any errors
   - Look for "ready for connections" message

### Stopping the Database

1. **Stop containers (keeps data):**
   docker-compose stop
   2. **Stop and remove containers (keeps data):**sh
   docker-compose down
   3. **Stop, remove containers, and delete volumes (⚠️ deletes data):**
   docker-compose down -v
   ## 🔌 Connection Details

### From Host Machine (Spring Boot Application)

Use these connection parameters in your `application.yml`:
ml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/drools_db
    username: drools_user
    password: drools_password
    driver-class-name: org.mariadb.jdbc.Driver### From Another Docker Container

If your Spring Boot app runs in Docker on the same network:
l
spring:
  datasource:
    url: jdbc:mariadb://mariadb:3306/drools_db
    username: drools_user
    password: drools_password
    driver-class-name: org.mariadb.jdbc.Driver**Note**: Use service name `mariadb` instead of `localhost` when connecting from another container.

### Direct Database Access

**Using MySQL/MariaDB Client:**
mysql -h localhost -P 3306 -u drools_user -p drools_db
# Password: drools_password**Using Docker Exec:**
docker exec -it drools-mariadb mysql -u drools_user -p drools_db
# Password: drools_password
**Root Access:**
docker exec -it drools-mariadb mysql -u root -p
# Password: rootpassword## 🛠️ Common Operations

### View Database Logs
docker-compose logs -f mariadb- `-f` flag follows log output in real-time

### Execute SQL Commands
docker exec -i drools-mariadb mysql -u drools_user -pdrools_password drools_db < script.sql### Backup Database
docker exec drools-mariadb mysqldump -u drools_user -pdrools_password drools_db > backup.sql### Restore Database
docker exec -i drools-mariadb mysql -u drools_user -pdrools_password drools_db < backup.sql### Access Container Shell
docker exec -it drools-mariadb bash### Check Database Status
docker exec drools-mariadb mysqladmin -u root -prootpassword status## 🔍 Troubleshooting

### Container Won't Start

1. **Check if port 3306 is already in use:**h
   netstat -an | findstr 3306  # Windows
   lsof -i :3306                # Linux/Mac
   2. **View detailed logs:**h
   docker-compose logs mariadb
   3. **Check container status:**
   docker-compose ps
   ### Connection Refused

1. **Verify container is running:**
   docker-compose ps
   
2. **Check health status:**
   docker inspect drools-mariadb | grep Health
   
3. **Wait for initialization:**
   - First startup may take 30-60 seconds
   - Check logs for "ready for connections"

### Permission Denied

1. **Verify credentials:**
   - Username: `drools_user`
   - Password: `drools_password`
   - Database: `drools_db`

2. **Test connection:**
   
   docker exec -it drools-mariadb mysql -u drools_user -p drools_db
   ### Data Not Persisting

1. **Verify volume exists:**
  
   docker volume ls | grep mariadb_data
   2. **Check volume mount:**
   docker inspect drools-mariadb | grep Mounts
   ### Reset Database

1. **Stop and remove containers:**ash
   docker-compose down
   2. **Remove volume (⚠️ deletes all data):**
   docker volume rm drools_v4_webflux_mariadb_data
   3. **Start fresh:**
   docker-compose up -d
   ## 📚 Additional Resources

- [MariaDB Official Documentation](https://mariadb.com/kb/en/documentation/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [MariaDB Docker Hub](https://hub.docker.com/_/mariadb)

## 🔒 Security Recommendations

1. **Change Default Passwords**: Update all passwords in production
2. **Use Environment Variables**: Store sensitive data in `.env` file
3. **Limit Network Exposure**: Use Docker networks instead of exposing ports
4. **Regular Backups**: Implement automated backup strategy
5. **Version Pinning**: Use specific MariaDB version instead of `latest`

## 📝 Environment Variables File (Optional)

Create a `.env` file for sensitive configuration:
v
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_DATABASE=drools_db
MYSQL_USER=drools_user
MYSQL_PASSWORD=your_secure_passwordThen update `docker-compose.yml`:

environment:
  MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
  MYSQL_DATABASE: ${MYSQL_DATABASE}
  MYSQL_USER: ${MYSQL_USER}
  MYSQL_PASSWORD: ${MYSQL_PASSWORD}**Note**: Add `.env` to `.gitignore` to prevent committing secrets.