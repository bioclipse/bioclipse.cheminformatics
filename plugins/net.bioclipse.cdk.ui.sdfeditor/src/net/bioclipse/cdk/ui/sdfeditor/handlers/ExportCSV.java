package net.bioclipse.cdk.ui.sdfeditor.handlers;

import java.io.File;

import net.bioclipse.cdk.ui.sdfeditor.Activator;
import net.bioclipse.cdk.ui.sdfeditor.business.IMoleculeTableManager;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.api.ResourcePathTransformer;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 * @author ola
 *
 */
public class ExportCSV extends AbstractHandler {

    Logger logger = Logger.getLogger( ExportCSV.class );

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);


		if(editorPart!=null && editorPart instanceof MultiPageMoleculesEditorPart) {

			MultiPageMoleculesEditorPart mpmep = (MultiPageMoleculesEditorPart)
			editorPart;
			MoleculesEditor editor = (MoleculesEditor) mpmep
			.getAdapter( MoleculesEditor.class );
			// FIXME there can be other models besides SDFIndexEditorModel
			if(editor!=null) {
				final IMoleculesEditorModel model = findModel( editor );
				if(model==null) {
					logger.warn( "Failed to export to CSV, model is null" );
					return null;
				}
				
				final Shell shell = HandlerUtil.getActiveShell(event);
				final String name=editor.getEditorInput().getName();
				
				//Open dialog and ask where to save
		        Display.getDefault().asyncExec( new Runnable() {
		            
		            public void run() {
		                String path = showDialog(name , shell );
		                if (path==null)
		                	return;
		                
		                logger.debug("User entered file: " + path);
		                
		                IMoleculeTableManager moltable = Activator.getDefault().getMoleculeTableManager();

		                try {
		                	File file=new File(path);
		                	if (!file.exists())
		                		file.createNewFile();
		                	
		                	IFile ifile = ResourcePathTransformer.getInstance().transform(path);

		                	moltable.saveAsCSV(model, ifile, new NullProgressMonitor());
						} catch (Exception e) {
							LogUtils.handleException(e, logger, Activator.PLUGIN_ID);
						}
		            }
		        });
			}
		}else {
			logger.warn( "Failed to export to CSV" );
		}

		return null;
	}

	private IMoleculesEditorModel findModel(MoleculesEditor editor) {
		return editor.getModel();
	}

    private String showDialog(String name, Shell shell) {
        
    	if (name==null || name.length()<=0)
    		name="moltable_export";
        String destFile = name + ".csv";
        
        FileDialog dialog = new FileDialog(shell,
                                           SWT.SAVE | SWT.SHEET);
        dialog.setFileName( destFile );
        dialog.setText("Export CSV");
        dialog.setFilterPath(System.getProperty( "user.home" ));
        dialog.setFilterIndex( 1 );
        String selectedDirectoryName = dialog.open();
        
        if ( selectedDirectoryName == null ) {
            return null;
        }

        int dot = selectedDirectoryName.lastIndexOf( "." );
        if(dot==-1)
            selectedDirectoryName = selectedDirectoryName+".csv";
        
        return selectedDirectoryName;
    }
}
