package org.openscience.cdk.renderer.generators;

import java.awt.Color;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;

/**
 * @cdk.module render
 */
public class HighlightGenerator extends BasicBondGenerator 
                                implements IGenerator {

    public HighlightGenerator() {}

    public IRenderingElement generate(
            IAtomContainer ac, IAtom atom, RendererModel model) {
        IAtom highlightedAtom = atom;
        if (highlightedAtom != null) {
            Point2d p = atom.getPoint2d();
            
            // the element size has to be scaled to model space 
            // so that it can be scaled back to screen space...
            double radius = model.getHighlightDistance() / model.getScale();
            return new OvalElement(p.x, p.y, radius, model.getHoverOverColor());
        }
        return null;
    }

    public IRenderingElement generate(
            IAtomContainer ac, IBond bond, RendererModel model) {
        if (bond != null) {
            super.ringSet = super.getRingSet(ac);
            
            double r = model.getHighlightDistance() / model.getScale();
            Color hColor = model.getHoverOverColor();
            Point2d p = bond.get2DCenter();
            return new OvalElement(p.x, p.y, r, hColor);
        } else {
            return new ElementGroup();
        }
    }

    public IRenderingElement generate(IAtomContainer ac, RendererModel model) {
        super.setOverrideColor(model.getHoverOverColor());
        ElementGroup elementGroup = new ElementGroup();
        IAtom atom = model.getHighlightedAtom();
        if (atom != null) {
            elementGroup.add(this.generate(ac, atom, model));
        }
        
        IBond bond = model.getHighlightedBond();
        if (bond != null) {
            elementGroup.add(this.generate(ac, bond, model));
        }
        return elementGroup;
    }
}
