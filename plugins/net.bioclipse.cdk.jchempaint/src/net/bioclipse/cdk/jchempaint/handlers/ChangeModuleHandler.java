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
    protected IControllerModule newInstance(Constructor<?> ct, Object[] arglist) 
                  throws IllegalArgumentException, 
                  InstantiationException, 
                  IllegalAccessException, 
                  InvocationTargetException {
        return (IControllerModule)ct.newInstance(arglist);
    }
    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    public Object execute( ExecutionEvent event ) throws ExecutionException {
        ControllerHub hub = getControllerHub( event );
        String module = event.getParameter( "jcp.controller.module"  );
        String intString = event.getParameter( "jcp.controller.param.int");
        Integer value = (intString != null ? new Integer(intString):null);
        String boolString = event.getParameter( "jcp.controller.param.boolean" );
        Boolean bool = boolString != null ?new Boolean(boolString):null;
        Class<IJChemPaintManager> cl = IJChemPaintManager.class;
        try {
            Class<?> cls = Class.forName( "org.openscience.cdk.controller."
                                          +module);
            Constructor<?> ct;
            try {
                ct = cls.getConstructor(new Class<?>[]{ IChemModelRelay.class });
                hub.setActiveDrawModule( newInstance( ct, new Object[] {hub} ) );
            } catch(NoSuchMethodException x) {
                try {
                    ct = cls.getConstructor(new Class<?>[]{ IChemModelRelay.class
                                                            ,int.class});
                    hub.setActiveDrawModule( newInstance( ct, 
                              new Object[] {hub,value} ) );
                }catch (NoSuchMethodException y) {
                   ct = cls.getConstructor(new Class<?>[]{ IChemModelRelay.class
                                                           ,int.class
                                                           ,boolean.class});
                    hub.setActiveDrawModule( newInstance( ct, 
                                         new Object[] {hub,value,bool} ) );
                }
            }
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
