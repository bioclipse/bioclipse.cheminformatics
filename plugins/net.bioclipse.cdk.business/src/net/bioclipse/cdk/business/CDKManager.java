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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
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
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bioclipse.cdk.domain.CDKConformer;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.MoleculesInfo;
import net.bioclipse.cdk.exceptions.TimedOutException;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.core.domain.RecordableList;
import net.bioclipse.core.domain.SMILESMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;
import nu.xom.Element;

import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
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
import org.openscience.cdk.exception.NoSuchAtomTypeException;
import org.openscience.cdk.fingerprint.FingerprinterTool;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.geometry.alignment.KabschAlignment;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.FormatFactory;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLV2000Writer;
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
import org.openscience.cdk.io.formats.RGroupQueryFormat;
import org.openscience.cdk.io.formats.SDFFormat;
import org.openscience.cdk.io.formats.SMILESFormat;
import org.openscience.cdk.io.iterator.IteratingMDLConformerReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.io.random.RandomAccessReader;
import org.openscience.cdk.io.random.RandomAccessSDFReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.IQueryAtom;
import org.openscience.cdk.isomorphism.matchers.OrderQueryBond;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.isomorphism.matchers.smarts.AromaticQueryBond;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.libio.cml.ICMLCustomizer;
import org.openscience.cdk.modeling.builder3d.ModelBuilder3D;
import org.openscience.cdk.nonotify.NNAtomContainer;
import org.openscience.cdk.nonotify.NNChemFile;
import org.openscience.cdk.nonotify.NNMolecule;
import org.openscience.cdk.nonotify.NNMoleculeSet;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.similarity.Tanimoto;
import org.openscience.cdk.smiles.DeduceBondSystemTool;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.smiles.smarts.parser.TokenMgrError;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.xmlcml.cml.element.CMLAtomType;

/**
 * The manager class for CDK. Contains CDK related methods.
 *
 * @author olas, jonalv
 */
public class CDKManager implements IBioclipseManager {

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

    public String getManagerName() {
        return "cdk";
    }

    public ICDKMolecule newMolecule() {
        return new CDKMolecule(new NNAtomContainer());
    }

    public ICDKMolecule newMolecule(IAtomContainer atomContainer) {
    	return new CDKMolecule(atomContainer);
    }

    public List<ICDKMolecule> asList(IMoleculeSet set) {
    	List<ICDKMolecule> result = new RecordableList<ICDKMolecule>();
    	for (IAtomContainer mol : set.molecules()) {
    		result.add(newMolecule(mol));
    	}
    	return result;
    }

    public IMoleculeSet asSet(List<ICDKMolecule> list) {
    	IMoleculeSet set = new NNMoleculeSet();
    	for (ICDKMolecule mol : list)
    		set.addAtomContainer(mol.getAtomContainer());
    	return set;
    }

    public ICDKMolecule loadMolecule(IFile file, IProgressMonitor monitor)
                        throws IOException, BioclipseException, CoreException {

        IChemFormat format = determineIChemFormat(file);
        ICDKMolecule loadedMol = loadMolecule( file.getContents(),
                                               format,
                                               monitor
        );
        loadedMol.setResource(file);

        return loadedMol;
    }

