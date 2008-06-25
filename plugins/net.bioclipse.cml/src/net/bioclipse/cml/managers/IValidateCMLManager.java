package net.bioclipse.cml.managers;

import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.IBioclipseManager;

public interface IValidateCMLManager extends IBioclipseManager{


	    /**
	     * example method
	     */
	    @Recorded
	    public void validate(String filename);
}
