/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2008  Arvid Berg <goglepox@users.sf.net>, gilleain
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

import static org.openscience.cdk.CDKConstants.STEREO_BOND_DOWN;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_DOWN_INV;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_NONE;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_UNDEFINED;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_UP_INV;

import java.awt.Color;

import javax.vecmath.Point2d;

import org.apache.log4j.Logger;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement;
import org.openscience.cdk.renderer.elements.LineElement.LineType;
import org.openscience.cdk.renderer.elements.WedgeLineElement.Direction;
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.tools.manipulator.RingSetManipulator;

/**
 * @cdk.module  render
 */
public class BasicBondGenerator extends AbstractGenerator {

    Logger logger = Logger.getLogger( BasicBondGenerator.class );
    IRingSet ringSet;
    double bondWidth;
    double bondLength;
    double bondDistance;
    
    Color bondColor;
    
    public BasicBondGenerator(IAtomContainer ac, Renderer2DModel r2dm) {        
        super(ac,r2dm);
        ringSet = getRingSet(ac);
        bondWidth = rm.getBondWidth();
        bondLength = rm.getBondLength();
        bondDistance = rm.getBondDistance();
        bondColor = rm.getForeColor();
    }
    
    protected IRingSet getRingSet( final IAtomContainer atomContainer ) {

        IRingSet ringSet = atomContainer.getBuilder().newRingSet();
        IMoleculeSet molecules;
        try {
            molecules =
                ConnectivityChecker.partitionIntoMolecules( atomContainer );
        } catch ( Exception exception ) {
            logger.warn( "Could not partition molecule: " 
                           + exception.getMessage() );
            logger.debug( exception );
            return ringSet;
        }

        for ( IAtomContainer mol : molecules.molecules() ) {
            SSSRFinder sssrf = new SSSRFinder( mol );
            ringSet.add( sssrf.findSSSR() );
        }

        return ringSet;
    }

    public IRenderingElement generate( IBond currentBond) {
        Renderer2DModel rendererModel = rm;            
        IRing ring;

        

        bondColor = rendererModel.getColorHash().get(currentBond);
        bondColor = (bondColor !=null?bondColor:rm.getForeColor());

        ring = RingSetManipulator.getHeaviestRing( ringSet, currentBond );
        if ( ring != null ) {            
            return generateRingElements( currentBond, ring );
        } else {
            return generateBond( currentBond );
        }
    }

    LineElement generateBondElement(IBond bond,LineType type) {
        // More than 2 atoms per bond not supported by this module
        if (bond.getAtomCount() > 2)
            return null;

        // is object right? if not replace with a good one
        Point2d p1 = bond.getAtom( 0 ).getPoint2d();
        Point2d p2 = bond.getAtom( 1 ).getPoint2d();

        return new LineElement( p1, p2, type, 
                                bondWidth,
                                bondDistance);
    }

    IRenderingElement generateRingElements(IBond bond, IRing ring) {
        if(isSingle( bond ) && isStereoBond( bond )) {
            return generateStereoElement( bond );
        }
        if(isDouble(bond)) {
            final IRenderingElement e1=generateBondElement( bond, LineType.SINGLE );
            final IRenderingElement e2=generateInnerElement(bond, ring);
            // makes a composite rendering element so you can return two elements
            return new IRenderingElement() {
                public void accept( IRenderingVisitor v ) {               
                    e1.accept( v );
                    e2.accept( v );                    
                }
            };
        }
           
        return generateBondElement( bond, getLineType( bond ) );        
    }
    
    
    private IRenderingElement generateInnerElement(IBond bond, IRing ring ) {
    		Point2d center = GeometryTools.get2DCenter(ring);
    		Point2d a = bond.getAtom(0).getPoint2d();
    		Point2d b = bond.getAtom(1).getPoint2d();
    
    		// the proportion to move in towards the ring center
    		final double DIST = 0.15;
    
    		Point2d w = new Point2d();
    		w.interpolate(a, center, DIST);
    		Point2d u = new Point2d();
    		u.interpolate(b, center, DIST);
    
    		// XXX : uncomment to make the bonds slightly shorter
    		//       double alpha = 0.2;
    		//       Point2d ww = new Point2d();
    		//       ww.interpolate(w, u, alpha);
    		//       Point2d uu = new Point2d();
    		//       uu.interpolate(u, w, alpha);
    		//       return new BondSymbol(uu.x, uu.y, ww.x, ww.y);
    		return new LineElement( u.x, u.y, w.x, w.y,
                                LineType.SINGLE, 
                                bondWidth, 
                                bondDistance);
    	}

    private IRenderingElement generateStereoElement( IBond bond ) {
        
        int stereo = bond.getStereo();
        boolean dashed = false;
        Direction dir = Direction.toSecond;
//        if(stereo == STEREO_BOND_UP || stereo == STEREO_BOND_UP_INV)
//            dashed = false;
        if(stereo == STEREO_BOND_DOWN || stereo == STEREO_BOND_DOWN_INV)
            dashed = true;
        if(stereo == STEREO_BOND_DOWN_INV || stereo == STEREO_BOND_UP_INV)
            dir = Direction.toFirst;
         
        return new WedgeLineElement( generateBondElement( bond, getLineType( bond )),
                                     dashed,
                                     dir);        
    }

    boolean isDouble(IBond bond) {
        return bond.getOrder() == IBond.Order.DOUBLE;
    }

    boolean isSingle(IBond bond) {
        return bond.getOrder() == IBond.Order.SINGLE;
    }

    boolean isStereoBond(IBond bond) {
        return bond.getStereo() != STEREO_BOND_NONE 
               &&
               bond.getStereo() != STEREO_BOND_UNDEFINED; 
    }

    boolean bindsHydrogen(IBond bond) {
        for ( int i = 0; i < bond.getAtomCount(); i++ ) {
            IAtom atom = bond.getAtom( i );
            if( "H".equals( atom.getSymbol() ))
                return true;
        }
        return false;
    }

    LineType getLineType(IBond bond) {
        LineType type;
        switch ( bond.getOrder() ) {
            case SINGLE:
                type = LineType.SINGLE;break;
            case DOUBLE:
                type = LineType.DOUBLE;break;
            case TRIPLE:
                type = LineType.TRIPPLE;break;
            case QUADRUPLE:
                type = LineType.QUADRUPLE;break;
            default:
                type = LineType.SINGLE;
        }
        return type;
    }

    IRenderingElement generateBond(IBond bond) {
        
        if(!rm.getShowExplicitHydrogens() && bindsHydrogen( bond ))
            return null;
        
        if(isStereoBond( bond ))
            return generateStereoElement(bond);
        else
            return generateBondElement( bond, getLineType( bond ) );
    }

    @Override
    public IRenderingElement generate( IAtom atom ) {
    
        return null;
    }
}
