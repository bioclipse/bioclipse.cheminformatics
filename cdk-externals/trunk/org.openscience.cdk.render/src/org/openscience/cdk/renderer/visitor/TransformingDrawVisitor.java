package org.openscience.cdk.renderer.visitor;

import java.awt.Graphics;
import java.awt.geom.Line2D;
import java.util.Iterator;

import org.openscience.cdk.renderer.elements.ElementGroup;
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

}
