/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arvid Berg
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.editor;

import java.awt.event.MouseWheelEvent;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.openscience.cdk.controller.IMouseEventRelay;

public class SWTMouseEventRelay implements MouseListener, MouseMoveListener,
        MouseWheelListener, Listener {

    Logger logger = Logger.getLogger( SWTMouseEventRelay.class );

	private int dragFromX = 0;
	private int dragFromY = 0;
	private boolean isDragging = false;
	private IMouseEventRelay relay;

	public SWTMouseEventRelay(IMouseEventRelay relay) {
		this.relay=relay;
	}

	public void handleEvent(Event event) {
        switch (event.type) {

        case SWT.MouseEnter:
        	relay.mouseEnter(event.x, event.y);
        	break;
        case SWT.MouseExit:
          isDragging = false;
        	relay.mouseExit(event.x, event.y);
        	break;
        	default:
        	    logger.debug( "Event("+ event.type + ") not supportet" );
        }
      }

	public void mouseDoubleClick(MouseEvent event) {
		relay.mouseClickedDouble(event.x, event.y);
	}

	public void mouseDown(MouseEvent event) {

	    if( ((MouseEvent)event).button == 1 && (event.stateMask & SWT.CTRL) == 0){
	        relay.mouseClickedDown(event.x, event.y);
	        dragFromX=event.x;
	        dragFromY=event.y;
	        isDragging=true;
	    }
	}

	public void mouseUp( MouseEvent event ) {

	    if( ((MouseEvent)event).button == 1 && (event.stateMask & SWT.CTRL) == 0){
	        relay.mouseClickedUp( event.x, event.y );
	    }
	    isDragging = false;
	}

	public void mouseMove(MouseEvent event) {
		if(isDragging){
    		relay.mouseDrag(dragFromX,dragFromY, event.x, event.y);
    		dragFromX=event.x;
    		dragFromY=event.y;
    	}else
    		relay.mouseMove(event.x, event.y);
	}

    public void mouseScrolled(MouseEvent e) {
        int clicks = e.count;
        if (clicks > 0) {
            relay.mouseWheelMovedForward(clicks);
        } else {
            relay.mouseWheelMovedBackward(clicks);
        }
    }

}
