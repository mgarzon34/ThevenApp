package com.circuitos.analisiscircuitos.gui.learning.database;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de base de datos SQLite para el sistema de E-Learning de la aplicación ThevenApp.
 * Gestiona usuarios, ejercicios, teoría y progreso de los estudiantes.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class DatabaseService {
	private static final Logger logger=Logger.getLogger(DatabaseService.class.getName());
	
	private static final String APP_FOLDER=System.getProperty("user.home")+File.separator+"ThevenApp";
	private static final String DB_URL="jdbc:sqlite:"+APP_FOLDER+File.separator+"thevenapp_database.db";
	
	private final UserData userdata;
	private final ContenidoData contendata;
	private final PdfData pdfdata;
	private final ProgresoData progresodata;
	
	/**
	 * Constructor. Asegura que la base de datos exista.
	 */
	public DatabaseService() {
		inicializarDatabase();
		
		this.userdata=new UserData(this);
		this.contendata=new ContenidoData(this);
		this.pdfdata=new PdfData(this);
		this.progresodata=new ProgresoData(this);
	}
	
	/**
	 * Obtiene la conexión con la base de datos. Los archivos Data lo utilizan
	 * para conectarse a la base de datos e interactuar con ella.
	 * 
	 * @return Conexión con la base de datos
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL);
	}
	
	/* Getters para archivos Data (user, contenido, pdf, progreso) */
	public UserData getUserData() { return userdata; }
	public ContenidoData getContenidoData() { return contendata; }
	public PdfData getPdfData() { return pdfdata; }
	public ProgresoData getProgresoData() { return progresodata; }
	
	/**
	 * Inicializa la base de datos creando las tablas necesarias.
	 */
	private void inicializarDatabase() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch(ClassNotFoundException e) {
			logger.severe("No se encontró el driver de SQLite en el classpath");
			throw new RuntimeException(e);
		}
		
		try {
			Files.createDirectories(Paths.get(APP_FOLDER));
			try(Connection conn=DriverManager.getConnection(DB_URL)) {
				if(conn!=null) {
					crearTablas(conn);
					logger.info("Base de datos conectada en: "+DB_URL);
				}
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Error crítico inicializando base de datos", e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Crea las tablas de la base de datos.
	 */
	private void crearTablas(Connection conn) throws SQLException {
		Statement stmt=conn.createStatement();
		
		//Tabla de usuarios
		stmt.execute("""
				CREATE TABLE IF NOT EXISTS users (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					username TEXT UNIQUE NOT NULL,
					password_hash TEXT NOT NULL,
					role TEXT NOT NULL CHECK(role IN ('ESTUDIANTE', 'PROFESOR')),
					nombre TEXT,
					apellido1 TEXT,
					apellido2 TEXT,
					pregunta_seguridad TEXT,
					respuesta_seguridad TEXT,
					calificacion_general REAL DEFAULT 0,
					comentarios_profesor TEXT,
					fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
				)
			""");
		
		//Tabla de teoría
		stmt.execute("""
				CREATE TABLE IF NOT EXISTS teoria (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					titulo TEXT NOT NULL,
					contenido TEXT NOT NULL,
					indice_orden INTEGER DEFAULT 0,
					creado_por INTEGER,
					fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
					FOREIGN KEY(creado_por) REFERENCES users(id)
				)
			""");
		
		//Tabla de ejercicios
		stmt.execute("""
				CREATE TABLE IF NOT EXISTS ejercicios (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					titulo TEXT NOT NULL,
					descripcion TEXT NOT NULL,
					datos_circuito TEXT NOT NULL,
					solucion_vth REAL,
					solucion_rth REAL,
					solucion_in REAL,
					solucion_rn REAL,
					tipo_analisis TEXT NOT NULL CHECK(tipo_analisis IN ('THEVENIN', 'NORTON', 'AMBOS')),
					dificultad INTEGER DEFAULT 1 CHECK(dificultad BETWEEN 1 AND 5),
					creado_por INTEGER,
					nodo_a INTEGER DEFAULT -1,
					nodo_b INTENGER DEFAULT -1,
					fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
					FOREIGN KEY(creado_por) REFERENCES users(id)
				)
			""");
		
		//Tabla de documentos PDF
		stmt.execute("""
				CREATE TABLE IF NOT EXISTS documentos_pdf (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					titulo TEXT NOT NULL,
					descripcion TEXT NOT NULL,
					nombre_archivo TEXT NOT NULL,
					path_archivo TEXT NOT NULL,
					tamano_archivo INTEGER DEFAULT 0,
					subido_por INTEGER,
					fecha_subida DATETIME DEFAULT CURRENT_TIMESTAMP,
					FOREIGN KEY(subido_por) REFERENCES users(id)
				)
			""");
		
		//Tabla de progreso de estudiantes
		stmt.execute("""
				CREATE TABLE IF NOT EXISTS progreso_estudiante (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					estudiante_id INTEGER NOT NULL,
					ejercicio_id INTEGER NOT NULL,
					completado_fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
					puntuacion REAL DEFAULT 0,
					intentos INTEGER DEFAULT 1,
					tiempo INTEGER DEFAULT 0,
					FOREIGN KEY(estudiante_id) REFERENCES users(id),
					FOREIGN KEY(ejercicio_id) REFERENCES ejercicios(id),
					UNIQUE(estudiante_id, ejercicio_id)
				)
			""");
		
		//Tabla de teoría leída
		stmt.execute("""
				CREATE TABLE IF NOT EXISTS progreso_teoria (
					user_id INTEGER,
					teoria_id INTEGER,
					fecha_lectura DATETIME DEFAULT CURRENT_TIMESTAMP,
					PRIMARY KEY(user_id, teoria_id),
					FOREIGN KEY(user_id) REFERENCES users(id),
					FOREIGN KEY(teoria_id) REFERENCES teoria(id)
				)
			""");
		
		stmt.close();
		logger.fine("Tablas de base de datos creadas con éxito");
	}
}
