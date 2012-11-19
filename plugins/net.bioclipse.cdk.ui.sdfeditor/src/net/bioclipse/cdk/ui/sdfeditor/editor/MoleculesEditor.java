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
package net.bioclipse.cdk.ui.sdfeditor.editor;

import static net.bioclipse.cdk.ui.sdfeditor.Activator.STRUCTURE_COLUMN_WIDTH;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.CDKMoleculeTransfer;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.MoleculesIndexEditorInput;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.jchempaint.view.AtomContainerTransfer;
import net.bioclipse.cdk.ui.sdfeditor.business.IMoleculeTableManager;
import net.bioclipse.cdk.ui.sdfeditor.business.MappingEditorModel;
import net.bioclipse.cdk.ui.sdfeditor.business.SDFIndexEditorModel;
import net.bioclipse.cdk.ui.sdfeditor.business.SDFileIndex;
import net.bioclipse.cdk.ui.views.IFileMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;
import net.bioclipse.jobs.BioclipseUIJob;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.LabelStack;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.IRenderer;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

public class MoleculesEditor extends EditorPart implements
        //ISelectionProvider,
        ISelectionListener {


    Logger logger = Logger.getLogger( MoleculesEditor.class );

    public List<String> propertyHeaders = new ArrayList<String>();

    private MoleculeTableViewer molTableViewer;

    private BioclipseJob<Void> parseJob;

    private BioclipseJob<SDFIndexEditorModel> indexJob;

    private boolean dirty;

    private MolTableOutline outlinePage;

    private IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(STRUCTURE_COLUMN_WIDTH)) {
                molTableViewer.resizeStructureColumn();
            }
        }
    };

    public MoleculesEditor() {
    }

    public MoleculeTableContentProvider getContentProvider() {
        IContentProvider provider = molTableViewer.getContentProvider();
        if(provider instanceof MoleculeTableContentProvider)
            return (MoleculeTableContentProvider) provider;
        return null;
    }

    public IMoleculesEditorModel getModel() {

        return (IMoleculesEditorModel) molTableViewer.getInput();
    }

    public MoleculeTableViewer getMolTableViewer() {
        return molTableViewer;
    }

    @Override
    public void doSave( IProgressMonitor monitor ) {
        // TODO Use a SDF-iterator and a appending writer to create a SDfile from the file and index.
    }

    @Override
    public void doSaveAs() {
        // TODO see doSave(...)
    }

    @Override
    public void init( IEditorSite site, IEditorInput input )
                                                      throws PartInitException {

        super.setSite( site );
        super.setInput( input );
        setPartName(input.getName() );
        net.bioclipse.cdk.ui.sdfeditor.Activator.getDefault()
            .getPreferenceStore().addPropertyChangeListener( propertyListener );
        // TODO listen to selections check and focus on selected element from
        // common navigator, load it and get columns
    }

    @Override
    public boolean isDirty() {
        if(getModel() instanceof SDFIndexEditorModel)
            return ((SDFIndexEditorModel)getModel()).isDirty();
        else
            return dirty;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl( Composite parent ) {

        MenuManager headerMgr = new MenuManager("Molecuels table","net.bioclipse.cdk.ui.sdfeditor.column.menu");
        MenuManager bodyMgr = new MenuManager("net.bioclipse.cdk.ui.sdfeditor.menu","net.bioclipse.cdk.ui.sdfeditor.menu");
        headerMgr.setRemoveAllWhenShown(true);
        bodyMgr.setRemoveAllWhenShown(true);
        bodyMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });

        molTableViewer = new MoleculeTableViewer(parent,SWT.NONE, headerMgr,bodyMgr);

        getSite().registerContextMenu( "net.bioclipse.cdk.ui.sdfeditor.column.menu",headerMgr, molTableViewer);
        getSite().registerContextMenu( "net.bioclipse.cdk.ui.sdfeditor.menu",bodyMgr, molTableViewer);
