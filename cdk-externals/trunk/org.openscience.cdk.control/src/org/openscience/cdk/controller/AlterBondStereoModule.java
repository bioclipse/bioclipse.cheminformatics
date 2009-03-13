/* $Revision$ $Author$ $Date$
 *
 * Copyright (C) 2008  Gilleain Torrance <gilleain.torrance@gmail.com>
 * Copyright (C) 2008  Stefan Kuhn (undo redo)
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

import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.ControllerHub.Direction;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;


/**
 * Alters the chirality of a bond, setting it to be into or outof the plane.
 * 
 * @author Gilleain Torrance
 * @cdk.svnrev $Revision$
 * @cdk.module control
 */
public class AlterBondStereoModule extends ControllerModuleAdapter {
    
    private Direction desiredDirection;

	public AlterBondStereoModule(IChemModelRelay chemModelRelay, Direction desiredDirection) {
		super(chemModelRelay);
		this.desiredDirection = desiredDirection;
	}


    public void mouseClickedDown(Point2d worldCoord) {
        
        IAtomContainer selectedAC = getSelectedAtomContainer( worldCoord );
        Set<IBond> newSelection = new HashSet<IBond>(); 
        if(selectedAC == null) return;
        for(IBond bond:selectedAC.bonds()) {
            newSelection.add( bond );
            chemModelRelay.makeBondStereo( bond, desiredDirection );
        }
	}

	public void setChemModelRelay(IChemModelRelay relay) {
		this.chemModelRelay = relay;
	}

	public String getDrawModeString() {
		if (desiredDirection==Direction.UP) {
			return "Add or convert to bond up";
		} else {
			return "Add or convert to bond down";
		}
	}

}
