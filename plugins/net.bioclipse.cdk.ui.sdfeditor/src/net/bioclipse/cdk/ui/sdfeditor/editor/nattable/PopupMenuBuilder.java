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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.ui.menu.IMenuItemProvider;
import net.sourceforge.nattable.ui.menu.MenuItemProviders;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.widgets.Menu;


/**
 * @author arvid
 *
 */
class PopupMenuBuilder extends ContributionItem {
    
    NatTable natTable;
    List<IMenuItemProvider> menuItems;
    
    public PopupMenuBuilder( NatTable natTable) {
        menuItems = new ArrayList<IMenuItemProvider>();
        this.natTable = natTable;
    }
    
    @Override
    public void fill( Menu menu, int index ) {
        for(IMenuItemProvider menuItemProvider:menuItems)
            menuItemProvider.addMenuItem( natTable, menu );
    }
    
    public PopupMenuBuilder hideColumnMenuItemProvider(){
        menuItems.add( MenuItemProviders.hideColumnMenuItemProvider());
        return this;
    }
    
    public PopupMenuBuilder autoResizeColumnMenuItemProvider(){
        menuItems.add( MenuItemProviders.autoResizeColumnMenuItemProvider());
        return this;
    }
    
    public PopupMenuBuilder autoResizeRowMenuItemProvider(){
        menuItems.add( MenuItemProviders.autoResizeRowMenuItemProvider());
        return this;
    }
    
    public PopupMenuBuilder add(IMenuItemProvider menuItemProvider) {
        menuItems.add( menuItemProvider );
        return this;
    }
}