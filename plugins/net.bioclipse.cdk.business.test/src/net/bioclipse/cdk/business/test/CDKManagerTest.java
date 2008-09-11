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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.MockIFile;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IMoleculeManager;
import net.bioclipse.core.business.MoleculeManager;
import net.bioclipse.core.domain.IMolecule;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.templates.MoleculeFactory;
import org.openscience.cdk.tools.diff.AtomContainerDiff;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

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
    public void testLoadMoleculeFromCMLFile() throws IOException, 
                                          BioclipseException, 
                                          CoreException {

//        InputStream atpFile = getClass().getResourceAsStream("/testFiles/polycarpol.mol");
//        InputStream pdbFile = getClass().getResourceAsStream("/testFiles/1D66.pdb");
        String path = getClass().getResource("/testFiles/0037.cml").getPath();
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

        System.out.println("mol: " + mol.toString());
    }

    @Test
    public void testLoadCMLFromFile2() throws IOException, 
                                          BioclipseException, 
                                          CoreException {

//        InputStream atpFile = getClass().getResourceAsStream("/testFiles/polycarpol.mol");
//        InputStream pdbFile = getClass().getResourceAsStream("/testFiles/1D66.pdb");
        String path = getClass().getResource("/testFiles/cs2a.cml").getPath();
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

        System.out.println("mol: " + mol.toString());
    }

    //FIXME: Fails. See bug #1958097
    @Test
    public void testLoadATP() throws IOException, 
                                     BioclipseException, 
                                     CoreException {

        String path = getClass().getResource("/testFiles/atp.mol")
                                .getPath();
        
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

        System.out.println("mol: " + mol.toString());
    }

    //FIXME: Fails. See bug #1958097
    @Test
    public void testLoadPolycarpol() throws IOException, 
                                            BioclipseException, 
                                            CoreException {

        String path = getClass().getResource("/testFiles/polycarpol.mol")
                                .getPath();
        
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

        System.out.println("mol: " + mol.toString());
    }

    @Test
    public void testCreateSMILES() throws BioclipseException, 
                                          IOException, 
                                          CoreException {
        String path = getClass().getResource("/testFiles/0037.cml").getPath();
        
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );
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
                    = cdk.createMoleculeIterator( new MockIFile(path),
                                                  null );
              iterator.hasNext(); ) {

            molecules.add( iterator.next() );
        }

        assertEquals( 2, molecules.size() );
    }
    
    @Test
    public void testFingerPrintMatch() throws BioclipseException {
        SmilesGenerator generator = new SmilesGenerator();
        String indoleSmiles  = generator
                               .createSMILES( MoleculeFactory
                                              .makeIndole() );
        String pyrroleSmiles = generator
                               .createSMILES( MoleculeFactory
                                              .makePyrrole() );
        ICDKMolecule indole  = cdk.fromSmiles( indoleSmiles );
        ICDKMolecule pyrrole = cdk.fromSmiles( pyrroleSmiles );
        
        assertTrue( cdk.fingerPrintMatches(indole, pyrrole) );
    }
    
    @Test
    public void testSubStructureMatch() throws BioclipseException {
        SmilesGenerator generator = new SmilesGenerator();
        String indoleSmiles  = generator
                               .createSMILES( MoleculeFactory
                                              .makeIndole() );
        String pyrroleSmiles = generator
                               .createSMILES( MoleculeFactory.
                                              makePyrrole() );
        ICDKMolecule indole  = cdk.fromSmiles( indoleSmiles  );
        ICDKMolecule pyrrole = cdk.fromSmiles( pyrroleSmiles );
        
        assertTrue( cdk.subStructureMatches( indole, pyrrole ) );
    }
    
    @Test
    public void testCDKMoleculeFromIMolecule() throws BioclipseException {
        SmilesGenerator generator = new SmilesGenerator();
        String indoleSmiles  = generator
                               .createSMILES( MoleculeFactory
                                              .makeIndole() );
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
        MockIFile file = new MockIFile( 
            getClass().getResource("/testFiles/dbsmallconf.sdf")
                      .getPath() );

        List<ICDKMolecule> mols = cdk.loadConformers(file, null);
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

    @Test
    public void testSave() throws BioclipseException, CDKException, CoreException, IOException {
        String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSmiles( propaneSmiles  );
        IChemModel chemmodel=propane.getAtomContainer().getBuilder().newChemModel();
        IMoleculeSet setOfMolecules=chemmodel.getBuilder().newMoleculeSet();
        setOfMolecules.addAtomContainer(propane.getAtomContainer());
        chemmodel.setMoleculeSet(setOfMolecules);
        
        getClass().getResource("/testFiles/dbsmallconf.sdf")
        .getPath();
        IFile target=new MockIFile();
        cdk.save(chemmodel, target, cdk.mol);
        byte[] bytes=new byte[6];
        target.getContents().read(bytes);
        Assert.assertArrayEquals(new byte[]{10,32,32,67,68,75}, bytes);
    }

    @Test
    public void testSaveMolecule() throws BioclipseException, CDKException, CoreException, IOException {
        String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSmiles( propaneSmiles  );
        
        getClass().getResource("/testFiles/dbsmallconf.sdf")
        .getPath();
        IFile target=new MockIFile();
        cdk.saveMolecule(propane, target, cdk.mol);
        byte[] bytes=new byte[6];
        target.getContents().read(bytes);
        Assert.assertArrayEquals(new byte[]{10,32,32,67,68,75}, bytes);
    }
    
    @Test
    public void testSybylAtomTypePerception() throws FileNotFoundException, IOException, BioclipseException, CoreException, InvocationTargetException{

    	String path = getClass().getResource("/testFiles/atp.mol").getPath();
    	ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

    	System.out.println("mol: " + mol.toString());
    	
    	ICDKMolecule mol2 = cdk.depictSybylAtomTypes(mol);
    	
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
    	
    	ICDKMolecule mol2 = cdk.depictSybylAtomTypes(mol);
    	
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
    	
    	ICDKMolecule mol2 = cdk.depictSybylAtomTypes(mol);
    	
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

    	ICDKMolecule mol2 = cdk.depictSybylAtomTypes(mol);
    	
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

    
    @Test
    public void testSaveMol2() throws BioclipseException, CDKException, CoreException, IOException {

    	String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSmiles( propaneSmiles  );

        IFile target=new MockIFile();
        cdk.saveMolecule(propane, target, cdk.mol2);
    	
    }

    @Test
    public void testCMLOK1() throws Exception {
        String filename = "testFiles/cs2a.cml";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
        CMLReader reader = new CMLReader(ins);
        IChemFile chemFile = (IChemFile)reader.read(new org.openscience.cdk.ChemFile());

        // test the resulting ChemFile content
        assertNotNull(chemFile);
        assertEquals(chemFile.getChemSequenceCount(), 1);
        org.openscience.cdk.interfaces.IChemSequence seq = chemFile.getChemSequence(0);
        assertNotNull(seq);
        assertEquals(seq.getChemModelCount(), 1);
        org.openscience.cdk.interfaces.IChemModel model = seq.getChemModel(0);
        assertNotNull(model);
        assertEquals(model.getMoleculeSet().getMoleculeCount(), 1);

        // test the molecule
        org.openscience.cdk.interfaces.IMolecule mol = model.getMoleculeSet().getMolecule(0);
        assertNotNull(mol);
        assertEquals(38, mol.getAtomCount());
        assertEquals(48, mol.getBondCount());
        assertTrue(GeometryTools.has3DCoordinates(mol));
        assertTrue(!GeometryTools.has2DCoordinates(mol));
    }

}
