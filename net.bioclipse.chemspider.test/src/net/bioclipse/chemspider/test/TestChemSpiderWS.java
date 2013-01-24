/*******************************************************************************
 * Copyright (c) 2012 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.chemspider.test;

import java.rmi.RemoteException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.chemspider.www.SearchStub;
import com.chemspider.www.SearchStub.CommonSearchOptions;
import com.chemspider.www.SearchStub.EComplexity;
import com.chemspider.www.SearchStub.EIsotopic;
import com.chemspider.www.SearchStub.EMatchType;
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
 * 
 * @author ola
 *
 */
public class TestChemSpiderWS {

	private static final Logger LOG = Logger.getLogger(TestChemSpiderWS.class.getName());

	private static String ChemSpiderToken = "ENTER_YOUR_KEY_HERE";
	private static String endpoint = "http://cs.dev.rsc-us.org/Search.asmx";
	private static String almostparacetamol = "C(=O)Nc1ccc(cc1)O";
	private static String paracetamol = "CC(=O)Nc1ccc(cc1)O";

	private static CommonSearchOptions copt;


	public static void main(String[] args) {
		
		//Initialize common params
		copt = new CommonSearchOptions();
		copt.setComplexity(EComplexity.Any);
		copt.setIsotopic(EIsotopic.Any);


		int[] results;
		String rid;

		//=======================
		//SimpleSearch by name Taxol
		//=======================
		System.out.println("SimpleSearch by name Taxol");
		try {
			results= simpleSearch("taxol", ChemSpiderToken);
			System.out.println("Got "+results.length +" compound(s) with CSID:" + Arrays.toString(results));
		} catch (RemoteException e2) {
			e2.printStackTrace();
		}

		//=======================
		//SimilaritySearch for similar to AlmostParacetamol
		//=======================
		System.out.println("\nSimilaritySearch for similar to AlmostParacetamol");
		try {
			rid = similaritySearch(almostparacetamol, 0.9f, ChemSpiderToken);
			results = pollAndReturnResults(rid, ChemSpiderToken);

			if (results!=null)
				System.out.println("Got "+results.length +" compound(s) with CSID:" + Arrays.toString(results));
			else
				System.out.println("Results empty");

		} catch (RemoteException e1) {
			System.out.println(e1.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		//=======================
		//SubstructureSearch for AlmostParacetamol 
		//=======================
		System.out.println("\nSubstructureSearch for AlmostParacetamol");
		
		try {
			rid = substructureSearch(almostparacetamol, ChemSpiderToken);
			results = pollAndReturnResults(rid, ChemSpiderToken);
			
			if (results!=null)
				System.out.println("Got "+results.length +" compound(s) with CSID:" + Arrays.toString(results));
			else
				System.out.println("Results empty");

		} catch (RemoteException e1) {
			System.out.println(e1.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		//=======================
		//ExactMatch for Paracetamol 
		//=======================
		System.out.println("\nExactSearch for Paracetamol");
		try {
			rid = exactSearch(paracetamol, ChemSpiderToken);
			results = pollAndReturnResults(rid, ChemSpiderToken);
			System.out.println("Exact match returned: " + Arrays.toString(results));
		} catch (RemoteException e1) {
			System.out.println(e1.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public static int[] simpleSearch(String query, String token) throws RemoteException {
		final SearchStub thisSearchStub = new SearchStub(endpoint);
		SimpleSearch SimpleSearchInput = new SimpleSearch();
		SimpleSearchInput.setQuery(query);
		SimpleSearchInput.setToken(token);

		final SimpleSearchResponse thisSimpleSearchResponse = thisSearchStub.simpleSearch(SimpleSearchInput);
		return thisSimpleSearchResponse.getSimpleSearchResult().get_int();
	}
	

	public static String exactSearch(String mol, String token) throws RemoteException {

			final SearchStub searchStub = new SearchStub(endpoint);
			StructureSearch search = new com.chemspider.www.SearchStub.StructureSearch();

			ExactStructureSearchOptions opt = new com.chemspider.www.SearchStub.ExactStructureSearchOptions();
			opt.setMolecule(mol);
			opt.setMatchType(EMatchType.ExactMatch);
			
			search.setOptions(opt);
			search.setCommonOptions(copt);
			search.setToken(token);

			StructureSearchResponse response=searchStub.structureSearch(search);
			return response.getStructureSearchResult();
			
	}
	
	
	public static String similaritySearch(String mol, float similarity, String token) throws RemoteException {

		final SearchStub searchStub = new SearchStub(endpoint);
			SimilaritySearch search = new SimilaritySearch();

			SimilaritySearchOptions opt = new SimilaritySearchOptions();
			opt.setMolecule(mol);
			opt.setSimilarityType(ESimilarityType.Tanimoto);
			opt.setThreshold(similarity);
			
			System.out.println("  -- Similarity search for " + mol + " with tanimoto " + similarity);
			
//			SearchScopeOptions scope = new SearchScopeOptions();
//			similaritySearch.setScopeOptions(scope);
			
			search.setOptions(opt);
			search.setCommonOptions(copt);
			search.setToken(token);

			SimilaritySearchResponse response=searchStub.similaritySearch(search);
			String rid = response.getSimilaritySearchResult();
			System.out.println("  -- got RID: " + rid);
			return rid;
	}


	public static String substructureSearch(String mol, String token) throws RemoteException {

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
			return searchStub.substructureSearch(search).getSubstructureSearchResult();
			
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
			
			System.out.println("  -- Retrieving results with RID: " + rid );
			GetAsyncSearchResultResponse thisGetAsyncSearchResultResponse = thisSearchStub.getAsyncSearchResult(GetAsyncSearchResultInput);
			int[] res = thisGetAsyncSearchResultResponse.getGetAsyncSearchResultResult().get_int();
			System.out.println("  -- Results:" + Arrays.toString(res));

			return res;
	}

	
	
	public static int[] pollAndReturnResults(String rid, String token) throws Exception {
		
		String status = getAsyncSearchStatus(rid, token);
		System.out.println("  -- Polling for results with RID: " + rid );
		while(!"ResultReady".equals(status)){
			Thread.sleep(1000);
			status = getAsyncSearchStatus(rid, token);
//			System.out.print(".");
			System.out.println("   Status: " + status);
			if ("Failed".equals(status))
				throw new Exception("Failed");
		}
		
		System.out.println("Retrieving results...");
		return getAsyncSearchResults(rid, token);
	}

}
