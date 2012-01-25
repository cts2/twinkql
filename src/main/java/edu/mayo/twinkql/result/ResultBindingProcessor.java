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
package edu.mayo.twinkql.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.mayo.twinkql.context.Qname;
import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.model.ResultMap;
import edu.mayo.twinkql.model.RowMap;
import edu.mayo.twinkql.model.SparqlMap;
import edu.mayo.twinkql.model.TripleMap;
import edu.mayo.twinkql.model.types.BindingPart;
import edu.mayo.twinkql.result.beans.PropertySetter;
import edu.mayo.twinkql.result.callback.AfterResultBinding;
import edu.mayo.twinkql.result.callback.CallbackInstantiator;

/**
 * The Class ResultBindingProcessor.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class ResultBindingProcessor {

	private Map<Qname, List<ResultMap>> resultMap = new HashMap<Qname, List<ResultMap>>();

	private PropertySetter propertySetter = new PropertySetter();
	
	private CallbackInstantiator callbackInstantiator;

	private TwinkqlContext twinkqlContext;

	/**
	 * Instantiates a new result binding processor.
	 *
	 * @param twinkqlContext the twinkql context
	 */
	public ResultBindingProcessor(TwinkqlContext twinkqlContext) {
		this.twinkqlContext = twinkqlContext;
		this.callbackInstantiator = new CallbackInstantiator(twinkqlContext);
		this.initCaches();
	}

	/**
	 * Inits the caches.
	 */
	protected void initCaches() {
		Map<Qname, ResultMap> results = new HashMap<Qname, ResultMap>();

		for (SparqlMap map : this.twinkqlContext.getSparqlMaps()) {
			for (ResultMap resultMap : map.getResultMap()) {
				results.put(new Qname(map.getNamespace(), resultMap.getId()),
						resultMap);
			}
		}

		for (Entry<Qname, ResultMap> result : results.entrySet()) {
			List<ResultMap> recursiveResult = new ArrayList<ResultMap>();
			recursiveResult.add(result.getValue());
			
			List<ResultMap> resultChain = this.getExtendedResultMap(recursiveResult, results);

			this.resultMap.put(result.getKey(), resultChain);
		}
	}

	/**
	 * Gets the extended result map.
	 *
	 * @param resultChain the result chain
	 * @param allResults the all results
	 * @return the extended result map
	 */
	protected List<ResultMap> getExtendedResultMap(List<ResultMap> resultChain,
			Map<Qname, ResultMap> allResults) {
		ResultMap lastResult = resultChain.get(resultChain.size() - 1);

		if (StringUtils.isBlank(lastResult.getExtends())) {
			return resultChain;
		} else {
			Qname extendsResult = Qname.toQname(lastResult.getExtends());
			ResultMap extendedResult = allResults.get(extendsResult);

			resultChain.add(extendedResult);

			return this.getExtendedResultMap(resultChain, allResults);
		}
	}

	/**
	 * Bind for list.
	 *
	 * @param resultSet the result set
	 * @param resultMap the result map
	 * @return the list
	 */
	public List<Object> bindForList(ResultSet resultSet, Qname resultMap) {
		List<ResultMap> resultMaps = this.resultMap.get(resultMap);
		
		Assert.isTrue(! CollectionUtils.isEmpty(resultMaps));
		
		return this.bindToRows(resultSet, resultMaps);

	}
	
	/**
	 * Bind for object.
	 *
	 * @param resultSet the result set
	 * @param resultMap the result map
	 * @return the object
	 */
	public Object bindForObject(ResultSet resultSet, Qname resultMap) {
		List<ResultMap> resultMaps = this.resultMap.get(resultMap);
		
		Assert.isTrue(! CollectionUtils.isEmpty(resultMaps));
		
		if(resultMaps.get(0).getRowMapCount() > 0){
			return this.bindToRows(resultSet, resultMaps);
		} else {
			return this.bindToTriples(resultSet, resultMaps);
		}
	}

	/**
	 * Bind.
	 * 
	 * @param resultSet
	 *            the result set
	 * @param result
	 *            the result
	 * @return the iterable< object>
	 */
	protected Object bindToTriples(ResultSet resultSet, List<ResultMap> result) {

		String className = result.get(0).getResultClass();

		Object instance;

		try {
			instance = Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Map<String, Set<TripleMap>> tripleMapSet = this
				.getMapForTriplesMap(result);
		
		Map<String,Integer> collectionTracker = new HashMap<String,Integer>();

		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();

			this.processOneTriple(
					querySolution,
					instance, 
					tripleMapSet,
					collectionTracker);

		}
		
		for(ResultMap resultMap : result){
			if(StringUtils.isNotBlank(resultMap.getAfterMap())){

				AfterResultBinding<Object> afterCallback = 
					this.callbackInstantiator.instantiateAfterCallback(resultMap.getAfterMap());
				
				instance = afterCallback.afterBinding(instance);
			}
		}

		return instance;
	}

	/**
	 * Bind to rows.
	 *
	 * @param resultSet the result set
	 * @param result the result
	 * @return the list
	 */
	protected List<Object> bindToRows(ResultSet resultSet, List<ResultMap> result) {

		List<Object> returnList = new ArrayList<Object>();

		while (resultSet.hasNext()) {

			Object instance = this.processOneRow(resultSet.next(), result);

			returnList.add(instance);
		}

		return returnList;
	}
	
	/**
	 * Process one row.
	 *
	 * @param querySolution the query solution
	 * @param resultMaps the result maps
	 * @return the object
	 */
	protected Object processOneRow(QuerySolution querySolution, List<ResultMap> resultMaps){
		Object instance;

		String className = resultMaps.get(resultMaps.size() - 1).getResultClass();
		try {
			instance = Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		for (ResultMap resultMap : resultMaps) {
			this.handleRowMaps(instance, querySolution, resultMap.getRowMap());
			if(StringUtils.isNotBlank(resultMap.getAfterMap())){
				try {
					AfterResultBinding<Object> afterCallback = 
							this.callbackInstantiator.instantiateAfterCallback(resultMap.getAfterMap());
					
					instance = afterCallback.afterBinding(instance);
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
			}
		}
		
		return instance;
	}

	/**
	 * Handle triples map.
	 * 
	 * @param binding
	 *            the binding
	 * @param instance 
	 * @param tripleMapSet 
	 * @param object
	 *            the object
	 * @param tripleMapSet
	 *            the triple map set
	 */
	protected void processOneTriple(
			QuerySolution querySolution, 
			Object instance, 
			Map<String, Set<TripleMap>> tripleMapSet, 
			Map<String,Integer> collectionTracker) {

			RDFNode predicate = querySolution.get("p");

			RDFNode object = querySolution.get("o");
			
			Set<TripleMap> tripleMaps = tripleMapSet.get(predicate.asNode().getURI());

			if(CollectionUtils.isEmpty(tripleMaps)){
				tripleMaps = tripleMapSet.get("*");
			}
			
			if(CollectionUtils.isEmpty(tripleMaps)){
				return;
			}
			
			for(TripleMap tripleMap : tripleMaps){
				if(StringUtils.isNotBlank(tripleMap.getResultMapping())){
					String composite = tripleMap.getResultMapping();
					
					List<ResultMap> compositeList = this.resultMap.get(Qname.toQname(composite));
					
					Object compositeObject;
					try {
						compositeObject = Class.forName(compositeList.get(0).getResultClass()).newInstance();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
			
					this.processOneTriple(
							querySolution, 
							compositeObject, 
							this.getMapForTriplesMap(compositeList), 
							collectionTracker);
					
					String property = tripleMap.getBeanProperty();
					
					if(this.hasNestedCollection(property)){
						Integer index = collectionTracker.get(property);
						if(index == null){
							index = 0;
						}
						String indexedProperty = this.addIndexToProperty(property, index);
						
						collectionTracker.put(property, ++index);
						
						property = indexedProperty;
					}
					
					this.propertySetter.setBeanProperty(
							instance,
							property, 
							compositeObject);
	
				} else {
					String value;

					if(tripleMap.getObjectPart() != null){
						value = this.getResultFromQuerySolution(object,
							tripleMap.getObjectPart());
					} else {
						value = this.getResultFromQuerySolution(predicate,
								tripleMap.getPredicatePart());
					}
					
					if(value != null){
					
						String property = tripleMap.getBeanProperty();
						
						if(this.hasNestedCollection(property)){
							Integer index = collectionTracker.get(property);
							if(index == null){
								index = 0;
							}
							String indexedProperty = this.addIndexToProperty(property, index);
							
							collectionTracker.put(property, ++index);
							
							property = indexedProperty;
						}
						
						this.propertySetter.setBeanProperty(
								instance,
								property, 
								value);
					}
				}
			}

	}
	
	protected String addIndexToProperty(String property, int index){
		return StringUtils.replace(property, "[]", "[" + Integer.toString(index) + "]");
	}
	
	protected boolean hasNestedCollection(String property){
		int matches = StringUtils.countMatches(property, "[]");
		if(matches > 1){
			throw new RuntimeException("Only ONE nested Collection property allowed.");
		}
		return matches > 0;
	}

	/**
	 * Handle row maps.
	 *
	 * @param binding the binding
	 * @param querySolution the query solution
	 * @param rowMaps the row maps
	 */
	protected void handleRowMaps(Object binding, QuerySolution querySolution,
			RowMap[] rowMaps) {

		for (RowMap rowMap : rowMaps) {
			String resultMapping = rowMap.getResultMapping();
			
			if(StringUtils.isNotBlank(resultMapping)){
	
				List<ResultMap> compositeResults = this.resultMap.get(Qname.toQname(resultMapping));
				
				Object compositeInstance = this.processOneRow(querySolution, compositeResults);

				this.propertySetter.setBeanProperty(
						binding,
						rowMap.getBeanProperty(), 
						compositeInstance);
			} else {
				RDFNode node = querySolution.get(rowMap.getVar());
	
				String value = this.getResultFromQuerySolution(node,
						rowMap.getVarType());
	
				this.propertySetter.setBeanProperty(binding,
						rowMap.getBeanProperty(), value);
			}
		}
	}

	/**
	 * Gets the result from query solution.
	 * 
	 * @param rdfNode
	 *            the rdf node
	 * @param objectPart
	 *            the object part
	 * @return the result from query solution
	 */
	private String getResultFromQuerySolution(RDFNode rdfNode,
			BindingPart objectPart) {
		if (rdfNode == null) {
			return null;
		}

		String result;

		switch (objectPart) {
		case LOCALNAME: {
			result = rdfNode.asNode().getLocalName();
			break;
		}
		case URI: {
			result = rdfNode.asNode().getURI();
			break;
		}
		case NAMESPACE: {
			result = rdfNode.asNode().getNameSpace();
			break;
		}
		case LITERALVALUE: {
			if(! rdfNode.isLiteral()){
				result = null;
			} else {
				result = rdfNode.asLiteral().getString();
			}
			break;
		}
		default: {
			throw new IllegalStateException();
		}
		}

		return result;
	}

	/**
	 * Gets the map for triples map.
	 *
	 * @param resultMaps the result maps
	 * @return the map for triples map
	 */
	protected Map<String, Set<TripleMap>> getMapForTriplesMap(
			List<ResultMap> resultMaps) {
		Map<String, Set<TripleMap>> returnMap = new HashMap<String, Set<TripleMap>>();

		for (ResultMap result : resultMaps) {
			for (TripleMap tripleMap : result.getTriplesMap().getTripleMap()) {
				String predicateUri = tripleMap.getPredicateUri();

				if (!returnMap.containsKey(predicateUri)) {
					returnMap.put(predicateUri, new HashSet<TripleMap>());
				}
				returnMap.get(predicateUri).add(tripleMap);
			}
		}

		return returnMap;
	}

}
