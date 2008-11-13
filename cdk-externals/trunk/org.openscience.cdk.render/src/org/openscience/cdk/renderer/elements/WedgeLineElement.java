/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2008  Arvid Berg <goglepox@users.sf.net>
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
package org.openscience.cdk.renderer.elements;

import javax.vecmath.Point2d;

/**
 * @cdk.module  render
 */
public class WedgeLineElement extends LineElement {

    boolean dashed;
    Direction direction;
    
    public enum Direction {
        toFirst,
        toSecond;
    }
    
    public WedgeLineElement( Point2d p1, 
                             Point2d p2, 
                             double width, 
                             double gap,
                             boolean dashed,
                             Direction direction) {
        super(p1,p2,LineType.SINGLE,width,gap);
        this.dashed = dashed;
        this.direction = direction;
    }

    public WedgeLineElement( LineElement element,
                             boolean dashed,
                             Direction direction) {       
        this( new Point2d( (direction==Direction.toFirst?element.x:element.x1),
                           (direction==Direction.toFirst?element.y:element.y1)),
              new Point2d( (direction==Direction.toFirst?element.x1:element.x),
                           (direction==Direction.toFirst?element.y1:element.y)),
              
              element.getWidth(),
              element.getGap(),
              dashed,
              direction);
    }
    
    public boolean isDashed() {
    
        return dashed;
    }
    
    @Override
    public void accept( IRenderingVisitor v ) {
        v.visitWedge( this );
    }
}
