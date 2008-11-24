/* $Revision: 7636 $ $Author: nielsout $ $Date: 2007-01-04 18:46:10 +0100 (Thu, 04 Jan 2007) $
 *
 * Copyright (C) 2007  Niels Out <nielsout@users.sf.net>
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
import org.openscience.cdk.renderer.Renderer2DModel;

/**
 * This should highlight the atom/bond when moving over with the mouse
 * 
 * @author Niels Out
 * @cdk.svnrev $Revision: 9162 $
 * @cdk.module control
 */
public class Controller2DModuleHighlight extends ControllerModuleAdapter {

	private IChemModelRelay chemObjectRelay;

	public Controller2DModuleHighlight(IChemModelRelay chemObjectRelay) {
		super(chemObjectRelay);
	}

	private IAtom PrevHighlightAtom;
	private IBond PrevHighlightBond;

	public void mouseMove(Point2d worldCoord) {
		IAtom atom = chemObjectRelay.getClosestAtom(worldCoord);
		IBond bond = chemObjectRelay.getClosestBond(worldCoord);
		Renderer2DModel model = chemObjectRelay.getIJava2DRenderer().getRenderer2DModel();
		
		if (atom != null
				&& (bond == null || bond.get2DCenter().distance(worldCoord) >= atom
						.getPoint2d().distance(worldCoord))) {
			if (PrevHighlightAtom != atom) {
				System.out.println("Hovering over another atom now: " + atom.getSymbol());

				model.setHighlightedAtom(atom);
				PrevHighlightAtom = atom;
				chemObjectRelay.updateView();
			}
		} else if (PrevHighlightAtom != null) {
			// 'un'-highlight things here..
			System.out
					.println("Time to 'un'-highlight PrevHighlightAtom now..: ");
			model.setHighlightedAtom(null);
			PrevHighlightAtom = null;
			chemObjectRelay.updateView();
		}
		if (bond != null
				&& (atom == null || bond.get2DCenter().distance(worldCoord) < atom
						.getPoint2d().distance(worldCoord))) {
			if (PrevHighlightBond != bond) {
				System.out.println("Hovering over another bond now: " + bond);

				model.setHighlightedBond(bond);
				PrevHighlightBond = bond;
				chemObjectRelay.updateView();
			}
		} else if (PrevHighlightBond != null) {
			// 'un'-highlight things here..
			System.out
					.println("Time to 'un'-highlight PrevHighlightBond now..: ");
			model.setHighlightedBond(null);
			PrevHighlightBond = null;
			chemObjectRelay.updateView();
		}
	}

	public void setChemModelRelay(IChemModelRelay relay) {
		this.chemObjectRelay = relay;
	}

}
