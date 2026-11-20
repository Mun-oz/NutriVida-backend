$ErrorActionPreference = "Stop"

# Ubicar la raíz del proyecto
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$envFile = Join-Path $root ".env"

# Verificar archivo .env
if (-not (Test-Path $envFile)) {
    throw "No existe .env. Copia .env.example a .env y configura las claves."
}

# Cargar variables de entorno
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()

    if ($line -match '^([A-Za-z_][A-Za-z0-9_]*)\s*=(.*)$') {
        $key = $Matches[1]
        $value = $Matches[2].Trim()

        if (
            ($value.StartsWith('"') -and $value.EndsWith('"')) -or
            ($value.StartsWith("'") -and $value.EndsWith("'"))
        ) {
            $value = $value.Substring(1, $value.Length - 2)
        }

        [Environment]::SetEnvironmentVariable(
            $key, $value, "Process"
        )
    }
}

# Verificar variables necesarias
foreach ($key in @(
    "MYSQL_PASSWORD",
    "JWT_SECRET",
    "JWT_REFRESH_SECRET"
)) {
    if ([string]::IsNullOrWhiteSpace(
        [Environment]::GetEnvironmentVariable($key)
    )) {
        throw "Falta configurar $key en el archivo .env"
    }
}

# Configuración para Maven ejecutado en Windows
$env:DB_URL = "jdbc:mysql://127.0.0.1:3308/nutrivida_db"
$env:DB_USERNAME = "nutrivida"
$env:DB_PASSWORD = $env:MYSQL_PASSWORD

Push-Location $root

try {
    Write-Host "Iniciando MySQL..." -ForegroundColor Cyan

    docker compose up -d --wait mysql

    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo iniciar MySQL."
    }

    Write-Host "Ejecutando pruebas Maven..." -ForegroundColor Cyan

    & ".\mvnw.cmd" test

    if ($LASTEXITCODE -ne 0) {
        throw "Las pruebas Maven fallaron."
    }

    Write-Host "Pruebas completadas correctamente." -ForegroundColor Green
}
finally {
    Pop-Location
}
