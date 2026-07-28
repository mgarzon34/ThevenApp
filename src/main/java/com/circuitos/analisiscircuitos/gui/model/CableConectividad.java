package com.circuitos.analisiscircuitos.gui.model;

import java.util.List;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.dominio.Tierra;

/**
 * Clase auxiliar que gestiona las validaciones y reparaciones de conectividad eléctrica.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class CableConectividad {
	private static final Logger logger=Logger.getLogger(CableConectividad.class.getName());
	
	/**
	 * Verifica si dos puntos de conexión pertenecen a la misma red (net) eléctrica.
	 * 
	 * @param inicio				Punto de inicio
	 * @param fin					Punto de final
	 * @return {@code true} si pertenecen a la misma net, {@code false} si no
	 */
	public static boolean verificarConectividad(PuntoConexion inicio, PuntoConexion fin) {
		if(inicio.getNet()==null || fin.getNet()==null) return false;
		if(inicio.getNet()!=fin.getNet()) return false;
		
		if(esTierraInvalida(inicio) || esTierraInvalida(fin)) {
			logger.warning("Componente Tierra no tiene nodo 0, requerirá corrección");
			return false;
		}
		return true;
	}
	
	/**
	 * Comrpueba si el punto de conexión de tierra posee un nodo distinto de 0, en cuyo caso es una 
	 * condición no válida.
	 * 
	 * @param p					Punto de conexión a evaluar
	 * @return {@code true} si Tierra tiene un nodo diferente a 0, {@code false} si es 0
	 */
	private static boolean esTierraInvalida(PuntoConexion p) {
		return p.getComponente() instanceof Tierra && p.getNet().getId()!=0;
	}
	
	/**
	 * Restaura la conectividad eléctrica entre dos puntos cuando un cable es reconstruido.
	 * 
	 * @param conector					Gestor de conexiones
	 * @param inicio					Punto de conexion inicial
	 * @param fin						Punto de conexión final
	 * @param cableId					Identificador del cable
	 */
	public static void restaurarConectividad(ConectorPuntos conector, PuntoConexion inicio, PuntoConexion fin, String cableId) {
		if(conector!=null)  {
			conector.conectarPuntos(inicio, fin);
			logger.info("Conectividad restaurada para cable: "+cableId);
		}
	}
	
	/**
	 * Asegura que los puntos intermedios por división del cable mantengan la conectividad.
	 * 
	 * @param conector					Gestor de conexiones
	 * @param intermedios				Lista de puntos intermedios
	 */
	public static void mantenerConectividad(ConectorPuntos conector, List<PuntoConexion> intermedios) {
		for(PuntoConexion intermedio : intermedios) {
			if(intermedio.getNet()==null && conector!=null) {
				conector.registrarPunto(intermedio);
			}
		}
	}
}
