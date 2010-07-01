/*******************************************************************************
 * Copyright (c) 2007      Jonathan Alvarsson
 *               2007-2008 Ola Spjuth
 *               2008-2010 Egon Willighagen
 *                    2009 Gilleain Torrance               
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.jchempaint.Activator;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.jchempaint.preferences.PreferenceConstants;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IServiceScopes;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator.WillDrawAtomNumbers;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.AtomRadius;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.ShowEndCarbons;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.ShowExplicitHydrogens;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondDistance;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondLength;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.WedgeWidth;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Margin;
import org.openscience.cdk.renderer.generators.ExtendedAtomGenerator.ShowImplicitHydrogens;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator.HighlightAtomDistance;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator.HighlightBondDistance;
import org.openscience.cdk.renderer.generators.RingGenerator.ShowAromaticity;

/**
 * Provides scripting for the global JChemPaint preferences.
 * 
 * @author egonw
 */
public class JChemPaintGlobalPropertiesManager implements IBioclipseManager {

	private static final Logger logger = Logger.getLogger(
		JChemPaintGlobalPropertiesManager.class
	);

    public String getManagerName() {
        return "jcpglobal";
    }

    private List<JChemPaintEditor> getEditors() {
        final List<IEditorReference> activeEditors = new ArrayList<IEditorReference>();
        final Display display = PlatformUI.getWorkbench().getDisplay();
        display.syncExec( new Runnable() {
            public void run() {
                IEditorReference[] editors
                    = PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow()
                                .getActivePage()
                                .getEditorReferences();
                for (IEditorReference ref : editors)
                    activeEditors.add(ref);
            }
        });
        List<JChemPaintEditor> jcpEditors = new ArrayList<JChemPaintEditor>();
        for (IEditorReference ref : activeEditors) {
            IEditorPart ePart = ref.getEditor(false);
            if(ePart == null) continue;
            JChemPaintEditor jcpEditor = (JChemPaintEditor)ePart.getAdapter(
                                                        JChemPaintEditor.class);
            if (jcpEditor!=null) {
                jcpEditors.add(jcpEditor);
            }
        }
        return jcpEditors;
    }
    
    /**
     * Tries to apply a parameter value, but since it matching {@link IGenerator}
     * may not be registered, we silently eat the exception.
     */
    private <T extends IGeneratorParameter<S>,S> void applyProperty(
    		RendererModel model, Class<T> paramType, S value) {
    	try {
    		model.set(paramType, value);
    	} catch (Error error) {
    		logger.warn(
    			"Error while applying a rendering property preference: " +
    			error.getMessage()
    		);
    	};
    }
    
