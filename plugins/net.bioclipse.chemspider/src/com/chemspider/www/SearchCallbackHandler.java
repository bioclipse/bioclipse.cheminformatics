/*******************************************************************************
 * Copyright (c) 2012 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
/**
 * SearchCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:22:40 CEST)
 */

    package com.chemspider.www;

    /**
     *  SearchCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class SearchCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public SearchCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public SearchCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getRecordDetails method
            * override this method for handling normal response from getRecordDetails operation
            */
           public void receiveResultgetRecordDetails(
                    com.chemspider.www.SearchStub.GetRecordDetailsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRecordDetails operation
           */
            public void receiveErrorgetRecordDetails(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for predictedPropertiesSearch method
            * override this method for handling normal response from predictedPropertiesSearch operation
            */
           public void receiveResultpredictedPropertiesSearch(
                    com.chemspider.www.SearchStub.PredictedPropertiesSearchResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from predictedPropertiesSearch operation
           */
            public void receiveErrorpredictedPropertiesSearch(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for asyncSimpleSearch method
            * override this method for handling normal response from asyncSimpleSearch operation
            */
           public void receiveResultasyncSimpleSearch(
                    com.chemspider.www.SearchStub.AsyncSimpleSearchResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from asyncSimpleSearch operation
           */
            public void receiveErrorasyncSimpleSearch(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getCompoundInfo method
            * override this method for handling normal response from getCompoundInfo operation
            */
           public void receiveResultgetCompoundInfo(
                    com.chemspider.www.SearchStub.GetCompoundInfoResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCompoundInfo operation
           */
            public void receiveErrorgetCompoundInfo(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRecordImage method
            * override this method for handling normal response from getRecordImage operation
            */
           public void receiveResultgetRecordImage(
                    com.chemspider.www.SearchStub.GetRecordImageResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRecordImage operation
           */
            public void receiveErrorgetRecordImage(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for mol2CSID method
            * override this method for handling normal response from mol2CSID operation
            */
           public void receiveResultmol2CSID(
                    com.chemspider.www.SearchStub.Mol2CSIDResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from mol2CSID operation
           */
            public void receiveErrormol2CSID(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for structureSearch method
            * override this method for handling normal response from structureSearch operation
            */
           public void receiveResultstructureSearch(
                    com.chemspider.www.SearchStub.StructureSearchResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from structureSearch operation
           */
            public void receiveErrorstructureSearch(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getDataSliceCompounds method
            * override this method for handling normal response from getDataSliceCompounds operation
            */
           public void receiveResultgetDataSliceCompounds(
                    com.chemspider.www.SearchStub.GetDataSliceCompoundsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getDataSliceCompounds operation
           */
            public void receiveErrorgetDataSliceCompounds(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for intrinsicPropertiesSearch method
            * override this method for handling normal response from intrinsicPropertiesSearch operation
            */
           public void receiveResultintrinsicPropertiesSearch(
                    com.chemspider.www.SearchStub.IntrinsicPropertiesSearchResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from intrinsicPropertiesSearch operation
           */
            public void receiveErrorintrinsicPropertiesSearch(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAsyncSearchStatus method
            * override this method for handling normal response from getAsyncSearchStatus operation
            */
           public void receiveResultgetAsyncSearchStatus(
                    com.chemspider.www.SearchStub.GetAsyncSearchStatusResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAsyncSearchStatus operation
           */
            public void receiveErrorgetAsyncSearchStatus(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for similaritySearch method
            * override this method for handling normal response from similaritySearch operation
            */
           public void receiveResultsimilaritySearch(
                    com.chemspider.www.SearchStub.SimilaritySearchResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from similaritySearch operation
           */
            public void receiveErrorsimilaritySearch(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for simpleSearch method
            * override this method for handling normal response from simpleSearch operation
            */
           public void receiveResultsimpleSearch(
                    com.chemspider.www.SearchStub.SimpleSearchResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from simpleSearch operation
           */
            public void receiveErrorsimpleSearch(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for substructureSearch method
            * override this method for handling normal response from substructureSearch operation
            */
           public void receiveResultsubstructureSearch(
                    com.chemspider.www.SearchStub.SubstructureSearchResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from substructureSearch operation
           */
            public void receiveErrorsubstructureSearch(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getCompoundThumbnail method
            * override this method for handling normal response from getCompoundThumbnail operation
            */
           public void receiveResultgetCompoundThumbnail(
                    com.chemspider.www.SearchStub.GetCompoundThumbnailResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCompoundThumbnail operation
           */
            public void receiveErrorgetCompoundThumbnail(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for cSID2ExtRefs method
            * override this method for handling normal response from cSID2ExtRefs operation
            */
           public void receiveResultcSID2ExtRefs(
                    com.chemspider.www.SearchStub.CSID2ExtRefsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from cSID2ExtRefs operation
           */
            public void receiveErrorcSID2ExtRefs(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for lassoSearch method
            * override this method for handling normal response from lassoSearch operation
            */
           public void receiveResultlassoSearch(
                    com.chemspider.www.SearchStub.LassoSearchResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from lassoSearch operation
           */
            public void receiveErrorlassoSearch(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for elementsSearch method
            * override this method for handling normal response from elementsSearch operation
            */
           public void receiveResultelementsSearch(
                    com.chemspider.www.SearchStub.ElementsSearchResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from elementsSearch operation
           */
            public void receiveErrorelementsSearch(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAsyncSearchResult method
            * override this method for handling normal response from getAsyncSearchResult operation
            */
           public void receiveResultgetAsyncSearchResult(
                    com.chemspider.www.SearchStub.GetAsyncSearchResultResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAsyncSearchResult operation
           */
            public void receiveErrorgetAsyncSearchResult(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for simpleSearch2IdList method
            * override this method for handling normal response from simpleSearch2IdList operation
            */
           public void receiveResultsimpleSearch2IdList(
                    com.chemspider.www.SearchStub.SimpleSearch2IdListResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from simpleSearch2IdList operation
           */
            public void receiveErrorsimpleSearch2IdList(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for molAndDS2CSID method
            * override this method for handling normal response from molAndDS2CSID operation
            */
           public void receiveResultmolAndDS2CSID(
                    com.chemspider.www.SearchStub.MolAndDS2CSIDResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from molAndDS2CSID operation
           */
            public void receiveErrormolAndDS2CSID(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAsyncSearchResultPart method
            * override this method for handling normal response from getAsyncSearchResultPart operation
            */
           public void receiveResultgetAsyncSearchResultPart(
                    com.chemspider.www.SearchStub.GetAsyncSearchResultPartResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAsyncSearchResultPart operation
           */
            public void receiveErrorgetAsyncSearchResultPart(java.lang.Exception e) {
            }
                


    }
    
