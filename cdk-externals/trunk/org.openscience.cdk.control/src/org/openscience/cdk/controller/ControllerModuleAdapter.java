package org.openscience.cdk.controller;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.IControllerModule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.RendererModel;

/**
 * @cdk.module control
 */
public abstract class ControllerModuleAdapter implements IControllerModule {

	protected IChemModelRelay chemModelRelay;

	public ControllerModuleAdapter(IChemModelRelay chemModelRelay) {
		this.chemModelRelay = chemModelRelay;
	}
	
	public double getHighlightDistance() {
	    RendererModel model = chemModelRelay.getRenderer().getRenderer2DModel();
        return model.getHighlightDistance() / model.getScale();
	}
	
	public double distanceToAtom(IAtom atom, Point2d p) {
	    if (atom == null) {
	        return Double.MAX_VALUE;
	    } else {
	        return atom.getPoint2d().distance(p);
	    }
	}
	
	public double distanceToBond(IBond bond, Point2d p) {
	    if (bond == null) {
            return Double.MAX_VALUE;
        } else {
            return bond.get2DCenter().distance(p);
        }
	}
	
	public boolean isBondOnlyInHighlightDistance(double dA, double dB, double dH) {
        return dA > dH && dB < dH;
    }

	public boolean isAtomOnlyInHighlightDistance(double dA, double dB, double dH) {
        return dA < dH && dB > dH;
    }

	public boolean noSelection(double dA, double dB, double dH) {
        return (dH == Double.POSITIVE_INFINITY) || (dA > dH && dB > dH);
    }
	
	public void mouseWheelMovedBackward(int clicks) {
	}

	public void mouseWheelMovedForward(int clicks) {
	}
	
	public void mouseClickedDouble(Point2d worldCoord) {
	}

	public void mouseClickedDown(Point2d worldCoord) {
	}

	public void mouseClickedUp(Point2d worldCoord) {
	}

	public void mouseClickedDownRight(Point2d worldCoord) {
	}

	public void mouseClickedUpRight(Point2d worldCoord) {
	}

	public void mouseDrag(Point2d worldCoordFrom, Point2d worldCoordTo) {
	}

	public void mouseEnter(Point2d worldCoord) {
	}

	public void mouseExit(Point2d worldCoord) {
	}

	public void mouseMove(Point2d worldCoord) {
	}

	public void setChemModelRelay(IChemModelRelay relay) {
	}

}