    public void applyProperties(RendererModel model) throws BioclipseException {
    	applyProperty(model, ShowAromaticity.class, getShowAromaticity());
    	applyProperty(model, ShowAromaticity.class, getShowAromaticity());
    	applyProperty(model, ShowEndCarbons.class, getShowEndCarbons());
    	applyProperty(model, ShowExplicitHydrogens.class, getShowExplicitHydrogens());
    	applyProperty(model, Margin.class, getMargin());
    	applyProperty(model, AtomRadius.class, getAtomRadius());
    	applyProperty(model, BondLength.class, getBondLength());
    	applyProperty(model, BondDistance.class, getBondDistance());
    	applyProperty(model, HighlightAtomDistance.class, getHighlightAtomDistance());
    	applyProperty(model, HighlightBondDistance.class, getHighlightBondDistance());
    	applyProperty(model, WedgeWidth.class, getWedgeWidth());
    	applyProperty(model, ShowImplicitHydrogens.class, getShowImplicitHydrogens());
    	applyProperty(model, WillDrawAtomNumbers.class, getShowNumbers());

    	ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
      Map filter = new HashMap();
      filter.put(IServiceScopes.WINDOW_SCOPE, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
      service.refreshElements("net.bioclipse.cdk.jchempaint.preference.atomNumbers", filter);
    }

    public void applyGlobalProperties() throws BioclipseException {
        for (final JChemPaintEditor editor : getEditors()) {
            RendererModel model = this.getRendererModel(editor);
            applyProperties(model);
            
            // update the editor's rendering
            PlatformUI.getWorkbench().getDisplay().syncExec( new Runnable() {
                public void run() {
                    if (editor != null) editor.update();
                }
            });
        }
    }
    
    private Preferences getPreferences() {
        return Activator.getDefault().getPluginPreferences();
    }
    
    private void setBool(String pref, boolean value) throws BioclipseException {
        Preferences prefs = getPreferences();
        if (prefs != null) {
            prefs.setValue(pref, value);
        } else {
            throwCannotGetPreferenceException();
        }
        applyGlobalProperties();
    }
    
    private boolean getBool(String pref) throws BioclipseException {
        Preferences prefs = getPreferences();
        if (prefs != null) {
            return prefs.getBoolean(pref);
        }
        throwCannotGetPreferenceException();
        return false;
    }
    
    private void setDouble(
            String pref, double value) throws BioclipseException {
        Preferences prefs = getPreferences();
        if (prefs != null) {
            prefs.setValue(pref, value);
        } else {
            throwCannotGetPreferenceException();
        }
        applyGlobalProperties();
    }
    
    private double getDouble(String pref) throws BioclipseException {
        Preferences prefs = getPreferences();
        if (prefs != null) {
            return prefs.getDouble(pref);
        }
        throwCannotGetPreferenceException();
        return 0;
    }

    private RendererModel getRendererModel(JChemPaintEditor editor) {
        if (editor == null) return null;
        
        return editor.getControllerHub().getRenderer().getRenderer2DModel();
    }

    private void throwCannotGetPreferenceException() throws BioclipseException {
        throw new BioclipseException("Cannot get access to the preferences...");
    }
    
    public boolean getShowAromaticity() throws BioclipseException {
        return getBool(PreferenceConstants.SHOW_AROMATICITY_BOOL);
    }
    
    public void setShowAromaticity(boolean showAromaticity)
    throws BioclipseException{
       setBool(PreferenceConstants.SHOW_AROMATICITY_BOOL, showAromaticity);
    }

    public boolean getShowEndCarbons() throws BioclipseException{
        return getBool(PreferenceConstants.SHOW_END_CARBONS_BOOL);
    }

    public void setShowEndCarbons(boolean showEndCarbons)
    throws BioclipseException{
        setBool(PreferenceConstants.SHOW_END_CARBONS_BOOL, showEndCarbons);
    }

    public boolean getShowExplicitHydrogens() throws BioclipseException {
        return getBool(PreferenceConstants.SHOW_EXPLICIT_HYDROGENS_BOOL);
    }

    public void setShowExplicitHydrogens(boolean showExplicitHydrogens)
            throws BioclipseException {
        setBool(PreferenceConstants.SHOW_EXPLICIT_HYDROGENS_BOOL, 
                showExplicitHydrogens);
    }

    public boolean getShowImplicitHydrogens() throws BioclipseException {
        return getBool(PreferenceConstants.SHOW_IMPLICIT_HYDROGENS_BOOL);
    }

    public void setShowImplicitHydrogens(boolean showImplicitHydrogens)
            throws BioclipseException {
        setBool(PreferenceConstants.SHOW_IMPLICIT_HYDROGENS_BOOL, 
                showImplicitHydrogens);
    }

    public boolean getShowNumbers() throws BioclipseException {
        return getBool(PreferenceConstants.SHOW_NUMBERS_BOOL);
    }

    public void setShowNumbers(boolean showNumbers) throws BioclipseException {
        setBool(PreferenceConstants.SHOW_NUMBERS_BOOL, showNumbers);
    }

    public double getAtomRadius() throws BioclipseException {
        return getDouble(PreferenceConstants.ATOM_RADIUS_DOUBLE);
    }

    public void setAtomRadius(double atomRadius) throws BioclipseException {
       setDouble(PreferenceConstants.ATOM_RADIUS_DOUBLE, atomRadius);
    }

    public double getBondLength() throws BioclipseException {
        return getDouble(PreferenceConstants.BOND_LENGTH_DOUBLE);
    }

    public void setBondLength(double bondLength) throws BioclipseException {
        setDouble(PreferenceConstants.BOND_LENGTH_DOUBLE, bondLength);
    }

    public double getBondWidth() throws BioclipseException {
        return getDouble(PreferenceConstants.BOND_WIDTH_DOUBLE);
    }

    public void setBondWidth(double bondWidth) throws BioclipseException {
        setDouble(PreferenceConstants.BOND_WIDTH_DOUBLE, bondWidth);
    }

    public double getBondDistance() throws BioclipseException {
        return getDouble(PreferenceConstants.BOND_DISTANCE_DOUBLE);
    }

    public void setBondDistance(double bondDistance) throws BioclipseException {
        setDouble(PreferenceConstants.BOND_DISTANCE_DOUBLE, bondDistance);
    }

    public double getHighlightAtomDistance() throws BioclipseException {
        return getDouble(PreferenceConstants.HIGHLIGHT_ATOM_DISTANCE_DOUBLE);
    }

    public void setHighlightAtomDistance(double dist) throws BioclipseException {
        setDouble(PreferenceConstants.HIGHLIGHT_ATOM_DISTANCE_DOUBLE, dist);
    }

    public double getHighlightBondDistance() throws BioclipseException {
        return getDouble(PreferenceConstants.HIGHLIGHT_BOND_DISTANCE_DOUBLE);
    }

    public void setHighlightBondDistance(double dist) throws BioclipseException {
        setDouble(PreferenceConstants.HIGHLIGHT_BOND_DISTANCE_DOUBLE, dist);
    }

    public double getMargin() throws BioclipseException {
        return getDouble(PreferenceConstants.MARGIN_DOUBLE);
    }

    public void setMargin(double margin) throws BioclipseException {
        setDouble(PreferenceConstants.MARGIN_DOUBLE, margin);
    }

    public double getWedgeWidth() throws BioclipseException {
        return getDouble(PreferenceConstants.WEDGE_WIDTH_DOUBLE);
    }

    public void setWedgeWidth(double wedgeWidth) throws BioclipseException {
        setDouble(PreferenceConstants.WEDGE_WIDTH_DOUBLE, wedgeWidth);
    }

}
