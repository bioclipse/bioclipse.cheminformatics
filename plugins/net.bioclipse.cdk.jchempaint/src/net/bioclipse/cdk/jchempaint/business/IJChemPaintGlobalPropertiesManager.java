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

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IBioclipseManager;

/**
 * Manager for the JChemPaintEditor Global Properties scripting language.
 * 
 * @author egonw
 */
@PublishedClass ("Manager for the JChemPaintEditor Properties scripting language." )
@TestClasses("net.bioclipse.cdk.jchempaint.business.test.JChemPaintManagerTest")
public interface IJChemPaintGlobalPropertiesManager extends IBioclipseManager {
    
    @Recorded
    @PublishedMethod ( methodSummary = "applies the global properties to all" +
    "opened JChemPaint editors" )
    public void applyGlobalProperties() throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "boolean true if the atoms are shown compact", 
                       methodSummary = "set to true if atoms are compact" )
    public void setIsCompact(boolean isCompact)
    throws BioclipseException;

    @PublishedMethod ( methodSummary = "true if the atoms are shown as compact")
    public boolean getIsCompact()
    throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "boolean true if aromatic indicators shown", 
                       methodSummary = "set to true if aromatic indicators on" )
    public void setShowAromaticity(boolean showAromaticity)
    throws BioclipseException;

    @PublishedMethod ( methodSummary = "true if aromatic indicators on" )
    public boolean getShowAromaticity()
    throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "boolean true if methyl groups shown explicitly", 
                       methodSummary = "set to show explicit methyl groups" )
    public void setShowEndCarbons(boolean showEndCarbons)
    throws BioclipseException;
    
    @PublishedMethod ( methodSummary = "true if showing explicit methyl groups")
    public boolean getShowEndCarbons()
    throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "boolean true if hydrogens shown explicitly", 
                       methodSummary = "set to show explicit hydrogens" )
    public void setShowExplicitHydrogens(boolean showExplicitHydrogens)
    throws BioclipseException;
    
    @PublishedMethod ( methodSummary = "true if showing explicit hydrogens")
    public boolean getShowExplicitHydrogens()
    throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "boolean true if implicit hydrogens shown", 
                       methodSummary = "set to show implicit hydrogens" )
    public void setShowImplicitHydrogens(boolean showImplicitHydrogens)
    throws BioclipseException;
    
    @PublishedMethod ( methodSummary = "true if showing implicit hydrogens")
    public boolean getShowImplicitHydrogens()
    throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "atomRadius the radius of a compact atom", 
                       methodSummary = "set the radius of a compact atom")
    public void setAtomRadius(double atomRadius) throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "returns the radius of a compact atom" )
    public double getAtomRadius() throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "bondLength the length of a standard bond", 
                       methodSummary = "set the length of a standard bond")
    public void setBondLength(double bondLength) throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "returns the length of a standard bond" )
    public double getBondLength() throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "bondDistance the distance between bonds", 
                       methodSummary = "set the distance between double bonds")
    public void setBondDistance(double bondDistance) throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "returns the length of a standard bond" )
    public double getBondDistance() throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "highlightDistance the distance within which " +
    		"an atom or bond is highlighted", 
                       methodSummary = "set the highlight distance")
    public void setHighlightDistance(double highlightDistance) 
    throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "returns the length of a standard bond" )
    public double getHighlightDistance() throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "returns the margin size" )
    public double getMargin() throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "margin whitespace margin size", 
                       methodSummary = "set the amount of whitespace around" +
                       		"the diagram" )
    public void setMargin(double margin) throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "wedge width on screen of the fat end of a" +
            "stereo wedge", 
                       methodSummary = "set the width on screen of a wedge")
    public void setWedgeWidth(double wedgeWidth) 
    throws BioclipseException;
    
    
    @PublishedMethod ( methodSummary = "returns the width of a stereo wedge" )
    public double getWedgeWidth() throws BioclipseException;
}
