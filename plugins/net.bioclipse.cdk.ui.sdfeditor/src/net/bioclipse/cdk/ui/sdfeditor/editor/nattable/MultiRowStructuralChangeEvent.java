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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.swt.graphics.Rectangle;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.IStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.MultiRowVisualChangeEvent;
import net.sourceforge.nattable.layer.event.StructuralDiff;


/**
 * @author arvid
 *
 */
public class MultiRowStructuralChangeEvent extends MultiRowVisualChangeEvent
                                      implements IStructuralChangeEvent {

    public MultiRowStructuralChangeEvent(ILayer layer) {
        super(layer);
    }
    public Collection<StructuralDiff> getColumnDiffs() { return null; }

    public Collection<StructuralDiff> getRowDiffs() { return null; }

    public boolean isHorizontalStructureChanged() { return false; }

    public boolean isVerticalStructureChanged() { return true; }

    public ILayerEvent cloneEvent() {
        return new MultiRowStructuralChangeEvent( getLayer() );
    }
    
    @Override
    public Collection<Rectangle> getChangedPositionRectangles() {
        Collection<Integer> rowPositions = getRowPositions();
        if(rowPositions == null || rowPositions.size() <= 0)
            return null;

        int minRowPosition = Collections.min( rowPositions );
        
        int columnCount = getLayer().getColumnCount();
        int rowCount = getLayer().getColumnCount();
        
        return Arrays.asList( new Rectangle[] {
                  new Rectangle( 0, minRowPosition, columnCount , 
                                 rowCount-minRowPosition )
        } );
    }

}
