package com.circuitos.analisiscircuitos.gui.learning.model;

/**
 * Clase auxiliar que gestiona las estadísticas de progreso de un estudiante.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class EstadisticasProgreso {
	
    private int ejerciciosCompletados;
    private int totalEjercicios;
    private double puntuacionPromedio;
    private double progresoTotal;

    /**
     * Obtiene el número de ejercicios completados.
     * 
     * @return	número de ejercicios completados
     */
    public int getEjerciciosCompletados() {
        return ejerciciosCompletados;
    }
    
    /**
     * Modifica el número de ejercicios completados.
     * 
     * @param ejerciciosCompletados
     */
    public void setEjerciciosCompletados(int ejerciciosCompletados) {
        this.ejerciciosCompletados=ejerciciosCompletados;
    }
    
    /**
     * Obtiene el número total de ejercicios que hay en el gestor.
     * 
     * @return número total de ejercicios propuestos
     */
    public int getTotalEjercicios() {
        return totalEjercicios;
    }
    
    /**
     * Modifica el número total de ejercicios que hay.
     * 
     * @param totalEjercicios
     */
    public void setTotalEjercicios(int totalEjercicios) {
        this.totalEjercicios=totalEjercicios;
    }
    
    /**
     * Obtiene la puntución promedio.
     * 
     * @return puntuación promedio
     */
    public double getPuntuacionPromedio() {
        return puntuacionPromedio;
    }
    
    /**
     * Modifica la puntuación promedio.
     * 
     * @param puntuacionPromedio
     */
    public void setPuntuacionPromedio(double puntuacionPromedio) {
        this.puntuacionPromedio=puntuacionPromedio;
    }
    
    /**
     * Obtiene el progreso total.
     * 
     * @return	progreso total
     */
    public double getProgresoTotal() {
    	return progresoTotal;
    }
    
    /**
     * Modifica el progreso total.
     * 
     * @param progresoTotal
     */
    public void setProgresoTotal(double progresoTotal) {
    	this.progresoTotal=progresoTotal;
    }
    
    /**
     * Obtiene el porcentaje de ejercicios completados.
     * 
     * @return porcentaje de ejercicios completados
     */
    public double getPorcentaje() {
        if (totalEjercicios==0) return 0.0;
        return (double) ejerciciosCompletados/totalEjercicios;
    }
    
    /**
     * Obtiene el porcentaje de ejercicios completados en modo texto.
     * 
     * @return String con el número de ejercicios completados
     */
    public String getPorcentajeTexto() {
        return String.format("%.1f%%", getPorcentaje()*100);
    }
    
    /**
     * Obtiene la puntuación promedio en modo texto.
     * 
     * @return String con la puntuación promedio
     */
    public String getPuntuacionPromedioTexto() {
        return String.format("%.1f%%", puntuacionPromedio);
    }
    
    /**
     * Calcula el progreso total de un alumno teniendo en cuenta teoría y ejercicios.
     * 
     * @param temasLeidos			Temas leídos por el alumno
     * @param totalTemas			Total temas propuestos
     */
    public void calcularProgresoTotal(int temasLeidos, int totalTemas) {
    	double pctEj=getPorcentaje();
    	double pctTeo=(totalTemas>0) ? (double) temasLeidos/totalTemas : 0.0;
    	if(totalEjercicios>0 && totalTemas>0) {
    		this.progresoTotal=(pctEj+pctTeo)/2.0;
    	} else if(totalEjercicios>0) {
    		this.progresoTotal=pctEj;	//Solo cuenta los ejercicios
    	} else if(totalTemas>0) {
    		this.progresoTotal=pctTeo;	//Solo cuentan la teoría
    	} else {
    		this.progresoTotal=0.0;
    	}
    }
}
