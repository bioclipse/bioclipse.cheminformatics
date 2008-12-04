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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.renderer.AWTFontManager;
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


/**
 * @cdk.module render
 */
public class TransformingDrawVisitor extends AbstractAWTRenderingVisitor {
	
	private final Graphics2D g;
	private final AWTFontManager fontManager;
	private final RendererModel rendererModel;
	
	public TransformingDrawVisitor(Graphics2D g, AffineTransform transform,
	        AWTFontManager fontManager, RendererModel rendererModel) {
	    super(transform);
		this.g = g;
		this.fontManager = fontManager;
		this.rendererModel = rendererModel;
	}
	
	public void visitElementGroup(ElementGroup elementGroup) {
		elementGroup.visitChildren(this);
	}
	
    public void visit(ElementGroup elementGroup) {
        elementGroup.visitChildren(this);
    }

    public void visit(LineElement line) {
        this.g.setColor(line.color);
        int[] a = this.transformPoint(line.x1, line.y1);
        int[] b = this.transformPoint(line.x2, line.y2);
        this.g.drawLine(a[0], a[1], b[0], b[1]);
    }

    public void visit(OvalElement oval) {
        this.g.setColor(oval.color);
        int[] min = this.transformPoint(oval.x - oval.radius, oval.y - oval.radius);
        int[] max = this.transformPoint(oval.x + oval.radius, oval.y + oval.radius);
        int w = max[0] - min[0];
        int h = max[1] - min[1];
        this.g.drawOval(min[0], min[1], w, h);
    }

    public void visit(TextElement textElement) {
        this.g.setFont(this.fontManager.getFont());
        Point p = this.getTextBasePoint(textElement.text, textElement.x, textElement.y, g);
        Rectangle2D textBounds =
                this.getTextBounds(textElement.text, textElement.x, textElement.y, g);
        
        this.g.setColor(this.rendererModel.getBackColor());
        this.g.fill(textBounds);
        this.g.setColor(textElement.color);
        this.g.drawString(textElement.text, p.x, p.y);
    }
    
    public void visit(WedgeLineElement wedge) {
        if (wedge.isDashed) {
            this.drawDashedWedge(wedge);
        } else {
            this.drawFilledWedge(wedge);
        }
    }
    
    private void drawFilledWedge(WedgeLineElement wedge) {
        Vector2d normal = new Vector2d(wedge.y1 - wedge.y2, wedge.x2 - wedge.x1);
        normal.normalize();
        normal.scale(0.2);
        
        Point2d c;
        Point2d e;
        e = new Point2d(wedge.x1, wedge.y1);
        c = new Point2d(wedge.x2, wedge.y2);
        
        Point2d d = new Point2d(c);
        c.add(normal);
        d.sub(normal);    
        
        int[] pc = this.transformPoint(c.x, c.y);
        int[] pd = this.transformPoint(d.x, d.y);
        int[] pe = this.transformPoint(e.x, e.y);
        
        this.g.setColor(wedge.color);
        int[] xs = new int[] { pc[0], pd[0], pe[0] };
        int[] ys = new int[] { pc[1], pd[1], pe[1] };
        this.g.fillPolygon(xs, ys, 3);
    }
    
    private void drawDashedWedge(WedgeLineElement wedge) {
        Point2d apex = new Point2d(wedge.x1, wedge.y1);
        Point2d centerBase = new Point2d(wedge.x2, wedge.y2);
        Vector2d normal = new Vector2d(wedge.y1 - wedge.y2, wedge.x2 - wedge.x1);
        normal.normalize();
        Stroke storedStroke = this.g.getStroke();
        this.g.setStroke(new BasicStroke(1));
        double distance = centerBase.distance(apex);
        double gapFactor = 0.1;
        double gap = distance * gapFactor;
        double numberOfDashes = distance / gap;
        
        double shorteningFactor = 0.02;
        double currentShortening = 0.25;
        
        double d = 0;
        for (int i = 0; i < numberOfDashes; i++) {
            Point2d c = new Point2d(centerBase);
            c.interpolate(centerBase, apex, d);
            Point2d p1 = new Point2d(c);
            Point2d p2 = new Point2d(c);
            Vector2d n = new Vector2d(normal);
            n.scale(currentShortening);
            p1.add(n);
            p2.sub(n);
            int[] p1T = this.transformPoint(p1.x, p1.y);
            int[] p2T = this.transformPoint(p2.x, p2.y);
            this.g.drawLine(p1T[0], p1T[1], p2T[0], p2T[1]);
            if (currentShortening - shorteningFactor < 0) {
                continue;
            } else {
                currentShortening -= shorteningFactor;
            }
            if (distance * (d + gapFactor) >= distance) {
                break;
            } else {
                d += gapFactor;
            }
        }
        this.g.setStroke(storedStroke);
    }
    
    public void visit(AtomSymbolElement atomSymbol) {
        this.g.setFont(this.fontManager.getFont());
        Point p = super.getTextBasePoint(atomSymbol.text, atomSymbol.x, atomSymbol.y, g);
        Rectangle2D textBounds = 
            this.getTextBounds(atomSymbol.text, atomSymbol.x, atomSymbol.y, g);
        this.g.setColor(this.rendererModel.getBackColor());
        this.g.fill(textBounds);
        this.g.setColor(atomSymbol.color);
        this.g.drawString(atomSymbol.text, p.x, p.y);
        
        int offset = 10;    // XXX
        String chargeString;
        if (atomSymbol.formalCharge == 0) {
            return;
        } else if (atomSymbol.formalCharge == 1) {
            chargeString = "+";
        } else if (atomSymbol.formalCharge > 1) {
            chargeString = atomSymbol.formalCharge + "+";
        } else if (atomSymbol.formalCharge == -1) {
            chargeString = "-";
        } else if (atomSymbol.formalCharge < -1) {
            int absCharge = Math.abs(atomSymbol.formalCharge);
            chargeString = absCharge + "-";
        } else {
            return;
        }
       
        int x = (int) textBounds.getCenterX();
        int y = (int) textBounds.getCenterY();
        if (atomSymbol.alignment == 1) {           // RIGHT
            this.g.drawString(chargeString, x + offset, (int)textBounds.getMinY());
        } else if (atomSymbol.alignment == -1) {   // LEFT
            this.g.drawString(chargeString, x - offset, (int)textBounds.getMinY());
        } else if (atomSymbol.alignment == 2) {    // TOP
            this.g.drawString(chargeString, x, y - offset);
        } else if (atomSymbol.alignment == -2) {   // BOT
            this.g.drawString(chargeString, x, y + offset);
        }
    }
    
    public void visit(RectangleElement rectangle) {
        int[] p1 = this.transformPoint(rectangle.x, rectangle.y);
        int[] p2 = this.transformPoint(
                rectangle.x + rectangle.width, rectangle.y + rectangle.height);
        this.g.setColor(rectangle.color);
        if (rectangle.filled) {
            this.g.fillRect(p1[0], p1[1], p2[0] - p1[0], p2[1] - p1[1]);
        } else {
            this.g.drawRect(p1[0], p1[1], p2[0] - p1[0], p2[1] - p1[1]);
        }
    }
    
    public void visit(PathElement path) {
        this.g.setColor(path.color);
        for (int i = 1; i < path.points.size(); i++) {
            Point2d point1 = path.points.get(i - 1);
            Point2d point2 = path.points.get(i);
            int[] p1 = this.transformPoint(point1.x, point1.y);
            int[] p2 = this.transformPoint(point2.x, point2.y);
            this.g.drawLine(p1[0], p1[1], p2[0], p2[1]);
        }
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
}
