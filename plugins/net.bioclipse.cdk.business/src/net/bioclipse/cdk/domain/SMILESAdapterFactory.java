/* *****************************************************************************
 *Copyright (c) 2013 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 ******************************************************************************/
package net.bioclipse.cdk.domain;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.SMILESMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 *
 * @author Klas Jšnsson (klas.joensson@gmail.com)
 *
 */
public class SMILESAdapterFactory implements IAdapterFactory {

    Logger logger = Logger.getLogger( SMILESAdapterFactory.class );
    
    @Override
    public Object getAdapter( Object adaptableObject, Class adapterType ) {
        
        Object molecule = null;
        if ( adaptableObject instanceof SMILESMolecule ) {
            SMILESMolecule mol = (SMILESMolecule) adaptableObject;
            if (adapterType.equals( ICDKMolecule.class )) {
                try {
                    molecule = Activator.getDefault()
                            .getJavaCDKManager().fromSMILES( mol.toSMILES() );
                } catch ( BioclipseException e ) {
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
        return new Class[] { ICDKMolecule.class };
    }

}
