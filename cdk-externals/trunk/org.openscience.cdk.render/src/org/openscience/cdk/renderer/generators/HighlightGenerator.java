package org.openscience.cdk.renderer.generators;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.RingElement;
import org.openscience.cdk.renderer.elements.LineElement.LineType;


public class HighlightGenerator extends AbstractGenerator{

    public HighlightGenerator(IAtomContainer ac, Renderer2DModel r2dm) {
        super( ac, r2dm );       
    }

    @Override
    public IRenderingElement generate( IAtom atom ) {

        if( rm.getHighlightedAtom()!=null 
            && rm.getHighlightedAtom().equals( atom )) {
            return new RingElement( atom.getPoint2d(),
                                    rm.getAtomRadius(),
                                    rm.getHoverOverColor());
        }
        return null;
    }
    
    public IRenderingElement generate( IBond bond) {
        
        if( rm.getHighlightedBond()!=null 
            && rm.getHighlightedBond().equals( bond )) {
            // i would like to create a bond element like it would be crated
            // and to some stuff with it
            return new LineElement( bond.getAtom( 0 ).getPoint2d(),
                             bond.getAtom( 1 ).getPoint2d(),
                             LineType.SINGLE,rm.getBondWidth()*3,
                             rm.getBondDistance(),
                             rm.getHoverOverColor());
            
                             
        }
        return null;
    }
}
