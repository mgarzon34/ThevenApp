package com.circuitos.analisiscircuitos.gui.renderer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.analisis.ResultadoThevenin;
import com.circuitos.analisiscircuitos.dominio.Circuito;
import com.circuitos.analisiscircuitos.dominio.Componente;
import com.circuitos.analisiscircuitos.dominio.FuenteTensionInd;
import com.circuitos.analisiscircuitos.dominio.Resistencia;
import com.circuitos.analisiscircuitos.dominio.Thevenin;
import com.circuitos.analisiscircuitos.gui.model.PuntoConexion.Posicion;

/**
 * Clase específica que define cómo se construye visualmente el circuito equivalente de Thevenin.
 * Utiliza la clase {@link CircuitoEquivalenteRenderer} para el renderizado.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class TheveninRenderer {
	private static final Logger logger=Logger.getLogger(TheveninRenderer.class.getName());
	
	private final int FUENTE_X=150, FUENTE_Y=350;
	private final int RTHEV_X=250, RTHEV_Y=250;
	private final int RCARGA_X=450, RCARGA_Y=350;
	
	private final CircuitoEquivalenteRenderer renderer;
	
	/**
	 * Constructor
	 * 
	 * @param renderer 		CircuitoEquivalenteRenderer
	 */
	public TheveninRenderer(CircuitoEquivalenteRenderer renderer) {
		this.renderer=renderer;
	}
	
	/**
	 * Renderiza un circuito en su equivalente de Thevenin.
	 * 
	 * @param original			Circuito original
	 * @param nodoNeg			Nodo de control negativo
	 * @param nodoPos			Nodo de control positivo
	 */
	public void renderizarTh(Circuito original, int nodoNeg, int nodoPos, ResultadoThevenin resultado) {
		logger.info("Calculando circuito equivalente de Thevenin...");
		long numCargas=original.getComponentes().stream()
				.filter(Componente::isCarga)
				.count();
		Thevenin thevenin=new Thevenin(original, nodoNeg, nodoPos);
		Circuito cThev=thevenin.getEquivalenteTheveninGrafico();
		
		Componente fuente=cThev.getComponentes().stream()
				.filter(c -> c instanceof FuenteTensionInd)
				.findFirst()
				.orElse(null);	
		List<Resistencia> resistencias=cThev.getComponentes().stream()
				.filter(c -> c instanceof Resistencia)
				.map(c -> (Resistencia) c)
				.toList();
		
		if(fuente==null || resistencias.isEmpty()) {
			logger.warning("Faltan componentes para construir el equivalente de Thevenin.");
			return;
		}
		if(fuente instanceof FuenteTensionInd fti) fti.setValor(resultado.getVth());
		Resistencia rThev=null;
		Resistencia rCarga=null;
		for(Resistencia r : resistencias) {
			if(r.isCarga() && rCarga==null) rCarga=r;
			else if(!r.isCarga() && rThev==null) rThev=r;
		}
		if(rThev==null && !resistencias.isEmpty()) {
			rThev=resistencias.get(0);
			if(rThev==rCarga) rCarga=null;
		}
		if(rThev!=null) rThev.setValor(resultado.getRth());
		if(numCargas>1) {
			logger.info("Carga compleja detectada ("+numCargas+" componentes). Se dibujarán bornes abiertos.");
			rCarga=null;
		}
		int fuenteRot=(resultado.getVth()<0 || nodoNeg>nodoPos) ? 180 : 0;
		int rthevRot=(rThev.getValor()<0) ? 180 : 0;
		int rcargaRot=(rCarga!=null && rCarga.getValor()<0) ? 90 : 270;
		renderer.setOrientacionPines(
				new CircuitoEquivalenteRenderer.PinesComponente(
						Posicion.ARRIBA, Posicion.ABAJO), 
				new CircuitoEquivalenteRenderer.PinesComponente(
						Posicion.IZQUIERDA, Posicion.DERECHA),
				new CircuitoEquivalenteRenderer.PinesComponente(
						Posicion.ARRIBA, Posicion.ABAJO));
		try {
			if(rCarga!=null) {
				renderer.renderizarThevenin(fuente, rThev, rCarga, 
						new CircuitoEquivalenteRenderer.VisualPos(FUENTE_X, FUENTE_Y, fuenteRot),
						new CircuitoEquivalenteRenderer.VisualPos(RTHEV_X, RTHEV_Y, rthevRot), 
						new CircuitoEquivalenteRenderer.VisualPos(RCARGA_X, RCARGA_Y, rcargaRot));
			} else {
				//Solo fuente + Rth, bornes abiertos
				renderer.renderizarSinCarga(fuente, rThev,
						new CircuitoEquivalenteRenderer.VisualPos(FUENTE_X, FUENTE_Y, fuenteRot),
						new CircuitoEquivalenteRenderer.VisualPos(RTHEV_X, RTHEV_Y, rthevRot));
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Error al renderizar el circuito equivalente de Thevenin", e);
		}
	}
}
