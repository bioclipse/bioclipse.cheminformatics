/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 * 
 * Contributors:
 *     Arvid Berg goglepox@users.sf.net
 *     
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.handlers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.bioclipse.cdk.jchempaint.business.IJChemPaintManager;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.IControllerModule;


/**
 * @author arvid
 *
 */
public class ChangeModuleHandler extends AbstractJChemPaintHandler {
    Logger logger = Logger.getLogger( ChangeModuleHandler.class );
    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    public Object execute( ExecutionEvent event ) throws ExecutionException {

        ControllerHub hub = getControllerHub( event );
        
        String module = event.getParameter( "jcp.controller.module"  );
        Class<IJChemPaintManager> cl = IJChemPaintManager.class;
        try {
            
            Class<?> cls = Class.forName( "org.openscience.cdk.controller."
                                          +module);
            Class<?> partypes[] =new Class<?>[]{ IChemModelRelay.class };
            Constructor<?> ct    = cls.getConstructor(partypes);
            Object arglist[] = new Object[] {hub};

            IControllerModule retobj = (IControllerModule)ct.newInstance(arglist);
            hub.setActiveDrawModule( retobj );

        } catch ( NoSuchMethodException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        } catch ( IllegalArgumentException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        } catch ( IllegalAccessException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        } catch ( InvocationTargetException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        } catch ( ClassNotFoundException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        } catch ( InstantiationException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        }
        return null;
    }

}
