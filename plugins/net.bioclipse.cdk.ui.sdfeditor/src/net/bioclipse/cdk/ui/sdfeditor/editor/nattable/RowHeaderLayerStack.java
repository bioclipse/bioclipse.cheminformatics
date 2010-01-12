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
import net.sourceforge.nattable.grid.layer.RowHeaderLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.SelectionLayer;


/**
 * @author arvid
 *
 */
public class RowHeaderLayerStack extends AbstractLayerTransform {

    
    public RowHeaderLayerStack( IDataProvider dataProvider, 
                                ILayer bodyLayer,
                                SelectionLayer selectionLayer) {
        
        DataLayer dataLayer = new DataLayer( dataProvider );
        RowHeaderLayer rowHeaderLayer = new RowHeaderLayer( 
                                                        dataLayer, 
                                                        bodyLayer, 
                                                        selectionLayer);
        setUnderlyingLayer( rowHeaderLayer );
    }
}
