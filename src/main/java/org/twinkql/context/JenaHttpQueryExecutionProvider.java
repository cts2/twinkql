/*
 * Copyright: (c) 2004-2012 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.twinkql.context;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * A QueryExecutionProvider that connects to a HTTP SPARQL Endpoint.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class JenaHttpQueryExecutionProvider 
	implements QueryExecutionProvider, InitializingBean {
	
	private String sparqlEndpointUrl;
	
	/**
	 * Instantiates a new jena http query execution provider.
	 */
	public JenaHttpQueryExecutionProvider(){
		super();
	}
	
	/**
	 * Instantiates a new jena http query execution provider.
	 *
	 * @param sparqlEndpointUrl the sparql endpoint url
	 */
	public JenaHttpQueryExecutionProvider(String sparqlEndpointUrl){
		this.sparqlEndpointUrl = sparqlEndpointUrl;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.isTrue(this.sparqlEndpointUrl != null, 
			"A SPARQL Endpoint URL is required.");
	}

	/* (non-Javadoc)
	 * @see edu.mayo.twinkql.context.QueryExecutionProvider#provideQueryExecution(java.lang.String)
	 */
	public QueryExecution provideQueryExecution(String query) {
		QueryEngineHTTP qexec = new QueryEngineHTTP(
				this.sparqlEndpointUrl, query);

		return this.decorateQueryEngineHTTP(qexec);
	}
	
	/**
	 * Subclasses can decorate the QueryEngineHTTP to add extra
	 * parameters, etc.
	 *
	 * @param queryEngine the query engine
	 * @return the query engine http
	 */
	protected QueryEngineHTTP decorateQueryEngineHTTP(QueryEngineHTTP queryEngine){
		return queryEngine;
	}
}
