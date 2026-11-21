# Activa la consola web de RabbitMQ (puerto 15672) sin depender del PATH.
# Si no eres administrador, esta ventana pide permiso y se vuelve a abrir elevada.
$ErrorActionPreference = "Stop"
$yo = [Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()
if (-not $yo.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
	Start-Process powershell -Verb RunAs -Wait -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
	exit $LASTEXITCODE
}

$env:ERLANG_HOME = "C:\Program Files\Erlang OTP"
$sbin = "C:\Program Files\RabbitMQ Server\rabbitmq_server-4.4.1\sbin"

$cookieServicio = Join-Path $env:WINDIR "system32\config\systemprofile\.erlang.cookie"
$cookieUsuario = Join-Path $env:USERPROFILE ".erlang.cookie"
Copy-Item $cookieServicio $cookieUsuario -Force
Write-Host "Cookie del servicio alineada con el usuario actual."

& "$sbin\rabbitmq-plugins.bat" enable rabbitmq_management
if ($LASTEXITCODE -ne 0) {
	Write-Host "El comando no pudo hablar con el nodo. Reiniciando el servicio RabbitMQ..."
	Restart-Service RabbitMQ -Force
	Start-Sleep -Seconds 6
}

$abierto = Get-NetTCPConnection -LocalPort 15672 -State Listen -ErrorAction SilentlyContinue
if ($abierto) {
	Write-Host "Consola lista en http://localhost:15672  usuario guest  clave guest"
} else {
	Write-Host "El puerto 15672 sigue cerrado. Abre PowerShell como administrador y vuelve a ejecutar este script."
	exit 1
}
