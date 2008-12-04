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

/**
 * Adds an atom on the given location on mouseclick
 * 
 * @author Gilleain Torrance
 * @cdk.module control
 */
public class AddRingModule extends ControllerModuleAdapter {

    private int ringSize;
    private boolean addingBenzene = false;

    public AddRingModule(IChemModelRelay chemModelRelay, int ringSize, boolean addingBenzene) {
        super(chemModelRelay);
        this.ringSize = ringSize;
        this.addingBenzene = addingBenzene;
    }
    
    private void addRingToEmptyCanvas() {
        if (this.addingBenzene) {
            Point2d randomPoint = new Point2d(0,0);
            chemModelRelay.addAtom("C", randomPoint);
            IAtom closestAtom = chemModelRelay.getClosestAtom(randomPoint);
            chemModelRelay.addPhenyl(closestAtom);
        } else {
            chemModelRelay.addRing(ringSize, new Point2d(0,0));
        }
    }
    
    private void addRingToAtom(IAtom closestAtom) {
        if (addingBenzene) {
            chemModelRelay.addPhenyl(closestAtom);
        } else {
            chemModelRelay.addRing(closestAtom, ringSize);
        }
    }
    
    private void addRingToBond(IBond bond) {
        if (addingBenzene) {
            chemModelRelay.addPhenyl(bond);
        } else {
            chemModelRelay.addRing(bond, ringSize);
        }
    }

    public void mouseClickedDown(Point2d worldCoord) {
        IAtom closestAtom = chemModelRelay.getClosestAtom(worldCoord);
        IBond closestBond = chemModelRelay.getClosestBond(worldCoord);
        
        if (closestAtom == null && closestBond == null) {
            this.addRingToEmptyCanvas();
        } else if (closestAtom != null && closestBond == null) {
            this.addRingToAtom(closestAtom);
        } else if (closestAtom == null && closestBond != null) {
            this.addRingToBond(closestBond);
        } else {
            double dA = closestAtom.getPoint2d().distance(worldCoord);
            double dB = closestBond.get2DCenter().distance(worldCoord);
            if (dA <= dB) {
                this.addRingToAtom(closestAtom);
            } else {
                this.addRingToBond(closestBond);
            }
        }
        chemModelRelay.updateView();
    }

    public void setChemModelRelay(IChemModelRelay relay) {
        this.chemModelRelay = relay;
    }

    public String getDrawModeString() {
        return IControllerModel.DrawMode.RING.getName() + " " + ringSize;
    }

}
