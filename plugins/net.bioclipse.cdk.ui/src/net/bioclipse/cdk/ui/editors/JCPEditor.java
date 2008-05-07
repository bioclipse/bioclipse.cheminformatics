/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org�epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     CDK Project Team - initial API and implementation
 *     Ola Spjuth - adaptation for Bioclipse2 resources
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.editors;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.Reader;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.ChemSequence;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.applications.jchempaint.DrawingPanel;
import org.openscience.cdk.applications.jchempaint.JCPControlListener;
import org.openscience.cdk.applications.jchempaint.JCPScrollBar;
import org.openscience.cdk.applications.jchempaint.JChemPaintModel;
import org.openscience.cdk.controller.Controller2DModel;
import org.openscience.cdk.event.ICDKChangeListener;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObjectChangeEvent;
import org.openscience.cdk.interfaces.IChemObjectListener;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.listener.SwingGUIListener;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

public class JCPEditor extends EditorPart implements ICDKChangeListener, IChemObjectListener, MouseMotionListener/*, ICDKEditBus*/, IJCPEditorPart {

	private Composite jcpComposite;
	private IChemModel model;
	private DrawingPanel drawingPanel;
	private JChemPaintModel jcpModel;
	private IEditorInput editorInput;
	private JCPScrollBar jcpScrollBar;
	private boolean isDirty = false;
	public ControlListener cl;
	
	private static final Logger logger = Logger.getLogger(JCPEditor.class);
    //TODO remove
	/*private static final Logger logger = Activator.getLogManager()
	.getLogger(JCPEditor.class.toString());*/

