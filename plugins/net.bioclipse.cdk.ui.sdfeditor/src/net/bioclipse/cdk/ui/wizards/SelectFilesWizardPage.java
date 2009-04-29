/*
 * Copyright (C) 2005 Bioclipse Project
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Kuhn - Implementation of new sdf wizard
 */
package net.bioclipse.cdk.ui.wizards;

import net.bioclipse.chemoinformatics.contentlabelproviders.MoleculeFileContentProvider;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class SelectFilesWizardPage extends WizardPage {
        private boolean withCheckbox;
        private IStructuredSelection selectedFiles = null;
        private Button includeRecursivlyButton;
        protected SelectFilesWizardPage(boolean withCheckbox) {
                super("New SD File");
                this.withCheckbox=withCheckbox;
                setTitle("Select files to include");
                setDescription("All structures you select will be added to the sd file.");
        }
        public void createControl(Composite parent) {
                Composite container = new Composite(parent, SWT.NULL);
                GridLayout layout = new GridLayout();
                container.setLayout(layout);
                layout.numColumns = 2;
                layout.verticalSpacing = 9;
                TreeViewer treeViewer = new TreeViewer(container);
                treeViewer.setContentProvider(new MoleculeFileContentProvider());
                treeViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
                treeViewer.setUseHashlookup(true);
                //Layout the tree viewer below the text field
                GridData layoutData = new GridData();
                layoutData.grabExcessHorizontalSpace = true;
                layoutData.grabExcessVerticalSpace = true;
                layoutData.horizontalAlignment = GridData.FILL;
                layoutData.verticalAlignment = GridData.FILL;
                layoutData.horizontalSpan = 3;
                treeViewer.getControl().setLayoutData(layoutData);
                treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot().findMember("."));
                treeViewer.expandToLevel(2);
                treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                        public void selectionChanged(SelectionChangedEvent event) {
                                ISelection sel = event.getSelection();
                                if (sel instanceof IStructuredSelection) {
                                        selectedFiles=(IStructuredSelection)sel;
                                        if(((IStructuredSelection)sel).getFirstElement()!=null)
                                                SelectFilesWizardPage.this.setPageComplete(true);
                                }
                        }
                });
                treeViewer.setSelection(new StructuredSelection(ResourcesPlugin.getWorkspace().getRoot().findMember(".")));
                if(withCheckbox){
                    includeRecursivlyButton = new Button(container, SWT.CHECK);
                    Label includeRecursivlyLabel = new Label(container, SWT.NULL);
                    includeRecursivlyLabel.setText("Include directories recursivly");
                }
                setPageComplete(false);
                setControl(container);
        }
        public IStructuredSelection getSelectedRes() {
                return this.selectedFiles;
        }
        public boolean doRecursive(){
                return includeRecursivlyButton.getSelection();
        }
}
