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
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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

		final IFile file = (IFile) obj;
		
		
		Job job=new Job("Converting SMILES to SDF"){

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				List<ICDKMolecule> mols;
				monitor.beginTask("Converting SMILES", 10);
				ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
				try {
					mols = cdk.loadSMILESFile( 
					           file, 
					           new SubProgressMonitor(monitor, 9) );
					if ( monitor.isCanceled()) {
					    return Status.CANCEL_STATUS;
					}
				} catch (Exception e) {
					LogUtils.handleException(e, logger, 
							net.bioclipse.cdk.ui.Activator.PLUGIN_ID);
					monitor.done();
					return new Status(IStatus.ERROR, 
							net.bioclipse.cdk.ui.Activator.PLUGIN_ID, 
							"Failed to convert file. " +
							" Cause: " + e.getMessage());
				} 
				
				if (mols==null || mols.size()<=0)
					return new Status(IStatus.ERROR, 
							net.bioclipse.cdk.ui.Activator.PLUGIN_ID, 
							"Error, no molecules to save. ");
				
				//Create output filename
				String newPath = file.getFullPath().toOSString()
									.replace(".smi", ".sdf");
				
				
				
//				debugAromaticity(mols.get(0));

				try {
					monitor.subTask("Saving SDFile");
					monitor.worked(1);
					cdk.saveSDFile(newPath, mols);
				} catch (Exception e) {
					LogUtils.handleException(e, logger, 
							net.bioclipse.cdk.ui.Activator.PLUGIN_ID);
					return new Status(IStatus.ERROR, 
							net.bioclipse.cdk.ui.Activator.PLUGIN_ID, 
							"Failed to write resulting " +
							"SD file. Cause: " + e.getMessage());
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
