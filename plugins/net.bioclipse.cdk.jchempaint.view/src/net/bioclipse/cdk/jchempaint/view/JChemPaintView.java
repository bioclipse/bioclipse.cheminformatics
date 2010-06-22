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
import net.bioclipse.cdk.domain.CDKChemObject;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget.Message;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.AtomIndexSelection;
import net.bioclipse.core.domain.IChemicalSelection;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseUIJob;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;
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
    implements ISelectionListener, ISelectionProvider {

    public static final String VIEW_ID="net.bioclipse.cdk.ui.view.Java2DRendererView";

    private static final Logger logger = Logger.getLogger(JChemPaintView.class);

    private JChemPaintWidget canvasView;
    private ChoiceGenerator extensionGenerator;
    private IPartListener2 partListener;
    private IAtomContainer ac;

    private ListenerList listeners = new ListenerList();

    /**
     * The constructor.
     */
    public JChemPaintView() {
    }

    private ICDKManager getCDKManager() {
        return net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
    }
    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        canvasView = new JChemPaintWidget(parent, SWT.NONE ) {
            @Override
            protected List<IGenerator<IAtomContainer>> createGenerators() {
                List<IGenerator<IAtomContainer>> genList =
                	new ArrayList<IGenerator<IAtomContainer>>();
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

        initilizeDrag();

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
                    if(ac==null) {
                        //See what's currently selected
                        ISelection selection=PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getSelectionService().getSelection();
                        reactOnSelection(selection);

                    }else
                        setAtomContainer( ac );
                    //TODO set atom colorer from editor part
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
        createContextMenu();
        getSite().getPage().addPartListener( partListener );
        parent.addDisposeListener( new DisposeListener () {

            public void widgetDisposed( DisposeEvent e ) {

                disposeControl( e );

            }

        });

    }

    private void createContextMenu() {
        // Create menu manager.
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager mgr) {
                fillContextMenu(mgr);
            }
        });

        // Create menu.
        Menu menu = menuMgr.createContextMenu(canvasView);
        canvasView.setMenu(menu);

        // Register menu for extension.
        getSite().registerContextMenu(menuMgr, this);
    }

    private void fillContextMenu(IMenuManager mgr) {
        mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    @Override
    public void setFocus() {
        canvasView.setFocus();
    }

    private IAtomContainer getAtomContainerFromPart( IWorkbenchPart part ) {

        return (IAtomContainer) part.getAdapter( IAtomContainer.class );

    }

    private ICDKMolecule getMoleculeFromPart(IWorkbenchPart part) {
        return (ICDKMolecule) part.getAdapter( ICDKMolecule.class );
    }

    public void selectionChanged( IWorkbenchPart part, final ISelection selection ) {

        if ( part instanceof IEditorPart ) {
            ICDKMolecule mc = getMoleculeFromPart( part );
            if(mc != null) {
                IAtomContainer ac = mc.getAtomContainer();
                if(ac!=null) {
                    canvasView.remove( Message.GENERATED );
                    setAtomContainer( ac );
                    return;
                }
            }
        }
        Display.getDefault().syncExec( new Runnable() {
            
            public void run() {
                reactOnSelection( selection );
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T adapt(IAdaptable adaptor,Class<T> clazz) {

        return (T)adaptor.getAdapter( clazz );
    }

    private void reactOnSelection(ISelection selection) {
        if(selection instanceof IStructuredSelection)
            reactOnSelection((IStructuredSelection)selection);
    }

    private void reactOn(ICDKMolecule mol, final IChemicalSelection chemSelection) {
        IAtomContainer atomContainer = mol.getAtomContainer();
        if (atomContainer==null){
            logger.debug("Unable to get atomcontainer from ICDKMolecule");
            return;
        }

        if( GeometryTools.has2DCoordinatesNew( atomContainer )<2) {
            BioclipseUIJob<List<IMolecule>> uiJob = new BioclipseUIJob<List<IMolecule>>() {
                @Override
                public void runInUI() {
                    List<IMolecule> returnValue = getReturnValue();
                    if( !returnValue.isEmpty()
                        && returnValue.get(0) instanceof ICDKMolecule) {
                    ICDKMolecule newMol = (ICDKMolecule) returnValue.get(0);
                    // Don't show 'Generated' message when preference is not set
                    if(showGeneratedLabel() )
                        canvasView.add( Message.GENERATED );
                    setAtomContainer(newMol.getAtomContainer());
                    if(chemSelection != null)
                        updateHighlight(newMol.getAtomContainer(), chemSelection);
                    canvasView.redraw();
                    }
                }
            };
            generate2DFrom(mol, uiJob);
        }else {
            setAtomContainer(atomContainer);
            if(chemSelection != null)
                updateHighlight(atomContainer, chemSelection);
            canvasView.redraw();
        }
    }

    private void reactOn(IAtomContainer atomContainer) {
        setAtomContainer(atomContainer);
    }

    private void reactOnSelection(IStructuredSelection ssel) {

        Object obj = ssel.getFirstElement();
        if(obj instanceof CDKChemObject<?>) return; //CDKChemObjects are not structures

        canvasView.remove( Message.GENERATED );

        if( obj instanceof IAtomContainer) {
            reactOn( (IAtomContainer) obj );
        }
        else if (obj instanceof ICDKMolecule) {
             reactOn((ICDKMolecule)obj, null);
        }
        //Try to get an IMolecule via the adapter
        else if (obj instanceof IAdaptable) {
            IAdaptable ada=(IAdaptable)obj;
            IChemicalSelection atomSelection=adapt(ada,IChemicalSelection.class);

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
                ICDKMolecule cdkMol=cdk.asCDKMolecule( bcmol );

               reactOn(cdkMol,atomSelection);
            } catch ( BioclipseException e ) {
                clearView();
                logger.debug( "Unable to generate structure in 2Dview: "
                              + e.getMessage() );
            } catch ( Exception e ) {
                clearView();
                logger.debug( "Unable to generate structure in 2Dview: "
                              + e.getMessage() );
            }
        }
    }

    private void updateHighlight(IAtomContainer ac,
            IChemicalSelection atomSelection) {
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
            }
        }
    }

    private void generate2DFrom( IMolecule mol,
            BioclipseUIJob<List<IMolecule>> uiJob)  {
        try {
            ICDKManager cdk = getCDKManager();
            cdk.generate2dCoordinates(mol, uiJob);

        } catch ( Exception e ) {
            setAtomContainer( null );
            logger.debug( "Error generating 2d coordinates: " +e.getMessage()  );
            LogUtils.debugTrace( logger, e );
        }
    }

    private boolean showGeneratedLabel() {
        return Platform.getPreferencesService().getBoolean(
                "net.bioclipse.cdk.jchempaint",
                "showGeneratedLabel",
                true, null );
    }

    /**
     * Hide canvasview
     */
    private void clearView() {
        canvasView.setVisible( false );
    }

    private void setAtomContainer(IAtomContainer ac) {
        this.ac = ac;
        IChemModel model = null;
       if(ac!= null) {
            try {
                model = ChemModelManipulator.newChemModel( ac );
            } catch (Exception e) {
                logger.debug( "Error displaying molecule in 2d structure view: "
                              + e.getMessage());
            }
       }
       canvasView.setModel( model );
       canvasView.setVisible( model!=null );
       canvasView.redraw();
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

    public void addSelectionChangedListener(
                                         ISelectionChangedListener listener ) {
        listeners.add( listener );
    }

    public ISelection getSelection() {
        ICDKMolecule mol;
        try {
            mol = new CDKMolecule((IAtomContainer)ac.clone());
            return new StructuredSelection(mol);
        } catch ( CloneNotSupportedException e ) {
           logger.debug( "Could not clone atomcontainer" );
        }
        return StructuredSelection.EMPTY;
    }

    public void removeSelectionChangedListener(
                                         ISelectionChangedListener listener ) {
        listeners.remove( listener );

    }

    public void setSelection( ISelection selection ) {
            SelectionChangedEvent event = new SelectionChangedEvent( this,
                                                                     selection);
            for(Object o:listeners.getListeners()) {
                ((ISelectionChangedListener)o).selectionChanged( event );
            }
    }

    public void refresh() {
        canvasView.redraw();
    }

    void initilizeDrag() {
        int operations = DND.DROP_COPY|DND.DROP_MOVE;
        Transfer[] transferTypes = new Transfer[] { AtomContainerTransfer.getInstance(),
                                                    PluginTransfer.getInstance()};
        DragSource source = new DragSource( canvasView, operations );
        source.setTransfer( transferTypes );
        source.addDragListener( new DragSourceAdapter() {
            @Override
            public void dragStart( DragSourceEvent event ) {
                if(ac!=null) {
                    Control control = canvasView;
                    GC gc = new GC(control);
                    int controlWidth = control.getSize().x;
                    int controlHeight = control.getSize().y;
                    int dragImageSize = 128;
                    Image image = new Image(control.getDisplay(), controlWidth, controlHeight);
                    gc.setAlpha( 128 );
                    gc.copyArea(image, 0,0);

                    Image imageX = new Image(control.getDisplay(), dragImageSize,dragImageSize);

                    GC gcX = new GC(imageX);
                    gcX.setAlpha( 128 );
                    gcX.setInterpolation( SWT.HIGH );
                    gcX.drawImage( image, 0, 0, controlWidth,controlHeight,
                                   0,0, dragImageSize,dragImageSize);
//                    ImageData ideaData = image.getImageData();
//                    int whitePixel = ideaData.palette.getPixel(new RGB(255,255,255));
//                    ideaData.transparentPixel = whitePixel;
//                    Image transparentIdeaImage = new Image(control.getDisplay(),ideaData);
                    gc.dispose();
                    gcX.dispose();
                    event.image = imageX;
                    event.offsetX = dragImageSize/2;
                    event.offsetY = dragImageSize/2;
                    event.doit = true;
                }
            }
            @Override
            public void dragSetData( DragSourceEvent event ) {
                if(AtomContainerTransfer.getInstance().isSupportedType( event.dataType ))
                    event.data = ac;
                else if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
                    byte[] data = AtomContainerTransfer.getInstance().toByteArray(ac);
                    event.data = new PluginTransferData("net.bioclipse.cdk.jchempaint.atomContainerDrop", data);
                 }
            }
        });
    }
}