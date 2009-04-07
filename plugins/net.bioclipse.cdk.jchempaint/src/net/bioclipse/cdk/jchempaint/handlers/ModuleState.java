package net.bioclipse.cdk.jchempaint.handlers;

import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;


public class ModuleState extends State implements IExecutableExtension {

    public final static String STATE_ID = "net.bioclipse.cdk.jchempaint.moduleState";
    public final static String PARAMETER_ID = "jcp.controller.module";
    
    public ModuleState() {

    }

    public void setInitializationData( IConfigurationElement config,
                                       String propertyName, Object data )
                                                                         throws CoreException {

        if(data instanceof String ) {
            setValue(data);
        }

    }

    @Override
    public void setValue( Object value ) {
    
        if( ! (value instanceof String))
            return;// we set only String values
        
        super.setValue( value );
    }
}
