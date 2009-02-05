/**
 *  Copyright (C) 2001-2007  The Chemistry Development Kit (CDK) Project
 *
 *  Contact: cdk-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package org.openscience.cdk.renderer.visitor;

import java.awt.Color;
import java.awt.geom.AffineTransform;

import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.AtomSymbolElement;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.PathElement;
import org.openscience.cdk.renderer.elements.RectangleElement;
import org.openscience.cdk.renderer.elements.TextElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement;
import org.openscience.cdk.renderer.font.IFontManager;

/**
 * @author maclean
 * @cdk.module render
 */
public class SVGGenerator implements IDrawVisitor {

	public static final String HEADER = "<?xml version=\"1.0\"?>\n" +
			"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n" +
			"\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
			"<svg xmlns=\"http://www.w3.org/2000/svg\" " +
			"width=\"1000\" height=\"600\">";

	private final StringBuffer svg = new StringBuffer();

	private AffineTransform transform;

	public SVGGenerator() {
		svg.append(SVGGenerator.HEADER);
	}

	private void newline() {
		svg.append("\n");
	}

	public String getResult() {
		newline();
		svg.append("</svg>");
		return svg.toString();
	}

	public int[] transformPoint(double x, double y) {
        double[] src = new double[] {x, y};
        double[] dest = new double[2];
        this.transform.transform(src, 0, dest, 0, 1);
        return new int[] { (int) dest[0], (int) dest[1] };
    }

	public void setTransform(AffineTransform transform) {
		this.transform = transform;
	}

	public void visit(ElementGroup group) {
		group.visitChildren(this);
	}

	public void visit(WedgeLineElement wedge) {

	}

	public void visit(PathElement path) {

	}

	public void visit(LineElement line) {
		newline();

		int[] p1 = transformPoint(line.x1, line.y1);
		int[] p2 = transformPoint(line.x2, line.y2);
		svg.append(String.format(
					"<line x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\" " +
					"style=\"stroke:black; stroke-width:1px;\" />",
					p1[0],
					p1[1],
					p2[0],
					p2[1]
					));
	}

	public void visit(OvalElement oval) {
		newline();
		int[] p1 = transformPoint(oval.x - oval.radius, oval.y - oval.radius);
		int[] p2 = transformPoint(oval.x + oval.radius, oval.y + oval.radius);
		int r = (p2[0] - p1[0]) / 2;
		svg.append(String.format(
				"<ellipse cx=\"%s\" cy=\"%s\" rx=\"%s\" ry=\"%s\" " +
				"style=\"stroke:black; stroke-width:1px; fill:none;\" />",
				p1[0] + r, p1[1] + r, r, r));
	}

	public void visit(AtomSymbolElement atomSymbol) {
		newline();
		int[] p = transformPoint(atomSymbol.x, atomSymbol.y);
		svg.append(String.format(
				"<text x=\"%s\" y=\"%s\" style=\"fill:%s\"" +
				">%s</text>",
				p[0],
				p[1],
				toColorString(atomSymbol.color),
				atomSymbol.text
				));
	}

	// this is a stupid method, but no idea how else to do it...
	private String toColorString(Color color) {
		if (color == Color.RED) {
			return "red";
		} else if (color == Color.BLUE) {
			return "blue";
		} else {
			return "black";
		}
	}

	public void visit(TextElement textElement) {
		newline();
		int[] p = transformPoint(textElement.x, textElement.y);
		svg.append(String.format(
				"<text x=\"%s\" y=\"%s\">%s</text>",
				p[0],
				p[1],
				textElement.text
				));
	}

	public void visit(RectangleElement rectangleElement) {

	}

	public void visit(IRenderingElement element) {
		if (element instanceof ElementGroup)
			visit((ElementGroup) element);
		else if (element instanceof WedgeLineElement)
			visit((WedgeLineElement) element);
		else if (element instanceof LineElement)
			visit((LineElement) element);
		else if (element instanceof OvalElement)
			visit((OvalElement) element);
		else if (element instanceof AtomSymbolElement)
			visit((AtomSymbolElement) element);
		else if (element instanceof TextElement)
			visit((TextElement) element);
		else if (element instanceof RectangleElement)
			visit((RectangleElement) element);
		else if (element instanceof PathElement)
			visit((PathElement) element);
		else
			System.err.println("Visitor method for "
					+ element.getClass().getName() + " is not implemented");
	}

    public void setFontManager(IFontManager fontManager) {
        // FIXME : what kind of font management does SVG really need?
    }

    public void setRendererModel(RendererModel rendererModel) {
        // TODO Auto-generated method stub
        
    }

}
