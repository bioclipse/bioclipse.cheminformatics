package net.bioclipse.cdk.smartsmatching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;

import org.apache.log4j.Logger;
import org.openscience.cdk.interfaces.IAtom;


public class SmartsMatchingHelper {

    private static final Logger logger = Logger.getLogger(CDKManager.class);

    /**
     * Deliver a list of integers to highlight
     * @param o
     * @return
     */
    public static List<Integer> parseProperty( Object o ) {

        if (!( o instanceof String ))
            return Collections.emptyList();
        
        String prop=(String)o;
        
        List<Integer> ret=new ArrayList<Integer>();
        
        logger.debug( "Parsing property: " + prop );
        
        //Parse 1,2,3,8 into list of Integers
        //split up entire string by ,
        StringTokenizer tk=new StringTokenizer(prop, ",");
        while ( tk.hasMoreElements() ) {
            String token = tk.nextToken();
            Integer atomno=Integer.parseInt( token );
            ret.add( atomno );
        }
        
        return ret;
    }

    /**
     * Save the atoms to property
     * @param cdkmol
     * @param hitAtoms
     */
    public static void serializeToProperty( ICDKMolecule cdkmol,
                                            Set<IAtom> hitAtoms ) {

        String prop="";
        for (IAtom atom : hitAtoms){
            int atomno=cdkmol.getAtomContainer().getAtomNumber( atom );
            if (atomno>0)
                prop=prop+atomno+",";
        }

        if (prop.length()<=0) return;

        prop=prop.substring( 0,prop.length()-1 );

        cdkmol.getAtomContainer().getProperties().put( 
                          SmartsMatchingConstants.SMARTS_MATCH_PROPERTY, prop );
        
        logger.debug("Serialized ac prop: " + prop);
        
    }

}
