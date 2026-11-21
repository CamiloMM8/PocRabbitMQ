package com.example.poc.consumidor.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * La misma topología que declara el productor. Si el productor está apagado,
 * esta API igual puede conectarse a las colas que ya viven en RabbitMQ.
 */
@Configuration
public class RabbitConfig {

	public static final String EXCHANGE_DIRECTO = "poc.directo";
	public static final String EXCHANGE_FANOUT = "poc.fanout";
	public static final String COLA_TAREAS = "poc.tareas";
	public static final String COLA_PANEL = "poc.avisos.panel";
	public static final String COLA_AUDITORIA = "poc.avisos.auditoria";
	public static final String ROUTING_TAREA = "tarea.nueva";

	@Bean
	public DirectExchange exchangeDirecto() {
		return new DirectExchange(EXCHANGE_DIRECTO, true, false);
	}

	@Bean
	public FanoutExchange exchangeFanout() {
		return new FanoutExchange(EXCHANGE_FANOUT, true, false);
	}

	@Bean
	public Queue colaTareas() {
		return QueueBuilder.durable(COLA_TAREAS).build();
	}

	@Bean
	public Queue colaPanel() {
		return QueueBuilder.durable(COLA_PANEL).build();
	}

	@Bean
	public Queue colaAuditoria() {
		return QueueBuilder.durable(COLA_AUDITORIA).build();
	}

	@Bean
	public Binding enlaceTareas(Queue colaTareas, DirectExchange exchangeDirecto) {
		return BindingBuilder.bind(colaTareas).to(exchangeDirecto).with(ROUTING_TAREA);
	}

	@Bean
	public Binding enlacePanel(Queue colaPanel, FanoutExchange exchangeFanout) {
		return BindingBuilder.bind(colaPanel).to(exchangeFanout);
	}

	@Bean
	public Binding enlaceAuditoria(Queue colaAuditoria, FanoutExchange exchangeFanout) {
		return BindingBuilder.bind(colaAuditoria).to(exchangeFanout);
	}

}
