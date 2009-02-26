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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.MoleculesIndexEditorInput;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.ui.sdfeditor.MoleculesOutlinePage;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.DefaultBodyConfig;
import net.sourceforge.nattable.config.DefaultRowHeaderConfig;
import net.sourceforge.nattable.config.SizeConfig;
import net.sourceforge.nattable.editor.EditorSelectionEnum;
import net.sourceforge.nattable.editor.ICellEditor;
import net.sourceforge.nattable.editor.IEditController;
import net.sourceforge.nattable.model.DefaultNatTableModel;
import net.sourceforge.nattable.renderer.AbstractCellRenderer;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.openscience.cdk.interfaces.IAtomContainer;

public class MoleculesEditor extends EditorPart implements
        //ISelectionProvider,
        ISelectionListener {

    public final static int STRUCTURE_COLUMN_WIDTH = 200;

    Logger logger = Logger.getLogger( MoleculesEditor.class );

    Collection<ISelectionChangedListener> selectionListeners =
                                 new LinkedHashSet<ISelectionChangedListener>();
    MoleculesEditorLabelProvider labelProvider;
    public List<String>                          propertyHeaders;

    private MoleculesOutlinePage outlinePage;
    private MoleculeTableContentProvider contentProvider;
    private MoleculeTableViewer molTableViewer;

    public MoleculesEditor() {
    }

    public MoleculeTableContentProvider getContentProvider() {
        return contentProvider;
    }
    public MoleculesEditorLabelProvider getLabelProvider() {
        return labelProvider;
    }


    public MoleculeTableViewer getMolTableViewer() {
        return molTableViewer;
    }

    @Override
    public void doSave( IProgressMonitor monitor ) {

        // TODO Auto-generated method stub

    }

    @Override
    public void doSaveAs() {

        // TODO Auto-generated method stub

    }

    @Override
    public void init( IEditorSite site, IEditorInput input )
                                                      throws PartInitException {

        super.setSite( site );
        super.setInput( input );
        setPartName(input.getName() );
        // TODO listen to selections check and focus on selected element from
        // common navigator, load it and get columns

    }

    @Override
    public boolean isDirty() {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {

        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public void createPartControl( Composite parent ) {
        DefaultNatTableModel model = new DefaultNatTableModel();

        contentProvider= new MoleculeTableContentProvider();
           contentProvider.inputChanged( null, null, getEditorInput() );



           DefaultRowHeaderConfig rowHeaderConfig = new DefaultRowHeaderConfig();
                rowHeaderConfig.setRowHeaderColumnCount(1);
                rowHeaderConfig.setCellRenderer(new AbstractCellRenderer() {



                  public String getDisplayText(int row, int col) {
                    return String.valueOf(row);
                  }

                  public Object getValue(int row, int col) {
                    return null;
                  }

                });
           model.setBodyConfig(new DefaultBodyConfig(contentProvider));
          model.setRowHeaderConfig(rowHeaderConfig);

                // Row heights
                SizeConfig rowHeightConfig = model.getBodyConfig().getRowHeightConfig();
                rowHeightConfig.setDefaultSize(STRUCTURE_COLUMN_WIDTH);
                rowHeightConfig.setDefaultResizable(true);
                rowHeightConfig.setIndexResizable(1, false);



              // NatTable
                new NatTable(parent,
                   SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL,
                    model
               );

        labelProvider = new MoleculesEditorLabelProvider(STRUCTURE_COLUMN_WIDTH);

    }

    protected void setupDragSource() {
        int operations = DND.DROP_COPY | DND.DROP_MOVE;
        CompositeTable viewer=null;
        DragSource dragSource = new DragSource(viewer,operations);
        Transfer[] transferTypes = new Transfer[]
                                        {
                                          LocalSelectionTransfer.getTransfer()};
        dragSource.setTransfer( transferTypes );

        dragSource.addDragListener(  new DragSourceListener() {


            public void dragStart( DragSourceEvent event ) {
               if(!getSelectedRows().isEmpty()) {
                   LocalSelectionTransfer.getTransfer()
                           .setSelection( getSelectedRows() );
                   event.image = labelProvider
                           .getColumnImage(
                                            ((IStructuredSelection)getSelectedRows())
                                            .getFirstElement(), 1 );
                   event.doit = true;
               } else
                   event.doit = false;
            }
            public void dragSetData( DragSourceEvent event ) {
                ISelection selection = LocalSelectionTransfer
                                            .getTransfer()
                                            .getSelection();

                if ( LocalSelectionTransfer
                                        .getTransfer()
                                        .isSupportedType( event.dataType )) {

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

    private List<String> createHeaderFromSelection( IAdaptable element ) {

        ICDKMolecule molecule = null;

        // try and get molecule
        if ( element != null ) {
            molecule = (ICDKMolecule) element.getAdapter( ICDKMolecule.class );
            // create headers
            createPropertyHeaders( molecule.getAtomContainer() );
        }
        return propertyHeaders;
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

//      molTableViewer.getControl().setFocus();

    }

//    public void addSelectionChangedListener( ISelectionChangedListener listener ) {
//
//        selectionListeners.add(listener );
//
//    }
//
//    public ISelection getSelection() {
//
//        return viewer.getSelection();
//
//    }
//
//    public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
//
//        selectionListeners.remove(listener );
//
//    }
//
//    public void setSelection( ISelection selection ) {
//
//        viewer.setSelection( selection );
//
//    }

    @SuppressWarnings("unchecked")
    private List<String> createPropertyHeaders( IAtomContainer ac ) {

        // property keys not Strings but i assume they are
        Set<Object> propterties = ac.getProperties().keySet();
        propertyHeaders =
                new ArrayList<String>( new LinkedHashSet( propterties ) );
        Table tree = null;//viewer.getTable();
        int oldCount = tree.getColumnCount();
        // creates missing columns so that column count is
        // proptertyHeaders.size()+2
        for ( int i = propertyHeaders.size() - (oldCount - 2); i > 0; i-- ) {
            new TableColumn( tree, SWT.NONE );
        }
        // set property name as column text
        for ( int i = 0; i < (propertyHeaders.size()); i++ ) {
            TableColumn tc = tree.getColumn( i + 2 );
            tc.setText( propertyHeaders.get( i ) );
            tc.setWidth( 100 );
            tc.setResizable( true );
        }
        return propertyHeaders;
    }

    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
        logger.debug( "Selection has chaged" + this.getClass().getName() );
        logger.debug( part.toString() + this.getSite().getPart().toString());
        if(part != null && part.equals( this )) return;
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

    @Override
    public Object getAdapter( Class adapter ) {

//        if(IContentOutlinePage.class.equals( adapter )) {
//            if(outlinePage == null) {
//                outlinePage = new MoleculesOutlinePage();
//                outlinePage.setInput(getEditorInput());
//            }
//            return outlinePage;
//        }
        return super.getAdapter( adapter );
    }
    public ISelection getSelection() {

            return StructuredSelection.EMPTY;
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
}
