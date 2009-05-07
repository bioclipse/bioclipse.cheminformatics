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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.openscience.cdk.controller.IMouseEventRelay;

public class SWTMouseEventRelay implements  Listener {

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
            case SWT.MouseDoubleClick:
                mouseDoubleClick( event );
                break;
            case SWT.MouseDown:
                mouseDown( event );
                break;
            case SWT.MouseUp:
                mouseUp(event );
                break;
            case SWT.MouseMove:
                mouseMove( event );
                break;
            case SWT.MouseWheel:
                mouseScrolled( event );
                break;
            default:
                logger.debug( "Event("+ event.type + ") not supportet" );
        }
    }

    public void mouseDoubleClick(Event event) {
        relay.mouseClickedDouble(event.x, event.y);
    }

    public void mouseDown(Event event) {

        if(!isMenuClick( event )) {
            relay.mouseClickedDown(event.x, event.y);
            dragFromX=event.x;
            dragFromY=event.y;
            isDragging=true;
        }
    }

    public void mouseUp( Event event ) {

        if( !isMenuClick( event )){
            relay.mouseClickedUp( event.x, event.y );
        }
        isDragging = false;
    }

    private boolean isMenuClick(Event event) {
        return (checkButton( event, 1 ) && checkMask( event, SWT.CTRL ))
               || checkButton( event, 3 );
    }

    private boolean checkButton(Event event,int mouseButton) {
        return event.button == mouseButton;
    }

    private boolean checkMask(Event event,int keyMask) {
        return (event.stateMask & keyMask) !=0;
    }

    private boolean checkState(Event event,int mouseButton,int key) {
        return event.button == mouseButton && (event.stateMask & key)!=0;
    }

    public void mouseMove(Event event) {
        if(isDragging){

            if(checkState( event, 0, SWT.SHIFT )) {
                int dx = event.x-dragFromX;
                if(dx<0)
                    relay.mouseWheelMovedBackward( 0 );
                else if(dx>0)
                    relay.mouseWheelMovedForward( 0 );

            }else
                relay.mouseDrag(dragFromX,dragFromY, event.x, event.y);
            dragFromX=event.x;
            dragFromY=event.y;
        }else
            relay.mouseMove(event.x, event.y);
    }

    public void mouseScrolled(Event e) {

        int clicks = e.count;
        if (clicks > 0) {
            relay.mouseWheelMovedBackward(0);
        } else if(clicks<0){
            relay.mouseWheelMovedForward(0);
        }
        e.doit = false;
    }

}
