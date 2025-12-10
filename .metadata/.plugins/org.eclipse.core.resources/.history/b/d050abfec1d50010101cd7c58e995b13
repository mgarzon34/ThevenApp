package com.circuitos.analisiscircuitos.demo;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.circuitos.analisiscircuitos.analisis.Analizador;
import com.circuitos.analisiscircuitos.analisis.ResultadoNorton;
import com.circuitos.analisiscircuitos.analisis.ResultadoThevenin;
import com.circuitos.analisiscircuitos.dominio.Circuito;
import com.circuitos.analisiscircuitos.dominio.FuenteCorrienteInd;
import com.circuitos.analisiscircuitos.dominio.FuenteTensionDependiente;
import com.circuitos.analisiscircuitos.dominio.Norton;
import com.circuitos.analisiscircuitos.dominio.Resistencia;
import com.circuitos.analisiscircuitos.dominio.Thevenin;
import com.circuitos.analisiscircuitos.dominio.FuenteDependiente.ControlType;
import com.circuitos.analisiscircuitos.dominio.util.CircuitPrinter;
import com.circuitos.analisiscircuitos.dominio.util.Unidades.Type;


//CCVS Fuente de tensión controlada por corriente Vth=24V, In=4mA, Rth=6kΩ
public class Lanzador8 {
	public static void main(String[] args) {
		Circuito circuito=new Circuito();
		Logger root=Logger.getLogger("");
		root.setLevel(Level.FINE);
		for(Handler h:root.getHandlers()) {
			h.setLevel(Level.FINE);
		}
		circuito.addComponente(new FuenteTensionDependiente(4000, 1, 0, ControlType.CORRIENTE, 2, 1));
		circuito.addComponente(new Resistencia("10k", 0, 1));
		circuito.addComponente(new Resistencia("10k", 1, 2));
		circuito.addComponente(new FuenteCorrienteInd("4m", 0, 2));
		circuito.addComponente(new Resistencia("10k", 0, 2, true));
		int nodoA=0, nodoB=2;
		Analizador analizador=new Analizador();
		ResultadoThevenin resTh=analizador.calculaThevenin(circuito, nodoA, nodoB);
		Circuito thevEquiv=new Thevenin(circuito, nodoA, nodoB).getEquivalenteTheveninGrafico();
		ResultadoNorton resNo=analizador.calculaNorton(circuito, nodoA, nodoB);
		Circuito nortEquiv=new Norton(circuito, nodoA, nodoB).getNorton();
		thevEquiv.showCircuito();
		nortEquiv.showCircuito();
		System.out.println("\n\nCircuito original.\n");
		CircuitPrinter.printCircuit(circuito);
		System.out.println("\nEquivalene de Thevenin:\n");
		System.out.println("Vth calculado: "+circuito.formatearValor(resTh.getVth(), Type.TENSION));
		System.out.println("Rth calculado: "+circuito.formatearValor(resTh.getRth(), Type.RESISTENCIA));
		System.out.println();
		CircuitPrinter.printCircuit(thevEquiv);
		
		System.out.println("\nEquivalente de Norton:");
		System.out.println("In="+circuito.formatearValor(resNo.getIn(), Type.CORRIENTE));
		System.out.println("Rn="+circuito.formatearValor(resNo.getRn(), Type.RESISTENCIA));
		CircuitPrinter.printCircuit(nortEquiv);
	}
}
