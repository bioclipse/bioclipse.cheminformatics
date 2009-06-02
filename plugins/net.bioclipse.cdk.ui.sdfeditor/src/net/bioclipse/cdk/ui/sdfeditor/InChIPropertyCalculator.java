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
package net.bioclipse.cdk.ui.sdfeditor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.inchi.InChI;
import net.bioclipse.inchi.business.IInChIManager;

import org.apache.log4j.Logger;


/**
 * @author arvid
 *
 */
public class InChIPropertyCalculator implements IPropertyCalculator<InChI> {

    public InChI calculate( ICDKMolecule molecule ) {

        IInChIManager inchi = net.bioclipse.inchi.business.Activator.
        getDefault().getJavaInChIManager();
        try {
            return inchi.generate(molecule);
        } catch ( Exception e ) {
            Logger logger = Logger.getLogger( InChIPropertyCalculator.class );
            logger.warn( "Failed to calculate InChI ");
        }
        return null;
    }

    public String getPropertyName() {

        return "net.bioclipse.cdk.InChI";
    }

    public InChI parse( String value ) {
        String[] values = value.split( "\r?\n" );
        InChI result = new InChI();
        result.setKey( values[0] );
        result.setValue( values[1] );
        return result;
    }

    public String toString( Object value ) {

        if(value instanceof InChI) {
            InChI inchi = (InChI)value;
            return inchi.getKey()+"\n"+ inchi.getValue();
        }
        return "";
    }

}
