package com.circuitos.analisiscircuitos.dto;

import java.util.Objects;

/**
 * DTO (Data Transfer Object) que representa la posición y rotación de un componente.
 * Incluye validación de rotación en ángulos multiplos de 45º.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0 
 */
public record PosicionComponenteDto (
		String componenteId,
		double x, double y,
		int rotacion) {
	
	/**
	 * Constructor.
	 * 
	 * @param componente Id			ID único del componente
	 * @param x						Coordenada X
	 * @param y						Coordenada Y
	 * @param rotacion				Rotación en grados (automática)
	 */
	public PosicionComponenteDto {
		Objects.requireNonNull(componenteId, "componenteId no puede ser nulo");
		rotacion=((int) Math.round(rotacion/45.0)*45)%360;
		if(rotacion<0) rotacion+=360;
	}
}