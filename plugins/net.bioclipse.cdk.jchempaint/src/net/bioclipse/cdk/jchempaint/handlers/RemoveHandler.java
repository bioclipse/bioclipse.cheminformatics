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

import net.bioclipse.core.business.BioclipseException;

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
            Collection<?> selection = getSelection( event );
            for(Object o:selection) {
                try {
                if(o instanceof IAtom) {
                    getManager().removeAtom( (IAtom )o);
                }
                else if(o instanceof IBond) {
                    getManager().removeBond((IBond)o);
                }
                } catch (BioclipseException e) {
                    logger.warn( "Failed to remove bond or atom" );
                    logger.debug( o.toString() );
                }
            }
        }
        return null;
    }
}
