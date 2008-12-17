/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 * 
 * Contributors:
 *     Arvid Berg goglepox@users.sf.se
 *     
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.handlers;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintManager;
import net.bioclipse.core.util.LogUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
/**
 * @author arvid
 *
 */
public class ManagerHandler extends AbstractJChemPaintHandler {
    public static final String managerMethod = "jcp.manager.method";
    Logger logger = Logger.getLogger( ManagerHandler.class );
    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    public Object execute( ExecutionEvent event ) throws ExecutionException {
        String methodName = event.getParameter( managerMethod  );
        Class<IJChemPaintManager> cl = IJChemPaintManager.class;
        try {
            Method method = cl.getDeclaredMethod(  methodName ,new Class[] {} );
           method.invoke( getManager(), new Object[] {} ); 
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
        }
        return null;
    }
}
