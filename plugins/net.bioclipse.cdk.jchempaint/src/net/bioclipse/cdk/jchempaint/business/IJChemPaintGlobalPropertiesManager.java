/*******************************************************************************
 * Copyright (c) 2008-2009  Egon Willighagen <egonw@users.sf.net>
 *                    2009  Gilleain Torrance
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.business;

import org.openscience.cdk.renderer.RendererModel;

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;

/**
 * Manager for the JChemPaintEditor Global Properties scripting language.
 * 
 * @author egonw
 */
@PublishedClass ("Manager for the JChemPaintEditor Properties scripting language." )
@TestClasses("net.bioclipse.cdk.jchempaint.business.test.jcpglobal.APITest")
public interface IJChemPaintGlobalPropertiesManager extends IBioclipseManager {
    
    public void applyProperties(RendererModel model) throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( methodSummary = "applies the global properties to all " +
    "opened JChemPaint editors" )
    public void applyGlobalProperties() throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "boolean showAromaticity", 
                       methodSummary = "Sets whether aromatic indicators " +
                       		           "are on" )
    public void setShowAromaticity(boolean showAromaticity)
    throws BioclipseException;

    @PublishedMethod ( methodSummary = "Returns whether aromatic indicators " +
    		                           "are on" )
    public boolean getShowAromaticity()
    throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "boolean showEndCarbons", 
                       methodSummary = "Sets whether explicit methyl groups " +
                       		           "are shown" )
    public void setShowEndCarbons(boolean showEndCarbons)
    throws BioclipseException;
    
    @PublishedMethod ( methodSummary = "Returns whether explicit methyl " +
    		                           "groups are shown" )
    public boolean getShowEndCarbons()
    throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "boolean showExplicitHydrogens", 
                       methodSummary = "Sets whether explicit hydrogens " +
                       	 	           "are shown" )
    public void setShowExplicitHydrogens(boolean showExplicitHydrogens)
    throws BioclipseException;
    
    @PublishedMethod ( methodSummary = "Returns whether explicit hydrogens" +
                                        " are shown." )
    public boolean getShowExplicitHydrogens()
    throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "boolean showImplicitHydrogens", 
                       methodSummary = "Sets whether implicit hydrogens are " +
                       		           "shown" )
    public void setShowImplicitHydrogens(boolean showImplicitHydrogens)
    throws BioclipseException;
    
    @PublishedMethod ( methodSummary = "Returns whether implicit hydrogens" +
    		                           "are shown" )
    public boolean getShowImplicitHydrogens()
    throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "boolean showNumbers", 
                       methodSummary = "Sets whether numbers on atoms are " +
                       		           "shown." )
    public void setShowNumbers(boolean showNumbers)
    throws BioclipseException;
    
    @PublishedMethod ( methodSummary = "Returns whether numbers on atoms are " +
    		                           "shown" )
    public boolean getShowNumbers()
    throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "double atomRadius", 
                       methodSummary = "Sets the radius of a compact atom")
    public void setAtomRadius(double atomRadius) throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "Returns the radius of a compact atom" )
    public double getAtomRadius() throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "souble bondLength", 
                       methodSummary = "Sets the length of a standard bond")
    public void setBondLength(double bondLength) throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "Returns the length of a standard bond" )
    public double getBondLength() throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "double bondDistance", 
                       methodSummary = "Sets the distance between double bonds")
    public void setBondDistance(double bondDistance) throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "Returns the length of a standard bond" )
    public double getBondDistance() throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = " double highlightDistance", 
                       methodSummary = "Sets the distance withing which a " +
                       		           "bond or atoms is highlighted" )
    public void setHighlightDistance(double highlightDistance) 
    throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "Returns the length of a standard bond" )
    public double getHighlightDistance() throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "Returns the margin size" )
    public double getMargin() throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "double margin", 
                       methodSummary = "Sets the amount of whitespace around" +
                       		           "the diagram" )
    public void setMargin(double margin) throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "double wedgeWidth", 
                       methodSummary = "Sets the width on screen of a wedge")
    public void setWedgeWidth(double wedgeWidth) 
    throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "Returns the width of a stereo wedge" )
    public double getWedgeWidth() throws BioclipseException;
}
