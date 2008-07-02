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
package net.bioclipse.cdk.business;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.domain.CDKConformer;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioList;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.ResourcesPlugin;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ConformerContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.FingerprinterTool;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.iterator.IteratingMDLConformerReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * The manager class for CDK. Contains CDK related methods.
 * 
 * @author olas, jonalv
 *
 */
public class CDKManager implements ICDKManager {

    private static final Logger logger = Logger.getLogger(CDKManager.class);
    
    ReaderFactory readerFactory;

    public String getNamespace() {
        return "cdk";
    }

    /**
     * Load a molecule from a file. If many molecules, just return first.
     * To return a list of molecules, use loadMolecules(...)
     */
    public ICDKMolecule loadMolecule(String path)
        throws IOException, BioclipseException {
        
        File file=new File(path);
        if (file.canRead()==false){
            throw new IllegalArgumentException(
                "Could not read file: " + file.getPath()
            );
        }
        FileInputStream stream;
        try {
            stream = new FileInputStream(file);
            return loadMolecule(stream);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(
                "Could not read file: " + file.getPath()
            );
        }
    }

    /**
     * Load a molecule from an InputStream. If many molecules, just return
     * first. To return list of molecules, use loadMolecules(...)
     */
    public ICDKMolecule loadMolecule(InputStream instream)
        throws IOException, BioclipseException {

        if (readerFactory==null){
            readerFactory=new ReaderFactory();
            CDKManagerHelper.registerFormats(readerFactory);
        }

        //Create the reader
        ISimpleChemObjectReader reader= readerFactory.createReader(instream);

        if (reader==null) {
            throw new BioclipseException("Could not create reader in CDK.");
        }

        IChemFile chemFile = new org.openscience.cdk.ChemFile();

        // Do some customizations...
        CDKManagerHelper.customizeReading(reader, chemFile);

        //Read file
        try {
            chemFile=(IChemFile)reader.read(chemFile);
        } catch (CDKException e) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace(logger, e);
        }

        //Store the chemFormat used for the reader
        IResourceFormat chemFormat=reader.getFormat();
        System.out.println("Rad CDK chemfile with format: "
                           + chemFormat.getFormatName());

        List<IAtomContainer> atomContainersList
            = ChemFileManipulator.getAllAtomContainers(chemFile);
        int nuMols=atomContainersList.size();
        System.out.println("This file contained: " + nuMols + " molecules");

        //If we have one AtomContainer, return a CDKMolecule with this ac
        //If we have more than one AtomContainer, return a list of the molecules
        //FIXME: requires common interface for CDKImplementations
        
        if (atomContainersList.size()==1){
            CDKMolecule retmol
                = new CDKMolecule((IAtomContainer)atomContainersList.get(0));
            return retmol;
        }
        
        List<ICDKMolecule> moleculesList = new ArrayList<ICDKMolecule>();

        for (int i=0; i<atomContainersList.size();i++){
            IAtomContainer ac=null;
            Object obj=atomContainersList.get(i);
            if (obj instanceof org.openscience.cdk.interfaces.IMolecule) {
                ac=(org.openscience.cdk.interfaces.IMolecule)obj;
            }else if (obj instanceof IAtomContainer) {
                ac=(IAtomContainer)obj;
            }

            CDKMolecule mol=new CDKMolecule(ac);
            String moleculeName="Molecule " + i; 
            if (ac instanceof IMolecule) {
                org.openscience.cdk.interfaces.IMolecule imol
                    = (org.openscience.cdk.interfaces.IMolecule) ac;
                String molName=(String) imol.getProperty(CDKConstants.TITLE);
                if (molName!=null && (!(molName.equals("")))){
                    moleculeName=molName;
                }
            }
            mol.setName(moleculeName);
            
            moleculesList.add(mol);
        }
        
