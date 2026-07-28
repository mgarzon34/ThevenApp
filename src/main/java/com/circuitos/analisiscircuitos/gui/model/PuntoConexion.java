package com.circuitos.analisiscircuitos.gui.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.dominio.Componente;
import com.circuitos.analisiscircuitos.gui.service.state.VisualOptionsService;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;

/**
 * Clase que representa un punto de conexión eléctrica asociado a un componente.
 * Al hacer click sobre él, se lanza un evento. Se dibuja como un círculo transparente con borde azul.
 * 
 * @author Marco Antonio Garzon Palos
 * @version 1.0
 */
public class PuntoConexion extends Circle {
	private static final Logger logger=Logger.getLogger(PuntoConexion.class.getName());
	
	private final ObjectProperty<Net> netProperty=new SimpleObjectProperty<>(null);
	private final IntegerProperty nodoProperty=new SimpleIntegerProperty(this, "nodo", -1);
	
	private static final double RADIO=5.0;
	private static final String PUNTO_CONEXION="conexion-punto-conexion-componente";
	
	/**
	 * Enumeración que define la posición relativa del punto en el componente.
	 */
	public enum Posicion { ARRIBA, ABAJO, IZQUIERDA, DERECHA, INICIO, FIN }
	
	private final Componente componente;		
	private final boolean esPositivo; 
	private final int nodoPredeterminado;			
	private Posicion posicion;	
	private Label etiquetaNodo;
	private Tooltip tooltip=new Tooltip();
	
	/**
	 * Constructor que crea un punto de conexión asociado a un componente.
	 * Manda con un bind la opción de mostrar u ocultar visualmente los puntos de conexión.
	 * 
	 * @param componente			Componente al que pertenece el punto
	 * @param esPositivo			{@code true} si el punto está en el terminal positivo, {@code false}, si no
	 * @param posicion				Posicion del punto de conexion respecto del componente
	 */
	public PuntoConexion(Componente componente, boolean esPositivo, Posicion posicion) {
		super(RADIO);		//radio del círculo
		this.componente=componente;
		this.esPositivo=esPositivo;
		this.posicion=posicion;
		this.nodoPredeterminado=esPositivo ? -2 : -1;
		getStyleClass().add(PUNTO_CONEXION);
		setPickOnBounds(true);
		visibleProperty().bind(VisualOptionsService.showAllControlElementsProperty());
		mouseTransparentProperty().bind(visibleProperty().not());
		managedProperty().bind(visibleProperty());
		nodoProperty.bind(
				Bindings.createIntegerBinding(
						()->netProperty.get()!=null ? netProperty.get().getId() : nodoPredeterminado,
								netProperty));
		actualizarTooltip();
		Tooltip.install(this, tooltip);
		addEventHandler(MouseEvent.MOUSE_CLICKED, this::controlarClick);
		netProperty.addListener((obs, oldVal, newVal)->actualizarTooltip());
	}
	
	/**
	 * Constructor de punto de conexión que no tiene en cuenta la posición del pin del componente.
	 * 
	 * @param componente			Componente al que pertenece el punto
	 * @param esPositivo			{@code true} si el punto está en el terminal positivo, {@code false}, si no
	 */
	public PuntoConexion(Componente componente, boolean esPositivo) {
		this(componente, esPositivo, null);
	}
	
	/**
	 * Devuelve la red (nodo) asociada a un punto.
	 * 
	 * @return {@link Net} asociada o {@code null} si no está conectado
	 */
	public Net getNet() {
		return netProperty.get();
	}
	
	/**
	 * Asocia el punto actual a una red (nodo).
	 * 
	 * @param net Red a la que se conecta el punto
	 */
	public void setNet(Net nuevaNet) {
		netProperty.set(nuevaNet);
		logger.log(Level.FINE, "Net asignada: {0} a punto {1}", new Object[] {nuevaNet, this});
	}
	
	/**
	 * Devuelve la propiedad observable de la red (Net) a la que está conectado el punto.
	 * 
	 * @return Propiedad observable de objeto {@link Net}
	 */
	public ObjectProperty<Net> netProperty() {
		return netProperty;
	}
	
	/**
	 * Devuelve la propiedad observable de solo lectura con el identificador del nodo.
	 * 
	 * @return Propiedad observable de solo lectura del identificador del nodo.
	 */
	public final IntegerProperty nodoProperty() {
		return nodoProperty;
	}
	
