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
import java.util.List;

import net.bioclipse.cdk.domain.MoleculesIndexEditorInput;
import net.bioclipse.cdk.domain.SDFElement;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

public class MoleculesEditor extends EditorPart implements
        //ISelectionProvider,
        ISelectionListener {

    public final static int STRUCTURE_COLUMN_WIDTH = 200;

    Logger logger = Logger.getLogger( MoleculesEditor.class );

    public List<String> propertyHeaders = new ArrayList<String>();

    private MoleculeTableContentProvider contentProvider;
    private MoleculeTableViewer molTableViewer;

    public MoleculesEditor() {
    }

    public MoleculeTableContentProvider getContentProvider() {
        return contentProvider;
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
        // TODO listen to selections check and focus on selected element from
        // common navigator, load it and get columns
    }

    @Override
    public boolean isDirty() {
        // TODO Check the index
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl( Composite parent ) {

        molTableViewer = new MoleculeTableViewer(parent,SWT.NONE);
        molTableViewer.setContentProvider( contentProvider =
                                        new MoleculeTableContentProvider() );
        molTableViewer.setInput( getEditorInput() );

        molTableViewer.setInput(getEditorInput());

                MenuManager menuMgr = new MenuManager("Molecuels table","net.bioclipse.cdk.ui.sdfeditor.menu");
                menuMgr.add( new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
                getSite().registerContextMenu( "net.bioclipse.cdk.ui.sdfeditor.menu",menuMgr, molTableViewer);
                Menu menu = menuMgr.createContextMenu(molTableViewer.getControl());
                molTableViewer.getControl().setMenu(menu);
                //(new TableViewer(parent,SWT.NONE)).setInput( input )
                logger.debug( "Menu id for SDFEditor " +menuMgr.getId());
//        ((NatTable)molTableViewer.getControl()).get

        getSite().setSelectionProvider( molTableViewer );
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

    @SuppressWarnings("unchecked")
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

    public IRenderer2DConfigurator getRenderer2DConfigurator() {
        return molTableViewer.getRenderer2DConfigurator();
    }

    public void setRenderer2DConfigurator(
                             IRenderer2DConfigurator renderer2DConfigurator ) {
        molTableViewer.setRenderer2DConfigurator( renderer2DConfigurator );
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
}
