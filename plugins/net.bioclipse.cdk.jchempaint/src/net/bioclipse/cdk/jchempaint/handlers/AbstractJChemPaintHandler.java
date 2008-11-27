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

import net.bioclipse.cdk.jchempaint.Activator;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintManager;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
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
    
    protected IAtom getSingleSelectedAtom( ExecutionEvent event ) {
        ISelection selection = HandlerUtil.getCurrentSelection( event );
        if(selection instanceof IStructuredSelection ) {
            Object element = ((IStructuredSelection)selection).getFirstElement();
            if(element instanceof IAtom) {
                return (IAtom)element;
            }
        }
        return null;
    }

    protected IBond getSingleSelectedBond( ExecutionEvent event ) {
        ISelection selection = HandlerUtil.getCurrentSelection( event );
        if(selection instanceof IStructuredSelection ) {
            Object element = ((IStructuredSelection)selection).getFirstElement();
            if(element instanceof IBond) {
                return (IBond)element;
            }
        }
        return null;
    }
    
    protected IChemModelRelay getChemModelRelay( ExecutionEvent event ) {

        IEditorPart editor = HandlerUtil.getActiveEditor( event );

        if ( (editor instanceof JChemPaintEditor) ) {
            return ((JChemPaintEditor) editor).getControllerHub();
        } else {
            return null;
        }
    }
}