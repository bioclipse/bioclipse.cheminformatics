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
package net.bioclipse.cdk.domain;


/**
 * @author arvid
 *
 */
public class CDKMoleculeUtils {
    
    /**
     * Method for inserting cached properties into a CDKMolecule
     * @param mol Molecule to work on.
     * @param key Property to set
     * @param value value of property
     */
    public static void setProperty(ICDKMolecule mol, String key, Object value) {
        if(mol instanceof CDKMolecule) {
            ((CDKMolecule)mol).setProperty( key, value );
        }
    }

}
