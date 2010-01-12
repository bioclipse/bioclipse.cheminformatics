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

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.SelectionLayer;


/**
 * @author arvid
 *
 */
public class ColumnHeaderLayerStack extends AbstractLayerTransform {

    
    private DataLayer dataLayer;

    public ColumnHeaderLayerStack( IDataProvider dataProvider, 
                                   ILayer bodyLayer,
                                   SelectionLayer selectionLayer) {
        
        dataLayer = new DataLayer( dataProvider);
        ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer( 
                                                        dataLayer, 
                                                        bodyLayer, 
                                                        selectionLayer);
        setUnderlyingLayer( colHeaderLayer );
    }

    
    public DataLayer getDataLayer() { return dataLayer; }
}
