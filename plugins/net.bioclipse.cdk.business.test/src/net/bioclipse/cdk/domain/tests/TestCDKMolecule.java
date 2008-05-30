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

import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk10.business.CDK10Manager;
import net.bioclipse.cdk10.business.CDK10Molecule;
import net.bioclipse.core.business.BioclipseException;

import org.junit.Before;
import org.junit.Test;

public class TestCDKMolecule {

    ICDKManager cdk;

    //Do not use SPRING OSGI for this manager
    //since we are only testing the implementations of the manager methods
    @Before
    public void initialize() {
        cdk=new CDKManager();
    }

    @Test
    public void testFingerprinter() throws IOException, BioclipseException{
        InputStream cmlFile = getClass().getResourceAsStream("/testFiles/0037.cml");

        ICDKMolecule mol=cdk.loadMolecule(cmlFile);
        assertNotNull(mol);
        BitSet bs=mol.getFingerprint(false);
        assertNotNull(bs);
        System.out.println("FP: " + bs.toString());
    }

    @Test
    public void testGetCML() throws IOException, BioclipseException{
        InputStream cmlFile = getClass().getResourceAsStream("/testFiles/0037.cml");

        ICDKMolecule mol=cdk.loadMolecule(cmlFile);
        assertNotNull(mol);
        String cmlString=mol.getCML();
        assertNotNull(cmlString);
        System.out.println("CML:\n" + cmlString);
    }

    @Test
    public void testGetSmiles() throws IOException, BioclipseException{
        InputStream cmlFile = getClass().getResourceAsStream("/testFiles/0037.cml");

        ICDKMolecule mol=cdk.loadMolecule(cmlFile);
        assertNotNull(mol);
        String smiles=mol.getSmiles();
        assertNotNull(smiles);
        System.out.println("Smiles: " + smiles);

    }
    
    
    @Test
    public void testCreateFromString() throws IOException, BioclipseException{
        InputStream cmlFile = getClass().getResourceAsStream("/testFiles/0037.cml");
        byte[] buf=new byte[60000];
        int a=cmlFile.read( buf );
        System.out.println("Read: " + a + "bytes");
        String content=new String(buf);
        String cutcontent=content.substring( 0,a );
        System.out.println("Content: " + cutcontent.length());
        
        ICDKMolecule mol=cdk.fromString( cutcontent );
        assertNotNull(mol);
        String smiles=mol.getSmiles();
        assertNotNull(smiles);
        System.out.println("Smiles: " + smiles);
    }

    /**
     * Test conversion of CDK10Molecule to CDKMolecule
     * @throws IOException
     * @throws BioclipseException
     */
    @Test
    public void testCreateFromImolecule() throws IOException, BioclipseException{
        
        CDK10Manager cdk10=new CDK10Manager();
        CDKManager cdk=new CDKManager();

        CDK10Molecule cdk10mol=cdk10.createMoleculeFromSMILES(
                                  "CC1CCCC(C#N)N1C(CO[Si](C)(C)C)C2=CC=CC=C2" );
        
        ICDKMolecule convertedmol=cdk.create( cdk10mol );
        assertNotNull(convertedmol);
        String smiles=convertedmol.getSmiles();
        assertNotNull(smiles);
        System.out.println("Smiles: " + smiles);
        
        ICDKMolecule cdkmol=cdk.fromSmiles(
                                  "CC1CCCC(C#N)N1C(CO[Si](C)(C)C)C2=CC=CC=C2" );
        
        assertTrue( cdk.fingerPrintMatches( cdkmol, convertedmol ));
        
    }

}
