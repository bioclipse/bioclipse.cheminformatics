/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 * 
 * Contributors:
 *      Arvid Berg 
 *     
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.core.BioclipseStore;
import org.eclipse.jface.viewers.IElementComparer;
public class SDFElementComparer implements IElementComparer {
        public boolean equals( Object a, Object b ) {
            if ( a == b )
                return true;
            if(a instanceof SDFElementComparer && b instanceof SDFElement) {
                SDFElement aa = (SDFElement)a;
                SDFElement bb = (SDFElement)b;
                return aa.getResource().equals( bb.getResource() )
                            && aa.getNumber() == bb.getNumber() 
                            && aa.getPosition() == bb.getNumber()
                            && aa.getName().equals( bb.getName() );
            }
            if(a instanceof SDFElement){
                SDFElement e = (SDFElement) a;
                Object o = BioclipseStore.get(e.getResource(),e);
                return o == b;                    
            }
            if(b instanceof SDFElement){
                SDFElement e = (SDFElement) b;
                Object o = BioclipseStore.get(e.getResource(),e);
                return o == a;                    
            }           
            return (a != null && a.equals( b ));
        }
        public int hashCode( Object element ) {
            if ( element instanceof SDFElement ) {                
                SDFElement e1 = (SDFElement) element;
                //element = BioclipseStore.get(e1.getResource(),e1 );
                int var = 8;
                var = 31 * var + e1.getNumber();
                var = 31 * var                         
                     + (int) (e1.getPosition() ^ (e1.getPosition() >>> 32));
                var = 31 * var + (e1.getResource() == null ? 
                                           0 : e1.getResource().hashCode());
                return var;
            }
            return ( element == null ? 0: element.hashCode());
        }
    }
