/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.smartsmatching;

import java.awt.Color;

import net.bioclipse.cdk.smartsmatching.views.SmartsMatchingView;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.renderer.color.CDK2DAtomColors;
import org.openscience.cdk.renderer.color.CPKAtomColors;

/**
 * Color atoms based on property 
 * @author ola
 *
 */
public class PropertyColorer extends CDK2DAtomColors {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Color getAtomColor( IAtom atom ) {
        if (getColorFromProps( atom )!=null)
            return getColorFromProps( atom );
        else
            return super.getAtomColor( atom );
    }

    public Color getAtomColor( IAtom atom, Color defaultColor ) {
        return super.getAtomColor( atom, defaultColor );
    }

    //Read color from property
    private Color getColorFromProps( IChemObject obj ) {

        Object col=obj.getProperty( SmartsMatchingView.COLOR_PROP );
        if (col==null){
            return null;
        }

        if ( col instanceof Color ) {
            Color color = (Color) col;
            return color;
        }
        return null;
    }

}
