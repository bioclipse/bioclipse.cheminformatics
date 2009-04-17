/*******************************************************************************
 * Copyright (c) 2008-2009 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arvid Berg
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKChemObject;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.handlers.ModuleState;
import net.bioclipse.cdk.jchempaint.handlers.RedoHandler;
import net.bioclipse.cdk.jchempaint.handlers.UndoHandler;
import net.bioclipse.cdk.jchempaint.outline.JCPOutlinePage;
import net.bioclipse.cdk.jchempaint.widgets.JChemPaintEditorWidget;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ui.jobs.BioclipseUIJob;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.IControllerModel;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.MDLV2000Format;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.selection.AbstractSelection;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;
import org.openscience.cdk.renderer.selection.MultiSelection;
import org.openscience.cdk.renderer.selection.SingleSelection;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

public class JChemPaintEditor extends EditorPart implements ISelectionListener {

    private Logger logger = Logger.getLogger(JChemPaintEditor.class);

    private JCPOutlinePage fOutlinePage;

    private ICDKMolecule           model;
    private JChemPaintEditorWidget widget;
    private IControllerModel       c2dm;
    private Menu                   menu;

    IPartListener2 partListener;

    public JChemPaintEditorWidget getWidget() {
        return widget;
    }

    public void undo() throws ExecutionException {
        widget.undo();
    }

    public void redo() throws ExecutionException {
        widget.redo();
    }

    @Override
    public void doSave( IProgressMonitor monitor ) {
        ICDKManager cdk = Activator.getDefault().getCDKManager();

        try {
            IFile resource = (IFile)model.getResource();
            IChemFormat chemFormat = cdk.determineFormat(
                resource.getContentDescription().getContentType()
            );
            if (chemFormat == MDLV2000Format.getInstance() ||
                chemFormat == CMLFormat.getInstance()) {
                cdk.saveMolecule(
                        getCDKMolecule(),
                        model.getResource().getLocationURI().toString(),
                        true // overwrite
                );
                widget.setDirty( false );
            } else {
                doSaveAs();
            }
        } catch ( BioclipseException e ) {
            monitor.isCanceled();
            logger.debug( "Failed to save file: " + e.getMessage() );
        } catch ( CDKException e ) {
            monitor.isCanceled();
            logger.debug( "Failed to save file: " + e.getMessage() );
        } catch ( CoreException e ) {
            monitor.isCanceled();
            logger.debug( "Failed to save file: " + e.getMessage() );
        }
    }

    @Override
    public void doSaveAs() {

        SaveAsDialog saveAsDialog = new SaveAsDialog( this.getSite().getShell() );
        if ( model.getResource() instanceof IFile )
            saveAsDialog.setOriginalFile( (IFile) model.getResource() );
        int result = saveAsDialog.open();
        if ( result == 1 ) {
            logger.debug( "SaveAs canceled." );
            return;
        }

        IPath path = saveAsDialog.getResult();
        IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile( path );
        try {
            // do a nasty trick... the SaveAs dialog does not allow us to
            // ask for a format (yet), so guess something from the file
            // extension
            IChemFormat format = Activator.getDefault().getCDKManager()
                .guessFormatFromExtension(path.toString());
            if (format == null) format = (IChemFormat)CMLFormat.getInstance();

            Activator.getDefault().getCDKManager().saveMolecule(
                model, file, format, true
            );
            setInput( new FileEditorInput(file) );
            setPartName( file.getName() );
            firePropertyChange( IWorkbenchPartConstants.PROP_PART_NAME);
            firePropertyChange( IWorkbenchPartConstants.PROP_INPUT);
        } catch ( BioclipseException e ) {
            logger.warn( "Failed to save molecule. " + e.getMessage() );
        } catch ( CDKException e ) {
            logger.warn( "Failed to save molecule. " + e.getMessage() );
        } catch ( CoreException e ) {
            logger.warn( "Failed to save molecule. " + e.getMessage() );
        }
        widget.setDirty( false );
    }

    @Override
    public void init( IEditorSite site, IEditorInput input )
                                       throws PartInitException {

        setSite( site );
        setInput( input );

        IFile file = (IFile) input.getAdapter( IFile.class );
        if(file != null) {
//            file.getContentDescription().getContentType()
            setPartName( input.getName() );
                return;
        }else if(input instanceof NullEditorInput) {
            return;
        }
        else{
            ICDKMolecule cModel = (ICDKMolecule)
                                    input.getAdapter( ICDKMolecule.class );
            if(cModel!=null) {
                // FIXME resolve molecule name
                setPartName( cModel.getName() );
                return;
            }
        }
        throw new PartInitException("File not supported");
    }

    @Override
    public boolean isDirty() {

        return widget.getDirty();
    }

    @Override
    public boolean isSaveAsAllowed() {

        return true;
    }

    @Override
    public void createPartControl( Composite parent ) {

        createWidget(parent);

        createMenu();

        getSite().getPage().addSelectionListener( this );

        IEditorInput input = getEditorInput();
        ICDKMolecule cdkModel = (ICDKMolecule) input
                                .getAdapter( ICDKMolecule.class );
        if(cdkModel!=null) {
            model = cdkModel;
            widget.setInput( model );
        }else {
            IFile file = (IFile) input.getAdapter( IFile.class );
            if(file != null && file.exists()) {
                try {
                    Activator.getDefault().getCDKManager().loadMolecule( file,
                         new BioclipseUIJob<ICDKMolecule>() {

                        @Override
                        public void runInUI() {
                            model = getReturnValue();
                            widget.setInput( model );
                            if(fOutlinePage!=null) {
                                fOutlinePage.setInput(
                                          getControllerHub().getIChemModel() );
                            }
                        }
                    });
                } catch ( IOException e1 ) {
                    logger.warn( "Failed to load molecule "+e1.getMessage() );
                    throw new RuntimeException(e1);
                } catch ( BioclipseException e1 ) {
                    logger.warn( "Failed to load molecule "+e1.getMessage() );
                    throw new RuntimeException(e1);
                } catch ( CoreException e1 ) {
                    logger.warn( "Failed to load molecule "+e1.getMessage() );
                    throw new RuntimeException(e1);
                }
            }
        }

        parent.addDisposeListener( new DisposeListener() {
            public void widgetDisposed( DisposeEvent e ) {
                disposeControl( e );
            }
        } );

        createPartListener();

        createUndoRedoHangler();
    }

    private void createPartListener() {

        partListener = new IPartListener2() {

            public void partActivated( IWorkbenchPartReference partRef ) {

                    Map<String, String> params = new HashMap<String, String>();
                    params.put( "net.bioclipse.cdk.jchempaint.DrawModeString",
                                getControllerHub().getActiveDrawModule()
                                    .getDrawModeString());
                    ICommandService service = (ICommandService) getSite()
                                            .getService(ICommandService.class);
                    if(service!=null) {
                        service.refreshElements( ModuleState.COMMAND_ID,
                                                 null);
                    }

            }

            public void partBroughtToTop( IWorkbenchPartReference partRef ) {
            }

            public void partClosed( IWorkbenchPartReference partRef ) {
            }

            public void partDeactivated( IWorkbenchPartReference partRef ) {
            }

            public void partHidden( IWorkbenchPartReference partRef ) {
            }

            public void partInputChanged( IWorkbenchPartReference partRef ) {
            }

            public void partOpened( IWorkbenchPartReference partRef ) {
            }

            public void partVisible( IWorkbenchPartReference partRef ) {
            }

        };
        getSite().getPage().addPartListener( partListener );

    }

    private void createUndoRedoHangler() {
     // set up action handlers
        UndoHandler undoAction = new UndoHandler();
        RedoHandler redoAction = new RedoHandler();
        IActionBars actionBars = this.getEditorSite().getActionBars();
        actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),undoAction);
        actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
    }

    private void createMenu() {

        MenuManager menuMgr = new MenuManager();
        menuMgr.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
        getSite().registerContextMenu("net.bioclipse.cdk.ui.editors.jchempaint",
                                      menuMgr, widget );

        menu = menuMgr.createContextMenu( widget );
        widget.setMenu( menu );
        widget.addMenuDetectListener( new MenuDetectListener() {

            public void menuDetected( MenuDetectEvent e ) {
                IChemModelRelay chemModelRelay = widget.getControllerHub();
                RendererModel rModel =chemModelRelay.getRenderer()
                                                    .getRenderer2DModel();

                IAtom atom = rModel.getHighlightedAtom();
                IBond bond = rModel.getHighlightedBond();

                IChemObjectSelection localSelection = rModel.getSelection();
                IChemObject chemObject = atom!=null?atom:bond;

                if(!localSelection.contains( chemObject )) {
                    if(chemObject != null)
                        localSelection = new SingleSelection<IChemObject>(chemObject);
                    else
                        localSelection = AbstractSelection.EMPTY_SELECTION;
                }
                rModel.setSelection( localSelection);
                widget.setSelection( widget.getSelection() );
                e.doit = true;
            }

        });

    }

    private void createWidget(Composite parent) {
     // create widget
        widget = new JChemPaintEditorWidget( parent, SWT.NONE ) {

            @Override
            public void setDirty( boolean dirty ) {
                super.setDirty( dirty );
                Display.getDefault().asyncExec( new Runnable() {
                    public void run() {
                        firePropertyChange( IEditorPart.PROP_DIRTY );
                    }
                });
            }
            @Override
            protected void structureChanged() {
                super.structureChanged();
                if(fOutlinePage!=null) {
                    Display.getDefault().asyncExec( new Runnable() {
                        public void run() {
                            fOutlinePage.setInput( getControllerHub().getIChemModel() );
                        }
                    });
                }
            }

            @Override
            protected void structurePropertiesChanged() {
                super.structurePropertiesChanged();
                if(fOutlinePage!=null) {
                    Display.getDefault().asyncExec( new Runnable() {
                        public void run() {
                            fOutlinePage.setInput( getControllerHub().getIChemModel() );
                        }
                    });
                }
            }
        };

        getSite().setSelectionProvider( widget );
    }
    @Override
    public void setFocus() {

        widget.setFocus();
    }

    public ControllerHub getControllerHub() {

        return widget.getControllerHub();
    }

    public IControllerModel getControllerModel() {

        return c2dm;
    }

    public void update() {
        IChemModel cModel = getControllerHub().getIChemModel();
        if(cModel == null) return;
        for(IAtomContainer ac:ChemModelManipulator.getAllAtomContainers( cModel )) {
            ac.setProperties( new HashMap<Object, Object>(
                    model.getAtomContainer().getProperties()) );
        }
        widget.redraw();
    }

    public void setInput( Object element ) {

        widget.setInput( element );
        widget.redraw();
    }

    public ICDKMolecule getCDKMolecule() {
        IAtomContainer modelContainer = model.getAtomContainer();

        IChemModel chemModel = getControllerHub().getIChemModel();
        for(IAtomContainer aContainer:ChemModelManipulator
                                        .getAllAtomContainers( chemModel )) {
            modelContainer.add( aContainer );
        }

        return model;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter ) {

        if ( IContentOutlinePage.class.equals( adapter ) ) {
            if ( fOutlinePage == null ) {
                fOutlinePage = new JCPOutlinePage(this);
                fOutlinePage.setInput( getControllerHub().getIChemModel() );
            }
            return fOutlinePage;
        }
        if ( IAtomContainer.class.equals( adapter ) ) {
            if(model!=null)
                return model.getAtomContainer();
            else
                return null;
        }
        return super.getAdapter( adapter );
    }

    public void doAddAtom() {

        logger.debug( "Executing 'Add atom' action" );
    }

    public void doChangeAtom() {

        logger.debug( "Executing 'Chage atom' action" );
    }

    private static boolean contains(Iterable<IAtomContainer> acIter,
                                    IChemObject chemObject) {
        boolean contains = false;
        for(IAtomContainer ac:acIter) {

            if(chemObject instanceof IAtom) {
                contains = ac.contains( (IAtom )chemObject);
            }else if(chemObject instanceof IBond){
                contains = ac.contains( (IBond )chemObject);
            }
            if(contains) break;
        }
        return contains;
    }
    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {

        if ( part != null && part.equals( this ) )
            return;

        if ( selection instanceof IStructuredSelection ) {
            IStructuredSelection bcSelection =
                (IStructuredSelection)selection;
            IChemObjectSelection jcpSelection = AbstractSelection.EMPTY_SELECTION;
            if(bcSelection.size()==0) { // do nothing jcpSelection already empty
            }else
                if(bcSelection.size()==1) {
                    if(bcSelection.getFirstElement() instanceof CDKChemObject) {
                        IChemObject chemObject= ((CDKChemObject)bcSelection
                                               .getFirstElement()).getChemobj();

                        if(contains(widget.getControllerHub()
                                    .getIChemModel().getMoleculeSet()
                                    .atomContainers(),
                                    chemObject)) {

                            jcpSelection = new SingleSelection<IChemObject>(
                                    chemObject);
                        }
                    }
            }else {

                Set<IChemObject> chemSelection = new HashSet<IChemObject>();
                for(Iterator<?> iter = bcSelection.iterator();iter.hasNext();) {
                    Object o = iter.next();
                    if(o instanceof CDKChemObject) {
                        IChemObject chemObject= ((CDKChemObject)bcSelection
                                .getFirstElement()).getChemobj();

                        if(contains(widget.getControllerHub()
                                    .getIChemModel().getMoleculeSet()
                                    .atomContainers(),
                                    chemObject)) {

                            chemSelection.add( ((CDKChemObject) o).getChemobj() );
                        }
                    }
                }
                jcpSelection = new MultiSelection<IChemObject>(chemSelection);
            }

            widget.getRenderer2DModel().setSelection( jcpSelection );
            if(!widget.isDisposed())
                widget.redraw();
        }
    }

    private void disposeControl( DisposeEvent e ) {

        // TODO remove regiistration?
        // getSite().registerContextMenu(
        // "net.bioclipse.cdk.ui.editors.jchempaint.menu",
        // menuMgr, widget);
        getSite().setSelectionProvider( null );
        getSite().getPage().removeSelectionListener( this );
        getSite().getPage().removePartListener( partListener );

        widget.dispose();
        menu.dispose();
    }

    public void snapshot(final IFile file) throws CoreException {
        Image image = widget.snapshot();
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] { image.getImageData() };
        ByteArrayOutputStream inMemoryFile = new ByteArrayOutputStream();
        loader.save(inMemoryFile, SWT.IMAGE_PNG);
        try {
            inMemoryFile.flush();
        } catch (IOException ioe) {

        }
        ByteArrayInputStream input = new ByteArrayInputStream(inMemoryFile.toByteArray());
        if (file.exists()) {
            file.setContents(input, true, false, null);
        } else {
            file.create(input, true, null);
        }
    }
}
