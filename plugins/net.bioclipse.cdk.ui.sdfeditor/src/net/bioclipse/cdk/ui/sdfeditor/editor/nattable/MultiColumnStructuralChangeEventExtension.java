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

import java.util.Collection;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.MultiColumnStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.StructuralDiff;


/**
 * @author arvid
 *
 */
public class MultiColumnStructuralChangeEventExtension extends
                                              MultiColumnStructuralChangeEvent {
    
    
    public MultiColumnStructuralChangeEventExtension(ILayer layer) {
        super(layer);
    }

    public Collection<StructuralDiff> getColumnDiffs() { return null; }
    
    @Override
    public boolean isHorizontalStructureChanged() { return true; }
    
    @Override
    public boolean isVerticalStructureChanged() { return true; }

    public ILayerEvent cloneEvent() {
        return new MultiColumnStructuralChangeEventExtension( getLayer() );
    }

}
