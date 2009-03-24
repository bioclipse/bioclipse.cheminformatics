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

import javax.vecmath.Point2d;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.scripting.ui.Activator;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.renderer.RendererModel;

/**
 * @author egonw
 */
public class JChemPaintManager implements IJChemPaintManager {

    /** Not to be used by manager method directly, but is just needed for the syncRun() call. */
    private JChemPaintEditor jcpEditor;

    public IAtom getClosestAtom(Point2d worldCoord) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            return relay.getClosestAtom(worldCoord);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
            return null;
        }
    }

    public String getNamespace() {
        return "jcp";
    }

    protected void setActiveEditor(JChemPaintEditor activeEditor) {
        jcpEditor = activeEditor;
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
                if (activeEditor != null &&
                    activeEditor instanceof JChemPaintEditor) {
                    setActiveEditor((JChemPaintEditor)activeEditor);
                }
            }
        });
        return jcpEditor;
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

    public void addAtom(String atomType, Point2d worldcoord) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.addAtom(atomType, worldcoord);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public IBond getClosestBond(Point2d worldCoord) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            return relay.getClosestBond(worldCoord);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
            return null;
        }
    }

    public void removeAtom(IAtom atom) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.removeAtom(atom);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void removeBond( IBond bond ) throws BioclipseException {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.removeBond( bond );
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
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

    class SetInputRunnable implements Runnable {
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
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void addBond(IAtom fromAtom, IAtom toAtom) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.addBond(fromAtom, toAtom);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void moveTo(IAtom atom, Point2d point) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.moveTo(atom, point);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void moveTo(IBond bond, Point2d point) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.moveTo(bond, point);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void setCharge( IAtom atom, int charge ) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.setCharge(atom, charge);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void setMassNumber( IAtom atom, int massNumber ) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.setMassNumber(atom, massNumber);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void setOrder( IBond bond, Order order ) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.setOrder(bond, order);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void setSymbol( IAtom atom, String symbol ) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.setSymbol(atom, symbol);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void setWedgeType( IBond bond, int type ) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.setWedgeType(bond, type);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void addAtom(String elementSymbol, IAtom atom) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.addAtom(elementSymbol, atom);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void zap() {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.zap();
            editor.getWidget().reset();
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void cleanup() {
        final JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            PlatformUI.getWorkbench().getDisplay().syncExec( new Runnable() {
                public void run() {
                    IChemModelRelay relay = editor.getControllerHub();
                    relay.cleanup();
                    editor.getWidget().reset();
                }
            });
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void addRing(IAtom atom, int size) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.addRing(atom, size);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
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
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void addRing(IBond bond, int size) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.addRing(bond, size);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
        }
        updateView();
    }

    public void addPhenyl(IBond bond) {
        JChemPaintEditor editor = findActiveEditor();
        if (editor != null) {
            IChemModelRelay relay = editor.getControllerHub();
            relay.addPhenyl(bond);
        } else {
            Activator.getDefault().getJsConsoleManager().say("No opened JChemPaint editor");
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
            model.setBondLength(bondLength);
        }
    }

    public void setAtomRadius(double atomRadius) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setAtomRadius(atomRadius);
        }
    }

    public void setIsCompact(boolean isCompact) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setIsCompact(isCompact);
        }
    }

    public void setBondDistance(double bondDistance) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setBondDistance(bondDistance);
        }
    }

    public void setBondWidth(double bondWidth) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setBondWidth(bondWidth);
        }
    }

    public void setDrawNumbers(boolean setDrawNumbers) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setDrawNumbers(setDrawNumbers);
        }
    }

    public void setFitToScreen(boolean fitToScreen) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setFitToScreen(fitToScreen);
        }
    }

    public void setHighlightDistance(double highlightDistance) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setHighlightDistance(highlightDistance);
        }
    }

    public void setRingProportion(double ringProportion) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setBondDistance(ringProportion);
        }
    }

    public void setShowAromaticity(boolean showAromaticity) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setShowAromaticity(showAromaticity);
        }
    }

    public void setShowAromaticityInCDKStyle(boolean showAromaticityCDK) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setShowAromaticityCDKStyle(showAromaticityCDK);
        }
    }

    public void setShowEndCarbons(boolean showEndCarbons) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setShowEndCarbons(showEndCarbons);
        }
    }

    public void setShowExplicitHydrogens(boolean explicitHydrogens) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setShowExplicitHydrogens(explicitHydrogens);
        }
    }

    public void setShowImplicitHydrogens(boolean implicitHydrogens) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setShowImplicitHydrogens(implicitHydrogens);
        }
    }

    public void setWedgeWidth(double wedgeWidth) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setWedgeWidth(wedgeWidth);
        }
    }

    public void setMargin(double margin) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setMargin(margin);
        }
    }
    
    public void setZoom(double zoom) {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            model.setZoomFactor(zoom);
        }
    }

    public double getAtomRadius() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getAtomRadius();
        } else {
            return 0;
        }
    }

    public double getBondDistance() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getBondDistance();
        } else {
            return 0;
        }
    }

    public double getBondLength() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getBondLength();
        } else {
            return 0;
        }
    }

    public double getBondWidth() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getBondWidth();
        } else {
            return 0;
        }
    }

    public boolean getDrawNumbers() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getDrawNumbers();
        } else {
            return false;
        }
    }

    public boolean getFitToScreen() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.isFitToScreen();
        } else {
            return false;
        }
    }

    public double getHighlightDistance() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getHighlightDistance();
        } else {
            return 0;
        }
    }

    public boolean getIsCompact() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getIsCompact();
        } else {
            return false;
        }
    }

    public double getRingProportion() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getRingProportion();
        } else {
            return 0;
        }
    }

    public boolean getShowAromaticity() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getShowAromaticity();
        } else {
            return false;
        }
    }

    public boolean getShowAromaticityInCDKStyle() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getShowAromaticityCDKStyle();
        } else {
            return false;
        }
    }

    public boolean getShowEndCarbons() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getShowEndCarbons();
        } else {
            return false;
        }
    }

    public boolean getShowExplicitHydrogens() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getShowExplicitHydrogens();
        } else {
            return false;
        }
    }

    public boolean getShowImplicitHydrogens() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getShowImplicitHydrogens();
        } else {
            return false;
        }
    }

    public double getWedgeWidth() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getWedgeWidth();
        } else {
            return 0;
        }
    }

    public double getMargin() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getMargin();
        } else {
            return 0;
        }
    }
    
    public double getZoom() {
        RendererModel model = this.getRendererModel();
        if (model != null) {
            return model.getZoomFactor();
        } else {
            return 0;
        }
    }
}
