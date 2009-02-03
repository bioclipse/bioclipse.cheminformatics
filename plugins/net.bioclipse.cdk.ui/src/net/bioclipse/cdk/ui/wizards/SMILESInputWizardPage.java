/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

public class SMILESInputWizardPage extends WizardPage {

    private NewFromSMILESWizard wizard;
    
    public SMILESInputWizardPage(String pageName, NewFromSMILESWizard wizard) {
        super(pageName);
        this.wizard = wizard;
    }
    
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        container.setLayout(layout);
        setControl(container);
        
        final Label label = new Label(container, SWT.NONE);
        final GridData gridData = new GridData();
        gridData.horizontalSpan = 3;
        label.setLayoutData(gridData);
        label.setText("SMILES");
        
        Text gistNumberField = new Text(container, SWT.BORDER);
        gistNumberField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updatePageComplete((Text)e.getSource());
            }
        });
        gistNumberField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void updatePageComplete(Text field) {
        setPageComplete(false);
        String smiles = field.getText();
        if (smiles.length() != 0) {
            SmilesParser parser = new SmilesParser(
                NoNotificationChemObjectBuilder.getInstance()
            );
            try {
                parser.parseSmiles(smiles);
                wizard.setSMILES(smiles);
            } catch (InvalidSmilesException exception) {
                setMessage(exception.getMessage());
                setErrorMessage("The given SMILES is invalid.");
                return;
            }
        }
        setPageComplete(true);
        setMessage(null);
        setErrorMessage(null);
    }
    
    public boolean canFlipToNextPage() {
        return wizard.getSMILES() != null;
    }
}

