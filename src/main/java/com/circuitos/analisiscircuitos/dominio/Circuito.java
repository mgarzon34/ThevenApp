package com.circuitos.analisiscircuitos.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.circuitos.analisiscircuitos.dominio.util.FormatUtil;
import com.circuitos.analisiscircuitos.dominio.util.GraphUtil;
import com.circuitos.analisiscircuitos.dominio.util.NodeMapUtil;
import com.circuitos.analisiscircuitos.dominio.util.Unidades;
import com.circuitos.analisiscircuitos.dominio.util.Unidades.Type;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase Circuito que define un circuito y sus operaciones correspondientes.
 * 
 * @author 	Marco Antonio Garzon Palos
 * @version 1.0 (2025)
 */
public class Circuito {
	private static final Logger logger=Logger.getLogger(Circuito.class.getName());
	private final List<Componente> componentes=new ArrayList<>();
	private final Map<Integer, Integer> nodos=new HashMap<>();
	
	/**
	 * Constructor. Construye un circuito vacío.
	 */
	public Circuito() { /* Vacío */ }
	
	/**
	 * Añade un nuevo componente al circuito.
	 * 
	 * @param componente	puede ser cualquier tipo de componente
	 * @throws IllegalArgumentException si ya existe Tierra y se añade otra
	 */
	public void addComponente(Componente componente) {
		Objects.requireNonNull(componente, "El componente no puede ser null");
		if(componente instanceof Tierra && hayTierra()) {
			logger.log(Level.WARNING, "Sólo se permite un componente Tierra. Ignorador: {0}", componente);
			throw new IllegalArgumentException("Sólo se permite un componente Tierra en un circuito.");
		}
		componentes.add(componente);
		NodeMapUtil.registrarNodo(nodos, componente.getNodo1());
		NodeMapUtil.registrarNodo(nodos, componente.getNodo2());
		logger.log(Level.INFO, "Componente registrado {0} valor={1} nodos=({2}, {3})",
				new Object[] {componente, componente.getValor(), componente.getNodo1(), componente.getNodo2()});
	}
	
	/**
	 * Elimina un componente del circuito.
	 * Actualiza la lista de nodos; si un nodo queda en desuso se elimina.
	 * 
	 * @param componente	cualquier componente del circuito
	 * @throws NullPointerException si {@code componente} es null
	 */
	public void eliminarComponente(Componente componente) {
		Objects.requireNonNull(componente, "El componente no puede ser null");
		if(componentes.remove(componente)) {
			NodeMapUtil.eliminarNodoNoUsado(nodos, componentes, componente.getNodo1());
			NodeMapUtil.eliminarNodoNoUsado(nodos, componentes, componente.getNodo2());
			logger.log(Level.INFO, "Eliminado componente {0} valor={1}",
					new Object[] {componente, componente.getValor()});
		} else {
			logger.log(Level.FINE, "Intento de eliminar componente inexistente: {0}", componente);
		}
	}
	
	/**
	 * Devuelve una lista con todos los componentes del circuito.
	 * 
	 * @return lista de componentes
	 */
	@JsonProperty("componentes")
	public List<Componente> getComponentes() {
		return componentes;
	}
	
	/**
	 * Filtra componentes por clase.
	 * 
	 * @param <T>		clase abstracta que se filtra
	 * @param tipo		clase por la se va a filtrar (extiende la clase Componente)
	 * @return lista	lista de componentes filtrados.		
	 */
	@JsonIgnore
	private <T extends Componente> List<T> filtrarTipo(Class<T> tipo) {
		return componentes.stream()
				.filter(tipo::isInstance)
				.map(tipo::cast)
				.collect(Collectors.toList());
	}
	
	/**
	 * Devuelve la lista de resistencias conectadas en el circuito.
	 * 
	 * @return lista de resistencias
	 */
	@JsonIgnore
	public List<Resistencia> getResistencias() {
		return filtrarTipo(Resistencia.class);
	}
	
	/**
	 * Devuelve la lista de fuentes de tensión independientes conectadas al circuito.
	 * 
	 * @return lista de fuentes de tensión independientes
	 */
	@JsonIgnore
	public List<FuenteTensionInd> getFuentesTensionInd() {
		return filtrarTipo(FuenteTensionInd.class);
	}
	
