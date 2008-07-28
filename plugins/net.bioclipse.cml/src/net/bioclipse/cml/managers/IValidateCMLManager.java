package net.bioclipse.cml.managers;

import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.IBioclipseManager;

import org.eclipse.core.resources.IFile;
import org.xmlcml.cml.base.CMLElement;

public interface IValidateCMLManager extends IBioclipseManager{


	    /**
	     * example method
	     */
	    @Recorded
	    public String validate(IFile input);
	    
	    /*
	     * After a validation, this tells if the validation was successfull
	     */
	    public boolean getSuceeded();
	    
	    /*
	     * After a validation, this contains the parsed file
	     */
	    public CMLElement getCMLElement();
}
