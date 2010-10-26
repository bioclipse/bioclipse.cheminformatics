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
package net.bioclipse.cdk.ui;

import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.core.api.domain.IBioObject;
import net.bioclipse.ui.business.IBioObjectFromStringBuilder;

import org.apache.log4j.Logger;


/**
 * @author jonalv
 *
 */
public class CDKBioObjectFromStringBuilder 
       implements IBioObjectFromStringBuilder {

    private static final Logger logger 
        = Logger.getLogger( CDKBioObjectFromStringBuilder.class );
    
    public IBioObject fromString( String s ) {
        List<? extends IBioObject> l = null;
        try {
            l = Activator.getDefault().getJavaCDKManager()
                                      .moleculesFromString( s );
        }
        catch ( Exception e ) {
            logger.debug( "CDKBioObjectFromStringBuilder could not " +
            		      "recognize the string: \n " + s );
            throw new IllegalArgumentException(
                "Failed while parsing the content of the given " +
                "string into a BioObject", e );
        }
        if ( l == null || l.size() == 0 ) {
            logger.debug( "CDKBioObjectFromStringBuilder could not " +
                          "recognize the string: \n " + s );
            throw new IllegalArgumentException(
                "Failed while parsing the content of the given" + 
                "string into a BioObject" );
        }
        if ( l.size() == 1 ) {
            return l.get( 0 );
        }
        if ( l instanceof IBioObject  ) {
            return (IBioObject)l;
        }
        else {
            throw new IllegalStateException(
                "Failed cast to IBioOject. Expected a RecordableList." );
        }
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
