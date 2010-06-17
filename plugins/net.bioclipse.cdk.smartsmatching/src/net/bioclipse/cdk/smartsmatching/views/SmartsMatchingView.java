/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.smartsmatching.views;


 import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.smartsmatching.Activator;
import net.bioclipse.cdk.smartsmatching.AddEditSmartsDialog;
import net.bioclipse.cdk.smartsmatching.SmartsMatchingRendererConfigurator;
import net.bioclipse.cdk.smartsmatching.model.SmartsFile;
import net.bioclipse.cdk.smartsmatching.model.SmartsHit;
import net.bioclipse.cdk.smartsmatching.model.SmartsWrapper;
import net.bioclipse.cdk.smartsmatching.prefs.SmartsMatchingPrefsHelper;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;
import net.bioclipse.core.business.BioclipseException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.contexts.ContextManagerEvent;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;
import org.openscience.cdk.interfaces.IAtomContainer;


/**
 * A view for matching smarts in open editors and views
 * @author ola
 *
 */
public class SmartsMatchingView extends ViewPart implements IPartListener,
                                                        IContextManagerListener{

    private static final Logger logger = Logger.getLogger(
                                                      SmartsMatchingView.class);

    private static CheckboxTreeViewer viewer;
    private Action addSmartsAction;
    private Action removeAction;

    private Action runAction;

    private static Map<IWorkbenchPart,List<SmartsFile>> editorSmartsMap;
    private static List<SmartsFile> smartsInView;

    private Action clearAction;
    ICDKManager cdk;

    //Used to handle the case with no open editor
    private static EditorPart bogusWBPart=new EditorPart(){
        @Override
        public void doSave( IProgressMonitor monitor ) {
        }
        @Override
        public void doSaveAs() {
        }
        @Override
        public void init( IEditorSite site, IEditorInput input )
                                                      throws PartInitException {
        }
        @Override
        public boolean isDirty() {
            return false;
        }
        @Override
        public boolean isSaveAsAllowed() {
            return false;
        }

        @Override
        public void createPartControl( Composite parent ) {
        }

        @Override
        public void setFocus() {
        }
    };

    private static IWorkbenchPart currentPart;

    private static SmartsMatchingView smartsView;

    private Action expandAllAction;

    private Action collapseAllAction;

    private Action showPropertiesViewAction;

    private Action addFileAction;


    /**
     * The constructor.
     */
    public SmartsMatchingView() {
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        
        smartsView=this;

        final Tree tree = new Tree(parent,SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL|SWT.CHECK);
        viewer = new CheckboxTreeViewer(tree);
//        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL| SWT.V_SCROLL | SWT.CHECK);
        viewer.setContentProvider(new SmartsMatchingContentProvider());
        viewer.setLabelProvider(new SmartsMatchingLabelProvider());
        viewer.setSorter(new ViewerSorter());
        
        viewer.addCheckStateListener( new ICheckStateListener() {
            
            public void checkStateChanged( CheckStateChangedEvent event ) {
        
                Object element = event.getElement();

                if ( element instanceof SmartsFile ) {
                    SmartsFile sf = (SmartsFile) element;
                    
                    for (SmartsWrapper sw : sf.getSmarts()){
                        sw.setActive( event.getChecked() );
                        for (TreeItem item : viewer.getTree().getItems()){
                            for (TreeItem childItem : item.getItems()){
                                if (childItem.getData()==sw){
                                    childItem.setChecked( sw.isActive() );
                                }
                            }
                        }
                    }
                }
                else if ( element instanceof SmartsWrapper ) {
                    SmartsWrapper sw = (SmartsWrapper) element;
                    sw.setActive( event.getChecked() );
                }                
            }
        });
        
        
        cdk=net.bioclipse.cdk.business.Activator.getDefault().
        getJavaCDKManager();
        
        //Read prefs for stored smarts
        smartsInView = SmartsMatchingPrefsHelper.getPreferences();
        viewer.setInput(smartsInView);
        editorSmartsMap=new HashMap<IWorkbenchPart, List<SmartsFile>>();
        editorSmartsMap.put( bogusWBPart, smartsInView);
        

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), 
                                     "net.bioclipse.cdk.smartsmatching.viewer");
        makeActions();
        hookContextMenu();
        contributeToActionBars();

        //Listen for part lifecycle events to react on editors
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);

        //Hook us up to react on JCP context changes
        IContextService contextService = (IContextService)PlatformUI
                              .getWorkbench().getService(IContextService.class);
        contextService.addContextManagerListener( this );

        //Post selections to Eclipse
        getSite().setSelectionProvider(viewer); 
    }


    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                SmartsMatchingView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(runAction);
        manager.add(clearAction);
        manager.add(new Separator());
        manager.add(addSmartsAction);
        manager.add(removeAction);
        manager.add(addFileAction);
        manager.add(new Separator());
        manager.add(showPropertiesViewAction);

        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(runAction);
        manager.add(clearAction);
        manager.add(new Separator());
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        manager.add(new Separator());
        manager.add(addSmartsAction);
        manager.add(removeAction);
        manager.add(addFileAction);
    }

    private void makeActions() {

        collapseAllAction = new Action() {
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAllAction.setText("Collapse all");
        collapseAllAction.setToolTipText("Collapse all SMARTS");
        collapseAllAction.setImageDescriptor(Activator.getImageDecriptor( 
                                                     "icons/collapseall.gif" ));

        expandAllAction = new Action() {
            public void run() {
                viewer.expandAll();
            }
        };
        expandAllAction.setText("Expand all");
        expandAllAction.setToolTipText("Expand all SMARTS to reveal hits");
        expandAllAction.setImageDescriptor(Activator.getImageDecriptor( 
                                                       "icons/expandall.gif" ));

        
        runAction = new Action() {
            public void run() {
                runSmartsMatching();
            }
        };
        runAction.setText("Run matching");
        runAction.setToolTipText("Run SMARTS matching for the active aditor");
        runAction.setImageDescriptor(Activator.getImageDecriptor( 
                                                        "icons/smallRun.gif" ));

        clearAction = new Action() {
            public void run() {
                clearHits( );
            }
        };
        clearAction.setText("Clear matches");
        clearAction.setToolTipText("Clear all SMARTS matches");
        clearAction.setImageDescriptor(Activator.getImageDecriptor( 
                                                       "icons/clear_co.gif" ));

        addSmartsAction = new Action() {
            public void run() {
                
                //Get selected SmartsFile
                IStructuredSelection ssel=(IStructuredSelection)viewer.
                getSelection();
                if (ssel.getFirstElement()==null){
                    showMessage( "Please select a SmartsFile to add to first.");
                    return;
                }

                //Locate smartsfile. Can be selected or a parent
                SmartsFile sfile=null;

                if ( ssel.getFirstElement() instanceof SmartsFile ) {
                    sfile=(SmartsFile)ssel.getFirstElement();
                }
                else if ( ssel.getFirstElement() instanceof SmartsWrapper ) {
                    SmartsWrapper sw = (SmartsWrapper)ssel.getFirstElement();
                    //Look up parent
                    for (SmartsFile sf : smartsInView){
                        if (sf.getSmarts().contains( sw )){
                            sfile=sf;
                        }
                    }
                }
                
                if (sfile==null){
                    showMessage( "Please select a SmartsFile first.");
                    return;
                }
                
                AddEditSmartsDialog dlg=new AddEditSmartsDialog(getSite()
                                                                .getShell());
                int ret=dlg.open();
                if (ret==Window.CANCEL)
                    return;

                SmartsWrapper wrapper = dlg.getSmartsWrapper();
                
                sfile.getSmarts().add( wrapper );

                savePrefsAndUpdate();
                
            }
        };
        addSmartsAction.setText("Add SMARTS");
        addSmartsAction.setToolTipText("Add a SMARTS entry");
        addSmartsAction.setImageDescriptor(Activator.getImageDecriptor( 
                                                         "icons/add_obj.gif" ));

        removeAction = new Action() {
            public void run() {
                
                if (!( viewer.getSelection() instanceof IStructuredSelection )){
                    showError("Please select one or more SMARTS to remove");
                    return;
                }
                IStructuredSelection ssel = (IStructuredSelection) viewer.
                getSelection();

                boolean prefsChanged=false;
                for (Object obj : ssel.toList()){
                    if ( obj instanceof SmartsWrapper ) {
                        SmartsWrapper sw = (SmartsWrapper)obj;
                        //Look up parent
                        for (SmartsFile sf : smartsInView){
                            if (sf.getSmarts().contains( sw )){
                                sf.getSmarts().remove( sw );
                            }
                        }
                    }
                    else if ( obj instanceof SmartsFile ) {
                        prefsChanged=true;
                        SmartsFile sFileToRemove = (SmartsFile) obj;
                        //For all lists, remove this smart
                        for (Iterator<IWorkbenchPart> it=editorSmartsMap.
                                keySet().iterator(); it.hasNext();){
                            IWorkbenchPart part=it.next();
                            editorSmartsMap.get( part ).remove( sFileToRemove );
                        }
                        smartsInView.remove( sFileToRemove );
                    }
                }
                
                if (prefsChanged){
                    //Write list to preferences
                    SmartsMatchingPrefsHelper.setPreferences( smartsInView );
                    viewer.refresh();
                }
                
            }
        };
        removeAction.setText("Remove SMARTS");
        removeAction.setToolTipText("Remove the selected SMARTS");
        removeAction.setImageDescriptor(Activator.getImageDecriptor( 
                                                      "icons/delete_obj.gif" ));
        
        showPropertiesViewAction = new Action() {
            public void run() {
                IWorkbenchPage page=getSite().getWorkbenchWindow().
                getActivePage();
                try {
                    page.showView( IPageLayout.ID_PROP_SHEET);
                } catch ( PartInitException e ) {
                  showError( "Could not show Properties View: "+e.getMessage());
                }
            }
        };
        showPropertiesViewAction.setText("Show Properties");
        showPropertiesViewAction.setToolTipText("Show the Properties View");
        showPropertiesViewAction.setImageDescriptor(Activator.getImageDecriptor(
                                                 "icons/table_row_props.gif" ));

        addFileAction = new Action() {
            public void run() {
                
                //Open dialog to input file
                FileDialog dlg=new FileDialog( viewer.getControl().getShell(), 
                                               SWT.OPEN );
                
                String file=dlg.open();
                if (file==null) return;
                
                logger.debug("Parsing possible SMARTS file: " + file);

                try {
                    parseSmartsFile(file);
                } catch ( IOException e ) {
                    showError( "Error parsing smarts file: " + e.getMessage() );
                }
                
            }
        };
        addFileAction.setText("Add SMARTS file");
        addFileAction.setToolTipText("Add smarts from file with structure: " +
        		"Name\tSMARTS on each line");
        addFileAction.setImageDescriptor(Activator.getImageDecriptor(
                                                 "icons/add_obj.gif" ));

        
    }

    protected void runSmartsMatching() {

        IEditorPart part=PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getActivePage().getActiveEditor();
        
        IEditorPart editor = getSupportedEditor( part );
        if (editor==null){
            if (!( part instanceof JChemPaintEditor )) {

                //Check if MolTable is active
                if (part instanceof net.bioclipse.cdk.ui.sdfeditor.editor
                        .MultiPageMoleculesEditorPart) {

                    executeInMolTableEditor(part);
                    return;
                }


                showMessage( "SmartsMatching is only supported for JCP " +
                "and MolTable editors." );
                return;
            }
        }

        JChemPaintEditor jcp=(JChemPaintEditor)editor;
        executeInJCP(jcp);

    }

    private void executeInMolTableEditor( IEditorPart part ) {

        MultiPageMoleculesEditorPart moltableTditor = 
                                             (MultiPageMoleculesEditorPart)part;
        
//        IContextService contextService = (IContextService) PlatformUI.
//                                         getWorkbench().
//                                         getService(IContextService.class);
//        if (contextService==null) return;
//        
//        for (Object cs : contextService.getActiveContextIds()){
//            if (MultiPageMoleculesEditorPart.JCP_CONTEXT.equals( cs )){
//                //JCP is active
//               Object obj = moltableTditor.getAdapter(JChemPaintEditor.class);
//                if (obj!= null){
//                    JChemPaintEditor jcp=(JChemPaintEditor)obj;
//                    executeInJCP( jcp );
//                }
//            }
//            
//        }
        
        //Moltable is active
        MoleculesEditor molEditor=moltableTditor.getMoleculesPage();
        
        molEditor.setUseExtensionGenerators( true );

        //Add configurator that calculates property and customizes rendering
        molEditor.setRenderer2DConfigurator( 
                                     new SmartsMatchingRendererConfigurator() );

        //Manually update the moltable
        molEditor.refresh();
        
      }

    private void executeInJCP( JChemPaintEditor jcp ) {

        ICDKMolecule mol = jcp.getCDKMolecule();
        
        //For each smarts...
        for (SmartsFile sfile : smartsInView){
            for (SmartsWrapper sw : sfile.getSmarts()){
                if (sw.getHits()!=null){
                    sw.getHits().clear();
                }
                if (sw.isActive() && sw.isValid())
                    processSmarts(sw, mol);
            }
        }
        
        viewer.refresh();
        
    }

    private void processSmarts( SmartsWrapper sw, ICDKMolecule mol ) {

        //Clear old matches
        sw.setHits( new ArrayList<SmartsHit>() );
        if (!cdk.isValidSmarts( sw.getSmartsString() )) return;

        List<IAtomContainer> lst;
        try {
            lst = cdk.getSmartsMatches( mol, sw.getSmartsString() );

            int i=1;
            if (lst!=null){

                for (IAtomContainer ac : lst){
                    SmartsHit hit = new SmartsHit("Hit " + i , ac);
                    hit.setParent( sw );
                    hit.setHitMolecule( mol );
                    sw.getHits().add( hit );
                    i++;
                }
            }
        } catch ( BioclipseException e ) {
            logger.equals("Error matching smiles: " + e.getMessage() );
        }


    }

    private void showMessage(String message) {
        MessageDialog.openInformation(
                                      viewer.getControl().getShell(),
                                      "Smarts Matching",
                                      message);
    }

    private void showError(String message) {
        MessageDialog.openError( 
                                      viewer.getControl().getShell(),
                                      "Smarts Matching",
                                      message);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    
    public void partActivated( IWorkbenchPart part ) {
        updateViewContent(part);
    }

    public void partBroughtToTop( IWorkbenchPart part ) {
        updateViewContent(part);
    }

    /**
     * When editor closed, remove entry in map
     */
    public void partClosed( IWorkbenchPart part ) {
        System.out.println("Part:" + part.getTitle() + " closed");
        if (!(getSupportedEditor(part)!=null)) return;

        if (editorSmartsMap.keySet().contains( part )){
            //Remove entry
            editorSmartsMap.remove( part );
        }
        if (getSite()!=null){
            if (getSite().getWorkbenchWindow()!=null){
                if (getSite().getWorkbenchWindow().getActivePage()!=null){
                    IEditorReference[] editors=getSite().getWorkbenchWindow().
                    getActivePage().getEditorReferences();
                    if (editors.length<=0){
                        partActivated( bogusWBPart );
                    }
                }
            }
        }
    }

    public void partDeactivated( IWorkbenchPart part ) {
    }

    public void partOpened( IWorkbenchPart part ) {
        updateViewContent(part);
        setAllChecked();
    }

    private void updateViewContent( IWorkbenchPart part ) {
        
//        if (getSupportedEditor(part)==null) return;
        if (part==currentPart) return;
        
        if ( part instanceof JChemPaintEditor ) {
            JChemPaintEditor jcp = (JChemPaintEditor) part;

            if (editorSmartsMap.keySet().contains( part )){
                //Use existing
                smartsInView=editorSmartsMap.get( part );
            }else {
                //Create new
                List<SmartsFile> newSmartsInView = SmartsMatchingPrefsHelper.
                getPreferences();
                
                // Register interest in changes from JCP editor
                jcp.addPropertyChangedListener( new IPropertyChangeListener() {
                    public void propertyChange( PropertyChangeEvent event ) {

                        if(event.getProperty().equals( JChemPaintEditor.
                                                    STRUCUTRE_CHANGED_EVENT )) {

                            // editor model has changed, clear everything fornow
                            //TODO: start new match in background thread
                            logger.debug(
                               ((JChemPaintEditor)event.getSource()).getTitle()
                               +" editor has changed");
                            
                            smartsInView = SmartsMatchingPrefsHelper.
                            getPreferences();
                            viewer.setInput(smartsInView);
                        }
                    }
                });
                
                editorSmartsMap.put( part, newSmartsInView );
                smartsInView=newSmartsInView;
                
            }
        }

        currentPart=part;
        
        viewer.setInput(smartsInView);
        viewer.expandAll();
        setViewerCheckedFromModel();
        
    }

    private void savePrefsAndUpdate() {

        //Write list to preferences
        SmartsMatchingPrefsHelper.setPreferences( smartsInView );

        //Clear all editors matches, they are now invalid
        editorSmartsMap.clear();
        editorSmartsMap.put( bogusWBPart, SmartsMatchingPrefsHelper.
                             getPreferences());

        //Update current editor, if open
        IEditorPart part=PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                             .getActivePage().getActiveEditor();
        if ((getSupportedEditor( part )!=null)){
            updateViewContent(part);
        }
        
        viewer.refresh();
    }

    
    public static void firePropertyChanged() {
        SmartsMatchingView view = getInstance();
        view.savePrefsAndUpdate();
        
    }

    
    private static SmartsMatchingView getInstance() {

        return smartsView;
    }

    @Override
    public void dispose() {

       //Unregister listening on parts
       getSite().getWorkbenchWindow().getPartService().removePartListener(this);
       super.dispose();
    }

    /**
     * Return the smarts for MolTable hits
     * @return
     */
    public static List<String> getActiveSmarts() {
        
        List<String> smarts=new ArrayList<String>();
        for (SmartsFile sf : smartsInView){
            for (SmartsWrapper sw : sf.getSmarts()){
                if (sw.isActive() && sw.isValid())
                    smarts.add(sw.getSmartsString());
            }
        }
        
        return smarts;
    }

    public void contextManagerChanged( ContextManagerEvent contextManagerEvent){

        IContextService contextService = (IContextService)PlatformUI
        .getWorkbench().getService(IContextService.class);
        
        if (getSite()==null) return;
        if (getSite().getWorkbenchWindow()==null) return;
        if (getSite().getWorkbenchWindow().getActivePage()==null) return;

        if (contextService.getActiveContextIds().contains( 
                                MultiPageMoleculesEditorPart.JCP_CONTEXT )){
            
            //MolTableEditor switched to tab JCP
            System.out.println("JCP context activated");
            IEditorPart editor = getSite().getWorkbenchWindow()
                                             .getActivePage().getActiveEditor();
            if (editor!=null){
                if ( editor instanceof MultiPageMoleculesEditorPart ) {
                    //Special case when SDF editor JCP is visible since same 
                    //editor, but different molecule.
                    IWorkbenchPart suped = getSupportedEditor( editor );

                    if (suped!=null){
                      //Handle new JCP
                      clearHits();
                    
                      updateView();
                    }                    
                }
                partActivated( editor );
            }
        }else{
            //MolTableEditor switched to tab other than JCP
            System.out.println("JCP context deactivated");
            IEditorPart editor = getSite().getWorkbenchWindow()
                                             .getActivePage().getActiveEditor();
            if (editor!=null)
                partActivated( editor );
        }
        
    }


    private void clearHits() {

        //Re-read from prefs, creates new objects
        smartsInView = SmartsMatchingPrefsHelper.getPreferences();
        viewer.setInput(smartsInView);
        
    }

    private void updateView() {

        System.out.println("Update SmartsView NOT IMPLEMENTED");

    }

    private IEditorPart getSupportedEditor( IWorkbenchPart part ) {
        if ( part instanceof JChemPaintEditor ) {
            logger.debug("We have a JCP editor for SmartsView!");
            return (IEditorPart)part;
        }
        else if ( part instanceof MoleculesEditor ) {
            //TODO: when does this happen?
            return (IEditorPart)part;
        }
        else if ( part instanceof MultiPageMoleculesEditorPart ) {
            logger.debug("We have a MPE editor for SmartsView");
            MultiPageMoleculesEditorPart editor = 
                    (MultiPageMoleculesEditorPart)part;

            if (editor.isJCPVisible()){
                //JCP is active
                Object obj = editor.getAdapter(JChemPaintEditor.class);
                if (obj!= null){
                    JChemPaintEditor jcp=(JChemPaintEditor)obj;
                    return jcp;
                }
            }
        }

        //Not supported editor
        return null;
    }
    
    protected void parseSmartsFile( String filename ) throws IOException {
        
        File file=new File(filename);
        BufferedReader reader = new BufferedReader( new FileReader( 
                                                           file ) );
        String name=file.getName();
        SmartsFile sfile=new SmartsFile();
        sfile.setName( name );

        String line=reader.readLine();
        while(line!=null){
            //Parse line
            String[] parts = line.split( "\t" );
            
            if (parts.length==2){
                SmartsWrapper sw=new SmartsWrapper(parts[0],parts[1]);
                sfile.addSmartsWrapper(sw);

            }else{
                logger.error("Error parsing line: " + line);
            }

            //get next line
            line=reader.readLine();            
        }

        smartsInView.add( sfile );
        savePrefsAndUpdate();
        
    }


    private void setViewerCheckedFromModel() {

        for (TreeItem item : viewer.getTree().getItems()){
            if ( item.getData() instanceof SmartsWrapper ) {
                SmartsWrapper sw = (SmartsWrapper) item.getData();
                item.setChecked( sw.isActive() );
            }

            for (TreeItem childitem : item.getItems()){
                if ( childitem.getData() instanceof SmartsWrapper ) {
                    SmartsWrapper sw = (SmartsWrapper) childitem.getData();
                    childitem.setChecked( sw.isActive() );
                }
            }
            
        }
        
    }

    private void setAllChecked() {

        for (TreeItem item : viewer.getTree().getItems()){
            item.setChecked( true );
            if ( item.getData() instanceof SmartsWrapper ) {
                SmartsWrapper sw = (SmartsWrapper) item.getData();
                sw.setActive( true );
            }

            for (TreeItem childitem : item.getItems()){
                if ( childitem.getData() instanceof SmartsWrapper ) {
                    SmartsWrapper sw = (SmartsWrapper) childitem.getData();
                    childitem.setChecked( true );
                    sw.setActive( true );
                }
            }
            
        }
        
    }




}
