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
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.jchempaint.widgets.JChemPaintEditorWidget;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jmol.editors.JmolEditor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
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
//                            ISelectionChangedListener, // do we need this?
                            ISelectionListener
                                {

    Logger logger = Logger.getLogger( MultiPageMoleculesEditorPart.class );
    private MoleculesEditor moleculesPage;
    private JChemPaintEditorWidget jcpPage;
    private JChemPaintEditor jcpEditor;
    private JmolEditor jmolPage;

    private final int MOLECULES_PAGE = 0;
    private final int SINGLE_ENTRY_PAGE = 1;
    private final int JMOL_PAGE = 2;

    public MultiPageMoleculesEditorPart() {
        super();
    }
    
    @Override
    protected void createPages() {

        moleculesPage = new MoleculesEditor();
        jcpPage = new JChemPaintEditorWidget(getContainer(),SWT.NONE);  
        jmolPage = new JmolEditor();
            try {
                addPage(MOLECULES_PAGE, moleculesPage , getEditorInput());
                setPageText( MOLECULES_PAGE, "Molecules" );
                //addPage(SINGLE_ENTRY_PAGE, jcpPage);
                addPage(SINGLE_ENTRY_PAGE, jcpEditor=new JChemPaintEditor(),getEditorInput());
                setPageText( SINGLE_ENTRY_PAGE, "Singel entry" );
                addPage( JMOL_PAGE, jmolPage, getEditorInput() );                 
                setPageText(JMOL_PAGE, "3D-editor");            
                 
            } catch ( PartInitException e ) {
                logger.debug( "Failed to create pages: " + e.getMessage() );
                LogUtils.debugTrace( logger, e );
            }
           
            
            setPartName( getEditorInput().getName());
            
            
        getSite().getPage().addPostSelectionListener( this );
    }
    
    @Override
    public void init( IEditorSite site, IEditorInput input )
                                                      throws PartInitException {
        super.init( site, input );
        
        //site.getPage().addSelectionListener( this );
     
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

//    private void reactOnSelection(IStructuredSelection selection){  
//        moleculesPage.reactOnSelection( selection );
//        if(SINGLE_ENTRY_PAGE == getActivePage()) {
//            updateJCPPage();
//        }
//    }
//
//    public void selectionChanged( SelectionChangedEvent event ) {
//        ISelection selection = event.getSelection();
//        if( selection instanceof IStructuredSelection){
//            reactOnSelection( (IStructuredSelection) selection );
//        }
//        
//    }

   @Override
    protected void pageChange( int newPageIndex ) {

        if ( SINGLE_ENTRY_PAGE == newPageIndex ) {
            updateJCPPage();            
        }
        else if (JMOL_PAGE == newPageIndex) {
            updateJMolPage();
        }
    }

    private void updateJMolPage() {
    
//        try {
//            //jmolPage.init( getEditorSite(), getEditorInput() );
//        } catch ( PartInitException e ) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }        
    }

    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {        
        if( part != this && selection instanceof IStructuredSelection) {
            logger.debug( "Selection has chaged" + this.getClass().getName() );
//        if( selection instanceof IStructuredSelection){
//            reactOnSelection( (IStructuredSelection) selection );
//        }
            moleculesPage.reactOnSelection( selection );
        }
    }
//    
    private void updateJCPPage() {        
        ISelection selection = moleculesPage.getSelection();
        if(selection instanceof IStructuredSelection) {
           Object element = ((IStructuredSelection)selection).getFirstElement();           
           if(element instanceof IAdaptable) {
               jcpEditor.setInput( element );
//               element = ((IAdaptable)element).getAdapter( ICDKMolecule.class );
//               if( element != null )
//                   jcpPage.setInput(((ICDKMolecule)element).getAtomContainer());
           }
        }
    }
    
}
