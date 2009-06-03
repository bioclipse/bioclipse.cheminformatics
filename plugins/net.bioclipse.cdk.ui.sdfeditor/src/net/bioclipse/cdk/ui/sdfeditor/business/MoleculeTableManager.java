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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.editor.SDFIndexEditorModel;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.managers.business.IBioclipseManager;

import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
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
        logger.info( "Dummy on molTable manager has been called with"
                     +" thees argumenst "+strings.toString() );
    }

    public SDFileIndex createSDFIndex(IFile file, IProgressMonitor monitor) {

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
        List<Long> propPos = new LinkedList<Long>();
        Map<Integer,List<Long>> propMap = new HashMap<Integer, List<Long>>();
        int num = 0;
        long pos = 0;
        long start = 0;
        int work = 0;

        try {
            ReadableByteChannel fc = Channels.newChannel( file.getContents() );
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

    public void calculateProperty( SDFIndexEditorModel model,
                                   IPropertyCalculator<?> calculator,
                                   IProgressMonitor monitor) {
        monitor.beginTask( "Calculating properties",
                           model.getNumberOfMolecules() );
        for(int i=0;i<model.getNumberOfMolecules();i++) {
            model.setPropertyFor( i, calculator.getPropertyName(),
                             calculator.calculate( model.getMoleculeAt( i ) ) );
            monitor.worked( 1 );
            if(i%100 == 0) {
                monitor.subTask( String.format( "%d/%d", i+1
                                             ,model.getNumberOfMolecules() ) );
            }
        }
        monitor.done();
    }

    private ByteArrayInputStream convertToByteArrayIs(StringWriter writer)
                                           throws UnsupportedEncodingException {
        return new ByteArrayInputStream( writer.toString()
                                         .getBytes("US-ASCII"));
    }

    public String saveSDF( IMoleculesEditorModel model, IFile file,
                           IProgressMonitor monitor) throws BioclipseException {
        monitor.beginTask( "Saving to file", model.getNumberOfMolecules() );
        IFile target = null;
        for(int i =0 ;i<model.getNumberOfMolecules();i++) {
            StringWriter writer = new StringWriter();
            IChemObjectWriter chemWriter = new SDFWriter(writer);
            // TODO Piped streams
            try {

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
                // get calculated properties form extension
                for(IPropertyCalculator<?> property:
                    SDFIndexEditorModel.retriveCalculatorContributions()) {
                    String name = property.getPropertyName();
                    Object value = molecule
                    .getProperty( name,Property.USE_CACHED );
                    if(value != null) {
                        String text = property.toString(value );
                        mol.setProperty( name, text );
                    }
                }
            chemWriter.write( mol );
            chemWriter.close();
            if(target==null) {
                target = file;
                if(file.exists()) {
                    target.setContents( convertToByteArrayIs( writer ),
                                                                  false,
                                                                  true,
                                                                  monitor );
                }else {
                    target.create( convertToByteArrayIs( writer ),
                                                           false,
                                                           monitor );
                }
            }else {
                target.appendContents( convertToByteArrayIs( writer ),
                                                                false,
                                                                true,
                                                                monitor);
            }
            }catch(Exception e) {
                LogUtils.debugTrace( logger, e );
                throw new BioclipseException("Faild to save file: "+
                                             e.getMessage());
            }
        }
        monitor.done();
        return file.getLocation().toPortableString();
    }

    private List<String> getProperties( InputStream is,
                                        long start,
                                        int numberOfProperties)
                                        throws IOException{
        List<String> properties = new ArrayList<String>(numberOfProperties);
        InputStream in = new BufferedInputStream(is);

        in.skip( start-1 );
        int read;
        int newLineCount = 0;
        StringBuilder builder = new StringBuilder();
        while(properties.size()< numberOfProperties
                && (read = in.read())!=-1) {
            if(read=='\r') continue;
            if(read =='\n') {
                newLineCount++;
                if(newLineCount>=2) {
                    builder.deleteCharAt( builder.length()-1 );
                    properties.add( builder.toString() );
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

    public void parseProperties(SDFIndexEditorModel model,IProgressMonitor monitor) {
        Pattern pNamePattern = Pattern.compile( "^>.*<(.*)>*.\n");
        try {
            monitor.beginTask( "Parsing properties", model.getNumberOfMolecules() );
        for(int i=0;i<model.getNumberOfMolecules();i++) {
            if(model.getPropertyCountFor( i )==0) {
                continue;
            }

            List<String> rawProperties = getProperties(
                                     ((IFile)model.getResource()).getContents(),
                                     model.getPropertyPositionFor( i ),
                                     model.getPropertyCountFor( i ) );
            for(String rawProperty:rawProperties) {
                String name = null;
                // extract property name


                Matcher matcher = pNamePattern.matcher( rawProperty );
                if(matcher.find() && matcher.groupCount()>0) {
                    name = matcher.group( 1 );
                    String value=rawProperty.substring( matcher.end( 0 ));
                    IPropertyCalculator<?> calculator = model.getCalculator( name );
                    if(calculator !=null) {
                        model.setPropertyFor( i, name, calculator.parse(value));
                    }
                }
            }
            monitor.worked( 1 );
        }
        }catch(IOException e) {
            logger.debug( "Failed to read properties" );
            throw new RuntimeException(e);

        }catch(CoreException e) {
            logger.debug( "Failed to read properties" );
            throw new RuntimeException(e);
        }
    }
}