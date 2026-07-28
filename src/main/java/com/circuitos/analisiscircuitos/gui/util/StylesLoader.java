package com.circuitos.analisiscircuitos.gui.util;

import java.util.Objects;

import javafx.scene.Scene;

/**
 * Clase utilitaria para aplicar todas las hojas de estilo definidas en la clase {@link Styles} a una escena.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class StylesLoader {
	
	/**
	 * Constructor no instanciable.
	 */
	private StylesLoader() { /* No instanciable */ }
	
	/**
	 * Aplica todas las hojas de estilo definidas a la escena.
	 * 
	 * @param scene		Escena a la que se aplican los estilos
	 */
	public static void aplicarCSS(Scene scene) {
		Objects.requireNonNull(scene, "Scene no puede ser null");
		Styles.ALL.stream()
			.map(Styles::getUrlOrThrow)
			.forEach(scene.getStylesheets()::add);
	}
}
