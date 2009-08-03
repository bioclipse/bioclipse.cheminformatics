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
package org.openscience.cdk.controller.edit;

import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.controller.Changed;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.AtomPlacer;

/**
 *
 * @author Arvid
 * @cdk.module controlbasic
 */
public class AppendAtom extends AbstractEdit implements IEdit {

    String symbol;
    IAtom source;
    Point2d pos;

    IAtom newAtom;
    IBond newBond;

    public static IEdit edit(String symbol,IAtom source) {
        AppendAtom edit = new AppendAtom();
        edit.symbol = symbol;
        edit.source = source;
        edit.pos = null;
        return edit;
    }

    public static IEdit edit( String symbol, IAtom source, Point2d pos) {
        AppendAtom edit = new AppendAtom();

        edit.symbol = symbol;
        edit.source = source;
        edit.pos = pos;
        return edit;
    }
    public void redo() {

        if(pos != null)
            newAtom = model.getBuilder().newAtom( symbol, pos );
        else
            newAtom = model.getBuilder().newAtom( symbol );
        newBond = model.getBuilder().newBond( source, newAtom );
        if(pos==null) {
            // The AtomPlacer generates coordinates for the new atom
            AtomPlacer atomPlacer = new AtomPlacer();
            atomPlacer.setMolecule(model);
            double bondLength;
            if (model.getBondCount() >= 1) {
                bondLength = GeometryTools.getBondLengthAverage(model);
            } else {
                bondLength = 1.4;       // XXX Or some sensible default?
            }

            // determine the atoms which define where the
            // new atom should not be placed
            List<IAtom> connectedAtoms = model.getConnectedAtomsList(source);

            if (connectedAtoms.size() == 0) {
                Point2d newAtomPoint = new Point2d(source.getPoint2d());
                double angle = Math.toRadians( -30 );
                Vector2d vec1 = new Vector2d(Math.cos(angle), Math.sin(angle));
                vec1.scale( bondLength );
                newAtomPoint.add( vec1 );
                newAtom.setPoint2d(newAtomPoint);
            } else if (connectedAtoms.size() == 1) {
                IMolecule ac = model.getBuilder().newMolecule();
                ac.addAtom(source);
                ac.addAtom(newAtom);
                Point2d distanceMeasure = new Point2d(0,0); // XXX not sure about this?
                IAtom connectedAtom = connectedAtoms.get(0);
                Vector2d v = atomPlacer.getNextBondVector( source,
                                                         connectedAtom,
                                                         distanceMeasure, true);
                atomPlacer.placeLinearChain(ac, v, bondLength);
            } else {
                IMolecule placedAtoms = model.getBuilder().newMolecule();
                for (IAtom conAtom : connectedAtoms) placedAtoms.addAtom(conAtom);
                Point2d center2D = GeometryTools.get2DCenter(placedAtoms);

                IAtomContainer unplacedAtoms = model.getBuilder().newAtomContainer();
                unplacedAtoms.addAtom(newAtom);

                atomPlacer.distributePartners( source, placedAtoms, center2D,
                                               unplacedAtoms, bondLength);
            }
        }

        model.addAtom( newAtom );
        model.addBond( newBond );

        updateHydrogenCount( source,newAtom );
    }

    public void undo() {

        model.removeBond( newBond );
        model.removeAtom( newAtom );
        updateHydrogenCount( source );

    }

    public Set<Changed> getTypeOfChanges() {

        return changed( Changed.Structure );
    }

}
