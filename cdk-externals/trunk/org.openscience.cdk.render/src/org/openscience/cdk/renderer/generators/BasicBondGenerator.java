/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2008  Arvid Berg <goglepox@users.sf.net>, gilleain
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

import static org.openscience.cdk.CDKConstants.STEREO_BOND_DOWN;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_DOWN_INV;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_NONE;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_UNDEFINED;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_UP_INV;

import java.awt.Color;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement.Direction;
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.tools.manipulator.RingSetManipulator;

/**
 * @cdk.module render
 */
public class BasicBondGenerator implements IGenerator{

	private Renderer2DModel model; 
	private LoggingTool logger = new LoggingTool(BasicBondGenerator.class);
	private IRingSet ringSet;
	private double bondWidth;
	private double bondDistance;

	public BasicBondGenerator(Renderer2DModel r2dm) {
		this.model = r2dm;
		bondWidth = this.model.getBondWidth();
//		bondDistance = this.model.getBondDistance();
		// the original bonddistance was in screen space, not model space...
		bondDistance = 0.2;
	}

	protected IRingSet getRingSet(final IAtomContainer atomContainer) {

		IRingSet ringSet = atomContainer.getBuilder().newRingSet();
		try {
			IMoleculeSet molecules =
				ConnectivityChecker.partitionIntoMolecules(atomContainer);
			for (IAtomContainer mol : molecules.molecules()) {
				SSSRFinder sssrf = new SSSRFinder(mol);
				ringSet.add(sssrf.findSSSR());
			}
			
			return ringSet;
		} catch (Exception exception) {
			logger.warn("Could not partition molecule: "
					+ exception.getMessage());
			logger.debug(exception);
			return ringSet;
		}
	}
	
	public Color getColorForBond(IBond bond) {
	    Color color = this.model.getColorHash().get(bond);
	    if(color == null) color = Color.BLACK;
	    return color; 
	}

	public IRenderingElement generate(IAtomContainer ac) {
		ElementGroup group = new ElementGroup();
		this.ringSet = this.getRingSet(ac);
		for (IBond bond : ac.bonds()) {
			group.add(this.generate(bond));
		}
		return group;
	}

	public IRenderingElement generate(IBond currentBond) {
		IRing ring = RingSetManipulator.getHeaviestRing(ringSet, currentBond);
		if (ring != null) {
			return generateRingElements(currentBond, ring);
		} else {
			return generateBond(currentBond);
		}
	}
	
	public IRenderingElement generateBondElement(IBond bond) {
	    return this.generateBondElement(bond, bond.getOrder());
	}

	/**
	 * Generate a LineElement or an ElementGroup of LineElements for this bond.
	 * This version should be used if you want to override the type - for
	 * example, for ring double bonds. 
	 * 
	 * @param bond the bond to generate for
	 * @param type the type of the bond - single, double, etc
	 * @return
	 */
	public IRenderingElement generateBondElement(IBond bond, IBond.Order type) {
		// More than 2 atoms per bond not supported by this module
		if (bond.getAtomCount() > 2)
			return null;

		// is object right? if not replace with a good one
		Point2d p1 = bond.getAtom(0).getPoint2d();
		Point2d p2 = bond.getAtom(1).getPoint2d();
		Color color = this.getColorForBond(bond);
		if (type == IBond.Order.SINGLE) {
		    return new LineElement(p1.x, p1.y, p2.x, p2.y, bondWidth, color);
		} else {
    		    double[] in = new double[] { p1.x, p1.y, p2.x, p2.y };
    		    double[] out = GeometryTools.distanceCalculator(in, bondDistance);
    		    ElementGroup group = new ElementGroup();
    		    switch (type) {
    		        case DOUBLE:
    		            LineElement l1 = 
    		                new LineElement(out[0], out[1], out[6], out[7], bondWidth, color);
    		            LineElement l2 = 
    		                new LineElement(out[2], out[3], out[4], out[5], bondWidth, color);
    		            group.add(l1);
    		            group.add(l2);
    		            break;
    		        case TRIPLE:
    		            LineElement l11 = 
    		                new LineElement(out[0], out[1], out[6], out[7], bondWidth, color);
    		            LineElement l22 = 
    		                new LineElement(out[2], out[3], out[4], out[5], bondWidth, color);
    		            group.add(l11);
    		            group.add(l22);
    		            group.add(new LineElement(p1.x, p1.y, p2.x, p2.y, bondWidth, color));
    		            break;
    		        case QUADRUPLE:
    		            double[] out2 = GeometryTools.distanceCalculator(in, bondDistance * 2); 
    		            // TODO
    		        default:
    		            break;
    		    }
    		    return group;
		}

	}

