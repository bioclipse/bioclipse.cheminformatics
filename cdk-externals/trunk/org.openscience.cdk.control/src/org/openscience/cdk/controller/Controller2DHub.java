/* $Revision: 7636 $ $Author: egonw $ $Date: 2007-01-04 18:46:10 +0100 (Thu, 04 Jan 2007) $
 *
 * Copyright (C) 2007-2008  Egon Willighagen <egonw@users.sf.net>
 *               2005       Christoph Steinbeck <steinbeck@users.sf.net>
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.layout.AtomPlacer;
import org.openscience.cdk.layout.RingPlacer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.layout.TemplateHandler;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.renderer.IJava2DRenderer;
import org.openscience.cdk.tools.CDKHydrogenAdder;
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
public class Controller2DHub implements IMouseEventRelay, IChemModelRelay {
	
	private IChemModel chemModel;
	
	private IController2DModel controllerModel; 
	private IJava2DRenderer renderer;
	private IViewEventRelay eventRelay;
	
	private List<IController2DModule> generalModules;
	private Map<Controller2DModel.DrawMode,IController2DModule> drawModeModules;
	
	private StructureDiagramGenerator diagramGenerator;
	private final static RingPlacer ringPlacer = new RingPlacer();
	
	public Controller2DHub(IController2DModel controllerModel,
		                   IJava2DRenderer renderer,
		                   IChemModel chemModel,
		                   IViewEventRelay eventRelay) {
		this.controllerModel = controllerModel;
		this.renderer = renderer;
		this.chemModel = chemModel;
		this.eventRelay = eventRelay;
		
		drawModeModules = new HashMap<Controller2DModel.DrawMode,IController2DModule>();
		generalModules = new ArrayList<IController2DModule>();
		
		//register all 'known' controllers
		registerDrawModeControllerModule( 
				IController2DModel.DrawMode.MOVE, 
				new Controller2DModuleMove(this));
		registerDrawModeControllerModule( 
				IController2DModel.DrawMode.ERASER, 
				new Controller2DModuleRemove(this));
		registerDrawModeControllerModule( 
				IController2DModel.DrawMode.INCCHARGE,
				new Controller2DModuleChangeFormalC(this, 1));
		registerDrawModeControllerModule( 
				IController2DModel.DrawMode.DECCHARGE,
				new Controller2DModuleChangeFormalC(this, -1));
		registerDrawModeControllerModule( 
				IController2DModel.DrawMode.ENTERELEMENT, 
				new Controller2DModuleAddAtom(this));
		
		registerGeneralControllerModule(new Controller2DModuleHighlight(this));
	}
	public IController2DModel getController2DModel() {
		return controllerModel;
	}
	public IJava2DRenderer getIJava2DRenderer() {
		return renderer;
	}
	public IChemModel getIChemModel() {
		return chemModel;
	}
	/**
	 * Register a draw mode that you want to have active for this Controller2DHub.
	 * 
	 * @param drawMode
	 * @param module
	 */
	public void registerDrawModeControllerModule(
		Controller2DModel.DrawMode drawMode, IController2DModule module) {
		module.setChemModelRelay(this);
		drawModeModules.put(drawMode, module);
	}
	
	/**
	 * Unregister all general IController2DModules.
	 */
	public void unRegisterAllControllerModule() {
		//module.setEventRelay(eventRelay);
		
		generalModules.clear();
	}
	/**
	 * Adds a general IController2DModule which will catch all mouse events.
	 */
	public void registerGeneralControllerModule(IController2DModule module) {
		module.setChemModelRelay(this);
		//module.setEventRelay(eventRelay);
		generalModules.add(module);
	}
	
	public void mouseClickedDouble(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
			
		// Relay the mouse event to the general handlers
		for (IController2DModule module : generalModules) {
			module.mouseClickedDouble(worldCoord);
		}

		// Relay the mouse event to the active 
		IController2DModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseClickedDouble(worldCoord);
	}

	public void mouseClickedDown(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IController2DModule module : generalModules) {
			module.mouseClickedDown(worldCoord);
		}

		// Relay the mouse event to the active 
		IController2DModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseClickedDown(worldCoord);
	}

	public void mouseClickedUp(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IController2DModule module : generalModules) {
			module.mouseClickedUp(worldCoord);
		}

		// Relay the mouse event to the active 
		IController2DModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseClickedUp(worldCoord);
	}

	public void mouseDrag(int screenCoordXFrom, int screenCoordYFrom, int screenCoordXTo, int screenCoordYTo) {
		Point2d worldCoordFrom = renderer.getCoorFromScreen(screenCoordXFrom, screenCoordYFrom);
		Point2d worldCoordTo = renderer.getCoorFromScreen(screenCoordXTo, screenCoordYTo);
			
		// Relay the mouse event to the general handlers
		for (IController2DModule module : generalModules) {
			module.mouseDrag(worldCoordFrom, worldCoordTo);
		}

		// Relay the mouse event to the active 
		IController2DModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseDrag(worldCoordFrom, worldCoordTo);
	}

	public void mouseEnter(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IController2DModule module : generalModules) {
			module.mouseEnter(worldCoord);
		}

		// Relay the mouse event to the active 
		IController2DModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseEnter(worldCoord);
	}

	public void mouseExit(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IController2DModule module : generalModules) {
			module.mouseExit(worldCoord);
		}

		// Relay the mouse event to the active 
		IController2DModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseExit(worldCoord);
	}

	public void mouseMove(int screenCoordX, int screenCoordY) {
		Point2d worldCoord = renderer.getCoorFromScreen(screenCoordX, screenCoordY);
		
		// Relay the mouse event to the general handlers
		for (IController2DModule module : generalModules) {
			module.mouseMove(worldCoord);
		}

		// Relay the mouse event to the active 
		IController2DModule activeModule = getActiveDrawModule();
		if (activeModule != null) activeModule.mouseMove(worldCoord);
	}
	public void updateView() {
		//call the eventRelay method here to update the view..
		System.out.println("updateView now in Controller2DHub");	
		eventRelay.updateView();
		
	}
	private IController2DModule getActiveDrawModule() {
		return drawModeModules.get(controllerModel.getDrawMode());
	}

	public IAtom getClosestAtom(Point2d worldCoord) {
		IAtom closestAtom = null;
		double closestDistance = Double.MAX_VALUE;
		
		Iterator<IAtomContainer> containers = ChemModelManipulator.getAllAtomContainers(chemModel).iterator();
		while (containers.hasNext()) {
			Iterator<IAtom> atoms = containers.next().atoms().iterator();
			while (atoms.hasNext()) {
				IAtom nextAtom = atoms.next();
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
			//GeometryToolsInternalCoordinates.getClosestBond( worldCoord.x, worldCoord.y, container);

		}
		return closestBond;
	}
	public void removeAtom(IAtom atom) {
		
		ChemModelManipulator.removeAtomAndConnectedElectronContainers(chemModel, atom);
	}
	public void addAtom(String atomType, Point2d worldCoord) {
		
	/*	IAtom newAtom1 = chemModel.getBuilder().newAtom(atomType, worldCoord);
		IAtomContainer atomCon = ChemModelManipulator.createNewMolecule(chemModel);
		atomCon.addAtom(newAtom1);
		//FIXME: update atoms for implicit H's or so
		//updateAtom(atomCon, newAtom1);
		chemModel.getMoleculeSet().addAtomContainer(atomCon);*/
		IAtom newAtom = chemModel.getBuilder().newAtom(atomType, worldCoord);
		chemModel.getMoleculeSet().getAtomContainer(0).addAtom(newAtom);
		
		System.out.println("atom added??");

	}

    public void addAtom(String atomType, IAtom atom) {
        IAtom newAtom = chemModel.getBuilder().newAtom(atomType);
        IBond newBond = chemModel.getBuilder().newBond(atom, newAtom);
        IAtomContainer atomCon = chemModel.getMoleculeSet().getAtomContainer(0);
        
        // The AtomPlacer generates coordinates for the new atom
        AtomPlacer atomPlacer = new AtomPlacer();
        atomPlacer.setMolecule(chemModel.getBuilder().newMolecule(atomCon));
        double bondLength = GeometryTools.getBondLengthAverage(atomCon);
        
        // determine the atoms which define where the new atom should not be
        // placed
        List<IAtom> connectedAtoms = atomCon.getConnectedAtomsList(atom);
        IAtomContainer placedAtoms = atomCon.getBuilder().newAtomContainer();
        for (IAtom conAtom : connectedAtoms) placedAtoms.addAtom(conAtom);
        Point2d center2D = GeometryTools.get2DCenter(placedAtoms);
        
        IAtomContainer unplacedAtoms = atomCon.getBuilder().newAtomContainer();
        unplacedAtoms.addAtom(newAtom);
        atomPlacer.distributePartners(atom, placedAtoms, center2D,
                                      unplacedAtoms, bondLength);

        atomCon.addAtom(newAtom);
        atomCon.addBond(newBond);
    }
    
    public void moveTo( IAtom atom, Point2d worldCoords ) {

        if ( atom != null ) {

            Point2d atomCoord = new Point2d( worldCoords );

            atom.setPoint2d( atomCoord );
        }

    }

    public void moveTo( IBond bond, Point2d point ) {

        if ( bond != null ) {
            Point2d center = bond.get2DCenter();
            for ( IAtom atom : bond.atoms() ) {
                Vector2d  offset = new Vector2d();
                offset.sub(  atom.getPoint2d(),center );
                Point2d result = new Point2d();
                result.add( point,offset );
                
                atom.setPoint2d( result);
            }
        }
    }

    public void addBond( IAtom fromAtom, IAtom toAtom ) {
        IBond newBond = chemModel.getBuilder().newBond(fromAtom, toAtom);
        chemModel.getMoleculeSet().getAtomContainer(0).addBond(newBond);
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
        CDKHydrogenAdder hAdder = CDKHydrogenAdder
            .getInstance(chemModel.getBuilder());
        for (IAtomContainer container :
             ChemModelManipulator.getAllAtomContainers(chemModel)) {
            for (IAtom atom : container.atoms()) {
                try {
                    hAdder.addImplicitHydrogens(container, atom);
                } catch ( CDKException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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

    public void addRing(IAtom atom, int ringSize) {
        addRing(atom, ringSize, false);
    }

    public void addPhenyl(IAtom atom) {
        addRing(atom, 6, true);
    }

    private void addRing(IAtom atom, int ringSize, boolean makeBenzene) {
        if (makeBenzene && ringSize != 6) // explode!
            return;
        IAtomContainer sourceContainer = ChemModelManipulator.getRelevantAtomContainer(chemModel, atom);
        IAtomContainer sharedAtoms = atom.getBuilder().newAtomContainer();
        sharedAtoms.addAtom(atom);
        Point2d sharedAtomsCenter = GeometryTools.get2DCenter(sharedAtoms);
        IRing newRing = createAttachRing(sharedAtoms, ringSize, "C");
        if (makeBenzene) {
            // make newRing a benzene ring
            newRing.getBond(0).setOrder(IBond.Order.DOUBLE);
            newRing.getBond(2).setOrder(IBond.Order.DOUBLE);
            newRing.getBond(4).setOrder(IBond.Order.DOUBLE);
            makeRingAromatic(newRing);
        }
        double bondLength = GeometryTools.getBondLengthAverage(sourceContainer);
        Point2d conAtomsCenter = getConnectedAtomsCenter(sharedAtoms, chemModel);
        Vector2d ringCenterVector = new Vector2d(sharedAtomsCenter);
        ringCenterVector.sub(conAtomsCenter);
        ringPlacer.placeSpiroRing(newRing, sharedAtoms, sharedAtomsCenter, ringCenterVector, bondLength);
        // removes the highlighted atom from the ring to add only the new placed
        // atoms to the AtomContainer.
        try {
            newRing.removeAtom(atom);
        } catch (Exception exc) {
            System.out.println("Could not remove atom from ring");
            exc.printStackTrace();
        }
        sourceContainer.add(newRing);
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
}
