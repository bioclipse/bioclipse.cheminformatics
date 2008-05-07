/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/
package net.bioclipse.cdk.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.CDKMoleculeList;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class CDKManager implements ICDKManager{

    private static final Logger logger = Logger.getLogger(CDKManager.class);
    
	ReaderFactory readerFactory;

	public String getNamespace() {
		return "cdk";
	}

	/**
	 * Load a molecule from a file. If many molecules, just return first. To return 
	 * list of molecules, use loadMolecules(...)
	 */
	public CDKMolecule loadMolecule(String path) throws IOException, BioclipseException {
		
		File file=new File(path);
		if (file.canRead()==false){
			throw new IllegalArgumentException("Could not read file: " + file.getPath());
		}
		FileInputStream stream;
		try {
			stream = new FileInputStream(file);
			return loadMolecule(stream);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Could not read file: " + file.getPath());
		}
	}

	/**
	 * Load a molecule from an InputStream. If many molecules, just return first. To return 
	 * list of molecules, use loadMolecules(...)
	 */
	public CDKMolecule loadMolecule(InputStream instream) throws IOException, BioclipseException {

		if (readerFactory==null){
			readerFactory=new ReaderFactory();
			CDKManagerHelper.registerFormats(readerFactory);
		}

//		System.out.println("no formats supported: " + readerFactory.getFormats().size());
//		System.out.println("format guess: " + readerFactory.guessFormat(instream).getFormatName());

		//Create the reader
		ISimpleChemObjectReader reader= readerFactory.createReader(instream);

		if (reader==null){
			throw new BioclipseException("Could not create reader in CDK. ");
		}

		IChemFile chemFile = new org.openscience.cdk.ChemFile();

		// Do some customizations...
		CDKManagerHelper.customizeReading(reader, chemFile);

		//Read file
		try {
			chemFile=(IChemFile)reader.read(chemFile);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			LogUtils.debugTrace(logger, e);
		}

		//Store the chemFormat used for the reader
		IResourceFormat chemFormat=reader.getFormat();
		System.out.println("Rad CDK chemfile with format: " + chemFormat.getFormatName());

		List<IAtomContainer> atomContainersList = ChemFileManipulator.getAllAtomContainers(chemFile);
		int nuMols=atomContainersList.size();
		System.out.println("This file contained: " + nuMols + " molecules");

		//If we have one AtomContainer, return a CDKMolecule with this ac
		//If we have more than one AtomContainer, return a list of the molecules
		//FIXME: requires common interface for CDKImplementations
		
		
		if (atomContainersList.size()==1){
			CDKMolecule retmol=new CDKMolecule((IAtomContainer)atomContainersList.get(0));
			return retmol;
		}
		
		CDKMoleculeList moleculesList=new CDKMoleculeList();
//		CDKMolecule[] moleculesData = new CDKMolecule[atomContainersList.size()];

		for (int i=0; i<atomContainersList.size();i++){
			IAtomContainer ac=null;
			Object obj=atomContainersList.get(i);
			if (obj instanceof org.openscience.cdk.interfaces.IMolecule) {
				ac=(org.openscience.cdk.interfaces.IMolecule)obj;
			}else if (obj instanceof IAtomContainer) {
				ac=(IAtomContainer)obj;
			}

			CDKMolecule mol=new CDKMolecule(ac);
			String moleculeName="Molecule " + i; 
			if (ac instanceof IMolecule) {
				org.openscience.cdk.interfaces.IMolecule imol = (org.openscience.cdk.interfaces.IMolecule) ac;
				String molName=(String) imol.getProperty(CDKConstants.TITLE);
				if (molName!=null && (!(molName.equals("")))){
					moleculeName=molName;
				}
			}
			mol.setName(moleculeName);
			
			moleculesList.add(mol);
		}
		
		//Just return the first molecule. To return all, use loadMolecules(..)
		return moleculesList.get(0);
	}

	
	/**
	 * Load a molecules from a file. 
	 */
	public CDKMoleculeList loadMolecules(String path) throws IOException, BioclipseException {
		
		File file=new File(path);
		if (file.canRead()==false){
			throw new IllegalArgumentException("Could not read file: " + file.getPath());
		}
		FileInputStream stream;
		try {
			stream = new FileInputStream(file);
			return loadMolecules(stream);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Could not read file: " + file.getPath());
		}
	}

	
	
	/**
	 * Load one or more molecules from an InputStream and return a CDKMoleculeList.
	 */
	public CDKMoleculeList loadMolecules(InputStream instream) throws IOException, BioclipseException {

		if (readerFactory==null){
			readerFactory=new ReaderFactory();
			CDKManagerHelper.registerFormats(readerFactory);
		}

		System.out.println("no formats supported: " + readerFactory.getFormats().size());
//		System.out.println("format guess: " + readerFactory.guessFormat(instream).getFormatName());

		//Create the reader
		ISimpleChemObjectReader reader= readerFactory.createReader(instream);

		if (reader==null){
			throw new BioclipseException("Could not create reader in CDK. ");
		}

		IChemFile chemFile = new org.openscience.cdk.ChemFile();

		// Do some customizations...
		CDKManagerHelper.customizeReading(reader, chemFile);

		//Read file
		try {
			chemFile=(IChemFile)reader.read(chemFile);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
		    LogUtils.debugTrace(logger, e);
		}

		//Store the chemFormat used for the reader
		IResourceFormat chemFormat=reader.getFormat();
		System.out.println("Rad CDK chemfile with format: " + chemFormat.getFormatName());

		List<IAtomContainer> atomContainersList = ChemFileManipulator.getAllAtomContainers(chemFile);
		int nuMols=atomContainersList.size();
		System.out.println("This file contained: " + nuMols + " molecules");

		CDKMoleculeList moleculesList=new CDKMoleculeList();
//		CDKMolecule[] moleculesData = new CDKMolecule[atomContainersList.size()];

		for (int i=0; i<atomContainersList.size();i++){
			IAtomContainer ac=null;
			Object obj=atomContainersList.get(i);
			if (obj instanceof org.openscience.cdk.interfaces.IMolecule) {
				ac=(org.openscience.cdk.interfaces.IMolecule)obj;
			}else if (obj instanceof IAtomContainer) {
				ac=(IAtomContainer)obj;
			}

			CDKMolecule mol=new CDKMolecule(ac);
			String moleculeName="Molecule " + i; 
			if (ac instanceof IMolecule) {
				org.openscience.cdk.interfaces.IMolecule imol = (org.openscience.cdk.interfaces.IMolecule) ac;
				String molName=(String) imol.getProperty(CDKConstants.TITLE);
				if (molName!=null && (!(molName.equals("")))){
					moleculeName=molName;
				}
			}
			mol.setName(moleculeName);
			
			moleculesList.add(mol);
		}
		
		return moleculesList;
	}

	public String calculateSmiles(IMolecule molecule) throws BioclipseException {
		return molecule.getSmiles();
	}

	public void saveMolecule(CDKMolecule seq) throws IllegalStateException {
		// TODO Auto-generated method stub

	}

	/**
	 * Create molecule from SMILES.
	 * @throws BioclipseException 
	 */
	public CDKMolecule createMoleculeFromSMILES(String SMILES) throws BioclipseException {
		SmilesParser parser=new SmilesParser(DefaultChemObjectBuilder.getInstance());
		try {
			org.openscience.cdk.interfaces.IMolecule mol=parser.parseSmiles(SMILES);
			return new CDKMolecule(mol);
		} catch (InvalidSmilesException e) {
			throw new BioclipseException("SMILES string is invalid");
		}
		
	}

	public Iterator<ICDKMolecule> creatMoleculeIterator(InputStream instream) {
		return new IteratingBioclipseMDLReader(instream, NoNotificationChemObjectBuilder.getInstance());
	}

	class IteratingBioclipseMDLReader implements Iterator<ICDKMolecule> {

		IteratingMDLReader reader;
		
		public IteratingBioclipseMDLReader(InputStream input, IChemObjectBuilder builder) {
			reader = new IteratingMDLReader(input, builder);
		}

		public boolean hasNext() {
			return reader.hasNext();
		}

		public ICDKMolecule next() {
			org.openscience.cdk.interfaces.IMolecule cdkMol = (org.openscience.cdk.interfaces.IMolecule)reader.next();
			ICDKMolecule bioclipseMol = new CDKMolecule(cdkMol);
			return bioclipseMol;
		}

		public void remove() {
			reader.remove();
		}
	}
}
