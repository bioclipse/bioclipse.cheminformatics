/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *     Jonathan Alvarsson
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.business.test.jcp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.jchempaint.business.IJChemPaintManager;
import net.bioclipse.cdk.jchempaint.business.JChemPaintManager;
import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.managers.business.IBioclipseManager;

import org.junit.Assert;
import org.junit.Test;

public class APITest extends AbstractManagerTest {

    private final static List<String> methodBlackList = new ArrayList<String>();
    
    static {
        methodBlackList.add("getIJava2DRenderer");
        methodBlackList.add("getIChemModel");
        methodBlackList.add("getController2DModel");
    }

    private JChemPaintManager manager =
        new JChemPaintManager();
    
    @Override
    public IBioclipseManager getManager() {
        return manager;
    }

    @Override
    public Class<? extends IBioclipseManager> getManagerInterface() {
        return IJChemPaintManager.class;
    }
    
    @Test public void testImplementsAllControllerHubMethods() throws Exception {
        Class hub = this.getClass().getClassLoader().loadClass("org.openscience.cdk.controller.IChemModelRelay");
        Assert.assertNotNull("Could not load the IChemModelRelay", hub);
        for (Method method : hub.getMethods()) {
            if (!methodBlackList.contains(method.getName())) {
                Method matchingMethod = getMatchingMethod(manager.getClass(), method);
                Assert.assertNotNull(
                     "The JChemPaintManager does not implement the IChemModelRelay method " +
                     method.getName(), matchingMethod
                );
                Assert.assertEquals(
                     "The IChemModelRelay method " + method.getName() + " must have the return " +
                     "type " + method.getReturnType().getName(),
                     method.getReturnType().getName(),
                     matchingMethod.getReturnType().getName()
                );
            }
        }
    }
    
    private Method getMatchingMethod(Class clazz, Method searchedMethod) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(searchedMethod.getName())) {
                Class[] actualParams = method.getParameterTypes();
                Class[] searchedParams = searchedMethod.getParameterTypes();
                if (actualParams.length == searchedParams.length) {
                    boolean paramsMatch = true;
                    for (int i=0; i<actualParams.length && paramsMatch; i++) {
                        if (!actualParams[i].getName().equals(searchedParams[i].getName()))
                            paramsMatch = false;
                    }
                    if (paramsMatch) return method;
                }
            }
        }
        return null;
    }

}
