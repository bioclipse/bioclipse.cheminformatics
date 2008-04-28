/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.editors.sdf;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.CDKMoleculeList;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;

public class SDFEditor extends FormEditor implements IResourceChangeListener, IAdaptable{

    private static final Logger logger = Logger.getLogger(SDFEditor.class);
    
    private TextEditor textEditor;
	private StructureTablePage tablePage;
	
	//Model for the editor: Based on CDK
	CDKMoleculeList molList;
	
	public CDKMoleculeList getMolList() {
		return molList;
	}

	public void setMolList(CDKMoleculeList molList) {
		this.molList = molList;
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);

		//Tables page
		tablePage=new StructureTablePage(this);

		//Texteditor, should be XMLEditor: TODO
		textEditor = new TextEditor();

		//Parse input with CDK
		parseInput();
	}
	

	@Override
	protected void addPages() {
		try {
			addPage(tablePage);

			int index = addPage(textEditor, getEditorInput());
			setPageText(index, textEditor.getTitle());

		} catch (PartInitException e) {
			LogUtils.debugTrace(logger, e);
		}
		

	}



	@Override
	public void doSave(IProgressMonitor monitor) {
		//TODO
	}

	@Override
	public void doSaveAs() {
		//TODO
	}

	@Override
	public boolean isSaveAsAllowed() {
		//TODO
		return false;
	}
	
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	public void resourceChanged(IResourceChangeEvent event) {
		//TODO
	}

	private void parseInput(){
		
		IEditorInput input=getEditorInput();
		if (!(input instanceof IFileEditorInput)) {
			logger.debug("Not FIleEditorInput.");
			//TODO: Close editor?
			return;
		}
		IFileEditorInput finput = (IFileEditorInput) input;
		
		IFile file=finput.getFile();
		if (!(file.exists())){
			logger.debug("File does not exist.");
			//TODO: Close editor?
			return;
		}

		try {
			InputStream instream = file.getContents();
			
			molList=Activator.getDefault().getCDKManager().loadMolecules(instream);
			logger.debug("In editor: " + molList.size() + " molecules.");
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BioclipseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tablePage.modelUpdated();
			
		return ;
	}
	
}
