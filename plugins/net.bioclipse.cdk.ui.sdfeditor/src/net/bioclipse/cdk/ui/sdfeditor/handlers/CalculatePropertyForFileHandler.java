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
package net.bioclipse.cdk.ui.sdfeditor.handlers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.bioclipse.cdk.ui.sdfeditor.Activator;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;


public class CalculatePropertyForFileHandler extends CalculatePropertyHandler {
    
    Logger logger = Logger.getLogger( CalculatePropertyForFileHandler.class );

    @Override
    public Object execute( ExecutionEvent event ) throws ExecutionException {
    
        ISelection selection = HandlerUtil.getCurrentSelection( event );
        if(selection instanceof IStructuredSelection) {
            Object o = ((IStructuredSelection)selection).getFirstElement();
            if(o instanceof IFile) {
                String calc = event.getParameter( PARAMETER_ID );
                List<String> calcs = Arrays.asList( calc.split( ",\\s*" ) );

                IConfigurationElement[] elements = getConfigurationElements();

                Collection<IPropertyCalculator<?>>  calcList;

                calcList = getCalculators( elements, calcs );
                try {
                    Activator.getDefault().getMoleculeTableManager()
                    .calculatePropertiesFor( (IFile)o, 
                            calcList.toArray( new IPropertyCalculator<?>[0]) );
                } catch ( BioclipseException e ) {
                    LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
                }
            }
        }
        return null;
    }
}
