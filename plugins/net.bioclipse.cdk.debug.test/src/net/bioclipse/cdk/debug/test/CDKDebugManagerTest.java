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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdkdebug.business.CDKDebugManager;
import net.bioclipse.cdkdebug.business.ICDKDebugManager;
import net.bioclipse.core.MockIFile;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.tests.AbstractManagerTest;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.templates.MoleculeFactory;
import org.openscience.cdk.tools.diff.AtomContainerDiff;

public class CDKDebugManagerTest extends AbstractManagerTest {

    ICDKManager cdk;
    ICDKDebugManager debug;

    public CDKDebugManagerTest() {
        cdk = new CDKManager();
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

    @Test
    public void testSybylAtomTypePerceptionFromSMILES() throws FileNotFoundException, IOException, BioclipseException, CoreException, InvocationTargetException{

        ICDKMolecule mol = cdk.fromSMILES("C1CCCCC1CCOC");
        
        ICDKMolecule mol2 = debug.perceiveSybylAtomTypes(mol);
        
        for (int i=0; i<mol2.getAtomContainer().getAtomCount(); i++){
            IAtom a=mol2.getAtomContainer().getAtom(i);
            System.out.println("Atom: " + a.getSymbol() + i + ", type=" + a.getAtomTypeName());
        }

    }

    @Test
    public void testSybylAtomTypePerception() throws FileNotFoundException, IOException, BioclipseException, CoreException, InvocationTargetException{

        String path = getClass().getResource("/testFiles/atp.mol").getPath();
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

        System.out.println("mol: " + mol.toString());
        
        ICDKMolecule mol2 = debug.perceiveSybylAtomTypes(mol);
        
        for (int i=0; i<mol2.getAtomContainer().getAtomCount(); i++){
            IAtom a=mol2.getAtomContainer().getAtom(i);
            System.out.println("Atom: " + a.getSymbol() + i + ", type=" + a.getAtomTypeName());
        }

    }

    @Test
    public void testSybylAtomTypePerception2() throws FileNotFoundException, IOException, BioclipseException, CoreException, InvocationTargetException{

        String path = getClass().getResource("/testFiles/polycarpol.mol")
        .getPath();

        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

        System.out.println("mol: " + mol.toString());
        
        ICDKMolecule mol2 = debug.perceiveSybylAtomTypes(mol);
        
        for (int i=0; i<mol2.getAtomContainer().getAtomCount(); i++){
            IAtom a=mol2.getAtomContainer().getAtom(i);
            System.out.println("Atom: " + a.getSymbol() + i + ", type=" + a.getAtomTypeName());
        }

    }
    
    @Test
    public void testSybylAtomTypePerception3() throws FileNotFoundException, IOException, BioclipseException, CoreException, InvocationTargetException{

        String path = getClass().getResource("/testFiles/aromatic.mol")
        .getPath();

        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

        System.out.println("mol: " + mol.toString());
        
        ICDKMolecule mol2 = debug.perceiveSybylAtomTypes(mol);
        
        assertEquals("C.ar", mol2.getAtomContainer().getAtom(1).getAtomTypeName());
        
        for (int i=0; i<mol2.getAtomContainer().getAtomCount(); i++){
            IAtom a=mol2.getAtomContainer().getAtom(i);
            System.out.println("Atom: " + a.getSymbol() + i + ", type=" + a.getAtomTypeName());
        }

    }

    @Test
    public void testSybylAtomTypePerceptionBenzene() throws CDKException, FileNotFoundException, IOException, BioclipseException, CoreException, InvocationTargetException{

        IAtomContainer ac=MoleculeFactory.makeBenzene();
        
        ICDKMolecule mol = new CDKMolecule(ac);

        ICDKMolecule mol2 = debug.perceiveSybylAtomTypes(mol);
        
        System.out.println("** BENZENE **");
        
        System.out.println(AtomContainerDiff.diff( ac, mol2.getAtomContainer()));
        
        for (int i=0; i<mol2.getAtomContainer().getAtomCount(); i++){
            IAtom a=mol2.getAtomContainer().getAtom(i);
            System.out.println("Atom: " + a.getSymbol() + i + ", type=" + a.getAtomTypeName());
        }

        assertEquals("C.ar", mol2.getAtomContainer().getAtom(0).getAtomTypeName());
        assertEquals("C.ar", mol2.getAtomContainer().getAtom(1).getAtomTypeName());
        assertEquals("C.ar", mol2.getAtomContainer().getAtom(2).getAtomTypeName());
        assertEquals("C.ar", mol2.getAtomContainer().getAtom(3).getAtomTypeName());
        assertEquals("C.ar", mol2.getAtomContainer().getAtom(4).getAtomTypeName());
        assertEquals("C.ar", mol2.getAtomContainer().getAtom(5).getAtomTypeName());
        

    }

}
