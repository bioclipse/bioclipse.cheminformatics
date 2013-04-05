/*******************************************************************************
 * Copyright (c) 2007      Jonathan Alvarsson
 *               2007-2008 Ola Spjuth
 *               2008      Egon Willighagen
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.business;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.scripting.ui.Activator;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.edit.AppendAtom;
import org.openscience.cdk.controller.edit.CompositEdit;
import org.openscience.cdk.controller.edit.RemoveAtom;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator.WillDrawAtomNumbers;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.AtomRadius;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.CompactAtom;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.ShowEndCarbons;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.ShowExplicitHydrogens;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondDistance;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondLength;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondWidth;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.WedgeWidth;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.FitToScreen;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Margin;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Scale;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.ZoomFactor;
import org.openscience.cdk.renderer.generators.ExtendedAtomGenerator.ShowImplicitHydrogens;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator.HighlightAtomDistance;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator.HighlightBondDistance;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.RingGenerator.CDKStyleAromaticity;
import org.openscience.cdk.renderer.generators.RingGenerator.RingProportion;
import org.openscience.cdk.renderer.generators.RingGenerator.ShowAromaticity;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;
import org.openscience.cdk.renderer.selection.LinkedSelection;
import org.openscience.cdk.renderer.selection.LogicalSelection;
import org.openscience.cdk.renderer.selection.LogicalSelection.Type;
import org.openscience.cdk.renderer.selection.MultiSelection;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * @author egonw
 */
public class JChemPaintManager implements IBioclipseManager {

	private Logger logger = Logger.getLogger(JChemPaintManager.class);
    /** Not to be used by manager method directly, but is just needed for the syncRun() call. */
    private JChemPaintEditor jcpEditor;

