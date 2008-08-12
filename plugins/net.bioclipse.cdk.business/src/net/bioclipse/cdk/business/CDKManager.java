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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import net.bioclipse.cdk.domain.CDKConformer;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioList;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ConformerContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.FingerprinterTool;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.CDKSourceCodeWriter;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLRXNWriter;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.SMILESWriter;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.iterator.IteratingMDLConformerReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * The manager class for CDK. Contains CDK related methods.
 * 
 * @author olas, jonalv
 *
 */
public class CDKManager implements ICDKManager {
	
    private static final Logger logger 
        = Logger.getLogger(CDKManager.class);
    
    ReaderFactory readerFactory;

    public String getNamespace() {
        return "cdk";
    }

    /*
     * Load a molecule from a file. If many molecules, just return first.
     * To return a list of molecules, use loadMolecules(...)
     */
    public ICDKMolecule loadMolecule(String path)
                        throws IOException, 
                               BioclipseException, 
                               CoreException {
        
        return loadMolecule( ResourcePathTransformer.getInstance()
                                                    .transform( path ),
                             null );
    }

    private ICDKMolecule loadMolecule( InputStream instream, 
                                       IProgressMonitor monitor )
        throws BioclipseException, IOException {

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        List<ICDKMolecule> moleculesList = new ArrayList<ICDKMolecule>();
        try {
            
            if (readerFactory==null){
                readerFactory=new ReaderFactory();
                CDKManagerHelper.registerFormats(readerFactory);
            }
    
            //Create the reader
            ISimpleChemObjectReader reader 
                = readerFactory.createReader(instream);
    
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
            
        }
        finally {
           monitor.done(); 
        }
        //Just return the first molecule. To return all, use loadMolecules(..)
        return moleculesList.get(0);
    }

    
    /**
     * Load a molecules from a file. 
     * @throws CoreException 
     */
    public List<ICDKMolecule> loadMolecules(String path)
        throws IOException, BioclipseException, CoreException {
        
        return loadMolecules( 
            ResourcePathTransformer.getInstance().transform( path ),
            null );
    }
    
