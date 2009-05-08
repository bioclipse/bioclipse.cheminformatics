package net.bioclipse.jmol.views;

import net.bioclipse.scripting.ui.views.ScriptingConsoleView;
import net.bioclipse.jmol.Activator;


public class JmolConsoleView extends ScriptingConsoleView {

    @Override
    protected String executeCommand( String command ) {
        Activator.getDefault().getJmolManager().run( command );
        return null;
    }

    @Override
    protected void waitUntilCommandFinished() {

        // TODO Auto-generated method stub
        
    }

}