//        molTableViewer.setContentProvider( contentProvider =
//                                        new MoleculeViewerContentProvider() );

        //molTableViewer.setInput( getEditorInput() );
        getIndexFromInput( getEditorInput() );


//        Menu menu = menuMgr.createContextMenu(molTableViewer.getControl());
//        molTableViewer.getControl().setMenu(menu);
//        logger.debug( "Menu id for SDFEditor " +menuMgr.getId());

        getSite().setSelectionProvider( molTableViewer );
        enableDrop();
        initDrag();

    }

    private void initDrag() {
        MoleculeTableViewer viewer = getMolTableViewer();
        int ops = DND.DROP_COPY | DND.DROP_MOVE;
        final FileTransfer fileTransfer = FileTransfer.getInstance();
        final AtomContainerTransfer acTransfer = AtomContainerTransfer.getInstance();
        final CDKMoleculeTransfer molTranfer = CDKMoleculeTransfer.getInstance();
        final PluginTransfer plugTransfer = PluginTransfer.getInstance();
        final LocalSelectionTransfer localSelTransfer =
                                           LocalSelectionTransfer.getTransfer();
        Transfer[] transfers = new Transfer[] { //molTranfer,
                                                acTransfer,
                                                plugTransfer,
                                                //fileTransfer,
                                                //localSelTransfer,
                                                };
        viewer.addDragSupport(ops, transfers, new DragSourceAdapter() {
            Point start;

            @Override
            public void dragStart( DragSourceEvent event ) {
                Control widget = ((DragSource)event.widget).getControl();
                if(widget instanceof NatTable) {
                    NatTable table = ((NatTable)widget);
                    LabelStack regionLabels = table.getRegionLabelsByXY(event.x, event.y);
                    if(regionLabels!= null && regionLabels.hasLabel(GridRegion.BODY)) {
                        start = new Point(event.x,event.y);
                        return;
                    }
                }
                event.doit = false;
            }
            @Override
            public void dragSetData( DragSourceEvent event ) {
                if(start==null){
                    event.doit = false;
                    return;
                }
                MoleculeTableViewer viewer = getMolTableViewer();
                NatTable table = (NatTable) viewer.getControl();
                int col = table.getColumnPositionByX( start.x );
                int row = table.getRowPositionByY( start.y );
                col = table.getRowIndexByPosition( col );
                row = table.getRowIndexByPosition( row );
                MoleculeTableContentProvider mtcp = (MoleculeTableContentProvider)
                                                    viewer.getContentProvider();
                Object data = mtcp.getDataValue( col, row );
                logger.debug( "Draging from MolTable: ("+row+", "+col+")= "+(data==null?"null":data.toString()) );
                if(data instanceof ICDKMolecule) {
                    ICDKMolecule mol = (ICDKMolecule) data;
//                    if( CDKMoleculeTransfer.getInstance().isSupportedType( event.dataType ) )
//                        event.data = new ICDKMolecule[] { mol };
//                    else
                        if( AtomContainerTransfer.getInstance().isSupportedType( event.dataType ) )
                            event.data = mol.getAtomContainer();
                        else if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
                            byte[] bData = AtomContainerTransfer.getInstance().toByteArray(mol.getAtomContainer());
                            event.data = new PluginTransferData("net.bioclipse.cdk.jchempaint.atomContainerDrop", bData);
                        }else event.doit = false;
                } else if ( TextTransfer.getInstance().isSupportedType( event.dataType )) {
                    event.data = data.toString();
                }
                else if( plugTransfer.isSupportedType(event.dataType)) {
                    event.data = new PluginTransferData( "net.bioclipse.cdk.jchempaint.atomContainerDrop",
                                                         data.toString().getBytes());
                } else event.doit = false;
            }
        });
    }

    private void enableDrop() {
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;

        final FileTransfer fileTransfer = FileTransfer.getInstance();
        final AtomContainerTransfer acTransfer = AtomContainerTransfer.getInstance();
        final CDKMoleculeTransfer molTranfer = CDKMoleculeTransfer.getInstance();
        final LocalSelectionTransfer localSelTransfer =
                                           LocalSelectionTransfer.getTransfer();
        Transfer[] transfers = new Transfer[] { molTranfer,
                                                acTransfer,
                                                fileTransfer,
                                                localSelTransfer};
        molTableViewer.addDropSupport( operations, transfers,
                                       new DropTargetAdapter() {

            public void drop( DropTargetEvent event ) {
                if(event.data == null){
                    event.detail = DND.DROP_NONE;
                    return;
                }
                if(localSelTransfer.isSupportedType( event.currentDataType )) {
                    IStructuredSelection sel = (IStructuredSelection)
                                    localSelTransfer.getSelection();
                    for(Object o: sel.toArray()){
                        if(o instanceof IFile) {
                            insert((IFile)o);
                        }
                    }
                } else if(acTransfer.isSupportedType( event.currentDataType )) {
                    insert((IAtomContainer)event.data);

                } else if(molTranfer.isSupportedType( event.currentDataType )) {
                    ICDKMolecule[] mols = (ICDKMolecule[])event.data;
                    List<ICDKMolecule> molsToInsert;
                    molsToInsert = new ArrayList<ICDKMolecule>(mols.length);
                    for(ICDKMolecule mol:mols) {
                        try {
                            molsToInsert.add(new CDKMolecule( (IAtomContainer)
                                              mol.getAtomContainer().clone()));
                        } catch ( CloneNotSupportedException e ) {
                            logger.warn( "Failed to clone molecule on drop" , e);
                        }
                    }
                    insert(molsToInsert.toArray( new ICDKMolecule[0] ));

                } else if(fileTransfer.isSupportedType( event.currentDataType )) {
                    String[] files =  (String[])event.data;
                    for(String file:files) {
                        IFile resource = ResourcePathTransformer.getInstance()
                        .transform( file );
                        insert( resource );
                    }

                } else
                    System.out.println("Other: "+event.data);

                molTableViewer.refresh();
            }

            public void dragOver( DropTargetEvent event ) {
                event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
            }

            public void dragOperationChanged( DropTargetEvent event ) {
                translateDefault( event );
            }

            public void dropAccept( DropTargetEvent event ) {}
            public void dragLeave( DropTargetEvent event ) {}

            public void dragEnter( DropTargetEvent event ) {
                translateDefault( event );
                for(TransferData tfData:event.dataTypes) {
                    if(molTranfer.isSupportedType( tfData )) {
                        event.currentDataType = tfData;
                        break;
                    }else
                        if(fileTransfer.isSupportedType( tfData )){
                            if (event.detail != DND.DROP_COPY) {
                                event.detail = DND.DROP_NONE;
                            }
                            break;
                        }
                }
            }

            private void translateDefault(DropTargetEvent event) {
                if( event.detail == DND.DROP_DEFAULT){
                    if((event.operations & DND.DROP_COPY) !=0)
                        event.detail = DND.DROP_COPY;
                    else
                        event.detail = DND.DROP_NONE;
                }
            }
        });
    }

    private void insert(IAtomContainer atomContainer) {
        ICDKMolecule molecule = new CDKMolecule( atomContainer );
        insert( molecule );
    }
    private void insert(IFile file) {
        List<ICDKMolecule> mols;
        try {
            mols = Activator.getDefault().getJavaCDKManager().loadMolecules( file );
            insert(mols.toArray( new ICDKMolecule[mols.size()] ));
        } catch ( IOException e ) {
            logger.warn( "Could not inster file from drop",e );
        } catch ( BioclipseException e ) {
            logger.warn( "Could not inster file from drop",e );
        } catch ( CoreException e ) {
            logger.warn( "Could not inster file from drop",e );
        }
    }

    private void insert(ICDKMolecule... molecules) {
        int[] selection = molTableViewer.getSelectedRows();
        int first = selection.length!=0?selection[0]:-1;
        IMoleculesEditorModel input = molTableViewer.getInput();
        if(input instanceof IFileMoleculesEditorModel && first!=-1)
            ((IFileMoleculesEditorModel)input).insert( first, molecules );
        else
            input.instert( molecules );
        setDirty( true );
        if(outlinePage!=null) {
        	outlinePage.setInput( input);
        }
        refresh();
    }


    @SuppressWarnings("unchecked")
    private static <T> T adapt(IAdaptable adaptable, Class<T> clazz) {
        return (T) adaptable.getAdapter( clazz );
    }

    private static boolean isKindOf(IContentType in,String contentTypeID) {
        IContentType testType = Platform.getContentTypeManager()
                .getContentType( contentTypeID );

        return in!=null && testType!=null && in.isKindOf( testType );
    }

    private void getIndexFromInput(IEditorInput editorInput) {

        ICDKManager cdkManager = Activator.getDefault().getJavaCDKManager();

        SDFileIndex input = null;
        input = adapt(editorInput, SDFileIndex.class);


        if(input==null) {
            IFile file = adapt(editorInput,IFile.class);
            if(file!=null) {

                IContentDescription contentDescr;
                try {
                    contentDescr = file.getContentDescription();
                }catch ( CoreException e) {
                    contentDescr = null;
                }
                final IFile inputFile = file;
                if(contentDescr != null && isKindOf( contentDescr.getContentType(),
                             "net.bioclipse.contenttypes.smi" )) {
                    try {
                        cdkManager.loadSMILESFile( file,
                                                   new BioclipseUIJob<List<ICDKMolecule>>() {
                            @Override
                            public void runInUI() {
                                List<ICDKMolecule> list = getReturnValue();
                                if(list == null || list.isEmpty()) {
                                	list = new ArrayList<ICDKMolecule>();
                                }
                                // FIXME there should be a IMoleculesEditorModel content provider
                                setInput( new ListMoleculesEditorModelWithResource( list ,inputFile));
                          }

                      } );
                    }catch (RuntimeException e) {
                    	logger.warn("could not read smi file",e);
                    	if(e.getCause() instanceof IOException) {
                    		Display.getDefault().asyncExec(new Runnable() {
                            	@Override
                            	public void run() {
                                    setInput( new ListMoleculesEditorModelWithResource( new ArrayList<ICDKMolecule>() ,inputFile));
                            	}
                            });
                    	}
                    }catch(IOException e) {
                        LogUtils.debugTrace( logger, e );
                    } catch ( CoreException e ) {
                        LogUtils.debugTrace( logger, e );
                    }

                }else {
                    final IMoleculeTableManager molTable =
                    net.bioclipse.cdk.ui.sdfeditor.Activator.getDefault()
                    .getMoleculeTableManager();

                    indexJob = molTable.createSDFIndex( file,
                         new BioclipseJobUpdateHook<SDFIndexEditorModel>(
                                                  "Create Index fro SDFile") {
                        public void completeReturn(final SDFIndexEditorModel sdfModel) {
                            indexJob = null;
                            Display.getDefault().asyncExec( new Runnable() {
                               public void run() {
                                   setInput( new MappingEditorModel( sdfModel ) );
                               };
                            });
                            parseJob = molTable.parseProperties( sdfModel ,
                                  Collections.<String>emptySet(),
                             new BioclipseJobUpdateHook<Void>("Parse SDFile") {
                                @Override
                                public void completeReturn( Void object ) {
                                    parseJob = null;
                                }
                            });
                        };
                    }
                    );
                }
            }else {
            final List<ICDKMolecule> list = adapt(editorInput,List.class);
            if(list!=null) {
                  setInput( new ListMoleculesEditorModel( list ) );
            }else {
                IMoleculesEditorModel molEditorModel = (IMoleculesEditorModel)
                    editorInput.getAdapter( IMoleculesEditorModel.class );
                if(molEditorModel!=null) {
                    if(molEditorModel instanceof SDFIndexEditorModel) {
                        molEditorModel = new MappingEditorModel(
                                         (SDFIndexEditorModel)molEditorModel );
                    }
                    setInput( molEditorModel );
                }
            }
        }
        }
//        molTableViewer.setInput( input );
    }

    private void setInput(IMoleculesEditorModel model) {
        molTableViewer.setContentProvider( new MoleculeTableContentProvider() );
        molTableViewer.setInput( model );
        if(outlinePage!=null) outlinePage.setInput(model);
        molTableViewer.refresh();
    }

    void reactOnSelection( ISelection selection ) {

        //if ( element instanceof ICDKMolecule )
//            if (((IStructuredSelection)viewer.getSelection()).toList()
//                                            .containsAll( selection.toList() ))
//                return;
//            else
//        if(viewer != null)
//                setSelectedRows(selection);
    }

    @Override
    public void setFocus() {

     molTableViewer.getControl().setFocus();

    }

    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
        logger.debug( "Selection has chaged" + this.getClass().getName() );
        if(part!=null) {
            logger.debug( part.toString() + this.getSite().getPart().toString());
            if(part.equals( this )) return;
        }
        setSelectedRows( selection );
