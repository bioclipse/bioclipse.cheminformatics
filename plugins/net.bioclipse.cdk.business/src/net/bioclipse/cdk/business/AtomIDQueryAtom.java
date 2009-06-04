/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.business;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.isomorphism.matchers.IQueryAtom;
import org.openscience.cdk.nonotify.NNAtom;

/**
 * {@link IQueryAtom} that matches atoms solely on the IAtom.getID().
 *
 * @author egonw
 */
public class AtomIDQueryAtom extends NNAtom implements IQueryAtom {

    private String identifierToMatch;

    public AtomIDQueryAtom(String identifier) {
        if (identifier == null)
            throw new RuntimeException("The identifier to match must " +
                  "not be null.");
        identifierToMatch = identifier;
    }

    public boolean matches(IAtom atom) {
        return identifierToMatch.equals(atom.getID());
    }
}
