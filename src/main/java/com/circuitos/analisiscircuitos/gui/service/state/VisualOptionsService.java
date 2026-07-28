package com.circuitos.analisiscircuitos.gui.service.state;

import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Servicio que gestiona las opciones visuales de la interfaz,
 * como la visibilidad de los puntos de control en cables o componentes
 * o la visibilidad de etiquetas de valor de los componentes o de los nodos.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 * @see VisualOptionsService.TipoElementoVisual
 */
public class VisualOptionsService {
	
	private static final Logger logger=Logger.getLogger(VisualOptionsService.class.getName());
	
	private static final boolean DEFAULT_SHOW_CONTROL_POINTS=true;
	private static final boolean DEFAULT_SHOW_LABELS=false;
	private static final boolean DEFAULT_SHOW_NODES=false;
	
	private static final BooleanProperty showControlPoints=new SimpleBooleanProperty(DEFAULT_SHOW_CONTROL_POINTS);
	private static final BooleanProperty showLabels=new SimpleBooleanProperty(DEFAULT_SHOW_LABELS);
	private static final BooleanProperty showNodes=new SimpleBooleanProperty(DEFAULT_SHOW_NODES);
	
	//Propiedad para activar/mostrar elementos de control en general en la interfaz de usuario
	private static final BooleanProperty showAllControlElements=new SimpleBooleanProperty(false);
	
	/* Constructor privado para evitar instancias */
	private VisualOptionsService() { /* NO INSTANCIABLE */ }
	
	static {
		configurarEnlacesProperty();
		showAllControlElements.set(showControlPoints.get());
		showAllControlElements.bindBidirectional(showControlPoints);
		logger.info("VisualOptionService inicializado con separación de elementos visuales");
	}
	
	/**
	 * Configura los listeners entre propiedades internas para mantener el estado visual y
	 * lanzar notificaciones.
	 */
	private static void configurarEnlacesProperty() {
		//Puntos de control property solo debería afectar a los puntos de control de cable (círculos azules)
		showControlPoints.addListener((obs, oldVal, newVal) -> {
			logger.info("Visibilidad de puntos de control cambiada: "+newVal);
			showAllControlElements.set(newVal);
		});
		
		//Nodos property solo debería afectar a las etiquetas de nodo (n0, n1, n2, etc.)
		showNodes.addListener((obs, oldVal, newVal) -> {
			logger.info("Visibilidad de etiquetas de nodo cambiada: "+newVal);
			notificarCambioNodos(newVal);
		});
		
		//Etiquetas property afecta a las etiquetas de valor del componente
		showLabels.addListener((obs, oldVal, newVal) -> {
			logger.info("Visibilidad de etiquetas de componente cambiada: "+newVal);
			notificarCambioEtiquetas(newVal);
		});
	}
	
	/**
	 * Notifica un cambio de visibilidad para las etiquetas de nodo.
	 * 
	 * @param visible		{@code true} para mostrar etiquetas de nodo
	 */
	private static void notificarCambioNodos(boolean visible) {
		System.setProperty("visual.nodes.visible", String.valueOf(visible));
		logger.fine("Notificación de visibilidad de etiquetas de nodo enviada: "+visible);
	}
	
	/**
	 * Notifica un cambio de visibilidad para las etiquetas de componentes.
	 * 
	 * @param visible		{@code true} para mostrar etiquetas de componente
	 */
	private static void notificarCambioEtiquetas(boolean visible) {
		System.setProperty("visual.labels.visible", String.valueOf(visible));
		logger.fine("Notificación de visibilidad de etiquetas de componente enviada: "+visible);
	}
	
	/**
	 * Devuelve la propiedad que controla todos los elementos de control visual.
	 * Esto incluye puntos de conexión, puntos de control de cables y elementos relacionados.
	 * 
	 * @return propiedad {@link BooleanProperty} unificada para todos los elementos de control
	 */
	public static BooleanProperty showAllControlElementsProperty() {
		return showAllControlElements;
	}
	
	/**
	 * Devuelve la propiedad observable sobre la visibilidad de las etiquetas de los componentes.
	 * 
	 * @return propiedad {@link BooleanProperty} asociada a la visibilidad de las etiquetas de los componentes
	 */
	public static BooleanProperty showLabelsProperty() {
		return showLabels;
	}
	
	/**
	 * Devuelve la propiedad observable sobre la visibilidad de los nodos.
	 * 
	 * @return propiedad {@link BooleanProperty} asociada a la visibilidad de los nodos
	 */
	public static BooleanProperty showNodesProperty() {
		return showNodes;
	}
	
