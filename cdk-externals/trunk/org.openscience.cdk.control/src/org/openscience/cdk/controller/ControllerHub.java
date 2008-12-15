/* $Revision: 7636 $ $Author: egonw $ $Date: 2007-01-04 18:46:10 +0100 (Thu, 04 Jan 2007) $
 *
 * Copyright (C) 2007-2008  Egon Willighagen <egonw@users.sf.net>
 *               2005-2007  Christoph Steinbeck <steinbeck@users.sf.net>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All I ask is that proper credit is given for my work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.layout.AtomPlacer;
import org.openscience.cdk.layout.RingPlacer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.layout.TemplateHandler;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.renderer.IJava2DRenderer;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * Class that will central interaction point between a mouse event throwing
 * widget (SWT or Swing) and the Controller2D modules.
 * 
 * <p>FIXME: will replace the old Controller2D class.
 * 
 * @cdk.svnrev  $Revision: 9162 $
 * @cdk.module  control
 * @author      Niels Out
 * @author      egonw
 */
public class ControllerHub implements IMouseEventRelay, IChemModelRelay {
	
	private IChemModel chemModel;
	
	private IControllerModel controllerModel; 
	private IJava2DRenderer renderer;
	private IViewEventRelay eventRelay;
	
	private List<IControllerModule> generalModules;
	
	private StructureDiagramGenerator diagramGenerator;

	private IControllerModule activeDrawModule;
	private final static RingPlacer ringPlacer = new RingPlacer();
	
	private IAtomContainer phantoms;
	
	public ControllerHub(IControllerModel controllerModel,
		                   IJava2DRenderer renderer,
		                   IChemModel chemModel,
		                   IViewEventRelay eventRelay) {
		this.controllerModel = controllerModel;
		this.renderer = renderer;
		this.chemModel = chemModel;
		this.eventRelay = eventRelay;
		this.phantoms = chemModel.getBuilder().newAtomContainer();
		
		generalModules = new ArrayList<IControllerModule>();
		
		registerGeneralControllerModule(new HighlightModule(this));
	}
	
	public IControllerModel getController2DModel() {
		return controllerModel;
	}
	
	public IJava2DRenderer getIJava2DRenderer() {
		return renderer;
	}
	
	public IChemModel getIChemModel() {
		return chemModel;
	}

  public void setChemModel(IChemModel model) {
      this.chemModel = model;
    }

	/**
	 * Unregister all general IController2DModules.
	 */
	public void unRegisterAllControllerModule() {
		generalModules.clear();
	}
	
	/**
	 * Adds a general IController2DModule which will catch all mouse events.
	 */
	public void registerGeneralControllerModule(IControllerModule module) {
		module.setChemModelRelay(this);
		generalModules.add(module);
	}
	
	public void mouseClickedDouble(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
			
		// Relay the mouse event to the general handlers
		for (IControllerModule module : generalModules) {
			module.mouseClickedDouble(worldCoord);
		}

		// Relay the mouse event to the active 
		IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseClickedDouble(worldCoord);
	}


	public void mouseClickedDownRight(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IControllerModule module : generalModules) {
			module.mouseClickedDownRight(worldCoord);
		}

