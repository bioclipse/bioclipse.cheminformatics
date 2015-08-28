package net.bioclipse.cdk.renderer.blur.impl;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.elements.OvalElement;

public class BoundsVisitor implements IRenderingVisitor {
		Rectangle2D bounds = null;
        AffineTransform transform = null;

        public void setTransform( java.awt.geom.AffineTransform transform ) {

            this.transform = transform;
        };

        @Override
        public void visit( IRenderingElement element ) {

            if ( element instanceof OvalElement )
                visit( (OvalElement) element );
            else if ( element instanceof ElementGroup )
                visit( (ElementGroup) element );
        }
		public void visit(ElementGroup element) {
            element.visitChildren( this );
		}
		public void visit(OvalElement element) {

            double r = element.radius;
            Shape oval = new Ellipse2D.Double( element.xCoord - r,
                element.yCoord - r, r * 2, r * 2 );
            if ( transform != null ) {
                oval = transform.createTransformedShape( oval );
            }
			if(bounds!=null) 
				bounds.add(oval.getBounds2D());
			else bounds = oval.getBounds2D();
		}
		
		public Rectangle2D getBounds() {
			return bounds;
		}

        static Rectangle2D bounds( AffineTransform transform, ElementGroup group ) {
        	BoundsVisitor visitor = new BoundsVisitor();
            // visitor.setTransform( transform );
        	group.visitChildren(visitor);
        	return visitor.getBounds();
        }
	}