        //Just return the first molecule. To return all, use loadMolecules(..)
        return moleculesList.get(0);
    }

    
    /**
     * Load a molecules from a file. 
     */
    public List<ICDKMolecule> loadMolecules(String path)
        throws IOException, BioclipseException {
        
        File file=new File(path);
        if (file.canRead()==false){
            throw new IllegalArgumentException(
                "Could not read file: " + file.getPath()
            );
        }
        FileInputStream stream;
        try {
            stream = new FileInputStream(file);
            return loadMolecules(stream);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(
                "Could not read file: " + file.getPath()
            );
        }
    }

    
    
    /**
     * Load one or more molecules from an InputStream and return a CDKMoleculeList.
     */
    public List<ICDKMolecule> loadMolecules(InputStream instream)
        throws IOException, BioclipseException {

        if (readerFactory==null){
            readerFactory=new ReaderFactory();
            CDKManagerHelper.registerFormats(readerFactory);
        }

        System.out.println("no formats supported: "
                           + readerFactory.getFormats().size());
//        System.out.println("format guess: " + readerFactory.guessFormat(instream).getFormatName());

        //Create the reader
        ISimpleChemObjectReader reader= readerFactory.createReader(instream);

        if (reader==null){
            throw new BioclipseException("Could not create reader in CDK. ");
        }

        IChemFile chemFile = new org.openscience.cdk.ChemFile();

        // Do some customizations...
        CDKManagerHelper.customizeReading(reader, chemFile);

        //Read file
        try {
            chemFile=(IChemFile)reader.read(chemFile);
        } catch (CDKException e) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace(logger, e);
        }

        //Store the chemFormat used for the reader
        IResourceFormat chemFormat=reader.getFormat();
        System.out.println("Rad CDK chemfile with format: " + chemFormat.getFormatName());

        List<IAtomContainer> atomContainersList
            = ChemFileManipulator.getAllAtomContainers(chemFile);
        int nuMols=atomContainersList.size();
        System.out.println("This file contained: " + nuMols + " molecules");

        List<ICDKMolecule> moleculesList = new BioList<ICDKMolecule>();
