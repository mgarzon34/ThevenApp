package com.circuitos.analisiscircuitos.dominio;

import java.util.Objects;

import com.circuitos.analisiscircuitos.dominio.util.GestorIds;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Clase abstracta que representa un componente genérico de un circuito eléctrico.
 * <p>
 * Preparada para serializar/deserializar con Jackson usando @type.
 * </p>
 * Las subclases deben implementar sus métodos.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
@JsonTypeInfo(
	use=JsonTypeInfo.Id.NAME,
	include=JsonTypeInfo.As.PROPERTY,
	property="@type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value=Resistencia.class, name="Resistencia"),
	@JsonSubTypes.Type(value=FuenteTensionInd.class, name="FuenteTensionIndependiente"),
	@JsonSubTypes.Type(value=FuenteCorrienteInd.class, name="FuenteCorrienteIndependiente"),
	@JsonSubTypes.Type(value=FuenteTensionDependiente.class, name="FuenteTensionDependiente"),
	@JsonSubTypes.Type(value=FuenteCorrienteDependiente.class, name="FuenteCorrienteDependiente"),
	@JsonSubTypes.Type(value=Tierra.class, name="Tierra")
})

@JsonIgnoreProperties({ "tipo" })
public abstract class Componente {
	private int nodo1;
	private int nodo2;
	private final String id;
	private final BooleanProperty carga=new SimpleBooleanProperty();
	private final DoubleProperty valor=new SimpleDoubleProperty();

	/**
	 * Constructor. Crea un componente con dos nodos y le pone la etiqueta "de carga"
	 * 
	 * @param nodo1		nodo al que se conecta el componente
	 * @param nodo2		nodo al que se conecta el componente
	 * @param carga		flag de carga (true si es componente de carga, sino false)
	 * 
	 */
	protected Componente(int nodo1, int nodo2, boolean ch) {
		this.nodo1=nodo1;
		this.nodo2=nodo2;
		carga.set(ch);
		this.id=GestorIds.getInstance().generarId(this);
	}
	
	/**
	 * Constructor. Crea un componente solo con dos nodos (la etiqueta de carga es false).
	 * 
	 * @param nodo1		nodo al que se conecta el componente
	 * @param nodo2		nodo al que se conecta el componente
	 */
	protected Componente(int nodo1, int nodo2) {
		this(nodo1, nodo2, false);
	}
	
	/**
	 * Constructor. Crea un componente de un solo nodo (para definir Tierra).
	 * @param nodo1	Nodo de tierra.
	 */
	protected Componente(int nodo1) {
		carga.set(false);
		this.nodo1=nodo1;
		this.nodo2=nodo1;
		this.carga.set(false);
		this.id="GND";
	}
	
	/**
	 * Obtiene el nodo 1 del componente.
	 * 
	 * @return nodo1
	 */
	@JsonProperty("nodo1")
	public int getNodo1() {
		return nodo1;
	}
	
	/**
	 * Obtiene el nodo 2 del componente.
	 * 
	 * @return nodo2
	 */
	@JsonProperty("nodo2")
	public int getNodo2() {
		return nodo2;
	}
	
	/**
	 * Modifica el nodo 1 del componente.
	 * 
	 * @param nodo1
	 */
	public void setNodo1(int nodo1) {
		this.nodo1=nodo1;
	}
	
	/**
	 * Modifica el nodo 2 del componente.
	 * 
	 * @param nodo2
	 */
	public void setNodo2(int nodo2) {
		this.nodo2=nodo2;
	}
	
	/**
	 * Comprueba si un componente es de carga o no.
	 * 
	 * @return true or false (si es de carga o no)
	 */
	@JsonProperty("carga")
	public final boolean isCarga() {
		return carga.get();
	}
	
	/**
	 * Modifica el estado de "carga" del componente.
	 * 
	 * @param carga
	 */
	public void setCarga(boolean ch) {
		this.carga.set(ch);
	}
	
	/**
	 * Obtiene la propiedad observable sobre si es un componente de carga.
	 * Permite enlazar el estado de carga en la interfaz JavaFX.
	 * 
	 * @return propiedad observable de carga (valor booleano)
	 */
	@JsonIgnore
	public BooleanProperty cargaProperty() {
		return carga;
	}
	
	/**
	 * Obtiene el Id único del componente.
	 * 
	 * @return Id asignado
	 */
	@JsonProperty("id")
	public final String getId() {
		return id;
	}
	
	/**
	 * Libera el Id del componente asociado.
	 * Para ello llama a {@link GestorIds}.
	 */
	public final void liberarId() {
		GestorIds.getInstance().liberarId(this);
	}
	
	/**
	 * Devuelve el prefijo que se usa para generar el Id único del componente.
	 * 
	 * @return Prefijo del componente
	 */
	@JsonIgnore
	public abstract String getPrefijo();
	
	/**
	 * Obtiene el tipo de componente.
	 * 
	 * @return tipo de componente
	 */
	@JsonIgnore
	public abstract String getTipo();
	
	/**
	 * Clona componentes de un circuito a otro.
	 * @return componente clonado
	 */
	public abstract Componente clonar();
	
	/**
	 * Clona componentes de un circuito a otro pero con otros nodos.
	 * @param nuevoNodo1
	 * @param nuevoNodo2
	 * @return componente clonado en otros nodos
	 */
	public Componente clonarConNuevosNodos(int nuevoNodo1, int nuevoNodo2) {
		Componente copia=clonar();
		copia.setNodo1(nuevoNodo1);
		copia.setNodo2(nuevoNodo2);
		return copia;
	}
	
	/**
	 * Obtiene el valor del componente.
	 * @return valor de componente.
	 */
	@JsonProperty("valor")
	public double getValor() {
		return valor.get();
	}

	/**
	 * Método que permite modificar el valor de un componente según el tipo. 
	 * Se completa en las clases que lo hereden.
	 * 
	 * @param nuevoValor			Valor nuevo que se modifica
	 */
	public void setValor(double nuevoValor) {
		valor.set(nuevoValor);
	}
	
	/**
	 * Obtiene la propiedad observable sobre el valor del componente.
	 * Permite enlazar el valor del componente en la interfaz JavaFX.
	 * 
	 * @return propiedad observable de valor del componente (tipo double)
	 */
	@JsonIgnore
	public DoubleProperty valorProperty() {
		return valor;
	}
	
	/**
	 * Método para devolver el componente en cadena String.
	 */
	@Override
	public String toString() {
		return describir();
	}
	
	/**
	 * Descripción en formato String del componente.
	 * 
	 * @return cadena que describe el componente
	 */
	@JsonIgnore
	public abstract String describir();
	
	/**
	 * Determina si dos instancias de "Componente" son iguales comparando sus valores.
	 * Se consideran iguales si son la misma instancia o tienen todos los parámetros iguales.
	 * 
	 * @return true o false (si son iguales o no)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) return true;
		if(!(obj instanceof Componente otro)) return false;
		return nodo1==otro.nodo1
				&& nodo2==otro.nodo2
				&& carga==otro.carga
				&& Objects.equals(getTipo(),  otro.getTipo())
				&& Double.compare(getValor(),  otro.getValor())==0;
	}
	
	/**
	 * Calcula el código hash de un componente. Dos instancias iguales tendrán el mismo hash.
	 * 
	 * @return hash 	valor de hash del componente
	 */
	@Override
	public int hashCode() {
		return Objects.hash(nodo1, nodo2, carga, getTipo(), getValor());
	}
}
