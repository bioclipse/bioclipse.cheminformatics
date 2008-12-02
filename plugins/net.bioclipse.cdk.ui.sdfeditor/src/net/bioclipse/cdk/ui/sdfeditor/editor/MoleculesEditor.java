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

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.MoleculesIndexEditorInput;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;
import net.bioclipse.cdk.ui.sdfeditor.MoleculesOutlinePage;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.swt.nebula.widgets.compositetable.AbstractSelectableRow;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.nebula.widgets.compositetable.GridRowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.openscience.cdk.interfaces.IAtomContainer;

public class MoleculesEditor extends EditorPart implements
        //ISelectionProvider,
        ISelectionListener {

    public final static int STRUCTURE_COLUMN_WIDTH = 100;

    Logger logger = Logger.getLogger( MoleculesEditor.class );

    Collection<ISelectionChangedListener> selectionListeners =
                                 new LinkedHashSet<ISelectionChangedListener>();
    MoleculesEditorLabelProvider labelProvider;
    public List<String>                          propertyHeaders;
    CompositeTable viewer;

    private MoleculesOutlinePage outlinePage;


    public MoleculesEditor() {
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
    public static class Header extends Composite {
        
        public Header(Composite parent, int style) {
            super(parent,style);
            setLayout( new GridRowLayout( new int[] {40,STRUCTURE_COLUMN_WIDTH},false) );
            new Label(this,SWT.NULL).setText( "Index" );
            new Label(this,SWT.NULL).setText( "2D-Structure" );
        }
    }
    public static class Row extends AbstractSelectableRow {
        public Row(Composite parent, int style) {
            super(parent,style);
            setLayout( new GridRowLayout( new int[] {40,STRUCTURE_COLUMN_WIDTH},false)  );
            //super.setColumnCount( 2 );
            index = new Text(this,SWT.NULL);
            index.setEnabled( false );
            structure = new JChemPaintWidget(this,SWT.NULL);
            structure.getRenderer2DModel().setShowExplicitHydrogens( false );

        }
        public final Text index;
        public final JChemPaintWidget structure;
    }

    @Override
    public void createPartControl( Composite parent ) {

        labelProvider = new MoleculesEditorLabelProvider(STRUCTURE_COLUMN_WIDTH);
        final MoleculeTableContentProvider contentProvider= new MoleculeTableContentProvider();
        contentProvider.inputChanged( null, null, getEditorInput() );
        
        CompositeTable cTable = new CompositeTable(parent, SWT.NULL);
        viewer = cTable;
        // get First element from list to determin Properties
        // use a iterator go get the first element and pass the property list to
        // header and row constructor
        new Header(cTable, SWT.NULL);
        new Row(cTable,SWT.NULL);
        cTable.setRunTime( true );
        cTable.setNumRowsInCollection( contentProvider.numberOfEntries( 500 ) );
        cTable.addRowContentProvider( contentProvider );
        
        if(contentProvider.getFile() !=null) { 
            Job job = new Job("Indexing SD-file") {
                protected IStatus run(IProgressMonitor monitor) {
                    Activator.getDefault().getCDKManager()
                    .createSDFileIndex( contentProvider.getFile(),monitor  );
                    final int result = contentProvider.init();
                    WorkbenchJob updateJob = new WorkbenchJob(
                    "Updating SD editor") {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
                         */
                        public IStatus runInUIThread(IProgressMonitor updateMonitor) {
                            // Cancel the job if the tree viewer got closed

                            contentProvider.ready();
                            int firstVisibleRow = viewer.getTopRow();
                            viewer.setNumRowsInCollection( result );
                            viewer.setTopRow( firstVisibleRow );
                            return Status.OK_STATUS;
                        }
                    };
                    updateJob.setSystem(true);
                    updateJob.schedule();
                    monitor.done();
                    return Status.OK_STATUS;
                }
            };
            job.setPriority(Job.SHORT);
            job.schedule(); // start as soon as possible

        }
        
        
       


        // See what's currently selected and select it
        ISelection selection =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getSelectionService().getSelection();
        if ( selection instanceof IStructuredSelection ) {
            IStructuredSelection stSelection = (IStructuredSelection) selection;
            //reactOnSelection( stSelection );
        }

        setupDragSource();
        //getEditorSite().getPage().addSelectionListener( this );
        //getSite().setSelectionProvider(viewer);

    }

    protected void setupDragSource() {
        int operations = DND.DROP_COPY | DND.DROP_MOVE;
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
        if(viewer != null)
                setSelectedRows(selection);
    }

    @Override
    public void setFocus() {

       viewer.setFocus();

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
        if(viewer != null)
            return getSelectedRows();
        else
            return StructuredSelection.EMPTY;
    }


    private ISelection getSelectedRows() {
        viewer.getSelection();
        viewer.getTopRow();
        
        return StructuredSelection.EMPTY;
        
    }
    private void setSelectedRows(ISelection selection) {
        // mapping between selections and index
        //viewer.setSelection(  );
    }
}
