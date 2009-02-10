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
 *     Stefan Kuhn
 *
 ******************************************************************************/
package net.bioclipse.cdk.business;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.bioclipse.cdk.domain.CDKConformer;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.MoleculesInfo;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioList;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ui.jobs.BioclipseUIJob;

import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.ConformerContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.FingerprinterTool;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.CDKSourceCodeWriter;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.FormatFactory;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLRXNWriter;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.Mol2Writer;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.SMILESWriter;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.formats.SMILESFormat;
import org.openscience.cdk.io.iterator.IteratingMDLConformerReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.io.random.RandomAccessReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.modeling.builder3d.ModelBuilder3D;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
/**
 * The manager class for CDK. Contains CDK related methods.
 *
 * @author olas, jonalv
 *
 */
public class CDKManager implements ICDKManager {

    private static final Logger logger = Logger.getLogger(CDKManager.class);

    // ReaderFactory used to instantiate IChemObjectReaders
    private static ReaderFactory readerFactory;

    // ReaderFactory used solely to determine chemical file formats
    private static FormatFactory formatsFactory;

    static {
        readerFactory = new ReaderFactory();
        CDKManagerHelper.registerSupportedFormats(readerFactory);
        formatsFactory = new FormatFactory();
        CDKManagerHelper.registerAllFormats(formatsFactory);
    }

    public String getNamespace() {
        return "cdk";
    }

    /*
     * Load a molecule from a file. If many molecules, just return first. To
     * return a list of molecules, use loadMolecules(...)
     */
    public ICDKMolecule loadMolecule(String path) throws IOException,
                                                         BioclipseException,
                                                         CoreException {
        return loadMolecule( ResourcePathTransformer.getInstance()
                                                    .transform(path), null);
    }

  	public ICDKMolecule loadMolecule(IFile file) throws IOException,
  	                                                    BioclipseException,
  	                                                    CoreException {
  	    return loadMolecule(file, null);
  	}

  	public ICDKMolecule loadMolecule(IFile file, IProgressMonitor monitor)
  	                    throws IOException, BioclipseException, CoreException {

        ICDKMolecule loadedMol = loadMolecule(
            file.getContents(),
            monitor,
            determineIChemFormat(file)
        );
  	    loadedMol.setResource(file);

  	    return loadedMol;
  	}

  	public ICDKMolecule loadMolecule( InputStream instream,
  	                                  IProgressMonitor monitor,
  	                                  IChemFormat format)
  	                    throws BioclipseException, IOException {

  	    if (monitor == null) {
  	        monitor = new NullProgressMonitor();
  	    }

  	    List<ICDKMolecule> moleculesList = new ArrayList<ICDKMolecule>();

  	    try {
  	        // Create the reader
  	        ISimpleChemObjectReader reader
  	            = readerFactory.createReader(format);

            if (reader == null) {
                throw new BioclipseException("Could not create reader in CDK.");
            }

  	        try {
  	            reader.setReader(instream);
  	        }
  	        catch ( CDKException e1 ) {
  	            throw new RuntimeException(
  	                "Failed to set the reader's inputstream", e1);
  	        }

  	        IChemFile chemFile = new org.openscience.cdk.ChemFile();

  	        // Do some customizations...
  	        CDKManagerHelper.customizeReading(reader, chemFile);

  	        // Read file
  	        try {
  	            chemFile = (IChemFile) reader.read(chemFile);
  	        }
  	        catch (CDKException e) {
  	            throw new RuntimeException("Failed to read file", e);
  	        }

  	        // Store the chemFormat used for the reader
  	        IResourceFormat chemFormat = reader.getFormat();
  	        logger.debug("Read CDK chemfile with format: "
  	                     + chemFormat.getFormatName());

  	        List<IAtomContainer> atomContainersList
  	            = ChemFileManipulator.getAllAtomContainers(chemFile);
  	        int nuMols = atomContainersList.size();
  	        logger.debug("This file contained: " + nuMols + " molecules");

  	        // If we have one AtomContainer, return a CDKMolecule with this ac
  	        // If we have more than one AtomContainer, return a list of the
  	        // molecules
  	        // FIXME: requires common interface for CDKImplementations
  	        if (atomContainersList.size() == 1) {
  	            CDKMolecule retmol
  	                = new CDKMolecule(
  	                    (IAtomContainer) atomContainersList.get(0) );
  	            return retmol;
  	        }

  	        for (int i = 0; i < atomContainersList.size(); i++) {
  	            IAtomContainer ac = null;
  	            Object obj = atomContainersList.get(i);
  	            if (obj instanceof org.openscience.cdk.interfaces.IMolecule) {
  	                ac = (org.openscience.cdk.interfaces.IMolecule) obj;
  	            }
  	            else if (obj instanceof IAtomContainer) {
  	                ac = (IAtomContainer) obj;
  	            }
  	            CDKMolecule mol = new CDKMolecule(ac);
  	            String molName = (String)ac.getProperty(CDKConstants.TITLE);
  	            if (molName != null && !(molName.length() > 0)) {
  	                mol.setName(molName);
  	            }
  	            else {
  	                mol.setName("Molecule " + i);
  	            }
  	            moleculesList.add(mol);
  	        }
  	    }
  	    finally {
  	        monitor.done();
  	    }
  	    // Just return the first molecule. To return all, use loadMolecules(..)
  	    return moleculesList.get(0);
  	}

  	/**
  	 * Load a molecules from a file.
  	 *
  	 * @throws CoreException
  	 */
  	public List<ICDKMolecule> loadMolecules(String path)
  	                          throws IOException,
  	                                 BioclipseException,
  	                                 CoreException {

  	    return loadMolecules( ResourcePathTransformer.getInstance()
  	                                                 .transform(path),
  	                                                 (IProgressMonitor)null );
  	}

  	/**
  	 * Load one or more molecules from an InputStream and return a
  	 * CDKMoleculeList.
  	 *
  	 * @throws CoreException
  	 */
  	public List<ICDKMolecule> loadMolecules( IFile file,
  	                                         IProgressMonitor monitor )
  	                          throws IOException,
  	                                 BioclipseException,
  	                                 CoreException {

  	    return loadMolecules(file, null, monitor);
  	}

