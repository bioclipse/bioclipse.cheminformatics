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

import java.util.ArrayList;
import java.util.Collection;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.SWTMosueEventRelay;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.ControllerModel;
import org.openscience.cdk.controller.IViewEventRelay;
import org.openscience.cdk.controller.IControllerModel.DrawMode;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;


public class JChemPaintEditorWidget extends JChemPaintWidget  implements ISelectionProvider {
    private final static StructureDiagramGenerator sdg = new
                                                    StructureDiagramGenerator();
    Collection<ISelectionChangedListener> listeners =
                                    new ArrayList<ISelectionChangedListener>();

    ISelection theSelection = StructuredSelection.EMPTY;
    IAtom prevHighlightedAtom;
    IBond prevHighlightedBond;

    ControllerHub hub;
    ControllerModel c2dm;
    SWTMosueEventRelay relay;
     boolean generated = false;

    public JChemPaintEditorWidget(Composite parent, int style) {
        super( parent, style );
        prevHighlightedAtom=null;
        prevHighlightedBond=null;
    }

    private void setupControllerHub( IAtomContainer atomContainer ) {

        hub = new ControllerHub( c2dm = new ControllerModel(),
                                  getRenderer(),
                                   ChemModelManipulator
                                   .newChemModel( atomContainer ),
                                   new IViewEventRelay() {

            public void updateView() {
                updateSelection();
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
    	// Arvid, please FIXME
//    	c2dm.setDrawMode(DrawMode.MOVE); 

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

    public ControllerHub getControllerHub() {
        return hub;
    }

    public void addSelectionChangedListener( ISelectionChangedListener listener ) {

        listeners.add( listener );

    }

    public ISelection getSelection() {
        if(this.getRenderer2DModel()== null)
            return StructuredSelection.EMPTY;
        Object element = null;
        Object atom = this.getRenderer2DModel().getHighlightedAtom();
        Object bond = this.getRenderer2DModel().getHighlightedBond();
        if(bond != null)
            element = bond;
        if(atom != null)
            element = atom;
        if(element == null)
            return StructuredSelection.EMPTY;
        else
            return new StructuredSelection(element);
    }

    public void removeSelectionChangedListener(
                                          ISelectionChangedListener listener ) {

        listeners.remove( listener );

    }

    @SuppressWarnings("deprecation")
    public void setSelection( ISelection selection ) {


        theSelection = selection;
        final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
        Object[] listenersArray = listeners.toArray();

        for (int i = 0; i < listenersArray.length; i++) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listenersArray[i];
            Platform.run(new SafeRunnable() {
                public void run() {
                    l.selectionChanged(e);
                }
            });
    }

    }

    private void updateSelection() {
        if( getRenderer2DModel().getHighlightedAtom() != prevHighlightedAtom ||
            getRenderer2DModel().getHighlightedBond() != prevHighlightedBond) {
            prevHighlightedAtom = getRenderer2DModel().getHighlightedAtom();
            prevHighlightedBond = getRenderer2DModel().getHighlightedBond();
            setSelection( getSelection() );
        }

    }
}