	/**
	 * Devuelve la propiedad observable sobre la visibilidad de los puntos de control.
	 * 
	 * @return propiedad {@link BooleanProperty} asociada a la visibilidad de los puntos de control
	 */
	public static BooleanProperty showControlPointsProperty() {
		return showControlPoints;
	}
	
	/**
	 * Indica si las etiquetas de nodo están visibles.
	 */
	public static boolean isShowNodes() {
		return showNodes.get();
	}
	
	/**
	 * Indica si los puntos de control están visibles.
	 */
	public static boolean isShowControlPoints() {
		return showControlPoints.get();
	}
	
	/**
	 * Indica si las etiquetas de componentes están visibles.
	 */
	public static boolean isShowLabels() {
		return showLabels.get();
	}
	
	/**
	 * Establece la visibilidad de las etiquetas de nodo.
	 */
	public static void setShowNodes(boolean show) {
		if(showNodes.get()!=show) {
			showNodes.set(show);
			logger.info("Visibilidad de etiquetas de nodo configurada a: "+show);
		}
	}
	
	/**
	 * Establece la visibilidad de los puntos de control.
	 */
	public static void setShowControlPoints(boolean show) {
		if(showControlPoints.get()!=show) {
			showControlPoints.set(show);
			logger.info("Visibilidad de puntos de control configurada a: "+show);
		}
	}
	
	/**
	 * Establece la visibilidad de las etiquetas de componentes.
	 */
	public static void setShowLabels(boolean show) {
		if(showLabels.get()!=show) {
			showLabels.set(show);
			logger.info("Visibilidad de etiquetas de componente configurada a: "+show);
		}
	}
	
	/**
	 * Determina si un elemento visual debe mostrarse en función del tipo de elemento y 
	 * su estado actual. 
	 * 
	 * @param tipo			Tipo de elemento visual a evaluar
	 * @return {@code true} si el elemento debe mostrarse.
	 */
	public static boolean shouldShowVisualElement(TipoElementoVisual tipo) {
		return switch(tipo) {
			case NODO_ETIQUETA -> isShowNodes();
			case PUNTO_CONTROL_CABLE -> isShowControlPoints();
			case ETIQUETA_COMPONENTE -> isShowLabels();
			case PUNTO_CONEXION -> showAllControlElements.get();
		};
	}
	
	/**
	 * Activa un modo de depuración visual.
	 */
	public static void activarDebugMode() {
		setAllOptions(true, true, true);
		logger.info("Modo debug activado: todos los elementos visuales están visibles");
		logEstadoActual();
	}
	
	/**
	 * Activa un modo "limpio" de visualización. 
	 */
	public static void activarCleanMode() {
		setAllOptions(false, false, false);
		logger.info("Modo limpio activado: sólo se muestran los puntos de conexión");
		logEstadoActual();
	}
	
	/**
	 * Tipos de elementos visuales que se pueden gestionar.
	 */
	public enum TipoElementoVisual {
		NODO_ETIQUETA,				//Etiquetas de nodo como "n0", "n1, "n2"...
		PUNTO_CONTROL_CABLE, 		//Círculos azules para control de cable
		ETIQUETA_COMPONENTE, 		//Etiquetas de valor de componente como "4.7kΩ"
		PUNTO_CONEXION				//Puntos de conexión actuales (siempre visibles)
	}
	
	/**
	 * Registra en el log el estado actual de todas las opciones visuales.
	 * Útil para diagnóstico y depuración.
	 */
	public static void logEstadoActual() {
		logger.info("Visual Options State:");
		logger.info("  - Node labels: "+isShowNodes());
		logger.info("  - Control points: "+isShowControlPoints());
		logger.info("  - Component labels: "+isShowLabels());
		logger.info("  - Control Elements: "+showAllControlElements.get());
	}
	
	/**
	 * Restaura todas las opciones visuales a sus valores por defecto.
	 */
	public static void reset() {
		setShowNodes(DEFAULT_SHOW_NODES);
		setShowControlPoints(DEFAULT_SHOW_CONTROL_POINTS);
		setShowLabels(DEFAULT_SHOW_LABELS);
		logger.info("Opciones visuales reseteadas a valores por defecto");
	}
	
	/**
	 * Establece de una vez el conjunto de todas las opciones visuales.
	 * 
	 * @param nodes					{@code true} para mostrar etiquetas de nodos
	 * @param controlPoints			{@code true} para mostrar puntos de control
	 * @param labels				{@code true} para mostrar etiquetas de componentes
	 */
	public static void setAllOptions(boolean nodes, boolean controlPoints, boolean labels) {
		setShowNodes(nodes);
		setShowControlPoints(controlPoints);
		setShowLabels(labels);
		logger.info("Opciones visuales actualizadas: nodos="+nodes+", puntos de control:"+controlPoints+", etiquetas="+labels);
	}
}
