/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.generators;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Point2d;

import net.bioclipse.cdk.domain.ISubStructure;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.generators.IGenerator;


/**
 * @author arvid
 *
 */
public class SubStructureGenerator implements IGenerator {
    static final Color DEFAULT_COLOR = Color.MAGENTA;

    Set<ISubStructure> subStructures = new HashSet<ISubStructure>();
    /* (non-Javadoc)
     * @see org.openscience.cdk.renderer.generators.IGenerator#generate(org.openscience.cdk.interfaces.IAtomContainer, org.openscience.cdk.renderer.RendererModel)
     */
    public IRenderingElement generate( IAtomContainer ac, RendererModel model ) {
        double r = model.getHighlightDistance() / model.getScale();
        ElementGroup group = new ElementGroup();
        
        for(ISubStructure subStructure:subStructures) {
            ElementGroup subGroup = new ElementGroup();
            if(subStructure.getAtomContainer()==null) continue;
            for(IAtom atom:subStructure.getAtomContainer().atoms()) {
                Point2d p = atom.getPoint2d();
                Color color = subStructure.getHighlightingColor( atom );
                subGroup.add( generateElement( p, r,  
                                             color!=null?color:DEFAULT_COLOR) );
            }
            group.add(subGroup);
        }
        return group;
    }
    
    private IRenderingElement generateElement(Point2d p,double r, Color color) {
        return new OvalElement(p.x, p.y, r, color);
    }

    public void clear() {
        subStructures.clear();
    }
    
    public void add(ISubStructure subStructure) {
        subStructures.add( subStructure );
    }
}
