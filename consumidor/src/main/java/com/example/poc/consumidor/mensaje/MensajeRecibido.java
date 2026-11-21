package com.example.poc.consumidor.mensaje;

public record MensajeRecibido(
		String id,
		String contenido,
		String tipo,
		String publicadoEn,
		String recibidoEn,
		String cola,
		String exchange,
		String routingKey,
		String messageId,
		String deliveryMode) {
}