	public IRenderingElement generateRingElements(IBond bond, IRing ring) {
		if (isSingle(bond) && isStereoBond(bond)) {
			return generateStereoElement(bond);
		} else if (isDouble(bond)) {
			ElementGroup pair = new ElementGroup();
			IRenderingElement e1 = generateBondElement(bond, IBond.Order.SINGLE);
			IRenderingElement e2 = generateInnerElement(bond, ring);
			pair.add(e1);
			pair.add(e2);
			return pair;
		} else {
			return generateBondElement(bond);
		}
	}

	private LineElement generateInnerElement(IBond bond, IRing ring) {
		Point2d center = GeometryTools.get2DCenter(ring);
		Point2d a = bond.getAtom(0).getPoint2d();
		Point2d b = bond.getAtom(1).getPoint2d();

		// the proportion to move in towards the ring center
		final double DIST = 0.15;

		Point2d w = new Point2d();
		w.interpolate(a, center, DIST);
		Point2d u = new Point2d();
		u.interpolate(b, center, DIST);

		// XXX : uncomment to make the bonds slightly shorter
		// double alpha = 0.2;
		// Point2d ww = new Point2d();
		// ww.interpolate(w, u, alpha);
		// Point2d uu = new Point2d();
		// uu.interpolate(u, w, alpha);
		// return new BondSymbol(uu.x, uu.y, ww.x, ww.y);
		return new LineElement(u.x, u.y, w.x, w.y, bondWidth, this.getColorForBond(bond));
	}

	private IRenderingElement generateStereoElement(IBond bond) {

		int stereo = bond.getStereo();
		boolean dashed = false;
		Direction dir = Direction.toSecond;
		// if(stereo == STEREO_BOND_UP || stereo == STEREO_BOND_UP_INV)
		// dashed = false;
		if (stereo == STEREO_BOND_DOWN || stereo == STEREO_BOND_DOWN_INV)
			dashed = true;
		if (stereo == STEREO_BOND_DOWN_INV || stereo == STEREO_BOND_UP_INV)
			dir = Direction.toFirst;

		IRenderingElement base = generateBondElement(bond, IBond.Order.SINGLE);
		// XXX
		return new WedgeLineElement((LineElement)base, dashed, dir, this.getColorForBond(bond));
	}

	public boolean isDouble(IBond bond) {
		return bond.getOrder() == IBond.Order.DOUBLE;
	}

	public boolean isSingle(IBond bond) {
		return bond.getOrder() == IBond.Order.SINGLE;
	}

	public boolean isStereoBond(IBond bond) {
		return bond.getStereo() != STEREO_BOND_NONE
				&& bond.getStereo() != STEREO_BOND_UNDEFINED;
	}

	public boolean bindsHydrogen(IBond bond) {
		for (int i = 0; i < bond.getAtomCount(); i++) {
			IAtom atom = bond.getAtom(i);
			if ("H".equals(atom.getSymbol()))
				return true;
		}
		return false;
	}

	public IRenderingElement generateBond(IBond bond) {
		if (!this.model.getShowExplicitHydrogens() && bindsHydrogen(bond)) {
			return null;
		}

		if (isStereoBond(bond)) {
			return generateStereoElement(bond);
		} else {
			return generateBondElement(bond);
		}
	}
	
}
