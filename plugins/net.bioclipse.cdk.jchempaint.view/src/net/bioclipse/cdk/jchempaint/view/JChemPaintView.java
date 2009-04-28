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

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.AtomIndexSelection;
import net.bioclipse.core.domain.IChemicalSelection;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;

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
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * 2D Rendering widget using the new SWT based JChemPaint renderer.
 */
public class JChemPaintView extends ViewPart
    implements ISelectionListener {

    private static final Logger logger = Logger.getLogger(JChemPaintView.class);

    private JChemPaintWidget canvasView;
    private ChoiceGenerator extensionGenerator;
    private IPartListener2 partListener;

    /**
     * The constructor.
     */
    public JChemPaintView() {
    }

    private ICDKManager getCDKManager() {
        return net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
    }
    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        canvasView = new JChemPaintWidget(parent, SWT.NONE ) {
            @Override
            protected List<IGenerator> createGenerators() {
                List<IGenerator> genList = new ArrayList<IGenerator>();
                genList.add(extensionGenerator
                            =ChoiceGenerator.getGeneratorsFromExtensionPoint());
                genList.addAll( super.createGenerators() );
                return genList;
            }
        };
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

    @SuppressWarnings("unchecked")
    private static <T> T adapt(IAdaptable adaptor,Class<T> clazz) {

        return (T)adaptor.getAdapter( clazz );
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
        else
            if (obj instanceof ICDKMolecule) {
                CDKMolecule mol = (CDKMolecule) obj;
                if (mol.getAtomContainer()==null){
                    logger.debug("CDKMolecule but can't get AtomContainer.");
                    return;
                }

                if( GeometryTools.has2DCoordinatesNew( mol.getAtomContainer() )<2) {
                    setAtomContainer( generate2DFrom( mol ) );
                }else
                    setAtomContainer(mol.getAtomContainer());
            }

        //Try to get an IMolecule via the adapter
        else if (obj instanceof IAdaptable) {
            IAdaptable ada=(IAdaptable)obj;

            if(ada instanceof EditorPart) {
                setAtomContainer( adapt(ada,IAtomContainer.class) );
                return;
            }
            //Start by requesting molecule
            IMolecule bcmol = adapt( ada, IMolecule.class);
            if(bcmol == null) {
                setAtomContainer( null );
                return;
            }
            IAtomContainer ac = null;
            try {
                ICDKManager cdk = getCDKManager();
                //Create cdkmol from IMol, via CML or SMILES if that fails
                ICDKMolecule cdkMol=cdk.create( bcmol );

                //Create molecule
                ac=cdkMol.getAtomContainer();

                //Create 2D-coordinates if not available
                if (GeometryTools.has2DCoordinatesNew( ac )<2){
                    ac = generate2DFrom( cdkMol );

                }

                //Set AtomColorer based on active editor
                //RFE: AtomColorer pŒ JCPWidget
                //TODO

                //Update widget
                setAtomContainer(ac);
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
            IChemicalSelection atomSelection=adapt(ada,IChemicalSelection.class);
            //                ArrayList<Integer> atomSelectionIndices=new ArrayList<Integer>();

            if (atomSelection!=null && ac!=null){

                if ( atomSelection instanceof AtomIndexSelection ) {
                    AtomIndexSelection isel = (AtomIndexSelection) atomSelection;
                    int[] selindices = isel.getSelection();
                    //                        System.out.println("\n** Should highlight these JCP atoms:\n");
                    IAtomContainer selectedMols=new AtomContainer();
                    for (int i=0; i<selindices.length;i++){
                        selectedMols.addAtom( ac.getAtom( selindices[i] ));
                        //                            System.out.println(i);
                    }
                    canvasView.getRenderer2DModel().setExternalSelectedPart( selectedMols );
                    canvasView.redraw();
                }
            }
        }
    }

    private IAtomContainer generate2DFrom(IMolecule mol)  {
        ICDKMolecule newMol = null;
            try {
                newMol = getCDKManager().generate2dCoordinates( mol );
                return newMol.getAtomContainer();
            } catch ( Exception e ) {
                setAtomContainer( null );
                logger.debug( "Error generating 2d coordinates: " +e.getMessage()  );
                LogUtils.debugTrace( logger, e );
                return null;
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
                IChemModel model = ChemModelManipulator.newChemModel( ac );
                canvasView.setModel( model );
                canvasView.setVisible( true );
                canvasView.redraw();
            } catch (Exception e) {
                canvasView.setVisible( false );
                logger.debug( "Error displaying molecule in 2d structure view: "
                              + e.getMessage());
            }
    }

    private void disposeControl(DisposeEvent e) {
        getViewSite().getPage().removeSelectionListener(this);
        getSite().getPage().removePartListener( partListener );
        canvasView.dispose();
    }

    public void showExternalGenerators(boolean show) {
        extensionGenerator.setUse( show );
        canvasView.redraw();
    }
}