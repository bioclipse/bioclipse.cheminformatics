package org.openscience.cdk.renderer.visitor;

import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.font.IFontManager;

/**
 * @cdk.module render
 */
public interface IDrawVisitor extends IRenderingVisitor {
    
    public void setFontManager(IFontManager fontManager);
    
    public void setRendererModel(RendererModel rendererModel);

}
