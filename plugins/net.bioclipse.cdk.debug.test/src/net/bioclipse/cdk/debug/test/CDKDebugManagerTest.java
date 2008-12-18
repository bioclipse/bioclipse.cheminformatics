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

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.junit.Test;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdkdebug.business.CDKDebugManager;
import net.bioclipse.cdkdebug.business.ICDKDebugManager;
import net.bioclipse.core.MockIFile;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.tests.AbstractManagerTest;

public class CDKDebugManagerTest extends AbstractManagerTest {

    ICDKDebugManager debug;

    public CDKDebugManagerTest() {
        debug = new CDKDebugManager();
    }

    public IBioclipseManager getManager() {
        return debug;
    }
    

    /**
     * Test that sybyl atom typing for 232 mols in an SDF does not
     * throw exception
     * @throws CoreException 
     * @throws BioclipseException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws Exception
     */
    @Test public void testDepictSybylAtomTypesFromSDF() throws FileNotFoundException, IOException, BioclipseException, CoreException{
        CDKManager cdk = new CDKManager();

        String path = getClass().getResource("/testFiles/m2d_ref_232.sdf").getPath();
        List<ICDKMolecule> mols = cdk.loadMolecules( new MockIFile(path), null);

        int cnt=0;
        for (ICDKMolecule mol : mols){
            try {
				debug.perceiveSybylAtomTypes(mol);
			} catch (InvocationTargetException e) {
	        	System.out.println("Atom typing of molecule " + cnt + " failed.");
				e.printStackTrace();
				fail("Atom typing of molecule " + cnt + " failed due to: " + e.getMessage() );
			} catch (NullPointerException e) {
	        	System.out.println("Atom typing of molecule " + cnt + " failed.");
				e.printStackTrace();
				fail("Atom typing of molecule " + cnt + " failed due to NPE: " + e.getMessage() );
			}
            // would like to test more, but the method does not return anything
			cnt++;
        }

    }


}
