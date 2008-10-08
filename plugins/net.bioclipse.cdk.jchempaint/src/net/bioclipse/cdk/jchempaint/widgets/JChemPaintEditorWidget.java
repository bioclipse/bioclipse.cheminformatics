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

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.SWTMosueEventRelay;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.openscience.cdk.controller.Controller2DHub;
import org.openscience.cdk.controller.Controller2DModel;
import org.openscience.cdk.controller.IViewEventRelay;
import org.openscience.cdk.controller.IController2DModel.DrawMode;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

import com.sun.tools.javac.tree.Tree.Throw;


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
    	    	
    	widget.addMouseListener(relay);
    	widget.addMouseMoveListener(relay);
    	widget.addListener(SWT.MouseEnter, relay);
    	widget.addListener(SWT.MouseExit, relay);
    	
    }
    
    public void setInput(IAtomContainer atomContainer){
        assert(atomContainer != null);
        generated = false;
        if(!GeometryTools.has2DCoordinates( atomContainer )) {
            if(!GeometryTools.has3DCoordinates( atomContainer )) {
                throw new IllegalArgumentException(
                "The structure has no 2D- or 3D-cooridnates.");
            }
            atomContainer = SWTRenderer.generate2Dfrom3D( atomContainer ); 
            generated = true;
        }        
        super.setInput( atomContainer );        
        setupControllerHub( atomContainer );
        if(atomContainer != null) {
            Dimension newSize=new Dimension(this.getSize().x,this.getSize().y);
            updateOnReize( newSize );
            setCompactedNess( newSize );
        }        
    }
    
    public void setInput( Object element ) {

        if(element instanceof IAdaptable) {
            ICDKMolecule molecule = (ICDKMolecule)((IAdaptable)element)
            .getAdapter( ICDKMolecule.class );
            if(molecule != null && molecule.getAtomContainer() != null) {
                // TODO : if null change input to what?
                this.setInput(molecule.getAtomContainer());
                this.redraw();
                // FIXME : update / change hubs chemmodel
            }
        }
    }
}
