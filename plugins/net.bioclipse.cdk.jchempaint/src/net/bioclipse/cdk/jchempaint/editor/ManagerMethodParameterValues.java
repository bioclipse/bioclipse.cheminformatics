/*******************************************************************************
 * Copyright (c) 2009 Arvid <goglepox@usrs.sourceforge.net>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 * 
 * Contributors:
 *     
 *     
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.editor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.cdk.jchempaint.business.IJChemPaintManager;

import org.eclipse.core.commands.IParameterValues;


public class ManagerMethodParameterValues implements IParameterValues {

    @SuppressWarnings("unchecked")
    public Map getParameterValues() {

        Method[] methods = IJChemPaintManager.class.getMethods();
        Map<String,String> values = new HashMap<String, String>();
        for(Method method:methods) {
            if(method.getParameterTypes().length == 0 ) {
                String value = method.getName();
                StringBuilder sBuilder = new StringBuilder();
                sBuilder.appendCodePoint( Character.toUpperCase( value.codePointAt( 0 )));
                sBuilder.append( value.substring( 1 ));
                values.put( sBuilder.toString(), value );
            }
        }
        return values;
    }

}
