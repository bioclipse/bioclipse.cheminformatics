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

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.AtomSymbolElement;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.validate.ProblemMarker;

/**
 * @cdk.module  render
 */
public class BasicGenerator extends AbstractGenerator {

    public BasicGenerator(IAtomContainer atomContainer,Renderer2DModel r2dm) {
        super(atomContainer,r2dm);
    }
    
    
    
    public IRenderingElement generate(IAtom atom) {
        
       
        // FIXME: pseudoatom from paintAtom
        
        if (isHydrogen( atom ) && !rm.getShowExplicitHydrogens() )
            return null;// don't draw hydrogen
        if (isCarbon(atom))
            if(!showCarbon( atom ))
                return null;// draw carbon
        // if atom.getSymbol() == null
        int alignment = GeometryTools.getBestAlignmentForLabelXY( ac, atom );
        return generateElements(atom,alignment);
    }
    
    IRenderingElement generateElements(IAtom atom, int alignment) {
       return new AtomSymbolElement( atom.getPoint2d().x,atom.getPoint2d().y, 
                               atom.getSymbol(),
                               rm.getAtomColor( atom, Color.BLACK ),
                               atom.getFormalCharge(),
                               atom.getHydrogenCount(),
                               alignment);
        /*
        atom.getSymbol();
        atom.getPoint2d();
        rm.getAtomColor( atom, Color.BLACK );
        atom.getFormalCharge();
        atom.getHydrogenCount();
        */
        
        
        
        
    }   
    
    boolean isHydrogen(IAtom atom) {
        return "H".equals( atom.getSymbol() );
    }
    
    boolean isCarbon(IAtom atom) {
        return "C".equals( atom.getSymbol() );
    }
    
    boolean showCarbon(IAtom atom) {
        
        if(rm.getKekuleStructure()) return true;
        
        if(atom.getFormalCharge() != 0) return true;
        
        if(ac.getConnectedBondsList( atom ).size() < 1) return true;
        
        if( rm.getShowEndCarbons() 
             && ac.getConnectedBondsList( atom ).size() == 1) return true;
        
        if(atom.getProperty( ProblemMarker.ERROR_MARKER )!= null ) return true;
        
        if( ac.getConnectedSingleElectronsCount( atom )>0 ) return true;
        
        return false;
    }
}
