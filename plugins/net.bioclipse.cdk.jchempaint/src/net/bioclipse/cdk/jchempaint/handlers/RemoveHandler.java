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

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;

public class RemoveHandler extends AbstractJChemPaintHandler {
    Logger logger = Logger.getLogger(RemoveHandler.class);
    public Object execute(ExecutionEvent event) throws ExecutionException {
        
        IChemModelRelay relay = getChemModelRelay( event );
        if ( relay != null ) {
            IAtom atom = getSingleSelectedAtom( event );
            IBond bond = getSingleSelectedBond( event );
            if ( atom != null ) {                
                try {
                    getManager().removeAtom( atom );
                } catch ( BioclipseException e ) {
                    LogUtils.debugTrace( logger, e );
                }
            }
        }
        return null;
    }
}
