package com.circuitos.analisiscircuitos.gui.util;

import java.util.Objects;
import java.util.logging.Logger;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Clase de utilidad que proporciona animaciones visuales para elementos de la interfaz gráfica.
 * Incluye efectos como rebote, fundido, parpadeo de error y realce sombreado al pasar el ratón.
 * 
 * @author Marco Antonio Garzon Palos
 * @version 1.0
 */
public class AnimadorUI {
	private static final Logger logger=Logger.getLogger(AnimadorUI.class.getName());
	
	private static final String CLASE_ERROR="conexion-punto-error";
	private static final String CLASE_NORMAL="conexion-punto-conexion-componente";
	private static final Duration DURACION_REBOTE=Duration.millis(300);
	private static final Duration DURACION_FADE_IN=Duration.millis(200);
	private static final Duration DURACION_FADE_OUT=Duration.millis(600);
	private static final Duration DURACION_MOUSE=Duration.millis(200);
	
	/**
	 * Constructor no instanciable.
	 */
	private AnimadorUI() { /* NO INSTANCIABLE */ }
	
	/**
	 * Crea una animación de rebote (escala) en un nodo.
	 * 
	 * @param nodo			Nodo al que se aplica la animación
	 * @return Intancia de ScaleTransition
	 */
	public static ScaleTransition crearRebote(Node nodo) {
		Objects.requireNonNull(nodo, "Nodo no puede ser null");
		ScaleTransition rebote=new ScaleTransition(DURACION_REBOTE, nodo);
		rebote.setFromX(0.8);
		rebote.setFromY(0.8);
		rebote.setToX(1.1);
		rebote.setToY(1.1);
		rebote.setCycleCount(2);
		rebote.setAutoReverse(true);
		logger.fine(()->"Animación rebote creada para nodo: "+nodo);
		return rebote;
	}
	
	/**
	 * Crea una animación de fundido de entrada.
	 * 
	 * @param nodo			Nodo al que se aplica la animación
	 * @return Transición de tipo FadeTransition
	 */
	public static FadeTransition crearFadeIn(Node nodo) {
		Objects.requireNonNull(nodo, "Nodo no puede ser null");
		FadeTransition fadeIn=new FadeTransition(DURACION_FADE_IN, nodo);
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);
		logger.fine(()->"Fade-in creado para nodo: "+nodo);
		return fadeIn;
	}
	
	/**
	 * Crea una animación de fundido de salida con retardo.
	 * 
	 * @param nodo			Nodo al que se aplica la animación
	 * @param delay			Tiempo de espera antes de comenzar la animación
	 * @param onFinish		Acción a ejecutar al terminar la animación
	 * @return Transición de tipo FadeTransition
	 */
	public static FadeTransition crearFadeOut(Node nodo, Duration delay, Runnable onFinish) {
		Objects.requireNonNull(nodo, "Nodo no puede ser null");
		FadeTransition fade=new FadeTransition(DURACION_FADE_OUT, nodo);
		fade.setFromValue(1.0);
		fade.setToValue(0.0);
		fade.setDelay(delay);
		fade.setOnFinished(k -> {
			if(onFinish!=null) onFinish.run();
			logger.fine(()->"Fade-out finalizado para nodo: "+nodo);
		});
		logger.fine(()->"Fade-out creado para nodo: "+nodo+" con delay "+delay);
		return fade;
	}
	
	/**
	 * Aplica un efecto visual al pasar el ratón sobre el nodo con sombra coloreada y ampliación de escala.
	 * Se revierte al salir.
	 * 
	 * @param nodo				Nodo sobre el que se aplica el efecto
	 * @param color				Color de la sombra al pasar el ratón
	 */
	public static void aplicarReboteMouse(Node nodo, Color color) {
		Objects.requireNonNull(nodo, "Nodo no puede ser null");
		ScaleTransition agrandar=new ScaleTransition(DURACION_MOUSE, nodo);
		agrandar.setToX(1.05);
		agrandar.setToY(1.05);
		agrandar.setInterpolator(Interpolator.EASE_OUT);
		
		ScaleTransition reducir=new ScaleTransition(DURACION_MOUSE, nodo);
		reducir.setToX(1.0);
		reducir.setToY(1.0);
		reducir.setInterpolator(Interpolator.EASE_BOTH);
		
		DropShadow sombra=new DropShadow();
		sombra.setRadius(10.0);
		sombra.setOffsetX(0.0);
		sombra.setOffsetY(0.0);
		sombra.setColor(color);
		
		nodo.setOnMouseEntered(e -> {
			reducir.stop();
			nodo.setEffect(sombra);
			agrandar.playFromStart();
			logger.finer(()->"Hover enter animado para nodo: "+nodo);
		});
		
		nodo.setOnMouseExited(e -> {
			agrandar.stop();
			nodo.setEffect(null);
			reducir.playFromStart();
			logger.finer(()->"Hover exit animado para nodo: "+nodo);
		});
	}
	
	/**
	 * Aplica una animaciónde parpadeo en rojo sobre un punto de conexión para indicar error de conexión.
	 * 
	 * @param punto 	Punto de conexión que debe parpadear en rojo
	 */
	public static void crearParpadeoError(Node nodo, int repeticiones, Duration duracion) {
		Objects.requireNonNull(nodo, "Nodo no puede ser null");
		nodo.getStyleClass().removeAll(CLASE_ERROR, CLASE_NORMAL);
		int total=repeticiones*2;
		PauseTransition[] parpadeos=new PauseTransition[total];
		
		for(int i=0; i<total; i++) {
			parpadeos[i]=new PauseTransition(duracion);
			final int idx=i;
			parpadeos[i].setOnFinished(ev -> {
				nodo.getStyleClass().removeAll(CLASE_ERROR, CLASE_NORMAL);
				if(idx%2==0) {
					nodo.getStyleClass().add(CLASE_ERROR);
				} else {
					nodo.getStyleClass().add(CLASE_NORMAL);
				}
				if(idx+1<total) parpadeos[idx+1].play();
				logger.fine(()->"Parpadeo error completado para nodo: "+nodo);
			});
		}
		parpadeos[0].play();
		logger.fine(()->"Parpadeo error iniciado para nodo: "+nodo+" con repeticiones: "+repeticiones);
	}
}
