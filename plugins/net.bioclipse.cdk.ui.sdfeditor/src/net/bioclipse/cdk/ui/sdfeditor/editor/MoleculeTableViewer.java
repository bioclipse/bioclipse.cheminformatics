/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.CDKMoleculeTransfer;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.CDKMoleculePropertySource;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.view.AtomContainerTransfer;
import net.bioclipse.cdk.ui.views.IFileMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.sourceforge.nattable.GridRegionEnum;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.action.SelectCellAction;
import net.sourceforge.nattable.config.DefaultBodyConfig;
import net.sourceforge.nattable.config.DefaultColumnHeaderConfig;
import net.sourceforge.nattable.config.DefaultRowHeaderConfig;
import net.sourceforge.nattable.config.SizeConfig;
import net.sourceforge.nattable.data.IColumnHeaderLabelProvider;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.event.matcher.MouseEventMatcher;
import net.sourceforge.nattable.model.DefaultNatTableModel;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.renderer.AbstractCellRenderer;
import net.sourceforge.nattable.support.EventBindingSupport;
import net.sourceforge.nattable.typeconfig.style.DefaultStyleConfig;
import net.sourceforge.nattable.typeconfig.style.DisplayModeEnum;
import net.sourceforge.nattable.typeconfig.style.IStyleConfig;
import net.sourceforge.nattable.util.GUIHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.interfaces.IAtomContainer;


public class MoleculeTableViewer extends ContentViewer {

    public final static int STRUCTURE_COLUMN_WIDTH = 200;
    Logger logger = Logger.getLogger( MoleculeTableViewer.class );

    NatTable table;
    JCPCellPainter cellPainter;

    private int currentSelected;

    private Runnable dblClickHook;

    public MoleculeTableViewer(Composite parent, int style) {

        cellPainter = new JCPCellPainter();

        DefaultNatTableModel model = new DefaultNatTableModel();

        IColumnHeaderLabelProvider columnHeaderLabelProvider = new IColumnHeaderLabelProvider() {

            public String getColumnHeaderLabel( int col ) {
                List<Object> prop = getColumnHandler().getProperties();
                if(col == 0)
                    return "2D-structure";
                if(col<prop.size()+1 )
                    return prop.get(col-1).toString();
                return "";
            }
        };

        DefaultRowHeaderConfig rowHeaderConfig = new DefaultRowHeaderConfig();
        rowHeaderConfig.setRowHeaderColumnCount(1);
        SizeConfig rowHeaderColumnWidthConfig = new SizeConfig();
        rowHeaderColumnWidthConfig.setDefaultSize(STRUCTURE_COLUMN_WIDTH/3);
        //              columnWidthConfig.setDefaultSize(150);
        rowHeaderColumnWidthConfig.setDefaultResizable(true);
        rowHeaderColumnWidthConfig.setIndexResizable( 1, true );
        rowHeaderConfig.setRowHeaderColumnWidthConfig( rowHeaderColumnWidthConfig );

        DefaultBodyConfig bodyConfig = new DefaultBodyConfig(new IDataProvider() {



            public int getColumnCount() {
                if(getDataProvider()==null) return 0;
                return getDataProvider().getColumnCount();
            }

            public int getRowCount() {
                if(getDataProvider()==null) return 0;
                return getDataProvider().getRowCount()+1;
            }

            public Object getValue( int row, int col ) {
                if(getDataProvider()==null) return null;
                return getDataProvider().getValue( row, col );
            }

        });

        bodyConfig.setCellRenderer( new AbstractCellRenderer() {

            ICellPainter textPainter = new TextCellPainter();

            DefaultStyleConfig selectedStyle = new DefaultStyleConfig(ICellPainter.COLOR_LIST_SELECTION, GUIHelper.COLOR_BLACK, null, null);
            @Override
            public IStyleConfig getStyleConfig(String displayMode, int row, int col) {
                if (DisplayModeEnum.SELECT.name().equals(displayMode)) {
                    return selectedStyle;
                }
                return super.getStyleConfig(displayMode, row, col);
            }

            @Override
            public ICellPainter getCellPainter( int row, int col ) {

                if(col == 0)
                    return cellPainter;
                return textPainter;
            }

            public String getDisplayText( int row, int col ) {

                return getDataProvider().getValue( row, col ).toString();
            }

            public Object getValue( int row, int col ) {

                return getDataProvider().getValue( row, col );
            }

        });

        model.setBodyConfig(bodyConfig);
        model.setRowHeaderConfig(rowHeaderConfig);
        model.setColumnHeaderConfig( new DefaultColumnHeaderConfig(columnHeaderLabelProvider));

        model.setSingleCellSelection( false );
        model.setMultipleSelection( true );
//        model.setMultipleSelection( true );
//        model.


        SizeConfig columnWidthConfig = model.getBodyConfig().getColumnWidthConfig();
        columnWidthConfig.setDefaultSize(STRUCTURE_COLUMN_WIDTH/2);
        columnWidthConfig.setInitialSize( 0, STRUCTURE_COLUMN_WIDTH );
        //              columnWidthConfig.setDefaultSize(150);
        columnWidthConfig.setDefaultResizable(true);
        columnWidthConfig.setIndexResizable(1, true);

        // Row heights
        SizeConfig rowHeightConfig = model.getBodyConfig().getRowHeightConfig();
        rowHeightConfig.setDefaultSize(STRUCTURE_COLUMN_WIDTH);
        rowHeightConfig.setDefaultResizable(true);
        //                rowHeightConfig.setIndexResizable(1, false);

        // NatTable
        table = new NatTable(parent,
                     SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE
                     | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL,
                     model
        );

        EventBindingSupport eventBindingSupport =table.getEventBindingSupport();
        eventBindingSupport.registerSingleClickBinding(
                 new MouseEventMatcher( SWT.COMMAND,
                                        GridRegionEnum.BODY.toString(), 1),
                                      new SelectCellAction(table, false, true));
        Listener listener = new Listener() {

            public void handleEvent( Event event ) {

                switch(event.type) {
                    case SWT.MouseDoubleClick:
                        doubleClickHook();
                        break;
                    case SWT.SELECTED:
                    case SWT.MouseUp:
                        updateSelection( getSelection() );
                        break;
                    case SWT.MouseDown:
                        table.dragDetect( event );
                        break;
                }
            }
        };
        table.addListener( SWT.SELECTED, listener);
        table.addListener( SWT.MouseUp, listener );
        table.addListener( SWT.MouseDoubleClick, listener );
        table.addListener( SWT.MouseDown, listener );

        table.setDragDetect( true );

        ScrollBar vSb = table.getVerticalBar();
        vSb.setIncrement( 1 );
        vSb.setPageIncrement( 1 );
        table.scrollVBarUpdate( vSb );


        enableDrop();

    }

