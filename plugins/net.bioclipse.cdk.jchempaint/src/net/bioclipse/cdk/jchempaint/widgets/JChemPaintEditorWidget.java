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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.SWTMosueEventRelay;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;

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
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.IJava2DRenderer;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;


public class JChemPaintEditorWidget extends JChemPaintWidget{
    private final static StructureDiagramGenerator sdg = new 
                                                    StructureDiagramGenerator();

    Controller2DHub hub;
    Controller2DModel c2dm;
    SWTMosueEventRelay relay;    
     boolean generated = false;
    
    public JChemPaintEditorWidget(Composite parent, int style) {
        
        super( parent, style );        
    }

    private void setupControllerHub( IAtomContainer atomContainer ) {
        
        hub = new Controller2DHub( c2dm = new Controller2DModel(),
                                   new IJava2DRenderer() {

            public Point2d getCoorFromScreen( int screenX,
                                              int screenY ) {
                return JChemPaintEditorWidget.this.getCoorFromScreen( screenX, 
                                                                      screenY );
            }

            public Renderer2DModel getRenderer2DModel() {

                return JChemPaintEditorWidget.this.getRenderer2DModel();
            }

            public void paintMolecule( IAtomContainer atomCon,
                                       Graphics2D graphics ) {

            }

            public void paintMolecule( IAtomContainer atomCon,
                                       Graphics2D graphics,
                                       Rectangle2D bounds ) {
            }

            public void setRenderer2DModel( Renderer2DModel model ) {

            }

        }
                                   ,
                                   ChemModelManipulator
                                   .newChemModel( atomContainer ),
                                   new IViewEventRelay() {

            public void updateView() {

                JChemPaintEditorWidget.this.redraw();
            }
        } );

      if(relay != null) {
          
          removeMouseListener( relay );
          removeMouseMoveListener( relay );
          removeListener( SWT.MouseEnter, (Listener)relay );
          removeListener( SWT.MouseExit, (Listener)relay );
      }
    	relay = new SWTMosueEventRelay(hub);
    	c2dm.setDrawMode(DrawMode.MOVE);
    	    	
    	addMouseListener(relay);
    	addMouseMoveListener(relay);
    	addListener(SWT.MouseEnter, relay);
    	addListener(SWT.MouseExit, relay);
    	
    }
    @Override
    public void setAtomContainer( IAtomContainer atomContainer ) {

        if( atomContainer != null) {
            generated = false;
            if(!GeometryTools.has2DCoordinates( atomContainer )) {
                
                atomContainer = generate2Dfrom3D( atomContainer ); 
                generated = true;
            }        
        }
        setupControllerHub( atomContainer );
        super.setAtomContainer( atomContainer );        
                        
    }
    
    public void setInput( Object element ) {

        if(element instanceof IAdaptable) {
            ICDKMolecule molecule = (ICDKMolecule)((IAdaptable)element)
            .getAdapter( ICDKMolecule.class );
            if(molecule != null ) {
                setAtomContainer( molecule.getAtomContainer());                
            }
            else setAtomContainer( null );
        }
    }
    /*
     * Utility method for copying 3D x,y to 2D coordinates 
     */
    public static IAtomContainer generate2Dfrom3D( IAtomContainer atomContainer ) {

        IAtomContainer container = null;
        try {
            sdg.setMolecule( (IMolecule) atomContainer.clone() );
            sdg.generateCoordinates();
            // sdg.getMolecule();
            container = sdg.getMolecule();
        } catch ( CloneNotSupportedException e ) {
            return null;
        } catch ( Exception e ) {
            return null;
        }
        return container;

    }

    public Controller2DHub getController2DHub() {
        return hub;
    }

}