    /**
     * Load one or more molecules from an InputStream and return a CDKMoleculeList.
     * @throws CoreException 
     */
    public List<ICDKMolecule> loadMolecules( IFile file, 
                                             IProgressMonitor monitor )
        throws IOException, BioclipseException, CoreException {

        if ( monitor == null ) {
            monitor = new NullProgressMonitor();
        }
        
        List<ICDKMolecule> moleculesList = new BioList<ICDKMolecule>();
        int ticks = 10000;
        try {
            monitor.beginTask( "Reading file", ticks );
            if ( readerFactory == null ) { 
                readerFactory = new ReaderFactory();
                CDKManagerHelper.registerFormats(readerFactory);
            }
    
            System.out.println("no formats supported: "
                               + readerFactory.getFormats().size());
    //        System.out.println("format guess: " + readerFactory.guessFormat(instream).getFormatName());
    
            //Create the reader
            ISimpleChemObjectReader reader = 
                readerFactory.createReader( file.getContents() );
    
            if ( reader == null ) {
                throw new BioclipseException (
                    "Could not create reader in CDK.");
            }
    
            IChemFile chemFile = new org.openscience.cdk.ChemFile();
    
            // Do some customizations...
            CDKManagerHelper.customizeReading(reader, chemFile);
    
            //Read file
            try {
                chemFile=(IChemFile)reader.read(chemFile);
            } 
            catch (CDKException e) {
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
            System.out.println("This file contained: " 
                               + nuMols + " molecules");
            
    //        CDKMolecule[] moleculesData = new CDKMolecule[atomContainersList.size()];
    
            for (int i=0; i<atomContainersList.size();i++){
                IAtomContainer ac=null;
                Object obj=atomContainersList.get(i);
                if (obj instanceof 
                        org.openscience.cdk.interfaces.IMolecule) {
                    ac=(org.openscience.cdk.interfaces.IMolecule)obj;
                }
                else if (obj instanceof IAtomContainer) {
                    ac=(IAtomContainer)obj;
                }
    
                CDKMolecule mol=new CDKMolecule(ac);
                String moleculeName="Molecule " + i; 
                if (ac instanceof IMolecule) {
                    org.openscience.cdk.interfaces.IMolecule imol
                        = (org.openscience.cdk.interfaces.IMolecule) ac;
                    String molName
                        = (String) imol.getProperty(CDKConstants.TITLE);
                    if (molName!=null && (!(molName.equals("")))){
                        moleculeName=molName;
                    }
                }
                mol.setName(moleculeName);
                
                moleculesList.add(mol);
                monitor.worked( (int) 
                                (ticks/nuMols) );
            }
        }
        finally {
            monitor.done();
        }
        return moleculesList;
    }

    public String calculateSmiles(IMolecule molecule)
        throws BioclipseException {
        
        return molecule.getSmiles();
    }
    
    public void save(IChemModel model, IFile target, String filetype) 
                throws BioclipseException, CDKException, CoreException {
        IProgressMonitor monitor = new NullProgressMonitor();
    	try{
	        int ticks = 10000;
	        monitor.beginTask( "Writing file", ticks );
	    	String towrite;
	    	if(filetype.equals(mol)){
	            StringWriter writer = new StringWriter();
	            MDLWriter mdlWriter = new MDLWriter(writer);
	            mdlWriter.write(model);
	            towrite=writer.toString();
	    	} else if(filetype.equals(cml)){
	    		StringWriter writer = new StringWriter();
	            CMLWriter cmlWriter = new CMLWriter(writer);
	            cmlWriter.write(model);
	            towrite=writer.toString();
	    	} else if(filetype.equals(rxn)){
	    		StringWriter writer = new StringWriter();
	            MDLRXNWriter cmlWriter = new MDLRXNWriter(writer);
	            cmlWriter.write(model);
	            towrite=writer.toString();
	    	} else if(filetype.equals(smi)){
	    		StringWriter writer = new StringWriter();
	            SMILESWriter cmlWriter = new SMILESWriter(writer);
	            cmlWriter.write(model);
	            towrite=writer.toString();
	    	} else if(filetype.equals(cdk)){
	    		StringWriter writer = new StringWriter();
	            CDKSourceCodeWriter cmlWriter = new CDKSourceCodeWriter(writer);
	            cmlWriter.write(model);
	            towrite=writer.toString();
	    	} else {
	    		throw new BioclipseException("Filetype "+filetype+" not supported!");
	    	}
	    	if(target.exists()){
	        	 target.setContents(new StringBufferInputStream(towrite), false, true, monitor);
	    	} else {
		    	target.create(new StringBufferInputStream(towrite), false, monitor);
	    	}
	        monitor.worked(ticks);
		}
		finally {
			monitor.done();
		}
    }

	public void saveMolecule(ICDKMolecule mol, IFile target, String filetype)
			throws BioclipseException, CDKException, CoreException {
		IChemModel chemModel=mol.getAtomContainer().getBuilder().newChemModel();
		chemModel.setMoleculeSet(chemModel.getBuilder().newMoleculeSet());
		chemModel.getMoleculeSet().addAtomContainer(mol.getAtomContainer());
		this.save(chemModel, target, filetype);
	}
	
	
	/**
     * Create molecule from SMILES.
     * @throws BioclipseException 
     */
    public ICDKMolecule fromSmiles(String smilesDescription)
                        throws BioclipseException {

        SmilesParser parser
            = new SmilesParser( DefaultChemObjectBuilder.getInstance() );
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
     * @throws IOException 
     */
    public ICDKMolecule fromCml( String molstring ) 
                        throws BioclipseException, IOException {

        if (molstring==null) 
            throw new BioclipseException("Input cannot be null");
        
        ByteArrayInputStream bais 
            = new ByteArrayInputStream( molstring.getBytes() );
        
        return loadMolecule( bais, null );
    }
    
    public Iterator<ICDKMolecule> createMoleculeIterator( 
        IFile file, IProgressMonitor monitor ) 
                                  throws CoreException {
        return new IteratingBioclipseMDLReader( 
            file.getContents(),
            NoNotificationChemObjectBuilder.getInstance(),
            monitor
        );
    }

    class IteratingBioclipseMDLReader implements Iterator<ICDKMolecule> {

        IteratingMDLReader reader;
        IProgressMonitor monitor = new NullProgressMonitor();
        
        public IteratingBioclipseMDLReader(InputStream input,
                                           IChemObjectBuilder builder, 
                                           IProgressMonitor monitor) {
            
            reader = new IteratingMDLReader(input, builder);
            if ( monitor != null ) {
                this.monitor = monitor; 
            }
            this.monitor.beginTask( "", IProgressMonitor.UNKNOWN );
        }

        public boolean hasNext() {
            boolean hasNext = reader.hasNext();
            if ( !hasNext ) {
               monitor.done();
            }
            return hasNext;
        }

        public ICDKMolecule next() {
            org.openscience.cdk.interfaces.IMolecule cdkMol
                = (org.openscience.cdk.interfaces.IMolecule)reader.next();
            
            ICDKMolecule bioclipseMol = new CDKMolecule(cdkMol);
            monitor.worked( 1 );
            return bioclipseMol;
        }

        public void remove() {
            reader.remove();
        }
    }
    
    public Iterator<ICDKMolecule> createConformerIterator( String path ) {
        return creatConformerIterator( 
            ResourcePathTransformer.getInstance().transform( path ), 
            null );
    }
    
    public Iterator<ICDKMolecule> 
        creatConformerIterator(IFile file, IProgressMonitor monitor) {
        
        try {
            return new IteratingBioclipseMDLConformerReader(
                file.getContents(),
                NoNotificationChemObjectBuilder.getInstance(),
                monitor
            );
        } catch ( CoreException e ) {
            throw new IllegalArgumentException(e);
        }
    }

    class IteratingBioclipseMDLConformerReader 
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
            monitor.beginTask( "Reading File", 
                               IProgressMonitor.UNKNOWN );
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
                return fromCml( cmlString );
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
            throw new BioclipseException("Could not parse SMARTS query", 
                                         e);
        }
        try {
            return querytool.matches( molecule.getAtomContainer() );
        } catch ( CDKException e ) {
            throw new BioclipseException( "A problem occured trying " +
                                          "to match SMARTS query");
        }
    }

