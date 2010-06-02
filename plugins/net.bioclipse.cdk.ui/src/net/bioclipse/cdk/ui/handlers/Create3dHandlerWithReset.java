/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * Stefan Kuhn
 ******************************************************************************/
package net.bioclipse.cdk.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * A handler class for a Generate 3D Coordinates menu item
 */
public class Create3dHandlerWithReset extends AbstractHandler {

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        ISelection sel = HandlerUtil.getCurrentSelection(event);
        if(sel instanceof IStructuredSelection && !((IStructuredSelection)sel).isEmpty()) {
            
            Create2dHandlerWithReset.doCreation( HandlerUtil.getActiveShell(event),
                        (IStructuredSelection)sel,
                        true,
                        Coordiantes.Coord_3D);
        }
        return null;
    }

}
