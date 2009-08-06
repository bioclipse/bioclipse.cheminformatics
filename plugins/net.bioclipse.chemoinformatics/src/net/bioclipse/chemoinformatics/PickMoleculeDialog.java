/*******************************************************************************
 * Copyright (c) 2009  Jonathan Alvarsson <jonalv@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.chemoinformatics;

import java.util.ArrayList;

import net.bioclipse.chemoinformatics.util.MoleculeContentTypeViewerFilter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;


/**
 * A dialog for selecting a molecule somewhere in the workspace.
 * 
 * @author jonalv
 *
 */
public class PickMoleculeDialog extends TitleAreaDialog 
                                implements ISelectionChangedListener {

    private TreeViewer treeViewer;
    private String title;
    private String message;
    private ArrayList<IFile> selectedFiles;

    /**
     * @param parentShell
     */
    public PickMoleculeDialog( Shell parentShell ) {
        super( parentShell );
    }

    public PickMoleculeDialog( Shell parentShell, 
                               String title, 
                               String message ) {
        super( parentShell );
        this.title = title;
        this.message = message;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {

        Composite dialogArea = new Composite( parent, SWT.NONE  );
        GridLayout layout = new GridLayout();
        dialogArea.setLayout( layout );

        treeViewer = createViewer( dialogArea );
        GridData data = new GridData( GridData.FILL_BOTH );
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.heightHint = 400;
        data.widthHint = 300;
        treeViewer.getControl().setLayoutData( data );
        treeViewer.addSelectionChangedListener( this );

        setTitle( title == null ? "Select molecule"
                                : title);
        setMessage( message == null ? "Select a molecule from your workspace" 
                                    : message );
        return dialogArea;
    }

    protected TreeViewer createViewer(Composite parent) {
        TreeViewer viewer = new TreeViewer( parent, SWT.MULTI 
                                                  | SWT.H_SCROLL 
                                                  | SWT.V_SCROLL 
                                                  | SWT.BORDER );
        viewer.setUseHashlookup( true );
        viewer.setContentProvider( new WorkbenchContentProvider() );
        viewer.setLabelProvider( new WorkbenchLabelProvider() );
        viewer.addFilter( new MoleculeContentTypeViewerFilter() );
        viewer.setInput( ResourcesPlugin.getWorkspace().getRoot() );
        viewer.expandToLevel(2);
        return viewer;
    }

    /**
     * React on treeviewer changes to be able to return the selected resources
     */
    public void selectionChanged( SelectionChangedEvent event ) {

        IStructuredSelection selection 
            = (IStructuredSelection) event.getSelection();
        selectedFiles = new ArrayList<IFile>();
        for ( Object obj : selection.toList() ) {
            if ( obj instanceof IFile ) {
                IFile file = (IFile) obj;
                selectedFiles.add( file );
            }
        }
    }

    public ArrayList<IFile> getSelectedFiles() {
        return selectedFiles;
    }
}
