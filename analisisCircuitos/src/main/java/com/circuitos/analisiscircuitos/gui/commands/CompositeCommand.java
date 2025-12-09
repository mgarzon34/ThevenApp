package com.circuitos.analisiscircuitos.gui.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando que implementa el patrón "Composite" para operaciones undo/redo.
 * Permite ejecutar varios comandos a la vez (p.ej. eliminar 3 componentes y deshacer al mismo tiempo).
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class CompositeCommand implements Command {
	private final String descripcion;
	private final List<Command> commands=new ArrayList<>();
	
	/**
	 * Constructor.
	 * 
	 * @param descripcion		Descripción de las acciones a realizar
	 */
	public CompositeCommand(String descripcion) {
		this.descripcion=descripcion!=null ? descripcion : "Acción compuesta";
	}
	
	/**
	 * Añade un comando a la lista de comandos a ejecutar.
	 * 
	 * @param cmd			Comando a añadir
	 * @return CompositeCommand de varios comandos
	 */
	public CompositeCommand add(Command cmd) {
		if(cmd!=null) commands.add(cmd);
		return this;
	}
	
	/**
	 * Comprueba si la lista de comandos está vacía.
	 * 
	 * @return {@code true} si está vacía, {@code false} si no
	 */
	public boolean isEmpty() {
		return commands.isEmpty();
	}
	
	/* Implementación métodos de la interfaz */
	
	@Override
	public void ejecutar() {
		for(Command c : commands) {
			if(c!=null && c.esValido()) 
				c.ejecutar();
		}
	}

	@Override
	public void deshacer() {
		for(int i=commands.size()-1; i>=0; i--) {
			Command c=commands.get(i);
			if(c!=null && c.esValido())
				c.deshacer();
		}
	}

	@Override
	public String getDescripcion() {
		return descripcion;
	}
	
	@Override
	public boolean esValido() {
		for(Command c : commands) {
			if(c!=null && c.esValido()) return true;
		}
		return false;
	}
	
	@Override
	public boolean puedeFusionarCon(Command otro) {
		return false; 		//No fusionar macros por defecto
	}
	
	@Override
	public void fusionarCon(Command otro) {
	}
}
