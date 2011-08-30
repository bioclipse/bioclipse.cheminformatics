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

import java.util.Iterator;
import java.util.Scanner;

import org.apache.commons.lang.StringEscapeUtils;
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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.AtomTypeAwareSaturationChecker;
import org.openscience.cdk.tools.SaturationChecker;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

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
	private static final String[] POSSIBLE_SEPARATORS=new String[]{",","\t"," ",";"};
	
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
				AtomTypeAwareSaturationChecker ataSatChecker = new AtomTypeAwareSaturationChecker();
//				FixBondOrdersTool bondOrderTool = new FixBondOrdersTool();
				long timestamp = System.currentTimeMillis();
				long before = timestamp;
				int current = 0;
				int last = 0;

				int failedCount= 0;
		        while ( iterator.hasNext() ) {
		            try {
		            	ICDKMolecule mol = iterator.next();
		            	boolean filterout = false;
		            	for(IAtom atom:mol.getAtomContainer().atoms()) {
		            		if( atom.getAtomTypeName()==null ||
		            		    atom.getAtomTypeName().equals("X")){
		            			filterout = true;
		            		}
		            	}
		            	if(!filterout) {
//		            	IMolecule newAC =
//		            		bondOrderTool.kekuliseAromaticRings((IMolecule) mol.getAtomContainer());
		            	IAtomContainer newAC = mol.getAtomContainer();
		            	AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms( newAC );
		            	ataSatChecker.decideBondOrder( newAC );
		            	mol = new CDKMolecule(newAC);
                        cdk.appendToSDF(output, mol);
		            	} else{
		            		++failedCount;
		            	}
                    } catch ( BioclipseException e ) {
                    	++failedCount;
                    	logger.error(e.getMessage(),e);
                    } catch (CDKException e) {
		            	++failedCount;
		            	logger.warn("Could not deduce bond orders");
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
				logger.debug("Wrote file" + output);
				if(failedCount!=0){
					setProperty(IProgressConstants.KEEP_PROPERTY, true);
				}

				return new Status(IStatus.OK,net.bioclipse.cdk.ui.Activator.PLUGIN_ID,
						"Failed to convert "+failedCount+" molecules");
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
		DeduceBondSystemTool bondSystemTool= new DeduceBondSystemTool();
		List<String> errors = new ArrayList<String>();

		try {

			int noLines=countLines(file.getContents());

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
				
				if (monitor.isCanceled())
					throw new InterruptedException("Canceled by user");
				
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
				ICDKMolecule mol = null;
				try {
				
					mol = cdk.fromSMILES(smiles.trim());

				} catch (BioclipseException e) {
					logger.error("Skipping SMILES due to invalid: " + smiles);
					errors.add("Line " + lineno + ": SMILES invalid - " + smiles);
					
					//Read next line
					line=br.readLine();
					lineno++;
					
					continue;
				}
				
				try {
					IMolecule newAC = bondSystemTool.fixAromaticBondOrders((IMolecule)mol.getAtomContainer());

					//Is this needed? TODO: VERIFY!
//					CDKHueckelAromaticityDetector.detectAromaticity(newAC);
//					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(newAC);
//
//					CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(newAC.getBuilder());
//					hAdder.addImplicitHydrogens(newAC);

					mol=new CDKMolecule(newAC);
				} catch (CDKException e) {
					logger.error("Could not deduce bond orders for mol: " + mol);
					errors.add("Line " + lineno + ": Could not deduce bond orders for SMILES " + smiles);
				}
				


				//Store rest of parts as properties on mol
				for (int i=1; i<headers.length;i++){
					mol.getAtomContainer().setProperty(headers[i], parts[i]);
				}
				
				//Filter molecules with failing atom types
				boolean filterout=false;
				for (IAtom atom : mol.getAtomContainer().atoms()){
					if (atom.getAtomTypeName()==null || 
							atom.getAtomTypeName().equals("X"))
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
				if (lineno%20==0){
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
		logger.debug("Errors found in " + errors.size() +" molecules.");
		for (String er : errors)
			logger.debug(er);
		
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
