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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class StructureTablePage extends FormPage implements ISelectionProvider{

    private static final Logger logger = Logger.getLogger(StructureTablePage.class);

    private Table table;
	private TableViewer viewer;
	private String[] colHeaders;
	
	/** Registered listeners */
	private List<ISelectionChangedListener> selectionListeners;

	/** Store last selection */
	private StructureEntitySelection selectedRows;

	
	public StructureTablePage(FormEditor editor, String[] colHeaders) {
		super(editor, "bc.structuretable", "Structure table");
		this.colHeaders=colHeaders;
		selectionListeners=new ArrayList<ISelectionChangedListener>();
		
//		selectedRows=new StructureEntitySelection();
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
		
		viewer = new TableViewer(body, SWT.BORDER  | SWT.MULTI |  SWT.FULL_SELECTION | SWT.VIRTUAL);
		table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		toolkit.adapt(table, true, true);

		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		
		//Add Structure column
		TableViewerColumn col=new TableViewerColumn(viewer,SWT.BORDER);
		col.getColumn().setText("Structure");
		tableLayout.addColumnData(new ColumnPixelData(100));
		
		for (String colkey : colHeaders){
			TableViewerColumn col2=new TableViewerColumn(viewer,SWT.BORDER);
			col2.getColumn().setText(colkey);
			col2.getColumn().setAlignment(SWT.LEFT);
			tableLayout.addColumnData(new ColumnPixelData(100));
		}
		
//		table.addSelectionListener(new SelectionListener(){
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//
//			public void widgetSelected(SelectionEvent e) {
//				
//				if (e.item instanceof TableItem) {
//					TableItem item = (TableItem)e.item;
//					if (item.getData() instanceof StructureTableEntry) {
//						StructureTableEntry entry = (StructureTableEntry) item.getData();
//						Set<StructureTableEntry> newsel=new HashSet<StructureTableEntry>();
//						newsel.add(entry);
//						setSelection(new StructureEntitySelection(newsel));
//					}
//				}
//			}
//			
//		});
//		
//		viewer.addSelectionChangedListener(new ISelectionChangedListener(){
//
//			public void selectionChanged(SelectionChangedEvent event) {
//				if (event.getSelection() instanceof IStructuredSelection) {
//					IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
//					Set<StructureTableEntry> newsel=new HashSet<StructureTableEntry>();
//					for (Object obj : ssel.toArray()){
//						if (obj instanceof StructureTableEntry) {
//							StructureTableEntry entry = (StructureTableEntry) obj;
//							newsel.add(entry);
//						}
//					}
//					setSelection(new StructureEntitySelection(newsel));
//				}
//			}
//		});

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
		
		//Post selections in Table to Eclipse
		getSite().setSelectionProvider(viewer);

	}
	
	/*
	 * Below is for providing selections from table to e.g. Jmol view
	 */

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if(!selectionListeners.contains(listener))
		{
			selectionListeners.add(listener);
		}
	}

	public ISelection getSelection() {

		TableItem[] itm=(TableItem[])viewer.getTable().getSelection();
		if (itm==null || itm.length<=0) return null;

		//Hold new selection
		Set<StructureTableEntry> newSet=new HashSet<StructureTableEntry>();

		//Recurse
		for(TableItem item : itm){
			if (item.getData() instanceof StructureTableEntry) {
				StructureTableEntry entry = (StructureTableEntry) item.getData();
				System.out.println("** Added selected in structtab: " + entry.getMoleculeImpl().hashCode());
				newSet.add(entry);
			}
		}

		selectedRows=new StructureEntitySelection(newSet);
		return selectedRows;
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		if(selectionListeners.contains(listener))
			selectionListeners.remove(listener);
	}

	public void setSelection(ISelection selection) {
		if (!(selection instanceof StructureEntitySelection)) return;
		this.selectedRows=(StructureEntitySelection)selection;
	}
	
}
