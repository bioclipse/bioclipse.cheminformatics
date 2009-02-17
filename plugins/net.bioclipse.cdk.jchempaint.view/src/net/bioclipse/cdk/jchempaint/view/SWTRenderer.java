package net.bioclipse.cdk.jchempaint.view;

import static java.lang.Math.round;

import java.awt.geom.AffineTransform;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
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
import org.openscience.cdk.renderer.font.IFontManager;
import org.openscience.cdk.renderer.visitor.IDrawVisitor;

public class SWTRenderer implements IDrawVisitor{

    private Logger logger = Logger.getLogger( SWTRenderer.class );

    private GC gc;
    
    private RendererModel model;
    
    private AffineTransform transform;

    private SWTFontManager fontManager;

    private Map<java.awt.Color, Color> cleanUp;
    
    public SWTRenderer(GC graphics) {
        transform = new AffineTransform();
        this.model = new RendererModel();
        this.gc = graphics;
    }

    public RendererModel getModel() {
        return model;
    }

    private int transformX(double x) {
        return (int) transform( x, 1 )[0];
    }
    
    private int transformY(double y) {
        return (int) transform( 1, y )[1];
    }
    
    private double[] transform(double x, double y) {
        double [] result = new double[2];
        transform.transform( new double[] {x,y}, 0, result, 0, 1 );
        return result;
    }
    
    private int scaleX(double x) {
        return (int) (x*transform.getScaleX());
    }

    private int scaleY(double y) {
        return (int) (y*transform.getScaleY());
    }

    public void render(ElementGroup renderingModel) {
        for (IRenderingElement re : renderingModel) {
           re.accept( this );
        }
    }


    public void visit( OvalElement element ) {
        Color colorOld = gc.getBackground();
        int radius = (int) round(scaleX(element.radius));
        int diameter = (int) round(scaleX(element.radius * 2));
        
        if (element.fill) {
            gc.setBackground(toSWTColor(gc, element.color));

            gc.fillOval(transformX(element.x) - radius,
                        transformY(element.y) - radius,
                        diameter,
                        diameter );
        } else {
            gc.setForeground(toSWTColor(gc, element.color));

            gc.drawOval(transformX(element.x) - radius,
                        transformY(element.y) - radius,
                        diameter,
                        diameter );
        }
        gc.setBackground( colorOld);
    }

    public void visit( LineElement element ) {
        Color colorOld = gc.getBackground();
        int oldLineWidth = gc.getLineWidth();
        // init recursion with background to get the first draw with foreground
        gc.setForeground( toSWTColor( gc, element.color ));
        gc.setLineWidth( (int)element.width );
        drawLine( element );

        gc.setLineWidth( oldLineWidth );
        gc.setBackground( colorOld);
    }

    public void visit( WedgeLineElement element) {
        Color colorOld = gc.getBackground();
        gc.setForeground( getForgroundColor() );
        gc.setBackground( getForgroundColor() );
        //drawWedge( element);
        drawWedge( element );
        gc.setBackground( colorOld );
    }

    private void drawWedge(WedgeLineElement wedge) {
      
        Vector2d normal = 
            new Vector2d(wedge.y1 - wedge.y2, wedge.x2 - wedge.x1);
        normal.normalize();
        normal.scale(model.getWedgeWidth() / model.getScale());
        
        // make the triangle corners
        Point2d vertexA = new Point2d(wedge.x1, wedge.y1);
        Point2d vertexB = new Point2d(wedge.x2, wedge.y2);
        Point2d vertexC = new Point2d(vertexB);
        vertexB.add(normal);
        vertexC.sub(normal);
        
//        gc.setLineWidth( (int) wedge.width );
        if (wedge.isDashed)
            drawDashedWedge( vertexA, vertexB, vertexC);
        else
            drawFilledWedge(vertexA, vertexB, vertexC);

    }
    
