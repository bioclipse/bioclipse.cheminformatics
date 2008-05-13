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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.CDKManagerHelper;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

import org.junit.Test;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.templates.MoleculeFactory;

public class CDKManagerTest {

    ICDKManager cdk;

    //Do not use SPRING OSGI for this manager
    //since we are only testing the implementations of the manager methods
    public CDKManagerTest() {
        cdk = new CDKManager();
    }

    @Test
    public void testLoadMolecule() throws IOException, BioclipseException {

//        InputStream atpFile = getClass().getResourceAsStream("/testFiles/polycarpol.mol");
//        InputStream pdbFile = getClass().getResourceAsStream("/testFiles/1D66.pdb");
        InputStream cmlFile = getClass().getResourceAsStream("/testFiles/0037.cml");

        CDKMolecule mol = cdk.loadMolecule(cmlFile);

        System.out.println("mol: " + mol.toString());
    }

    //FIXME: Fails. See bug #1958097
    @Test
    public void testLoadATP() throws IOException, BioclipseException {

        InputStream atpFile = getClass().getResourceAsStream("/testFiles/atp.mol");

        CDKMolecule mol = cdk.loadMolecule(atpFile);

        System.out.println("mol: " + mol.toString());
    }

    //FIXME: Fails. See bug #1958097
    @Test
    public void testLoadPolycarpol() throws IOException, BioclipseException {

        InputStream atpFile = getClass().getResourceAsStream("/testFiles/polycarpol.mol");

        CDKMolecule mol = cdk.loadMolecule(atpFile);

        System.out.println("mol: " + mol.toString());
    }

    @Test
    public void testCreateSMILES() throws BioclipseException, IOException {
        InputStream cmlFile = getClass().getResourceAsStream("/testFiles/0037.cml");

        CDKMolecule mol = cdk.loadMolecule(cmlFile);
        String smiles = mol.getSmiles();

        assertEquals("CC1CCCC(C#N)N1C(CO[Si](C)(C)C)C2=CC=CC=C2", smiles);
    }

    @Test
    public void testCreateMoleculeFromSMILES() throws BioclipseException {

        CDKMolecule mol=cdk.fromSmiles("C1CCCCC1CCO");

        assertEquals(mol.getAtomContainer().getAtomCount(), 9);
        assertEquals(mol.getAtomContainer().getBondCount(), 9);
    }

    @Test
    public void testCreatingMoleculeIterator() {
        InputStream sdfFile = getClass()
                              .getResourceAsStream("/testFiles/test.sdf");

        List<IMolecule> molecules = new ArrayList<IMolecule>();

        for ( Iterator<net.bioclipse.cdk.domain.ICDKMolecule> iterator
                    = cdk.creatMoleculeIterator(sdfFile);
              iterator.hasNext(); ) {

            molecules.add( iterator.next() );
        }

        assertEquals( 2, molecules.size() );
    }
    
    @Test
    public void isSubstructure() throws BioclipseException {
        SmilesGenerator generator = new SmilesGenerator();
        String indoleSmiles  = generator
                               .createSMILES( MoleculeFactory.makeIndole() );
        String pyrroleSmiles = generator
                               .createSMILES( MoleculeFactory.makePyrrole() );
        ICDKMolecule indole  = cdk.fromSmiles( indoleSmiles );
        ICDKMolecule pyrrole = cdk.fromSmiles( pyrroleSmiles );
        
        assertTrue( cdk.fingerPrintMatches(indole, pyrrole) );
    }
}
