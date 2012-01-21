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

import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.model.ResultMap;
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
	
	private ResultBindingProcessor resultBindingProcessor = new ResultBindingProcessor();
	
	private Map<Qname,Select> selectMap = new HashMap<Qname,Select>();
	private Map<Qname,ResultMap> resultMap = new HashMap<Qname,ResultMap>();
	
	
	/**
	 * Instantiates a new twinkql template.
	 *
	 * @param twinkqlContext the twinkql context
	 */
	public TwinkqlTemplate(){
		super();
	}
	
	public TwinkqlTemplate(TwinkqlContext twinkqlContext){
		this.twinkqlContext = twinkqlContext;
		this.initCaches();
	}
	
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.twinkqlContext, "The property 'twinkqlContext' must be set!");
		this.initCaches();
	}
	
	protected void initCaches(){
		for(SparqlMap map : this.twinkqlContext.getSparqlMaps()){
			for(ResultMap resultMap : map.getResultMapList()){
				this.resultMap.put(
						new Qname(map.getNamespace(), resultMap.getId()), 
						resultMap);
			}
			for(Select select : map.getSelectList()){
				this.selectMap.put(new Qname(map.getNamespace(), select.getId()), select);
			}
		}
	}

	/**
	 * Query for string.
	 *
	 * @param namespace the namespace
	 * @param mapId the map id
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

	protected String doGetSparqlQueryString(Select select, Map<String,Object> parameters){
		String query = select.getString();
		
		if(!CollectionUtils.isEmpty(parameters)){
			for(Entry<String,Object> entrySet : parameters.entrySet()){
				query = query.replace(
						"#{"+entrySet.getKey()+"}", 
						entrySet.getValue().toString());
			}
		}
		
		return query;
	}
	
	public <T> List<T> selectForList(String namespace, String selectId, Map<String,Object> parameters, Class<T> requiredType){	
		
		Select select = this.selectMap.get(new Qname(namespace,selectId));
		
		String queryString = this.getSelectQueryString(namespace, selectId, parameters);
		
		Query query = this.doCreateQuery(queryString);
		
		QueryExecution queryExecution = 
				this.twinkqlContext.getQueryExecution(query);
		
		ResultSet resultSet = queryExecution.execSelect();
		
		ResultMap resultMap = this.resultMap.get(new Qname(namespace, select.getResultMap()));
		
		return (List<T>) this.resultBindingProcessor.bindToRows(resultSet, resultMap);
	}
	
	protected Query doCreateQuery(String queryString){
		return QueryFactory.create(queryString);
	}
	
	private static class Qname {
		private String namespace;
		private String localName;
		
		private Qname(String namespace, String localName){
			super();
			this.namespace = namespace;
			this.localName = localName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((localName == null) ? 0 : localName.hashCode());
			result = prime * result
					+ ((namespace == null) ? 0 : namespace.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Qname other = (Qname) obj;
			if (localName == null) {
				if (other.localName != null)
					return false;
			} else if (!localName.equals(other.localName))
				return false;
			if (namespace == null) {
				if (other.namespace != null)
					return false;
			} else if (!namespace.equals(other.namespace))
				return false;
			return true;
		}

		public String toString(){
			return "Namespace: " + this.namespace + ", LocalName: " + this.localName;
		}
	}
}
