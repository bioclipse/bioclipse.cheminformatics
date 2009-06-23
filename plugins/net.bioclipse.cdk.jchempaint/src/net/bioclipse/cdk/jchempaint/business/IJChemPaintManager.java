/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Egon Willighagen
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.business;

import javax.vecmath.Point2d;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;

/**
 * Manager for the JChemPaintEditor scripting language.
 * 
 * @author egonw
 */
@PublishedClass ("Manager for the JChemPaintEditor scripting language." )
@TestClasses("net.bioclipse.cdk.jchempaint.business.test.jcp.APITest")
public interface IJChemPaintManager extends IBioclipseManager {
    
    @PublishedMethod ( params = "String path",
                       methodSummary = "set the zoom factor" )
    public void snapshot(String path);                      
    
    @Recorded
    @PublishedMethod ( params = "double zoom",
                       methodSummary = "set the zoom factor" )
    public void setZoom(double zoom);
    
    @Recorded
    @PublishedMethod ( params = "double margin",
                       methodSummary = "set the margin of the diagram" )
    public void setMargin(double margin);
    
    @Recorded
    @PublishedMethod ( params = "double wedgeWidth",
                       methodSummary = "set the on-screen width of a wedge" )
    public void setWedgeWidth(double wedgeWidth);
    
    @Recorded
    @PublishedMethod ( params = "boolean setDrawNumbers",
                       methodSummary = "set to show atom numbers" )
    public void setDrawNumbers(boolean setDrawNumbers);
    
    @Recorded
    @PublishedMethod ( params = "boolean explicitHydrogens",
                       methodSummary = "set to show hydrogens explicitly" )
    public void setShowExplicitHydrogens(boolean explicitHydrogens);
    
    @Recorded
    @PublishedMethod ( params = "boolean implicitHydrogens",
                       methodSummary = "set to show hydrogens implicitly" )
    public void setShowImplicitHydrogens(boolean implicitHydrogens);
    
    @Recorded
    @PublishedMethod ( params = "boolean showEndCarbons",
                       methodSummary = "set to show explicit methyl groups" )
    public void setShowEndCarbons(boolean showEndCarbons);
    
    @Recorded
    @PublishedMethod ( params = "boolean showAromaticityCDK",
                       methodSummary = "set to true for CDK indicators" )
    public void setShowAromaticityInCDKStyle(boolean showAromaticityCDK);    
    
    @Recorded
    @PublishedMethod ( params = "boolean showAromaticity",
                       methodSummary = "set to true if aromatic indicators on" )
    public void setShowAromaticity(boolean showAromaticity);
    
    @Recorded
    @PublishedMethod ( params = "double ringProportion",
                       methodSummary = "set the position of inner-ring bonds" )
    public void setRingProportion(double ringProportion);

    @Recorded
    @PublishedMethod ( params = "double highlightDistance",
                       methodSummary = "set the distance to highlight within" )
    public void setHighlightDistance(double highlightDistance);
    
    @Recorded
    @PublishedMethod ( params = "boolean fitToScreen",
                       methodSummary = "set the diagram to fit the screen" )
    public void setFitToScreen(boolean fitToScreen);

    @Recorded
    @PublishedMethod ( params = "double bondWidth",
                       methodSummary = "set the width of bonds" )
    public void setBondWidth(double bondWidth);
  
    @Recorded
    @PublishedMethod ( params = "double bondDistance",
                       methodSummary = "set distance between multiple bonds")
    public void setBondDistance(double bondDistance);
    
    @Recorded
    @PublishedMethod ( params = "boolean isCompact",
                       methodSummary = "set to true if atoms are to be compact")
    public void setIsCompact(boolean isCompact);

    @Recorded
    @PublishedMethod ( params = "double bondLength",
                       methodSummary = "set the standard bond length" )
    public void setBondLength(double bondLength);
    
    @Recorded
    @PublishedMethod ( params = "double atomRadius",
                       methodSummary = "set the radius of an atom symbol" )
    public void setAtomRadius(double atomRadius);
    
    @Recorded
    @PublishedMethod ( methodSummary = "get the zoom factor" )
    public double getZoom();
    
    @Recorded
    @PublishedMethod ( methodSummary = "get the margin of the diagram" )
    public double getMargin();

    @Recorded
    @PublishedMethod ( methodSummary = "get the on-screen width of a wedge" )
    public double getWedgeWidth();
    
    @Recorded
    @PublishedMethod ( methodSummary = "showing atom numbers" )
    public boolean getDrawNumbers();
    
    @Recorded
    @PublishedMethod ( methodSummary = "get to show hydrogens explicitly" )
    public boolean getShowExplicitHydrogens();
    
    @Recorded
    @PublishedMethod ( methodSummary = "get to show hydrogens implicitly" )
    public boolean getShowImplicitHydrogens();
    
    @Recorded
    @PublishedMethod ( methodSummary = "true if showing explicit methyl groups")
    public boolean getShowEndCarbons();
    
    @Recorded
    @PublishedMethod ( methodSummary = "true if CDK indicators are shown" )
    public boolean getShowAromaticityInCDKStyle();    
    
    @Recorded
    @PublishedMethod ( methodSummary = "true if aromatic indicators on" )
    public boolean getShowAromaticity();
    
    @Recorded
    @PublishedMethod ( methodSummary = "get the position of inner-ring bonds" )
    public double getRingProportion();

    @Recorded
    @PublishedMethod ( methodSummary = "get the distance to highlight within" )
    public double getHighlightDistance();
    
    @Recorded
    @PublishedMethod ( methodSummary = "get if the diagram fits the screen" )
    public boolean getFitToScreen();

    @Recorded
    @PublishedMethod ( methodSummary = "get the width of bonds" )
    public double getBondWidth();
  
