package org.openscience.cdk.renderer.generators;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;

/**
 * @cdk.module render
 */
public class BasicGenerator {
	
	private BasicAtomGenerator atomGenerator;
	private BasicBondGenerator bondGenerator;
	
	public BasicGenerator(Renderer2DModel model) {
		this.atomGenerator = new BasicAtomGenerator(model);
		this.bondGenerator = new BasicBondGenerator(model);
	}
	
	public IRenderingElement generate(IAtomContainer ac) {
		Point2d center = GeometryTools.get2DCenter(ac);
		ElementGroup diagram = new ElementGroup();
		diagram.add(this.bondGenerator.generate(ac, center));
		diagram.add(this.atomGenerator.generate(ac, center));
		return diagram;
	}

}
