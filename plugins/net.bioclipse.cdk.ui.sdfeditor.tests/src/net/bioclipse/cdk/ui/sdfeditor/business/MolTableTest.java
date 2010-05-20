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

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;


/**
 * @author arvid
 *
 */
public class MolTableTest {

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
}
