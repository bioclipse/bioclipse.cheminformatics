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
package net.bioclipse.cdk.jchempaint.view;

import java.io.IOException;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.ui.business.IUIManager;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * @author arvid
 *
 */
public class OpenJCPHandler extends AbstractHandler implements IHandler {
    
    Logger logger = Logger.getLogger( OpenJCPHandler.class );
    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    public Object execute( ExecutionEvent event ) throws ExecutionException {
        IUIManager ui = net.bioclipse.ui.business.Activator.getDefault()
                                                           .getUIManager();
        ISelection sel;
        IWorkbenchPart part = HandlerUtil.getActivePart( event );
        if( part instanceof JChemPaintView) {
            sel = ((JChemPaintView)part).getSelection();
        }else
            return null;
        if(sel instanceof IStructuredSelection) {
            Object o = ((IStructuredSelection)sel).getFirstElement();
            if(o instanceof IBioObject) {
                try {
                    ui.open( (IBioObject )o);
                } catch ( BioclipseException e ) {
                    logging(e);
                } catch ( CoreException e ) {
                    logging(e);
                } catch ( IOException e ) {
                    logging(e);
                }
            }
        }
        return null;
    }
    private void logging(Exception x) {
        logger.warn( "Faild to open selection" );
        logger.debug( "Could not open ediotr " + x.getMessage(),x );
    }
}
