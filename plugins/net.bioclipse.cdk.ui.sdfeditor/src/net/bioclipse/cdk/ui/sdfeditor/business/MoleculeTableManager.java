/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bioclipse.cdk.domain.CDKMoleculeUtils;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IFileMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;

import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.SDFWriter;


public class MoleculeTableManager implements IBioclipseManager {

    Logger logger = Logger.getLogger( MoleculeTableManager.class );

    public String getManagerName() {
        return "molTable";
    }

    public void dummy(String... strings) {
        StringBuilder string = new StringBuilder();
        string.append( "Dummy on molTable manager has been called with" );
        string.append( " thees argumenst " );
        for(String s:strings) {
            string.append( s );
            string.append( ", " );
        }
        string.delete( string.length()-1, string.length() );
        logger.info( string.toString() );
    }

    public void createSDFIndex( IFile file,
                                IReturner<SDFIndexEditorModel> returner,
                                IProgressMonitor monitor ) {

        returner.completeReturn(
                  new SDFIndexEditorModel(createIndex( file, monitor ) ) );

    }

    //TODO refactor out file.getContent()
    private SDFileIndex createIndex( IFile file,
                                     IProgressMonitor monitor) {
        if(file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist ");
        }
        SubMonitor progress = SubMonitor.convert( monitor );
        long size = -1;
        try {
            size = EFS.getStore( file.getLocationURI() )
            .fetchInfo().getLength();
            progress.beginTask( "Parsing SDFile",
                                (int) (size/1048576));

        }catch (CoreException e) {
            logger.debug( "Failed to get size of file" );
            progress.beginTask("Parsing SDFile", IProgressMonitor.UNKNOWN );
        }

        long tStart = System.nanoTime();
        List<Long> values = new LinkedList<Long>();
        List<Long> propPos = new LinkedList<Long>();
        Map<Integer,List<Long>> propMap = new HashMap<Integer, List<Long>>();
        int num = 0;
        long pos = 0;
        long start = 0;
        int work = 0;

        long workBits = 0;

        try {
            InputStream is;
                IFileStore store = EFS.getStore( file.getLocationURI() );
                is = store.openInputStream( EFS.NONE, progress.newChild( 10 ) );
            ReadableByteChannel fc = Channels.newChannel( is );
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect( 200 );
            int dollarCount = 0;
            boolean firstInLine = true;
            int bytesRead = 0;
            int c;
            while( bytesRead >=0) {
                byteBuffer.rewind();
                bytesRead = fc.read( byteBuffer );
                byteBuffer.rewind();
                for(;bytesRead >0;bytesRead--) {
                    c = byteBuffer.get();
                    pos++;

                    if( c == '\n') {
                        firstInLine = true;
                        if(dollarCount==4) {
                            work = (int) start;
                            start = pos;

                            propMap.put(num,propPos);
                            propPos= new  LinkedList<Long>();

                            num++;
                            values.add( start );
                            dollarCount = 0;
                            // progress code
                            if(workBits/1048576 >0) {
                                progress.worked( (int) (workBits/1048576) );
                                progress.setWorkRemaining( (int) ((size-pos)/1048576) );
                                workBits =0;
                            }
                            workBits += pos-work;
                            if(size >-1) {
                                progress.subTask(
                                   String.format( "Read: %dMB\\%dMB",
                                   pos/(1048576),size/(1048576)));
                            }else {
                                progress.subTask(
                                   String.format( "Read: %dMB",
                                   pos/(1048576)));
                            }
                            if ( progress.isCanceled() ) {
                                throw new OperationCanceledException();
                            }
                            // clear builder for the next line
                        }
                    }else if(c == '\r') continue;
                    else if(c == '$') dollarCount++;
                    else if(c== '>' && firstInLine) {
                        propPos.add( pos );
                        firstInLine = false;
                    }else {
                        firstInLine=false;
                        dollarCount = 0;
                    }
                }
            }

            if( (pos-start)>3) {
                values.add(pos);
                num++;
            }
            fc.close();
        }catch (OperationCanceledException e) {
            throw new OperationCanceledException(e.getMessage());
        }
        catch (Exception exception) {
            // ok, I give up...
            logger.debug( "Could not create index: "
                          + exception.getClass().getSimpleName() + " : "
                          + exception.getMessage(),
                          exception );
        }
        logger.debug( String.format(
                          "createSDFIndex took %d to complete",
                          (int)((System.nanoTime()-tStart)/1e6)) );
        progress.done();
       return new SDFileIndex(file,values,propMap);
    }

