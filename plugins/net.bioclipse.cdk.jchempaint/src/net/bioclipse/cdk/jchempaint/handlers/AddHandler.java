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

//import net.bioclipse.cdk.jchempaint.business.Activator;
//import net.bioclipse.cdk.jchempaint.business.IJChemPaintManager;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.interfaces.IAtom;

public class AddHandler extends AbstractHandler implements IHandler {

    Logger logger = Logger.getLogger(AddHandler.class);
    public Object execute(ExecutionEvent event) throws ExecutionException {
//       IJChemPaintManager jcp= Activator.getDefault().getExampleManager();
       Object object = event.getApplicationContext();

       ISelection selection = HandlerUtil.getCurrentSelection( event );
       if(selection instanceof IStructuredSelection ) {
           Object element = ((IStructuredSelection)selection).getFirstElement();
           if(element instanceof IAtom) {
//               jcp.addAtom( "C", (IAtom) element);
           }

       }
       return null;
    }

    protected IAtom getSingleSelectedAtom(ISelection selection) {

        if(selection instanceof IStructuredSelection ) {
            Object element = ((IStructuredSelection)selection).getFirstElement();
            if(element instanceof IAtom) {
                return (IAtom)element;
            }
        }
        return null;
    }
}