    public IAtom getClosestAtom(Point2d worldCoord) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            return relay.getClosestAtom(worldCoord);
        } else {
            say("No opened JChemPaint editor");
            return null;
        }
    }

    public String getManagerName() {
        return "jcp";
    }

    protected void setActiveEditor(JChemPaintEditor activeEditor) {
        jcpEditor = activeEditor;
    }

    private void say(String message) {
        Activator.getDefault().getJavaJsConsoleManager().say(message);
    }
    
    private JChemPaintEditor findActiveEditor() {
        final Display display = PlatformUI.getWorkbench().getDisplay();
        setActiveEditor(null);
        display.syncExec( new Runnable() {
            public void run() {
                IEditorPart activeEditor 
                    = PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow()
                                .getActivePage()
                                .getActiveEditor();
                if (activeEditor != null) {
                    setActiveEditor( (JChemPaintEditor)activeEditor
                                     .getAdapter( JChemPaintEditor.class ) );
                }
            }
        });
        return jcpEditor;
    }
    
    public void snapshot(String filepath) {
        final IFile file = ResourcePathTransformer.getInstance().transform(filepath);
        PlatformUI.getWorkbench().getDisplay().syncExec( new Runnable() {
            public void run() {
                try {
                    findActiveEditor().snapshot(file);
                } catch (CoreException c) {
                    
                }
            }
        });
    }

    public void selectAtoms(String atomIndices) throws BioclipseException {
    	String[] strIndices = atomIndices.split(",");
    	int[] atomIntices = new int[strIndices.length];
    	for (int i=0; i<strIndices.length; i++) {
    		atomIntices[i] = Integer.parseInt(strIndices[i].trim());
    	}
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            RendererModel model = editor.getControllerHub().getRenderModel();
        	IAtomContainer atomContainer = getModel().getAtomContainer();
        	Set<IAtom> newSelection = new HashSet<IAtom>();
    	    for (int number : atomIntices) {
    	        newSelection.add( atomContainer.getAtom(number-1));
    	    }
    	    model.setSelection(new MultiSelection<IAtom>(newSelection));
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void selectBonds(String bondIndices) throws BioclipseException {
    	String[] strIndices = bondIndices.split(",");
    	int[] bondIntices = new int[strIndices.length];
    	for (int i=0; i<strIndices.length; i++) {
    		bondIntices[i] = Integer.parseInt(strIndices[i].trim());
    	}
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            RendererModel model = editor.getControllerHub().getRenderModel();
        	IAtomContainer atomContainer = getModel().getAtomContainer();
        	Set<IBond> newSelection = new HashSet<IBond>();
    	    for (int number : bondIntices) {
    	        newSelection.add( atomContainer.getBond(number-1));
    	    }
    	    model.setSelection(new MultiSelection<IBond>(newSelection));
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public ICDKMolecule getModel() throws BioclipseException {
        JChemPaintEditor editor = findActiveEditor();
        if (editor == null) {
            throw new BioclipseException("No active JChemPaint editor found.");
        }
        return editor.getCDKMolecule();
    }

    public void setModel(ICDKMolecule molecule) throws BioclipseException {
        if (molecule == null) {
            throw new BioclipseException("Input is null.");
        }
        JChemPaintEditor editor = findActiveEditor();
        if (editor == null) {
            throw new BioclipseException("No active JChemPaint editor found.");
        }
        Display.getDefault().asyncExec(new SetInputRunnable(editor, molecule));
    }

    public IAtom addAtom(String atomType, Point2d worldcoord) {
        JChemPaintEditor editor = findActiveEditor();
        IAtom newAtom = null;
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            newAtom = relay.addAtom(atomType, worldcoord);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
        return newAtom;
    }

    public IBond getClosestBond(Point2d worldCoord) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            return relay.getClosestBond(worldCoord);
        } else {
            say("No opened JChemPaint editor");
            return null;
        }
    }

    public void removeAtom(IAtom atom) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.removeAtom(atom);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void removeBond( IBond bond ) throws BioclipseException {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.removeBond( bond );
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }
    
    public void updateView() {
        PlatformUI.getWorkbench().getDisplay().syncExec( new Runnable() {
            public void run() {
                JChemPaintEditor editor = findActiveEditor();
                if (editor != null) editor.update();
            }
        });
    }

    static class SetInputRunnable implements Runnable {
        ICDKMolecule molecule;
        JChemPaintEditor editor;

        public SetInputRunnable(JChemPaintEditor editor, ICDKMolecule molecule) {
            this.molecule = molecule;
            this.editor = editor;
        }

        public void run() {
            editor.setInput(molecule);
        }
    }

    public Point2d newPoint2d( double x, double y ) {
        return new Point2d(x, y);
    }

    public void updateImplicitHydrogenCounts() {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.updateImplicitHydrogenCounts();
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public IBond addBond(IAtom fromAtom, IAtom toAtom) {
        JChemPaintEditor editor = findActiveEditor();
        IBond newBond = null;
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            newBond = relay.addBond(fromAtom, toAtom);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
        return newBond;
    }

    public void moveTo(IAtom atom, Point2d point) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.moveTo(atom, point);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void moveTo(IBond bond, Point2d point) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.moveTo(bond, point);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void setCharge( IAtom atom, int charge ) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.setCharge(atom, charge);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void setMassNumber( IAtom atom, int massNumber ) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.setMassNumber(atom, massNumber);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void setOrder( IBond bond, Order order ) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.setOrder(bond, order);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void setSymbol( IAtom atom, String symbol ) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.setSymbol(atom, symbol);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void setWedgeType( IBond bond, IBond.Stereo type ) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.setWedgeType(bond, type);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public IAtom addAtom(String elementSymbol, IAtom atom) {
        JChemPaintEditor editor = findActiveEditor();
        IAtom newAtom = null;
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            newAtom = relay.addAtom(elementSymbol, atom);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
        return newAtom;
    }

    public void zap() {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.zap();
            editor.getWidget().reset();
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void cleanup(IProgressMonitor monitor) throws BioclipseException{
        final JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            monitor.beginTask( "Cleaning up of structure", IProgressMonitor.UNKNOWN );
            IChemModelRelay relay = editor.getControllerHub();
            // Removes empty atomcontainers
            // This is a dirty fix until we can work on a single atom container instead of a ChemModel
            List<IAtomContainer> toRemove = new ArrayList<IAtomContainer>();
            IMoleculeSet mSet = relay.getIChemModel().getMoleculeSet();
            for(IAtomContainer ac:mSet.molecules()) {
            	if(ac.getAtomCount()==0)
            		toRemove.add(ac);
            }
            for(IAtomContainer ac:toRemove) {
            	mSet.removeAtomContainer(ac);
            }
            try{
                relay.cleanup();
            } catch (NullPointerException e) {
                throw new BioclipseException(
                    "You seem to have run into bug 950 we are aware of this "+
                    "problem and are working on a solution. One cause of this "+
                    "exception could be free hydrogen molecules in the model",
                    e );
            }
            PlatformUI.getWorkbench().getDisplay().syncExec( new Runnable() {
                public void run() {
                	try{
                    editor.getWidget().reset();
                	} catch (Exception e) {
                		logger.error(e.getMessage());
                	}
                }
            });
            monitor.done();
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void addRing(IAtom atom, int size) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.addRing(atom, size);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public IBond.Order getBondOrder(int order) {
        switch (order) {
            case 1:  return IBond.Order.SINGLE;
            case 2:  return IBond.Order.DOUBLE;
            case 3:  return IBond.Order.TRIPLE;
            case 4:  return IBond.Order.QUADRUPLE;
            default: return null;
        }
    }

    public void addPhenyl(IAtom atom) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.addPhenyl(atom);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void addRing(IBond bond, int size) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.addRing(bond, size);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void addPhenyl(IBond bond) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.addPhenyl(bond);
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }
    
    private RendererModel getRendererModel() {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            return editor.getControllerHub().getRenderer().getRenderer2DModel();
        } else {
            return null;
        }
    }
    
    public void setBondLength(double bondLength) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter(BondLength.class).setValue(bondLength);
        }
        updateView();
    }

    public void setAtomRadius(double atomRadius) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            ((AtomRadius)model.getParameter(AtomRadius.class))
               .setValue(atomRadius);
        }
        updateView();
    }

    public void setIsCompact(boolean isCompact) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter(CompactAtom.class).setValue(true);
        }
        updateView();
    }

    public void setBondDistance(double bondDistance) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter( BondDistance.class ).setValue( bondDistance);
        }
        updateView();
    }

    public void setBondWidth(double bondWidth) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter(BondWidth.class).setValue(bondWidth);
        }
        updateView();
    }

    public void setDrawNumbers(boolean setDrawNumbers) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.set(WillDrawAtomNumbers.class, setDrawNumbers);
        }
        updateView();
    }

    public void setFitToScreen(boolean fitToScreen) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.set(FitToScreen.class, fitToScreen);
        }
        updateView();
    }

    public void setHighlightAtomDistance(double highlightDistance) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.set(HighlightAtomDistance.class, highlightDistance);
        }
        updateView();
    }

    public void setHighlightBondDistance(double highlightDistance) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.set(HighlightBondDistance.class, highlightDistance);
        }
        updateView();
    }

    public void setRingProportion(double ringProportion) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter( BondDistance.class ).setValue( ringProportion );
        }
        updateView();
    }

    public void setShowAromaticity(boolean showAromaticity) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter(ShowAromaticity.class).setValue(showAromaticity);
        }
        updateView();
    }

    public void setShowAromaticityInCDKStyle(boolean showAromaticityCDK) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter(CDKStyleAromaticity.class).setValue(showAromaticityCDK);
        }
        updateView();
    }

    public void setShowEndCarbons(boolean showEndCarbons) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter(ShowEndCarbons.class).setValue(showEndCarbons);
        }
        updateView();
    }

    public void setShowExplicitHydrogens(boolean explicitHydrogens) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter(ShowExplicitHydrogens.class)
            	.setValue(explicitHydrogens);
        }
        updateView();
    }

    public void setShowImplicitHydrogens(boolean implicitHydrogens) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter(ShowImplicitHydrogens.class)
            	.setValue(implicitHydrogens);
        }
        updateView();
    }

    public void setWedgeWidth(double wedgeWidth) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.set(WedgeWidth.class, wedgeWidth);
        }
        updateView();
    }

    public void setMargin(double margin) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter(Margin.class).setValue(margin);
        }
        updateView();
    }
    
    public void setZoom(double zoom) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.getParameter(ZoomFactor.class).setValue(zoom);
        }
        updateView();
    }

    public double getAtomRadius() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return (Double)model.getParameter(BasicAtomGenerator.AtomRadius.class).getValue();
        } else {
            return 0;
        }
    }

    public double getBondDistance() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter( BondDistance.class ).getValue();
        } else {
            return 0;
        }
    }

    public double getBondLength() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(BondLength.class).getValue();
        } else {
            return 0;
        }
    }

    public double getBondWidth() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(BondWidth.class).getValue();
        } else {
            return 0;
        }
    }

    public boolean getDrawNumbers() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(WillDrawAtomNumbers.class).getValue();
        } else {
            return false;
        }
    }

    public boolean getFitToScreen() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(FitToScreen.class).getValue();
        } else {
            return false;
        }
    }

    public double getHighlightAtomDistance() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(HighlightAtomDistance.class).getValue();
        } else {
            return 0;
        }
    }

    public double getHighlightBondDistance() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(HighlightBondDistance.class).getValue();
        } else {
            return 0;
        }
    }

    public boolean getIsCompact() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(CompactAtom.class).getValue();
        } else {
            return false;
        }
    }

    public double getRingProportion() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(RingProportion.class).getValue();
        } else {
            return 0;
        }
    }

    public boolean getShowAromaticity() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(ShowAromaticity.class).getValue();
        } else {
            return false;
        }
    }
    
    public double getScale() {
    	return this.getRendererModel().get(Scale.class);
    }
    
    public void setScale(double scale) {
    	this.getRendererModel().set(Scale.class,scale);
    }

    public boolean getShowAromaticityInCDKStyle() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(CDKStyleAromaticity.class).getValue();
        } else {
            return false;
        }
    }

    public boolean getShowEndCarbons() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(ShowEndCarbons.class).getValue();
        } else {
            return false;
        }
    }

    public boolean getShowExplicitHydrogens() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(ShowExplicitHydrogens.class).getValue();
        } else {
            return false;
        }
    }

    public boolean getShowImplicitHydrogens() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(ShowImplicitHydrogens.class).getValue();
        } else {
            return false;
        }
    }

    public double getWedgeWidth() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(WedgeWidth.class).getValue();
        } else {
            return 0;
        }
    }

    public double getMargin() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(Margin.class).getValue();
        } else {
            return 0;
        }
    }
    
    public double getZoom() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getParameter(ZoomFactor.class).getValue();
        } else {
            return 0;
        }
    }
    
    public void removeExplicitHydrogens()  {
        JChemPaintEditor editor = findActiveEditor();
        if(editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            IChemModel model = relay.getIChemModel();
            List<RemoveAtom> atomsToRemove = new ArrayList<RemoveAtom>();
            for (IAtomContainer container : ChemModelManipulator
                                            .getAllAtomContainers( model)) {
                for(IAtom atom: container.atoms()) {
                    if(atom.getSymbol().equals( "H" ))
                        atomsToRemove.add( RemoveAtom.remove( atom) );
                }
            }
            if(!atomsToRemove.isEmpty()) {
                relay.execute( CompositEdit.compose( atomsToRemove ) );
                updateView();
            }
        } else {
            say("No opened JChemPaint editor");
        }
    }

    public void addExplicitHydrogens() {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.updateImplicitHydrogenCounts();
            IChemModel model = relay.getIChemModel();
            List<IAtomContainer> containers =
                ChemModelManipulator.getAllAtomContainers(model);
            List<AppendAtom> edits = new LinkedList<AppendAtom>();
            for (IAtomContainer container : containers) {
                for (IAtom atom : container.atoms()) {
                    int hCount = atom.getImplicitHydrogenCount() == null ? 0 :
                        atom.getImplicitHydrogenCount();
                    for (int i=0; i<hCount; i++) {
                        edits.add( AppendAtom.appendAtom( "H", atom));
                    }
                    atom.setImplicitHydrogenCount(0);
                }
            }
            relay.execute( CompositEdit.compose( edits ) );
        } else {
           say("No opened JChemPaint editor");
        }
    }

    @SuppressWarnings("deprecation")
    private void select(IChemObjectSelection selection) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.select( selection );
            relay.getRenderModel().setSelection( selection );
        } else {
            say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void selectAll() {
            LogicalSelection selection = new LogicalSelection(Type.ALL);
            select( selection );
    }

    public void selectPart(IChemObject element) {
        LinkedSelection selection = new LinkedSelection( element );
        select(selection);
    }
}
