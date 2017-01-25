package net.bioclipse.cdk.jchempaint.rendering;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;

public class AWTDrawVisitorFactory {
    
    List<Renderer<Graphics2D>> renderers = new ArrayList<Renderer<Graphics2D>>();
    
    void addRenderer(Renderer<Graphics2D> renderer) {
        renderers.add( renderer );
    }
    
    void removeRenderer(Renderer<Graphics2D> renderer) {
        renderers.remove( renderer );
    }
    
    public AWTDrawVisitor createAWTDrawVisitor(Graphics2D gc) {
        return new OSGiAWTDrawVisitor( gc  ,renderers) ;
    }
}

class OSGiAWTDrawVisitor extends AWTDrawVisitor {

    List<Renderer<Graphics2D>> renderers;
    public OSGiAWTDrawVisitor(Graphics2D gc,List<Renderer<Graphics2D>> renderers) {
        super( gc );
        this.renderers = renderers;
    }

    @Override
    public void visit( IRenderingElement element ) {

        if ( !visitOSGi( element ) ) {
            super.visit( element );
        }
    }

    protected boolean visitOSGi( IRenderingElement element ) {

        for ( Renderer<Graphics2D> r : renderers ) {
            if ( r.accepts( element ) ) {
                r.visit( getGraphics(), transform, getRendererModel(), element );
                return true;
            }
        }
        return false;
    }
}
