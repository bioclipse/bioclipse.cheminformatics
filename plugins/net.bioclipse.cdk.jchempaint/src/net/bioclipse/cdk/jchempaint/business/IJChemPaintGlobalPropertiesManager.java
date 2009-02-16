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
    @PublishedMethod ( params = "boolean true if methyl groups shown explicitly", 
                       methodSummary = "set to show explicit methyl groups" )
    public void setShowEndCarbons(boolean showEndCarbons)
    throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "boolean true if aromatic indicators shown", 
                       methodSummary = "set to true if aromatic indicators on" )
    public void setShowAromaticity(boolean showAromaticity)
    throws BioclipseException;
    
    @PublishedMethod ( methodSummary = "true if showing explicit methyl groups")
    public boolean getShowEndCarbons()
    throws BioclipseException;
    
    @PublishedMethod ( methodSummary = "true if aromatic indicators on" )
    public boolean getShowAromaticity()
    throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( methodSummary = "applies the global properties to all" +
    		"opened JChemPaint editors" )
    public void applyGlobalProperties() throws BioclipseException;
}
