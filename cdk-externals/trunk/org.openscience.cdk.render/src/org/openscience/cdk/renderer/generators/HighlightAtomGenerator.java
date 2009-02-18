package org.openscience.cdk.renderer.generators;


import java.awt.Color;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;

/**
 * @cdk.module render
 */
public class HighlightAtomGenerator extends BasicAtomGenerator 
                                implements IGenerator {

    public HighlightAtomGenerator() {}
    
    private boolean shouldHighlight(IAtom atom, RendererModel model) {
        return !super.isHydrogen(atom) || model.getShowExplicitHydrogens();
    }

    public IRenderingElement generate(IAtomContainer ac, RendererModel model) {
        IAtom atom = model.getHighlightedAtom();
        if (atom != null && shouldHighlight(atom, model)) {
            Point2d p = atom.getPoint2d();
            
            // the element size has to be scaled to model space 
            // so that it can be scaled back to screen space...
            double radius = model.getHighlightDistance() / model.getScale();
            boolean filled = model.getHighlightShapeFilled();
            Color highlightColor = model.getHoverOverColor(); 
            return new OvalElement(p.x, p.y, radius, filled, highlightColor);
        }
        
        return new ElementGroup();
    }
}
