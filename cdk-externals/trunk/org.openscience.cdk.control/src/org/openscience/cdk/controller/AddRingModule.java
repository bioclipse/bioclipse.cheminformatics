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

import org.openscience.cdk.controller.undoredo.IUndoRedoable;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRing;

/**
 * Adds an atom on the given location on mouseclick
 * 
 * @author Gilleain Torrance
 * @cdk.module control
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
        if (this.addingBenzene) {
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
        
        double dH = getHighlightDistance();
        double dA = super.distanceToAtom(closestAtom, worldCoord);
        double dB = super.distanceToBond(closestBond, worldCoord);
        
        IRing newring;
        if (noSelection(dA, dB, dH)) {
            newring = this.addRingToEmptyCanvas(worldCoord);
        } else if (isAtomOnlyInHighlightDistance(dA, dB, dH) || dA < dB) {
        	newring = this.addRingToAtom(closestAtom);
        } else if (isBondOnlyInHighlightDistance(dA, dB, dH) || dB < dA) {
        	newring = this.addRingToBond(closestBond);
        } else {
            // the closest bond and closest atom are equidistant
        	newring = this.addRingToAtom(closestAtom);
        }
	    if(chemModelRelay.getUndoRedoFactory()!=null && chemModelRelay.getUndoRedoHandler()!=null){
		    IUndoRedoable undoredo = chemModelRelay.getUndoRedoFactory().getAddAtomsAndBondsEdit(chemModelRelay.getIChemModel(), newring.getBuilder().newAtomContainer(newring), this.getDrawModeString(),chemModelRelay.getController2DModel());
		    chemModelRelay.getUndoRedoHandler().postEdit(undoredo);
	    }
        chemModelRelay.updateView();
    }

    public void setChemModelRelay(IChemModelRelay relay) {
        this.chemModelRelay = relay;
    }

    public String getDrawModeString() {
    	if(addingBenzene)
    		return "Benzene";
    	else
    		return "Ring" + " " + ringSize;
    }

}
