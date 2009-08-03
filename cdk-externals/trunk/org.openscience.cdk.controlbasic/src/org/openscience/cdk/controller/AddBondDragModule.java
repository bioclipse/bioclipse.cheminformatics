/*
 * Copyright (C) 2009  Arvid Berg <goglepox@users.sourceforge.net>
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

import java.util.Iterator;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.controller.edit.AddAtom;
import org.openscience.cdk.controller.edit.AddBond;
import org.openscience.cdk.controller.edit.AppendAtom;
import org.openscience.cdk.controller.edit.CreateBond;
import org.openscience.cdk.controller.edit.IEdit;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.renderer.selection.SingleSelection;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * Adds a bond at direction that is draged.
 *
 * @cdk.module controlbasic
 */
public class AddBondDragModule extends ControllerModuleAdapter {

    Point2d start;
    Point2d dest;
    IAtom source = null;// either atom at mouse down or new atom
    IAtom merge = null;
    boolean newSource = false;

    boolean isBond = false;
    private double bondLenght;

    class State {
        // atom || point

        IEdit edit;
    }

    public AddBondDragModule(IChemModelRelay chemModelRelay) {
        super( chemModelRelay );
    }

    private IChemObjectBuilder getBuilder() {
        return chemModelRelay.getIChemModel().getBuilder();
    }

    @Override
    public void mouseClickedDown( Point2d worldCoord ) {
        start = null;
        dest = null;
        source = null;
        merge = null;
        isBond = false;
        newSource = false;
        bondLenght = calculateAverageBondLength( chemModelRelay.getIChemModel() );
        start = new Point2d(worldCoord);
        IAtom closestAtom = chemModelRelay.getClosestAtom(worldCoord);
        IBond closestBond = chemModelRelay.getClosestBond( worldCoord );

        IChemObject singleSelection = getHighlighted( worldCoord,
                                                      closestAtom,
                                                      closestBond );

        if(singleSelection == null || singleSelection instanceof IAtom ) {
            isBond = false;
        source =  (IAtom) getHighlighted(worldCoord, closestAtom);

        if(source == null) {
            source = getBuilder().newAtom( "C", start );
            newSource = true;
        }
        }
        else if (singleSelection instanceof IBond) {
            chemModelRelay.cycleBondValence((IBond) singleSelection);
            setSelection(new SingleSelection<IChemObject>(singleSelection));
            isBond = true;
        }
    }

    private double calculateAverageBondLength(IChemModel chemModel) {
    	List<IAtomContainer> containers =
    		ChemModelManipulator.getAllAtomContainers(chemModel);
    	double bondLengthSum = 0.0;
		int bondCount = 0;
    	for (IAtomContainer container : containers) {
    		Iterator<IBond> bonds = container.bonds().iterator();
    		while (bonds.hasNext()) {
    			IBond bond = bonds.next();
    			IAtom atom1 = bond.getAtom(0);
    			IAtom atom2 = bond.getAtom(1);
    			if (atom1.getPoint2d() != null && atom2.getPoint2d() != null) {
    				bondCount++;
    				bondLengthSum += GeometryTools.getLength2D(bond);
    			}
    		}
    	}
    	if (bondCount == 0) return 0.0;
		return bondLengthSum / bondCount;
	}

	@Override
    public void mouseDrag( Point2d worldCoordFrom, Point2d worldCoordTo ) {
        if(isBond) return;
        IAtom closestAtom = chemModelRelay.getClosestAtom(worldCoordTo);

        merge =  (IAtom) getHighlighted(worldCoordTo, closestAtom);


        chemModelRelay.clearPhantoms();
        if(start.distance( worldCoordTo )<getHighlightDistance()) {
            // clear phantom
            merge = null;
            dest = null;
        }else if (merge != null) {
            // set bond
            chemModelRelay.addPhantomBond( getBuilder().newBond(source,merge) );
            dest = null;

        }else {
            dest = roundAngle( start, worldCoordTo );
            IAtom atom = getBuilder().newAtom( "C", dest );
            IBond bond = getBuilder().newBond( source,atom );
            chemModelRelay.addPhantomBond( bond );
            // update phantom
        }
        chemModelRelay.updateView();
    }

    private Point2d roundAngle(Point2d s,Point2d d) {

        Vector2d v = new Vector2d();
        v.sub( d, s );
        double rad = Math.atan2(v.y,v.x);
        double deg = Math.toDegrees( rad );
        deg = Math.round( deg/30)*30;
        rad = Math.toRadians( deg );
        v.x = bondLenght*Math.cos( rad );
        v.y = bondLenght*Math.sin( rad );
        Point2d result = new Point2d();
        result.add( s, v );
        return result;
    }

    @Override
    public void mouseClickedUp( Point2d worldCoord ) {
        final IEdit edit;
        chemModelRelay.clearPhantoms();
        if(isBond) return;


        if(start.distance( worldCoord )< getHighlightDistance()) {
            if(newSource)
                edit = AddAtom.edit( "C", start );
            else
                edit = AppendAtom.edit( "C", source );
        }
        else
            if(merge != null) {
                if(newSource)
                    edit = AppendAtom.edit("C",merge,start);
                else
                    edit = AddBond.edit( source, merge );
            }else {
                if(newSource)
                    edit = CreateBond.edit( start,dest);
                else
                    edit = AppendAtom.edit("C",source,dest);
            }
        chemModelRelay.execute(edit);

    }

    public String getDrawModeString() {

        return "Draw Bond";
    }

}
