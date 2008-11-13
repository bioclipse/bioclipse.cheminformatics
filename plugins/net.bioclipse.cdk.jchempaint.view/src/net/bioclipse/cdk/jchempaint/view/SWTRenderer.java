package net.bioclipse.cdk.jchempaint.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.RenderingModel;
import org.openscience.cdk.renderer.elements.TextElement;


public class SWTRenderer implements IRenderingVisitor{

    GC gc;
    double scaleX = 1;
    double scaleY = 1;
    Renderer2DModel model;
    
    // scale a lite more and translate the differense to center it
    // dosen't handle zoom
    public SWTRenderer(GC graphics, Renderer2DModel model, double[] scale) {
        scaleX = Math.min( scale[0], scale[1] );
        scaleY = scaleX;
        this.model = model;
        gc = graphics;
        
    }
    
    private Map<java.awt.Color, Color> cleanUp;
    
    public Renderer2DModel getModel() {
        return model;
    }
    
    private int scaleX(double x) {
        return scale(x, scaleX);
    }
    
    private int scaleY(double y) {
        return -scale( y, scaleY );
    }
    
    private int scale(double v,double f) {
        return (int)(v*f+.5);
    }
    
    public void render(RenderingModel renderingModel) {
        for(IRenderingElement re:renderingModel ) {
           re.accept( this );
        }
    }
    

    public void visitOval( OvalElement element ) {
        Color colorOld = gc.getBackground();
        gc.setBackground( toSWTColor( gc, element.getColor() ) );
        int radius = (int) (element.getRadius()+.5);
        int radius_2 = (int) (element.getRadius()/2.0+.5);
        gc.fillOval( scaleX(element.getX())-radius_2, 
                     scaleY(element.getY())-radius_2, 
                     radius,
                     radius );   
        gc.setBackground( colorOld);
    }

    public void visitLine( LineElement element ) {
        Color colorOld = gc.getBackground();
        // init recursion with background to get the first draw with foreground
        gc.setForeground( getBackgroundColor() ); 
        drawLineX( element, element.type().count() );
            
        gc.setBackground( colorOld);
    }
    
    private Color getForgroundColor() {
        return toSWTColor( gc, getModel().getForeColor() );
    }
    
    private Color getBackgroundColor() {
        return toSWTColor( gc, getModel().getBackColor() );
    }

    private void drawLine(LineElement element) {
        gc.drawLine( scaleX(element.getX()),
                     scaleY(element.getY()),
                     scaleX(element.getX1()),
                     scaleY(element.getY1()));
    }
    private void drawSingleLine( LineElement element) {
        int width1 = (int) (element.getWidth()*1+element.getGap()*0+.5);
        gc.setForeground( getForgroundColor() );
        gc.setLineWidth( width1 );
        drawLine(element);
    }
    private void drawDoubleLine( LineElement element ) {
        int width1 = (int) (element.getWidth()*2+element.getGap()*1+.5);
        int width2 = (int) (element.getWidth()*1+element.getGap()*0+.5);
        gc.setForeground( getForgroundColor() );
        gc.setLineWidth( width1);
        drawLine(element);
        gc.setLineWidth( width2 );
        gc.setForeground( getBackgroundColor() );
        drawLine( element );
    }

    private void drawLineX(LineElement element, int val) {
        if(val <= 0) return; // end recursion if less than 1
        int width = (int) (element.getWidth()*val+element.getGap()*(val-1)+.5);
        if(gc.getForeground().equals( getForgroundColor() ))
            gc.setForeground( getBackgroundColor() );
        else
            gc.setForeground( getForgroundColor() );
        gc.setLineWidth( width );
        drawLine(element);
        
        drawLineX(element, val-1);
    }
    private void drawTripleLine( LineElement element ) {        
        int width1 = (int) (element.getWidth()*3+element.getGap()*2+.5);
        int width2 = (int) (element.getWidth()*2+element.getGap()*1+.5);
        int width3 = (int) (element.getWidth()*1+element.getGap()*0+.5);
        gc.setForeground( getForgroundColor() );
        gc.setLineWidth(width1 );
        drawLine(element);
        gc.setLineWidth( width2 );
        gc.setForeground( getBackgroundColor() );
        drawLine( element );
        gc.setForeground( getForgroundColor() );
        gc.setLineWidth( width3 );
        drawLine( element );
    }
    