  	public List<ICDKMolecule> loadMolecules(
  	                                         IFile file,
  	                                         BioclipseUIJob<List<ICDKMolecule>> uiJob ) {

  	    throw new UnsupportedOperationException(
        "This manager method should not be called");
  	}

  	public List<ICDKMolecule> loadMolecules( IFile file,
  	                                         IChemFormat format,
  	                                         IProgressMonitor monitor )
  	                          throws IOException,
  	                                 BioclipseException,
  	                                 CoreException {

  	    if (monitor == null) {
  	        monitor = new NullProgressMonitor();
  	    }

  	    List<ICDKMolecule> moleculesList = new BioList<ICDKMolecule>();

  	    int ticks = 10000;

  	    try {
  	        monitor.beginTask("Reading file", ticks);
  	        System.out.println( "no formats supported: "
  	                            + readerFactory.getFormats().size() );
  	        ISimpleChemObjectReader reader = null;

            // Create the reader
  	        if (format == null) {
  	            reader = readerFactory.createReader( file.getContents() );
  	        }
  	        else {
  	            reader = readerFactory.createReader(format);
  	        }

  	        try {
  	            reader.setReader( file.getContents() );
  	        }
  	        catch (CDKException e1) {
  	            throw new BioclipseException(
                    "Could not set the reader's input." );
  	        }

  	        if (reader == null) {

  	            // Try SMILES
  	            List<ICDKMolecule> moleculesList2 = loadSMILESFile(file);
  	            if (moleculesList2 != null && moleculesList2.size() > 0)
  	                return moleculesList2;

  	            // Ok, not even SMILES works
  	            throw new BioclipseException(
  	                "Could not create reader in CDK." );
  	        }

  	        IChemFile chemFile = new org.openscience.cdk.ChemFile();

  	        // Do some customizations...
  	        CDKManagerHelper.customizeReading(reader, chemFile);

  	        // Read file
  	        try {
  	            chemFile = (IChemFile) reader.read(chemFile);
  	        }
  	        catch (CDKException e) {
  	            // TODO Auto-generated catch block
  	            LogUtils.debugTrace(logger, e);
  	        }

  	        // Store the chemFormat used for the reader
  	        IResourceFormat chemFormat = reader.getFormat();
  	        System.out.println( "Rad CDK chemfile with format: "
  	                            + chemFormat.getFormatName() );

  	        List<IAtomContainer> atomContainersList
  	            = ChemFileManipulator.getAllAtomContainers(chemFile);

  	        int nuMols = atomContainersList.size();
  	        int currentMolecule = 0;

  	        System.out.println( "This file contained: "
  	                            + nuMols + " molecules" );

  	        for (int i = 0; i < atomContainersList.size(); i++) {

  	            IAtomContainer ac = null;
  	            Object obj = atomContainersList.get(i);

  	            if (obj instanceof org.openscience.cdk.interfaces.IMolecule) {
  	                ac = (org.openscience.cdk.interfaces.IMolecule) obj;
  	            }
  	            else if (obj instanceof IAtomContainer) {
  	                ac = (IAtomContainer) obj;
  	            }

  	            CDKMolecule mol = new CDKMolecule(ac);

  	            // Set up name for molecule
  	            String moleculeName = file.getName() + "-" + i;

  	            // If only one mol, no trailing digit
  	            if (atomContainersList.size() == 1) {
  	                moleculeName = file.getName();
  	            }

  	            // If there's a CDK property TITLE (read from file), use that
  	            // as name
  	            if (ac instanceof org.openscience.cdk.interfaces.IMolecule) {

  	                org.openscience.cdk.interfaces.IMolecule imol
  	                    = (org.openscience.cdk.interfaces.IMolecule) ac;

  	                String molName
  	                    = (String) 
  	                imol.getProperty("PUBCHEM_IUPAC_TRADITIONAL_NAME");

                    if ( molName == null || ( molName.equals("") ) )
  	                    molName
  	                        = (String) imol.getProperty(CDKConstants.TITLE);

  	                if ( molName != null && !( molName.equals("") ) ) {
  	                    moleculeName = molName;
                    }
  	            }

  	            mol.setName(moleculeName);
  	            moleculesList.add(mol);
  	            monitor.worked( (int) (ticks / nuMols) );

  	            monitor.subTask( "Loaded molecule:" +
  	                             ++currentMolecule + "/" + nuMols );

  	            if ( monitor.isCanceled() ) {
  	                throw new OperationCanceledException();
  	            }
  	        }
  	    }
  	    finally {
  	        monitor.done();
  	    }
  	    return moleculesList;
  	}

  	public String calculateSMILES(IMolecule molecule)
  	              throws BioclipseException {
  	    return molecule.getSMILES();
  	}

  	public void save(IChemModel model, String target, String filetype)
  	            throws BioclipseException, CDKException, CoreException {
  	    save( model,
  	          ResourcePathTransformer.getInstance().transform(target),
  	          filetype,
  	          null );
  	}

