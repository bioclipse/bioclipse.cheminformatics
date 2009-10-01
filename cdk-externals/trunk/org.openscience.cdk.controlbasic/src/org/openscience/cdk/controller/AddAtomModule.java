/* $Revision: 7636 $ $Author: nielsout $ $Date: 2007-01-04 18:46:10 +0100 (Thu, 04 Jan 2007) $
 *
 * Copyright (C) 2007  Niels Out <nielsout@users.sf.net>
 * Copyright (C) 2008  Stefan Kuhn (undo redo)
 * Copyright (C) 2009 Arvid Berg <goglepox@users.sf.net>
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
import org.openscience.cdk.renderer.RendererModel;
import static org.openscience.cdk.controller.edit.AddAtom.createAtom;
import static org.openscience.cdk.controller.edit.SetSymbol.setSymbol;

/**
 * Adds an atom on the given location on mouseclick
 * 
 * @author Niels Out
 * @cdk.svnrev $Revision: 9162 $
 * @cdk.module controlbasic
 */
public class AddAtomModule extends ControllerModuleAdapter {

	public AddAtomModule(IChemModelRelay chemModelRelay) {
		super(chemModelRelay);
	}

	public void mouseClickedDown(Point2d worldCoord) {

		IAtom closestAtom = chemModelRelay.getClosestAtom(worldCoord);
		RendererModel model = chemModelRelay.getRenderer().getRenderer2DModel();
		
		double dH = model.getHighlightDistance() / model.getScale();
		String atomType = 
			chemModelRelay.getController2DModel().getDrawElement();
		
		if (closestAtom == null || 
		        closestAtom.getPoint2d().distance(worldCoord) > dH) {
		    chemModelRelay.execute(createAtom(atomType, worldCoord));
		} else {
			chemModelRelay.execute(setSymbol(closestAtom, atomType));
		}
	}

	public String getDrawModeString() {
		return "Add Atom Or Change Element";
	}

}
