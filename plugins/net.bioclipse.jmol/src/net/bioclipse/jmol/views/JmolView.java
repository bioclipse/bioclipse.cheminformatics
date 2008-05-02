/*******************************************************************************
 * Copyright (c) 2007 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/

package net.bioclipse.jmol.views;

import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;

import net.bioclipse.core.business.ChemicalStructureProvider;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.openscience.cdk.BioPolymer;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * A view for Jmol embedded in an SWT_AWT frame (requires java5.0+)
 * 
 * @author ola
 */
public class JmolView extends ViewPart implements ISelectionListener, ISelectionProvider, ISelectionChangedListener{

	//Use logging
    private static final Logger logger = Logger.getLogger(JmolView.class);

	public static final String ID = "net.bioclipse.jmol.views.JmolView";

	private JmolPanel jmolPanel;
	private Text text;
	private ArrayList<String> history;

	private Composite composite;

	//Provide selections from Jmol to e.g. outline
	private List<ISelectionChangedListener> selectionListeners;
	private JmolSelection selection;


	/**
	 * The constructor.
	 */
	public JmolView() {
		history = new ArrayList<String>(50);
		jmolPanel= new JmolPanel(this);
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		logger.debug("JmolView is initiating...");

		//Set the layout for parent
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		parent.setLayout(layout);

		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace=true;
		parent.setLayoutData(layoutData);

		//Add the Jmol composite to the top
		composite = new Composite(parent, SWT.EMBEDDED);
		layout = new GridLayout();
		composite.setLayout(layout);
		layoutData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(layoutData);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "net.bioclipse.jmol.scriptInputField");	


		java.awt.Frame fileTableFrame = SWT_AWT.new_Frame(composite);
		java.awt.Panel panel = new java.awt.Panel(new java.awt.BorderLayout());
		fileTableFrame.add(panel);

		JScrollPane scrollPane = new JScrollPane(jmolPanel);
		panel.add(scrollPane);

		jmolPanel.addMouseListener((MouseListener) new JmolCompMouseListener(composite));

		Label label1 = new Label(parent, SWT.NONE);
		label1.setText("Jmol scripting console");

		//Layout the text field below Jmol
		text = new Text(parent, SWT.SINGLE | SWT.BORDER);

		GridData layoutData2 = new GridData();
		layoutData2.grabExcessHorizontalSpace = true;
		layoutData2.horizontalAlignment = GridData.FILL;
		text.setLayoutData(layoutData2);
		text.setTextLimit(60);
		text.setEditable(true);
		text.setEnabled(false);
		text.setText("");

		text.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if (e.stateMask != SWT.CTRL && e.keyCode == SWT.CR){
					//Execute jmol command
					String jmolcmd=text.getText();
					if (jmolcmd!=null && jmolcmd.length()>0)
//						executeJmolCommand(jmolcmd);
					if (!history.contains(text.getText()))
						history.add(0, text.getText());
					text.setText("");
				}
				/*laszlo: 
				 * Store a history of typed commands in Jmol script console,
				 * toggle UP/DOWN to see previous script.
				 * */

				if (e.keyCode == SWT.ARROW_UP){
					text.setText(history.get(0));
					history.add(history.get(0));
					history.remove(0);
				}

				if (e.keyCode == SWT.ARROW_DOWN){
					text.setText(history.get(0));
					history.add(0, history.get(history.size()-1));
					history.remove(history.size()-1);
				}
			}
		});

		//Register this page as a listener for selections
		//We want to update information based on selection i e g TreeViewer
		getViewSite().getPage().addSelectionListener(this);

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub

		reactOnSelection(selection);
	}

	
	/* Below are for setting selections in Bioclipse from Jmol, e.g when 
	 clicked on an Atom*/

	
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if(!selectionListeners.contains(listener))
		{
			selectionListeners.add(listener);
		}
	}

	public ISelection getSelection() {
		return selection;
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		if(selectionListeners.contains(listener))
			selectionListeners.remove(listener);
	}

	public void setSelection(ISelection selection) {
		this.selection = (JmolSelection)selection;
		java.util.Iterator<ISelectionChangedListener> iter = selectionListeners.iterator();
		while( iter.hasNext() )
		{
			final ISelectionChangedListener listener = iter.next();
			final SelectionChangedEvent e = new SelectionChangedEvent(this, this.selection);
			//Does SWT stuff so this has to be called on SWT's thread
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					listener.selectionChanged(e);
				}
			});
			
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		reactOnSelection(event.getSelection());
	}

	
	/**
	 * 
	 * @param selection
	 */
	private void reactOnSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structsel = (IStructuredSelection) selection;
			for (Object obj : structsel.toList()){
				
				//TODO: Handle multile selections!
				
				if (obj instanceof ChemicalStructureProvider) {
					ChemicalStructureProvider struct = (ChemicalStructureProvider) obj;
					Object obj2=struct.getMoleculeImpl();
					if (obj2 instanceof IAtomContainer) {
						IAtomContainer newAC = (IAtomContainer) obj2;
						
						//Set new mol in Jmol
						setMolecule(newAC);
						
					}
				}
				else{
				}
			}
		}
		
		
	}

	private void setMolecule(IAtomContainer ac) {

			//Check if 3D coordinates exist
			if (GeometryTools.has3DCoordinates(ac)){
				logger.debug("Opening protein via CDK's ChemFile and CdkJmolAdapter...");

				//Maybe fork off a new thread?? TODO!
				jmolPanel.openClientFile("", "", ac);

				//TODO: fix for Biopolymers
				if (ac instanceof BioPolymer) {
					logger.debug("Biopol identified.");
//					executeSilentJmolCommand("cartoon on; wireframe on; color cartoon group");
//				}else executeSilentJmolCommand("cpk 20%");

				text.setEnabled(true);
				text.getParent().redraw();

				String strError = jmolPanel.getOpenFileError();
				if (strError != null){
					logger.error(strError);
					text.setEnabled(false);
				}
				logger.debug("Done viewing...");
			}
			//We have no 3D-coordinates
			else{
//				viewer.openClientFile(null,null,new Molecule());

				//TODO: unload last molecule

				//Now: just hide it
//				executeSilentJmolCommand("set echo middle center; font echo 12 serif ; color echo red; echo \"No 3D coordinates\"");
				text.setEnabled(false);
			}
		}
		//We have no IChemFile
		else{
			jmolPanel.openClientFile(null,null,new Molecule());
			text.setEnabled(false);

		}
	}

}