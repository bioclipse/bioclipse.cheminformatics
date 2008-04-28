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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.CDKMolecule;
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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.openscience.cdk.CDKConstants;

public class SDFEditor extends FormEditor implements IResourceChangeListener, IAdaptable{

	private static final Logger logger = Logger.getLogger(SDFEditor.class);

	private TextEditor textEditor;
	private StructureTablePage tablePage;

	//Model for the editor: Based on CDK
	StructureTableEntry[] entries;
	ArrayList<String> propHeaders;


	public StructureTableEntry[] getEntries() {
		return entries;
	}


	public void setEntries(StructureTableEntry[] entries) {
		this.entries = entries;
	}


	@Override
	public void init(IEditorSite site, IEditorInput input)
	throws PartInitException {
		super.init(site, input);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);

		propHeaders=new ArrayList<String>();

		//Parse input with CDK
		parseInput();

		//Tables page
		tablePage=new StructureTablePage(this, propHeaders.toArray(new String[0]));

		//Texteditor, should be XMLEditor: TODO
		textEditor = new TextEditor();

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

		try {

			new ProgressMonitorDialog(getSite().getShell()).run(false, true, new IRunnableWithProgress(){

				public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {


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

					InputStream instream;
					try {
						instream = file.getContents();
						CDKMoleculeList molList=Activator.getDefault().getCDKManager().loadMolecules(instream);
					logger.debug("In editor: " + molList.size() + " molecules.");

					monitor.beginTask("Reading SDFile...", molList.size()+1);

					ArrayList<StructureTableEntry> newlist=new ArrayList<StructureTableEntry>();

					for (CDKMolecule mol : molList){

						Map<Object, Object> props=mol.getAtomContainer().getProperties();

						for (Object obj : props.keySet()){
//							System.out.println("Key: '" + obj.toString() + "'; val: '" + props.get(obj) + "'" );
							if (obj instanceof String) {
								String key = (String) obj;
								if (!(propHeaders.contains(key))){
									propHeaders.add(key);
									logger.debug("Header added: " + key);
								}
							}
						}

						//Read vals for this molecule
						ArrayList<Object> vals=new ArrayList<Object>();
						for (String key : propHeaders){
							Object obj=mol.getAtomContainer().getProperty(key);
							vals.add(obj);
						}

						StructureTableEntry entry=new StructureTableEntry(mol.getAtomContainer(), vals.toArray());
						newlist.add(entry);
						monitor.worked(1);
					}
					setEntries(newlist.toArray(new StructureTableEntry[0]));

					monitor.done();

					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} catch (IOException e) {
						throw new InvocationTargetException(e);
					} catch (BioclipseException e) {
						throw new InvocationTargetException(e);
					}

				

			}});

		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	
}