    private void enableDrop() {
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
        DropTarget target = new DropTarget( table, operations );

        final FileTransfer fileTransfer = FileTransfer.getInstance();
        final AtomContainerTransfer acTransfer = AtomContainerTransfer.getInstance();
        final CDKMoleculeTransfer molTranfer = CDKMoleculeTransfer.getInstance();
        final LocalSelectionTransfer locationSelectionTransfer = LocalSelectionTransfer.getTransfer();
        Transfer[] transfers = new Transfer[] { molTranfer,
                                                acTransfer,
                                                fileTransfer,
                                                //pluginTransfer,
                                                locationSelectionTransfer};
        target.setTransfer( transfers );
        target.addDropListener( new DropTargetListener() {


            public void drop( DropTargetEvent event ) {

                if(locationSelectionTransfer.isSupportedType( event.currentDataType )) {
                    IStructuredSelection sel = (IStructuredSelection) locationSelectionTransfer.getSelection();
                    for(Object o: sel.toArray()){
                        if(o instanceof IFile) {
                            insert((IFile)o);
                        }
                    }
                } else if(acTransfer.isSupportedType( event.currentDataType )) {
                    insert((IAtomContainer)event.data);

                } else if(molTranfer.isSupportedType( event.currentDataType )) {
                    ICDKMolecule[] mols = (ICDKMolecule[])event.data;
                    List<ICDKMolecule> molsToInsert = new ArrayList<ICDKMolecule>(mols.length);
                    for(ICDKMolecule mol:mols) {
                        try {
                            molsToInsert.add(new CDKMolecule( (IAtomContainer)mol.getAtomContainer().clone()));
                        } catch ( CloneNotSupportedException e ) {
                            logger.warn( "Failed to clone molecule on drop" , e);
                        }
                    }
                    insert(molsToInsert.toArray( new ICDKMolecule[molsToInsert.size()] ));

                } else if(fileTransfer.isSupportedType( event.currentDataType )) {
                    String[] files =  (String[])event.data;
                    for(String file:files) {
                        IFile resource = ResourcePathTransformer.getInstance()
                                                            .transform( file );
                        insert( resource );
                    }

                } else
                    System.out.println("Other: "+event.data);

                table.redraw();
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

    private void initializeDrag() {
        DragSource ds = new DragSource( table, DND.DROP_MOVE );
        final CDKMoleculeTransfer cdkTransfer = CDKMoleculeTransfer.getInstance();
        ds.setTransfer( new Transfer[] {cdkTransfer} );

        ds.addDragListener( new DragSourceListener() {

            public void dragStart( DragSourceEvent event ) {

                logger.info("Drag started for mol-table");

            }

            public void dragSetData( DragSourceEvent event ) {


            }

            public void dragFinished( DragSourceEvent event ) {

                // TODO Auto-generated method stub

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
        int[] selection =table.getSelectionModel().getSelectedRows();
        int first = selection.length!=0?selection[0]:-1;
        Object input = getInput();
        if(input instanceof IFileMoleculesEditorModel && first!=-1)
            ((IFileMoleculesEditorModel)input).insert( first, molecules );
        else
            ((IMoleculesEditorModel)input).instert( molecules );
    }

    @Override
    public Control getControl() {

        return table;
    }

    public static class MolTableElement implements IAdaptable {

        final IMoleculesEditorModel model;
        final int index;

        CDKMoleculePropertySource pSource;
        public MolTableElement(int index, IMoleculesEditorModel model) {
            this.model = model;
            this.index = index;
        }

        @SuppressWarnings("unchecked")
        public Object getAdapter( Class adapter ) {

            if(adapter.isAssignableFrom( ICDKMolecule.class )) {
                return model.getMoleculeAt( index );
            }
            if (adapter.isAssignableFrom(IPropertySource.class)) {
                ICDKMolecule mol = model.getMoleculeAt( index );
                if(mol instanceof CDKMolecule) {
                    if(pSource == null)
                        pSource=  new CDKMoleculePropertySource((CDKMolecule) mol);
                    return pSource;
                } else
                    return null;
            }
            return Platform.getAdapterManager().getAdapter(this, adapter);
        }
    }

    public static class MolTableSelection implements ISelection, IAdaptable{

        IMoleculesEditorModel model;
        int[] selection;

        public MolTableSelection(int[] selection,IMoleculesEditorModel model) {
            this.selection = selection;
            this.model = model;
        }

        public boolean isEmpty() {
            return selection.length==0;
        }

        @SuppressWarnings("unchecked")
        public Object getAdapter( Class adapter ) {

            if(adapter.isAssignableFrom( IMoleculesEditorModel.class ) ) {
                final IMoleculesEditorModel editorModel = new IMoleculesEditorModel() {

                    public ICDKMolecule getMoleculeAt( int index ) {
                        return model.getMoleculeAt( selection[index] );
                    }

                    public int getNumberOfMolecules() {
                        return selection.length;
                    }

                    public void markDirty( int index,
                                           ICDKMolecule moleculeToSave ) {

                        throw new UnsupportedOperationException();

                    }

                    public void save() {
                        throw new UnsupportedOperationException();
                    }
                    public Collection<Object> getAvailableProperties() {

                        return model.getAvailableProperties();
                    }

                    public <T> void setPropertyFor( int moleculeIndex,
                                                    String property, T value ) {

                        model.setPropertyFor( moleculeIndex, property, value );

                    }
                    public void instert( ICDKMolecule... molecules ) {
                        throw new UnsupportedOperationException();
                    }
                    public void delete( int index ) {
                        throw new UnsupportedOperationException();
                    }
                };
                return editorModel;
            }
            return null;
        }
    }

    @Override
    public ISelection getSelection() {

        if(getContentProvider() instanceof MoleculeTableContentProvider) {

            int[] selected = table.getSelectionModel().getSelectedRows();

            if(selected.length==0) {
                currentSelected = -1;
                return StructuredSelection.EMPTY;
            }
            currentSelected = selected[0];

            IMoleculesEditorModel model;
            if(getInput() instanceof IMoleculesEditorModel) {
                model = (IMoleculesEditorModel) getInput();
                if(selected.length == 1) {
                    return new StructuredSelection(
                               new MolTableElement( selected[0], model));
                }else {

                    return new  StructuredSelection(
                             new MolTableSelection(selected,model));
                }
            }
        }

        return StructuredSelection.EMPTY;
    }

    private IDataProvider getDataProvider() {
        return (IDataProvider)getContentProvider();
    }

    private IMoleculeTableColumnHandler getColumnHandler() {
        return (IMoleculeTableColumnHandler) getContentProvider();
    }

    @Override
    public void refresh() {
        if(!table.isDisposed()) {
            table.reset();
            table.redraw();
            table.updateResize();
            table.update();
        }
    }

    @Override
    public void setSelection( ISelection selection, boolean reveal ) {

        // TODO Auto-generated method stub

    }

     IRenderer2DConfigurator getRenderer2DConfigurator() {
        return cellPainter.getRenderer2DConfigurator();
    }

     void setRenderer2DConfigurator(
                             IRenderer2DConfigurator renderer2DConfigurator ) {
        cellPainter.setRenderer2DConfigurator( renderer2DConfigurator);
    }


    protected void updateSelection(ISelection selection) {
        SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
        fireSelectionChanged(event);
    }


    public int getFirstSelected() {

        return currentSelected;
    }

    public void setDoubleClickHook(Runnable hook) {
        dblClickHook = hook;
    }
    protected void doubleClickHook() {
        if(dblClickHook!=null) {
            dblClickHook.run();
        }
    }
}