    public void calculateProperty( IMoleculesEditorModel model,
                                   IPropertyCalculator<?> calculator,
                                   IProgressMonitor monitor) {
        calculateProperty( model, new IPropertyCalculator<?>[] {calculator},
                           monitor );
    }

    public void calculateProperty( IMoleculesEditorModel model,
                                   IPropertyCalculator<?>[] calculators,
                                   IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert( monitor,1000 );
        progress.beginTask( "Calculating properties",
                           model.getNumberOfMolecules()*1000);
        for(int i=0;i<model.getNumberOfMolecules();i++) {
            progress.subTask( String.format( "%d/%d", i+1
                                             ,model.getNumberOfMolecules() ) );
            SubMonitor calculateProgress = progress.newChild( 1000);
            calculateProgress.beginTask( "property", calculators.length*100 );
            int prop=1;
            for(IPropertyCalculator<?> calculator:calculators) {
                calculateProgress.subTask( String.format( "%d/%d", prop++,
                                                          calculators.length) );
                model.setPropertyFor( i, calculator.getPropertyName(),
                             calculator.calculate( model.getMoleculeAt( i ) ) );
                calculateProgress.worked( 100 );
                if(progress.isCanceled())
                    throw new OperationCanceledException();
            }
            calculateProgress.done();
        }
        progress.done();
    }

    public void calculateProperties( ICDKMolecule molecule,
                                     IPropertyCalculator<?> [] calculators,
                                     IReturner<Void> returner,
                                     IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert( monitor );
        progress.beginTask( "Calculating properties for "+ molecule.getName(),
                            calculators.length*1000 );
        for(IPropertyCalculator<?> calculator: calculators) {
            CDKMoleculeUtils.setProperty( molecule,
                                          calculator.getPropertyName(),
                                          calculator.calculate(molecule) );
            progress.worked( 1000 );
            if(progress.isCanceled())
                throw new OperationCanceledException();
        }
        progress.done();
        returner.completeReturn( null );
    }

    private ByteArrayInputStream convertToByteArrayIs(StringWriter writer)
                                           throws UnsupportedEncodingException {
        return new ByteArrayInputStream( writer.toString()
                                         .getBytes("US-ASCII"));
    }

    public void saveSDF( IMoleculesEditorModel model, IFile file,
                         IReturner<IFileMoleculesEditorModel> returner,
                         IProgressMonitor monitor)
                                            throws BioclipseException {
            SDFIndexEditorModel index = saveSDF( model, file, monitor );
            returner.completeReturn( index);
    }

    public void saveSDF( IFileMoleculesEditorModel model,
                         IReturner<IFileMoleculesEditorModel> returner,
                         IProgressMonitor monitor) throws BioclipseException {
        SDFIndexEditorModel index = saveSDF(model,model.getResource(),monitor);
        returner.completeReturn( index );
    }

