/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.smartsmatching;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;


public class SmartsMatchGenerator implements IGenerator {

    public SmartsMatchGenerator() {

    }
    
    /**
     * Set up the colored circles based on calculated properties = matches
     */
    public IRenderingElement generate( IAtomContainer ac,
                                       RendererModel model ) {

//        System.out.println("Generator found AC: " + ac);

        ElementGroup group = new ElementGroup();
        Object o = ac.getProperty( SmartsMatchingConstants.SMARTS_MATCH_PROPERTY );
        if (o==null) return group;

        //Need to parse the property into something which we can highlight
        List<Integer> highlightList=SmartsMatchingHelper.parseProperty(o);

        for(int i = 0;i<ac.getAtomCount();i++) {  //Loop over all atoms
            for (Integer ii : highlightList){   //Loop over list of atom indices with M2D results
                if (ii.intValue()==i){
                    IAtom atom = ac.getAtom( i );

                    Color drawColor=SmartsMatchingConstants.SMARTS_HIGHLIGHT_COLOR;
                    double radius=SmartsMatchingConstants.SMARTS_HIGHLIGHT_RADIUS;

                    group.add( new OvalElement( atom.getPoint2d().x,
                                                atom.getPoint2d().y,
                                                radius,true, drawColor ));
                }
            }
        }
        
        return group;
    }

    public List<IGeneratorParameter<?>> getParameters() {
        return Collections.emptyList();
    }
}
