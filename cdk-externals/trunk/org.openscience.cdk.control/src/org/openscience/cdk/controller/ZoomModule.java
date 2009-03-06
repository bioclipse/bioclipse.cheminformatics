package org.openscience.cdk.controller;

import org.openscience.cdk.renderer.RendererModel;

/**
 * @cdk.module control
 */
public class ZoomModule extends ControllerModuleAdapter {
    
    public ZoomModule(IChemModelRelay chemModelRelay) {
        super(chemModelRelay);
    }
    
    public void mouseWheelMovedForward(int clicks) {
        RendererModel model = chemModelRelay.getRenderer().getRenderer2DModel();
        if (model.getZoomFactor()*.9 > .1) 
        	model.setZoomFactor(model.getZoomFactor() * 0.9);
        chemModelRelay.updateView();
    }
    
    public void mouseWheelMovedBackward(int clicks) {
        RendererModel model = chemModelRelay.getRenderer().getRenderer2DModel();
        if (model.getZoomFactor()*1.1 < 10) 
        	model.setZoomFactor(model.getZoomFactor() * 1.1);
        chemModelRelay.updateView();
    }

    public String getDrawModeString() {
       return "Zoom";
    }

}
