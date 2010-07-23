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
package net.bioclipse.cdk.ui.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.filters.FilterOutSalts;
import net.bioclipse.cdk.ui.handlers.ConvertSMILEStoSDF;
import net.bioclipse.core.MockIFile;
import net.bioclipse.core.business.BioclipseException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

/**
 * Tests for the handlers in net.bc.cdk.ui
 * 
 * @author ola
 *
 */
public class HandlerTests {

	/**
	 * Test converting a comma-separated SMIELS file into an SDF.
	 * @throws CoreException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws BioclipseException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
    @Test
    public void testConvertSMILEStoSDF() throws CoreException, IOException, URISyntaxException, BioclipseException, InvocationTargetException, InterruptedException {
		String csvfile = "/testFiles/testsmi2sdf.smi";
		InputStream stream = getClass().getResourceAsStream(csvfile);
		MockIFile ifile=new MockIFile(stream);

		List<ICDKMolecule> mols = ConvertSMILEStoSDF.readFileIntoMoleculeList(ifile, new NullProgressMonitor());

		//Confirm all molecules are read
		assertEquals(8, mols.size());
		
		//Confirm properties are stored on first mol
		assertEquals("842267", mols.get(0).getAtomContainer().getProperty("PUBCHEM_SID"));
		assertEquals("", mols.get(0).getAtomContainer().getProperty("PUBCHEM_EXT_DATASOURCE_REGID"));
		assertEquals("644526", mols.get(0).getAtomContainer().getProperty("PUBCHEM_CID"));
		assertEquals("2", mols.get(0).getAtomContainer().getProperty("PUBCHEM_ACTIVITY_OUTCOME"));
		assertEquals("26", mols.get(0).getAtomContainer().getProperty("PUBCHEM_ACTIVITY_SCORE"));
		assertEquals("\"\"", mols.get(0).getAtomContainer().getProperty("PUBCHEM_ACTIVITY_URL"));
		assertEquals("20100519", mols.get(0).getAtomContainer().getProperty("PUBCHEM_ASSAYDATA_COMMENT"));
		assertEquals("\"\"", mols.get(0).getAtomContainer().getProperty("PUBCHEM_ASSAYDATA_REVOKE"));
		assertEquals("123.22", mols.get(0).getAtomContainer().getProperty("1"));
		assertEquals("10.2743", mols.get(0).getAtomContainer().getProperty("2"));

	}	
    
    @Test
    public void testFilterOutSalts() throws MalformedURLException, IOException, BioclipseException, CoreException, URISyntaxException{

    	String smiles1="CC1=CC=CC2=C1N=C(C(=C2)C#N)SCCN3CCCCC3.Cl";
		String smiles2="C1CN2C3=CC=CC=C3N(C2=N1)CC(=O)C45CC6CC(C4)CC(C6)C5.Br";

		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
		List<ICDKMolecule> mols = new ArrayList<ICDKMolecule>();
		mols.add(cdk.fromSMILES(smiles1));
		mols.add(cdk.fromSMILES(smiles2));
		
		//Assert we have salts in all mols
		for (ICDKMolecule mol : mols){
			assertFalse(cdk.isConnected(mol));
		}

		//Remove salts
		FilterOutSalts.filterOutSalts(mols, new NullProgressMonitor());

		//Assert we have NO salts in all mols
		for (ICDKMolecule mol : mols){
			assertTrue(cdk.isConnected(mol));
		}

    }
    

	private String sourcePathtoAbsolutePath(String sourcepath) 
	throws URISyntaxException, MalformedURLException, IOException{
		URI uri = getClass().getResource(sourcepath).toURI();
		URL url = FileLocator.toFileURL(uri.toURL());
		return url.getFile();
	}

}