	public JCPEditor() {
		super();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
	throws PartInitException{
		super.setSite(site);
		super.setInput(input);
		this.editorInput = input;

//		if (input instanceof BioResourceEditorInput) {
//			BioResourceEditorInput bioInput = (BioResourceEditorInput) input;
//			IBioResource res=bioInput.getBioResource();
//			((BioResource) res).addBioResourceChangeListener(this);
//		}


	}

	@Override
	public void createPartControl(Composite parent) {
		jcpComposite = new JCPComposite(parent, SWT.EMBEDDED | SWT.H_SCROLL | SWT.V_SCROLL);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(jcpComposite, "net.bioclipse.plugins.bc_jchempaint.JCPEditor");
		GridLayout layout = new GridLayout();
		jcpComposite.setLayout(layout);
		if (fillWithJCP(jcpComposite)) {
			cl = new JCPControlListener(this);
			jcpComposite.addControlListener(cl);
			jcpScrollBar = new JCPScrollBar(this, true, true);
		}
		else {
			//TODO open message box stating "no valid file - could not be opnened with JCP"
		}
		jcpComposite.addFocusListener(new JCPCompFocusListener((JCPComposite) jcpComposite));
	}

	public void mouseMoved(MouseEvent e){
	}

	public void mouseDragged(MouseEvent e){
		drawingPanel.repaint();
	}


	public boolean updateModel(){
//		boolean sucess=((BioResourceEditorInput)this.editorInput).getBioResource().parseResource();
		boolean sucess=false;
		if(!sucess)
			return false;
		model=this.getModelFromEditorInput();
		if (model != null) {
			model.addListener(this);
			jcpModel.setChemModel(model);
			jcpModel.fireChange();
			drawingPanel.repaint();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Adds the the drawingPanel to the jcpComposite and fills jcp with model
	 * @param composite
	 * @return 
	 * @throws JCPException 
	 */
	private boolean fillWithJCP(Composite composite) {
		//Get model from the editorInput that is the parsed resourceString
		model = getModelFromEditorInput();
		if (model != null) {
			model.addListener(this);
			drawingPanel = new DrawingPanel(composite.getDisplay());
			jcpModel = new JChemPaintModel(model);
			jcpModel.getControllerModel().setAutoUpdateImplicitHydrogens(true);
			jcpModel.getRendererModel().setShowEndCarbons(true);
			CDKHydrogenAdder hydrogenAdder = CDKHydrogenAdder.getInstance(jcpModel.getChemModel().getBuilder());

			List acS = ChemModelManipulator.getAllAtomContainers(model);
			Iterator molsI = acS.iterator();
			while(molsI.hasNext()){
				IMolecule molecule = (IMolecule)molsI.next();
				if (molecule != null)
				{
					try{
						hydrogenAdder.addImplicitHydrogens(molecule);
						AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
					}catch(Exception ex){
						//do nothing
					}
				}
			}

			jcpModel.getRendererModel().addCDKChangeListener(this);
			jcpModel.getControllerModel().setDrawMode(Controller2DModel.DrawMode.LASSO);
			drawingPanel.setJChemPaintModel(jcpModel);
			drawingPanel.addMouseMotionListener(this);
			java.awt.Frame jcpFrame = SWT_AWT.new_Frame(composite);
			jcpFrame.add(drawingPanel);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Get the IChemModel from the parsedResource
	 * @return
	 */
	private IChemModel getModelFromEditorInput(){
//		Object parsedRes = ((BioResource)((BioResourceEditorInput)this.editorInput).getBioResource()).getParsedResource();
		Object parsedRes=null; //Workaround
		
		if (parsedRes instanceof IChemModel) {
			model = (IChemModel) parsedRes;
			return model;
		}
		else if (parsedRes instanceof IChemFile) {
			model = ((IChemFile)parsedRes).getChemSequence(0).getChemModel(0);
			return model;
		}
		else {
			return null;
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
//		BioResourceEditorInput brinp= (BioResourceEditorInput) editorInput;

		//get the actual model from jcp and create a ChemFile of it
		IChemModel chemModel = getJCPModel().getChemModel();
		ChemSequence seq = new ChemSequence();
		seq.addChemModel(chemModel);
		ChemFile chemFile = new ChemFile();
		chemFile.addChemSequence(seq);
//		//set the BioResouces parsedRes object to this ChemFile
//		brinp.getBioResource().setParsedResource(chemFile);
//		//then call save() of the BioResource 
//		brinp.getBioResource().save();
		this.setDirty(false);
	}

	private void fireSetDirtyChanged() {
		Runnable r= new Runnable() {
			public void run() {
				firePropertyChange(PROP_DIRTY);
			}
		};
		Display fDisplay = getSite().getShell().getDisplay();
		fDisplay.asyncExec(r);

	}

	@Override
	public void doSaveAs() {
//		IBioResource resource=((BioResourceEditorInput)getEditorInput()).getBioResource();
		IChemFile content=getChemFile();

		//Delegate to EditorUtils for the doSaveAs
//		boolean ret=EditorUtils.doSaveAs(resource, content);
	}

	@Override
	public void setFocus() {
		this.jcpComposite.setFocus();

	}

	@Override
	public boolean isDirty() {
		return this.isDirty ;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	public JChemPaintModel getJCPModel() {
		return this.jcpModel;
	}

	public DrawingPanel getDrawingPanel() {
		return this.drawingPanel;
	}

	public Composite getJcpComposite() {
		return jcpComposite;
	}

	public JCPScrollBar getJcpScrollBar() {
		return jcpScrollBar;
	}

	public void stateChanged(EventObject e) {
		if(e.getSource() instanceof Renderer2DModel) {
			getDrawingPanel().repaint();
			if (!this.isDirty() && jcpModel.isModified()) {
				setDirty(true);
			}
		}
	}

	public void stateChanged(IChemObjectChangeEvent chemObjectEvent) {
		if (!this.isDirty() && jcpModel.isModified()) {
			setDirty(true);
		}
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
		fireSetDirtyChanged();
	}


	/**
	 *  Gets the aPIVersion attribute of the JChemPaintPanel object
	 *
	 *@return    The aPIVersion value
	 */
	public String getAPIVersion() {
		return "1.11";
	}


	/**
	 *  Description of the Method
	 *
	 *@param  mimeType  Description of the Parameter
	 *@param  script    Description of the Parameter
	 */
	public void runScript(String mimeType, String script) {
		logger.error("JChemPaintPanel's CDKEditBus.runScript() implementation called but not implemented!");
	}

	/**
	 *  Description of the Method
	 *
	 *@param  chemFile  Description of the Parameter
	 */
	public void showChemFile(org.openscience.cdk.interfaces.IChemFile chemFile) {

		int chemSequenceCount = chemFile.getChemSequenceCount();

		for (int i = 0; i < chemSequenceCount; i++) {
			org.openscience.cdk.interfaces.IChemSequence chemSequence = chemFile.getChemSequence(i);

			int chemModelCount = chemSequence.getChemModelCount();

			for (int j = 0; j < chemModelCount; j++) {
				org.openscience.cdk.interfaces.IChemModel chemModel = chemSequence.getChemModel(j);
				showChemModel(chemModel);
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  chemModel  Description of the Parameter
	 */
	public void showChemModel(org.openscience.cdk.interfaces.IChemModel chemModel) {
		// check for bonds
		if (chemModel.getMoleculeSet().getAtomContainer(0).getBondCount() == 0) {
			String error = "Model does not have bonds. Cannot depict contents.";
//			BioclipseConsole.writeToConsole("error: " +  error);
			return;
		}

		// check for coordinates
		if (!(GeometryTools.has2DCoordinatesNew(chemModel.getMoleculeSet().getAtomContainer(0))==0)) {

			String error = "Model does not have coordinates. Will ask for coord generation.";

			boolean generate2DCoords = MessageDialog.openQuestion(new Shell(), "Generate 2D Coordinates?", "This file does not contain 2D coordinates. Should they be calculated?");
			if (generate2DCoords) {
				StructureDiagramGenerator str = new StructureDiagramGenerator();
				str.setMolecule(new Molecule(chemModel.getMoleculeSet().getAtomContainer(0)));
				try {
					str.generateCoordinates();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				return;
			}
		} else if ((GeometryTools.has2DCoordinatesNew(chemModel.getMoleculeSet().getAtomContainer(0))==0)) {
			boolean show=MessageDialog.openConfirm(new Shell(),"Only some coordinates","Model has some 2d coordinates. Do you want to show only the atoms with 2d coordiantes?");
			if(!show){
				boolean generate2DCoords = MessageDialog.openQuestion(new Shell(), "Generate 2D Coordinates?", "This file does not contain 2D coordinates. Should they be calculated?");
				if (generate2DCoords) {
					StructureDiagramGenerator str = new StructureDiagramGenerator();
					str.setMolecule(new Molecule(chemModel.getMoleculeSet().getAtomContainer(0)));
					try {
						str.generateCoordinates();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					return;
				}
			}else{
				for(int i=0;i<chemModel.getMoleculeSet().getAtomContainerCount();i++){
					for(int k=0;i<chemModel.getMoleculeSet().getAtomContainer(i).getAtomCount();k++){
						if(chemModel.getMoleculeSet().getAtomContainer(i).getAtom(k).getPoint2d()==null)
							chemModel.getMoleculeSet().getAtomContainer(i).removeAtomAndConnectedElectronContainers(chemModel.getMoleculeSet().getAtomContainer(i).getAtom(k));
					}						
				}
			}
		}

		this.getJCPModel().setChemModel(chemModel);
	}


	/**
	 *  Gets the chemModel attribute of the JChemPaint object. This method
	 *  implements part of the CDKEditBus interface.
	 *
	 *@return    The chemModel value
	 */
	public org.openscience.cdk.interfaces.IChemModel getChemModel() {
		return this.model;
	}


	/**
	 *  Gets the chemFile attribute of the JChemPaint object. This method
	 *  implements part of the CDKEditBus interface.
	 *
	 *@return    The chemFile value
	 */
	public org.openscience.cdk.interfaces.IChemFile getChemFile() {
		ChemFile file = new ChemFile();
		ChemSequence sequence = new ChemSequence();
		sequence.addChemModel(getChemModel());
		file.addChemSequence(sequence);
		return file;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  file  Description of the Parameter
	 */
	public void showChemFile(Reader file) {
		IChemObjectReader cor = null;

		/*
		 *  Have the ReaderFactory determine the file format
		 */
		try {
			cor = getChemObjectReader(file);
		} catch (IOException ioExc) {
			logger.warn("IOException while determining file format.");
			logger.debug(ioExc);
		} catch (Exception exc) {
			logger.warn("Exception while determining file format.");
			logger.debug(exc);
		}

		if (cor == null) {
			MessageDialog.openError(new Shell(), "Could not determine file format", "Could not determine file format");
			return;
		}

		String error = null;
		ChemFile chemFile = null;
		ChemModel chemModel = null;
		if (cor.accepts(ChemFile.class)) {
			// try to read a ChemFile
			try {
				chemFile = (ChemFile) cor.read(new ChemFile());
				if (chemFile != null) {
					processChemFile(chemFile);
					return;
				} else {
					logger.warn("The object chemFile was empty unexpectedly!");
				}
			} catch (Exception exception) {
				error = "Error while reading file: " + exception.getMessage();
				logger.warn(error);
				logger.debug(exception);
			}
		}
		if (error != null) {
			MessageDialog.openError(new Shell(), error, error);
			return;
		}
		if (cor.accepts(ChemModel.class)) {
			// try to read a ChemModel
			try {
				chemModel = (ChemModel) cor.read((ChemObject) new ChemModel());
				if (chemModel != null) {
					processChemModel(chemModel);
					return;
				} else {
					logger.warn("The object chemModel was empty unexpectedly!");
				}
				error = null;
				// overwrite previous problems, it worked now
			} catch (Exception exception) {
				error = "Error while reading file: " + exception.getMessage();
				logger.error(error);
				logger.debug(exception);
			}
		}
		if (error != null) {
			MessageDialog.openError(new Shell(), error, error);
		}
	}

	/**
	 *  Gets the chemObjectReader attribute of the JChemPaintPanel object
	 *
	 *@param  reader           Description of the Parameter
	 *@return                  The chemObjectReader value
	 *@exception  IOException  Description of the Exception
	 */
	public IChemObjectReader getChemObjectReader(Reader reader) throws IOException {
		ReaderFactory factory = new ReaderFactory();
		IChemObjectReader coReader = factory.createReader(reader);
		if (coReader != null) {
			coReader.addChemObjectIOListener(new SwingGUIListener(new JFrame(), 4));
		}
		return coReader;
	}

	/**
	 *  Description of the Method
	 *
	 *@param  chemFile  Description of the Parameter
	 */
	public void processChemFile(org.openscience.cdk.interfaces.IChemFile chemFile) {
		logger.info("Information read from file:");

		int chemSequenceCount = chemFile.getChemSequenceCount();
		logger.info("  # sequences: "+ chemSequenceCount);

		for (int i = 0; i < chemSequenceCount; i++) {
			org.openscience.cdk.interfaces.IChemSequence chemSequence = chemFile.getChemSequence(i);

			int chemModelCount = chemSequence.getChemModelCount();
			logger.info("  # model in seq(" + i + "): "+ chemModelCount);

			for (int j = 0; j < chemModelCount; j++) {
				org.openscience.cdk.interfaces.IChemModel chemModel = chemSequence.getChemModel(j);
				processChemModel(chemModel);
			}
		}
	}

	/**
	 *  Description of the Method
	 *
	 *@param  chemModel  Description of the Parameter
	 */
	public void processChemModel(org.openscience.cdk.interfaces.IChemModel chemModel) {
		// check for bonds
		if (chemModel.getMoleculeSet().getAtomContainer(0).getBondCount() == 0) {
			String error = "Model does not have bonds. Cannot depict contents.";
			logger.warn(error);
			MessageDialog.openError(new Shell(), error, error);
			return;
		}

		// check for coordinates
		if ((GeometryTools.has2DCoordinatesNew(chemModel.getMoleculeSet().getAtomContainer(0))==0)) {
			String error = "Model does not have 2D coordinates. Cannot open file.";
			logger.warn(error);
			MessageDialog.openError(new Shell(), error, error);
			boolean generate2DCoords = MessageDialog.openQuestion(new Shell(), "Generate 2D Coordinates?", "This file does not contain 2D coordinates. Should they be calculated?");
			if (generate2DCoords) {
				StructureDiagramGenerator str = new StructureDiagramGenerator();
				str.setMolecule(new Molecule(chemModel.getMoleculeSet().getAtomContainer(0)));
				try {
					str.generateCoordinates();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				return;
			}
		} else if ((GeometryTools.has2DCoordinatesNew(chemModel.getMoleculeSet().getAtomContainer(0))==0)) {
			boolean show=MessageDialog.openConfirm(new Shell(),"Only some coordinates","Model has some 2d coordinates. Do you want to show only the atoms with 2d coordiantes?");
			if(!show){
				boolean generate2DCoords = MessageDialog.openQuestion(new Shell(), "Generate 2D Coordinates?", "This file does not contain 2D coordinates. Should they be calculated?");
				if (generate2DCoords) {
					StructureDiagramGenerator str = new StructureDiagramGenerator();
					str.setMolecule(new Molecule(chemModel.getMoleculeSet().getAtomContainer(0)));
					try {
						str.generateCoordinates();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					return;
				}
			}else{
				for(int i=0;i<chemModel.getMoleculeSet().getAtomContainerCount();i++){
					for(int k=0;i<chemModel.getMoleculeSet().getAtomContainer(i).getAtomCount();k++){
						if(chemModel.getMoleculeSet().getAtomContainer(i).getAtom(k).getPoint2d()==null)
							chemModel.getMoleculeSet().getAtomContainer(i).removeAtomAndConnectedElectronContainers(chemModel.getMoleculeSet().getAtomContainer(i).getAtom(k));
					}						
				}
			}
		}

		this.getJCPModel().setChemModel(chemModel);

	}

}
