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


/**
 * @author arvid
 *
 */
public class NumberOfAtomCalculator implements IPropertyCalculator<Integer> {

    public static final String PROPERTY_NAME = "NumberOfAtoms";
    /**
     *
     */
    public NumberOfAtomCalculator() {

        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator#calculate(net.bioclipse.cdk.domain.ICDKMolecule)
     */
    public Integer calculate( ICDKMolecule molecule ) {

        return molecule.getAtomContainer().getAtomCount();
    }

    /* (non-Javadoc)
     * @see net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator#getPropertyName()
     */
    public String getPropertyName() {

        return PROPERTY_NAME;
    }

    public Integer parse( String value ) {
        return Integer.parseInt( value );
    }

    public String toString( Object value ) {
        if(value instanceof String) return (String)value;
        Integer tmp = (Integer) value;
        return tmp.toString();
    }

}
