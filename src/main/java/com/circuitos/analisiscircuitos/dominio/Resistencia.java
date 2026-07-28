package com.circuitos.analisiscircuitos.dominio;

import java.util.Objects;

import com.circuitos.analisiscircuitos.dominio.util.FormatUtil;
import com.circuitos.analisiscircuitos.dominio.util.Unidades;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Clase que extiende la clase abstracta Componente para definir
 * una resistencia.
 * 
 * @author 	Marco Antonio Garzon Palos
 * @version 1.0 (2025)
 */
public class Resistencia extends Componente {
	private final DoubleProperty ohmios=new SimpleDoubleProperty();
	
	/**
	 * Constructor sin argumentos para deserialización (pedido por libreria Jackson)
	 */
	public Resistencia() {
		super(0, 0, false);
		this.ohmios.set(0.0);
	}
	
	/**
	 * Constructor con solo valor de resistencia.
	 * Inicializa con nodos (0, 0) y carga=false.
	 * 
	 * @param r			Resistencia (Ohmios, Ω)
	 * @throws IllegalArgumentException si {@code r<=0}
	 */
	public Resistencia(double r) {
		super(0, 0, false);
		if(r<=0) throw new IllegalArgumentException("El valor de la resistencia debe ser positivo.");
		this.ohmios.set(r);
	}
	
	/**
	 * Constructor completo (con manejo de excepciones).
	 * 
	 * @param r 	resistencia (Ohmios, Ω)
	 * @param n1	nodo 1
	 * @param n2	nodo 2
	 * @param carga flag de carga (si es resistencia de carga, true, si no false)
	 * @throws IllegalArgumentException Si los dos nodos son iguales
	 * @throws IllegalArgumentException Si el valor de resistencia es menor o igual que cero
	 */
	public Resistencia(double r, int n1, int n2, boolean carga) {
		super(n1, n2, carga);
		if(n1==n2) throw new IllegalArgumentException("Los dos nodos de una resistencia no pueden ser el mismo.");
		if(r<=0) throw new IllegalArgumentException("El valor de la resistencia debe ser positivo.");
		this.ohmios.set(r);
	}
	
	/**
	 * Constructor normal (sin flag de carga).
	 * 
	 * @param r 	resistencia (Ohmios, Ω)
	 * @param n1	nodo 1
	 * @param n2 	nodo 2
	 */
	public Resistencia(double r, int n1, int n2) {
		this(r, n1, n2, false);
	}
	
	/**
	 * Constructor que acepta string con sufijo multiplicador completo (con flag de carga).
	 * 
	 * @param r		resistencia (Ohmios, Ω con sufijo)
	 * @param n1 	nodo 1
	 * @param n2 	nodo 2
	 * @param carga flag de carga
	 */
	public Resistencia(String r, int n1, int n2, boolean carga) {
		this(Unidades.parsear(r), n1, n2, carga);
	}
	
	/**
	 * Constructor que acepta string con sufijo multiplicador (sin flag de carga).
	 * 
	 * @param r 	resistencia (Ohmios, Ω con sufijo)
	 * @param n1 	nodo 1
	 * @param n2 	nodo 2
	 */
	public Resistencia(String r, int n1, int n2) {
		this(Unidades.parsear(r), n1, n2, false);
	}
	
	/**
	 * Obtiene el valor de la resistencia.
	 * 
	 * @return resistencia en Ohmios
	 */
	@Override
	@JsonProperty("valor")
	public double getValor() {
		return ohmios.get();
	}
	
	/**
	 * Asigna el valor de la resistencia.
	 * 
	 * @param r nuevo valor en Ohmios
	 * @throws IllegalArgumentException si {@code r<=0}
	 */
	@Override
	public void setValor(double r) {
		if(r<=0) {
			throw new IllegalArgumentException("El valor de la resistencia debe ser >0: "+r);
		}
		this.ohmios.set(r);
	}
	
	/**
	 * Propiedad observable de la resistencia.
	 * 
	 * @return DoubleProperty de resistencia (Ohmios).
	 */
	@JsonIgnore
	public DoubleProperty valorProperty() {
		return ohmios;
	}
	
	/**
	 * Devuelve el tipo de componente como String.
	 * 
	 * @return "Resistencia"
	 */
	@Override
	public String getTipo() {
		return "Resistencia";
	}
	
	/**
	 * Clona una resistencia de un circuito en otro.
	 * 
	 * @return nueva resistencia clonada
	 */
	@Override
	public Componente clonar() {
		return new Resistencia(this.getValor(), getNodo1(), getNodo2(), isCarga());
	}
	
	/**
	 * Clona una resistencia de un circuito en otro con nodos nuevos.
	 * 
	 * @return nueva resistencia clonada con nuevos nodos
	 */
	@Override
	public Componente clonarConNuevosNodos(int nuevoNodo1, int nuevoNodo2) {
		return new Resistencia(this.getValor(), nuevoNodo1, nuevoNodo2, isCarga());
	}
	
	/**
	 * Devuelve el prefijo "R" (Resistencia) para añadirlo al identificador único del componente.
	 */
	@Override
	public String getPrefijo() {
		return "R";
	}
	
	/**
	 * Describe una resistencia
	 * Complementa el método describir de la clase {@link Componente}
	 */
	@Override
	@JsonIgnore
	public String describir() {
		return String.format(
				"%s (%s)\nNodos: %d->%d\nResistencia: %s",
				getTipo(), getId(), getNodo1(), getNodo2(),
				FormatUtil.format(getValor(), Unidades.Type.RESISTENCIA));
	}
	
	/**
	 * Compara esta resistencia con otro objeto.
	 * 
	 * @param obj objeto a comparar
	 * @return {@code true} si son equivalentes, {@code false} en caso contrario
	 */
	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj)) return false;
		if(!(obj instanceof Resistencia)) return false;
		Resistencia otro=(Resistencia) obj;
		return Double.compare(getValor(), otro.getValor())==0;
	}
	
	/**
	 * Calcula el código hash.
	 * 
	 * @return código hash
	 */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getValor());
	}
}
