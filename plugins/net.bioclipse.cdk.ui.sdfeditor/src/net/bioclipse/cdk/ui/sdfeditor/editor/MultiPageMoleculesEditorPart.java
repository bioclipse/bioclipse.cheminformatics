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

import java.util.HashMap;
import java.util.Map;

import net.bioclipse.cdk.domain.MoleculesIndexEditorInput;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.jchempaint.widgets.JChemPaintEditorWidget;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jmol.editors.JmolEditor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

public class MultiPageMoleculesEditorPart extends MultiPageEditorPart implements
                                                    ISelectionListener {

    Logger logger = Logger.getLogger( MultiPageMoleculesEditorPart.class );

    private MoleculesEditor moleculesPage;
    private JChemPaintEditorWidget jcpWidget;
    private JmolEditor jmolPage;
    PropertySelector ps;
    Pages lastPage;

    enum Pages {
        Molecules,
        Headers,
        JCP,
        Jmol;
    }
    Map<Integer,Pages> pageOrder = new HashMap<Integer,Pages>();


    public MultiPageMoleculesEditorPart() {
        super();
    }

    public MoleculesEditor getMoleculesPage() {
        return moleculesPage;
    }



    @Override
    protected void createPages() {

        try {
            int i;
            for(Pages page:new Pages[]{ Pages.Molecules,
                                        Pages.Headers}) {
                switch(page) {
                    case Molecules:
                        i = addPage( moleculesPage =
                                            new MoleculesEditor(),
                                                getEditorInput());
                        pageOrder.put(i, page);
                        setPageText(i,"Molecules");
                        break;

                    case Headers:
                        i = addPage( ps = new PropertySelector(
                                                   this.getContainer(),
                                                   SWT.NONE));
                        pageOrder.put(i, page);
                        setPageText(i,"Headers");
                        break;
                }
            }

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
        throw new UnsupportedOperationException();
    }

    @Override
    public void doSaveAs() {
        throw new UnsupportedOperationException();
        // Unsupported

    }

    @Override
    public boolean isSaveAsAllowed() {
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
       Pages page = pageOrder.get( newPageIndex );

       switch(page) {
           case Molecules:
               if(lastPage == Pages.Headers) {
                   moleculesPage.getContentProvider()
                           .setVisibleProperties( ps.getVisibleProperties() );
                   moleculesPage.setUseExtensionGenerators(ps.isUseGenerators());
                   moleculesPage.getContentProvider().updateHeaders();
               }break;
           case Headers:
               MoleculeTableContentProvider contentProvider =
                       moleculesPage.getContentProvider();
               ps.setInitialData( contentProvider.getProperties(),
                                  contentProvider.getAvailableProperties());
       }

       lastPage = pageOrder.get(newPageIndex);
       super.pageChange( newPageIndex );
    }

    private void updateJmolPage() {
        ISelection selection = moleculesPage.getSelection();
        if(selection instanceof IStructuredSelection) {
           Object element = ((IStructuredSelection)selection).getFirstElement();
           if(element instanceof SDFElement) {
               jmolPage.setDataInput(  (IEditorInput) ((SDFElement) element)
                              .getAdapter( MoleculesIndexEditorInput.class ) );
           }
        }

//        try {
//            //jmolPage.init( getEditorSite(), getEditorInput() );
//        } catch ( PartInitException e ) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
        if( part != this && selection instanceof IStructuredSelection) {

            moleculesPage.reactOnSelection( selection );
            if(pageOrder.get(getActivePage() ) == Pages.Jmol)
                updateJmolPage();
            if(pageOrder.get(getActivePage() ) == Pages.JCP)
                updateJCPPage();
        }
    }

    private void updateJCPPage() {
        ISelection selection = moleculesPage.getSelection();
        if(selection instanceof IStructuredSelection) {
           Object element = ((IStructuredSelection)selection).getFirstElement();
           if(element instanceof SDFElement) {
               try {
               jcpWidget.setInput( element );
               } catch (IllegalArgumentException x) {

               }
           }
        }
    }
}
