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
package net.bioclipse.cdk.jchempaint.describer;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.api.BioclipseException;
import net.bioclipse.core.api.domain.IBioObject;
import net.bioclipse.core.api.domain.IMolecule;
import net.bioclipse.ui.business.describer.IBioObjectDescriber;


public class JCPEditorDescriber implements IBioObjectDescriber {

    ICDKManager cdk;

    public JCPEditorDescriber() {

        cdk=Activator.getDefault().getJavaCDKManager();
    }

    public String getPreferredEditorID( IBioObject object ) {
        if ( object instanceof IMolecule ) {
            try {
                ICDKMolecule cdkmol = cdk.asCDKMolecule( ( IMolecule)object);
                System.out.println(cdk.calculateMass( cdkmol ));
                if (cdk.has2d( cdkmol )){
                    return "net.bioclipse.cdk.ui.editors.jchempaint";
                }
                //Dirty workaround until we ship the plugin net.bioclipse.jmol.cdk
                //TODO: Move to JmolEditorDescriber in that plugin
                else if (cdk.has3d( cdkmol )){
                    return "net.bioclipse.jmol.editors.JmolEditor";
                }
            } catch ( BioclipseException e ) {
            }
        }

        return null;
    }

}
