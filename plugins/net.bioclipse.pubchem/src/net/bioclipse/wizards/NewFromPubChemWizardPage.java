/*
 * Created on Nov 18, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.bioclipse.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author egonw
 */
public class NewFromPubChemWizardPage extends WizardPage {

	private Text txtName;
	
	private String query = "";
	
	public NewFromPubChemWizardPage() {
		super("Query PubChem");
		setTitle("Query PubChem");
		setDescription("Give a query. For example a name like 'aspirin', a CAS registry number like '50-00-0' or an InChI.");
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		txtName=new Text(container, SWT.BORDER);
		txtName.setText(query);
		txtName.setBounds(25, 70, 445, 25);
		txtName.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					checkForCompletion();
				}
			});
		
		
		final Label lblSmiles = new Label(container, SWT.NONE);
		lblSmiles.setBounds(25, 40, 210, 25);
		lblSmiles.setText("Enter query");

		setControl(container);
		checkForCompletion();

	}
	
	public static Object createObject(String className)	throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return Class.forName(className).newInstance();	
	}

	/**
	 * If page not complete, set error messages
	 */
	protected void checkForCompletion() {
		
		if (txtName.getText() == null || txtName.getText().compareTo("") == 0){
			// ok, let's wait
		} else {
			setErrorMessage(null);
			((NewFromPubChemWizard)getWizard()).molecule = txtName.getText();

			System.out.println("Found a valid name! -> " + txtName.getText());
			this.setPageComplete(true);
		}
		getWizard().getContainer().updateButtons();
	}

	public void setQuery(String query) {
		this.query = query;
		if (txtName != null) this.txtName.setText(query);
	}
	
	public String getQuery() {
		this.query = txtName.getText();
		return this.query;
	}
	
}
