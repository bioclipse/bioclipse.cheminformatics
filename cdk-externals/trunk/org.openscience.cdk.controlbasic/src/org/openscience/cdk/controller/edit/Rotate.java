/*
 * Copyright (C) 2008  Gilleain Torrance <gilleain.torrance@gmail.com>
 *               2009  Mark Rijnbeek <mark_rynbeek@users.sourceforge.net>
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
package org.openscience.cdk.controller.edit;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.Changed;
import org.openscience.cdk.interfaces.IAtom;

/**
 * Module to rotate a selection of atoms (and their bonds).
 *
 * @cdk.module controlbasic
 */
public class Rotate extends AbstractEdit {

    private double angle;
    Point2d rotationCenter;
    Collection<IAtom> atoms;
    
    public static Rotate rotate(Collection<IAtom> atoms, double angle, Point2d rotationCenter) {
        return new Rotate( atoms, angle, rotationCenter );
    }

    private Rotate(Collection<IAtom> atoms, double angle, Point2d rotationCenter) {
        this.angle = angle;
        this.rotationCenter = rotationCenter;
        this.atoms = new HashSet<IAtom>(atoms);
    }

    public Set<Changed> getTypeOfChanges() {

        return changed( Changed.Coordinates );
    }

    private void rotate(double angle) {
        /* For more info on the mathematics, see Wiki at
         * http://en.wikipedia.org/wiki/Coordinate_rotation
         */
        double cosine = java.lang.Math.cos(angle);
        double sine = java.lang.Math.sin(angle);
        double x,y;
        for(IAtom atom:atoms) {
            Point2d p = atom.getPoint2d();
            p.sub( rotationCenter );
            x= p.x*cosine-p.y*sine;
            y= p.x*sine+p.y*cosine;
            Point2d p2 = new Point2d(x,y);
            p2.add( rotationCenter );
            atom.setPoint2d( p2 );
        }
        updateHydrogenCount( atoms );
    }

    public void redo() {

        rotate(angle);
    }

    public void undo() {

        rotate(-angle);
    }

}
