package org.openscience.cdk.renderer.elements;

import java.awt.Color;

/**
 * @cdk.module render
 */
public class RingElement extends OvalElement implements IRenderingElement {

	public RingElement(double x, double y, double radius, Color color) {
		super(x, y, radius, false, color);
	}

	public void accept(IRenderingVisitor v) {
		v.visit(this);
	}

}
