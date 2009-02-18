package org.openscience.cdk.renderer.generators;

import java.awt.Color;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;

/**
 * @cdk.module render
 */
public class HighlightBondGenerator extends BasicBondGenerator 
                                    implements IGenerator {

    public HighlightBondGenerator() {}
    
    private boolean shouldHighlight(IBond bond, RendererModel model) {
        return !super.bindsHydrogen(bond) || model.getShowExplicitHydrogens();
    }

    public IRenderingElement generate(IAtomContainer ac, RendererModel model) {
        IBond bond = model.getHighlightedBond();
        if (bond != null && shouldHighlight(bond, model)) {
            super.ringSet = super.getRingSet(ac);
            
            double r = model.getHighlightDistance() / model.getScale();
            Color hColor = model.getHoverOverColor();
            Point2d p = bond.get2DCenter();
            boolean filled = model.getHighlightShapeFilled();
            return new OvalElement(p.x, p.y, r, filled, hColor);
        }
        return new ElementGroup();
    }
}
