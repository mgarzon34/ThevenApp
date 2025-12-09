package com.circuitos.analisiscircuitos.dominio;

import javafx.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.linear.*;

import com.circuitos.analisiscircuitos.dominio.util.*;

/**
 * Clase que calcula el circuito equivalente de Thevenin de un circuito dado,
 * visto entre dos nodos (nodoA, nodoB).
 *  
 * @author 	Marco Antonio Garzon Palos
 * @version 1.0
 */
public class Thevenin {
	private static final Logger logger=Logger.getLogger(Thevenin.class.getName());
	private static final double I_TEST=1.0; //Corriente de prueba para cálculo de Rth
	private final Circuito circuito;
	private final int nodoA;
	private final int nodoB;
	
	/**
	 * Constructor. Crea el circuito equivalente al que se le añadirán componentes.
	 * 
	 * @param circuito	circuito equivalente de Thévenin
	 * @param nodoA		nodo terminal
	 * @param nodoB		nodo terminal
	 */
	public Thevenin(Circuito circuito, int nodoA, int nodoB) {
		this.circuito=Objects.requireNonNull(circuito, "Circuito no puede ser null");
		this.nodoA=nodoA;
		this.nodoB=nodoB;
	}
	
	/**
	 * Obtiene el circuito equivalente de Thevenin de un circuito dado.
	 * 
	 * @return circuito equivalente de Thevenin
	 */
	public Circuito getThevenin() {
		logger.log(Level.INFO, "Obteniendo circuito equivalente de Thevenin entre {0} y {1}", new Object[] {nodoA, nodoB});
		Pair<Double, Double> params=calcularParametros();
		return construirCircuitoThevenin(params.getKey(), params.getValue(), 0, 1, 0, 1);
	}
	
	/**
	 * Obtiene el circuito equivalente de Thevenin de un circuito dado.
	 * Ordena los nodos para que sea representable gráficamente (nodos 0, 1, 2).
	 * 
	 * @return circuito equivalente de Thevenin con nodos para rep. gráfica.
	 */
	public Circuito getEquivalenteTheveninGrafico() {
		logger.log(Level.INFO, "Obteniendo circuito equivalente de Thevenin gráfico entre {0} y {1}", new Object[] {nodoA, nodoB});
		Pair<Double, Double> params=calcularParametros();
		double vth=params.getKey();
		double rth=params.getValue();
		if(rth<=0.0) {
			logger.log(Level.WARNING, "Rth no positivo: "+rth);
		}
		return construirCircuitoThevenin(vth, rth, 0, 1, 1, 2);
	}
	
	/**
	 * Calcula Vth y Rth
	 * 
	 * @return par (Vth, Rth)
	 */
	public Pair<Double, Double> calcularParametros() {
		double vth=calcularVth();
		double rth=calcularRth();
		logger.log(Level.FINE, "Vth={0}, Rth={1}", new Object[] {vth, rth});
		return new Pair<>(vth, rth);
	}
	
	/**
	 * Calcula la tensión de Thevenin entre dos nodos de un circuito.
	 * 
	 * @return tension	tensión de Thevenin entre A y B
	 */
	private double calcularVth() {
		logger.log(Level.FINE, "Calculando Tensión Thevenin");
		Circuito circuitoAbierto=circuito.abrirCircuitoEntreNodos(nodoA, nodoB);
		Map<Integer, Integer> nodosInternos=circuitoAbierto.getNodos();
		double[] voltajes=MatrixUtil.resolverCircuitoNodal(circuitoAbierto);
		int indiceA=nodosInternos.get(nodoA);
		int indiceB=nodosInternos.get(nodoB);
		return voltajes[indiceA] - voltajes[indiceB];
	}
	
	/**
	 * Calcula la resistencia de Thevenin entre dos nodos de un circuito.
	 * 
	 * @throws IllegalStateException si, tras desactivar fuentes, no se encuentran los nodos terminales
	 * @throws IllegalStateException si no se pudo resolver el sistema nodal para cálculo de Rth
	 * @throws IllegalStateException si Rth es inválido (isNaN o isInfinite)
	 * @return resistencia	resistencia de Thevenin entre A y B
	 */
	private double calcularRth() {
		logger.log(Level.FINE, "Calculando Resistencia Thevenin");
		Circuito circuitoAbierto=circuito.abrirCircuitoEntreNodos(nodoA, nodoB);
		Circuito circuitoDesactivado=CircuitUtil.desactivarFuentes(circuitoAbierto);
		NodeMapUtil.actualizarMapaNodos(circuitoDesactivado.getNodos(), circuitoDesactivado.getComponentes());
		Map<Integer, Integer> mapaNodos=circuitoDesactivado.getNodos();
		Integer idxA=mapaNodos.get(nodoA);
		Integer idxB=mapaNodos.get(nodoB);
		if(idxA==null || idxB==null) {
			throw new IllegalStateException("No se encuentran los nodos A o B tras desactivar fuentes");
		}
		if(idxA.equals(idxB)) {
			return 0.0;
		}
		circuitoDesactivado.addComponente(new FuenteCorrienteInd(I_TEST, nodoA, nodoB));
		double[] voltajes;
		try {
			voltajes=MatrixUtil.resolverCircuitoNodal(circuitoDesactivado);
		} catch (SingularMatrixException e) {
			throw new IllegalStateException("No se pudo resolver sistema nodal para calcular Rth", e);
		}
		double rth=Math.abs(voltajes[idxA] - voltajes[idxB]);
		if(Double.isNaN(rth) || Double.isInfinite(rth)) {
			throw new IllegalStateException("Rth inválido: " +rth);
		}
		return rth;
	}
	
	/**
	 * Construye el nuevo circuito equivalente añadiendo Vth y Rth,
	 * y clonando los componentes de carga.
	 */
	private Circuito construirCircuitoThevenin(double vth, double rth,
				int srcVn, int dstVn, int srcCn, int dstCn) {
		Circuito circ=new Circuito();
		if(vth>=0) {
			circ.addComponente(new FuenteTensionInd(vth, srcVn, dstVn));
		} else {
			circ.addComponente(new FuenteTensionInd(-vth, dstVn, srcVn));
		}
		if(rth>0) {
			circ.addComponente(new Resistencia(rth, srcCn, dstCn));
		}
		List<Componente> cargas=CircuitUtil.obtenerComponentesCarga(circuito, nodoA, nodoB);
		List<Resistencia> cargasResis=cargas.stream()
				.filter(c -> c instanceof Resistencia)
				.map(c -> (Resistencia) c)
				.toList();
		if(cargasResis.size()==1) {
			Resistencia carga=cargasResis.get(0);
			circ.addComponente(carga.clonarConNuevosNodos(srcCn, dstCn));
		} else if(!cargas.isEmpty()) {
			logger.log(Level.INFO, "Se han encontrado {0} componentes de carga entre {1} y {2}, "
					+ "pero no exactamente una resistencia. El equivalente "
					+" gráfico de Thevenin se mostrará sin rama de carga.",
					new Object[] { cargas.size(), nodoA, nodoB });
		}
		return circ;
	}
}