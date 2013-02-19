/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org�epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.jmol.views.outline;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.bioclipse.jmol.Activator;
import net.bioclipse.jmol.editors.JmolProteinEditor;
import net.bioclipse.jmol.views.JmolAtomSelection;
import net.bioclipse.jmol.views.JmolPolymerSelection;
import net.bioclipse.jmol.views.JmolSelection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jmol.modelset.Atom;
import org.jmol.modelset.Chain;
import org.jmol.modelset.Model;
import org.jmol.modelset.ModelSet;
import org.jmol.modelsetbio.BioPolymer;
import org.jmol.modelsetbio.Monomer;

/**
 * An Outline Page for a Jmol model.
 * @author ola
 *
 */
public class JmolProteinContentOutlinePage
       extends ContentOutlinePage
       implements ISelectionListener,
                  IAdaptable,
                  ITabbedPropertySheetPageContributor {

    private final String CONTRIBUTOR_ID
        = "net.bioclipse.jmol.views.outline.JmolProteinContentOutlinePage";

    private static final Logger logger
        = Logger.getLogger(JmolContentOutlinePage.class);

    private org.jmol.viewer.Viewer jmolViewer;
    private IEditorPart part;
    private TreeViewer treeViewer;

    private CollapseAllHandler     collapseAllHandler;

    class JmolProteinOutlineContentProvider 
        implements IStructuredContentProvider, 
                   ITreeContentProvider {

        ModelSet modelset;    //The root object
        boolean isInitialized;
        Set<String> chains;    //Set of PDBchains

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }
        
        public void dispose() {
        }
        
        public Object[] getElements(Object parent) {
            return getChildren(parent);
        }

        public Object getParent(Object child) {

            if (child instanceof JmolObject) {
                JmolObject obj = (JmolObject) child;
                return obj.getParent();
            }

            return null;
        }
        
        public Object [] getChildren(Object parent) {

            if (parent instanceof JmolObject) {
                JmolObject o = (JmolObject) parent;
                if (o.getChildren()!=null){
                    if (o.getChildren().size()==1)
                        return getChildren(o.getChildren().get(0));
                    else
                        return o.getChildren().toArray();
                }
            }
            return new Object[0];
        }

        public boolean hasChildren(Object parent) {
            return getChildren(parent).length>0;
        }
    }

    class JmolProteinOutlineLabelProvider extends LabelProvider {

        public String getText(Object obj) {
            if (obj instanceof JmolObject) {
                JmolObject o = (JmolObject) obj;
                return o.getName();
            }

            if (obj instanceof Model) {
                Model model = (Model) obj;
                return "Model " + model.getModelIndex();
            }
            else if (obj instanceof BioPolymer) {
                BioPolymer polymer = (BioPolymer) obj;
                int[] ix=polymer.getLeadAtomIndices();
                return "BioPolymer " + ix[0] + "-" + ix[ix.length-1];
            }

            return obj.toString();
        }
        
        public Image getImage(Object obj) {

            String type = obj instanceof JmolAtom    ? "atom"
                        : obj instanceof JmolChain   ? "chain"
                        : obj instanceof JmolMonomer ? "peptide"
                        : obj instanceof JmolModel   ? "model"
                        : null;
            
            if (type != null) {
                return Activator.getImageDescriptor(
                           "icons/" + type + ".png"
                       ).createImage();
            }

            return PlatformUI.getWorkbench()
                             .getSharedImages()
                             .getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
    }

    class NameSorter extends ViewerSorter {
    }

    /**
     * Our constructor
     * @param editorInput
     * @param jmolEditor
     */
    public JmolProteinContentOutlinePage( IEditorInput editorInput,
                                   JmolProteinEditor jmolEditor ) {
        super();

        this.part  = jmolEditor;
        jmolViewer = jmolEditor.getJmolPanel().getViewer();
    }


    /**
     * Set up the treeviewer for the outline with Providers for Jmol
     */
    public void createControl(Composite parent) {

        super.createControl(parent);
        
        treeViewer = getTreeViewer();
        treeViewer.setContentProvider( new JmolProteinOutlineContentProvider() );
        treeViewer.setLabelProvider(   new JmolProteinOutlineLabelProvider()   );
        treeViewer.addSelectionChangedListener(this);
        
        updateTreeViewerModel();
        getSite().getPage().addSelectionListener(this);
        
        // This is needed to set focus to outline when clicked on an element in 
        // the treeviewer. Else no selection posted.
        treeViewer.getTree().addFocusListener(new FocusListener(){

            public void focusGained(FocusEvent e) {

                System.out.println("tree gain");
                IViewPart outline
                  = part.getSite().getPage().findView( IPageLayout.ID_OUTLINE );
                part.getSite().getPage().activate(outline);
            }

            public void focusLost(FocusEvent e) {
                // Not interested in this
            }
        });
        makeActions();
    }

    protected void makeActions() {

        IHandlerService service =
                        (IHandlerService) getSite()
                                        .getService( IHandlerService.class );
        collapseAllHandler = new CollapseAllHandler( treeViewer );
        service.activateHandler( "net.bioclipse.jmol.collapseAll",
                                 collapseAllHandler );
    }
    public void updateTreeViewerModel() {
        
        ModelSet modelSet = jmolViewer.getModelSet();
        if ( modelSet == null 
          || modelSet.getModels() == null
          || modelSet.getModels().length < 1 )
            return;

        JmolModelSet ms = new JmolModelSet(modelSet);
        treeViewer.setInput(ms);
        treeViewer.expandToLevel(2);
    }

    /**
     * Update selected items if selected in Jmol
     */
    public void selectionChanged(IWorkbenchPart selectedPart,
                                 ISelection selection) {

        //Only react on selections in the corresponding editor
        if (part!=selectedPart) return;

        if (selection instanceof JmolSelection) {
            JmolSelection jmolSelection = (JmolSelection) selection;

            IStructuredSelection sel;
            if ( jmolSelection.isEmpty() ) {
                sel = new StructuredSelection();
                treeViewer.setSelection(sel);
            }
            else {
                //Look up in tree and select it
                JmolModelSet ms = (JmolModelSet) treeViewer.getInput();

                List<JmolObject> objs = findInModelSet(jmolSelection, ms);

                //If none found, just don't select anything
                if (objs == null) return;

                sel = new StructuredSelection(objs);
                treeViewer.setSelection(sel);
                treeViewer.reveal(objs);
            }
            fireSelectionChanged(sel);
        }
    }


    /**
     * Look up a Jmol monomer in Outline based on JmolSelection
     * @param jmolSelection
     * @param ms
     * @return
     */
    private List<JmolObject> findInModelSet( JmolSelection jmolSelection,
                                             JmolModelSet ms ) {
        
        List<JmolObject> result = new ArrayList<JmolObject>();
        
        for ( String str : jmolSelection ) {
            
            if (jmolSelection instanceof JmolPolymerSelection) {
                JmolPolymerSelection polymerSelection
                    = (JmolPolymerSelection) jmolSelection;
    
                String monomerStr=polymerSelection.getMonomer();
                String chainStr=polymerSelection.getChain();
                if (chainStr==null) chainStr="";
    
                //Locate chain in modelset via model
                JmolChain chain=null;
                for (IJmolObject jobj : ms.getChildren()){
                    if (jobj instanceof JmolModel) {
                        JmolModel jmobj=(JmolModel)jobj;
                        for (IJmolObject jmobjChild : jmobj.getChildren()){
                            if (jmobjChild instanceof JmolChain) {
                                JmolChain cobj = (JmolChain) jmobjChild;
                                Chain c=(Chain) cobj.getObject();
                                String cid=String.valueOf(c.getChainID());
                                if (cid.equals(chainStr))
                                    chain=cobj;
                            }
                        }
                    }
                }
    
                //If chain found, search for monomers in the chain
                if (chain!=null){
                    JmolMonomer monomer=null;
                    for (IJmolObject jobj : chain.getChildren()){
                        if (jobj instanceof JmolMonomer) {
                            JmolMonomer mobj = (JmolMonomer) jobj;
                            Monomer c=(Monomer) mobj.getObject();
                            String mid=String.valueOf(c.getSeqNumber());
                            if (mid.equals(monomerStr))
                                monomer=mobj;
                        }
                    }
    
                    //If we have a monomer, return it
                    if (monomer!=null){
                        result.add( monomer ); 
                        continue;
                    }
                }
            }
            else if (jmolSelection instanceof JmolAtomSelection) {
                String[] parts=str.split("=");
    
                String atomno=parts[1];
    
                for (IJmolObject jobj : ms.getChildren()){
                    JmolModel jmobj=(JmolModel)jobj;
                    for (IJmolObject jmobjChild : jmobj.getChildren()){
                        if (jmobjChild instanceof JmolChain) {
                            JmolChain cobj = (JmolChain) jmobjChild;
                            List<IJmolObject> objs=cobj.getChildren();
                            for (IJmolObject o : objs){
                                if ( o instanceof JmolGroup ) {
                                    JmolGroup gr=(JmolGroup)o;
                                    List<IJmolObject> atoms=gr.getChildren();
                                    for (IJmolObject patom : atoms){
                                        JmolAtom atom=(JmolAtom)patom;
                                        Atom jmolAtom=(Atom) atom.getObject();
                                        if ( Integer.valueOf(atomno)
                                                    .intValue()-1
                                            == jmolAtom.getAtomIndex()){
                                            result.add( atom );
                                            continue;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * This is our ID for the TabbedPropertiesContributor
     */
    public String getContributorId() {
        return CONTRIBUTOR_ID;
    }

    /**
     * Provide adapter for TabbedPropertyPage
     */
    @SuppressWarnings("unchecked") // cannot fix because of inheritance
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySheetPage.class)
            return new TabbedPropertySheetPage(this);
        return null;
    }
}