//        CDKMolecule[] moleculesData = new CDKMolecule[atomContainersList.size()];

        for (int i=0; i<atomContainersList.size();i++){
            IAtomContainer ac=null;
            Object obj=atomContainersList.get(i);
            if (obj instanceof org.openscience.cdk.interfaces.IMolecule) {
                ac=(org.openscience.cdk.interfaces.IMolecule)obj;
            }else if (obj instanceof IAtomContainer) {
                ac=(IAtomContainer)obj;
            }

            CDKMolecule mol=new CDKMolecule(ac);
            String moleculeName="Molecule " + i; 
            if (ac instanceof IMolecule) {
                org.openscience.cdk.interfaces.IMolecule imol
                    = (org.openscience.cdk.interfaces.IMolecule) ac;
                String molName=(String) imol.getProperty(CDKConstants.TITLE);
                if (molName!=null && (!(molName.equals("")))){
                    moleculeName=molName;
                }
            }
            mol.setName(moleculeName);
            
            moleculesList.add(mol);
        }
        
        return moleculesList;
    }

    public String calculateSmiles(IMolecule molecule)
        throws BioclipseException {
        
        return molecule.getSmiles();
    }

    public void saveMolecule(CDKMolecule seq) throws IllegalStateException {
        // TODO Auto-generated method stub

    }

    /**
     * Create molecule from SMILES.
     * @throws BioclipseException 
     */
    public ICDKMolecule fromSmiles(String smilesDescription)
        throws BioclipseException {

        SmilesParser parser
            = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        try {
            org.openscience.cdk.interfaces.IMolecule mol
                = parser.parseSmiles(smilesDescription);
            return new CDKMolecule(mol);
        } catch (InvalidSmilesException e) {
            throw new BioclipseException("SMILES string is invalid");
        }
        
    }

    /**
     * Create molecule from String
     * @throws BioclipseException 
     * @throws BioclipseException 
     * @throws IOException 
     */
    public ICDKMolecule fromString( String molstring ) throws BioclipseException, IOException {

        if (molstring==null) throw new BioclipseException("Input cannot be null");

        ByteArrayInputStream bais=new ByteArrayInputStream(molstring.getBytes());
        
        return loadMolecule( bais );
        
    }

    
    
    
    public Iterator<ICDKMolecule> creatMoleculeIterator(InputStream instream) {
        return new IteratingBioclipseMDLReader(
            instream,
            NoNotificationChemObjectBuilder.getInstance()
        );
    }

    class IteratingBioclipseMDLReader implements Iterator<ICDKMolecule> {

        IteratingMDLReader reader;
        
        public IteratingBioclipseMDLReader(InputStream input,
                                           IChemObjectBuilder builder) {
            
            reader = new IteratingMDLReader(input, builder);
        }

        public boolean hasNext() {
            return reader.hasNext();
        }

        public ICDKMolecule next() {
            org.openscience.cdk.interfaces.IMolecule cdkMol
                = (org.openscience.cdk.interfaces.IMolecule)reader.next();
            
            ICDKMolecule bioclipseMol = new CDKMolecule(cdkMol);
            return bioclipseMol;
        }

        public void remove() {
            reader.remove();
        }
    }
    
    public Iterator<ICDKMolecule> creatConformerIterator(InputStream instream) {
        return new IteratingBioclipseMDLConformerReader(
            instream,
            NoNotificationChemObjectBuilder.getInstance()
        );
    }


    class IteratingBioclipseMDLConformerReader implements Iterator<ICDKMolecule> {

        IteratingMDLConformerReader reader;
        
        public IteratingBioclipseMDLConformerReader(InputStream input,
                                           IChemObjectBuilder builder) {
            
            reader = new IteratingMDLConformerReader(input, builder);
        }

        public boolean hasNext() {
            return reader.hasNext();
        }

        public ICDKMolecule next() {
            ConformerContainer cdkMol
                = (ConformerContainer)reader.next();
            
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

        return FingerprinterTool.isSubset( molecule.getFingerprint( true ),
                                           molecule.getFingerprint( true ) );
    }

    public boolean subStructureMatches( ICDKMolecule molecule,
                                        ICDKMolecule subStructure ) {
        try {
            return UniversalIsomorphismTester
                   .isSubgraph( molecule.getAtomContainer(), 
                                subStructure.getAtomContainer() );
        } 
        catch ( CDKException e ) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create an ICDKMolecule from an IMolecule.
     * First tries to create ICDKMolecule from CML. If that fails, tries to
     * create from SMILES.
     */
    public ICDKMolecule create( IMolecule m ) throws BioclipseException {
        //First try to create from CML
        try {
            String cmlString=m.getCML();
            if (cmlString!=null){
                return fromString( cmlString );
            }
        } catch ( IOException e ) {
            logger.debug( "Could not create mol from CML" );
        } catch ( UnsupportedOperationException e ) {
            logger.debug( "Could not create mol from CML" );
        }
        
        //Secondly, try to create from SMILES
        return fromSmiles( m.getSmiles() );
    }

    public boolean smartsMatches( ICDKMolecule molecule, String smarts ) 
                   throws BioclipseException {

        SMARTSQueryTool querytool;
        try {
            querytool = new SMARTSQueryTool(smarts);
        } catch ( CDKException e ) {
            throw new BioclipseException("Could not parse SMARTS query", e);
        }
        try {
            return querytool.matches( molecule.getAtomContainer() );
        } catch ( CDKException e ) {
            throw new BioclipseException( "A problem occured trying to " +
            		                      "match SMARTS query");
        }
    }

    public int numberOfEntriesInSDF( String filePath ) {
        int num = 0;
        try {
            FileInputStream counterStream = new FileInputStream(filePath);
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
                            }
                        }
                    }
                }
            }
            counterStream.close();
        } catch (Exception exception) {
            // ok, I give up...
            logger.debug("Could not determine the number of molecules to read, because: " +
                         exception.getMessage(), exception
            );
        }
        return 0;
    }

    /**
     * Reads files and extracts conformers if available. 
     * Currently limited to read SDFiles, CMLFiles is for the future.
     * @param path the full path to the file
     * @return a list of molecules that may have multiple conformers
     */
    public List<ICDKMolecule> loadConformers( String path ) {
        
        File file=new File(path);
        if (file.canRead()==false){
            throw new IllegalArgumentException(
                "Could not read file: " + file.getPath()
            );
        }
        FileInputStream stream;
        try {
            stream = new FileInputStream(file);
            return loadConformers(stream);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(
                "Could not read file: " + file.getPath()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                           "Problem parsing conformer file. " + e.getMessage()
            );
        }
        
    }

    /**
     * Reads files and extracts conformers if available. 
     * Currently limited to read SDFiles, CMLFiles is for the future.
     * @param path the full path to the file
     * @return a list of molecules that may have multiple conformers
     * @throws BioclipseException 
     */
    public List<ICDKMolecule> loadConformers( InputStream stream ) {
        

        Iterator<ICDKMolecule> it=creatConformerIterator( stream );
        
        List<ICDKMolecule> mols=new ArrayList<ICDKMolecule>();
        while ( it.hasNext() ) {
            ICDKMolecule molecule = (ICDKMolecule) it.next();
            String moleculeName="Molecule X";
//            String molName=(String) molecule.getAtomContainer().getProperty(CDKConstants.TITLE);
//            if (molName!=null && (!(molName.equals("")))){
//                moleculeName=molName;
//            }
//            molecule.setName(moleculeName);
            mols.add( molecule );
        }
        
        if (mols==null || mols.size()<=0)
            throw new IllegalArgumentException("No conformers could be read");

        return mols;

    }
}
