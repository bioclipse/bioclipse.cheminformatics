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

package net.bioclipse.cdk.domain;

import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioObject;
import net.bioclipse.core.domain.IMolecule;

public class CDKChemFile extends BioObject implements IMolecule{

	private IChemFile chemFile;
	private String cachedSMILES;
	
	public CDKChemFile(IChemFile chemFile) {
		super();
		this.chemFile=chemFile;
	}

	public Object getParsedResource() {
		return chemFile;
	}

	public IChemFile getChemFile() {
		return chemFile;
	}

	public void setChemFile(IChemFile chemFile) {
		this.chemFile = chemFile;
	}

	public String getSMILES() throws BioclipseException {
		
		//TODO: wrap in job?
		
		if (cachedSMILES != null) {
			return cachedSMILES;
		}
		
		if (getChemFile() == null) throw new BioclipseException("Molecule is empty");
		
		if (ChemFileManipulator.getAtomCount(getChemFile()) > 100)
			throw new BioclipseException("Not calculating SMILES: molecule has more than 100 atoms.");
		
		if (ChemFileManipulator.getBondCount(getChemFile()) == 0)
			throw new BioclipseException("Not calculating SMILES: molecule has no bonds.");
		
        // ok, need to create a SMILES...
//		logger.debug("Calculating SMILES...");
        SmilesGenerator generator = new SmilesGenerator();
        try {
        	List containersList = ChemFileManipulator.getAllAtomContainers(getChemFile());
        	if (containersList.size() > 1) {
        		org.openscience.cdk.interfaces.IMolecule fullSet = getChemFile().getBuilder().newMolecule();
        		Iterator iterator = containersList.iterator();
        		while(iterator.hasNext())
        			fullSet.add((IAtomContainer)iterator.next());
        		cachedSMILES = generator.createSMILES(fullSet);
        	} else if (containersList.size() == 1) {
        		cachedSMILES = generator.createSMILES(
        			getChemFile().getBuilder().newMolecule(
        				(IAtomContainer)containersList.get(0)
        			)
        		);
        	} // skip
//        	logger.debug("SMILES cached: " + cachedSMILES);
        } catch(Exception exception) {
        	 throw new BioclipseException("General error generating SMILES");
        	 //        	logger.error(message);
        }

		return cachedSMILES;
	}

}
