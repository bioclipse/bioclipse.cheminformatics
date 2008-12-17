/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Jonathan Alvarsson
 *     
 ******************************************************************************/
package net.bioclipse.cdk.domain;
public class Node {
    private Node next;
    private SDFElement data;
    boolean hasNext;
    public Node(SDFElement data) {
        this.data = data;
    }
    public synchronized Node next() {
        while(!hasNext){
            try {
                this.wait();
            } catch ( InterruptedException e ) {
                // TODO: handle exception
            }
        }
        return next;
    }
    public synchronized SDFElement data() {
        return data;
    }
    public synchronized void link(Node node){
        next = node;
        hasNext = true;
        this.notifyAll();
    }
}