  	public void save( IChemModel model,
  	                  IFile target,
  	                  String filetype,
  	                  IProgressMonitor monitor )
  	            throws BioclipseException, CDKException, CoreException {

  	    if (monitor == null)
  	        monitor = new NullProgressMonitor();

  	    try {
  	        int ticks = 10000;
  	        monitor.beginTask("Writing file", ticks);
  	        StringWriter writer = new StringWriter();

  	        if (filetype.equals(mol) || filetype.equals(mdl)) {
  	            MDLWriter mdlWriter = new MDLWriter(writer);
  	            mdlWriter.write(model);
  	        } else if (filetype.equals(cml)) {
  	            CMLWriter cmlWriter = new CMLWriter(writer);
  	            cmlWriter.write(model);
  	        } else if (filetype.equals(mol2)) {
  	            Mol2Writer mol2Writer = new Mol2Writer();
  	            IAtomContainer mol
  	                = ChemModelManipulator.getAllAtomContainers(model).get(0);
  	            org.openscience.cdk.interfaces.IMolecule nmol = mol
  	            .getBuilder().newMolecule(mol);
  	            mol2Writer.write(nmol);
  	        } else if (filetype.equals(rxn)) {
  	            MDLRXNWriter cmlWriter = new MDLRXNWriter(writer);
  	            cmlWriter.write(model);
  	        } else if (filetype.equals(smi)) {
  	            SMILESWriter cmlWriter = new SMILESWriter(writer);
  	            cmlWriter.write(model);
  	        } else if (filetype.equals(cdk)) {
  	            CDKSourceCodeWriter cmlWriter = new CDKSourceCodeWriter(writer);
  	            cmlWriter.write(model);
  	        } else if (filetype.equals(sdf)) {
  	            SDFWriter cmlWriter = new SDFWriter(writer);
  	            cmlWriter.write(model.getMoleculeSet());
  	        } else {
  	            // by default, save as CML, not matter what the extension is
  	            // CML just needs some extra promotion love
  	            CMLWriter cmlWriter = new CMLWriter(writer);
  	            cmlWriter.write(model);
  	        }

  	        if (target.exists()) {
  	            try {
  	                target.setContents(
  	                    new ByteArrayInputStream(writer.toString()
  	                                                   .getBytes("US-ASCII")),
  	                    false,
  	                    true,
  	                    monitor );
  	            }
  	            catch (UnsupportedEncodingException e) {
  	                throw new BioclipseException(e.getMessage(), e);
  	            }
  	        } else {
  	            try {
  	                target.create(
  	                    new ByteArrayInputStream(writer.toString()
  	                                                   .getBytes("US-ASCII")),
  	                    false,
  	                    monitor );
  	            }
  	            catch (UnsupportedEncodingException e) {
  	                throw new BioclipseException(e.getMessage());
  	            }
  	        }
  	        monitor.worked(ticks);
  	    }
  	    finally {
  	        monitor.done();
  	    }
  	}

  	public void saveMolecule(IMolecule mol)
  	            throws BioclipseException, CDKException, CoreException {
  	    saveMolecule(mol, false);
  	}

  	public void saveMolecule(IMolecule mol, String filename)
  	            throws BioclipseException, CDKException, CoreException {
  	    saveMolecule(mol, filename, false);
  	}

  	public void saveMolecule(IMolecule mol, boolean overwrite)
  	            throws BioclipseException, CDKException, CoreException {

  	    if (mol.getResource() == null)
  	        throw new BioclipseException(
  	            "Molecule does not have an associated File." );

  	    saveMolecule( mol,
  	                  (IFile)mol.getResource(),
  	                  mol.getResource().getFileExtension(),
  	                  overwrite );
  	}

  	public void saveMolecule(IMolecule mol, String filename, boolean overwrite)
  	            throws BioclipseException, CDKException, CoreException {

  	    IFile file = ResourcePathTransformer.getInstance().transform(filename);
  	    saveMolecule(mol, file, overwrite);
  	}

  	public void saveMolecule(IMolecule mol, IFile file, boolean overwrite)
  	            throws BioclipseException, CDKException, CoreException {
  	    saveMolecule(mol, file, file.getFileExtension(), overwrite);
  	}

  	public void saveMolecule( IMolecule mol_in,
  	                          String filename,
  	                          String filetype )
                throws BioclipseException, CDKException, CoreException {
  	    this.saveMolecule(mol_in, filename, filetype, false);
  	}

  	public void saveMolecule(IMolecule mol_in, IFile target, String filetype)
  	            throws BioclipseException, CDKException, CoreException {
  	    this.saveMolecule(mol_in, target, filetype, false);
  	}

  	public void saveMolecule( IMolecule mol_in,
  	                          IFile target,
  	                          String filetype,
  	                          boolean overwrite)
                throws BioclipseException, CDKException, CoreException {

  	    if ( target.exists() && overwrite == false ) {
  	        throw new BioclipseException("File already exists!");
  	    }

  	    ICDKMolecule mol = create(mol_in);
  	    IChemModel chemModel = mol.getAtomContainer()
  	                              .getBuilder().newChemModel();
  	    chemModel.setMoleculeSet( chemModel.getBuilder().newMoleculeSet() );
  	    chemModel.getMoleculeSet().addAtomContainer( mol.getAtomContainer() );

  	    this.save(chemModel, target, filetype, null);
  	}

  	public void saveMolecule( IMolecule mol,
  	                          String filename,
  	                          String filetype,
  	                          boolean overwrite )
  	            throws BioclipseException, CDKException, CoreException {

  	    saveMolecule( mol,
  	                  ResourcePathTransformer.getInstance()
  	                                         .transform(filename),
  	                  filetype,
  	                  overwrite );
  	}

  	/**
  	 * Delegate to IFile via ResourcePathTransformer
  	 */
  	public void saveMolecules( List<? extends IMolecule> molecules,
  	                           String path,
  	                           String filetype )
  	            throws BioclipseException, CDKException, CoreException {

  	    saveMolecules( molecules,
  	                   ResourcePathTransformer.getInstance().transform(path),
  	                   filetype) ;
  	}

  	/**
  	 * Save a list of molecules in SDF or CML
  	 */
  	public void saveMolecules( List<? extends IMolecule> molecules,
  	                           IFile target,
  	                           String filetype )
  	            throws BioclipseException, CDKException, CoreException {

  	    if ( filetype.equalsIgnoreCase(cml) ||
  	         filetype.equalsIgnoreCase(sdf) ) {

      	    IChemModel chemModel = new ChemModel();
      	    chemModel.setMoleculeSet( chemModel.getBuilder()
      	                                       .newMoleculeSet() );
      	    for (IMolecule mol : molecules) {

      	        ICDKMolecule cdkmol = create(mol);
      	        org.openscience.cdk.interfaces.IMolecule imol = null;

      	        if (cdkmol instanceof IMolecule) {
      	            imol = (org.openscience.cdk.interfaces.IMolecule)
      	                   cdkmol.getAtomContainer();
      	        }
      	        else {
      	            imol = new Molecule( cdkmol.getAtomContainer() );
      	            //Properties are lost in this CDK operation, so copy them
      	            imol.setProperties( cdkmol.getAtomContainer()
      	                                      .getProperties() );
      	        }

      	        chemModel.getMoleculeSet().addMolecule(imol);
      	    }

      	    this.save(chemModel, target, filetype, null);
  	    }
  	    else {
  	        throw new IllegalArgumentException("Multiple molecules can only " +
                "be serialized in SDF or CML.");
  	    }
  	}

