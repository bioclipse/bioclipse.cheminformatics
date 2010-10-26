/* Copyright (c) 2008 The Bioclipse Project and others.
 *               2010  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Egon Willighagen
 */
package net.bioclipse.cdk.jchempaint.business;

import javax.vecmath.Point2d;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.api.BioclipseException;
import net.bioclipse.core.api.Recorded;
import net.bioclipse.core.api.managers.IBioclipseManager;
import net.bioclipse.core.api.managers.PublishedClass;
import net.bioclipse.core.api.managers.PublishedMethod;
import net.bioclipse.core.api.managers.TestClasses;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;

/**
 * Manager for the JChemPaintEditor scripting language.
 * 
 * @author egonw
 */
@PublishedClass ("Manager for the JChemPaintEditor scripting language." )
@TestClasses("net.bioclipse.cdk.jchempaint.business.test.jcp.APITest")
public interface IJChemPaintManager extends IBioclipseManager {
    
    @PublishedMethod ( params = "String path",
                       methodSummary = "Takes a snapshot." )
    public void snapshot(String path);                      
    
    @Recorded
    @PublishedMethod ( params = "double zoom",
                       methodSummary = "Sets the zoom factor." )
    public void setZoom(double zoom);
    
    @Recorded
    @PublishedMethod ( params = "double margin",
                       methodSummary = "Sets the margin of the diagram." )
    public void setMargin(double margin);
    
    @Recorded
    @PublishedMethod ( params = "double wedgeWidth",
                       methodSummary = "Sets the on-screen width of a wedge." )
    public void setWedgeWidth(double wedgeWidth);
    
    @Recorded
    @PublishedMethod ( params = "boolean setDrawNumbers",
                       methodSummary = "Sets whether atom numbers are shown." )
    public void setDrawNumbers(boolean setDrawNumbers);
    
    @Recorded
    @PublishedMethod ( params = "boolean explicitHydrogens",
                       methodSummary = "Sets whether explicit hydrogens" +
                       		           " are shown." )
    public void setShowExplicitHydrogens(boolean explicitHydrogens);
    
    @Recorded
    @PublishedMethod ( params = "boolean implicitHydrogens",
                       methodSummary = "Sets whether implicit hydrogens are " +
                       		           "shown." )
    public void setShowImplicitHydrogens(boolean implicitHydrogens);
    
    @Recorded
    @PublishedMethod ( params = "boolean showEndCarbons",
                       methodSummary = "Sets whether explicit methyl groups " +
                       		           "at the end of chains are shown." )
    public void setShowEndCarbons(boolean showEndCarbons);
    
    @Recorded
    @PublishedMethod ( params = "boolean showAromaticityCDK",
                       methodSummary = "Sets whether aromaticity are shown " +
                       		           "in CDK style." )
    public void setShowAromaticityInCDKStyle(boolean showAromaticityCDK);    
    
    @Recorded
    @PublishedMethod ( params = "boolean showAromaticity",
                       methodSummary = "Sets whether aromatic indicators are " +
                       		           "on." )
    public void setShowAromaticity(boolean showAromaticity);
    
    @Recorded
    @PublishedMethod ( params = "double ringProportion",
                       methodSummary = "Sets the position of inner-ring " +
                       		           "bonds." )
    public void setRingProportion(double ringProportion);

    @Recorded
    @PublishedMethod ( params = "double highlightDistance",
                       methodSummary = "Sets the distance within which " +
                       		           "highlighting occurs for atoms." )
    public void setHighlightAtomDistance(double highlightDistance);
    
    @Recorded
    @PublishedMethod ( params = "double highlightDistance",
                       methodSummary = "Sets the distance within which " +
                       		           "highlighting occurs for bonds." )
    public void setHighlightBondDistance(double highlightDistance);
    
    @Recorded
    @PublishedMethod ( params = "boolean fitToScreen",
                       methodSummary = "Resizes the diagram to fit the " +
                       		           "screen." )
    public void setFitToScreen(boolean fitToScreen);

    @Recorded
    @PublishedMethod ( params = "double bondWidth",
                       methodSummary = "Sets the width of bonds." )
    public void setBondWidth(double bondWidth);
  
    @Recorded
    @PublishedMethod ( params = "double bondDistance",
                       methodSummary = "Sets distance between lines in" +
                       		" multiple bonds.")
    public void setBondDistance(double bondDistance);
    
    @Recorded
    @PublishedMethod ( params = "boolean isCompact",
                       methodSummary = "Sets whether atoms are drawn as " +
                       		           "compact." )
    public void setIsCompact(boolean isCompact);

    @Recorded
    @PublishedMethod ( params = "double bondLength",
                       methodSummary = "Sets the standard bond length." )
    public void setBondLength(double bondLength);
    
