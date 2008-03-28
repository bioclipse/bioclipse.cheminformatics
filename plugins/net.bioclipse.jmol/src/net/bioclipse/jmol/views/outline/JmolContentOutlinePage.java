/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.jmol.views.outline;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.bioclipse.jmol.editors.JmolEditor;
import net.bioclipse.jmol.views.JmolSelection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.presentations.util.LeftToRightTabOrder;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jmol.modelset.Chain;
import org.jmol.modelset.Model;
import org.jmol.modelset.ModelSet;
import org.jmol.modelset.Polymer;
import org.jmol.modelsetbio.BioPolymer;
import org.jmol.modelsetbio.Monomer;

/**
 * An Outline Page for a Jmol model.
 * @author ola
 *
 */
public class JmolContentOutlinePage extends ContentOutlinePage implements ISelectionListener, IAdaptable, ITabbedPropertySheetPageContributor {

	private final String CONTRIBUTOR_ID="net.bioclipse.jmol.views.outline.JmolContentOutlinePage";

	private static final Logger logger = Logger.getLogger(JmolContentOutlinePage.class);
	
	
	class JmolOutlineContentProvider implements IStructuredContentProvider, 
	ITreeContentProvider {

		ModelSet modelset;	//The root object
		boolean isInitialized;
		Set<String> chains;	//Set of PDBchains

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

	class JmolOutlineLabelProvider extends LabelProvider {

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
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}

	class NameSorter extends ViewerSorter {
	}






	private org.jmol.viewer.Viewer jmolViewer;
	private IEditorInput editorInput;
	private IEditorPart part;
	private TreeViewer treeViewer;

	/**
	 * Our constructor
	 * @param editorInput
	 * @param jmolEditor
	 */
	public JmolContentOutlinePage(IEditorInput editorInput, JmolEditor jmolEditor) {
		super();

		this.editorInput=editorInput;
		this.part=jmolEditor;
		jmolViewer=jmolEditor.getJmolPanel().getViewer();

	}


	/**
	 * Set up the treeviewer for the outline with Providers for Jmol
	 */
	public void createControl(Composite parent) {

		super.createControl(parent);

		if (jmolViewer.getModelSet()==null)
			return;
		if (jmolViewer.getModelSet().getModels()==null)
			return;
		if (jmolViewer.getModelSet().getModels().length<1)
			return;

		treeViewer= getTreeViewer();
		treeViewer.setContentProvider(new JmolOutlineContentProvider());
		treeViewer.setLabelProvider(new JmolOutlineLabelProvider());
//		viewer.setSorter(new NameSorter());
		treeViewer.addSelectionChangedListener(this);

		JmolModelSet ms=new JmolModelSet(jmolViewer.getModelSet());
		treeViewer.setInput(ms);
		treeViewer.expandToLevel(2);

		getSite().getPage().addSelectionListener(this);
	}


	/**
	 * Update selected items if selected in Jmol
	 */
	public void selectionChanged(IWorkbenchPart selectedPart, ISelection selection) {

		//Only react on selections in the corresponding editor
		if (part!=selectedPart) return;

		if (selection instanceof JmolSelection) {
			JmolSelection jmolSelection = (JmolSelection) selection;
			System.out.println("** jmol selection caught in outline: " + jmolSelection.getFirstElement().toString());

			//Look up in tree and select it
			JmolModelSet ms=(JmolModelSet) treeViewer.getInput();

			JmolObject obj=findInModelSet(jmolSelection, ms);
			
			//If none found, just don't select anything
			if (obj==null) return;
			
			logger.debug("Found obj: " + obj.getName());
			IStructuredSelection sel=new StructuredSelection(obj);
			treeViewer.setSelection(sel);
			treeViewer.reveal(obj);
			fireSelectionChanged(sel);

		}
	}


	/**
	 * Look up a Jmol monomer in Outline based on JmolSelection
	 * @param jmolSelection
	 * @param ms
	 * @return
	 */
	private JmolObject findInModelSet(JmolSelection jmolSelection, JmolModelSet ms) {
		String str=(String)jmolSelection.getFirstElement();
		String[] parts=str.split(":");

		String monomerStr=parts[0];
		String chainStr=parts[1];

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
				return monomer;
			}
		}



		return null;
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
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySheetPage.class)
			return new TabbedPropertySheetPage(this);
		return null;
	}


}
