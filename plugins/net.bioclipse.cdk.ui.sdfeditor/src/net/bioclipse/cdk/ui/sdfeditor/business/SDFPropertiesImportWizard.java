/* *****************************************************************************
 * Copyright (c) 2006, 2008-2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.jface.wizard.Wizard;

/**
 * This wizard let the user import properties from a txt file to a SDF-file.
 * 
 * @author klas jonsson
 *
 */
public class SDFPropertiesImportWizard extends Wizard implements IImportWizard {

	private SDFPropertiesImportWizardPage mainPage;
//	private MuliplePropertiesWizardPage propertiesPage;
	private IStructuredSelection selection;
	
	public SDFPropertiesImportWizard() {
	    super();
	    mainPage = new SDFPropertiesImportWizardPage("Import File", selection);
//	    mainPage.init( selection );
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("SDF Properties Import Wizard");
//		setNeedsProgressMonitor(true);
		this.selection = selection;
		mainPage.init( selection );
	}

	@Override
	public void addPages() {
        super.addPages();
//        mainPage = new SDFPropertiesImportWizardPage("Import File");
        addPage(mainPage);
//        addPage(propertiesPage);
//        mainPage.init( selection );
	}

	@Override
	public boolean canFinish() {
		return mainPage.canFlipToNextPage();
	}

//	@Override
//	public void dispose() {
//	    dispose();
//	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		return null;
	}

	@Override
	public int getPageCount() {
		return 1;
	}

	@Override
	public IWizardPage getStartingPage() {
		return mainPage;
	}

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

	@Override
	public boolean needsPreviousAndNextButtons() {
		return false;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean performFinish() {
		// TODO Write it...
		return true;
	}

}
