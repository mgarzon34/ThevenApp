module com.circuitos.AnalisisCircuitos {
	requires transitive javafx.controls;
	requires javafx.fxml;
	requires java.logging;
	requires commons.math3;
	requires transitive javafx.base;
	requires transitive javafx.graphics;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires java.desktop;
	requires org.controlsfx.controls;
	requires java.sql;
	requires javafx.web;
	requires com.fasterxml.jackson.core;
	
	opens com.circuitos.analisiscircuitos.gui to javafx.graphics, javafx.fxml;
	opens com.circuitos.analisiscircuitos.gui.controller to javafx.fxml;
	opens com.circuitos.analisiscircuitos.dominio to com.fasterxml.jackson.databind;
	opens com.circuitos.analisiscircuitos.dto to com.fasterxml.jackson.databind;
	
	exports com.circuitos.analisiscircuitos.gui.controller;
	exports com.circuitos.analisiscircuitos.dominio;
	exports com.circuitos.analisiscircuitos.gui.model;
	exports com.circuitos.analisiscircuitos.gui.util;
	exports com.circuitos.analisiscircuitos.dto;
	exports com.circuitos.analisiscircuitos.gui.service.cable;
	exports com.circuitos.analisiscircuitos.gui.service.undo;
	exports com.circuitos.analisiscircuitos.gui.service.nodes;
	exports com.circuitos.analisiscircuitos.gui.commands;
	exports com.circuitos.analisiscircuitos.gui.learning.model;
}