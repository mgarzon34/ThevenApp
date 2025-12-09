package com.circuitos.analisiscircuitos.gui.learning.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.gui.learning.model.Ejercicio;
import com.circuitos.analisiscircuitos.gui.learning.model.Teoria;

public class ContenidoData {
	
	private static final Logger logger=Logger.getLogger(ContenidoData.class.getName());
	private final DatabaseService db;
	
	public ContenidoData(DatabaseService db) {
		this.db=db;
	}
	
	/**
	 * Obtiene todo el contenido de teoría ordenado.
	 * 
	 * @return	Lista de contenido de teoría
	 */
	public List<Teoria> getTeoria() {
		List<Teoria> contenido=new ArrayList<>();
		String sql="SELECT id, titulo, contenido, indice_orden FROM teoria ORDER BY indice_orden, titulo";
		try(Connection conn=db.getConnection();
			PreparedStatement stmt=conn.prepareStatement(sql);
			ResultSet rs=stmt.executeQuery()) {
			while(rs.next()) {
				Teoria teoria=new Teoria();
				teoria.setId(rs.getInt("id"));
				teoria.setTitulo(rs.getString("titulo"));
				teoria.setContenido(rs.getString("contenido"));
				teoria.setOrderIndex(rs.getInt("indice_orden"));
				contenido.add(teoria);
			}
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error obteniendo contenido de teoría", e);
		}
		return contenido;	
	}
	
	/**
	 * Guarda la teoría nueva o actualiza teoría existente editada por un profesor en la base de datos. 
	 * 
	 * @param t			Teoría nueva
	 * @return {@code true} si se ha guardado, {@code false} si no
	 */
	public boolean guardarTeoria(Teoria t) {
		//Si el ID es 0, es nuevo, si ya tiene ID, es actualizar
		if(t.getId()==0) {
			String sql="INSERT INTO teoria (titulo, contenido, indice_orden, creado_por) VALUES (?, ?, ?, ?)";
			try(Connection conn=db.getConnection();
					PreparedStatement pstmt=conn.prepareStatement(sql)) {
				pstmt.setString(1, t.getTitulo());
				pstmt.setString(2, t.getContenido());
				pstmt.setInt(3, t.getOrderIndex());
				pstmt.setInt(4, 1);		//PASAR USUARIO ACTUAL
				return pstmt.executeUpdate()>0;
			} catch(SQLException e) {
				logger.log(Level.WARNING, "Error guardando teoría", e);
				return false;
			}
		} else {
			String sql="UPDATE teoria SET titulo=?, contenido=?, indice_orden=? WHERE id=?";
			try(Connection conn=db.getConnection();
					PreparedStatement pstmt=conn.prepareStatement(sql)) {
				pstmt.setString(1,  t.getTitulo());
				pstmt.setString(2,  t.getContenido());
				pstmt.setInt(3, t.getOrderIndex());
				pstmt.setInt(4, t.getId());
				return pstmt.executeUpdate()>0;
			} catch(SQLException e) {
				logger.log(Level.WARNING, "Error actualizando teoría", e);
				return false;
			}
		}
	}
	
