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

package net.bioclipse.cdk.business.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.MockIFile;
import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.CDKManagerHelper;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IMoleculeManager;
import net.bioclipse.core.business.MoleculeManager;
import net.bioclipse.core.domain.IMolecule;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.templates.MoleculeFactory;

public class CDKManagerTest {

    //Needed to run these tests on some systems. If it breaks them on 
    //other systems we need to do some sort of checking before 
    //setting them...
    static {
        System.setProperty( "javax.xml.parsers.SAXParserFactory", 
                            "com.sun.org.apache.xerces.internal." 
                                + "jaxp.SAXParserFactoryImpl" );
        System.setProperty( "javax.xml.parsers.DocumentBuilderFactory", 
                            "com.sun.org.apache.xerces.internal."
                                + "jaxp.DocumentBuilderFactoryImpl" );
    }
    
    ICDKManager cdk;

    //Do not use SPRING OSGI for this manager
    //since we are only testing the implementations of the manager methods
    public CDKManagerTest() {
        cdk = new CDKManager();
    }

    @Test
    public void testLoadMolecule() throws IOException, 
                                          BioclipseException, 
                                          CoreException {

//        InputStream atpFile = getClass().getResourceAsStream("/testFiles/polycarpol.mol");
//        InputStream pdbFile = getClass().getResourceAsStream("/testFiles/1D66.pdb");
        String path = getClass().getResource("/testFiles/0037.cml").getPath();
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path) );

        System.out.println("mol: " + mol.toString());
    }

    //FIXME: Fails. See bug #1958097
    @Test
    public void testLoadATP() throws IOException, 
                                     BioclipseException, 
                                     CoreException {

        String path = getClass().getResource("/testFiles/atp.mol")
                                .getPath();
        
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path) );

        System.out.println("mol: " + mol.toString());
    }

    //FIXME: Fails. See bug #1958097
    @Test
    public void testLoadPolycarpol() throws IOException, 
                                            BioclipseException, 
                                            CoreException {

        String path = getClass().getResource("/testFiles/polycarpol.mol")
                                .getPath();
        
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path) );

        System.out.println("mol: " + mol.toString());
    }

    @Test
    public void testCreateSMILES() throws BioclipseException, 
                                          IOException, 
                                          CoreException {
        String path = getClass().getResource("/testFiles/0037.cml").getPath();
        
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path) );
        String smiles = mol.getSmiles();

        assertEquals("CC1CCCC(C#N)N1C(CO[Si](C)(C)C)C2=CC=CC=C2", smiles);
    }

    @Test
    public void testCreateMoleculeFromSMILES() throws BioclipseException {

        ICDKMolecule mol=cdk.fromSmiles("C1CCCCC1CCO");

        assertEquals(mol.getAtomContainer().getAtomCount(), 9);
        assertEquals(mol.getAtomContainer().getBondCount(), 9);
    }

    @Test
    public void testCreatingMoleculeIterator() 
                throws CoreException, 
                       FileNotFoundException {

        String path = getClass().getResource("/testFiles/test.sdf")
                                .getPath();
        
        List<IMolecule> molecules = new ArrayList<IMolecule>();

        for ( Iterator<net.bioclipse.cdk.domain.ICDKMolecule> iterator
                    = cdk.createMoleculeIterator( new MockIFile(path) );
              iterator.hasNext(); ) {

            molecules.add( iterator.next() );
        }

        assertEquals( 2, molecules.size() );
    }
    
    @Test
    public void testFingerPrintMatch() throws BioclipseException {
        SmilesGenerator generator = new SmilesGenerator();
        String indoleSmiles  = generator
                               .createSMILES( MoleculeFactory.makeIndole() );
        String pyrroleSmiles = generator
                               .createSMILES( MoleculeFactory.makePyrrole() );
        ICDKMolecule indole  = cdk.fromSmiles( indoleSmiles );
        ICDKMolecule pyrrole = cdk.fromSmiles( pyrroleSmiles );
        
        assertTrue( cdk.fingerPrintMatches(indole, pyrrole) );
    }
    
    @Test
    public void testSubStructureMatch() throws BioclipseException {
        SmilesGenerator generator = new SmilesGenerator();
        String indoleSmiles  = generator
                               .createSMILES( MoleculeFactory.makeIndole() );
        String pyrroleSmiles = generator
                               .createSMILES( MoleculeFactory.makePyrrole() );
        ICDKMolecule indole  = cdk.fromSmiles( indoleSmiles  );
        ICDKMolecule pyrrole = cdk.fromSmiles( pyrroleSmiles );
        
        assertTrue( cdk.subStructureMatches( indole, pyrrole ) );
    }
    
    @Test
    public void testCDKMoleculeFromIMolecule() throws BioclipseException {
        SmilesGenerator generator = new SmilesGenerator();
        String indoleSmiles  = generator
                               .createSMILES( MoleculeFactory.makeIndole() );
        IMoleculeManager molecule = new MoleculeManager();
        IMolecule m = molecule.fromSmiles( indoleSmiles );
        ICDKMolecule cdkm = cdk.create( m );
        assertEquals( cdkm.getSmiles(), m.getSmiles() );
    }
    
    @Test
    public void testSMARTSMatching() throws BioclipseException {
        String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSmiles( propaneSmiles  );
        
        assertTrue( cdk.smartsMatches(propane, propaneSmiles) );
    }

    @Test
    public void testLoadConformers() throws BioclipseException, IOException {
        InputStream sdfFile = getClass().getResourceAsStream("/testFiles/dbsmallconf.sdf");

        List<ICDKMolecule> mols = cdk.loadConformers(sdfFile);
        assertNotNull( mols );
        assertEquals( 3, mols.size() );
        
        assertEquals( 3, mols.get( 0 ).getConformers().size() );
        assertEquals( 1, mols.get( 1 ).getConformers().size() );
        assertEquals( 2, mols.get( 2 ).getConformers().size() );
        
//        System.out.println(mols.get( 0 ).getConformers().get( 0 ).getSmiles());
//        System.out.println(mols.get( 0 ).getConformers().get( 1 ).getSmiles());
//        System.out.println(mols.get( 0 ).getConformers().get( 2 ).getSmiles());
//        System.out.println(mols.get( 1 ).getConformers().get( 0 ).getSmiles());
//        System.out.println(mols.get( 2 ).getConformers().get( 0 ).getSmiles());
//        System.out.println(mols.get( 2 ).getConformers().get( 1 ).getSmiles());

    }

}