    private SDFIndexEditorModel saveSDF(IMoleculesEditorModel model, IFile file,
                                       IProgressMonitor monitor)
                                        throws BioclipseException {

        SubMonitor subMonitor = SubMonitor.convert( monitor );

        subMonitor.beginTask( "Saving to file", 1000 );

        IPath filePath = file.getFullPath();
        IPath renamePath = null;
        // If we are overwriting a.k.a save to the same file as we are reading
        if(file.exists()) {
            renamePath = filePath;
            filePath = filePath.addFileExtension( "tmp" );

        }

        IFile target = null;
        Collection<Object> availableProperties = null;
            availableProperties = model.getAvailableProperties();

        SubMonitor loopProgress = subMonitor.newChild( 1000 );
        loopProgress.setWorkRemaining( 1000*model.getNumberOfMolecules() );
        loopProgress.subTask( "Writing to file" );
         for(int i =0 ;i<model.getNumberOfMolecules();i++) {
            StringWriter writer = new StringWriter();
            IChemObjectWriter chemWriter = new SDFWriter(writer);
            // TODO Piped streams
            try {
                SubMonitor firstPart = loopProgress.newChild( 200 );
                ICDKMolecule molecule = model.getMoleculeAt( i );
                // copy properties
                IAtomContainer ac = molecule.getAtomContainer();
                IMolecule mol = null;
                if(ac instanceof IMolecule)
                    mol = (IMolecule) ac;
                else {
                    mol = new Molecule( ac );
                    //Properties are lost in this CDK operation, so copy them
                    mol.setProperties( ac.getProperties() );
                }
                if(availableProperties!=null) {
                    Set<Object> acProps = new HashSet<Object>(ac.getProperties()
                                                              .keySet());
                    acProps.removeAll( availableProperties);
                    for(Object o:acProps) {
                       mol.removeProperty( o );
                    }
                }
                // Add properties form model/molecule to cdk IMolecule
                for(IPropertyCalculator<?> property:
                    SDFIndexEditorModel.retriveCalculatorContributions()) {
                    String name = property.getPropertyName();
                    Object value = molecule
                    .getProperty( name,Property.USE_CACHED );
                    if( value != null && (availableProperties == null
                        || availableProperties.contains( name ))) {
                        String text = property.toString(value );
                        mol.setProperty( name, text );
                    }
                }
                firstPart.worked( 100 );
            chemWriter.write( mol );
            chemWriter.close();
            firstPart.worked( 100 );
            // If  it is the first time we need to create or write over the file
            if(target==null) {
                target = file.getWorkspace().getRoot().getFile( filePath );
                if(target.exists()) {
                    target.setContents( convertToByteArrayIs( writer ),
                                                                  false,
                                                                  true,
                                                 loopProgress.newChild( 500 ) );
                }else {
                    target.create( convertToByteArrayIs( writer ),
                                                           false,
                                                 loopProgress.newChild( 500 ) );
                }
            }else {
                target.appendContents( convertToByteArrayIs( writer ),
                                                                false,
                                                                true,
                                                 loopProgress.newChild( 500 ) );
            }
            }catch(Exception e) {
                LogUtils.debugTrace( logger, e );
                throw new BioclipseException("Faild to save file: "+
                                             e.getMessage());
            }
            if ( loopProgress.isCanceled() ) {
                throw new OperationCanceledException();
            }
        }
         subMonitor.setWorkRemaining( 1000 );
         SDFIndexEditorModel sdfModel = null;
         // Rename temp file to real thing and recreate index
         try {
             if(renamePath != null) {
                 subMonitor.subTask( "Renaming file" );
                 file.setHidden(true);
                 file.delete( true, subMonitor.newChild( 1000 ) );
                 target.move( renamePath, true, subMonitor.newChild( 1000 ) );
             }
             subMonitor.setWorkRemaining( 1000 );
                  SDFileIndex index=
                 createIndex( file, subMonitor.newChild( 1000 ) );
                  if(index!=null) {
                      sdfModel = new SDFIndexEditorModel(index);
                  }else
                     throw new BioclipseException("Failed to create new index");
         } catch ( CoreException e1 ) {
             logger.warn( "Could not rename original" );
             throw new BioclipseException( "Failed to create new index: "
                                           + e1.getMessage());
         }
        return sdfModel;
    }

    //TODO take a SDFileIndex and index instead of start and numberOfProperties
    private List<String> getProperties( InputStream is,
                                        long start,
                                        int numberOfProperties)
                                        throws IOException{
        List<String> properties = new ArrayList<String>(numberOfProperties);
        InputStream in = new BufferedInputStream(is);

        long skip = in.skip( start-1 );
        if(skip != start-1)
            throw new IOException("Failed to skip to properties");
        int read;
        int newLineCount = 0;
        StringBuilder builder = new StringBuilder();
        while(properties.size()< numberOfProperties
                && (read = in.read())!=-1) {
            if(read=='\r') continue;
            if(read =='\n') {
                newLineCount++;
                if(newLineCount>=2) {
                    if(builder.length()>0) {
                        builder.deleteCharAt( builder.length()-1 );
                        properties.add( builder.toString() );
                    }else
                        logger.warn( "Empty line in file near position "+start );
                    builder = new StringBuilder();
                    continue;
                }
            }else
                newLineCount = 0;
            builder.append( (char)read );
        }
        in.close();

        return properties;
    }

