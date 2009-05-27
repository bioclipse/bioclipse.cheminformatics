/*******************************************************************************
 * Copyright (c) 2008  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.wizards;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.editors.text.NonExistingFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Creates a new molecule and opens it in JChemPaint.
 * 
 * @author egonw
 */
public class NewMoleculeWizard extends Wizard implements INewWizard {

	public static final String WIZARD_ID =
		"net.bioclipse.cdk.jchempaint.wizards.NewMoleculeWizard"; //$NON-NLS-1$
	
	public static String newline = System.getProperty("line.separator");
	
	private static final String FILE_CONTENT =
		"<molecule xmlns=\"http://www.xml-cml.org/schema\">" + newline +
		"  <atomArray>" + newline +
        "  </atomArray>" + newline +
		"  <bondArray>" + newline +
        "  </bondArray>" + newline +
		"</molecule>" + newline;
	
    /**
     * Creates a wizard for creating a new file resource in the workspace.
     */
    public NewMoleculeWizard() {
        super();
    }

    public void addPages() {
    }

    public boolean canFinish() {
        return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        setWindowTitle("New Molecule");
        setNeedsProgressMonitor(true);
    }

    public boolean performFinish() {
      //Open editor with content (String) as content

        IFileStore fileStore= queryFileStore();
        IEditorInput input= createEditorInput(fileStore);
        String editorId= getEditorId(fileStore);
        IWorkbenchPage page= PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();
        try {
            IEditorPart editor=page.openEditor(input, editorId);
            if (editor instanceof TextEditor) {
                TextEditor ted = (TextEditor) editor;
                IDocumentProvider pr=ted.getDocumentProvider();
                IDocument doc=pr.getDocument(input);
                //                String currentContent=doc.get();
                doc.set(FILE_CONTENT);
            }

        } catch (PartInitException e) {
//            LogUtils.debugTrace(logger, e);
            e.printStackTrace();
        }
        return true;
    }

    private IFileStore queryFileStore() {
        IPath stateLocation= EditorsPlugin.getDefault().getStateLocation();
        IPath path= stateLocation.append("/_" + new Object().hashCode()); //$NON-NLS-1$
        return EFS.getLocalFileSystem().getStore(path);
    }

    private String getEditorId(IFileStore fileStore) {
        return "net.bioclipse.cdk.ui.editors.jchempaint";
    }

    private IEditorInput createEditorInput(IFileStore fileStore) {
        return new NonExistingFileEditorInput(fileStore, "New script");
    }
}
