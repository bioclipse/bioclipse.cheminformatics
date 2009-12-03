/*******************************************************************************
 * Copyright (c) 2009 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arvid Berg
 *
 *****************************************************************************/
package net.bioclipse.cdk.jchempaint.handlers;

import static org.openscience.cdk.controller.edit.AddSingleElectron.addElectron;
import static org.openscience.cdk.controller.edit.RemoveSingleElectron.removeElectron;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.edit.CompositEdit;
import org.openscience.cdk.controller.edit.IEdit;
import org.openscience.cdk.interfaces.IAtom;


public class ChangeElectronHandler extends AbstractJChemPaintHandler {
    
    enum Change {
        add, remove
    }

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        
        Change change;
        String o = event.getParameter( "addRemove" );
        if( o.equalsIgnoreCase( "add" )) change=Change.add;
        else change = Change.remove;
                        
        IChemModelRelay relay = getChemModelRelay( event );
        Collection<?> c = getSelection( event );
        List<IEdit> edits = new ArrayList<IEdit>(c.size());
        for(Object element: c) {
            if(element instanceof IAdaptable) {
                IAtom atom = null;
                atom = (IAtom) ((IAdaptable)element).getAdapter( IAtom.class );
                if(atom!=null) {
                    switch(change) {
                        case add: edits.add( addElectron( atom ) );break;
                        case remove: edits.add( removeElectron( atom ));break;
                    }
                }
            }
        }
        relay.execute( CompositEdit.compose( edits ) );
        return null;
    }

}
