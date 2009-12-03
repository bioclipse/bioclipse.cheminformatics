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
import org.openscience.cdk.interfaces.ISingleElectron;

/**
 * Edit representing the creating of an atom.
 * @author Arvid
 * @cdk.module controlextra
 */
public class AddSingleElectron extends AbstractEdit {

    ISingleElectron newElectron;
    IAtom atom;
    
    
    public static AddSingleElectron addElectron(IAtom atom) {
        return new AddSingleElectron( atom );
    }
    
    public AddSingleElectron(IAtom atom) {
        this.atom = atom;
    }
    
    public Set<Changed> getTypeOfChanges() {
        return changed( Changed.Properties ,Changed.Structure );
    }

    public void redo() {
        if(newElectron==null)
            newElectron = atom.getBuilder().newSingleElectron( atom );

        model.addSingleElectron( newElectron );
        updateHydrogenCount( atom );
    }

    public void undo() {
        model.removeSingleElectron( newElectron );
        updateHydrogenCount( atom );
    }
}
