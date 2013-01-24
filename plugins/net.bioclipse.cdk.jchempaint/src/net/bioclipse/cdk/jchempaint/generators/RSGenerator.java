package net.bioclipse.cdk.jchempaint.generators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.geometry.cip.CIPTool;
import org.openscience.cdk.geometry.cip.CIPTool.CIP_CHIRALITY;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.ITetrahedralChirality;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.TextElement;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Scale;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.parameter.AbstractGeneratorParameter;

public class RSGenerator implements IGenerator<IAtomContainer> {

	public static class TextColor
	extends AbstractGeneratorParameter<Color> {
		public Color getDefault() {
			return Color.ORANGE;
		}
	}

	private IGeneratorParameter<Color> textColor = new TextColor();

	public static class Offset
	extends AbstractGeneratorParameter<Vector2d> {
		public Vector2d getDefault() {
			return new Vector2d();
		}
	}
	private Offset offset = new Offset();

	public List<IGeneratorParameter<?>> getParameters() {
		List<IGeneratorParameter<?>> list = new ArrayList<IGeneratorParameter<?>>();
		list.add(textColor);
		list.add(offset);
		return list;
	};

	public IRenderingElement generate(IAtomContainer container, RendererModel model) {
		ElementGroup rsIndicators = new ElementGroup();
		Vector2d offset = new Vector2d(
				this.offset.getValue().x, -this.offset.getValue().y
		);
		offset.scale( 1/model.getParameter(Scale.class).getValue() );

		Iterable<IStereoElement> stereoElements = container.stereoElements();
		for (IStereoElement stereo : stereoElements) {
			if (stereo instanceof ITetrahedralChirality) {
				ITetrahedralChirality l4Chiral = (ITetrahedralChirality)stereo;
				CIP_CHIRALITY chirality = CIPTool.getCIPChirality(container, l4Chiral);
				String stereoText = (chirality == CIP_CHIRALITY.R ? "R" : "S");
				if (chirality == CIP_CHIRALITY.NONE) stereoText = "?";
				IAtom atom = l4Chiral.getChiralAtom();
				Point2d point = new Point2d(atom.getPoint2d());
				point.add( offset );
				rsIndicators.add(
						new TextElement(
								point.x, point.y,
								stereoText,
								textColor.getValue()
						)
				);
			}
		}
		return rsIndicators;
	};
}
