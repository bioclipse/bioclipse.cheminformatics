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
import net.bioclipse.chemoinformatics.wizards.WizardHelper;
import net.bioclipse.ui.contentlabelproviders.FolderContentProvider;
import net.bioclipse.ui.contentlabelproviders.FolderLabelProvider;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name.
 */
public class NewSDFileWizardPage extends WizardPage {
        private Text dirText;
        private Text fileText;
        private IResource selectedFolder = null;
        /**
         * Constructor for SampleNewWizardPage.
         * 
         * @param pageName
         */
        public NewSDFileWizardPage() {
                super("New SD File");
                setTitle("Choose name for new file");
                setDescription("Choose a name.");
        }
        /**
         * @see IDialogPage#createControl(Composite)
         */
        public void createControl(Composite parent) {
                Composite container = new Composite(parent, SWT.NULL);
                GridLayout layout = new GridLayout();
                container.setLayout(layout);
                layout.numColumns = 3;
                layout.verticalSpacing = 9;
                Label label = new Label(container, SWT.NULL);
                label.setText("&File Directory:");
                dirText = new Text(container, SWT.BORDER | SWT.SINGLE);
                GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                dirText.setLayoutData(gd);
                if(selectedFolder!=null)
                    dirText.setText( selectedFolder.getFullPath().toOSString() );
                gd.horizontalSpan = 3;
                dirText.addModifyListener(new ModifyListener() {
                        public void modifyText(ModifyEvent e) {
                                dialogChanged();
                        }
                });
                TreeViewer treeViewer = new TreeViewer(container);
                treeViewer.setContentProvider(new FolderContentProvider());
                treeViewer.setLabelProvider(new DecoratingLabelProvider(
                                new FolderLabelProvider(), PlatformUI.getWorkbench()
                                                .getDecoratorManager().getLabelDecorator()));
                treeViewer.setUseHashlookup(true);
                // Layout the tree viewer below the text field
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
                                        Object element = ((IStructuredSelection) sel)
                                                        .getFirstElement();
                                        if (element instanceof IFolder) {
                                                selectedFolder = (IFolder) element;
                                                String path = ((IFolder) element).getFullPath().toOSString();
                                                dirText.setText(path);
                                        } else if(element instanceof IProject){
                                                selectedFolder = (IProject) element;
                                                dirText.setText(((IProject) element).getFullPath().toOSString());
                                        }
                                }
                        }
                });
                treeViewer.setSelection(new StructuredSelection(ResourcesPlugin.getWorkspace().getRoot().findMember(".")));
                label = new Label(container, SWT.NULL);
                label.setText("&File name:");
                fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
                gd = new GridData(GridData.FILL_HORIZONTAL);
                fileText.setLayoutData(gd);
                if(selectedFolder!=null)
                    fileText.setText( WizardHelper.findUnusedFileName( new StructuredSelection(selectedFolder), "unnamed", ".sdf" ) );
                fileText.addModifyListener(new ModifyListener() {
                        public void modifyText(ModifyEvent e) {
                                dialogChanged();
                        }
                });
                dialogChanged();
                setControl(container);
        }
        /**
         * Ensures that both text fields are set.
         */
        private void dialogChanged() {
                String fileName = getFileName();
                String dirStr = getPathStr();
                if (dirStr.length() == 0) {
                        updateStatus("Directory must be specified");
                        return;
                }
                if (fileName == null || fileName.length() == 0) {
                        updateStatus("File name must be specified");
                        return;
                }
                if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
                        updateStatus("File name must be valid");
                        return;
                }
                updateStatus(null);
        }
        private void updateStatus(String message) {
                setErrorMessage(message);
                setPageComplete(message == null);
        }
        public String getFileName() {
                if (fileText != null) {
                        return fileText.getText();
                } else {
                        return null;
                }
        }
        public String getPathStr() {
                return dirText.getText();
        }
        public IResource getSelectedFolder() {
                return selectedFolder;
        }
        public void setSelectedFolder(IContainer path){
            selectedFolder=path;
        }
}