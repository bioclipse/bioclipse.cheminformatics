package org.openscience.cdk.renderer;

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

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.TextElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement;
import org.openscience.cdk.renderer.generators.BasicGenerator;
import org.openscience.cdk.renderer.generators.HighlightGenerator;

/**
 * @cdk.module render
 */
public class IntermediateRenderer implements IJava2DRenderer, IRenderingVisitor {
	
	private AWTFontManager fontManager;
	private Renderer2DModel rendererModel;
	private BasicGenerator generator;
	
	private transient Graphics2D g;
	
	private Stroke stroke;
	private AffineTransform transform;
	
	public static final double NATURAL_SCALE = 30.0;
	
	private double scale;
	
	public IntermediateRenderer() {
		this.rendererModel = new Renderer2DModel();
		this.rendererModel.setHighlightRadiusModel(3.5);
		this.generator = new BasicGenerator(this.rendererModel);
		this.stroke = new BasicStroke(2);
		this.fontManager = new AWTFontManager();
		this.transform = new AffineTransform();
	}

	public Renderer2DModel getRenderer2DModel() {
		return this.rendererModel;
	}

	public void paintMolecule(IAtomContainer atomCon, Graphics2D g) {
	    // don't think that this method should be used... 
	}

	public void paintMolecule(IAtomContainer atomCon, Graphics2D g, Rectangle2D bounds) {
		if (atomCon.getAtomCount() == 0) return;	// XXX
		Rectangle2D atomicBounds = this.calculateBounds(atomCon);
		
		IRenderingElement diagram = this.generator.generate(atomCon);
		IRenderingElement highlights 
			= new HighlightGenerator(this.rendererModel).generate(atomCon);
		((ElementGroup) diagram).add(highlights);
		
		// FIXME
		double toFitScale = this.calculateScale(atomicBounds, bounds); 
		if (toFitScale > IntermediateRenderer.NATURAL_SCALE) {
		    this.scale = IntermediateRenderer.NATURAL_SCALE;
		} else {
    		this.scale = toFitScale; 
    		this.scale *= 0.8;	// provide a border
		}
		
		this.transform = new AffineTransform();
		this.transform.translate(bounds.getCenterX(), bounds.getCenterY());
		this.transform.scale(scale, scale);
		this.transform.translate(-atomicBounds.getCenterX(), -atomicBounds.getCenterY());

		
		double h = bounds.getHeight();
		double fractionalScale = (1 - h / (h * this.scale));
		this.fontManager.setFontForScale(fractionalScale);
		
		g.setStroke(this.stroke);
		this.g = g;
//		diagram.accept(new org.openscience.cdk.renderer.visitor.PrintVisitor());
		diagram.accept(this);
	}
	
	/**
	 * Calculates the multiplication factor that will fit the model completely within
	 * the display bounds (not accounting for a border).
	 * 
	 * @param modelBounds
	 * @param displayBounds
	 * @return
	 */
	private double calculateScale(Rectangle2D modelBounds, Rectangle2D displayBounds) {
		double widthRatio = displayBounds.getWidth() / modelBounds.getWidth();
		double heightRatio = displayBounds.getHeight() / modelBounds.getHeight();

		// the area is contained completely within the target
		if (widthRatio > 1 && heightRatio > 1) {
			return Math.min(widthRatio, heightRatio);
		}

		// the area is wider than the target, but fits the height
		if (widthRatio < 1 && heightRatio > 1) {
			return widthRatio;
		}

		// the area is taller than the target, but fits the width
		if (widthRatio > 1 && heightRatio < 1) {
			return heightRatio;
		}

		// the target is completely contained by the area
		if (widthRatio > 1 && heightRatio > 1) {
			return heightRatio;
		}

		// the bounds are equal
		return widthRatio;	// could return either...
	}
	
