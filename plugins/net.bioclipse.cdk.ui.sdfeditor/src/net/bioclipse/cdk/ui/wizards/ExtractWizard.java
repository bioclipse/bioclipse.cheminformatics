/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.cdk.ui.wizards;


import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.openscience.cdk.exception.CDKException;

public class ExtractWizard extends Wizard implements INewWizard {

	private SelectFileWithLimitsWizardPage selectFilePage;
	private static final Logger logger = Logger.getLogger(ExtractWizard.class);
	
	public ExtractWizard() {
		super();
		setWindowTitle("Extract entries from SDF");
		setNeedsProgressMonitor(true);
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	public void addPages()  
	{  
		selectFilePage = new SelectFileWithLimitsWizardPage();
		this.addPage(selectFilePage);

	}
	
	@Override
	public boolean performFinish() {
		try {
		    ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
	      if (sel instanceof IStructuredSelection) {
          IStructuredSelection ssel = (IStructuredSelection) sel;
          IFile toExtract=(IFile) ssel.getFirstElement();
          List<IMolecule> result=Activator.getDefault().getCDKManager().extractFromSDFile( toExtract, Integer.parseInt( selectFilePage.getFrom() ), selectFilePage.getTo().equals( "" ) ? Integer.parseInt( selectFilePage.getFrom() ) : Integer.parseInt( selectFilePage.getTo() ) );
          String filename=selectFilePage.getPathStr()+Path.SEPARATOR+selectFilePage.getFileName()+"."+ICDKManager.sdf;
          if(result.size()==1){
              Activator.getDefault().getCDKManager().saveMDLMolfile( (ICDKMolecule)result.get( 0 ), filename );
          }else{
              Activator.getDefault().getCDKManager().saveMolecules( result, filename, ICDKManager.sdf );
          }
	      }
	      return true;
		} catch (InvocationTargetException e) {
			LogUtils.handleException(e,logger);
		} catch (BioclipseException e) {
			LogUtils.handleException(e,logger);
		} catch (CoreException e) {
			LogUtils.handleException(e,logger);
		} catch (CDKException e) {
      LogUtils.handleException(e,logger);
    }
		return false;
	}
}
