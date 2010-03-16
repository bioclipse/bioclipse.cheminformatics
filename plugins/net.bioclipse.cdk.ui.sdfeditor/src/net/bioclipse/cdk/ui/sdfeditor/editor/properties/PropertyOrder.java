/*******************************************************************************
 * Copyright (c) 2009  Jonathan Alvarsson <jonalv@users.sourceforge.net>
 *                     Arvid Berg         <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor.properties;

import java.util.HashMap;
import java.util.concurrent.Callable;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.domain.IMolecule.Property;

public class PropertyOrder implements Callable<Object> {

    IMoleculesEditorModel model;
    String                propertyName;
    int                   row;
    int                   col;

    public PropertyOrder( IMoleculesEditorModel model,
                         String propertyName, int row, int col) {

        this.model = model;
        this.propertyName = propertyName;
        this.row = row;
        this.col = col;
    }
    
    public static String createPropertyKey(String propertyName, int row, int col) {
        if ( col == 0 ) {
            return "the-molecule" + "|" + row + "|" + col;
        }
        else {
            return row + "|" + col + "|" + propertyName;
        }
    }

    public Object call() throws Exception {

        ICDKMolecule molecule = model.getMoleculeAt( row );

        if(col == 0) return calculateCoordinates( molecule );

        Object property =  molecule.getProperty( propertyName, Property.USE_CACHED );
        if(property == null) {
            property = molecule.getAtomContainer().getProperty( propertyName );
        }
        return property;
    }

    private ICDKMolecule calculateCoordinates(ICDKMolecule mol) {
     // If no 2D coordinates
        if ( GeometryTools.has2DCoordinatesNew( mol.getAtomContainer() )<2 ) {
            // Test if 3D coordinates
            try {

                ICDKMolecule molecule = Activator.getDefault()
                        .getJavaCDKManager()
                        .generate2dCoordinates( mol );
                IAtomContainer ac = molecule.getAtomContainer();
                //FIXME work-around for bug 613 see bug 1926
                ac.setProperties( new HashMap<Object, Object>(
                                    mol.getAtomContainer().getProperties()) );
                return molecule;
            } catch ( Exception e ) {
                return null;
            }
        }
        return mol;
    }
}