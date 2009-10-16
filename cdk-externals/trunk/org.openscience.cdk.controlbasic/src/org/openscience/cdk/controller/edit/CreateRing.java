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

import java.util.Set;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.Changed;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.layout.RingPlacer;

/**
 * 
 * @author arvid
 *  @cdk.module controlbasic
 */
public class CreateRing extends AbstractEdit implements IEdit {

    Point2d pos;
    int ringSize;

    IRing ringToAdd;

    public static CreateRing addRing(Point2d pos, int ringSize) {
        return new CreateRing( null,pos, ringSize );
    }

    public static CreateRing addRing(IRing ring) {
        return new CreateRing( ring, null, -1 );
    }

    private CreateRing(IRing ring, Point2d pos, int ringSize) {
        this.ringToAdd = ring;
        this.pos = pos;
        this.ringSize = ringSize;
    }

    public Set<Changed> getTypeOfChanges() {
        return changed( Changed.Structure );
    }

    public void redo() {

        if ( ringToAdd == null ) {
            ringToAdd = model.getBuilder().newRing( ringSize );
            RingPlacer placer = new RingPlacer();
            placer.placeRing( ringToAdd, pos, 1.4 );
        }
        model.add( ringToAdd );
    }

    public void undo() {

        model.remove( ringToAdd );
    }

}
