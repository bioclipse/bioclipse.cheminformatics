/*******************************************************************************
 * Copyright (c) 2007      Jonathan Alvarsson
 *               2007-2008 Ola Spjuth
 *               2008-2009 Egon Willighagen
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
import java.util.List;

import net.bioclipse.cdk.jchempaint.Activator;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.jchempaint.preferences.PreferenceConstants;
import net.bioclipse.core.business.BioclipseException;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.openscience.cdk.renderer.RendererModel;

/**
 * Provides scripting for the global JChemPaint preferences.
 * 
 * @author egonw
 */
public class JChemPaintGlobalPropertiesManager
    implements IJChemPaintGlobalPropertiesManager {

    public String getNamespace() {
        return "jcpprop";
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
            if (ePart instanceof JChemPaintEditor) {
                jcpEditors.add((JChemPaintEditor)ePart);
            }
        }
        return jcpEditors;
    }

    public void applyGlobalProperties() throws BioclipseException {
        for (final JChemPaintEditor editor : getEditors()) {
            RendererModel model = this.getRendererModel(editor);
            model.setShowAromaticity(getShowAromaticity());
            model.setShowEndCarbons(getShowEndCarbons());
            model.setMargin(getMargin());
            
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

    private RendererModel getRendererModel(JChemPaintEditor editor) {
        if (editor == null) return null;
        
        return editor.getControllerHub().getRenderer().getRenderer2DModel();
    }

    private void throwCannotGetPreferenceException() throws BioclipseException {
        throw new BioclipseException("Cannot get access to the preferences...");
    }

    public boolean getShowAromaticity() throws BioclipseException {
        Preferences prefs = getPreferences();
        if (prefs != null) {
            return prefs.getBoolean(PreferenceConstants.SHOWAROMATICITY_BOOL);
        }
        throwCannotGetPreferenceException();
        return false;
    }

    public void setShowAromaticity(boolean showAromaticity)
    throws BioclipseException{
        Preferences prefs = getPreferences();
        if (prefs != null) {
            prefs.setValue(
                    PreferenceConstants.SHOWAROMATICITY_BOOL, 
                    showAromaticity
            );
        } else {
            throwCannotGetPreferenceException();
        }
        applyGlobalProperties();
    }

    public double getBondLength() throws BioclipseException {
        Preferences prefs = getPreferences();
        if (prefs != null) {
            return prefs.getDouble(PreferenceConstants.BOND_LENGTH_DOUBLE);
        }
        throwCannotGetPreferenceException();
        return 0;
    }

    public void setBondLength(double bondLength) throws BioclipseException {
            Preferences prefs = getPreferences();
            if (prefs != null) {
                prefs.setValue(
                        PreferenceConstants.BOND_LENGTH_DOUBLE, 
                        bondLength
                );
            } else {
                throwCannotGetPreferenceException();
            }
            applyGlobalProperties();
    }

    public boolean getShowEndCarbons() throws BioclipseException{
        Preferences prefs = getPreferences();
        if (prefs != null) {
            boolean pref = prefs.getBoolean(PreferenceConstants.SHOWENDCARBONS_BOOL);
            return pref; 
        }
        throwCannotGetPreferenceException();
        return false;
    }

    public void setShowEndCarbons(boolean showEndCarbons)
    throws BioclipseException{
        Preferences prefs = getPreferences();
        if (prefs != null) {
            prefs.setValue(
                    PreferenceConstants.SHOWENDCARBONS_BOOL, 
                    showEndCarbons
            );
        } else {
            throwCannotGetPreferenceException();
        }
        applyGlobalProperties();
    }

    public double getMargin() throws BioclipseException {
        Preferences prefs = getPreferences();
        if (prefs != null) {
            double pref = prefs.getDouble(PreferenceConstants.MARGIN_DOUBLE);
            return pref; 
        }
        throwCannotGetPreferenceException();
        return 0.0;
    }

    public void setMargin(double margin) throws BioclipseException {
        Preferences prefs = getPreferences();
        if (prefs != null) {
            prefs.setValue(
                    PreferenceConstants.MARGIN_DOUBLE, 
                    margin
            );
        } else {
            throwCannotGetPreferenceException();
        }
        applyGlobalProperties();
    }

}
