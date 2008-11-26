package org.openscience.cdk.renderer.generators;

import java.awt.Color;

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
public class HighlightGenerator implements IGenerator {
	
	private Renderer2DModel model;
	private Color highlightColor;


	public HighlightGenerator( Renderer2DModel r2dm) {
		this.model= r2dm;
		this.highlightColor = Color.GRAY;
	}
	

	public IRenderingElement generate(IAtomContainer ac, IAtom atom) {
		IAtom highlightedAtom = this.model.getHighlightedAtom(); 
		if (highlightedAtom != null && highlightedAtom.equals(atom)) {
			Point2d p = atom.getPoint2d();
			return new RingElement(p.x, p.y, this.model.getAtomRadius(), this.highlightColor);
		}
		return null;
	}


	public IRenderingElement generate(IAtomContainer ac, IBond bond) {
		IBond highlightedBond = this.model.getHighlightedBond();
		if (highlightedBond != null && highlightedBond.equals(bond)) {
			Point2d p1 = bond.getAtom(0).getPoint2d();
			Point2d p2 = bond.getAtom(1).getPoint2d();
			return new LineElement(p1.x, 
								   p1.y,
								   p2.x,
								   p2.y,
								   LineType.SINGLE,
								   this.model.getBondWidth() * 3,
								   this.model.getBondDistance(),
								   this.highlightColor);
		}
		return null;
	}

    public IRenderingElement generate(IAtomContainer ac) {

        ElementGroup elementGroup = new ElementGroup();
        for (IAtom atom : ac.atoms()) {
          elementGroup.add(this.generate(ac, atom));
        }
        for (IBond bond: ac.bonds()) {
            elementGroup.add(this.generate(ac, bond));
        }
        return elementGroup;
      }
        
    
}
