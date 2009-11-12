/*******************************************************************************
 * Copyright (c) 2009 Arvid Berg <goglepox@users.sourceforge.net>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 * 
 * Contributors:
 *     
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.handlers;

import net.bioclipse.cdk.jchempaint.Activator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;


/**
 * @author arvid
 *
 */
public class SelectPartHandler extends AbstractJChemPaintHandler implements IHandler {

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        IChemModelRelay relay = getChemModelRelay( event );
        
        IChemObjectSelection selection = relay.getRenderModel().getSelection();
        Activator.getDefault().getJavaManager().selectPart( selection.getConnectedAtomContainer() );
        return null;
    }

}
