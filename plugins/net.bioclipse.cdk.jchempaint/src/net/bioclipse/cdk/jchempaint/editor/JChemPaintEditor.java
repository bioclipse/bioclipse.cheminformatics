/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Arvid Berg
 *     
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.editor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.widgets.JChemPaintSWTWidget;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.openscience.cdk.controller.Controller2DHub;
import org.openscience.cdk.controller.Controller2DModel;
import org.openscience.cdk.controller.IController2DModel;
import org.openscience.cdk.controller.IViewEventRelay;
import org.openscience.cdk.controller.IController2DModel.DrawMode;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectChangeEvent;
import org.openscience.cdk.interfaces.IChemObjectListener;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

public class JChemPaintEditor extends EditorPart{
    boolean dirty=false;
	ICDKMolecule model;
	JChemPaintSWTWidget widget;
	Controller2DHub hub;
	IController2DModel c2dm;
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
	    
	    setSite(site);
        setInput(input);    
        IFile file = (IFile) input.getAdapter(IFile.class);
		ICDKMolecule cModel=(ICDKMolecule)  file.getAdapter(ICDKMolecule.class);
		if(cModel==null){
//		    site.getPage().closeEditor(this,false);
		    return;
		}
		
		setPartName(input.getName());
		model=cModel;
		model.getAtomContainer().addListener(new IChemObjectListener(){
		   public void stateChanged(IChemObjectChangeEvent event) {
		       if(!isDirty()){
		           dirty=true;		           
		           firePropertyChange(IEditorPart.PROP_DIRTY);
		       }		        
		    } 
		});
//		widget.setAtomContainer(model.getMoleculeSet().getAtomContainer(0));
	}

	@Override
	public boolean isDirty() {		
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		widget=new JChemPaintSWTWidget(parent,SWT.NONE);
		IAtomContainer atomContainer=null;
		if(model!=null)
		    widget.setAtomContainer(atomContainer=model.getAtomContainer());
		   

		IViewEventRelay eventRelay=new IViewEventRelay(){
			public void updateView() {
				widget.redraw();
			}
		};		
		
		hub = new Controller2DHub(
				c2dm=new Controller2DModel(), widget.getRenderer(),
				ChemModelManipulator.newChemModel(atomContainer),
				eventRelay
			);
//			hub.registerGeneralControllerModule(new ExampleController2DModule());
//			hub.registerGeneralControllerModule(new Controller2DModuleMove());
//			hub.registerGeneralControllerModule(new Controller2DModuleHighlight());
//			 hub.registerGeneralControllerModule(new Controller2DModuleRemove());
			SWTMosueEventRelay	 relay = new SWTMosueEventRelay(hub);
			c2dm.setDrawMode(DrawMode.MOVE);
			
			widget.addMouseListener(relay);
			widget.addMouseMoveListener(relay);
			widget.addListener(SWT.MouseEnter, relay);
			widget.addListener(SWT.MouseExit, relay);
			
//			widget.getRendererModel().setBackColor(Color.cyan);
			widget.getRendererModel().setHighlightRadiusModel(20);
//			widget.getRendererModel().set
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

    public Controller2DHub getControllerHub() {
        return hub;
    }

    public IController2DModel getControllerModel() {
        return c2dm;
    }
	
}
