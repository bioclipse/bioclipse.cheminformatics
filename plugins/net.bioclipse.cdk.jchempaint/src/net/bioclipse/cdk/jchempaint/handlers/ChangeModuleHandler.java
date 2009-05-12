/*******************************************************************************
 * Copyright (c) 2008-2009 The Bioclipse Project and others.
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.IControllerModule;
import org.openscience.cdk.controller.ControllerHub.Direction;


/**
 * @author arvid
 *
 */
public class ChangeModuleHandler extends AbstractJChemPaintHandler
                                                implements IElementUpdater{
    Logger logger = Logger.getLogger( ChangeModuleHandler.class );

    enum Params {
        MODULE_PARAM("jcp.controller.module"),
        INT_PARAM("jcp.controller.param.int"),
        BOOLEAN_PARAM("jcp.controller.param.boolean"),
        DIRECTION_PARAM("jcp.controller.param.direction"),
        DrawModeString("net.bioclipse.cdk.jchempaint.DrawModeString");

        Params(String val){
            value = val;
        }

        String value;

        public String getValue() {
            return value;
        }
    }

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

        //if(matchesRadioState( event ))
        //    return null;

        ControllerHub hub = getControllerHub( event );
        if(hub==null) return null;

        Parameters params= new Parameters(event.getParameters());

        String module = (String)params.getParameter( Params.MODULE_PARAM );
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
                                                            ,Integer.TYPE});
                    hub.setActiveDrawModule( newInstance( ct,
                              params.getArray( hub,
                                               Params.INT_PARAM )));
                }catch (NoSuchMethodException y) {
                    try {
                   ct = cls.getConstructor(new Class<?>[]{ IChemModelRelay.class
                                                           ,int.class
                                                           ,boolean.class});
                    hub.setActiveDrawModule( newInstance( ct,
                      params.getArray( hub,
                                       Params.INT_PARAM,
                                       Params.BOOLEAN_PARAM  ) ) );

                    } catch(NoSuchMethodException z) {
                        ct = cls.getConstructor(new Class<?>[]{ IChemModelRelay.class
                                ,Direction.class});
                        hub.setActiveDrawModule( newInstance( ct,
                                 params.getArray( hub,
                                                  Params.DIRECTION_PARAM )) );
                    }
                }
            }

            updateRadioState( event.getCommand(), module);
            upate( event );

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

    private boolean matchesRadioState(ExecutionEvent event)
                        throws ExecutionException {

        String parameter = event.getParameter(ModuleState.PARAMETER_ID );
        if(parameter == null)
            throw new ExecutionException("Missing radio state parameter");
//        Command command= event.getCommand();
//        State state = command.getState( ModuleState.STATE_ID );
//        if(state == null)
//            throw new ExecutionException("No radio state");
//        if(!(state.getValue() instanceof String) )
//                throw new ExecutionException("Radio state not string");

//        return parameter.equals( state.getValue())
            return getControllerHub( event ).getActiveDrawModule().getClass().getName().equals( parameter );
    }

    private void updateRadioState(Command command, String newState)
                            throws ExecutionException {
        State state = command.getState(ModuleState.STATE_ID);
        if(state == null)
            throw new ExecutionException("No radio state");
        state.setValue( newState );

    }

    private void upate(ExecutionEvent event) throws ExecutionException {
        ICommandService service = (ICommandService) HandlerUtil
        .getActiveWorkbenchWindowChecked(event).getService(
            ICommandService.class);
        service.refreshElements(event.getCommand().getId(), null);

    }

    public String getCurrentValue() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        if(workbenchWindow != null) {

            IEditorPart editor = workbenchWindow
            .getActivePage()
            .getActiveEditor();

            if ( (editor instanceof JChemPaintEditor) ) {
                ControllerHub hub = ((JChemPaintEditor) editor).getControllerHub();
                return hub.getActiveDrawModule().getDrawModeString();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void updateElement(UIElement element, Map parameters) {

        String parm = (String) parameters.get(Params.DrawModeString.getValue());
        if (parm != null) {
          if (getCurrentValue() != null && getCurrentValue().equals(parm)) {
            element.setChecked(true);
          } else {
            element.setChecked(false);
          }
        }
      }

    static class Parameters {

        Map<Params,Object> parameters;


        @SuppressWarnings("unchecked")
        public Parameters(Map commandParams) {
            parameters = new HashMap<Params, Object>();
            for(Params p:Params.values()) {
                String par=(String)commandParams.get( p.getValue() );
                if(par != null) {
                Object result = null;

                switch(p) {
                    case MODULE_PARAM: result =  par ;break;
                    case INT_PARAM: result = Integer.valueOf( par );break;
                    case BOOLEAN_PARAM: result = Boolean.valueOf( par );break;
                    case DIRECTION_PARAM:
                           result = Direction.valueOf( par.toUpperCase());break;
                }
                 if(result !=null)
                     parameters.put( p, result );
                }
            }
        }

        Object[] getArray(Object o,Params... args) {
            List<Object> result = new LinkedList<Object>();
            if(o !=null) result.add( o );
            for(Params s:args) {
                result.add( parameters.get(s) );
            }
            return result.toArray();
        }

        Object getParameter(Params p) {
            return parameters.get(p);
        }
    }
}
