 /*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *     Stefan Kuhn
 *
 ******************************************************************************/
package net.bioclipse.cdk.business;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.domain.IMolecule;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemModel;

@PublishedClass( "Contains CDK related methods")
public interface ICDKManager extends IBioclipseManager {

	public final static String rxn = "rxn";
	public final static String mol = "mol";
	public final static String cml = "cml";
	public final static String smi = "smi";
    public final static String cdk = "cdk";

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
     * @throws CoreException 
     */
    @Recorded
    @PublishedMethod( params = "String path", 
                      methodSummary = "Loads a molecule from file. " +
                                      "Returns the first if multiple " +
                      		          "molecules exists in the file ")
    public ICDKMolecule loadMolecule( String path )
        throws IOException, BioclipseException, CoreException;

    /**
     * Load molecule from an <code>IFile</code> using CDK.
     * If many molecules, just return first.
     * To return a list of molecules, use loadMolecules(...)
     * 
     * @param file to be loaded
     * @return loaded sequence
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException 
     */
    @Recorded
    public ICDKMolecule loadMolecule( IFile file, 
                                      IProgressMonitor monitor )
        throws IOException, BioclipseException, CoreException;

    /**
     * @param file
     * @return
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
    @Recorded
    public ICDKMolecule loadMolecule( IFile file )
        throws IOException, BioclipseException, CoreException;
    
    /**
     * Loads molecules from a file at a given path.
     *
     * @param path
     * @return a list of molecules
     * @throws IOException
     * @throws BioclipseException on parsing trouble of the molecules
     * @throws CoreException 
     */
    @Recorded
    @PublishedMethod( params = "String path", 
                      methodSummary = "Loads molecules from a file at " +
                      		          "a given path into a list of " +
                      		          "molecules")
    public List<ICDKMolecule> loadMolecules(String path)
        throws IOException, BioclipseException, CoreException;

    /**
     * Loads molecules from an IFile.
     * 
     * @param file
     * @return a list of molecules
     * @throws IOException
     * @throws BioclipseException on parsing trouble of the molecules
     * @throws CoreException 
     */
    @Recorded
    public List<ICDKMolecule> loadMolecules( IFile file,
                                             IProgressMonitor monitor )
        throws IOException, BioclipseException, CoreException;

    @Recorded
    public List<ICDKMolecule> loadMolecules( IFile file )
        throws IOException, BioclipseException, CoreException;
    
    /**
     * @param mol The molecule to save
     * @param target Where to save
     * @param filetype Which format to save (for formats, see constants)
     * @throws IllegalStateException
     */
    @Recorded
    public void saveMolecule(ICDKMolecule mol, IFile target, String filetype) 
    	throws BioclipseException, CDKException, CoreException;

    /**
     * @param model The ChemModel to save
     * @param target Where to save
     * @param filetype Which format to save (for formats, see constants)
     * @throws IllegalStateException
     */
    @Recorded
    public void save(IChemModel model, IFile target, String filetype) 
    	throws BioclipseException, CDKException, CoreException;

    /**
     * Calculate SMILES string for an IMolecule
     * @param molecule
     * @return
     * @throws BioclipseException
     */
    @Recorded
    @PublishedMethod ( params = "IMolecule molecule", 
                       methodSummary = "Returns the SMILES for a " +
                       		           "molecule" )
    public String calculateSmiles (IMolecule molecule) 
                  throws BioclipseException;

    /**
     * @param path
     * @return
     * @throws CoreException
     */
    @PublishedMethod (params = "String path",
                      methodSummary = "Creates and iterator to the " +
                      		          "molecules in the file at the " +
                      		          "path")
    public Iterator<ICDKMolecule> createMoleculeIterator(String path) 
                                  throws CoreException;
    
    /**
     * @param file
     * @return
     * @throws CoreException 
     */
    public Iterator<ICDKMolecule> createMoleculeIterator( 
        IFile file,
        IProgressMonitor monitor ) throws CoreException;
    
    public Iterator<ICDKMolecule> createMoleculeIterator(IFile file) 
                                  throws CoreException;
    
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
                      methodSummary = "Returns true if the " +
                      		          "fingerprint of the " +
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
    		                       "checking fingerprints)")
    @Recorded
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
    @Recorded
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
    @Recorded
    public ICDKMolecule fromCml( String cml ) 
                        throws BioclipseException, IOException;

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
    @Recorded
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
    @Recorded
    public int numberOfEntriesInSDF( String filePath );
    
    /**
     * Reads files and extracts conformers if available.
     * @param path the full path to the file
     * @return a list of molecules that may have multiple conformers
     */
    @Recorded
    @PublishedMethod ( params = "String path",
                       methodSummary = "Loads the molecules at the " +
                          "path into a list, and take conformers into " +
                          "account. Currently only reads SDFiles.")
    public List<ICDKMolecule> loadConformers( String path );

    /**
     * Reads files and extracts conformers if available.
     * @param file
     * @return a list of molecules that may have multiple conformers
     */
    @Recorded
    public List<ICDKMolecule> loadConformers( IFile file, 
                                              IProgressMonitor monitor );
    
    /**
     * @param file
     * @return
     */
    public List<ICDKMolecule> loadConformers( IFile file);

    /**
     * Returns an iterator to the molecules in an IFile that might
     * contain conformers.
     *
     * @param instream
     * @return
     */
    @Recorded
    public Iterator<ICDKMolecule> creatConformerIterator( 
        IFile file, IProgressMonitor monitor );
    
    /**
     * @param file
     * @return
     */
    @Recorded
    public Iterator<ICDKMolecule> creatConformerIterator(IFile file);
    
    @Recorded
    @PublishedMethod( params = "String path",
                      methodSummary = "" )
    public Iterator<ICDKMolecule> createConformerIterator( String path );

    @PublishedMethod ( params = "Imolecule molecule",
                       methodSummary = "Calculate and return the " +
                                       "molecular weight for the " +
                                       "molecule.")
    @Recorded
    public double calculateMass( IMolecule molecule ) 
                  throws BioclipseException;

    /**
     * @param file
     * @param subProgressMonitor
     * @return
     */
    @Recorded
    public int numberOfEntriesInSDF( IFile file,
                                     IProgressMonitor monitor );
    
    @Recorded
    public IMolecule generate2dCoordinates(IMolecule molecule) throws Exception;

    /**
     * @param file
     * @return
     */
    @Recorded
    public int numberOfEntriesInSDF( IFile file );

    public List<SDFElement> loadSDFElements( IFile file,
                                            IProgressMonitor monitor ) 
                            throws CoreException, IOException;
    
    /**
     * @param sdfFile
     * @return
     * @throws CoreException 
     * @throws IOException 
     */
    public List<SDFElement> loadSDFElements( IFile sdfFile ) 
                            throws CoreException, IOException;
}