  	/**
  	 * Create molecule from SMILES.
  	 *
  	 * @throws BioclipseException
  	 */
  	public ICDKMolecule fromSMILES(String smilesDescription)
  	                    throws BioclipseException {

  	    SmilesParser parser
  	        = new SmilesParser( DefaultChemObjectBuilder.getInstance() );

  	    try {
  	        org.openscience.cdk.interfaces.IMolecule mol
  	            = parser.parseSmiles(smilesDescription);
  	        return new CDKMolecule(mol);
  	    }
  	    catch (InvalidSmilesException e) {
  	        throw new BioclipseException("SMILES string is invalid", e);
  	    }
  	}

  	/**
  	 * Create molecule from String
  	 *
  	 * @throws BioclipseException
  	 * @throws IOException
  	 */
  	public ICDKMolecule fromCml(String molstring)
  	                    throws BioclipseException, IOException {

  	    if (molstring == null)
  	        throw new BioclipseException("Input cannot be null");

  	    ByteArrayInputStream bais
  	        = new ByteArrayInputStream( molstring.getBytes() );

  	    return loadMolecule( (InputStream)bais,
  	                         new NullProgressMonitor(),
  	                         (IChemFormat)CMLFormat.getInstance() );
  	}

    public ICDKMolecule fromString(String molstring)
        throws BioclipseException, IOException {
        if (molstring == null)
            throw new BioclipseException("Input cannot be null.");
        if (molstring.length() == 0)
            throw new BioclipseException("Input cannot be empty.");

        return loadMolecule(
            new ByteArrayInputStream(molstring.getBytes()),
            new NullProgressMonitor(),
            formatsFactory.guessFormat(
                 new ByteArrayInputStream(molstring.getBytes())
            )
        );
    }

    public Iterator<ICDKMolecule>
  	    createMoleculeIterator( IFile file, IProgressMonitor monitor)
  	        throws CoreException {

  	    return new IteratingBioclipseMDLReader(
  	                   file.getContents(),
  	                   NoNotificationChemObjectBuilder.getInstance(),
  	                   monitor );
  	}

  	static class IteratingBioclipseMDLReader
  	       implements Iterator<ICDKMolecule> {

  	    IteratingMDLReader reader;
  	    IProgressMonitor monitor = new NullProgressMonitor();

  	    public IteratingBioclipseMDLReader( InputStream input,
  	                                        IChemObjectBuilder builder,
  	                                        IProgressMonitor monitor ) {
  	        reader = new IteratingMDLReader(input, builder);
  	        if (monitor != null) {
  	            this.monitor = monitor;
  	        }
  	        this.monitor.beginTask("", IProgressMonitor.UNKNOWN);
  	    }

  	    public boolean hasNext() {
  	        boolean hasNext = reader.hasNext();
  	        if (!hasNext) {
  	            monitor.done();
  	        }
  	        return hasNext;
  	    }

  	    public ICDKMolecule next() {
  	        org.openscience.cdk.interfaces.IMolecule cdkMol
  	            = (org.openscience.cdk.interfaces.IMolecule) reader.next();
  	        ICDKMolecule bioclipseMol = new CDKMolecule(cdkMol);
  	        monitor.worked(1);
  	        return bioclipseMol;
  	    }

  	    public void remove() {
  	        reader.remove();
  	    }
  	}

  	public Iterator<ICDKMolecule> createConformerIterator(String path) {
  	    return creatConformerIterator( ResourcePathTransformer.getInstance()
  	                                                          .transform(path),
  	                                   null );
  	}

  	public Iterator<ICDKMolecule>
  	    creatConformerIterator( IFile file,
  	                            IProgressMonitor monitor) {
  	    try {
  	        return new IteratingBioclipseMDLConformerReader(
  	                       file.getContents(),
  	                       NoNotificationChemObjectBuilder.getInstance(),
  	                       monitor );
  	    }
  	    catch (CoreException e) {
  	        throw new IllegalArgumentException(e);
  	    }
  	}

  	static class IteratingBioclipseMDLConformerReader
  	       implements Iterator<ICDKMolecule> {

  	    IteratingMDLConformerReader reader;
  	    IProgressMonitor monitor;

  	    public IteratingBioclipseMDLConformerReader(InputStream input,
  	                                                IChemObjectBuilder builder,
  	                                                IProgressMonitor monitor) {
  	        if (monitor == null) {
  	            monitor = new NullProgressMonitor();
  	        }

  	        reader = new IteratingMDLConformerReader(input, builder);
  	        this.monitor = monitor;
  	        monitor.beginTask("Reading File", IProgressMonitor.UNKNOWN);
  	    }

  	    public boolean hasNext() {
  	        return reader.hasNext();
  	    }

  	    public ICDKMolecule next() {
  	        ConformerContainer cdkMol = (ConformerContainer) reader.next();
  	        ICDKMolecule bioclipseMol = new CDKConformer(cdkMol);
  	        return bioclipseMol;
  	    }

  	    public void remove() {
  	        reader.remove();
  	    }
  	}

  	public boolean fingerPrintMatches( ICDKMolecule molecule,
  	                                   ICDKMolecule subStructure )
                   throws BioclipseException {

  	    return FingerprinterTool.isSubset(molecule.getFingerprint(true),
  	                                      molecule.getFingerprint(true));
  	}

  	public boolean subStructureMatches( ICDKMolecule molecule,
  	                                    ICDKMolecule subStructure ) {
  	    try {
  	        return UniversalIsomorphismTester.isSubgraph(
                       molecule.getAtomContainer(),
                       subStructure.getAtomContainer() );
  	    }
  	    catch (CDKException e) {
  	        throw new RuntimeException(e);
  	    }
  	}

  	public boolean areIsomorphic( ICDKMolecule molecule,
  	                              ICDKMolecule subStructure ) {
  	    try {
  	        return UniversalIsomorphismTester.isIsomorph(
  	                   molecule.getAtomContainer(),
  	                   subStructure.getAtomContainer() );
  	    }
  	    catch (CDKException e) {
  	        throw new RuntimeException(e);
  	    }
  	}

