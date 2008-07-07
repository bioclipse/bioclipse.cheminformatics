package net.bioclipse.cml.managers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

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
	private static UnitListMap dictListMap = null;

	
	public String validate(String filename) {
		initDicts();
		// FIXME quick hack till the file handling is clear
		File file=new File("/home/shk3/runtime-bioclipse.product"+filename.substring(1));
		return this.validateCMLFile(file);
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
		/*if (dictListMap == null) {
			try {
				dictListMap  = new DictListMap(dictDir, true);
			} catch (IOException e) {
				throw new CMLRuntime("dictListMap could not be created "+e);
			}
		}*/
	}

    
	private String validateCMLFile(File file) {
		succeeded = true;
		StringBuffer returnString=new StringBuffer();
		CMLElement cmlElement = null;
		try {
			cmlElement = (CMLElement) new CMLBuilder().build(file).getRootElement() ;
		} catch (ParsingException e) {
			returnString.append(e);
			this.succeeded = false;
		} catch (ClassCastException ccee) {
			try{
				Element element=new CMLBuilder().build(file).getRootElement();
				namespaceThemAll(element.getChildElements());
				element.setNamespaceURI(CMLUtil.CML_NS);
				cmlElement = (CMLElement) new CMLBuilder().parseString(element.toXML());
			}catch(Exception ex){
				returnString.append(ex);
				this.succeeded = false;
			}
		} catch (Exception e) {
			returnString.append(e);
			this.succeeded = false;
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
			return(file+" is valid CML. "+returnString.toString());
		}else{
			return(file+" is not valid CML: "+returnString.toString());
		}
	}
}
