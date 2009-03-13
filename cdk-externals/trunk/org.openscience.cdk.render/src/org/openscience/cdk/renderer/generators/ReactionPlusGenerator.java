/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2009  Stefan Kuhn
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

import java.awt.geom.Rectangle2D;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.TextElement;

/**
 * Generate the arrow for a reaction.
 * 
 * @author maclean
 * @cdk.module render
 *
 */
public class ReactionPlusGenerator  implements IReactionGenerator {

	public IRenderingElement generate(IReaction reaction, RendererModel model) {
		ElementGroup diagram = new ElementGroup();
        Rectangle2D totalBoundsReactants = null;
        for (IAtomContainer molecule : reaction.getReactants().molecules()) {
            Rectangle2D bounds = BoundsGenerator.calculateBounds(molecule);
            if (totalBoundsReactants == null) {
            	totalBoundsReactants = bounds;
            } else {
            	totalBoundsReactants = totalBoundsReactants.createUnion(bounds);
            }
        }
        for(int i=0;i<reaction.getReactantCount()-1;i++){
        	Rectangle2D bounds1 = BoundsGenerator.calculateBounds(reaction.getReactants().getAtomContainer(i));
        	Rectangle2D bounds2 = BoundsGenerator.calculateBounds(reaction.getReactants().getAtomContainer(i+1));
        	TextElement text= new TextElement((bounds1.getCenterX()+bounds2.getCenterX())/2,totalBoundsReactants.getCenterY(),"+",model.getForeColor());
        	diagram.add(text);
        }
        Rectangle2D totalBoundsProducts = null;
        for (IAtomContainer molecule : reaction.getProducts().molecules()) {
            Rectangle2D bounds = BoundsGenerator.calculateBounds(molecule);
            if (totalBoundsProducts == null) {
            	totalBoundsProducts = bounds;
            } else {
            	totalBoundsProducts = totalBoundsProducts.createUnion(bounds);
            }
        }
        for(int i=0;i<reaction.getProductCount()-1;i++){
        	Rectangle2D bounds1 = BoundsGenerator.calculateBounds(reaction.getProducts().getAtomContainer(i));
        	Rectangle2D bounds2 = BoundsGenerator.calculateBounds(reaction.getProducts().getAtomContainer(i+1));
        	TextElement text= new TextElement((bounds1.getCenterX()+bounds2.getCenterX())/2,totalBoundsProducts.getCenterY(),"+",model.getForeColor());
        	diagram.add(text);
        }
        return diagram;
	}
}
