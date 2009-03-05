/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.cdk.ui.wizards;

import net.bioclipse.chemoinformatics.wizards.WizardHelper;
import net.bioclipse.ui.contentlabelproviders.FolderContentProvider;
import net.bioclipse.ui.contentlabelproviders.FolderLabelProvider;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class SelectFileWithLimitsWizardPage extends WizardPage {

	protected IContainer selectedFolder;
	protected Text fromText;
	protected Text toText;
	private Text fileText;
  protected Text dirText;

	protected SelectFileWithLimitsWizardPage() {
		super("Define file");
    setTitle("Extract from SDF wizard");
    setDescription("You need to choose the start and end entry number (no end number means one is extracted) and the files the entries should be saved to.");
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 4;
		layout.verticalSpacing = 9;
		
    Label fromLabel = new Label(container, SWT.NULL);
    fromLabel.setText("From:");   
    fromText = new Text(container, SWT.BORDER | SWT.SINGLE);
    fromText.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          dialogChanged();
        }
      });
    Label toLabel = new Label(container, SWT.NULL);
    toLabel.setText("To:");		
    toText = new Text(container, SWT.BORDER | SWT.SINGLE);
    toText.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          dialogChanged();
        }
      });
    
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 4;
		Label dirLabel = new Label(container, SWT.NULL);
		dirLabel.setText("Enter or select the parent Directory for the file to extract to:");
		dirLabel.setLayoutData(gd);
	
		dirText = new Text(container, SWT.BORDER | SWT.SINGLE);
    ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
    dirText.setText( ((IResource)((IStructuredSelection)sel).getFirstElement()).getParent().getFullPath().toOSString());
		dirText.setLayoutData(gd);
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
		
		//Layout the tree viewer below the text field
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		layoutData.horizontalSpan = 4;
		treeViewer.getControl().setLayoutData(layoutData);
		
		treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot().findMember("."));
		treeViewer.expandToLevel(2);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				if (sel instanceof IStructuredSelection) {
					Object element = ((IStructuredSelection) sel).getFirstElement();
					if (element instanceof IContainer) {
						selectedFolder = (IContainer) element;
						String path = ((IContainer) element).getFullPath().toString();
						dirText.setText(path);
					}
				}
			}
			
		});
		treeViewer.setSelection(new StructuredSelection(ResourcesPlugin.getWorkspace().getRoot().findMember(".")));
		

		Label fileLabel = new Label(container, SWT.NULL);
		fileLabel.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
    fileText.setText( WizardHelper.findUnusedFileName((IStructuredSelection)sel, "unnamed", "") );
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		setControl(container);
		dialogChanged();
	}
	

	public boolean isNotParsableToInt(String i)
	{
  	try
  	{
    	Integer.parseInt(i);
    	return false;
  	}
  	catch(NumberFormatException nfe)
  	{
  	    return true;
  	}
	}
	
	private void dialogChanged() {
		String fileName = getFileName();
		String dirStr = getPathStr();
		
		if(fromText.getText().equals( "" ) || isNotParsableToInt(fromText.getText())){
		    updateStatus( "From must be set and a figure" );
		    return;
		}
		
    if(!toText.getText().equals( "" ) && isNotParsableToInt(toText.getText())){
        updateStatus( "To must be a figure, if set" );
        return;
    }
		
		
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
		}
		else {
			return null;
		}
	}

	public String getPathStr() {
		return dirText.getText();
	}
	
	public String getFrom(){
	    return fromText.getText();
	}

  public String getTo(){
      return toText.getText();
  }
}
