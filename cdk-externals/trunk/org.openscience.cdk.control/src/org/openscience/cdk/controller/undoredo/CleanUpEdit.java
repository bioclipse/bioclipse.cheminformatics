/* $RCSfile$
 * $Author: egonw $
 * $Date: 2008-05-12 07:29:49 +0100 (Mon, 12 May 2008) $
 * $Revision: 10979 $
 *
 * Copyright (C) 2005-2007  The Chemistry Development Kit (CDK) project
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
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
package org.openscience.cdk.controller.undoredo;

import java.util.Map;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;

/**
 * Undo/Redo Edit class for the CleanUpAction using the ChangeCoordsEdit
 * superclass for providing undo/redo functionality
 * 
 * @cdk.module control
 * @cdk.svnrev  $Revision: 10979 $
 */
public class CleanUpEdit extends ChangeCoordsEdit {

    private static final long serialVersionUID = 8640399009338470686L;

    /**
	 * @param atomCoordsMap
	 *            A HashMap containing the changed atoms as key and an Array
	 *            with the former and the changed coordinates as Point2ds
	 */
	public CleanUpEdit(Map<IAtom, Point2d[]> atomCoordsMap, String type) {
		super(atomCoordsMap, type);
	}
}
