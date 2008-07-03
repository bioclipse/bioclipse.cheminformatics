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
import java.util.List;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.domain.IMolecule;

@PublishedClass( "Contains CDK related methods")
public interface ICDKManager extends IBioclipseManager {

    /**
     * Create a CDKMolecule from SMILES
     * @param SMILES
     * @return
     * @throws BioclipseException
     */
    @Recorded
    @PublishedMethod( params = "String smiles", 
                      methodSummary = "Creates a cdk molecule from " +
                      		            "smiles")
    public ICDKMolecule fromSmiles(String smiles)
        throws BioclipseException;

    /**
     * Loads a molecule from file using CDK.
     * If many molecules, just return first.
     * To return a list of molecules, use loadMolecules(...)
     *
     * @param path The path to the file
     * @return a BioJavaSequence object
     * @throws IOException
     * @throws BioclipseException
     */
    @Recorded
    @PublishedMethod( params = "String path", 
                      methodSummary = "Loads a molecule from file. " +
                      		            "Returns the first if multiple " +
                      		            "molecules exists in the file ")
    public ICDKMolecule loadMolecule( String path )
        throws IOException, BioclipseException;

    /**
     * Load molecule from InputStream using CDK.
     * If many molecules, just return first.
     * To return a list of molecules, use loadMolecules(...)
     * 
     * @param instream to be loaded
     * @return loaded sequence
     * @throws IOException
     * @throws BioclipseException
     */
    @Recorded
    @PublishedMethod( params = "InputStream instream", 
                      methodSummary = "Loads a molecule from an " +
                      		            "inputstream. Returns the first " +
                      		            "if multiple molecules exists " +
                      		            "in the file " )
    public ICDKMolecule loadMolecule( InputStream instream )
        throws IOException, BioclipseException;

    /**
     * Loads molecules from a file at a given path.
     *
     * @param path
     * @return a list of molecules
     * @throws IOException
     * @throws BioclipseException on parsing trouble of the molecules
     */
    @Recorded
    @PublishedMethod( params = "String path", 
                      methodSummary = "Loads molecules from a file at " +
                      		            "a given path into a list of " +
                      		            "molecules")
    public List<ICDKMolecule> loadMolecules(String path)
        throws IOException, BioclipseException;

    /**
     * Loads molecules from an InputStream.
     * 
     * @param instream
     * @return a list of molecules
     * @throws IOException
     * @throws BioclipseException on parsing trouble of the molecules
     */
    @Recorded
    @PublishedMethod( params = "InputStream instream", 
                      methodSummary = "Loads molecules from an " +
                      		            "InputStream")
    public List<ICDKMolecule> loadMolecules(InputStream instream)
        throws IOException, BioclipseException;

    /**
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
    @PublishedMethod ( params = "IMolecule molecule", 
                       methodSummary = "returns the SMILES for a molecule" )
    public String calculateSmiles (IMolecule molecule) throws BioclipseException;

    /**
     * Returns an iterator to the molecules in an Inputstream
     *
     * @param instream
     * @return
     */
    @Recorded
    public Iterator<ICDKMolecule> creatMoleculeIterator(InputStream instream);

    /**
     * True if the fingerprint of the subStructure is a subset of the 
     * fingerprint for the molecule
     * 
     * @param molecule
     * @param subStructure
     * @return
     * @throws BioclipseException
     */
    @PublishedMethod (params = "ICDKMolecule molecule, " +
    		                       "ICDKMolecule subStructure",
                      methodSummary = "True if the fingerprint of the " +
                      		            "subStructure is a subset of the" +
                      		            "fingerprint for the molecule")
    @Recorded
    public boolean fingerPrintMatches( ICDKMolecule molecule, 
                                       ICDKMolecule subStructure ) 
                   throws BioclipseException;
    
    /**
     * True if the paramater substructure is a substructure to the 
     * paramater molecule. 
     * 
     * (Performs an isomophism test without checking fingerprints first)
     * 
     * @param molecule
     * @param subStructure
     * @return
     */
    @PublishedMethod (params = "ICDKMolecule molecule, " +
    		                       "ICDKMolecule subStructure",
    		       methodSummary = "Returns true if the paramater named " +
    		                       "subStructure is a substructure of the " +
    		                       "paramater named molecule. \n" +
    		                       "(Performs an isomophism test without " +
    		                       "checking fingerprints first")
    public boolean subStructureMatches( ICDKMolecule molecule,
                                        ICDKMolecule subStructure );
    
    /**
     * Creates a cdk molecule from an IMolecule
     * 
     * @param m
     * @return
     * @throws BioclipseException 
     */
    @PublishedMethod ( params = "IMolecule m",
                       methodSummary = "Creates a cdk molecule from a" +
                       		           " molecule" )
    public ICDKMolecule create( IMolecule m ) throws BioclipseException;

    /**
     * Creates a cdk molecule from a CML String
     * 
     * @param m
     * @return
     * @throws BioclipseException if input is null or parse fails
     * @throws IOException if file cannot be read
     */
    @PublishedMethod ( params = "String cml",
                       methodSummary = "Creates a cdk molecule from a " +
                       		             "CML String" )
    public ICDKMolecule fromString( String cml ) throws BioclipseException, IOException;

    /**
     * Returns true if the given molecule matches the given SMARTS
     * 
     * @param molecule
     * @param smarts
     * @return whether the given SMARTS matches the given molecule
     * @throws BioclipseException 
     */
    @PublishedMethod ( params = "ICDKMolecule molecule, String smarts", 
                       methodSummary = "Returns true if the given " +
                       		             "SMARTS matches the given " +
                       		             "molecule" )
    public boolean smartsMatches( ICDKMolecule molecule, String smarts ) 
                   throws BioclipseException;

    
    /**
     * @param filePath
     * @return the number of entries in the sdf file at the given path or
     *         0 if failed to read somehow.
     */
    @PublishedMethod ( params = "String filePath",
                       methodSummary = "Counts the number of entries " +
                       		             "in an SDF file at the given " +
                       		             "file path. Returns 0 in case " +
                       		             "of problem.")
    public int numberOfEntriesInSDF( String filePath );

    /**
     * Reads files and extracts conformers if available.
     * @param path the full path to the file
     * @return a list of molecules that may have multiple conformers
     */
    @Recorded
    @PublishedMethod ( params = "String path",
                       methodSummary = "Loads the molecules at the path into a " +
                       		"list, and take conformers into account. " +
                       		"Currently only reads SDFiles.")
    public List<ICDKMolecule> loadConformers( String path );

    /**
     * Reads files and extracts conformers if available.
     * @param stream to (file) contents
     * @return a list of molecules that may have multiple conformers
     */
    @Recorded
    public List<ICDKMolecule> loadConformers( InputStream stream );

    /**
     * Returns an iterator to the molecules in an Inputstream that might
     * contain conformers.
     *
     * @param instream
     * @return
     */
    @Recorded
    public Iterator<ICDKMolecule> creatConformerIterator( InputStream instream );

    @PublishedMethod ( params = "Imolecule molecule",
                       methodSummary = "Calculate and return the molecular " +
                       		"weight for the molecule.")
    @Recorded
    public double calculateMass( IMolecule molecule ) throws BioclipseException;
    
}