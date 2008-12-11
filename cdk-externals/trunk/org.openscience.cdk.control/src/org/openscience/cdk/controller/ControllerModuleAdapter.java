package org.openscience.cdk.controller;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.IControllerModule;

/**
 * @cdk.module control
 */
public abstract class ControllerModuleAdapter implements IControllerModule {

	protected IChemModelRelay chemModelRelay;

	public ControllerModuleAdapter(IChemModelRelay chemModelRelay) {
		this.chemModelRelay = chemModelRelay;
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
