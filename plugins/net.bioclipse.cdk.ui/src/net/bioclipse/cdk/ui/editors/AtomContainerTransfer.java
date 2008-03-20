/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.editors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import net.bioclipse.cdk.ui.Activator;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.MDLFormat;

public class AtomContainerTransfer extends ByteArrayTransfer {
	
    private static final Logger logger = Logger.getLogger(AtomContainerTransfer.class);
	
    // TODO remove:
    /*private static final Logger logger = Activator.getLogManager()
	.getLogger(AtomContainerTransfer.class.toString());*/
	private static AtomContainerTransfer instance = new AtomContainerTransfer();
	private static final String TYPE_NAME = "AtomContainer-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);
	
	/**
	 * Returns the singleton BioResource transfer instance.
	 */
	public static AtomContainerTransfer getInstance() {
		return instance;
	}
	/**
	 * Avoid explicit instantiation
	 */
	private AtomContainerTransfer() {
	}
	protected IAtomContainer fromByteArray(byte[] bytes) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
		try {
			in.readInt();
			/* read BioResource */
			return readAtomContainer(in);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/*
	 * Method declared on Transfer.
	 */
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}
	/*
	 * Method declared on Transfer.
	 */
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}
	/*
	 * Method declared on Transfer.
	 */
	protected void javaToNative(Object object, TransferData transferData) {
		byte[] bytes = toByteArray((IAtomContainer)object);
		if (bytes != null)
			super.javaToNative(bytes, transferData);
	}
	/*
	 * Method declared on Transfer.
	 */
	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[])super.nativeToJava(transferData);
		return fromByteArray(bytes);
	}
	/**
	 * Reads and returns a single BioResource from the given stream.
	 */
	private IAtomContainer readAtomContainer(DataInputStream dataIn) throws IOException, CDKException {
		Vector<Integer> v=new Vector<Integer>();
		int i=0;
		while((i=dataIn.read())!=-1){
			v.add(new Integer(i));
		}
		byte[] bytes=new byte[v.size()];
		for(i=0;i<v.size();i++){
			bytes[i]=((Integer)v.get(i)).byteValue();
		}
		String molString=new String(bytes);
		return (IAtomContainer) new MDLReader(new StringReader(molString)).read(new org.openscience.cdk.Molecule());
		
	}
	protected byte[] toByteArray(IAtomContainer resources) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		
		byte[] bytes = null;
		
		try {
			out.writeInt(1);
			writeAtomContainer(resources, out);
			out.close();
			bytes = byteOut.toByteArray();
		} catch (IOException e) {
			//when in doubt send nothing
			e.printStackTrace();
		}
		return bytes;
	}
	
	/**
	 * Writes the given BioResource to the stream.
	 */
	@SuppressWarnings("unused")
	private void writeAtomContainer(IAtomContainer resource, DataOutputStream dataOut) throws IOException {
		Object formatClass = MDLFormat.getInstance();
		IChemFormat format = (IChemFormat) formatClass;
		IChemObjectWriter chemObjectWriterClass = null;
		try {
			chemObjectWriterClass = new MDLWriter();
		} catch (Exception e) {
			logger.error("was not able to load the Writer Class");
		} 
		ByteArrayOutputStream stream;
		String outputString = null;
		boolean success;
		try {
			stream = new ByteArrayOutputStream();
			chemObjectWriterClass.setWriter(stream);
			chemObjectWriterClass.write(resource);
			chemObjectWriterClass.close();
			outputString = stream.toString();
			stream.close();
			success = true;
			dataOut.write(stream.toByteArray());
		} catch (Exception e) {
			success = false;
			e.printStackTrace();
		}		
	}
}
