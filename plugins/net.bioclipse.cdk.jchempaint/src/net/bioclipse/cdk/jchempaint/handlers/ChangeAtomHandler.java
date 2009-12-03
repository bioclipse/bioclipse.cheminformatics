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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.edit.CompositEdit;
import org.openscience.cdk.controller.edit.IEdit;
import org.openscience.cdk.controller.edit.SetSymbol;
import org.openscience.cdk.interfaces.IAtom;

public class ChangeAtomHandler extends AbstractJChemPaintHandler {

    Logger logger = Logger.getLogger(ChangeAtomHandler.class);
    public Object execute(ExecutionEvent event) throws ExecutionException {
       String o = event.getParameter( "atomType" );
       if( o == null) o= "C";
       IChemModelRelay relay = getChemModelRelay( event );
       Collection<?> c = getSelection( event );
       List<IEdit> edits = new ArrayList<IEdit>(c.size());
       for(Object element: c) {
           IAtom atom = null;
           if(element instanceof IAdaptable)
               atom = (IAtom) ((IAdaptable)element).getAdapter( IAtom.class );
           if(atom!=null)
               edits.add( SetSymbol.setSymbol( atom, o ) );
       }
       relay.execute( CompositEdit.compose( edits ) );
       return null;
    }
}