package com.circuitos.analisiscircuitos.gui.commands;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import java.util.List;

import com.circuitos.analisiscircuitos.gui.model.Cable;
import com.circuitos.analisiscircuitos.gui.service.undo.DescripcionesAccion;

public class RemoveCableCommand implements Command {
    private final Cable cable;
    private final Pane zonaDibujo;
    private final List<Cable> cablesCircuito;
    private final String descripcion;
    private int oldPaneIndex=-1;
    private int oldListIndex=-1;
    private boolean ejecutado=false;

    public RemoveCableCommand(Cable cable, Pane zonaDibujo, List<Cable> cablesCircuito) {
        this.cable=cable;
        this.zonaDibujo=zonaDibujo;
        this.cablesCircuito=cablesCircuito;
        this.descripcion=DescripcionesAccion.eliminar(cable);
    }
    
    /**
     * Restringe un valor entero entre [min, max]
     */
    private static int clamp(int val, int min, int max) {
    	if(val<min) return min;
    	if(val>max) return max;
    	return val;
    }

    /* Implementación métodos de la interfaz */
    
    @Override
    public void ejecutar() {
    	if(ejecutado || cable==null || zonaDibujo==null || cablesCircuito==null) return;
    	oldPaneIndex=zonaDibujo.getChildren().indexOf(cable);
    	oldListIndex=cablesCircuito.indexOf(cable);
    	if(oldPaneIndex>=0) {
    		zonaDibujo.getChildren().remove(oldPaneIndex);
    	}
    	if(oldListIndex>=0) {
    		cablesCircuito.remove(oldListIndex);
    	}
    	ejecutado=true;
    }

    @Override
    public void deshacer() {
    	if(!ejecutado || cable==null || zonaDibujo==null || cablesCircuito==null) return;
    	Parent parent=cable.getParent();
    	if(parent instanceof Pane p && p!=zonaDibujo) {
    		p.getChildren().remove(cable);
    	}
    	int li=(oldListIndex<0) ? cablesCircuito.size() :
    		clamp(oldListIndex, 0, cablesCircuito.size());
    	if(!cablesCircuito.contains(cable)) {
    		cablesCircuito.add(li, cable);
    	}
    	int pi=(oldPaneIndex<0) ? zonaDibujo.getChildren().size()
    			: clamp(oldPaneIndex, 0, zonaDibujo.getChildren().size());
    	if(!zonaDibujo.getChildren().contains(cable)) {
    		zonaDibujo.getChildren().add(pi, cable);
    	}
    	ejecutado=false;
    }

    @Override
    public String getDescripcion() {
    	return descripcion;
    }
    
    @Override
    public boolean esValido() {
    	return cable!=null && zonaDibujo!=null && cablesCircuito!=null;
    }
}