/* *****************************************************************************
 * Copyright (c) 2007-2012 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *    
 ******************************************************************************/
package net.bioclipse.cdk.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.jface.wizard.Wizard;

/**
 * This class lunches a wizard that let the user import properties from a 
 * txt- or csv-file to a SDF-file. The result is saved in a new sd-file.
 * 
 * @author Klas Jšnsson (klas.joensson@gmail.com)
 *
 */
public class SDFPropertiesImportWizard extends Wizard implements IImportWizard {
    
    private Logger logger = Logger.getLogger( this.getClass() );
	private SDFPropertiesImportWizardPage mainPage;
	private IStructuredSelection selection;
	
	public SDFPropertiesImportWizard() {
	    super();
	    setNeedsProgressMonitor(true);
	    setWindowTitle("SDF Properties Import Wizard");
	    mainPage = new SDFPropertiesImportWizardPage("Import File", null);
	}
	
	public SDFPropertiesImportWizard(IStructuredSelection ssel) {
	    super();
	    setNeedsProgressMonitor(true);
	    selection = ssel;
	    setWindowTitle("SDF Properties Import Wizard");
	    mainPage = new SDFPropertiesImportWizardPage("Import File",
	                                                 selection);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("SDF Properties Import Wizard");
		setNeedsProgressMonitor(true);
		this.selection = selection;
		mainPage.init( selection );
	}

	@Override
	public void addPages() {
        super.addPages();
        addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.canFlipToNextPage();
	}

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
	    mainPage.updateNameArray();
	    try {
            getContainer().run( true, true, new IRunnableWithProgress() {
                
                @Override
                public void run( IProgressMonitor monitor )
                        throws InvocationTargetException, InterruptedException {
                    
                    mainPage.meargeFiles(monitor);
                    
                }
            } );
        } catch ( InvocationTargetException e ) {
            logger.error( e );
            return false;
        } catch ( InterruptedException e ) {
            logger.error( e );
            return false;
        }
		
	    return true;
	}

}
