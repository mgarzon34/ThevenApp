package com.circuitos.analisiscircuitos.gui.learning.manager;

import com.circuitos.analisiscircuitos.gui.learning.database.LearningService;
import com.circuitos.analisiscircuitos.gui.learning.model.Teoria;
import com.circuitos.analisiscircuitos.gui.util.UIHelper;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;

/**
 * Clase que gestiona el contenido teórico del módulo de E-Learning.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class TeoriaSectionManager {
	
	private final LearningService service;
	
	//UI Profesor (Gestión)
	private final ListView<Teoria> listaGestion;
	private final TextField txtTitulo, txtOrden;
	private final HTMLEditor editor;
	
	//UI Estudiante (Visualización)
	private final ListView<Teoria> listaAlumno;
	private final WebView webView;
	private final Runnable onTeoriaLeida; 
	
	private Teoria teoriaSel=null;	//Selección del profesor
	private Teoria teoriaSelAlumno=null;
	private static final String PROTOCOLO_FINALIZAR="app://self/finalizar";
	
	/**
	 * Constructor del gestor.
	 * 
	 * @param service					Servicio de aprendizaje ({@link LearningService})
	 * @param listaGestion				Lista de temas
	 * @param txtTitulo					Campo de título del tema
	 * @param txtOrden					Campo de orden (índice) del tema
	 * @param editor					Editor HTML para crear temas
	 * @param listaAlumno				Lista de temas para el alumno
	 * @param webView					Visor web
	 * @param onTeoriaLeida				Acción de teoría leída
	 */
	public TeoriaSectionManager(LearningService service, ListView<Teoria> listaGestion,
								TextField txtTitulo, TextField txtOrden, HTMLEditor editor,
								ListView<Teoria> listaAlumno, WebView webView, Runnable onTeoriaLeida) {
		this.service=service;
		this.listaGestion=listaGestion;
		this.txtTitulo=txtTitulo;
		this.txtOrden=txtOrden;
		this.editor=editor;
		this.listaAlumno=listaAlumno;
		this.webView=webView;
		this.onTeoriaLeida=onTeoriaLeida;
		
		inicializar();
	}
	
	/**
	 * Inicializa el gestor.
	 */
	private void inicializar() {
		if(listaGestion!=null) {
			UIHelper.configurarLista(listaGestion, t->t.getOrderIndex()+". "+t.getTitulo());
			listaGestion.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> cargarEditor(newVal));
		}
		if(listaAlumno!=null) {
			UIHelper.configurarLista(listaAlumno, t->t.getOrderIndex()+". "+t.getTitulo());
			listaAlumno.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> mostrarTeoriaAlumno(newVal));
		}
		if(editor!=null) editor.setHtmlText("<p>...</p>");
		if(webView!=null) {
			webView.getEngine().setOnAlert(event -> {
				String mensaje=event.getData();
				if(mensaje!=null && mensaje.startsWith(PROTOCOLO_FINALIZAR)) {
					ejecutarFinalizarTema();
				}
			});
		}
	}
	
	/**
	 * Muestra una página de bienvenida cuando no hay un tema seleccionado.
	 */
	public void mostrarBienvenida() {
		if(webView==null) return;
		this.teoriaSelAlumno=null; 
		String css="""
				<style>
					body { font-family: 'Segoe UI', Helvetica, Arial, sans-serif; text-align: center; padding-top: 50px; color: #555; }
					h1 { color: #0078d7; margin-bottom: 20px; }
					p { font-size: 16px; line-height: 1.6; }
					.icono { font-size: 60px; color: #bdc3c7; margin-bottom: 20px; display: block; }
				</style>
				""";
		String html="""
				<html>
					<head>
						<meta charset="UTF-8">
						%s
					</head>
					<body>
						<span class='icono'>&#128218;</span>
						<h1>Biblioteca de Conocimientos</h1>
						<p>Selecciona un tema del menú de la izquierda<br>para comenzar a estudiar.</p>
					</body>
				</html>
				""".formatted(css);
		webView.getEngine().loadContent(html);
	}
	
	/**
	 * Ejecuta las acciones cuando el usuario termina de estudiar un tema y 
	 * pulsa el botón de finalizar.
	 */
	private void ejecutarFinalizarTema() {
		if(teoriaSelAlumno==null && listaAlumno!=null) {
			teoriaSelAlumno=listaAlumno.getSelectionModel().getSelectedItem();
		}
		if(teoriaSelAlumno!=null) {
			service.marcarTeoriaLeida(teoriaSelAlumno.getId());
			Platform.runLater(() -> {
				if(onTeoriaLeida!=null) onTeoriaLeida.run();
			});
		}
	}
	
	/**
	 * Carga la lista de temas teóricos para gestionarla.
	 */
	public void cargarListaGestion() {
		if(listaGestion!=null) listaGestion.getItems().setAll(service.getTeoria());
	}
	
	/**
	 * Crea un nuevo tema de contenido teórico. Parte de una plantilla para
	 * facilitar la creación.
	 */
	public void nuevo() {
		teoriaSel=null;
		if(listaGestion!=null) listaGestion.getSelectionModel().clearSelection();
		txtTitulo.clear();
		txtOrden.setText("1");
		String plantilla="""
				<body style="font-family: sans-serif; padding: 15px;">
					<h2 style="color: #2c3e50;">Título del tema</h2>
					<p>Escribe aquí la introducción teórica...</p>
					<h3 style="color: #0078d7;">Escribe aquí el contenido que desees...</h3>
					<div style="background-colo: #f8f9fa; padding: 10px; border-left: 4px solid #0078d7;">
						<strong>Nota:</strong> Puedes usar este cuadro para resaltar avisos.
					</div>
				</body>
				""";
		editor.setHtmlText(plantilla);
	}
	
	/**
	 * Guarda un tema creado por el profesor.
	 */
	public void guardar() {
		if(txtTitulo.getText().isEmpty()) {
			UIHelper.mostrarError("Título obligatorio.");
			return;
		}
		Teoria t=(teoriaSel!=null) ? teoriaSel : new Teoria();
		t.setTitulo(txtTitulo.getText());
		t.setContenido(editor.getHtmlText());
		try {
			t.setOrderIndex(Integer.parseInt(txtOrden.getText()));
		} catch(NumberFormatException e) {
			t.setOrderIndex(0);
		}
		if(service.guardarTeoria(t)) {
			UIHelper.mostrarInfo("Contenido teórico guardado.");
			cargarListaGestion();
			cargarListaAlumno();
			nuevo();
		}
	}
	
	/**
	 * Elimina un tema de contenido teórico de la lista.
	 */
	public void eliminar() {
		Teoria t=listaGestion.getSelectionModel().getSelectedItem();
		if(t==null) return;
		if(UIHelper.mostrarConfirmacion("Borrar Tema", "¿Seguro que quieres borrar este tema?")) {
			service.eliminarTeoria(t.getId());
			cargarListaGestion();
			cargarListaAlumno();
			nuevo();
		}
	}
	
	/**
	 * Carga un tema teórico en el editor para su modificación.
	 * 
	 * @param t				Tema a cargar
	 */
	private void cargarEditor(Teoria t) {
		if(t==null) return;
		teoriaSel=t;
		txtTitulo.setText(t.getTitulo());
		txtOrden.setText(String.valueOf(t.getOrderIndex()));
		editor.setHtmlText(t.getContenido());
	}
	
	/**
	 * Carga la lista de temas que verá el alumno.
	 */
	public void cargarListaAlumno() {
		if(listaAlumno!=null) {
			var lista=service.getTeoria();
			listaAlumno.getItems().setAll(lista);
			if(lista.isEmpty() || listaAlumno.getSelectionModel().getSelectedItem()==null) {
				mostrarBienvenida();
			}
		}
	}
	
	/**
	 * Muestra un tema de contenido teórico al alumno para su estudio.
	 * 
	 * @param t				Tema de teoría
	 */
	private void mostrarTeoriaAlumno(Teoria t) {
		if(t==null || webView==null) return;
		this.teoriaSelAlumno=t;
		boolean leido=service.isTeoriaLeida(t.getId());
		String btnTexto=leido ? "✓ Tema Leído" : "Marcar como leído";
		String btnEstado=leido ? "disabled" : "onclick='finalizarClick()'";
		String btnEstiloExtra=leido
				? "border-color: #bdc3c7; background-color: #bdc3c7; color: white; cursor: default;"
				: "";
		String cssStyle="""
				<style>
					body { font-family: 'SegoeUI', Helvetica, Arial, sans-serif; font-size: 14px; line-height; 1.6; padding: 20px; color: #333; }
					h1 { color: #0078d7; font-size: 24px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
					h2 { color: #2c3e50; font-size: 20px; margin-top: 20px; }
					p { margin-bottom: 15px; }
					img { max-width: 100%; height: auto; }
					
					.finalizar-container{ margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; text-align: right; }
					.btn-finalizar {
						background-color: transparent;
						color:#0078d7;
						border: 2px solid #0078d7;
						padding: 10px 25px;
						font-size: 14px;
						font-weight: bold;
						border-radius: 30px;
						cursor: pointer;
						transition: all 0.2s ease-in-out;
					}
					.btn-finalizar:enabled:hover { background-color: #0078d7; color: white; }
					}
				</style>
			""";
		//JavaScript
		String botonHtml="""
				<div class="finalizar-container">
					<button id="btnFin" class="btn-finalizar" style="%s" %s>
						%s
					</button>
				</div>
				<script>
					function finalizarClick() {
						var btn=document.getElementById('btnFin');
						btn.innerText='✓ Tema Leído';
						btn.disabled=true;
						btn.style.borderColor='bdc3c7';
						btn.style.backgroundColor='#bdc3c7';
						btn.style.color='white';
						btn.style.cursor='default';
						
						//Enviar señal a Java usando el protocolo personalizado a través de alerta.
						alert('app://self/finalizar');
					}
				</script>
				""".formatted(btnEstiloExtra, btnEstado, btnTexto);
		String htmlFinal="<html><head>"+cssStyle+"</head><body>"+t.getContenido()+botonHtml+"</body></html>";
		webView.getEngine().loadContent(htmlFinal);
	}
}