    private void drawFilledWedge(Point2d pA, Point2d pB, Point2d pC) {
        double[] a = transform(pA.x, pA.y);
        double[] b = transform(pB.x, pB.y);
        double[] c = transform(pC.x, pC.y);
        
        Path path = new Path(gc.getDevice());
        path.moveTo((float) a[0], (float) a[1]);
        path.lineTo((float) b[0], (float) b[1]);
        path.lineTo((float) c[0], (float) c[1]);
        path.close();

        gc.fillPath( path );

        path.dispose();
    }
    
    private void drawDashedWedge(Point2d pA, Point2d pB, Point2d pC) {
        // calculate the distances between lines
        double distance = pB.distance(pA);
        double gapFactor = 0.075;
        double gap = distance * gapFactor;
        double numberOfDashes = distance / gap;
        double d = 0;
        
        // draw by interpolating along the edges of the triangle
        Path path = new Path(gc.getDevice());
        for (int i = 0; i < numberOfDashes; i++) {
            Point2d p1 = new Point2d();
            p1.interpolate(pA, pB, d);
            Point2d p2 = new Point2d();
            p2.interpolate(pA, pC, d);
            double[] p1T = transform(p1.x, p1.y);
            double[] p2T = transform(p2.x, p2.y);
            path.moveTo((float)p1T[0], (float)p1T[1]);
            path.lineTo((float)p2T[0], (float)p2T[1]);
            if (distance * (d + gapFactor) >= distance) {
                break;
            } else {
                d += gapFactor;
            }
        }
        gc.drawPath( path);
        path.dispose();
    }

    private Color getForgroundColor() {
        return toSWTColor( gc, getModel().getForeColor() );
    }

    private Color getBackgroundColor() {
        return toSWTColor( gc, getModel().getBackColor() );
    }

    private void drawLine(LineElement element) {
        Path path = new Path(gc.getDevice());
        double[] p1=transform( element.x1, element.y1 );
        double[] p2=transform( element.x2, element.y2);
        path.moveTo( (float)p1[0], (float)p1[1] );
        path.lineTo( (float)p2[0], (float)p2[1] );
       gc.drawPath( path );
       path.dispose();
    }

    private Font getFont() {

        return fontManager.getFont();
    }

    private Font getSmallFont() {

        return fontManager.getSmallFont();
    }

    public void visit( TextElement element ) {

        int x = transformX(element.x);
        int y = transformY(element.y);
        String text = element.text;

        gc.setFont(getFont());

        Point textSize = gc.textExtent( text );
        x = x - textSize.x/2;
        y = y - textSize.y/2;
        gc.setForeground( toSWTColor( gc, element.color ) );
        gc.setBackground(  getBackgroundColor() );
        gc.setAdvanced( true );
        gc.drawText( text, x, y, true );
    }

    public void visit(AtomSymbolElement element) {
        int x = transformX( element.x );
        int y = transformY( element.y);

        String text = element.text;

        gc.setFont(getFont());

        Point textSize = gc.textExtent( text );
        x = x - textSize.x/2;
        y = y - textSize.y/2;
        gc.setForeground( toSWTColor( gc, element.color ) );
        gc.setBackground(  getBackgroundColor() );
        gc.setAdvanced( true );
        gc.drawText( text, x, y, false );

        Point secondTextSize = gc.textExtent( "H" );
        gc.setFont( getSmallFont() );
        Point cp = new Point(0,0);
        if(element.formalCharge!=0) {
            String fc = Integer.toString( element.formalCharge);
            fc = (element.formalCharge==1?"+":fc);
            fc = (element.formalCharge>1?"+"+fc:fc);
            fc = (element.formalCharge==-1?"-":fc);
            cp = gc.textExtent( fc );
            int fcX = x+textSize.x;
            int fcY = y-cp.y/2;
            gc.drawText( fc, fcX, fcY, true );
        }

        if(element.hydrogenCount >0) {

            Point hc = new Point(0,0);
            if(element.hydrogenCount >1) {
                hc = gc.textExtent( Integer.toString( element.hydrogenCount ));
            }
            switch(element.alignment) {
                case -1: x = x -secondTextSize.x - hc.x;break;
                case 1:  x = x + textSize.x+cp.x;break;
                case -2: y = y + textSize.y;break;
                case 2:  y = y+cp.y/2 - Math.max( secondTextSize.y,secondTextSize.y/2 - hc.y);break;
            }
            if(element.hydrogenCount >1) {
                gc.drawText( Integer.toString( element.hydrogenCount),
                             x + secondTextSize.x, y + secondTextSize.y/2
                             ,true);
            }
            gc.setFont(getFont());
            gc.drawText( "H", x, y ,false);

        }
    }
    
