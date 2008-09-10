/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 * 
 * Contributors:
 *      Arvid Berg 
 *     
 *     
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.widgets;

import java.awt.Dimension;

import net.bioclipse.cdk.jchempaint.editor.SWTMosueEventRelay;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.openscience.cdk.controller.Controller2DHub;
import org.openscience.cdk.controller.Controller2DModel;
import org.openscience.cdk.controller.IViewEventRelay;
import org.openscience.cdk.controller.IController2DModel.DrawMode;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;


public class JChemPaintEditorWidget extends JChemPaintSWTWidget{

    Controller2DHub hub;
    Controller2DModel c2dm;
    SWTMosueEventRelay relay;    
    
    public JChemPaintEditorWidget(Composite parent, int style) {
        
        super( parent, style );        
    }

    private void setupControllerHub( IAtomContainer atomContainer ) {
        JChemPaintEditorWidget widget = this;
        hub = new Controller2DHub(
                            c2dm=new Controller2DModel(), widget.getRenderer(),
                            ChemModelManipulator.newChemModel(atomContainer),
                            new IViewEventRelay(){
                                public void updateView() {
                                    JChemPaintEditorWidget.this.redraw();
                                }
                            } );
    
      if(relay != null) {
          widget.removeMouseListener( relay );
          widget.removeMouseMoveListener( relay );
          widget.removeListener( SWT.MouseEnter, (Listener)relay );
          widget.removeListener( SWT.MouseExit, (Listener)relay );
      }
    	relay = new SWTMosueEventRelay(hub);
    	c2dm.setDrawMode(DrawMode.MOVE);
    	widget.getRendererModel().setHighlightRadiusModel(20);
    	
    	widget.addMouseListener(relay);
    	widget.addMouseMoveListener(relay);
    	widget.addListener(SWT.MouseEnter, relay);
    	widget.addListener(SWT.MouseExit, relay);
    	
    }
    
    public void setInput(IAtomContainer atomContainer){
        this.setAtomContainer( atomContainer );        
        setupControllerHub( atomContainer );
        if(atomContainer != null) {
            Dimension newSize=new Dimension(this.getSize().x,this.getSize().y);
            updateOnReize( newSize );
            setCompactedNess( newSize );
        }        
    }
}
