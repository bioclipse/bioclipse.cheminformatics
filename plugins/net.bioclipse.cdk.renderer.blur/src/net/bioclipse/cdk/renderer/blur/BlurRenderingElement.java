package net.bioclipse.cdk.renderer.blur;

import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;

public class BlurRenderingElement implements IRenderingElement {
	
	ElementGroup node;
    int          stencilSize;
	
    public BlurRenderingElement(ElementGroup group, int stencilSize) {
		this.node = group;
        this.stencilSize = stencilSize;
	}
	
    public ElementGroup getNode() {

        return node;
    }

    public int getStencilSize() {

        return stencilSize;
    }

	@Override
	public void accept(IRenderingVisitor visitor) {
		visitor.visit(this);
	}
	
	
}