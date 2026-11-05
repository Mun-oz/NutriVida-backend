## Ejecucion con Docker

### Requisitos

- Docker Desktop
- Docker Compose

### Configuracion

1. Copiar `.env.example` como `.env`.
2. Configurar las contraseñas de MySQL.
3. Ejecutar los contenedores.

### Iniciar

```bash
docker compose up -d --build
```

### Verificar

```bash
docker compose ps
docker compose logs backend
```

### Detener

```bash
docker compose down
```

### API REST

http://localhost:8080/api/v1/servicios

### Base de datos

MySQL 8.4, disponible localmente en el puerto 3308.
