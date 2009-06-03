/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.handlers;

import java.util.List;

import net.bioclipse.cdk.domain.CDKChemObject;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.nonotify.NNAtomContainer;


/**
 * @author arvid
 *
 */
public class SubStructureSearchHandler extends AbstractHandler implements IHandler{

    public Object execute( ExecutionEvent event ) throws ExecutionException {

        IAtomContainer ac = getACFromSelection(
                                  HandlerUtil.getCurrentSelection( event ));


        // TODO Do sub structure search
        return null;
    }

    private IAtomContainer getACFromSelection(ISelection selection) {

        IAtomContainer ac = new NNAtomContainer();
        if(selection instanceof IStructuredSelection) {
            List<?> sel = ((IStructuredSelection)selection).toList();

            for(Object o: sel) {
                if(o instanceof CDKChemObject<?>) {
                    IChemObject chemObject = ((CDKChemObject<?>)o).getChemobj();
                    if( chemObject instanceof IAtom ) {
                        ac.addAtom( (IAtom) chemObject );
                    }
                    else if(chemObject instanceof IBond) {
                        ac.addBond( (IBond) chemObject );
                    }
                }
            }
        }
        return ac;
    }
}
