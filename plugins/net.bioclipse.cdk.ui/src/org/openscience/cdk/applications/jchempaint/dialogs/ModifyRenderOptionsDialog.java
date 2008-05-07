/* $RCSfile: ModifyRenderOptionsDialog.java,v $
 * $Author: egonw $
 * $Date: 2005/10/22 16:25:37 $
 * $Revision: 1.5 $
 *
 * Copyright (C) 2004-2005  The JChemPaint project
 *
 * Contact: jchempaint-devel@lists.sf.net
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

import java.awt.Font;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openscience.cdk.applications.jchempaint.JChemPaintModel;
import org.openscience.cdk.applications.swing.editor.Renderer2DModelEditor;
import org.openscience.cdk.renderer.Renderer2DModel;

/**
  * Simple Dialog that shows the loaded dictionaries..
  *
  * @cdk.module jchempaint
  */
public class ModifyRenderOptionsDialog extends Dialog {

    private Renderer2DModelEditor editor;
    private JChemPaintModel jcpmodel;
    private Shell shell;
    private Font currentFont;
    private Button drawNumbers;
    private Button showAtomAtomMapping;
    private Button useKekuleStructure;
    private Button showEndCarbons;
    private Button showImplicitHydrogens;
    private Button showAromaticity;
    private Button showAromaticityInCDKStyle;
    private Button colorAtomsByType;
    private Button useAA;
    private Button showToolTip;
    private Renderer2DModel renderModel;
    private Button okButton;
    private Button applyButton;
    private Button cancelButton;
    private Label fontLabel;

    /**
     * Displays the Info Dialog for JChemPaint.
     * @param renderModel
     */
    public ModifyRenderOptionsDialog(Shell parent, int style, Renderer2DModel renderModel) {
        super(parent, style);
        this.renderModel = renderModel;
        this.setText("Renderer2D Options");
        this.jcpmodel = jcpmodel;
//        editor = new Renderer2DModelEditor(this);
//        createDialog();
//        pack();
//        setVisible(true);
    }

