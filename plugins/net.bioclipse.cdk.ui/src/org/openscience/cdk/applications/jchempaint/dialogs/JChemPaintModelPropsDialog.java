/* $RCSfile: JChemPaintModelPropsEditor.java,v $
 * $Author: egonw $
 * $Date: 2005/10/22 16:25:37 $
 * $Revision: 1.5 $
 *
 * Copyright (C) 2003-2005  The JChemPaint project
 *
 * Contact: jchempaint-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
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
package org.openscience.cdk.applications.jchempaint.dialogs;


import java.util.Properties;

import javax.swing.JTextField;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openscience.cdk.applications.jchempaint.JChemPaintModel;

/**
 * Internal frame to allow for changing the propterties.
 *
 * @cdk.module jchempaint
 */
public class JChemPaintModelPropsDialog extends Dialog {

    Properties props;
    JTextField author;
    JTextField software;
    JTextField date;
    private Shell shell;
    private JChemPaintModel jcpModel;
    private Button okButton;
    private Button cancelButton;
    private Text softwareBox;
    private Text generatedBox;
    private Text authorBox;

    public JChemPaintModelPropsDialog(Shell parent, int style, JChemPaintModel jcpModel) {
        super(parent, style);
        this.setText("Edit Model Properties...");
        this.jcpModel = jcpModel;
    }

    public Object open() {
        Shell parent = getParent();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText(getText());

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        shell.setLayout(gridLayout);

        Label authorLabel = new Label(shell, SWT.NONE);
        authorLabel.setText("Author:");
        authorBox = new Text(shell, SWT.BORDER);
        if (jcpModel.getAuthor() != null) {
            authorBox.setText(jcpModel.getAuthor());
        }
        GridData data = new GridData();
        data.widthHint = 100;
        authorBox.setLayoutData(data);

        Label softwareLabel = new Label(shell, SWT.NONE);
        softwareLabel.setText("Software:");
        softwareBox = new Text(shell, SWT.BORDER);
        if (jcpModel.getSoftware() != null) {
            softwareBox.setText(jcpModel.getSoftware());
        }
        softwareBox.setLayoutData(data);

        Label generatedLabel = new Label(shell, SWT.NONE);
        generatedLabel.setText("Generated on:");
        generatedBox = new Text(shell, SWT.BORDER);
        if (jcpModel.getGendate() != null) {
            generatedBox.setText(jcpModel.getGendate());
        }
        generatedBox.setLayoutData(data);

        GridData buttonData = new GridData();
        buttonData.horizontalAlignment = SWT.RIGHT;
        okButton = new Button(shell, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(buttonData);
        okButton.addSelectionListener(new ModelDialogListener());

        cancelButton = new Button(shell, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new ModelDialogListener());

        shell.pack();
        shell.open();
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        return display;
    }

    private class ModelDialogListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == okButton) {
                setSettings();
                shell.dispose();
            }
            else if (e.getSource() == cancelButton) {
                shell.dispose();
            }

        }

        private void setSettings() {
            jcpModel.setAuthor(authorBox.getText());
            jcpModel.setSoftware(softwareBox.getText());
            jcpModel.setGendate(generatedBox.getText());
        }
    }

//    public void closeFrame(){
//        dispose();
//    }
//
//    class UpdateAction extends AbstractAction {
//        UpdateAction() {
//            super("Update");
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            jcpm.setAuthor(author.getText());
//            jcpm.setSoftware(software.getText());
//            jcpm.setGendate(date.getText());
//            closeFrame();
//        }
//    }
//
//    class CancelAction extends AbstractAction {
//        CancelAction() {
//            super("Cancel");
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            closeFrame();
//        }
//    }
//
//    class EditAction extends AbstractAction {
//        private String prop = "";
//
//        EditAction(String prop) {
//            super("Edit");
//            this.prop = prop;
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            // do not validate content
//        }
//    }
 }
