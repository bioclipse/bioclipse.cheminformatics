/*******************************************************************************
 * Copyright (c) 2008  Ola Spjuth
 *                     Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.outline;

import net.bioclipse.cdk.domain.CDKChemObject;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

public class JCPOutlinePage extends ContentOutlinePage
                                    implements ISelectionListener, IAdaptable {

    private final String CONTRIBUTOR_ID =
        "net.bioclipse.cdk.jchempaint.outline.JCPOutlinePage";

    private JChemPaintEditor editor;
    private TreeViewer treeViewer;


    public JCPOutlinePage(JChemPaintEditor editor) {
        this.editor = editor;
    }
    IChemModel chemModel;

    /**
     * Set up the TreeViewer for the outline with Providers for JCPEditor.
     */
    public void createControl(Composite parent) {
        super.createControl(parent);

        treeViewer= getTreeViewer();
        treeViewer.setContentProvider(new StructureContentProvider());
        treeViewer.setLabelProvider(new StructureLabelProvider());
        //viewer.setSorter(new NameSorter());
        treeViewer.addSelectionChangedListener(this);

        if (editor.getControllerHub()==null) return;

        chemModel= editor.getControllerHub().getIChemModel();

        treeViewer.setComparer( new IElementComparer() {

            public boolean equals( Object a, Object b ) {

                if(a instanceof CDKChemObject && b instanceof CDKChemObject) {
                    return ((CDKChemObject)a).getChemobj().equals(
                            ((CDKChemObject)b).getChemobj());
                }
                return a.equals( b );
            }

            public int hashCode( Object element ) {

                if(element instanceof CDKChemObject) {
                    return ((CDKChemObject)element).getChemobj().hashCode();
                }
                return element.hashCode();
            }

        });

        treeViewer.setInput(chemModel);
        treeViewer.expandToLevel(2);

        getSite().getPage().addSelectionListener(this);
        getSite().setSelectionProvider( treeViewer );
    }

    /**
     * Update selected items if selected in editor
     */
    public void selectionChanged(IWorkbenchPart selectedPart,
                                 ISelection selection) {
      if(selectedPart.equals( this.getSite().getPage().getActiveEditor() )) {

          treeViewer.setSelection( selection );
      }
    }

    /**
     * This is our ID for the TabbedPropertiesContributor
     */
    public String getContributorId() {
        return CONTRIBUTOR_ID;
    }

    public void setInput(IChemModel model) {
        this.chemModel = model;
        if(treeViewer!=null) {
            treeViewer.refresh();
            treeViewer.setSelection( editor.getWidget().getSelection() );
            treeViewer.expandAll();// FIXME This should restore the expanded state
        }
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        // TODO Auto-generated method stub
        return null;
    }

}
