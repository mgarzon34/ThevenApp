package com.circuitos.analisiscircuitos.gui.controller;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.gui.util.AnimadorUI;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Controlador del panel lateral de componentes.
 * Permite la selección visual y el arrastre de componentes como resistencias,
 * fuentes o tierra desde la paleta hacia el área de diseño.
 * 
 * @author Marco Antonio Garzon Palos
 * @version 1.0
 */
public class PanelComponentesController {
	private static final Logger logger=Logger.getLogger(PanelComponentesController.class.getName());
	private static final String CSS_SELECTED="comp-seleccionado";
	
	@FXML private ScrollPane scrollPane;
	@FXML private StackPane stackResist, stackFti, stackFci, stackFtd, stackFcd, stackTierra;
	@FXML private ImageView iconResist, iconFti, iconFci, iconFtd, iconFcd, iconTierra;
	
	private StackPane componenteSeleccionado=null;
	
	/**
	 * Inicializa los iconos de los componentes y su comportamiento.
	 * Configura eventos de selección y arrastre de componentes.
	 * También limpia la selección si se hace click fuera de los componentes.
	 */
	@FXML
	public void initialize() {
		configurarComponente(stackResist, iconResist, "resistor.png");
		configurarComponente(stackFti, iconFti, "fti.png");
		configurarComponente(stackFci, iconFci, "fci.png");
		configurarComponente(stackFtd, iconFtd, "ftd.png");
		configurarComponente(stackFcd, iconFcd, "fcd.png");
		configurarComponente(stackTierra, iconTierra, "tierra.png");
		
		//Deseleccionar si hacemos click fuera del área seleccionada
		scrollPane.setOnMousePressed(e -> {
			if(!estaDentroComponente((Node) e.getTarget())) {
				if(componenteSeleccionado!=null) {
					componenteSeleccionado.getStyleClass().remove(CSS_SELECTED);
					componenteSeleccionado=null;
					logger.fine("Deseleccionado componente");
				}
			}
		});
	}
	
	/**
	 * Comprueba si un nodo forma parte de alguno de los contenedores de
	 * componentes disponibles en el panel (resistencia, fuentes, tierra).
	 * 
	 * @param n			Nodo a verificar
	 * @return {@code true} si pertenece a algún contenedor, {@code false} si no 
	 */
	private boolean estaDentroComponente(Node n) {
		while(n!=null) {
			if(n==stackResist || n==stackFti || n==stackFci ||
					n==stackFtd || n==stackFcd || n==stackTierra) {
				return true;
			}
			n=n.getParent();
		}
		return false;
	}
	
	/**
	 * Configura un componente visual para que sea seleccionable y arrastrable.
	 * 
	 * @param contenedor			StackPane contendor del icono
	 * @param icono					ImageView que representa el componente
	 * @param nombreImagen			nombre del archivo de imagen
	 */
	private void configurarComponente(StackPane contenedor, ImageView icono, String nombreImagen) {
		Objects.requireNonNull(contenedor, "Contenedor no puede ser null");
		Objects.requireNonNull(icono, "Icono no puede ser null");
		Objects.requireNonNull(nombreImagen, "nombreImagen no puede ser null");
		
		AnimadorUI.aplicarReboteMouse(contenedor, Color.WHITE);
		
		//Selección visual al hacer click
		contenedor.setOnMouseClicked((MouseEvent e) ->{
			if(componenteSeleccionado!=null) {
				componenteSeleccionado.getStyleClass().remove(CSS_SELECTED);
			}
			var css=contenedor.getStyleClass();
			if(!css.contains(CSS_SELECTED))
				css.add(CSS_SELECTED);
			componenteSeleccionado=contenedor;
			logger.log(Level.INFO, "Seleccionado componente: {0}", nombreImagen);
		}); 
		
		//Drag and drop
		contenedor.setOnDragDetected(e -> startDrag(e, icono, nombreImagen));
		contenedor.setPickOnBounds(true);
	}
	
	/**
	 * Inicia el arrastrado de componentes desde el panel de componentes al área de dibujo.
	 * 
	 * @param e					Evento de ratón
	 * @param icono				Icono que arrastramos
	 * @param nombreImagen		Nombre de la imagen
	 */
	private void startDrag(MouseEvent e, ImageView icono, String nombreImagen) {
		if(icono.getImage()==null) {
			logger.warning("No se pudo iniciar drag: imagen no cargada ("+nombreImagen+")");
			return;
		}
		var db=icono.startDragAndDrop(TransferMode.COPY);
		var content=new ClipboardContent();
		content.putImage(icono.getImage());
		content.putString(nombreImagen);
		db.setContent(content);
		if(componenteSeleccionado!=null) {
			componenteSeleccionado.getStyleClass().remove(CSS_SELECTED);
			componenteSeleccionado=null;
		}
		logger.log(Level.INFO, "Iniciado drag-drop: {0}", nombreImagen);
		e.consume();
	}
}
