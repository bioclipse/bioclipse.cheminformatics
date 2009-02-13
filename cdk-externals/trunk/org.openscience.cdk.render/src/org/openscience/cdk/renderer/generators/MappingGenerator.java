package org.openscience.cdk.renderer.generators;

import java.awt.Color;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMapping;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.LineElement;

/**
 * @cdk.module render
 */
public class MappingGenerator implements IReactionGenerator {

    public MappingGenerator() {}

    public IRenderingElement generate(IReaction reaction, RendererModel model) {
        ElementGroup elementGroup = new ElementGroup();
        Color mappingColor = model.getAtomAtomMappingLineColor();
        for (IMapping mapping : reaction.mappings()) {
            // XXX assume that there are only 2 endpoints!
            // XXX assume that the ChemObjects are actually IAtoms...
            IAtom endPointA = (IAtom) mapping.getChemObject(0);
            IAtom endPointB = (IAtom) mapping.getChemObject(1);
            Point2d pA = endPointA.getPoint2d();
            Point2d pB = endPointB.getPoint2d();
            elementGroup.add(
                    new LineElement(pA.x, pA.y, pB.x, pB.y, 1, mappingColor));
        }
        return elementGroup;
    }
}
