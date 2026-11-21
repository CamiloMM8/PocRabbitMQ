package com.example.poc.productor.web;

import java.util.Map;

import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.poc.productor.mensaje.ContenidoRequest;
import com.example.poc.productor.mensaje.PublicacionResponse;
import com.example.poc.productor.service.PublicadorService;

@RestController
@RequestMapping("/api")
public class ProductorController {

	private final PublicadorService publicador;

	public ProductorController(PublicadorService publicador) {
		this.publicador = publicador;
	}

	@PostMapping("/tareas")
	public PublicacionResponse publicarTarea(@RequestBody ContenidoRequest request) {
		return publicador.publicarTarea(request.contenido());
	}

	@PostMapping("/avisos")
	public PublicacionResponse publicarAviso(@RequestBody ContenidoRequest request) {
		return publicador.publicarAviso(request.contenido());
	}

	@GetMapping("/salud")
	public Map<String, String> salud() {
		return Map.of(
				"servicio", "productor-api",
				"estado", "EN_LINEA",
				"rol", "productor");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> contenidoInvalido(IllegalArgumentException ex) {
		return Map.of("error", ex.getMessage());
	}

	@ExceptionHandler(AmqpException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public Map<String, String> brokerNoDisponible(AmqpException ex) {
		return Map.of("error", "No se pudo publicar en RabbitMQ. Verifica que el servicio del broker esté iniciado.");
	}

}