		// Relay the mouse event to the active 
		IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseClickedDownRight(worldCoord);
	}

	public void mouseClickedUpRight(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IControllerModule module : generalModules) {
			module.mouseClickedUpRight(worldCoord);
		}

		// Relay the mouse event to the active 
		IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseClickedUpRight(worldCoord);
	}
	
	public void mouseClickedDown(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IControllerModule module : generalModules) {
			module.mouseClickedDown(worldCoord);
		}

		// Relay the mouse event to the active 
		IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseClickedDown(worldCoord);
	}

	public void mouseClickedUp(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IControllerModule module : generalModules) {
			module.mouseClickedUp(worldCoord);
		}

		// Relay the mouse event to the active 
		IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseClickedUp(worldCoord);
	}

	public void mouseDrag(int screenCoordXFrom, int screenCoordYFrom, int screenCoordXTo, int screenCoordYTo) {
		Point2d worldCoordFrom = renderer.getCoorFromScreen(screenCoordXFrom, screenCoordYFrom);
		Point2d worldCoordTo = renderer.getCoorFromScreen(screenCoordXTo, screenCoordYTo);
			
		// Relay the mouse event to the general handlers
		for (IControllerModule module : generalModules) {
			module.mouseDrag(worldCoordFrom, worldCoordTo);
		}

		// Relay the mouse event to the active 
		IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseDrag(worldCoordFrom, worldCoordTo);
	}

	public void mouseEnter(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IControllerModule module : generalModules) {
			module.mouseEnter(worldCoord);
		}

		// Relay the mouse event to the active 
		IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseEnter(worldCoord);
	}

	public void mouseExit(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IControllerModule module : generalModules) {
			module.mouseExit(worldCoord);
		}

		// Relay the mouse event to the active 
		IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseExit(worldCoord);
	}

	public void mouseMove(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IControllerModule module : generalModules) {
			module.mouseMove(worldCoord);
		}

		// Relay the mouse event to the active 
		IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseMove(worldCoord);
	}
	
	public void updateView() {
		//call the eventRelay method here to update the view..
		eventRelay.updateView();
	}
	
	public IControllerModule getActiveDrawModule() {
		return activeDrawModule;
	}
	
	public void setActiveDrawModule(IControllerModule activeDrawModule){
		this.activeDrawModule=activeDrawModule;
	}

	public IAtom getClosestAtom(Point2d worldCoord) {
		IAtom closestAtom = null;
		double closestDistance = Double.MAX_VALUE;
		
		Iterator<IAtomContainer> containers = ChemModelManipulator.getAllAtomContainers(chemModel).iterator();
		while (containers.hasNext()) {
			Iterator<IAtom> atoms = containers.next().atoms().iterator();
			while (atoms.hasNext()) {
				IAtom nextAtom = atoms.next();
				if (nextAtom.getPoint2d() == null) continue;
				double distance = nextAtom.getPoint2d().distance(worldCoord);
				if (distance <= renderer.getRenderer2DModel().getHighlightRadiusModel() &&
					distance < closestDistance) {
					closestAtom = nextAtom;
					closestDistance = distance;
				}
			}
		}
		return closestAtom;
	}
	
	public IBond getClosestBond(Point2d worldCoord) {
		IBond closestBond = null;
		double closestDistance = Double.MAX_VALUE;
		
		Iterator<IAtomContainer> containers = ChemModelManipulator.getAllAtomContainers(chemModel).iterator();
		while (containers.hasNext()) {
			Iterator<IBond> bonds = containers.next().bonds().iterator();
			while (bonds.hasNext()) {
				IBond nextBond = bonds.next();
				double distance = nextBond.get2DCenter().distance(worldCoord);
				if (distance <= renderer.getRenderer2DModel().getHighlightRadiusModel() &&
					distance < closestDistance) {
					closestBond = nextBond;
					closestDistance = distance;
				}
			}
		}
		return closestBond;
	}
	
	public void removeAtom(IAtom atom) {
		ChemModelManipulator.removeAtomAndConnectedElectronContainers(chemModel, atom);
	}
	
	public IAtom addAtom(String atomType, Point2d worldCoord) {
		//FIXME: update atoms for implicit H's or so
		IAtom newAtom = chemModel.getBuilder().newAtom(atomType, worldCoord);
		
		//FIXME : there should be an initial hierarchy?
		IMoleculeSet molSet = chemModel.getMoleculeSet();
		if (molSet == null) {
		    molSet = chemModel.getBuilder().newMoleculeSet();
		    IAtomContainer ac = chemModel.getBuilder().newAtomContainer();
		    ac.addAtom(newAtom);
		    molSet.addAtomContainer(ac);
		    chemModel.setMoleculeSet(molSet);
		} else {
		    // FIXME : always add to the first container?
		    molSet.getAtomContainer(0).addAtom(newAtom);
		}
		return newAtom;
	}

    public IAtom addAtom(String atomType, IAtom atom) {
        IAtom newAtom = chemModel.getBuilder().newAtom(atomType);
        IBond newBond = chemModel.getBuilder().newBond(atom, newAtom);
        IAtomContainer atomCon = chemModel.getMoleculeSet().getAtomContainer(0);
        
        // The AtomPlacer generates coordinates for the new atom
        AtomPlacer atomPlacer = new AtomPlacer();
        atomPlacer.setMolecule(chemModel.getBuilder().newMolecule(atomCon));
        double bondLength;
        if (atomCon.getBondCount() >= 1) {
            bondLength = GeometryTools.getBondLengthAverage(atomCon);
        } else {
            bondLength = 1.4;       // XXX Or some sensible default?
        }
        
        // determine the atoms which define where the 
        // new atom should not be placed
        List<IAtom> connectedAtoms = atomCon.getConnectedAtomsList(atom);

        if (connectedAtoms.size() == 0) {
            Point2d newAtomPoint = new Point2d(atom.getPoint2d());
            newAtomPoint.x += bondLength;
            newAtom.setPoint2d(newAtomPoint);
        } else if (connectedAtoms.size() == 1) {
            IAtomContainer ac = atomCon.getBuilder().newAtomContainer();
            ac.addAtom(atom);
            ac.addAtom(newAtom);
            Point2d distanceMeasure = new Point2d(0,0); // XXX not sure about this?
            IAtom connectedAtom = connectedAtoms.get(0);
            Vector2d v = atomPlacer.getNextBondVector(atom, connectedAtom, distanceMeasure, true);
            atomPlacer.placeLinearChain(ac, v, bondLength);
        } else {
            IAtomContainer placedAtoms = atomCon.getBuilder().newAtomContainer();
            for (IAtom conAtom : connectedAtoms) placedAtoms.addAtom(conAtom);
            Point2d center2D = GeometryTools.get2DCenter(placedAtoms);
            
            IAtomContainer unplacedAtoms = atomCon.getBuilder().newAtomContainer();
            unplacedAtoms.addAtom(newAtom);
            
            atomPlacer.distributePartners(
                    atom, placedAtoms, center2D, unplacedAtoms, bondLength);
        }

        atomCon.addAtom(newAtom);
        atomCon.addBond(newBond);
        return newAtom;
    }
    
    public void moveTo( IAtom atom, Point2d worldCoords ) {
        if ( atom != null ) {
            Point2d atomCoord = new Point2d( worldCoords );
            atom.setPoint2d( atomCoord );
        }
    }

    public void moveTo( IBond bond, Point2d point ) {
        if (bond != null) {
			Point2d center = bond.get2DCenter();
			for (IAtom atom : bond.atoms()) {
				Vector2d offset = new Vector2d();
				offset.sub(atom.getPoint2d(), center);
				Point2d result = new Point2d();
				result.add(point, offset);

				atom.setPoint2d(result);
			}
        }
    }

    public IBond addBond(IAtom fromAtom, IAtom toAtom) {
        IBond newBond = chemModel.getBuilder().newBond(fromAtom, toAtom);
        chemModel.getMoleculeSet().getAtomContainer(0).addBond(newBond);
        return newBond;
    }

    public void setCharge(IAtom atom, int charge) {
        atom.setFormalCharge(charge);
    }

    public void setMassNumber(IAtom atom, int charge) {
        atom.setMassNumber(charge);
    }

    public void setOrder(IBond bond, Order order) {
        bond.setOrder(order);
    }

    public void setSymbol(IAtom atom, String symbol) {
        atom.setSymbol(symbol);
    }

    public void setWedgeType(IBond bond, int type) {
        bond.setStereo(type);
    }

    public void updateImplicitHydrogenCounts() {
        CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher
            .getInstance(chemModel.getBuilder());
        for (IAtomContainer container :
             ChemModelManipulator.getAllAtomContainers(chemModel)) {
            for (IAtom atom : container.atoms()) {
                if (!(atom instanceof IPseudoAtom)) {
                    try {
                        IAtomType type = matcher.findMatchingAtomType(
                            container, atom
                        );
                        if (type != null &&
                            type.getFormalNeighbourCount() != null) {
                            atom.setHydrogenCount(
                                type.getFormalNeighbourCount() -
                                container.getConnectedAtomsCount(atom)
                            );
                        }
                    } catch ( CDKException e ) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void zap() {
        for (IAtomContainer container :
            ChemModelManipulator.getAllAtomContainers(chemModel)) {
            container.removeAllElements();
        }
    }

    public void cleanup() {
        IChemObjectBuilder builder = 
            NoNotificationChemObjectBuilder.getInstance();
        if (diagramGenerator == null) {
            diagramGenerator = new StructureDiagramGenerator();
            diagramGenerator.setTemplateHandler(
                new TemplateHandler(builder)
            );
        }
        for (IAtomContainer container :
            ChemModelManipulator.getAllAtomContainers(chemModel)) {
            for (IAtom atom : container.atoms()) atom.setPoint2d(null);
            diagramGenerator.setMolecule(
                container instanceof IMolecule ? (IMolecule)container :
                    builder.newMolecule(container)
            );
            try {
                diagramGenerator.generateExperimentalCoordinates(
                    new Vector2d(0,1)
                );
                IMolecule cleanedMol = diagramGenerator.getMolecule();
                // now copy/paste coordinates
                for (int i=0; i<cleanedMol.getAtomCount(); i++) {
                    container.getAtom(i).setPoint2d(
                         cleanedMol.getAtom(i).getPoint2d()
                    );
                }
            } catch ( Exception e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public IRing addRing(int ringSize, Point2d worldcoord) {
        IRing ring = chemModel.getBuilder().newRing(ringSize, "C");
        double bondLength = 1.4;
        ringPlacer.placeRing(ring, worldcoord, bondLength);
        IMoleculeSet set = chemModel.getMoleculeSet();
        
        // the molecule set should not be null, but just in case...
        if (set == null) {
            set = chemModel.getBuilder().newMoleculeSet();
            chemModel.setMoleculeSet(set);
        }
        set.addAtomContainer(ring);
        return ring;
    }

    public IRing addPhenyl(Point2d worldcoord) {
        return addPhenyl(addAtom("C", worldcoord));
    }

    public IRing addRing(IAtom atom, int ringSize) {
        IAtomContainer sourceContainer 
            = ChemModelManipulator.getRelevantAtomContainer(chemModel, atom);
        IAtomContainer sharedAtoms = atom.getBuilder().newAtomContainer();
        sharedAtoms.addAtom(atom);
        
        IRing newRing = createAttachRing(sharedAtoms, ringSize, "C");
        double bondLength = GeometryTools.getBondLengthAverage(sourceContainer);
        Point2d conAtomsCenter = getConnectedAtomsCenter(sharedAtoms, chemModel);
        
        Point2d sharedAtomsCenter = atom.getPoint2d();
        Vector2d ringCenterVector = new Vector2d(sharedAtomsCenter);
        ringCenterVector.sub(conAtomsCenter);
        ringPlacer.placeSpiroRing(
                newRing, sharedAtoms, sharedAtomsCenter, ringCenterVector, bondLength);
        
        for (IAtom ringAtom : newRing.atoms()) {
            if (ringAtom != atom) sourceContainer.addAtom(ringAtom);
        }
        
        for (IBond ringBond : newRing.bonds()) {
            sourceContainer.addBond(ringBond);
        }
        return newRing;
    }

    public IRing addPhenyl(IAtom atom) {
        IAtomContainer sourceContainer 
            = ChemModelManipulator.getRelevantAtomContainer(chemModel, atom);
        IAtomContainer sharedAtoms = atom.getBuilder().newAtomContainer();
        sharedAtoms.addAtom(atom);
        
        // make a benzene ring
        IRing newRing = createAttachRing(sharedAtoms, 6, "C");
        newRing.getBond(0).setOrder(IBond.Order.DOUBLE);
        newRing.getBond(2).setOrder(IBond.Order.DOUBLE);
        newRing.getBond(4).setOrder(IBond.Order.DOUBLE);
        makeRingAromatic(newRing);
        
        double bondLength;
        if (sourceContainer.getBondCount() == 0) {
            /*
             * Special case of adding a ring to a single, unconnected atom
             * - places the ring centered on the place where the atom was.
             */
            bondLength = 1.4;
            Point2d ringCenter = new Point2d(atom.getPoint2d());
            ringPlacer.placeRing(newRing, ringCenter, bondLength);
        } else {
            bondLength = GeometryTools.getBondLengthAverage(sourceContainer);
            Point2d conAtomsCenter = getConnectedAtomsCenter(sharedAtoms, chemModel);
            
            Point2d sharedAtomsCenter = atom.getPoint2d();
            Vector2d ringCenterVector = new Vector2d(sharedAtomsCenter);
            ringCenterVector.sub(conAtomsCenter);
            ringPlacer.placeSpiroRing(newRing,
                                      sharedAtoms,
                                      sharedAtomsCenter,
                                      ringCenterVector,
                                      bondLength);
        }
        
        // add the ring to the source container
        for (IAtom ringAtom : newRing.atoms()) {
            if (ringAtom != atom) sourceContainer.addAtom(ringAtom);
        }
        
        for (IBond ringBond : newRing.bonds()) {
            sourceContainer.addBond(ringBond);
        }
        return newRing;
    }

    private void makeRingAromatic(IRing newRing) {
        for (IAtom atom : newRing.atoms())
            atom.setFlag(CDKConstants.ISAROMATIC, true);
        for (IBond bond : newRing.bonds())
            bond.setFlag(CDKConstants.ISAROMATIC, true);
    }
    
    /**
     * Constructs a new Ring of a certain size that contains all the atoms and
     * bonds of the given AtomContainer and is filled up with new Atoms and Bonds.
     *
     * @param  sharedAtoms  The AtomContainer containing the Atoms and bonds for the
     *                      new Ring
     * @param  ringSize     The size (number of Atoms) the Ring will have
     * @param  symbol       The element symbol the new atoms will have
     * @return              The constructed Ring
     */
    private IRing createAttachRing(IAtomContainer sharedAtoms, int ringSize, String symbol) {
        IRing newRing = sharedAtoms.getBuilder().newRing(ringSize);
        IAtom[] ringAtoms = new IAtom[ringSize];
        for (int i = 0; i < sharedAtoms.getAtomCount(); i++) {
            ringAtoms[i] = sharedAtoms.getAtom(i);
        }
        for (int i = sharedAtoms.getAtomCount(); i < ringSize; i++) {
            ringAtoms[i] = sharedAtoms.getBuilder().newAtom(symbol);
        }
        for (IBond bond : sharedAtoms.bonds()) newRing.addBond(bond);
        for (int i = sharedAtoms.getBondCount(); i < ringSize - 1; i++) {
            newRing.addBond(sharedAtoms.getBuilder().newBond(
                ringAtoms[i], ringAtoms[i + 1], IBond.Order.SINGLE)
            );
        }
        newRing.addBond(sharedAtoms.getBuilder().newBond(
            ringAtoms[ringSize - 1], ringAtoms[0], IBond.Order.SINGLE)
        );
        newRing.setAtoms(ringAtoms);
        return newRing;
    }

    /**
     * Searches all the atoms attached to the Atoms in the given AtomContainer and
     * calculates the center point of them.
     *
     * @param  sharedAtoms  The Atoms the attached partners are searched of
     * @return              The Center Point of all the atoms found
     */
    private Point2d getConnectedAtomsCenter(IAtomContainer sharedAtoms, 
                                            IChemModel chemModel) {
        IAtomContainer conAtoms = sharedAtoms.getBuilder().newAtomContainer();
        for (IAtom sharedAtom : sharedAtoms.atoms()) {
            conAtoms.addAtom(sharedAtom);
            IAtomContainer atomCon = 
                ChemModelManipulator.getRelevantAtomContainer(chemModel, 
                                                              sharedAtom);
            for (IAtom atom : atomCon.getConnectedAtomsList(sharedAtom)) {
                conAtoms.addAtom(atom);
            }
        }
        return GeometryTools.get2DCenter(conAtoms);
    }

    public IRing addRing(IBond bond, int size) {
        IAtomContainer sharedAtoms = bond.getBuilder().newAtomContainer();
        IAtom firstAtom = bond.getAtom(0); // Assumes two-atom bonds only
        IAtom secondAtom = bond.getAtom(1);
        sharedAtoms.addAtom(firstAtom);
        sharedAtoms.addAtom(secondAtom);
        sharedAtoms.addBond(bond);
        IAtomContainer sourceContainer = ChemModelManipulator
            .getRelevantAtomContainer(chemModel, firstAtom);

        Point2d sharedAtomsCenter = GeometryTools.get2DCenter(sharedAtoms);

        // calculate two points that are perpendicular to the highlighted bond
        // and have a certain distance from the bond center
        Point2d firstPoint = firstAtom.getPoint2d();
        Point2d secondPoint = secondAtom.getPoint2d();
        Vector2d diff = new Vector2d(secondPoint);
        diff.sub(firstPoint);
        double bondLength = firstPoint.distance(secondPoint);
        double angle = GeometryTools.getAngle(diff.x, diff.y);
        Point2d newPoint1 = new Point2d( // FIXME: what is this point??
            (Math.cos(angle + (Math.PI/2))*bondLength/4) + sharedAtomsCenter.x,
            (Math.sin(angle + (Math.PI/2))*bondLength/4) + sharedAtomsCenter.y
        );
        Point2d newPoint2 = new Point2d( // FIXME: what is this point??
            (Math.cos(angle - (Math.PI/2))*bondLength/4) + sharedAtomsCenter.x,
            (Math.sin(angle - (Math.PI/2))*bondLength/4) + sharedAtomsCenter.y
        );

        // decide on which side to draw the ring??
        IAtomContainer connectedAtoms = bond.getBuilder().newAtomContainer();
        for (IAtom atom : sourceContainer.getConnectedAtomsList(firstAtom)) {
            if (atom != secondAtom) connectedAtoms.addAtom(atom);
        }
        for (IAtom atom : sourceContainer.getConnectedAtomsList(secondAtom)) {
            if (atom != firstAtom) connectedAtoms.addAtom(atom);
        }
        Point2d conAtomsCenter = GeometryTools.get2DCenter(connectedAtoms);
        double distance1 = newPoint1.distance(conAtomsCenter);
        double distance2 = newPoint2.distance(conAtomsCenter);
        Vector2d ringCenterVector = new Vector2d(sharedAtomsCenter);
        if (distance1 < distance2) {
            ringCenterVector.sub(newPoint1);
        } else { // distance2 <= distance1
            ringCenterVector.sub(newPoint2);
        }

        // construct a new Ring that contains the highlighted bond an its two atoms
        IRing newRing = createAttachRing(sharedAtoms, size, "C");
        ringPlacer.placeFusedRing(newRing, sharedAtoms, sharedAtomsCenter,
                                  ringCenterVector, bondLength);
        // add the new atoms and bonds
        for (IAtom ringAtom : newRing.atoms()) {
            if (ringAtom != firstAtom && ringAtom != secondAtom) {
                sourceContainer.addAtom(ringAtom);
            }
        }
        for (IBond ringBond : newRing.bonds()) {
            if (ringBond != bond) {
                sourceContainer.addBond(ringBond);
            }
        }
        return newRing;
    }

    public IRing addPhenyl(IBond bond) {
        IAtomContainer sharedAtoms = bond.getBuilder().newAtomContainer();
        IAtom firstAtom = bond.getAtom(0); // Assumes two-atom bonds only
        IAtom secondAtom = bond.getAtom(1);
        sharedAtoms.addAtom(firstAtom);
        sharedAtoms.addAtom(secondAtom);
        sharedAtoms.addBond(bond);
        IAtomContainer sourceContainer = ChemModelManipulator
            .getRelevantAtomContainer(chemModel, firstAtom);

        Point2d sharedAtomsCenter = GeometryTools.get2DCenter(sharedAtoms);

        // calculate two points that are perpendicular to the highlighted bond
        // and have a certain distance from the bond center
        Point2d firstPoint = firstAtom.getPoint2d();
        Point2d secondPoint = secondAtom.getPoint2d();
        Vector2d diff = new Vector2d(secondPoint);
        diff.sub(firstPoint);
        double bondLength = firstPoint.distance(secondPoint);
        double angle = GeometryTools.getAngle(diff.x, diff.y);
        Point2d newPoint1 = new Point2d( // FIXME: what is this point??
            (Math.cos(angle + (Math.PI/2))*bondLength/4) + sharedAtomsCenter.x,
            (Math.sin(angle + (Math.PI/2))*bondLength/4) + sharedAtomsCenter.y
        );
        Point2d newPoint2 = new Point2d( // FIXME: what is this point??
            (Math.cos(angle - (Math.PI/2))*bondLength/4) + sharedAtomsCenter.x,
            (Math.sin(angle - (Math.PI/2))*bondLength/4) + sharedAtomsCenter.y
        );

        // decide on which side to draw the ring??
        IAtomContainer connectedAtoms = bond.getBuilder().newAtomContainer();
        for (IAtom atom : sourceContainer.getConnectedAtomsList(firstAtom)) {
            if (atom != secondAtom) connectedAtoms.addAtom(atom);
        }
        for (IAtom atom : sourceContainer.getConnectedAtomsList(secondAtom)) {
            if (atom != firstAtom) connectedAtoms.addAtom(atom);
        }
        Point2d conAtomsCenter = GeometryTools.get2DCenter(connectedAtoms);
        double distance1 = newPoint1.distance(conAtomsCenter);
        double distance2 = newPoint2.distance(conAtomsCenter);
        Vector2d ringCenterVector = new Vector2d(sharedAtomsCenter);
        if (distance1 < distance2) {
            ringCenterVector.sub(newPoint1);
        } else { // distance2 <= distance1
            ringCenterVector.sub(newPoint2);
        }

        // construct a new Ring that contains the highlighted bond an its two atoms
        IRing newRing = createAttachRing(sharedAtoms, 6, "C");
        makeRingAromatic(newRing);
        ringPlacer.placeFusedRing(newRing, sharedAtoms, sharedAtomsCenter,
                                  ringCenterVector, bondLength);
        if (bond.getOrder() == IBond.Order.SINGLE) {
            newRing.getBond(1).setOrder(IBond.Order.DOUBLE);
            newRing.getBond(3).setOrder(IBond.Order.DOUBLE);
            newRing.getBond(5).setOrder(IBond.Order.DOUBLE);
        } else { // assume Order.DOUBLE, so only need to add 2 double bonds
            newRing.getBond(2).setOrder(IBond.Order.DOUBLE);
            newRing.getBond(4).setOrder(IBond.Order.DOUBLE);
        }
        // add the new atoms and bonds
        for (IAtom ringAtom : newRing.atoms()) {
            if (ringAtom != firstAtom && ringAtom != secondAtom) {
                sourceContainer.addAtom(ringAtom);
            }
        }
        for (IBond ringBond : newRing.bonds()) {
            if (ringBond != bond) {
                sourceContainer.addBond(ringBond);
            }
        }
        return newRing;
    }

    public void removeBond(IBond bond) {
        IAtomContainer sourceContainer = ChemModelManipulator
            .getRelevantAtomContainer(chemModel, bond);
        sourceContainer.removeBond(bond);
    }

    public void addPhantomAtom( IAtom atom ) {
        this.phantoms.addAtom(atom);
    }

    public void addPhantomBond( IBond bond ) {
        this.phantoms.addBond(bond);
    }

    public void clearPhantoms() {
        this.phantoms.removeAllElements();
    }

    public IAtomContainer getPhantoms() {
        return this.phantoms;
    }
}
