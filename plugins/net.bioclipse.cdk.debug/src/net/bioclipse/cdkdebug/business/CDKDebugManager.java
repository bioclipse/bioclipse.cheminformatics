/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Egon Willighagen <egonw@user.sf.net>
 ******************************************************************************/
package net.bioclipse.cdkdebug.business;

import org.openscience.cdk.tools.diff.AtomContainerDiff;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ui.Activator;

public class CDKDebugManager implements ICDKDebugManager {

    public void diff(ICDKMolecule mol, ICDKMolecule mol2) {
        Activator.getDefault().CONSOLE.echo(
            AtomContainerDiff.diff(mol.getAtomContainer(), mol2.getAtomContainer())
        ); 
    }

    public void debug(ICDKMolecule mol) {
        Activator.getDefault().CONSOLE.echo(
            mol.getAtomContainer().toString()
        ); 
    }

    public String getNamespace() {
        return "cdkdebug";
    }

}