    @Recorded
    @PublishedMethod ( params = "double atomRadius",
                       methodSummary = "Sets the radius of an atom symbol." )
    public void setAtomRadius(double atomRadius);
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns the zoom factor." )
    public double getZoom();
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns the margin of the diagram." )
    public double getMargin();

    @Recorded
    @PublishedMethod ( methodSummary = "Returns the on-screen width of a " +
    		                           "wedge." )
    public double getWedgeWidth();
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns whether atom numbers are " +
    		                           "showed." )
    public boolean getDrawNumbers();
    
    @Recorded
    @PublishedMethod (methodSummary = "Returns whether explicit hydrogens are " +
    		                          "shown.")
    public boolean getShowExplicitHydrogens();
    
    @Recorded
    @PublishedMethod (methodSummary = "Returns whether implicit hydrogens are " +
    		                          "shown.")
    public boolean getShowImplicitHydrogens();
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns whether explicit methyl " +
    		                           "groups are shown." )
    public boolean getShowEndCarbons();
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns whether CDK-style aromiticity " +
    		                           "is used." )
    public boolean getShowAromaticityInCDKStyle();    
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns whether aromatic indicators " +
    		                           "are on." )
    public boolean getShowAromaticity();
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns the position of inner-ring " +
    		                           "bonds." )
    public double getRingProportion();

    @Recorded
    @PublishedMethod ( methodSummary = "Returns the distance to within which " +
    		                           "highlighting occurs for atoms." )
    public double getHighlightAtomDistance();
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns the distance to within which " +
    		                           "highlighting occurs for bonds." )
    public double getHighlightBondDistance();
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns whether the diagram fits the " +
    		                           "screen." )
    public boolean getFitToScreen();

    @Recorded
    @PublishedMethod ( methodSummary = "Returns the width of bonds." )
    public double getBondWidth();
  
    @Recorded
    @PublishedMethod ( methodSummary = "Returns the distance between " +
    		                           "multiple bonds.")
    public double getBondDistance();
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns whether atoms are compact.")
    public boolean getIsCompact();

    @Recorded
    @PublishedMethod ( methodSummary = "Returns the standard bond length." )
    public double getBondLength();
    
    @Recorded
    @PublishedMethod ( methodSummary = "Returns the radius of an atom symbol." )
    public double getAtomRadius();

    @Recorded
    @PublishedMethod ( params = "Point2d atomRadius",
                       methodSummary = "Returns the IAtom closest to the " +
                       		           "given world coordinate." )
    public IAtom getClosestAtom(Point2d worldCoord);

    @Recorded
    @PublishedMethod ( methodSummary = "Returns the ICDKMolecule of the" +
    		"active JChemPaint editor." )
    public ICDKMolecule getModel() throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( methodSummary = "Sets the ICDKMolecule of the active" +
    		" JChemPaint editor." )
    public void setModel(ICDKMolecule molecule) throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "IAtom atom",
                       methodSummary = "Removes an IAtom from the data model." )
    public void removeAtom(IAtom atom) throws BioclipseException;
    
    @Recorded
    @PublishedMethod ( params = "IBond bond",
                       methodSummary = "Removes an IBond from the data model." )
    public void removeBond(IBond bond) throws BioclipseException;

    @Recorded
    @PublishedMethod ( params = "Point2d worldCoord",
                       methodSummary = "Returns the IBond closest to the " +
                       		"given world coordinate." )
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
                         "current connectivity."
    )
    public void updateImplicitHydrogenCounts();

    @Recorded
    @PublishedMethod(
         methodSummary = "Adds explicit hydrogens, given the " +
                         "current connectivity."
    )
    public void addExplicitHydrogens();
    
    @Recorded
    @PublishedMethod( methodSummary = "Removes all explicit hydrogens")
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
         methodSummary = "Changes the element of this atom to the given symbol."
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
    public void setWedgeType(IBond bond, IBond.Stereo type);
    
    @Recorded
    @PublishedMethod(
         params = "int order",
         methodSummary = "Returns an instance of IBond.Order matching the " +
         		         "given order."
    )
    public IBond.Order getBondOrder(int order);

    @Recorded
    @PublishedMethod(
         methodSummary = "Deletes all atoms and bonds in the model."
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

    @Recorded
    @PublishedMethod ( methodSummary = "Selectes all atoms and bonds" )
    public void selectAll();

    @Recorded
    @PublishedMethod ( methodSummary = "Selectes all atoms and bonds that is " +
    		                           "connected to the selected element",
                       params = "IChemObject element")
    public void selectPart(IChemObject element);

}
