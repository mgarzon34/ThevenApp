package com.circuitos.analisiscircuitos.dominio.util;

import java.text.DecimalFormat;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase para formatear unidades y hacer lectura de unidades formateadas con multiplicadores o divisores.
 * 
 * @author 	Marco Antonio Garzon Palos
 * @version 1.0 (2025)
 */
public class Unidades {
	
	private record Prefix(String simbolo, double factor) {}
	
	/**
	 * Enumerado con tipos de magnitudes eléctricas.
	 */
	public enum Type { RESISTENCIA, CORRIENTE, TENSION }
	
	/**
	 * Constructor no instanciable.
	 */
	private Unidades() { /* No instanciable */ }
	
	
	private static final List<Prefix> PREFIX_LIST=List.of(
		new Prefix("G", 1e9),
		new Prefix("M", 1e6),
		new Prefix("k", 1e3),
		new Prefix("", 1.0),
		new Prefix("m", 1e-3),
		new Prefix("µ", 1e-6),
		new Prefix("n",  1e-9)
	);
	//Mapa de prefijos y factores de conversión
	private static final NavigableMap<String, Double> SUFFIX_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	static {
		for(Prefix p : PREFIX_LIST) {
			SUFFIX_MAP.put(p.simbolo(), p.factor());
		}
	}
	private static final ThreadLocal<DecimalFormat> DEC_FMT=
			ThreadLocal.withInitial(()->new DecimalFormat("0.###"));
	private static final ThreadLocal<DecimalFormat> SCI_FMT=
			ThreadLocal.withInitial(()->new DecimalFormat("0.###E0"));
	private static final Pattern PARSE_REGEX=
			Pattern.compile("^([-+]?\\d*\\.?\\d+)([GMkmun]?)$");
	
	/**
	 * Parsea un string con sufijo y devuelve su valor en double.
	 * 
	 * @param text							texto a parsear
	 * @throws IllegalArgumentException		si el número es no válido
	 * @throws IllegalArgumentException		si el prefijo es desconocido
	 * @return valor 						valor parseado (double)
	 */
	public static double parsear(String text) {
		Objects.requireNonNull(text, "Texto no puede ser null");
		String trimmed=text.trim();
		if(trimmed.isBlank()) throw new IllegalArgumentException("Texto no puede estar vacío");
		Matcher m=PARSE_REGEX.matcher(trimmed);
		if(!m.matches()) {
			throw new IllegalArgumentException("Formato inválido: "+trimmed);
		}
		double valor;
		try {
			valor=Double.parseDouble(m.group(1));
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Número no válido: "+m.group(1), e);
		}
		String suf=m.group(2);
		Double factor=SUFFIX_MAP.get(suf);
		if(factor==null) {
			throw new IllegalArgumentException("Prefijo desconocido: "+suf);
		}
		return valor*factor;
	}
	
	/**
	 * Formatea un valor base de resistencia, corriente o tensión a string con el prefijo 
	 * del Sistema Internacional apropiado.
	 * 
	 * @param valorBase		valor que se va a formatear
	 * @param tipo			tipo de valor (resistencia, tensión o corriente)
	 * @return num			valor formateado
	 */
	public static String format(double valorBase, Type tipo) {
		double abs=Math.abs(valorBase);
		for(Prefix p : PREFIX_LIST) {
			double scaled=abs/p.factor();
			if(scaled>=1.0 && scaled<1000.0) {
				//Redondear a 3 dígitos significativos
				String num=DEC_FMT.get().format(valorBase/p.factor());
				String unidad;
				switch(tipo) {
					case RESISTENCIA -> unidad="Ω";
					case CORRIENTE -> unidad="A";
					case TENSION -> unidad="V";
					default -> unidad="";
				}
				return num + " " + p.simbolo() + unidad;
			}
		}
		return SCI_FMT.get().format(valorBase);
	}
}
