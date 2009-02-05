package org.openscience.cdk.renderer.generators;

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

    public HighlightGenerator(RendererModel model) {
        super(model);
        super.setOverrideColor(model.getHoverOverColor());
    }

    public IRenderingElement generate(IAtomContainer ac, IAtom atom) {
        IAtom highlightedAtom = atom;
        if (highlightedAtom != null) {
            Point2d p = atom.getPoint2d();
            return new OvalElement(
                    p.x, p.y, 
                    model.getHighlightRadiusModel(), model.getHoverOverColor());
        }
        return null;
    }

    public IRenderingElement generate(IAtomContainer ac, IBond bond) {
        if (bond != null) {
            super.ringSet = super.getRingSet(ac);
            return super.generate(bond);
        } else {
            return new ElementGroup();
        }
    }

    public IRenderingElement generate(IAtomContainer ac) {
        ElementGroup elementGroup = new ElementGroup();
        elementGroup.add(this.generate(ac, model.getHighlightedAtom()));
        elementGroup.add(this.generate(ac, model.getHighlightedBond()));
        return elementGroup;
    }
    
    @Override
    public void setRendererModel( RendererModel model ) {
        super.setRendererModel( model );
    }
}
