/******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/
package net.bioclipse.cdk.jchempaint.preferences;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Editor for doubles, downloaded from http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.gmf/plugins/org.eclipse.gmf.runtime.diagram.ui/src/org/eclipse/gmf/runtime/diagram/ui/preferences/RulerGridPreferencePage.java?root=Modeling_Project&view=co.
 *
 * @author egonw
 */
public class DoubleFieldEditor extends StringFieldEditor {

    private double minValidValue = 00.009;
    private double maxValidValue = 99.999;

    public DoubleFieldEditor(String pref, String label, Composite parent ) {
        super(pref,label,parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.StringFieldEditor#doCheckState()
     */
    protected boolean doCheckState() {
        Text text = getTextControl();

        if (text == null)
            return false;

        try {
            NumberFormat numberFormatter = NumberFormat.getInstance();
            ParsePosition parsePosition = new ParsePosition(0);
            Number parsedNumber = numberFormatter.parse(text.getText(), parsePosition);

            if (parsedNumber == null) {
                showErrorMessage();
                return false;
            }

            Double pageHeight = forceDouble(parsedNumber);
            double number = pageHeight.doubleValue();
            if (number >= minValidValue && number <= maxValidValue 
                    && parsePosition.getIndex() == text.getText().length()) {
                clearErrorMessage();
                return true;
            } else {
                showErrorMessage();
                return false;
            }
        } catch (NumberFormatException e1) {
            showErrorMessage();
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.StringFieldEditor#doLoadDefault()
     */
    protected void doLoadDefault() {
        Text text = getTextControl();
        if (text != null) {
            double value = getPreferenceStore().getDefaultDouble(getPreferenceName());
            NumberFormat numberFormatter = NumberFormat.getNumberInstance();
            text.setText(numberFormatter.format(value));
        }
        valueChanged();
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoad() {
        Text text = getTextControl();           
        if (text != null) {
            double value = getPreferenceStore().getDouble(getPreferenceName());
            NumberFormat numberFormatter = NumberFormat.getNumberInstance();
            text.setText(numberFormatter.format(value));                
        }
    }       

    protected void doStore() {
        NumberFormat numberFormatter = NumberFormat.getInstance();              
        Double gridWidth;
        try {
            gridWidth = forceDouble(numberFormatter.parse(getTextControl().getText()));
            getPreferenceStore().setValue(getPreferenceName(), gridWidth.doubleValue());                
        } catch (ParseException e) {
            showErrorMessage();
        }
    }
    
    private Double forceDouble(Number number) {
        if (!(number instanceof Double))
            return new Double(number.doubleValue());            
        return (Double) number;
    }   

}
