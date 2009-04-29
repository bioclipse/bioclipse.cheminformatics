package net.bioclipse.cdk.ui.sdfeditor.editor;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;

public interface IRenderer2DConfigurator {

	public void configure(RendererModel rendererModel, IAtomContainer atomContainer);
	
}
