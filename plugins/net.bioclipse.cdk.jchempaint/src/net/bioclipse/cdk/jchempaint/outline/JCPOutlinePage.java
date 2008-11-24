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

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

public class JCPOutlinePage extends ContentOutlinePage
                                    implements ISelectionListener, IAdaptable {

    private final String CONTRIBUTOR_ID =
        "net.bioclipse.cdk.jchempaint.outline.JCPOutlinePage";

    private JChemPaintEditor editor;
    private TreeViewer treeViewer;

    IChemModel chemModel;

    public JCPOutlinePage(IEditorInput editorInput,
                          JChemPaintEditor editor) {
        super();
        this.editor = editor;
    }

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

        if (editor.getCDKMolecule() == null) return;

        chemModel = ChemModelManipulator.newChemModel(
            editor.getCDKMolecule().getAtomContainer()
        );

        treeViewer.setInput(chemModel);
        treeViewer.expandToLevel(2);

        getSite().getPage().addSelectionListener(this);
    }

    /**
     * Update selected items if selected in editor
     */
    public void selectionChanged(IWorkbenchPart selectedPart,
                                 ISelection selection) {
        // TODO Auto-generated method stub
        // Does nothing for now. See selectionChanged in
        // net.bioclipse.jmol.views.outline.JmolContentOutlinePage
        // for implementation inspiration.
    }

    /**
     * This is our ID for the TabbedPropertiesContributor
     */
    public String getContributorId() {
        return CONTRIBUTOR_ID;
    }

    public Object getAdapter(Class adapter) {
        // TODO Auto-generated method stub
        return null;
    }

}
