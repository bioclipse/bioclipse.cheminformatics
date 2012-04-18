/*******************************************************************************
 * Copyright (c) 2010  Egon Willighagen <egon.willighagen@gmail.com>
 * Copyright (c) 2012  Ola Spjuth <ola.spjuth@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.chemspider.business;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.chemspider.ChemspiderWSHelper;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.managers.business.IBioclipseManager;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.chemspider.www.MassSpecAPIStub.ExtendedCompoundInfo;

/**
 * 
 * @author Ola Spjuth, Egon Willighagen
 *
 */
public class ChemspiderManager implements IBioclipseManager {

	private static final ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
	private static final Logger logger = Logger.getLogger(ChemspiderManager.class);

	/**
	 * Gives a short one word name of the manager used as variable name when
	 * scripting.
	 */
	public String getManagerName() {
		return "chemspider";
	}

	public IFile loadCompound(int csid, IFile target, IProgressMonitor monitor)
	throws IOException, BioclipseException, CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(
				"Downloading CSID " + csid + " from ChemSpider.", 2
		);
		URL url = new URL("http://www.chemspider.com/mol/" + csid);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						url.openConnection().getInputStream()
				)
		);
		String line = reader.readLine();
		StringBuffer molString = new StringBuffer(); 
		while (line != null) {
			molString.append(line).append('\n');
			line = reader.readLine();
		}

		monitor.worked(1);
		if (target.exists()) {
			target.setContents(
					new ByteArrayInputStream(molString.toString().getBytes()),
					true, false, null
			);
		} else {
			target.create(
					new ByteArrayInputStream(molString.toString().getBytes()),
					false, null
			);
		}
		monitor.worked(1);

		monitor.done();
		return target;
	}

	public List<Integer> resolve(String inchiKey, IProgressMonitor monitor)
	throws IOException, BioclipseException, CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		Set<Integer> results = new HashSet<Integer>();
		monitor.beginTask(
				"Resolving the InChIKey '" + inchiKey + "' on ChemSpider...", 1
		);

		URL url = new URL("http://www.chemspider.com/InChIKey/" + inchiKey);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						url.openConnection().getInputStream()
				)
		);
		String line = reader.readLine();
		Pattern pattern = Pattern.compile("Chemical-Structure.(\\d*).html");
		String csid = "";
		while (line != null) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				csid = matcher.group(1);
				results.add(Integer.valueOf(csid));
			}
			line = reader.readLine();
		}

		List<Integer> uniqueResults = new ArrayList<Integer>();
		uniqueResults.addAll(results);
		return uniqueResults;
	}

	public String downloadAsString(Integer csid, IProgressMonitor monitor)
	throws IOException, BioclipseException, CoreException {
		if (monitor == null) monitor = new NullProgressMonitor();

		monitor.subTask("Downloading CSID " + csid);
		StringBuffer fileContent = new StringBuffer(); 
		try {                
			monitor.beginTask(
					"Downloading CSID " + csid + " from ChemSpider.", 2
			);
			URL url = new URL("http://www.chemspider.com/mol/" + csid);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							url.openConnection().getInputStream()
					)
			);
			String line = reader.readLine();
			while (line != null) {
				fileContent.append(line).append('\n');
				line = reader.readLine();
			}
			reader.close();
			monitor.worked(1);
		} catch (PatternSyntaxException exception) {
			exception.printStackTrace();
			throw new BioclipseException("Invalid Pattern.", exception);
		} catch (MalformedURLException exception) {
			exception.printStackTrace();
			throw new BioclipseException("Invalid URL.", exception);
		}
		return fileContent.toString();
	}

	public IMolecule download(Integer csid, IProgressMonitor monitor)
	throws IOException, BioclipseException, CoreException {
		monitor.beginTask("Downloading Compound from ChemSpider...", 2);
		String molstring = downloadAsString(csid, monitor);
		if (monitor.isCanceled()) return null;

		ICDKMolecule molecule = cdk.fromString(molstring);
		monitor.worked(1);
		return molecule;
	}


	public List<ICDKMolecule> similaritySearch(IMolecule mol, Float tanimoto, IProgressMonitor monitor)
	throws BioclipseException{

		String smiles = cdk.calculateSMILES(mol);

		//SimilaritySearch for similar to AlmostParacetamol
		//=======================
		logger.debug("Chemspider SimilaritySearch for " + smiles + " with tanimoto " + tanimoto + "...");
		try {

			//Start search
			String rid = ChemspiderWSHelper.similaritySearch(smiles, tanimoto, 
					net.bioclipse.chemspider.Activator.getDefault().getPreferenceStore().getString(
							net.bioclipse.chemspider.Activator.PREF_SECURITY_TOKEN));

			//Poll and get results
			int[] results = ChemspiderWSHelper.pollAndReturnResults(rid, 					
					net.bioclipse.chemspider.Activator.getDefault().getPreferenceStore().getString(
					net.bioclipse.chemspider.Activator.PREF_SECURITY_TOKEN), monitor);


			return getMoleculesWithExtendedInfo(results, monitor);

		} catch (RemoteException e) {
			e.printStackTrace();
			throw new BioclipseException(e.getMessage());
		}

	}

	public List<ICDKMolecule> exactSearch(IMolecule mol, IProgressMonitor monitor)
	throws BioclipseException{

		String smiles = cdk.calculateSMILES(mol);

		//SimilaritySearch for similar to AlmostParacetamol
		//=======================
		logger.debug("Chemspider exact search for " + smiles );
		try {

			//Start search
			String rid = ChemspiderWSHelper.exactSearch(smiles,  					
					net.bioclipse.chemspider.Activator.getDefault().getPreferenceStore().getString(
							net.bioclipse.chemspider.Activator.PREF_SECURITY_TOKEN));

			//Poll and get results
			int[] results = ChemspiderWSHelper.pollAndReturnResults(rid,  					
					net.bioclipse.chemspider.Activator.getDefault().getPreferenceStore().getString(
							net.bioclipse.chemspider.Activator.PREF_SECURITY_TOKEN), monitor);

			return getMoleculesWithExtendedInfo(results, monitor);

		} catch (RemoteException e) {
			e.printStackTrace();
			throw new BioclipseException(e.getMessage());
		}

	}
	
	public List<ICDKMolecule> substructureSearch(IMolecule mol, IProgressMonitor monitor)
	throws BioclipseException{

		String smiles = cdk.calculateSMILES(mol);

		//SimilaritySearch for similar to AlmostParacetamol
		//=======================
		logger.debug("Chemspider substructure search for " + smiles );
		try {

			//Start search
			String rid = ChemspiderWSHelper.substructureSearch(smiles,  					
					net.bioclipse.chemspider.Activator.getDefault().getPreferenceStore().getString(
							net.bioclipse.chemspider.Activator.PREF_SECURITY_TOKEN));

			//Poll and get results
			int[] results = ChemspiderWSHelper.pollAndReturnResults(rid,  					
					net.bioclipse.chemspider.Activator.getDefault().getPreferenceStore().getString(
							net.bioclipse.chemspider.Activator.PREF_SECURITY_TOKEN), monitor);

			return getMoleculesWithExtendedInfo(results, monitor);

		} catch (RemoteException e) {
			e.printStackTrace();
			throw new BioclipseException(e.getMessage());
		}

	}

	public List<ICDKMolecule> simpleSearch(IMolecule mol, IProgressMonitor monitor)
	throws BioclipseException{

		String smiles = cdk.calculateSMILES(mol);

		//SimilaritySearch for similar to AlmostParacetamol
		//=======================
		logger.debug("Chemspider simple search for " + smiles );
		try {

			//Start search
			int[] results = ChemspiderWSHelper.simpleSearch(smiles,  					
					net.bioclipse.chemspider.Activator.getDefault().getPreferenceStore().getString(
							net.bioclipse.chemspider.Activator.PREF_SECURITY_TOKEN));

			return getMoleculesWithExtendedInfo(results, monitor);

		} catch (RemoteException e) {
			e.printStackTrace();
			throw new BioclipseException(e.getMessage());
		}

	}
	
	private List<ICDKMolecule> getMoleculesWithExtendedInfo(int[] results, IProgressMonitor monitor) {

		List<ICDKMolecule> mols = new ArrayList<ICDKMolecule>();

		if (results!=null)
			logger.debug("Got "+results.length +" compound(s) with CSID:" + Arrays.toString(results));
		else{
			logger.debug("Results empty");
			return mols;
		}

		//Get extended info on each compound
		ExtendedCompoundInfo[] info=null;
		try {
			info = ChemspiderWSHelper.getExtendedInfo(results,  					
					net.bioclipse.chemspider.Activator.getDefault().getPreferenceStore().getString(
							net.bioclipse.chemspider.Activator.PREF_SECURITY_TOKEN));
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}

		for (int i = 0; i< results.length; i++){
			int crid = results[i];
			IMolecule csmol;

			try {

				//Download mol from Chemspider
				csmol = download(crid, monitor);

				//Set properties on mol from Chemspider info
				ICDKMolecule cdkmol = cdk.asCDKMolecule(csmol);

				if (info!=null && info[i]!=null){
					cdkmol.setProperty("chemspider.commonname", info[i].getCommonName());
					cdkmol.setProperty("chemspider.inchi", info[i].getInChI());
					cdkmol.setProperty("chemspider.inchikey", info[i].getInChIKey());
					cdkmol.setProperty("chemspider.alogp", info[i].getALogP());
					cdkmol.setProperty("chemspider.averagemass", info[i].getAverageMass());
					cdkmol.setProperty("chemspider.id", info[i].getCSID());
					cdkmol.setProperty("chemspider.mf", info[i].getMF());
					cdkmol.setProperty("chemspider.mw", info[i].getMolecularWeight());
					cdkmol.setProperty("chemspider.monoisotopicmass", info[i].getMonoisotopicMass());
					cdkmol.setProperty("chemspider.nominalmass", info[i].getNominalMass());
					cdkmol.setProperty("chemspider.xlogp", info[i].getXLogP());
				}

				mols.add(cdkmol);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			} 
		}

		return mols;
	}


}
