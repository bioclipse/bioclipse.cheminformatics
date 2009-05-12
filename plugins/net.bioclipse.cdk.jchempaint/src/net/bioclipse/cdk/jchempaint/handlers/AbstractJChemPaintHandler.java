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
package net.bioclipse.cdk.jchempaint.handlers;

import java.util.Collection;
import java.util.List;

import net.bioclipse.cdk.jchempaint.Activator;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintManager;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;

public abstract class AbstractJChemPaintHandler extends AbstractHandler {

    public AbstractJChemPaintHandler() {

        super();
    }

    protected IJChemPaintManager getManager() {
        return Activator.getDefault().getExampleManager();
    }
    
    protected JChemPaintEditor getEditor(ExecutionEvent event) {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);

        return (JChemPaintEditor)editor.getAdapter( JChemPaintEditor.class );
    }
    
    protected IAtom getSingleSelectedAtom( ExecutionEvent event ) {
        ISelection selection = HandlerUtil.getCurrentSelection( event );
        if(selection instanceof IStructuredSelection ) {
            //Object element = ((IStructuredSelection)selection).toArray()[1];
            Object element = ((IStructuredSelection)selection).getFirstElement();
            IAtom atom = null;
            if(element instanceof IAdaptable)
                atom = (IAtom) ((IAdaptable)element).getAdapter( IAtom.class );
            return atom;
        }
        return null;
    }

    protected IBond getSingleSelectedBond( ExecutionEvent event ) {
        ISelection selection = HandlerUtil.getCurrentSelection( event );
        if(selection instanceof IStructuredSelection ) {
            //Object element = ((IStructuredSelection)selection).toArray()[1];
            Object element = ((IStructuredSelection)selection).getFirstElement();
            IBond bond = null;
            if(element instanceof IAdaptable)
                bond = (IBond) ((IAdaptable)element).getAdapter( IBond.class );
            return bond;

        }
        return null;
    }
    
    protected Collection<?> getSelection(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection( event );
        if(selection instanceof IStructuredSelection) {
            List<?> elements = ((IStructuredSelection)selection).toList();
            return elements; 
        }
        return null;
    }
    
    protected IChemModelRelay getChemModelRelay( ExecutionEvent event ) {

        return getControllerHub( event );
    }
    
    protected ControllerHub getControllerHub(ExecutionEvent event) {
        JChemPaintEditor editor = getEditor( event );
        if(editor !=null)
            return editor.getControllerHub();
        return null;
    }
}