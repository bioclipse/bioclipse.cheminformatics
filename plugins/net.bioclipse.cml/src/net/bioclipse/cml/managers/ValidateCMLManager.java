package net.bioclipse.cml.managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLRuntime;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLBuilder;
import org.xmlcml.cml.element.DictRefAttribute;
import org.xmlcml.cml.element.DictionaryMap;
import org.xmlcml.cml.element.MetadataNameAttribute;
import org.xmlcml.cml.element.UnitAttribute;
import org.xmlcml.cml.element.UnitListMap;


public class ValidateCMLManager implements IValidateCMLManager {

	private boolean succeeded = true;
	private List<String> errorList = new ArrayList<String>();
	private static File simpleDir = null;
	private static File unitsDir = null;
	private static File dictDir = null;
	private static DictionaryMap simpleMap = null;
	private static UnitListMap unitListMap = null;
	private static DictionaryMap dictListMap = null;
	CMLElement cmlElement = null;
	
	
	public String validate(IFile input) {
		initDicts();
		return this.validateCMLFile(input);
	}

    public String getNamespace() {
        return "cml";
    }
    
	private void namespaceThemAll(Elements elements) {
		for (int i = 0; i < elements.size(); i++) {
			Element elem = elements.get(i);
			elem.setNamespaceURI(CMLUtil.CML_NS);
			if (elem.getChildCount() != 0) {
				namespaceThemAll(elem.getChildElements());
			}
		}
	}
	
	private void initDicts() {
		if (simpleDir == null || unitsDir == null) {
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

			URL simpleURL = bundle.getEntry("/dict10/simple");
			URL unitsURL = bundle.getEntry("/dict10/units");
			URL dictURL = bundle.getEntry("/dict10/dict");
			
			try {
				simpleDir = new File(simpleURL.toURI().getPath());
				unitsDir = new File(unitsURL.toURI().getPath());
				dictDir = new File(dictURL.toURI().getPath());
				
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		if (simpleMap  == null) {
			try {
				simpleMap = new DictionaryMap(simpleDir, true);
			} catch (IOException e) {
				throw new CMLRuntime("dictionaryMap could not be created "+e);
			}
		}
		if (unitListMap == null) {
			try {
				unitListMap  = new UnitListMap(unitsDir, true);
			} catch (IOException e) {
				throw new CMLRuntime("unitListMap could not be created "+e);
			}
		}
		if (dictListMap == null) {
			try {
				dictListMap  = new DictionaryMap(dictDir, true);
			} catch (IOException e) {
				throw new CMLRuntime("dictListMap could not be created "+e);
			}
		}
	}

    
	private String validateCMLFile(IFile input) {
		succeeded = true;
		StringBuffer returnString=new StringBuffer();
		InputStream is=null;
		try {
			is=input.getContents();
			cmlElement = (CMLElement) new CMLBuilder().build(is).getRootElement() ;
		} catch (ParsingException e) {
			returnString.append(e);
			this.succeeded = false;
		} catch (ClassCastException ccee) {
			InputStream is2=null;
			try{
				is2=input.getContents();
				Element element=new CMLBuilder().build(is2).getRootElement();
				namespaceThemAll(element.getChildElements());
				element.setNamespaceURI(CMLUtil.CML_NS);
				cmlElement = (CMLElement) new CMLBuilder().parseString(element.toXML());
			}catch(Exception ex){
				returnString.append(ex);
				this.succeeded = false;
			}
			finally{
				
				try {
					is2.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			returnString.append(e);
			this.succeeded = false;
		}
		finally{
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (succeeded) {
			errorList.clear();
			errorList = new DictRefAttribute().checkAttribute(cmlElement, simpleMap);
			if (errorList.size() > 0) {
				for (String error : errorList) {
					returnString.append("warning: " + error);
				}
			}
			errorList.clear();
			errorList = new MetadataNameAttribute().checkAttribute(cmlElement, simpleMap);
			if (errorList.size() > 0) {
				for (String error : errorList) {
					returnString.append("warning: " + error);
				}
			}
			errorList.clear();
			errorList = new UnitAttribute().checkAttribute(cmlElement, unitListMap);
			if (errorList.size() > 0) {
				for (String error : errorList) {
					returnString.append("warning: " + error);
				}
			}
			return("Input is valid CML. "+returnString.toString());
		}else{
			return("Input is not valid CML: "+returnString.toString());
		}
	}

	@Override
	public boolean getSuceeded() {
		return succeeded;
	}

	@Override
	public CMLElement getCMLElement() {
		return cmlElement;
	}
}
