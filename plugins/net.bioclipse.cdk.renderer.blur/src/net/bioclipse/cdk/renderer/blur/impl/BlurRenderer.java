package net.bioclipse.cdk.renderer.blur.impl;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import net.bioclipse.cdk.jchempaint.rendering.Renderer;
import net.bioclipse.cdk.jchempaint.view.SWTRenderer;
import net.bioclipse.cdk.renderer.blur.BlurRenderingElement;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.elements.MarkedElement;
import org.openscience.cdk.renderer.elements.OvalElement;

public class BlurRenderer implements Renderer{
	
	@Override
	public boolean accepts(IRenderingElement element) {
		return element instanceof BlurRenderingElement;
	}
	
	@Override
	public void visit(GC gc, AffineTransform transform, RendererModel model,
			IRenderingElement element) {
		assert( element instanceof BlurRenderingElement);
		renderBlur(gc, transform, model, (BlurRenderingElement)element);
		
	}
	static void renderBlur(GC originalGC,AffineTransform transform,RendererModel model,
			BlurRenderingElement source) {

        int stencilSize = source.getStencilSize();

        Rectangle2D preBounds = bounds( transform, source.getNode() );
        Rectangle2D bounds = transform.createTransformedShape( preBounds )
                        .getBounds2D();
        double margin = stencilSize * 2;
        bounds.setFrame( bounds.getX() - margin, bounds.getY() - margin,
                         bounds.getWidth() + margin * 2,
                         bounds.getHeight() + margin * 2 );
        AffineTransform af = new AffineTransform( transform );
        af.translate( -bounds.getX(), -bounds.getY() );
		Image image = new Image(Display.getDefault(),(int)bounds.getWidth(),(int)bounds.getHeight());
	    GC gc = new GC(image);
	    Transform tr = new Transform( gc.getDevice() );
        tr.translate( (int) -bounds.getX(), (int) -bounds.getY() );
        gc.setTransform( tr );

		SWTRenderer renderer = new SWTRenderer(gc);
		renderer.setRendererModel(model);
        renderer.setTransform( transform );
        renderer.visit( source.getNode() );
        // gc.setBackground( new Color( Display.getDefault(), 255, 0, 0 ) );
        // gc.fillRectangle( -10, -10, 20, 20 );
        // gc.setBackground( new Color( Display.getDefault(), 0, 0, 255 ) );
        // gc.fillRectangle( (int) bounds.getX(), (int) bounds.getY(), 20, 20 );
		gc.dispose();
        ImageData data = image.getImageData();
        ImageData blured = Blur.blur( data, stencilSize );
        Image finalImage = new Image( originalGC.getDevice(), blured );
		originalGC.drawImage(finalImage, (int)bounds.getX(), (int)bounds.getY());
        // originalGC.drawRectangle((int)bounds.getX(),(int) bounds.getY(),(int)
        // bounds.getWidth(),(int) bounds.getHeight());
	}
	
    static Rectangle2D bounds( AffineTransform transform, ElementGroup group ) {
		BoundsVisitor visitor = new BoundsVisitor();
        // visitor.setTransform( transform );
		group.visitChildren(visitor);
		return visitor.getBounds();
	}
	
	static class BoundsVisitor implements IRenderingVisitor {
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
            else if ( element instanceof MarkedElement ) {
                visit( ((MarkedElement) element).element() );
            }
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
	}
}
