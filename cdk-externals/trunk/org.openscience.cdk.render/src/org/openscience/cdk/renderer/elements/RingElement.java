package org.openscience.cdk.renderer.elements;

import java.awt.Color;

import javax.vecmath.Point2d;


public class RingElement extends OvalElement implements IRenderingElement {
    
    public RingElement(Point2d center,double radius,Color color) {
        super(center.x,center.y,radius,color,false);        
    }

    public void accept( IRenderingVisitor v ) {

        v.visitOval( this );

    }

}
