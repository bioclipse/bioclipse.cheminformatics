/* $Revision: $ $Author:  $ $Date$
 *
 * Copyright (C) 2007  Gilleain Torrance <gilleain.torrance@gmail.com>
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
import org.openscience.cdk.tools.manipulator.BondManipulator;

/**
 * @cdk.module control
 */
public class AddBondModule extends ControllerModuleAdapter {
	
	public AddBondModule(IChemModelRelay relay) {
		super(relay);
	}
	
	private void cycleBondValence(IBond bond) {
        // cycle the bond order up to maxOrder
//      IBond.Order maxOrder = IBond.Order.TRIPLE;
        IBond.Order maxOrder = IBond.Order.QUADRUPLE; //testing
        if (BondManipulator.isLowerOrder(bond.getOrder(), maxOrder)) {
            BondManipulator.increaseBondOrder(bond);
        } else {
            bond.setOrder(IBond.Order.SINGLE);
        }
        chemModelRelay.updateView();
	}
	
	private void addBondToAtom(IAtom atom) {
	       String atomType = chemModelRelay.getController2DModel().getDrawElement();
           try {
               chemModelRelay.addAtom(atomType, atom);
               chemModelRelay.updateView();
           } catch (Exception e) {
               e.printStackTrace();
           }    
	}
	
	public void mouseClickedDown(Point2d worldCoordinate) {
		IAtom closestAtom = chemModelRelay.getClosestAtom(worldCoordinate);
		IBond closestBond = chemModelRelay.getClosestBond(worldCoordinate);
		
		if (closestAtom == null && closestBond == null) {
			return;
		} else if (closestAtom == null && closestBond != null) {
		    this.cycleBondValence(closestBond);
		} else if (closestAtom != null && closestBond == null) {
		    this.addBondToAtom(closestAtom);
		} else {
		    double dA = closestAtom.getPoint2d().distance(worldCoordinate);
		    double dB = closestBond.get2DCenter().distance(worldCoordinate);
		    if (dA <= dB) {
		        this.addBondToAtom(closestAtom);
		    } else {
		        this.cycleBondValence(closestBond);
		    }
		}
		
	}

	public String getDrawModeString() {
		return IControllerModel.DrawMode.DRAWBOND.getName();
	}

}
