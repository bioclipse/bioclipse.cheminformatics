/*******************************************************************************
 * Copyright (c) 2008-2009  Ola Spjuth
 *               2008-2010  Jonathan Alvarsson
 *               2008-2009  Stefan Kuhn
 *               2008-2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.business;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.MoleculesInfo;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.TestMethods;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.managers.business.IBioclipseManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.formats.IChemFormat;

@PublishedClass(
    value="Provides CDK-derived functionality.",
    doi={"10.1021/ci025584y","10.2174/138161206777585274"}
)
@TestClasses(
    "net.bioclipse.cdk.business.test.APITest," +
    "net.bioclipse.cdk.business.test.AbstractCDKManagerPluginTest"
)
public interface ICDKManager extends IBioclipseManager {

    /**
     * Create an empty CDKMolecule.
     *
     * @return An ICDKMolecule.
     * @throws BioclipseException
     */
    @Recorded
    @PublishedMethod(
        methodSummary = "Creates a cdk molecule from SMILES")
    public ICDKMolecule newMolecule();

    @Recorded
    @PublishedMethod(
    	params="IAtomContainer atomContainer",
        methodSummary=
            "Creates a Bioclipse IMolecule object from a CDK " +
        	"IAtomContainer."
    )
    public ICDKMolecule newMolecule(IAtomContainer atomContainer);

    @Recorded
    @PublishedMethod(
    	params="IMoleculeSet set",
        methodSummary="Converts a CDK IMoleculeSet object into a " +
        		"List<ICDKMolecule>."
    )
    public List<ICDKMolecule> asList(IMoleculeSet set);

    @Recorded
    @PublishedMethod(
    	params="List<ICDKMolecule> list",
        methodSummary="Converts a Bioclipse List<ICDKMolecule> list into a " +
        		"CDK IMoleculeSet."
    )
    public IMoleculeSet asSet(List<ICDKMolecule> list);

    /**
     * Create a CDKMolecule from SMILES
     * @param SMILES
     * @return
     * @throws BioclipseException
     */
    @Recorded
    @PublishedMethod( 
        params = "String SMILES",
        methodSummary = "Creates a cdk molecule from SMILES")
    @TestMethods(
       "testLoadMoleculeFromSMILESFile,testCreateMoleculeFromSMILES," +
       "testFingerPrintMatch,testSubStructureMatch,testSMARTSMatching," +
       "testSave")
    public ICDKMolecule fromSMILES(String SMILES)
                        throws BioclipseException;

    public BioclipseJob<ICDKMolecule> fromSMILES( String SMILES,
                                                  BioclipseJobUpdateHook hook );    
    
    /**
     * Perceives aromaticity on an IMolecule
     * @param mol
     * @return
     * @throws BioclipseException
     */
    @Recorded
    @PublishedMethod( 
        params = "IMolecule mol",
        methodSummary = "Perceives aromaticity on an IMolecule. The molecule " +
        		            "will be unchanged and a new molecule created. Uses " +
        		            "the org.openscience.cdk.aromaticity." +
        		            "CDKHueckelAromaticityDetector class from CDK.")
    @TestMethods(
       "testPerceiveAromaticity,")
    public ICDKMolecule perceiveAromaticity(IMolecule mol) 
                     throws BioclipseException;
    
	public List<ICDKMolecule> perceiveAromaticity(List<? extends IMolecule> mols);


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
    @PublishedMethod( 
        params = "String path",
        methodSummary = "Loads a molecule from file. Does not do " +
        		            "aromaticity detection (use perceiveAromaticity). " +
                        "Returns the first if multiple molecules exists " +
                        "in the file ")
    @TestMethods("testLoadMoleculeFromCMLFile,testLoadCMLFromFile2")
    public ICDKMolecule loadMolecule( String path )
                        throws IOException, BioclipseException, CoreException;

    /**
     * @param file
     * @return
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
    @Recorded
    public ICDKMolecule loadMolecule( IFile file );

    @Recorded
    @PublishedMethod( 
        params = "String path",
        methodSummary = "Determines the file format if the file, if chemical")
    @TestMethods("testDetermineFormat")
    public String determineFormat(String path) 
                  throws IOException, CoreException;

    @Recorded
    @PublishedMethod( 
        params = "String path",
        methodSummary = "Determines the file format of the file, if chemical")
    @TestMethods("testDetermineChemFormatPubChemFile")
    public IChemFormat determineIChemFormat(String path) 
                  throws IOException, CoreException;

    @Recorded
    @PublishedMethod( 
        params = "String fileContent",
        methodSummary = "Determines the file format of the file in the String")
    @TestMethods("testDetermineChemFormatPubChemFile")
    public IChemFormat determineIChemFormatOfString(String fileContent) 
                  throws IOException;

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
    @PublishedMethod( 
        params = "String path",
        methodSummary = "Loads molecules from a file at a given path into a " +
        		            "list of molecules")
    @TestMethods("testLoadMolecules")
    public List<ICDKMolecule> loadMolecules(String path)
                              throws IOException, 
                                     BioclipseException, 
                                     CoreException;

    @Recorded
    public List<ICDKMolecule> loadMolecules( IFile file )
                              throws IOException, 
                                     BioclipseException, 
                                     CoreException;

    public List<ICDKMolecule> loadMolecules( IFile file, IProgressMonitor monitor )
    throws IOException, 
           BioclipseException, 
           CoreException;

    public List<ICDKMolecule> 
        loadMolecules( IFile file,
                       BioclipseUIJob<List<ICDKMolecule>> uiJob );

    public List<ICDKMolecule> loadMolecules( InputStream contents,
            IChemFormat format,
            IProgressMonitor monitor ) throws BioclipseException,
            								  CoreException,
            								  IOException;
    /**
     * Save a molecule in same format as loaded to same filename, if exists
     * @param mol The molecule to save
     * @throws IllegalStateException
     */
    @Recorded
    @PublishedMethod(params = "IMolecule mol",
            methodSummary="Saves molecule to file, if read from file.")
    @TestMethods("testSaveMolecule_IMolecule")
    public void saveMolecule(IMolecule mol)
    	          throws BioclipseException, CoreException;

    /**
     * Save a molecule in same format as loaded to same filename, if exists
     * @param mol The molecule to save
     * @param overwrite If set to true, overwrite if file exists
     * @throws IllegalStateException
     */
    @Recorded
    @PublishedMethod(
        params = "IMolecule mol, boolean overwrite",
        methodSummary = "saves mol to a file, if previously read from file. " +
            		        "Overwrite determines if existing file shall be " +
            		        "overwritten." )
    @TestMethods("testSaveMolecule_IMolecule_boolean")
    public void saveMolecule(IMolecule mol, boolean overwrite)
    	          throws BioclipseException, CoreException;

    /**
     * Save a molecule in same format as loaded
     * @param mol The molecule to save
     * @param filename Where to save, relative to workspace root
     * @throws IllegalStateException
     */
    @Recorded
    @PublishedMethod(
        params = "IMolecule mol, String filename",
        methodSummary = "saves mol to a file (filename must be a relative to " +
        		            "workspace root and folder must exist), filetype " +
        		            "must be one of the constants given by " +
        		            "getPossibleFiletypes" )
    @TestMethods("testSaveMolecule_IMolecule_String")
    public void saveMolecule(IMolecule mol, String filename)
    	          throws BioclipseException, CoreException;

    /**
     * Save a molecule in same format as loaded.
     *
     * @param mol The molecule to save
     * @param filename Where to save, relative to workspace root
     * @param overwrite If set to true, overwrite if file exists
     * @throws IllegalStateException
     */
    @Recorded
    @PublishedMethod(
        params = "IMolecule mol, String filename, boolean overwrite",
        methodSummary = "saves mol to a file (filename must be a relative " +
        		            "to workspace root and folder must exist), with a " +
        		            "given filename and overwrites determined by the " +
        		            "given boolean. File type is taken from the mol " +
        		            "object, if available. If not, then the file " +
        		            "extension is used to make a somewhat educated guess." )
    @TestMethods("testSaveMolecule_IMolecule_String_boolean")
    public void saveMolecule(IMolecule mol, String filename, boolean overwrite)
    	          throws BioclipseException, CoreException;

    /**
     * Save a molecule in same format as loaded
     * @param mol The molecule to save
     * @param file Where to save
     * @param overwrite If set to true, overwrite if file exists
     * @throws IllegalStateException
     */
    @Recorded
    @TestMethods("testSaveMolecule_IMolecule_IFile_boolean")
    public void saveMolecule(IMolecule mol, IFile file, boolean overwrite)
    	          throws BioclipseException, CoreException;

    /**
     * @param mol The molecule to save
     * @param filename Where to save, relative to workspace root
     * @param filetype Which format to save (for formats, see constants)
     * @throws IllegalStateException
     */
    @Recorded
    @PublishedMethod(
        params = "IMolecule mol, String filename, IChemFormat filetype",
        methodSummary = "saves mol to a file (filename must be a relative to " +
        		            "workspace root and folder must exist), filetype " +
        		            "must be a IChemFormat" )
    @TestMethods("testSaveMolecule_IMolecule_String_String")
    public void saveMolecule( IMolecule mol, 
                              String filename, 
                              IChemFormat filetype )
    	          throws BioclipseException, CoreException;

    /**
     * @param mol The molecule to save
     * @param target Where to save
     * @param filetype Which format to save (for formats, see constants)
     * @throws IllegalStateException
     */
    @Recorded
    public void saveMolecule( IMolecule mol, 
                              IFile target, 
                              IChemFormat filetype )
    	          throws BioclipseException, CoreException;

    /**
     * @param mol The molecule to save
     * @param filename Where to save, relative to workspace root
     * @param filetype Which format to save (for formats, see constants)
     * @param overwrite if true and file exists, overwrite
     * @throws IllegalStateException
     */
    @Recorded
    @PublishedMethod(
        params = "IMolecule mol, String filename, " +
        		     "IChemFormat filetype, boolean overwrite",
        methodSummary = "saves mol to a file (filename must be a relative to " +
        		            "workspace root and folder must exist), filetype " +
        		            "must be a IChemFormat. If overwrite=true then file " +
        		            "will be overwritten if exists." )
    @TestMethods("testSaveMolecule_IMolecule_String_String")
    public void saveMolecule( IMolecule mol, 
                              String filename, 
                              IChemFormat filetype, 
                              boolean overwrite )
    	          throws BioclipseException, CoreException;

    @Recorded
    @PublishedMethod(
        params = "IMolecule mol, String filename, " +
                 "IChemFormat filetype, boolean overwrite, " +
                 "Properties writerProperties",
        methodSummary = "saves mol to a file (filename must be a relative to " +
                        "workspace root and folder must exist), filetype " +
                        "must be a IChemFormat. If overwrite=true then file " +
                        "will be overwritten if exists. Properties can be " +
                        "given to tune the output of the writer." )
    @TestMethods("testSaveMolecule_IMolecule_String_String")
    public void saveMolecule( IMolecule mol, 
                              String filename, 
                              IChemFormat filetype, 
                              boolean overwrite,
                              Properties writerProperties)
                  throws BioclipseException, CoreException;

    /**
     * @param mol The molecule to save
     * @param target Where to save
     * @param filetype Which format to save (for formats, see constants)
     * @param overwrite if true and file exists, overwrite
     * @throws IllegalStateException
     */
    @Recorded
    public void saveMolecule( IMolecule mol, 
                              IFile target, 
                              IChemFormat filetype, 
                              boolean overwrite )
    	          throws BioclipseException, CoreException;

    /**
     * @param mol The molecule to save
     * @param target Where to save
     * @param filetype Which format to save (for formats, see constants)
     * @param overwrite if true and file exists, overwrite
     * @param writerProperties for the CDK writer
     * @throws IllegalStateException
     */
    @Recorded
    public void saveMolecule( IMolecule mol, 
                              IFile target, 
                              IChemFormat filetype, 
                              boolean overwrite,
                              Properties writerProperties)
                  throws BioclipseException, CoreException;

    /**
     * Save a list of molecules to file
     * @param molecules The molecules to save
     * @param target The IFile to save to
     * @param filetype either CML or SDF
     * @throws BioclipseException
     * @throws CoreException
     */
    @Recorded
    public void saveMolecules( List<? extends IMolecule> molecules, 
                               IFile target, 
                               IChemFormat filetype )
    	          throws BioclipseException, CoreException;

    /**
     * Save a list of molecules to file
     * @param molecules The molecules to save
     * @param path Path to file to save to
     * @param filetype either CML or SDF
     * @throws BioclipseException
     * @throws CoreException
     */
    @Recorded
    public void saveMolecules( List<? extends IMolecule> molecules, 
                               String path, 
                               IChemFormat filetype )
    	          throws BioclipseException, CoreException;


    /**
     * @param model The ChemModel to save
     * @param target Where to save
     * @param filetype Which format to save (for formats, see constants)
     * @throws IllegalStateException
     */
    @Recorded
    public void save( IChemModel model, String target, IChemFormat filetype,
                      Properties writerProperties)
    	          throws BioclipseException, CoreException;

    /**
     * Generate and saves an image of a molecule in a file specified by path.
     * If the file exists it gets overwritten.
     *  
     * @param mol The molecule that should be on the image
     * @param path The path to the image including the image-name
     */
    @PublishedMethod (params = "IMolecule molecule, String filename",
                      methodSummary = "Saves an image of a molecule in a file" )
    public void generateImage(IMolecule mol, String path);
    
    /**
     * Generate and saves an image with a specified width and height of a 
     * molecule in a file specified by path.
     * If the file exists it gets overwritten.
     *  
     * @param mol The molecule that should be on the image
     * @param path The path to the image including the image-name
     * @param width The width of the image
     * @param height The height of the image
     */
    @PublishedMethod (params = "IMolecule molecule, String filename, int " +
    		                "with, int height",
                      methodSummary = "Saves an image with a specified width " +
                      		"and height of a molecule in a file" )
    public void generateImage(IMolecule mol, String path, int width, int height);
    
    /**
     * Calculate SMILES string for an IMolecule
     * @param molecule
     * @return
     * @throws BioclipseException
     */
    @Recorded
    @PublishedMethod ( 
        params = "IMolecule molecule",
        methodSummary = "Returns the SMILES for a molecule" )
    @TestMethods( "testSaveMoleculesSDF," +
    		          "testSaveMoleculesCML," +
    		          "testSaveMoleculesCMLwithProps" )
    public String calculateSMILES (IMolecule molecule)
                  throws BioclipseException;

    @Recorded
    public BioclipseJob<String> calculateSMILES( IMolecule molecule,
                                 BioclipseJobUpdateHook<String> hook);

    /**
     * @param path
     * @return
     * @throws CoreException
     */
    @PublishedMethod (
        params = "String path",
        methodSummary = "Creates and iterator to the molecules in the file " +
        		            "at the path" )
    @TestMethods("testCreatingMoleculeIterator")
    public Iterator<ICDKMolecule> createMoleculeIterator(String path)
                                  throws CoreException, IOException, BioclipseException;

    public Iterator<ICDKMolecule> createMoleculeIterator(IFile file)
                                  throws CoreException, IOException, BioclipseException;

    /**
     * True if the fingerprint of the subStructure is a subset of the
     * fingerprint for the molecule
     *
     * @param molecule
     * @param subStructure
     * @return
     * @throws BioclipseException
     */
    @PublishedMethod (
        params = "ICDKMolecule molecule, ICDKMolecule subStructure",
        methodSummary = "Returns true if the fingerprint of the subStructure " +
        		            "is a subset of the fingerprint for the molecule")
    @Recorded
    @TestMethods("testLoadMoleculeFromSMILESFile,testFingerPrintMatch")
    public boolean fingerPrintMatches( ICDKMolecule molecule,
                                       ICDKMolecule subStructure )
                   throws BioclipseException;

    /**
     * True if the parameter molecule1 and the parameter molecule2 are isomorph.
     * Performs an isomophism test without checking fingerprints first.
     */
    @PublishedMethod (
        params = "ICDKMolecule molecule1, ICDKMolecule molecule2",
        methodSummary = "Returns true if the paramater named molecule1 and " +
                        "the paramater named molecule2 are isomorph. \n" +
                        "(Performs an isomophism test without checking " +
                        "fingerprints)" )
    @Recorded
    @TestMethods("testStructureMatches")
    public boolean areIsomorphic( ICDKMolecule molecule1,
                                  ICDKMolecule molecule2 );


    /**
     * True if the parameter substructure is a substructure to the
     * parameter molecule. Performs an isomophism test without checking
     * fingerprints first.
     */
    @PublishedMethod (
        params = "ICDKMolecule molecule, ICDKMolecule subStructure",
        methodSummary = "Returns true if the paramater named subStructure is " +
                        "a substructure of the paramater named molecule. \n" +
                        "(Performs an isomophism test without checking " +
                        "fingerprints)" )
    @Recorded
    @TestMethods("testSubStructureMatch")
    public boolean subStructureMatches( ICDKMolecule molecule,
                                        ICDKMolecule subStructure );

    @PublishedMethod (
        params = "List<ICDKMolecule> molecules, ICDKMolecule subStructure",
        methodSummary = "Returns all molecules in the given list which have " +
                        "a given subStructure as subgraph.\n" +
                        "(Performs an isomophism test without checking " +
                        "fingerprints)"
    )
    @Recorded
    @TestMethods("testSubStructureMatches_List")
    public List<ICDKMolecule> subStructureMatches(List<ICDKMolecule> molecules,
                                                  ICDKMolecule subStructure);

    @PublishedMethod (
        params = "ICDKMolecule molecule, ICDKMolecule subStructure",
        methodSummary = "Returns the substructure matches in the molecule for" +
            " the given substructure"
    )
    @Recorded
    public List<ICDKMolecule> getSubstructures(ICDKMolecule molecule,
                                               ICDKMolecule substructure)
        throws BioclipseException;

    /**
     * Creates a cdk molecule from an IMolecule
     *
     * @param m
     * @return
     * @throws BioclipseException
     */
    @PublishedMethod ( 
        params = "IMolecule m",
        methodSummary = "Creates a cdk molecule from a molecule" )
    @Recorded
    @TestMethods("testCDKMoleculeFromIMolecule")
    public ICDKMolecule asCDKMolecule( IMolecule m ) throws BioclipseException;


    /**
     * Creates a cdk molecule from a CML String
     *
     * @param m
     * @return
     * @throws BioclipseException if input is null or parse fails
     * @throws IOException if file cannot be read
     */
    @PublishedMethod ( 
        params = "String cml",
        methodSummary = "Creates a cdk molecule from a CML String" )
    @Recorded
    @TestMethods("testFromCML")
    public ICDKMolecule fromCml( String cml )
                        throws BioclipseException, IOException;

    /**
     * Creates a cdk molecule from any String.
     */
    @PublishedMethod ( 
        params = "String input file",
        methodSummary = "Creates a cdk molecule from a String. " +
        		        "Guessing the format of the String" )
    @Recorded
    @TestMethods("testFromString")
    public ICDKMolecule fromString( String cml ) 
                        throws BioclipseException, IOException;

    
    /**
     * Creates a list of molecules from a given String, guessing the format of 
     * the String.
     * 
     * @param s
     * @return
     * @throws BioclipseException
     */
    @PublishedMethod (
        params = "String s",
        methodSummary = "Creates a list of molecules from a given String, " +
        		        "guessing the format of the String" )
    public List<ICDKMolecule> moleculesFromString( String s ) 
                              throws BioclipseException;
    
    /**
     * Returns true if the given molecule matches the given SMARTS
     *
     * @param molecule
     * @param smarts
     * @return whether the given SMARTS matches the given molecule
     * @throws BioclipseException
     */
    @PublishedMethod ( 
        params = "ICDKMolecule molecule, String smarts",
        methodSummary = "Returns true if the given SMARTS matches the given " +
                        "molecule" )
    @Recorded
    @TestMethods("testSMARTSMatching")
    public boolean smartsMatches( ICDKMolecule molecule, String smarts )
                   throws BioclipseException;

    /**
     * @param filePath
     * @return the number of entries in the sdf file at the given path or
     *         0 if failed to read somehow.
     */
    @PublishedMethod ( 
        params = "String filePath",
        methodSummary = "Counts the number of entries in an SDF file at the " +
        		            "given file path. Returns 0 in case of problem." )
    @Recorded
    @TestMethods("testNumberOfEntriesInSDFString")
    public int numberOfEntriesInSDF( String filePath );

    /**
     * @param filePath
     */
    @PublishedMethod ( params = "String filePath",
                       methodSummary = "Creates a index file for the SD-file." )
    public void createSDFileIndex( String filePath);

    public void createSDFileIndex(IFile file, BioclipseUIJob<?> uiJob);
    
    /**
     * Reads files and extracts conformers if available.
     * @param path the full path to the file
     * @return a list of molecules that may have multiple conformers
     */
    @Recorded
    @PublishedMethod ( 
        params = "String path",
        methodSummary = "Loads the molecules at the path into a list, " +
        		            "and store conformers in a single molecule. " +
        		            "Currently only reads SDFiles." )
    @TestMethods("testLoadConformers")
    public List<ICDKMolecule> loadConformers( String path );

    /**
     * @param file
     * @return
     */
    public List<ICDKMolecule> loadConformers( IFile file);

    @Recorded
    @PublishedMethod( 
        params = "String path",
        methodSummary = "Returns an iterator to the molecules in an IFile " +
        		            "that might contain conformers." )
    public Iterator<ICDKMolecule> createConformerIterator( String path );

    @PublishedMethod ( 
        params = "IMolecule molecule",
        methodSummary = "Calculate and return the molecular weight for the " +
                        "molecule." )
    @Recorded
    @TestMethods("testLoadMoleculeFromSMILESFile")
    public double calculateMass( IMolecule molecule )
                  throws BioclipseException;


    @TestMethods( "testNumberOfEntriesInSDFIFileUIJob" )
    public int numberOfEntriesInSDF( IFile file, 
                                     BioclipseUIJob<Integer> uiJob );

    @Recorded
    @PublishedMethod(
        params = "List<IMolecule> molecule",
        methodSummary = "Create 2D coordinates for the given molecules")
    @TestMethods("testGenerate2DCoordinates")
    public List<ICDKMolecule> 
           generate2dCoordinates( List<? extends IMolecule> molecules ) 
           throws Exception;

    @Recorded
    @PublishedMethod(
        params = "IMolecule molecule",
        methodSummary = "Create 2D coordinates for the given molecule")
    @TestMethods("testGenerate2DCoordinatesSingle")
    public ICDKMolecule generate2dCoordinates(IMolecule molecule) 
                        throws Exception;

    @Recorded
    @TestMethods("testGenerate2DCoordinates")
    public void generate2dCoordinates( List<? extends IMolecule> molecules,
                                       BioclipseUIJob<List<IMolecule>> uiJob) 
           throws Exception;

    @Recorded
    @TestMethods("testGenerate2DCoordinatesSingle")
    public void generate2dCoordinates(IMolecule molecule,
                                      BioclipseUIJob<IMolecule> uiJob)
                        throws Exception;

    @Recorded
    @PublishedMethod(
        params = "List<IMolecule> molecule",
        methodSummary = "Create 3D coordinates for the given molecules" )
    @TestMethods("testGenerate3DCoordinates")
    public List<ICDKMolecule> generate3dCoordinates(List<IMolecule> molecule) 
                           throws BioclipseException;

    @Recorded
    @PublishedMethod(
        params = "IMolecule molecule",
        methodSummary = "Create 3D coordinates for the given molecule" )
    @TestMethods("testGenerate3DCoordinatesSingle")
    public ICDKMolecule generate3dCoordinates(IMolecule molecule) throws BioclipseException;

    @Recorded
    @TestMethods("testGenerate3DCoordinates")
    public void generate3dCoordinates(List<IMolecule> molecule,
                                      BioclipseUIJob<List<IMolecule>> uiJob) 
                           throws BioclipseException;

    @Recorded
    @TestMethods("testGenerate3DCoordinatesSingle")
    public void generate3dCoordinates(IMolecule molecule,
                                      BioclipseUIJob<IMolecule> uiJob) 
    throws BioclipseException;

    
    @Recorded
	  public void saveMol2(ICDKMolecule mol2, String filename) 
                throws InvocationTargetException, 
                       BioclipseException, 
                       CoreException;

    @Recorded
    @PublishedMethod(
        params = "ICDKMolecule molecule, String filename",
        methodSummary = "Saves a molecule in the MDL molfile V2000 format " +
        		            "(filename must be a relative to workspace root and " +
    								    "folder must exist)" )
    @TestMethods("testSaveMDLMolfile")
    public void saveMDLMolfile(ICDKMolecule mol, String filename) 
                throws InvocationTargetException, 
                       BioclipseException, 
                       CoreException;

    @Recorded
    @PublishedMethod(
        params = "ICDKMolecule molecule, String filename",
        methodSummary = "Saves a molecule in the Chemical Markup Language " +
        		            "format (filename must be relative to workspace " +
        		            "root and folder must exist). Example of file " +
        		            "String: \"/Virtual/bla.cml\"" )
    @TestMethods("testSaveCML")
    public void saveCML(ICDKMolecule cml, String filename) 
                throws InvocationTargetException, 
                       BioclipseException, 
                       CoreException;

    /**
     * Loads molecules from a SMILES file.
     *
     * @param path String with the path to the file
     * @return a list of molecules
     * @throws CoreException
     * @throws IOException
     */
    @Recorded
    @PublishedMethod( 
         params = "String path",
         methodSummary = "Loads molecules from a SMILES file at a given path " +
         		             "into a list of molecules" )
    @TestMethods( "testLoadMoleculeFromSMILESFileDirectly," +
    		          "testLoadMoleculeFromSMILESFile" )
    public List<ICDKMolecule> loadSMILESFile(String path) 
                              throws CoreException, IOException;

	public List<ICDKMolecule> loadSMILESFile(IFile file) 
	                          throws CoreException, IOException;

	public List<ICDKMolecule> 
	    loadSMILESFile( IFile file,
	                    BioclipseUIJob<List<ICDKMolecule>> uiJob )
	       throws CoreException, IOException;

	/**
	 * Return number of molecules in file
	 * @param file
	 * @return
	 */
    @Recorded
    @PublishedMethod( params = "String path",
                      methodSummary = "Returns number of molecules in file.")
    @TestMethods("testNoMoleucles_String")
	  public int getNoMolecules(String path);

	  /**
	   * Return number of molecules in file
	   * @param file
	   * @return
	   */
    @Recorded
    @PublishedMethod( params = "String path",
                      methodSummary = "Returns information about molecules in file.")
	  @TestMethods("testGetInfo")
    public MoleculesInfo getInfo(String path);

    /**
     * Depict if molecule has 2D coordinates available.
     * @param mol IMolecule to depict 2D for
     * @return
     * @throws BioclipseException if calculation failed
     */
    @Recorded
    @PublishedMethod( 
        params = "IMolecule mol",
        methodSummary = "Returns true if molecule has 2D coordinates, false " +
        		            "otherwise." )
    @TestMethods("testHas2d")
	  boolean has2d(IMolecule mol) throws BioclipseException;

    /**
     * Depict if molecule has 3D coordinates available.
     * @param mol IMolecule to depict 3D for
     * @return
     * @throws BioclipseException if calculation failed
     */
    @Recorded
    @PublishedMethod( 
        params = "IMolecule mol",
        methodSummary = "Returns true if molecule has 3D coordinates, false " +
        		            "otherwise." )
    @TestMethods("testHas3d")
	  boolean has3d(IMolecule mol) throws BioclipseException;

    @Recorded
    @PublishedMethod(
        params = "IMolecule mol",
        methodSummary = "Adds explicit hydrogens to this molecule" )
    @TestMethods("testAddExplicitHydrogens")
    public ICDKMolecule addExplicitHydrogens(IMolecule molecule) throws Exception;

    @Recorded
    @PublishedMethod(params = "IMolecule mol",
                     methodSummary="Adds implicit hydrogens to this molecule")
    @TestMethods("testAddImplicitHydrogens")
   	public ICDKMolecule addImplicitHydrogens(IMolecule molecule) 
                     throws BioclipseException, InvocationTargetException;

    @Recorded
    @PublishedMethod(methodSummary="Creates an empty molecule list.")
    public List<ICDKMolecule> createMoleculeList()
                           throws BioclipseException, InvocationTargetException;

    @Recorded
    @TestMethods("testCreateSDFile_IFile_IMoleculeArray")
   	public void saveSDFile( IFile file, 
   	                       List<? extends IMolecule> entries ) 
                throws BioclipseException, InvocationTargetException;

    @Recorded
    public void saveSDFile( IFile file, 
                            List<? extends IMolecule> entries ,
                            BioclipseUIJob<Void> uiJob) 
                throws BioclipseException, InvocationTargetException;
    
    @Recorded
    @PublishedMethod(
        params = "String file, List<IMolecule> entries",
        methodSummary = "Creates an sd file from a number of molecules" )
    @TestMethods("testCreateSDFile_String_IMoleculeArray")
    public void saveSDFile(String file, List<? extends IMolecule> entries ) 
                throws BioclipseException, InvocationTargetException;

    @Recorded
    @TestMethods("testExtractFromSDFile_IFile_int_int")
    public List<ICDKMolecule> extractFromSDFile( IFile file, 
                                              int startentry, 
                                              int endentry ) 
                           throws BioclipseException, InvocationTargetException;

    @Recorded
    @TestMethods("testExtractFromSDFile_IFile_int_int")
    public List<ICDKMolecule> extractFromSDFile( IFile file,
                                                 int startentry,
                                                 int endentry,
                                                 IProgressMonitor monitor)
                           throws BioclipseException, InvocationTargetException;
    @Recorded
    @PublishedMethod(
        params = "String file, int startentry, int endentry",
        methodSummary = "Extracts a number of entries from an sd file. " +
        		            "Does not read the complete file for this." )
    @TestMethods("testExtractFromSDFile_String_int_int")
    public List<ICDKMolecule> extractFromSDFile( String file, 
                                              int startentry, 
                                              int endentry ) 
                           throws BioclipseException, InvocationTargetException;

    @PublishedMethod(
        params = "String file, String property, Collection values",
        methodSummary = "Extracts moleculs indicated by index in values " +
        		            "to a list" ) 
    public List<ICDKMolecule> extractFromSDFile( String file,
                                                 String property,
                                                 Collection<String> value );
    
    public List<ICDKMolecule> extractFromSDFile( IFile file,
                                                 String property,
                                                 Collection<String> value );

    public Map<Integer,String> createSDFPropertyMap( IFile file,
                                                     String property )
                               throws CoreException, IOException;
    
    @PublishedMethod(
        params = "String file, String property",
        methodSummary = "Create a index for molecules that contains a " +
        		            "certain property" )
    public Map<Integer,String> createSDFPropertyMap( String file,
                                                     String property )
                               throws CoreException, IOException;

    @Recorded
    @PublishedMethod(params = "ICDKMolecule m",
                     methodSummary = "Gives the molecularformula as a String")
    public String molecularFormula( ICDKMolecule m );

    @Recorded
    @PublishedMethod(
         params = "IContentType type",
         methodSummary = "Determines the IChemFormat equivalent of the given " +
                         "content type" )
    public IChemFormat determineFormat(IContentType type);

    @Recorded
    @PublishedMethod(
         params = "String type",
         methodSummary = "Returns the IChemFormat for the given type")
    @TestMethods("testGetFormat")
    public IChemFormat getFormat(String type);

    @Recorded
    @PublishedMethod(
         methodSummary = "Returns all available IChemFormats")
    public String getFormats();

    @Recorded
    @PublishedMethod(
         params = "String type",
         methodSummary = "Makes an educated guess if the file format based " +
                         "on the file extension alone and returns a CDK" +
                         "IChemFormat object." )
    @TestMethods("testGuessFormatFromExtension")
    public IChemFormat guessFormatFromExtension(String type);

    @Recorded
    @PublishedMethod(
         params = "String SMARTS",
         methodSummary = "Determines if a SMARTS string can be interpreted " +
         		             "by CDK." )
    @TestMethods("testSMARTSonFile")
    boolean isValidSmarts( String SMARTS );

    @Recorded
    @PublishedMethod(
         params = "ICDKMolecule molecule, String SMARTS",
         methodSummary = "Query a molecule for a SMARTS string and return a " +
             "list of IAtomContainers with the matches." )
    @TestMethods("testSMARTSonFile")
    public List<IAtomContainer> getSmartsMatches( ICDKMolecule molecule, 
                                                  String SMARTS )
                                throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params = "IMolecule molecule: atom container to fragmentate",
         methodSummary = "Splits up an atom container into a List of " +
         		"IAtomContainer's with fully connected molecules")
    @TestMethods("testSMARTSonFile")
    public List<IAtomContainer> partition( IMolecule molecule ) 
                                throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params = "IMolecule molecule",
         methodSummary = "Returns true if the given atom container is " +
         		"fully connected. Use partition directly if that is the " +
         		"antipicated next action, to no repeat to same computation.")
    public boolean isConnected(IMolecule molecule) throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params = "IMolecule molecule: molecule to calculate the total formal" +
         		      " charge for.",
         methodSummary = "Calculates the total formal charge.")
    @TestMethods("testSMARTSonFile")
    public int totalFormalCharge(IMolecule molecule)
               throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params = "IMolecule calculateFor, IMolecule reference",
         methodSummary = "Calculate tanimoto similarity of calculateFor and " +
         		             "reference via CDK fingerprint.")
    @TestMethods("testSingleTanimoto")
    public float calculateTanimoto(IMolecule calculateFor, IMolecule reference)
                 throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params = "BitSet fingerprint1, BitSet fingerprint2",
         methodSummary = "Calculate tanimoto similarity between two " +
                         "fingerprints" )
    @TestMethods("testCalculateTanimoto_BitSet_BitSet")
    public float calculateTanimoto( BitSet fingerprint1,
                                    BitSet fingerprint2 ) 
                 throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params = "BioList<IMolecule> calculateFor, BitSet reference",
         methodSummary = "Calculate tanimoto similarity of a molecule " +
                         "(calculateFor) to a BitSet (reference) via CDK " +
                         "fingerprint." )
    @TestMethods("testCalculateTanimoto_IMolecule_BitSet")
    public float calculateTanimoto( IMolecule calculateFor, BitSet reference ) 
                 throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params = "BioList<IMolecule> calculateFor, IMolecule reference",
         methodSummary = "Calculate tanimoto similarity of a list of " +
         		             "molecules (calculateFor) to another molecule " +
         		             "(reference) via CDK fingerprint." )
    @TestMethods("testMultipleTanimoto")
    public List<Float> calculateTanimoto( List<IMolecule> calculateFor, 
                                          IMolecule reference )
                       throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params = "List<IMolecule> molecules, String filename",
         methodSummary = "Calculate tanimoto similarities between all " +
         		"molecule in the list using CDK fingerprints." )
    public String calculateTanimoto(List<IMolecule> calculateFor,
                                    String filename)
    throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params = "List<IMolecule> molecules, String filename",
         methodSummary = "Calculate similarities between all " +
                "molecules in the list based on the RMSD between the " +
                "the Kabsch-aligned MCSSs." )
    public String calculateRMSD(List<IMolecule> calculateFor,
                                String filename)
    throws BioclipseException;

    @TestMethods("testMultipleTanimoto")
    public void calculateTanimoto( List<IMolecule> calculateFor, 
                                          IMolecule reference, 
                                          BioclipseUIJob<List<Float>> uiJob )
                       throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
         params = "IMolecule molecule",
         methodSummary = "Returns a MDL V2000 molfile serialization" )
    @TestMethods("testGetMDLMolfileString()")
    public String getMDLMolfileString( IMolecule molecule ) 
                  throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params = "ICDKMolecule molecule, Object propertyName",
         methodSummary = "Returns the property value for the given property " +
         		             "name" )
    @TestMethods("testGetSetProperty()")
    public Object getProperty(ICDKMolecule molecule, Object propertyName);

    @Recorded
    @PublishedMethod(
         params = "ICDKMolecule molecule, Object propertyName, Object " +
         		      "propertyValue",
         methodSummary = "Sets the property value for the given property " +
         		             "name. Returns the old value, or null if it was " +
         		             "yet unset." )
    @TestMethods("testGetSetProperty()")
    public Object setProperty( ICDKMolecule molecule, 
                               Object propertyName,
                               Object propertyValue );

    @Recorded
    @PublishedMethod(
         params = "ICDKMolecule molecule",
         methodSummary = "Removes implicit hydrogens" )
    @TestMethods("testRemoveImplicitHydrogens()")
    public ICDKMolecule removeImplicitHydrogens(ICDKMolecule molecule);

    @Recorded
    @PublishedMethod(
         params = "ICDKMolecule molecule",
         methodSummary = "Removes explicit hydrogens" )
    @TestMethods("testRemoveExplicitHydrogens()")
    public ICDKMolecule removeExplicitHydrogens(ICDKMolecule molecule);

    public void loadMolecule( IFile file,
                              BioclipseUIJob<ICDKMolecule> bioclipseUIJob );

    public int numberOfEntriesInSDF( IFile file,
                                     IProgressMonitor monitor );

    @Recorded
    @PublishedMethod(
         params = "ICDKMolecule molecule",
         methodSummary = "Clones the CDK data model." )
    public ICDKMolecule clone(ICDKMolecule molecule) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
         params="molecules List of molecules",
         methodSummary="Determines the Maximal Common SubStructure (MCSS) for" +
             " the list of molecules."
    )
    @TestMethods("testMCSS")
    public ICDKMolecule mcss(List<IMolecule> molecules)
    throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params="molecules List of molecules",
         methodSummary="Aligns the molecular geometries to the first molecule " +
             "based on their MCSS."
    )
    public List<ICDKMolecule> kabsch(List<IMolecule> molecules)
    throws BioclipseException;

    /**
     * This method can be used to call the saveSDFile with an already 
     * constructed monitor. For example a SubProgressMonitor or a 
     * NullProgressMonitor to force running in the same thread...
     * 
     * @param sdfile
     * @param molecules
     * @param nullProgressMonitor
     */
    public void saveSDFile( String sdfile,
                            List<? extends IMolecule> molecules,
                            IProgressMonitor monitor );

    public void saveSDFile( IFile molFile, List<? extends IMolecule> mols,
                            IProgressMonitor monitor );


    @Recorded
    @PublishedMethod(
         params="List<IMolecule> mols, int parts",
         methodSummary="Split a list of molecules in N parts.")
    public List<List<IMolecule>> randomSplit(List<IMolecule> mols, int parts);
    @Recorded
    @PublishedMethod(
         params="List<IMolecule> mols, double firstRatio",
         methodSummary="Split a list of molecules in 2 parts by a ratio.")
    public List<List<IMolecule>> randomSplit2parts(List<IMolecule> mols_in, double firstRatio);

    public List<ICDKMolecule> loadSMILESFile( IFile file,
                                              IProgressMonitor monitor ) 
                                              throws CoreException, IOException;

    @Recorded
    @PublishedMethod(
         params="String sdFile, ICDKMolecule molecule",
         methodSummary="Append a molecule at the end of a given sd file."
    )
    public void appendToSDF( String sdFile, ICDKMolecule molecule ) throws BioclipseException;
    
    public void appendToSDF( IFile sdFile, ICDKMolecule molecule ) throws BioclipseException;

    @Recorded
    @PublishedMethod(
         params="IMolecule molecule",
         methodSummary="Generates a list of tautomers."
    )
    public List<ICDKMolecule> getTautomers(IMolecule molecule) throws BioclipseException;
}
