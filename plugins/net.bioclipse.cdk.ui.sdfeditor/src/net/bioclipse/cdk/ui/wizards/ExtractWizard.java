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
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.MDLFormat;
import org.openscience.cdk.io.formats.SDFFormat;

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
        ISelection sel = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                        .getSelectionService().getSelection();
        if ( sel instanceof IStructuredSelection ) {
            IStructuredSelection ssel = (IStructuredSelection) sel;
            final IFile toExtract = (IFile) ssel.getFirstElement();
            final String from = selectFilePage.getFrom();
            final String to = selectFilePage.getTo();
            final String filename = selectFilePage.getPathStr() +
                                    Path.SEPARATOR +
                                    selectFilePage.getFileName() + ".";
            try {
                getContainer().run( true, true, new IRunnableWithProgress() {

                    @Override
                    public void run( IProgressMonitor monitor ) {

                        try {
                            extractMoleuces( toExtract,filename,
                                             from, to, monitor );
                        } catch ( BioclipseException e ) {
                            LogUtils.handleException( e, logger,
                                             "net.bioclipse.cdk.ui.sdfeditor" );
                        } catch ( InvocationTargetException e ) {
                            LogUtils.handleException( e, logger,
                                             "net.bioclipse.cdk.ui.sdfeditor" );
                        } catch ( CoreException e ) {
                            LogUtils.handleException( e, logger,
                                             "net.bioclipse.cdk.ui.sdfeditor" );
                        }
                    }
                } );
            } catch ( InvocationTargetException e ) {
                LogUtils.handleException( e, logger,
                                          "net.bioclipse.cdk.ui.sdfeditor" );
            } catch ( InterruptedException e ) {
                LogUtils.handleException( e, logger,
                                          "net.bioclipse.cdk.ui.sdfeditor" );
            }
        }
        return true;
    }

    void extractMoleuces( IFile toExtract, String filename,
                          String from, String to, IProgressMonitor monitor )
                                              throws BioclipseException,
                                                     InvocationTargetException,
                                                     CoreException {

        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        SubMonitor progress = SubMonitor.convert( monitor );
        int formInt = Integer.parseInt( from );
        int toInt = to.equals( "" ) ? formInt : Integer.parseInt( to );
        progress.beginTask( "Extracting molecules", 100 );

        List<ICDKMolecule> result = cdk.extractFromSDFile( toExtract,
                                                           formInt, toInt,
                                                           progress.newChild(80)
                                                         );
        progress.setWorkRemaining( 20 );
        progress.subTask( "Saving" );
        if ( result.size() == 1 ) {
            filename += MDLFormat.getInstance().getPreferredNameExtension();
            cdk.saveMDLMolfile( (ICDKMolecule) result.get( 0 ), filename );
        } else {
            filename += SDFFormat.getInstance().getPreferredNameExtension();
            cdk.saveMolecules( result, filename,
                               (IChemFormat) SDFFormat.getInstance() );
        }
        progress.done();
    }
}
