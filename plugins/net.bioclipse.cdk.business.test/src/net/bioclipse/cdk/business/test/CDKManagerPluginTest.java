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
import static org.junit.Assert.fail;

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

import net.bioclipse.cdk.business.CDKManagerHelper;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.MoleculesInfo;
import net.bioclipse.cdkdebug.business.ICDKDebugManager;
import net.bioclipse.core.MockIFile;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IMoleculeManager;
import net.bioclipse.core.business.MoleculeManager;
import net.bioclipse.core.domain.IMolecule;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.NoSuchAtomException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.Mol2Reader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.MDLV2000Format;
import org.openscience.cdk.io.formats.Mol2Format;
import org.openscience.cdk.io.formats.SDFFormat;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.templates.MoleculeFactory;
import org.openscience.cdk.tools.diff.AtomContainerDiff;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

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
    
    private static ICDKManager cdk;
    private static ICDKDebugManager cdkdebug;

    @BeforeClass public static void setupCDKManagerPluginTest() {
        // the next line is needed to ensure the OSGI loader properly start
        // the org.springframework.bundle.osgi.extender, so that the manager
        // can be loaded too. Otherwise, it will fail with a time out.
        net.bioclipse.ui.Activator.getDefault();

        try {
            cdk = net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
            cdkdebug= net.bioclipse.cdkdebug.Activator.getDefault().getManager();
        } catch (RuntimeException exception) {
            fail("Failed to instantiate the CDK managers.");
        }
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
    public void testLoadMolecules() throws IOException,
                                          BioclipseException,
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/dbsmallconf.sdf").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        List<ICDKMolecule> mols = cdk.loadMolecules(path);

        assertNotNull(mols);
        Assert.assertNotSame(0, mols.size());
        for (ICDKMolecule mol : mols) {
            Assert.assertNotNull(mol);
            Assert.assertNotSame(0, mol.getAtomContainer().getAtomCount());
            Assert.assertEquals( "dbsmallconf.sdf", mol.getResource().getName() );
        }
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
        Assert.assertEquals( "cs2a.cml", mol.getResource().getName() );

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
    public void testloadMoleculesFromSMILESCheck() throws Exception {
        String[] input = {"CC","CCC(CC)CC","CCC"};
        
        StringBuilder sb = new StringBuilder();
        for(String s: input) {
            sb.append( s );
            sb.append( "\n" );
        }
        
        IFile file = new MockIFile(
                           new ByteArrayInputStream(sb.toString().getBytes()))
                            .extension( "smi" );
        
        
        List<ICDKMolecule> molecules = cdk.loadSMILESFile(
                file, (IProgressMonitor)null
        );
        Assert.assertNotNull( molecules );
        List<String> inputList = new ArrayList<String>(Arrays.asList( input ));

        for(ICDKMolecule molecule:molecules) {
            String smiles = molecule.getSMILES();
            if(inputList.contains( smiles ))
                inputList.remove( smiles );
        }
        Assert.assertEquals( 0, inputList.size() );
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

    @Test @Ignore("Loading PDB with the CDK is not yet supported")
    public void testLoadPDB() throws IOException, 
                                            BioclipseException, 
                                            CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/1D66.pdb").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

        System.out.println("PDB mol: " + mol.toString());
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
        
        IFile target=new MockIFile();
        cdk.saveMolecule(propane, target, (IChemFormat)MDLV2000Format.getInstance());
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
    public void testResourceIsSet() throws Exception {
        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

        //Make sure resource is set
        IFile resourceFile=(IFile)mol.getResource();
        assertNotNull(resourceFile);
        assertTrue(resourceFile.getFullPath().toOSString().endsWith("atp.mol"));
    }

    @Test(expected=BioclipseException.class)
    public void testUnwantedOverWrite() throws Exception {
        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

        //Save to the molecules resource, should throw exception (file exists)
        cdk.saveMolecule(mol);
    }

    @Test
    public void testSaveMoleculeOverwrite() throws Exception {
        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);
        
        //Save to the molecules resource with overwrite =true
        cdk.saveMolecule(mol, true);
    }

    @Test(expected=BioclipseException.class)
    public void testUnwantedOverWrite2() throws Exception {
        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);
        
        //Save mol to same resource read from, should throw exc (file exists)
        cdk.saveMolecule(mol, mol.getResource().getLocation().toOSString());
    }

    @Test
    public void testSaveAsSameResource() throws Exception {
        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

        //Save mol to same resource read from with overwrite=true
        cdk.saveMolecule(mol, mol.getResource().getLocation().toOSString(), true);
    }

    @Test
    public void testSaveAsSameTypeDifferentFile() throws Exception {
        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

        //Save mol to other location (virtual) without specifying file extension
        cdk.saveMolecule(mol, "/Virtual/atp0.mol");
    }

    @Test
    public void testSaveAsMDLV2000() throws Exception {
        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol2 = cdk.loadMolecule(path);
        assertNotNull(mol2);

        //Save mol to other location (virtual) with extension specified
        cdk.saveMolecule(mol2, "/Virtual/atp2.mol", (IChemFormat)MDLV2000Format.getInstance());
        mol2 = cdk.loadMolecule("/Virtual/atp2.mol");
        assertNotNull(mol2);
    }

    @Test
    public void testSaveAsCML() throws Exception {
        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol2 = cdk.loadMolecule(path);
        assertNotNull(mol2);

        //Save as CML
        cdk.saveMolecule(mol2, "/Virtual/atp3.cml", (IChemFormat)CMLFormat.getInstance());
        mol2 = cdk.loadMolecule("/Virtual/atp3.cml");
        assertNotNull(mol2);
    }

    @Test public void testSaveMolecule_IMolecule() throws Exception {
        // set up a file to load, edit and then save
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        cdk.saveMolecule(propane, "/Virtual/testSaveMoleculeBBB.mol", false);
        ICDKMolecule mol = cdk.loadMolecule("/Virtual/testSaveMoleculeBBB.mol");

        // try overwrite
        ICDKMolecule coc  = cdk.fromSMILES("COC");
        coc.setResource(mol.getResource());
        cdk.saveMolecule(coc,true);
        mol = cdk.loadMolecule("/Virtual/testSaveMoleculeBBB.mol");
        assertTrue("O(C)C".equals(mol.getSMILES()) ||
                   "COC".equals(mol.getSMILES()));
    }

    @Test public void testSaveMolecule_IMolecule_boolean() throws Exception {
        // set up a file to load, edit and then save
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        cdk.saveMolecule(propane, "/Virtual/testSaveMoleculeBCD.mol", false);
        ICDKMolecule mol = cdk.loadMolecule("/Virtual/testSaveMoleculeBCD.mol");

        // try overwrite
        ICDKMolecule coc  = cdk.fromSMILES("COC");
        coc.setResource(mol.getResource());
        cdk.saveMolecule(coc, true);
        mol = cdk.loadMolecule("/Virtual/testSaveMoleculeBCD.mol");
        assertTrue("O(C)C".equals(mol.getSMILES()) ||
                "COC".equals(mol.getSMILES()));
    }

    @Test public void testSaveMolecule_IMolecule_String() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        cdk.saveMolecule(propane, "/Virtual/testSaveMoleculeAAA.mol", false);
        ICDKMolecule mol = cdk.loadMolecule("/Virtual/testSaveMoleculeAAA.mol");
        assertEquals("CCC", mol.getSMILES());
    }

    @Test public void testSaveMolecule_IMolecule_String_boolean() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        cdk.saveMolecule(propane, "/Virtual/testSaveMoleculeZZZ.mol", false);
        ICDKMolecule mol = cdk.loadMolecule("/Virtual/testSaveMoleculeZZZ.mol");
        assertEquals("CCC", mol.getSMILES());

        // try overwrite
        ICDKMolecule coc  = cdk.fromSMILES("COC");
        coc.setResource(mol.getResource());
        cdk.saveMolecule(coc, "/Virtual/testSaveMoleculeZZZ.mol", true);
        mol = cdk.loadMolecule("/Virtual/testSaveMoleculeZZZ.mol");
        assertTrue("O(C)C".equals(mol.getSMILES()) ||
                "COC".equals(mol.getSMILES()));
    }

    @Test public void testSaveMolecule_IMolecule_IFile_boolean() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        IFile target = ResourcePathTransformer.getInstance()
            .transform("/Virtual/testSaveMoleculeXXX.mol");
        cdk.saveMolecule(propane, target, false);
        ICDKMolecule mol = cdk.loadMolecule("/Virtual/testSaveMoleculeXXX.mol");
        assertEquals("CCC", mol.getSMILES());

        // try overwrite
        ICDKMolecule coc  = cdk.fromSMILES("COC");
        coc.setResource(mol.getResource());
        cdk.saveMolecule(coc, target, true);
        mol = cdk.loadMolecule("/Virtual/testSaveMoleculeXXX.mol");
        assertTrue("O(C)C".equals(mol.getSMILES()) ||
                   "COC".equals(mol.getSMILES()));
    }

    @Test(expected=java.lang.Exception.class)
    public void testSaveMolecule_IMolecule_IFile_boolean_overwrite() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        IFile target = ResourcePathTransformer.getInstance()
            .transform("/Virtual/testSaveMoleculeYYY.mol");
        cdk.saveMolecule(propane, target, false);
        ICDKMolecule mol = cdk.loadMolecule("/Virtual/testSaveMoleculeYYY.mol");
        assertEquals("CCC", mol.getSMILES());

        // try overwrite
        ICDKMolecule coc  = cdk.fromSMILES("COC");
        target = ResourcePathTransformer.getInstance()
            .transform("/Virtual/testSaveMoleculeYYY.mol");
        cdk.saveMolecule(coc, target, false);
        mol = cdk.loadMolecule("/Virtual/testSaveMoleculeYYY.mol");
        assertTrue("O(C)C".equals(mol.getSMILES()) ||
                "COC".equals(mol.getSMILES()));
    }

    @Test
    public void testSaveMolecule_IMolecule_String_String() throws BioclipseException, CDKException, CoreException, IOException {
        String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSMILES( propaneSmiles  );
        
        cdk.saveMolecule(propane, "/Virtual/testSaveMolecule.mol", (IChemFormat)MDLV2000Format.getInstance());
        ICDKMolecule mol = cdk.loadMolecule("/Virtual/testSaveMolecule.mol");
        assertEquals("CCC", mol.getSMILES());
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
        
        String longStringValue="[(42/593;0.11804384485666104), (3/5;1.0), (9/560;0.026785714285714284), (0/2;0.0), (0/1;0.0), (0/0;0.0), (0/0;0.0), (0/0;0.0), (0/0;0.0), (0/0;0.0), (0/0;0.0), (0/0;0.0), (2/80;0.04166666666666667), (35/492;0.11856368563685638), (29/466;0.10371959942775394), (49/484;0.16873278236914602), (2/810;0.00411522633744856), (2/810;0.00411522633744856), (39/111;0.5855855855855856), (35/109;0.5351681957186545), (1/260;0.006410256410256411), (1/192;0.008680555555555556), (41/101;0.6765676567656767), (35/95;0.6140350877192983), (1/340;0.004901960784313725), (1/340;0.004901960784313725), (1/175;0.009523809523809525), (0/0;0.0), (2/1066;0.0031269543464665416), (0/0;0.0), (4/261;0.02554278416347382)]        ";
        mol1.getAtomContainer().setProperty("wee", "how");
        mol2.getAtomContainer().setProperty("santa", longStringValue);
        
        
        List<IMolecule> mols=new ArrayList<IMolecule>();
        mols.add(mol1);
        mols.add(mol2);
        
        String virtualPath="/Virtual/testSaveMoleculesSDFtoTEMPwithProps.cml";
        cdk.saveMolecules(mols, virtualPath, (IChemFormat)SDFFormat.getInstance());
        
        //For debug output
        System.out.println("#############################################");
        IFile target=ResourcePathTransformer.getInstance().transform(virtualPath);
        assertNotNull(target);
        BufferedReader reader=new BufferedReader(new InputStreamReader(target.getContents()));

        String line=reader.readLine();
        while(line!=null){
        	System.out.println(line);
            line=reader.readLine();
        }
        System.out.println("#############################################");
        
        //So, load back again
        List<ICDKMolecule> readmols = cdk.loadMolecules(virtualPath);
        assertEquals(2, readmols.size());

    	System.out.println("** Reading back created SDFile: ");
        for (ICDKMolecule cdkmol : readmols){
        	System.out.println("  - SMILES: " + cdk.calculateSMILES(cdkmol));
        	if (cdkmol.getAtomContainer().getAtomCount()==3){
            	assertEquals("how", cdkmol.getAtomContainer().getProperty("wee"));
        	}else{
            	assertEquals(longStringValue, cdkmol.getAtomContainer().getProperty("santa"));
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
    public void testSaveMoleculesCMLtoTEMPwithProps() throws BioclipseException, CDKException, CoreException, IOException {

        System.out.println("*************************");
        System.out.println("testSaveMoleculesCMLwithProps()");

        ICDKMolecule mol1=cdk.fromSMILES("CCC");
        ICDKMolecule mol2=cdk.fromSMILES("C1CCCCC1CCO");
        
        mol1.getAtomContainer().setProperty("wee", "how");
        mol2.getAtomContainer().setProperty("santa", "claus");
        
        List<IMolecule> mols=new ArrayList<IMolecule>();
        mols.add(mol1);
        mols.add(mol2);

        String vitualPath="/Virtual/testSaveMoleculesCMLtoTEMPwithProps.cml";
        cdk.saveMolecules(mols, vitualPath, (IChemFormat)CMLFormat.getInstance());

        List<ICDKMolecule> readmols = cdk.loadMolecules(vitualPath);
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
    	
    	ICDKMolecule mol2 = cdkdebug.perceiveSybylAtomTypes(mol);
    	
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
    	
    	ICDKMolecule mol2 = cdkdebug.perceiveSybylAtomTypes(mol);
    	
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
    	
    	ICDKMolecule mol2 = cdkdebug.perceiveSybylAtomTypes(mol);
    	
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
    	
    	ICDKMolecule mol2 = cdkdebug.perceiveSybylAtomTypes(mol);
    	
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

    	ICDKMolecule mol2 = cdkdebug.perceiveSybylAtomTypes(mol);
    	
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

    
    @Test @Ignore("See bug #582")
    public void testSaveMol2() throws BioclipseException, CDKException, CoreException, IOException, NoSuchAtomException {

    	String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSMILES( propaneSmiles  );

        IFile target=new MockIFile();
        cdk.saveMolecule(propane, target, (IChemFormat)Mol2Format.getInstance());

        BufferedReader breader = new BufferedReader(
                new InputStreamReader(target.getContents()));
        String line = breader.readLine();
        while (line != null) {
            System.out.println(line);
            line = breader.readLine();
        }
        Mol2Reader reader = new Mol2Reader(target.getContents());
        IChemFile file = (IChemFile)reader.read(new ChemFile());
        assertNotNull(file);
        Assert.assertNotSame(0, ChemFileManipulator.getAtomCount(file));
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
        CDKManagerHelper.customizeReading(reader);

        //Read file
        try {
            chemFile=(IChemFile)reader.read(chemFile);
        } catch (CDKException e) {
        	fail(e.getMessage());
        }

    }

    @Test public void testDetermineFormat() throws Exception {
        URI uri = getClass().getResource("/testFiles/cs2a.cml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        String format = cdk.determineFormat(path);
        assertEquals(CMLFormat.getInstance().getFormatName(), format);
    }

    @Test public void testDetermineFormat_FromWSResource() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        cdk.saveMDLMolfile(propane, "/Virtual/testDetermineFormat.mol");
        String format = cdk.determineFormat("/Virtual/testDetermineFormat.mol");
        assertEquals(MDLV2000Format.getInstance().getFormatName(), format);
    }

    @Test public void testDetermineFormat_IContentType() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        cdk.saveMDLMolfile(propane, "/Virtual/testBug607.mol");

        propane = cdk.loadMolecule("/Virtual/testBug607.mol");
        Assert.assertNotNull(propane.getResource());
        Assert.assertTrue(propane.getResource() instanceof IFile);
        IFile resource = (IFile)propane.getResource();
        Assert.assertNotNull(resource.getContentDescription());
        IContentType type = resource.getContentDescription().getContentType();
        Assert.assertNotNull(type);
        IChemFormat format = cdk.determineFormat(type);
        Assert.assertNotNull(format);
        assertEquals(MDLV2000Format.getInstance(), format);
    }

    @Test
    public void testSaveMDLMolfile() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        cdk.saveMDLMolfile(propane, "/Virtual/testSaveMDLMolfile.mol");
        ICDKMolecule mol = cdk.loadMolecule("/Virtual/testSaveMDLMolfile.mol");
        assertEquals("CCC", mol.getSMILES());
    }

    @Test
    public void testSaveCML() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        cdk.saveCML(propane, "/Virtual/testSaveMDLMolfile.cml");
        ICDKMolecule mol = cdk.loadMolecule("/Virtual/testSaveMDLMolfile.cml");
        assertEquals("CCC", mol.getSMILES());
    }

    @Test
    public void testNumberOfEntriesInSDF() throws Exception {
        
        URI uri = getClass().getResource("/testFiles/test.sdf").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        
        assertEquals( "There should be two entries in the file",
            2,
            cdk.numberOfEntriesInSDF(
               ResourcePathTransformer.getInstance().transform(path),
               (IProgressMonitor)null
            )
        );
    }

    @Test public void testGetInfo() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CCCCl");
        cdk.saveMolecule((IMolecule)mol, "/Virtual/testGetInfo.mol",
                (IChemFormat)MDLV2000Format.getInstance(), true);

        MoleculesInfo info = cdk.getInfo("/Virtual/testGetInfo.mol");
        Assert.assertNotNull(info);
        Assert.assertEquals(1, info.getNoMols());
        Assert.assertEquals(0, info.getNoMols2d());
        Assert.assertEquals(0, info.getNoMols3d());
    }

    
    @Test
    public void testExtractFromSDFile_String_int_int() throws Exception{
        URI uri = getClass().getResource("/testFiles/test.sdf").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        
        List<IMolecule> mol = cdk.extractFromSDFile( path, 0, 1 );
        Assert.assertEquals( 2,mol.size() );
    }
    
    @Test 
    public void testCreateSDFile_String_IMoleculeArray() throws Exception{
        List<IMolecule> mol = new ArrayList<IMolecule>();
        mol.add(cdk.fromSMILES("CCCBr"));
        mol.add(cdk.fromSMILES("CCCCl"));
        cdk.createSDFile("/Virtual/testFFF.sdf", mol);
        byte[] bytes=new byte[1000];
        IFile file= ResourcePathTransformer.getInstance()
           .transform("/Virtual/testFFF.sdf");
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
    public void testCreateSDFile_PropagateAtomContainerProperties()
        throws Exception{
        List<IMolecule> mols = new ArrayList<IMolecule>();
        ICDKMolecule mol = cdk.fromSMILES("CCCBr");
        mol.getAtomContainer().setProperty("whoopsie", "daisy");
        mols.add(mol);
        cdk.createSDFile("/Virtual/testPropateProps.sdf", mols);
        byte[] bytes=new byte[1000];
        IFile file= ResourcePathTransformer.getInstance()
           .transform("/Virtual/testPropateProps.sdf");
        file.getContents().read(bytes);
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<bytes.length;i++){
            sb.append((char)bytes[i]);
        }
        System.out.println(sb.toString());
        assertTrue(sb.toString().contains("whoopsie"));
        assertTrue(sb.toString().contains("daisy"));
    }

    @Test
    public void testMolecularFormula() throws BioclipseException {
        ICDKMolecule m = cdk.fromSMILES( "C" );
        assertEquals( "CH4", cdk.molecularFormula(m) );
        m = cdk.fromSMILES( "C[H]" );
        assertEquals( "CH4", cdk.molecularFormula(m) );
    }

    @Test
    public void testNoMolecules_String() throws Exception{
        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        int nomols = cdk.getNoMolecules( path );
        assertEquals( 1, nomols);
    }

    @Test
    public void testNoMolecules_String_SMILESFile() throws Exception{
        URI uri = getClass().getResource("/testFiles/nprods.smi").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        int nomols = cdk.getNoMolecules( path );
        assertEquals(30, nomols);
    }

    @Test
    public  void testBug583() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        cdk.saveMolecule(mol,"/Virtual/bug583.cml", true);
    }

    @Test
    public  void testGenerate2D() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        Assert.assertFalse( cdk.has2d( mol ));

        mol.getAtomContainer().getAtom( 0 ).getProperties().put( "wee", "how" );
        Assert.assertEquals( "how", mol.getAtomContainer().getAtom( 0 )
                             .getProperties().get( "wee" ) );

        ICDKMolecule mol2 = (ICDKMolecule)cdk.generate2dCoordinates( mol );
        Assert.assertTrue( cdk.has2d( mol2 ));

        //Make sure properties are copied to new molecule
        Assert.assertEquals( "how", mol2.getAtomContainer().getAtom( 0 )
                             .getProperties().get( "wee" ) );
    }

    @Test
    public  void testStoresResource_Save_IMolecule_String_Boolean() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        cdk.saveMolecule(mol,"/Virtual/testResource1.cml", true);
        Assert.assertNotNull(mol.getResource());
    }

    @Test
    public  void testStoresResource_Save_IMolecule_String() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        cdk.saveMolecule(mol,"/Virtual/testResource2.cml");
        Assert.assertNotNull(mol.getResource());
    }

    @Test
    public  void testStoresResource_Save_IMolecule_IChemFormat()
    throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        cdk.saveMolecule(mol,"/Virtual/testResource3.cml",
                (IChemFormat)CMLFormat.getInstance());
        Assert.assertNotNull(mol.getResource());
    }

    @Test
    public  void testStoresResource_Save_IMolecule_IChemFormat_Boolean()
    throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        cdk.saveMolecule(mol,"/Virtual/testResource4.cml",
                (IChemFormat)CMLFormat.getInstance(), true);
        Assert.assertNotNull(mol.getResource());
    }

    @Test
    public  void testStoresResource_ContentType()
    throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        cdk.saveMolecule(mol,"/Virtual/testResource4.cml",
                (IChemFormat)CMLFormat.getInstance(), true);
        Assert.assertNotNull(mol.getResource());
        Assert.assertTrue(mol.getResource() instanceof IFile);
        IFile ifile = (IFile)mol.getResource();
        Assert.assertNotNull(ifile.getContentDescription());
    }

    @Test
    public  void testContentType_onLoad() throws Exception {
        final String FILENAME = "/Virtual/testResource7.cml";
        cdk.saveMolecule(
            cdk.fromSMILES("CC"),
            FILENAME,
            (IChemFormat)CMLFormat.getInstance(),
            true
        );

        ICDKMolecule mol = cdk.loadMolecule(FILENAME);
        Assert.assertNotNull(mol.getResource());
        Assert.assertTrue(mol.getResource() instanceof IFile);
        IFile ifile = (IFile)mol.getResource();
        Assert.assertNotNull(ifile.getContentDescription());
    }
}
