package com.circuitos.analisiscircuitos.gui.commands;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.gui.model.Cable;
import com.circuitos.analisiscircuitos.gui.service.undo.DescripcionesAccion;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/**
 * Comando para añadir un cable al diseño del circuito.
 * Maneja la adición y eliminación de cables en operaciones undo/redo.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class AddCableCommand implements Command {
	private static final Logger logger=Logger.getLogger(AddCableCommand.class.getName());
	
	private final Cable cable;
	private final Pane zonaDibujo;
	private final List<Cable> cablesCircuito;
	
	private int paneIndexUsado=-1;
	private int listIndexUsado=-1;
	private boolean ejecutado=false;
	private String descripcion;
	
	/**
	 * Constructor.
	 * 
	 * @param cable					Cable a añadir
	 * @param zonaDibujo			Zona de dibujo del circuito
	 * @param cablesCircuito		Lista de cables del circuito
	 */
	public AddCableCommand(Cable cable, Pane zonaDibujo, List<Cable> cablesCircuito) {
		this.cable=Objects.requireNonNull(cable, "cable");
		this.zonaDibujo=Objects.requireNonNull(zonaDibujo, "zonaDibujo");
		this.cablesCircuito=cablesCircuito;
		this.descripcion=DescripcionesAccion.anadir(cable);
	}
	
	/**
	 * Restringe un valor entero dentro de [min, max]
	 */
	private static int clamp(int val, int min, int max) {
		if(val<min) return min;
		if(val>max) return max;
		return val;
	}
	
	/* Implementación métodos de la interfaz */ 
	
	@Override
	public void ejecutar() {
		if(!ejecutado) {
			//Primera vez: calculamos y guardamos índices
			paneIndexUsado=zonaDibujo.getChildren().size();
			if(cablesCircuito!=null) {
				listIndexUsado=cablesCircuito.size();
			}
		} else {
			//Redo: clamp por si cambió el tamaño
			paneIndexUsado=clamp(paneIndexUsado, 0, zonaDibujo.getChildren().size());
			if(cablesCircuito!=null) {
				listIndexUsado=clamp(listIndexUsado, 0, cablesCircuito.size());
			}
		}
		//Si pertenece a otro padre, desacoplar primero
		Parent parent=cable.getParent();
		if(parent!=null && parent!=zonaDibujo && parent instanceof Pane p) {
			p.getChildren().remove(cable);
		}
		//Reubicar en Pane en el índice exacto
		if(zonaDibujo.getChildren().contains(cable)) {
			zonaDibujo.getChildren().remove(cable);
		}
		zonaDibujo.getChildren().add(paneIndexUsado, cable);
		
		//Reubicar en lista de dominio
		if(cablesCircuito!=null) {
			if(cablesCircuito.contains(cable)) {
				cablesCircuito.remove(cable);
			}
			cablesCircuito.add(listIndexUsado<0 ? cablesCircuito.size() : listIndexUsado, cable);
		}
		if(!ejecutado) {
			this.descripcion=DescripcionesAccion.anadir(cable);
		}
		ejecutado=true;
		logger.fine("Cable añadido: "+(cable!=null ? cable.getCableId() : "desconocido"));
	}

	@Override
	public void deshacer() {
		if(!ejecutado) return;
		zonaDibujo.getChildren().remove(cable);
		if(cablesCircuito!=null) {
			cablesCircuito.remove(cable);
		}
		ejecutado=false;
		logger.fine("Cable eliminado por -deshacer-: "+(cable!=null ? cable.getCableId() : "desconocido"));
	}

	@Override
	public String getDescripcion() {
		return descripcion;
	}
	
	@Override
	public boolean esValido() {
		//El comando es válido si el cable y la zona de dibujo existen
		return cable!=null && zonaDibujo!=null;
	}
}
