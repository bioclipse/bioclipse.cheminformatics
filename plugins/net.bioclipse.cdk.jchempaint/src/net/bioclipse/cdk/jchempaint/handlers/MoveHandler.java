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

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.controller.Controller2DHub;
import org.openscience.cdk.controller.IController2DModel;
import org.openscience.cdk.controller.IController2DModel.DrawMode;

public class MoveHandler extends AbstractHandler implements IHandler {

    Logger logger = Logger.getLogger(RemoveHandler.class);
    public Object execute(ExecutionEvent event) throws ExecutionException {
       IEditorPart editor=HandlerUtil.getActiveEditor(event);
       if(! (editor instanceof JChemPaintEditor)){
           logger.debug("Not JChemPaintEditor");
           return null;
       }
       
       Controller2DHub hub=((JChemPaintEditor)editor).getControllerHub();
       IController2DModel c2dm=hub.getController2DModel();       
       c2dm.setDrawMode(DrawMode.MOVE);
        return null;
    }

}
