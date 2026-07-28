package com.circuitos.analisiscircuitos.gui.service.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que se encarga de asignar los nodos cuando se realizan las conexiones.
 * Asigna nodos en orden creciente desde 0; recicla los nodos que queden libres al eliminar
 * componentes y renumera el conjunto cuando se asigna un nodo como Tierra (este siempre sera nodo 0).
 * 
 * @author Marco Antonio Garzon Palos
 * @version 1.0
 */
public class NodoManager {
	
	private static final Logger logger=Logger.getLogger(NodoManager.class.getName());
	private final PriorityQueue<Integer> nodosLibres=new PriorityQueue<>();
	private final Map<Integer, Integer> cuentaNodo=new HashMap<>();
	private int nextNodo=1;
	
	public NodoManager() { }
	
	/**
	 * Devuelve el siguiente nodo disponible.
	 * Si hay alguno en nodosLibres coge el pequeño sino usa nextNodo.
	 * 
	 * @return número de nodo asignado (>=0)
	 */
	public int asignarNodo() {
		final int nodo;
		if(!nodosLibres.isEmpty()) {
			nodo=nodosLibres.poll(); //usa el más pequeño
		} else {
			nodo=nextNodo;
			nextNodo++;
		}
		cuentaNodo.putIfAbsent(nodo, 0);
		logger.fine("Nodo asignado: "+nodo);
		return nodo;
	}
	
	/**
	 * Incrementa la cuenta de conexiones que tiene un nodo.
	 * 
	 * @param nodo número de nodo que se usa en un extremo
	 */
	public void incrementarCuenta(int nodo) {
		if(nodo<0) return;
		int prev=cuentaNodo.getOrDefault(nodo, 0);
		cuentaNodo.put(nodo, prev+1);
		logger.finer(()->"Cuenta incrementada para nodo "+nodo+": "+(prev+1));
	}
	
	/**
	 * Decrementa en uno la cuenta de conexiones del nodo; si llegamos a 0,
	 * entonces el nodo se añade a nodosLibres y se podrá reutilizar.
	 * 
	 * @param nodo número de nodo que dejamos libre
	 */
	public void decrementarCuenta(int nodo) {
		if(nodo<0) return;
		Integer prev=cuentaNodo.get(nodo);
		if(prev==null) return;
		if(prev<=1) {
			cuentaNodo.remove(nodo);
			nodosLibres.offer(nodo);
			logger.fine("Nodo liberado: "+nodo);
		} else {
			cuentaNodo.put(nodo, prev-1);
			logger.finer(()->"Cuenta decrementada para nodo "+nodo+": "+(prev-1));
		}
	}
	
	/**
	 * Reasigna números de nodos de forma circular para que el nodoTierra pase a ser 0
	 * y el resto sigan en orden creciente.
	 * 
	 * @param nodoTierra	número de nodo (actual) que queremos que sea 0.
	 * @return Map que indica, para cada antiguo nodo activo, su nuevo número de nodo.
	 * @throws IllegalArgumentException si nodoTierra no está disponible actualmente
	 */
	public Map<Integer, Integer> reordenarTierra(int nodoTierra) {
		if(nodoTierra<0) throw new IllegalArgumentException("El nodo tierra no puede ser negativo.");
		List<Integer> nodosActivos=new ArrayList<>(cuentaNodo.keySet());
		if(!nodosActivos.contains(nodoTierra)) {
			throw new IllegalArgumentException("El nodo "+nodoTierra+" no está activo y no puede usarse como tierra.");
		}
		Collections.sort(nodosActivos);
		int i=nodosActivos.indexOf(nodoTierra);
		
		List<Integer> rotacion=new ArrayList<>();
		for(int j=i; j<nodosActivos.size(); j++) {
			rotacion.add(nodosActivos.get(j));
		}
		for(int j=0; j<i; j++) {
			rotacion.add(nodosActivos.get(j));
		}
		Map<Integer, Integer> newMap=new HashMap<>(rotacion.size());
		Map<Integer, Integer> newCuenta=new HashMap<>(rotacion.size());
		for(int newIndex=0; newIndex<rotacion.size(); newIndex++) {
			int oldNodo=rotacion.get(newIndex);
			newMap.put(oldNodo, newIndex);
			int cnt=cuentaNodo.getOrDefault(oldNodo, 0);
			newCuenta.put(newIndex, cnt);
		}
		cuentaNodo.clear();
		cuentaNodo.putAll(newCuenta);
		int M=rotacion.size();
		nodosLibres.clear();
		nextNodo=M;
		logger.log(Level.INFO, "Nodos reordenados, nodoTierra {0} -> 0", nodoTierra);
		return newMap;
	}
	
	/**
	 * Devuelve un {@link Set} con todos los nodos que están en uso.
	 * 
	 * @return {@link Set} con todos los nodos activos actualmente.
	 */
	public Set<Integer> getNodosActivos() {
		return Collections.unmodifiableSet(cuentaNodo.keySet());
	}
	
	/**
	 * Devuelve cuál sería el próximo nodo si no hubiera ninguno libre.
	 * 
	 * @return Siguiente nodo si no se reutiliza uno anterior
	 */
	public int getNextNodo() {
		return nextNodo;
	}
	
	/**
	 * Reinicia la asignación de nodos, borrando todos los nodos activos y comenzando desde cero.
	 */
	public void reset() {
		nodosLibres.clear();
		cuentaNodo.clear();
		nextNodo=1;
		logger.fine("NodoManager reiniciado");
	}
}
