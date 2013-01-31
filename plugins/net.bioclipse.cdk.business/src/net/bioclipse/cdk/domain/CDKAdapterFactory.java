/* *****************************************************************************
 * Copyright (c) 2008-2009 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.bioclipse.cdk.domain;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;

@SuppressWarnings("unchecked")
public class CDKAdapterFactory implements IAdapterFactory {

    Logger logger = Logger.getLogger( CDKAdapterFactory.class );
    
    public Object getAdapter( Object adaptableObject, 
                              Class adapterType ) {

        Object molecule = null;
        if ( adaptableObject instanceof IFile ) {
            IFile file = (IFile) adaptableObject;
            if ( adapterType.equals( ICDKMolecule.class ) ) {
                if ( molecule == null ) {
                    try {
                        molecule = Activator.getDefault()
                                            .getJavaCDKManager()
                                            .loadMolecule( file );
                    } catch ( Exception e ) {
                        logger.debug( LogUtils.traceStringOf( e ));
                        return null;
                    }
                }
            }
        }
        if(molecule !=null &&adapterType.isAssignableFrom( molecule.getClass()))
            return molecule;

        if(adapterType.isAssignableFrom(ImageDescriptor.class)) {
                return Activator.getImageDescriptor( "icons/benzene.gif" );
        }

        return null;
    }

    public Class[] getAdapterList() {

        return new Class[] { ICDKMolecule.class,ImageDescriptor.class };
    }
}
