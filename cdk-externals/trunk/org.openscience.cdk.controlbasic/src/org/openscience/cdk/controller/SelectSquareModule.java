/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2008  Gilleain Torrance <gilleain.torrance@gmail.com>
 *
 *  Contact: cdk-devel@list.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.controller;

import javax.vecmath.Point2d;

import org.openscience.cdk.renderer.selection.RectangleSelection;

/**
 * @cdk.module controlbasic
 */
public class SelectSquareModule extends ControllerModuleAdapter {
    
    private RectangleSelection selection = new RectangleSelection();
    
    public SelectSquareModule(IChemModelRelay chemModelRelay) {
        super(chemModelRelay);
    }
    
    public void mouseClickedDown(Point2d p) {
        this.selection.clear();
        this.chemModelRelay.getRenderer()
                           .getRenderer2DModel()
                           .setSelection(this.selection);
    }
    
    public void mouseDrag(Point2d from, Point2d to) {
        this.selection.addPoint(to);
        this.chemModelRelay.select(selection);
        this.chemModelRelay.updateView();
    }
    
    public void mouseClickedUp(Point2d p) {
        this.chemModelRelay.select(selection);
        this.selection.reset();
        this.chemModelRelay.updateView();
    }
    
    public String getDrawModeString() {
        return "Select Square";
    }

}
