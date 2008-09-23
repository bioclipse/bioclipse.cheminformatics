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
import net.bioclipse.cdk.jchempaint.widgets.JChemPaintEditorWidget;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.openscience.cdk.controller.Controller2DHub;
import org.openscience.cdk.controller.IController2DModel;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectChangeEvent;
import org.openscience.cdk.interfaces.IChemObjectListener;

public class JChemPaintEditor extends EditorPart{
    boolean dirty=false;
	ICDKMolecule model;
	JChemPaintEditorWidget widget;
	Controller2DHub hub;
	IController2DModel c2dm;
	SWTMosueEventRelay relay;
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
        ICDKMolecule cModel = (ICDKMolecule)input.getAdapter( ICDKMolecule.class );
        if(cModel == null){
            IFile file = (IFile) input.getAdapter(IFile.class);
            if(file != null)
		            cModel=(ICDKMolecule)  file.getAdapter(ICDKMolecule.class);
        }
		if(cModel != null ){
		  
		
		
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
		}
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
	    //  create widget
		widget=new JChemPaintEditorWidget(parent,SWT.NONE);
		IAtomContainer atomContainer=null;
		if(model!=null)
		    atomContainer=model.getAtomContainer();
		 
		// setup hub 
		if(atomContainer != null )
		   widget.setInput( atomContainer );
			
//			widget.getRendererModel().setHighlightRadiusModel(20);
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

    public void setInput( Object element ) {
        
        if(element instanceof IAdaptable) {
            ICDKMolecule molecule = (ICDKMolecule)((IAdaptable)element)
                                              .getAdapter( ICDKMolecule.class );
            if(molecule != null && molecule.getAtomContainer() != null) {
                // TODO : if null change input to what?
                widget.setInput(molecule.getAtomContainer());
                
                // FIXME : update / change hubs chemmodel
            }
        }
    }
	
}
