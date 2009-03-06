package net.bioclipse.cdk.jchempaint.handlers;

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class RedoHandler extends AbstractJChemPaintHandler implements IAction{
    
    private static final Logger logger = Logger.getLogger(RedoHandler.class);

    public Object execute(ExecutionEvent event) throws ExecutionException {
        getEditor(event).redo();
        getEditor(event).update();
        return null;
    }

    public void addPropertyChangeListener( IPropertyChangeListener listener ) {

        // TODO Auto-generated method stub
        
    }

    public int getAccelerator() {

        // TODO Auto-generated method stub
        return 0;
    }

    public String getActionDefinitionId() {

        // TODO Auto-generated method stub
        return null;
    }

    public String getDescription() {

        // TODO Auto-generated method stub
        return null;
    }

    public ImageDescriptor getDisabledImageDescriptor() {

        // TODO Auto-generated method stub
        return null;
    }

    public HelpListener getHelpListener() {

        // TODO Auto-generated method stub
        return null;
    }

    public ImageDescriptor getHoverImageDescriptor() {

        // TODO Auto-generated method stub
        return null;
    }

    public String getId() {

        // TODO Auto-generated method stub
        return null;
    }

    public ImageDescriptor getImageDescriptor() {

        // TODO Auto-generated method stub
        return null;
    }

    public IMenuCreator getMenuCreator() {

        // TODO Auto-generated method stub
        return null;
    }

    public int getStyle() {

        // TODO Auto-generated method stub
        return 0;
    }

    public String getText() {
        return "Redo JCP edit";
    }

    public String getToolTipText() {

        // TODO Auto-generated method stub
        return null;
    }

    public boolean isChecked() {

        // TODO Auto-generated method stub
        return false;
    }

    public void removePropertyChangeListener( IPropertyChangeListener listener ) {

        // TODO Auto-generated method stub
        
    }

    public void run() {

        // TODO Auto-generated method stub
        
    }

    public void runWithEvent( Event event ) {
        try {
            ((JChemPaintEditor)WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).redo();
            ((JChemPaintEditor)WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).update();
        } catch ( ExecutionException e ) {
            LogUtils.handleException( e, logger );
        }        
    }

    public void setAccelerator( int keycode ) {

        // TODO Auto-generated method stub
        
    }

    public void setActionDefinitionId( String id ) {

        // TODO Auto-generated method stub
        
    }

    public void setChecked( boolean checked ) {

        // TODO Auto-generated method stub
        
    }

    public void setDescription( String text ) {

        // TODO Auto-generated method stub
        
    }

    public void setDisabledImageDescriptor( ImageDescriptor newImage ) {

        // TODO Auto-generated method stub
        
    }

    public void setEnabled( boolean enabled ) {

        // TODO Auto-generated method stub
        
    }

    public void setHelpListener( HelpListener listener ) {

        // TODO Auto-generated method stub
        
    }

    public void setHoverImageDescriptor( ImageDescriptor newImage ) {

        // TODO Auto-generated method stub
        
    }

    public void setId( String id ) {

        // TODO Auto-generated method stub
        
    }

    public void setImageDescriptor( ImageDescriptor newImage ) {

        // TODO Auto-generated method stub
        
    }

    public void setMenuCreator( IMenuCreator creator ) {

        // TODO Auto-generated method stub
        
    }

    public void setText( String text ) {

        // TODO Auto-generated method stub
        
    }

    public void setToolTipText( String text ) {

        // TODO Auto-generated method stub
        
    }

}
