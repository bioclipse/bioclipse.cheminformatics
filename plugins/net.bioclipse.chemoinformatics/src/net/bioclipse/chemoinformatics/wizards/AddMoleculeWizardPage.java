/*******************************************************************************
* Copyright (c) 2008-2009 Stefan Kuhn <stefan.kuhn@ebi.ac.uk>
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
*
* Contact: http://www.bioclipse.net/
******************************************************************************/
package net.bioclipse.chemoinformatics.wizards;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.chemoinformatics.contentlabelproviders.MoleculeFileContentProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A wizard page for selecting a molecule file.
 *
 */
public class AddMoleculeWizardPage extends WizardPage {

    private ArrayList<IFile> selectedFiles;
    private Button     checkBox;
    private String     checkBoxLabel;
    private TreeViewer treeViewer;
    private boolean    multipleSelection;

    /**
     * Constructor for the wizard page.
     * 
     * @param title        Will be used as title of the page.
     * @param description  Will be used as description of the page.
     * @param multipleSelection true=multiple selection possible, 
     * false= one selection only.
     */
    public AddMoleculeWizardPage( String title, 
                                  String description, 
                                  boolean multipleSelection ) {

        this( title, description, null, multipleSelection );
    }

    /**
     * Constructor for the wizard page. Using this constructor adds a checkbox 
     * with the label checkBoxLabel to the page. isCheckboxChecked can be used 
     * to get the value of the checkbox.
     * 
     * @param title        Will be used as title of the page.
     * @param description  Will be used as description of the page.
     * @param checkBoxLabel The label of the check box.
     * @param multipleSelection true=multiple selection possible, 
     * false= one selection only.
     */
    public AddMoleculeWizardPage(String title, String description,
            String checkBoxLabel, boolean multipleSelection) {

        super( title );
        setTitle( title );
        setDescription( description );
        this.checkBoxLabel = checkBoxLabel;
        this.multipleSelection = multipleSelection;
    }

    public void createControl( Composite parent ) {

        Composite container = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        container.setLayout( layout );
        layout.numColumns = 2;
        layout.verticalSpacing = 9;

        if(multipleSelection)
            treeViewer = new TreeViewer( container );
        else
            treeViewer = new TreeViewer( container, SWT.SINGLE | SWT.H_SCROLL | 
                                         SWT.V_SCROLL | SWT.BORDER);
        treeViewer.setContentProvider( new MoleculeFileContentProvider() );
        treeViewer.setUseHashlookup( true );

        // Layout the tree viewer below the text field
        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.FILL;
        layoutData.horizontalSpan = 3;
        treeViewer.getControl().setLayoutData( layoutData );
        treeViewer.setInput( ResourcesPlugin.getWorkspace().getRoot()
                .findMember( "." ) );
        treeViewer.expandToLevel( 2 );
        treeViewer.setLabelProvider( WorkbenchLabelProvider
                .getDecoratingWorkbenchLabelProvider() );
        treeViewer
                .addSelectionChangedListener( new ISelectionChangedListener() {

                    public void selectionChanged( 
                                    SelectionChangedEvent event ) {

                        updateMessage();
                    }

                } );
        treeViewer.setSelection( new StructuredSelection( ResourcesPlugin
                .getWorkspace().getRoot().findMember( "." ) ) );

        // add a checkbox if desirec
        if ( checkBoxLabel != null ) {
            checkBox = new Button( container, SWT.CHECK );
            checkBox.addSelectionListener( new SelectionListener() {

                public void widgetDefaultSelected( SelectionEvent e ) {

                    updateMessage();

                }

                public void widgetSelected( SelectionEvent e ) {

                    updateMessage();
                }

            } );
            Label emptyMolLabel = new Label( container, SWT.NULL );
            emptyMolLabel.setText( checkBoxLabel );
        }
        setControl( container );
    }

    /**
     * Updates the error message and disables next/finish 
     * if no molecule is choosen.
     */
    protected void updateMessage() {

        ISelection sel = treeViewer.getSelection();
        if ( sel instanceof IStructuredSelection ) {
            selectedFiles = new ArrayList<IFile>();
            for (Object obj : ((IStructuredSelection)sel).toList()){
                if ( obj instanceof IFile ) {
                    IFile file = (IFile) obj;
                    selectedFiles.add(file);
                }
            }
            if ( selectedFiles.size()>0 
                 || (checkBox != null && checkBox.getSelection() )) {
                setErrorMessage( null );
                setPageComplete( true );
            }else{
                setErrorMessage( "Please select a molecule file!" );
                setPageComplete( false );
            }
        }
    }

    /**
     * Returns the selected file.
     * 
     * @return The molecule file selected by the user.
     */
    public List<IFile> getSelectedRes() {

        return this.selectedFiles;
    }

    /**
     * Tells if the optional checkbox is checked.
     * 
     * @return false=no checkbox or not checked, true=checked.
     */
    public boolean isCheckboxChecked() {

        return checkBox != null && checkBox.getSelection();
    }

}
