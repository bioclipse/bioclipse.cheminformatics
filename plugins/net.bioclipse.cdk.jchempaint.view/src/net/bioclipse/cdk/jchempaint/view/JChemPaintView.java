/*******************************************************************************
 * Copyright (c) 2005-2005-2007-2009 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Kuhn <shk3@users.sf.net> - original implementation
 *     Carl <carl_marak@users.sf.net>  - converted into table
 *     Ola Spjuth                      - minor fixes
 *     Egon Willighagen                - adapted for the new renderer from CDK
 *     Arvid <goglepox@users.sf.net>   - adapted to SWT renderer
 *******************************************************************************/
package net.bioclipse.cdk.jchempaint.view;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.AtomIndexSelection;
import net.bioclipse.core.domain.IChemicalSelection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;

/**
 * 2D Rendering widget using the new SWT based JChemPaint renderer.
 */
public class JChemPaintView extends ViewPart
    implements ISelectionListener {

    private static final Logger logger = Logger.getLogger(JChemPaintView.class);

    private JChemPaintWidget canvasView;
    private IMolecule molecule;
    private final static StructureDiagramGenerator sdg = new StructureDiagramGenerator();
    private IPartListener2 partListener;
    private ICDKManager cdk;

    /**
     * The constructor.
     */
    public JChemPaintView() {

        cdk = net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        canvasView = new JChemPaintWidget(parent, SWT.NONE );
        canvasView.setSize( 200, 200 );

        // Register this page as a listener for selections
        getViewSite().getPage().addSelectionListener(this);

        //See what's currently selected
        ISelection selection=PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getSelectionService().getSelection();
        reactOnSelection(selection);

        partListener = new IPartListener2() {

            public void partVisible( IWorkbenchPartReference partRef ) {

                IWorkbenchPart part = partRef.getPart( false );
                IEditorPart editorPart = null;
                if ( part instanceof JChemPaintView ) {
                    editorPart = partRef.getPage().getActiveEditor();

                }

                if ( part instanceof IEditorPart ) {
                    editorPart = (IEditorPart) part;

                }

                if ( editorPart != null ) {
                    IAtomContainer ac;
                    ac = (IAtomContainer) editorPart
                                            .getAdapter( IAtomContainer.class );
                    //TODO set atom colorer from editor part
                    setAtomContainer( ac );
                }
            }

            public void partHidden( IWorkbenchPartReference partRef ) {

                IWorkbenchPart part = partRef.getPart( false );
                if ( part instanceof IEditorPart ) {
                    setAtomContainer( null );
                }
            }

            public void partActivated( IWorkbenchPartReference partRef ) {

            }

            public void partBroughtToTop( IWorkbenchPartReference partRef ) {

            }

            public void partClosed( IWorkbenchPartReference partRef ) {

            }

            public void partDeactivated( IWorkbenchPartReference partRef ) {

            }

            public void partInputChanged( IWorkbenchPartReference partRef ) {

            }

            public void partOpened( IWorkbenchPartReference partRef ) {

            }

        };
        getSite().getPage().addPartListener( partListener );
        parent.addDisposeListener( new DisposeListener () {

            public void widgetDisposed( DisposeEvent e ) {

                disposeControl( e );

            }

        });

    }

    @Override
    public void setFocus() {
        canvasView.setFocus();
    }

    private IAtomContainer getAtomContainerFromPart( IWorkbenchPart part ) {

        return (IAtomContainer) part.getAdapter( IAtomContainer.class );

    }

    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {

        if ( part instanceof IEditorPart ) {
            IAtomContainer ac = getAtomContainerFromPart( part );
            if(ac != null) {
                setAtomContainer( ac );
                return;
            }
        }
        reactOnSelection( selection );
    }

    private void reactOnSelection(ISelection selection) {

        if (!(selection instanceof IStructuredSelection))
            return;

        IStructuredSelection ssel = (IStructuredSelection) selection;

        Object obj = ssel.getFirstElement();

        if( obj instanceof IAtomContainer) {
            setAtomContainer( (IAtomContainer) obj );
        }
        //If we have an ICDKMolecule, just get the AC
        else if (obj instanceof ICDKMolecule) {
            CDKMolecule mol = (CDKMolecule) obj;
            if (mol.getAtomContainer()==null){
                logger.debug("CDKMolecule but can't get AtomContainer.");
                return;
            }
            setAtomContainer(mol.getAtomContainer());
        }

        //Try to get an IMolecule via the adapter
        else if (obj instanceof IAdaptable) {
            IAdaptable ada=(IAdaptable)obj;

            if(ada instanceof EditorPart) {
                setAtomContainer( (IAtomContainer)ada
                                  .getAdapter( IAtomContainer.class ) );
            }
            //Start by requesting molecule
            Object molobj=ada
            .getAdapter( net.bioclipse.core.domain.IMolecule.class );
            if (molobj==null ){
                //Nothing to show
                //                    clearView();
                return;
            }

            net.bioclipse.core.domain.IMolecule bcmol
            = (net.bioclipse.core.domain.IMolecule) molobj;

            try {

                //Create cdkmol from IMol, via CML or SMILES if that fails
                ICDKMolecule cdkMol=cdk.create( bcmol );

                //Create molecule
                IAtomContainer ac=cdkMol.getAtomContainer();
                molecule=new Molecule(ac);

                //Create 2D-coordinates if not available
                if (GeometryTools.has2DCoordinatesNew( molecule )==0){
                    sdg.setMolecule((IMolecule)molecule.clone());
                    sdg.generateCoordinates();
                    molecule = sdg.getMolecule();
                }

                //Set AtomColorer based on active editor
                //RFE: AtomColorer på JCPWidget
                //TODO

                //Update widget
                setAtomContainer(molecule);
            } catch (CloneNotSupportedException e) {
                clearView();
                logger.debug( "Unable to clone structure in 2Dview: "
                              + e.getMessage() );
            } catch ( BioclipseException e ) {
                clearView();
                logger.debug( "Unable to generate structure in 2Dview: "
                              + e.getMessage() );
            } catch ( Exception e ) {
                clearView();
                logger.debug( "Unable to generate structure in 2Dview: "
                              + e.getMessage() );
            }



            //Handle case where Iadaptable can return atoms to be highlighted
            Object selobj=ada
            .getAdapter( IChemicalSelection.class );
            //                ArrayList<Integer> atomSelectionIndices=new ArrayList<Integer>();

            if (selobj!=null){
                IChemicalSelection atomSelection=(IChemicalSelection)selobj;

                if ( atomSelection instanceof AtomIndexSelection ) {
                    AtomIndexSelection isel = (AtomIndexSelection) atomSelection;
                    int[] selindices = isel.getSelection();
                    //                        System.out.println("\n** Should highlight these JCP atoms:\n");
                    IAtomContainer selectedMols=new AtomContainer();
                    for (int i=0; i<selindices.length;i++){
                        selectedMols.addAtom( molecule.getAtom( selindices[i] ));
                        //                            System.out.println(i);
                    }
                    canvasView.getRenderer2DModel().setExternalSelectedPart( selectedMols );
                    canvasView.redraw();
                }
            }
        }
    }

    /**
     * Hide canvasview
     */
    private void clearView() {
        canvasView.setVisible( false );
    }

    private void setAtomContainer(IAtomContainer ac) {
            try {
                canvasView.setAtomContainer(ac);
                canvasView.setVisible( true );
                canvasView.redraw();
            } catch (Exception e) {
                canvasView.setVisible( false );
                logger.debug("Error displaying molecule in viewer: " + e.getMessage());
            }
    }

    private void disposeControl(DisposeEvent e) {
        getViewSite().getPage().removeSelectionListener(this);
        getSite().getPage().removePartListener( partListener );
        canvasView.dispose();
    }
}