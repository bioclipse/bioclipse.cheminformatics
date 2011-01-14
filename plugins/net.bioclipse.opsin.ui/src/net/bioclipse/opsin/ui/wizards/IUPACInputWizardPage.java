/* Copyright (c) 2011  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/epl-v10.html/.
 * 
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.opsin.ui.wizards;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.opsin.business.OpsinManager;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class IUPACInputWizardPage extends WizardPage {

	private NewFromIUPACWizard wizard;
	
    public IUPACInputWizardPage(String pageName, NewFromIUPACWizard wizard) {
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
        String iupac = field.getText();
        if (iupac.length() != 0) {
        	OpsinManager opsin = new OpsinManager();
            try {
            	opsin.parseIUPACName(iupac, null);
            	this.wizard.setIUPAC(iupac);
            } catch (BioclipseException exception) {
                setMessage(exception.getMessage());
                setErrorMessage("The given IUPAC name is invalid.");
                return;
            }
        }
        setPageComplete(true);
        setMessage(null);
        setErrorMessage(null);
    }
    
    public boolean canFlipToNextPage() {
        return false; // there is not next window
    }
}

