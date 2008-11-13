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
package org.openscience.cdk.renderer.generators;

import java.awt.Color;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.AtomMassSymbolElement;
import org.openscience.cdk.renderer.elements.IRenderingElement;

/**
 * @cdk.module  render
 */
public class ExtraGenerator extends BasicGenerator{
    Logger logger = Logger.getLogger( ExtraGenerator.class );
    public ExtraGenerator(IAtomContainer atomContainer, Renderer2DModel r2dm) {

        super( atomContainer, r2dm );
       
    }

    
    @Override
    IRenderingElement generateElements( IAtom atom, int alignment ) {
        // it would be nice not to have to copy this code from BasicGenerator
        return new AtomMassSymbolElement( atom.getPoint2d().x,
                                          atom.getPoint2d().y, 
                                          atom.getSymbol(),
                                          rm.getAtomColor( atom, Color.BLACK ),
                                          atom.getFormalCharge(),
                                          atom.getHydrogenCount(),
                                          alignment,
                                          atom.getMassNumber());
    }
    
    @Override
    boolean showCarbon( IAtom atom ) {
    
        if(atom.getMassNumber() != null) {
            try {
                if( atom.getMassNumber() != 
                    IsotopeFactory.getInstance( ac.getBuilder() )
                        .getMajorIsotope( atom.getSymbol() ).getMassNumber())
                return true;
            } catch ( IOException e ) {
               logger.warn( e );
            }          
        }
        return super.showCarbon( atom );
    }
}
