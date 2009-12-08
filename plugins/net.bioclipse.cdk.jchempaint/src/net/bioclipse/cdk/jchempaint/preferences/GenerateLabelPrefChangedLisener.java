/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.preferences;

import net.bioclipse.cdk.jchempaint.Activator;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget.Message;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class GenerateLabelPrefChangedLisener implements IPropertyChangeListener{
    
    private JChemPaintWidget widget;
    
    public GenerateLabelPrefChangedLisener(JChemPaintWidget widget) {
        this.widget = widget;
    }

    public static boolean showGeneratedLabel() {
        return Activator.getDefault().getPreferenceStore()
                        .getBoolean( PreferenceConstants.SHOW_LABEL_GENERATED );
    }

    public void propertyChange( PropertyChangeEvent event ) {
        if (event.getProperty().equals(PreferenceConstants.SHOW_LABEL_GENERATED)) {
            Boolean newValue = (Boolean) event.getNewValue();
            if(newValue == null) return;
            if(!newValue)
                widget.remove( Message.GENERATED );
        }
    }
}
