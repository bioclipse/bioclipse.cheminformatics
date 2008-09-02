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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;


public class MoleculesFromSDF implements IDeferredWorkbenchAdapter {

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

        monitor.beginTask("Reading SDF file", IProgressMonitor.UNKNOWN);
        Node first = (Node) sdfFile.getAdapter( Node.class );
        if ( first == null ) {
            first = new Node(null);
            BioclipseStore.put( first, sdfFile, Node.class );
            //monitor only used for checking when to abort. Nothing else.
            BuilderThread builder = new BuilderThread(sdfFile, 
                                                      first, 
                                                      monitor);
            builder.start();
        }
        readSDFElementsFromList( first, collector, monitor );
        monitor.done();
    }
    
    private void readSDFElementsFromList( Node first,
                                          IElementCollector collector,
                                          IProgressMonitor monitor ) {
        Node node = first;
        while((node = node.next())!=null ) {
            collector.add( node.data(), monitor);
            children.add( node.data() );
            monitor.worked( 1 );
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
}
