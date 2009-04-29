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

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.MoleculesIndexEditorInput;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jmol.editors.JmolEditor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

public class MultiPageMoleculesEditorPart extends MultiPageEditorPart implements
                                                    ISelectionListener {

    Logger logger = Logger.getLogger( MultiPageMoleculesEditorPart.class );

    private MoleculesEditor moleculesPage;
    private JChemPaintEditor jcpPage;
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

    private boolean dirty = false;


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
                                        Pages.JCP,
                                        Pages.Headers,
                                        }) {
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
                    case JCP:
                        i = addPage( jcpPage = new JChemPaintEditor(),
                                     new IEditorInput() {

                                        public boolean exists() {
                                            return false;
                                        }
                                        public ImageDescriptor getImageDescriptor() {
                                            return null;
                                        }
                                        public String getName() {
                                            return null;
                                        }
                                        public IPersistableElement getPersistable() {
                                            return null;
                                        }
                                        public String getToolTipText() {
                                            return null;
                                        }
                                        @SuppressWarnings("unchecked")
                                        public Object getAdapter( Class adapter ) {
                                            return null;
                                        }

                        });
                        pageOrder.put( i, page );
                        setPageText( i, "2D-structure" );
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

   @Override
    protected void pageChange( int newPageIndex ) {
       Pages page = pageOrder.get( newPageIndex );
       setPartProperty( "activePage", page.name() );
       if(lastPage == Pages.JCP)
           syncJCP();
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
               break;
           case JCP:
               updateJCPPage();
       }

       lastPage = pageOrder.get(newPageIndex);
       firePartPropertyChanged( "activePage", lastPage.name(), page.name() );
       super.pageChange( newPageIndex );
    }

   private void syncJCP() {
       ICDKMolecule newMol = jcpPage.getCDKMolecule();
       if(jcpPage.isDirty())
           setDirty(true);
       moleculesPage.getModel().markDirty(
                       moleculesPage.getMolTableViewer().getFirstSelected(),
                       newMol );

   }

    private void setDirty( boolean b ) {
        if(dirty != b) {
            dirty  = b;
            firePropertyChange( IEditorPart.PROP_DIRTY );
        }
    }

    @Override
    public boolean isDirty() {

        return dirty;
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
        int index = moleculesPage.getMolTableViewer().getFirstSelected();
        if(index < 0 ) index = 0;
        IMoleculesEditorModel model = moleculesPage.getModel();
        if(model != null)
            jcpPage.setInput( model.getMoleculeAt( index ));
        else
            jcpPage.setInput( null );
//        ISelection selection = moleculesPage.getSelection();
//        if(selection instanceof IStructuredSelection) {
//           Object element = ((IStructuredSelection)selection).getFirstElement();
//           if(element instanceof SDFElement) {
//               try {
//               jcpPage.setInput( element );
//               } catch (IllegalArgumentException x) {
//
//               }
//           }else if( element instanceof ICDKMolecule) {
//               jcpPage.setInput( element );
//           }
//        }
    }
}