  	/**
  	 * Create an ICDKMolecule from an IMolecule. First tries to create
  	 * ICDKMolecule from CML. If that fails, tries to create from SMILES. If
  	 */
  	public ICDKMolecule create(IMolecule imol) throws BioclipseException {

  	    if (imol instanceof ICDKMolecule) {
  	        return (ICDKMolecule) imol;
  	    }

  	    // First try to create from CML
  	    try {
  	        String cmlString = imol.getCML();
  	        if (cmlString != null) {
  	            return fromCml(cmlString);
  	        }
  	    }
  	    catch (IOException e) {
  	        logger.debug("Could not create mol from CML");
  	    }
  	    catch (UnsupportedOperationException e) {
  	        logger.debug("Could not create mol from CML");
  	    }

  	    // Secondly, try to create from SMILES
  	    return fromSMILES( imol.getSMILES() );
  	}

  	public boolean smartsMatches(ICDKMolecule molecule, String smarts)
  	               throws BioclipseException {

  	    SMARTSQueryTool querytool;

  	    try {
  	        querytool = new SMARTSQueryTool(smarts);
  	    }
  	    catch (CDKException e) {
  	        throw new BioclipseException("Could not parse SMARTS query", e);
  	    }

  	    try {
  	        return querytool.matches(molecule.getAtomContainer());
  	    }
  	    catch (CDKException e) {
  	        throw new BioclipseException("A problem occured trying "
  	                                     + "to match SMARTS query", e);
  	    }
  	}

  	public int numberOfEntriesInSDF(IFile file, IProgressMonitor monitor) {

  	    if (monitor == null) {
  	        monitor = new NullProgressMonitor();
  	    }

  	    long tStart = System.nanoTime();
  	    int num = 0;
  	    try {
  	        BufferedInputStream counterStream
  	            = new BufferedInputStream( file.getContents() );
  	        int c = 0;
  	        while (c != -1) {
  	            c = counterStream.read();
  	            if (c == '$') {
  	                c = counterStream.read();
  	                if (c == '$') {
  	                    c = counterStream.read();
  	                    if (c == '$') {
  	                        c = counterStream.read();
  	                        if (c == '$') {
  	                            num++;
  	                            counterStream.read();
  	                            if ( monitor.isCanceled() ) {
  	                                throw new OperationCanceledException();
  	                            }
  	                        }
  	                    }
  	                }
  	            }
  	        }
  	        counterStream.close();
  	    }
  	    catch (Exception exception) {
  	        // ok, I give up...
  	        logger.debug( "Could not determine the number of molecules to " +
  	        		            "read, because: "
  	                        + exception.getClass().getSimpleName() + " : "
  	                        + exception.getMessage(),
  	                      exception );
  	    }
  	    logger.debug( "numberOfEntriesInSDF took "
  	                  +(int)((System.nanoTime()-tStart)/1e6)+" to complete");
  	    monitor.done();
  	    return num;
  	}

  	private static class Record {
  	    public Record(int s, int l) {
  	        start =s;
  	        length = l;
  	    }
  	    int start;
  	    int length;
  	}

  	public void createSDFileIndex( String path ) {
  	    throw new UnsupportedOperationException(
  	    "This manager method should not be called");
  	}

  	public void createSDFileIndex(IFile file, BioclipseUIJob<?> uiJob) {
  	    throw new UnsupportedOperationException(
  	    "This manager method should not be called");
  	}

  	public void createSDFileIndex( IFile file , IProgressMonitor monitor) {

  	    if (monitor == null) {
  	        monitor = new NullProgressMonitor();
  	    }

  	    if ( file.getLocation() != null ) {
  	        try {

  	            long tStart = System.nanoTime();

  	            File indexFile = RandomAccessReader.getIndexFile(
  	                                file.getLocation().toOSString() );

  	            if ( indexFile.exists() ) {
  	                indexFile.delete();
  	            }

  	            long size = EFS.getStore( file.getLocationURI() )
  	                           .fetchInfo().getLength();

  	            monitor.beginTask( "Creating SD-file index", (int)size );
  	            List<Record> indexList = new LinkedList<Record>();

  	            BufferedInputStream cs
  	                = new BufferedInputStream( file.getContents(), 8192 );

  	            boolean nextNewLine = false;

  	            int num = 0;
  	            int pos = 0;
  	            int start = 0;
  	            int c = 0;
  	            int dollars = 0;
  	            int newLine = 0;

  	            while ( (c = cs.read()) != -1 ) {
  	                pos++;
  	                if ( c == '$' ) {
  	                    dollars++;
  	                    if ( dollars == 4 ) {

  	                        indexList.add(
  	                            new Record( start, (pos) - start ) );

  	                        monitor.worked( pos-start );

  	                        if ( monitor.isCanceled() ) {
  	                            cs.close();
  	                            throw new OperationCanceledException();
  	                        }

  	                        dollars = 0;
  	                        nextNewLine = true;
  	                        newLine = 0;
  	                        num++;
  	                    }
  	                }
  	                else
  	                    dollars = 0;

  	                if(nextNewLine && c == '\n') {
  	                    newLine++;
  	                    if(newLine == 1) {
  	                        start = pos;
  	                        nextNewLine = false;
  	                    }
  	                }
  	            }
  	            // Adds the last entry file contains no ending '$$$$'
  	            if(start!=pos) {
  	                indexList.add(
  	                          new Record(start,pos-1-start));
  	                monitor.worked( pos-start );
  	                num++;
  	            }

  	            cs.close();
  	            PrintStream os = new PrintStream( indexFile );
  	            os.println( "1" );
  	            os.println( file.getLocation().toOSString() );
  	            os.println( pos );
  	            os.println( num );

  	            for ( Record rec : indexList ) {
  	                os.printf( "%d\t%d\t%d\n", rec.start, rec.length, -1 );
  	            }

  	            os.println( num );
  	            os.println( file.getLocation().toOSString() );
  	            os.flush();
  	            os.close();

  	            indexFile.deleteOnExit();

  	            logger.debug( "Created Index in "
  	                          + (int) ((System.nanoTime() - tStart) / 1e6)
  	                          + "ms" );

  	        }
  	        catch ( IOException e ) {
  	            LogUtils.debugTrace( logger, e );
  	        }
  	        catch ( CoreException e ) {
  	            LogUtils.debugTrace( logger, e );
  	        }
  	        finally {
  	            monitor.done();
  	        }
  	    }
  	}

  	public int numberOfEntriesInSDF(String filePath) {
  	           throw new UnsupportedOperationException(
  	    "This manager method should not be called");
  	}

