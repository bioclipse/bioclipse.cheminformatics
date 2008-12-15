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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * A wizard to create a new sd file from existing structure files.
 *
 */
public class NewSdfWizard extends Wizard implements INewWizard {

	private SelectFilesWizardPage specPage;
	private NewSDFileWizardPage newsdPage;
	private static final Logger logger = Logger.getLogger(NewSdfWizard.class);

	public NewSdfWizard() {
		super();
		setWindowTitle("Create a New SD File");
	}

	/**
	 * Adding the pages to the wizard.
	 */

	public void addPages() {
		newsdPage = new NewSDFileWizardPage();
		specPage = new SelectFilesWizardPage();
		addPage(newsdPage);
		addPage(specPage);
	}
	
	@Override
	public boolean performFinish() {
		IFile newFile= ((IContainer)newsdPage.getSelectedFolder()).getFile(new Path(newsdPage.getFileName() + (newsdPage.getFileName().indexOf(".")==-1 ? ".sdf" : "")));
		Iterator it=specPage.getSelectedRes().iterator();
		List<IMolecule> entries=new ArrayList<IMolecule>();
		List<IFile> failures=new ArrayList<IFile>();
		while(it.hasNext()){
			Object selection=it.next();
			if(selection instanceof IFile){
				try {
					entries.add(Activator.getDefault().getCDKManager().loadMolecules((IFile)selection).get(0));
				} catch (Exception e) {
					failures.add((IFile)selection);
				}
			}
			if(selection instanceof IContainer && specPage.doRecursive()){
				try {
					doRecursion((IContainer)selection,entries,failures);
				} catch (CoreException e) {
					LogUtils.handleException(e, logger);
				}
			}
		}
		if(failures.size()>0){
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<failures.size();i++){
				sb.append(failures.get(i).getName()+"; ");
			}
			MessageDialog.openError(this.getShell(), "Problems parsings files", "Some of the files you selected could not be read ("+sb.toString().substring(0, sb.toString().length()-2)+"). We will still use the rest!");
		}
		try {
			Activator.getDefault().getCDKManager().createSDFile(newFile, (IMolecule[])entries.toArray(new IMolecule[entries.size()]));
		} catch (Exception e) {
			LogUtils.handleException(e, logger);
		}
		return true;
	}

	private void doRecursion(IContainer selection, List<IMolecule> entries, List<IFile> failures) throws CoreException {
		for(int i=0;i<selection.members().length;i++){
			if(selection.members()[i] instanceof IContainer)
				doRecursion((IContainer)selection.members()[i], entries,failures);
			if(selection.members()[i] instanceof IFile){
				try {
					entries.add(Activator.getDefault().getCDKManager().loadMolecules((IFile)selection.members()[i]).get(0));
				} catch (Exception e) {
					failures.add((IFile)selection.members()[i]);
				}
			}
		}
		
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

}
