/*
 *  $RCSfile: InsertFromSmiles.java,v $
 *  $Author: egonw $
 *  $Date: 2005/11/18 12:34:45 $
 *  $Revision: 1.21 $
 *
 *  Copyright (C) 1997-2005  The JChemPaint project
 *
 *  Contact: jchempaint-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.applications.jchempaint.dialogs;

import javax.swing.JTextField;
import javax.vecmath.Vector2d;

import net.bioclipse.cdk.ui.editors.JCPMultiPageEditor;
import net.bioclipse.cdk.ui.editors.JCPMultiPageEditorContributor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.applications.jchempaint.DrawingPanel;
import org.openscience.cdk.applications.jchempaint.JChemPaintModel;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.layout.TemplateHandler;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * Frame to allow for changing the propterties.
 *
 * @cdk.module jchempaint
 * @author     steinbeck
 */
public class InsertFromSmiles extends Dialog
{
    
    JTextField valueText;
    private Shell shell;
    private Button insertButton;
    private Button cancelButton;
    private JChemPaintModel jcpModel;
    private Text smilesTextBox;
    private JCPMultiPageEditorContributor contributor;
    
    
    /**
     *  Constructor for the InsertFromSmiles object
     * @param contributor 
     *
     *@param  jcpPanel  Description of the Parameter
     */
    public InsertFromSmiles(Shell parent, int style, JCPMultiPageEditorContributor contributor)
    {
        super(parent, style);
        this.contributor = contributor;
        this.jcpModel = ((JCPMultiPageEditor)contributor.getActiveEditorPart()).getJcpModel();
        this.setText("Insert from SMILES");
    }
    
    public Object open(){
        Shell parent = getParent();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText(getText());
        final Display display = parent.getDisplay();
        
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        shell.setLayout(gridLayout);
        
        Label smilesLabel = new Label(shell, SWT.NONE);
        smilesLabel.setText("Enter SMILES string:");
        
        smilesTextBox = new Text(shell, SWT.BORDER);
        GridData textBoxData = new GridData();
        textBoxData.widthHint = 200;
        smilesTextBox.setLayoutData(textBoxData);
        
        insertButton = new Button(shell, SWT.PUSH);
        insertButton.setText("Insert");
        GridData insertButtonData = new GridData();
        insertButtonData.horizontalAlignment = SWT.RIGHT;
        insertButton.setLayoutData(insertButtonData);
        insertButton.addSelectionListener(new InsertSmilesListener());
        
        
        cancelButton = new Button(shell, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new InsertSmilesListener());
        
        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        return display;
    }
    
    private class InsertSmilesListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == insertButton) {
                generateModel();
                
            }
            else if (e.getSource() == cancelButton) {
                shell.dispose();
            }
        }
        
        private void generateModel() {
            try
            {
                String SMILES = smilesTextBox.getText();
                SmilesParser sp = new SmilesParser(null);//FIXME: replace null
                IMolecule m = sp.parseSmiles(SMILES);
                
                // ok, get relevent bits from active model
                Renderer2DModel renderModel = jcpModel.getRendererModel();
                IChemModel chemModel = jcpModel.getChemModel();
                IMoleculeSet moleculeSet = chemModel.getMoleculeSet();
                if (moleculeSet == null) {
                    moleculeSet = new MoleculeSet();
                }
                
                // ok, now generate 2D coordinates
                StructureDiagramGenerator sdg = new StructureDiagramGenerator();
                sdg.setTemplateHandler(new TemplateHandler(chemModel.getBuilder()));
                try
                {
                    sdg.setMolecule(m);
                    sdg.generateCoordinates(new Vector2d(0,1));
                    m = sdg.getMolecule();
                    double bondLength = renderModel.getBondLength();
                    double scaleFactor = GeometryTools.getScaleFactor(m, bondLength,jcpModel.getRendererModel().getRenderingCoordinates());
                    GeometryTools.scaleMolecule(m, scaleFactor,jcpModel.getRendererModel().getRenderingCoordinates());
                    //if there are no atoms in the actual chemModel all 2D-coordinates would be set to NaN
                    if (chemModel.getMoleculeSet().getAtomContainer(0).getAtomCount() != 0) {
                        GeometryTools.translate2DCenterTo(m,
                                GeometryTools.get2DCenter(chemModel.getMoleculeSet().getAtomContainer(0),jcpModel.getRendererModel().getRenderingCoordinates()
                                ),jcpModel.getRendererModel().getRenderingCoordinates()
                        );
                    }
                    GeometryTools.translate2D(m, new Vector2d(5*bondLength, 0),jcpModel.getRendererModel().getRenderingCoordinates()); // in pixels
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
                
                // now add the structure to the active model
                moleculeSet.addMolecule(m);
                // and select it
                renderModel.setSelectedPart(m);
                jcpModel.fireChange();
                DrawingPanel drawingPanel = ((JCPMultiPageEditor)contributor.getActiveEditorPart()).getDrawingPanel();
                drawingPanel.repaint();
                shell.dispose();
                
                
            } catch (InvalidSmilesException ise)
            {
                MessageBox warningDialog = new MessageBox(shell, SWT.ICON_WARNING);
                warningDialog.setMessage("Invalid SMILES String!");
                warningDialog.open();
            }
            
        }
    }
}
