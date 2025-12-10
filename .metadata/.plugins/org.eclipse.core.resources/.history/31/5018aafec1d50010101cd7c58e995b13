package com.circuitos.analisiscircuitos.demo;
import com.circuitos.analisiscircuitos.analisis.Analizador;
import com.circuitos.analisiscircuitos.analisis.ResultadoNorton;
import com.circuitos.analisiscircuitos.analisis.ResultadoThevenin;
import com.circuitos.analisiscircuitos.dominio.Circuito;
import com.circuitos.analisiscircuitos.dominio.FuenteTensionInd;
import com.circuitos.analisiscircuitos.dominio.Norton;
import com.circuitos.analisiscircuitos.dominio.Resistencia;
import com.circuitos.analisiscircuitos.dominio.Thevenin;
import com.circuitos.analisiscircuitos.dominio.util.CircuitPrinter;
import com.circuitos.analisiscircuitos.dominio.util.Unidades.Type;

public class Lanzador7 {
	public static void main(String[] args) {
		Circuito circuito=new Circuito();
		
		//In=5mA, Rn=36/17kΩ=2,118kΩ
		circuito.addComponente(new FuenteTensionInd(12, 0, 1));
		circuito.addComponente(new Resistencia("6k", 1, 2));
		circuito.addComponente(new Resistencia("2k", 0, 2));
		circuito.addComponente(new Resistencia("3k", 2, 3));
		circuito.addComponente(new Resistencia("4k", 0, 3));
		circuito.addComponente(new Resistencia("2k", 1, 3, true)); //Resistencia de carga
		int nodoA=1, nodoB=3;
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