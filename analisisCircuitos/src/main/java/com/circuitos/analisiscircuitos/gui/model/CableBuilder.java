package com.circuitos.analisiscircuitos.gui.model;

import java.util.List;

import com.circuitos.analisiscircuitos.dominio.util.GestorIds;

import javafx.scene.layout.Pane;

/**
 * Clase que implementa el patrón Builder para construir cables de una manera sencilla.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class CableBuilder {
	
	private String id;
	private PuntoConexion inicio;
	private PuntoConexion fin;
	private Pane zonaDibujo;
	private ConectorPuntos conector;
	private List<Double> puntosXY;
		
	/**
	 * Métodos que permiten construir un cable con instrucciones sencillas:
	 * Nuevo CableBuilder - desde(PuntoConexion) - hasta(PuntoConexion)- conId(String) - 
	 * en(zonaDibujo) - usando(conector) - conTrayectoria(coordenadas) - construir.
	 */
	public CableBuilder desde(PuntoConexion inicio) {
		this.inicio=inicio;			
		return this;
	}	
	public CableBuilder hasta(PuntoConexion fin) {
		this.fin=fin;
		return this;
	}	
	public CableBuilder conId(String id) {
		this.id=id;
		return this;
	}	
	public CableBuilder en(Pane zonaDibujo) {
		this.zonaDibujo=zonaDibujo;
		return this;
	}	
	public CableBuilder usando(ConectorPuntos conector) {
		this.conector=conector;
		return this;
	}	
	public CableBuilder conTrayectoria(List<Double> puntosXY) {
		this.puntosXY=puntosXY;
		return this;
	}	
	public Cable construir() {
		Cable cable;
		if(inicio==null || fin==null || zonaDibujo==null || conector==null)
			throw new IllegalStateException("Faltan parámetros obligatorios en el builder de Cable.");
		if(puntosXY!=null) {
			cable=new Cable(inicio, fin, zonaDibujo, conector, puntosXY);
		} else {
			cable=new Cable(inicio, fin, zonaDibujo, conector);
		}
		if(this.id!=null) {
			cable.setCableId(this.id);
			cable.setId(this.id);
		} else {
			String nuevoId=GestorIds.getInstance().generarId("Cable-");
			cable.setCableId(nuevoId);
			cable.setId(nuevoId);
		}
		return cable;
	}
}
