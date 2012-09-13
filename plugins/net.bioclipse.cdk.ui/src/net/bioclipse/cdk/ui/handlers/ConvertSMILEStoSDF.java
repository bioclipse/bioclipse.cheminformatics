/* *****************************************************************************
 * Copyright (c) 2010 Ola Spjuth
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.ui.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.DeduceBondSystemTool;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.core.util.TimeCalculator;
import net.bioclipse.ui.business.IUIManager;

/**
 * A handler that can convert SMILES files into SDFiles.
 * A SMIELS file is expected to have a header line with property names, 
 * and data lines should start with a SMILES string, and have 
 * properties separated by either ',','\t', or ' '. Properties are also 
 * stored in the SDF, with header names as identifiers.
 * 
 * @author ola
 */
public class ConvertSMILEStoSDF extends AbstractHandler{

	private static final Logger logger =
		Logger.getLogger(ConvertSMILEStoSDF.class);

	//Possible separators
	private static final String[] POSSIBLE_SEPARATORS=new String[]{",","\t"," "};
	
    private final IUIManager ui = net.bioclipse.ui.business.Activator
                                     .getDefault().getUIManager();

	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection sel=HandlerUtil.getCurrentSelection(event);
		if (!(sel instanceof IStructuredSelection))
			throw new ExecutionException("Selection is not a SMILES file");

		IStructuredSelection ssel=(IStructuredSelection)sel;
		
		//We onlu operate on a single file currently
		Object obj = ssel.getFirstElement();
		
		if (!(obj instanceof IFile)) 
			throw new ExecutionException("Selection is not a SMILES file");

		final IFile input = (IFile) obj;

		IPath outPath = input.getFullPath().removeFileExtension().addFileExtension("sdf");

		final IFile output = ResourcesPlugin.getWorkspace().getRoot().getFile(outPath);
		if ( ui.fileExists( output ) ) {
            if ( !MessageDialog.openConfirm( 
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                          .getShell(),
                "SD-file already exists, confirm overwrite",
                "A file with the same name but the sdf file ending " +
                "already exists. Okey to overwrite it?" ) ) {
               return null;
            }
		}
		
		Job job=new Job("Converting "+input.getFullPath().toPortableString()+" to SDF"){

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Converting SMILES", 100);
				ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
				
				int count = 0;
				Scanner s;
                try {
                    s = new Scanner( input.getContents() );
                } catch ( CoreException e ) {
                    LogUtils.handleException( e, logger, 
                            "net.bioclipse.cdk.ui" );
                    return new Status(IStatus.ERROR, 
                                      net.bioclipse.cdk.ui.Activator.PLUGIN_ID, 
                                      "Error, failed to read file.");
                }
				while ( s.hasNextLine() ) {
				    s.nextLine();
				    count++;
				}
				
				Iterator<ICDKMolecule> iterator = null;
                
				try {
                    iterator = cdk.createMoleculeIterator(input);
                } 
                catch ( Exception e ) {
                    LogUtils.handleException( e, logger, 
                                              "net.bioclipse.cdk.ui" );
                    return new Status(IStatus.ERROR, 
                                      net.bioclipse.cdk.ui.Activator.PLUGIN_ID, 
                                      "Error, failed to read file.");
                }

                if ( ui.fileExists( output ) ) {
                    ui.remove(output);
                }
				monitor.beginTask( "Converting file", count );
				long timestamp = System.currentTimeMillis();
				long before = timestamp;
				int current = 0;
				int last = 0;
		        while ( iterator.hasNext() ) {
		            try {
                        cdk.appendToSDF(newPath, iterator.next());
                    } catch ( BioclipseException e ) {
                        LogUtils.handleException( e, logger, 
                                                  "net.bioclipse.cdk.ui" );
                        return new Status(
                            IStatus.ERROR, 
                            net.bioclipse.cdk.ui.Activator.PLUGIN_ID, 
                            "Error, failed to write to file.");
                    }
		            current++;
		            if (System.currentTimeMillis() - timestamp > 1000) {
		                monitor.subTask( "Done: " + current + "/" + count 
		                    + " (" + TimeCalculator.generateTimeRemainEst( 
		                                 before, current, count ) + " )" );
		                monitor.worked( current - last );
		                last = current;
		                synchronized ( monitor ) {
        		                if ( monitor.isCanceled() ) {
        		                    return Status.CANCEL_STATUS;
        		                }
                        }
		                timestamp = System.currentTimeMillis();
		            }
		        }
				
				monitor.done();
				logger.debug("Wrote file" + newPath);
				return Status.OK_STATUS;

			}};
			job.setUser(true);
			job.schedule();
		
		return null;
	}		
}
