/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.smartsmatching;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.smartsmatching.model.SmartsWrapper;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


public class AddEditSmartsDialog extends TitleAreaDialog{

    private SmartsWrapper smartsWrapper;
    private Text txtName;
    private Text txtSmarts;
    private String name;
    private String smarts;
    private ICDKManager cdk;


    public AddEditSmartsDialog(Shell parentShell) {
        this(parentShell,null);
    }

    public AddEditSmartsDialog(Shell shell, SmartsWrapper sw) {
        super(shell);
        if (sw!=null){
            this.smartsWrapper=sw;
            this.name=sw.getName();
            this.smarts=sw.getSmartsString();
        }
        else{
            this.smartsWrapper=new SmartsWrapper();
        }
        
        cdk=Activator.getDefault().getJavaCDKManager();

    }

    protected Control createDialogArea(Composite parent) {

        setTitle("Add/Edit SMARTS");
        setMessage("Enter name and SMARTS string");

        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        txtName = new Text(container, SWT.BORDER);
        txtName.setBounds(20, 30, 180, 30);
        if (name!=null)
            txtName.setText(name);
        txtName.addKeyListener( new KeyListener(){

            public void keyPressed( KeyEvent e ) {
            }

            public void keyReleased( KeyEvent e ) {
                updateStatus();
            }
            
        });
        
        txtSmarts = new Text(container, SWT.BORDER);
        txtSmarts.setBounds(235, 30, 350, 30);
        if (smarts!=null)
            txtSmarts.setText(smarts);
        txtSmarts.addKeyListener( new KeyListener(){

            public void keyPressed( KeyEvent e ) {
            }

            public void keyReleased( KeyEvent e ) {
                updateStatus();
            }
            
        });


        final Label lblName = new Label(container, SWT.NONE);
        lblName.setBounds(20, 10, 185, 20);
        lblName.setText("Name");

        final Label lblFileExtension = new Label(container, SWT.NONE);
        lblFileExtension.setBounds(235, 10, 220, 20);
        lblFileExtension.setText("SMARTS String");

        return area;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                     true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                     IDialogConstants.CANCEL_LABEL, false);
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {

            if (txtName.getText().length()<=0){
                showMessage("Name cannot be empty");
                return;
            }
            if (txtSmarts.getText().length()<=0){
                showMessage("URL cannot be empty");
                return;
            }

            updateStatus();


            smartsWrapper.setName( txtName.getText() );
            smartsWrapper.setSmartsString( txtSmarts.getText() );

            okPressed();
            return;
        }
        super.buttonPressed(buttonId);
    }

    private void updateStatus(){

        setErrorMessage( null );
        if (txtName.getText().length()<=0){
            setErrorMessage("Name must not be empty");
            return;
        }
        if (txtSmarts.getText().length()<=0){
            setErrorMessage("SMARTS string must not be empty");
            return;
        }

        if (!(cdk.isValidSmarts( txtSmarts.getText() ))){
            setErrorMessage( "SMARTS string is not valid. " );
        }
        
        getButtonBar().update();

    }

    private void showMessage(String message) {
        MessageDialog.openInformation(
                                      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                      "Update Site",
                                      message);
    }

    public Text getTxtFileExtension() {
        return txtSmarts;
    }

    public void setTxtFileExtension(String filext) {
        this.txtSmarts.setText(filext);
    }

    public Text getTxtName() {
        return txtName;
    }

    public void setTxtName(String name) {
        this.txtName.setText(name);
    }


    public SmartsWrapper getSmartsWrapper() {

        return smartsWrapper;
    }


    public void setSmartsWrapper( SmartsWrapper smartsWrapper ) {

        this.smartsWrapper = smartsWrapper;
    }

}
