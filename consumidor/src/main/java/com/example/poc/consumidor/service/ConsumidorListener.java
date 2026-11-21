package com.example.poc.consumidor.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.poc.consumidor.config.RabbitConfig;
import com.example.poc.consumidor.mensaje.Mensaje;
import com.example.poc.consumidor.mensaje.MensajeRecibido;
import com.rabbitmq.client.Channel;

import tools.jackson.databind.ObjectMapper;

@Component
public class ConsumidorListener {

	private static final Logger log = LoggerFactory.getLogger(ConsumidorListener.class);
	private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	private final ObjectMapper objectMapper;
	private final BandejaService bandeja;
	private final long esperaAntesDelAcuse;

	public ConsumidorListener(
			ObjectMapper objectMapper,
			BandejaService bandeja,
			@Value("${poc.ack-delay-ms:2000}") long esperaAntesDelAcuse) {
		this.objectMapper = objectMapper;
		this.bandeja = bandeja;
		this.esperaAntesDelAcuse = esperaAntesDelAcuse;
	}

	@RabbitListener(queues = RabbitConfig.COLA_TAREAS, ackMode = "MANUAL")
	public void recibirTarea(Message message, Channel channel) throws IOException {
		procesar(message, channel, RabbitConfig.COLA_TAREAS, bandeja::agregarTarea);
	}

	@RabbitListener(queues = RabbitConfig.COLA_PANEL, ackMode = "MANUAL")
	public void recibirPanel(Message message, Channel channel) throws IOException {
		procesar(message, channel, RabbitConfig.COLA_PANEL, bandeja::agregarPanel);
	}

	@RabbitListener(queues = RabbitConfig.COLA_AUDITORIA, ackMode = "MANUAL")
	public void recibirAuditoria(Message message, Channel channel) throws IOException {
		procesar(message, channel, RabbitConfig.COLA_AUDITORIA, bandeja::agregarAuditoria);
	}

	private void procesar(Message message, Channel channel, String cola, Consumer<MensajeRecibido> destino)
			throws IOException {
		long deliveryTag = message.getMessageProperties().getDeliveryTag();
		try {
			String cuerpo = new String(message.getBody(), StandardCharsets.UTF_8);
			Mensaje mensaje = objectMapper.readValue(cuerpo, Mensaje.class);
			log.info("Mensaje {} en cola {}. Sigue Unacked hasta enviar el acuse.", mensaje.id(), cola);
			Thread.sleep(esperaAntesDelAcuse);

			MessageProperties propiedades = message.getMessageProperties();
			String routingKey = propiedades.getReceivedRoutingKey();
			if (routingKey == null || routingKey.isBlank()) {
				routingKey = "(fanout, sin clave)";
			}
			String modo = propiedades.getReceivedDeliveryMode() == null
					? "PERSISTENT"
					: propiedades.getReceivedDeliveryMode().name();

			destino.accept(new MensajeRecibido(
					mensaje.id(),
					mensaje.contenido(),
					mensaje.tipo(),
					mensaje.fecha(),
					ZonedDateTime.now(ZoneId.systemDefault()).format(HORA),
					cola,
					propiedades.getReceivedExchange(),
					routingKey,
					propiedades.getMessageId(),
					modo));

			channel.basicAck(deliveryTag, false);
			log.info("Acuse de recibido enviado. deliveryTag={} cola={}", deliveryTag, cola);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			channel.basicNack(deliveryTag, false, true);
			log.warn("Lectura interrumpida. El mensaje vuelve a la cola {}.", cola);
		} catch (Exception ex) {
			log.error("No se pudo procesar el mensaje de la cola {}", cola, ex);
			channel.basicNack(deliveryTag, false, false);
		}
	}

}
