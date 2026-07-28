package com.circuitos.analisiscircuitos.gui.commands;

/**
 * Interfaz base para aplicar el patrón "Command" en la operación deshacer/rehacer.
 * 
 * Cada comando representa una acción reversible que puede ser ejecutada y,
 * posteriormente, deshecha o rehecha.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public interface Command {
	
	/**
	 * Ejecuta el comando. Se llama cuando se ejecuta la acción por primera vez
	 * o se hace "rehacer".
	 */
	void ejecutar();
	
	/**
	 * Deshace el comando. 
	 */
	void deshacer();
	
	/**
	 * Obtiene una descripción legible de la acción que realiza este comando. 
	 * Se usa para mostrar en la interfaz de usuario qué acción se deshace o se rehace.
	 * 
	 * @return Descripción del comando (ej.: "Añadir resistencia", "mover cable")
	 */
	String getDescripcion();
	
	/**
	 * Indica si este comando se puede fusionar con otro comando similar.
	 * Es útil para agrupar comandos de movimiento continuo en uno solo.
	 * 
	 * @param otroComando			Otro comando para verificar si se puede fusionar.
	 * @return {@code true} si los comandos se pueden fusionar, {@code false} si no
	 */
	default boolean puedeFusionarCon(Command otroComando) {
		return false;
	}
	
	/**
	 * Fusiona este comando con otro comando similar.
	 * Solo se llama si "puedeFusionarCon" devuelve {@code true}.
	 * 
	 * @param otroComando			Comando a fusionar con este
	 */
	default void fusionarCon(Command otroComando) {
		throw new UnsupportedOperationException("Este comando no soporta fusión");
	}
	
	/**
	 * Verifica si este comando es válido y puede ser ejecutado.
	 * Por ejemplo, un comando de cable puede ser inválido si los componentes conectados ya no existen.
	 * 
	 * @return {@code true} si el comando puede ser ejecutado, {@code false} si no
	 */
	default boolean esValido() {
		return true;
	}
}
