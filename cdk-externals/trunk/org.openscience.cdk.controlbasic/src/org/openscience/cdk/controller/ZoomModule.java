package org.openscience.cdk.controller;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.renderer.IRenderer;
import org.openscience.cdk.renderer.RendererModel;

/**
 * @cdk.module controlbasic
 */
public class ZoomModule extends ControllerModuleAdapter {

    private Point2d worldCoord = null;

    public ZoomModule(IChemModelRelay chemModelRelay) {
        super(chemModelRelay);
    }

    public void mouseWheelMovedForward(int clicks) {
        doZoom( .9 );
        chemModelRelay.fireZoomEvent();
        chemModelRelay.updateView();
    }

    public void mouseWheelMovedBackward(int clicks) {
        doZoom( 1.1 );
        chemModelRelay.fireZoomEvent();
        chemModelRelay.updateView();
    }

    private void doZoom(double z) {
        IRenderer renderer = chemModelRelay.getRenderer();
        Point2d screenCoord = 
            renderer.toScreenCoordinates( worldCoord.x, worldCoord.y );
        zoom(z);
        Point2d newScreenCoords = 
            renderer.toScreenCoordinates( worldCoord.x, worldCoord.y );
        
        Vector2d v= new Vector2d();
        v.sub( screenCoord, newScreenCoords );
        renderer.shiftDrawCenter( v.x, v.y );
    }
    
    private void zoom(double zoomFactor) {
        RendererModel model = chemModelRelay.getRenderer().getRenderer2DModel();
        double zoom = model.getZoomFactor();
        zoom = zoom * zoomFactor;
        if (zoom < .1 && zoom > 100)
            return;
        chemModelRelay.getRenderer().setZoom( zoom );
    }

    @Override
    public void mouseMove( Point2d worldCoord ) {
        this.worldCoord = worldCoord;
    }
    
    public String getDrawModeString() {
       return "Zoom";
    }

}
