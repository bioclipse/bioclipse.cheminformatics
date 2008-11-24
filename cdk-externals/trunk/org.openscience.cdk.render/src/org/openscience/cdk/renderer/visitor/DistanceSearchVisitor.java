package org.openscience.cdk.renderer.visitor;

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
public class DistanceSearchVisitor implements IRenderingVisitor {
	
	private int x;
	private int y;
	private double searchRadiusSQ;
	private double closestDistanceSQ;
	public IRenderingElement bestHit;
	
	public DistanceSearchVisitor(int x, int y, double searchRadius) {
		this.x = x;
		this.y = y;
		this.searchRadiusSQ = searchRadius * searchRadius;
		this.bestHit = null;
		this.closestDistanceSQ = -1;
	}
	
	private void check(IRenderingElement element, double xx, double yy) {
		double dSQ = (this.x - xx) * (this.x - xx) + (this.y - yy) * (this.y - yy);
		if (dSQ < this.searchRadiusSQ && 
				(this.closestDistanceSQ == -1 || dSQ < this.closestDistanceSQ)) {
			this.bestHit = element;
			this.closestDistanceSQ = dSQ;
		}
	}

	public void visitElementGroup(ElementGroup elementGroup) {
		elementGroup.visitChildren(this);
	}

	public void visitLine(LineElement lineElement) {
		// FIXME
		int xx = (int)(0.5 * (lineElement.x1 - lineElement.x2));
		int yy = (int)(0.5 * (lineElement.y1 - lineElement.y2));
		this.check(lineElement, xx, yy);
	}

	public void visitOval(OvalElement ovalElement) {
		this.check(ovalElement, ovalElement.x, ovalElement.y);
	}

	public void visitText(TextElement textElement) {
		this.check(textElement, textElement.x, textElement.y);
	}

	public void visitWedge(WedgeLineElement wedgeElement) {
		// TODO
	}

}