//        if( part != null && part.equals( this )) return;
//        if( selection == null || selection.isEmpty() ) {
//            if(!viewer.getSelection().isEmpty())
//                viewer.setSelection( selection );
//            return;
//        }
//        if(selection instanceof IStructuredSelection)
//            reactOnSelection( (IStructuredSelection) selection );
        //viewer.setSelection( selection );
    }

    public ISelection getSelection() {
            return molTableViewer.getSelection();
    }

    private ISelection getSelectedRows() {
//        viewer.getSelection();
//        viewer.getTopRow();

        return StructuredSelection.EMPTY;

    }
    private void setSelectedRows(ISelection selection) {
        // mapping between selections and index
        //viewer.setSelection(  );
    }

    public IRenderer2DConfigurator getRenderer2DConfigurator() {
        return molTableViewer.getRenderer2DConfigurator();
    }

    public void setRenderer2DConfigurator(
                             IRenderer2DConfigurator renderer2DConfigurator ) {
        molTableViewer.setRenderer2DConfigurator( renderer2DConfigurator );
    }


    protected void setupDragSource() {

        MoleculeTableViewer viewer = molTableViewer;
        int ops = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer()};
        DragSource source = new DragSource( viewer.getControl(), ops );
        source.setTransfer( transfers );
        source.addDragListener( new DragSourceAdapter() {

            public void dragStart( DragSourceEvent event ) {
                ISelection sel = getSelectedRows();
                if(sel != null && !sel.isEmpty()) {
                    LocalSelectionTransfer.getTransfer().setSelection( sel );
                    event.doit = true;
                } else
                    event.doit = false;
            }

            public void dragSetData( DragSourceEvent event ) {
                LocalSelectionTransfer selTransfer = LocalSelectionTransfer
                                                         .getTransfer();
                ISelection selection = selTransfer.getSelection();

                if ( selTransfer.isSupportedType( event.dataType )) {
                    event.data = selection;
                } else {
                    IStructuredSelection selection1 =
                        (IStructuredSelection) getSelectedRows();
                    List<EditorInputData> data = new ArrayList<EditorInputData>();
                    for(Object o : selection1.toList()) {
                        MoleculesIndexEditorInput input =
                            new MoleculesIndexEditorInput((SDFElement)o);
                        data.add( EditorInputTransfer
                                  .createEditorInputData(
                                    "net.bioclipse.cdk.ui.editors.jchempaint",
                                      input ));
                    }
                    event.data = data.toArray( new EditorInputData[0] );
                }

            }

            public void dragFinished( DragSourceEvent event ) {
            }

        });
    }

    public void setUseExtensionGenerators( boolean useGenerators ) {

        molTableViewer.cellPainter.setUseExtensionGenerators( useGenerators );
    }

    public void refresh() {

        molTableViewer.refresh();
    }

    public void setDirty( boolean b ) {
        if(dirty!=b) {
            dirty = b;
            firePropertyChange( IEditorPart.PROP_DIRTY );
        }
    }

    @Override
    public void dispose() {
        if(parseJob!=null) parseJob.cancel();
        if(indexJob!=null) indexJob.cancel();
        net.bioclipse.cdk.ui.sdfeditor.Activator.getDefault()
        .getPreferenceStore().removePropertyChangeListener( propertyListener );
        super.dispose();
    }

    @Override
    public Object getAdapter( Class adapter ) {

        if ( IContentOutlinePage.class.equals( adapter ) ) {
            if ( outlinePage == null ) {
                outlinePage = new MolTableOutline();
                Object model = getMolTableViewer().getInput();
                if(model instanceof IMoleculesEditorModel)
                    outlinePage.setInput( (IMoleculesEditorModel) model );
            }
            return outlinePage;
        } else if( adapter.isAssignableFrom(AtomContainerRenderer.class)) {
        	return getMolTableViewer().cellPainter.renderer;
        }
        return super.getAdapter( adapter );
    }
}

