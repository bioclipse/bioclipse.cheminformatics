/* *****************************************************************************
 *Copyright (c) 2013 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 ******************************************************************************/
package net.bioclipse.cdk.domain;

import java.io.IOException;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.CMLMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;

/**
*
* @author Klas Jšnsson (klas.joensson@gmail.com)
*
*/
public class CMLAdapterFactory implements IAdapterFactory {

    Logger logger = Logger.getLogger( CMLAdapterFactory.class );
    
    @Override
    public Object getAdapter( Object adaptableObject, Class adapterType ) {

        Object molecule = null;
        if ( adaptableObject instanceof CMLMolecule) {
            CMLMolecule mol = (CMLMolecule) adaptableObject;
            if ( adapterType.equals( CMLMolecule.class ) ) {
                try {
                    molecule = Activator.getDefault()
                            .getJavaCDKManager().fromCml( mol.toCML() );
                } catch ( BioclipseException e ) {
                    logger.debug( LogUtils.traceStringOf( e ));
                    return null;
                } catch ( IOException e ) {
                    logger.debug( LogUtils.traceStringOf( e ));
                    return null;
                }
            }
        }
        if(molecule !=null &&adapterType.isAssignableFrom( molecule.getClass()))
            return molecule;
        
        return null;
    }

    @Override
    public Class[] getAdapterList() {
        return new Class[] { CMLMolecule.class };
    }

}
