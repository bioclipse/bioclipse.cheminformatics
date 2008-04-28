/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.editors.sdf;

import net.bioclipse.cdk.domain.CDKMoleculeList;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class StructureTablePage extends FormPage {

    private static final Logger logger = Logger.getLogger(StructureTablePage.class);

    private Table table;
	private TableViewer viewer;
	
	public StructureTablePage(FormEditor editor) {
		super(editor, "bc.structuretable", "Structure table");
	}

	/**
	 * Add content to form
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {

		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		form.setText("Structure table");
//		form.setBackgroundImage(FormArticlePlugin.getDefault().getImage(FormArticlePlugin.IMG_FORM_BG));
		final Composite body = form.getBody();
		FillLayout layout=new FillLayout();
		body.setLayout(layout);
		
		viewer = new TableViewer(body, SWT.BORDER |  SWT.FULL_SELECTION | SWT.VIRTUAL);
		table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		toolkit.adapt(table, true, true);

		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		
		//Add columns
		TableViewerColumn col=new TableViewerColumn(viewer,SWT.NONE);
		col.getColumn().setText("Structure");
		tableLayout.addColumnData(new ColumnPixelData(200));
		
		TableViewerColumn col2=new TableViewerColumn(viewer,SWT.NONE);
		col2.getColumn().setText("SMILES");
		tableLayout.addColumnData(new ColumnPixelData(300));

		TableViewerColumn col3=new TableViewerColumn(viewer,SWT.NONE);
		col3.getColumn().setText("Name");
		tableLayout.addColumnData(new ColumnPixelData(200));

//		col3.setEditingSupport(new StructureEditingSupport(viewer));

		viewer.setContentProvider(new MoleculeListContentProvider());
		viewer.setLabelProvider(new MoleculeListLabelProviderNew());
		viewer.setUseHashlookup(true);
		OwnerDrawLabelProvider.setUpOwnerDraw(viewer);
		
		StructureTableEntry[] mlist=((SDFEditor)getEditor()).getEntries();
		if (mlist!=null){
			logger.debug("Setting table input with: " + mlist.length + " molecules.");
			viewer.setInput(mlist);
		}
		else{
			logger.debug("Editor moleculeList is empty.");
		}
		
	}

}
