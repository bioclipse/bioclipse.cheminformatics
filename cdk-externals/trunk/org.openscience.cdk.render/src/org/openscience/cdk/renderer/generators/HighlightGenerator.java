package org.openscience.cdk.renderer.generators;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.RingElement;
import org.openscience.cdk.renderer.elements.LineElement.LineType;

/**
 * @cdk.module render
 */
public class HighlightGenerator implements IGenerator{
	
	private Renderer2DModel model;
	private Color highlightColor;


	public HighlightGenerator( Renderer2DModel r2dm) {
		this.model= r2dm;
		this.highlightColor = Color.GRAY;
	}
	

	public IRenderingElement generate(IAtomContainer ac, IAtom atom,Point2d c) {
		IAtom highlightedAtom = this.model.getHighlightedAtom(); 
		if (highlightedAtom != null && highlightedAtom.equals(atom)) {
			Point2d p = atom.getPoint2d();
			return new RingElement(p.x-c.x, p.y-c.y, this.model.getAtomRadius(), this.highlightColor);
		}
		return null;
	}


	public IRenderingElement generate(IAtomContainer ac,IBond bond,Point2d c) {
		IBond highlightedBond = this.model.getHighlightedBond();
		if (highlightedBond != null && highlightedBond.equals(bond)) {
			// i would like to create a bond element like it would be crated
			// and to some stuff with it
			Point2d p1 = bond.getAtom(0).getPoint2d();
			Point2d p2 = bond.getAtom(1).getPoint2d();
			return new LineElement(p1.x - c.x, 
								   p1.y - c.y,
								   p2.x - c.x,
								   p2.y - c.y,

								   LineType.SINGLE,
								   this.model.getBondWidth() * 3,
								   this.model.getBondDistance(),
								   this.highlightColor);
		}
		return null;
	}

    public IRenderingElement generate( IAtomContainer ac, Point2d center ) {

        ElementGroup elementGroup = new ElementGroup();
        for (IAtom atom : ac.atoms()) {
          elementGroup.add(this.generate(ac, atom, center));
        }
        for (IBond bond: ac.bonds()) {
            elementGroup.add(this.generate( ac, bond, center ) );
        }
        return elementGroup;
      }
        
    
}
