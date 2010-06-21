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
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.MoleculesIndexEditorInput;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.ui.sdfeditor.Activator;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.cdk.ui.sdfeditor.business.MappingEditorModel;
import net.bioclipse.cdk.ui.sdfeditor.business.SDFIndexEditorModel;
import net.bioclipse.cdk.ui.sdfeditor.handlers.CalculatePropertyHandler;
import net.bioclipse.cdk.ui.views.IFileMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jmol.editors.JmolEditor;
import net.bioclipse.jobs.BioclipseUIJob;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.openscience.cdk.Mapping;

public class MultiPageMoleculesEditorPart extends MultiPageEditorPart implements
                                                    ISelectionListener,
                                                    IResourceChangeListener{

    Logger logger = Logger.getLogger( MultiPageMoleculesEditorPart.class );

    public static final String JCP_CONTEXT = "net.bioclipse.ui.contexts.JChemPaint";

    private MoleculesEditor moleculesPage;
    private JChemPaintEditor jcpPage;
    private JmolEditor jmolPage;

    IContextActivation jcpActivation = null;

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
                        setPageText(i,"Table");
                        moleculesPage.getMolTableViewer().setDoubleClickHook(
                         new Runnable() {
                            public void run() {
                                try {
                                IHandlerService services = (IHandlerService)
                                    getSite().getService(IHandlerService.class);
                                services.executeCommand(
                                      "net.bioclipse.cdk.ui.sdfeditor.open.jcp",
                                      null);
                                }catch(Exception e) {
                                    logger.warn( "Failed to open 2D-strcuture tab" );
                                    LogUtils.debugTrace( logger, e );
                                }
                            }
                         }
                        );
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
                        setPageText( i, "Single Molecule" );
                        break;
                }
            }

        } catch ( PartInitException e ) {
            logger.debug( "Failed to create pages: " + e.getMessage() );
            LogUtils.debugTrace( logger, e );
        }

        setPartName( getEditorInput().getName());
        getSite().getPage().addPostSelectionListener( this );
        ResourcesPlugin.getWorkspace().addResourceChangeListener(
                                       this, IResourceChangeEvent.POST_CHANGE);
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
        IMoleculesEditorModel model = moleculesPage.getModel();

        syncJCP();
        if(model instanceof IFileMoleculesEditorModel) {
           save(model,((IFileMoleculesEditorModel) model).getResource());
        }else {
            model.save();
            jcpPage.getWidget().setDirty( false );
            setDirty( false );
        }
    }

    @Override
    public void doSaveAs() {
        IMoleculesEditorModel model = moleculesPage.getModel();
        IFile original = null;
        if(model instanceof IFileMoleculesEditorModel) {
            original = ((IFileMoleculesEditorModel)model).getResource();
        }
        SaveAsDialog saveAsDialog = new SaveAsDialog( this.getSite().getShell() );
        saveAsDialog.setOriginalFile( (IFile) original );
        int result = saveAsDialog.open();
        if ( result == 1 ) {
            logger.debug( "SaveAs canceled." );
            return;
        }
        syncJCP();
        moleculesPage.getMolTableViewer().setInput( null );
        IPath path = saveAsDialog.getResult();
        IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile( path );
        save(model,file);
    }

    private void save(IMoleculesEditorModel model, IFile file) {
        try {
            Activator.getDefault().getMoleculeTableManager()
            .saveSDF( model,file,new UpdateEditorFromFileUIJob(this));
        } catch ( BioclipseException e ) {
            logger.warn( "Failed to save molecule. " + e.getMessage() );
            LogUtils.handleException( e, logger, "net.bioclipse.cdk.ui.sdfeditor" );
        }
    }

    public static class UpdateEditorFromFileUIJob extends BioclipseUIJob<IFileMoleculesEditorModel> {
        MultiPageMoleculesEditorPart mpmep;


        public UpdateEditorFromFileUIJob(MultiPageMoleculesEditorPart part) {
            mpmep = part;
        }
        @Override
        public void runInUI() {
            IFileMoleculesEditorModel model =getReturnValue();
            if(model instanceof SDFIndexEditorModel) {
                model = new MappingEditorModel( model );
            }
            mpmep.moleculesPage.getMolTableViewer().setInput( model );
            mpmep.moleculesPage.getMolTableViewer().refresh();

            IResource origin = model.getResource();
            if(origin !=null)
                mpmep.setPartName(origin.getName() );
            mpmep.firePropertyChange( IWorkbenchPartConstants.PROP_PART_NAME);
            mpmep.firePropertyChange( IWorkbenchPartConstants.PROP_INPUT);

            mpmep.jcpPage.getWidget().setDirty( false );
            mpmep.moleculesPage.setDirty( false );
            mpmep.setDirty( false );
        }
    }
    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }

    private void calculateProperties(ICDKMolecule molecule) {
        Collection<IPropertyCalculator<?>>  ids=new ArrayList<IPropertyCalculator<?>>();
        Collection<IPropertyCalculator<?>> calculators = CalculatePropertyHandler
        .gatherCalculators( CalculatePropertyHandler
                            .getConfigurationElements(), null );

        Collection<Object> idsx =
                        moleculesPage.getModel().getAvailableProperties();
        for ( IPropertyCalculator<?> calculator : calculators ) {
            if ( idsx.contains( calculator.getPropertyName() ) ) {
                ids.add( calculator );
            }
        }


        Activator.getDefault().getMoleculeTableManager()
                .calculateProperties( molecule,
                                      ids.toArray(
                                              new IPropertyCalculator<?>[0] ),
                                      new BioclipseUIJob<Void>() {
                        @Override
                        public void runInUI() {

                            moleculesPage.refresh();

                        }
                });
    }

   @Override
    protected void pageChange( int newPageIndex ) {
       Pages page = pageOrder.get( newPageIndex );
       setPartProperty( "activePage", page.name() );
       IContextService contextService = (IContextService) getSite()
                                       .getService( IContextService.class );
       if(lastPage == Pages.JCP) {
           syncJCP();
           if(jcpActivation != null) {
               contextService.deactivateContext( jcpActivation );
               jcpActivation = null;
           }
       }
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
               jcpActivation = contextService.activateContext( JCP_CONTEXT );
               break;
       }

       lastPage = pageOrder.get(newPageIndex);
       super.pageChange( newPageIndex );
    }

   private ISaveablePart getSaveablePart(Pages page) {
       switch(page) {
           case JCP: return jcpPage;
           case Molecules: return moleculesPage;
           default: return null;
       }
   }

   private void syncJCP() {
       ICDKMolecule newMol = jcpPage.getCDKMolecule();
       if(newMol == null) return;
       if(jcpPage.isDirty()) {
           setDirty(true);
           calculateProperties( newMol );
           int selection = moleculesPage.getMolTableViewer().getFirstSelected();
           selection = selection < 0 ? 0:selection;// see updateJCPPage()
           moleculesPage.getModel().markDirty(
                       selection,
                       newMol );
           moleculesPage.setDirty( true );
       }
   }

    public void setDirty( boolean b ) {
        if(dirty != b) {
            dirty  = b;
            firePropertyChange( IEditorPart.PROP_DIRTY );
        }
    }

    @Override
    public boolean isDirty() {
        ISaveablePart saveable = getSaveablePart( lastPage );

            return dirty || (saveable!=null?saveable.isDirty():false);
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
//            if(pageOrder.get(getActivePage() ) == Pages.Jmol)
//                updateJmolPage();
//            if(pageOrder.get(getActivePage() ) == Pages.JCP)
//                updateJCPPage();
        }
    }

    /**
     * Updates the input for the JChemPaint editor from selection.
     * If there is no selection the molecule at index 0 is used.
     */
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

    public void resourceChanged( IResourceChangeEvent event ) {

        switch (event.getType()) {
            case IResourceChangeEvent.POST_CHANGE:
                final boolean[] val = new boolean[1];
                IResourceDelta delta = event.getDelta();
                IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

                    public boolean visit( IResourceDelta delta )
                                                                throws CoreException {
                        if( delta.getFlags() != IResourceDelta.MARKERS
                            && delta.getResource().getType() == IResource.FILE) {
                            if(delta.getKind() == IResourceDelta.REMOVED) {
                                IResource resource = delta.getResource();
                                IMoleculesEditorModel model = getMoleculesPage()
                                                                .getModel();
                                if(model instanceof IFileMoleculesEditorModel) {
                                    IResource modelRe = ((IFileMoleculesEditorModel)
                                                           model).getResource();

                                    if( resource.equals( modelRe )) {
                                        val[0]=true;
                                    }
                                }
                            }
                        }
                        return true;
                    }

                };
                try {
                    delta.accept( visitor );
                    if(val[0])
                        Display.getDefault().asyncExec( new Runnable() {
                            public void run() {
                                getSite().getPage().closeEditor(
                                    MultiPageMoleculesEditorPart.this, true );
                            }
                        });
                } catch ( CoreException e ) {
                    LogUtils.handleException( e, logger,
                             net.bioclipse.cdk.jchempaint.Activator.PLUGIN_ID );
                }

                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter( Class adapter ) {
        if(adapter.equals( MoleculesEditor.class ))
            return moleculesPage;
        if(adapter.equals( JChemPaintEditor.class ))
            return jcpPage;
        return super.getAdapter( adapter );
    }

    /**
     * @return true if JCP is current editor
     */
    public boolean isJCPVisible() {
        if (getActiveEditor() instanceof JChemPaintEditor)
            return true;
        return false;
    }

}
