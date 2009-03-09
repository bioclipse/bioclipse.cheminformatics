/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2008  Gilleain Torrance <gilleain.torrance@gmail.com>
 *
 *  Contact: cdk-devel@list.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.renderer.generators;

import java.awt.Color;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.RenderingParameters.AtomShape;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.RectangleElement;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;

/**
 * @cdk.module render
 */
public class SelectionGenerator implements IGenerator {

    private boolean autoUpdateSelection = true;
    
    public SelectionGenerator() {}

    public IRenderingElement generate(IAtomContainer ac, RendererModel model) {
        Color selectionColor = model.getSelectedPartColor();
        AtomShape shape = model.getSelectionShape();
        IChemObjectSelection selection = model.getSelection();
        
        ElementGroup selectionElements = new ElementGroup();
        if (this.autoUpdateSelection || selection.isFilled()) {
            double r;
            switch(shape) {
                case SQUARE: r = 0.1; break;
                
                case OVAL: default: r = 1; break;
            }
            
            double d = 2 * r;
            IAtomContainer selectedAC = selection.getConnectedAtomContainer();
            if (selectedAC != null) {
                for (IAtom atom : selectedAC.atoms()) {
                    Point2d p = atom.getPoint2d();
                    IRenderingElement element;
                    switch (shape) {
                        case SQUARE:
                            element = 
                                new RectangleElement(
                                    p.x - r, p.y - r, d, d, true, 
                                    selectionColor);
                            break;
                        case OVAL:
                        default:
                            element = new OvalElement(
                                            p.x, p.y, d, false, selectionColor);
                    }
                    selectionElements.add(element);
                }

                for(IBond bond:selectedAC.bonds()) {
                    Point2d p1 = bond.getAtom( 0 ).getPoint2d();
                    Point2d p2 = bond.getAtom( 1 ).getPoint2d();
                    selectionElements.add( new LineElement( p1.x, p1.y,
                                                            p2.x, p2.y, d*60,
                                                            selectionColor));
                }
            }
        }

        if (!selection.isFinished()) {
           selectionElements.add(selection.generate(selectionColor));
        }
        return selectionElements;
    }
}
