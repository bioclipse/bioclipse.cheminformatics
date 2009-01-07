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

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.interfaces.IAtom;

public class SelectHandler extends AbstractJChemPaintHandler {

    Logger logger = Logger.getLogger(RemoveHandler.class);
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IChemModelRelay relay = getChemModelRelay( event );
        if ( relay != null ) {
            IAtom selected = getSingleSelectedAtom( event );
            if ( selected != null ) {
                throw new UnsupportedOperationException("Selecte handler not implemented");
            }
        }
        return null;
    }
}
