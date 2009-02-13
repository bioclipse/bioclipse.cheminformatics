package org.openscience.cdk.renderer.generators;

import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.IRenderingElement;

/**
 * A Generator specifically for Reactions.
 * 
 * @author maclean
 *
 * @cdk.module render
 */
public interface IReactionGenerator {
    
    public IRenderingElement generate(IReaction reaction, RendererModel model);

}
