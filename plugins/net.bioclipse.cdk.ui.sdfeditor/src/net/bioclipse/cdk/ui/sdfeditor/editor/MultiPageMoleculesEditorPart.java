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

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.widgets.JChemPaintEditorWidget;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;


public class MultiPageMoleculesEditorPart extends MultiPageEditorPart implements
                            ISelectionChangedListener, // do we need this?
                            ISelectionListener{

    private MoleculesEditor moleculesPage;
    private JChemPaintEditorWidget jcpPage;

    private final int MOLECULES_PAGE = 0;
    private final int JCP_PAGE = 1;


    public MultiPageMoleculesEditorPart() {
        super();
    }
    
    @Override
    protected void createPages() {

        moleculesPage = new MoleculesEditor();
        jcpPage = new JChemPaintEditorWidget(getContainer(),SWT.NONE);  
        
            try {
                addPage(MOLECULES_PAGE, moleculesPage , getEditorInput());
            } catch ( PartInitException e ) {
                
            }
            setPageText( 0, "Molecules" );
            addPage(JCP_PAGE, jcpPage);
            setPageText( 1, "JCP" );
            setPartName( getEditorInput().getName());
            
        
        getEditorSite().getPage().addSelectionListener( this );
    }
    
    @Override
    public void init( IEditorSite site, IEditorInput input )
                                                      throws PartInitException {
        super.init( site, input );
        
        site.getPage().addSelectionListener( this );
     
    }

    @Override
    protected void setActivePage( int pageIndex ) {    
        //reactOnSelection( (IStructuredSelection)moleculesPage.getSelection() );
        updateJCPPage();
        super.setActivePage( pageIndex );
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
    public boolean isSaveAsAllowed() {

        // TODO Auto-generated method stub
        return false;
    }

    private void reactOnSelection(IStructuredSelection selection){  
        moleculesPage.reactOnSelection( selection );
        if(JCP_PAGE == getActivePage()) {
            updateJCPPage();
        }
    }

    public void selectionChanged( SelectionChangedEvent event ) {
        ISelection selection = event.getSelection();
        if( selection instanceof IStructuredSelection){
            reactOnSelection( (IStructuredSelection) selection );
        }
        
    }

   @Override
    protected void pageChange( int newPageIndex ) {

        if ( JCP_PAGE == newPageIndex )
            updateJCPPage();
    }

    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {        
        if( selection instanceof IStructuredSelection){
            reactOnSelection( (IStructuredSelection) selection );
        }        
    }
    
    private void updateJCPPage() {        
        ISelection selection = moleculesPage.getSelection();
        if(selection instanceof IStructuredSelection) {
           Object element = ((IStructuredSelection)selection).getFirstElement();           
           if(element instanceof IAdaptable) {
               element = ((IAdaptable)element).getAdapter( ICDKMolecule.class );
               if( element != null )
                   jcpPage.setInput(((ICDKMolecule)element).getAtomContainer());
           }
        }
    }
    
}
