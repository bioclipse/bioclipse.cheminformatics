/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *
 ******************************************************************************/
package net.bioclipse.cdk.domain.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.List;

import net.bioclipse.cdk.MockIFile;
import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk10.business.CDK10Manager;
import net.bioclipse.cdk10.business.CDK10ManagerHelper;
import net.bioclipse.cdk10.business.CDK10Molecule;
import net.bioclipse.core.business.BioclipseException;

import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;

public class TestCDKMolecule {

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
    @Before
    public void initialize() {
        cdk = new CDKManager();
    }

    @Test
    public void testFingerprinter() throws IOException, 
                                           BioclipseException, 
                                           CoreException {
        String path = getClass().getResource("/testFiles/0037.cml")
                                .getPath();

        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );
        assertNotNull(mol);
        BitSet bs = mol.getFingerprint(false);
        assertNotNull(bs);
        System.out.println("FP: " + bs.toString());
    }

    @Test
    public void testGetCML() throws IOException, 
                                    BioclipseException, 
                                    CoreException {
        String path = getClass().getResource("/testFiles/0037.cml")
                                .getPath();

        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );
        assertNotNull(mol);
        String cmlString = mol.getCML();
        assertNotNull(cmlString);
        System.out.println("CML:\n" + cmlString);
    }

    @Test
    public void testGetSmiles() throws IOException, 
                                       BioclipseException, 
                                       CoreException {
        String path = getClass().getResource("/testFiles/0037.cml")
                                .getPath();

        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(path), null );
        assertNotNull(mol);
        String smiles = mol.getSmiles();
        assertNotNull(smiles);
        System.out.println("Smiles: " + smiles);
    }
    
    @Test
    public void testCreateFromString() throws IOException, 
                                              BioclipseException {
        InputStream cmlFile 
            = getClass().getResourceAsStream("/testFiles/0037.cml");
        byte[] buf = new byte[60000];
        int a = cmlFile.read( buf );
        System.out.println("Read: " + a + "bytes");
        String content = new String(buf);
        String cutcontent = content.substring( 0,a );
        System.out.println("Content: " + cutcontent.length());
        
        ICDKMolecule mol = cdk.fromCml( cutcontent );
        assertNotNull(mol);
        String smiles = mol.getSmiles();
        assertNotNull(smiles);
        System.out.println("Smiles: " + smiles);
    }

    /**
     * Test conversion of CDK10Molecule to CDKMolecule starting from SMILES
     * @throws IOException
     * @throws BioclipseException
     * @throws CDKException 
     */
    @Test
    public void testCreateFromImoleculeSmiles() 
                throws IOException, 
                       BioclipseException, 
                       CDKException {
        
        CDK10Manager cdk10=new CDK10Manager();
        CDKManager cdk=new CDKManager();

        CDK10Molecule cdk10mol = cdk10.createMoleculeFromSMILES(
            "CC1CCCC(C#N)N1C(CO[Si](C)(C)C)C2=CC=CC=C2" );
        
        ICDKMolecule convertedmol=cdk.create( cdk10mol );
        assertNotNull(convertedmol);
        String smiles=convertedmol.getSmiles();
        assertNotNull(smiles);
        System.out.println("Smiles: " + smiles);
        
        ICDKMolecule cdkmol = cdk.fromSmiles(
            "CC1CCCC(C#N)N1C(CO[Si](C)(C)C)C2=CC=CC=C2" );
        
        assertTrue( cdk.fingerPrintMatches( cdkmol, convertedmol ));

        //It seems we cannot test isomorphism since AC have different impl
        //FIXME: verify by egonw
        assertTrue( UniversalIsomorphismTester
          .isIsomorph( cdk10mol.getAtomContainer(), 
                       cdkmol.getAtomContainer()));        

        assertTrue( UniversalIsomorphismTester
                    .isIsomorph( cdk10mol.getAtomContainer(), 
                                 convertedmol.getAtomContainer()));        

        assertTrue( UniversalIsomorphismTester
                    .isIsomorph( cdkmol.getAtomContainer(), 
                                 convertedmol.getAtomContainer()));        

        //It seems that MassNumber is not created from Smiles?
        System.out.println("MassNumber cdk10: " + cdk10mol.getAtomContainer().getAtom( 0 ).getMassNumber());
        System.out.println("MassNumber cdk: " + cdkmol.getAtomContainer().getAtom( 0 ).getMassNumber());
        System.out.println("MassNumber converted: " + convertedmol.getAtomContainer().getAtom( 0 ).getMassNumber());
        
    }

    @Test
    public void testReadCML() throws IOException{
            
        String cmlstring="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
+"<molecule id=\"m1\" xmlns=\"http://www.xml-cml.org/schema\">"
+"  <atomArray>"
+"  <atom id=\"a1\" elementType=\"C\" x2=\"0.1542\" y2=\"0.2\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"   <atom id=\"a2\" elementType=\"C\" x2=\"-0.1458\" y2=\"1.05\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a3\" elementType=\"C\" x2=\"1.0292\" y2=\"-0.0875\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a4\" elementType=\"N\" x2=\"0.4542\" y2=\"1.7208\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"14\"/>"
+"  <atom id=\"a5\" elementType=\"S\" x2=\"-0.5708\" y2=\"-0.3292\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"32\"/>"
+"  <atom id=\"a6\" elementType=\"C\" x2=\"1.3375\" y2=\"1.5333\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a7\" elementType=\"O\" x2=\"1.6792\" y2=\"0.5208\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"16\"/>"
+"  <atom id=\"a8\" elementType=\"O\" x2=\"1.6\" y2=\"0.8833\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"16\"/>"
+"  <atom id=\"a9\" elementType=\"C\" x2=\"-1.0333\" y2=\"1.05\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a10\" elementType=\"C\" x2=\"-1.2958\" y2=\"0.1958\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a11\" elementType=\"O\" x2=\"1.2\" y2=\"-0.9667\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"16\"/>"
+"  <atom id=\"a12\" elementType=\"Cl\" x2=\"1.65\" y2=\"3.0458\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"35\"/>"
+"  <atom id=\"a13\" elementType=\"C\" x2=\"1.9292\" y2=\"2.2125\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a14\" elementType=\"C\" x2=\"2.05\" y2=\"-1.25\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"</atomArray>"
+"<bondArray>"
+"  <bond id=\"b1\" atomRefs2=\"a2 a1\" order=\"D\"/>"
+"  <bond id=\"b2\" atomRefs2=\"a3 a1\" order=\"S\"/>"
+"  <bond id=\"b3\" atomRefs2=\"a4 a2\" order=\"S\"/>"
+"  <bond id=\"b4\" atomRefs2=\"a5 a1\" order=\"S\"/>"
+"    <bond id=\"b5\" atomRefs2=\"a6 a4\" order=\"S\"/>"
+"  <bond id=\"b6\" atomRefs2=\"a7 a3\" order=\"D\"/>"
+"  <bond id=\"b7\" atomRefs2=\"a8 a6\" order=\"D\"/>"
+"  <bond id=\"b8\" atomRefs2=\"a9 a2\" order=\"S\"/>"
+"  <bond id=\"b9\" atomRefs2=\"a10 a5\" order=\"S\"/>"
+"  <bond id=\"b10\" atomRefs2=\"a11 a3\" order=\"S\"/>"
+"  <bond id=\"b11\" atomRefs2=\"a12 a13\" order=\"S\"/>"
+"  <bond id=\"b12\" atomRefs2=\"a13 a6\" order=\"S\"/>"
+"  <bond id=\"b13\" atomRefs2=\"a14 a11\" order=\"S\"/>"
+"  <bond id=\"b14\" atomRefs2=\"a9 a10\" order=\"S\"/>"
+"</bondArray>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"ROTB\" dataType=\"xsd:string\">5</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"LOGP\" dataType=\"xsd:string\">9.080000000000000e-001</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"INFO\" dataType=\"xsd:string\">TANGIBLE</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"DONH\" dataType=\"xsd:string\">1</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"TPS\" dataType=\"xsd:string\">5.729560000000000e+001</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"IDNUMBER\" dataType=\"xsd:string\">F0326-0475</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"ACCH\" dataType=\"xsd:string\">3</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"SALTID\" dataType=\"xsd:string\">0</scalar>"
+"</molecule>";
    	
        ByteArrayInputStream bais=new ByteArrayInputStream(cmlstring.getBytes());

        ReaderFactory readerFactory=new ReaderFactory();
        CDK10ManagerHelper.registerFormats(readerFactory);

        //Create the reader
        IChemObjectReader reader= readerFactory.createReader(bais);
        reader.getFormat();
        
        
    }

    /**
     * Test conversion of CDK10Molecule to CDKMolecule starting from CML string
     * @throws IOException
     * @throws BioclipseException
     * @throws CDKException 
     */
    @Test
    public void testCreateFromImoleculeCML() throws IOException, 
                                                    BioclipseException, 
                                                    CDKException {
        
        CDK10Manager cdk10 = new CDK10Manager();
        CDKManager   cdk   = new CDKManager();
            
        String cmlstring="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
+"<molecule id=\"m1\" xmlns=\"http://www.xml-cml.org/schema\">"
+"  <atomArray>"
+"  <atom id=\"a1\" elementType=\"C\" x2=\"0.1542\" y2=\"0.2\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"   <atom id=\"a2\" elementType=\"C\" x2=\"-0.1458\" y2=\"1.05\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a3\" elementType=\"C\" x2=\"1.0292\" y2=\"-0.0875\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a4\" elementType=\"N\" x2=\"0.4542\" y2=\"1.7208\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"14\"/>"
+"  <atom id=\"a5\" elementType=\"S\" x2=\"-0.5708\" y2=\"-0.3292\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"32\"/>"
+"  <atom id=\"a6\" elementType=\"C\" x2=\"1.3375\" y2=\"1.5333\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a7\" elementType=\"O\" x2=\"1.6792\" y2=\"0.5208\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"16\"/>"
+"  <atom id=\"a8\" elementType=\"O\" x2=\"1.6\" y2=\"0.8833\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"16\"/>"
+"  <atom id=\"a9\" elementType=\"C\" x2=\"-1.0333\" y2=\"1.05\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a10\" elementType=\"C\" x2=\"-1.2958\" y2=\"0.1958\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a11\" elementType=\"O\" x2=\"1.2\" y2=\"-0.9667\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"16\"/>"
+"  <atom id=\"a12\" elementType=\"Cl\" x2=\"1.65\" y2=\"3.0458\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"35\"/>"
+"  <atom id=\"a13\" elementType=\"C\" x2=\"1.9292\" y2=\"2.2125\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"  <atom id=\"a14\" elementType=\"C\" x2=\"2.05\" y2=\"-1.25\" formalCharge=\"0\" hydrogenCount=\"0\" isotopeNumber=\"12\"/>"
+"</atomArray>"
+"<bondArray>"
+"  <bond id=\"b1\" atomRefs2=\"a2 a1\" order=\"D\"/>"
+"  <bond id=\"b2\" atomRefs2=\"a3 a1\" order=\"S\"/>"
+"  <bond id=\"b3\" atomRefs2=\"a4 a2\" order=\"S\"/>"
+"  <bond id=\"b4\" atomRefs2=\"a5 a1\" order=\"S\"/>"
+"    <bond id=\"b5\" atomRefs2=\"a6 a4\" order=\"S\"/>"
+"  <bond id=\"b6\" atomRefs2=\"a7 a3\" order=\"D\"/>"
+"  <bond id=\"b7\" atomRefs2=\"a8 a6\" order=\"D\"/>"
+"  <bond id=\"b8\" atomRefs2=\"a9 a2\" order=\"S\"/>"
+"  <bond id=\"b9\" atomRefs2=\"a10 a5\" order=\"S\"/>"
+"  <bond id=\"b10\" atomRefs2=\"a11 a3\" order=\"S\"/>"
+"  <bond id=\"b11\" atomRefs2=\"a12 a13\" order=\"S\"/>"
+"  <bond id=\"b12\" atomRefs2=\"a13 a6\" order=\"S\"/>"
+"  <bond id=\"b13\" atomRefs2=\"a14 a11\" order=\"S\"/>"
+"  <bond id=\"b14\" atomRefs2=\"a9 a10\" order=\"S\"/>"
+"</bondArray>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"ROTB\" dataType=\"xsd:string\">5</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"LOGP\" dataType=\"xsd:string\">9.080000000000000e-001</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"INFO\" dataType=\"xsd:string\">TANGIBLE</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"DONH\" dataType=\"xsd:string\">1</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"TPS\" dataType=\"xsd:string\">5.729560000000000e+001</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"IDNUMBER\" dataType=\"xsd:string\">F0326-0475</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"ACCH\" dataType=\"xsd:string\">3</scalar>"
+"<scalar dictRef=\"cdk:molecularProperty\" title=\"SALTID\" dataType=\"xsd:string\">0</scalar>"
+"</molecule>";

        CDK10Molecule cdk10mol = cdk10.fromString(cmlstring);
        
        ICDKMolecule convertedmol = cdk.create( cdk10mol );
        assertNotNull(convertedmol);
        String smiles=convertedmol.getSmiles();
        assertNotNull(smiles);
        System.out.println("Smiles: " + smiles);
        
        ICDKMolecule cdkmol = cdk.fromCml(cmlstring);
        
        assertTrue( cdk.fingerPrintMatches( cdkmol, convertedmol ));

        assertTrue( UniversalIsomorphismTester
          .isIsomorph( cdk10mol.getAtomContainer(), 
                       cdkmol.getAtomContainer()));        

        assertTrue( UniversalIsomorphismTester
                    .isIsomorph( cdk10mol.getAtomContainer(), 
                                 convertedmol.getAtomContainer()));        

        assertTrue( UniversalIsomorphismTester
                    .isIsomorph( cdkmol.getAtomContainer(), 
                                 convertedmol.getAtomContainer()));        

        //It seems that MassNumber is not created from Smiles?
        System.out.println("MassNumber cdk10: " + cdk10mol.getAtomContainer().getAtom( 0 ).getMassNumber());
        System.out.println("MassNumber cdk: " + cdkmol.getAtomContainer().getAtom( 0 ).getMassNumber());
        System.out.println("MassNumber converted: " + convertedmol.getAtomContainer().getAtom( 0 ).getMassNumber());
    }
}
