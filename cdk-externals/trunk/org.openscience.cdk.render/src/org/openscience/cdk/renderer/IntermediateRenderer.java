package org.openscience.cdk.renderer;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;

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
	private double scale;
	private double dx;
	private double dy;
	
	public IntermediateRenderer() {
		this.rendererModel = new Renderer2DModel();
		this.generator = new BasicGenerator(this.rendererModel);
		this.stroke = new BasicStroke(2);
		this.fontManager = new AWTFontManager();
	}

	public Renderer2DModel getRenderer2DModel() {
		return this.rendererModel;
	}

	public void paintMolecule(IAtomContainer atomCon, Graphics2D g) {
		// TODO : cache this and recreate only when necessary.
		IRenderingElement diagram = this.generator.generate(atomCon);
		this.g = g;
		diagram.accept(this);
	}

	Point2d tmpHack;
	public void paintMolecule(IAtomContainer atomCon, Graphics2D g, Rectangle2D bounds) {
		Rectangle2D atomicBounds = this.calculateBounds(atomCon);
		tmpHack = new Point2d(atomicBounds.getCenterX(), atomicBounds.getCenterY());
		
		IRenderingElement diagram = this.generator.generate(atomCon);
		IRenderingElement highlights 
			= new HighlightGenerator(this.rendererModel).generate(atomCon, tmpHack);
		((ElementGroup) diagram).add(highlights);
		this.dx = bounds.getCenterX();
		this.dy = bounds.getCenterY();
		
		// FIXME
		this.scale = this.calculateScale(atomicBounds, bounds);
		this.scale *= 0.8;	// provide a border
		double h = bounds.getHeight();
		double fractionalScale = (1 - h / (h * this.scale));
		System.err.println("fscale = " + fractionalScale + " scale = "
				+ this.scale + " dx =" + this.dx + " dy = " + this.dy);
		this.fontManager.setFontForScale(fractionalScale);
		
		g.setStroke(this.stroke);
		this.g = g;
		diagram.accept(new org.openscience.cdk.renderer.visitor.PrintVisitor());
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
		return new Point2d(invTX(screenX), invTY(screenY));
	}
	
	private double invTX(int x) {
		return (1.0 / this.scale) * (x - this.dx) + this.tmpHack.x;
	}
	
	private double invTY(int y) {
		return (1.0 / this.scale) * (y - this.dy) + this.tmpHack.y;
	}
	
	private int tX(double x) {
		return (int) (this.dx + (this.scale * x));
	}
	
	private int tY(double y) {
		return (int) (this.dy + (this.scale * y));
	}

	public void visitElementGroup(ElementGroup elementGroup) {
		elementGroup.visitChildren(this);
	}

	public void visitLine(LineElement line) {
		this.g.setColor(line.color);
		this.g.drawLine(tX(line.x1), tY(line.y1), tX(line.x2), tY(line.y2));
	}

	public void visitOval(OvalElement oval) {
		int r = (int) oval.radius;
		int d = 2 * r;
		this.g.setColor(oval.color);
		this.g.drawOval(tX(oval.x) - r, tY(oval.y) - r, d, d);
	}
	
	private Rectangle2D getTextBounds(TextElement text, Graphics2D g) {
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D bounds = fm.getStringBounds(text.text, g);
		
		double widthPad = 3;
		double heightPad = 1;
		
		double w = bounds.getWidth() + widthPad;
		double h = bounds.getHeight() + heightPad;
		return new Rectangle2D.Double(tX(text.x) - w / 2, tY(text.y) - h / 2, w, h);
	}
	
	private Point getTextBasePoint(TextElement textElement, Graphics2D g) {
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D stringBounds = fm.getStringBounds(textElement.text, g);
		int baseX = (int) (tX(textElement.x) - (stringBounds.getWidth() / 2));
		
		// correct the baseline by the ascent
		int baseY = (int) (tY(textElement.y) + 
				(fm.getAscent() - stringBounds.getHeight() / 2));
		return new Point(baseX, baseY);
	}

	public void visitText(TextElement textElement) {
		this.g.setFont(this.fontManager.getFont());
		Point p = this.getTextBasePoint(textElement, g);
		Rectangle2D textBounds = this.getTextBounds(textElement, g);
		
		this.g.setColor(this.rendererModel.getBackColor());
		this.g.fill(textBounds);
		this.g.setColor(textElement.color);
		this.g.drawString(textElement.text, p.x, p.y);
	}

	public void visitWedge(WedgeLineElement wedgeElement) {
		// TODO Auto-generated method stub
		
	}

}
