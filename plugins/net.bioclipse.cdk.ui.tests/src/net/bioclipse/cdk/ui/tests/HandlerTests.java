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
import java.net.URISyntaxException;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.handlers.ConvertSMILEStoSDF;
import net.bioclipse.core.MockIFile;
import net.bioclipse.core.business.BioclipseException;

import org.eclipse.core.runtime.CoreException;
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
}
