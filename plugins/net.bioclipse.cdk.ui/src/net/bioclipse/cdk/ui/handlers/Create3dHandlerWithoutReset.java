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

/**
 * A handler class for a Generate 3D Coordinates menu item
 */
public class Create3dHandlerWithoutReset extends AbstractHandler {

    public int                  answer;

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        Create2dHandlerWithReset.doCreation(false,true);
        return null;
    }

}
