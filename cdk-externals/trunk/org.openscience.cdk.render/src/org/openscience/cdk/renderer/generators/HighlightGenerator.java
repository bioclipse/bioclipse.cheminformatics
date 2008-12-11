package org.openscience.cdk.renderer.generators;

import java.awt.Color;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.OvalElement;

/**
 * @cdk.module render
 */
public class HighlightGenerator implements IGenerator {
	
	private RendererModel model;
	private Color highlightColor;
	private double highlightRadius;  

	public HighlightGenerator(RendererModel model) {
		this.model = model;
		this.highlightRadius = model.getHighlightRadiusModel();
		this.highlightColor = model.getHoverOverColor();
	}

	public IRenderingElement generate(IAtomContainer ac, IAtom atom) {
		IAtom highlightedAtom = atom;
		if (highlightedAtom != null) {
			Point2d p = atom.getPoint2d();
			return new OvalElement(p.x, p.y, highlightRadius, highlightColor);
		}
		return null;
	}

	public IRenderingElement generate(IAtomContainer ac, IBond bond) {
		IBond highlightedBond = bond;
		if (highlightedBond != null ) {
			Point2d p1 = bond.getAtom(0).getPoint2d();
			Point2d p2 = bond.getAtom(1).getPoint2d();
			double w = this.model.getBondWidth() * 3;
			return new LineElement(p1.x, p1.y, p2.x, p2.y, w, highlightColor);
		}
		return null;
	}

    public IRenderingElement generate(IAtomContainer ac) {
        ElementGroup elementGroup = new ElementGroup();
        elementGroup.add(this.generate(ac, this.model.getHighlightedAtom()));
        elementGroup.add(this.generate(ac, this.model.getHighlightedBond()));
        return elementGroup;
    }
}
