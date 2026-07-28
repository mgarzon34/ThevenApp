package com.circuitos.analisiscircuitos.gui.learning.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import com.circuitos.analisiscircuitos.gui.learning.model.User;

public class UserData {
	
	private static final Logger logger=Logger.getLogger(UserData.class.getName());
	private final DatabaseService db;
	
	public UserData(DatabaseService db) {
		this.db=db;
	}
	
	/**
	 * Autentica un usuario previamente registrado.
	 * 
	 * @param username		Nombre de usuario
	 * @param password		Contraseña
	 * @return {@link User} si es válido, {@code null} si no
	 */
	public User autenticarUsuario(String username, String password) {
		String sql="SELECT id, username, password_hash, role, nombre, apellido1, apellido2,"
				+ "pregunta_seguridad, respuesta_seguridad, calificacion_general, comentarios_profesor "
				+ "FROM users WHERE username = ?";
		User user=null;
		boolean necesitaRehash=false;
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setString(1, username);
			ResultSet rs=pstmt.executeQuery();
			if(rs.next()) {
				String storedHash=rs.getString("password_hash");
				if(verificarPassword(password, storedHash)) {
					user=mapearUsuario(rs);
					necesitaRehash=!esFormatoBCrypt(storedHash);
				}
			}
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error autenticando usuario", e);
			return null;
		}
		// Importante: la conexión/consulta de lectura ya está cerrada aquí (fin del try-with-resources).
		// SQLite solo admite un escritor a la vez y bloquea si hay una conexión de lectura todavía
		// abierta; por eso la migración a BCrypt se hace fuera del bloque anterior.
		if(user!=null && necesitaRehash) {
			cambiarPassword(username, password); //regenera el hash ya en BCrypt
		}
		return user;
	}
	
	/**
	 * Crea un nuevo usuario en la base de datos.
	 * 
	 * @param username					Nombre de usuario
	 * @param password					Contraseña del usuario
	 * @param nombre					Nombre del usuario
	 * @param apellido1					Primer apellido
	 * @param apellido2					Segundo apellido
	 * @param role						Rol (Estudiante/Profesor)	
	 * @param preguntaSeguridad			Pregunta de seguridad
	 * @param respuestaSeguridad		Respuesta de seguridad
	 * @return {@code true} si se creó correctamente, {@code false} si no
	 */
	public boolean crearUsuario(String username, String password, String nombre, String apellido1, String apellido2, String role,
								String preguntaSeguridad, String respuestaSeguridad) {
		String sql="INSERT INTO users (username, password_hash, role, nombre, apellido1, apellido2,"
				+ "pregunta_seguridad, respuesta_seguridad) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		
		try(Connection conn=db.getConnection();
			PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setString(1, username);
			pstmt.setString(2, hashPassword(password));
			pstmt.setString(3, role);
			pstmt.setString(4, nombre);
			pstmt.setString(5, apellido1);
			pstmt.setString(6,  apellido2);
			pstmt.setString(7,  preguntaSeguridad);
			pstmt.setString(8, respuestaSeguridad);
			return pstmt.executeUpdate()>0;
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error creando usuario", e);
			return false;
		}
	}
	
	/**
	 * Busca un usuario por su nombre de usuario (para recuperación de contraseña).
	 * 
	 * @param username			Nombre de usuario a buscar
	 * @return Objeto User con datos del usuario o {@code null} si no existe
	 */
	public User buscarUsuarioPorNombre(String username) {
		String sql="SELECT id, username, password_hash, role, nombre, apellido1, apellido2,"
				+ "pregunta_seguridad, respuesta_seguridad, calificacion_general, comentarios_profesor "
				+ "FROM users WHERE username=?";
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setString(1, username);
			ResultSet rs=pstmt.executeQuery();
			if(rs.next()) {
				return mapearUsuario(rs);
			}
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error buscando usuario por nombre", e);
		}
		return null;
	}
	
	/**
	 * Permite modificar la contraseña de un usuario (en caso de recuperación).
	 * 
	 * @param username					Nombre de usuario
	 * @param newPassword				Nueva contraseña
	 * @return {@code true} si se cambia con éxito, {@code false} si no
	 */
	public boolean cambiarPassword(String username, String newPassword) {
		String sql="UPDATE users SET password_hash=? WHERE username=?";
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setString(1, hashPassword(newPassword));
			pstmt.setString(2, username);
			return pstmt.executeUpdate()>0;
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error modificando contraseña", e);
			return false;
		}
	}
	
	/**
	 * Mapea el "valor" de una fila de la base de datos y rellena los parámetros
	 * del objeto de dominio {@link User}.
	 * 
	 * @param rs				ResultSet de la fila a convertir
	 * @return {@link User} creado a partir de los datos
	 * @throws SQLException Si ocurre un error al obtener algún campo.
	 */
	private User mapearUsuario(ResultSet rs) throws SQLException {
		User user=new User();
		user.setId(rs.getInt("id"));
		user.setUsername(rs.getString("username"));
		user.setRole(rs.getString("role"));
		user.setName(rs.getString("nombre"));
		user.setApellido1(rs.getString("apellido1"));
		user.setApellido2(rs.getString("apellido2"));
		user.setPreguntaSeguridad(rs.getString("pregunta_seguridad"));
		user.setRespuestaSeguridad(rs.getString("respuesta_seguridad"));
		user.setCalificacionGeneral(rs.getDouble("calificacion_general"));
		user.setComentariosProfesor(rs.getString("comentarios_profesor"));
		return user;
	}
	
	/**
	 * Obtiene la lista de estudiantes registrados.
	 * 
	 * @return lista de estudiantes
	 */
	public List<User> getEstudiantes() {
		List<User> lista=new ArrayList<>();
		String sql="SELECT * FROM users WHERE role='ESTUDIANTE'";
		try(Connection conn=db.getConnection();
				Statement stmt=conn.createStatement();
				ResultSet rs=stmt.executeQuery(sql)) {
			while(rs.next()) {
				lista.add(mapearUsuario(rs));
			}
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error obteniendo estudiantes", e);
		}
		return lista;
	}
	
	/**
	 * Hash de contraseña con BCrypt (incluye sal aleatoria y factor de coste)
	 * 
	 * @param password			Contraseña que hashea
	 */
	private String hashPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(12));
	}

	/**
	 * Comprueba si un hash tiene formato BCrypt ($2a$, $2b$, $2y$).
	 */
	private boolean esFormatoBCrypt(String hash) {
		return hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
	}
	
	/**
	 * Verifica contraseña. Soporta el nuevo BCrypt y el antiguo SHA-256 (por compatibilidad).
	 * 
	 * @param password			Contraseña
	 * @param hash				Hash de la contraseña
	 * @return {@code true} si se ha verificado, {@code false} si no
	 */
	private boolean verificarPassword(String password, String hash) {
		if(esFormatoBCrypt(hash)) {
			return BCrypt.checkpw(password, hash);
		}
		return verificarPasswordSha256(password, hash);
	}

	/**
	 * Verificación de contraseña con el algoritmo antiguo SHA-256.
	 */
	private boolean verificarPasswordSha256(String password, String hash) {
		try {
			java.security.MessageDigest digest=java.security.MessageDigest.getInstance("SHA-256");
			byte[] calculado=digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			String calculadoBase64=java.util.Base64.getEncoder().encodeToString(calculado);
			return calculadoBase64.equals(hash);
		} catch(Exception e) {
			throw new RuntimeException("Error verificando contraseña heredada", e);
		}
	}
}
