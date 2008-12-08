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
package net.bioclipse.cdk.debug.test;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdkdebug.business.CDKDebugManager;
import net.bioclipse.cdkdebug.business.ICDKDebugManager;
import net.bioclipse.core.business.IBioclipseManager;

import org.junit.Test;

public class CDKDebugManagerPluginTest {

    ICDKDebugManager debug;

    public CDKDebugManagerPluginTest() {
        debug = new CDKDebugManager();
    }

    public IBioclipseManager getManager() {
        return debug;
    }

    @Test public void testDebug() throws Exception {
        CDKManager cdk = new CDKManager();
        ICDKMolecule mol = (ICDKMolecule)cdk.fromSMILES("C");
        debug.debug(mol);
        // would like to test more, but the method does not return anything
    }

    @Test public void testDiff() throws Exception {
        CDKManager cdk = new CDKManager();
        ICDKMolecule mol1 = (ICDKMolecule)cdk.fromSMILES("C");
        ICDKMolecule mol2 = (ICDKMolecule)cdk.fromSMILES("C");
        debug.diff(mol1, mol2);
        // would like to test more, but the method does not return anything
    }

    @Test public void testDepictSybylAtomTypes() throws Exception {
        CDKManager cdk = new CDKManager();
        ICDKMolecule mol = (ICDKMolecule)cdk.fromSMILES("CNCO");
        debug.depictSybylAtomTypes(mol);
        // would like to test more, but the method does not return anything
    }
}
