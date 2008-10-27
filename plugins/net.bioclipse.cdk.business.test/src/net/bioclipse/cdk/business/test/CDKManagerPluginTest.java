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
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.CDKManagerHelper;
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
import org.eclipse.core.runtime.FileLocator;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.templates.MoleculeFactory;
import org.openscience.cdk.tools.diff.AtomContainerDiff;

public class CDKManagerPluginTest {

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
    public CDKManagerPluginTest() {
        cdk = new CDKManager();
    }

    @Test
    public void testLoadMoleculeFromCMLFile() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/0037.cml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

        System.out.println("mol: " + mol.toString());
        assertNotNull(mol);
    }

    @Test
    public void testLoadCMLFromFile2() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/cs2a.cml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

        System.out.println("mol: " + mol.toString());
        assertNotNull(mol);
    }

    
    @Test
    public void testLoadMoleculeFromSMILESFileDirectly() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/nprods.smi").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        List<ICDKMolecule> mols = cdk.loadSMILESFile( path);

        assertNotNull( mols );
        System.out.println("SMILES file size: " + mols.size());
        assertEquals(30, mols.size());
    }

    @Test
    public void testLoadMoleculeFromSMILESFile() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/nprods.smi").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        List<ICDKMolecule> mols = cdk.loadSMILESFile( path);
        
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
        String[] input = {"CC","CCC(CC)C","CC"};
        
        StringBuilder sb = new StringBuilder();
        for(String s: input) {
            sb.append( s );
            sb.append( "\n" );
        }
        
        IFile file = new MockIFile(
                           new ByteArrayInputStream(sb.toString().getBytes()))
                            .extension( "smi" );
        
        
        try {
            List<ICDKMolecule> molecules = cdk.loadSMILESFile( file );
            Assert.assertNotNull( molecules );
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
                                     CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

        System.out.println("mol: " + mol.toString());
        assertNotNull(mol);
    }

    @Test
    public void testLoadPolycarpol() throws IOException, 
                                            BioclipseException, 
                                            CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/polycarpol.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

        System.out.println("mol: " + mol.toString());
        assertNotNull(mol);
    }

    @Test
    public void testCreateSMILES() throws BioclipseException, 
                                          IOException, 
                                          CoreException, URISyntaxException {

    	URI uri = getClass().getResource("/testFiles/0037.cml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);
        
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
                       URISyntaxException, MalformedURLException, IOException {

        URI uri = getClass().getResource("/testFiles/test.sdf").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        
        List<IMolecule> molecules = new ArrayList<IMolecule>();

        for ( Iterator<net.bioclipse.cdk.domain.ICDKMolecule> iterator
                    = cdk.createMoleculeIterator( path);

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
    public void testLoadConformers() throws BioclipseException, IOException, URISyntaxException {
        
        URI uri = getClass().getResource("/testFiles/dbsmallconf.sdf").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();

        List<ICDKMolecule> mols = cdk.loadConformers(path);
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
        cdk.save(chemmodel, target, ICDKManager.mol);
        byte[] bytes=new byte[6];
        target.getContents().read(bytes);
        Assert.assertArrayEquals(new byte[]{10,32,32,67,68,75}, bytes);
    }

    @Test
    public void testSaveMolecule() throws BioclipseException, CDKException, CoreException, IOException {
        String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSMILES( propaneSmiles  );
        
        IFile target=new MockIFile();
        cdk.saveMolecule(propane, target, ICDKManager.mol);
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
        
        //FIXME: needs porting to PLUGIN I/O from URL not IMockFile
        IFile target=new MockIFile();
        cdk.saveMolecules(mols, target, ICDKManager.sdf);

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
        
        //FIXME: needs porting to PLUGIN I/O from URL not IMockFile
        IFile target=new MockIFile();
        cdk.saveMolecules(mols, target, ICDKManager.sdf);

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
        
        //FIXME: needs porting to PLUGIN I/O from URL not IMockFile
        IFile target=new MockIFile();
        cdk.saveMolecules(mols, target, ICDKManager.cml);

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
        cdk.saveMolecules(mols, target, ICDKManager.cml);

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
    public void testSybylAtomTypePerceptionFromSMILES() throws FileNotFoundException, IOException, BioclipseException, CoreException, InvocationTargetException{

    	ICDKMolecule mol = cdk.fromSMILES("C1CCCCC1CCOC");
    	
    	ICDKMolecule mol2 = cdk.depictSybylAtomTypes(mol);
    	
    	for (int i=0; i<mol2.getAtomContainer().getAtomCount(); i++){
    		IAtom a=mol2.getAtomContainer().getAtom(i);
    		System.out.println("Atom: " + a.getSymbol() + i + ", type=" + a.getAtomTypeName());
    	}

    }

    @Test
    public void testSybylAtomTypePerception() throws FileNotFoundException, IOException, BioclipseException, CoreException, InvocationTargetException, URISyntaxException{

        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

    	System.out.println("mol: " + mol.toString());
    	
    	ICDKMolecule mol2 = cdk.depictSybylAtomTypes(mol);
    	
    	for (int i=0; i<mol2.getAtomContainer().getAtomCount(); i++){
    		IAtom a=mol2.getAtomContainer().getAtom(i);
    		System.out.println("Atom: " + a.getSymbol() + i + ", type=" + a.getAtomTypeName());
    	}

    }

    @Test
    public void testSybylAtomTypePerception2() throws FileNotFoundException, IOException, BioclipseException, CoreException, InvocationTargetException, URISyntaxException{

        URI uri = getClass().getResource("/testFiles/polycarpol.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

    	System.out.println("mol: " + mol.toString());
    	
    	ICDKMolecule mol2 = cdk.depictSybylAtomTypes(mol);
    	
    	for (int i=0; i<mol2.getAtomContainer().getAtomCount(); i++){
    		IAtom a=mol2.getAtomContainer().getAtom(i);
    		System.out.println("Atom: " + a.getSymbol() + i + ", type=" + a.getAtomTypeName());
    	}

    }
    
    @Test
    public void testSybylAtomTypePerception3() throws FileNotFoundException, IOException, BioclipseException, CoreException, InvocationTargetException, URISyntaxException{

        URI uri = getClass().getResource("/testFiles/aromatic.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

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
        
        ICDKMolecule propane  = cdk.fromSMILES( propaneSmiles  );

        IFile target=new MockIFile();
        cdk.saveMolecule(propane, target, ICDKManager.mol2);
    	
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
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/cs2a.cml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();

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
        CDKManagerHelper.customizeReading(reader, chemFile);

        //Read file
        try {
            chemFile=(IChemFile)reader.read(chemFile);
        } catch (CDKException e) {
        	e.printStackTrace();
        }

    }

}
