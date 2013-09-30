/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *     Jonathan Alvarsson
 *
 ******************************************************************************/
package net.bioclipse.opsin.test;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.opsin.business.IOpsinManager;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractOpsinManagerPluginTest {

    protected static IOpsinManager opsin;

    @Test
    public void testParseIUPACName() throws BioclipseException {
    	ICDKMolecule mol = opsin.parseIUPACName("acetyl salicylic acid"); // aspirin
    	Assert.assertNotNull(mol);
    	Assert.assertEquals(21, mol.getAtomContainer().getAtomCount());
    }

    @Test
    public void testParseIUPACNameAsCML() throws BioclipseException {
    	String cml = opsin.parseIUPACNameAsCML("acetyl salicylic acid"); // aspirin
    	Assert.assertNotNull(cml);
    	Assert.assertNotSame(0, cml.length());
    	Assert.assertTrue(cml.contains("http://www.xml-cml.org/schema"));
    }

}
