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
import org.openscience.cdk.interfaces.IAtom;
public class ChangeAtomHandler extends AbstractJChemPaintHandler {
    Logger logger = Logger.getLogger(ChangeAtomHandler.class);
    public Object execute(ExecutionEvent event) throws ExecutionException {
       String o = event.getParameter( "atomType" );
       if( o == null) o= "C";
       IAtom atom = getSingleSelectedAtom( event );
       if(atom != null) {
           getManager().setSymbol( atom, o );
       }
       return null;
    }
}