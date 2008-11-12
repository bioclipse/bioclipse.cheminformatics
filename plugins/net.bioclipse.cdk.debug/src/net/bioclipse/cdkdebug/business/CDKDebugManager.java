/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Egon Willighagen <egonw@user.sf.net>
 ******************************************************************************/
package net.bioclipse.cdkdebug.business;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ui.Activator;

import org.apache.log4j.Logger;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.atomtype.mapper.AtomTypeMapper;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.tools.diff.AtomContainerDiff;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

public class CDKDebugManager implements ICDKDebugManager {

    private static final Logger logger = Logger.getLogger(CDKManager.class);
    private static final CDKManager cdk = new CDKManager();

    public void diff(ICDKMolecule mol, ICDKMolecule mol2) {
        Activator.getDefault().CONSOLE.echo(
            AtomContainerDiff.diff(mol.getAtomContainer(), mol2.getAtomContainer())
        ); 
    }

    public void debug(ICDKMolecule mol) {
        Activator.getDefault().CONSOLE.echo(
            mol.getAtomContainer().toString()
        ); 
    }

    public String getNamespace() {
        return "cdkdebug";
    }

    public ICDKMolecule depictSybylAtomTypes(IMolecule mol)
    throws InvocationTargetException {

        ICDKMolecule cdkmol;
        try {
            cdkmol = cdk.create(mol);
        } catch (BioclipseException e) {
            System.out.println("Error converting cdk10 to cdk");
            e.printStackTrace();
            throw new InvocationTargetException(e);
        }

        IAtomContainer ac = cdkmol.getAtomContainer();

        CDKAtomTypeMatcher cdkMatcher = CDKAtomTypeMatcher.getInstance(ac
                                                                       .getBuilder());
        AtomTypeMapper mapper = AtomTypeMapper
        .getInstance("org/openscience/cdk/dict/data/cdk-sybyl-mappings.owl");
        InputStream iStream = org.openscience.cdk.atomtype.Activator.class.getResourceAsStream("/org/openscience/cdk/dict/data/sybyl-atom-types.owl");
        AtomTypeFactory factory = AtomTypeFactory.getInstance(iStream, "owl", ac
                                                              .getBuilder());
        IAtomType[] sybylTypes = new IAtomType[ac.getAtomCount()];
        int atomCounter = 0;

        //  try {
        //    System.out.println("smiles: " + mol.getSmiles());
        //    System.out.println("cml: " + mol.getCML());
        //    System.out.println("Arom: "
        //        + CDKHueckelAromaticityDetector.detectAromaticity(ac));
        //  } catch (Exception e1) {
        //    // TODO Auto-generated catch block
        //    e1.printStackTrace();
        //  }

        try {
            int a=0;
            for (IAtom atom : ac.atoms()) {
                IAtomType type = cdkMatcher.findMatchingAtomType(ac, atom);
                if (type==null){
                    logger.debug("AT null for atom: " + atom);
                }else{
                    AtomTypeManipulator.configure(atom, type);
                }
                a++;
            }
            System.out.println("Arom: "
                               + CDKHueckelAromaticityDetector.detectAromaticity(ac));
            for (IAtom atom : ac.atoms()) {
                String mappedType = mapper.mapAtomType(atom.getAtomTypeName());
                if ("C.2".equals(mappedType)
                        && atom.getFlag(CDKConstants.ISAROMATIC)) {
                    mappedType = "C.ar";
                } else if ("N.pl3".equals(mappedType)
                        && atom.getFlag(CDKConstants.ISAROMATIC)) {
                    mappedType = "N.ar";
                }
                sybylTypes[atomCounter] = factory.getAtomType(mappedType);
                ; // yes, setting null's here is important
                atomCounter++;
            }

            // now that full perception is finished, we can set atom type names:
            for (int i = 0; i < sybylTypes.length; i++) {
                ac.getAtom(i).setAtomTypeName(sybylTypes[i].getAtomTypeName());
            }
        } catch (CDKException exception) {
            throw new InvocationTargetException(exception,
                                                "Error while perceiving atom types: "
                                                + exception.getMessage());
        }

        return cdkmol;
    }

    public void depictCDKAtomTypes(IMolecule mol) throws InvocationTargetException {

        ICDKMolecule cdkmol;
        try {
            cdkmol = cdk.create(mol);
        } catch ( BioclipseException e ) {
            e.printStackTrace();
            throw new InvocationTargetException(e, "Error while creating a ICDKMolecule");
        }
        IAtomContainer ac = cdkmol.getAtomContainer();

        CDKAtomTypeMatcher cdkMatcher = CDKAtomTypeMatcher.getInstance(ac.getBuilder());
        int i = 1;
        for (IAtom atom : ac.atoms()) {
            IAtomType type = null;
            try {
                type = cdkMatcher.findMatchingAtomType(ac, atom);
            } catch ( CDKException e ) {}
            Activator.getDefault().CONSOLE.echo(
                (i) + ": " + (type != null ? type.getAtomTypeName() : "null") +
                "\n" // FIXME: should use NEWLINE here
            );
            i++;
        }
    }

}
