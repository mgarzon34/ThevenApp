package com.circuitos.analisiscircuitos.gui.learning.model;

/**
 * Clase que define un documento PDF según sus parámetros.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class PdfDocument {
	
    private int id;
    private String titulo;
    private String descripcion;
    private String nombreArchivo;
    private String pathArchivo;
    private long tamanoArchivo;
    private int subidoPor;
    private String uploaderNombre;
    private String fechaSubida;

    /**
     * Obtiene el ID del documento PDF.
     * 
     * @return ID del PDF
     */
    public int getId() { 
    	return id; 
    }
    
    /**
     * Modifica el ID del documento PDF.
     * 
     * @param id nuevo del PDF
     */
    public void setId(int id) { 
    	this.id = id; 
    }
    
    /**
     * Obtiene el título del documento PDF.
     * 
     * @return título del PDF
     */
    public String getTitulo() { 
    	return titulo; 
    }
    
    /**
     * Modifica el título del documento PDF.
     * 
     * @param titulo nuevo del PDF
     */
    public void setTitulo(String titulo) { 
    	this.titulo=titulo; 
    }
    
    /**
     * Obtiene la descripción del documento PDF.
     * 
     * @return descripción nueva del PDF
     */
    public String getDescripcion() { 
    	return descripcion; 
    }
    
    /**
     * Modifica la descripción del documento PDF.
     * 
     * @param descripcion nueva del PDF
     */
    public void setDescripcion(String descripcion) { 
    	this.descripcion=descripcion; 
    }
    
    /**
     * Obtiene el nombre del archivo PDF.
     * 
     * @return nombre del archivo PDF
     */
    public String getNombreArchivo() { 
    	return nombreArchivo; 
    }
    
    /**
     * Modifica el nombre del archivo PDF.
     * 
     * @param nombreArchivo nuevo del archivo PDF
     */
    public void setNombreArchivo(String nombreArchivo) { 
    	this.nombreArchivo=nombreArchivo; 
    }
    
    /**
     * Obtiene la ruta donde está alojado el documento PDF.
     * 
     * @return ruta del archivo PDF
     */
    public String getPathArchivo() { 
    	return pathArchivo; 
    }
    
    /**
     * Modifica la ruta donde se aloja el documento PDF.
     * 
     * @param pathArchivo ruta nueva del archivo PDF
     */
    public void setPathArchivo(String pathArchivo) { 
    	this.pathArchivo=pathArchivo; 
    }
    
    /**
     * Obtiene el tamaño del archivo PDF.
     * 
     * @return tamaño de archivo PDF
     */
    public long getTamanoArchivo() { 
    	return tamanoArchivo; 
    }
    
    /**
     * Modifica el tamaño del archivo PDF.
     * 
     * @param tamanoArchivo nuevo del PDF
     */
    public void setTamanoArchivo(long tamanoArchivo) { 
    	this.tamanoArchivo=tamanoArchivo; 
    }
    
    /**
     * Obtiene el ID del usuario que subió el documento PDF.
     * 
     * @return ID del usuario que lo ha subido
     */
    public int getSubidoPor() { 
    	return subidoPor; 
    }
    
    /**
     * Modifica el usuario que subió el documento cambiando el ID por el de otro usuario.
     * 
     * @param subidoPor nuevo usuario
     */
    public void setSubidoPor(int subidoPor) { 
    	this.subidoPor=subidoPor; 
    }
    
    /**
     * Obtiene el nombre del usuario que subió el documento PDF.
     * 
     * @return nombre del usuario que subió el archivo
     */
    public String getUploaderNombre() { 
    	return uploaderNombre; 
    }
    
    /**
     * Modifica el usuario que subió el documento cambiando el nombre por el de otro usuario.
     * 
     * @param uploaderNombre nuevo usuario
     */
    public void setUploaderNombre(String uploaderNombre) { 
    	this.uploaderNombre=uploaderNombre; 
    }
    
    /**
     * Obtiene la fecha de subida del documento PDF.
     * 
     * @return fecha de subida
     */
    public String getFechaSubida() { 
    	return fechaSubida; 
    }
    
    /**
     * Modifica la fecha de subida del documento PDF.
     * 
     * @param fechaSubida nueva
     */
    public void setFechaSubida(String fechaSubida) { 
    	this.fechaSubida=fechaSubida; 
    }
    
    /**
     * Obtiene el tamaño del archivo en modo texto.
     * 
     * @return String con el tamaño del archivo
     */
    public String getTamanoArchivoTexto() {
        if (tamanoArchivo<1024) {
            return tamanoArchivo + " B";
        } else if (tamanoArchivo<1024*1024) {
            return String.format("%.1f KB", tamanoArchivo/1024.0);
        } else {
            return String.format("%.1f MB", tamanoArchivo/(1024.0 * 1024.0));
        }
    }
}
