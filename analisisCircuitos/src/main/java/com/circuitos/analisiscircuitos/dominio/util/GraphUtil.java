package com.circuitos.analisiscircuitos.dominio.util;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.dominio.Componente;

/**
 * Clase con herramientas para el control de recorridos BFS y poda de subcircuitos.
 * Contiene herramientas para comprobar camino eléctrico entre dos nodos, los nodos alcanzables desde otro inicial
 * eliminación de componentes fuera de un subgrafo conexo o eliminación de nodos colgantes.
 * 
 * @author 	Marco Antonio Garzon Palos
 * @version 1.0 (2025)
 */
public class GraphUtil {
	private static final Logger logger=Logger.getLogger(GraphUtil.class.getName());
	
	private GraphUtil() { /* No instanciable */ }
	
	/**
	 * Comprueba si existe un camino (eléctrico) a través de cualquier componente
	 * entre dos nodos dados en el circuito.
	 * 
	 * @param comps			componentes de comprobación
	 * @param origen		nodo de origen
	 * @param destino		nodo de destino
	 * @return true/false	true si hay camino eléctrico, false si no lo hay
	 */
	public static boolean caminoNodos(Collection<Componente> comps, int origen, int destino) {
		Objects.requireNonNull(comps, "Lista de componentes no puede ser null");
		logger.log(Level.FINE, "Comprobando camino entre nodos {0} y {1}", new Object[] {origen, destino});
		return bfs(comps, origen).contains(destino);
	}
	
	/**
	 * Obtiene, mediante recorrido BFS (por niveles), el conjunto de nodos conectados
	 * a partir de uno inicial.
	 * 
	 * @param comps			componentes de comprobación
	 * @param nodoInicial	nodo de partida
	 * @return out			conjutno de nodos conectados
	 */
	public static Set<Integer> nodosConexos(Collection<Componente> comps, int nodoInicial) {
		Objects.requireNonNull(comps, "Lista de componentes no puede ser null");
		return bfs(comps, nodoInicial);
	}
	
	/**
	 * Devuelve el conjunto de nodos alcanzables desde un nodo origen. Recorrido BFS (por niveles).
	 * 
	 * @param comps			componentes de comprobación
	 * @param origen		nodo de partida
	 * @return nodos		conjunto de nodos alcanzables desde origen
	 */
	private static Set<Integer> bfs(Collection<Componente> comps, int origen) {
		Set<Integer> vistos=new HashSet<>();
		Queue<Integer> q=new LinkedList<>();
		q.add(origen);
		while(!q.isEmpty()) {
			int u=q.poll();
			if(!vistos.add(u)) continue;
			for(Componente c : comps) {
				int a=c.getNodo1(), b=c.getNodo2();
				if(a==u && !vistos.contains(b)) q.add(b);
				if(b==u && !vistos.contains(a)) q.add(a);
			}
		}
		logger.log(Level.FINER, "BFS desde {0} alcanzó nodos {1}", new Object[] {origen, vistos});
		return vistos;
	}
	
	/**
	 * Elimina del circuito los componentes que no están en el subcircuito
	 * conexo entre nodoA y nodoB.
	 * 
	 * @param comps						componentes de comprobación
	 * @param A							nodo A
	 * @param B							nodo B
	 * @throws IllegalStateException	si no existe conexión entre A y B
	 */
	public static void eliminarDesconectados(Collection<Componente> comps, int A,  int B) {
		Objects.requireNonNull(comps, "Lista de componentes no puede ser null");
		var alcanzados=nodosConexos(comps, A);
		if(!alcanzados.contains(B)) {
			throw new IllegalStateException("No hay conexión A-B");
		}
		comps.removeIf(c->!alcanzados.contains(c.getNodo1()) || !alcanzados.contains(c.getNodo2()));
		logger.log(Level.FINE, "Componentes desconectados eliminados, restantes: {0}", comps.size());
	}
	
