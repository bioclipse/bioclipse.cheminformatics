/*
 * Copyright (C) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All I ask is that proper credit is given for my work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.controller.edit;

import java.util.Set;

import org.openscience.cdk.controller.Changed;
import org.openscience.cdk.interfaces.IAtom;

/**
* Edit for changing symbol of an atom.
* @author Arvid
* @cdk.module controlbasic
*/
public class SetSymbol extends AbstractEdit{

    IAtom atom;
    String symbol;
    String oldSymbol;

    /**
     * Creates an edit for changing the given atom's symbol.
     *
     * @param atom to change.
     * @param symbol to change to.
     * @return an edit representing this change.
     */
    public static SetSymbol setSymbol(IAtom atom, String symbol) {
        return new SetSymbol(atom,symbol);
    }

    private SetSymbol(IAtom atom, String symbol) {
        this.atom = atom;
        this.symbol = symbol;
    }

    public void redo() {
        oldSymbol = atom.getSymbol();
        atom.setSymbol(symbol);
        updateHydrogenCount(atom);
    }

    public void undo() {

        atom.setSymbol(oldSymbol);
        updateHydrogenCount(atom);
    }

    public Set<Changed> getTypeOfChanges() {
        return changed(Changed.Properties);
    }
}
