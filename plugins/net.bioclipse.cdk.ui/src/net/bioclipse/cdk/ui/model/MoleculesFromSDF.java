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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.bioclipse.cdk.domain.Node;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.core.BioclipseStore;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IElementCollector;


public class MoleculesFromSDF implements IMoleculesFromFile{

    private List<SDFElement> children = Collections.synchronizedList(
                                           new ArrayList<SDFElement>() );
    private IFile sdfFile;
    private Logger logger = Logger.getLogger( this.getClass() );

    public MoleculesFromSDF( IFile sdfFile ) {
        this.sdfFile = sdfFile;
    }

    public void fetchDeferredChildren( Object object,
                                       IElementCollector collector,
                                       IProgressMonitor monitor ) {

        int ticks = IProgressMonitor.UNKNOWN;
        try {
            long fileSize = EFS.getStore( sdfFile.getLocationURI() )
                               .fetchInfo()
                               .getLength();
            if( fileSize > 1048576) { // if larger than 1MB
                monitor.done();
                return;
            }
            if ( fileSize < Integer.MAX_VALUE ) {
                ticks = (int)fileSize;
                logger.debug( "ticks: " + ticks );
            }
        }
        catch ( CoreException e ) {
            LogUtils.debugTrace( logger, e );
        }
        monitor.beginTask("Reading SDF file", ticks);
        Node first;
        first = new Node(null);
        BuilderThread builder = new BuilderThread(sdfFile,
                                                  first,
                                                  monitor);
        builder.start();

        readSDFElementsFromList( first, collector, monitor );
        monitor.done();

    }

    private void readSDFElementsFromList( Node first,
                                          IElementCollector collector,
                                          IProgressMonitor monitor ) {
        Node node = first;
        long lastPos = 0;
        while ( (node = node.next()) != null ) {
            collector.add( node.data(), monitor);
            children.add( node.data() );
            long currentPos = node.data().getPosition();
            monitor.worked( (int) (currentPos - lastPos) );
//            logger.debug( "currentpos: " + currentPos );
            lastPos = currentPos;
            if (monitor.isCanceled())
                throw new OperationCanceledException();
        }

    }

    public ISchedulingRule getRule( Object object ) {

        // TODO Auto-generated method stub
        return null;
    }

    public boolean isContainer() {
        return true;
    }

    public Object[] getChildren( Object o ) {
        return children.toArray();
    }

    public ImageDescriptor getImageDescriptor( Object object ) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLabel( Object o ) {
        return "Molecules";
    }

    public Object getParent( Object o ) {
        return sdfFile;
    }

    /* (non-Javadoc)
     * @see net.bioclipse.cdk.ui.views.IMoleculesEditorModel#getMoleculeAt(int)
     */
    public Object getMoleculeAt( int index ) {
        if(children.size() <=index) {
            logger.debug( "index out of bounds Index: "
                                          +index + ", Size: "+children.size() );
          return null;
        } else
            return children.get(index );
    }

    /* (non-Javadoc)
     * @see net.bioclipse.cdk.ui.views.IMoleculesEditorModel#getNumberOfMolecules()
     */
    public int getNumberOfMolecules() {

        return children.size();
    }

    /* (non-Javadoc)
     * @see net.bioclipse.cdk.ui.views.IMoleculesEditorModel#save()
     */
    public void save() {
        throw new UnsupportedOperationException(this.getClass().getName()+
                                        " does not support this operation yet");
        // TODO Auto-generated method stub

    }
}
