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

import net.bioclipse.cdk.jchempaint.Activator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;

/**
 * @author arvid
 *
 */
public class ToggleImplicitHydrogens extends AbstractHandler {
    
    public static final String STATE_ID = "org.eclipse.ui.commands.toggleState";

    public Object execute( ExecutionEvent event ) throws ExecutionException {

        Command command = event.getCommand();
        boolean oldValue = toggleCommandState(command);
        
        Activator.getDefault().getJavaManager()
        .setShowImplicitHydrogens( !oldValue );

       return null; 
    }

    public static boolean toggleCommandState(Command command) throws ExecutionException {
        State state = command.getState(STATE_ID);
        if(state == null)
          throw new ExecutionException("The command does not have a toggle state"); //$NON-NLS-1$
         if(!(state.getValue() instanceof Boolean))
          throw new ExecutionException("The command's toggle state doesn't contain a boolean value"); //$NON-NLS-1$
           
        boolean oldValue = ((Boolean) state.getValue()).booleanValue();
        state.setValue(new Boolean(!oldValue));
        return oldValue;
      }

}
