/* $Revision$ 
 * $Author$ 
 * $Date$ 
 * Copyright (C) 2008 Arvid Berg <goglepox@users.sf.net> 
 * Contact: cdk-devel@list.sourceforge.net 
 * This program
 * is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.renderer.elements;

import java.awt.Color;
import javax.vecmath.Point2d;

/**
 * @cdk.module  render
 */
public class LineElement implements IRenderingElement {

    double   x, y, x1, y1;
    LineType type;
    double width, gap;
    
    Color color;
    
    public LineElement(Point2d p1, Point2d p2, LineType type, 
                                               double width,
                                               double gap) {

        this(p1.x, p1.y, p2.x, p2.y, type, width, gap);
    }
    
    public LineElement(double x, double y, double x1, double y1, LineType type,
                       double width,
                       double gap) {
        this(x,y,x1,y1,type,width,gap,defaultColor);
    }
    public LineElement(double x, double y, double x1, double y1, LineType type,
                       double width,
                       double gap,Color color) {
        this.x = x;
        this.y = y;
        this.x1 = x1;
        this.y1 = y1;
        this.type = type;
        this.width = width;
        this.gap = gap;
        this.color = color;
    }

    public double getX() {

        return x;
    }

    public double getX1() {

        return x1;
    }

    public double getY() {

        return y;
    }

    public double getY1() {

        return y1;
    }

    public LineType type() {

        return type;
    }
    
    
    public double getWidth() {
    
        return width;
    }

    
    public double getGap() {
    
        return gap;
    }
    
    public Color getColor() {
    
        return color;
    }

    public void accept( IRenderingVisitor v ) {

        v.visitLine( this );
    }

    public enum LineType {
        SINGLE(1), DOUBLE(2), TRIPPLE(3), QUADRUPLE(4);

        int n;

        private LineType(int n) {

            this.n = n;
        }

        public int count() {

            return n;
        }
    }
}
