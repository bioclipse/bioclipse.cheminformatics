package org.openscience.cdk.renderer.generators;


import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.RectangleElement;


/**
 * Produce a bounding rectangle for various chem objects.
 * 
 * @author maclean
 * @cdk.module render
 */
public class BoundsGenerator {
    
    public BoundsGenerator() {}
    
    public IRenderingElement generate(IReaction reaction, RendererModel model) {
        ElementGroup elementGroup = new ElementGroup();
        IMoleculeSet reactants = reaction.getReactants();
        if (reactants != null) {
            elementGroup.add(this.generate(reactants, model));
        }
        
        IMoleculeSet products = reaction.getProducts();
        if (products != null) {
            elementGroup.add(this.generate(products, model));
        }
        
        return elementGroup;
    }
    
    public IRenderingElement generate(IMolecule molecule, RendererModel model) {
        Rectangle2D bounds = this.calculateBounds(molecule);
        return new RectangleElement(bounds.getMinX(),
                bounds.getMinY(),
                bounds.getMaxX(),
                bounds.getMaxY(),
                model.getBoundsColor());
    }
    
    public IRenderingElement generate(
            IMoleculeSet moleculeSet, RendererModel model) {
        Rectangle2D totalBounds = null;
        for (IAtomContainer molecule : moleculeSet.molecules()) {
            Rectangle2D bounds = this.calculateBounds(molecule);
            if (totalBounds == null) {
                totalBounds = bounds;
            } else {
                totalBounds = totalBounds.createUnion(bounds);
            }
        }
        if (totalBounds == null) return null;
        
        return new RectangleElement(totalBounds.getMinX(),
                                    totalBounds.getMinY(),
                                    totalBounds.getMaxX(),
                                    totalBounds.getMaxY(),
                                    model.getBoundsColor());
    }
    
    private Rectangle2D calculateBounds(IAtomContainer ac) {
        // this is essential, otherwise a rectangle
        // of (+INF, -INF, +INF, -INF) is returned!
        if (ac.getAtomCount() == 0) {
            return new Rectangle2D.Double();
        } else if (ac.getAtomCount() == 1) {
            Point2d p = ac.getAtom(0).getPoint2d();
            return new Rectangle2D.Double(p.x, p.y, 0, 0);
        }
    
        double xmin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
    
        for (IAtom atom : ac.atoms()) {
            Point2d p = atom.getPoint2d();
            xmin = Math.min(xmin, p.x);
            xmax = Math.max(xmax, p.x);
            ymin = Math.min(ymin, p.y);
            ymax = Math.max(ymax, p.y);
        }
        double w = xmax - xmin;
        double h = ymax - ymin;
        return new Rectangle2D.Double(xmin, ymin, w, h);
    }

}