  	/**
  	 * Reads files and extracts conformers if available. Currently limited to
  	 * read SDFiles, CMLFiles is for the future.
  	 *
  	 * @param path
  	 *            the full path to the file
  	 * @return a list of molecules that may have multiple conformers
  	 */
  	public List<ICDKMolecule> loadConformers(String path) {
  	    return loadConformers( ResourcePathTransformer.getInstance()
  	                                                  .transform(path), null);
  	}

  	/**
  	 * Reads files and extracts conformers if available. Currently limited to
  	 * read SDFiles, CMLFiles is for the future.
  	 */
  	public List<ICDKMolecule> loadConformers( IFile file,
  	                                          IProgressMonitor monitor ) {
  	    if (monitor == null) {
  	        monitor = new NullProgressMonitor();
  	    }

  	    monitor.beginTask("Reading file", IProgressMonitor.UNKNOWN);

  	    Iterator<ICDKMolecule> it
  	        = creatConformerIterator( file,
  	                                  new SubProgressMonitor(monitor, 100) );

  	    List<ICDKMolecule> mols = new ArrayList<ICDKMolecule>();

  	    while ( it.hasNext() ) {
  	        ICDKMolecule molecule = (ICDKMolecule) it.next();
  	        String molName = (String) molecule.getAtomContainer()
  	                                          .getProperty(
  	                                              CDKConstants.TITLE );
  	        if ( molName != null &&
  	             !molName.equals("") ) {
  	            molecule.setName(molName);
  	        }
  	        mols.add(molecule);
  	    }

  	    if (mols == null || mols.size() <= 0)
  	        throw new IllegalArgumentException("No conformers could be read");
  	    return mols;
  	}

  	public double calculateMass(IMolecule molecule) throws BioclipseException {

  	    ICDKMolecule cdkmol = null;

  	    if (molecule instanceof ICDKMolecule) {
  	        cdkmol = (ICDKMolecule) molecule;
  	    }
  	    else {
  	        cdkmol = create(molecule);
  	    }

  	    return AtomContainerManipulator.getNaturalExactMass(
  	               cdkmol.getAtomContainer() );
  	}

  	public IMolecule generate2dCoordinates(IMolecule molecule)
  	                 throws Exception {

  	    ICDKMolecule cdkmol = null;

  	    if (molecule instanceof ICDKMolecule) {
  	        cdkmol = (ICDKMolecule) molecule;
  	    }
  	    else {
  	        cdkmol = create(molecule);
  	    }

  	    IMoleculeSet mols
  	        = ConnectivityChecker.partitionIntoMolecules(
  	              cdkmol.getAtomContainer() );

  	    StructureDiagramGenerator sdg = new StructureDiagramGenerator();
  	    org.openscience.cdk.interfaces.IMolecule newmolecule
  	        = cdkmol.getAtomContainer().getBuilder().newMolecule();

  	    for ( IAtomContainer mol : mols.molecules() ) {
  	        sdg.setMolecule( cdkmol.getAtomContainer()
  	                               .getBuilder().newMolecule(mol) );
  	        sdg.generateCoordinates();
  	        IAtomContainer ac = sdg.getMolecule();
  	        for (IAtom a : ac.atoms()) {
  	            a.setPoint3d(null);
  	        }
  	        newmolecule.add(ac);
  	    }
  	    return new CDKMolecule(newmolecule);
  	}

  	public Iterator<ICDKMolecule> createMoleculeIterator(String path)
  	                              throws CoreException {
  	    return createMoleculeIterator( ResourcePathTransformer.getInstance()
  	                                                          .transform(path),
  	                                   null );
  	}

  	public Iterator<ICDKMolecule> creatConformerIterator(IFile file) {
  	    return creatConformerIterator(file, null);
  	}

  	public Iterator<ICDKMolecule> createMoleculeIterator(IFile file)
  	                              throws CoreException {
  	    return createMoleculeIterator(file, null);
  	}

  	public List<ICDKMolecule> loadConformers(IFile file) {
  	    return loadConformers(file, null);
  	}

  	public List<ICDKMolecule> loadMolecules(IFile file)
  	                          throws IOException,
  	                                 BioclipseException,
  	                                 CoreException {
  	    return loadMolecules(file, (IProgressMonitor)null);
  	}

  	public int numberOfEntriesInSDF( IFile file,
  	                                 BioclipseUIJob<Integer> uiJob ) {
  	    throw new UnsupportedOperationException(
  	        "This manager method should not be called");
  	}

  	public void saveMol2(ICDKMolecule mol, String filename)
  	            throws InvocationTargetException,
  	                   BioclipseException,
  	                   CDKException,
  	                   CoreException {
  	    saveMolecule(mol, filename,CDKManager.mol2);
  	}

  	public List<ICDKMolecule> loadSMILESFile(String path)
  	                          throws CoreException,
  	                                 IOException {
  	    return loadSMILESFile( ResourcePathTransformer.getInstance()
  	                                                  .transform(path) );
  	}

  	public List<ICDKMolecule> loadSMILESFile( IFile file,
  	                                 BioclipseUIJob<List<ICDKMolecule>> uiJob )
  	                                 throws CoreException, IOException {
  	      throw new UnsupportedOperationException(
  	      "This manager method should not be called");
  	}

  	public List<ICDKMolecule> loadSMILESFile( IFile file )
  	                                                      throws CoreException,
  	                                                      IOException {

  	    throw new UnsupportedOperationException(
  	    "This manager method should not be called");
  	}

