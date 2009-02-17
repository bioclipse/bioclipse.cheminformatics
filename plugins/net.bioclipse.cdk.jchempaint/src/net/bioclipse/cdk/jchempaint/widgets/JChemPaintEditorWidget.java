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

import static net.bioclipse.cdk.jchempaint.outline.StructureContentProvider.createCDKChemObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.bioclipse.cdk.domain.CDKChemObject;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.Activator;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintGlobalPropertiesManager;
import net.bioclipse.cdk.jchempaint.editor.SWTMouseEventRelay;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;
import net.bioclipse.core.business.BioclipseException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.SafeRunner;
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
import org.openscience.cdk.controller.IChemModelEventRelayHandler;
import org.openscience.cdk.controller.IControllerModule;
import org.openscience.cdk.controller.IViewEventRelay;
import org.openscience.cdk.controller.MoveModule;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.renderer.generators.ExternalHighlightGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.SelectionGenerator;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;


public class JChemPaintEditorWidget extends JChemPaintWidget  implements ISelectionProvider {
    private final static StructureDiagramGenerator sdg = new
                                                    StructureDiagramGenerator();
    Collection<ISelectionChangedListener> listeners =
                                    new ArrayList<ISelectionChangedListener>();

    ISelection theSelection = StructuredSelection.EMPTY;
    IAtom prevHighlightedAtom;
    IBond prevHighlightedBond;

    ICDKMolecule model;

    ControllerHub hub;
    ControllerModel c2dm;
    SWTMouseEventRelay relay;
    boolean generated = false;

    boolean isdirty = false;

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

      hub.setEventHandler( new IChemModelEventRelayHandler() {

        public void coordinatesChanged() {
            setDirty( true );
        }

        public void selectionChanged() {
            setSelection( getSelection() );
        }

        public void structureChanged() {
            setDirty( true );
        }

        public void structurePropertiesChanged() {
            setDirty( true );
        }

      });

      if(relay != null) {

          removeMouseListener( relay );
          removeMouseMoveListener( relay );
          removeListener( SWT.MouseEnter, (Listener)relay );
          removeListener( SWT.MouseExit, (Listener)relay );
      }
    	relay = new SWTMouseEventRelay(hub);
    	hub.setActiveDrawModule( new MoveModule(hub) );

        // apply the global JCP properties
        IJChemPaintGlobalPropertiesManager jcpprop =
            Activator.getDefault().getJCPPropManager();
        try {
           jcpprop.applyProperties(hub.getRenderer().getRenderer2DModel());
        } catch (BioclipseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    	addMouseListener(relay);
    	addMouseMoveListener(relay);
    	addListener(SWT.MouseEnter, relay);
    	addListener(SWT.MouseExit, relay);

    }

    @Override
    protected List<IGenerator> createGenerators() {
        List<IGenerator> generatorList = new ArrayList<IGenerator>();
        generatorList.add(new ExternalHighlightGenerator());
        generatorList.addAll( super.createGenerators() );
        generatorList.add(new SelectionGenerator());

        return generatorList;
    }

    @Override
    public void setAtomContainer( IAtomContainer atomContainer ) {

        if( atomContainer != null) {
            generated = false;
            if(atomContainer.getAtomCount() > 0 &&
               !GeometryTools.has2DCoordinates( atomContainer )) {
                atomContainer = generate2Dfrom3D( atomContainer );
                generated = true;
                setDirty( true );
            }
        } else {
        	atomContainer = NoNotificationChemObjectBuilder.getInstance()
        	    .newAtomContainer();
        }
        setupControllerHub( atomContainer );
        super.setAtomContainer( atomContainer );
        setDirty( false );
    }

    public void setInput( Object element ) {

        if(element instanceof IAdaptable) {
            ICDKMolecule molecule = (ICDKMolecule)((IAdaptable)element)
            .getAdapter( ICDKMolecule.class );
            if(molecule != null ) {
                model = molecule;
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
        	System.out.println("Could not create 3D coordinates: " + e.getMessage());
            return atomContainer;
        } catch (Exception e) {
        	System.out.println("Could not create 3D coordinates: " + e.getMessage());
            return atomContainer;
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
        if (this.getRenderer2DModel() == null && model != null)
            return new StructuredSelection(model);
        List<CDKChemObject> selection = new LinkedList<CDKChemObject>();

        //selection.add( atomContainer );

        IAtom highlightedAtom = this.getRenderer2DModel().getHighlightedAtom();
        IBond highlightedBond = this.getRenderer2DModel().getHighlightedBond();
        if(highlightedBond != null)
            selection.add(createCDKChemObject( highlightedBond ));
        if(highlightedAtom != null)
            selection.add( createCDKChemObject( highlightedAtom ) );

        IChemObjectSelection sel = getRenderer2DModel().getSelection();
        IAtomContainer modelSelection = sel.getConnectedAtomContainer();
        if(modelSelection != null) {
            for(IAtom atom:modelSelection.atoms()) {
                selection.add(createCDKChemObject( atom ));
            }
            for(IBond bond:modelSelection.bonds()) {
                selection.add(createCDKChemObject( bond ));
            }
        }
        if (selection.size() == 0 && model != null) {
            return new StructuredSelection(model);
        }
        return new StructuredSelection(selection);
    }

    public void removeSelectionChangedListener(
                                          ISelectionChangedListener listener ) {

        listeners.remove( listener );

    }

    public void setSelection( ISelection selection ) {


        theSelection = selection;
        final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
        Object[] listenersArray = listeners.toArray();

        for (int i = 0; i < listenersArray.length; i++) {
            final ISelectionChangedListener l = (ISelectionChangedListener)
                                                              listenersArray[i];
            SafeRunner.run(new SafeRunnable() {
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
            if(prevHighlightedAtom!=null) {
                setToolTipText( rendererModel
                                       .getToolTipText( prevHighlightedAtom ) );
            } else if(prevHighlightedBond != null){
                setToolTipText( null ); // put getToolTipText(prevHighlightedBond) here
            } else {
                setToolTipText( "" );
            }
            setSelection( getSelection() );
        }

    }

	public void setActiveDrawModule(IControllerModule activeDrawModule){
		hub.setActiveDrawModule(activeDrawModule);
	}

	public void setDirty( boolean dirty) {
	    this.isdirty = dirty;
	}

	public boolean getDirty() {
	    return isdirty;
	}

}