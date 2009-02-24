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
package net.bioclipse.cml.tests;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cml.managers.Activator;
import net.bioclipse.cml.managers.IValidateCMLManager;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;

public class ValidateCMLManagerPluginTest {

    private static IValidateCMLManager cml;
    private static ICDKManager cdk;

    public ValidateCMLManagerPluginTest() {}

    @BeforeClass public static void setup() {
        // the next line is needed to ensure the OSGI loader properly start
        // the org.springframework.bundle.osgi.extender, so that the manager
        // can be loaded too. Otherwise, it will fail with a time out.
        net.bioclipse.ui.Activator.getDefault();

        cml = Activator.getDefault().getValidateCMLManager();
        cdk = net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
    }

    @Test
    public void testValidate_String() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CNC");
        cdk.saveMolecule(mol, "/Virtual/testValidate.cml",
                (IChemFormat)CMLFormat.getInstance(), true);
        cml.validate("/Virtual/testValidate.cml");
    }
    
}
