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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.CDKManagerHelper;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdkdebug.business.ICDKDebugManager;
import net.bioclipse.core.MockIFile;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.business.IMoleculeManager;
import net.bioclipse.core.business.MoleculeManager;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.tests.AbstractManagerTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.MDLV2000Format;
import org.openscience.cdk.io.formats.Mol2Format;
import org.openscience.cdk.io.formats.SDFFormat;
import org.openscience.cdk.io.formats.SMILESFormat;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.templates.MoleculeFactory;

public class CDKManagerTest extends AbstractManagerTest {

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
    
    CDKManager cdk;
    ICDKDebugManager cdkdebug;

    //Do not use SPRING OSGI for this manager
    //since we are only testing the implementations of the manager methods
    public CDKManagerTest() {
        cdk = new CDKManager();
    }
    
    public IBioclipseManager getManager() {
        return cdk;
    }

    @Test
    public void testLoadMoleculeFromCMLFile() throws IOException, 
                                          BioclipseException, 
                                          CoreException {

//        InputStream atpFile = getClass().getResourceAsStream("/testFiles/polycarpol.mol");
//        InputStream pdbFile = getClass().getResourceAsStream("/testFiles/1D66.pdb");
        String path = getClass().getResource("/testFiles/0037.cml").getPath();
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

        Assert.assertNotNull(mol);
        Assert.assertNotSame(0, mol.getAtomContainer().getAtomCount());
        Assert.assertNotSame(0, mol.getAtomContainer().getBondCount());
    }

    @Test
    public void testLoadCMLFromFile2() throws IOException, 
                                          BioclipseException, 
                                          CoreException {

//        InputStream atpFile = getClass().getResourceAsStream("/testFiles/polycarpol.mol");
//        InputStream pdbFile = getClass().getResourceAsStream("/testFiles/1D66.pdb");
        String path = getClass().getResource("/testFiles/cs2a.cml").getPath();
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

        Assert.assertNotNull(mol);
        Assert.assertNotSame(0, mol.getAtomContainer().getAtomCount());
        Assert.assertNotSame(0, mol.getAtomContainer().getBondCount());
    }

    
    @Test
    public void testLoadMoleculeFromSMILESFileDirectly() throws IOException, 
                                          BioclipseException, 
                                          CoreException {

        String path = getClass().getResource("/testFiles/nprods.smi").getPath();
        List<ICDKMolecule> mol = cdk.loadSMILESFile(
            new MockIFile(path), (IProgressMonitor)null
        );
        assertNotNull( mol );
        System.out.println("SMILES file size: " + mol.size());
        assertEquals(30, mol.size());
    }

    @Test
    public void testLoadMoleculeFromSMILESFile() throws IOException, 
                                          BioclipseException, 
                                          CoreException {

        String path = getClass().getResource("/testFiles/nprods.smi").getPath();
        List<ICDKMolecule> mols = cdk.loadMolecules(new MockIFile(path), (IChemFormat)SMILESFormat.getInstance(), null);
        
        System.out.println("SMILES file size: " + mols.size());
        assertEquals(30, mols.size());
        
        for (ICDKMolecule mol : mols){
        	System.out.println("Mol: " + mol.getName() + " SMILES: " + mol.getSMILES());
        	if (mol.getName().equals("1")){
                ICDKMolecule smilesMol1 = cdk.fromSMILES("C(=O)N(Cc1ccco1)C(c1cc2ccccc2cc1)C(=O)NCc1ccccc1");
                double expm=cdk.calculateMass(smilesMol1);
                assertEquals(expm, cdk.calculateMass(mol));
                assertTrue(cdk.fingerPrintMatches(smilesMol1, mol));
        	}

        	if (mol.getName().equals("30")){
                ICDKMolecule smilesMol1 = cdk.fromSMILES("C(=O)N(Cc1ccc(o1)C)C(c1ccccc1)C(=O)NCS(=O)(=O)c1ccc(cc1)C");
                double expm=cdk.calculateMass(smilesMol1);
                assertEquals(expm, cdk.calculateMass(mol));
                assertTrue(cdk.fingerPrintMatches(smilesMol1, mol));
        	}
        }

    }
    