	/**
	 * Maneja el evento de click, imprimiendo información del punto en consola.
	 * 
	 * @param event			Evento de click recibido
	 */
	private void controlarClick(MouseEvent event) {
		if(componente==null) {
			logger.warning("Click en PuntoConexion sin componente asociado. Nodo: "+getNodo());
			event.consume();
			return;
		}
		logger.fine(String.format(
				"Click en punto %s de %s (Nodo: %d)",
				esPositivo ? "positivo" : "negativo",
				componente.getClass().getSimpleName(), getNodo()));
		event.consume();
	}
	
	/**
	 * Obtiene la posición relativa del punto respecto al componente.
	 * 
	 * @return Posicion relativa del punto respecto al componente
	 */
	public Posicion getPosicion() {
		return posicion;
	}
	
	/**
	 * Establece una posición nueva relativa del punto.
	 * 
	 * @param Posicion nueva
	 */
	public void setPosicion(Posicion posicion) {
		this.posicion=posicion;
	}
	
	/**
	 * Obtiene el componente al que pertenece el punto.
	 * 
	 * @return Componente al que pertenece el punto
	 */
	public Componente getComponente() {
		return componente;
	}
	
	/**
	 * Devuelve si el punto es positivo o no.
	 * 
	 * @return true si es positivo, false si no lo es
	 */
	public boolean esPositivo() {
		return esPositivo;
	}
	
	/**
	 * Devuelve el nodo predeterminado que se asignó al crear el componente.
	 * 
	 * @return Nodo predeterminado
	 */
	public int getNodoPredeterminado() {
		return nodoPredeterminado;
	}
	
	/**
	 * Obtiene el nodo al que está conectado este punto.
	 * 
	 * @return Nodo al que está conectado (-1 si no está conectado)
	 */
	public int getNodo() {
		return nodoProperty.get();
	}
	
	/**
	 * Asigna un nuevo nodo a nodoProperty quitando el binding en primer lugar.
	 * El uso de este método tiene riesgo, al quitar el binding.
	 * 
	 * @param value			Nuevo valor
	 */
	public final void setNodo(int value) {
		if(nodoProperty.isBound()) {
			nodoProperty.unbind();
		}
		nodoProperty.set(value);
		actualizarTooltip();
	}
	
	/**
	 * Modifica el valor de un nodo libre.
	 * 
	 * @param value		Nuevo nodo
	 */
	public void setNodoLibre(int value) {
		if(componente!=null) {
			throw new IllegalStateException("Este punto pertence a un componente; usa Net para cambiarle el nodo.");
		}
		setNodo(value);
	}
	
	/**
	 * Verifica si el punto está conectado a un nodo válido.
	 * 
	 * @return {@code true} si está conectado a un nodo válido, {@code false} si no.
	 */
	public boolean estaConectado() {
		return getNet()!=null && getNet().getId()>=0;
	}
	
	/**
	 * Obtiene la coordenada X del punto.
	 * 
	 * @return Coordenada X del punto
	 */
	public double getX() {
		return localToScene(getBoundsInLocal()).getMinX()+getRadius();
	}
	
	/**
	 * Obtiene la coordenada Y del punto.
	 * 
	 * @return Coordenada Y del punto
	 */
	public double getY() {
		return localToScene(getBoundsInLocal()).getMinY()+getRadius();
	}
	
	/**
	 * Asocia el tooltip al círculo visual de conexión. 
	 */
	public void actualizarTooltip() {
		StringBuilder sb=new StringBuilder();
		if(componente==null) {
			sb.append("Sin componente asociado");
		} else {
			sb.append("Componente: ")
			  .append(componente.getClass().getSimpleName())
			  .append("\nPunto: ")
			  .append(esPositivo ? "positivo" : "negativo")
			  .append("\nNodo: ")
			  .append(getNodo());
		}
		tooltip.setText(sb.toString());
	}
	
	/**
	 * Modifica la etiqueta visual del punto de conexión. 
	 * 
	 * @param Etiqueta que representa el nodo
	 */
	public void setEtiquetaNodo(Label etiqueta) {
		this.etiquetaNodo=etiqueta;
	}
	
	/**
	 * Obtiene la etiqueta visual del punto de conexión.
	 * 
	 * @return Etiqueta visual del nodo o {@code null} si no está definida
	 */
	public Label getEtiquetaNodo() {
		return etiquetaNodo;
	}
	
	/**
	 * Devuelve un string con las características del PuntoConexión.
	 */
	@Override
	public String toString() {
		return String.format(
				"PuntoConexion[%s, nodo=%d, componente=%s]",
				esPositivo ? "+" : "-",
				getNodo(),
				componente!=null ? componente.getClass().getSimpleName() : "null");
	}
}
