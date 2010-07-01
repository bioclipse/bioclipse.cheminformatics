/*******************************************************************************
 * Copyright (c) 2009 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *      Arvid Berg
 *
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.handlers;

import java.util.HashMap;
import java.util.Map;

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceScopes;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator.WillDrawAtomNumbers;


public class TogglePrefHandler extends AbstractHandler implements IElementUpdater{

    boolean isSelected = false;

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        Command command = event.getCommand();
       // boolean oldValue = HandlerUtil.toggleCommandState( command );
        boolean oldValue = false;
        IEditorPart jcpPart = HandlerUtil.getActiveEditor(event);

        JChemPaintWidget jcpWidget = getWidget(jcpPart);
        if(jcpWidget != null)
            updateModel(jcpWidget, oldValue);

        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        Map filter = new HashMap();
        filter.put(IServiceScopes.WINDOW_SCOPE, window);
        ICommandService service = (ICommandService) window.getService(ICommandService.class);
        service.refreshElements("net.bioclipse.cdk.jchempaint.preference.atomNumbers", filter);
        return null;
    }

    private JChemPaintWidget getWidget(IWorkbenchPart part) {
        if(part instanceof JChemPaintEditor) {
            return ((JChemPaintEditor) part).getWidget();
        } else {
            JChemPaintEditor editor = (JChemPaintEditor) part.getAdapter(JChemPaintEditor.class);
            if(editor != null) return editor.getWidget();
        }
        return null;
    }

    private void updateModel(JChemPaintWidget widget, boolean oldValue) {
        RendererModel model = widget.getRenderer2DModel();
        boolean value = model.get(WillDrawAtomNumbers.class);
        model.set(WillDrawAtomNumbers.class, !value);
        isSelected = !value;
        widget.redraw();

    }

    @Override
    public void updateElement(UIElement element, Map parameters) {
        boolean state = isSelected;
        Object value = parameters.get("org.eclipse.ui.part.IWorkbenchPartSite");
        if( value instanceof IWorkbenchPartSite) {
            IWorkbenchPartSite site = (IWorkbenchPartSite) value;
            IWorkbenchPart part = site.getPart();
            JChemPaintWidget widget = getWidget(part);
            if(widget!=null) {
                RendererModel model = widget.getRenderer2DModel();
                state = model.get(WillDrawAtomNumbers.class);
            }
        }
        element.setChecked(state);
    }
}
