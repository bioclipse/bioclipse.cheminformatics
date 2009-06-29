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

import net.bioclipse.cdk.ui.sdfeditor.Activator;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculeTableContentProvider;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;
import net.bioclipse.cdk.ui.sdfeditor.editor.SDFIndexEditorModel;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseUIJob;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;


/**
 * @author arvid
 *
 */
public class CalculatePropertyHandler extends AbstractHandler implements IHandler {

    Logger logger = Logger.getLogger( CalculatePropertyHandler.class );

    protected static final String PARAMETER_ID = "net.bioclipse.cdk.ui.sdfeditor.calculatorId";

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        String calc = event.getParameter( PARAMETER_ID );
        IPropertyCalculator<?>[] calcList = getCalculators( calc );

        IEditorPart editorPart = null;
        ISelection selection = HandlerUtil.getCurrentSelection( event );
        if(selection instanceof IStructuredSelection) {
            Object o = ((IStructuredSelection)selection).getFirstElement();
            if(o instanceof IFile) {
                IEditorPart part = getEditor( (IFile)o );
                if(part==null) {
                    executeCalculators( (IFile)o, calcList );
                }else {
                    editorPart = part;
                }
            }
        }

        if(editorPart==null) {
            editorPart = HandlerUtil.getActiveEditor( event );
        }
        if(editorPart!=null && editorPart instanceof MultiPageMoleculesEditorPart) {

            MultiPageMoleculesEditorPart mpmep = (MultiPageMoleculesEditorPart)
            editorPart;
            MoleculesEditor editor = (MoleculesEditor) mpmep
            .getAdapter( MoleculesEditor.class );
            // FIXME there can be other models besides SDFIndexEditorModel
            if(editor!=null) {
                SDFIndexEditorModel model = findModel( editor );
                executeCalculators( model, editor, calcList  );
                mpmep.setDirty( true );
            }
        }else {
            logger.warn( "Failed to calculate property" );
        }

        return null;
    }

    private SDFIndexEditorModel findModel(MoleculesEditor editor) {

            IMoleculesEditorModel model = editor.getModel();
            if(model instanceof SDFIndexEditorModel)
                return (SDFIndexEditorModel) model;
            else {
                IllegalArgumentException e = new IllegalArgumentException(
                                                 "Only SDF model in supported");
                LogUtils.handleException( e, logger,
                                          "net.bioclipse.cdk.ui.sdfeditor" );
                throw e;
            }
    }
    private IPropertyCalculator<?>[] getCalculators(String calc) {

        List<String> calcs = Arrays.asList( calc.split( ",\\s*" ) );

        IConfigurationElement[] elements = getConfigurationElements();

        return getCalculators( elements, calcs )
                        .toArray( new IPropertyCalculator<?>[0] );
    }

    public static IConfigurationElement[] getConfigurationElements() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        return registry.getConfigurationElementsFor(
                                       "net.bioclipse.cdk.propertyCalculator" );
    }
    protected Collection<IPropertyCalculator<?>> getCalculators(
                                              IConfigurationElement[] elements,
                                              Collection<String> ids) {
        if(elements.length==0) return Collections.emptyList();

        List<IPropertyCalculator<?>> calcList
        = new ArrayList<IPropertyCalculator<?>>();
        for(String id:ids) {
            for(IConfigurationElement element:elements) {
                if(id.equals( element.getAttribute( "id" ) )) {
                    try {
                    IPropertyCalculator<?> calculator = (IPropertyCalculator<?>)
                    element.createExecutableExtension( "class" );
                    calcList.add(calculator);
                    break;
                    }catch(CoreException e) {
                        Logger.getLogger( CalculatePropertyHandler.class )
                        .debug( "Failed to craete a IPropertyCalculator", e );
                    }
                }
            }
        }
        return calcList;
    }

    public static Collection<IPropertyCalculator<?>> gatherCalculators(
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
                Logger.getLogger( CalculatePropertyHandler.class )
                        .debug( "Failed to craete a IPropertyCalculator", e );
            }
        }
        return calcList;
    }
    private void executeCalculators( IFile file,
                                     IPropertyCalculator<?>[] calculators  ) {
        try {
            Activator.getDefault().getMoleculeTableManager()
            .calculatePropertiesFor( file, calculators );
        } catch ( BioclipseException e ) {
            LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
        }
    }
    private void executeCalculators( SDFIndexEditorModel model,
                                     final MoleculesEditor editor,
                         final IPropertyCalculator<?>[] calculators) {

        Activator.getDefault().getMoleculeTableManager()
        .calculateProperty( model,
                            calculators,
                            new BioclipseUIJob<Void>() {
            @Override
            public void runInUI() {
                MoleculeTableContentProvider contProv =
                    editor.getContentProvider();
                List<Object> props= contProv.getProperties();
                for(IPropertyCalculator<?> calculator:calculators) {
                    String name = calculator.getPropertyName();
                    if(!name.equals( "net.bioclipse.cdk.fingerprint" )) {

                        if(!props.contains( name )) {
                            if(props.size()>=1)
                                props.add( 1, name );
                            else
                                props.add(0, name);
                        }
                    }
                }
                contProv.setVisibleProperties( props );
                contProv.updateHeaders();
            }
        });
    }
    private IEditorPart getEditor(IFile file) {
        IWorkbench workB = PlatformUI.getWorkbench();
        IWorkbenchPage page
        = workB.getActiveWorkbenchWindow().getActivePage();
        List<IEditorReference> toClose
        = new ArrayList<IEditorReference>();
        IFileEditorInput ei= new FileEditorInput(file);
        IEditorReference[] editorRefs = page.getEditorReferences();
        for(IEditorReference ref:editorRefs) {
            try {
                if(ei.equals( ref.getEditorInput()))
                    toClose.add(ref);
            } catch ( PartInitException e ) {
                // Failed to close one edtior
            }
        }
        if(toClose.size()>0) {
            IEditorPart editor = toClose.get(0).getEditor( false );
            return editor;
        }
        return null;
    }
}
