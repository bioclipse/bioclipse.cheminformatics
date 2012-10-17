/*******************************************************************************
 * Copyright (c) 2008  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.inchi.business.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ChemObjectPropertySource;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.inchi.InChI;
import net.bioclipse.inchi.business.IInChIManager;

import org.junit.Test;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.AtomTypeAwareSaturationChecker;

import com.sun.tools.internal.xjc.generator.bean.ImplStructureStrategy;

public abstract class AbstractInChIManagerPluginTest {

    protected static IInChIManager inchi;
    protected static ICDKManager cdk;
    private static final String BUG3170_SMILES = "COc1ccc2[C@@H]3[C@H](COc2c1)C(C)(C)OC4=C3C(=O)C(=O)C5=C4OC(C)(C)[C@@H]6COc7cc(OC)ccc7[C@H]56";
    private static final String bug3170_inchi = "InChI=1S/C32H32O8/c1-31(2)19-13-37-21-11-15(35-5)7-9-17(21)23(19)25-27(33)28(34)26-24-18-10-8-16(36-6)12-22(18)38-14-20(24)32(3,4)40-30(26)29(25)39-31/h7-12,19-20,23-24H,13-14H2,1-6H3/t19-,20+,23+,24-";

    @Test
    public void testInitialization() {
    	Assert.assertNotNull(inchi);
    }
    
    @Test
    public void testGenerate() throws Exception {
        IMolecule mol = cdk.fromSMILES("C");
        Assert.assertNotNull("Input structure is unexpectedly null", mol);
        InChI inchiObj = inchi.generate(mol);
        Assert.assertNotNull(inchiObj);
        Assert.assertEquals("InChI=1S/CH4/h1H4", inchiObj.getValue());
    }

    @Test
    public void testGenerateWithOptions() throws Exception {
        IMolecule mol = cdk.fromSMILES("OC=O");
        Assert.assertNotNull("Input structure is unexpectedly null", mol);
        InChI inchiObj = inchi.generate(mol, "FixedH");
        Assert.assertNotNull(inchiObj);
        Assert.assertEquals("InChI=1/CH2O2/c2-1-3/h1H,(H,2,3)/f/h2H", inchiObj.getValue());
    }

    @Test
    public void testGenerateNoStereo() throws Exception {
        IMolecule mol = cdk.fromSMILES("ClC(Br)(F)(O)");
        Assert.assertNotNull("Input structure is unexpectedly null", mol);
        InChI inchiStr = inchi.generate(mol);
        Assert.assertNotNull(inchiStr);
        Assert.assertEquals(
            "InChI=1S/CHBrClFO/c2-1(3,4)5/h5H",
            inchiStr.getValue()
        );
    }

    @Test
    public void testGenerateKey() throws Exception {
        IMolecule mol = cdk.fromSMILES("C");
        Assert.assertNotNull("Input structure is unexpectedly null", mol);
        InChI key = inchi.generate(mol);
        Assert.assertNotNull(key);
        Assert.assertEquals(
            "VNWKTOKETHGBQD-UHFFFAOYSA-N",
            key.getKey()
        );
    }

    @Test
    public void testOptions() throws Exception {
        List<String> options = inchi.options();
        Assert.assertNotNull(options);
        Assert.assertNotSame(0, options.size()); // at least one option
        Assert.assertTrue(options.contains("FixedH"));
    }

    @Test
    public void bug3170_CdkMolecule_BcInchi() throws Exception {
        IAtomContainer molecule = setupCDKMolecule( BUG3170_SMILES );
        ICDKMolecule mol = new CDKMolecule( molecule );
        InChI inchiStr = inchi.generate(mol);
        Assert.assertNotNull(inchiStr);
        Assert.assertEquals( bug3170_inchi,
                             inchiStr.getValue() );
    }
    
    @Test
    public void bug3170_BcMolecule_CdkInchi() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES(BUG3170_SMILES);
        IAtomContainer molecule = mol.getAtomContainer();
        for (IBond bond:molecule.bonds())
            if (bond.getFlag(CDKConstants.ISAROMATIC) == true)
                bond.setFlag(CDKConstants.ISAROMATIC, false);
        
        InChIGeneratorFactory fac = InChIGeneratorFactory.getInstance();
        InChIGenerator gen = fac.getInChIGenerator(molecule);
        System.out.println(gen.getMessage());
        Assert.assertEquals( bug3170_inchi,
                             gen.getInchi() );
    }
    
    @Test
    public void bug3170_BcMolecule_BcInchi() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES(BUG3170_SMILES);
        InChI inchiStr = inchi.generate(mol);
        Assert.assertNotNull(inchiStr);
        Assert.assertEquals( bug3170_inchi,
                             inchiStr.getValue() );
    }
    
    @Test
    public void bug3170_CdkMolecule_CdkInchi() throws Exception {
        IAtomContainer molecule = setupCDKMolecule( BUG3170_SMILES );
        for (IBond bond:molecule.bonds())
            if (bond.getFlag(CDKConstants.ISAROMATIC) == true)
                bond.setFlag(CDKConstants.ISAROMATIC, false);
        
        InChIGeneratorFactory fac = InChIGeneratorFactory.getInstance();
        InChIGenerator gen = fac.getInChIGenerator(molecule);
        System.out.println(gen.getMessage());
        Assert.assertEquals( bug3170_inchi,
                             gen.getInchi() );
    }
     
    private IAtomContainer setupCDKMolecule( final String smiles ) throws Exception{

        SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
        AtomTypeAwareSaturationChecker atasc = new AtomTypeAwareSaturationChecker();
        
        IAtomContainer molecule = sp.parseSmiles(smiles);
        atasc.decideBondOrder(molecule);
    
        for (IBond bond:molecule.bonds())
            if (bond.getFlag(CDKConstants.ISAROMATIC) == true)
                bond.setFlag(CDKConstants.ISAROMATIC, false);
        return molecule;
    }
}
