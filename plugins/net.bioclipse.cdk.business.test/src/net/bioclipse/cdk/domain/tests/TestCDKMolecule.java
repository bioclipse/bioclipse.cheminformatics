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
package net.bioclipse.cdk.domain.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.core.business.BioclipseException;

import org.junit.Before;
import org.junit.Test;

public class TestCDKMolecule {

	ICDKManager cdk;
	
	//Do not use SPRING OSGI for this manager	
	//since we are only testing the implementations of the manager methods
	@Before
	public void initialize() {
		cdk=new CDKManager();
	}

	@Test
	public void testFingerprinter() throws IOException, BioclipseException{
		InputStream cmlFile = getClass().getResourceAsStream("/testFiles/0037.cml");

		CDKMolecule mol=cdk.loadMolecule(cmlFile);
		assertNotNull(mol);
		BitSet bs=mol.getFingerprint(false);
		assertNotNull(bs);
		System.out.println("FP: " + bs.toString());
	}
	
	@Test
	public void testGetCML() throws IOException, BioclipseException{
		InputStream cmlFile = getClass().getResourceAsStream("/testFiles/0037.cml");

		CDKMolecule mol=cdk.loadMolecule(cmlFile);
		assertNotNull(mol);
		String cmlString=mol.getCML();
		assertNotNull(cmlString);
		System.out.println("CML:\n" + cmlString);
	}

	@Test
	public void testGetSmiles() throws IOException, BioclipseException{
		InputStream cmlFile = getClass().getResourceAsStream("/testFiles/0037.cml");

		CDKMolecule mol=cdk.loadMolecule(cmlFile);
		assertNotNull(mol);
		String smiles=mol.getSmiles();
		assertNotNull(smiles);
		System.out.println("Smiles: " + smiles);
		
	}

}
