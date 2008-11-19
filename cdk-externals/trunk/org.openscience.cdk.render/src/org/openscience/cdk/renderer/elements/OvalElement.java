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

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;


/**
 * @cdk.module  render
 */
public class OvalElement implements IRenderingElement {

    private final Point2D position;
    private final double radius;
    private final Color color;
    private final boolean fill;
    
    public OvalElement(Point pos) {
        this(pos.x,pos.y);              
    }
    
    public OvalElement(double x, double y) {
        this(x,y,10);
    }
    public OvalElement(double x, double y,double radius) {
        this(x, y, radius, defaultColor);
    }
    public OvalElement(double x, double y,double radius,Color color) {
        this(x,y,radius,color,true);
    }
    public OvalElement(double x, double y,double radius,Color color,boolean fill) {
        position = new Point2D.Double(x,y);
        this.radius = radius;
        this.color = color;
        this.fill = fill;
    }

    public double getX() {
        return position.getX();
    }
    
    public double getY() {
        return position.getY();
    }
    
    public double getRadius(){ return radius; }
    public Color getColor() { return color; }
    public boolean isFilled() {return fill;}
    
    public void accept( IRenderingVisitor v ) {
        v.visitOval( this );        
    }
}
