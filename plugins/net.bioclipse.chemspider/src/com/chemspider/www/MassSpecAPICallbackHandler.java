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
 * MassSpecAPICallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:22:40 CEST)
 */

    package com.chemspider.www;


    /**
     *  MassSpecAPICallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class MassSpecAPICallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public MassSpecAPICallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public MassSpecAPICallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getCompressedRecordsSdf method
            * override this method for handling normal response from getCompressedRecordsSdf operation
            */
           public void receiveResultgetCompressedRecordsSdf(
                    com.chemspider.www.MassSpecAPIStub.GetCompressedRecordsSdfResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCompressedRecordsSdf operation
           */
            public void receiveErrorgetCompressedRecordsSdf(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for searchByFormula2 method
            * override this method for handling normal response from searchByFormula2 operation
            */
           public void receiveResultsearchByFormula2(
                    com.chemspider.www.MassSpecAPIStub.SearchByFormula2Response result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from searchByFormula2 operation
           */
            public void receiveErrorsearchByFormula2(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for searchByMass2 method
            * override this method for handling normal response from searchByMass2 operation
            */
           public void receiveResultsearchByMass2(
                    com.chemspider.www.MassSpecAPIStub.SearchByMass2Response result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from searchByMass2 operation
           */
            public void receiveErrorsearchByMass2(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for searchByFormula method
            * override this method for handling normal response from searchByFormula operation
            */
           public void receiveResultsearchByFormula(
                    com.chemspider.www.MassSpecAPIStub.SearchByFormulaResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from searchByFormula operation
           */
            public void receiveErrorsearchByFormula(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for searchByFormulaAsync method
            * override this method for handling normal response from searchByFormulaAsync operation
            */
           public void receiveResultsearchByFormulaAsync(
                    com.chemspider.www.MassSpecAPIStub.SearchByFormulaAsyncResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from searchByFormulaAsync operation
           */
            public void receiveErrorsearchByFormulaAsync(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for searchByMassAsync method
            * override this method for handling normal response from searchByMassAsync operation
            */
           public void receiveResultsearchByMassAsync(
                    com.chemspider.www.MassSpecAPIStub.SearchByMassAsyncResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from searchByMassAsync operation
           */
            public void receiveErrorsearchByMassAsync(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getExtendedCompoundInfoArray method
            * override this method for handling normal response from getExtendedCompoundInfoArray operation
            */
           public void receiveResultgetExtendedCompoundInfoArray(
                    com.chemspider.www.MassSpecAPIStub.GetExtendedCompoundInfoArrayResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getExtendedCompoundInfoArray operation
           */
            public void receiveErrorgetExtendedCompoundInfoArray(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRecordMol method
            * override this method for handling normal response from getRecordMol operation
            */
           public void receiveResultgetRecordMol(
                    com.chemspider.www.MassSpecAPIStub.GetRecordMolResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRecordMol operation
           */
            public void receiveErrorgetRecordMol(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRecordsSdf method
            * override this method for handling normal response from getRecordsSdf operation
            */
           public void receiveResultgetRecordsSdf(
                    com.chemspider.www.MassSpecAPIStub.GetRecordsSdfResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRecordsSdf operation
           */
            public void receiveErrorgetRecordsSdf(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getExtendedCompoundInfo method
            * override this method for handling normal response from getExtendedCompoundInfo operation
            */
           public void receiveResultgetExtendedCompoundInfo(
                    com.chemspider.www.MassSpecAPIStub.GetExtendedCompoundInfoResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getExtendedCompoundInfo operation
           */
            public void receiveErrorgetExtendedCompoundInfo(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for searchByMass method
            * override this method for handling normal response from searchByMass operation
            */
           public void receiveResultsearchByMass(
                    com.chemspider.www.MassSpecAPIStub.SearchByMassResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from searchByMass operation
           */
            public void receiveErrorsearchByMass(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getDatabases method
            * override this method for handling normal response from getDatabases operation
            */
           public void receiveResultgetDatabases(
                    com.chemspider.www.MassSpecAPIStub.GetDatabasesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getDatabases operation
           */
            public void receiveErrorgetDatabases(java.lang.Exception e) {
            }
                


    }
    
