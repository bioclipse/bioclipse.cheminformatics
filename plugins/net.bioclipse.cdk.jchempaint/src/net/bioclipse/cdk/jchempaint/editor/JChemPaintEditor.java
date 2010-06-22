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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKChemObject;
import net.bioclipse.cdk.domain.CDKMoleculeUtils;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.ISubStructure;
import net.bioclipse.cdk.domain.CDKMoleculeUtils.MolProperty;
import net.bioclipse.cdk.jchempaint.generators.SubStructureGenerator;
import net.bioclipse.cdk.jchempaint.handlers.ModuleState;
import net.bioclipse.cdk.jchempaint.handlers.RedoHandler;
import net.bioclipse.cdk.jchempaint.handlers.UndoHandler;
import net.bioclipse.cdk.jchempaint.outline.JCPOutlinePage;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget.Message;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget.Message.Alignment;
import net.bioclipse.cdk.jchempaint.widgets.JChemPaintEditorWidget;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.ui.dialogs.SaveAsDialog;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.IControllerModel;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.formats.MDLV2000Format;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.selection.AbstractSelection;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;
import org.openscience.cdk.renderer.selection.MultiSelection;
import org.openscience.cdk.renderer.selection.SingleSelection;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

public class JChemPaintEditor extends EditorPart implements ISelectionListener ,
                                                        IResourceChangeListener{

    private Logger logger = Logger.getLogger(JChemPaintEditor.class);

    public static final String STRUCUTRE_CHANGED_EVENT="structure_changed";
    public static final String MODEL_LOADED = "net.bioclipse.jchempaint.load.model";

    private JCPOutlinePage fOutlinePage;

    private JChemPaintEditorWidget widget;
    private IControllerModel       c2dm;
    private Menu                   menu;

    IPartListener2 partListener;

    private Message customMessage;

    private ListenerList propertyChangedListenerList = new ListenerList();

    private SubStructureGenerator subStructureGenerator;

    public JChemPaintEditorWidget getWidget() {
        return widget;
    }

    public void undo() throws ExecutionException {
        widget.undo();
        fireStructureChanged();
    }

    public void redo() throws ExecutionException {
        widget.redo();
        fireStructureChanged();
    }

    @Override
    public void doSave( IProgressMonitor monitor ) {
        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        ICDKMolecule model = getCDKMolecule();
        if(model.getResource() == null) {
            doSaveAs();
            return;
        }
        try {
            IFile resource = (IFile)model.getResource();
            IChemFormat chemFormat = cdk.determineFormat(
                resource.getContentDescription().getContentType()
            );
            if (chemFormat == MDLV2000Format.getInstance()) {
                // check for loss of information
                IAtomContainer container = model.getAtomContainer();
                if (GeometryTools.has3DCoordinates(container) &&
                    GeometryTools.has2DCoordinates(container)) {
                    boolean agreedWithInfoLoss = MessageDialog.openQuestion(
                        this.getSite().getShell(),
                        chemFormat.getFormatName(),
                        "This file format cannot save 3D and 2D coordinates. " +
                        "Do you want to save only 2D?"
                    );
                    if (!agreedWithInfoLoss) return;
                }

                Properties properties = new Properties();
                properties.setProperty("ForceWriteAs2DCoordinates", "true");
                cdk.saveMolecule(
                    getCDKMolecule(),
                    model.getResource().getLocationURI().toString(),
                    chemFormat,
                    true, // overwrite
                    properties
                );
                widget.setDirty( false );
            } else if (chemFormat == CMLFormat.getInstance()) {
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
        } catch ( CoreException e ) {
            monitor.isCanceled();
            logger.debug( "Failed to save file: " + e.getMessage() );
        }
    }

    @Override
    public void doSaveAs() {
        ICDKMolecule model = getCDKMolecule();
        List<IResourceFormat> formats = new ArrayList<IResourceFormat>();
        formats.add(CMLFormat.getInstance());
        formats.add(MDLV2000Format.getInstance());
        SaveAsDialog saveAsDialog = new SaveAsDialog(
            this.getSite().getShell(), formats
        );
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
            IChemFormat format = Activator.getDefault().getJavaCDKManager()
                .guessFormatFromExtension(path.toString());
            if (format == null) format = (IChemFormat)CMLFormat.getInstance();

            if (format instanceof MDLV2000Format) {
                Properties properties = new Properties();
                properties.setProperty("ForceWriteAs2DCoordinates", "true");
                Activator.getDefault().getJavaCDKManager().saveMolecule(
                    model, file, format, true, properties
                );
            } else {
                Activator.getDefault().getJavaCDKManager().saveMolecule(
                    model, file, format, true
                );
            }
            setInput( new FileEditorInput(file) );
            setPartName( file.getName() );
            firePropertyChange( IWorkbenchPartConstants.PROP_PART_NAME);
            firePropertyChange( IWorkbenchPartConstants.PROP_INPUT);
        } catch ( BioclipseException e ) {
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
        if(input==null) return;
        IFile file = (IFile) input.getAdapter( IFile.class );
        if(file != null) {
//            file.getContentDescription().getContentType()
            setPartName( input.getName() );
                return;
        }
        else{
            ICDKMolecule cModel = (ICDKMolecule)
                                    input.getAdapter( ICDKMolecule.class );
            if(cModel!=null) {
                // FIXME resolve molecule name
                if(cModel.getResource()!=null)
                    setPartName( cModel.getResource().getName() );
                else
                    setPartName( "UNNAMED" );
                return;
            }
        }
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
            widget.setInput( cdkModel );fireModelLoaded();
            if(cdkModel.getResource()==null)
                widget.setDirty( true );
        }else {
            IFile file = (IFile) input.getAdapter( IFile.class );
            if(file != null && file.exists()) {
                Activator.getDefault().getJavaCDKManager()
                         .loadMolecule( file,
                                        new BioclipseUIJob<ICDKMolecule>() {

                    @Override
                    public void runInUI() {
                        ICDKMolecule model = getReturnValue();
                        int x2d = GeometryTools.has2DCoordinatesNew(
                                                     model.getAtomContainer() );
                        x2d = 2;
                        if(x2d <2 ) {
                            logger.error( "Not all atoms has 2d coordinates" );
                            JChemPaintEditor.this.getSite().getPage()
                               .closeEditor( JChemPaintEditor.this, false );
                            model = null;
                            return;
                        }
                        widget.setInput( model );fireModelLoaded();
                        if(fOutlinePage!=null) {
                            fOutlinePage.setInput(
                                      getControllerHub().getIChemModel() );
                        }
                    }
                });
            }else {
                widget.setInput( null );
                logger.error( "Failed to get molecule form editor input" );
            }
        }

        parent.addDisposeListener( new DisposeListener() {
            public void widgetDisposed( DisposeEvent e ) {
                disposeControl( e );
            }
        } );

            ResourcesPlugin.getWorkspace().addResourceChangeListener(
                  this, IResourceChangeEvent.POST_CHANGE);
        createPartListener();
        IContextService contextService = (IContextService) getSite()
                                        .getService( IContextService.class );

        contextService.activateContext( "net.bioclipse.ui.contexts.JChemPaint" );

        createUndoRedoHandler();
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
                    IWorkbenchPart activatedPart = partRef.getPart(false);
                    if(JChemPaintEditor.this.equals(activatedPart))
                        widget.reset();
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

    private void createUndoRedoHandler() {
        IEditorSite site = getEditorSite();
        UndoRedoActionGroup actionGroup = new UndoRedoActionGroup( site,
                                                        widget.getUndoContext(),
                                                        true );
        actionGroup.fillActionBars( site.getActionBars() );
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

                if(localSelection!=null && !localSelection.contains( chemObject )) {
                    if(chemObject != null)
                        localSelection = new SingleSelection<IChemObject>(chemObject);
                    else
                        localSelection = AbstractSelection.EMPTY_SELECTION;
                }
                if(localSelection==null) localSelection = AbstractSelection.EMPTY_SELECTION;
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
            protected List<IGenerator<IAtomContainer>> createGenerators() {
                List<IGenerator<IAtomContainer>> generators =
                	new ArrayList<IGenerator<IAtomContainer>>();
                generators.add( subStructureGenerator = new SubStructureGenerator() );
                generators.addAll( super.createGenerators() );
                return generators;
            }
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
                widget.getRenderer2DModel().setExternalSelectedPart( null );
                subStructureGenerator.clear();
                fireStructureChanged();
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
                subStructureGenerator.clear();
                fireStructureChanged();
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
        // FIXME this syncing of properties should be done in some other way
        for(IAtomContainer ac:ChemModelManipulator.getAllAtomContainers( cModel )) {
            ac.setProperties( new HashMap<Object, Object>(
                    widget.getMolecule().getAtomContainer().getProperties()) );
        }
        widget.redraw();
    }

    public void setInput( Object element ) {
        if(element instanceof IAdaptable) {
            widget.setInput( (IAdaptable)element );fireModelLoaded();
            widget.redraw();
        }
    }

    public ICDKMolecule getCDKMolecule() {
        ICDKMolecule model = widget.getMolecule();
        if(model == null) return null;
        IAtomContainer modelContainer = model.getAtomContainer();
        modelContainer.removeAllElements();
        IChemModel chemModel = getControllerHub().getIChemModel();
        for(IAtomContainer aContainer:ChemModelManipulator
                                        .getAllAtomContainers( chemModel )) {
            modelContainer.add( aContainer );
        }

        return model;
    }

    public void setMoleculeProperty(Object key,Object value) {
        widget.setProperty(key, value);
    }
    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter ) {

        if(adapter.equals( this.getClass() )) {
            return this;
        }
        if ( IContentOutlinePage.class.equals( adapter ) ) {
            if ( fOutlinePage == null ) {
                fOutlinePage = new JCPOutlinePage(this);
                fOutlinePage.setInput( getControllerHub().getIChemModel() );
            }
            return fOutlinePage;
        }
        if ( IAtomContainer.class.equals( adapter ) ) {
            ICDKMolecule mol = getCDKMolecule();
            if(mol!=null)
                return mol.getAtomContainer();
            return null;
        }
        if( adapter.isAssignableFrom(ICDKMolecule.class)) {
            return getCDKMolecule();
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

        if( part != null) {
            if(part.equals( this ))
                    return;
            else {
                Object o = part.getAdapter( JChemPaintEditor.class );
                if(o != null && o.equals( this ))
                    return;
            }

        }


        if ( selection instanceof IStructuredSelection ) {
            IStructuredSelection bcSelection =
                (IStructuredSelection)selection;


            IChemObjectSelection jcpSelection = AbstractSelection.EMPTY_SELECTION;

            if(selection.isEmpty()) {
                widget.getRenderer2DModel().setExternalSelectedPart(
                      widget.getControllerHub().getIChemModel().getBuilder().
                          newInstance(IAtomContainer.class));
            }
            subStructureGenerator.clear();
            Set<IChemObject> chemSelection = new HashSet<IChemObject>();
            for(Iterator<?> iter = bcSelection.iterator();iter.hasNext();) {
                Object o = iter.next();
                if(o instanceof CDKChemObject) {
                    IChemObject chemObject= ((CDKChemObject<?>)o).getChemobj();

                    if(contains(ChemModelManipulator.getAllAtomContainers(
                                  widget.getControllerHub().getIChemModel()),
                                chemObject)) {

                        chemSelection.add( chemObject );
                    }
                }
                else if(o instanceof ISubStructure) {
                    subStructureGenerator.add( (ISubStructure)o );
                }
            }

            if(chemSelection.size()==1) {
                for(IChemObject o:chemSelection) {
                    jcpSelection = new SingleSelection<IChemObject>(o);
                }
            }else if(chemSelection.size()!=0) {
                jcpSelection = new MultiSelection<IChemObject>(chemSelection);
            }

            widget.getRenderer2DModel().setSelection( jcpSelection );
            if(!widget.isDisposed())
                widget.redraw();
        }
    }

    private void disposeControl( DisposeEvent e ) {

        ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );
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

    public void setMessage(String message) {
        customMessage = new JChemPaintWidget.Message( message,
                                                      Alignment.BOTTOM_LEFT);
        getWidget().add( customMessage );
        getWidget().redraw();
    }

    public void clearMessage() {
        getWidget().remove( customMessage );
        getWidget().redraw();
    }

    public void addPropertyChangedListener(IPropertyChangeListener listener) {
        propertyChangedListenerList.add( listener );
    }

    public void removePropertyChangedListener(IPropertyChangeListener listener) {
        propertyChangedListenerList.remove( listener );
    }

    private void firePropertyChangedEvent(PropertyChangeEvent event) {
        Object[] listeners = propertyChangedListenerList.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
         ((IPropertyChangeListener) listeners[i]).propertyChange(event);
        }
    }

    private void invalidateProperties() {
        final ICDKMolecule mol = getCDKMolecule();
        CDKMoleculeUtils.clearProperty( mol, MolProperty.SMILES.name() );
        CDKMoleculeUtils.clearProperty( mol, MolProperty.InChI.name() );
        CDKMoleculeUtils.clearProperty( mol, MolProperty.Fingerprint.name() );

        widget.setSelection( widget.getSelection() );

        if(mol == null || mol.getAtomContainer().getAtomCount() == 0) return;
//        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
//        IInChIManager inchi = net.bioclipse.inchi.business.Activator
//                        .getDefault().getJavaInChIManager();
        //calculating smiles in the Propertysource instead.
//        try {
//            if(SMILESJob!=null) {
//                if(SMILESJob.cancel());
//                SMILESJob.join();
//            }
//        } catch ( InterruptedException e ) {
//            logger.debug( "SMILES job interrupted" );
//        }
//        SMILESJob = cdk.calculateSMILES( mol,
//                                  new BioclipseJobUpdateHook<String>("SMILES") {
//            public void completeReturn(String object) {
//                SMILESJob = null;
//                CDKMoleculeUtils.setProperty( mol,
//                                              MolProperty.SMILES.name(),
//                                              object );
//                Display.getDefault().asyncExec( new Runnable() {
//                    public void run() {
//                        widget.setSelection( widget.getSelection());
//                    }
//                });
//            }
//        });
        // removed inchi auto generation bug 1257
//        try {
//            if(InChIJob!=null) {
//                if(InChIJob.cancel());
//                InChIJob.join();
//            }
//        } catch ( InterruptedException e ) {
//            logger.debug( "InChI job interrupted" );
//        }
//        InChIJob = inchi.generate( mol, new BioclipseJobUpdateHook<InChI>(
//                                        "InChI generation") {
//            @Override
//            public void completeReturn( InChI object ) {
//                InChIJob = null;
//                CDKMoleculeUtils.setProperty( mol,
//                                              MolProperty.InChI.name(),
//                                              object );
//                Display.getDefault().asyncExec( new Runnable() {
//                    public void run() {
//                        widget.setSelection( widget.getSelection());
//                    }
//                });
//            }
//        });
    }

    protected final void fireStructureChanged() {
        fireEvent(STRUCUTRE_CHANGED_EVENT);
    }

    private final void fireEvent(String event) {
        invalidateProperties();
        firePropertyChangedEvent(
                   new PropertyChangeEvent( this,
                                            event,
                                            null, null) );
    }

    protected final void fireModelLoaded() {
        fireEvent(MODEL_LOADED);
    }

    public void resourceChanged( IResourceChangeEvent event ) {

        switch (event.getType()) {
            case IResourceChangeEvent.POST_CHANGE:
                final boolean[] val = new boolean[1];
                IResourceDelta delta = event.getDelta();
                IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

                    public boolean visit( IResourceDelta delta )
                                                                throws CoreException {
                        if( delta.getFlags() != IResourceDelta.MARKERS
                            && delta.getResource().getType() == IResource.FILE) {
                            if(delta.getKind() == IResourceDelta.REMOVED) {
                                IResource resource = delta.getResource();
                                ICDKMolecule mol = getCDKMolecule();
                                if( mol != null
                                    && resource.equals( mol.getResource() )) {
                                    val[0]=true;
                                }
                            }
                        }
                        return true;
                    }

                };
                try {
                    delta.accept( visitor );
                    if(val[0])
                        Display.getDefault().asyncExec( new Runnable() {
                            public void run() {
                                getSite().getPage().closeEditor(
                                    JChemPaintEditor.this, true );
                            }
                        });
                } catch ( CoreException e ) {
                    LogUtils.handleException( e, logger,
                             net.bioclipse.cdk.jchempaint.Activator.PLUGIN_ID );
                }

                break;
            default:
                break;
        }
    }
}
