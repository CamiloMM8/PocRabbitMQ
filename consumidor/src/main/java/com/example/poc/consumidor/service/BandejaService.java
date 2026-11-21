package com.example.poc.consumidor.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import com.example.poc.consumidor.mensaje.BandejaResponse;
import com.example.poc.consumidor.mensaje.MensajeRecibido;

@Service
public class BandejaService {

	private static final int LIMITE = 40;

	private final List<MensajeRecibido> tareas = new CopyOnWriteArrayList<>();
	private final List<MensajeRecibido> panel = new CopyOnWriteArrayList<>();
	private final List<MensajeRecibido> auditoria = new CopyOnWriteArrayList<>();

	public void agregarTarea(MensajeRecibido mensaje) {
		guardar(tareas, mensaje);
	}

	public void agregarPanel(MensajeRecibido mensaje) {
		guardar(panel, mensaje);
	}

	public void agregarAuditoria(MensajeRecibido mensaje) {
		guardar(auditoria, mensaje);
	}

	public BandejaResponse bandeja() {
		return new BandejaResponse(List.copyOf(tareas), List.copyOf(panel), List.copyOf(auditoria));
	}

	public void limpiar() {
		tareas.clear();
		panel.clear();
		auditoria.clear();
	}

	private void guardar(List<MensajeRecibido> lista, MensajeRecibido mensaje) {
		lista.add(0, mensaje);
		while (lista.size() > LIMITE) {
			lista.remove(lista.size() - 1);
		}
	}

}
