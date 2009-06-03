/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor;

import java.util.BitSet;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.openscience.cdk.fingerprint.Fingerprinter;

/**
 * @author arvid
 *
 */
public class CDKFingerPrintPropertyCalculator implements IPropertyCalculator<BitSet> {

    public BitSet calculate( ICDKMolecule molecule ) {

        Fingerprinter fp=new Fingerprinter();
        try {
            BitSet fingerprint=fp.getFingerprint(molecule.getAtomContainer());
            return fingerprint;
        } catch (Exception e) {
            LogUtils.handleException( new  BioclipseException(
                     "Could not create fingerprint: "
                    + e.getMessage()),
                    Logger.getLogger( CDKFingerPrintPropertyCalculator.class ),
                    "net.bioclipse.cdk.ui.sdfeditor");
        }
        return null;
    }

    public String getPropertyName() {

        return "net.bioclipse.cdk.fingerprint";
    }

    public BitSet parse( String value ) {
        // TODO check if this is right
        byte[] bytes = new Base64().decode( value.getBytes() );
        BitSet set = new BitSet();
        int index = 0;
        for(byte b:bytes) {
            for(int i=0;i<8;i++) {
                set.set( index++ ,(b & 0x01));
                b = (byte) (b>>1);
            }
        }
        return set;
    }

    public String toString( Object value ) {
        // TODO check if this is right
         BitSet val = (BitSet)value;
         int numOfBytes = (int) Math.ceil( val.length()/8.0);
         byte[] bytes = new byte[numOfBytes];
         for(int i=0;i<val.length();i++) {
             int b = 0;
             b = b<< 1;
             if(!val.get( i )) {
                 b = (b & 0xFE);
             }
             if(i%8 == 0) {
                 bytes[i/8] =(byte) b;
             }
         }
        return new String(new Base64().encode( bytes ));
    }

}
