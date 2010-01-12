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

import org.eclipse.jface.viewers.IColorProvider;

import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculeTableViewer;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.layer.cell.IConfigLabelAccumulator;


/**
 * @author arvid
 *
 */
public class ColorProviderLableAccumulator implements IConfigLabelAccumulator {

    IDataProvider dataProvider;
    ILayer layer;
    
    
    public ColorProviderLableAccumulator(ILayer layer, IDataProvider dataProvider) {
        this.layer = layer;
        this.dataProvider = dataProvider;
    }
    /* (non-Javadoc)
     * @see net.sourceforge.nattable.layer.cell.IConfigLabelAccumulator#accumulateConfigLabels(net.sourceforge.nattable.layer.LabelStack, int, int)
     */
    public void accumulateConfigLabels( LabelStack configLabels,
                                        int columnPosition, int rowPosition ) {
       
       Object value = layer.getDataValueByPosition( columnPosition, rowPosition );
       if(value instanceof IColorProvider)
           configLabels.addLabel( MoleculeTableViewer.COLOR_PROVIDER_LABEL );

    }

}
