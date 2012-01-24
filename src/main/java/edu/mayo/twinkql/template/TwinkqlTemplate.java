/*
 * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
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
package edu.mayo.twinkql.template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

import edu.mayo.twinkql.context.Qname;
import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.model.Select;
import edu.mayo.twinkql.model.SparqlMap;
import edu.mayo.twinkql.result.ResultBindingProcessor;

/**
 * The Class TwinkqlTemplate.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class TwinkqlTemplate implements InitializingBean {

	private TwinkqlContext twinkqlContext;
	
	private ResultBindingProcessor resultBindingProcessor ;
	
	private Map<Qname,Select> selectMap = new HashMap<Qname,Select>();
	
	/**
	 * Instantiates a new twinkql template.
	 *
	 */
	public TwinkqlTemplate(){
		super();
	}
	
	/**
	 * Instantiates a new twinkql template.
	 *
	 * @param twinkqlContext the twinkql context
	 */
	public TwinkqlTemplate(TwinkqlContext twinkqlContext){
		this.resultBindingProcessor = new ResultBindingProcessor(twinkqlContext);
		this.twinkqlContext = twinkqlContext;
		this.initCaches();
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.twinkqlContext, "The property 'twinkqlContext' must be set!");
		this.resultBindingProcessor = new ResultBindingProcessor(this.twinkqlContext);
		this.initCaches();
	}
	
	/**
	 * Inits the caches.
	 */
	protected void initCaches(){
		for(SparqlMap map : this.twinkqlContext.getSparqlMaps()){
			
			if(map.getSparqlMapSequence() != null){
				for(Select select : map.getSparqlMapSequence().getSelect()){
					this.selectMap.put(new Qname(map.getNamespace(), select.getId()), select);
				}
			}
		}
	}
	
	/**
	 * Query for string.
	 *
	 * @param namespace the namespace
	 * @param selectId the select id
	 * @param parameters the parameters
	 * @return the string
	 */
	public String getSelectQueryString(
			String namespace, 
			String selectId, 
			Map<String,Object> parameters){
		
		Select select = this.selectMap.get(new Qname(namespace,selectId));
		
		String queryString = this.doGetSparqlQueryString(select, parameters);
		
		return queryString;
	}

	/**
	 * Do get sparql query string.
	 *
	 * @param select the select
	 * @param parameters the parameters
	 * @return the string
	 */
	protected String doGetSparqlQueryString(Select select, Map<String,Object> parameters){
		String query = select.getContent();
		
		if(!CollectionUtils.isEmpty(parameters)){
			for(Entry<String,Object> entrySet : parameters.entrySet()){
				query = query.replace(
						"#{"+entrySet.getKey()+"}", 
						entrySet.getValue().toString());
			}
		}
		
		return query;
	}
	
	/**
	 * Select for list.
	 *
	 * @param <T> the generic type
	 * @param namespace the namespace
	 * @param selectId the select id
	 * @param parameters the parameters
	 * @param requiredType the required type
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> selectForList(String namespace, String selectId, Map<String,Object> parameters, Class<T> requiredType){	
		return this.doBind(namespace, selectId, parameters, new DoBind<List<T>>(){

			public List<T> doBind(ResultSet resultSet, Qname resultMap) {
				return (List<T>) resultBindingProcessor.bindForList(resultSet, resultMap);
			}	
		});
	}
	
	/**
	 * Select for object.
	 *
	 * @param <T> the generic type
	 * @param namespace the namespace
	 * @param selectId the select id
	 * @param parameters the parameters
	 * @param requiredType the required type
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public <T> T selectForObject(String namespace, String selectId, Map<String,Object> parameters, Class<T> requiredType){	
		return this.doBind(namespace, selectId, parameters, new DoBind<T>(){

			public T doBind(ResultSet resultSet, Qname resultMap) {
				return (T) resultBindingProcessor.bindForObject(resultSet, resultMap);
			}
			
		});
	}
	
	/**
	 * The Interface DoBind.
	 *
	 * @param <T> the generic type
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	private interface DoBind<T> {
		
		/**
		 * Do bind.
		 *
		 * @param resultSet the result set
		 * @param resultMap the result map
		 * @return the t
		 */
		public T doBind(ResultSet resultSet, Qname resultMap);
	}
	
	/**
	 * Do bind.
	 *
	 * @param <T> the generic type
	 * @param namespace the namespace
	 * @param selectId the select id
	 * @param parameters the parameters
	 * @param doBind the do bind
	 * @return the t
	 */
	public <T> T doBind(String namespace, String selectId, Map<String,Object> parameters, DoBind<T> doBind){	
		Select select = this.selectMap.get(new Qname(namespace,selectId));
		
		String queryString = this.getSelectQueryString(namespace, selectId, parameters);
		
		Query query = this.doCreateQuery(queryString);
		
		QueryExecution queryExecution = 
				this.twinkqlContext.getQueryExecution(query);
		
		ResultSet resultSet = queryExecution.execSelect();
		
		Qname resultQname = Qname.toQname(select.getResultMap(), namespace);
			
		return doBind.doBind(resultSet, resultQname);
	}
	
	/**
	 * Do create query.
	 *
	 * @param queryString the query string
	 * @return the query
	 */
	protected Query doCreateQuery(String queryString){
		return QueryFactory.create(queryString);
	}
	
	public TwinkqlContext getTwinkqlContext() {
		return twinkqlContext;
	}

	public void setTwinkqlContext(TwinkqlContext twinkqlContext) {
		this.twinkqlContext = twinkqlContext;
	}

	public ResultBindingProcessor getResultBindingProcessor() {
		return resultBindingProcessor;
	}

	public void setResultBindingProcessor(
			ResultBindingProcessor resultBindingProcessor) {
		this.resultBindingProcessor = resultBindingProcessor;
	}
}
