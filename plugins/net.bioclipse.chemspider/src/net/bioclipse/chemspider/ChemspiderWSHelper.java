/*******************************************************************************
 * Copyright (c) 2012 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.chemspider;

import java.rmi.RemoteException;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bioclipse.core.business.BioclipseException;

import com.chemspider.www.MassSpecAPIStub;
import com.chemspider.www.MassSpecAPIStub.ArrayOfInt;
import com.chemspider.www.MassSpecAPIStub.ExtendedCompoundInfo;
import com.chemspider.www.MassSpecAPIStub.GetExtendedCompoundInfoArray;
import com.chemspider.www.MassSpecAPIStub.GetExtendedCompoundInfoArrayResponse;
import com.chemspider.www.SearchStub;
import com.chemspider.www.SearchStub.CommonSearchOptions;
import com.chemspider.www.SearchStub.EComplexity;
import com.chemspider.www.SearchStub.EIsotopic;
import com.chemspider.www.SearchStub.EMatchType;
import com.chemspider.www.SearchStub.ERequestStatus;
import com.chemspider.www.SearchStub.ESimilarityType;
import com.chemspider.www.SearchStub.ExactStructureSearchOptions;
import com.chemspider.www.SearchStub.GetAsyncSearchResult;
import com.chemspider.www.SearchStub.GetAsyncSearchResultResponse;
import com.chemspider.www.SearchStub.GetAsyncSearchStatus;
import com.chemspider.www.SearchStub.GetAsyncSearchStatusResponse;
import com.chemspider.www.SearchStub.SimilaritySearch;
import com.chemspider.www.SearchStub.SimilaritySearchOptions;
import com.chemspider.www.SearchStub.SimilaritySearchResponse;
import com.chemspider.www.SearchStub.SimpleSearch;
import com.chemspider.www.SearchStub.SimpleSearchResponse;
import com.chemspider.www.SearchStub.StructureSearch;
import com.chemspider.www.SearchStub.StructureSearchResponse;
import com.chemspider.www.SearchStub.SubstructureSearch;
import com.chemspider.www.SearchStub.SubstructureSearchOptions;

/**
 * Helper methods to access Chemspider via SOAP12 service.
 * 
 * @author Ola Spjuth
 *
 */
public class ChemspiderWSHelper {

	private static final Logger logger = LoggerFactory.getLogger(ChemspiderWSHelper.class);

	//Get token and server endpoint from preference
	public static final String endpoint = Activator.getDefault().
					getPreferenceStore().getString(Activator.PREF_SERVER_ENDPOINT);

	public static CommonSearchOptions copt;

	static{
		//Initialize common params
		copt = new CommonSearchOptions();
		copt.setComplexity(EComplexity.Any);
		copt.setIsotopic(EIsotopic.Any);
	}

	public static int[] simpleSearch(String query, String token) throws RemoteException {
		final SearchStub thisSearchStub = new SearchStub(endpoint);
		SimpleSearch SimpleSearchInput = new SimpleSearch();
		SimpleSearchInput.setQuery(query);
		SimpleSearchInput.setToken(token);

		final SimpleSearchResponse thisSimpleSearchResponse = thisSearchStub.simpleSearch(SimpleSearchInput);
		return thisSimpleSearchResponse.getSimpleSearchResult().get_int();
	}


	public static String exactSearch(String mol, String token) throws RemoteException, BioclipseException {

		if (token==null || token.length()<=0){
			throw new BioclipseException("No security token set. Please set this in Bioclipse Preferences.");
		}

		final SearchStub searchStub = new SearchStub(endpoint);
		StructureSearch search = new com.chemspider.www.SearchStub.StructureSearch();

		ExactStructureSearchOptions opt = new com.chemspider.www.SearchStub.ExactStructureSearchOptions();
		opt.setMolecule(mol);
		opt.setMatchType(EMatchType.ExactMatch);

		search.setOptions(opt);
		search.setCommonOptions(copt);
		search.setToken(token);

		StructureSearchResponse response=searchStub.structureSearch(search);
		String rid = response.getStructureSearchResult();
		logger.debug("Exact search got request id: " + rid);
		return rid;

	}


