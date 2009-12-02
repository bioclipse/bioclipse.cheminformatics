/* $Revision: $ $Author:  $ $Date: $
 *
 * Copyright (C) 2007  Gilleain Torrance
 *               2009  Arvid Berg <goglepox@users.sourceforge.net>
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

import static org.openscience.cdk.controller.MoveModule.calculateMerge;
import static org.openscience.cdk.geometry.GeometryTools.calculatePerpendicularUnitVector;
import static org.openscience.cdk.geometry.GeometryTools.getBondLengthAverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.controller.edit.CompositEdit;
import org.openscience.cdk.controller.edit.CreateRing;
import org.openscience.cdk.controller.edit.IEdit;
import org.openscience.cdk.controller.edit.Merge;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.layout.RingPlacer;

/**
 * Adds an atom on the given location on mouseclick
 *
 * @author maclean
 * @cdk.module controlbasic
 */
public class AddRingModule extends ControllerModuleAdapter {

    private int ringSize;
    private boolean addingBenzene = false;

    RingPlacer ringPlacer;
    IRing ring;
    int maxDistance;
    double bondLength = 1.4;

    public AddRingModule(IChemModelRelay chemModelRelay, int ringSize,
            boolean addingBenzene) {
        super(chemModelRelay);
        this.ringSize = ringSize;
        this.addingBenzene = addingBenzene;
        this.ringPlacer = new RingPlacer();
    }

    public void mouseClickedDown(Point2d worldCoord) {

        Map<IAtom,IAtom> mergeMap = new HashMap<IAtom, IAtom>(
                        chemModelRelay.getRenderModel().getMerge());

        // Shift the ring to minimize distortion
        Vector2d shift = MoveModule.calcualteShift( mergeMap );
        for(IAtom atom:ring.atoms()) {
            atom.getPoint2d().add( shift );
        }

        IEdit ringEdit = CreateRing.addRing( ring );
        IEdit mergeEdit = Merge.merge( mergeMap );

        ring  = null;
        chemModelRelay.clearPhantoms();
        chemModelRelay.getRenderModel().getMerge().clear();
        chemModelRelay.execute( CompositEdit.compose( ringEdit,mergeEdit ) );
	}

    private void makeRingAromatic(IRing ring) {
        for(int i=0;i<ring.getBondCount();i++) {
            if(i%2==0)
                ring.getBond(i).setOrder(IBond.Order.DOUBLE);
        }

        for (IAtom atom : ring.atoms())
            atom.setFlag(CDKConstants.ISAROMATIC, true);
        for (IBond bond : ring.bonds())
            bond.setFlag(CDKConstants.ISAROMATIC, true);
    }

    private Point2d getConnectedAtomCenter(IAtomContainer ac,IAtom... atoms) {
        List<IAtom> connectedAtoms = new ArrayList<IAtom>();
        for(IAtom atom:atoms) {
            connectedAtoms.addAll( ac.getConnectedAtomsList( atom ) );
        }
        for(IAtom atom:atoms){
            connectedAtoms.remove( atom );
        }
        if(connectedAtoms.isEmpty()) return null;
        return GeometryTools.get2DCenter( connectedAtoms );
    }

    @Override
    public void mouseMove( Point2d worldCoord ) {

        if(ring==null) {
            bondLength = getBondLengthAverage( getModel() );
            if(Double.isNaN( bondLength ) || bondLength == 0) bondLength = 1.4;
            ring = getModel().getBuilder().newRing(ringSize, "C");
            if(addingBenzene)
                makeRingAromatic( ring );
            for(IBond bond:ring.bonds()){
                chemModelRelay.addPhantomBond( bond );
                for(IAtom atom:bond.atoms())
                    chemModelRelay.addPhantomAtom( atom );
            }
        }
        IBond bond = chemModelRelay.getRenderModel().getHighlightedBond();
        IAtom atom = chemModelRelay.getRenderModel().getHighlightedAtom();
        if(bond!=null) {
            IAtomContainer ac = bond.getBuilder().newAtomContainer();
            IBond ringBond = ring.getBond( 0 );
            for(int i=0;i<2;i++) {
                Point2d atomPos = new Point2d(bond.getAtom( i ).getPoint2d());
                ringBond.getAtom( i ).setPoint2d( atomPos);
            }
            ac.addBond( ringBond );
            for(IAtom rAtom:ringBond.atoms()) ac.addAtom(rAtom);

            Point2d acCenter = GeometryTools.get2DCenter( ac );
            Vector2d normal;
            Point2d connCenter = getConnectedAtomCenter( getModel(),
                                                         bond.getAtom( 0 ),
                                                         bond.getAtom( 1 ));
            if(connCenter!=null) {
                normal = new Vector2d();
                normal.sub( acCenter, connCenter );
            } else
                normal = calculatePerpendicularUnitVector(
                                             ringBond.getAtom( 0 ).getPoint2d(),
                                             ringBond.getAtom( 1 ).getPoint2d() );

            ringPlacer.placeFusedRing( ring, ac, acCenter, normal, bondLength );
        } else if (atom!=null){
            Point2d ringCenter = GeometryTools.get2DCenter( ring );
            IAtomContainer ac = atom.getBuilder().newAtomContainer();
            IAtom ringAtom = ring.getAtom( 0 );
            ringAtom.setPoint2d( new Point2d(atom.getPoint2d()) );
            ac.addAtom( ringAtom );

            Point2d acCenter = new Point2d(atom.getPoint2d());
            Vector2d normal;
            Point2d connCenter = getConnectedAtomCenter( getModel(), atom );
            if(connCenter!=null) {
                normal = new Vector2d();
                normal.sub(acCenter,connCenter);
            }else  {
                if(Double.isNaN( ringCenter.x ) || Double.isNaN( ringCenter.y ))
                    normal = new Vector2d(1,0);
                else {
                    normal = new Vector2d();
                    normal.sub( ringCenter, acCenter );
                }
            }

            ringPlacer.placeSpiroRing( ring, ac, acCenter, normal, bondLength );
        } else {
            ringPlacer.placeRing(ring, worldCoord, bondLength);
        }
        Map<IAtom,IAtom> merge = calculateMerge( ring,
                                                 getModel(),
                                                 getHighlightDistance() );
        Map<IAtom,IAtom> oldMerge = chemModelRelay.getRenderModel().getMerge();
        oldMerge.clear();
        oldMerge.putAll( merge );
        chemModelRelay.updateView();
    }

    public String getDrawModeString() {
    	if (addingBenzene) {
			return "Benzene";
    	} else {
			return "Ring" + " " + ringSize;
    	}
    }

    @Override
    public void mouseExit( Point2d worldCoord ) {
        chemModelRelay.clearPhantoms();
        chemModelRelay.updateView();
    }

    @Override
    public void mouseEnter( Point2d worldCoord ) {
        if(ring!=null) {
            for(IBond bond:ring.bonds())
                chemModelRelay.addPhantomBond( bond );
        }
    }
}
