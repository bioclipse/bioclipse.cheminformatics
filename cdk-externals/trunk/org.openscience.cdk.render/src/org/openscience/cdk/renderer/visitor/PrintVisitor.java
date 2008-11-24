/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2008  The Chemistry Development Kit (CDK) project
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

import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.TextElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement;


/**
 * @cdk.module render
 */
public class PrintVisitor implements IRenderingVisitor {
	
	private int depth; 
	
	public PrintVisitor() {
		this.depth = 0;
	}

	public void visitElementGroup(ElementGroup elementGroup) {
		this.depth += 1;
		elementGroup.visitChildren(this);
		this.depth -= 1;
	}

	public void visitLine(LineElement lineElement) {
		System.out.println("line" + lineElement.x1 + " " + lineElement.y1 + " "
				+ lineElement.x2 + " " + lineElement.y2);
	}

	public void visitOval(OvalElement ovalElement) {
		System.out.println("oval" + ovalElement.x + "," + ovalElement.y + " " + ovalElement.radius);
	}

	public void visitText(TextElement textElement) {
		System.out.println("text");
	}

	public void visitWedge(WedgeLineElement wedgeElement) {
		System.out.println("wedge");
	}


}