  	public List<ICDKMolecule> loadSMILESFile(IFile file,IProgressMonitor monitor)
  	                          throws CoreException, IOException {

  	    //Only process files with smiles extension
  	    if ( !file.getFileExtension().equals(
  	          SMILESFormat.getInstance().getPreferredNameExtension() ) )
  	        return null;

  	    BufferedInputStream buf = new BufferedInputStream(file.getContents());
  	    InputStreamReader reader = new InputStreamReader(buf);
  	    BufferedReader br = new BufferedReader(reader);

  	    if ( !br.ready() ) {
  	        throw new IOException("File: " + file.getName()
  	                              + " is not ready to read.");
  	    }

  	    String line = br.readLine();

  	    if (line == null)
  	        throw new IOException("File: " + file.getName()
  	                              + " has null contents");
  	    int cnt = 0;
  	    Map<String, String> entries = new HashMap<String, String>();
  	    while (line != null) {
  	        //			System.out.println("Line " + cnt + ": " + line);
  	        Scanner smilesScanner = new Scanner(line).useDelimiter("\\s+");
  	        String part1 = null;
  	        String part2 = null;
  	        if (smilesScanner.hasNext()) {
  	            part1 = smilesScanner.next();
  	            if (smilesScanner.hasNext()) {
  	                part2 = smilesScanner.next();
  	            }
  	        }
  	        if (part1 != null) {
  	            if (part2 != null) {
  	                entries.put(part1, part2);
  	            }else{
  	                entries.put(part1, "entry-" + cnt);
  	            }
  	            //				System.out
  	            //						.println("  - " + part1 + " -> " + entries.get(part1));
  	        }
  	        // Get next line
  	        line = br.readLine();
  	        cnt++;
  	    }
  	    // Depict where the smiles are, in first or second
  	    boolean smilesInFirst = true;
  	    String firstKey = (String) entries.keySet().toArray()[0];
  	    String firstVal = (String) entries.get(firstKey);
  	    ICDKMolecule mol = null;
  	    try {
  	        mol = fromSMILES(firstKey);
  	    } catch (BioclipseException e) {
  	    }
  	    if (mol == null) {
  	        try {
  	            mol = fromSMILES(firstVal);
  	            smilesInFirst = false;
  	        } catch (BioclipseException e) {
  	        }
  	    }
  	    List<ICDKMolecule> mols = new ArrayList<ICDKMolecule>();
  	    for (String part1 : entries.keySet()) {
  	        if (smilesInFirst) {
  	            try {
  	                mol = fromSMILES(part1);
  	                mol.setName(entries.get(part1));
  	                mols.add(mol);
  	            } catch (BioclipseException e) {
  	            }
  	        } else {
  	            try {
  	                mol = fromSMILES(entries.get(part1));
  	                mol.setName(part1);
  	                mols.add(mol);
  	            } catch (BioclipseException e) {
  	            }
  	        }
  	    }
  	    return mols;
  	}

  	public int getNoMolecules(String path) {
  	    if (path.endsWith("sdf")){
  	        return numberOfEntriesInSDF(path);
  	    }
  	    List<ICDKMolecule> lst;
  	    try {
  	        lst = loadMolecules(path);
  	        if (lst!=null) return lst.size();
  	    } catch (Exception e) {
  	        logger.debug("Could not count mols in file: " + path + ". Reason: "
  	                     + e.getMessage());
  	    }
  	    return -1;
  	}

  	public MoleculesInfo getInfo(String path) {
  	    return getInfo( ResourcePathTransformer.getInstance()
  	                                           .transform(path) );
  	}

  	public IMolecule generate3dCoordinates(IMolecule molecule)
  	                 throws Exception {

  	    ICDKMolecule cdkmol = null;
  	    if ( molecule instanceof ICDKMolecule ) {
  	        cdkmol = (ICDKMolecule) molecule;
  	    }else {
  	        cdkmol=create(molecule);
  	    }

  	    ModelBuilder3D mb3d = ModelBuilder3D.getInstance();
  	    IMoleculeSet mols = ConnectivityChecker.partitionIntoMolecules(
  	                            cdkmol.getAtomContainer() );

  	    org.openscience.cdk.interfaces.IMolecule newmolecule
  	        = cdkmol.getAtomContainer().getBuilder().newMolecule();

  	    for ( IAtomContainer mol : mols.molecules() ) {

  	        addExplicitHydrogens( new CDKMolecule(mol) );
  	        org.openscience.cdk.interfaces.IMolecule ac
  	            = mb3d.generate3DCoordinates(
  	                  (org.openscience.cdk.interfaces.IMolecule)mol, false);

  	        for (IAtom a : ac.atoms()) {
  	            a.setPoint2d(null);
  	        }

  	        newmolecule.add(ac);
  	    }
  	    return new CDKMolecule(newmolecule);
  	}

  	public IMolecule addExplicitHydrogens(IMolecule molecule)
  	                 throws Exception {

  	    addImplicitHydrogens(molecule);
  	    ICDKMolecule cdkmol=null;

  	    if ( molecule instanceof ICDKMolecule ) {
  	        cdkmol = (ICDKMolecule) molecule;
  	    }
  	    else {
  	        cdkmol=create(molecule);
  	    }

  	    IAtomContainer ac = cdkmol.getAtomContainer();
  	    AtomContainerManipulator.convertImplicitToExplicitHydrogens(ac);
  	    return new CDKMolecule(ac);
  	}

  	public IMolecule addImplicitHydrogens(IMolecule molecule)
  	                 throws BioclipseException, InvocationTargetException {

  	    ICDKMolecule cdkmol = null;
  	    if ( molecule instanceof ICDKMolecule ) {
  	        cdkmol = (ICDKMolecule) molecule;
  	    }
  	    else {
  	        cdkmol=create(molecule);
  	    }

  	    IAtomContainer container = cdkmol.getAtomContainer();
  	    CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(
  	                                     container.getBuilder() );
  	    Iterator<IAtom> atoms = container.atoms().iterator();

  	    try {
  	        while (atoms.hasNext()) {
  	            IAtom atom = atoms.next();
  	            IAtomType type = matcher.findMatchingAtomType(container, atom);
  	            AtomTypeManipulator.configure(atom, type);
  	        }
  	        CDKHydrogenAdder hAdder
  	            = CDKHydrogenAdder.getInstance(container.getBuilder());
  	        hAdder.addImplicitHydrogens(container);
  	        return new CDKMolecule(container);
  	    }
  	    catch (CDKException e) {
  	        e.printStackTrace();
  	        throw new InvocationTargetException(e);
  	    }
  	}

  	public MoleculesInfo getInfo(IFile transform) {
  	    int numMols=0;
  	    int num2d=0;
  	    int num3d=0;
  	    List<ICDKMolecule> lst;
  	    try {
  	        lst = loadMolecules(transform);
  	        if (lst!=null){
  	            for (ICDKMolecule mol : lst){
  	                numMols++;
  	                if (has2d(mol))
  	                    num2d++;
  	                if (has3d(mol))
  	                    num3d++;
  	            }
  	            MoleculesInfo retInfo=new MoleculesInfo(numMols, num2d, num3d);
  	            return retInfo;
  	        }
  	    } catch (Exception e) {
  	        logger.debug("Could not count mols in file: " +
  	                     transform.getProjectRelativePath() + ". Reason: "
  	                     + e.getMessage());
  	    }
  	    return null;
  	}

