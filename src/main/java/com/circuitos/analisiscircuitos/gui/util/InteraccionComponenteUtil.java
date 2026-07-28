package com.circuitos.analisiscircuitos.gui.util;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.dominio.Componente;
import com.circuitos.analisiscircuitos.gui.commands.MoveComponentCommand;
import com.circuitos.analisiscircuitos.gui.model.Cable;
import com.circuitos.analisiscircuitos.gui.model.PuntoConexion;
import com.circuitos.analisiscircuitos.gui.service.undo.UndoRedoManager;

import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Clase utilitaria que gestiona la interacción visual con los componentes.
 * Proporciona funciones para selección, movimiento, escalado y rotación.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class InteraccionComponenteUtil {
	private static final Logger logger=Logger.getLogger(InteraccionComponenteUtil.class.getName());
	
	private static final String CLASE_SELECCIONADO="comp-seleccionado";
	private static final double ZOOM=0.02;
	private static final double ROTATION=45.0;
	
	private static Node seleccionado=null;
	private static Contexto contextoActual;
	private static Consumer<Object> listenerDiseno;
	private static Consumer<Object> listenerAnalisis;
	private static Supplier<List<Node>> seleccionSupplier;
	
	/**
	 * Enumerado con los paneles de contexto que tenemos (Diseño o Análisis).
	 */
	public enum Contexto {
		DISENO, ANALISIS
	}
	
	/**
	 * Constructor no instanciable.
	 */
	private InteraccionComponenteUtil() { /* NO INSTANCIABLE */ }
	
	/**
	 * Registra un listener para el contexto indicado.
	 * 
	 * @param contexto			Contexto que utiliza
	 * @param listener			Listener para el contexto
	 */
	public static void setListener(Contexto contexto, Consumer<Object> listener) {
		Objects.requireNonNull(contexto, "Contexto no puede ser null");
		if(contexto==Contexto.DISENO) {
			listenerDiseno=listener;
		} else {
			listenerAnalisis=listener;
		}
	}
	
	/**
	 * Establece quien es el proveedor que indica qué elementos están seleccionados.
	 * 
	 * @param supplier			Proveedor
	 */
	public static void setSeleccionSupplier(Supplier<List<Node>> supplier) {
		seleccionSupplier=supplier;
	}
	
	/**
	 * Obtiene la lista segura de elementos seleccionados.
	 * 
	 * @return lista de seleccionados
	 */
	private static List<Node> getSeleccionadosSeguro() {
		if(seleccionSupplier==null) return Collections.emptyList();
		return seleccionSupplier.get();
	}
	
	/**
	 * Actualiza el contexto de interacción.
	 * 
	 * @param contexto		Contexto que actualizamos
	 */
	public static void setContextoActual(Contexto contexto) {
		contextoActual=Objects.requireNonNull(contexto, "Contexto no puede ser null");
		logger.log(Level.INFO, "Contexto cambiado a: {0}", contexto);
	}
	
	/**
	 * Marca visualmente un nodo como seleccionado y desmarca el anterior.
	 * Notifica al listener si está registrado.
	 * 
	 * @param nodo				Nodo a seleccionar
	 */
	public static void seleccionar(Node nodo) {
		if(seleccionado!=null) {
			seleccionado.getStyleClass().remove(CLASE_SELECCIONADO);
			if(seleccionado instanceof Cable cable) {
				cable.deseleccionar();
			}
		}
		seleccionado=nodo;
		if(nodo!=null) {
			if(nodo instanceof StackPane panel) {
				panel.getStyleClass().add(CLASE_SELECCIONADO);
			} else if(nodo instanceof Cable cable) {
				cable.seleccionar();
			}
		}
		Consumer<Object> listener=(contextoActual==Contexto.DISENO) ? listenerDiseno : listenerAnalisis;
		if(listener!=null) {
			Object obj=(nodo instanceof StackPane panel) ? panel.getUserData() : nodo;
			listener.accept(obj);
		}
		logger.log(Level.INFO, "Nodo seleccionado:{0}", nodo);
	}
	
	/**
	 * Marca un nodo como seleccionado SIN desmarcar otros (para multiselección).
	 * 
	 * @param nodo				Nodo a seleccionar
	 */
	public static void marcarSeleccion(Node nodo) {
		if(nodo==null) return;
		if(nodo instanceof StackPane sp) {
			if(!sp.getStyleClass().contains(CLASE_SELECCIONADO)) {
				sp.getStyleClass().add(CLASE_SELECCIONADO);
			}
		} else if(nodo instanceof Cable c) {
			c.seleccionar();
		}
	}
	
	/**
	 * Quita la marca de selección SIN tocar el resto (para multiselección).
	 * 
	 * @param nodo				Nodo a deseleccionar
	 */
	public static void desmarcarSeleccion(Node nodo) {
		if(nodo==null) return;
		if(nodo instanceof StackPane sp) {
			sp.getStyleClass().remove(CLASE_SELECCIONADO);
		} else if(nodo instanceof Cable c) {
			c.deseleccionar();
		}
	}
	
	/**
	 * Devuelve el nodo actualmente seleccionado.
	 * 
	 * @return Nodo seleccionado o null
	 */
	public static Node getSeleccionado() {
		return seleccionado;
	}
	
	/**
	 * Aplica eventos de interacción al panel de un componente: seleción, arrastre, zoom y rotación.
	 * 
	 * @param panel							Nodo visual del componente (StackPane)
	 * @param areaDibujo					Pane donde se colocan los componentes
	 * @param onSeleccionarComponente		Llamada que se invoca al seleccionar componente
	 */
	public static void aplicarEventos(
			StackPane panel, 
			Pane areaDibujo, 
			Consumer<Componente> onSeleccionarComponente,
			Consumer<Componente> onMoverTerminado) {
		Objects.requireNonNull(panel, "Panel no puede ser null");
		final double[] offset=new double[2];
		
		panel.setOnMousePressed(e -> handleMousePressed(e, panel, offset, onSeleccionarComponente));
		panel.setOnMouseDragged(e -> handleMouseDragged(e, panel, offset));
		panel.setOnScroll(e -> handleScroll(e, panel));
		panel.setOnMouseClicked(e -> handleMouseClicked(e, panel));
		panel.setOnMouseReleased(e -> handleMouseReleased(e, panel, onMoverTerminado));
	}
	
	/* Maneja la selección de un componente */
	
	private static void handleMousePressed(MouseEvent e, StackPane panel, 
			double[] offset, Consumer<Componente> onSeleccionar) {
		panel.getProperties().put("arrastreActivo", true);				//Ponemos bandera para evitar
		panel.getProperties().put("startX", panel.getLayoutX());		//arrastre indebidos al hacer marquee
		panel.getProperties().put("startY", panel.getLayoutY());
		List<Node> seleccionados=getSeleccionadosSeguro();
		if(seleccionados.contains(panel)) {
			for(Node n : seleccionados) {
				if(n!=panel) {	
					if(n instanceof StackPane) {
						n.getProperties().put("startX", n.getLayoutX());
						n.getProperties().put("startY", n.getLayoutY());
					} else if(n instanceof PuntoConexion p && p.getComponente()==null) {
						n.getProperties().put("startX", n.getLayoutX());
						n.getProperties().put("startY", n.getLayoutY());
					} 
				}
			}
		}
		if(e.isMetaDown() || e.isControlDown()) {
			panel.toFront();
			panel.getParent().requestFocus();
			return;
		}
		offset[0]=e.getSceneX()-panel.getLayoutX();
		offset[1]=e.getSceneY()-panel.getLayoutY();
		panel.toFront();
		seleccionar(panel);
		panel.getParent().requestFocus();
        if (onSeleccionar!=null) {
            Componente comp=(Componente) panel.getUserData();
            onSeleccionar.accept(comp);
        }
        logger.log(Level.FINE, "Mouse pressed en componente: {0}", panel.getUserData());
	}
	
	/* Maneja el arrastre de un componente */
	
	private static void handleMouseDragged(MouseEvent e, StackPane panel, double[] offset) {
		if(panel.getProperties().get("arrastreActivo")==null) return;	//Si no hay pressed sobre el componente
		double newX=e.getSceneX()-offset[0];							//ignorar arrastre
		double newY=e.getSceneY()-offset[1];
		double dx=newX-panel.getLayoutX();			//dx, dy cuánto nos movemos respecto de pos. actual
		double dy=newY-panel.getLayoutY();
		panel.setLayoutX(newX);						//mover líder (el que se arrastra)
		panel.setLayoutY(newY);
		List<Node> seleccionados=getSeleccionadosSeguro();
		if(seleccionados.contains(panel)) {
			for(Node n : seleccionados) {
				if(n!=panel && n instanceof StackPane) {
					n.setLayoutX(n.getLayoutX()+dx);
					n.setLayoutY(n.getLayoutY()+dy);
				} else if(n instanceof Cable c) {
					c.moverPuntosControl(dx, dy);
				} else if(n instanceof PuntoConexion p) {
					if(p.getComponente()==null) {		//Solo movemos los que no tienen componente
						p.setLayoutX(p.getLayoutX()+dx);
						p.setLayoutY(p.getLayoutY()+dy);
					}
				}
			}
		}
		logger.log(Level.FINER, "Arrastrando grupo de componentes...");
	}
	
	/* Maneja el scroll de un componente (aumento o disminución de zoom) */
	
	private static void handleScroll(ScrollEvent e, StackPane panel) {
        double scaleFactor=(e.getDeltaY() > 0) ? (1+ZOOM) : (1-ZOOM);
        panel.setScaleX(panel.getScaleX()*scaleFactor);
        panel.setScaleY(panel.getScaleY()*scaleFactor);
        logger.log(Level.FINER, "Zoom aplicado: escala={0}", panel.getScaleX());
	}
	
	/* Maneja la rotación de un componente al hacer click derecho */
	
	private static void handleMouseClicked(MouseEvent e, StackPane panel) {
        if (e.getButton()==MouseButton.SECONDARY) {
            panel.setRotate(panel.getRotate()+ROTATION);
            updateConexionPuntos(panel);
            logger.log(Level.FINE, "Rotado componente a {0}º", panel.getRotate());
        }
	}
	
	/* Maneja el final del arrastre de un componente: registra comando de movimiento */
	
	private static void handleMouseReleased(MouseEvent e, StackPane panel, Consumer<Componente> onMoverTerminado) {
		panel.getProperties().remove("arrastreActivo");				//limpiar bandera al soltar
		try {
			Double startX=(Double) panel.getProperties().get("startX");
			Double startY=(Double) panel.getProperties().get("startY");
			if(startX==null || startY==null) return;
			double endX=panel.getLayoutX();
			double endY=panel.getLayoutY();
			
			//Evitar micromovimientos o click sin arrastre
			if(Math.abs(endX-startX)>0.5 && Math.abs(endY-startY)>0.5) {
				Componente comp=(Componente) panel.getUserData();
				Runnable pedirActualizar=() -> {
					if(onMoverTerminado!=null) onMoverTerminado.accept(comp);
				};
				UndoRedoManager.getInstance().ejecutarComando(
						new MoveComponentCommand(comp, panel, startX, startY, endX, endY, pedirActualizar));
				logger.log(Level.FINE, "Movimiento registrado (undo/redo) para componente.");
			}
			panel.getProperties().remove("startX");
			panel.getProperties().remove("startY");
		} catch(Exception ex) {
			logger.log(Level.WARNING, "Error registrando movimiento grupo", ex);
		}
	}
	
	/**
	 * Actualiza posiciones de puntos de conexión tras una rotación.
	 * 
	 * @param panel			Panel que ha rotado
	 */
	private static void updateConexionPuntos(StackPane panel) {
		for(Node child : panel.getChildren()) {
			if(child instanceof PuntoConexion pc) {
				pc.setPosicion(PosicionUtil.calcularPosicion(
						pc.getComponente(), pc.esPositivo(), (int) panel.getRotate()));
			}
		}
	}
}
