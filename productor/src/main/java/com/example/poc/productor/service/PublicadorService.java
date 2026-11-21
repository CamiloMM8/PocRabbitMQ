package com.example.poc.productor.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.poc.productor.config.RabbitConfig;
import com.example.poc.productor.mensaje.Mensaje;
import com.example.poc.productor.mensaje.PublicacionResponse;

import tools.jackson.databind.ObjectMapper;

@Service
public class PublicadorService {

	private static final Logger log = LoggerFactory.getLogger(PublicadorService.class);
	private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;

	public PublicadorService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
		this.rabbitTemplate = rabbitTemplate;
		this.objectMapper = objectMapper;
		this.rabbitTemplate.setMandatory(true);
		this.rabbitTemplate.setReturnsCallback(returned -> log.warn(
				"Mensaje no enrutado. exchange={} routingKey={}",
				returned.getExchange(), returned.getRoutingKey()));
	}

	public PublicacionResponse publicarTarea(String contenido) {
		return publicar(
				contenido,
				"TAREA",
				"Punto a punto",
				RabbitConfig.EXCHANGE_DIRECTO,
				RabbitConfig.ROUTING_TAREA,
				List.of(RabbitConfig.COLA_TAREAS),
				"La clave de enrutamiento tarea.nueva coincide con la clave de enlace de la cola poc.tareas. Un solo consumidor recibe el mensaje.");
	}

	public PublicacionResponse publicarAviso(String contenido) {
		return publicar(
				contenido,
				"AVISO",
				"Publicador / suscriptor",
				RabbitConfig.EXCHANGE_FANOUT,
				"",
				List.of(RabbitConfig.COLA_PANEL, RabbitConfig.COLA_AUDITORIA),
				"El intercambio fanout ignora la clave de enrutamiento y copia el mensaje en cada cola enlazada.");
	}

	private PublicacionResponse publicar(
			String contenido,
			String tipo,
			String modelo,
			String exchange,
			String routingKey,
			List<String> colas,
			String nota) {
		if (contenido == null || contenido.isBlank()) {
			throw new IllegalArgumentException("El contenido del mensaje es obligatorio.");
		}
		String texto = contenido.trim();
		String id = UUID.randomUUID().toString();
		String fecha = ZonedDateTime.now(ZoneId.systemDefault()).format(HORA);
		Mensaje mensaje = new Mensaje(id, texto, tipo, fecha);
		String json = objectMapper.writeValueAsString(mensaje);

		rabbitTemplate.convertAndSend(exchange, routingKey, json, message -> atributos(message, id, tipo));

		log.info("Publicado. modelo={} exchange={} routingKey={} colas={} id={}",
				modelo, exchange, routingKey, colas, id);

		return new PublicacionResponse(id, modelo, exchange, routingKey, colas, texto, true, nota);
	}

	private Message atributos(Message message, String id, String tipo) {
		MessageProperties propiedades = message.getMessageProperties();
		propiedades.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
		propiedades.setContentType("text/plain");
		propiedades.setContentEncoding("UTF-8");
		propiedades.setMessageId(id);
		propiedades.setTimestamp(new Date());
		propiedades.setAppId("productor-api");
		propiedades.setType(tipo);
		propiedades.setHeader("formato", "json");
		propiedades.setHeader("origen", "productor-api");
		return message;
	}

}
