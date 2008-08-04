/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.jmol.views.outline;

import org.jmol.modelset.Atom;
import org.jmol.modelsetbio.Monomer;

/**
 * A class wrapping a Monomer in JmolContentOutline
 * @author ola
 */
public class JmolAtom extends JmolObject{

	Atom atom;
    
    /**
     * Construct a JmolChain for a Chain. Set name to ChainID
     * @param chain
     */
    public JmolAtom(Atom atom) {
        this.atom=atom;
        setName(atom.getElementSymbol() + atom.getAtomNumber());
    }

    
    public Object getObject() {
        return atom;
    }

    public void setObject(Object object) {
        atom=(Atom)object;
    }

    public Object getAdapter(Class adapter) {
        return super.getAdapter(adapter);
    }

    @Override
    public void createChildren() {
    }


    /**
     * Return monomerNo + ":" + chainID to select only this monomer 
     * or monomerNo if no chainID exists
     */
    public String getSelectString() {
        if (atom==null) return null;
        String ret="atomno=" + (atom.getAtomIndex()+1);
        return ret;
    }

}