	public static String similaritySearch(String mol, float similarity, String token) throws RemoteException, BioclipseException {
		
		if (token==null || token.length()<=0){
			throw new BioclipseException("No security token set. Please set this in Bioclipse Preferences.");
		}

		final SearchStub searchStub = new SearchStub(endpoint);
		SimilaritySearch search = new SimilaritySearch();

		SimilaritySearchOptions opt = new SimilaritySearchOptions();
		opt.setMolecule(mol);
		opt.setSimilarityType(ESimilarityType.Tanimoto);
		opt.setThreshold(similarity);

		logger.debug("Similarity search for " + mol + " with tanimoto " + similarity);

		//			SearchScopeOptions scope = new SearchScopeOptions();
		//			similaritySearch.setScopeOptions(scope);

		search.setOptions(opt);
		search.setCommonOptions(copt);
		search.setToken(token);

		SimilaritySearchResponse response=searchStub.similaritySearch(search);
		String rid = response.getSimilaritySearchResult();
		logger.debug("Similarity search got request id: " + rid);
		return rid;
	}


	public static String substructureSearch(String mol, String token) throws RemoteException, BioclipseException {

		if (token==null || token.length()<=0){
			throw new BioclipseException("No security token set. Please set this in Bioclipse Preferences.");
		}

		
		final SearchStub searchStub = new SearchStub(endpoint);
		SubstructureSearch search = new com.chemspider.www.SearchStub.SubstructureSearch();

		SubstructureSearchOptions opt = new com.chemspider.www.SearchStub.SubstructureSearchOptions();
		opt.setMolecule(mol);
		opt.setMatchTautomers(false);

		//			SearchScopeOptions scope = new SearchScopeOptions();
		//			similaritySearch.setScopeOptions(scope);

		search.setOptions(opt);
		search.setCommonOptions(copt);
		search.setToken(token);

		//Do search and get requestID back
		String rid = searchStub.substructureSearch(search).getSubstructureSearchResult();
		logger.debug("Substructure search got request id: " + rid);
		return rid;

	}



	public static String getAsyncSearchStatus(String rid, String token) throws RemoteException {
		final SearchStub thisSearchStub = new SearchStub(endpoint);
		GetAsyncSearchStatus GetAsyncSearchStatusInput = new GetAsyncSearchStatus();
		GetAsyncSearchStatusInput.setRid(rid);
		GetAsyncSearchStatusInput.setToken(token);
		final GetAsyncSearchStatusResponse thisGetAsyncSearchStatusResponse = thisSearchStub.getAsyncSearchStatus(GetAsyncSearchStatusInput);
		return thisGetAsyncSearchStatusResponse.getGetAsyncSearchStatusResult().getValue();
	}



	public static int[] getAsyncSearchResults(String rid, String token) throws RemoteException {
		final SearchStub thisSearchStub = new SearchStub(endpoint);
		GetAsyncSearchResult GetAsyncSearchResultInput = new GetAsyncSearchResult();
		GetAsyncSearchResultInput.setRid(rid);
		GetAsyncSearchResultInput.setToken(token);

		logger.debug("Retrieving results with RID: " + rid );
		GetAsyncSearchResultResponse thisGetAsyncSearchResultResponse = thisSearchStub.getAsyncSearchResult(GetAsyncSearchResultInput);
		int[] res = thisGetAsyncSearchResultResponse.getGetAsyncSearchResultResult().get_int();
		logger.debug("Results with RID=" + rid + ": " + Arrays.toString(res));

		return res;
	}



	public static int[] pollAndReturnResults(String rid, String token, IProgressMonitor monitor) throws BioclipseException, RemoteException {

		String status = getAsyncSearchStatus(rid, token);
		logger.debug("Polling for results with RID: " + rid );
		int i=0;
		while(!ERequestStatus.ResultReady.getValue().equals(status)){
			i++;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			
			if (monitor.isCanceled())
				throw new BioclipseException("Cancelled");

			status = getAsyncSearchStatus(rid, token);

			if (i%10==0)//output each 5th sek
				logger.debug("Status for RID=" + rid + ": " + status);

			//			System.out.println("   Status: " + status);
			if (ERequestStatus.Failed.getValue().equals(status))
				throw new BioclipseException("Failed");
		}

		System.out.println("Retrieving results...");
		return getAsyncSearchResults(rid, token);
	}

	public static ExtendedCompoundInfo[] getExtendedInfo(int[] rids, String token) throws RemoteException {

		//We need the MassSpec API for this
		final MassSpecAPIStub searchStub = new MassSpecAPIStub();

		ArrayOfInt intArray = new ArrayOfInt();
		intArray.set_int(rids);

		GetExtendedCompoundInfoArray infoArray = new com.chemspider.www.MassSpecAPIStub.GetExtendedCompoundInfoArray();
		infoArray.setCSIDs(intArray);
		infoArray.setToken(token);

		//Invoke service
		GetExtendedCompoundInfoArrayResponse result = searchStub.getExtendedCompoundInfoArray(infoArray);
		return result.getGetExtendedCompoundInfoArrayResult().getExtendedCompoundInfo();
	}


}
