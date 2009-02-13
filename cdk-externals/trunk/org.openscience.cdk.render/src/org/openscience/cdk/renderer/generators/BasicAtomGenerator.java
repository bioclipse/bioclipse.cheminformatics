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
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.RenderingParameters.AtomShape;
import org.openscience.cdk.renderer.elements.AtomSymbolElement;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.RectangleElement;
import org.openscience.cdk.validate.ProblemMarker;

/**
 * @cdk.module render
 */
public class BasicAtomGenerator implements IGenerator {

	protected RendererModel model;

	public BasicAtomGenerator(RendererModel r2dm) {
		this.model = r2dm;
	}
	
    public void setRendererModel(RendererModel model) {
        this.model = model;
    }

	public IRenderingElement generate(IAtomContainer ac) {
		ElementGroup elementGroup = new ElementGroup();
		for (IAtom atom : ac.atoms()) {
			elementGroup.add(this.generate(ac, atom));
		}
		return elementGroup;
	}

	protected Color getColorForAtom(IAtom atom) {
		return this.model.getAtomColor(atom, Color.BLACK);
	}

	public IRenderingElement generate(IAtomContainer ac, IAtom atom) {
		// FIXME: pseudoatom from paintAtom
	    if (atom == null || atom.getPoint2d() == null)
	      return null;

		if (isHydrogen(atom) && !this.model.getShowExplicitHydrogens()) {
		    // don't draw hydrogen
			return null;
		}

		if (isCarbon(atom) && !showCarbon(atom, ac)) {
		    // don't draw carbon
		    return null;
		}

		if (this.model.getIsCompact()) {
		    return this.generateCompactElement(atom);
		}

		int alignment = GeometryTools.getBestAlignmentForLabelXY(ac, atom);
		return generateElements(atom, alignment);
	}

	public IRenderingElement generateCompactElement(IAtom atom) {
	    Point2d p = atom.getPoint2d();
	    double r = model.getAtomRadius() / model.getScale();
	    double d = 2 * r;
	    if (model.getCompactShape() == AtomShape.SQUARE) {
    	    return new RectangleElement(
    	            p.x - r, p.y - r, d, d, true, this.getColorForAtom(atom));
	    } else {
	        return new OvalElement(
	                p.x, p.y, r, true, this.getColorForAtom(atom));
	    }
	}

	public IRenderingElement generateElements(IAtom atom, int alignment) {
		return new AtomSymbolElement(
				atom.getPoint2d().x,
				atom.getPoint2d().y,
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