class ListMoleculesEditorModelWithResource extends ListMoleculesEditorModel {
    private Logger logger = Logger.getLogger(ListMoleculesEditorModelWithResource.class);
	IFile inputFile;
	public ListMoleculesEditorModelWithResource(List<ICDKMolecule> list,IFile file) {
		super(list);
		inputFile = file;
	}
	private String generateHeader(List<String> keys) {
		StringBuilder builder = new StringBuilder();
		builder.append("SMILES");
		for (String key : keys) {
			builder.append(';');
			builder.append(key);
		}
		return builder.toString();
	}

	private String generateLine(ICDKMolecule molecule, List<String> keys)
			throws BioclipseException {
		StringBuilder builder = new StringBuilder();
		builder.append(molecule.toSMILES());
		for (String key : keys) {
			Object val = molecule.getProperty(key,
					IMolecule.Property.USE_CACHED_OR_CALCULATED);
			builder.append(';');
			if(val!=null)
				builder.append(val.toString());
		}
		return builder.toString();
	}

	@Override
	public void save() {
		try {
			StringWriter sWriter = new StringWriter();
			BufferedWriter writer = new BufferedWriter(sWriter);
			List<String> keys = new ArrayList<String>();
			for (Object p : getAvailableProperties()) {
				keys.add(p.toString());
			}
			keys.remove("net.bioclipse.cdk.domain.property.SMILES");
			writer.write(generateHeader(keys));
			writer.append('\n');
			for (int i = 0; i < getNumberOfMolecules(); i++) {
				ICDKMolecule mol = getMoleculeAt(i);
				try {
					writer.append(generateLine(mol, keys));
					writer.append('\n');
				} catch (BioclipseException e) {
					logger.error("Could not save molecule at position "
							+ (i + 1));
				}
			}
			writer.flush();
			ByteArrayInputStream bis = new ByteArrayInputStream(sWriter
					.toString().getBytes());
			inputFile.setContents(bis, true, true, null);
		} catch (IOException e) {
			LogUtils.handleException(e, logger,
					"net.bioclipse.cdk.ui.sdfeditor");
			throw new RuntimeException("Faild to save " + inputFile.getName(),
					e);
		} catch (CoreException e) {
			LogUtils.handleException(e, logger,
					"net.bioclipse.cdk.ui.sdfeditor");
			throw new RuntimeException("Faild to save " + inputFile.getName(),
					e);
		}
	}
}