    @Test
    public void testloadMoleculesFromSMILESCheck() throws BioclipseException {
        String[] input = {"CC","CCC(CC)CC","CCC"};
        
        StringBuilder sb = new StringBuilder();
        for(String s: input) {
            sb.append( s );
            sb.append( "\n" );
        }
        
        IFile file = new MockIFile(
                           new ByteArrayInputStream(sb.toString().getBytes()))
                            .extension( "smi" );
        
        
        try {
            List<ICDKMolecule> molecules = cdk.loadSMILESFile(
                file, (IProgressMonitor)null
            );
            Assert.assertNotNull( molecules );
            Assert.assertEquals(3, molecules.size());
            List<String> inputList = new ArrayList<String>(Arrays.asList( input ));
            
            for(ICDKMolecule molecule:molecules) {
                String smiles = molecule.getSMILES();
                if(inputList.contains( smiles ))
                    inputList.remove( smiles );
            }
            Assert.assertEquals( 0, inputList.size() );
        } catch ( CoreException e ) {
            Assert.fail( e.getMessage() );
        } catch ( IOException e ) {
            Assert.fail( e.getMessage());
        }
    }
    
    @Test
    public void testLoadATP() throws IOException, 
                                     BioclipseException, 
                                     CoreException {

        String path = getClass().getResource("/testFiles/atp.mol")
                                .getPath();
        
        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );

        System.out.println("mol: " + mol.toString());
    }

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
        String smiles = mol.getSMILES();

        assertEquals("N#CC1CCCC(C)N1C(CO[Si](C)(C)C)C2=CC=CC=C2", smiles);
    }

    @Test
    public void testCreateMoleculeFromSMILES() throws BioclipseException {

        ICDKMolecule mol=cdk.fromSMILES("C1CCCCC1CCO");

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
        ICDKMolecule indole  = cdk.fromSMILES( indoleSmiles );
        ICDKMolecule pyrrole = cdk.fromSMILES( pyrroleSmiles );
        
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
        ICDKMolecule indole  = cdk.fromSMILES( indoleSmiles  );
        ICDKMolecule pyrrole = cdk.fromSMILES( pyrroleSmiles );
        
        assertTrue( cdk.subStructureMatches( indole, pyrrole ) );
    }
    
    @Test
    public void testStructureMatches() throws BioclipseException {
    	ICDKMolecule molecule = cdk.fromSMILES("CCCBr");
    	ICDKMolecule molecule2 = cdk.fromSMILES("CCCBr");
    	ICDKMolecule molecule3 = cdk.fromSMILES("C1CCBrC1");
    	assertTrue(cdk.areIsomorphic(molecule, molecule2));
    	Assert.assertFalse(cdk.areIsomorphic(molecule, molecule3));
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
        assertEquals( cdkm.getSMILES(), m.getSMILES() );
    }
    
    @Test
    public void testSMARTSMatching() throws BioclipseException {
        String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSMILES( propaneSmiles  );
        
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
        
        ICDKMolecule propane  = cdk.fromSMILES( propaneSmiles  );
        IChemModel chemmodel=propane.getAtomContainer().getBuilder().newChemModel();
        IMoleculeSet setOfMolecules=chemmodel.getBuilder().newMoleculeSet();
        setOfMolecules.addAtomContainer(propane.getAtomContainer());
        chemmodel.setMoleculeSet(setOfMolecules);
        
        IFile target=new MockIFile();
        cdk.save(chemmodel, target, (IChemFormat)MDLV2000Format.getInstance(), null);
        byte[] bytes=new byte[6];
        target.getContents().read(bytes);
        Assert.assertArrayEquals(new byte[]{10,32,32,67,68,75}, bytes);
    }

    @Test
    public void testSaveMolecule() throws BioclipseException, CDKException, CoreException, IOException {
        String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSMILES( propaneSmiles  );
        
        IFile target=new MockIFile();
        cdk.saveMolecule(propane, target, (IChemFormat)MDLV2000Format.getInstance());
        byte[] bytes=new byte[6];
        target.getContents().read(bytes);
        Assert.assertArrayEquals(new byte[]{10,32,32,67,68,75}, bytes);
    }

    @Test
    public void testSaveMoleculesSDF() throws BioclipseException, CDKException, CoreException, IOException {

        System.out.println("*************************");
        System.out.println("testSaveMoleculesSDF()");

        MoleculeManager molmg=new MoleculeManager();
        IMolecule mol1=molmg.fromSmiles("CCC");
        IMolecule mol2=molmg.fromSmiles("C1CCCCC1CCO");
        
        List<IMolecule> mols=new ArrayList<IMolecule>();
        mols.add(mol1);
        mols.add(mol2);
        
        IFile target=new MockIFile();
        cdk.saveMolecules(mols, target, (IChemFormat)SDFFormat.getInstance());

        List<ICDKMolecule> readmols = cdk.loadMolecules(target);
        assertEquals(2, readmols.size());
        
    	System.out.println("** Reading back created SDFile: ");
        for (ICDKMolecule cdkmol : readmols){
        	System.out.println("  - SMILES: " + cdk.calculateSMILES(cdkmol));
        }
        
        System.out.println("*************************");
        
    }
    
    @Test
    public void testSaveMoleculesSDFwithProps() throws BioclipseException, CDKException, CoreException, IOException {

        System.out.println("*************************");
        System.out.println("testSaveMoleculesSDFwithProps()");

        ICDKMolecule mol1=cdk.fromSMILES("CCC");
        ICDKMolecule mol2=cdk.fromSMILES("C1CCCCC1CCO");
        
        mol1.getAtomContainer().setProperty("wee", "how");
        mol2.getAtomContainer().setProperty("santa", "claus");
        
        List<IMolecule> mols=new ArrayList<IMolecule>();
        mols.add(mol1);
        mols.add(mol2);
        
        IFile target=new MockIFile();
        cdk.saveMolecules(mols, target, (IChemFormat)SDFFormat.getInstance());

        BufferedReader reader=new BufferedReader(new InputStreamReader(target.getContents()));

        System.out.println("#############################################");
        String line=reader.readLine();
        while(line!=null){
        	System.out.println(line);
            line=reader.readLine();
        }
        System.out.println("#############################################");
        
        List<ICDKMolecule> readmols = cdk.loadMolecules(target);
        assertEquals(2, readmols.size());

    	System.out.println("** Reading back created SDFile: ");
        for (ICDKMolecule cdkmol : readmols){
        	System.out.println("  - SMILES: " + cdk.calculateSMILES(cdkmol));
        	if (cdkmol.getAtomContainer().getAtomCount()==3){
            	assertEquals("how", cdkmol.getAtomContainer().getProperty("wee"));
        	}else{
            	assertEquals("claus", cdkmol.getAtomContainer().getProperty("santa"));
        	}
        }
        
        System.out.println("*************************");
        
    }
    
    
    @Test
    public void testSaveMoleculesCML() throws BioclipseException, CDKException, CoreException, IOException {

        System.out.println("*************************");
        System.out.println("testSaveMoleculesCML()");

        MoleculeManager molmg=new MoleculeManager();
        IMolecule mol1=molmg.fromSmiles("CCC");
        IMolecule mol2=molmg.fromSmiles("C1CCCCC1CCO");
        
        List<IMolecule> mols=new ArrayList<IMolecule>();
        mols.add(mol1);
        mols.add(mol2);
        
        IFile target=new MockIFile();
        cdk.saveMolecules(mols, target, (IChemFormat)CMLFormat.getInstance());

        List<ICDKMolecule> readmols = cdk.loadMolecules(target);
        assertEquals(2, readmols.size());

    	System.out.println("** Reading back created CML file: ");
        for (ICDKMolecule cdkmol : readmols){
        	System.out.println("  - SMILES: " + cdk.calculateSMILES(cdkmol));
//            System.out.println("Generated CML: \n");
//            System.out.println(cdkmol.getCML());
        }
        System.out.println("*************************");
        
    }

    @Test
    public void testSaveMoleculesCMLwithProps() throws BioclipseException, CDKException, CoreException, IOException {

        System.out.println("*************************");
        System.out.println("testSaveMoleculesCMLwithProps()");

        ICDKMolecule mol1=cdk.fromSMILES("CCC");
        ICDKMolecule mol2=cdk.fromSMILES("C1CCCCC1CCO");
        
        mol1.getAtomContainer().setProperty("wee", "how");
        mol2.getAtomContainer().setProperty("santa", "claus");
        
        List<IMolecule> mols=new ArrayList<IMolecule>();
        mols.add(mol1);
        mols.add(mol2);
        
        IFile target=new MockIFile();
        cdk.saveMolecules(mols, target, (IChemFormat)CMLFormat.getInstance());

        BufferedReader reader=new BufferedReader(new InputStreamReader(target.getContents()));

        System.out.println("#############################################");
        String line=reader.readLine();
        while(line!=null){
        	System.out.println(line);
            line=reader.readLine();
        }
        System.out.println("#############################################");
        
        List<ICDKMolecule> readmols = cdk.loadMolecules(target);
    	System.out.println("** Reading back created CML File: ");
        for (ICDKMolecule cdkmol : readmols){
        	System.out.println("  - SMILES: " + cdk.calculateSMILES(cdkmol));
        	if (cdkmol.getAtomContainer().getAtomCount()==3){
            	assertEquals("how", cdkmol.getAtomContainer().getProperty("wee"));
        	}else{
            	assertEquals("claus", cdkmol.getAtomContainer().getProperty("santa"));
        	}
        }
        
        System.out.println("*************************");
        
    }


    
    @Test
    public void testSaveMol2() throws BioclipseException, CDKException, CoreException, IOException {

    	String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSMILES( propaneSmiles  );

        IFile target=new MockIFile();
        cdk.saveMolecule(propane, target, (IChemFormat)Mol2Format.getInstance());
    	
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

    
    @Test
    public void testLoadCMLFromFile3() throws IOException, 
                                          BioclipseException, 
                                          CoreException {

        String path = getClass().getResource("/testFiles/cs2a.cml").getPath();
        MockIFile mf=new MockIFile(path);
        
        ReaderFactory readerFactory=new ReaderFactory();
        CDKManagerHelper.registerSupportedFormats(readerFactory);

        //Create the reader
        ISimpleChemObjectReader reader 
            = readerFactory.createReader(mf.getContents());

        if (reader==null) {
            throw new BioclipseException("Could not create reader in CDK.");
        }

        IChemFile chemFile = new org.openscience.cdk.ChemFile();

        // Do some customizations...
        CDKManagerHelper.customizeReading(reader);

        //Read file
        try {
            chemFile=(IChemFile)reader.read(chemFile);
        } catch (CDKException e) {
        	e.printStackTrace();
        }

    }
    
    @Test public void testAddExplicitHydrogens() throws Exception {
        ICDKMolecule molecule = cdk.fromSMILES("C");
        assertEquals(1, molecule.getAtomContainer().getAtomCount());
        cdk.addExplicitHydrogens(molecule);
        assertEquals(5, molecule.getAtomContainer().getAtomCount());
        assertEquals(0, molecule.getAtomContainer().getAtom(0).getHydrogenCount());
    }

    @Test public void testAddImplicitHydrogens() throws Exception {
        ICDKMolecule molecule = cdk.fromSMILES("C");
        assertEquals(1, molecule.getAtomContainer().getAtomCount());
        cdk.addImplicitHydrogens(molecule);
        assertEquals(1, molecule.getAtomContainer().getAtomCount());
        assertEquals(4, molecule.getAtomContainer().getAtom(0).getHydrogenCount());
    }

    @Test public void testGenerate3DCoordinates() throws Exception {
        ICDKMolecule molecule = cdk.fromSMILES("CCC");
        assertEquals(3, molecule.getAtomContainer().getAtomCount());
        Assert.assertNull(molecule.getAtomContainer().getAtom(0).getPoint3d());
        cdk.generate3dCoordinates(molecule);
        assertNotNull(molecule.getAtomContainer().getAtom(0).getPoint3d());
    }

    @Test public void testGenerate2DCoordinates() throws Exception {
        ICDKMolecule molecule = cdk.fromSMILES("CCCBr");
        assertEquals(4, molecule.getAtomContainer().getAtomCount());
        Assert.assertNull(molecule.getAtomContainer().getAtom(0).getPoint2d());
        IMolecule cdkMolecule = cdk.generate2dCoordinates(molecule);
        Assert.assertTrue(cdkMolecule instanceof ICDKMolecule);
        assertNotNull(((ICDKMolecule)cdkMolecule).getAtomContainer().getAtom(0).getPoint2d());
    }
    
    @Test public void testHas2d() throws Exception {
        ICDKMolecule molecule = cdk.fromSMILES("CCCBr");
        Assert.assertFalse(cdk.has2d(molecule));
        IMolecule cdkMolecule = cdk.generate2dCoordinates(molecule);
        Assert.assertTrue(cdk.has2d(cdkMolecule));
    }

    @Test public void testHas3d() throws Exception {
        ICDKMolecule molecule = cdk.fromSMILES("CCCBr");
        Assert.assertFalse(cdk.has3d(molecule));
        cdk.generate3dCoordinates(molecule);
        Assert.assertTrue(cdk.has3d(molecule));
    }

    @Test public void testFromCML() throws Exception {
        ICDKMolecule molecule = cdk.fromCml("<molecule id='m1'><atomArray atomID='a1 a2' x2='0.0 0.1' y2='1.2 1.3'/></molecule>");
        Assert.assertNotNull(molecule);
        Assert.assertEquals(2, molecule.getAtomContainer().getAtomCount());
    }

    @Test public void testFromString() throws Exception {
        ICDKMolecule molecule = cdk.fromCml("<molecule id='m1'><atomArray atomID='a1 a2' x2='0.0 0.1' y2='1.2 1.3'/></molecule>");
        Assert.assertNotNull(molecule);
        Assert.assertEquals(2, molecule.getAtomContainer().getAtomCount());
    }

    @Test public void testCreateSDFile_File_IMoleculeArray() throws Exception{
    	IMolecule[] mol=new IMolecule[2];
    	mol[0] = cdk.fromSMILES("CCCBr");
    	mol[1] = cdk.fromSMILES("CCCCl");
    	IFile file=new MockIFile();
    	cdk.createSDFile(file, mol);
    	byte[] bytes=new byte[1000];
    	file.getContents().read(bytes);
    	StringBuffer sb=new StringBuffer();
        for(int i=0;i<bytes.length;i++){
        	sb.append((char)bytes[i]);
        }
        assertTrue(sb.toString().contains("$$$$"));
        assertTrue(sb.toString().contains("Cl"));
        assertTrue(sb.toString().contains("Br"));
    }
    
    
    @Test
    public void testExtractFromSDFile_IFile_int_int() throws FileNotFoundException, BioclipseException, InvocationTargetException{
        String path = getClass().getResource("/testFiles/test.sdf")
        .getPath();

        List<IMolecule> mol = cdk.extractFromSDFile( new MockIFile(path), 0, 1 );
        Assert.assertEquals( 2,mol.size() );
    }

    @Test
    public void testGetFormat() {
        Assert.assertEquals(
                MDLV2000Format.getInstance(),
                cdk.getFormat("MDLV2000Format")
        );
        Assert.assertEquals(
                Mol2Format.getInstance(),
                cdk.getFormat("Mol2Format")
        );
        Assert.assertEquals(
                CMLFormat.getInstance(),
                cdk.getFormat("CMLFormat")
        );
        Assert.assertEquals(
                SDFFormat.getInstance(),
                cdk.getFormat("SDFFormat")
        );
    }

    @Test
    public void testGuessFormatFromExtension() {
        Assert.assertEquals(
                MDLV2000Format.getInstance(),
                cdk.guessFormatFromExtension("file.mol")
        );
        Assert.assertEquals(
                Mol2Format.getInstance(),
                cdk.guessFormatFromExtension("file.mol2")
        );
        Assert.assertEquals(
                CMLFormat.getInstance(),
                cdk.guessFormatFromExtension("file.cml")
        );
        Assert.assertEquals(
                SDFFormat.getInstance(),
                cdk.guessFormatFromExtension("file.sdf")
        );
    }

    @Test
    public void testGetFormats() {
        String formats = cdk.getFormats();
        Assert.assertTrue(formats.contains("Mol2Format"));
        Assert.assertTrue(formats.contains("CMLFormat"));
        Assert.assertTrue(formats.contains("MDLV2000Format"));
        Assert.assertTrue(formats.contains("SDFFormat"));
    }
}
