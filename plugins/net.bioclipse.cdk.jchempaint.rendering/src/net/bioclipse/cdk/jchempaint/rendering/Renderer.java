package net.bioclipse.cdk.jchempaint.rendering;

import java.awt.geom.AffineTransform;

import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.IRenderingElement;

public interface Renderer<G> {

	public void visit(G gc,AffineTransform transform, RendererModel model,IRenderingElement element);
	public boolean accepts(IRenderingElement element);
}
