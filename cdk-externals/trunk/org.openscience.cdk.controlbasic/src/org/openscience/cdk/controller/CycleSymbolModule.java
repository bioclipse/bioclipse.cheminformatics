/* $Revision: 7636 $ $Author: nielsout $ $Date: 2007-01-04 18:46:10 +0100 (Thu, 04 Jan 2007) $
 *
 * Copyright (C) 2007  Stefan Kuhn
 * Copyright (C) 2009  Arvid Berg
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

import static org.openscience.cdk.controller.edit.SetSymbol.setSymbol;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.edit.IEdit;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.renderer.selection.AbstractSelection;

/**
 * Adds an atom on the given location on mouseclick
 * 
 * @author Stefan Kuhn
 * @cdk.svnrev $Revision: 9162 $
 * @cdk.module controlbasic
 */
public class CycleSymbolModule extends ControllerModuleAdapter {

    public CycleSymbolModule(IChemModelRelay chemModelRelay) {
        super(chemModelRelay);
    }

    public void mouseClickedDown(Point2d worldCoord) {
        
        IChemObject singleSelected = getHighlighted( worldCoord, 
                                                     chemModelRelay.getClosestAtom(worldCoord) );
        IAtom closestAtom; 
        if(singleSelected instanceof IAtom) {
            closestAtom = (IAtom) singleSelected;
        }else {
            setSelection( AbstractSelection.EMPTY_SELECTION );
            return;
        }


        String symbol = closestAtom.getSymbol();
        String[] elements = chemModelRelay.getControlModel().getCommonElements();
        IEdit edit = null;
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].equals(symbol)) {
                if (i < elements.length - 1) {
                    edit = setSymbol( closestAtom,elements[i + 1]);
                } else {
                    edit = setSymbol( closestAtom, elements[0]);
                }
                break;
            }
        }
        if(edit!=null)
            chemModelRelay.execute( edit );
        else
            chemModelRelay.execute( setSymbol( closestAtom, "C" ) );
    }

	public String getDrawModeString() {
		return "Cycle Symbol";
	}
}