    public Color toSWTColor(GC graphics,java.awt.Color color) {
        if (cleanUp == null) {
            cleanUp = new HashMap<java.awt.Color,Color>();
        }
        
        if (color == null) {
            return graphics.getDevice().getSystemColor(SWT.COLOR_MAGENTA);
        }
        
        assert(color != null);
        Color otherColor = cleanUp.get(color);
        if (otherColor == null) {
            otherColor = new Color(graphics.getDevice(),
                                   color.getRed(),
                                   color.getGreen(),
                                   color.getBlue());
            cleanUp.put(color,otherColor);
        }
        return otherColor;
    }

    public void dispose() {
        for (Color c : cleanUp.values()) {
            c.dispose();
        }
        cleanUp.clear();
    }

    public void visit( ElementGroup elementGroup ) {
        for (IRenderingElement element : elementGroup) {
            element.accept(this);
        }
    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }

    public void visitDefault(IRenderingElement element) {
        logger.debug("No visitor method implemented for : "
                + element.getClass());
    }

    public void visit(RectangleElement element) {

        if (element.filled) {
            gc.setBackground(toSWTColor(gc, element.color));
            gc.fillRectangle(
                    transformX(element.x), transformY(element.y),
                    scaleX(element.width), scaleY(element.height));
        } else {
            gc.setForeground(toSWTColor(gc, element.color));
            gc.drawRectangle(
                    transformX(element.x), transformY(element.y),
                    scaleX(element.width), scaleY(element.height));
        }
    }

    public void visit(PathElement element) {
        gc.setForeground(toSWTColor(gc, element.color));
        Path path = new Path(gc.getDevice());
        boolean first = true;
        for(Point2d p: element.points) {
            double[] tp = transform( p.x, p.y );

            if(first) {
                path.moveTo( (float)tp[0], (float)tp[1]);
                first = false;
            } else {
                path.lineTo( (float)tp[0], (float)tp[1]);
            }
        }
        gc.drawPath( path );
        path.dispose();
    }

    public void visit(IRenderingElement element)  {

        Method method = getMethod( element );
        if (method == null) {
            visitDefault(element);
        }
        else {
            try {
                method.invoke( this, new Object[] {element} );
            } catch ( IllegalArgumentException e ) {
                visitDefault( element );
            } catch ( IllegalAccessException e ) {
                visitDefault( element );
            } catch ( InvocationTargetException e ) {
                visitDefault( element );
            }
        }
    }
    
    private Method getMethod( IRenderingElement element ) {

        Class<?> cl = element.getClass();
        while ( !cl.equals( Object.class ) ) {
            try {
                return this.getClass().getDeclaredMethod( "visit",
                                                          new Class[] { cl } );

            } catch ( NoSuchMethodException e ) {
                cl = cl.getSuperclass();
            }
        }

        return null;
    }

    public void setFontManager( IFontManager fontManager ) {
        this.fontManager = (SWTFontManager) fontManager;
    }

    public void setRendererModel( RendererModel rendererModel ) {
        this.model = rendererModel;
    }
}
