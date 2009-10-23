package net.bioclipse.cdk.smartsmatching.model;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.domain.BioObject;


public class SmartsFile extends BioObject{
    
    List<SmartsWrapper> smarts;
    String name;
    
    public List<SmartsWrapper> getSmarts() {
    
        return smarts;
    }
    
    public void setSmarts( List<SmartsWrapper> smarts ) {
    
        this.smarts = smarts;
    }
    
    public String getName() {
    
        return name;
    }
    
    public void setName( String name ) {
    
        this.name = name;
    }

    public void addSmartsWrapper( SmartsWrapper sw ) {
        if (smarts==null) smarts= new ArrayList<SmartsWrapper>();
        smarts.add( sw );
    }
    
}
