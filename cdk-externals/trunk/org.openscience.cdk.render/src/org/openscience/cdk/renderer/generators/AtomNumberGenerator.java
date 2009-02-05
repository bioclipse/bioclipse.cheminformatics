/**
 *  Copyright (C) 2001-2007  The Chemistry Development Kit (CDK) Project
 *
 *  Contact: cdk-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package org.openscience.cdk.renderer.generators;

import java.awt.Color;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.TextElement;

/**
 * @author maclean
 * @cdk.module render
 */
public class AtomNumberGenerator implements IGenerator {

	private RendererModel model;

	public AtomNumberGenerator(RendererModel model) {
		this.model = model;
	}

	/* (non-Javadoc)
	 * @see org.openscience.cdk.renderer.generators.IGenerator#generate(org.openscience.cdk.interfaces.IAtomContainer)
	 */
	public IRenderingElement generate(IAtomContainer ac) {
		ElementGroup numbers = new ElementGroup();
		if (!this.model.drawNumbers()) return numbers;

		int number = 1;
		for (IAtom atom : ac.atoms()) {
			Point2d p = atom.getPoint2d();
			numbers.add(
					new TextElement(
							p.x, p.y, String.valueOf(number), Color.BLACK));
			number++;
		}
		return numbers;
	}

    public void setRendererModel(RendererModel model) {
        this.model = model;
    }

}
