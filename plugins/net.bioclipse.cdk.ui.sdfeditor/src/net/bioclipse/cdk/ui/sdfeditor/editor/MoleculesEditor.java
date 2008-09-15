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
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.ui.model.MoleculesFromSDF;
import net.bioclipse.cdk.ui.sdfeditor.MoleculesOutlinePage;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
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
    TreeViewer viewer;

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

    

    @Override
    public void createPartControl( Composite parent ) {
        
        
        viewer =
                new TreeViewer( parent, SWT.VIRTUAL | SWT.V_SCROLL
                                        | SWT.H_SCROLL | SWT.MULTI
                                        | SWT.FULL_SELECTION | SWT.BORDER ) {

                    @Override
                    public void add( Object parentElementOrTreePath,
                                     Object[] childElements ) {

                        if ( propertyHeaders == null
                             && childElements.length > 0 ) {
                            // TODO make it a job on the GUI thread
                            if ( childElements[0] instanceof IAdaptable ){
                                if( ((IAdaptable) childElements[0]).getAdapter( ICDKMolecule.class )!= null)
                                labelProvider.setPropertyHeaders(
                                               createHeaderFromSelection( 
                                               (IAdaptable) childElements[0] ));
                                
                            }
                        }
                        super.add( parentElementOrTreePath, childElements );
                    }
                    /*
                     * (non-Javadoc)
                     * @see
                     * org.eclipse.jface.viewers.TreeViewer#replace(java.lang
                     * .Object, int, java.lang.Object)
                     */
                    @Override
                    public void replace( Object parentElementOrTreePath,
                                         int index, Object element ) {

                        if ( propertyHeaders == null && element != null ) {
                            // TODO make it a job on the GUI thread
                            if ( element instanceof IAdaptable ) {
                                if ( ((IAdaptable) element)
                                        .getAdapter( ICDKMolecule.class ) != null )
                                    labelProvider
                                            .setPropertyHeaders( createHeaderFromSelection( (IAdaptable) element ) );

                            }
                        }
                        super.replace( parentElementOrTreePath, index, element );
                    }
                };

        Tree tree = viewer.getTree();
        tree.setHeaderVisible( true );

        TreeColumn itemColumn = new TreeColumn( tree, SWT.NONE );
        itemColumn.setText( "Index" );
        itemColumn.setResizable( true );
        itemColumn.setWidth( 100 );

        TreeColumn nameColumn = new TreeColumn( tree, SWT.NONE );
        nameColumn.setText( "Structure" );
        nameColumn.setResizable( false );
        nameColumn.setWidth( STRUCTURE_COLUMN_WIDTH );

        viewer.setComparer( new SDFElementComparer() );        

        viewer.setColumnProperties( new String[] { "Index", "Name" } );

        viewer.setContentProvider( new MoleculesEditorContentProvider(viewer) );
        viewer.setLabelProvider( labelProvider = 
                     new MoleculesEditorLabelProvider(STRUCTURE_COLUMN_WIDTH) );
        
        viewer.setUseHashlookup(true );
        viewer.setInput( 
                     getEditorInput().getAdapter(IMoleculesEditorModel.class ) );
       
        
        // See what's currently selected and select it
        ISelection selection =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getSelectionService().getSelection();
        if ( selection instanceof IStructuredSelection ) {
            IStructuredSelection stSelection = (IStructuredSelection) selection;
            reactOnSelection( stSelection );
        }
        //getEditorSite().getPage().addSelectionListener( this );
        getSite().setSelectionProvider(viewer);
        
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
                viewer.setSelection(selection ,true);
    }

    @Override
    public void setFocus() {

       viewer.getTree().setFocus();

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
        Tree tree = viewer.getTree();
        int oldCount = tree.getColumnCount();
        // creates missing columns so that column count is
        // proptertyHeaders.size()+2
        for ( int i = propertyHeaders.size() - (oldCount - 2); i > 0; i-- ) {
            new TreeColumn( tree, SWT.NONE );
        }
        // set property name as column text
        for ( int i = 0; i < (propertyHeaders.size()); i++ ) {
            TreeColumn tc = tree.getColumn( i + 2 );
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
            viewer.setSelection( selection );
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
    
        if(IContentOutlinePage.class.equals( adapter )) {
            if(outlinePage == null) {
                outlinePage = new MoleculesOutlinePage();
                outlinePage.setInput(getEditorInput());
            }
            return outlinePage;
        }
        return super.getAdapter( adapter );
    }
    public ISelection getSelection() {
        if(viewer != null)
            return viewer.getSelection();
        else
            return StructuredSelection.EMPTY;
    }
    
}