    public void parseProperties( SDFIndexEditorModel model,
                                 Collection<String> propertyKeys,
                                 IReturner<Void> returner,
                                 IProgressMonitor monitor) {
        if(model.getResource() == null || !model.getResource().exists()) {
            throw new IllegalArgumentException("Model must have a resource. "+
                        model.getResource().getLocationURI()+" does not exist ");
        }
        Pattern pNamePattern = Pattern.compile( "^>.*<([^>]+)>.*\n" );
        try {
            monitor.beginTask( "Parsing properties", model.getNumberOfMolecules() );
        for(int i=0;i<model.getNumberOfMolecules();i++) {
            if(model.getPropertyCountFor( i )==0) {
                continue;
            }

            List<String> rawProperties = getProperties(
                                     model.getResource().getContents(),
                                     model.getPropertyPositionFor( i ),
                                     model.getPropertyCountFor( i ) );
            for(String rawProperty:rawProperties) {
                String name = null;
                // extract property name


                Matcher matcher = pNamePattern.matcher( rawProperty );
                if(matcher.find() && matcher.groupCount()>0) {
                    name = matcher.group( 1 );
                    model.addPropertyKey(name);
                    String value=rawProperty.substring( matcher.end( 0 ));
                    IPropertyCalculator<?> calculator = model.getCalculator( name );
                    if(calculator !=null) {
                        model.setPropertyFor( i, name, calculator.parse(value));
                    }else {
                        if(propertyKeys.contains( name ))
                            model.setPropertyFor( i, name, value );
                    }
                }
            }
            monitor.worked( 1 );
            if ( monitor.isCanceled() ) {
                throw new OperationCanceledException();
            }
        }
        }catch(IOException e) {
            logger.debug( "Failed to read properties" );
            throw new RuntimeException(e);

        }catch(CoreException e) {
            logger.debug( "Failed to read properties" );
            throw new RuntimeException(e);
        }
    }

    public void calculatePropertiesFor( IFile file,
                                         IPropertyCalculator<?>[] calculators,
                                         IProgressMonitor monitor)
                                            throws BioclipseException{
        SubMonitor progress = SubMonitor.convert( monitor, 10000 );

        IPath filePath = file.getFullPath();
        IPath renamePath = null;
        // If we are overwriting a.k.a save to the same file as we are reading
        if(file.exists()) {
            renamePath = filePath;
            filePath = filePath.addFileExtension( "tmp" );

        }

        SDFileIndex index = createIndex( file, progress.newChild( 1000 ) );
        SDFIndexEditorModel model = new SDFIndexEditorModel(index);

        IFile target = null;
        SubMonitor loopProgress = progress.newChild( 8000 );
        loopProgress.setWorkRemaining( model.getNumberOfMolecules()*20 );
        loopProgress.subTask( "Writing to file" );
        for(int i = 0;i<model.getNumberOfMolecules();i++ ) {
            //loopProgress.setWorkRemaining( (model.getNumberOfMolecules()-i) );
            StringWriter writer = new StringWriter();
            IChemObjectWriter chemWriter = new SDFWriter(writer);
            try {
            //SubMonitor firstPart = loopProgress.newChild( 300 );
            ICDKMolecule molecule = model.getMoleculeAt( i );
            // copy properties
            IAtomContainer ac = molecule.getAtomContainer();
            IMolecule mol = null;
            if(ac instanceof IMolecule)
                mol = (IMolecule) ac;
            else {
                mol = new Molecule( ac );
                //Properties are lost in this CDK operation, so copy them
                mol.setProperties( ac.getProperties() );
            }
            loopProgress.worked( 5 );
            calculateProperties( molecule, calculators, new IReturner<Void>() {
                public void completeReturn( Void object ) {
                }
                public void partialReturn( Void object ) {
                }
            }, loopProgress.newChild( 5 ) );
            for(IPropertyCalculator<?> property:
                calculators) {
                String name = property.getPropertyName();
                Object value = molecule
                .getProperty( name,Property.USE_CACHED );
                if(value != null ) {
                    String text = property.toString(value );
                    mol.setProperty( name, text );
                }
            }

            chemWriter.write( mol );
            chemWriter.close();
            loopProgress.worked( 5 );
            if(loopProgress.isCanceled())
                throw new OperationCanceledException();
            // If  it is the first time we need to create or write over the file
            if(target==null) {
                target = file.getWorkspace().getRoot().getFile( filePath );
                if(target.exists()) {
                    target.setContents( convertToByteArrayIs( writer ),
                                                                  false,
                                                                  true,
                                                 loopProgress.newChild( 5 ) );
                }else {
                    target.create( convertToByteArrayIs( writer ),
                                                           false,
                                                 loopProgress.newChild( 4 ) );
                    target.setHidden( true );
                    target.getParent().refreshLocal( IResource.DEPTH_INFINITE ,
                                                   loopProgress.newChild( 1));
                }
            }else {
                target.appendContents( convertToByteArrayIs( writer ),
                                                                false,
                                                                true,
                                                 loopProgress.newChild( 5 ) );
            }
            }catch(OperationCanceledException e) {
                try {
                    target.delete( true, progress.newChild( 1000 ) );
                    target.refreshLocal( IResource.DEPTH_ZERO,
                                                     progress.newChild( 1000 ));
                } catch ( CoreException e1 ) {
                    logger.debug( "Failed to clean up after cancelation" );
                }
                throw new OperationCanceledException(e.getMessage());
            }
            catch(Exception e) {
                LogUtils.debugTrace( logger, e );
                throw new BioclipseException("Faild to save file. "+
                           e.getMessage()!=null?e.getMessage():"");
            }
            loopProgress.subTask( String.format( "%d/%d", i,
                                               model.getNumberOfMolecules() ) );
        }
        progress.setWorkRemaining( 1000 );

        try {
            if(renamePath != null) {
                progress.subTask( "Renaming file" );
                file.delete( true, progress.newChild( 500 ) );
                target.setHidden( false );
                target.move( renamePath, true, progress.newChild( 500 ) );
            }
        } catch ( CoreException e1 ) {
            logger.warn( "Could not rename original" );
            throw new BioclipseException( "Failed to create new index: "
                                          + e1.getMessage());
        }
        progress.done();
    }

