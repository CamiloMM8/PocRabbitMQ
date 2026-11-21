package com.example.poc.consumidor.web;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.poc.consumidor.mensaje.BandejaResponse;
import com.example.poc.consumidor.service.BandejaService;

@RestController
@RequestMapping("/api")
public class ConsumidorController {

	private final BandejaService bandeja;

	public ConsumidorController(BandejaService bandeja) {
		this.bandeja = bandeja;
	}

	@GetMapping("/recibidos")
	public BandejaResponse recibidos() {
		return bandeja.bandeja();
	}

	@DeleteMapping("/recibidos")
	public Map<String, String> limpiar() {
		bandeja.limpiar();
		return Map.of("estado", "pantalla limpia");
	}

	@GetMapping("/salud")
	public Map<String, String> salud() {
		return Map.of(
				"servicio", "consumidor-api",
				"estado", "EN_LINEA",
				"rol", "consumidor");
	}

}
