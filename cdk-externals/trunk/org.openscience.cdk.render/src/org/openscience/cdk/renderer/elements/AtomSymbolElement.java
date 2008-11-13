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

/**
 * @cdk.module  render
 */
public class AtomSymbolElement extends TextElement {
    
    int formalCharge;
    int hydrogenCount;
    int alignment;
    
    public AtomSymbolElement( double x, double y, 
                              String symbol, 
                              Color color, 
                              Integer formalCharge, 
                              Integer hydrogenCount,
                              int alignment) {

        super(x,y,symbol,color);
        this.formalCharge = formalCharge!=null?formalCharge:-1; 
        this.hydrogenCount = hydrogenCount!=null?hydrogenCount:-1;
        this.alignment = alignment;
    }

    @Override
    public void accept( IRenderingVisitor v ) {
    
        // FIXME: implement AtomSymbolRendering
        //v.visitAtomSymbol( this );
        super.accept( v );
    }
    
    public int getFormalCharge() {
    
        return formalCharge;
    }

    
    public int getHydrogenCount() {
    
        return hydrogenCount;
    }
    
}
