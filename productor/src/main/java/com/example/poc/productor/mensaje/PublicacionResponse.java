package com.example.poc.productor.mensaje;

import java.util.List;

public record PublicacionResponse(
		String id,
		String modelo,
		String exchange,
		String routingKey,
		List<String> colas,
		String contenido,
		boolean persistente,
		String nota) {
}
