package com.circuitos.analisiscircuitos.gui.util;

import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Clase utilitaria para mostrar mensajes visuales animados sobre el área de dibujo.
 * Permite mensajes de diferentes tipos (información, error, éxito o advertencia) y
 * posicionarlos (arriba, centro o abajo). Se desvanecen después de un tiempo.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class MensajesUI {
	
	/* Enumerado con tipos posibles de mensajes */
	public enum TipoMensaje { INFO, EXITO, ADVERTENCIA, ERROR }
	
	/* Enumerado con posibles posiciones en la pantalla */
	public enum PosicionMensaje {TOP, CENTER, BOTTOM}
	
	private static final double ANCHO_MAX=600;
	private static final double OFFSET_X=200;
	private static final double OFFSET_Y_TOP=20;
	private static final double OFFSET_Y_BOTTOM=60;
	private static final double ESCALA_INICIAL=0.75;
	private static final String CLASE_AVISO="mensaje-aviso";
	private static final String CLASE_ADVERTENCIA="mensaje-label-negro";
	private static final String CLASE_BOTON_ADVERTENCIA="mensaje-boton-cerrar-negro";
	private static final String CLASE_NORMAL="mensaje-label-blanco";
	private static final String CLASE_BOTON_NORMAL="mensaje-boton-cerrar-blanco";
	private static final String CLASE_INFO="mensaje-info";
	private static final String CLASE_EXITO="mensaje-exito";
	private static final String CLASE_WARNING="mensaje-warning";
	private static final String CLASE_ERROR="mensaje-error";
	
	/**
	 * Muestra un mensaje flotante con animación en el contenedor indicado.
	 * 
	 * @param contenedor			Contenedor donde se muestra (zona de dibujo)
	 * @param texto					Texto del mensaje
	 * @param tipo					Tipo visual del mensaje
	 * @param posicion				Posición vertical del mensaje
	 * @param duracion				Duración antes de que desaparezca
	 */
	public static void mostrarMensaje(Pane contenedor, String texto, TipoMensaje tipo, PosicionMensaje posicion, Duration duracion) {
		StackPane aviso=new StackPane();
		aviso.getStyleClass().add(CLASE_AVISO);
		
		//Botón cerrar "x"
		Label cerrar=new Label("x");
		cerrar.setPickOnBounds(true);
		cerrar.setOnMouseClicked(k -> contenedor.getChildren().remove(aviso));
		
		//Mensaje principal
		Label label=new Label(texto);
		label.setWrapText(true);
		label.setMaxWidth(ANCHO_MAX);
		
		if(tipo==TipoMensaje.ADVERTENCIA) {
			label.getStyleClass().add(CLASE_ADVERTENCIA);
			cerrar.getStyleClass().add(CLASE_BOTON_ADVERTENCIA);
		} else {
			label.getStyleClass().add(CLASE_NORMAL);
			cerrar.getStyleClass().add(CLASE_BOTON_NORMAL);
		}
		
		//Cabecera del mensaje
		HBox contenido=new HBox(cerrar, label);
		contenido.setAlignment(Pos.CENTER_LEFT);
		contenido.setSpacing(10);
		contenido.setPadding(new Insets(10));
		aviso.getChildren().add(contenido);
		
		//Estilo según tipo
		switch(tipo) {
			case INFO -> aviso.getStyleClass().add(CLASE_INFO);
			case EXITO -> aviso.getStyleClass().add(CLASE_EXITO);
			case ADVERTENCIA -> aviso.getStyleClass().add(CLASE_WARNING);
			case ERROR -> aviso.getStyleClass().add(CLASE_ERROR);
		}
		
		//Posicion Y según selección
		double y;
		switch(posicion) {
			case TOP -> y=OFFSET_Y_TOP;
			case CENTER -> y=contenedor.getHeight()/2.0 - OFFSET_Y_TOP;
			case BOTTOM -> y=contenedor.getHeight() - OFFSET_Y_BOTTOM;
			default -> y=OFFSET_Y_TOP;
		}
		
		//Inicialización de posición y estado visual
		aviso.setLayoutX(OFFSET_X);
		aviso.setLayoutY(y);
		aviso.setOpacity(0.0);
		aviso.setScaleX(ESCALA_INICIAL);
		aviso.setScaleY(ESCALA_INICIAL);
		
		//Mostrar y animar
		contenedor.getChildren().add(aviso);
		animacionMensaje(aviso, contenedor, duracion);
	}
	
	/**
	 * Aplica la animación de entrada y salida al mensaje flotante.
	 * 
	 * @param aviso				Nodo visual del mensaje
	 * @param contenedor		Contenedor donde se encuentra
	 * @param duracion			Tiempo que debe permanecer antes de desvanecerse
	 */
	private static void animacionMensaje(StackPane aviso, Pane contenedor, Duration duracion) {
		ParallelTransition entrada=new ParallelTransition(
				AnimadorUI.crearFadeIn(aviso)
		);
		SequentialTransition secuencia=new SequentialTransition(
				entrada,
				AnimadorUI.crearFadeOut(aviso, duracion, ()->contenedor.getChildren().remove(aviso))
		);
		secuencia.play();
	}
}
