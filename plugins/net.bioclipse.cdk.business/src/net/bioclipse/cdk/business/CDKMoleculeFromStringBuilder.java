/*******************************************************************************
 * Copyright (c) 2009  Jonathan Alvarsson <jonalv@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.business;

import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.ui.business.IBioObjectFromStringBuilder;

import org.apache.log4j.Logger;


/**
 * @author jonalv
 *
 */
public class CDKMoleculeFromStringBuilder 
       implements IBioObjectFromStringBuilder {

    private static final Logger logger 
        = Logger.getLogger( CDKMoleculeFromStringBuilder.class );
    
    public IBioObject fromString( String s ) {
        IBioObject b = null;
        try {
            b = Activator.getDefault().getJavaCDKManager().fromString( s );
        }
        catch ( Exception e ) {
            logger.debug( "CDKMoleculeFromStringBuilder could not " +
            		      "recognize the string: \n " + s );
            throw new IllegalArgumentException(
                "Failed while parsing the content of the given " +
                "string into a BioObject", e );
        }
        if ( b == null ) {
            logger.debug( "CDKMoleculeFromStringBuilder could not " +
                          "recognize the string: \n " + s );
            throw new IllegalArgumentException(
                "Failed while parsing the content of the given" + 
                "string into a BioObject" );
        }
        return b;
    }

    public boolean recognize( String s ) {
        IBioObject b = null;
        try {
            b = Activator.getDefault().getJavaCDKManager().fromString( s );
        }
        catch ( Exception e ) {
            return false;
        }
        return b != null;
    }
}
