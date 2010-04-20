/* *****************************************************************************
 * Copyright (c) 2010  Ola Spjuth <ospjuth@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.pubchem.scraper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bioclipse.browser.scraper.IBrowserScraper;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.pubchem.domain.LWPubchemMolecule;

/**
 * 
 * @author ola
 *
 */
public class PubChemScraper implements IBrowserScraper {
    
    
    public PubChemScraper() {
    }

    /**
     * Check base URL
     */
    public boolean matchesURL( String url ) {

        if (url.startsWith("http://www.ncbi.nlm.nih.gov")){
            return true;
        }
        return false;
    }

    /**
     * Accept all content for now
     */
    public boolean matchesContent( String content ) {
        return true;
    }

    /**
     * Deliver different URLs to different extractors
     */
    public List<? extends IBioObject> extractObjects( String url, String content ) {

        if (url.startsWith("http://www.ncbi.nlm.nih.gov/sites/entrez?db=pccompound")){
            System.out.println("Processing PubChem page");
            return extractMultipleMolecules(content);
        }
        else if (url.startsWith( "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi" )){
            //Example
            //http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=4594
            //so extract from URL
            return extractMultipleMolecules(url);
        }
        System.out.println("Not a PubChem page.");

        return null;
    }

    private List<IMolecule> extractMultipleMolecules( String content ) {

//        Pattern slp = Pattern.compile("cid=(\\d+)\\&");
        Pattern slp = Pattern.compile("cid=(\\d+)");
        Set<Integer> cids=new HashSet<Integer>();
        Matcher slm = slp.matcher(content);
        while (slm.find()) {
            String cidstr=slm.group( 1 );
//            System.out.println("Found cid: " + cidstr);
            try{
                int cid = Integer.parseInt( cidstr );
                cids.add(cid);
            }catch (NumberFormatException e){
                System.out.println("Could not parse CID=" + cidstr);
            }

        }

        //Convert all cids to mols
        List<IMolecule> mols=new ArrayList<IMolecule>();
        for (int i : cids){
            mols.add(new LWPubchemMolecule( i ));
        }
        
        System.out.println("Found no CID mols: " + mols.size());

        return mols;
    }
    
    public static void main( String[] args ) {

        PubChemScraper p = new PubChemScraper();

        String a="   [\"LinkOut\", \"window.top.location='/sites/entrez?Cmd=ShowLinkOut&Db=pccompound&TermToSearch=4594&ordinalpos=1' \", \"\", \"\"]\n" + 
        		"                    ]\n" + 
        		"                --></script><a class=\"menulinks\" href=\"javascript:PopUpMenu2_Set(Menu4594_5);\" onMouseOut=\"PopUpMenu2_Hide();\" target=\"_self\">Other Links</a></span></td></tr><tr><td></td><td align=\"left\" valign=\"top\" colspan=\"3\"><table class=\"pcmaintext\"><tr><td width=\"120\" valign=\"top\"><a href=\"http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=4594&amp;loc=ec_rcs\" style=\"color:white\"><img border=\"0\" src=\"http://pubchem.ncbi.nlm.nih.gov/image/imgsrv.fcgi?cid=4594&amp;loc=ec_rcs\" /></a></td><td><a href=\"http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=4594&amp;loc=ec_rcs\">omeprazole; Antra; Losec ...</a><br /><span class=\"termtext\">IUPAC</span>: 6-methoxy-2-[(4-methoxy-3,5-dimethylpyridin-2-yl)methylsulfinyl]-<br />1H-benzimidazole<br /><span class=\"termtext\">MW</span>:     345.416060 g/mol | <span class=\"termtext\">MF</span>: C<sub>1</sub><sub>7</sub>H<sub>1</sub><sub>9</sub>N<sub>3</sub>O<sub>3</sub>S<br /><span class=\"termtext\">Tested in BioAssays: </span>All: <a href=\"/sites/entrez?db=pcassay&amp;dbfrom=pccompound&amp;cmd=link&amp;linkname=pccompound_pcassay&amp;IdsFromResult=4594\">440</a>, Active: <a href=\"/sites/entrez?db=pcassay&amp;dbfrom=pccompound&amp;cmd=link&amp;linkname=pccompound_pcassay_active&amp;IdsFromResult=4594\">19</a>; <a href=\"http://pubchem.ncbi.nlm.nih.gov/assay/assay.cgi?q=cids&amp;cid=4594&amp;loc=ec_rba\" title=\"Activity summary of the compound in BioAssays\">BioActivity Analysis <img src=\"http://pubchem.ncbi.nlm.nih.gov/images/pcbioactivitys.gif\" border=\"0\" title=\"BioActivity Analysis\" class=\"pcimagevalign\" /></a><br />Enzyme Inhibitors... <a href=\"http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=4594#pharmaction\">more</a><br /></td></tr></table></td></tr></table><br /></div><div id=\"main\" name=\"main\"><table cellpadding=\"3\" width=\"100%\"><tr class=\"pcmaintext\"><td nowrap=\"nowrap\" valign=\"top\" style=\"position:relative; top: -3px;\"><input name=\"EntrezSystem2.PEntrez.Pccompound.Pccompound_ResultsPanel.Pccompound_RVDocSum.uid\" sid=\"2\" type=\"checkbox\" id=\"UidCheckBox_2\" value=\"9579578\" /><b>2: </b></td><td nowrap=\"nowrap\" valign=\"top\"><a href=\"http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=9579578&amp;loc=ec_rcs\">CID: 9579578</a></td><td width=\"100%\" align=\"right\" valign=\"top\"><span><script language=\"JavaScript1.2\"><!-- \n" + 
        		"var Menu9579578_1 = [ \n" + 
        		"";

        p.extractMultipleMolecules(a);

        
    }
    

}
