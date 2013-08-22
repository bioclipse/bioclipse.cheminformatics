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

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/**
 * @author arvid
 *
 */
public class CDKFingerPrintPropertyCalculator implements IPropertyCalculator<BitSet> {

    public BitSet calculate( ICDKMolecule molecule ) {

        Fingerprinter fp=new Fingerprinter();
        try {
            IAtomContainer ac = SilentChemObjectBuilder.getInstance().newInstance(IAtomContainer.class, molecule.getAtomContainer());
            IBitFingerprint fingerprint = fp.getBitFingerprint( ac );
            return fingerprint.asBitSet();
        } catch (Throwable e) {
        	Logger.getLogger( CDKFingerPrintPropertyCalculator.class ).warn(
                     "Could not create fingerprint: "
                    + e.getMessage());
            return null;
        }
    }

    public String getPropertyName() {

        return "CDK Fingerprint";
    }

    public BitSet parse( String value ) {
        // TODO check if this is right
        byte[] bytes = new Base64().decode( value.getBytes() );
        BitSet set = new BitSet(1024);
        for(int i=0;i<bytes.length*8;i++) {
            if( (bytes[bytes.length-i/8-1] & (1<<(i%8))) > 0) {
                set.set( i );
            }
        }
        return set;
    }

    public String toString( Object value ) {
        if(value instanceof String) return (String)value;
        // TODO check if this is right
         BitSet val = (BitSet)value;
         byte[] bytes = new byte[val.length()/8+1];
         for(int i=0;i<val.length();i++) {
             if(val.get( i )) {
                 bytes[bytes.length-i/8-1] |= 1 <<(i%8);
             }
         }
        return new String(new Base64().encode( bytes ));
    }

}
