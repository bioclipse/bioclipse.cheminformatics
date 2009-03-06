/* $Revision: $ $Author:  $ $Date$
 *
 * Copyright (C) 2007  Gilleain Torrance <gilleain.torrance@gmail.com>
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

import static org.openscience.cdk.CDKConstants.STEREO_BOND_NONE;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.undoredo.IUndoRedoFactory;
import org.openscience.cdk.controller.undoredo.IUndoRedoable;
import org.openscience.cdk.controller.undoredo.UndoRedoHandler;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.tools.manipulator.BondManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * Adds a bond on clicking an atom, or cycles the order of clicked bonds. 
 * 
 * @cdk.module control
 */
public class AddBondModule extends ControllerModuleAdapter {

	public AddBondModule(IChemModelRelay relay) {
		super(relay);
	}
	
	public void mouseClickedDown(Point2d worldCoordinate) {
		IAtom closestAtom = chemModelRelay.getClosestAtom(worldCoordinate);
		IBond closestBond = chemModelRelay.getClosestBond(worldCoordinate);
		
        double dH = super.getHighlightDistance();
        double dA = super.distanceToAtom(closestAtom, worldCoordinate);
        double dB = super.distanceToBond(closestBond, worldCoordinate);
		
		if (noSelection(dA, dB, dH)) {
            chemModelRelay.addNewBond(worldCoordinate);
        } else if (isAtomOnlyInHighlightDistance(dA, dB, dH)) {
    	    String atomType = 
    	        chemModelRelay.getController2DModel().getDrawElement();
    	    chemModelRelay.addAtom(atomType, closestAtom);
        } else if (isBondOnlyInHighlightDistance(dA, dB, dH)) {
            chemModelRelay.cycleBondValence(closestBond);
        } else {
		    if (dA <= dB) {
			    String atomType = 
			        chemModelRelay.getController2DModel().getDrawElement();
			    chemModelRelay.addAtom(atomType, closestAtom);
		    } else {
		    	chemModelRelay.cycleBondValence(closestBond);
		    }
		}
		chemModelRelay.updateView();
	}
	
	public String getDrawModeString() {
		return "Draw Bond";
	}

}