	/**
	 * Devuelve la lista de fuentes de corriente independientes conectadas al circuito.
	 * 
	 * @return lista de fuentes de corriente independientes
	 */
	@JsonIgnore
	public List<FuenteCorrienteInd> getFuentesCorrienteInd() {
		return filtrarTipo(FuenteCorrienteInd.class);
	}
	
	/**
	 * Devuelve la lista de fuentes de tensión dependientes conectadas al circuito.
	 * 
	 * @return lista de fuentes de tensión dependientes
	 */
	@JsonIgnore
	public List<FuenteTensionDependiente> getFuentesTensionDep() {
		return filtrarTipo(FuenteTensionDependiente.class);
	}
	
	/**
	 * Devuelve la lista de fuentes de corriente dependientes conectadas al circuito.
	 * 
	 * @return lista de fuentes de corriente dependientes
	 */
	@JsonIgnore
	public List<FuenteCorrienteDependiente> getFuentesCorrienteDep() {
		return filtrarTipo(FuenteCorrienteDependiente.class);
	}
	
	/**
	 * Devuelve la lista de nodos de los que se compone el circuito.
	 * 
	 * @return lista de nodos del circuito
	 */
	@JsonIgnore
	public Map<Integer, Integer> getNodos() {
		return nodos;
	}
	
	/**
	 * Elimina resistencias que no están haciendo nada en el circuito.
	 */
	public void eliminarResistenciasInutiles() {
		componentes.removeIf(c->(c instanceof Resistencia) && (c.getNodo1()==c.getNodo2()));
		logger.log(Level.FINE, "Resistencias inútiles eliminadas");
	}
	
	/**
	 * Depura los nodos una vez que se realiza una reducción de resistencias.
	 * 
	 * @param terminales terminales de cálculo no se tocan
	 */
	
	public void depurarNodos(Set<Integer> terminales) {
		Objects.requireNonNull(terminales, "Terminales no puede ser null");
		//Obtener los nodos usados de forma ordenada
		TreeSet<Integer> usados=new TreeSet<>();
		for(Componente c : componentes) {
			usados.add(c.getNodo1());
			usados.add(c.getNodo2());
		}
		
		//Quitar los terminales para asignarles índices fijos.
		usados.removeAll(terminales);
		logger.log(Level.FINE, "Quitando terminales para depurar nodos.");
		
		//Crear lista final: primero los terminales en orden y luego el resto
		List<Integer> listaFinal=new ArrayList<>(terminales);
		Collections.sort(listaFinal);
		listaFinal.addAll(usados);
		
		//Asignar nuevos índices
		Map<Integer, Integer> mapa=new HashMap<>();
		for(int i=0; i<listaFinal.size(); i++) {
			mapa.put(listaFinal.get(i), i);
		}
		
		//Actualizar cada componente con los nuevos nodos
		for(Componente c : componentes) {
			c.setNodo1(mapa.get(c.getNodo1()));
			c.setNodo2(mapa.get(c.getNodo2()));
			logger.log(Level.INFO, "Nodos actualizados {0}: ({1}, {2})",
					new Object[] {c, c.getNodo1(), c.getNodo2()});
		}
		NodeMapUtil.actualizarMapaNodos(nodos, componentes);
	}
	
	/**
	 * Realiza una copia exacta del circuito original para trabajar con ella y,
	 * de esta forma, no tener que modificar el circuito original. 
	 * 
	 * @return copia del circuito original
	 */
	public Circuito copiar() {
		Circuito nuevoCircuito=new Circuito();
		for(Componente comp : this.getComponentes()) {
			if(comp instanceof Tierra) continue;
			nuevoCircuito.addComponente(comp.clonar());
		}
		logger.log(Level.INFO, "Creada copia exacta del circuito original.");
		return nuevoCircuito;
	}
	
	/**
	 * Modifica las resistencias de la lista de componentes, eliminando las viejas y
	 * añadiendo las nuevas.
	 * @param nuevasResistencias resistencias que se añaden al circuito
	 */
	public void setResistencias(ArrayList<Resistencia> nuevasResistencias) {
		Objects.requireNonNull(nuevasResistencias, "La lista de resistencias no puede ser null");
		componentes.removeIf(c -> c instanceof Resistencia); //Eliminar resistencias viejas
		componentes.addAll(nuevasResistencias); //Añadir resistencias nuevas.
		logger.log(Level.INFO, "Resistencias actualizadas. Cantidad: {0}", nuevasResistencias.size());
	}
	
