package net.bioclipse.cdk.jchempaint.handlers;

import static org.eclipse.jface.window.Window.OK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.edit.CompositEdit;
import org.openscience.cdk.controller.edit.IEdit;
import org.openscience.cdk.controller.edit.SetSymbol;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.tools.periodictable.PeriodicTable;


public class AddOtherAtom extends AbstractJChemPaintHandler implements IHandler {

    @Override
    public Object execute( ExecutionEvent event ) throws ExecutionException {
        IChemModelRelay relay = getChemModelRelay( event );
        Collection<?> c = getSelection( event );
        
        IWorkbenchWindow window = HandlerUtil
                        .getActiveWorkbenchWindowChecked(event);
        AddAtomDialog dialog = new AddAtomDialog( window.getShell() );
        int result = dialog.open();
        if(result == OK )
            setAtoms(relay,c,dialog.getInput());
        return null;
    }
    
    private void setAtoms(IChemModelRelay relay,Collection<?> c,String o) {
        
        List<IEdit> edits = new ArrayList<IEdit>(c.size());
        for(Object element: c) {
            IAtom atom = null;
            if(element instanceof IAdaptable)
                atom = (IAtom) ((IAdaptable)element).getAdapter( IAtom.class );
            if(atom!=null)
                edits.add( SetSymbol.setSymbol( atom, o ) );
        }
        relay.execute( CompositEdit.compose( edits ) );
    }

}

class AddAtomDialog extends ApplicationWindow {

    private String input;
    
    public AddAtomDialog(Shell parentShell) {
        super( parentShell );
        setBlockOnOpen( true );
        setShellStyle( SWT.NONE );
    }
    
    @Override
    protected Control createContents( Composite parent ) {
        final Combo combo = new Combo( parent, SWT.SIMPLE );
        combo.setItems( getAtomicNumbers() );
        combo.addSelectionListener( new SelectionListener() {
            
            @Override
            public void widgetSelected( SelectionEvent e ) {
            }
            
            @Override
            public void widgetDefaultSelected( SelectionEvent e ) {
                 input = combo.getText();
                 AddAtomDialog.this.setReturnCode( OK );
                 AddAtomDialog.this.close();
            }
        } );
        return parent;
    }
    
    public String getInput() { return input;}
    
    public static String[] getAtomicNumbers() {
        List<String> candidates = new ArrayList<String>(112);
        for(int i=1;i<200;i++) {
            String symbol = PeriodicTable.getSymbol( i );
            if( symbol!=null) {
                candidates.add( symbol );
            }
        }
        return candidates.toArray( new String[candidates.size()] );
    }
    
}
