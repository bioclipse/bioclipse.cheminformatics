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
import org.openscience.cdk.interfaces.IBond;

/**
 *
 * @author Arvid
 * @cdk.module controlbasic
 */
public class AddBond extends AbstractEdit implements IEdit{

    IAtom atom1;
    IAtom atom2;

    IBond newBond;

    public static IEdit edit(IAtom atom1, IAtom atom2) {
        AddBond edit = new AddBond();
        edit.atom1 = atom1;
        edit.atom2 = atom2;
        return edit;
    }

    public void redo() {

        newBond = model.getBuilder().newBond(atom1,atom2);
        model.addBond( newBond );

        updateHydrogenCount( atom1,atom2 );
    }

    public void undo() {

        model.removeBond( newBond);

        updateHydrogenCount( new IAtom[] { newBond.getAtom( 0 ),
                                           newBond.getAtom(1)} );
    }

    public Set<Changed> getTypeOfChanges() {

        return changed( Changed.Structure );
    }
}