    public Object open(){
        Shell parent = getParent();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText(getText());
        final Display display = parent.getDisplay();

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        shell.setLayout(gridLayout);

        currentFont = null;

        GridData drawNumbersData = new GridData();
        drawNumbersData.horizontalSpan = 3;
        drawNumbers = new Button(shell, SWT.CHECK);
        drawNumbers.setText("Draw atom numbers");
        drawNumbers.setLayoutData(drawNumbersData);

        GridData showAtomAtomMappingData = new GridData();
        showAtomAtomMappingData.horizontalSpan = 3;
        showAtomAtomMapping =new Button(shell, SWT.CHECK);
        showAtomAtomMapping.setText("Show atom-atom mappings");
        showAtomAtomMapping.setLayoutData(showAtomAtomMappingData);

        GridData useKekuleStructureData = new GridData();
        useKekuleStructureData.horizontalSpan = 3;
        useKekuleStructure = new Button(shell, SWT.CHECK);
        useKekuleStructure.setText("Explicit carbons");
        useKekuleStructure.setLayoutData(useKekuleStructureData);

        GridData showEndCarbonsData = new GridData();
        showEndCarbonsData.horizontalSpan = 3;
        showEndCarbons = new Button(shell, SWT.CHECK);
        showEndCarbons.setText("Show explicit methyl groups");
        showEndCarbons.setLayoutData(showEndCarbonsData);

        GridData showImplicitHydrogensData = new GridData();
        showImplicitHydrogensData.horizontalSpan = 3;
        showImplicitHydrogens = new Button(shell, SWT.CHECK);
        showImplicitHydrogens.setText("Show implicit hydrogens");
        showImplicitHydrogens.setLayoutData(showImplicitHydrogensData);

        GridData showAromaticityData = new GridData();
        showAromaticityData.horizontalSpan = 3;
        showAromaticity = new Button(shell, SWT.CHECK);
        showAromaticity.setText("Use aromatic ring circles");
        showAromaticity.setLayoutData(showAromaticityData);

        GridData showAromaticityInCDKStyleData = new GridData();
        showAromaticityInCDKStyleData.horizontalSpan = 3;
        showAromaticityInCDKStyle = new Button(shell, SWT.CHECK);
        showAromaticityInCDKStyle.setText("Use CDK style aromaticity indicators");
        showAromaticityInCDKStyle.setLayoutData(showAromaticityInCDKStyleData);

        GridData colorAtomsByTypeData = new GridData();
        colorAtomsByTypeData.horizontalSpan = 3;
        colorAtomsByType = new Button(shell, SWT.CHECK);
        colorAtomsByType.setText("Color atoms by element");
        colorAtomsByType.setLayoutData(colorAtomsByTypeData);

        GridData useAAData = new GridData();
        useAAData.horizontalSpan = 3;
        useAA = new Button(shell, SWT.CHECK);
        useAA.setText("Use Anti-Aliasing");
        useAA.setLayoutData(useAAData);

        GridData showToolTipData = new GridData();
        showToolTipData.horizontalSpan = 3;
        showToolTip = new Button(shell, SWT.CHECK);
        showToolTip.setText("Show tooltips");
        showToolTip.setLayoutData(showToolTipData);

        setModel();

        GridData fontButtonData = new GridData();
        fontButtonData.horizontalSpan = 3;
        final Button fontButton = new Button(shell, SWT.PUSH | SWT.BORDER);
        fontButton.setText("Change Font...");
        fontButton.setLayoutData(fontButtonData);
        fontButton.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            FontDialog fd = new FontDialog(shell, SWT.NONE);
            fd.setText("Select Font");
//            fd.setRGB(new RGB(0, 0, 255));
            FontData defaultFont = new FontData("Sans", 10, SWT.BOLD);
            fd.setFontData(defaultFont);
            FontData newFont = fd.open();
            if (newFont == null)
              return;
            currentFont = new Font(newFont.getName(), newFont.getStyle(), newFont.getHeight());
//            t.setForeground(new Color(d, fd.getRGB()));
          }
        });

        okButton = new Button(shell, SWT.PUSH);
        okButton.setText("OK");
        okButton.addSelectionListener(new ModifyRenderOptionsListener());


        applyButton = new Button(shell, SWT.PUSH);
        applyButton.setText("Apply");
        applyButton.addSelectionListener(new ModifyRenderOptionsListener());


        cancelButton = new Button(shell, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new ModifyRenderOptionsListener());

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        return display;
    }

    public void setModel() {
        drawNumbers.setSelection(renderModel.getDrawNumbers());
        showAtomAtomMapping.setSelection(renderModel.getShowAtomAtomMapping());
        useKekuleStructure.setSelection(renderModel.getKekuleStructure());
        showEndCarbons.setSelection(renderModel.getShowEndCarbons());
        showImplicitHydrogens.setSelection(renderModel.getShowImplicitHydrogens());
        showAromaticity.setSelection(renderModel.getShowAromaticity());
        showAromaticityInCDKStyle.setSelection(renderModel.getShowAromaticityInCDKStyle());
        colorAtomsByType.setSelection(renderModel.getColorAtomsByType());
        useAA.setSelection(renderModel.getUseAntiAliasing());
        showToolTip.setSelection(renderModel.getShowTooltip());
        currentFont = renderModel.getFont();
    }

    private class ModifyRenderOptionsListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == okButton) {
                setSettings();
                shell.dispose();
            }
            else if (e.getSource() == applyButton) {
                setSettings();
            }
            else if (e.getSource() == cancelButton) {
                shell.dispose();
            }

        }

        private void setSettings() {
            renderModel.setDrawNumbers(drawNumbers.getSelection());
            renderModel.setShowAtomAtomMapping(showAtomAtomMapping.getSelection());
            renderModel.setKekuleStructure(useKekuleStructure.getSelection());
            renderModel.setShowEndCarbons(showEndCarbons.getSelection());
            renderModel.setShowImplicitHydrogens(showImplicitHydrogens.getSelection());
            renderModel.setShowAromaticity(showAromaticity.getSelection());
            renderModel.setShowAromaticityInCDKStyle(showAromaticityInCDKStyle.getSelection());
            renderModel.setColorAtomsByType(colorAtomsByType.getSelection());
            renderModel.setUseAntiAliasing(useAA.getSelection());
            renderModel.setShowTooltip(showToolTip.getSelection());
            renderModel.setFont(currentFont);
        }
    }
}
