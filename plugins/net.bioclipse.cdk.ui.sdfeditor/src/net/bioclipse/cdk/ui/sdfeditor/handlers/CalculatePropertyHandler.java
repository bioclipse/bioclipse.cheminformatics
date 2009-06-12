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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.bioclipse.cdk.ui.sdfeditor.Activator;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculeTableContentProvider;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;
import net.bioclipse.cdk.ui.sdfeditor.editor.SDFIndexEditorModel;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseUIJob;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * @author arvid
 *
 */
public class CalculatePropertyHandler extends AbstractHandler implements IHandler {

    Logger logger = Logger.getLogger( CalculatePropertyHandler.class );

    private static final String PARAMETER_ID = "net.bioclipse.cdk.ui.sdfeditor.calculatorId";

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        IEditorPart editorPart = HandlerUtil.getActiveEditor( event );
        MultiPageMoleculesEditorPart mpmep = (MultiPageMoleculesEditorPart)
                                                        editorPart;
        // FIXME there can be other models besides SDFIndexEditorModel
        final MoleculesEditor editor = (MoleculesEditor) editorPart
                                         .getAdapter( MoleculesEditor.class );
        if(editor== null) {
            logger.warn( "Could not find a MoleculesEditor" );
            return null;
        }

        if(!(editor.getModel() instanceof SDFIndexEditorModel)) {
            IllegalArgumentException e = new IllegalArgumentException("Only SDF model in supported");
            LogUtils.handleException( e, logger, "net.bioclipse.cdk.ui.sdfeditor" );
            throw e;
        }

        SDFIndexEditorModel model = (SDFIndexEditorModel) editor.getModel();
        String calc = event.getParameter( PARAMETER_ID );
        Set<String> calcs = new TreeSet<String>(
                                    Arrays.asList( calc.split( ",\\s*" ) ));

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] elements = registry.getConfigurationElementsFor(
                                       "net.bioclipse.cdk.propertyCalculator" );


        Collection<IPropertyCalculator<?>>  calcList;

        calcList = gatherCalculators( elements, calcs );

        executeCalculators( model, editor, calcList );
        mpmep.setDirty( true );
        return null;
    }
    private Collection<IPropertyCalculator<?>> gatherCalculators(
                        IConfigurationElement[] elements,
                        Collection<String> ids) {
        if(elements.length==0) return Collections.emptyList();

        List<IPropertyCalculator<?>> calcList
                                    = new ArrayList<IPropertyCalculator<?>>();
        for(IConfigurationElement element:elements) {
            if(ids!=null && !ids.contains( element.getAttribute( "id" ) ))
                    continue;
            try {
                IPropertyCalculator<?> calculator = (IPropertyCalculator<?>)
                                   element.createExecutableExtension( "class" );
                calcList.add(calculator);
            } catch ( CoreException e ) {
                logger.debug( "Failed to craete a IPropertyCalculator", e );
            }
        }
        return calcList;
    }
    private void executeCalculators( SDFIndexEditorModel model,
                                     final MoleculesEditor editor,
                         final Collection<IPropertyCalculator<?>> calculators) {

        Activator.getDefault().getMoleculeTableManager()
        .calculateProperty( model,
                            calculators.toArray( new IPropertyCalculator<?>[0]),
                            new BioclipseUIJob<Void>() {
            @Override
            public void runInUI() {
                MoleculeTableContentProvider contProv =
                    editor.getContentProvider();
                List<Object> props= contProv.getProperties();
                for(IPropertyCalculator<?> calculator:calculators) {
                    String name = calculator.getPropertyName();
                    if(!name.equals( "net.bioclipse.cdk.fingerprint" )) {

                        if(!props.contains( name ))
                            props.add( 0, name );
                    }
                }
                contProv.setVisibleProperties( props );
                contProv.updateHeaders();
            }
        });
    }
}
