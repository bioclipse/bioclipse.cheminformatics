package net.bioclipse.cdk.jchempaint.rendering;

import java.awt.geom.AffineTransform;

import org.eclipse.swt.graphics.GC;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.IRenderingElement;

public interface Renderer {

	public void visit(GC gc,AffineTransform transform, RendererModel model,IRenderingElement element);
	public boolean accepts(IRenderingElement element);
}
