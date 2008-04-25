/*******************************************************************************
 * Copyright (c) 2005-2007 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Kuhn <shk3@users.sf.net> - original implementation
 *     Carl <carl_marak@users.sf.net>  - converted into table
 *     Ola Spjuth                      - minor fixes
 *     Egon Willighagen                - adapted for the new renderer from CDK
 *******************************************************************************/
package net.bioclipse.cdk.ui.views;



import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.widgets.JChemPaintWidget;
import net.bioclipse.core.business.BioclipseException;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * 2D Rendering widget using the new Java2D based JChemPaint renderer.
 */
public class Java2DRendererView extends ViewPart
	implements ISelectionListener {
	
    private static final Logger logger = Logger.getLogger(Java2DRendererView.class);

	private JChemPaintWidget canvasView;
	private IMolecule molecule;
	private final static StructureDiagramGenerator sdg = new StructureDiagramGenerator();
	
	private IChemObjectBuilder chemObjBuilder;
	
	/**
	 * The constructor.
	 */
	public Java2DRendererView() {
		
		chemObjBuilder=DefaultChemObjectBuilder.getInstance();
		
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
    	canvasView = new JChemPaintWidget(parent, SWT.PUSH );
		canvasView.setSize( 200, 200 );
		
		// Register this page as a listener for selections
		getViewSite().getPage().addSelectionListener(this);
		
		//See what's currently selected
		ISelection selection=PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow().getSelectionService().getSelection();
		reactOnSelection(selection);
		
	}

	@Override
	public void setFocus() {
		canvasView.setFocus();
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		reactOnSelection(selection);
	}
	
	private void reactOnSelection(ISelection selection) {
		
		if (!(selection instanceof IStructuredSelection))
		return;
		IStructuredSelection ssel = (IStructuredSelection) selection;
		
		Object obj = ssel.getFirstElement();
		
		//If we have an ICDKMolecule, just get the AC
		if (obj instanceof ICDKMolecule) {
			ICDKMolecule mol = (ICDKMolecule) obj;
			if (mol.getAtomContainer()==null){
				logger.debug("CDKMolecule but can't get AtomContainer.");
				return;
			}
			setAtomContainer(mol.getAtomContainer());
		}
		
		//We can always get the SMILES and render an IMolecule
		else if (obj instanceof net.bioclipse.core.domain.IMolecule) {
			net.bioclipse.core.domain.IMolecule mol = (net.bioclipse.core.domain.IMolecule) obj;
			try {
				setMoleculeFromSMILES(mol.getSmiles());
			} catch (BioclipseException e) {
				e.printStackTrace();
			}
		}
		
	}


	private void setMoleculeFromSMILES(String smiles) {
		try {
//			molecule = new SmilesParser(chemObjBuilder).parseSmiles("CCOCCN(C)C");
			molecule = new SmilesParser(chemObjBuilder).parseSmiles("smiles");
			sdg.setMolecule((IMolecule)molecule.clone());
			sdg.generateCoordinates();
			molecule = sdg.getMolecule();
			setAtomContainer(molecule);
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void setAtomContainer(IAtomContainer ac) {
			try {
				canvasView.setAtomContainer(ac);
				canvasView.redraw();
			} catch (Exception e) {
				logger.debug("Error displaying molecule in viewer: " + e.getMessage());
			}
		
	}

	
    /**
     * Unsubscriped from listening to the <code>BioResourceView</code> and
     * delegates to superclass implementations.
     */
    @Override
    public void dispose() {
        getViewSite().getPage().removeSelectionListener(this);
        canvasView.dispose();
        super.dispose();
    }
}