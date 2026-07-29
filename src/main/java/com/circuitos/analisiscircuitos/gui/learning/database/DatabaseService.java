package com.circuitos.analisiscircuitos.gui.learning.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.flywaydb.core.Flyway;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Servicio de base de datos SQLite para el sistema de E-Learning de la aplicación ThevenApp.
 * Gestiona usuarios, ejercicios, teoría y progreso de los estudiantes.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class DatabaseService {
	private static final Logger logger=Logger.getLogger(DatabaseService.class.getName());
	
	private final HikariDataSource dataSource;

	private final UserData userdata;
	private final ContenidoData contendata;
	private final PdfData pdfdata;
	private final ProgresoData progresodata;
	
	/**
	 * Constructor.
	 */
	public DatabaseService() {
		this.dataSource=crearDataSource();
		ejecutarMigraciones();
		
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
		return dataSource.getConnection();
	}
	
	/* Getters para archivos Data (user, contenido, pdf, progreso) */
	public UserData getUserData() { return userdata; }
	public ContenidoData getContenidoData() { return contendata; }
	public PdfData getPdfData() { return pdfdata; }
	public ProgresoData getProgresoData() { return progresodata; }
	
	/**
	 * Crea el pool de conexiones. 
	 */
	private HikariDataSource crearDataSource() {
		// stringtype=unspecified: evita el error "column es de tipo X pero la expresion es de tipo character varying"
		// al insertar Strings de Java en columnas ENUM (user_role, tipo_analisis) via setString().
		String url=System.getenv().getOrDefault("THEVENAPP_DB_URL", "jdbc:postgresql://localhost:5433/thevenapp?stringtype=unspecified");
		String user=System.getenv().getOrDefault("THEVENAPP_DB_USER", "thevenapp");
		String password=System.getenv().getOrDefault("THEVENAPP_DB_PASSWORD", "thevenapp_dev");

		HikariConfig config=new HikariConfig();
		config.setJdbcUrl(url);
		config.setUsername(user);
		config.setPassword(password);
		config.setMaximumPoolSize(10);
		config.setPoolName("ThevenAppPool");

		logger.info("Conectando a la base de datos: "+url);
		return new HikariDataSource(config);
	}

	/**
	 * Aplica las migraciones de Flyway (src/main/resources/db/migration/*.sql)
	 * Sustituye al antiguo "crearTablas()"
	 */
	private void ejecutarMigraciones() {
		Flyway flyway=Flyway.configure()
				.dataSource(dataSource)
				.locations("classpath:db/migration")
				.load();
		flyway.migrate();
		logger.info("Migraciones de Flyway aplicadas correctamente");
	}
}