    public IPropertyCalculator<?> getCalculator(String id) {
        Collection<IPropertyCalculator<?>> calculators =
            SDFIndexEditorModel.retriveCalculatorContributions();
        for(IPropertyCalculator<?> calculator:calculators) {
            if(calculator.getPropertyName().endsWith( id ))
                return calculator;
        }
        throw new IllegalArgumentException("Id "+id+" dose not exist");
    }

    public void saveAsCSV( IMoleculesEditorModel model,IFile file, IProgressMonitor monitor) throws BioclipseException, IOException, CoreException {
        SubMonitor submon = SubMonitor.convert( monitor );
        submon.beginTask( "Writing CSV-file", model.getNumberOfMolecules()*100+200 );
        String del = ", ";
        List<Object> avaiableProperties;
        avaiableProperties = new ArrayList<Object>(model.getAvailableProperties());

        StringBuilder header = new StringBuilder();
        header.append( "SMILES" ).append( del );
        for(Object o: avaiableProperties) {
            header.append( o.toString() ).append( del );
        }
        header.append( "\n" );
        if(submon.isCanceled()) throw new OperationCanceledException();
        ByteArrayInputStream input = new ByteArrayInputStream( header.toString().getBytes() );
        file.create(input, true, submon.newChild( 200 ));

        submon.setWorkRemaining( model.getNumberOfMolecules()*100 );
        for(int i=0;i<model.getNumberOfMolecules();i++) {
            SubMonitor childMon = submon.newChild( 100 );
            StringBuilder sb = new StringBuilder();
            ICDKMolecule molecule = model.getMoleculeAt( i );
            sb.append( "\"" ).append( molecule.toSMILES() ).append( "\"" );
            sb.append( del );
            SubMonitor childeMon2 = childMon.newChild( 70);
            childeMon2.setWorkRemaining( avaiableProperties.size() );
            for(Object key: avaiableProperties) {
                Object o = molecule.getProperty( key.toString(), Property.USE_CACHED );
                if(o != null) {
                    String value = o instanceof String?(String)o:o.toString();
                    value = encodeCSV( value );
                    sb.append( value );
                }
                sb.append( del );
                if(childeMon2.isCanceled()) throw new OperationCanceledException();
                childeMon2.worked( 1 );
            }
            sb.append( "\n" );
            ByteArrayInputStream bais = new ByteArrayInputStream( sb.toString().getBytes() );
            file.appendContents( bais, IFile.FORCE, childMon );
        }
    }

    String encodeCSV(String value) {
        if(value.length()==0) return "\"\"";
        if(!value.matches( "[^,^\n]*" )) {
            return "\""+value+"\"";
        }
        if(value.contains( "\"" )) {
            if(value.matches( "\".+\"" )) value = value.substring( 1, value.length()-1 );
            return "\""+value.replaceAll( "\"", "\"\"" )+"\"";
        }
        if(value.startsWith( " " ) || value.endsWith( " " ) ) {
            return "\"" +value+"\"";
        }
        return value;
    }
}