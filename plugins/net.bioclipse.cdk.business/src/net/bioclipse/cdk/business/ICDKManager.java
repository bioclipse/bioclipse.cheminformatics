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
package net.bioclipse.cdk.business;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.CDKMoleculeList;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.domain.IMolecule;

public interface ICDKManager extends IBioclipseManager {

    /**
     * Create a CDKMolecule from SMILES
     * @param SMILES
     * @return
     * @throws BioclipseException
     */
    @Recorded
    public CDKMolecule createMoleculeFromSMILES(String SMILES) throws BioclipseException;

    /**
     * Loads a molecule from file using CDK
     *
     * @param path The path to the file
     * @return a BioJavaSequence object
     * @throws IOException
     * @throws BioclipseException
     */
    @Recorded
    public CDKMolecule loadMolecule( String path ) throws IOException, BioclipseException;

    /**
     * Load molecule from InputStream using CDK
     * @param instream to be loaded
     * @return loaded sequence
     * @throws IOException
     * @throws BioclipseException
     */
    @Recorded
    public CDKMolecule loadMolecule(InputStream instream) throws IOException, BioclipseException;

    /**
     * Load a molecules from a file.
     */
    @Recorded
    public CDKMoleculeList loadMolecules(String path) throws IOException, BioclipseException;

    /**
     * Load one or more molecules from an InputStream and return a CDKMoleculeList.
     */
    @Recorded
    public CDKMoleculeList loadMolecules(InputStream instream) throws IOException, BioclipseException;

    /**
     *
     * @param mol
     * @throws IllegalStateException
     */
    @Recorded
    public void saveMolecule(CDKMolecule mol) throws IllegalStateException;


    /**
     * Calculate SMILES string for an IMolecule
     * @param molecule
     * @return
     * @throws BioclipseException
     */
    @Recorded
    public String calculateSmiles (IMolecule molecule) throws BioclipseException;

    /**
     * Returns an iterator to the molecules in an Inputstream
     *
     * @param instream
     * @return
     */
    @Recorded
    public Iterator<ICDKMolecule> creatMoleculeIterator(InputStream instream);
}