    public void visitModel( RenderingModel model ) {

        for(IRenderingElement element:model) {
            element.accept( this );
        }
    }
    
    public void visitText( TextElement element ) {

        int x = scaleX(element.getPosition().getX());
        int y = scaleY(element.getPosition().getY());
        String text = element.getText();
        
        Font font= new Font(gc.getDevice(),"Arial",30,SWT.NORMAL);
        gc.setFont(font);
        Point textSize = gc.textExtent( text );
        x = x - textSize.x/2;
        y = y - textSize.y/2;
        gc.setForeground( toSWTColor( gc, element.getColor() ) );
        gc.setBackground(  getBackgroundColor() );
        gc.setAdvanced( true );
        
        drawTextUpsideDown( gc, text, x, y, textSize.x, textSize.y );
    }

    private ImageData drawText(Device device, String text, Rectangle rect) {
    	    Image image = new Image(device,rect);
    	    
    	    GC imageGC = new GC(image);
    	        imageGC.setFont( gc.getFont() );
    	        imageGC.setForeground( device.getSystemColor( SWT.COLOR_WHITE ) );
    	        imageGC.setBackground( device.getSystemColor( SWT.COLOR_BLACK ) );
    	        imageGC.setAdvanced( true );
    	        imageGC.setInterpolation(SWT.HIGH);
    	        imageGC.setAntialias( SWT.OFF);
    	        
    	        imageGC.fillRectangle( rect);
//    	        layout.draw( imageGC, 0, 0 );
    	        imageGC.drawText( text, 0, 0 );
          imageGC.dispose();
    	    ImageData imageData = image.getImageData();
    	    image.dispose();
//    	    font.dispose();
    	    return imageData;
    	}

    private void drawTextUpsideDown( GC graphics, String text, 
                                     int x, int y,
                                     int width, int height){
    	    
    	    Rectangle bounds = new Rectangle(x,y,width,height);
    	    Rectangle doubleBounds = new Rectangle(
                                                   bounds.x,
                                                   bounds.y,
                                                   bounds.width,
                                                   bounds.height);
    	    if(false) {//rendererModel.getIsCompact()) {
    	        int size = Math.max( bounds.width, bounds.height );
    	        graphics.setBackground( graphics.getForeground() );
    	        graphics.fillOval( x, y, size,size);
    	    } else {
    	        ImageData alphaMask =
                        drawText( graphics.getDevice(), text, doubleBounds );
                alphaMask = superFlip( alphaMask, true, graphics.getForeground() );
                graphics.setInterpolation( SWT.HIGH );
                Image image = new Image( graphics.getDevice(), alphaMask );
                graphics.drawImage( image, 0, 0, doubleBounds.width,
                                    doubleBounds.height, x, y, bounds.width,
                                    bounds.height );
    
                image.dispose();
    	    }
    	}

    static ImageData superFlip(ImageData srcData, boolean vertical,Color color) {
    
      ImageData newImageData = (ImageData) srcData.clone();
    
      for (int srcY = 0; srcY < srcData.height; srcY++) {
        for (int srcX = 0; srcX < srcData.width; srcX++) {
          int destX = 0, destY = 0;
//          if (vertical) {
//            destX = srcX;
//            destY = srcData.height - srcY - 1;
//          } else {
//            destX = srcData.width - srcX - 1;
//            destY = srcY;
//          }       
          destX =srcX;
          destY = srcY;
          int red = srcData.palette.getRGB( srcData.getPixel( srcX, srcY)).red;
          newImageData.setAlpha( destX, destY, red );
          newImageData.setPixel( destX, destY, srcData.palette.getPixel(
                                                             color.getRGB() ) );          
        }
      }
    
      return newImageData;
    }
    
    public  Color toSWTColor(GC graphics,java.awt.Color color) {
        if(cleanUp == null) 
            cleanUp = new HashMap<java.awt.Color,Color>();
        if(color == null) color = IRenderingElement.defaultColor;
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
}