	/**
	 * Abre el circuito entre dos nodos concretos (para poder calcular los 
	 * equivalentes de Thevenin o Norton).
	 * 
	 * @param nodoA		
	 * @param nodoB
	 * @return circuito abierto entre nodoA y nodoB
	 */
	public Circuito abrirCircuitoEntreNodos(int nodoA, int nodoB) {
		Circuito copia=this.copiar();
		copia.getComponentes().removeIf(c ->
				c.isCarga() && (
						(c.getNodo1()==nodoA && c.getNodo2()==nodoB) || 
						(c.getNodo1()==nodoB && c.getNodo2()==nodoA)));
		logger.log(Level.INFO, "Circuito abierto entre nodos {0} y {1}", new Object[] {nodoA, nodoB});
		copia.eliminarResistenciasInutiles();
		NodeMapUtil.actualizarMapaNodos(copia.getNodos(), copia.getComponentes());
		return copia;
	}
	
	/**
	 * Comprueba si existe un camino (eléctrico) a través de cualquier componente
	 * entre dos nodos dados en el circuito.
	 * 
	 * @param origen
	 * @param destino
	 * @return true si hay camino, false si no lo hay
	 */
	public boolean existeCaminoEntreNodos(int origen, int destino) {
		return GraphUtil.caminoNodos(componentes, origen, destino);
	}
	
	/**
	 * Elimina del circuito los componentes que no están en el subcircuito
	 * conexo entre nodoA y nodoB.
	 * 
	 * @param nodoA
	 * @param nodoB
	 */
	public void eliminarComponentesDesconectados(int nodoA, int nodoB) {
		GraphUtil.eliminarDesconectados(componentes, nodoA, nodoB);
		logger.log(Level.FINE, "Eliminados componentes desconectados entre {0}-{1}",
				new Object[] {nodoA, nodoB});
	}

	/**
	 * Obtiene, mediante recorrido BFS (por niveles), el conjunto de nodos conectados
	 * a partir de uno inicial.
	 * 
	 * @param nodoInicial nodo de partida para recorrido BFS
	 * @return Conjunto de nodos conexos.
	 */
	public Set<Integer> obtenerNodosConexos(int nodoInicial) {
		return GraphUtil.nodosConexos(componentes, nodoInicial);
	}
	
	/**
	 * Elimina los nodos "colgantes" (con grado 1) que no sean los terminales
	 * especificados, eliminando la resistencia incidente.
	 * 
	 * @param nodoA		nodo terminal
	 * @param nodoB		nodo terminal
	 */
	public void eliminarNodosColgantes(int nodoA, int nodoB) {
		GraphUtil.eliminarNodosColgantes(componentes, nodoA, nodoB);
		logger.log(Level.FINE, "Eliminados nodos colgantes.");
	}
	
	/**
	 * Da un formato determinado a un valor concreto para mejorar su lectura.
	 * 
	 * @param valor		valor que se formatea
	 * @return valor 	valor formateado
	 */
	@SuppressWarnings("exports")
	public String formatearValor(double valor, Unidades.Type tipo) {
		return FormatUtil.format(valor, tipo);
	}
	
	/**
	 * Comprueba si ya hay un componente Tierra en la lista de componentes.
	 * 
	 * @return {@code true} si ya hay tierra, {@code false} si no
	 */
	private boolean hayTierra() {
		return componentes.stream().anyMatch(c -> c instanceof Tierra);
	}
	
	/**
	 * Muestra por log un resumen del circuito.
	 */
	public void showCircuito() {
		logger.log(Level.INFO, "Resumen del circuito:\nNodos: {0}\nComponentes:\n{1}",
				new Object[] {nodos.keySet(),
						componentes.stream()
							.map(c->c.getTipo()+" "+formatearValor(c.getValor(), inferirTipo(c)) +
									" nodos(" + c.getNodo1()+","+c.getNodo2()+")")
							.collect(Collectors.joining("\n"))});
	}
	
	/**
	 * Infiere el tipo de componente para poder mostrar el log de resumen del circuito.
	 * 
	 * @param c		Componente del que se busca el tipo
	 * @return Tipo de componente
	 */
	private Type inferirTipo(Componente c) {
		switch(c.getTipo()) {
			case "Resistencia": return Type.RESISTENCIA;
			case "Fuente Tension Independiente":
			case "Fuente Tension Dependiente": return Type.TENSION;
			case "Fuente Corriente Independiente":
			case "Fuente Corriente Dependiente": return Type.CORRIENTE;
			default: return Type.RESISTENCIA;
		}
	}
}