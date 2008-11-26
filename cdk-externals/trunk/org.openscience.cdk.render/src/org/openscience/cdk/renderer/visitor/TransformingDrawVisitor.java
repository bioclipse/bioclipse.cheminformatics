/* $Revision$ $Author$ $Date$
*
*  Copyright (C) 2008 Gilleain Torrance <gilleain.torrance@gmail.com>
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

package org.openscience.cdk.renderer.visitor;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.Iterator;

import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.TextElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement;


/**
 * @cdk.module render
 */
public class TransformingDrawVisitor implements IRenderingVisitor {
	
	private final double scale;
	private final double dx;
	private final double dy;
	private final Graphics g;
	
	public TransformingDrawVisitor(Graphics g, double scale, double dx, double dy) {
		this.g = g;
		this.scale = scale;
		this.dx = dx;
		this.dy = dy;
	}
	
	public int inverseTransformX(int x) {
		return -1;
	}
	
	public int inverseTransformY(int y) {
		return -1;
	}
	
	private int transformX(double x) {
		return (int) (this.dx + (this.scale * x));
	}
	
	private int transformY(double y) {
		return (int) (this.dy + (this.scale * y));
	}

	public void visitElementGroup(ElementGroup elementGroup) {
		elementGroup.visitChildren(this);
	}

	public void visitLine(LineElement lineElement) {
		// TODO Auto-generated method stub
		
	}

	public void visitOval(OvalElement ovalElement) {
		// TODO Auto-generated method stub
		
	}

	public void visitText(TextElement textElement) {
		// TODO Auto-generated method stub
		
	}

	public void visitWedge(WedgeLineElement wedgeElement) {
		// TODO Auto-generated method stub
		
	}

	public void visit( IRenderingElement element ) {
      if(element instanceof ElementGroup)
          visit((ElementGroup) element);
      else if(element instanceof LineElement)
          visit((LineElement) element);
      else if(element instanceof OvalElement)
          visit((OvalElement) element);
      else if(element instanceof TextElement)
          visit((TextElement) element);
      else
        System.err.println( "Visitor method for "+element.getClass().getName() 
                            + " is not implemented");
    }
	
public void setTransform( AffineTransform transform ) {

        // TODO Auto-generated method stub     
    }
}