	/**
	 * Elimina los nodos "colgantes" (con grado 1) que no sean los terminales
	 * especificados, eliminando la resistencia incidente.
	 * 
	 * @param comps		componentes de comprobación
	 * @param A			nodo A
	 * @param B			nodo B
	 */
	public static void eliminarNodosColgantes(Collection<Componente> comps, int A, int B) {
		Objects.requireNonNull(comps, "Lista de componentes no puede ser null");
		boolean cambios;
		do {
			cambios=false;
			Map<Integer, Integer> grado=new HashMap<>();
			for(Componente c : comps) {
				if(c instanceof com.circuitos.analisiscircuitos.dominio.Resistencia) {
					grado.merge(c.getNodo1(), 1, Integer::sum);
					grado.merge(c.getNodo2(), 1, Integer::sum);
				}
			}
			for(Integer node : new ArrayList<>(grado.keySet())) {
				if(node!=A && node!=B && grado.get(node)==1) {
					comps.removeIf(c->c instanceof com.circuitos.analisiscircuitos.dominio.Resistencia &&
							(c.getNodo1()==node || c.getNodo2()==node));
					cambios=true;
					break;
				}
			}
		} while(cambios);
	}
	
	/**
	 * Detecta los nodos de bornes de una rama de carga validando, previamente, la selección de componentes
	 * de carga para comprobar que pertenecen a un subgrafo conexo. De lo contrario, lanza excepciones.
	 * @param compCarga			Lista de componentes de carga
	 * @return Array con los nodos de los bornes de carga
	 * @throws IllegalArgumentException si la selección no es válida
	 */
	public static int[] detectarBornesCarga(Collection<Componente> compCarga, Collection<Componente> todoCircuito) {
		Objects.requireNonNull(compCarga, "Lista de componentes de carga no puede ser null");
		Objects.requireNonNull(todoCircuito, "Circuito total no puede ser null");
		if(compCarga.isEmpty()) {
			throw new IllegalArgumentException("No se ha seleccionado ningún componente de carga.");
		}
		if(compCarga.stream().anyMatch(GraphUtil::esFuente)) {
			throw new IllegalArgumentException(
					"La carga no puede contener fuentes de tensión o corriente. Revise el circuito.");
		}
		//Varios componentes deben estar en un subgrafo conexo
		Set<Integer> nodosCarga=new HashSet<>();
		for(Componente c : compCarga) {
			nodosCarga.add(c.getNodo1());
			nodosCarga.add(c.getNodo2());
		}
		//BFS interno solo con componentes de carga partir de nodo más pequeño
		Integer semilla=nodosCarga.stream().min(Integer::compareTo).orElse(0);
		Set<Integer> alcanzables=bfs(compCarga, semilla);
		for(Componente c : compCarga) {
			if(!alcanzables.contains(c.getNodo1()) || !alcanzables.contains(c.getNodo2())) {
				throw new IllegalArgumentException("Los componentes de carga no forman un bloque conectado único.");
			}
		}
		//Detectar nodos frontera (Intersección entre carga y resto del circuito)
		Set<Integer> nodosResto=new HashSet<>();
		for(Componente c : todoCircuito) {
			if(!compCarga.contains(c)) {
				nodosResto.add(c.getNodo1());
				nodosResto.add(c.getNodo2());
			}
		}
		//La intersección son los bornes
		Set<Integer> bornes=new HashSet<>(nodosCarga);
		bornes.retainAll(nodosResto);
		if(bornes.size()!=2) {
			throw new IllegalArgumentException(
					"La carga seleccionada tiene "+bornes.size()+" puntos de conexión con el circuito."+
					"Debe tener exactamente 2. Verifique las conexiones.");
		}
		List<Integer> listaBornes=new ArrayList<>(bornes);
		logger.log(Level.FINE, "Bornes frontera detectados: {0} y {1}", new Object[] {listaBornes.get(0), listaBornes.get(1)});
		return new int[] {listaBornes.get(0), listaBornes.get(1)};
	}
	
	/**
	 * Devuelve true si el componente es una fuente (tensión o corriente). Se usa para impedir que se marquen como carga.
	 * 
	 * @param c					Componente de comprobación
	 * @return {@code true} si es fuente, {@code false} si no
	 */
	private static boolean esFuente(Componente c) {
		return c instanceof com.circuitos.analisiscircuitos.dominio.FuenteTensionInd
				|| c instanceof com.circuitos.analisiscircuitos.dominio.FuenteCorrienteInd
				|| c instanceof com.circuitos.analisiscircuitos.dominio.FuenteTensionDependiente
				|| c instanceof com.circuitos.analisiscircuitos.dominio.FuenteCorrienteDependiente
				|| c instanceof com.circuitos.analisiscircuitos.dominio.FuenteDependiente;
	}
}