    public ICDKMolecule loadMolecule( InputStream instream,
                                      IChemFormat format,
                                      IProgressMonitor monitor )
                        throws BioclipseException, IOException {

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        // Create the reader
        ISimpleChemObjectReader reader = readerFactory.createReader(format);
        if (reader == null) {
            throw new BioclipseException("Could not create reader in CDK.");
        }

        try {
            reader.setReader(instream);
        }
        catch ( CDKException e1 ) {
            throw new RuntimeException(
                "Failed to set the reader's inputstream", e1
            );
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
        if (atomContainersList.size() == 0)
            throw new RuntimeException("File did not contain any molecule");
        if (atomContainersList.size() > 1)
            logger.debug("Ignoring all but the first molecule.");

        IAtomContainer containerToReturn = atomContainersList.get(0);
        // sanatize the input for certain file formats
        CDKMolecule retmol = new CDKMolecule(containerToReturn);
        String molName = (String)containerToReturn
            .getProperty(CDKConstants.TITLE);
        if (molName != null && !(molName.length() > 0)) {
            retmol.setName(molName);
        }
        // try to recover certain information for certain content types
        sanatizeFileInput(format, retmol);
        // OK, and we're done
        monitor.done();
        return retmol;
    }

    private void sanatizeFileInput(IChemFormat format,
                                   CDKMolecule molecule) {
        if (format == MDLV2000Format.getInstance()) {
            sanatizeMDLV2000MolFileInput(molecule);
        }
    }

    private void sanatizeMDLV2000MolFileInput(CDKMolecule molecule) {
        IAtomContainer container = molecule.getAtomContainer();
        if (container != null && container.getAtomCount() > 0) {
            CDKHydrogenAdder hAdder =
                CDKHydrogenAdder.getInstance(container.getBuilder());
            CDKAtomTypeMatcher matcher =
                CDKAtomTypeMatcher.getInstance(container.getBuilder());
            try {
                // perceive atom types
                IAtomType[] types = matcher.findMatchingAtomType(container);
                for (int i=0; i<container.getAtomCount(); i++) {
                    if (types[i] != null) {
                        IAtom atom = container.getAtom(i);
                        // set properties needed for H adding and aromaticity
                        atom.setAtomTypeName(types[i].getAtomTypeName());
                        atom.setHybridization(types[i].getHybridization());
                        hAdder.addImplicitHydrogens(container, atom);
                    }
                }
                // perceive aromaticity
                CDKHueckelAromaticityDetector.detectAromaticity(container);
            } catch ( CDKException e ) {
                e.printStackTrace();
            }
        }
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

        IChemFormat format = determineIChemFormat(file);
        List<ICDKMolecule> mols = loadMolecules( file.getContents(),
                                                 format,
                                                 monitor
        );
        for ( ICDKMolecule m : mols ) {
            m.setResource( file );
        }

        return mols;
    }

    public List<ICDKMolecule> loadMolecules( InputStream contents,
                                             IChemFormat format,
                                             IProgressMonitor monitor )
                              throws BioclipseException,
                                     CoreException,
                                     IOException {

        if (contents == null)
            throw new BioclipseException(
                          "Cannot load molecules: content was null" );

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        List<ICDKMolecule> moleculesList = new RecordableList<ICDKMolecule>();

        int ticks = 10000;

        try {
             monitor.beginTask("Reading file", ticks);
             System.out.println( "no formats supported: "
                                 + readerFactory.getFormats().size() );
             ISimpleChemObjectReader reader = null;

             if (format == null) {
                 reader = readerFactory.createReader( contents );
             }
             else {
                 reader = readerFactory.createReader(format);
             }

             if (reader == null) {

                 // Try SMILES
                 List<ICDKMolecule> moleculesList2 = loadSMILESFile( contents,
                                                                     monitor );
                 if (moleculesList2 != null && moleculesList2.size() > 0)
                     return moleculesList2;

                 // Ok, not even SMILES works
                 throw new BioclipseException(
                               "Could not create reader in CDK." );
             }

             try {
                 reader.setReader( contents );
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
                 throw new BioclipseException( "Could not read input: " +
                                               e.getMessage(), e );
             }

             // Store the chemFormat used for the reader
             IChemFormat chemFormat = (IChemFormat)reader.getFormat();
             System.out.println( "Read CDK chemfile with format: "
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

                // try to recover certain information for certain content types
                sanatizeFileInput(chemFormat, mol);

                String moleculeName = molecularFormula( mol );
                // If there's a CDK property TITLE (read from input), use that
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

    public List<ICDKMolecule> loadMolecules( IFile file,
                                             IChemFormat format,
                                             IProgressMonitor monitor )
                              throws IOException,
                                     BioclipseException,
                                     CoreException {
        List<ICDKMolecule> mols = loadMolecules( file.getContents(),
                                                 format,
                                                 monitor );
        for ( ICDKMolecule m : mols ) {
            m.setResource( file );
        }

        return mols;
    }

      public void calculateSMILES(IMolecule molecule,IReturner<String> returner,
                                  IProgressMonitor monitor ) {
        assert(molecule!=null);
        monitor.beginTask( "Calculating SMILES", IProgressMonitor.UNKNOWN );
        ICDKMolecule mol;
        try {
            mol = asCDKMolecule( molecule );
        } catch ( BioclipseException e ) {
            LogUtils.handleException( e, logger, "net.bioclipse.cdk.business" );
            return;
        }
        String result;
        try {
            IAtomContainer cdkMol = mol.getAtomContainer();

        // Create the SMILES
        SmilesGenerator generator = new SmilesGenerator();

        // Operate on a clone with removed hydrogens
        cdkMol = AtomContainerManipulator.removeHydrogens(cdkMol);
        org.openscience.cdk.interfaces.IMolecule newMol;
        if (org.openscience.cdk.interfaces.IMolecule.class.isAssignableFrom(
            cdkMol.getClass())) {
            newMol = (org.openscience.cdk.interfaces.IMolecule)cdkMol;
        } else {
            newMol = cdkMol.getBuilder().newInstance(
            	org.openscience.cdk.interfaces.IMolecule.class, cdkMol);
        }
        result = generator.createSMILES( newMol );
        }catch (Exception e) {
            logger.warn( "Failed to generate SMILES",e);
            return;
        }
        if(monitor.isCanceled())
            throw new OperationCanceledException();
        returner.completeReturn( result );
      }

      public void save(IChemModel model, String target, IChemFormat filetype,
                        Properties writerProperties)
                  throws BioclipseException, CoreException {
          save( model,
                ResourcePathTransformer.getInstance().transform(target),
                filetype,
                null,
                writerProperties);
      }

      public void save( IAtomContainer model,
              IFile target,
              IChemFormat filetype,
              IProgressMonitor monitor,
              Properties writerProperties )
      throws BioclipseException, CoreException {
    	  saveChemObject(model, target, filetype, monitor, writerProperties);
      }

      public void save( IChemModel model,
                        IFile target,
                        IChemFormat filetype,
                        IProgressMonitor monitor,
                        Properties writerProperties )
                  throws BioclipseException, CoreException {
    	  saveChemObject(model, target, filetype, monitor, writerProperties);
      }


      private void saveChemObject( IChemObject model,
              IFile target,
              IChemFormat filetype,
              IProgressMonitor monitor,
              Properties writerProperties )
        throws BioclipseException, CoreException {
    	  if (!(model instanceof IAtomContainer ||
    			model instanceof IChemModel))
    		  throw new BioclipseException(
    		      "The model must be an IChemModel or an IAtomContainer."
    		  );

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
            } else if (filetype == MDLV2000Format.getInstance()) {
            	chemWriter = new MDLV2000Writer(writer);
            } else {
                chemWriter = writerFactory.createWriter(filetype);
                if (chemWriter == null) {
                    throw new BioclipseException(
                        "No writer available for this format: " +
                        filetype.getFormatName());
                }
                chemWriter.setWriter(writer);
            }
            // register a listener for the writer option
            if (writerProperties != null) {
                PropertiesListener listener = new PropertiesListener(
                    writerProperties
                );
                chemWriter.addChemObjectIOListener(listener);
            }
            // if CMLWriter, add a customizer to cause writing of atom types
            if (chemWriter instanceof CMLWriter) {
                CMLWriter cmlWriter = (CMLWriter)chemWriter;
                cmlWriter.registerCustomizer(new ICMLCustomizer() {
                    public void customize( IAtom atom, Object nodeToAdd )
                        throws Exception {
                        if (atom.getAtomTypeName() != null) {
                            if (nodeToAdd instanceof Element) {
                                Element element = (Element)nodeToAdd;
                                CMLAtomType atomType = new CMLAtomType();
                                atomType.setConvention("bioclipse:atomType");
                                atomType.appendChild(atom.getAtomTypeName());
                                element.appendChild(atomType);
                            }
                        }
                    }
                    // don't customize the rest
                    public void customize( IBond bond, Object nodeToAdd )
                        throws Exception {}
                    public void customize( IAtomContainer molecule,
                        Object nodeToAdd ) throws Exception {}
                });
            }

            // write the model
            if (model instanceof IAtomContainer) {
            	if (chemWriter.accepts(AtomContainer.class)) {
            		chemWriter.write(model);
            	} else if (chemWriter.accepts(AtomContainerSet.class)) {
            		IAtomContainerSet set =
            			model.getBuilder().newInstance(IAtomContainerSet.class);
            		set.addAtomContainer((IAtomContainer)model);
            		chemWriter.write(set);
            	} else if (chemWriter.accepts(ChemModel.class)) {
            		IMoleculeSet set =
            			model.getBuilder().newInstance(IMoleculeSet.class);
            		set.addAtomContainer((IAtomContainer)model);
            		IChemModel chemModel = model.getBuilder().newInstance(
            			IChemModel.class);
            		chemModel.setMoleculeSet(set);
            		chemWriter.write(chemModel);
            	} else {
                    throw new BioclipseException(
                        "Writer does not support writing of " +
                        "IAtomContainer, IMoleculeSet, and IChemModel: "+
                        chemWriter.getClass().getName()
                    );
            	}
            } else if (model instanceof IChemModel) {
            	if (chemWriter.accepts(ChemModel.class)) {
            		chemWriter.write(model);
            	} else if (chemWriter.accepts(MoleculeSet.class)){
            		IMoleculeSet list =
            			model.getBuilder().newInstance(IMoleculeSet.class);
            		for (IAtomContainer container :
            			ChemModelManipulator.getAllAtomContainers(
            			    (IChemModel)model)
            			) {
            			list.addAtomContainer(container);
            		}
            		chemWriter.write(list);
            	} else if (chemWriter.accepts(Molecule.class)){
            		org.openscience.cdk.interfaces.IMolecule smashedContainer =
            			model.getBuilder().newInstance(
            				org.openscience.cdk.interfaces.IMolecule.class);
            		for (IAtomContainer container :
            			ChemModelManipulator.getAllAtomContainers(
            				(IChemModel)model)
            			) {
            			smashedContainer.add(container);
            		}
            		chemWriter.write(smashedContainer);
            	} else {
            		throw new BioclipseException("Writer does not support writing" +
            		"IChemModel or IMolecule.");
            	}
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
          } catch (CDKException exception) {
              throw new BioclipseException(
                  "Failed to write file: " + exception.getMessage(),
                  exception
              );
        } finally {
              monitor.done();
          }
      }

      public void saveMolecule(IMolecule mol)
                  throws BioclipseException, CoreException {
          saveMolecule(mol, false);
      }

      public void saveMolecule(IMolecule mol, String filename)
                  throws BioclipseException, CoreException {
          saveMolecule(mol, filename, false);
      }

      public void saveMolecule(IMolecule mol, boolean overwrite)
                  throws BioclipseException, CoreException {

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
                throws BioclipseException, CoreException {

        IFile file = ResourcePathTransformer.getInstance().transform(filename);
        saveMolecule(mol, file, overwrite);
    }

    public void saveMolecule(IMolecule mol, IFile file, boolean overwrite)
                throws BioclipseException, CoreException {

    	if ( file.exists() && overwrite == false ) {
            throw new BioclipseException("File already exists!");
        }

        IChemFormat format = null;

        // are we really overwriting an old file?
        if (mol.getResource() != null &&
            (mol.getResource() instanceof IFile)) {
            IFile oldFile = (IFile)mol.getResource();
            if (oldFile.getContentDescription() == null) {
                logger.warn(
                    "Unexpected null value for content description, " +
                    "when saving to an existing file. Why did Eclipse loose " +
                    "the content type??"
                );
                try {
                    format = determineIChemFormatOfStream(oldFile.getContents());
                } catch (IOException exception) {
                    logger.error(
                        "Error while determining content type from the " +
                        "InputStream of an existing IFile", exception
                    );
                }
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
            } else if (aFormat == RGroupQueryFormat.getInstance()) {
            	// Bioclipse does not support such files yet
            } else if (file.endsWith("."+aFormat.getPreferredNameExtension())) {
                return aFormat;
            }
        }
        return null;
    }

    public void saveMolecule( IMolecule mol_in,
                              String filename,
                              IChemFormat filetype )
                throws BioclipseException, CoreException {
          this.saveMolecule(mol_in, filename, filetype, false);
      }

      public void saveMolecule(IMolecule mol_in, IFile target, IChemFormat filetype)
                  throws BioclipseException, CoreException {
          this.saveMolecule(mol_in, target, filetype, false);
      }

      public void saveMolecule( IMolecule mol_in,
                                IFile target,
                                IChemFormat filetype,
                                boolean overwrite)
                throws BioclipseException, CoreException {
          saveMolecule(mol_in, target, filetype, overwrite, null);
      }

      public void saveMolecule( IMolecule mol_in,
                                IFile target,
                                IChemFormat filetype,
                                boolean overwrite,
                                Properties writerProperties)
                throws BioclipseException, CoreException {

          if ( target.exists() && overwrite == false ) {
              throw new BioclipseException("File already exists!");
          }

          ICDKMolecule mol = asCDKMolecule(mol_in);
          this.save(
            mol.getAtomContainer(), target, filetype, null, writerProperties
          );

        mol.setResource(target);
      }

      public void saveMolecule( IMolecule mol,
                                String filename,
                                IChemFormat filetype,
                                boolean overwrite)
                  throws BioclipseException, CoreException {
          saveMolecule(mol, filename, filetype, overwrite, null);
      }

      public void saveMolecule( IMolecule mol,
                                String filename,
                                IChemFormat filetype,
                                boolean overwrite,
                                Properties writerProperties)
                  throws BioclipseException, CoreException {

          saveMolecule( mol,
                        ResourcePathTransformer.getInstance()
                                               .transform(filename),
                        filetype,
                        overwrite,
                        writerProperties);
      }

      /**
       * Delegate to IFile via ResourcePathTransformer
       */
      public void saveMolecules( List<? extends IMolecule> molecules,
                                 String path,
                               IChemFormat filetype )
                  throws BioclipseException, CoreException {

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
                  throws BioclipseException, CoreException {

          if ( filetype == CMLFormat.getInstance() ||
               filetype == MDLV2000Format.getInstance() ||
               filetype == SDFFormat.getInstance()) {

              IChemModel chemModel = new ChemModel();
              chemModel.setMoleculeSet(
                  chemModel.getBuilder().newInstance(IMoleculeSet.class)
              );
              for (IMolecule mol : molecules) {

                  ICDKMolecule cdkmol = asCDKMolecule(mol);
                  org.openscience.cdk.interfaces.IMolecule imol = null;

                  if (cdkmol.getAtomContainer() instanceof
                          org.openscience.cdk.interfaces.IMolecule) {
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

              this.save(chemModel, target, filetype, null, null);
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
                               (IChemFormat)CMLFormat.getInstance(),
                               new NullProgressMonitor() );
      }

    public ICDKMolecule fromString(String molstring)
        throws BioclipseException, IOException {
        if (molstring == null)
            throw new BioclipseException("Input cannot be null.");
        if (molstring.length() == 0)
            throw new BioclipseException("Input cannot be empty.");

        IChemFormat format = determineIChemFormatOfString(molstring);
        if (format == null)
            throw new BioclipseException(
                "Could not identify format for the input string."
            );

        System.out.println("Format: " + format);
        return loadMolecule(
            new ByteArrayInputStream(molstring.getBytes()),
            format,
            new NullProgressMonitor()
        );
    }

    public List<ICDKMolecule> moleculesFromString( String s )
                              throws BioclipseException,
                                     IOException,
                                     CoreException {
        if (s == null)
            throw new BioclipseException("Input cannot be null.");
        if (s.length() == 0)
            throw new BioclipseException("Input cannot be empty.");

        IChemFormat format = determineIChemFormatOfString(s);
        if (format == null)
            throw new BioclipseException(
                "Could not identify format for the input string."
            );

        System.out.println("Format: " + format);
        return loadMolecules(
            new ByteArrayInputStream(s.getBytes()),
            format,
            new NullProgressMonitor()
        );
    }

    public Iterator<ICDKMolecule>
          createMoleculeIterator( IFile file, IProgressMonitor monitor)
              throws CoreException, IOException, BioclipseException {

    	IChemFormat format = determineIChemFormat(file);
    	if (format == SMILESFormat.getInstance()) {
    		return null;
    		//TODO: Implement IteratingBioclipseSMILESReader
    	}
    	else if (format == null) {
    		throw new BioclipseException("Unsupported format for file: " + file.getName());
    	}

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

          return FingerprinterTool.isSubset(
              molecule.getFingerprint(net.bioclipse.core.domain
                  .IMolecule.Property.USE_CALCULATED),
              molecule.getFingerprint(net.bioclipse.core.domain
                  .IMolecule.Property.USE_CALCULATED)
          );
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

      public List<ICDKMolecule> subStructureMatches(
          List<ICDKMolecule> molecules, ICDKMolecule subStructure ) {
          List<ICDKMolecule> matches = new RecordableList<ICDKMolecule>();
          for (ICDKMolecule molecule : molecules) {
              try {
                  if (subStructureMatches(molecule, subStructure)) {
                      matches.add(molecule);
                  }
              } catch (RuntimeException exception) {
                  // just don't add the structure as hit
              }
          }
          return matches;
      }

      public List<ICDKMolecule> getSubstructures(ICDKMolecule molecule,
                                                 ICDKMolecule substructure)
          throws BioclipseException {
          IAtomContainer originalContainer = molecule.getAtomContainer();

          // the below code is going to use IAtom.Id, so we need to keep
          // track of the originals. At the same time, we overwrite them
          // with internal identifiers.
          String[] originalIdentifiers =
              new String[originalContainer.getAtomCount()];
          for (int i=0; i<originalContainer.getAtomCount(); i++) {
              IAtom atom = originalContainer.getAtom(i);
              originalIdentifiers[i] = atom.getID();
              // the new identifier is simply the position in the container
              atom.setID(""+i);
          }

          List<IAtomContainer> uniqueMatches = new ArrayList<IAtomContainer>();
          try {
              // get all matches, which may include duplicates
              List<List<RMap>> substructures = UniversalIsomorphismTester
              .getSubgraphMaps(molecule.getAtomContainer(),
                               substructure.getAtomContainer());
              int i = 1;
              for (List<RMap> substruct : substructures) {
                  // convert the RMap into an IAtomContainer
                  IAtomContainer match = new NNAtomContainer();
                  for (RMap mapping : substruct) {
                      IBond bond = originalContainer.getBond(mapping.getId1());
                      for (IAtom atom : bond.atoms()) match.addAtom(atom);
                      match.addBond(bond);
                  }
                  // OK, see if we already have an equivalent match
                  boolean foundEquivalentSubstructure = false;
                  for (IAtomContainer mol : uniqueMatches) {
                      QueryAtomContainer matchQuery = createQueryContainer(
                          match
                      );
                      if (UniversalIsomorphismTester.isIsomorph(
                              mol, matchQuery))
                          foundEquivalentSubstructure = true;
                  }
                  if (!foundEquivalentSubstructure) {
                      // make a clone (to ensure modifying it doesn't change the
                      // original), and wrap in a CDKMolecule.
                      uniqueMatches.add((IAtomContainer)match.clone());
                  }
              }
          } catch ( CDKException e ) {
              throw new BioclipseException(
                  "Error while finding substructures: " +
                  e.getMessage(), e
              );
          } catch ( CloneNotSupportedException e ) {
              throw new BioclipseException(
                  "Error while finding substructures: " +
                  e.getMessage(), e
              );
          }
          // set up a List<ICDKMolecule> return list
          List<ICDKMolecule> molecules = new RecordableList<ICDKMolecule>();
          for (IAtomContainer mol : uniqueMatches) {
              molecules.add(new CDKMolecule(mol));
          }

          // restore the original identifiers
          for (int i=0; i<originalContainer.getAtomCount(); i++) {
              originalContainer.getAtom(i).setID(
                  originalIdentifiers[i]
              );
          }

          return molecules;
      }

      private static QueryAtomContainer createQueryContainer(
          IAtomContainer container) {
          QueryAtomContainer queryContainer = new QueryAtomContainer();
          for (int i = 0; i < container.getAtomCount(); i++) {
              queryContainer.addAtom(
                 new AtomIDQueryAtom(container.getAtom(i).getID())
              );
          }
          Iterator<IBond> bonds = container.bonds().iterator();
          while (bonds.hasNext()) {
              IBond bond = bonds.next();
              int index1 = container.getAtomNumber(bond.getAtom(0));
              int index2 = container.getAtomNumber(bond.getAtom(1));
              if (bond.getFlag(CDKConstants.ISAROMATIC)) {
                  queryContainer.addBond(new AromaticQueryBond((IQueryAtom) queryContainer.getAtom(index1),
                                        (IQueryAtom) queryContainer.getAtom(index2),
                                        IBond.Order.SINGLE));
              } else {
                  queryContainer.addBond(new OrderQueryBond((IQueryAtom) queryContainer.getAtom(index1),
                                        (IQueryAtom) queryContainer.getAtom(index2),
                                        bond.getOrder()));
              }
          }
          return queryContainer;
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
       * ICDKMolecule from CML. If that fails, tries to create from SMILES.
       */
      public ICDKMolecule asCDKMolecule(IMolecule imol) throws BioclipseException {

          if (imol instanceof ICDKMolecule) {
              return (ICDKMolecule) imol;
          }

          // First try to create from CML
          try {
              String cmlString = imol.toCML();
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
          return fromSMILES( imol.toSMILES(
          ) );
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
              if ( e.getMessage().contains(
                       "Timeout for AllringsFinder exceeded" ) ) {
                  throw new TimedOutException( "The AllringsFinder in CDK " +
                  		                          "did not succed in time", e );
              }
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

      public List<IAtomContainer> getSmartsMatches(ICDKMolecule molecule,
                                                   String smarts)
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
                logger.debug("Found " + nmatch + " SMARTS matches");

                List<List<Integer>> mappings = querytool.getMatchingAtoms();
                for (int i = 0; i < nmatch; i++) {
                    List<Integer> atomIndices = (List<Integer>) mappings.get(i);
                    IAtomContainer match =
                    	ac.getBuilder().newInstance(IAtomContainer.class);
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

          SubMonitor progress = SubMonitor.convert( monitor ,100);
          long size = -1;
          try {
              size = EFS.getStore( file.getLocationURI() )
                        .fetchInfo().getLength();
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
          logger.debug( String.format(
                            "numberOfEntriesInSDF took %d to complete",
                            (int)((System.nanoTime()-tStart)/1e6)) );
          progress.done();
          return values.size();
      }

      private static class Record {
          public Record(int s, int l) {
              start =s;
              length = l;
          }
          int start;
          int length;
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

          List<ICDKMolecule> mols = new RecordableList<ICDKMolecule>();

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
        } else {
            cdkmol = asCDKMolecule(molecule);
        }

        IMolecularFormula mf = molecularFormulaObject( cdkmol );
        // use four digits in the precision
        double mass = MolecularFormulaManipulator.getNaturalExactMass(mf);
        mass = (Math.round(mass*10000.0))/10000.0;

        return mass;
    }

      public void generate2dCoordinates(IMolecule molecule,
                                        IReturner<IMolecule> returner,
                                        IProgressMonitor monitor)
                       throws Exception {
          monitor.beginTask( "Creating 2d coordinates", IProgressMonitor.UNKNOWN );
          List<IMolecule> molecules = new ArrayList<IMolecule>();
          molecules.add( molecule );
          returner.completeReturn( generate2dCoordinates( molecules,
                                                          monitor).get( 0 ) );
          monitor.done();
      }

      private List<ICDKMolecule> generate2dCoordinates(List<IMolecule> molecules,
                                                    IProgressMonitor monitor)
                  throws Exception {
          ICDKMolecule cdkmol = null;
          List<ICDKMolecule> newMolecules= new RecordableList<ICDKMolecule>();

          for(IMolecule molecule:molecules){
            if(monitor.isCanceled())
                return null;

            if (molecule instanceof ICDKMolecule) {
                cdkmol = (ICDKMolecule) molecule;
            }
            else {
                cdkmol = asCDKMolecule(molecule);
            }

            IMoleculeSet mols
                = ConnectivityChecker.partitionIntoMolecules(
                      cdkmol.getAtomContainer() );

            StructureDiagramGenerator sdg = new StructureDiagramGenerator();

            for ( IAtomContainer mol : mols.molecules() ) {
                sdg.setMolecule( cdkmol.getAtomContainer()
                     .getBuilder().newInstance(
                    	  org.openscience.cdk.interfaces.IMolecule.class, mol)
                );
                sdg.generateCoordinates();
                IAtomContainer molWithCoords = sdg.getMolecule();
                // copy the coordinates
                for (int i=0; i<molWithCoords.getAtomCount(); i++) {
                	mol.getAtom(i).setPoint2d(molWithCoords.getAtom(i).getPoint2d());
                }
            }

            newMolecules.add(cdkmol);
          }
          if(monitor.isCanceled())
              return null;
          else
              return newMolecules;
      }

      public void generate2dCoordinates(List<IMolecule> molecules,
                                        IReturner returner,
                                        IProgressMonitor monitor)
                       throws Exception {
          monitor.beginTask( "Creating 3d coordinates", IProgressMonitor.UNKNOWN );
          returner.completeReturn( generate2dCoordinates( molecules, monitor ) );
          monitor.done();
      }

      public void saveMol2(ICDKMolecule mol, String filename)
                  throws InvocationTargetException,
                         BioclipseException,
                         CoreException {
          saveMolecule(mol, filename, (IChemFormat)Mol2Format.getInstance());
      }

      public List<ICDKMolecule> loadSMILESFile( IFile file,
                                                IProgressMonitor monitor )
                                throws CoreException, IOException {

          //Only process files with smiles extension
          if ( !file.getFileExtension().equals(
                SMILESFormat.getInstance().getPreferredNameExtension() ) )
              return new RecordableList<ICDKMolecule>();
          return loadSMILESFile( file.getContents(), monitor );
      }

      /**
       * A simple implementation testing separator by splitting a line using a 
       * list of possible separators and returning the first one giving 
       * more than 1 parts.
       * 
       * @param line Line to split
       * @return a String separator, or null if none found
       */
      private static String determineSeparator(String line) {
          String[] POSSIBLE_SEPARATORS=new String[]{",","\t"," "};
          for (int i = 0; i< POSSIBLE_SEPARATORS.length; i++){
              String[] splits = line.split(POSSIBLE_SEPARATORS[i]);
              if (splits.length>1)
                  return POSSIBLE_SEPARATORS[i];
          }
          return null;
      }
      
      public List<ICDKMolecule> loadSMILESFile( InputStream contents,
                                                IProgressMonitor monitor )
                                throws CoreException, IOException {


          BufferedInputStream buf = new BufferedInputStream(contents);
          InputStreamReader reader = new InputStreamReader(buf);
          BufferedReader breader = new BufferedReader(reader);

          if ( !breader.ready() ) {
              throw new IOException("Input was not ready to be read.");
          }
          List<ICDKMolecule> molecules = new ArrayList<ICDKMolecule>();
          DeduceBondSystemTool bondSystemTool = new DeduceBondSystemTool();
          List<String> lines = new LinkedList<String>();
          for ( String line = breader.readLine() ; 
                line != null ; 
                line = breader.readLine() ) {
              lines.add( line );
          }
          breader.close();
          
          try {

              int noLines = lines.size();

              logger.debug("Number of lines in file: " + noLines);
              
              monitor.beginTask("Converting SMILES file to SDF", noLines);
              
              String firstLine = lines.remove( 0 );

              if (firstLine==null)
                  throw new IOException("First line is null!");
              
              logger.debug("Header line is: " + firstLine);

              //Determine separator from first line
              String separator=determineSeparator(firstLine);

              //First line is header
              String[] headers;
              if ( separator == null) {
                  // no separator so assuming only a SMILES string on each row
                  headers = new String[] {"smiles"};
              }
              else {
                   headers = firstLine.split(separator);
              }
              
              
              // or is it?
              boolean haveHeaders = false;
              try { 
                  fromSMILES( headers.length == 1 ? firstLine 
                                                  : headers[0] );
              }
              catch (BioclipseException e) {
                // well at least it's not SMILES so suppose it's headers
                haveHeaders = true;
              }
              
              if (!haveHeaders) {
                  lines.add( 0, firstLine );
                  if ( headers.length != 1 ) {
                      headers = new String[] {"smiles", "identifier"};
                  }
              }
              
              //Strip headers of " and spaces
              for (int i=0; i< headers.length; i++){
                  headers[i]=headers[i].trim();
                  if (headers[i].startsWith("\""))
                      headers[i] = headers[i].substring(1);
                  if (headers[i].endsWith("\""))
                      headers[i] = headers[i].substring( 
                                       0,
                                       headers[i].length() - 1 );
              }

              //Read subsequent lines until end
              int lineno=2;
              for (String line : lines) {
                  
                  if (monitor.isCanceled())
                      return null;
                  
                  String[] parts = headers.length > 1 ? line.split(separator) 
                                                      : new String[] {line};
                  
                  //Assert header is same size as data
                  if (parts.length!=headers.length)
                      throw new BioclipseException("Header and data have " +
                              "different number of columns. " +
                              "Header size=" + headers.length + 
                              "Line " + lineno + " size=" + parts.length );

                  //Part 1 is expected to be SMILES
                  String smiles=parts[0];

                  //Create a new CDKMolecule from smiles
                  ICDKMolecule mol = fromSMILES(smiles);
                  
                  try {
                      org.openscience.cdk.interfaces.IMolecule newAC 
                          = bondSystemTool.fixAromaticBondOrders(
                              (org.openscience.cdk.interfaces.IMolecule)
                              mol.getAtomContainer() );
                      mol = new CDKMolecule(newAC);
                  } 
                  catch (CDKException e) {
                      logger.error("Could not deduce bond orders for mol: " + mol);
                  }

                  //Store rest of parts as properties on mol
                  for (int i=1; i<headers.length;i++){
                      mol.getAtomContainer().setProperty(headers[i], parts[i]);
                  }
                  
                  //Filter molecules with failing atom types
                  boolean filterout=false;
                  for (IAtom atom : mol.getAtomContainer().atoms()){
                      if (atom.getAtomTypeName()==null || 
                              atom.getAtomTypeName().equals("X"))
                          filterout=true;
                  }

                  if (filterout)
                      logger.debug("Skipped molecule " + lineno + " due to " +
                              "failed atom typing.");
                  else
                      molecules.add(mol);

                  //Read next line
                  lineno++;
                  
                  monitor.worked(1);
                  if (lineno%100==0){
                      if (monitor.isCanceled())
                          return null;
                      monitor.subTask("Processed: " + lineno + "/" + noLines);
                  }
              }
          } catch (IOException e) {
              e.printStackTrace();
          } catch (BioclipseException e) {
              e.printStackTrace();
          }finally{
              monitor.done();
          }
          logger.debug("Read " + molecules.size() +" molecules.");
          return molecules;
      }

      public int getNoMolecules(IFile file)
          throws IOException, BioclipseException, CoreException {
          if (file.getFileExtension().equals("sdf") ) {
              return numberOfEntriesInSDF(file, new NullProgressMonitor());
          }
          List<ICDKMolecule> lst
              = loadMolecules(file, new NullProgressMonitor());
          if (lst!=null) return lst.size();
          return -1;
      }

      public void generate3dCoordinates(IMolecule molecule,
                                             IReturner returner,
                                             IProgressMonitor monitor)
                       throws BioclipseException {
          List<IMolecule> molecules = new RecordableList<IMolecule>();
          monitor.beginTask( "Creating 3d coordinates",
                             IProgressMonitor.UNKNOWN );
          molecules.add( asCDKMolecule(molecule) );
          returner.completeReturn(
                   generate3dCoordinates( molecules, monitor ).get( 0 ) );
          monitor.done();
      }

      public void generate3dCoordinates(List<IMolecule> molecules,
                                                   IReturner returner,
                                                   IProgressMonitor monitor)
                             throws BioclipseException {
          monitor.beginTask( "Creating 3d coordinates",
                             IProgressMonitor.UNKNOWN );
          returner.completeReturn( generate3dCoordinates( molecules,
                                                          monitor ) );
          monitor.done();
      }

      private List<ICDKMolecule> generate3dCoordinates(List<IMolecule> molecules,
                                                    IProgressMonitor monitor)
                             throws BioclipseException {

          ICDKMolecule cdkmol = null;
          List<ICDKMolecule> newMolecules=new RecordableList<ICDKMolecule>();
          for(int i=0;i<molecules.size();i++){
            if(monitor.isCanceled())
                return null;

            if ( molecules.get(i) instanceof ICDKMolecule ) {
                cdkmol = (ICDKMolecule) molecules.get(i);
            }else {
                cdkmol=asCDKMolecule(molecules.get(i));
            }

            ModelBuilder3D mb3d;
            try {
                mb3d = ModelBuilder3D.getInstance();
            } catch ( CDKException e ) {
                throw new BioclipseException(e.getMessage());
            }
            IMoleculeSet mols = ConnectivityChecker.partitionIntoMolecules(
                                    cdkmol.getAtomContainer() );

            org.openscience.cdk.interfaces.IMolecule newmolecule
                = cdkmol.getAtomContainer().getBuilder().newInstance(
                		org.openscience.cdk.interfaces.IMolecule.class
                	);

            for ( IAtomContainer mol : mols.molecules() ) {
                try{
                  org.openscience.cdk.interfaces.IMolecule ac
                      = mb3d.generate3DCoordinates(
                            (org.openscience.cdk.interfaces.IMolecule)mol, false);
                  newmolecule.add(ac);
                }catch(NoSuchAtomTypeException ex){
                    throw new BioclipseException(ex.getMessage()+", molecule number "+i, ex);
                } catch ( Exception e ) {
                    throw new BioclipseException(e.getMessage(), e);
                }
            }
            newMolecules.add( new CDKMolecule(newmolecule) );
          }
          if(monitor.isCanceled())
              return null;
          else
              return newMolecules;
      }


      public ICDKMolecule addExplicitHydrogens(IMolecule molecule)
                       throws Exception {

          addImplicitHydrogens(molecule);
          ICDKMolecule cdkmol=null;

          if ( molecule instanceof ICDKMolecule ) {
              cdkmol = (ICDKMolecule) molecule;
          }
          else {
              cdkmol=asCDKMolecule(molecule);
          }

          IAtomContainer ac = cdkmol.getAtomContainer();
          AtomContainerManipulator.convertImplicitToExplicitHydrogens(ac);
          return new CDKMolecule(ac);
      }

      public ICDKMolecule addImplicitHydrogens(IMolecule molecule)
                       throws BioclipseException, InvocationTargetException {

          ICDKMolecule cdkmol = null;
          if ( molecule instanceof ICDKMolecule ) {
              cdkmol = (ICDKMolecule) molecule;
          }
          else {
              cdkmol=asCDKMolecule(molecule);
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
              lst = loadMolecules(transform, new NullProgressMonitor());
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
          return GeometryTools.has2DCoordinates(asCDKMolecule(mol).getAtomContainer());
      }

      public boolean has3d(IMolecule mol) throws BioclipseException {
          return GeometryTools.has3DCoordinates(asCDKMolecule(mol).getAtomContainer());
      }

      public void saveCML(ICDKMolecule cml,  String filename)
                  throws InvocationTargetException,
                         BioclipseException,
                         CoreException {
          saveMolecule(cml, filename, (IChemFormat)CMLFormat.getInstance());
      }

      public void saveMDLMolfile(ICDKMolecule mol, String filename)
                  throws InvocationTargetException,
                         BioclipseException,
                         CoreException {
          saveMolecule(mol, filename, (IChemFormat)MDLV2000Format.getInstance());
      }

      private IChemFormat determineIChemFormatOfStream(InputStream fileContent)
      throws IOException {
    	  if (!fileContent.markSupported())
    		  fileContent = new BufferedInputStream(fileContent);
          return formatsFactory.guessFormat(fileContent);
      }

      public IChemFormat determineIChemFormatOfString(String fileContent)
          throws IOException {
          return formatsFactory.guessFormat(
              new StringReader(fileContent)
          );
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

      public void saveSDFile(final IFile file, List<? extends IMolecule> entries,
              IProgressMonitor monitor)
                  throws BioclipseException, InvocationTargetException {

          if ( file.exists() )
              throw new BioclipseException("File " + file.getName()
                                           + " already exists");

          if (monitor == null) {
              monitor = new NullProgressMonitor();
          }
          int ticks = entries.size();
          try {

              monitor.beginTask( "Writing file", ticks );

            final StringWriter writer = new StringWriter();
            SDFWriter mdlwriter = new SDFWriter(writer);
            for (IMolecule molecule : entries) {
            	ICDKMolecule cdkMol = asCDKMolecule(molecule);
            	mdlwriter.write(cdkMol.getAtomContainer());
            	monitor.worked(1);
            }
            mdlwriter.close();

            ResourcesPlugin.getWorkspace().run(
                new IWorkspaceRunnable() {
                    public void run(IProgressMonitor monitor)
                                throws CoreException {
                        file.create( new ByteArrayInputStream(
                                             writer.toString().getBytes() ),
                        false,
                        monitor );
                    }
                },
                file.getProject(),
                IResource.NONE,
                new NullProgressMonitor() );

              monitor.worked(ticks);
          }
          catch (Exception e) {
              e.printStackTrace();
              throw new BioclipseException(e.getMessage(), e);
          }
          finally {
              monitor.done();
          }
      }

      public List<ICDKMolecule> extractFromSDFile( IFile file,
                                                   String property,
                                                   Collection<String> value,
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
              molList = new RecordableList<ICDKMolecule>();

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

      public Map<Integer,String> createSDFPropertyMap( IFile file,
                                                       String property)
                                 throws CoreException, IOException {
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

      public List<ICDKMolecule> extractFromSDFile( IFile file, int startenty,
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
              List<ICDKMolecule> result=new RecordableList<ICDKMolecule>();
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

    public IMolecularFormula molecularFormulaObject(ICDKMolecule m) {
        IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(
            m.getAtomContainer()
        );

        int missingHCount = 0;
        for (IAtom atom : m.getAtomContainer().atoms()) {
            missingHCount += calculateMissingHydrogens( m.getAtomContainer(),
                                                        atom );
        }

        if (missingHCount > 0) {
            mf.addIsotope( m.getAtomContainer().getBuilder()
                           .newInstance(IIsotope.class, Elements.HYDROGEN),
                           missingHCount
            );
        }
        return mf;
    }

    public String molecularFormula( ICDKMolecule m ) {
        return MolecularFormulaManipulator.getString(molecularFormulaObject(m));
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
        IChemFormat format = null;
        if (contentTypeMap.containsKey(type.getId()))
        	format = contentTypeMap.get(type.getId());
        if (format == null) {
        	// ... then as prefix
        	for (String prefix : contentTypeMap.keySet()) {
        		if (type.getId().startsWith(prefix)) {
        			format = contentTypeMap.get(prefix);
        			return format;
        		}
        	}
        }
        return format;
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

    public List<ICDKMolecule> createMoleculeList() throws BioclipseException,
            InvocationTargetException {
        return new RecordableList<ICDKMolecule>();
    }

    public ICDKMolecule perceiveAromaticity( IMolecule mol ) throws BioclipseException {
        IAtomContainer todealwith;
        if(mol instanceof ICDKMolecule){
            todealwith = ((ICDKMolecule) mol).getAtomContainer();
        }else{
            todealwith = asCDKMolecule( mol ).getAtomContainer();
        }
        try{
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(todealwith);
            CDKHueckelAromaticityDetector.detectAromaticity(todealwith);
        }catch(CDKException ex){
            throw new BioclipseException("Problems perceiving aromaticity: "+ex.getMessage());
        }
        return new CDKMolecule( ((ICDKMolecule) mol).getAtomContainer() );
    }
    
	public List<ICDKMolecule> perceiveAromaticity(List<? extends IMolecule> mols) throws BioclipseException{
		
		List<ICDKMolecule> retmols=new ArrayList<ICDKMolecule>();
		for (IMolecule mol : mols)
			retmols.add(perceiveAromaticity(mol));
		return retmols;
	}


    public List<IAtomContainer> partition(IMolecule molecule)
            throws BioclipseException {
        IAtomContainer todealwith;
        if (molecule instanceof ICDKMolecule) {
            todealwith = ((ICDKMolecule) molecule).getAtomContainer();
        } else {
            todealwith = asCDKMolecule( molecule ).getAtomContainer();
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
            todealwith = asCDKMolecule( molecule ).getAtomContainer();
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
        BitSet f2 = asCDKMolecule(calculateFor).getFingerprint(
            net.bioclipse.core.domain.IMolecule.Property.USE_CALCULATED);
        return calculateTanimoto( reference, f2 );
    }

    public float calculateTanimoto( IMolecule calculateFor,
                                    IMolecule reference )
                                    throws BioclipseException {
        BitSet f1 = asCDKMolecule(reference).getFingerprint(
                net.bioclipse.core.domain.IMolecule.Property.USE_CALCULATED);
        BitSet f2 = asCDKMolecule(calculateFor).getFingerprint(
                net.bioclipse.core.domain.IMolecule.Property.USE_CALCULATED);
        return calculateTanimoto( f1, f2 );
    }

    public void calculateTanimoto( List<IMolecule> calculateFor,
                                   IMolecule reference,
                                   IReturner<List<Float>> returner,
                                   IProgressMonitor monitor)
                throws BioclipseException {
        List<Float> result = new ArrayList<Float>();
        BitSet refensetBitSet = asCDKMolecule(reference).getFingerprint(
                net.bioclipse.core.domain.IMolecule.Property.USE_CALCULATED);
        for(int i=0;i<calculateFor.size();i++ ){
            result.add(
                calculateTanimoto(calculateFor.get(i), refensetBitSet)
            );
        }
        returner.completeReturn( result );
    }

    public IFile calculateTanimoto(List<IMolecule> molecules,
            IFile file, IProgressMonitor monitor)
    throws BioclipseException {
        StringBuilder matrix = new StringBuilder();
        int molCount = molecules.size();
        for (int row=0; row<molCount; row++) {
            matrix.append(',')
                  .append(molecules.get(row).getResource().getName());
        }
        matrix.append('\n');
        for (int row=0; row<molCount; row++) {
            ICDKMolecule rowMol = asCDKMolecule(molecules.get(row));
            matrix.append(molecules.get(row).getResource().getName())
                  .append(',');
            BitSet reference = rowMol.getFingerprint(Property.USE_CALCULATED);
            for (int col=0; col<row; col++) {
                matrix.append(String.format("%.3f", 0.0));
                if (col<(molCount-1)) matrix.append(',');
            }
            matrix.append(String.format("%.3f", 1.0));
            if (row<(molCount-1)) matrix.append(',');
            for (int col=(row+1); col<molCount; col++) {
                ICDKMolecule colMol = asCDKMolecule(molecules.get(col));
                BitSet compare = colMol.getFingerprint(Property.USE_CALCULATED);
                matrix.append(String.format("%.3f",
                    calculateTanimoto(reference, compare)
                ));
                if (col<(molCount-1)) matrix.append(',');
            }
            matrix.append('\n');
        }
        try {
            if (file.exists()) {
                file.setContents(
                    new ByteArrayInputStream(matrix.toString().getBytes()),
                    IResource.FORCE, monitor
                );
            } else {
                file.create(
                    new ByteArrayInputStream(matrix.toString().getBytes()),
                    IResource.FORCE, monitor
                );
            }
        } catch (CoreException exception) {
            throw new BioclipseException("Exception while creating resource.",
                exception);
        }
        return file;
    }

    public IFile calculateRMSD(List<IMolecule> molecules,
            IFile file, IProgressMonitor monitor)
    throws BioclipseException {
        StringBuilder matrix = new StringBuilder();
        int molCount = molecules.size();
        for (int row=0; row<molCount; row++) {
            matrix.append(',')
                  .append(molecules.get(row).getResource().getName());
        }
        matrix.append( "\n" );
        for (int row=0; row<molCount; row++) {
            ICDKMolecule rowMol = asCDKMolecule(molecules.get(row));
            List<ICDKMolecule> alignedMols = kabsch(molecules, rowMol, monitor);
            matrix.append(molecules.get(row).getResource().getName())
                  .append(',');
            for (int col=0; col<molCount; col++) {
                ICDKMolecule colMol = asCDKMolecule(alignedMols.get(col));
                matrix.append(String.format("%.3f",
                    colMol.getProperty("MCSS-RMSD", Property.USE_CACHED)
                ));
                if (col<(molCount-1)) matrix.append(',');
            }
            matrix.append('\n');
        }
        try {
            if (file.exists()) {
                file.setContents(
                    new ByteArrayInputStream(matrix.toString().getBytes()),
                    IResource.FORCE, monitor
                );
            } else {
                file.create(
                    new ByteArrayInputStream(matrix.toString().getBytes()),
                    IResource.FORCE, monitor
                );
            }
        } catch (CoreException exception) {
            throw new BioclipseException("Exception while creating resource.",
                exception);
        }
        return file;
    }

    public String getMDLMolfileString(IMolecule molecule_in) throws BioclipseException {

        ICDKMolecule molecule = asCDKMolecule(molecule_in);

        StringWriter stringWriter = new StringWriter();
        MDLV2000Writer writer = new MDLV2000Writer(stringWriter);
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

    public ICDKMolecule removeExplicitHydrogens(ICDKMolecule molecule) {
        IAtomContainer container = molecule.getAtomContainer();
        for (int i=container.getAtomCount()-1; i>=0; i--) {
            IAtom atom = container.getAtom(i);
            if ("H".equals(atom.getSymbol())) {
                container.removeAtomAndConnectedElectronContainers(atom);
            }
        }
        return molecule;
    }

    public ICDKMolecule removeImplicitHydrogens(ICDKMolecule molecule) {
        IAtomContainer container = molecule.getAtomContainer();
        for (IAtom atom : container.atoms()) {
            atom.setImplicitHydrogenCount(0);
        }
        return molecule;
    }

    public boolean isConnected(IMolecule molecule) throws BioclipseException {
        List<IAtomContainer> containers = partition(molecule);
        return containers.size() == 1;
    }

    public ICDKMolecule clone(ICDKMolecule molecule) throws BioclipseException {
        try {
            return new CDKMolecule(
                (IAtomContainer)molecule.getAtomContainer().clone()
            );
        } catch ( CloneNotSupportedException exception ) {
            throw new BioclipseException(
                "Could not clone the CDK data model.",
                exception
            );
        }
    }

    public ICDKMolecule mcss(List<IMolecule> molecules,
            IProgressMonitor monitor)
        throws BioclipseException {
        if (molecules.size() < 2)
            throw new BioclipseException("List must contain at least two " +
                "molecules.");
        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Calculating MCSS...", molecules.size());
        ICDKMolecule firstMolecule = asCDKMolecule(molecules.get(0));
        IAtomContainer mcss = firstMolecule.getAtomContainer();
        int counter = 1;
        for (IMolecule mol : molecules) {
            ICDKMolecule followupMolecule = asCDKMolecule(mol);
            counter++;
            try {
                mcss = UniversalIsomorphismTester.getOverlaps(
                    mcss, followupMolecule.getAtomContainer()
                ).get(0);
                monitor.worked(1);
            } catch (CDKException exception) {
                throw new BioclipseException("Could not determine MCSS, " +
                    "because of molecule " + counter + ": " +
                    exception.getMessage());
            }
        }
        ICDKMolecule newMolecule = newMolecule();
        newMolecule.getAtomContainer().add(mcss);
        return newMolecule;
    }

    public List<ICDKMolecule> kabsch(List<IMolecule> molecules,
                                     IProgressMonitor monitor)
    throws BioclipseException {
        return kabsch(molecules, molecules.get(0), monitor);
    }

    public List<ICDKMolecule> kabsch(List<IMolecule> molecules,
                                     IMolecule molecule,
                                     IProgressMonitor monitor)
    throws BioclipseException {
        if (molecules.size() < 2)
            throw new BioclipseException("List must contain at least two " +
            "molecules.");
        if (monitor == null) monitor = new NullProgressMonitor();
        monitor.beginTask("Aligning Molecules...", molecules.size());

        List<ICDKMolecule> results = new RecordableList<ICDKMolecule>();
        monitor.subTask("Calculating MCSS...");
        ICDKMolecule mcss = mcss(molecules, monitor);
        if (mcss.getAtomContainer().getAtomCount() < 3)
            throw new BioclipseException(
                "The MCSS must have at least 3 atoms."
            );
        ICDKMolecule firstMolecule = asCDKMolecule(molecule);
        ICDKMolecule firstSubstructure =
            getSubstructures(firstMolecule, mcss).get(0);
        for (IMolecule mol : molecules) {
            ICDKMolecule secondMolecule = asCDKMolecule(mol);
            ICDKMolecule substructure = getSubstructures(
                secondMolecule, mcss
            ).get(0);
            try {
                KabschAlignment ka = new KabschAlignment(
                    firstSubstructure.getAtomContainer(),
                    substructure.getAtomContainer()
                );
                ka.align();
                IAtomContainer clone =
                    (IAtomContainer)secondMolecule.getAtomContainer().clone();
                ka.rotateAtomContainer(clone);
                monitor.worked(1);
                CDKMolecule result = new CDKMolecule(clone);
                result.setProperty("MCSS-RMSD", ka.getRMSD());
                results.add(result);
            } catch (CloneNotSupportedException exc) {
                throw new BioclipseException("Failed to clone the input", exc);
            } catch (CDKException exception) {
                // TODO Auto-generated catch block
                exception.printStackTrace();
            }
        }
        monitor.done();

        return results;
    }

    /**
     * Split a list of mols in N equally sized parts by random sampling.
     * 
     * @param mols
     * @param parts
     * @return
     */
    public List<List<IMolecule>> randomSplit(List<IMolecule> mols_in, int parts){

    	//Make a local copy of mols
    	ArrayList<IMolecule> mols = new ArrayList<IMolecule>(mols_in.size()); 
    	for (IMolecule mol : mols_in)
    		mols.add(mol);

		Random generator = new Random();
    	List<List<IMolecule>> retList= new ArrayList<List<IMolecule>>();
    	//Add parts # lists
    	for (int i=0; i< parts; i++){
    		retList.add(new ArrayList<IMolecule>());
    	}

    	//Draw one element of a time from list and put in partLists
    	int partIX=0;
    	int maxSize=mols.size();
    	for (int i=0; i< maxSize; i++){
    		int ix = generator.nextInt(mols.size());
    		List<IMolecule> r = retList.get(partIX);
    		r.add(mols.get(ix));
    		mols.remove(ix);

    		partIX++;
    		if (partIX % parts==0)
    			partIX=0; //Start over sequence
    		
    	}
    	
    	return retList;
    }
    
    /**
     * Split a list of mols in N equally sized parts by random sampling.
     * 
     * @param mols
     * @param parts
     * @return
     */
    public List<List<IMolecule>> randomSplit2parts(List<IMolecule> mols_in, double firstRatio){

    	//Make a local copy of mols
    	ArrayList<IMolecule> mols = new ArrayList<IMolecule>(mols_in.size()); 
    	for (IMolecule mol : mols_in)
    		mols.add(mol);
    	
		Random generator = new Random();
    	List<List<IMolecule>> retList= new ArrayList<List<IMolecule>>();
    	List<IMolecule> firstList = new ArrayList<IMolecule>();
		retList.add(firstList);
		retList.add(mols);
		
		//Sizes
		int firstLength = (int) (mols.size() * firstRatio);
		
		//Randomly draw firstLength from mols
		for (int i = 0; i < firstLength; i++){
    		int ix = generator.nextInt(mols.size());
    		firstList.add(mols.get(ix));
    		mols.remove(ix);
		}

    	return retList;
    }
    
        
}
