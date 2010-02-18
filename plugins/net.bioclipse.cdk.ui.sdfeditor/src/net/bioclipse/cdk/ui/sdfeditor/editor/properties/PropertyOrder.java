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

import java.util.concurrent.Callable;

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

        if(col == 0) return molecule;

        Object property =  molecule.getProperty( propertyName, Property.USE_CACHED );
        if(property == null) {
            property = molecule.getAtomContainer().getProperty( propertyName );
        }
        return property;
    }
}