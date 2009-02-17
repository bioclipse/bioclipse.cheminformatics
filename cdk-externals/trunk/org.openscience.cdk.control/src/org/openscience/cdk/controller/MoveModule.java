/* $Revision: 7636 $ $Author: nielsout $ $Date: 2007-01-04 18:46:10 +0100 (Thu, 04 Jan 2007) $
 *
 * Copyright (C) 2007  Niels Out <nielsout@users.sf.net>
 * Copyright (C) 2008  Arvid Berg <goglepox@users.sf.net>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All I ask is that proper credit is given for my work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.controller;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;
import org.openscience.cdk.tools.LoggingTool;

/**
 * Demo IController2DModule. -write picture to file on doubleclick -show atom
 * name on hove-over -drags atoms around (click near atom and move mouse)
 *
 * @author Niels Out
 * @cdk.svnrev $Revision: 9162 $
 * @cdk.module control
 */
public class MoveModule extends ControllerModuleAdapter {

    private enum Type {
        BOND, ATOM, SELECTION, NONE
    }

    private LoggingTool logger = new LoggingTool(MoveModule.class);
    private IAtom atom;
    private IBond bond;
    private IChemObjectSelection selection;
    private Vector2d offset;
    private Type type = Type.NONE;

    public MoveModule(IChemModelRelay chemObjectRelay) {
        super(chemObjectRelay);
    }

    private Type getClosest(IAtom atom, IBond bond, IChemObjectSelection selection,
            Point2d worldCoord) {
        if (selection.isFilled())
            return Type.SELECTION;
        
        double dA = super.distanceToAtom(atom, worldCoord);
        double dB = super.distanceToBond(bond, worldCoord);
        double dH = super.getHighlightDistance();
        
        if (super.noSelection(dA, dB, dH)) {
            return Type.NONE;
        } else if (super.isAtomOnlyInHighlightDistance(dA, dB, dH)) {
            return Type.ATOM;
        } else if (super.isBondOnlyInHighlightDistance(dA, dB, dH)) {
            return Type.BOND;
        } else {
            if (dA > dB) {
                return Type.ATOM;
            } else { 
                return Type.BOND;
            }
        }
    }

    public void mouseClickedDown(Point2d worldCoord) {

        Point2d current = null;
        atom = chemModelRelay.getClosestAtom(worldCoord);
        bond = chemModelRelay.getClosestBond(worldCoord);
        selection = 
              chemModelRelay.getRenderer().getRenderer2DModel().getSelection();

        switch (type = getClosest(atom, bond, selection, worldCoord)) {
            case ATOM:
                current = atom.getPoint2d();
                break;
            case BOND:
                current = bond.get2DCenter();
                break;
            case SELECTION:
                current = GeometryTools.get2DCenter(
                        selection.getConnectedAtomContainer());
                break;
            default:
                return;
        }

        offset = new Vector2d();
        offset.sub(current, worldCoord);
    }

    public void mouseClickedUp(Point2d worldCoord) {
        type = Type.NONE;
        atom = null;
        bond = null;
        selection = null;
        offset = null;
    }

    public void mouseDrag(Point2d worldCoordFrom, Point2d worldCoordTo) {
        if (chemModelRelay != null && offset != null) {

            Point2d atomCoord = new Point2d();
            atomCoord.add(worldCoordTo, offset);
            switch (type) {
                case ATOM:
                	chemModelRelay.moveTo(atom, atomCoord);
                    break;
                case BOND:
                	chemModelRelay.moveTo(bond, atomCoord);
                    break;
                case SELECTION:
                    Point2d d = new Point2d();
                    d.sub(worldCoordTo, worldCoordFrom);
                    for (IAtom atom : 
                        selection.getConnectedAtomContainer().atoms()) {
                        atom.getPoint2d().add(d);
                    }
                    break;
                default:
                    return;
            }

            chemModelRelay.updateView();

        } else {
            if (chemModelRelay == null) {
                logger.debug("chemObjectRelay is NULL!");
            }
        }
    }

    public void setChemModelRelay(IChemModelRelay relay) {
        this.chemModelRelay = relay;
    }

	public String getDrawModeString() {
		return "Move";
	}

}
