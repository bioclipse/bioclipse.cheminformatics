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

import java.lang.reflect.Method;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.outline.JCPOutlinePage;
import net.bioclipse.cdk.jchempaint.widgets.JChemPaintEditorWidget;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.IControllerModel;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectChangeEvent;
import org.openscience.cdk.interfaces.IChemObjectListener;

public class JChemPaintEditor extends EditorPart{

    Logger logger = Logger.getLogger( JChemPaintEditor.class );

    private JCPOutlinePage fOutlinePage;

    boolean dirty=false;
    ICDKMolecule model;
    JChemPaintEditorWidget widget;
    IControllerModel c2dm;
    SWTMouseEventRelay relay;
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
		           Display.getDefault().syncExec( new Runnable() {
		               public void run() {
		                   firePropertyChange(IEditorPart.PROP_DIRTY);
		               }		               
		           });
		           
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
		
		
		MenuManager menuMgr = new MenuManager();
	  menuMgr.add( new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
	  getSite().registerContextMenu( "net.bioclipse.cdk.ui.editors.jchempaint.menu",
	                                 menuMgr, widget);
	    
	  //Control control = lViewer.getControl();
	  Menu menu = menuMgr.createContextMenu(widget);
	  widget.setMenu(menu);    


		// setup hub 
		getSite().setSelectionProvider( widget );
		widget.setAtomContainer( atomContainer );
			
	}

    @Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

    public ControllerHub getControllerHub() {
        return widget.getControllerHub();
    }

    public IControllerModel getControllerModel() {
        return c2dm;
    }

    public void update() {
        widget.redraw();
    }

    public void setInput( Object element ) {
        widget.setInput( element );
        widget.redraw();
    }
	
    public ICDKMolecule getCDKMolecule() {
        return model;
    }

    public Object getAdapter(Class adapter) {
        if (IContentOutlinePage.class.equals(adapter)) {
            if (fOutlinePage == null) {
                fOutlinePage= new JCPOutlinePage(getEditorInput(), this);
            }
            return fOutlinePage;
        }
        return super.getAdapter(adapter);
    }

    protected MenuItem createMenuItem( Menu parent, int style, String text, 
                                       Image icon, int accel, boolean enabled, 
                                       String callback) {
        MenuItem mi = new MenuItem(parent, style);
        if (text != null) {
            mi.setText(text);
        }
        if (icon != null) {
            mi.setImage(icon);
        }
        if (accel != -1) {
            mi.setAccelerator(accel);
        }
        mi.setEnabled(enabled);
        if (callback != null) {
            registerCallback(mi, this, callback);
        }
        return mi;
    }

    protected void registerCallback(final MenuItem mi, 
                                    final Object handler, 
                                    final String handlerName) {
        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    Method m = handler.getClass().getMethod(handlerName, null);
                    m.invoke(handler, null);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    public void doAddAtom() {
        
        logger.debug( "Executing 'Add atom' action" );        
    }
    public void doChageAtom() {
        logger.debug( "Executing 'Chage atom' action" );
    }
}
