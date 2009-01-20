/*******************************************************************************
 * Copyright (c) 2005 Bioclipse Project
 *               2009 Egon Willighagen <egonw@users.sf.net>
 *               
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/
package net.bioclipse.chemoinformatics;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * The Bioclipse Chemoinformatic perspective.
 * 
 * @author ola
 *
 */
public class ChemoinformaticsPerspective implements IPerspectiveFactory {

	IPageLayout storedLayout;

	public static final String ID_PERSPECTIVE =
		"net.bioclipse.chemoinformatics.ChemoinformaticsPerspective";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout left_folder_layout =
			layout.createFolder(
					"left",
					IPageLayout.LEFT,
					0.20f,
					editorArea);

		IFolderLayout right_folder_layout =
			layout.createFolder(
					"right",
					IPageLayout.RIGHT,
					0.70f,
					editorArea);

		IFolderLayout bottom_folder_layout =
			layout.createFolder(
					"bottom",
					IPageLayout.BOTTOM,
					0.70f,
					editorArea);


		IFolderLayout rightBottom_folder_layout =
			layout.createFolder(
					"rightBottom",
					IPageLayout.BOTTOM,
					0.50f,
			"right");

		IFolderLayout leftBottom_folder_layout =
			layout.createFolder(
					"leftBottom",
					IPageLayout.BOTTOM,
					0.70f,
			"left");

		IFolderLayout COT_layout =
			layout.createFolder(
					"cot",
					IPageLayout.RIGHT,
					0.70f,
					editorArea);

		left_folder_layout.addView("net.bioclipse.views.BioResourceView");
		bottom_folder_layout.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		rightBottom_folder_layout.addView(IPageLayout.ID_PROP_SHEET);

//		Note:
//		We ship Bioclipse with jmol and cdk
//		therefore, we can add the views on first start
//		When this changes, change addView to addPlaceHolder
		right_folder_layout.addView("net.bioclipse.plugins.views.JmolView");
		COT_layout.addView("net.bioclipse.plugins.views.ChemTreeView");

		leftBottom_folder_layout.addView("net.bioclipse.plugins.views.Structure2DView");

		layout.addNewWizardShortcut("net.bioclipse.wizards.NewFileWizard");
		layout.addNewWizardShortcut("net.bioclipse.wizards.NewFolderWizard");
		layout.addNewWizardShortcut("net.bioclipse.wizards.NewMoleculeWizard");
		layout.addNewWizardShortcut("net.bioclipse.wizards.NewMoleculeFromSMILESWizard");

		layout.addPerspectiveShortcut(ID_PERSPECTIVE);
		layout.addShowViewShortcut("net.bioclipse.views.BioResourceView");
		layout.addShowViewShortcut("net.bioclipse.plugins.views.JmolView");
		layout.addShowViewShortcut("net.bioclipse.plugins.views.Structure2DView");
		layout.addShowViewShortcut("net.bioclipse.plugins.views.ChemTreeView");
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
	}

	public IPageLayout getStoredLayout() {
		return storedLayout;
	}

}
