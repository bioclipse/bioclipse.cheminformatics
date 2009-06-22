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
package net.bioclipse.cdk.jchempaint.handlers;

import java.util.Map;

import net.bioclipse.cdk.jchempaint.Activator;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintManager;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * @author arvid
 *
 */
public class ToggleImplicitHydrogens extends AbstractHandler implements IElementUpdater {

    public static final String STATE_ID = "org.eclipse.ui.commands.toggleState";

    public Object execute( ExecutionEvent event ) throws ExecutionException {

        Command command = event.getCommand();
        IJChemPaintManager jcp = Activator.getDefault().getJavaManager();
        boolean value = jcp.getShowImplicitHydrogens();
        boolean oldValue = toggleCommandState( command, value);

        if(!oldValue)
            jcp.updateImplicitHydrogenCounts();

        jcp.setShowImplicitHydrogens( !oldValue );

       return null;
    }

    public static boolean toggleCommandState( Command command, boolean value)
                                                    throws ExecutionException {
        State state = command.getState(STATE_ID);
        if(state == null)
          throw new ExecutionException("The command does not have a toggle state");
         if(!(state.getValue() instanceof Boolean))
          throw new ExecutionException("The command's toggle state doesn't contain a boolean value");

        boolean oldValue = ((Boolean) state.getValue()).booleanValue();
        state.setValue( !value );
        return value;
      }

    public void updateElement(UIElement element, Map parameters) {
        element.setChecked(Activator.getDefault().getJavaManager()
                                       .getShowImplicitHydrogens());
      }

}
