package com.example.poc.consumidor.mensaje;

import java.util.List;

public record BandejaResponse(
		List<MensajeRecibido> tareas,
		List<MensajeRecibido> panel,
		List<MensajeRecibido> auditoria) {
}
