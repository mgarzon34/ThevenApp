package com.circuitos.analisiscircuitos.dominio;

import java.util.Objects;

import com.circuitos.analisiscircuitos.dominio.util.FormatUtil;
import com.circuitos.analisiscircuitos.dominio.util.Unidades;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Clase que extiende la clase abstracta componente para definir
 * una fuente de corriente independiente (controlada por tensión o corriente).
 *  
 * @author 	Marco Antonio Garzon Palos
 * @version 1.0 (2025)
 */
public class FuenteCorrienteInd extends Componente {
	private final DoubleProperty corriente=new SimpleDoubleProperty();
	
	/**
	 * Constructor sin argumentos para deserialización (pedido por libreria Jackson)
	 */
	public FuenteCorrienteInd() {
		super(0, 0, false);
		this.corriente.set(0.0);
	}
	
	/**
	 * Constructor completo (con flag de carga).
	 * 
	 * @param corriente 	corriente de la fuente (amperios)
	 * @param nodoNeg		nodo negativo de la fuente
	 * @param nodoPos		nodo positivo de la fuente
	 * @param carga			flag de carga
	 */
	public FuenteCorrienteInd(double corriente, int nodoNeg, int nodoPos, boolean carga) {
		super(nodoNeg, nodoPos, carga);
		this.corriente.set(corriente);
	}
	
	/**
	 * Constructor (sin flag de carga).
	 * 
	 * @param corriente		corriente de la fuente (amperios)
	 * @param nodoNeg		nodo negativo de la fuente
	 * @param nodoPos		nodo positivo de la fuente
	 */
	public FuenteCorrienteInd(double corriente, int nodoNeg, int nodoPos) {
		this(corriente, nodoNeg, nodoPos, false);
	}
	
	/**
	 * Constructor que acepta string con sufijo multiplicador (con flag de carga).
	 * 
	 * @param corrienteUnidad	corriente de la fuente (amperios, con sufijo)
	 * @param nodoNeg			nodo negativo de la fuente
	 * @param nodoPos			nodo positivo de la fuente
	 * @param carga				flag de carga
	 */
	public FuenteCorrienteInd(String corrienteUnidad, int nodoNeg, int nodoPos, boolean carga) {
		this(Unidades.parsear(corrienteUnidad), nodoNeg, nodoPos, carga);
	}
	
	/**
	 * Constructor que acepta string con sufijo multiplicador (sin flag de carga).
	 * 
	 * @param corrienteUnidad	corriente de la fuente (amperios, con sufijo)
	 * @param nodoNeg			nodo negativo de la fuente
	 * @param nodoPos			nodo positivo de la fuente
	 */
	public FuenteCorrienteInd(String corrienteUnidad, int nodoNeg, int nodoPos) {
		this(Unidades.parsear(corrienteUnidad), nodoNeg, nodoPos);
	}
	
	/**
	 * Devuelve el valor de la corriente
	 * 
	 * @return valor de la corriente
	 */
	@Override
	@JsonProperty("valor")
	public double getValor() {
		return corriente.get();
	}
	
	/**
	 * Modifica el valor de la corriente.
	 */
	@Override
	public void setValor(double corriente) {
		this.corriente.set(corriente);
	}
	
	/**
	 * Propiedad observable de la corriente.
	 * 
	 * @return DoubleProperty de la corriente
	 */
	public DoubleProperty valorProperty() {
		return corriente;
	}
	
	/**
	 * Clona una fuente de corriente independiente de un circuito a otro.
	 * 
	 * @return nueva fuente de corriente independiente clonada
	 */
	@Override
	public Componente clonar() {
		return new FuenteCorrienteInd(getValor(), getNodo1(), getNodo2(), isCarga());
	}
	
	/**
	 * Obtiene el tipo de componente mediante String.
	 * 
	 * @return "Fuente Corriente Independiente"
	 */
	@Override
	public String getTipo() {
		return "Fuente Corriente Independiente";
	}
	 
	/**
	 * Devuelve el prefijo "I" (Intensidad) para añadirlo al identificador único del componente.
	 */
	@Override
	public String getPrefijo() {
		return "I";
	}
	
	/**
	 * Describe una fuente de corriente independiente.
	 * Complementa el método describir de la clase {@link Componente}
	 */
	@Override
	@JsonIgnore
	public String describir() {
		return String.format(
				"%s (%s)\nNodos: %d->%d\nCorriente: %s",
				getTipo(), getId(), getNodo1(), getNodo2(),
				FormatUtil.format(getValor(), Unidades.Type.CORRIENTE));
	}
	
	/**
	 * Compara esta fuente con otro objeto.
	 * 
	 * @param obj objeto a comparar
	 * @return {@code true} si son iguales, {@code false} en caso contrario
	 */
	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj)) return false;
		if(!(obj instanceof FuenteCorrienteInd)) return false;
		FuenteCorrienteInd otro=(FuenteCorrienteInd) obj;
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