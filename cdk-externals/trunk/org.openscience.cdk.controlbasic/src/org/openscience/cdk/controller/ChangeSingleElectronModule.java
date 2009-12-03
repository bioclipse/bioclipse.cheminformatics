/*
 * Copyright (C) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 * Copyright (C) 2009  Stefan Kuhn
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
package org.openscience.cdk.controller;

import static org.openscience.cdk.controller.edit.AddSingleElectron.addElectron;
import static org.openscience.cdk.controller.edit.CompositEdit.compose;
import static org.openscience.cdk.controller.edit.RemoveSingleElectron.removeElectron;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.edit.IEdit;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
+ * Changes (Increases or Decreases) single electrons of an atom
+ * 
+ * @cdk.module controlextra
+ */
public class ChangeSingleElectronModule extends ControllerModuleAdapter {
    
    boolean add;

    public ChangeSingleElectronModule( IChemModelRelay chemModelRelay,
                                       boolean add) {
        super( chemModelRelay );
        this.add = add;
    }
    
    @Override
    public void mouseClickedDown( Point2d worldCoord ) {
        IAtomContainer selectedAC = getSelectedAtomContainer( worldCoord );
        List<IEdit> edits = new ArrayList<IEdit>(selectedAC.getAtomCount());
        for(IAtom atom:selectedAC.atoms()) {
            if(add){
                edits.add( addElectron( atom ) );
            }else{
                edits.add( removeElectron( atom ) );
            }
        }
        if(!edits.isEmpty()) {
            chemModelRelay.execute( compose( edits ) );
        }
    }

    public String getDrawModeString() {
        if (add)
            return "Add Single Electron";
        else
            return "Remove Single Electron";
    }

}
