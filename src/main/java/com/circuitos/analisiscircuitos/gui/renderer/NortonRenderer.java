package com.circuitos.analisiscircuitos.gui.renderer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.analisis.ResultadoNorton;
import com.circuitos.analisiscircuitos.dominio.Circuito;
import com.circuitos.analisiscircuitos.dominio.Componente;
import com.circuitos.analisiscircuitos.dominio.FuenteCorrienteInd;
import com.circuitos.analisiscircuitos.dominio.Norton;
import com.circuitos.analisiscircuitos.dominio.Resistencia;
import com.circuitos.analisiscircuitos.gui.model.PuntoConexion.Posicion;

/**
 * Clase específica que define cómo se representa visualmente el circuito equivalente de Norton. 
 * Usa la clase {@link CircuitoEquivalenteRenderer} para renderizado.
 * 
 * @author Marco Antonio Garzón Palos
 * @version 1.0
 */
public class NortonRenderer {
	private static final Logger logger=Logger.getLogger(NortonRenderer.class.getName());
	
	private final int FUENTE_X=150, FUENTE_Y=350;
	private final int RNORTON_X=250, RNORTON_Y=350;
	private final int RCARGA_X=450, RCARGA_Y=350;
	
	private final CircuitoEquivalenteRenderer renderer;
	
	public NortonRenderer(CircuitoEquivalenteRenderer renderer) {
		this.renderer=renderer;
	}
	
	/**
	 * Renderiza un circuito en su equivalente de Norton.
	 * 
	 * @param original			Circuito original
	 * @param nodoNeg			Nodo negativo de control
	 * @param nodoPos			Nodo positivo de control
	 * @param resultado			Resultado Norton (In, Rn)
	 */
	public void renderizarNo(Circuito original, int nodoNeg, int nodoPos, ResultadoNorton resultado) {
		logger.info("Calculando circuito equivalente de Norton...");
		long numCargas=original.getComponentes().stream()
				.filter(Componente::isCarga)
				.count();
		Norton norton=new Norton(original, nodoNeg, nodoPos);
		Circuito cNorton=norton.getNorton();
		
		Componente fuente=cNorton.getComponentes().stream()
				.filter(c -> c instanceof FuenteCorrienteInd)
				.findFirst()
				.orElse(null);
		List<Resistencia> resistencias=cNorton.getComponentes().stream()
				.filter(c -> c instanceof Resistencia)
				.map(c -> (Resistencia) c)
				.toList();	
		if(fuente==null || resistencias.isEmpty()) {
			logger.warning("Faltan componentes para construir el equivalente de Norton");
			return;
		}
		if(fuente instanceof FuenteCorrienteInd fci) fci.setValor(resultado.getIn());
		Resistencia rNorton=null;
		Resistencia rCarga=null;
		for(Resistencia r : resistencias) {
			if(r.isCarga() && rCarga==null) rCarga=r;
			else if(!r.isCarga() && rNorton==null) rNorton=r;
		}
		if(rNorton==null) {
			rNorton=resistencias.get(0);
			if(rNorton==rCarga) rCarga=null;
		}
		if(rNorton!=null) rNorton.setValor(resultado.getRn());
		if(numCargas>1) rCarga=null;
		int fuenteRot=(resultado.getIn()<0 || nodoNeg>nodoPos) ? 180: 0;
		int rRot=90;
		int rCargaRot=90;
		renderer.setOrientacionPines(
				new CircuitoEquivalenteRenderer.PinesComponente(
						Posicion.ARRIBA,	 Posicion.ABAJO),
				new CircuitoEquivalenteRenderer.PinesComponente(
						Posicion.ARRIBA, Posicion.ABAJO),
				new CircuitoEquivalenteRenderer.PinesComponente(
						Posicion.ARRIBA, Posicion.ABAJO));
		try {
			if(rCarga!=null) {
				renderer.renderizarNorton(fuente, rNorton, rCarga, 
						new CircuitoEquivalenteRenderer.VisualPos(FUENTE_X, FUENTE_Y, fuenteRot), 
						new CircuitoEquivalenteRenderer.VisualPos(RNORTON_X, RNORTON_Y, rRot), 
						new CircuitoEquivalenteRenderer.VisualPos(RCARGA_X, RCARGA_Y, rCargaRot));
			} else {
				renderer.renderizarNortonSinCarga(fuente, rNorton,
						new CircuitoEquivalenteRenderer.VisualPos(FUENTE_X, FUENTE_Y, fuenteRot),
						new CircuitoEquivalenteRenderer.VisualPos(RNORTON_X, RNORTON_Y, rRot));
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Error al renderizar el circuito equivalente de Norton");
		}
	}
}
