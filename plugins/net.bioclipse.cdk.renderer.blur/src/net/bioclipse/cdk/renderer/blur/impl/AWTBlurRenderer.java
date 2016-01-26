package net.bioclipse.cdk.renderer.blur.impl;

import static net.bioclipse.cdk.renderer.blur.impl.BoundsVisitor.bounds;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

import net.bioclipse.cdk.jchempaint.rendering.Renderer;
import net.bioclipse.cdk.renderer.blur.BlurRenderingElement;

import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;

import com.jhlabs.image.GaussianFilter;

public class AWTBlurRenderer implements Renderer<Graphics2D> {

    @Override
    public void visit( Graphics2D gc,
                       AffineTransform transform,
                       RendererModel model,
                       IRenderingElement element ) {

        assert (element instanceof BlurRenderingElement);
        renderBlur( gc, transform, model, (BlurRenderingElement) element );

    }

    private void renderBlur( Graphics2D originalGC,
                             AffineTransform transform,
                             RendererModel model,
                             BlurRenderingElement element ) {

        Rectangle2D bounds = transform.createTransformedShape( bounds(transform,element.getNode()) ).getBounds2D();
        AffineTransform af = calculateBounds( element, transform );
        BufferedImage image = new BufferedImage( (int)bounds.getWidth(), (int)bounds.getHeight(), BufferedImage.TYPE_INT_ARGB );
        {
            Graphics2D gc = image.createGraphics();
            AffineTransform tr = new AffineTransform();
            tr.translate( -bounds.getX(), -bounds.getY() );
            gc.setTransform( tr );

            AWTDrawVisitor renderer = new AWTDrawVisitor( gc );
            renderer.setRendererModel( model );
            renderer.setTransform( transform );
            renderer.visit( element.getNode() );
            gc.dispose();
            int stencilSize = 9;// element.getStencilSize() * 2;

//            float[] kernel = new float[(stencilSize / 3) * (stencilSize / 3)];
//            Arrays.fill( kernel, 1f );
            int kernelSize = 15;
            float[] matrix = new float[kernelSize * kernelSize];
            Arrays.fill( matrix, 0.111f );

             float[] kernel = { 0.111f,0.111f,0.111f,
                                0.111f,0.111f,0.111f,
                                0.111f,0.111f,0.111f};
//                         {
//             0.00f, 0.10f, 0.00f,
//             0.10f, 0.50f, 0.10f,
//             0.00f, 0.10f, 0.00f };
            ConvolveOp op = new ConvolveOp( new Kernel( kernelSize, kernelSize, matrix ), ConvolveOp.EDGE_ZERO_FILL,
                null );
            BufferedImage blured = op.filter( image, null );
            // blured = op.filter( blured, null );
            // blured = op.filter( blured, null );

            // originalGC.drawImage( blured, (int) bounds.getX(),
            // (int) bounds.getY(), (int) bounds.getWidth(),
            // (int) bounds.getHeight(), null );
            BufferedImageOp bufferedOp = new GaussianFilter( 17 );
            originalGC.drawImage( image, bufferedOp, (int) bounds.getX(), (int) bounds.getY() );
        }
        
    }

    @Override
    public boolean accepts( IRenderingElement element ) {

        return element instanceof BlurRenderingElement;
    }

    AffineTransform calculateBounds( BlurRenderingElement source,
                                     AffineTransform transform ) {

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
        return af;
    }
}