    @Recorded
    @PublishedMethod ( methodSummary = "get distance between multiple bonds")
    public double getBondDistance();
    
    @Recorded
    @PublishedMethod ( methodSummary = "get to true if atoms are to be compact")
    public boolean getIsCompact();

    @Recorded
    @PublishedMethod ( methodSummary = "get the standard bond length" )
    public double getBondLength();
    
    @Recorded
    @PublishedMethod ( methodSummary = "get the radius of an atom symbol" )
    public double getAtomRadius();

    @Recorded
    @PublishedMethod ( params = "Point2d atomRadius",
                       methodSummary = "Returns the IAtom closest to the world coordinate." )
    public IAtom getClosestAtom(Point2d worldCoord);

    @Recorded
    @PublishedMethod ( methodSummary = "Returns the ICDKMolecule of the active JChemPaint editor." )
    public ICDKMolecule getModel() throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( methodSummary = "Sets the ICDKMolecule of the active JChemPaint editor." )
    public void setModel(ICDKMolecule molecule) throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "IAtom atom",
                       methodSummary = "Removes an IAtom from the data model." )
    public void removeAtom(IAtom atom) throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "IBond bond",
                       methodSummary = "Removes a IBond from the data model." )
    public void removeBond(IBond bond) throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "Point2d worldCoord",
                       methodSummary = "Returns the IBond closest to the world coordinate." )
    public IBond getClosestBond(Point2d worldCoord);

    @Recorded
    @PublishedMethod(
         methodSummary = "Refreshes the JChemPaint view screen."
    )
    public void updateView();

    @Recorded
    @PublishedMethod(
         params = "String elementSymbol, Point2d worldcoord",
         methodSummary = "Adds a new atom at the given coordinates."
    )
    public IAtom addAtom(String elementSymbol, Point2d worldcoord);

    @Recorded
    @PublishedMethod(
         params = "String elementSymbol, IAtom atom",
         methodSummary = "Adds a new atom bonded to the given atom."
    )
    public IAtom addAtom(String elementSymbol, IAtom atom);

    @Recorded
    @PublishedMethod(
         params = "double x, double y",
         methodSummary = "Creates a new javax.vecmath.Point2d."
    )
    public Point2d newPoint2d(double x, double y);

    @Recorded
    @PublishedMethod(
         methodSummary = "Updates the implicit hydrogen counts, given the " +
                         "given the current connectivity."
    )
    public void updateImplicitHydrogenCounts();

    @Recorded
    @PublishedMethod(
         methodSummary = "Adds explicit hydrogens, given the " +
                         "given the current connectivity."
    )
    public void addExplicitHydrogens();
    
    @Recorded
    @PublishedMethod( methodSummary = "Removes explicit hydrogens, "+
                                      "given the current connectivity")
    public void removeExplicitHydrogens();

    @Recorded
    @PublishedMethod(
         params = "IAtom atom, Point2d point",
         methodSummary = "Moves an atom to the given location."
    )
    public void moveTo(IAtom atom, Point2d point);

    @Recorded
    @PublishedMethod(
         params = "IAtom atom, String symbol",
         methodSummary = "Changes the element of this atom."
    )
    public void setSymbol(IAtom atom, String symbol);

    @Recorded
    @PublishedMethod(
         params = "IAtom atom, int charge",
         methodSummary = "Changes the formal charge of this atom."
    )
    public void setCharge(IAtom atom, int charge);

    @Recorded
    @PublishedMethod(
         params = "IAtom atom, int charge",
         methodSummary = "Changes the mass number of this element."
    )
    public void setMassNumber(IAtom atom, int massNumber);

    @Recorded
    @PublishedMethod(
         params = "IAtom atom, int charge",
         methodSummary = "Create a new bond between the two given atoms."
    )
    public IBond addBond(IAtom fromAtom, IAtom toAtom);

    @Recorded
    @PublishedMethod(
         params = "IBond bond, Point2d point",
         methodSummary = "Moves the center of the bond to the new point."
    )
    public void moveTo(IBond bond, Point2d point);

    @Recorded
    @PublishedMethod(
         params = "IBond bond, IBond.Order order",
         methodSummary = "Changes the order of the bond."
    )
    public void setOrder(IBond bond, IBond.Order order);

    @Recorded
    @PublishedMethod(
         params = "IBond bond, int type",
         methodSummary = "Changes the wedge type of the bond."
    )
    public void setWedgeType(IBond bond, int type);
    
    @Recorded
    @PublishedMethod(
         params = "int order",
         methodSummary = "Returns a IBond.Order matching the given order."
    )
    public IBond.Order getBondOrder(int order);

    @Recorded
    @PublishedMethod(
         methodSummary = "Deletes all atoms and bonds."
    )
    public void zap();

    @Recorded
    @PublishedMethod(
         methodSummary = "Recalculates 2D coordinates for the complete molecule."
    )
    public void cleanup();

    @Recorded
    @PublishedMethod(
         params = "IAtom atom, int size",
         methodSummary = "Adds a carbon ring of the given size to the given atom."
    )
    public void addRing(IAtom atom, int size);

    @Recorded
    @PublishedMethod(
         params = "IBond bond, int size",
         methodSummary = "Adds a carbon ring of the given size fused with " +
                         "the given bond."
    )
    public void addRing(IBond bond, int size);

    @Recorded
    @PublishedMethod(
         params = "IAtom atom",
         methodSummary = "Adds a phenyl ring to the given atom."
    )
    public void addPhenyl(IAtom atom);

    @Recorded
    @PublishedMethod(
         params = "IBond bond",
         methodSummary = "Adds a phenyl ring fused with the given bond."
    )
    public void addPhenyl(IBond bond);

}
