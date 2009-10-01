/* $Revision: $ $Author:  $ $Date: $
 *
 * Copyright (C) 2007  Gilleain Torrance
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

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.renderer.selection.AbstractSelection;
import org.openscience.cdk.renderer.selection.SingleSelection;

/**
 * Adds an atom on the given location on mouseclick
 *
 * @author maclean
 * @cdk.module controlbasic
 */
public class AddRingModule extends ControllerModuleAdapter {

    private int ringSize;
    private boolean addingBenzene = false;

    public AddRingModule(IChemModelRelay chemModelRelay, int ringSize,
            boolean addingBenzene) {
        super(chemModelRelay);
        this.ringSize = ringSize;
        this.addingBenzene = addingBenzene;
    }

    private IRing addRingToEmptyCanvas(Point2d p) {
        if (addingBenzene) {
            return chemModelRelay.addPhenyl(p);
        } else {
            return chemModelRelay.addRing(ringSize, p);
        }
    }

    private IRing addRingToAtom(IAtom closestAtom) {
    	IRing newring;
        if (addingBenzene) {
            newring = chemModelRelay.addPhenyl(closestAtom);
        } else {
            newring = chemModelRelay.addRing(closestAtom, ringSize);
        }
        newring.removeAtom(closestAtom);
        return newring;
    }

    private IRing addRingToBond(IBond bond) {
    	IRing newring;
        if (addingBenzene) {
            newring = chemModelRelay.addPhenyl(bond);
        } else {
            newring = chemModelRelay.addRing(bond, ringSize);
        }
        newring.removeAtom(bond.getAtom(0));
        newring.removeAtom(bond.getAtom(1));
        newring.removeBond(bond);
        return newring;
    }

    public void mouseClickedDown(Point2d worldCoord) {
        IAtom closestAtom = chemModelRelay.getClosestAtom(worldCoord);
        IBond closestBond = chemModelRelay.getClosestBond(worldCoord);

        IChemObject singleSelection = getHighlighted( worldCoord,
                                                      closestAtom,closestBond );

        if (singleSelection == null) {
			this.addRingToEmptyCanvas(worldCoord);
		} else if (singleSelection instanceof IAtom) {
			this.addRingToAtom((IAtom) singleSelection);
		} else if (singleSelection instanceof IBond) {
			this.addRingToBond((IBond) singleSelection);
		}
		if (singleSelection == null)
			setSelection(AbstractSelection.EMPTY_SELECTION);
		else
			setSelection(new SingleSelection<IChemObject>(singleSelection));

		chemModelRelay.updateView();
	}

    public String getDrawModeString() {
    	if (addingBenzene) {
			return "Benzene";
    	} else {
			return "Ring" + " " + ringSize;
    	}
    }

}
