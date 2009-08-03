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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openscience.cdk.controller.Changed;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 *
 * @author Arvid
 * @cdk.module controlbasic
 */
public abstract class AbstractEdit implements IEdit{

    protected IAtomContainer model;
    private Collection<IAtom> atomsToUpdate;

    /**
     * Adds <code>Changed</code> types passed in to its changed types set.
     * @param changed changes types that this edit has made
     */
    static Set<Changed> changed(Changed... changed) {
        List<Changed> ch = Arrays.asList( changed );

        Set<Changed> changes = new HashSet<Changed>();
        changes.addAll( ch );
        return changes;
    }


    /**
     *  Updates an atoms with respect to its hydrogen count
     *
     *@param  atomsToUpdate  The atoms that needs updating
     */
     void updateHydrogenCount( Collection<IAtom> atomsToUpdate ) {

        if (this.atomsToUpdate == null) {
            this.atomsToUpdate = new ArrayList<IAtom>();
        }

        this.atomsToUpdate = atomsToUpdate;
    }

     void updateHydrogenCount( IAtom... atoms ) {
         updateHydrogenCount( Arrays.asList( atoms ) );
     }

     public Collection<IAtom> getAtomsToUpdate() {
         return atomsToUpdate;
    }

     public void execute( IAtomContainer ac ) {
         model = ac;
         redo();
    }

     public boolean canRedo() {
         return true;
    }

     public boolean canUndo() {
         return true;
     }
}