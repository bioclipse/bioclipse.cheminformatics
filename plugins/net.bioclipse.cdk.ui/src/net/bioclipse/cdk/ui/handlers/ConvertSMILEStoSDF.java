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
import org.openscience.cdk.interfaces.IAtom;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
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
				try {
					mols = ConvertSMILEStoSDF.readFileIntoMoleculeList(
							file, new SubProgressMonitor(monitor, 9));
				} catch (InterruptedException e) {
					monitor.done();
					logger.debug("Canceled.");
					return Status.CANCEL_STATUS;
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
				
				ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
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

	
	
	/**
	 * Read a SMILES file into a list of molecules.
	 * @param file
	 * @param monitor
	 * @return
	 * @throws BioclipseException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public static List<ICDKMolecule> readFileIntoMoleculeList(
			IFile file, IProgressMonitor monitor)
			throws BioclipseException, InvocationTargetException, 
			InterruptedException {
		
		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
		List<ICDKMolecule> molecules=new ArrayList<ICDKMolecule>();
		
		try {

			int noLines=countLines(file.getContents());
//			int noLines=4000;

			logger.debug("Number of lines in file: " + noLines);
			
			monitor.beginTask("Converting SMILES file to SDF", noLines);
			
			BufferedReader br = 
				new BufferedReader(new InputStreamReader(file.getContents()));

			String line = br.readLine();

			if (line==null)
				throw new IOException("First line is null!");
			
			logger.debug("Header line is: " + line);

			//Determine separator from first line
			String separator=determineSeparator(line);

			//First line is header
			String[] headers = line.split(separator);
			//Strip headers of " and spaces
			for (int i=0; i< headers.length; i++){
				headers[i]=headers[i].trim();
				if (headers[i].startsWith("\""))
					headers[i]=headers[i].substring(1);
				if (headers[i].endsWith("\""))
					headers[i]=headers[i].substring(0,headers[i].length()-1);
			}

			//Read subsequent lines until end
			int lineno=2;
			line=br.readLine();
			while(line!=null){
				
				String[] parts = line.split(separator);
				
				//Assert header is same size as data
				if (parts.length!=headers.length)
					throw new BioclipseException("Header and data have " +
							"different number of columns. " +
							"Header size=" + headers.length + 
							"Line " + lineno + " size=" + parts.length );

				//Part 1 is expected to be SMILES
				String smiles=parts[0];

				//Create a new CDKMolecule from smiles
				ICDKMolecule mol = cdk.fromSMILES(smiles);

				//Store rest of parts as properties on mol
				for (int i=1; i<headers.length;i++){
					mol.getAtomContainer().setProperty(headers[i], parts[i]);
				}
				
				//Filter molecules with failing atom types
				boolean filterout=false;
				for (IAtom atom : mol.getAtomContainer().atoms()){
					if (atom.getAtomTypeName().equals("X"))
						filterout=true;
				}

				if (filterout)
					logger.debug("Skipped molecule " + lineno + " due to " +
							"failed atom typing.");
				else
					molecules.add(mol);

				//Read next line
				line=br.readLine();
				lineno++;
				
				monitor.worked(1);
				if (lineno%100==0){
					if (monitor.isCanceled())
			            throw new InterruptedException("Operation cancelled");
					monitor.subTask("Processed: " + lineno + "/" + noLines);
				}
			}

			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (BioclipseException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}finally{
			monitor.done();
		}
		

		logger.debug("Read " + molecules.size() +" molecules.");
		return molecules;
		
	}

	/**
	 * A simple implementation testing separator by splitting a line using a 
	 * list of possible separators and returning the first one giving 
	 * more than 1 parts.
	 * 
	 * @param line Line to split
	 * @return a String separator, or null if none found
	 */
	private static String determineSeparator(String line) {

		for (int i = 0; i< POSSIBLE_SEPARATORS.length; i++){
			String[] splits = line.split(POSSIBLE_SEPARATORS[i]);
			if (splits.length>1)
				return POSSIBLE_SEPARATORS[i];
		}

		return null;
	}

	/**
	 * A fast implementation to count lines in a file.
	 * Reference: http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
	 * 
	 * @param instream
	 * @return
	 * @throws IOException
	 */
	public static int countLines(InputStream instream) throws IOException {
	    InputStream is = new BufferedInputStream(instream);
	    byte[] c = new byte[1024];
	    int count = 0;
	    int readChars = 0;
	    while ((readChars = is.read(c)) != -1) {
	        for (int i = 0; i < readChars; ++i) {
	            if (c[i] == '\n')
	                ++count;
	        }
	    }
	    is.close();
	    return count;
	}

}
