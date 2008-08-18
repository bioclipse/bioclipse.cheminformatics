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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;


public class MoleculesFromSDF implements IDeferredWorkbenchAdapter {

    private ICDKManager cdk = Activator.getDefault().getCDKManager();
    private List<Object> children = new ArrayList<Object>();
    private IFile sdfFile;
    private Logger logger = Logger.getLogger( this.getClass() );
    private List<SDFElement> allElements;
    
    public MoleculesFromSDF( IFile sdfFile ) {
        this.sdfFile = sdfFile;
    }
    
    public void fetchDeferredChildren( Object object,
                                       IElementCollector collector,
                                       IProgressMonitor monitor ) {

        monitor.beginTask("Reading SDF file", IProgressMonitor.UNKNOWN);
               
        cdk.collectSDFElements( sdfFile, collector, monitor );
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
