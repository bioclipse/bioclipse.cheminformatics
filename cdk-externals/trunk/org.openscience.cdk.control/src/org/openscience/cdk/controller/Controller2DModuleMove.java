/* $Revision: 7636 $ $Author: nielsout $ $Date: 2007-01-04 18:46:10 +0100 (Thu, 04 Jan 2007) $
 *
 * Copyright (C) 2007  Niels Out <nielsout@users.sf.net>
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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.renderer.IJava2DRenderer;

import sun.security.acl.WorldGroupImpl;


/**
 * Demo IController2DModule.
 * -write picture to file on doubleclick
 * -show atom name on hove-over
 * -drags atoms around (click near atom and move mouse)
 * 
 * @author Niels Out
 * @cdk.svnrev  $Revision: 9162 $
 * @cdk.module  control
 */
public class Controller2DModuleMove implements IController2DModule {

    enum Type {
        BOND,ATOM,NONE
    }
    
	private IChemModelRelay chemObjectRelay;
	IAtom atom;
	IBond bond;
	Vector2d offset;
	Type type;
	/*private IViewEventRelay eventRelay;
	public void setEventRelay(IViewEventRelay relay) {
		this.eventRelay = relay;
	}*/
	
	public void mouseClickedDouble(Point2d worldCoord) {
		
	}

	private Type getClosest(IAtom atom,IBond bond,Point2d worldCoord) {
	    if(atom == null && bond == null) return Type.NONE;
	    if(atom!= null && bond != null) {
	        double atomDist = atom.getPoint2d().distance( worldCoord );
	        double bondDist = bond.get2DCenter().distance( worldCoord );
	        if(bondDist >= atomDist)
	            return Type.ATOM;
	        else return Type.BOND;
	    }
	    if(atom != null) 
	        return Type.ATOM;
	    else
	        return Type.BOND;
	}
	
	public void mouseClickedDown(Point2d worldCoord) {
	
		Point2d current=null;
		atom = chemObjectRelay.getClosestAtom( worldCoord );
		bond = chemObjectRelay.getClosestBond( worldCoord );
		
		type = getClosest( atom, bond, worldCoord );
		switch(type) {
		    case ATOM: current = atom.getPoint2d();bond=null;break;
		    case BOND: current = bond.get2DCenter();atom=null;break;
		    default : return;		    
		}
		
		offset = new Vector2d();
		offset.sub( current, worldCoord );
		
	}

	
	
	public void mouseClickedUp(Point2d worldCoord) {
	    type=Type.NONE;
	    atom=null;
	    bond=null;
	    offset=null;
	    
	}

	public void mouseDrag(Point2d worldCoordFrom, Point2d worldCoordTo) {
		// TODO Auto-generated method stub
//		System.out.println("mousedrag at DumpClosestObject shizzle");
//		System.out.println("From: " + worldCoordFrom.x + "/" + worldCoordFrom.y + " to " +
//				worldCoordTo.x + "/" + worldCoordTo.y);
		
		if (chemObjectRelay != null && offset!=null) {
//			IAtom atom = chemObjectRelay.getClosestAtom(worldCoordFrom);
			
				//System.out.println("Dragging atom: " + atom);
				
				Point2d atomCoord = new Point2d();
				atomCoord.add( worldCoordTo, offset );
				switch(type) {
				    case ATOM: chemObjectRelay.moveTo( atom, atomCoord );break;
				    case BOND: chemObjectRelay.moveTo( bond, atomCoord );break;
				    default: return;
				}
				
				chemObjectRelay.updateView();
				
			
		} else {
			System.out.println("chemObjectRelay is NULL!");
		}
	}

	public void mouseEnter(Point2d worldCoord) {
		// TODO Auto-generated method stub
		
	} 

	public void mouseExit(Point2d worldCoord) {
		// TODO Auto-generated method stub
		
	}

	public void mouseMove(Point2d worldCoord) {
		if (chemObjectRelay != null) {
			IAtom atom = chemObjectRelay.getClosestAtom(worldCoord);
			if (atom != null) {
				//System.out.println("Found atom: " + atom);
			}
		} else {
			System.out.println("chemObjectRelay is NULL!");
		}
	}

	public void setChemModelRelay(IChemModelRelay relay) {
		this.chemObjectRelay = relay;
	}
	
}
