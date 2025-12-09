package com.circuitos.analisiscircuitos.gui.controller;

import java.util.List;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

/**
 * Controlador del panel de propiedades del panel de análisis.
 * Es un panel informativo: muestra el resultado del análisis del circuito
 * equivalente (Thévenin o Norton), sin permitir edición.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class PanelPropiedadesAnalisisController {
	private static final Logger logger=Logger.getLogger(PanelPropiedadesAnalisisController.class.getName());
	
	@FXML private AnchorPane root;
	@FXML private Label titulo, labelFuenteEquivalente, labelResistenciaEquivalente;
	@FXML private TextArea areaCargas;
	@FXML private Label areaExplicacion;
	
	/**
	 * Inicialización del Panel de Propiedades de los componentes.
	 */
	@FXML
	public void initialize() {
		limpiarPanel();
	}
	
	/**
	 * Limpia el panel de propiedades.
	 */
	public void limpiarPanel() {
		if(titulo!=null) titulo.setText("Resumen de análisis");
		if(labelFuenteEquivalente!=null) labelFuenteEquivalente.setText("-");
		if(labelResistenciaEquivalente!=null) labelResistenciaEquivalente.setText("-");
		if(areaCargas!=null) {
			areaCargas.clear();
			areaCargas.setText("Ningún componente de carga conectado.");
		}
		if(areaExplicacion!=null) {
			areaExplicacion.setText("");
		}
	}
	
	/**
	 * Muestra el resultado del análisis de circuito equivalente (Thevenin o Norton) en 
	 * el panel de propiedades de la pestaña de Análisis. 
	 * 
	 * @param tituloAnalisis		Texto del título
	 * @param txFuente				Descripción de la fuente equivalente
	 * @param txRes					Descripción de la resistencia equivalente
	 * @param cargas				Lista de componentes de carga conectados
	 * @param resumen				Resumen del análisis realizado
	 */
	public void mostrarResultadoAnalisis(String tituloAnalisis, String txFuente, String txRes,
			List<String> cargas, String resumen) {
		logger.fine("Mostrando resultado de análisis en panel de propiedades");
		if(titulo!=null && tituloAnalisis!=null) titulo.setText(tituloAnalisis);
		if(labelFuenteEquivalente!=null) {
			labelFuenteEquivalente.setText(
					(txFuente!=null && !txFuente.isBlank()) ? txFuente : "-");
		}
		if(labelResistenciaEquivalente!=null) {
			labelResistenciaEquivalente.setText(
					(txRes!=null && !txRes.isBlank()) ? txRes : "-");
		}
		if(areaCargas!=null) {
			if(cargas==null || cargas.isEmpty()) {
				areaCargas.setText("Ningún componente de carga conectado.");
			} else {
				StringBuilder sb=new StringBuilder();
				for(String c : cargas) {
					sb.append(" - ").append(c).append("\n");
				}
				areaCargas.setText(sb.toString());
			}
		}
		if(areaExplicacion!=null) areaExplicacion.setText(resumen!=null ? resumen : "");
		mostrarPanel();
	}
	
	/**
	 * Muestra el panel de propiedades de la pestaña de Análisis.
	 */
	public void mostrarPanel() {
		toggle(root, true);
	}
	
	/**
	 * Oculta el panel de propiedades de la pestaña de Análisis.
	 */
	public void ocultarPanel() {
		toggle(root, false);
	}
	
	/**
	 * Muestra u oculta un nodo concreto.
	 * 
	 * @param node			Nodo a mostrar u ocultar
	 * @param visible		Valor que controla la visibilidad
	 */
	private void toggle(Node node, boolean visible) {
		if(node!=null) {
			node.setVisible(visible);
			node.setManaged(visible);
		}
	}
}