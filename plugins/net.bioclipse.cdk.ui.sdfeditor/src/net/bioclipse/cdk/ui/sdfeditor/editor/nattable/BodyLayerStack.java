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

import net.bioclipse.cdk.ui.sdfeditor.Activator;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.reorder.ColumnReorderLayer;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.viewport.ViewportLayer;


/**
 * @author arvid
 *
 */
public class BodyLayerStack extends AbstractLayerTransform {

    
    private SelectionLayer selectionLayer;
    private DataLayer bodyDataLayer;

    public BodyLayerStack(IDataProvider dataProvider) {

        bodyDataLayer = new DataLayer(dataProvider);
        int width = Activator.getDefault().getPreferenceStore()
                            .getInt( Activator.STRUCTURE_COLUMN_WIDTH );
        bodyDataLayer.setColumnWidthByPosition( 0, width );
        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer( bodyDataLayer );
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer( columnReorderLayer );
        selectionLayer = new SelectionLayer(columnHideShowLayer);
        ViewportLayer viewportLayer = new ViewportLayer( selectionLayer );
        setUnderlyingLayer( viewportLayer );
        
    }
    
    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }
    
    public DataLayer getDataLayer() { return bodyDataLayer; }
}
