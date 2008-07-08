package net.bioclipse.cml.managers;

import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.IBioclipseManager;

import org.eclipse.core.resources.IFile;

public interface IValidateCMLManager extends IBioclipseManager{


	    /**
	     * example method
	     */
	    @Recorded
	    public String validate(IFile input);
}
