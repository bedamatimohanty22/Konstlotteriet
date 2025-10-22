# Konstlotteriet - Deployment Guide

This guide provides instructions for deploying the Konstlotteriet application for testing and production.

## Table of Contents
- [Quick Start with Docker](#quick-start-with-docker)
- [Building JAR Files](#building-jar-files)
- [Manual Deployment](#manual-deployment)
- [Environment Variables](#environment-variables)
- [Test Credentials](#test-credentials)

---

## Quick Start with Docker

The easiest way to deploy the entire application stack is using Docker Compose.

### Prerequisites

**Required Software:**
- **Docker Desktop** - Download and install from:
  - Windows/Mac: https://www.docker.com/products/docker-desktop
  - Linux: https://docs.docker.com/engine/install/
- Docker Desktop must be **running** before executing commands
- At least 4GB RAM available
- Ports 80, 8080, and 5432 available

**Note for Testing Team:**
If you don't have Docker Desktop installed, you'll need to download and install it first. After installation, make sure Docker Desktop is running (you should see the Docker icon in your system tray/menu bar) before proceeding with the deployment steps.

### Steps

1. **Build the backend JAR** (one-time step):
   ```bash
   cd konstlotteriet
   ./mvnw.cmd clean package -DskipTests
   cd ..
   ```

2. **Start all services**:
   ```bash
   docker-compose up -d
   ```

3. **Access the application**:
   - Frontend: http://localhost
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui

4. **View logs**:
   ```bash
   docker-compose logs -f
   ```

5. **Stop all services**:
   ```bash
   docker-compose down
   ```

6. **Stop and remove all data**:
   ```bash
   docker-compose down -v
   ```

### Service Details

| Service | Port | Description |
|---------|------|-------------|
| Frontend | 80 | React application served by Nginx |
| Backend | 8080 | Quarkus REST API |
| PostgreSQL | 5432 | Database |

---

## Building JAR Files

### Backend (Quarkus)

Build a production JAR:
```bash
cd konstlotteriet
./mvnw.cmd clean package -DskipTests
```

The JAR will be created at: `konstlotteriet/target/quarkus-app/quarkus-run.jar`

Run the JAR:
```bash
java -jar target/quarkus-app/quarkus-run.jar
```

### Frontend (React + Vite)

Build production assets:
```bash
cd konstlotteriet-frontend
npm install
npm run build
```

The build will be created in: `konstlotteriet-frontend/dist/`

Serve with any static file server (e.g., nginx, Apache, or simple HTTP server).

---

## Manual Deployment

### 1. PostgreSQL Setup

Create database:
```sql
CREATE DATABASE konstlotteriet;
CREATE USER konstlotteriet WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE konstlotteriet TO konstlotteriet;
```

### 2. Backend Deployment

1. Build the JAR (see above)
2. Set environment variables:
   ```bash
   export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/konstlotteriet
   export QUARKUS_DATASOURCE_USERNAME=konstlotteriet
   export QUARKUS_DATASOURCE_PASSWORD=your_password
   export QUARKUS_DATASOURCE_DEVSERVICES_ENABLED=false
   ```
3. Run:
   ```bash
   java -jar target/quarkus-app/quarkus-run.jar
   ```

### 3. Frontend Deployment

1. Build the frontend (see above)
2. Copy `dist/` contents to your web server root
3. Configure nginx (example in `konstlotteriet-frontend/nginx.conf`)
4. Update `VITE_API_URL` in `.env.production` to point to your backend

---

## Environment Variables

### Backend (Quarkus)

| Variable | Default | Description |
|----------|---------|-------------|
| `QUARKUS_DATASOURCE_JDBC_URL` | - | PostgreSQL JDBC URL |
| `QUARKUS_DATASOURCE_USERNAME` | - | Database username |
| `QUARKUS_DATASOURCE_PASSWORD` | - | Database password |
| `QUARKUS_DATASOURCE_DEVSERVICES_ENABLED` | true | Auto-start Docker DB (dev only) |
| `QUARKUS_HTTP_CORS_ORIGINS` | * | Allowed CORS origins |
| `QUARKUS_HTTP_PORT` | 8080 | HTTP port |

### Frontend (Vite/React)

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_URL` | http://localhost:8080/api | Backend API URL |

---

## Test Credentials

### Default Admin User
- **Email**: admin@konstlotteriet.se
- **Password**: admin123

**⚠️ IMPORTANT**: Change this password in production!

To create additional users, log in as admin and use the user management interface.

---

## Docker Commands Reference

### Build specific service
```bash
docker-compose build backend
docker-compose build frontend
```

### Restart specific service
```bash
docker-compose restart backend
docker-compose restart frontend
```

### View service logs
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### Execute commands in container
```bash
docker-compose exec backend sh
docker-compose exec postgres psql -U konstlotteriet -d konstlotteriet
```

### Scale services (not applicable for database)
```bash
docker-compose up -d --scale backend=2
```

---

## Troubleshooting

### Database connection errors
1. Check PostgreSQL is running: `docker-compose ps postgres`
2. Check logs: `docker-compose logs postgres`
3. Verify connection settings in environment variables

### Frontend can't reach backend
1. Check CORS configuration in backend
2. Verify `VITE_API_URL` in frontend `.env.production`
3. Check network connectivity between containers

### Port conflicts
If ports are already in use, modify `docker-compose.yml`:
```yaml
ports:
  - "8081:8080"  # Use 8081 instead of 8080
```

### Reset everything
```bash
docker-compose down -v
docker system prune -a
# Then rebuild: docker-compose up -d --build
```

---

## Production Checklist

- [ ] Change default admin password
- [ ] Update CORS origins to match your domain
- [ ] Set strong database password
- [ ] Configure HTTPS/SSL
- [ ] Set up database backups
- [ ] Configure proper logging
- [ ] Set up monitoring
- [ ] Review and update JWT secret keys
- [ ] Configure email SMTP settings (if needed)
- [ ] Test backup and restore procedures

---

## Support

For issues or questions, check:
- Backend logs: `docker-compose logs backend`
- Frontend logs: `docker-compose logs frontend`
- Database logs: `docker-compose logs postgres`
- Swagger UI: http://localhost:8080/swagger-ui

---

**Last Updated**: 2025-10-22