    public int numberOfEntriesInSDF( IFile file,
                                     IProgressMonitor monitor ) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        int num = 0;
        try {
            InputStream counterStream = EFS.getStore( 
                file.getLocationURI() )
                    .openInputStream( EFS.NONE, monitor );
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
        return num;
    }
       
    public int numberOfEntriesInSDF( String filePath ) {
        return numberOfEntriesInSDF( ResourcePathTransformer
                                         .getInstance()
                                         .transform( filePath ),
                                     null );
    }

    /**
     * Reads files and extracts conformers if available. 
     * Currently limited to read SDFiles, CMLFiles is for the future.
     * @param path the full path to the file
     * @return a list of molecules that may have multiple conformers
     */
    public List<ICDKMolecule> loadConformers( String path ) {
        
        return loadConformers( ResourcePathTransformer.getInstance()
                                               .transform( path ), 
                               null );
    }

    /**
     * Reads files and extracts conformers if available. 
     * Currently limited to read SDFiles, CMLFiles is for the future.
     */
    public List<ICDKMolecule> loadConformers( IFile file, 
                                              IProgressMonitor monitor ){

        if(monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask( "Reading file", IProgressMonitor.UNKNOWN );
        Iterator<ICDKMolecule> it 
            = creatConformerIterator( file, 
                                      new SubProgressMonitor( monitor, 
                                                              100 ) );
        
        List<ICDKMolecule> mols = new ArrayList<ICDKMolecule>();
        while ( it.hasNext() ) {
            ICDKMolecule molecule = (ICDKMolecule) it.next();
            String moleculeName = "Molecule X";
//            String molName=(String) molecule.getAtomContainer().getProperty(CDKConstants.TITLE);
//            if (molName!=null && (!(molName.equals("")))){
//                moleculeName=molName;
//            }
//            molecule.setName(moleculeName);
            mols.add( molecule );
        }
        
        if ( mols==null || mols.size()<=0 )
            throw new IllegalArgumentException(
                "No conformers could be read" );

        return mols;
    }

    public double calculateMass( IMolecule molecule ) 
                  throws BioclipseException {

        ICDKMolecule cdkmol=null;
        if ( molecule instanceof ICDKMolecule ) {
            cdkmol = (ICDKMolecule) molecule;
        }else {
            cdkmol=create(molecule);
        }
        
        double d=AtomContainerManipulator.getNaturalExactMass(
            cdkmol.getAtomContainer() );
        return d;
    }
    
    public IMolecule generate2dCoordinates(IMolecule molecule) throws Exception{
        ICDKMolecule cdkmol=null;
        if ( molecule instanceof ICDKMolecule ) {
            cdkmol = (ICDKMolecule) molecule;
        }else {
            cdkmol=create(molecule);
        }        
    	StructureDiagramGenerator sdg = new StructureDiagramGenerator();
    	sdg.setMolecule(cdkmol.getAtomContainer().getBuilder().newMolecule(cdkmol.getAtomContainer()));
    	sdg.generateCoordinates();
    	IAtomContainer ac=sdg.getMolecule();
    	Iterator<IAtom> it=ac.atoms();
    	while(it.hasNext()){
    		it.next().setPoint3d(null);
    	}
    	return new CDKMolecule(ac);
    }

    public ICDKMolecule loadMolecule( IFile file, 
                                      IProgressMonitor monitor ) 
                        throws IOException,
                               BioclipseException,
                               CoreException {
        return loadMolecule( file.getContents(), monitor );
    }

    public Iterator<ICDKMolecule> createMoleculeIterator( String path )
                                  throws CoreException {
        
        return createMoleculeIterator( 
            ResourcePathTransformer.getInstance().transform( path ),
            null );
    }

    public Iterator<ICDKMolecule> creatConformerIterator( IFile file ) {
        return creatConformerIterator( file, null );
    }

    public Iterator<ICDKMolecule> createMoleculeIterator( IFile file )
                                  throws CoreException {
        return createMoleculeIterator( file, null );
    }

    public List<ICDKMolecule> loadConformers( IFile file ) {
        return loadConformers( file, null );
    }

    public ICDKMolecule loadMolecule( IFile file ) throws IOException,
                                                  BioclipseException,
                                                  CoreException {
        return loadMolecule( file, null );
    }

    public List<ICDKMolecule> loadMolecules( IFile file ) 
                              throws IOException,
                                     BioclipseException,
                                     CoreException {
        return loadMolecules( file, null );
    }

    public int numberOfEntriesInSDF( IFile file ) {
        return numberOfEntriesInSDF( file, null );
    }
}
