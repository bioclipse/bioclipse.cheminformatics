/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Jonathan Alvarsson
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.model;

import java.io.IOException;
import java.io.InputStream;

import net.bioclipse.cdk.domain.Node;
import net.bioclipse.cdk.domain.SDFElement;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


public class BuilderThread extends Thread {

    private Logger logger = Logger.getLogger( this.getClass() );

    private Node node;
    private IFile file;
    IProgressMonitor monitor;
    
    
    /**
     * @param sdfFile
     * @param first
     * @param monitor only used for checking if to abort. 
     * It will not be updated by this class.
     */
    public BuilderThread( IFile sdfFile, 
                          Node first, 
                          IProgressMonitor monitor) {
        this.node    = first;
        this.file    = sdfFile;
        this.monitor = monitor;
    }
    
    @Override
    public void run() {
   
        InputStream input = null;
        try {
            input = file.getContents();
        } 
        catch ( CoreException e ) {
            logger.error( "Could not open file", e );
        }
        
        try {
            int moleculeNumber = 1;
            int c = 0;
            long position = -1;
            int dollars = 0;
            boolean readingName = false;
            int newlinesFoundWhileReadingName = 0;
            
            StringBuffer name = new StringBuffer();
            long moleculeStartsAt = 0;
            boolean readingFirstName = true;
            
            while ( c != -1 ) {
                if ( monitor.isCanceled() ) {
                    return;
                }
                c = input.read();
                position++;
                if ( c == '$' ) {
                    dollars++;
                }
                else {
                    dollars = 0;
                }
                if ( dollars == 4 ) {
                    readingName = true;
                    moleculeStartsAt = position;
                }
                if ( readingFirstName ) {
                    if (c == '\n') {
                        newlinesFoundWhileReadingName++;
                    }
                    if ( newlinesFoundWhileReadingName == 1 ) {
                        node.link(node= new Node( 
                            new SDFElement( file, 
                                            name.toString(), 
                                            0,
                                            moleculeNumber++ ) ));
                        
                        readingFirstName = false;
                        newlinesFoundWhileReadingName = 0;
                        name = new StringBuffer();
                    }
                    else {
                        if ( c != '\n' ) {
                            name.append( (char)c );
                        }
                    }
                }
                if (readingName) {
                    if (c == '\n') {
                        newlinesFoundWhileReadingName++;
                    }
                    if ( newlinesFoundWhileReadingName == 2 ) {
                        node.link(node= new Node( 
                            new SDFElement( file, 
                                            //remove $ from name
                                            name.substring(1), 
                                            moleculeStartsAt,
                                            moleculeNumber++ ) ));
                       
                        readingName = false;
                        newlinesFoundWhileReadingName = 0;
                        name = new StringBuffer();
                    }
                    else {
                        if ( c != '\n' ) {
                            name.append( (char)c );
                        }
                    }
                }
            }
            node.link( null );
        } 
        catch ( IOException e ) {
            logger.error( "Could not read from file", e );
        }
        finally {
            
            try {
                input.close();
            } 
            catch ( Exception e ) {
                logger.error( "Could not close file", e );
            }
            //note that we reset the list reference 
            //to null without returning null
        }
    }
}
