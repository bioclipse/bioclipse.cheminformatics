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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openscience.cdk.controller.Changed;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;

/**
 * Edit representing the removal of an atom.
 * @author Arvid
 * @cdk.module controlbasic
 */
public class RemoveAtom extends AbstractEdit implements IEdit{

    IAtom atomToRemove;

    Collection<IAtom> connectedAtoms;
    Set<IBond> removedBonds;
    
    /**
     * Creates an edit representing the removing of given atom.
     * @param atom to be removed.
     * @return edit representing the removal.
     */
    public static RemoveAtom remove(IAtom atom) {
        return new RemoveAtom( atom );
    }

    public static IEdit edit(IAtom atom) {
        return new RemoveAtom(atom);
    }

    private RemoveAtom(IAtom atom) {
        atomToRemove = atom;
    }

    public void redo() {
        IAtom atom = atomToRemove;

        Collection<IAtom> atomsToUpdate = model.getConnectedAtomsList( atom );
        removedBonds = new HashSet<IBond>();
        connectedAtoms = atomsToUpdate;

        removedBonds.addAll( model.getConnectedBondsList( atom ));
        model.removeAtomAndConnectedElectronContainers( atom );

        updateHydrogenCount(atomsToUpdate);
    }

    public void undo() {
        model.addAtom( atomToRemove );
        for(IBond bond:removedBonds) {
            model.addBond( bond );
        }
        Collection<IAtom> atomsToUpdate = new ArrayList<IAtom>(connectedAtoms);
        atomsToUpdate.add( atomToRemove );

        updateHydrogenCount( atomsToUpdate );
    }

    public Set<Changed> getTypeOfChanges() {
        return changed( Changed.Structure );
    }
}
