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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.interfaces.IAtom;

public class DecchargeHandler extends AbstractJChemPaintHandler {

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        
        IChemModelRelay relay = getChemModelRelay( event );
        if ( relay != null ) {
            IAtom selected = getSingleSelectedAtom( event );
            if ( selected != null ) {
                int newCharge = -1;
                if ( selected.getFormalCharge() != null ) {
                    newCharge = selected.getFormalCharge();
                    newCharge--;
                }
                getManager().setCharge( selected, newCharge );
            }
        }
        return null;
    }
}