  	public boolean has2d(IMolecule mol) throws BioclipseException {
  	    return GeometryTools.has2DCoordinates(create(mol).getAtomContainer());
  	}

  	public boolean has3d(IMolecule mol) throws BioclipseException {
  	    return GeometryTools.has3DCoordinates(create(mol).getAtomContainer());
  	}

  	public void saveCML(ICDKMolecule cml,  String filename)
  	            throws InvocationTargetException,
  	                   BioclipseException,
  	                   CDKException,
  	                   CoreException {
  	    saveMolecule(cml, filename,CDKManager.cml);
  	}

  	public void saveMDLMolfile(ICDKMolecule mol, String filename)
  	            throws InvocationTargetException,
  	                   BioclipseException,
  	                   CDKException,
  	                   CoreException {
  	    saveMolecule(mol, filename,CDKManager.mol);
  	}

    public IChemFormat determineIChemFormat(IFile file)
        throws IOException, CoreException {
        return formatsFactory.guessFormat(
            new BufferedReader(new InputStreamReader(
                file.getContents()
            ))
        );
    }

    public String determineFormat( String path ) throws IOException,
                                                        CoreException {
        IChemFormat format = determineIChemFormat(
            ResourcePathTransformer.getInstance().transform(path)
        );
        return format == null ? "Unknown" : format.getFormatName();
    }

  	public void createSDFile(IFile file, IMolecule[] entries)
  	            throws BioclipseException, InvocationTargetException {

  	    if ( file.exists() )
  	        throw new BioclipseException("File " + file.getName()
  	                                     + " already exists");

  	    IProgressMonitor monitor = new NullProgressMonitor();
  	    int ticks = 10000;
  	    try {

  	        monitor.beginTask( "Writing file", ticks );
  	        StringBuffer sb = new StringBuffer();

  	        for (int i=0;i<entries.length;i++) {

  	            CMLReader reader
  	                = new CMLReader(
  	                    new StringBufferInputStream(entries[i].getCML()) );

  	            IAtomContainer ac
  	                = ((IChemFile)reader.read(
  	                    DefaultChemObjectBuilder.getInstance()
  	                                            .newChemFile() ))
  	                  .getChemSequence(0).getChemModel(0)
  	                                     .getMoleculeSet().getAtomContainer(0);

  	            StringWriter writer = new StringWriter();
  	            MDLWriter mdlwriter = new MDLWriter(writer);
  	            mdlwriter.write(ac);
  	            sb.append(writer.toString());
  	            sb.append("$$$$"+System.getProperty("line.separator"));
  	        }

  	        file.create( new StringBufferInputStream(sb.toString()),
  	                     false,
  	                     monitor );
  	        monitor.worked(ticks);
  	    }
  	    catch (Exception e) {
  	        e.printStackTrace();
  	        throw new BioclipseException(e.getMessage());
  	    }
  	    finally {
  	        monitor.done();
  	    }
  	}

  	public List<IMolecule> extractFromSDFile( IFile file, int startenty,
  	                                          int endentry )
  	                                          throws BioclipseException,
  	                                          InvocationTargetException {

  	    IProgressMonitor monitor = new NullProgressMonitor();
  	    int ticks = 10000;

  	    try {
  	        monitor.beginTask( "Writing file", ticks );
  	        IteratingBioclipseMDLReader reader
  	            = new IteratingBioclipseMDLReader(
  	                      file.getContents(),
  	                      DefaultChemObjectBuilder.getInstance(),
  	                      monitor );
  	        int i = 0;
  	        List<IMolecule> result=new ArrayList<IMolecule>();
  	        while (reader.hasNext()) {
  	            if (i>=startenty && i<=endentry) {
  	                result.add( reader.next() );
  	            }
  	            i++;
  	            if(i>endentry)
  	                break;
  	        }
  	        monitor.worked(ticks);
  	        return result;
  	    }
  	    catch (Exception e) {
  	        e.printStackTrace();
  	        throw new BioclipseException(e.getMessage());
  	    }
  	    finally {
  	        monitor.done();
  	    }
  	}

    public List<IMolecule> extractFromSDFile( String file, int startenty,
                                              int endentry )
                                                            throws BioclipseException,
                                                            InvocationTargetException {
        return extractFromSDFile( ResourcePathTransformer.getInstance().transform(file), startenty, endentry );
    }

    public void createSDFile( String file, IMolecule[] entries )
                                                                throws BioclipseException,
                                                                InvocationTargetException {

        createSDFile( ResourcePathTransformer.getInstance().transform(file), entries );
        
    }

    public String molecularFormula( ICDKMolecule m ) {

        IMolecularFormula mf
            = MolecularFormulaManipulator.getMolecularFormula(
                  m.getAtomContainer() );

        int missingHCount = 0;
        for (IAtom atom : m.getAtomContainer().atoms()) {
            missingHCount += calculateMissingHydrogens( m.getAtomContainer(),
                                                        atom );
        }

        mf.addIsotope( m.getAtomContainer().getBuilder()
                                           .newIsotope( Elements.HYDROGEN),
                       missingHCount );

        return MolecularFormulaManipulator.getString( mf );
    }

    private int calculateMissingHydrogens( IAtomContainer container,
                                           IAtom atom ) {
        CDKAtomTypeMatcher matcher
            = CDKAtomTypeMatcher.getInstance(container.getBuilder());
        IAtomType type;
        try {
            type = matcher.findMatchingAtomType(container, atom);
            if (type.getAtomTypeName() == null)
                return 0;

            if ("X".equals(atom.getAtomTypeName())) {
                return 0;
              }

            if (type.getFormalNeighbourCount() == CDKConstants.UNSET)
              return 0;

            // very simply counting:
            // each missing explicit neighbor is a missing hydrogen
            return type.getFormalNeighbourCount()
                   - container.getConnectedAtomsCount(atom);
        }
        catch ( CDKException e ) {
            return 0;
        }
    }
}
