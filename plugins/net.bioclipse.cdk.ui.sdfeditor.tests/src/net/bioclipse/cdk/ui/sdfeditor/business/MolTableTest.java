/* *****************************************************************************
* Copyright (c) 2010 Arvid Berg <goglepox@users.sourceforge.net
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Arvid Berg
******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import static org.junit.Assert.assertEquals;

import java.nio.CharBuffer;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;



/**
 * @author arvid
 *
 */
public class MolTableTest {

    private static Logger logger = Logger.getLogger( MolTableTest.class );

    @Test
    public void encodeCSVTest() {
        MoleculeTableManager moltable = new MoleculeTableManager();
        
        Map<String,String> testPairs = new TreeMap<String, String>();
        testPairs.put( "Value", "Value" );
        testPairs.put( "Value,Value", "\"Value,Value\"" );
        testPairs.put( "Value\nValue", "\"Value\nValue\"" );
        testPairs.put( "Value\"Value", "\"Value\"\"Value\"" );
        testPairs.put( "\"Value\"", "\"Value\"" );
        testPairs.put( "\"Val\"ue\"", "\"Val\"\"ue\"" );
        testPairs.put( " Value", "\" Value\"" );
        testPairs.put( "Value ", "\"Value \"" );
        testPairs.put( "", "\"\"" );
        testPairs.put( " ", "\" \"" );
        
        for(String test:testPairs.keySet()) {
            String result = moltable.encodeCSV(test);
            assertEquals( testPairs.get( test ), result );
        }
        
    }

    public static String bondOrderLine = "  1  2  4  0  0  0  0\r\n";
    public static String bondOrderLine2 = "  13  2  2  0  0  0  0\n";

    @Test
    public void detetectBondOrder4OfLine() {
        MoleculeTableManager moltable = new MoleculeTableManager();
        CharBuffer buffer;
        buffer = CharBuffer.wrap( bondOrderLine );
        logger.info(buffer.toString());
        System.out.println(buffer.toString());
        Assert.assertTrue( moltable.checkBondOrder( buffer ));
        buffer = CharBuffer.wrap( bondOrderLine2 );
        Assert.assertFalse( moltable.checkBondOrder( buffer ));
    }
}