	/**
	 * Elimina un contenido teórico de la base de datos.
	 * 
	 * @param id			ID del contenido a eliminar
	 * @return {@code true} si se elimina, {@code false} si no
	 */
	public boolean eliminarTeoria(int id) {
		String sql="DELETE FROM teoria WHERE id=?";
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setInt(1,  id);
			return pstmt.executeUpdate()>0;
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error eliminando teoría", e);
			return false;
		}
	}
	
	/**
	 * Marca un tema de teoría como leído por el usuario.
	 * 
	 * @param userId				Id del usuario
	 * @param teoriaId				Ide de la teoría
	 * @return {@code true} si se actualizó, {@code false} si no
	 */
	public boolean marcarTeoriaLeida(int userId, int teoriaId) {
		String sql="INSERT OR IGNORE INTO progreso_teoria (user_id, teoria_id) VALUES (?, ?)";
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, teoriaId);
			return pstmt.executeUpdate()>0;
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error actualizando calificación", e);
			return false;
		}
	}
	
	/**
	 * Comprueba si un usuario ya ha leído un tema específico.
	 * 
	 * @param userId				ID del usuario
	 * @param teoriaId				ID del tema de teoría
	 * @return {@code true} si lo ha leído, {@code false} si no
	 */
	public boolean isTeoriaLeida(int userId, int teoriaId) {
		String sql="SELECT COUNT(*) FROM progreso_teoria WHERE user_id=? AND teoria_id=?";
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, teoriaId);
			try(ResultSet rs=pstmt.executeQuery()) {
				if(rs.next()) {
					return rs.getInt(1)>0;
				}
			}
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error comprobando teoría leída", e);
		}
		return false;
	}
	
	/**
	 * Cuenta los temas leídos por un estudiante.
	 * 
	 * @param userId			Id del usuario
	 * @return número de temas leídos
	 */
	public int contarTemasLeidos(int userId) {
		String sql="SELECT COUNT(*) FROM progreso_teoria WHERE user_id=?";
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			ResultSet rs=pstmt.executeQuery();
			return rs.next() ? rs.getInt(1) : 0;
		} catch(SQLException e) {
			return 0;
		}
	}
	/**
	 * Obtiene todos los ejercicios.
	 * 
	 * @return Lista de ejercicios
	 */
	public List<Ejercicio> getEjercicios() {
		List<Ejercicio> ejercicios=new ArrayList<>();
		String sql="SELECT id, titulo, descripcion, datos_circuito, solucion_vth, solucion_rth, solucion_in, solucion_rn, "+
						"tipo_analisis, dificultad, nodo_a, nodo_b FROM ejercicios ORDER BY dificultad, titulo";
		try(Connection conn=db.getConnection();
			PreparedStatement pstmt=conn.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery()) {
			while(rs.next()) {
				Ejercicio ejercicio=new Ejercicio();
				ejercicio.setId(rs.getInt("id"));
				ejercicio.setTitulo(rs.getString("titulo"));
				ejercicio.setDescripcion(rs.getString("descripcion"));
				ejercicio.setDatosCircuito(rs.getString("datos_circuito"));
				ejercicio.setSolucionVth(rs.getDouble("solucion_vth"));
				ejercicio.setSolucionRth(rs.getDouble("solucion_rth"));
				ejercicio.setSolucionIn(rs.getDouble("solucion_in"));
				ejercicio.setSolucionRn(rs.getDouble("solucion_rn"));
				ejercicio.setTipoAnalisis(rs.getString("tipo_analisis"));
				ejercicio.setDificultad(rs.getInt("dificultad"));
				ejercicio.setNodoAnalisisA(rs.getInt("nodo_a"));
				if(rs.wasNull()) ejercicio.setNodoAnalisisA(-1);
				ejercicio.setNodoAnalisisB(rs.getInt("nodo_b"));
				if(rs.wasNull()) ejercicio.setNodoAnalisisB(-1);
				ejercicios.add(ejercicio);
			}
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error obteniendo ejercicios", e);
		}
		return ejercicios;
	}
	
	/**
	 * Guarda un ejercicio en la base de datos.
	 * 
	 * @param e				Ejercicio a guardar
	 * @return {@code true} si lo guarda, {@code false} si no
	 */
	public boolean guardarEjercicio(Ejercicio e) {
		if(e.getId()==0) {
			String sql="INSERT INTO ejercicios (titulo, descripcion, datos_circuito, "+
						"solucion_vth, solucion_rth, solucion_in, solucion_rn, "+
						"tipo_analisis, dificultad, creado_por, nodo_a, nodo_b) "+
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			try(Connection conn=db.getConnection();
					PreparedStatement pstmt=conn.prepareStatement(sql)) {
				pstmt.setString(1, e.getTitulo());
				pstmt.setString(2, e.getDescripcion());
				pstmt.setString(3, e.getDatosCircuito());
				pstmt.setDouble(4, e.getSolucionVth());
				pstmt.setDouble(5, e.getSolucionRth());
				pstmt.setDouble(6, e.getSolucionIn());
				pstmt.setDouble(7, e.getSolucionRn());
				pstmt.setString(8, e.getTipoAnalisis());
				pstmt.setInt(9, e.getDificultad());
				pstmt.setInt(10, 1);
				pstmt.setInt(11, e.getNodoAnalisisA());
				pstmt.setInt(12, e.getNodoAnalisisB());
				return pstmt.executeUpdate()>0;
			} catch(SQLException ex) {
				logger.log(Level.WARNING, "Error guardando ejercicio", ex);
				return false;
			}
		} else {
			String sql="UPDATE ejercicios SET titulo=?, descripcion=?, datos_circuito=?,"+
					"solucion_vth=?, solucion_rth=?, solucion_in=?, solucion_rn=?,"+
					"tipo_analisis=?, dificultad=?, nodo_a=?, nodo_b=? WHERE id=?";
			try(Connection conn=db.getConnection();
					PreparedStatement pstmt=conn.prepareStatement(sql)) {
				pstmt.setString(1, e.getTitulo());
				pstmt.setString(2, e.getDescripcion());
				pstmt.setString(3, e.getDatosCircuito());
				pstmt.setDouble(4, e.getSolucionVth());
				pstmt.setDouble(5, e.getSolucionRth());
				pstmt.setDouble(6, e.getSolucionIn());
				pstmt.setDouble(7, e.getSolucionRn());
				pstmt.setString(8, e.getTipoAnalisis());
				pstmt.setInt(9, e.getDificultad());
				pstmt.setInt(10, e.getNodoAnalisisA());
				pstmt.setInt(11, e.getNodoAnalisisB());
				pstmt.setInt(12, e.getId());
				return pstmt.executeUpdate()>0;
			} catch(SQLException ex) {
				logger.log(Level.WARNING, "Error actualizando ejercicio", ex);
				return false;
			}
		}
	}
	
	/**
	 * Elimina un ejercicio de la base de datos por su ID.
	 * 
	 * @param id			Identificador único
	 * @return {@code true} si se eliminó, {@code false} si no
	 */
	public boolean eliminarEjercicio(int id) {
		String sql="DELETE FROM ejercicios WHERE id=?";
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			return pstmt.executeUpdate()>0;
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error eliminando ejercicio", e);
			return false;
		}
	}
	
	/**
	 * Cuenta los ejercicios resueltos por un estudiante.
	 * 
	 * @param userId			Id del usuario
	 * @return número de ejercicios resueltos
	 */
	public int contarEjerciciosResueltos(int userId) {
		String sql="SELECT COUNT(*) FROM progreso_estudiante WHERE estudiante_id=?";
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			ResultSet rs=pstmt.executeQuery();
			return rs.next() ? rs.getInt(1) : 0;
		} catch(SQLException e) {
			return 0;
		}
	}
	
	/**
	 * Guarda o actualiza (si lo repite) la puntuación de un ejercicio realizado.
	 * 
	 * @param userId				Id del usuario
	 * @param ejercicioId			Id del ejercicio
	 * @param puntuacion			Puntuación del ejercicio (10 - realizado)
	 * @return {@code true} si se guarda o actualiza, {@code false} si no
	 */
	public boolean guardarProgresoEjercicio(int userId, int ejercicioId, double puntuacion) {
		String sql="INSERT OR REPLACE INTO progreso_estudiante (estudiante_id, ejercicio_id, puntuacion, completado_fecha) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2,  ejercicioId);
			pstmt.setDouble(3, puntuacion);
			return pstmt.executeUpdate()>0;
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error guardando progreso ejercicio", e);
			return false;
		}
	}
	
	/**
	 * Obtiene la lista de ejercicios completados por un usuario.
	 * 
	 * @param userId			Id del usuario
	 * @return lista de ejercicios completados
	 */
	public List<Integer> getIdsEjerciciosCompletados(int userId) {
		List<Integer> ids=new ArrayList<>();
		String sql="SELECT ejercicio_id FROM progreso_estudiante WHERE estudiante_id=? AND puntuacion>=10";
		try(Connection conn=db.getConnection();
				PreparedStatement pstmt=conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			ResultSet rs=pstmt.executeQuery();
			while(rs.next()) {
				ids.add(rs.getInt("ejercicio_id"));
			}
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Error obteniendo ejercicios completados", e);
		}
		return ids;
	}
}
