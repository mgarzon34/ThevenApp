package com.circuitos.analisiscircuitos.gui.util;

import java.net.URL;
import java.util.List;

/**
 * Clase utilitaria que agrupa las rutas a hojas de estilo CSS utilizadas en la interfaz gráfica.
 * Proporciona constantes para cada hoja de estilo.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public final class Styles {
	
	private static final String BASE_PATH="/com/circuitos/analisiscircuitos/gui/styles/";
	
	public static final String BIENVENIDA=			BASE_PATH+"PanelBienvenida.css";
	public static final String CABLE=				BASE_PATH+"cable.css";
	public static final String COMPONENTES=			BASE_PATH+"componentes.css";
	public static final String CONEXIONES=			BASE_PATH+"conexiones.css";
	public static final String LOGO=				BASE_PATH+"logoControl.css";
	public static final String DISENO=				BASE_PATH+"PanelDiseno.css";
	public static final String MENSAJESUI=			BASE_PATH+"mensajesUI.css";
	public static final String PROPIEDADES=			BASE_PATH+"PanelPropiedades.css";
	public static final String TABS=				BASE_PATH+"tabPane.css";
	public static final String OPSVISUALES=			BASE_PATH+"PanelOpcionesVisuales.css";
	public static final String ETIQUETAS=			BASE_PATH+"etiquetas.css";
	public static final String ANALISIS_OPCIONES=	BASE_PATH+"analisisOpciones.css";
	public static final String ANALISIS=			BASE_PATH+"PanelAnalisis.css";
	public static final String LOGS=				BASE_PATH+"logViewer.css";
	public static final String APRENDIZAJE=			BASE_PATH+"PanelAprendizaje.css";
	
	/**
	 * Constructor no instanciable.
	 */
	private Styles() { /* No instanciable */ }
	
	/**
	 * Lista de rutas internas a CSS.
	 */
	public static final List<String> ALL=List.of(
			BIENVENIDA, CABLE, COMPONENTES, CONEXIONES, DISENO,
			LOGO, MENSAJESUI, PROPIEDADES, TABS, OPSVISUALES,
			ETIQUETAS, ANALISIS_OPCIONES, ANALISIS, LOGS, APRENDIZAJE);
	
	/**
	 * Devuelve la URL externa de una hoja de estilo para cargarla.
	 * 
	 * @param resourcePath		Ruta interna del archivo CSS
	 * @return Cadena con la URL externa del recurso.
	 * @throws IllegalStateException si el archivo no se encuentra
	 */
	public static String getUrlOrThrow(String resourcePath) {
		URL url=Styles.class.getResource(resourcePath);
		if(url==null) {
			throw new IllegalStateException("No se encontró el archivo de estilo: "+resourcePath);
		}
		return url.toExternalForm();
	}
}
