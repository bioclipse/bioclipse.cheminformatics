/* $Revision: 7636 $ $Author: nielsout $ $Date: 2007-01-04 18:46:10 +0100 (Thu, 04 Jan 2007) $
 *
 * Copyright (C) 2007  Niels Out <nielsout@users.sf.net>
 * Copyright (C) 2008  Stefan Kuhn (undo/redo)
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

import java.util.Iterator;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.undoredo.IUndoRedoable;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;


/**
 * Deletes closest atom on click
 * 
 * @author Niels Out
 * @cdk.svnrev  $Revision: 9162 $
 * @cdk.module  control
 */
public class RemoveModule extends ControllerModuleAdapter {

	public RemoveModule(IChemModelRelay chemObjectRelay) {
		super(chemObjectRelay);
	}
	
	public void mouseClickedDown(Point2d worldCoordinate) {
	    IAtom closestAtom = chemModelRelay.getClosestAtom(worldCoordinate);
		IBond closestBond = chemModelRelay.getClosestBond(worldCoordinate);
		
		double dH = super.getHighlightDistance();
        double dA = super.distanceToAtom(closestAtom, worldCoordinate);
        double dB = super.distanceToBond(closestBond, worldCoordinate);
		
		if (super.noSelection(dA, dB, dH)) {
		    return;
		} else if (super.isAtomOnlyInHighlightDistance(dA, dB, dH)) {
		    removeAtom(closestAtom);
		} else if (super.isBondOnlyInHighlightDistance(dA, dB, dH)) {
		    removeBond(closestBond);
		} else {
            if (dA <= dB) {
                removeAtom(closestAtom);               
            } else {
                removeBond(closestBond);
            }
		}
			
	}
	
	private void removeAtom(IAtom atom) {
		IAtomContainer undAtomContainer = atom.getBuilder().newAtomContainer();
        undAtomContainer.addAtom(atom);
        Iterator<IBond> it=ChemModelManipulator.getRelevantAtomContainer(chemModelRelay.getIChemModel(),atom).getConnectedBondsList(atom).iterator();
        while(it.hasNext())
        	undAtomContainer.addBond(it.next());
        chemModelRelay.removeAtom(atom);
        chemModelRelay.updateView();
	    if(chemModelRelay.getUndoRedoFactory()!=null && chemModelRelay.getUndoRedoHandler()!=null){
		    IUndoRedoable undoredo = chemModelRelay.getUndoRedoFactory().getRemoveAtomsAndBondsEdit(chemModelRelay.getIChemModel(), undAtomContainer,this.getDrawModeString());
		    chemModelRelay.getUndoRedoHandler().postEdit(undoredo);
	    }
	}
	
	private void removeBond(IBond bond) {
		IAtomContainer undAtomContainer = bond.getBuilder().newAtomContainer();
        chemModelRelay.removeBond(bond);
        undAtomContainer.addBond(bond);
        chemModelRelay.updateView();
	    if(chemModelRelay.getUndoRedoFactory()!=null && chemModelRelay.getUndoRedoHandler()!=null){
		    IUndoRedoable undoredo = chemModelRelay.getUndoRedoFactory().getRemoveAtomsAndBondsEdit(chemModelRelay.getIChemModel(), undAtomContainer,this.getDrawModeString());
		    chemModelRelay.getUndoRedoHandler().postEdit(undoredo);
	    }
	}

	public String getDrawModeString() {
		return "Delete";
	}
	
}
