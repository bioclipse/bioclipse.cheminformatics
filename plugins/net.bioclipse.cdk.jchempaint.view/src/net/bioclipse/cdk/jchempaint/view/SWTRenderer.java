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
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.PathElement;
import org.openscience.cdk.renderer.elements.RectangleElement;
import org.openscience.cdk.renderer.elements.TextElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement;

public class SWTRenderer implements IRenderingVisitor{

    Logger logger = Logger.getLogger( SWTRenderer.class );

    GC gc;
    RendererModel model;
    AffineTransform transform;

    SWTFontManager fontManager;

    // scale a lite more and translate the differense to center it
    // dosen't handle zoom
    public SWTRenderer(GC graphics, SWTFontManager fontManager, RendererModel model) {
        this.transform = new AffineTransform();
        this.model = model;
        this.gc = graphics;
        this.fontManager = fontManager;

    }

    private Map<java.awt.Color, Color> cleanUp;

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
        for(IRenderingElement re:renderingModel ) {
           re.accept( this );
        }
    }


    public void visit( OvalElement element ) {
        Color colorOld = gc.getBackground();
//        int radius = (int) (scaleX(element.getRadius())+.5);
//        int radius_2 = (int) (scaleX(element.getRadius())/2.0+.5);
        int radius = (int)round( scaleX( element.radius));
        int radius_2 = (int)round( scaleX(element.radius/2d ));
        if(element.fill) {
        gc.setBackground( toSWTColor( gc, element.color ) );

        gc.fillOval( transformX( element.x)-radius_2,
                     transformY(element.y)-radius_2,
                     radius,
                     radius );
        } else {
            gc.setForeground(  toSWTColor( gc, element.color ) );

            gc.drawOval( transformX(element.x)-radius_2,
                         transformY(element.y)-radius_2,
                         radius,
                         radius );
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

    private void drawWedge(WedgeLineElement element) {
        double width = element.width;
        Point2d p1 = new Point2d( transformX(element.x1),
                                  transformY(element.y1));
        Point2d p2 = new Point2d( transformX(element.x2),
                                  transformY(element.y2));
        Vector2d p12 = new Vector2d(p2);p12.sub( p1 );
        Vector2d v12n = new Vector2d(p12.y,-p12.x); // normal for p12
        v12n.normalize();
        //   wedge thickness is based on line width probably better to be based
        //  on text size
        double l = width*4/2;
        Vector2d pa = new Vector2d(v12n);pa.scale( l );
        Vector2d pb = new Vector2d(v12n);pb.scale(-l);
        v12n.scale( l);

        Point2d p1a = new Point2d();p1a.add( p1, v12n );
        Point2d p1b = new Point2d();p1b.sub( p1, v12n );

        gc.setLineWidth( (int) width );
        if(element.isDashed)
            drawDashedWedge( p1a, p1b, p2, width );
        else
            drawFilledWedge( p1a, p1b, p2);

    }
    private void drawFilledWedge( Point2d p1a, Point2d p1b,
                                  Point2d p2) {
        Path path = new Path(gc.getDevice());
        path.moveTo( (float)p2.x,(float) p2.y );
        path.lineTo( (float)(p1a.x), (float)(p1a.y) );
        path.lineTo( (float)(p1b.x), (float)(p1b.y) );
        path.close();

        gc.fillPath( path );

        path.dispose();
    }
    private void drawDashedWedge( Point2d p1a, Point2d p1b,
                                  Point2d p2, double d) {

        double s = (d*2);
        double t = s / p1a.distance( p2 );
        double t2 = p1a.distance( p2 )/ s;
        Path dashes = new Path(gc.getDevice());
        for(int i=0 ;i<t2;i++) {
            Point2d w = new Point2d();
            w.interpolate( p1a, p2, t*i);
            Point2d u = new Point2d();
            u.interpolate( p1b,p2, t*i );
            dashes.moveTo( (float) (w.x), (float) (w.y) );
            dashes.lineTo( (float) (u.x), (float) (u.y) );
        }

        gc.drawPath( dashes );
        dashes.dispose();
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
//        int fontSize = (int) (scaleX(.4));
//        fontSize = (fontSize<12?12:fontSize);
//        fontSize = (fontSize>100?100:fontSize);
//        return  new Font(gc.getDevice(),"Arial",fontSize,SWT.NORMAL);
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
    public  Color toSWTColor(GC graphics,java.awt.Color color) {
        if(cleanUp == null)
            cleanUp = new HashMap<java.awt.Color,Color>();
        if(color == null) return graphics.getDevice().getSystemColor( SWT.COLOR_MAGENTA );
        assert(color != null);
        Color otherColor=cleanUp.get(color);
        if(otherColor==null){
            otherColor = new Color(graphics.getDevice(),
                                   color.getRed(),
                                   color.getGreen(),
                                   color.getBlue());
            cleanUp.put(color,otherColor);
        }
        return otherColor;
    }

    public void dispose() {
        for(Color c:cleanUp.values())
            c.dispose();
          cleanUp.clear();
    }

    public void visit( ElementGroup elementGroup ) {

        for(IRenderingElement element:elementGroup) {
            element.accept( this );
        }

    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
        fontManager.setFontForScale( transform.getScaleX() );
    }

    public void render() {

        // TODO Auto-generated method stub

    }

    public void visitDefault(IRenderingElement element) {
//        throw new RuntimeException( "visitor for "+element.getClass().getName()
//                                    + " is not implemented yet.");
        logger.debug( "No visitor method implemented for : "+element.getClass() );

    }

//    public void visit( AtomSymbolElement element ) {
//
//        // TODO Auto-generated method stub
//
//    }

    public void visit( RectangleElement element ) {

        if(element.filled) {
            gc.setBackground( toSWTColor( gc, element.color ) );
            gc.fillRectangle( transformX(element.x), transformY(element.y),
                              scaleX(element.width), scaleY(element.height ));
        } else {
            gc.setForeground( toSWTColor( gc, element.color ) );
            gc.drawRectangle( transformX(element.x), transformY(element.y),
                              scaleX(element.width), scaleY(element.height ));
        }
    }

    public void visit( PathElement element) {
        gc.setForeground( toSWTColor( gc,element.color) );
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
        if(method == null) {
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
//        Class<?>[] interfaces = element.getClass().getInterfaces();
//        for ( Class<?> c : interfaces ) {
//            try {
//                return this.getClass().getDeclaredMethod( "visit",
//                                                          new Class[] { c } );
//            } catch ( NoSuchMethodException e ) {
//                // try with the next interface
//            }
//        }
        return null;
    }
}
