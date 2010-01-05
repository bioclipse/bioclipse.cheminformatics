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
package net.bioclipse.cdk.ui.sdfeditor.describer;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.RecordableList;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.ui.business.describer.IBioObjectDescriber;


public class MolTableDescriber implements IBioObjectDescriber {

    public MolTableDescriber() {

    }

    @SuppressWarnings("unchecked")
    public String getPreferredEditorID( IBioObject object ) throws BioclipseException {

        if ( object instanceof RecordableList ) {
            RecordableList<IBioObject> biolist = (RecordableList<IBioObject>) object;
            if (biolist.isEmpty())
                throw new BioclipseException("BioList is empty");
            
            //Make sure first object is IMolecule
            if ( biolist.get( 0 ) instanceof IMolecule ) {
                return "net.bioclipse.cdk.ui.sdfeditor";
            }
        }

        return null;
    }

}
