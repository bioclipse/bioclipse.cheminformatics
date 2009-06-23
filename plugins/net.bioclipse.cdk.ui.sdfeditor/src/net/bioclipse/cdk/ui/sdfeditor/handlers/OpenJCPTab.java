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

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;



public class OpenJCPTab extends AbstractHandler implements IHandler {

    public Object execute( ExecutionEvent event ) throws ExecutionException {

        IEditorPart part = HandlerUtil.getActiveEditor( event );
        if(part instanceof MultiPageMoleculesEditorPart) {
            IEditorPart editor = (IEditorPart) ((MultiPageMoleculesEditorPart)part)
                                    .getAdapter( JChemPaintEditor.class );
            if(editor != null) {
                ((MultiPageMoleculesEditorPart)part).setActiveEditor( editor );
            }
        }
        return null;
    }

    
}
