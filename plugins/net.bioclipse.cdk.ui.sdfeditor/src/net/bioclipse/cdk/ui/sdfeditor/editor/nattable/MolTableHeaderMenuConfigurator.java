/*******************************************************************************
* Copyright (c) 2009 Arvid Berg <goglepox@users.sourceforge.net
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Arvid Berg
******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor.nattable;

import static net.sourceforge.nattable.ui.menu.MenuItemProviders.showAllColumnMenuItemProvider;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;
import net.sourceforge.nattable.ui.menu.PopupMenuAction;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Menu;


/**
 * @author arvid
 *
 */
public class MolTableHeaderMenuConfigurator extends AbstractUiBindingConfiguration {
    
    private Menu colHeaderMenu;
    
    public MolTableHeaderMenuConfigurator(final NatTable natTable,MenuManager menuManager) {

        PopupMenuBuilder columnBuilder = new PopupMenuBuilder( natTable )
                                .autoResizeColumnMenuItemProvider()
                                .hideColumnMenuItemProvider()
                                .add( showAllColumnMenuItemProvider() );
        menuManager.add( columnBuilder );

        colHeaderMenu = menuManager.createContextMenu( natTable );
        
        natTable.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                colHeaderMenu.dispose();
            }

        });
    }
    
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerMouseDownBinding(
                new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 3), 
                new PopupMenuAction(colHeaderMenu));
    }
}