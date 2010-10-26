/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.jmol.model;

import java.util.List;

import org.xmlcml.cml.element.CMLMolecule;

import net.bioclipse.core.api.BioclipseException;
import net.bioclipse.core.api.domain.BioObject;
import net.bioclipse.core.api.domain.IMolecule;
import net.bioclipse.core.api.domain.RecordableList;

public class JmolMolecule extends BioObject implements IJmolMolecule {

    private CMLMolecule molecule;

    public List<IMolecule> getConformers() {
        return new RecordableList<IMolecule>();
    }

    public JmolMolecule(CMLMolecule molecule) {
        this.molecule = molecule;
    }

    public String toCML() throws BioclipseException {
        return molecule.toXML();
    }

    public String toSMILES() throws BioclipseException {
        throw new BioclipseException("Not implemented");
    }

}
