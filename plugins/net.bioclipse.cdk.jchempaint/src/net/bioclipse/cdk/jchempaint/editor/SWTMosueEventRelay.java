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

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.openscience.cdk.controller.IMouseEventRelay;

public class SWTMosueEventRelay implements MouseListener,MouseMoveListener,Listener{
    
    Logger logger = Logger.getLogger( SWTMosueEventRelay.class );
    
	private int dragFromX = 0;
	private int dragFromY = 0;
	private boolean isDraging=false;
	private IMouseEventRelay relay;
	
	public SWTMosueEventRelay(IMouseEventRelay relay) {
		this.relay=relay;
	}
	
	public void handleEvent(Event event) {
        switch (event.type) {
        case SWT.MouseDown:
        	logger.debug( "SWT.MouseDown, should not get here" );
          break;
        case SWT.MouseMove:
        	if(isDraging){
        		relay.mouseDrag(dragFromX,dragFromY, event.x, event.y);
        		dragFromX=event.x;
        		dragFromY=event.y;
        	}else
        		relay.mouseMove(event.x, event.y);
        	break;
        case SWT.MouseUp:
        	relay.mouseClickedUp(event.x, event.y);
        	isDraging=false;
          break;
        case SWT.MouseDoubleClick:
        	relay.mouseClickedDouble(event.x, event.y);
        	break;
        case SWT.MouseEnter:
        	relay.mouseEnter(event.x, event.y);
        	break;
        case SWT.MouseExit:
        	relay.mouseExit(event.x, event.y);
        	break;
        }
      }

	public void mouseDoubleClick(MouseEvent event) {
		relay.mouseClickedDouble(event.x, event.y);		
	}

	public void mouseDown(MouseEvent event) {
		relay.mouseClickedDown(event.x, event.y);
		if( ((MouseEvent)event).button == 1) {
        dragFromX=event.x;
        dragFromY=event.y;
        isDraging=true;
    }
	}

	public void mouseUp(MouseEvent event) {
		relay.mouseClickedUp(event.x, event.y);
    	isDraging=false;
	}

	public void mouseMove(MouseEvent event) {
		if(isDraging){
    		relay.mouseDrag(dragFromX,dragFromY, event.x, event.y);
    		dragFromX=event.x;
    		dragFromY=event.y;
    	}else
    		relay.mouseMove(event.x, event.y);
	}

}
