/*******************************************************************************
 * Copyright (c) 2009 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Kuhn, Miguel Rojas
 *
 ******************************************************************************/

package net.bioclipse.cdk.domain;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioObject;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.libio.cml.Convertor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.xmlcml.cml.element.CMLReaction;

/**
 * The CDKReaction wraps an IReaction
 *
 */
public class CDKReaction extends BioObject implements ICDKReaction {

    private IReaction reaction;

    
    public CDKReaction(IReaction reaction) {
        this.reaction=reaction;
    }

    public String getSmiles() throws BioclipseException {
        // Create the SMILES
        SmilesGenerator generator = new SmilesGenerator();
        String cachedSMILES;
        try {
            cachedSMILES = generator.createSMILES(reaction);
        } catch ( CDKException e ) {
            throw new BioclipseException(e.getMessage());
        }

        return cachedSMILES;
    }

    public IReaction getReaction() {
        return reaction;
    }


    public String getCML() throws BioclipseException {

        if (getReaction()==null) throw new BioclipseException("No molecule to " +
        "get CML from!");

        Convertor convertor = new Convertor(true, null);
        CMLReaction cmlMol = convertor.cdkReactionToCMLReaction( getReaction());
        return cmlMol.toXML();
    }
}
