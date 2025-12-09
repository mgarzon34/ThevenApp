package com.circuitos.analisiscircuitos.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.dominio.Componente;
import com.circuitos.analisiscircuitos.dominio.Tierra;
import com.circuitos.analisiscircuitos.gui.commands.ConnectComponentsCommand;
import com.circuitos.analisiscircuitos.gui.service.nodes.NodoManager;
import com.circuitos.analisiscircuitos.gui.service.undo.UndoRedoManager;
import com.circuitos.analisiscircuitos.gui.util.AnimadorUI;
import com.circuitos.analisiscircuitos.gui.util.MensajesUI;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * Clase que gestiona las conexiones eléctricas entre puntos de los componentes.
 * Registra clicks sobre los puntos de conexión, valida conexiones, crea cables visuales
 * y asigna nodos a los componentes. Muestra mensajes visuales y animaciones en caso de conexión errónea.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class ConectorPuntos {
	private static final Logger logger=Logger.getLogger(ConectorPuntos.class.getName());
	private static final double GROSOR_SELECCION=3.0;
	private static final double GROSOR_NORMAL=1.5;
	private static final double DURACION_PARPADEO=100;
	
	private PuntoConexion puntoSeleccionado=null;	
	private Pane zonaDibujo;
	private final NodoManager nodoManager;
	private final List<Net> nets=new ArrayList<>();
	
	/**
	 * Crea un nuevo conector para una zona de dibujo.
	 * 
	 * @param zonaDibujo		Pane sobre el que se dibujarán los cables y se mostrarán mensajes
	 */
	public ConectorPuntos(Pane zonaDibujo, NodoManager manager) {
		this.zonaDibujo=zonaDibujo;
		this.nodoManager=manager;
		configurarClickCables();
		logger.fine("ConectorPuntos inicializado");
	}
	
	/**
	 * Crea una nueva Net sin ID, registrada en la lista de redes.
	 * 
	 * @return	Nueva Net sin ID
	 */
	public Net crearNet() {
		Net n=new Net(-1);
		nets.add(n);
		logger.fine("Net creada: provisional ID -1");
		return n;
	}
	
	/**
	 * Fusiona dos nets en una.
	 * 
	 * @param a		Net a
	 * @param b		Net b
	 */
	private void fusionar(Net a, Net b) {
		if(a==b) return;
		
		for(PuntoConexion pin : b.getPins()) {
			a.addPin(pin);
			pin.setNet(a);
		}
		
		nets.remove(b);
		if(b.getId()>=0) {
			nodoManager.decrementarCuenta(b.getId());
		}
		logger.log(Level.FINE, "Nets fusionados: {0} - {1}", new Object[] {a.getId(), b.getId()});
	}
	
	/**
	 * Devuelve la lista de nets registrados como lista no modificable.
	 * 
	 * @return	Lista de nets
	 */
	public List<Net> getNets() {
		return Collections.unmodifiableList(nets);
	}
	
	/**
	 * Devuelve la Net según el ID proporcionado o null si no existe.
	 * 
	 * @param id		Id del que buscamos su Net
	 * @return net asociado al Id o {@code null} si no lo encontramos
	 */
	public Net getNetId(int id) {
		for(Net net : nets) {
			if(net.getId()==id) {
				return net;
			}
		}
		return null;
	}
	
	/**
	 * Registra el evento de click sobre un punto de conexión. 
	 * Si ya hay otro punto seleccionado, intenta conectarlos o muestra error si son del mismo componente.
	 * 
	 * @param punto				Punto de conexión a registrar
	 */
	public void registrarPunto(PuntoConexion punto) {
		if(punto==null) return;
		if(punto.getNet()==null) {
			crearNet().addPin(punto);
		}
		
		punto.setOnMouseClicked(null);
		punto.setOnMouseClicked(e -> {
			if(puntoSeleccionado==null) {
				puntoSeleccionado=punto;
				punto.setStrokeWidth(GROSOR_SELECCION);
				logger.fine("Punto seleccionado: "+puntoSeleccionado);
			} else if(puntoSeleccionado!=punto) {
				if(puntoSeleccionado.getComponente()!=punto.getComponente()) {
					//Crear comando para deshacer/rehacer
					ConnectComponentsCommand connectCommand=new ConnectComponentsCommand(puntoSeleccionado, punto, this);
					UndoRedoManager.getInstance().ejecutarComando(connectCommand);
				} else {
					PuntoConexion puntoError=puntoSeleccionado;
					AnimadorUI.crearParpadeoError(punto, 3, Duration.millis(DURACION_PARPADEO));
					AnimadorUI.crearParpadeoError(puntoError, 3, Duration.millis(DURACION_PARPADEO));
					logger.warning("Intento de conexión no válido entre puntos del mismo componente");
					MensajesUI.mostrarMensaje(
							zonaDibujo, 
							"Conexión no válida", 
							MensajesUI.TipoMensaje.ERROR, 
							MensajesUI.PosicionMensaje.TOP, 
							Duration.seconds(1.5));
				}
				puntoSeleccionado.setStrokeWidth(GROSOR_NORMAL);
				puntoSeleccionado=null;
			} else {
				puntoSeleccionado.setStrokeWidth(GROSOR_NORMAL);
				puntoSeleccionado=null;
				logger.fine("Selección eliminada: "+puntoSeleccionado);
			}
			logger.fine("Punto registrado: "+punto);
			e.consume();
		});
	}
	
	/**
	 * Elimina un punto del registro del conector y limpia las referencias.
	 * 
	 * @param punto Punto a desregistrar
	 */
	public void desregistrarPunto(PuntoConexion punto) {
		if(punto==null) return;
		//Si está seleccionado, limpia la selección visual y referencia
		if(puntoSeleccionado==punto) {
			try {
				punto.setStrokeWidth(GROSOR_NORMAL);
			} catch(Throwable ignore) { }
			puntoSeleccionado=null;
		}
		//Limpia manejadores añadidos en registroPunto
		try { punto.setOnMouseClicked(null); } catch(Throwable ignore) { }
		try { punto.setOnMouseEntered(null); } catch(Throwable ignore) { }
		try { punto.setOnMouseExited(null); } catch(Throwable ignore) { }
		
		//Quita el pin de su net
		try {
			Net net=punto.getNet();
			if(net!=null) {
				try { net.eliminarPin(punto); } catch(Throwable ignore) { }
				try {
					List<PuntoConexion> pins=net.getPins();
					if(pins==null || pins.isEmpty()) {
						nets.remove(net);
					}
				} catch(Throwable ignore) { }
				try { punto.setNet(null); } catch(Throwable ignore) { }
			}
		} catch(Throwable t) {
			logger.log(Level.FINE, "DesregistrarPunto: cleanup parcial", t);
		}
	}
	
	/**
	 * Detecta clicks sobre cables para permitir conexión desde un punto ya seleccionado.
	 */
	private void configurarClickCables() {
		zonaDibujo.setOnMouseClicked((MouseEvent e) -> {
			if(puntoSeleccionado!=null) {
				double x=e.getX();
				double y=e.getY();
				
				for(Node n : zonaDibujo.getChildren()) {
					if(n instanceof Cable cable) {
						if(cable.getBoundsInParent().contains(x, y)) {
							cable.gestionClickConector(puntoSeleccionado, x, y);
							puntoSeleccionado.setStrokeWidth(GROSOR_NORMAL);
							puntoSeleccionado=null;
							e.consume();
							return;
						}
					}
				}
			}
		});
	}
	
	/**
	 * Conecta dos puntos de conexión, asignándoles el mismo nodo y creando un cable visual.
	 * 
	 * @param a					Primer punto de conexión
	 * @param b					Segundo punto de conexión
	 */
	public Cable conectarPuntos(PuntoConexion a, PuntoConexion b) {
		Net netA=a.getNet();
		Net netB=b.getNet();
		Net netFinal;
		
		if(netA==null && netB==null) {
			netFinal=crearNet();
			netFinal.addPin(a);
			netFinal.addPin(b);
		} else if(netA==null) {
			netFinal=netB;
			netFinal.addPin(a);
		} else if(netB==null) {
			netFinal=netA;
			netFinal.addPin(b);
		} else if(netA!=netB) {
			netFinal=(netA.getId() < netB.getId()) ? netA : netB;
			Net toRemove = (netFinal==netA) ? netB : netA;
			fusionar(netFinal, toRemove);
		} else {
			netFinal=netA;
		}
		a.setNet(netFinal);
		b.setNet(netFinal);
		
		asignarIdSiNecesario(netFinal);
		actualizarComponente(a.getComponente(), a.esPositivo(), netFinal.getId());
		actualizarComponente(b.getComponente(), b.esPositivo(), netFinal.getId());
		
		//Crear cable visual
		Cable cable=new CableBuilder()
				.desde(a)
				.hasta(b)
				.en(zonaDibujo)
				.usando(this)
				.construir();
		zonaDibujo.getChildren().add(cable);
		logger.fine("Cable visual creado entre puntos");
		
		if(a.getComponente() instanceof Tierra
				|| b.getComponente() instanceof Tierra) {
			int nodoTierra=(a.getComponente() instanceof Tierra) ? a.getNet().getId() : b.getNet().getId();
			Map<Integer, Integer> mapa=nodoManager.reordenarTierra(nodoTierra);
			aplicarReordenacion(mapa);
		}
		
		MensajesUI.mostrarMensaje(
			zonaDibujo,
			"Cable conectado",
			MensajesUI.TipoMensaje.EXITO,
			MensajesUI.PosicionMensaje.TOP,
			Duration.seconds(2)
		);
		logger.fine("Puntos conectados en Net "+netFinal.getId());
		return cable;
	}

	/**
	 * Asigna ID a un net en caso de que sea necesario.
	 * 
	 * @param net		Net al que asignamos el ID.
	 */
	private void asignarIdSiNecesario(Net net) {
		if(net.getId()==-1) {
			int id=nodoManager.asignarNodo();
			net.setId(id);
			nodoManager.incrementarCuenta(id);
			logger.fine("ID asignado a Net: "+id);
		}
	}
	
	/**
	 * Reordena el mapa de nets asignando nuevos numeros a los nodos según un mapa de nodos.
	 * 
	 * @param mapa		Mapa de nodos para reordenar
	 */
	private void aplicarReordenacion(Map<Integer, Integer> mapa) {
		for(Net n : nets) {
			int viejoId=n.getId();
			int nuevoId=mapa.getOrDefault(viejoId, viejoId);
			if(viejoId==nuevoId) continue;
			
			n.setId(nuevoId);
			
			for(PuntoConexion pin : n.getPins()) {
				pin.setNet(n);
				actualizarComponente(pin.getComponente(), pin.esPositivo(), nuevoId);
			}
			logger.fine("Net reordenada: "+viejoId+"->"+nuevoId);
		}
	}
	
	/**
	 * Actualiza los nodos del componente correspondiente.
	 * 
	 * @param comp				Componente al que pertenece el punto
	 * @param positivo			true si el punto es positivo, false si es negativo
	 * @param nodo				Identificador del nodo a asignar
	 */
	public void actualizarComponente(Componente comp, boolean positivo, int nodo) {
		if(comp==null) return;
		if(comp instanceof Tierra tierra) {
			tierra.setNodoTierra(nodo);
		} else {
			if(positivo) {
				comp.setNodo2(nodo);
			} else {
				comp.setNodo1(nodo);
			}
		}
		logger.fine("Actualizados los nodos del componente "+comp.getId());
	}
	
	/**
	 * Pide un nodo libre (reutilizado o nuevo).
	 * 
	 * @return nodo libre
	 */
	public int obtenerNodoLibre() {
		return nodoManager.asignarNodo();
	}
	
	/**
	 * Elimina visualmente un cable de la zona de dibujo (no modifica Nets).
	 * 
	 * @param cable			Cable a eliminar
	 */
	public void eliminarCable(Cable cable) {
		if(cable==null) return;
		try {
			if(cable.getParent() instanceof Pane p) {
				p.getChildren().remove(cable);
			}
			logger.fine(()->"Cable eliminado: "+cable.getCableId());
		} catch(Exception e) {
			logger.log(Level.FINE, "eliminarCable: no se pudo eliminar de forma limpia", e);
		}
	}
	
	/**
	 * Restaura visualmente un cable en la zona de dibujo (no modifica Nets).
	 * 
	 * @param cable			Cable a restaurar
	 */
	public void restaurarCable(Cable cable) {
		if(cable==null) return;
		try {
			if(!(cable.getParent() instanceof Pane)) {
				if(!zonaDibujo.getChildren().contains(cable)) {
					zonaDibujo.getChildren().add(cable);
				}
			} else {
				Pane p=(Pane) cable.getParent();
				if(!p.getChildren().contains(cable)) {
					p.getChildren().add(cable);
				}
			}
			logger.fine(()->"Cable restaurado: "+cable.getCableId());
		} catch(Exception e) {
			logger.log(Level.FINE, "restaurarCable: no se pudo restaurar de forma limpia", e);
		}
	}
	
	/**
	 * Asigna un pin a la Net indicada, añadiendo como pin si no estuviera.
	 * 
	 * @param pin			Punto de conexión de la net
	 * @param net			Net a la que se asigna el pin
	 */
	public void asignarPinANet(PuntoConexion pin, Net net) {
		if(pin==null) return;
		try {
			if(net!=null) {
				if(pin.getNet()!=net) {
					eliminarPinDeSuNet(pin);
					pin.setNet(net);
				}
				if(!net.getPins().contains(pin)) {
					net.addPin(pin);
				}
				asignarIdSiNecesario(net);
				actualizarComponente(pin.getComponente(), pin.esPositivo(), net.getId());
			} else {
				eliminarPinDeSuNet(pin);
			}
		} catch(Exception e) {
			logger.log(Level.FINE, "asignarPinANet: ajuste parcial", e);
		}
	}
	
	/**
	 * Elimina el pin de su Net actual; si la Net queda vacía, la borra.
	 * 
	 * @param pin 			Pin a eliminar
	 */
	public void eliminarPinDeSuNet(PuntoConexion pin) {
		if(pin==null) return;
		try {
			Net net=pin.getNet();
			if(net==null) return;
			try {
				net.eliminarPin(pin);
			} catch(Throwable ignore) {}
			pin.setNet(null);
			List<PuntoConexion> pins=net.getPins();
			if(pins==null || pins.isEmpty()) {
				if(net.getId()>=0) {
					nodoManager.decrementarCuenta(net.getId());
				}
				nets.remove(net);
			}
		} catch(Exception e) {
			logger.log(Level.FINE, "eliminarPinDeSuNet: ajuste parcial", e);
		}
	}
	
	/**
	 * Avisa al gestor de nodos que vamos a usar un extremo en este nodo.
	 * 
	 * @param nodo	Nodo que vamos al que vamos a incrementar su uso
	 */
	public void incrementarUsoNodo(int nodo) {
		nodoManager.incrementarCuenta(nodo);
	}
	
	/**
	 * Avisa al gestor de que liberamos un extremo en se nodo.
	 * 
	 * @param nodo Nodo al que vamos a decrementar su uso
	 */
	public void decrementarUsoNodo(int nodo) {
		nodoManager.decrementarCuenta(nodo);
	}
	
	/**
	 * Resetea el estado del conector, eliminando nets, 
	 * limpiando el nodo manager y eliminando los cables de la zona de dibujo.
	 */
	public void reset() {
		this.puntoSeleccionado=null;
		nets.clear();
		nodoManager.reset();
		zonaDibujo.getChildren().removeIf(n -> n instanceof Cable);
		logger.fine("ConectorPuntos reseteado");
	}
}