	private Rectangle2D calculateBounds(IAtomContainer ac) {
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

	public void setRenderer2DModel(Renderer2DModel model) {
		this.rendererModel = model;
	}

	public Point2d getCoorFromScreen(int screenX, int screenY) {
	    try {
	        Point2D t = new Point2D.Double(screenX, screenY);
	        this.transform.inverseTransform(t, t);
	        return new Point2d(t.getX(), t.getY());
	    } catch (NoninvertibleTransformException n) {
	        return new Point2d(0,0);
	    }
	}

	private Point2D transformPoint(Point2D source) {
	    return this.transform.transform(source, null);
	}

	public void visit(ElementGroup elementGroup) {
		elementGroup.visitChildren(this);
	}

	public void visit(LineElement line) {
		this.g.setColor(line.color);
		
		switch (line.type) {
		    case SINGLE:
		        Point2D a = this.transformPoint(new Point2D.Double(line.x1, line.y1));
		        Point2D b = this.transformPoint(new Point2D.Double(line.x2, line.y2));
		        this.g.drawLine((int)a.getX(), (int)a.getY(), (int)b.getX(), (int)b.getY());
		        break;
		    case DOUBLE:
		        double[] inDo = new double[] { line.x1, line.y1, line.x2, line.y2 };
		        double[] outD = GeometryTools.distanceCalculator(inDo, line.gap/3);   //XXX
		        
		        Point2D s1 = this.transformPoint(new Point2D.Double(outD[0], outD[1]));
		        Point2D e1 = this.transformPoint(new Point2D.Double(outD[6], outD[7]));
		        Point2D s2 = this.transformPoint(new Point2D.Double(outD[2], outD[3]));
                Point2D e2 = this.transformPoint(new Point2D.Double(outD[4], outD[5]));
                
		        this.g.drawLine((int)s1.getX(), (int)s1.getY(), (int)e1.getX(), (int)e1.getY());
		        this.g.drawLine((int)s2.getX(), (int)s2.getY(), (int)e2.getX(), (int)e2.getY());
		        break;
		    case TRIPLE:
		        double[] inTr = new double[] { line.x1, line.y1, line.x2, line.y2 };
		        double[] outT = GeometryTools.distanceCalculator(inTr, line.gap/2);//XXX
		        Point2D s1T = this.transformPoint(new Point2D.Double(outT[0], outT[1]));
		        Point2D e1T = this.transformPoint(new Point2D.Double(outT[6], outT[7]));
		        Point2D s2T = this.transformPoint(new Point2D.Double(line.x1, line.y1));
		        Point2D e2T = this.transformPoint(new Point2D.Double(line.x2, line.y2));
		        Point2D s3T = this.transformPoint(new Point2D.Double(outT[2], outT[3]));
		        Point2D e3T = this.transformPoint(new Point2D.Double(outT[4], outT[5]));

		        this.g.drawLine((int)s1T.getX(), (int)s1T.getY(), (int)e1T.getX(), (int)e1T.getY());
		        this.g.drawLine((int)s2T.getX(), (int)s2T.getY(), (int)e2T.getX(), (int)e2T.getY());
		        this.g.drawLine((int)s3T.getX(), (int)s3T.getY(), (int)e3T.getX(), (int)e3T.getY());
		        break;
		    case QUADRUPLE:
		        double[] inQ = new double[] { line.x1, line.y1, line.x2, line.y2 };
		        double[] outQ1 = GeometryTools.distanceCalculator(inQ, line.gap/3);//XXX
		        double[] outQ2 = GeometryTools.distanceCalculator(inQ, line.gap/2); //XXX
		        Point2D s1Q = this.transformPoint(new Point2D.Double(outQ1[0], outQ1[1]));
                Point2D e1Q = this.transformPoint(new Point2D.Double(outQ1[6], outQ1[7]));
                Point2D s2Q = this.transformPoint(new Point2D.Double(outQ2[0], outQ2[1]));
                Point2D e2Q = this.transformPoint(new Point2D.Double(outQ2[6], outQ2[7]));
                Point2D s3Q = this.transformPoint(new Point2D.Double(outQ2[2], outQ2[3]));
                Point2D e3Q = this.transformPoint(new Point2D.Double(outQ2[4], outQ2[5]));
                Point2D s4Q = this.transformPoint(new Point2D.Double(outQ1[2], outQ1[3]));
                Point2D e4Q = this.transformPoint(new Point2D.Double(outQ1[4], outQ1[5]));

                this.g.drawLine((int)s1Q.getX(), (int)s1Q.getY(), (int)e1Q.getX(), (int)e1Q.getY());
                this.g.drawLine((int)s2Q.getX(), (int)s2Q.getY(), (int)e2Q.getX(), (int)e2Q.getY());
                this.g.drawLine((int)s3Q.getX(), (int)s3Q.getY(), (int)e3Q.getX(), (int)e3Q.getY());
                this.g.drawLine((int)s4Q.getX(), (int)s4Q.getY(), (int)e4Q.getX(), (int)e4Q.getY());
		    default:
		        break;
		}
	}

	public void visit(OvalElement oval) {
		int r = (int) oval.radius;
		int d = 2 * r;
		this.g.setColor(oval.color);
		Point2D p = this.transformPoint(new Point2D.Double(oval.x, oval.y));
//		this.g.drawOval(tX(oval.x) - r, tY(oval.y) - r, d, d);
		this.g.drawOval((int)p.getX() - r, (int)p.getY() - r, d, d);
	}
	
	private Rectangle2D getTextBounds(TextElement text, Graphics2D g) {
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D bounds = fm.getStringBounds(text.text, g);
		
		double widthPad = 3;
		double heightPad = 1;
		
		double w = bounds.getWidth() + widthPad;
		double h = bounds.getHeight() + heightPad;
		Point2D p = this.transformPoint(new Point2D.Double(text.x, text.y));
//		return new Rectangle2D.Double(tX(text.x) - w / 2, tY(text.y) - h / 2, w, h);
		return new Rectangle2D.Double(p.getX() - w / 2, p.getY() - h / 2, w, h);
	}
	
	private Point getTextBasePoint(TextElement textElement, Graphics2D g) {
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D stringBounds = fm.getStringBounds(textElement.text, g);
		Point2D p = this.transformPoint(new Point2D.Double(textElement.x, textElement.y));
//		int baseX = (int) (tX(textElement.x) - (stringBounds.getWidth() / 2));
		int baseX = (int) (p.getX() - (stringBounds.getWidth() / 2));
		
		// correct the baseline by the ascent
//		int baseY = (int) (tY(textElement.y) + 
//				(fm.getAscent() - stringBounds.getHeight() / 2));
		int baseY = (int) (p.getY() + 
              (fm.getAscent() - stringBounds.getHeight() / 2));
		return new Point(baseX, baseY);
	}

	public void visit(TextElement textElement) {
		this.g.setFont(this.fontManager.getFont());
		Point p = this.getTextBasePoint(textElement, g);
		Rectangle2D textBounds = this.getTextBounds(textElement, g);
		
		this.g.setColor(this.rendererModel.getBackColor());
		this.g.fill(textBounds);
		this.g.setColor(textElement.color);
		this.g.drawString(textElement.text, p.x, p.y);
	}

	public void visit(WedgeLineElement wedge) {
	    // TODO : FIXME
	    Point2D a = this.transformPoint(new Point2D.Double(wedge.x1, wedge.y1));
        Point2D b = this.transformPoint(new Point2D.Double(wedge.x2, wedge.y2));
        this.g.drawLine((int)a.getX(), (int)a.getY(), (int)b.getX(), (int)b.getY());
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
	
	public void setTransform(AffineTransform transform) {
	    this.transform = transform;
    }
}
