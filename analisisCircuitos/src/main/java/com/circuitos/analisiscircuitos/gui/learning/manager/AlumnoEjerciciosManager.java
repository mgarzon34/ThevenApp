package com.circuitos.analisiscircuitos.gui.learning.manager;

import java.util.List;

import com.circuitos.analisiscircuitos.gui.controller.PanelDisenoController;
import com.circuitos.analisiscircuitos.gui.learning.database.LearningService;
import com.circuitos.analisiscircuitos.gui.learning.model.Ejercicio;
import com.circuitos.analisiscircuitos.gui.learning.model.LearningFactory;
import com.circuitos.analisiscircuitos.gui.learning.view.VentanaResolucion;
import com.circuitos.analisiscircuitos.gui.util.UIHelper;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Clase que gestiona la vista de ejercicios del alumno dentro del módulo de E-Learning.
 * 
 * @author Marco Anotnio Garzón Palos
 * @version 1.0
 */
public class AlumnoEjerciciosManager {

	private final LearningService service;
	private PanelDisenoController disenoController;
	
	//Referencias UI
	private final FlowPane container;
	private final ScrollPane scrollLista;
	private final VBox panelDetalle;
	private final Label lblTitulo, lblDesc, lblDiff, lblTipo;
	
	//Llamadas
	private final Runnable onRequestDisenoTab;
	private final Runnable onRequestLearningTab;
	
	private Ejercicio ejSel=null; 
	
	/**
	 * Constructor del gestor.
	 * 
	 * @param service					Servicio de aprendizaje ({@link LearningService})
	 * @param container					Contenedor de tarjetas de ejercicios
	 * @param scrollLista				ScrollPane de la lista de ejercicios
	 * @param panelDetalle				Panel que muestra el detalle de un ejercicio
	 * @param lblTitulo					Etiqueta del título del ejercicio
	 * @param lblDesc					Etiqueta de la descripción
	 * @param lblDiff					Etiqueta de dificultad
	 * @param lblTipo					Etiqueta de tipo de análisis
	 * @param onRequestDisenoTab		Acción para cambiar a pestaña de diseño
	 * @param onRequestLearningTab		Acción para cambiar a pestaña de E-Learning
	 */
	public AlumnoEjerciciosManager(LearningService service, FlowPane container, ScrollPane scrollLista,
									VBox panelDetalle, Label lblTitulo, Label lblDesc, Label lblDiff,
									Label lblTipo, Runnable onRequestDisenoTab, Runnable onRequestLearningTab) {
		this.service=service;
		this.container=container;
		this.scrollLista=scrollLista;
		this.panelDetalle=panelDetalle;
		this.lblTitulo=lblTitulo;
		this.lblDesc=lblDesc;
		this.lblDiff=lblDiff;
		this.lblTipo=lblTipo;
		this.onRequestDisenoTab=onRequestDisenoTab;
		this.onRequestLearningTab=onRequestLearningTab;
	}
	
	/**
	 * Establece el controlador del panel de diseño para cargar los ejercicios en él.
	 * 
	 * @param c			Controlador del panel de diseño
	 */
	public void setPanelDisenoController(PanelDisenoController c) {
		this.disenoController=c;
	}
	
	/**
	 * Carga la lista de ejercicios en lista visual y marca los ya completados.
	 */
	public void cargarEjercicios() {
		volverLista();				//Asegura estado inicial
		if(container==null) return;
		container.getChildren().clear();
		List<Ejercicio> lista=service.getEjercicios();
		List<Integer> completados=service.getEjerciciosCompletadosIds();
		if(lista.isEmpty()) {
			Label vacio=new Label("No hay ejercicios disponibles.");
			vacio.setStyle("-fx-font-size: 16; -fx-text-fill: #777; -fx-padding: 20;");
			container.getChildren().add(vacio);
		} else {
			for(Ejercicio ej : lista) {
				var tarjeta=LearningFactory.crearTarjetaEjercicio(ej, () -> mostrarDetalle(ej));
				if(completados.contains(ej.getId())) {
					tarjeta.getStyleClass().add("ejercicio-card-completado");
					tarjeta.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2; -fx-background-color: #e8f8f5;");
				}
				container.getChildren().add(tarjeta);
			}
		}
	}
	
	/**
	 * Muestra la información detallada del ejercicio seleccionado.
	 * 
	 * @param ej				Ejercicio que se quiere mostrar
	 */
	private void mostrarDetalle(Ejercicio ej) {
		this.ejSel=ej;
		lblTitulo.setText(ej.getTitulo());
		String descripcionTexto=ej.getDescripcion();
		if(ej.tieneNodosManuales()) {
			descripcionTexto+="\n\n ⚠️ IMPORTANTE️:\n"+
					"Este ejercicio no tiene componentes de carga. Debes calcular el equivalente "+
					"desde los nodos:\n"+
					"👉 Nodo A: "+ej.getNodoAnalisisA()+"\n"+
					"👉 Nodo B: "+ej.getNodoAnalisisB();
		}
		lblDesc.setText(descripcionTexto);
		lblDiff.setText("Dificultad: "+"⭐".repeat(ej.getDificultad()));
		lblTipo.setText("Tipo de Análisis: "+ej.getTipoAnalisis());
		scrollLista.setVisible(false);
		scrollLista.setManaged(false);
		panelDetalle.setVisible(true);
		panelDetalle.setManaged(true);
	}
	
	/**
	 * Vuelve a la lista de ejercicios (oculta el panel de detalle).
	 */
	public void volverLista() {
		panelDetalle.setVisible(false);
		panelDetalle.setManaged(false);
		scrollLista.setVisible(true);
		scrollLista.setManaged(true);
		ejSel=null;
	}
	
	/**
	 * Inicia la resolución del ejercicio seleccionado cargando el circuito en el panel 
	 * de diseño y abre la ventana de resolución.
	 */
	public void comenzarResolucion() {
		if(ejSel==null) return;
		if(disenoController!=null) {
			disenoController.cargarCircuitoDesdeJson(ejSel.getDatosCircuito());
		} else {
			UIHelper.mostrarError("Error interno: Controlador de diseño no conectado.");
			return;
		}
		if(onRequestDisenoTab!=null) onRequestDisenoTab.run();
		VentanaResolucion ventana=new VentanaResolucion(ejSel, 
				() -> {
					if(onRequestLearningTab!=null) onRequestLearningTab.run();
					volverLista();
					cargarEjercicios();
				},
				() -> {
					service.marcarEjercicioCompletado(ejSel.getId());
				});
		ventana.mostrar();
	}
}