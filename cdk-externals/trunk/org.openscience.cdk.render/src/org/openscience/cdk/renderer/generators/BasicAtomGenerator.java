/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2008  Arvid Berg <goglepox@users.sf.net>
 *
 *  Contact: cdk-devel@list.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.renderer.generators;

import java.awt.Color;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.AtomSymbolElement;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.validate.ProblemMarker;

/**
 * @cdk.module render
 */
public class BasicAtomGenerator implements IGenerator{

	protected Renderer2DModel model; 
	
	public BasicAtomGenerator(Renderer2DModel r2dm) {
		this.model = r2dm;
	}

	public IRenderingElement generate(IAtomContainer ac, Point2d center) {
		ElementGroup elementGroup = new ElementGroup();
		for (IAtom atom : ac.atoms()) {
			elementGroup.add(this.generate(ac, atom, center));
		}
		return elementGroup;
	}
	
	protected Color getColorForAtom(IAtom atom) {
		return this.model.getAtomColor(atom, Color.BLACK);
	}

	public IRenderingElement generate(IAtomContainer ac, IAtom atom, Point2d center) {
		// FIXME: pseudoatom from paintAtom

		if (isHydrogen(atom) && !this.model.getShowExplicitHydrogens()) {
			return null;// don't draw hydrogen
		}
		
		if (isCarbon(atom)) {
			if (!showCarbon(atom, ac)) {
				return null;// draw carbon
			}
		}
		
		int alignment = GeometryTools.getBestAlignmentForLabelXY(ac, atom);
		return generateElements(atom, alignment, center);
	}

	public IRenderingElement generateElements(IAtom atom, int alignment, Point2d center) {
		return new AtomSymbolElement(
				atom.getPoint2d().x - center.x,
				atom.getPoint2d().y - center.y,
				atom.getSymbol(), 
				atom.getFormalCharge(),
				atom.getHydrogenCount(),
				alignment, this.getColorForAtom(atom));
	}

	public boolean isHydrogen(IAtom atom) {
		return "H".equals(atom.getSymbol());
	}

	public boolean isCarbon(IAtom atom) {
		return "C".equals(atom.getSymbol());
	}

	public boolean showCarbon(IAtom atom, IAtomContainer ac) {

		if (this.model.getKekuleStructure())
			return true;

		if (atom.getFormalCharge() != 0)
			return true;

		if (ac.getConnectedBondsList(atom).size() < 1)
			return true;

		if (this.model.getShowEndCarbons()
				&& ac.getConnectedBondsList(atom).size() == 1)
			return true;

		if (atom.getProperty(ProblemMarker.ERROR_MARKER) != null)
			return true;

		if (ac.getConnectedSingleElectronsCount(atom) > 0)
			return true;

		return false;
	}
}
