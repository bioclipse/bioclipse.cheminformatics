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

import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculeTableContentProvider;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;
import net.sourceforge.nattable.NatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;


public class RemoveColumnHandler extends AbstractHandler implements IHandler {

    public Object execute( ExecutionEvent event ) throws ExecutionException {

        IEditorPart part = HandlerUtil.getActiveEditor( event );
        MoleculesEditor molEditor = (MoleculesEditor)
                                    part.getAdapter( MoleculesEditor.class );

        int[] vals  = ((NatTable)molEditor.getMolTableViewer().getControl())
                                .getSelectionModel().getSelectedColumns();
        MoleculeTableContentProvider contentProvider =
                                                molEditor.getContentProvider();

        for(int i:vals) {
            Object o = contentProvider.getProperties().get( i-1 );
            contentProvider.removeColumn( o );
        }
        molEditor.setDirty(true);
        return null;
    }

}
