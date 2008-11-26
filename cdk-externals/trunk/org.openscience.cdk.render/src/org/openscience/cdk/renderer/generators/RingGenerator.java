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

import static org.openscience.cdk.CDKConstants.ISAROMATIC;

import java.util.Collection;
import java.util.HashSet;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.LineElement.LineType;
import org.openscience.cdk.tools.LoggingTool;

/**
 * @cdk.module render
 */
public class RingGenerator extends BasicBondGenerator {

	private Renderer2DModel model;
	private LoggingTool logger = new LoggingTool(RingGenerator.class);
	private Collection<IRing> painted_rings;

	public RingGenerator(Renderer2DModel r2dm) {
		super(r2dm);
		this.model = r2dm;
		painted_rings = new HashSet<IRing>();
	}

	@Override
	public IRenderingElement generateRingElements(IBond bond, IRing ring) {
		if (ringIsAromatic(ring) && this.model.getShowAromaticity()) {
			ElementGroup pair = new ElementGroup();
			pair.add(generateBondElement(bond, LineType.SINGLE));
			if (!painted_rings.contains(ring)) {
				painted_rings.add(ring);
				pair.add(generateRingRingElement(bond, ring));
			}
			return pair;
		} else {
			return super.generateRingElements(bond, ring);
		}
	}

	private IRenderingElement generateRingRingElement(IBond bond, IRing ring) {
		Point2d center = GeometryTools.get2DCenter(ring);
		logger.debug(" painting a Ringring now at " + center);

		double[] minmax = GeometryTools.getMinMax(ring);
		double width = (minmax[2] - minmax[0]) * 0.7;
		double height = (minmax[3] - minmax[1]) * 0.7;

		// offset is the width of the ring
		double lineWidth = (0.05 * Math.min(width, height));
		double radius = Math.min(width, height) - lineWidth / 2;

		return new OvalElement(center.x, center.y, radius, super.getColorForBond(bond));
	}

	private boolean ringIsAromatic(final IRing ring) {
		boolean isAromatic = true;
		for (IAtom atom : ring.atoms()) {
			if (!atom.getFlag(ISAROMATIC)) {
				isAromatic = false;
				break;
			}
		}
		if (!isAromatic) {
			for (IBond b : ring.bonds()) {
				if (!b.getFlag(ISAROMATIC)) {
					return false;
				}
			}
		}
		return isAromatic;
	}
}