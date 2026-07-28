package com.circuitos.analisiscircuitos.gui.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.gui.learning.database.LearningService;
import com.circuitos.analisiscircuitos.gui.learning.manager.AlumnoEjerciciosManager;
import com.circuitos.analisiscircuitos.gui.learning.manager.AuthSectionManager;
import com.circuitos.analisiscircuitos.gui.learning.manager.EjercicioSectionManager;
import com.circuitos.analisiscircuitos.gui.learning.manager.PdfFileManager;
import com.circuitos.analisiscircuitos.gui.learning.manager.ProgresoSectionManager;
import com.circuitos.analisiscircuitos.gui.learning.manager.TeoriaSectionManager;
import com.circuitos.analisiscircuitos.gui.learning.model.Ejercicio;
import com.circuitos.analisiscircuitos.gui.learning.model.EstadisticasProgreso;
import com.circuitos.analisiscircuitos.gui.learning.model.PdfDocument;
import com.circuitos.analisiscircuitos.gui.learning.model.Teoria;
import com.circuitos.analisiscircuitos.gui.learning.model.User;
import com.circuitos.analisiscircuitos.gui.util.UIHelper;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;

/**
 * Controlador del panel de aprendizaje - Sistema educativo.
 * Gestiona autenticación y registro de usuarios en modo estudiante y modo profesor.
 * Se integra con el sistema de análisis existente para verificar respuesta.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class PanelAprendizajeController {
	private static final Logger logger=Logger.getLogger(PanelAprendizajeController.class.getName());
	private LearningService service;
	private PanelDisenoController panelDisenoController;
	private final Map<SeccionID, Runnable> mapaNav=new HashMap<>();
	
	//Managers
	private AuthSectionManager authManager;
	private PdfFileManager pdfManager;
	private EjercicioSectionManager gestorEjProf;
	private TeoriaSectionManager gestorTeoria;
	private AlumnoEjerciciosManager gestorEjAl;
	private ProgresoSectionManager gestorProgreso;
	
	//Llamadas
	private Runnable onLogin, onLogout, reqDisenoTab, reqLearningTab;
	private boolean usuarioLogueado=false;
	
	//Panel Login/Registro
	@FXML private VBox loginPanel, registerFields;
	@FXML private ToggleGroup modeToggleGroup;
	@FXML private ToggleButton loginModeButton, registerModeButton;
	@FXML private TextField usernameField, nombreField, apellido1Field, apellido2Field, securityAnswerField;
	@FXML private PasswordField passwordField, confirmPasswordField;
	@FXML private ComboBox<String> roleComboBox, securityQuestionCombo;
	@FXML private Button authButton;
	@FXML private Label authStatusLabel;
	@FXML private Hyperlink forgotPasswordLink;
	
	//Panel princial E-Learning
	@FXML private VBox learningMainPanel;
	@FXML private Label welcomeLabel;
	@FXML private Button logoutButton;
	
	//Paneles de contenido
	@FXML private VBox welcomePanel, teoriaPanel, ejerciciosPanel, progresoPanel, pdfPanel, pdfGestionPanel;
	
	//Cabecera estadísticas (Estudiante)
	@FXML private HBox statsPanel;
	@FXML private ProgressBar generalProgressBar;
	@FXML private Label progressLabel, ejerciciosCompletadosLabel, puntuacionPromedioLabel;
	
	//Teoría (alumno y gestión)
	@FXML private HBox teoriaMenuContainer;
	@FXML private VBox gestionTeoriaPanel;
	@FXML private ListView<Teoria> listaGestionTeoria, listaTeoriaAlumnos;
	@FXML private TextField txtTituloTeoria, txtOrdenTeoria;
	@FXML private HTMLEditor editorTeoria;
	@FXML private WebView teoriaWebView;
	
	//Ejercicios (alumno y gestión)
	@FXML private VBox gestionEjerciciosPanel, detalleEjercicioPanel;
	@FXML private ListView<Ejercicio> listaGestionEjercicios;
	@FXML private TextField txtTituloEjercicio, txtSolVth, txtSolRth, txtSolIn, txtSolRn;
	@FXML private TextArea txtDescEjercicio;
	@FXML private ComboBox<Integer> comboDificultad;
	@FXML private ComboBox<String> comboTipoAnalisis, comboUnitVth, comboUnitRth, comboUnitIn, comboUnitRn;
	@FXML private Label lblEstadoCircuito, lblDetalleTitulo, lblDetalleDescripcion, lblDetalleDificultad, lblDetalleTipo;
	@FXML private ScrollPane scrollEjercicios;
	@FXML private FlowPane ejerciciosContainer;
	
	//Progreso (Profesor/Estudiante)
	@FXML private SplitPane progresoProfesorSplit;
	@FXML private VBox progresoAlumnoContainer;
	@FXML private ListView<User> listaAlumnosProfesor;
	@FXML private Label lblAlumnoSeleccionado, lblProfesorInfoEjercicios, lblProfesorInfoTeoria, lblAlumnoNota, lblProgresoDetalle;
	@FXML private TextField txtNotaProfesor;
	@FXML private TextArea txtMensajeProfesor, txtAlumnoMensajes;
	@FXML private ProgressBar barProgresoDetalle;
	
	//PDF
	@FXML private ListView<PdfDocument> pdfListView, gestionPdfListView;
	@FXML private Label tituloPdfLabel, uploadStatusLabel;
	@FXML private TextArea infoPdfArea, descripcionPdfField;
	@FXML private Button abrirPdfButton, uploadPdfButton, eliminarPdfButton;
	@FXML private TextField tituloPdfField;
	
	/**
	 * Enumerado con las secciones del bloque de E-Learning
	 */
	public enum SeccionID {
		TEORIA, EJERCICIOS, PDF, GESTION_PDF, PROGRESO,
		GESTION_TEORIA, GESTION_EJERCICIOS, INICIO
	}
	
	/**
	 * Inicializa el controlador del panel de aprendizaje.
	 */
	@FXML
	public void initialize() {
		try {
			service=new LearningService();
			
			if(loginPanel!=null) {
				authManager=new AuthSectionManager(service, this::iniciarSesionExito,
						loginPanel, modeToggleGroup, loginModeButton, registerModeButton,
						usernameField, passwordField, nombreField, apellido1Field, apellido2Field, 
						registerFields, roleComboBox, authButton, authStatusLabel,
						securityQuestionCombo, securityAnswerField);
				if(securityQuestionCombo!=null) {
					securityQuestionCombo.getItems().addAll(
							"¿Cuál fue el nombre de tu primera mascota?",
							"¿En qué ciudad naciste?",
							"¿Cuál es tu comida favorita?",
							"¿Cuál fue tu primer concierto?",
							"¿Cuál fue tu primer coche?");
					securityQuestionCombo.getSelectionModel().selectFirst();
				}
			}
			if(pdfListView!=null) {
				pdfManager=new PdfFileManager(service, pdfListView, gestionPdfListView, 
						tituloPdfLabel, infoPdfArea, abrirPdfButton, eliminarPdfButton, tituloPdfField,
						descripcionPdfField, uploadStatusLabel);
				UIHelper.configurarLista(pdfListView, p->"📄"+p.getTitulo());
				UIHelper.configurarLista(gestionPdfListView, PdfDocument::getTitulo);
			}
			if(gestionTeoriaPanel!=null || teoriaPanel!=null) {
				gestorTeoria=new TeoriaSectionManager(service, listaGestionTeoria, txtTituloTeoria, txtOrdenTeoria,
										editorTeoria, listaTeoriaAlumnos, teoriaWebView, this::actualizarEstadisticas);
			}
			if(gestionEjerciciosPanel!=null) {
				gestorEjProf=new EjercicioSectionManager(service, listaGestionEjercicios,
										txtTituloEjercicio, txtDescEjercicio, comboDificultad, comboTipoAnalisis,
										lblEstadoCircuito, txtSolVth, comboUnitVth, txtSolRth, comboUnitRth, txtSolIn,
										comboUnitIn, txtSolRn, comboUnitRn);
			}
			if(ejerciciosPanel!=null) {
				gestorEjAl=new AlumnoEjerciciosManager(service, ejerciciosContainer, scrollEjercicios, detalleEjercicioPanel, 
											lblDetalleTitulo, lblDetalleDescripcion, lblDetalleDificultad, lblDetalleTipo, 
											() ->{ if(reqDisenoTab!=null) reqDisenoTab.run(); },
											()->{ 
												if(reqLearningTab!=null) reqLearningTab.run();
												actualizarEstadisticas();
											});
				if(panelDisenoController!=null) gestorEjAl.setPanelDisenoController(panelDisenoController);
			}
			if(progresoPanel!=null) {
				gestorProgreso=new ProgresoSectionManager(service, progresoProfesorSplit, progresoAlumnoContainer,
											listaAlumnosProfesor, lblAlumnoSeleccionado, lblProfesorInfoEjercicios, lblProfesorInfoTeoria,
											txtNotaProfesor, txtMensajeProfesor, lblAlumnoNota, lblProgresoDetalle, barProgresoDetalle, txtAlumnoMensajes);
			}
			inicializarNavegacion();
			logger.info("PanelAprendizajeController inicializado correctamente");	
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Error crítico inicializando PanelAprendizajeController", e);
		}
	}
	
	/**
	 * Acción del botón de autenticación (login/registro).
	 */
	@FXML
	private void onAuthAction() {
		if(authManager!=null) {
			authManager.procesarAccionAuth();
		}
	}
	
	/**
	 * Cierra la sesión del usuario.
	 */
	@FXML
	private void onLogout() {
		usuarioLogueado=false;
		if(authManager!=null) authManager.logout();
		if(learningMainPanel!=null) {
			learningMainPanel.setVisible(false);
			learningMainPanel.setManaged(false);
		}
		if(onLogout!=null) onLogout.run();
	}
	
	/**
	 * Gestiona la recuperación de la contraseña a través de la pregunta de seguridad configurada
	 * por el usuario.
	 */
	@FXML
	private void onOlvidarPassword() {
		TextInputDialog dialog=new TextInputDialog();
		dialog.setTitle("Recuperar Contraseña");
		dialog.setContentText("Introduce tu nombre de usuario:");
		Optional<String> result=dialog.showAndWait();
		if(result.isEmpty()) return;
		String username=result.get();
		User usuario=service.buscarUsuario(username);
		if(usuario==null || usuario.getPreguntaSeguridad()==null) {
			UIHelper.mostrarError("Usuario no encontrado o sin pregunta de seguridad configurada.");
			return;
		}
		TextInputDialog dialogSec=new TextInputDialog();
		dialogSec.setTitle("Recuperar Contraseña");
		dialogSec.setContentText(usuario.getPreguntaSeguridad()+"\n\nTu respuesta:");
		Optional<String> respuesta=dialogSec.showAndWait();
		if(respuesta.isEmpty()) return;
		if(!usuario.verificarRespuestaSeguridad(respuesta.get())) {
			UIHelper.mostrarError("La respuesta es incorrecta.");
		}
		mostrarDialogoNuevaPassword().ifPresent(newPass -> {
			boolean cambiado=service.cambiarPassword(username, newPass);
			if(cambiado) {
				UIHelper.mostrarInfo("Contraseña cambiada.");
			} else {
				UIHelper.mostrarError("No se ha podido cambiar la contraseña.");
			}
		});
	}
	
	/**
	 * Muestra un diálogo personalizado con dos campos de contraseña para validar que 
	 * ambos coincidan. Se usa en cambio de contraseña tras recuperación.
	 * 
	 * @return Optional con la nueva contraseña si se acepta o vacío si se cancela
	 */
	private Optional<String> mostrarDialogoNuevaPassword() {
		Dialog<String> dialog=new Dialog<>();
		dialog.setTitle("Restablecer Contraseña");
		dialog.setHeaderText("Introduce una contraseña nueva");
		ButtonType okButtonType=new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
		PasswordField pass1=new PasswordField();
		pass1.setPromptText("Nueva contraseña");
		PasswordField pass2=new PasswordField();
		pass2.setPromptText("Repite la contraseña");
		VBox contenido=new VBox(10);
		contenido.getChildren().addAll(pass1, pass2);
		dialog.getDialogPane().setContent(contenido);
		Node okButton=dialog.getDialogPane().lookupButton(okButtonType);
		okButton.setDisable(true);
		pass2.textProperty().addListener((obs, oldVal, newVal) -> {				//Valida mientras escribe
			okButton.setDisable(!pass1.getText().equals(pass2.getText()) || pass1.getText().isEmpty());
		});
		pass1.textProperty().addListener((obs, oldVal, newVal) -> {				//Valida si cambia el primero después de escribir el segundo
			okButton.setDisable(!pass1.getText().equals(pass2.getText()) || pass1.getText().isEmpty());
		});
		dialog.setResultConverter(dialogButton -> {
			if(dialogButton==okButtonType) {
				return pass1.getText();
			}
			return null;
		});
		return dialog.showAndWait();
	}
	
	/**
	 * Métodos de gestión de teoría.
	 * Crea un nuevo tema de teoría, guarda la teoría o elimina un tema de teoría.
	 */
	@FXML private void onNuevoTema() {
		if(gestorTeoria!=null) gestorTeoria.nuevo();
	}
	@FXML private void onGuardarTeoria() {
		if(gestorTeoria!=null) gestorTeoria.guardar();
	}
	@FXML private void onEliminarTema() {
		if(gestorTeoria!=null) gestorTeoria.eliminar();
	}
	
	/**
	 * Métodos de gestión de ejercicios.
	 * Incluye la creación de nuevo ejercicio, guardar, eliminar o importar un circuito del area de 
	 * diseño para incluirlo en un ejercicio.
	 */
	@FXML private void onNuevoEjercicio() {
		if(gestorEjProf!=null) gestorEjProf.nuevo();
	}
	@FXML private void onGuardarEjercicio() {
		if(gestorEjProf!=null) gestorEjProf.guardar();
	}
	@FXML private void onEliminarEjercicio() {
		if(gestorEjProf!=null) gestorEjProf.eliminar();
	}
	@FXML private void onImportarCircuito() {
		if(gestorEjProf!=null) gestorEjProf.importarCircuito();
	}

	/**
	 * Métodos de visualización y resolución de ejercicios, normalmente para la vista de estudiante.
	 * Incluye volver a la lista de ejercicios o comenzar la resolución de uno en concreto.
	 */
	@FXML private void onVolverListaEjercicios() {
		if(gestorEjAl!=null) gestorEjAl.volverLista();
	}
	@FXML private void onComenzarResolucion() {
		if(gestorEjAl!=null) gestorEjAl.comenzarResolucion();
	}
	
	/**
	 * Métodos para gestionar el progreso de los estudiantes (usado por el profesor).
	 * Incluye guardar una calificación para un alumno o un comentario que quiera hacer el profesor y
	 * resetear el progreso de los alumnos.
	 */
	@FXML private void onGuardarNota() {
		if(gestorProgreso!=null) gestorProgreso.guardarNota();
	}
	@FXML private void onGuardarComentario() {
		if(gestorProgreso!=null) gestorProgreso.guardarComentario();
	}
	@FXML private void onResetearProgreso() {
		if(gestorProgreso!=null) gestorProgreso.resetearProgreso();
	}
	
	/**
	 * Métodos para gestionar documentos PDF. El profesor puede subirlos o eliminarlos y el estudiante
	 * puede visualizarlos.
	 * Incluye subir un PDF, eliminarlo o abrirlo en un visor de PDF externo.
	 */
	@FXML private void onSubirPdf() {
		if(pdfManager!=null) pdfManager.subirPdf(null);
	}
	@FXML private void onEliminarPdf() {
		if(pdfManager!=null) pdfManager.eliminarPdf();
	}
	@FXML private void abrirPdfVisorExterno() {
		if(pdfManager!=null) pdfManager.abrirPdf();
	}
	
	/**
	 * Inicia la sesión de un usuario siempre que se haya logueado con éxito
	 * mostrando los paneles de usuario.
	 * Llama a {@link #cargarInterfazUsuario()}
	 */
	private void iniciarSesionExito() {
		usuarioLogueado=true;
		authManager.ocultarPanel();
		learningMainPanel.setVisible(true);
		learningMainPanel.setManaged(true);	
		cargarInterfazUsuario();
		if(onLogin!=null) onLogin.run();
	}
	
	/**
	 * Carga la interfaz del usuario una vez logueado.
	 * Se llama desde {@link #iniciarSesionExito()}.
	 */
	private void cargarInterfazUsuario() {
		User userActual=service.getUsuarioActual();
		if(userActual==null) return;
		if(welcomeLabel!=null) {
			String nombre=userActual.getName()+" "+(userActual.getApellido1()!=null ? userActual.getApellido1() : "");
			welcomeLabel.setText("Bienvenido, "+nombre);
		}
		if(statsPanel!=null) {
			boolean esEstudiante=userActual.esEstudiante();
			statsPanel.setVisible(userActual.esEstudiante());
			statsPanel.setManaged(userActual.esEstudiante());
			if(esEstudiante) actualizarEstadisticas();
		}
		mostrarPanel(welcomePanel);
		if(gestorTeoria!=null) gestorTeoria.cargarListaAlumno();
		if(gestorEjAl!=null) gestorEjAl.cargarEjercicios();
		if(pdfManager!=null) pdfManager.cargarDocumentosPdf();
	}
	
	/**
	 * Actualiza las estadísticas del estudiante con la calificación, ejercicios resueltos, etc.
	 */
	private void actualizarEstadisticas() {
		if(generalProgressBar==null) return;
		EstadisticasProgreso stats=service.getEstadisticasProgreso();
		generalProgressBar.setProgress(stats.getProgresoTotal());
		progressLabel.setText(String.format("%.0f%% Completado", stats.getProgresoTotal()*100));
		if(ejerciciosCompletadosLabel!=null) ejerciciosCompletadosLabel.setText(stats.getEjerciciosCompletados()+"/"+stats.getTotalEjercicios());
		puntuacionPromedioLabel.setText(String.format("%.2f/10", stats.getPuntuacionPromedio()));
	}
	
	/**
	 * Inicializa la navegación por los paneles del módulo de E-Learning.
	 */
	private void inicializarNavegacion() {
		mapaNav.put(SeccionID.TEORIA, () -> mostrarPanel(teoriaPanel));				//Panel de teoría
		mapaNav.put(SeccionID.EJERCICIOS, () -> {									//Panel de ejercicios
			mostrarPanel(ejerciciosPanel);
			if(gestorEjAl!=null) gestorEjAl.cargarEjercicios();
		});
		mapaNav.put(SeccionID.PDF, () -> mostrarPanel(pdfPanel));					//Panel de PDF
		mapaNav.put(SeccionID.GESTION_PDF, () -> mostrarPanel(pdfGestionPanel));	//Panel de gestión de PDF
		mapaNav.put(SeccionID.PROGRESO, () -> {										//Panel de progreso
			mostrarPanel(progresoPanel);
			if(gestorProgreso!=null) gestorProgreso.actualizarVista();
		});
		mapaNav.put(SeccionID.GESTION_TEORIA, () -> {								//Panel de gestión de teoría
			mostrarPanel(gestionTeoriaPanel);
			if(gestorTeoria!=null) gestorTeoria.cargarListaGestion();
		});
		mapaNav.put(SeccionID.GESTION_EJERCICIOS, () -> {							//Panel de gestión de ejercicios
			mostrarPanel(gestionEjerciciosPanel);
			if(gestorEjProf!=null) gestorEjProf.cargarLista();
		});
		mapaNav.put(SeccionID.INICIO, () -> mostrarPanel(welcomePanel));			//Panel de inicio
	}
	/**
	 * Muestra los paneles necesarios para cada sección.
	 * 
	 * @param seccion		Sección a la que va el usuario
	 */
	public void navegarA(SeccionID seccion) {
		mapaNav.getOrDefault(seccion, mapaNav.get(SeccionID.INICIO)).run();
	}
	
	/**
	 * Muestra un panel en concreto y permite interactuar con él.
	 * 
	 * @param panel			Panel que se muestra
	 */
	private void mostrarPanel(VBox panel) {
		//Ocultar todos
		VBox[] paneles= {welcomePanel, teoriaPanel, ejerciciosPanel, progresoPanel,
				pdfPanel, pdfGestionPanel, gestionTeoriaPanel, gestionEjerciciosPanel };
		for(VBox p : paneles) {
			if(p!=null) {
				p.setVisible(false);
				p.setManaged(false);
			}
		}
		if(panel!=null) {
			panel.setVisible(true);
			panel.setManaged(true);
		} else {
			logger.warning("Se intentó mostrar un panel nulo en navegación.");
		}
	}
	
	/**
	 * Ejecutan la acción según petición del usuario (listeners).
	 * Incluyen petición de login, logout, cambio a panel de diseño o a panel de E-Learning.
	 * 
	 * @param r		Objeto que ejecuta la acción
	 */
	public void setOnLoginListener(Runnable r) {
		this.onLogin=r;
	}
	public void setOnLogoutListener(Runnable r) {
		this.onLogout=r;
	}
	public void setOnRequestDisenoTab(Runnable r) {
		this.reqDisenoTab=r;
	}
	public void setOnRequestLearningTab(Runnable r) {
		this.reqLearningTab=r;
	}
	
	/**
	 * Establece el controlador del panel de diseño para realizar operaciones de gestión.
	 * 
	 * @param c		Objeto {@link PanelDisenoController}
	 */
	public void setPanelDisenoController(PanelDisenoController c) {
		if(gestorEjProf!=null) gestorEjProf.setPanelDisenoController(c);
		if(gestorEjAl!=null) gestorEjAl.setPanelDisenoController(c);
	}
	
	/**
	 * Comprueba si un usuario se ha logueado en el sistema.
	 * 
	 * @return {@code true} si se ha logueado, {@code false} si no
	 */
	public boolean isUsuarioLogueado() {
		return usuarioLogueado;
	}
	
	/**
	 * Obtiene el usuario actual.
	 * 
	 * @return usuario actual
	 */
	public User getUsuarioActual() {
		return service.getUsuarioActual();
	}
}