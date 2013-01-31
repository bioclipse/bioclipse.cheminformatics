/*******************************************************************************
 * Copyright (c) 2009 Arvid Berg <goglepox@users.sourceforge.net>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Arvid Berg
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class AtomContainerTransfer extends ByteArrayTransfer{
    Logger logger = Logger.getLogger( AtomContainerTransfer.class.getName() );
    private static AtomContainerTransfer instance = new AtomContainerTransfer();
    private static final String TYPE_NAME = "atomcontainer-tranfser-format";
    private static final int TYPEID = registerType( TYPE_NAME );

    public static AtomContainerTransfer getInstance() { return instance; }
    private AtomContainerTransfer() {}

    @Override
    protected int[] getTypeIds() {
        return new int[] {TYPEID};
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] {TYPE_NAME};
    }

    public byte[] toByteArray(IAtomContainer acs) {
        ByteArrayOutputStream bo=new ByteArrayOutputStream();

        CMLWriter writer=new CMLWriter(bo);
        try {
            writer.write(acs);
            writer.close();
            return bo.toByteArray();
        } catch (CDKException e) {
           logger.warn(  "Could not convert molecule to CML: ", e );
        } catch (IOException e) {
            logger.warn(  "Could not convert molecule to CML: ", e );
        }
        return null;
    }

    private IChemObjectBuilder getBuilder() {
        return SilentChemObjectBuilder.getInstance();
    }

    protected IAtomContainer fromByteArray(byte[] bytes) {
        CMLReader reader= new CMLReader(new ByteArrayInputStream( bytes ));
        IChemFile cFile = getBuilder().newInstance(IChemFile.class);
        try {
            cFile = (IChemFile) reader.read( cFile );
            List<IAtomContainer> acs = ChemFileManipulator.getAllAtomContainers( cFile );
            if(!acs.isEmpty())
                return acs.get( 0 );
        } catch ( CDKException e ) {
            logger.warn( "Could not read CML data",e );
        }
        return null;
    }

    @Override
    protected void javaToNative( Object object, TransferData transferData ) {
        byte[] bytes = toByteArray((IAtomContainer)object);
        if(bytes!=null)
            super.javaToNative( bytes, transferData );
    }

    @Override
    protected Object nativeToJava( TransferData transferData ) {
        byte[] bytes = (byte[]) super.nativeToJava( transferData );
        if(bytes==null) return null;
        return fromByteArray( bytes );
    }
}
