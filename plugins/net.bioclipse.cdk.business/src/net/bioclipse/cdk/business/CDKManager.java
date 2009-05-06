/*******************************************************************************
 * Copyright (c) 2008-2009  Ola Spjuth
 *               2008-2009  Jonathan Alvarsson
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.ConformerContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
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
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.FormatFactory;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.WriterFactory;
import org.openscience.cdk.io.formats.CIFFormat;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.IChemFormatMatcher;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.formats.MDLFormat;
import org.openscience.cdk.io.formats.MDLV2000Format;
import org.openscience.cdk.io.formats.Mol2Format;
import org.openscience.cdk.io.formats.PDBFormat;
import org.openscience.cdk.io.formats.PubChemCompoundXMLFormat;
import org.openscience.cdk.io.formats.PubChemCompoundsXMLFormat;
import org.openscience.cdk.io.formats.PubChemSubstanceXMLFormat;
import org.openscience.cdk.io.formats.PubChemSubstancesXMLFormat;
import org.openscience.cdk.io.formats.SDFFormat;
import org.openscience.cdk.io.formats.SMILESFormat;
import org.openscience.cdk.io.iterator.IteratingMDLConformerReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.io.random.RandomAccessReader;
import org.openscience.cdk.io.random.RandomAccessSDFReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.modeling.builder3d.ModelBuilder3D;
import org.openscience.cdk.nonotify.NNChemFile;
import org.openscience.cdk.nonotify.NNMolecule;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.similarity.Tanimoto;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.smiles.smarts.parser.TokenMgrError;
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

    private static final WriterFactory writerFactory = new WriterFactory();

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
        throw new UnsupportedOperationException(
           "This manager method should not be called, " +
           "loadMolecules(IFile, BioclipseUIJob)");
    }

      public ICDKMolecule loadMolecule(IFile file) throws IOException,
                                                          BioclipseException,
                                                          CoreException {
          throw new UnsupportedOperationException(
                "This manager method should not be called, " +
                "loadMolecules(IFile, BioclipseUIJob)");
      }

      public ICDKMolecule loadMolecule( IFile file,
                                        BioclipseUIJob<ICDKMolecule> uiJob)
                                               throws IOException,
                                                      BioclipseException,
                                                      CoreException {
          throw new UnsupportedOperationException(
               "This manager method should not be called, " +
               "loadMolecules(IFile, BioclipseUIJob)");
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

          List<ICDKMolecule> moleculesList = new BioList<ICDKMolecule>();

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

              // Do some customizations...
              CDKManagerHelper.customizeReading(reader);

            List<IAtomContainer> atomContainersList =
                new ArrayList<IAtomContainer>();

              // Read file
              try {
                  if (reader.accepts(ChemFile.class)) {
                      IChemFile chemFile =
                          (IChemFile) reader.read(new NNChemFile());
                      atomContainersList =
                          ChemFileManipulator.getAllAtomContainers(chemFile);
                  } else if (reader.accepts(Molecule.class)) {
                      atomContainersList.add(
                          (NNMolecule) reader.read(new NNMolecule())
                      );
                  } else {
                      throw new RuntimeException("Failed to read file.");
                  }
              }
              catch (CDKException e) {
                  throw new RuntimeException("Failed to read file", e);
              }

              // Store the chemFormat used for the reader
              IResourceFormat chemFormat = reader.getFormat();
              logger.debug("Read CDK chemfile with format: "
                           + chemFormat.getFormatName());

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
            "This manager method should not be called, " +
            "loadMolecules(IFile, BioclipseUIJob)");
      }

      public List<ICDKMolecule> loadMolecules( IFile file,
                                               IChemFormat format,
                                               IProgressMonitor monitor )
                                throws IOException,
                                       BioclipseException,
                                       CoreException {
        if (file == null)
            throw new BioclipseException("Cannot load molecules: file was null");

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

              if (reader == null) {

                  // Try SMILES
                List<ICDKMolecule> moleculesList2 = loadSMILESFile(file, monitor);
                  if (moleculesList2 != null && moleculesList2.size() > 0)
                      return moleculesList2;

                  // Ok, not even SMILES works
                  throw new BioclipseException(
                      "Could not create reader in CDK." );
              }

            try {
                reader.setReader( file.getContents() );
            }
            catch (CDKException e1) {
                throw new BioclipseException(
                    "Could not set the reader's input." );
            }

              IChemFile chemFile = new org.openscience.cdk.ChemFile();

              // Do some customizations...
              CDKManagerHelper.customizeReading(reader);

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

                  //Associate molecule with the file it comes from
                  mol.setResource( file );

                  String moleculeName = molecularFormula( mol );
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
                          moleculeName  = molName;
                    }
                  }

                  mol.setName(moleculeName);
                  moleculesList.add(mol);
                  monitor.worked( (int) (ticks / nuMols) );

                  if ( ++currentMolecule % 100 == 0 ) {
                      monitor.subTask( "Loaded molecule:" +
                                       currentMolecule + "/" + nuMols );
                  }

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

      public void save(IChemModel model, String target, IChemFormat filetype)
                  throws BioclipseException, CDKException, CoreException {
          save( model,
                ResourcePathTransformer.getInstance().transform(target),
                filetype,
                null );
      }

      public void save( IChemModel model,
                        IFile target,
                      IChemFormat filetype,
                        IProgressMonitor monitor )
                  throws BioclipseException, CDKException, CoreException {

          if (monitor == null)
              monitor = new NullProgressMonitor();

          if (filetype == null) filetype = (IChemFormat)CMLFormat.getInstance();

          try {
              int ticks = 10000;
              monitor.beginTask("Writing file", ticks);
              StringWriter writer = new StringWriter();

            writerFactory.registerWriter(CMLWriter.class);
            // OK, CDK does not properly save SDF files, using the WriterFactory
            // approach... so work around that...
            IChemObjectWriter chemWriter = null;
            if (filetype == SDFFormat.getInstance()) {
                chemWriter = new SDFWriter(writer);
            } else {
                chemWriter = writerFactory.createWriter(filetype);
                if (chemWriter == null) {
                    throw new BioclipseException("No writer available for this format: " +
                        filetype.getFormatName());
                }
                chemWriter.setWriter(writer);
            }
            if (chemWriter.accepts(ChemModel.class)) {
                chemWriter.write(model);
            } else if (chemWriter.accepts(MoleculeSet.class)){
                IMoleculeSet list =
                    model.getBuilder().newMoleculeSet();
                for (IAtomContainer container :
                     ChemModelManipulator.getAllAtomContainers(model)) {
                    list.addAtomContainer(container);
                }
                chemWriter.write(list);
            } else if (chemWriter.accepts(Molecule.class)){
                org.openscience.cdk.interfaces.IMolecule smashedContainer =
                    model.getBuilder().newMolecule();
                for (IAtomContainer container :
                     ChemModelManipulator.getAllAtomContainers(model)) {
                    smashedContainer.add(container);
                }
                chemWriter.write(smashedContainer);
            } else {
                throw new BioclipseException("Writer does not support writing" +
                        "IChemModel or IMolecule.");
            }
              chemWriter.close();

              if (target.exists()) {
                  target.setContents(
                          new ByteArrayInputStream(writer.toString()
                                  .getBytes("US-ASCII")),
                                  false,
                                  true, // overwrite
                                  monitor );
              } else {
                  target.create(
                          new ByteArrayInputStream(writer.toString()
                                  .getBytes("US-ASCII")),
                                  false,
                                  monitor );
              }
              monitor.worked(ticks);
          } catch (IOException exception) {
            throw new BioclipseException("Failed to write file: " +
                    exception.getMessage());
          } finally {
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

          if (mol.getResource() == null ||
              !(mol.getResource() instanceof IFile))
              throw new BioclipseException(
                  "Molecule does not have an associated File." );

        IFile file = (IFile)mol.getResource();
        IContentDescription contentDesc = file.getContentDescription();
        if (contentDesc == null) {
            logger.error("Hej, you are running OS/X! You just encountered a" +
                "known bug: contentDesc == null for who-knows-what" +
                "reason... it works on Linux... So, I am guessing from" +
                "the file name. Blah, yuck...");
            saveMolecule(
                mol, file,
                guessFormatFromExtension(file.getName()),
                overwrite
            );
        } else {
            saveMolecule(
                mol, file,
                determineFormat(contentDesc.getContentType()),
                overwrite
            );
        }
    }

      public void saveMolecule(IMolecule mol, String filename, boolean overwrite)
                  throws BioclipseException, CDKException, CoreException {

          IFile file = ResourcePathTransformer.getInstance().transform(filename);
          saveMolecule(mol, file, overwrite);
      }

      public void saveMolecule(IMolecule mol, IFile file, boolean overwrite)
                  throws BioclipseException, CDKException, CoreException {

        IChemFormat format = null;

        // are we really overwriting an old file?
        if (mol.getResource() != null &&
            (mol.getResource() instanceof IFile)) {
            IFile oldFile = (IFile)mol.getResource();
            if (oldFile.getContentDescription() == null) {
                logger.error("Hej, you are running OS/X! You just encountered " +
                        "a known bug: contentDesc == null for who-knows-what" +
                        "reason... it works on Linux... So, I am guessing" +
                        "from the file name. Blah, yuck...");
            } else {
                format = determineFormat(
                    oldFile.getContentDescription().getContentType()
                );
            }
        }

        if (overwrite && format == null) {
            format = guessFormatFromExtension(file);
        }

        // OK, not overwriting, but unknown format: default to CML
        if (format == null) format = (IChemFormat)CMLFormat.getInstance();

          saveMolecule(
            mol, file, format, overwrite
          );
      }

    private IChemFormat guessFormatFromExtension(IFile file) {
        return guessFormatFromExtension(file.getName());
    }

    public IChemFormat guessFormatFromExtension(String file) {
        if (file.endsWith(".mdl")) {
            return (IChemFormat)MDLV2000Format.getInstance();
        }
        for (IChemFormat aFormat : formatsFactory.getFormats()) {
            if (aFormat == MDLFormat.getInstance()) {
                // never match this one: it's outdated and != MDLV2000Format
            } else if (file.endsWith("."+aFormat.getPreferredNameExtension())) {
                return aFormat;
            }
        }
        return null;
    }

    public void saveMolecule( IMolecule mol_in,
                                String filename,
                              IChemFormat filetype )
                throws BioclipseException, CDKException, CoreException {
          this.saveMolecule(mol_in, filename, filetype, false);
      }

      public void saveMolecule(IMolecule mol_in, IFile target, IChemFormat filetype)
                  throws BioclipseException, CDKException, CoreException {
          this.saveMolecule(mol_in, target, filetype, false);
      }

      public void saveMolecule( IMolecule mol_in,
                                IFile target,
                                IChemFormat filetype,
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

        mol.setResource(target);
      }

      public void saveMolecule( IMolecule mol,
                                String filename,
                                IChemFormat filetype,
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
                               IChemFormat filetype )
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
                               IChemFormat filetype )
                  throws BioclipseException, CDKException, CoreException {

          if ( filetype == CMLFormat.getInstance() ||
               filetype == MDLV2000Format.getInstance() ||
               filetype == SDFFormat.getInstance()) {

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
              throw new IllegalArgumentException("Input cannot be null");

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
          return createConformerIterator(
              ResourcePathTransformer.getInstance().transform(path),
              null
          );
      }

      public Iterator<ICDKMolecule>
          createConformerIterator( IFile file,
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

      public boolean isValidSmarts(String smarts){
        try {
            new SMARTSQueryTool(smarts);
            return true;
        } catch (CDKException e) {
            return false;
        } catch (TokenMgrError error) {
            return false;
        }

      }

      public List<IAtomContainer> getSmartsMatches(ICDKMolecule molecule, String smarts)
      throws BioclipseException {

          SMARTSQueryTool querytool;

          try {
              querytool = new SMARTSQueryTool(smarts);
          }
          catch (CDKException e) {
              throw new BioclipseException("Could not parse SMARTS query", e);
          }

          try {
              IAtomContainer ac=molecule.getAtomContainer();
              if (querytool.matches(ac)){
                  List<IAtomContainer> retac=new ArrayList<IAtomContainer>();
                int nmatch = querytool.countMatches();
                logger.debug("Found " + nmatch + " smarts matches");

                List<List<Integer>> mappings = querytool.getMatchingAtoms();
                for (int i = 0; i < nmatch; i++) {
                    List<Integer> atomIndices = (List<Integer>) mappings.get(i);
                    IAtomContainer match=ac.getBuilder().newAtomContainer();
                    for (Integer aindex : atomIndices){
                        IAtom atom=ac.getAtom( aindex );
                        match.addAtom( atom );
                    }
                    retac.add( match );

                }
                return retac;
              }
              return null;
          }
          catch (CDKException e) {
              throw new BioclipseException("A problem occured trying "
                                           + "to match SMARTS query", e);
          }
      }

      public int numberOfEntriesInSDF(IFile file, IProgressMonitor monitor) {

          SDFileIndex index = createSDFIndex( file, monitor );
          return index.size();
      }

      public SDFileIndex createSDFIndex(String file) {
          throw new IllegalStateException("This methos call should have been" +
                                          "translated to method(IFile,...,"+
                                          "IProgresMonitor");
      }

      public SDFileIndex createSDFIndex(IFile file) {
          throw new IllegalStateException("This methos call should have been" +
                                          "translated to method(IFile,...,"+
                                          "IProgresMonitor");
      }

      public SDFileIndex createSDFIndex(IFile file, BioclipseUIJob<SDFileIndex> uiJob) {
          throw new IllegalStateException("This methos call should have been" +
                                          "translated to method(IFile,...,"+
                                          "IProgresMonitor");
      }

      public SDFileIndex createSDFIndex(IFile file, IProgressMonitor monitor) {

          SubMonitor progress = SubMonitor.convert( monitor ,100);
          long size = -1;
          try {
              size =EFS.getStore( file.getLocationURI() ).fetchInfo().getLength();
              progress.beginTask( "Parsing SDFile",
                                  (int)size);

          }catch (CoreException e) {
              logger.debug( "Failed to get size of file" );
              progress.beginTask( "Parsing SDFile", IProgressMonitor.UNKNOWN );
          }
          long tStart = System.nanoTime();
          List<Long> values = new LinkedList<Long>();
          int num = 0;
          long pos = 0;
          long start = 0;
          int work = 0;
          try {
              BufferedInputStream counterStream
              = new BufferedInputStream( file.getContents() );
              int c = 0;
              while (c != -1) {
                  c = counterStream.read();pos++;
                  if (c == '$') {
                      c = counterStream.read();pos++;
                      if (c == '$') {
                          c = counterStream.read();pos++;
                          if (c == '$') {
                              c = counterStream.read();pos++;
                              if (c == '$') {
                                  c = counterStream.read();pos++;
                                  if ( c == '\r') {
                                      c = counterStream.read();// only CR or CR+LF
                                  }else pos--;
                                  if( c == '\n') {
                                      pos++;work = (int) start;
                                      start = pos;
                                      counterStream.read();pos++;
                                      num++;
                                  }else { // next pos already read
                                      work = (int) start;
                                      start = pos;
                                      pos++;
                                  }
                                  values.add( start );
                                  progress.worked( (int) (pos-work) );
                                  if(size >-1) {
                                      progress.subTask(
                                            String.format( "Read: %dMB\\%dMB",
                                              pos/(1048576),size/(1048576)));
                                  }else {
                                      progress.subTask(
                                              String.format( "Read: %dMB",
                                              pos/(1048576)));
                                  }
                                  if ( monitor.isCanceled() ) {
                                      throw new OperationCanceledException();
                                  }
                              }
                          }
                      }
                  }
              }
              if( (pos-start)>3) {
                  values.add(pos);
                  num++;
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
          logger.debug( String.format("numberOfEntriesInSDF took %d to complete",
                                       (int)((System.nanoTime()-tStart)/1e6)));
          progress.done();
          return new SDFileIndex(file,values);
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
              = createConformerIterator( file,
                                        new SubProgressMonitor(monitor, 100) );

          List<ICDKMolecule> mols = new BioList<ICDKMolecule>();

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

        // use four digits in the precision
        double mass = AtomContainerManipulator.getNaturalExactMass(
               cdkmol.getAtomContainer()
        );
        mass = (Math.round(mass*10000.0))/10000.0;

        return mass;
      }

      public ICDKMolecule generate2dCoordinates(IMolecule molecule)
                       throws Exception {
          List<IMolecule> molecules = new ArrayList<IMolecule>();
          molecules.add( molecule );
          return generate2dCoordinates( molecules ).get( 0 );
      
      }

      public List<ICDKMolecule> generate2dCoordinates(List<? extends IMolecule> molecules)
                       throws Exception {

          ICDKMolecule cdkmol = null;
          List<ICDKMolecule> newMolecules= new BioList<ICDKMolecule>();

          for(IMolecule molecule:molecules){
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
  
            org.openscience.cdk.interfaces.IMolecule newmolecule = DefaultChemObjectBuilder.getInstance().newMolecule();
            for ( IAtomContainer mol : mols.molecules() ) {
                sdg.setMolecule( cdkmol.getAtomContainer()
                                       .getBuilder().newMolecule(mol) );
                sdg.generateCoordinates();
                IAtomContainer ac = sdg.getMolecule();
                newmolecule.add(ac);
            }
            newMolecules.add(  new CDKMolecule(newmolecule) );
          }
          return newMolecules;
      }

      public Iterator<ICDKMolecule> createMoleculeIterator(String path)
                                    throws CoreException {
          return createMoleculeIterator( ResourcePathTransformer.getInstance()
                                                                .transform(path),
                                         null );
      }

      public Iterator<ICDKMolecule> creatConformerIterator(IFile file) {
          return createConformerIterator(file, null);
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
          saveMolecule(mol, filename, (IChemFormat)Mol2Format.getInstance());
      }

      public List<ICDKMolecule> loadSMILESFile(String path)
                                throws CoreException,
                                       IOException {
        return loadSMILESFile(
             ResourcePathTransformer.getInstance().transform(path),
             (IProgressMonitor)null
        );
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

          class StringPair {
              final String first;
              final String second;
              StringPair(String first,String second) {
                  this.first = first;
                  this.second = second;
              }
          };

          String line = br.readLine();

          if (line == null)
              throw new IOException("File: " + file.getName()
                                    + " has null contents");
          int cnt = 0;
          List<StringPair> list = new LinkedList<StringPair>();
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
                      list.add( new StringPair(part1,part2) );
                  }else{
                      list.add( new StringPair(part1,"entry-" + cnt) );
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
          String firstKey = list.get( 0 ).first;
          String firstVal = list.get( 0 ).second;
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
          List<ICDKMolecule> mols = new BioList<ICDKMolecule>();
          for (StringPair part : list) {
              if (smilesInFirst) {
                  try {
                      mol = fromSMILES(part.first);
                      mol.setName(part.second);
                      mols.add(mol);
                  } catch (BioclipseException e) {
                  }
              } else {
                  try {
                      mol = fromSMILES(part.second);
                      mol.setName(part.first);
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
          List<IMolecule> molecules = new BioList<IMolecule>();
          molecules.add( molecule );
          return generate3dCoordinates( molecules ).get( 0 );

      }


      public List<IMolecule> generate3dCoordinates(List<IMolecule> molecules)
                       throws Exception {

          ICDKMolecule cdkmol = null;
          List<IMolecule> newMolecules=new BioList<IMolecule>();

          for(int i=0;i<molecules.size();i++){
  
            if ( molecules.get(i) instanceof ICDKMolecule ) {
                cdkmol = (ICDKMolecule) molecules.get(i);
            }else {
                cdkmol=create(molecules.get(i));
            }
  
            ModelBuilder3D mb3d = ModelBuilder3D.getInstance();
            IMoleculeSet mols = ConnectivityChecker.partitionIntoMolecules(
                                    cdkmol.getAtomContainer() );
  
            org.openscience.cdk.interfaces.IMolecule newmolecule
                = cdkmol.getAtomContainer().getBuilder().newMolecule();
  
            for ( IAtomContainer mol : mols.molecules() ) {
  
                org.openscience.cdk.interfaces.IMolecule ac
                    = mb3d.generate3DCoordinates(
                          (org.openscience.cdk.interfaces.IMolecule)mol, false);
                newmolecule.add(ac);
            }
            newMolecules.add( new CDKMolecule(newmolecule) );
          }
          return newMolecules;
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
          saveMolecule(cml, filename, (IChemFormat)CMLFormat.getInstance());
      }

      public void saveMDLMolfile(ICDKMolecule mol, String filename)
                  throws InvocationTargetException,
                         BioclipseException,
                         CDKException,
                         CoreException {
          saveMolecule(mol, filename, (IChemFormat)MDLV2000Format.getInstance());
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

      public void saveSDFile(IFile file, List<IMolecule> entries,
              IProgressMonitor monitor)
                  throws BioclipseException, InvocationTargetException {

          if ( file.exists() )
              throw new BioclipseException("File " + file.getName()
                                           + " already exists");

          if (monitor == null) {
              monitor = new NullProgressMonitor();
          }
          int ticks = 10000;
          try {

              monitor.beginTask( "Writing file", ticks );

            StringWriter writer = new StringWriter();
            SDFWriter mdlwriter = new SDFWriter(writer);
            for (IMolecule molecule : entries) {

                IAtomContainer ac = null;
                if (molecule.getClass().isAssignableFrom(CDKMolecule.class)) {
                    ac = ((CDKMolecule)molecule).getAtomContainer();
                } else {
                    CMLReader reader = new CMLReader(
                        new ByteArrayInputStream(molecule.getCML().getBytes())
                    );
                    ac = ((IChemFile)reader.read(
                            DefaultChemObjectBuilder.getInstance()
                            .newChemFile() ))
                            .getChemSequence(0).getChemModel(0)
                            .getMoleculeSet().getAtomContainer(0);
                }

                  mdlwriter.write(ac);
              }
            mdlwriter.close();

            file.create( new ByteArrayInputStream(writer.toString().getBytes()),
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

      public List<ICDKMolecule> extractFromSDFile( String file, String property,
                                                   Collection<String> value ) {
          throw new IllegalStateException("This method should not be calld, use the one with IFile");
      }

      public List<ICDKMolecule> extractFromSDFile( IFile file,
                                                   String property, Collection<String> value,
                                                   IProgressMonitor monitor) {
          long timer = System.nanoTime();
          monitor.beginTask( "Extracting molecules", 3000);
          monitor.subTask( "Parsing properties" );
          List<String> valueList = new LinkedList<String>(value);
          List<Integer> extractedIndexList = new ArrayList<Integer>();
          List<ICDKMolecule> molList = Collections.emptyList();
          try {
              Map<Integer,String> sdfToProperty = createSDFPropertyMap( file, property );
              double work = 0;
              double val = 1000d/sdfToProperty.size();
              monitor.worked( 1000 );
              monitor.subTask( "Searching for value" );
              boolean found;
              for(int i:sdfToProperty.keySet()) {
                  if(monitor.isCanceled()) {
                      throw new OperationCanceledException();
                  }

                  found = false;
                  String proper = sdfToProperty.get(i);
                  for(String searchValue:valueList) {
                      if(searchValue.equals( proper )) {
                          found = true;
                          break;
                      }
                  }
                  if(found) {
                      extractedIndexList.add( i );
                      valueList.remove( Integer.valueOf( i ) );
                  }

                  work+=val;
                  if(work >1) {
                      monitor.worked( (int)work );
                      work = work -(int)work;
                  }
              }
              Collections.sort( extractedIndexList );
              createSDFileIndex( file, monitor );
              molList = new BioList<ICDKMolecule>();

              IChemObjectBuilder builder = DefaultChemObjectBuilder
              .getInstance();
              RandomAccessSDFReader reader;
              IPath location = file.getLocation();
              java.io.File jFile = (location!=null?location.toFile():null);
              if(jFile == null) {
                  monitor.done();
                  throw new IllegalArgumentException("Not a local file");
              }
              reader = new RandomAccessSDFReader( jFile, builder );
              for(int v:extractedIndexList) {
                  IChemObject obj =reader.readRecord( v );
                  molList.add( new CDKMolecule((IAtomContainer)obj));
              }
              reader.close();
          } catch (IOException e ) {
              monitor.done();
              LogUtils.debugTrace( logger, e );
              logger.error( "Failed to extract molecules from "+file.getName() );
          } catch ( Exception e ) {
              monitor.done();
              LogUtils.debugTrace( logger, e );
              logger.error( "Failed to extract molecules from "+file.getName() );
          }
          monitor.done();
          logger.debug( "Created property map in "
                      + (int) ((System.nanoTime() - timer) / 1e6)
                      + "ms" );
          return molList;
      }
      public Map<Integer,String> createSDFPropertyMap( String file,
                                                     String property)
                                                     throws CoreException,
                                                     IOException{
          throw new IllegalStateException("This method should not be calld, use the one with IFile");

      }
      public Map<Integer,String> createSDFPropertyMap( IFile file,
                                                        String property)
                                                        throws CoreException,
                                                        IOException{
          LineNumberReader input = new LineNumberReader(
                                   new InputStreamReader(file.getContents()));
          Map<Integer,String> result = new HashMap<Integer, String >();
          Pattern propertyPattern = Pattern.compile( "^>.*<"+property+">.*");
        Pattern endOfEntry = Pattern.compile("\\${4}");
        int start = 0;
        String line;
        int molIndex = 0;
        String val;
        long tStart = System.nanoTime();
        while((line = input.readLine())!=null) {
            Matcher match = propertyPattern.matcher( line );
            if(match.matches()) {
               val = input.readLine();
               if(val==null) {
                   input.close();
                   throw new RuntimeException("Excpected a property value");
               }
               result.put( molIndex, val );
            }
            Matcher m2 = endOfEntry.matcher( line );
            if(m2.matches()) {
                molIndex++;
                start = input.getLineNumber();
            }
        }
        if(input.getLineNumber()!=start) {
            molIndex++;
        }
        logger.debug( "Created property map in "
                      + (int) ((System.nanoTime() - tStart) / 1e6)
                      + "ms" );
        input.close();
        return result;
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
              List<IMolecule> result=new BioList<IMolecule>();
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

    public void saveSDFile( String file, List<IMolecule> entries )
                                                                throws BioclipseException,
                                                                InvocationTargetException {
        saveSDFile(
            ResourcePathTransformer.getInstance().transform(file),
            entries,
            null
        );
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

        if (missingHCount > 0) {
            mf.addIsotope( m.getAtomContainer().getBuilder()
                    .newIsotope( Elements.HYDROGEN),
                    missingHCount
            );
        }

        return MolecularFormulaManipulator.getString( mf );
    }

    private int calculateMissingHydrogens( IAtomContainer container,
                                           IAtom atom ) {
        CDKAtomTypeMatcher matcher
            = CDKAtomTypeMatcher.getInstance(container.getBuilder());
        IAtomType type;
        try {
            type = matcher.findMatchingAtomType(container, atom);
            if (type == null || type.getAtomTypeName() == null)
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

    // IMPORTANT: the String is the *prefix*, so when matching
    // startsWith() MUST be used, and NOT an exact match!
    private final static Map<String, IChemFormat> contentTypeMap =
        new HashMap<String, IChemFormat>();

    static {
        contentTypeMap.put(
                "net.bioclipse.contenttypes.cml",
                (IChemFormat)CMLFormat.getInstance());
        contentTypeMap.put(
                "net.bioclipse.contenttypes.mdlMolFile",
                (IChemFormat)MDLV2000Format.getInstance());
        contentTypeMap.put(
                "net.bioclipse.contenttypes.pubchem.xml.compounds",
                (IChemFormat)PubChemCompoundsXMLFormat.getInstance());
        contentTypeMap.put(
                "net.bioclipse.contenttypes.pubchem.xml.substances",
                (IChemFormat)PubChemSubstancesXMLFormat.getInstance());
        contentTypeMap.put(
                "net.bioclipse.contenttypes.pubchem.xml.substance",
                (IChemFormat)PubChemSubstanceXMLFormat.getInstance());
        contentTypeMap.put(
                "net.bioclipse.contenttypes.pubchem.xml.compound",
                (IChemFormat)PubChemCompoundXMLFormat.getInstance());
        contentTypeMap.put(
                "net.bioclipse.contenttypes.smi",
                (IChemFormat)SMILESFormat.getInstance());
        contentTypeMap.put(
                "net.bioclipse.contenttypes.pdb",
                (IChemFormat)PDBFormat.getInstance());
        contentTypeMap.put(
                "net.bioclipse.contenttypes.cif",
                (IChemFormat)CIFFormat.getInstance());
    }

    public IChemFormat determineFormat(IContentType type) {
        if (type == null) return null;

        // first try a quick exact match ...
        if (contentTypeMap.containsKey(type.getId()))
            return contentTypeMap.get(type.getId());
        // ... then as prefix
        for (String prefix : contentTypeMap.keySet()) {
            if (type.getId().startsWith(prefix)) {
                return contentTypeMap.get(prefix);
            }
        }

        // OK, no clue...
        return null;
    }

    public IChemFormat getFormat(String type) {
        List<IChemFormatMatcher> formats = formatsFactory.getFormats();
        for (IChemFormatMatcher format : formats) {
            if (format.getClass().getName().substring(
                    "org.openscience.cdk.io.formats.".length()
                ).equals(type)) return format;
        }
        return null;
    }

    public String getFormats() {
        StringBuffer buffer = new StringBuffer();
        List<IChemFormatMatcher> formats = formatsFactory.getFormats();
        for (IChemFormatMatcher format : formats) {
            buffer.append(
                format.getClass().getName().substring(
                    "org.openscience.cdk.io.formats.".length()
                )
            );
            buffer.append(": ");
            buffer.append(format.getFormatName());
            buffer.append('\n');
        }
        return buffer.toString();
    }

    public List<IMolecule> createMoleculeList() throws BioclipseException,
            InvocationTargetException {
        return new BioList<IMolecule>();
    }

    public IMolecule perceiveAromaticity( IMolecule mol ) throws BioclipseException {
        IAtomContainer todealwith;
        if(mol instanceof ICDKMolecule){
            todealwith = ((ICDKMolecule) mol).getAtomContainer();
        }else{
            todealwith = create( mol ).getAtomContainer();
        }
        try{
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(todealwith);
            CDKHueckelAromaticityDetector.detectAromaticity(todealwith);
        }catch(CDKException ex){
            throw new BioclipseException("Problems perceiving aromaticity: "+ex.getMessage());
        }
        return new CDKMolecule( ((ICDKMolecule) mol).getAtomContainer() );
    }

    public List<IAtomContainer> partition(IMolecule molecule)
            throws BioclipseException {
        IAtomContainer todealwith;
        if (molecule instanceof ICDKMolecule) {
            todealwith = ((ICDKMolecule) molecule).getAtomContainer();
        } else {
            todealwith = create( molecule ).getAtomContainer();
        }

        IMoleculeSet set = ConnectivityChecker.partitionIntoMolecules(todealwith);
        List<IAtomContainer> result = new ArrayList<IAtomContainer>();
        for (IAtomContainer container : set.atomContainers()) {
            result.add(container);
        }

        return result;
    }

    public int totalFormalCharge(IMolecule molecule)
            throws BioclipseException {
        IAtomContainer todealwith;
        if (molecule instanceof ICDKMolecule) {
            todealwith = ((ICDKMolecule) molecule).getAtomContainer();
        } else {
            todealwith = create( molecule ).getAtomContainer();
        }

        int totalCharge = 0;
        for (IAtom atom : todealwith.atoms()) {
            totalCharge += atom.getFormalCharge() == null ? 0 : atom.getFormalCharge();
        }
        return totalCharge;
    }

    public float calculateTanimoto(BitSet fingerprint1, BitSet fingerprint2)
        throws BioclipseException {
        try {
            return Tanimoto.calculate(fingerprint1, fingerprint2);
        } catch (CDKException exception) {
            throw new BioclipseException(
                "Could not calculate the tanimoto distance between the two " +
                "fingerprints: " + exception.getMessage(), exception
            );
        }
    }

    public float calculateTanimoto(IMolecule calculateFor, BitSet reference )
        throws BioclipseException {
        BitSet f2 = create(calculateFor).getFingerprint(true);
        return calculateTanimoto( reference, f2 );
    }

    public float calculateTanimoto( IMolecule calculateFor, 
                                    IMolecule reference )
                                    throws BioclipseException {
        BitSet f1 = create(reference).getFingerprint(true);
        BitSet f2 = create(calculateFor).getFingerprint(true);
        return calculateTanimoto( f1, f2 );
    }

    public List<Float> calculateTanimoto( List<IMolecule> calculateFor,
                                  IMolecule reference )
                                  throws BioclipseException {
        List<Float> result=new ArrayList<Float>();
        BitSet refensetBitSet = create(reference).getFingerprint(true);
        for(int i=0;i<calculateFor.size();i++ ){
            result.add(
                calculateTanimoto(calculateFor.get(i), refensetBitSet)
            );
        }
        return result;
    }

    public String getMDLMolfileString(ICDKMolecule molecule) {
        StringWriter stringWriter = new StringWriter();
        MDLWriter writer = new MDLWriter(stringWriter);
        try {
            writer.writeMolecule(molecule.getAtomContainer());
            writer.close();
        } catch (Exception exc) {
            logger.error(
                "Error while creating MDL molfile string: " + exc.getMessage(),
                exc);
            return null;
        }
        return stringWriter.toString();
    }

    public Object getProperty(ICDKMolecule molecule, Object propertyName) {
        IAtomContainer container = molecule.getAtomContainer();
        if (container == null) {
            throw new IllegalArgumentException(
                "Passed ICDKMolecule has a null IAtomContainer."
            );
        }
        return container.getProperty(propertyName);
    }

    public Object setProperty(ICDKMolecule molecule, Object propertyName,
            Object propertyValue) {
        IAtomContainer container = molecule.getAtomContainer();
        if (container == null) {
            throw new IllegalArgumentException(
                "Passed ICDKMolecule has a null IAtomContainer."
            );
        }
        Object oldValue = container.getProperty(propertyName);
        container.setProperty(propertyName, propertyValue);
        return oldValue;
    }
}
