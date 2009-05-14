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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.smartsmatching.Activator;
import net.bioclipse.cdk.smartsmatching.AddEditSmartsDialog;
import net.bioclipse.cdk.smartsmatching.model.SmartsHit;
import net.bioclipse.cdk.smartsmatching.model.SmartsWrapper;
import net.bioclipse.cdk.smartsmatching.prefs.SmartsMatchingPrefsHelper;
import net.bioclipse.core.business.BioclipseException;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.openscience.cdk.interfaces.IAtomContainer;


/**
 * A view for matching smarts in open editors and views
 * @author ola
 *
 */
public class SmartsMatchingView extends ViewPart implements IPartListener{

    private static final Logger logger = Logger.getLogger(CDKManager.class);

    private static TreeViewer viewer;
    private Action addSmartsAction;
    private Action removeSmartsAction;

    private Action runAction;

    private static Map<IWorkbenchPart,List<SmartsWrapper>> editorSmartsMap;
    private static List<SmartsWrapper> smartsInView;

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

    private Action expandAllAction;

    private Action collapseAllAction;

    private Action showPropertiesViewAction;


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
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new SmartsMatchingContentProvider());
        viewer.setLabelProvider(new SmartsMatchingLabelProvider());
        viewer.setSorter(new ViewerSorter());
        
        cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
        
        //Read prefs for stored smarts
        smartsInView = SmartsMatchingPrefsHelper.getPreferences();
        viewer.setInput(smartsInView);
        editorSmartsMap=new HashMap<IWorkbenchPart, List<SmartsWrapper>>();
        editorSmartsMap.put( bogusWBPart, smartsInView);
        

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "net.bioclipse.cdk.smartsmatching.viewer");
        makeActions();
        hookContextMenu();
        contributeToActionBars();

        //Listen for part lifecycle events to react on editors
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);

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
        manager.add(removeSmartsAction);
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
        manager.add(removeSmartsAction);
    }

    private void makeActions() {

        collapseAllAction = new Action() {
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAllAction.setText("Collapse all");
        collapseAllAction.setToolTipText("Collapse all SMARTS");
        collapseAllAction.setImageDescriptor(Activator.getImageDecriptor( "icons/collapseall.gif" ));

        expandAllAction = new Action() {
            public void run() {
                viewer.expandAll();
            }
        };
        expandAllAction.setText("Expand all");
        expandAllAction.setToolTipText("Expand all SMARTS to reveal hits");
        expandAllAction.setImageDescriptor(Activator.getImageDecriptor( "icons/expandall.gif" ));

        
        runAction = new Action() {
            public void run() {
                runSmartsMatching();
            }
        };
        runAction.setText("Run matching");
        runAction.setToolTipText("Run SMARTS matching for the active aditor");
        runAction.setImageDescriptor(Activator.getImageDecriptor( "icons/smallRun.gif" ));

        clearAction = new Action() {
            public void run() {
                //Re-read from prefs, creates new objects
                smartsInView = SmartsMatchingPrefsHelper.getPreferences();
                viewer.setInput(smartsInView);
            }
        };
        clearAction.setText("Clear matches");
        clearAction.setToolTipText("Clear all SMARTS matches");
        clearAction.setImageDescriptor(Activator.getImageDecriptor( "icons/clear_co.gif" ));

        addSmartsAction = new Action() {
            public void run() {
                
                
                AddEditSmartsDialog dlg=new AddEditSmartsDialog(getSite().getShell());
                int ret=dlg.open();
                if (ret==Window.CANCEL)
                    return;

                SmartsWrapper wrapper = dlg.getSmartsWrapper();

                //Add to current list
                smartsInView.add( wrapper );
                
                savePrefsAndUpdate();
                
            }
        };
        addSmartsAction.setText("Add SMARTS");
        addSmartsAction.setToolTipText("Add a SMARTS entry");
        addSmartsAction.setImageDescriptor(Activator.getImageDecriptor( "icons/add_obj.gif" ));

        removeSmartsAction = new Action() {
            public void run() {
                
                if (!( viewer.getSelection() instanceof IStructuredSelection )) {
                    showError("Please select one or more SMARTS to remove");
                    return;
                }
                IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();

                boolean prefsChanged=false;
                for (Object obj : ssel.toList()){
                    if ( obj instanceof SmartsWrapper ) {
                        prefsChanged=true;
                        SmartsWrapper swToRemove = (SmartsWrapper) obj;
                        //For all lists, remove this smart
                        for (Iterator<IWorkbenchPart> it=editorSmartsMap.keySet().iterator(); it.hasNext();){
                            IWorkbenchPart part=it.next();
                            editorSmartsMap.get( part ).remove( swToRemove );
                        }
                        smartsInView.remove( swToRemove );
                    }
                }
                
                if (prefsChanged){
                    //Write list to preferences
                    SmartsMatchingPrefsHelper.setPreferences( smartsInView );
                    viewer.refresh();
                }
                
            }
        };
        removeSmartsAction.setText("Remove SMARTS");
        removeSmartsAction.setToolTipText("Remove the selected SMARTS");
        removeSmartsAction.setImageDescriptor(Activator.getImageDecriptor( "icons/delete_obj.gif" ));
        
        showPropertiesViewAction = new Action() {
            public void run() {
                IWorkbenchPage page=getSite().getWorkbenchWindow().getActivePage();
                try {
                    page.showView( IPageLayout.ID_PROP_SHEET);
                } catch ( PartInitException e ) {
                    showError( "Could not show Properties View: " + e.getMessage() );
                }
            }
        };
        showPropertiesViewAction.setText("Show Properties");
        showPropertiesViewAction.setToolTipText("Show the Properties View");
        showPropertiesViewAction.setImageDescriptor(Activator.getImageDecriptor( "icons/table_row_props.gif" ));

        
    }

    protected void runSmartsMatching() {

        IEditorPart part=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (!( part instanceof JChemPaintEditor )) {
            showMessage( "You must have an open 2D editor to run SmartsMatching on." );
            return;
        }

        JChemPaintEditor jcp=(JChemPaintEditor)part;

        ICDKMolecule mol = jcp.getCDKMolecule();
        
        //For each smarts...
        for (SmartsWrapper sw : smartsInView){
            if (sw.getHits()!=null){
                sw.getHits().clear();
            }
            processSmarts2(sw, mol);
        }
        
        viewer.refresh();

    }

    private void processSmarts2( SmartsWrapper sw, ICDKMolecule mol ) {

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
        if (!(isSupportedEditor(part))) return;

        if (editorSmartsMap.keySet().contains( part )){
            //Remove entry
            editorSmartsMap.remove( part );
        }
        if (getSite()!=null){
            if (getSite().getWorkbenchWindow()!=null){
                if (getSite().getWorkbenchWindow().getActivePage()!=null){
                    IEditorReference[] editors=getSite().getWorkbenchWindow().getActivePage().getEditorReferences();
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
    }

    private static void updateViewContent( IWorkbenchPart part ) {
        
        if (!(isSupportedEditor(part))) return;
        if (part==currentPart) return;

        if (editorSmartsMap.keySet().contains( part )){
            //Use existing
            smartsInView=editorSmartsMap.get( part );
        }else {
            //Create new
            List<SmartsWrapper> newSmartsInView = SmartsMatchingPrefsHelper.getPreferences();
            editorSmartsMap.put( part, newSmartsInView );
            smartsInView=newSmartsInView;
        }

        currentPart=part;
        
        viewer.setInput(smartsInView);
        
    }

    private static boolean isSupportedEditor( IWorkbenchPart part ) {
        if ( part instanceof JChemPaintEditor ) {
            return true;
        }
        if ( part==bogusWBPart ) {
            return true;
        }

        
        //TODO: update here to support MolTable as well

        //Not supported
        return false;
    }

    private static void savePrefsAndUpdate() {

        //Write list to preferences
        SmartsMatchingPrefsHelper.setPreferences( smartsInView );

        //Clear all editors matches, they are now invalid
        editorSmartsMap.clear();
        editorSmartsMap.put( bogusWBPart, SmartsMatchingPrefsHelper.getPreferences());

        //Update current editor, if open
        IEditorPart part=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if ((isSupportedEditor( part ))){
            updateViewContent(part);
        }
        
        viewer.refresh();
    }

    
    public static void firePropertyChanged() {
        savePrefsAndUpdate();
        
    }

//    @Override
//    public Object getAdapter( Class adapter ) {
//        
//        if (adapter.equals(IContextProvider.class)) {
//            return new SmartsContextProvider();
//          }
//        return super.getAdapter( adapter );
//    }

    @Override
    public void dispose() {

        //Unregister listening on parts
        getSite().getWorkbenchWindow().getPartService().removePartListener(this);
        super.dispose();
    }

}
