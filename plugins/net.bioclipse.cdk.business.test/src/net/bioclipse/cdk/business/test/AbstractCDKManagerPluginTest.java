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
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import net.bioclipse.cdk.business.CDKManagerHelper;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.ICDKReaction;
import net.bioclipse.cdk.domain.MoleculesInfo;
import net.bioclipse.cdkdebug.business.ICDKDebugManager;
import net.bioclipse.core.MockIFile;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.jobs.BioclipseUIJob;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openscience.cdk.CDKConstants;
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
import org.openscience.cdk.io.formats.MDLRXNFormat;
import org.openscience.cdk.io.formats.MDLV2000Format;
import org.openscience.cdk.io.formats.Mol2Format;
import org.openscience.cdk.io.formats.SDFFormat;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.templates.MoleculeFactory;
import org.openscience.cdk.tools.diff.AtomContainerDiff;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public abstract class AbstractCDKManagerPluginTest {

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
    
    protected static ICDKManager      cdk;
    protected static ICDKDebugManager debug;
    
    @Test
    public void testLoadMoleculeFromCMLFile() throws IOException, 
                                                     BioclipseException, 
                                                     CoreException, 
                                                     URISyntaxException {

        URI uri = getClass().getResource("/testFiles/0037.cml").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path = url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);

        System.out.println("mol: " + mol.toString());
        assertNotNull(mol);
        Assert.assertNotSame(0, mol.getAtomContainer().getAtomCount());
        Assert.assertNotSame(0, mol.getAtomContainer().getBondCount());
    }

    @Test
    public void testLoadMolecules() throws IOException,
                                           BioclipseException,
                                           CoreException, 
                                           URISyntaxException {

        URI uri = getClass().getResource("/testFiles/dbsmallconf.sdf").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        List<ICDKMolecule> mols = cdk.loadMolecules(path);

        assertNotNull(mols);
        Assert.assertNotSame(0, mols.size());
        for (ICDKMolecule mol : mols) {
            Assert.assertNotNull(mol);
            Assert.assertNotSame(0, mol.getAtomContainer().getAtomCount());
            Assert.assertEquals( "dbsmallconf.sdf", 
                                 mol.getResource().getName() );
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
        	System.out.println("Mol: " + mol.getName() + " SMILES: " +
        	    mol.getSMILES(
                    net.bioclipse.core.domain.IMolecule
                        .Property.USE_CACHED_OR_CALCULATED
                ));
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
        
        
        List<ICDKMolecule> molecules = cdk.loadSMILESFile(file);
        Assert.assertNotNull( molecules );
        List<String> inputList = new ArrayList<String>(Arrays.asList( input ));

        for(ICDKMolecule molecule:molecules) {
            String smiles = molecule.getSMILES(
                net.bioclipse.core.domain.IMolecule
                    .Property.USE_CACHED_OR_CALCULATED
            );
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
        
        List<ICDKMolecule> lst = cdk.loadMolecules( path);
        assertNotNull(lst);
        assertEquals( 1, lst.size() );
        ICDKMolecule mol2 = lst.get( 0 );
        
        assertEquals( mol2.getInChI( IMolecule.Property.USE_CACHED_OR_CALCULATED ), 
                      mol.getInChI( IMolecule.Property.USE_CACHED_OR_CALCULATED ) );

        List<ICDKMolecule> lst2 = cdk.loadMolecules( new MockIFile(path));
        assertNotNull(lst2);
        assertEquals( 1, lst2.size() );
        ICDKMolecule mol3 = lst2.get( 0 );
        
        assertEquals( mol3.getInChI( IMolecule.Property.USE_CACHED_OR_CALCULATED ), 
                      mol.getInChI( IMolecule.Property.USE_CACHED_OR_CALCULATED ) );

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
        
        String smiles = mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        );

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
        final String indoleSmiles  = new SmilesGenerator().createSMILES(
        	MoleculeFactory.makeIndole()
        );
        ICDKMolecule cdkm = cdk.create(new MockMolecule(indoleSmiles));
        assertEquals(indoleSmiles,
            cdkm.getSMILES(
                net.bioclipse.core.domain.IMolecule
                    .Property.USE_CACHED_OR_CALCULATED
            )
         );
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
    public void testSMARTSMatching() throws BioclipseException {
        String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSMILES( propaneSmiles  );
        
        assertTrue( cdk.smartsMatches(propane, propaneSmiles) );
    }
    
    @Test
    public void testSMARTSonFile() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/0037.cml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);
        assertNotNull(mol);
        
        String SMARTS="[N](*(*(O)))";
        assertTrue( cdk.isValidSmarts( SMARTS ) );
        assertTrue( cdk.smartsMatches( mol, SMARTS ) );
        
        List<IAtomContainer> aclist = cdk.getSmartsMatches( mol, SMARTS );
        assertNotNull("Smarts matching returned NULL", aclist );
        assertTrue( aclist.size()==1 );
        
        IAtomContainer ac=aclist.get( 0 );
        assertEquals( 4, ac.getAtomCount() );

        //Test an invalid SMARTS
        SMARTS="[RTE](*( QQ*(O )))";
        assertTrue( !cdk.isValidSmarts( SMARTS ) );

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
        cdk.saveMolecule(mol, "/Virtual/atp" + mol.hashCode() + ".mol");
    }

    @Test
    public void testSaveAsMDLV2000() throws Exception {
        URI uri = getClass().getResource("/testFiles/atp.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol2 = cdk.loadMolecule(path);
        assertNotNull(mol2);

        //Save mol to other location (virtual) with extension specified
        String atp2Path = "/Virtual/atp" + mol2.hashCode() + ".mol";
        cdk.saveMolecule(mol2, atp2Path,
            (IChemFormat)MDLV2000Format.getInstance());
        mol2 = cdk.loadMolecule(atp2Path);
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
        String atp2Path = "/Virtual/atp" + mol2.hashCode() + ".mol";
        cdk.saveMolecule(mol2, atp2Path,
            (IChemFormat)CMLFormat.getInstance());
        mol2 = cdk.loadMolecule(atp2Path);
        assertNotNull(mol2);
    }

    @Test public void testSaveMolecule_IMolecule() throws Exception {
        // set up a file to load, edit and then save
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        String path = "/Virtual/testSaveMolecule" +
        	propane.hashCode() + ".mol";
        cdk.saveMolecule(propane, path, false);
        ICDKMolecule mol = cdk.loadMolecule(path);

        // try overwrite
        ICDKMolecule coc  = cdk.fromSMILES("COC");
        coc.setResource(mol.getResource());
        cdk.saveMolecule(coc,true);
        mol = cdk.loadMolecule(path);
        assertTrue("O(C)C".equals(mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        )) ||
                   "COC".equals(mol.getSMILES(
                       net.bioclipse.core.domain.IMolecule
                           .Property.USE_CACHED_OR_CALCULATED
                   )));
    }

    @Test public void testSaveMolecule_IMolecule_boolean() throws Exception {
        // set up a file to load, edit and then save
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        String path = "/Virtual/testSaveMolecule" + propane.hashCode() + ".mol";
        cdk.saveMolecule(propane, path, false);
        ICDKMolecule mol = cdk.loadMolecule(path);

        // try overwrite
        ICDKMolecule coc  = cdk.fromSMILES("COC");
        coc.setResource(mol.getResource());
        cdk.saveMolecule(coc, true);
        mol = cdk.loadMolecule(path);
        assertTrue("O(C)C".equals(mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        )) ||
                "COC".equals(mol.getSMILES(
                    net.bioclipse.core.domain.IMolecule
                        .Property.USE_CACHED_OR_CALCULATED
                )));
    }

    @Test public void testSaveMolecule_IMolecule_String() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        String path = "/Virtual/testSaveMolecule" + propane.hashCode() + ".mol";
        cdk.saveMolecule(propane, path, false);
        ICDKMolecule mol = cdk.loadMolecule(path);
        assertEquals("CCC", mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        ));
    }

    @Test public void testSaveMolecule_IMolecule_String_boolean() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        String path = "/Virtual/testSaveMolecule" + propane.hashCode() + ".mol";
        cdk.saveMolecule(propane, path, false);
        ICDKMolecule mol = cdk.loadMolecule(path);
        assertEquals("CCC", mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        ));

        // try overwrite
        ICDKMolecule coc  = cdk.fromSMILES("COC");
        coc.setResource(mol.getResource());
        cdk.saveMolecule(coc, path, true);
        mol = cdk.loadMolecule(path);
        assertTrue("O(C)C".equals(mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        )) ||
                "COC".equals(mol.getSMILES(
                    net.bioclipse.core.domain.IMolecule
                        .Property.USE_CACHED_OR_CALCULATED
                )));
    }

    @Test public void testSaveMolecule_IMolecule_IFile_boolean() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        String path = "/Virtual/testSaveMolecule" + propane.hashCode() + ".mol";
        IFile target = ResourcePathTransformer.getInstance()
            .transform(path);
        cdk.saveMolecule(propane, target, false);
        ICDKMolecule mol = cdk.loadMolecule(path);
        assertEquals("CCC", mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        ));

        // try overwrite
        ICDKMolecule coc  = cdk.fromSMILES("COC");
        coc.setResource(mol.getResource());
        cdk.saveMolecule(coc, target, true);
        mol = cdk.loadMolecule(path);
        assertTrue("O(C)C".equals(mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        )) ||
                   "COC".equals(mol.getSMILES(
                       net.bioclipse.core.domain.IMolecule
                           .Property.USE_CACHED_OR_CALCULATED
                   )));
    }

    @Test(expected=java.lang.Exception.class)
    public void testSaveMolecule_IMolecule_IFile_boolean_overwrite() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        String path = "/Virtual/testSaveMolecule" + propane.hashCode() + ".mol";
        IFile target = ResourcePathTransformer.getInstance()
            .transform(path);
        cdk.saveMolecule(propane, target, false);
        ICDKMolecule mol = cdk.loadMolecule(path);
        assertEquals("CCC", mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        ));

        // try overwrite
        ICDKMolecule coc  = cdk.fromSMILES("COC");
        target = ResourcePathTransformer.getInstance()
            .transform(path);
        cdk.saveMolecule(coc, target, false);
        mol = cdk.loadMolecule(path);
        assertTrue("O(C)C".equals(mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        )) ||
                "COC".equals(mol.getSMILES(
                    net.bioclipse.core.domain.IMolecule
                        .Property.USE_CACHED_OR_CALCULATED
                )));
    }

    @Test
    public void testSaveMolecule_IMolecule_String_String() throws BioclipseException, CDKException, CoreException, IOException {
        String propaneSmiles = "CCC"; 
        
        ICDKMolecule propane  = cdk.fromSMILES( propaneSmiles  );
        String path = "/Virtual/testSaveMolecule" + propane.hashCode() + ".mol";
        cdk.saveMolecule(propane, path, (IChemFormat)MDLV2000Format.getInstance());
        ICDKMolecule mol = cdk.loadMolecule(path);
        assertEquals("CCC", mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        ));
    }
    
    @Test
    public void testSaveMoleculesSDF() throws BioclipseException, CDKException, CoreException, IOException {

        System.out.println("*************************");
        System.out.println("testSaveMoleculesSDF()");

        IMolecule mol1 = new MockMolecule("CCC");
        IMolecule mol2 = new MockMolecule("C1CCCCC1CCO");
        
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

        IMolecule mol1 = new MockMolecule("CCC");
        IMolecule mol2 = new MockMolecule("C1CCCCC1CCO");
        
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
    	
    	ICDKMolecule mol2 = debug.perceiveSybylAtomTypes(mol);
    	
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
    	
    	ICDKMolecule mol2 = debug.perceiveSybylAtomTypes(mol);
    	
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
    	
    	ICDKMolecule mol2 = debug.perceiveSybylAtomTypes(mol);
    	
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
        String path = "/Virtual/testDetermineFormat" + propane.hashCode() + ".mol";
        cdk.saveMDLMolfile(propane, path);
        String format = cdk.determineFormat(path);
        assertEquals(MDLV2000Format.getInstance().getFormatName(), format);
    }

    @Test public void testDetermineFormat_IContentType() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        String path = "/Virtual/testBug607." + propane.hashCode() + ".mol";
        cdk.saveMDLMolfile(propane, path);

        propane = cdk.loadMolecule(path);
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
        String path = "/Virtual/testSaveMDLMolefile" + propane.hashCode() + ".mol";
        cdk.saveMDLMolfile(propane, path);
        ICDKMolecule mol = cdk.loadMolecule(path);
        assertEquals("CCC", mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        ));
    }

    @Test
    public void testSaveCML() throws Exception {
        ICDKMolecule propane  = cdk.fromSMILES("CCC");
        String path = "/Virtual/testSaveCMLfile" + propane.hashCode() + ".mol";
        cdk.saveCML(propane, path);
        ICDKMolecule mol = cdk.loadMolecule(path);
        assertEquals("CCC", mol.getSMILES(
            net.bioclipse.core.domain.IMolecule
                .Property.USE_CACHED_OR_CALCULATED
        ));
    }

    @Test
    public void testNumberOfEntriesInSDFString() throws Exception {
        
        URI uri = getClass().getResource("/testFiles/test.sdf").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path = url.getFile();
        
        assertEquals( "There should be two entries in the file",
            2,
            cdk.numberOfEntriesInSDF(path)
        );
    }
    
    @Test
    @Ignore("Have yet to find a good way to test this. " +
    		    "JUnit runs the test in the ui thread so can't see that " +
    		    "something else gets run in it during the test...")
    public void testNumberOfEntriesInSDFIFileUIJob() throws Exception {
        
        URI uri = getClass().getResource("/testFiles/test.sdf").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        
        final BioclipseUIJob<Integer> uiJob = new BioclipseUIJob<Integer>() {
            @Override
            public void runInUI() {
            }
        };

        cdk.numberOfEntriesInSDF( ResourcePathTransformer.getInstance()
                                                         .transform(path), 
                                  uiJob );
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
        String path = "/Virtual/testFFF" + mol.hashCode() + ".sdf";
        cdk.saveSDFile(path, mol);
        byte[] bytes=new byte[1000];
        IFile file= ResourcePathTransformer.getInstance()
           .transform(path);
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
        String path = "/Virtual/testPropateProps" + mol.hashCode() + ".sdf";
        cdk.saveSDFile(path, mols);
        byte[] bytes=new byte[1000];
        IFile file= ResourcePathTransformer.getInstance()
           .transform(path);
        file.getContents().read(bytes);
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<bytes.length;i++){
            sb.append((char)bytes[i]);
        }
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
    public void testMolecularFormulaCharged() throws BioclipseException {
        ICDKMolecule m = cdk.fromSMILES( "[O-]" );
        assertEquals( "[HO]-", cdk.molecularFormula(m) );
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
        cdk.saveMolecule(mol,"/Virtual/bug583." + mol.hashCode() + ".cml", true);
    }

    @Test @Ignore("See bug #613")
    public  void testGenerate2D() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        Assert.assertFalse( cdk.has2d( mol ));

        //Prop for Atom 0
        mol.getAtomContainer().getAtom( 0 ).getProperties().put( "wee", "how" );
        Assert.assertEquals( "how", mol.getAtomContainer().getAtom( 0 )
                             .getProperties().get( "wee" ) );

        //Prop for AtomContainer
        mol.getAtomContainer().getProperties().put( "wee", "how" );
        Assert.assertEquals( "how", mol.getAtomContainer()
                             .getProperties().get( "wee" ) );


        ICDKMolecule mol2 = (ICDKMolecule)cdk.generate2dCoordinates( mol );
        Assert.assertTrue( cdk.has2d( mol2 ));

        //Make sure Atom properties are copied to new molecule
        Assert.assertEquals("Atom property lost on generate 2D", 
                            "how", mol2.getAtomContainer().getAtom( 0 )
                             .getProperties().get( "wee" ) );

        //Make sure AC properties are copied to new molecule
        Assert.assertEquals("AtomContainer property lost on generate 2D", 
                            "how", mol2.getAtomContainer()
                             .getProperties().get( "wee" ) );
}
    @Test
    public  void testStoresResource_Save_IMolecule_String_Boolean() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        String path = "/Virtual/testResource" + mol.hashCode() + ".cml";
        cdk.saveMolecule(mol, path, true);
        Assert.assertNotNull(mol.getResource());
    }

    @Test
    public  void testStoresResource_Save_IMolecule_String() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        String path = "/Virtual/testResource" + mol.hashCode() + ".cml";
        cdk.saveMolecule(mol, path);
        Assert.assertNotNull(mol.getResource());
    }

    @Test
    public  void testStoresResource_Save_IMolecule_IChemFormat()
    throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        String path = "/Virtual/testResource" + mol.hashCode() + ".cml";
        cdk.saveMolecule(mol, path,
                (IChemFormat)CMLFormat.getInstance());
        Assert.assertNotNull(mol.getResource());
    }

    @Test
    public  void testStoresResource_Save_IMolecule_IChemFormat_Boolean()
    throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        String path = "/Virtual/testResource" + mol.hashCode() + ".cml";
        cdk.saveMolecule(mol, path,
                (IChemFormat)CMLFormat.getInstance(), true);
        Assert.assertNotNull(mol.getResource());
    }

    @Test
    public  void testStoresResource_ContentType()
    throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        String path = "/Virtual/testResource" + mol.hashCode() + ".cml";
        cdk.saveMolecule(mol, path,
                (IChemFormat)CMLFormat.getInstance(), true);
        Assert.assertNotNull(mol.getResource());
        Assert.assertTrue(mol.getResource() instanceof IFile);
        IFile ifile = (IFile)mol.getResource();
        Assert.assertNotNull(ifile.getContentDescription());
    }

    @Test
    public  void testContentType_onLoad() throws Exception {
    	String path = "/Virtual/testResource" + Math.random() + ".cml";
        cdk.saveMolecule(
            cdk.fromSMILES("CC"),
            path,
            (IChemFormat)CMLFormat.getInstance(),
            true
        );

        ICDKMolecule mol = cdk.loadMolecule(path);
        Assert.assertNotNull(mol.getResource());
        Assert.assertTrue(mol.getResource() instanceof IFile);
        IFile ifile = (IFile)mol.getResource();
        Assert.assertNotNull(ifile.getContentDescription());
    }
    
    
    @Test public void testBug826() throws Exception{
        URI uri = getClass().getResource("/testFiles/polycarpol.mdl").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ICDKMolecule mol = cdk.loadMolecule( path);
        final String FILENAME = "/Virtual/testResource" + Math.random() + ".cml";
        cdk.saveMolecule( mol, FILENAME,true );
        IFile newfile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(FILENAME));
        byte[] b=new byte[100];
        newfile.getContents().read( b );
        Assert.assertEquals( 'p', (char)b[0]);
        Assert.assertEquals( 'o', (char)b[1]);
        Assert.assertEquals( 'l', (char)b[2]);
        Assert.assertEquals( 'y', (char)b[3]);
        Assert.assertEquals( 'c', (char)b[4]);
        Assert.assertEquals( 'a', (char)b[5]);
        Assert.assertEquals( 'r', (char)b[6]);
        Assert.assertEquals( 'p', (char)b[7]);
        Assert.assertEquals( 'o', (char)b[8]);
        Assert.assertEquals( 'l', (char)b[9]);
        Assert.assertEquals( '.', (char)b[10]);
        Assert.assertEquals( 'm', (char)b[11]);
        Assert.assertEquals( 'd', (char)b[12]);
        Assert.assertEquals( 'l', (char)b[13]);
    }

    @Test public void testGetSetProperty() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        Assert.assertNull(cdk.setProperty(mol, "foo", "bar"));
        Assert.assertEquals("bar", cdk.getProperty(mol, "foo"));
    }

    @Test public void testRemoveImplicitHydrogens() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        cdk.removeImplicitHydrogens(mol);
        for (IAtom atom : mol.getAtomContainer().atoms()) {
            Assert.assertEquals(0, atom.getHydrogenCount().intValue());
        }
    }

    @Test public void testRemoveExplicitHydrogens() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("[H]C([H])([H])[H]");
        Assert.assertEquals(5, mol.getAtomContainer().getAtomCount());
        cdk.removeExplicitHydrogens(mol);
        Assert.assertEquals(1, mol.getAtomContainer().getAtomCount());
    }

    @Test public void testRoundtripFingerprintViaSDF() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("C");
        BitSet molBS = mol.getFingerprint(net.bioclipse.core.domain
            .IMolecule.Property .USE_CALCULATED);

        List<IMolecule> mols=new ArrayList<IMolecule>();
        mols.add(mol);

        IFile target = new MockIFile();
        cdk.saveMolecules(mols, target, (IChemFormat)SDFFormat.getInstance());

        List<ICDKMolecule> readmols = cdk.loadMolecules(target);
        Assert.assertEquals(1, readmols.size());
        ICDKMolecule mol2 = readmols.get(0);
        Assert.assertNotNull(mol2);
        BitSet mol2BS = mol2.getFingerprint(net.bioclipse.core.domain
           .IMolecule.Property.USE_CACHED_OR_CALCULATED);

        Assert.assertEquals(molBS.toString(), mol2BS.toString());
    }

    @Test public void testRoundtripFingerprintViaCML() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("C");
        BitSet molBS = mol.getFingerprint(net.bioclipse.core.domain
                .IMolecule.Property.USE_CALCULATED);

        IFile target = new MockIFile();
        cdk.saveMolecule(mol, target, (IChemFormat)CMLFormat.getInstance());

        ICDKMolecule mol2 = cdk.loadMolecule(target);
        Assert.assertNotNull(mol2);
        BitSet mol2BS = mol2.getFingerprint(net.bioclipse.core.domain
                .IMolecule.Property.USE_CACHED_OR_CALCULATED);

        Assert.assertEquals(molBS.toString(), mol2BS.toString());
    }

    private final static class MockMolecule implements IMolecule {
		private final String smiles;

		private MockMolecule(String smiles) {
			this.smiles = smiles;
		}

		public String getSMILES(IMolecule.Property urgency)
		    throws BioclipseException {
			return smiles;
		}

		// we are not using the rest, and will therefore not implement them
		public String getCML() throws BioclipseException { return null; }

		public List<IMolecule> getConformers() { return null; }

		public IResource getResource() { return null; }

		public String getUID() { return null; }

		public void setResource(IResource resource) {}

		public Object getAdapter(Class adapter) { return null; }
	}
    
    @Test public void testAddExplicitHydrogens() throws Exception {
        ICDKMolecule molecule = cdk.fromSMILES("C");
        assertEquals(1, molecule.getAtomContainer().getAtomCount());
        cdk.addExplicitHydrogens(molecule);
        assertEquals(5, molecule.getAtomContainer().getAtomCount());
        assertEquals(0, molecule.getAtomContainer().getAtom(0).getHydrogenCount());
    }

    @Test public void testBug691() throws Exception {
        ICDKMolecule molecule = cdk.fromSMILES("C(C1C(C(C(C(O1)O)O)O)O)O");
        assertEquals(12, molecule.getAtomContainer().getAtomCount());
        cdk.addExplicitHydrogens(molecule);
        assertEquals(24, molecule.getAtomContainer().getAtomCount());
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
        List<IMolecule> molecule = new ArrayList<IMolecule>();
        molecule.add( cdk.fromSMILES("CCC"));
        molecule.add( cdk.fromSMILES("C1CCCCC1"));
        assertEquals(3, ((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtomCount());
        assertEquals(6, ((ICDKMolecule)molecule.get( 1 )).getAtomContainer().getAtomCount());
        Assert.assertNull(((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom(0).getPoint3d());
        Assert.assertNull(((ICDKMolecule)molecule.get( 1 )).getAtomContainer().getAtom(0).getPoint3d());
        ((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom( 0 ).setPoint2d( new Point2d(0,0) );
        ((ICDKMolecule)molecule.get( 1 )).getAtomContainer().getAtom( 0 ).setPoint2d( new Point2d(0,0) );
        cdk.generate3dCoordinates(molecule);
        assertNotNull(((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom(0).getPoint3d());
        assertNotNull(((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom(0).getPoint2d());
        assertNotNull(((ICDKMolecule)molecule.get( 1 )).getAtomContainer().getAtom(0).getPoint3d());
        assertNotNull(((ICDKMolecule)molecule.get( 1 )).getAtomContainer().getAtom(0).getPoint2d());
    }

    @Test public void testGenerate3DCoordinatesSingle() throws Exception {
        List<IMolecule> molecule = new ArrayList<IMolecule>();
        molecule.add( cdk.fromSMILES("CCC"));
        assertEquals(3, ((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtomCount());
        Assert.assertNull(((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom(0).getPoint3d());
        //2d coords should stay, we test that
        ((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom( 0 ).setPoint2d( new Point2d(0,0) );
        cdk.generate3dCoordinates(molecule);
        assertNotNull(((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom(0).getPoint3d());
        assertNotNull(((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom(0).getPoint2d());
    }

    @Test public void testGenerate2DCoordinatesSingle() throws Exception {
        List<IMolecule> molecule = new ArrayList<IMolecule>();
        molecule.add(cdk.fromSMILES("CCCBr"));
        assertEquals(4, ((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtomCount());
        Assert.assertNull(((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom(0).getPoint2d());
        //3d coords should stay, we test that.
        ((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom( 0 ).setPoint3d( new Point3d(0,0,0) );
        List<ICDKMolecule> cdkMolecule = cdk.generate2dCoordinates(molecule);
        Assert.assertTrue(cdkMolecule.get(0) instanceof ICDKMolecule);
        assertNotNull(((ICDKMolecule)cdkMolecule.get(0)).getAtomContainer().getAtom(0).getPoint2d());
        assertNotNull(((ICDKMolecule)cdkMolecule.get(0)).getAtomContainer().getAtom(0).getPoint3d());
    }

    
    @Test public void testGenerate2DCoordinates() throws Exception {
        List<IMolecule> molecule = new ArrayList<IMolecule>();
        molecule.add(cdk.fromSMILES("CCCBr"));
        molecule.add( cdk.fromSMILES("C1CCCCC1"));
        assertEquals(4, ((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtomCount());
        assertEquals(6, ((ICDKMolecule)molecule.get( 1 )).getAtomContainer().getAtomCount());
        Assert.assertNull(((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom(0).getPoint2d());
        Assert.assertNull(((ICDKMolecule)molecule.get( 1 )).getAtomContainer().getAtom(0).getPoint2d());
        //3d coords should stay, we test that.
        ((ICDKMolecule)molecule.get( 0 )).getAtomContainer().getAtom( 0 ).setPoint3d( new Point3d(0,0,0) );
        ((ICDKMolecule)molecule.get( 1 )).getAtomContainer().getAtom( 0 ).setPoint3d( new Point3d(0,0,0) );
        List<ICDKMolecule> cdkMolecule = cdk.generate2dCoordinates(molecule);
        Assert.assertTrue(cdkMolecule.get(0) instanceof ICDKMolecule);
        assertNotNull(((ICDKMolecule)cdkMolecule.get(0)).getAtomContainer().getAtom(0).getPoint2d());
        assertNotNull(((ICDKMolecule)cdkMolecule.get(0)).getAtomContainer().getAtom(0).getPoint3d());
        assertNotNull(((ICDKMolecule)cdkMolecule.get(1)).getAtomContainer().getAtom(0).getPoint2d());
        assertNotNull(((ICDKMolecule)cdkMolecule.get(1)).getAtomContainer().getAtom(0).getPoint3d());
    }

    @Test 
    public void testPerceiveAromaticity() throws Exception{
        URI uri = getClass().getResource("/testFiles/aromatic.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        MockIFile mf=new MockIFile(path);
        ICDKMolecule mol = cdk.loadMolecule(mf);
        Assert.assertFalse( mol.getAtomContainer().getAtom( 6 ).getFlag( CDKConstants.ISAROMATIC ) );
        ICDKMolecule molwitharomaticity = (ICDKMolecule)cdk.perceiveAromaticity( mol );
        Assert.assertTrue( molwitharomaticity.getAtomContainer().getAtom( 6 ).getFlag( CDKConstants.ISAROMATIC ) );
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
                MDLV2000Format.getInstance(),
                cdk.guessFormatFromExtension("file.mdl")
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

    @Test public void testPartition() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("O=C(CC)[O-].[Na+]");
        List<IAtomContainer> fragments = cdk.partition(mol);
        Assert.assertEquals(2, fragments.size());
    }

    @Test public void testIsConnected() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("O=C(CC)[O-].[Na+]");
        Assert.assertFalse(cdk.isConnected(mol));
    }

    @Test public void testIsConnected2() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("O=C(CC)O");
        Assert.assertTrue(cdk.isConnected(mol));
    }

    @Test public void testTotalFormalCharge() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("O=C(CC)[O-].[Na+]");
        Assert.assertEquals(0, cdk.totalFormalCharge(mol));

        mol = cdk.fromSMILES("O=C(CC)[O-]");
        Assert.assertEquals(-1, cdk.totalFormalCharge(mol));

        mol = cdk.fromSMILES("O=C(CC(=O)[O-])[O-]");
        Assert.assertEquals(-2, cdk.totalFormalCharge(mol));
    }

    @Test public void testSingleTanimoto() throws Exception {
        URI uri = getClass().getResource("/testFiles/aromatic.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        MockIFile mf=new MockIFile(path);
        ICDKMolecule mol = cdk.loadMolecule(mf);
        float similarity = cdk.calculateTanimoto( mol,mol );
        Assert.assertEquals( 1, similarity, 0.0001 );
        uri = getClass().getResource("/testFiles/atp.mol").toURI();
        url = FileLocator.toFileURL(uri.toURL());
        path = url.getFile();
        mf=new MockIFile(path);
        ICDKMolecule mol2 = cdk.loadMolecule(mf);
        float similarity2 = cdk.calculateTanimoto( mol,mol2 );
        Assert.assertEquals( 0.1972, similarity2, 0.0001 );
    }

    @Test public void testMultipleTanimoto() throws Exception {
        List<Float> expected= new ArrayList<Float>();
        expected.add((float)1);
        expected.add((float)0.19720767);
        List<Float> actuals= new ArrayList<Float>();
        URI uri = getClass().getResource("/testFiles/aromatic.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        MockIFile mf=new MockIFile(path);
        ICDKMolecule mol = cdk.loadMolecule(mf);
        actuals.add(cdk.calculateTanimoto( mol,mol ));
        uri = getClass().getResource("/testFiles/atp.mol").toURI();
        url=FileLocator.toFileURL(uri.toURL());
        path=url.getFile();
        mf=new MockIFile(path);
        ICDKMolecule mol2 = cdk.loadMolecule(mf);
        actuals.add(cdk.calculateTanimoto( mol,mol2 ));
        Assert.assertEquals( expected, actuals );
    }

    @Test public void testGetMDLMolfileString() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("O=C(CC)[O-].[Na+]");

        String fileContent = cdk.getMDLMolfileString(mol);

        Assert.assertNotNull(fileContent);
        Assert.assertTrue(fileContent.contains("V2000"));
    }

    @Test public void testCalculateTanimoto_BitSet_BitSet() throws Exception {
        BitSet b1 = new BitSet(5); b1.set(5); b1.set(4);
        BitSet b3 = new BitSet(5); b3.set(3); b3.set(4);
        BitSet b4 = new BitSet(5); b4.set(3);
        Assert.assertEquals(1.0, cdk.calculateTanimoto(b1, b1), 0.0);
        Assert.assertEquals(0.0, cdk.calculateTanimoto(b1, b4), 0.0);
        Assert.assertNotSame(1.0, cdk.calculateTanimoto(b1, b3));
        Assert.assertNotSame(0.0, cdk.calculateTanimoto(b1, b3));
    }

    @Test public void testCalculateTanimoto_IMolecule_BitSet() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("CC");
        BitSet b3 = mol.getFingerprint(
            net.bioclipse.core.domain.IMolecule.Property.USE_CALCULATED);
        Assert.assertEquals(1.0, cdk.calculateTanimoto(mol, b3), 0.0);
    }

    @Test public void testLoadReaction_InputStream_IProgressMonitor_IChemFormat() throws Exception{
        URI uri = getClass().getResource("/testFiles/0002.stg01.rxn").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        IFile file = new MockIFile( path );
        ICDKReaction reaction = cdk.loadReactions( file.getContents(),
                (IChemFormat)MDLRXNFormat.getInstance()).get( 0 );

        Assert.assertNotNull(reaction);
        Assert.assertSame(1, reaction.getReaction().getReactantCount());
        Assert.assertSame(1, reaction.getReaction().getProductCount());

    }
    
    @Test public void testLoadReaction_IFile_IProgressMonitor() throws Exception{
        URI uri = getClass().getResource("/testFiles/reaction.1.cml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        IFile file = new MockIFile( path );
        ICDKReaction reaction = cdk.loadReactions( file ).get( 0 );

        Assert.assertNotNull(reaction);
        Assert.assertSame(1, reaction.getReaction().getReactantCount());
        Assert.assertSame(1, reaction.getReaction().getProductCount());
        
    }
}
