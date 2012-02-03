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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jodd.bean.BeanUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.mayo.twinkql.context.Qname;
import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.model.CompositeResultMap;
import edu.mayo.twinkql.model.Conditional;
import edu.mayo.twinkql.model.PerRowResultMap;
import edu.mayo.twinkql.model.ResultMap;
import edu.mayo.twinkql.model.RowMap;
import edu.mayo.twinkql.model.SparqlMap;
import edu.mayo.twinkql.model.SparqlMapItem;
import edu.mayo.twinkql.model.TripleMap;
import edu.mayo.twinkql.model.TwinkqlConfig;
import edu.mayo.twinkql.model.TwinkqlConfigItem;
import edu.mayo.twinkql.model.types.BindingPart;
import edu.mayo.twinkql.result.beans.PropertySetter;
import edu.mayo.twinkql.result.callback.AfterResultBinding;
import edu.mayo.twinkql.result.callback.CallbackInstantiator;
import edu.mayo.twinkql.result.callback.ConditionalTest;
import edu.mayo.twinkql.result.callback.Modifier;

/**
 * The Class ResultBindingProcessor.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class ResultBindingProcessor {

	private Map<Qname, List<PerRowResultMap>> perRowResultMap = new HashMap<Qname, List<PerRowResultMap>>();

	private Map<Qname, List<CompositeResultMap>> compositeResultMap = new HashMap<Qname, List<CompositeResultMap>>();

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
		this.addInDeclaredNamespaces();
		this.initCaches();
	}
	
	protected void addInDeclaredNamespaces() {
		Map<String,String> namespaceMap = this.getNamespaceMap(this.twinkqlContext.getTwinkqlConfig());
		if(CollectionUtils.isEmpty(namespaceMap)){
			return;
		}
		
		for (SparqlMap map : this.twinkqlContext.getSparqlMaps()) {
			for (SparqlMapItem item : map.getSparqlMapItem()) {
				CompositeResultMap composite = item.getCompositeResultMap();
				if(composite != null && composite.getTripleMap() != null){
					for(TripleMap tripleMap : composite.getTripleMap()){
						String uri = tripleMap.getPredicateUri();
						
						if(uri.contains(":")){
							String namespace = StringUtils.substringBefore(uri, ":");
							
							if(StringUtils.isNotBlank(namespace)){
								String localPart = StringUtils.substringAfter(uri, ":");
								tripleMap.setPredicateUri(namespaceMap.get(namespace) + localPart);
							}
						}
					}
				}
			}
		}
	}
	
	private Map<String,String> getNamespaceMap(TwinkqlConfig config){
		if(config == null){
			return null;
		}
		
		Map<String,String> returnMap = new HashMap<String,String>();
		if(config.getTwinkqlConfigItem() != null){
			for(TwinkqlConfigItem item : config.getTwinkqlConfigItem()){
				if(item.getNamespace() != null){
					returnMap.put(
							item.getNamespace().getPrefix(), 
							item.getNamespace().getUri());
				}
			}
		}
		
		return returnMap;
	}

	/**
	 * Inits the caches.
	 */
	protected void initCaches() {
		Map<Qname, CompositeResultMap> compositeResults = new HashMap<Qname, CompositeResultMap>();
		Map<Qname, PerRowResultMap> perRowResults = new HashMap<Qname, PerRowResultMap>();

		for (SparqlMap map : this.twinkqlContext.getSparqlMaps()) {
			for (SparqlMapItem item : map.getSparqlMapItem()) {
				CompositeResultMap composite = item.getCompositeResultMap();
				if(composite != null){
					compositeResults.put(new Qname(map.getNamespace(), composite.getId()),
						composite);
				}
				PerRowResultMap perRow = item.getPerRowResultMap();
				if(perRow != null){
					perRowResults.put(new Qname(map.getNamespace(), perRow.getId()),
						perRow);
				}
			}
		}

		for (Entry<Qname, CompositeResultMap> result : compositeResults.entrySet()) {
			List<CompositeResultMap> recursiveResult = new ArrayList<CompositeResultMap>();
			recursiveResult.add(result.getValue());
			
			List<CompositeResultMap> resultChain = this.getExtendedResultMap(recursiveResult, compositeResults);

			this.compositeResultMap.put(result.getKey(), resultChain);
		}
		
		for (Entry<Qname, PerRowResultMap> result : perRowResults.entrySet()) {
			List<PerRowResultMap> recursiveResult = new ArrayList<PerRowResultMap>();
			recursiveResult.add(result.getValue());
			
			List<PerRowResultMap> resultChain = this.getExtendedResultMap(recursiveResult, perRowResults);

			this.perRowResultMap.put(result.getKey(), resultChain);
		}
		
	}

	/**
	 * Gets the extended result map.
	 *
	 * @param resultChain the result chain
	 * @param allResults the all results
	 * @return the extended result map
	 */
	protected <T extends ResultMap> List<T> getExtendedResultMap(List<T> resultChain,
			Map<Qname, T> allResults) {
		T lastResult = resultChain.get(resultChain.size() - 1);

		if (StringUtils.isBlank(lastResult.getExtends())) {
			return resultChain;
		} else {
			Qname extendsResult = Qname.toQname(lastResult.getExtends());
			T extendedResult = allResults.get(extendsResult);

			if(extendedResult == null){
				throw new MappingException("\n\nExtends: " + lastResult.getExtends() + " is not a valid ResultMap.\n"
						+ "Check ResultMap: " + lastResult.getId() + "\n");
			}
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
		List<PerRowResultMap> resultMaps = this.perRowResultMap.get(resultMap);
		
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
		List<CompositeResultMap> resultMaps = this.compositeResultMap.get(resultMap);
		
		Assert.isTrue(! CollectionUtils.isEmpty(resultMaps));
	
		return this.bindToTriples(resultSet, resultMaps);
	}
	
	protected String getPredicateVariableName(List<CompositeResultMap> result){
		//TODO: finish this method
		return "p";
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
	protected Object bindToTriples(ResultSet resultSet, List<CompositeResultMap> result) {
		//return quickly for empty ResultSet
		if(! resultSet.hasNext()){
			return null;
		} 

		String className = result.get(0).getResultClass();

		Object instance;

		try {
			instance = Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		PredicateUriMatcher tripleMapSet = this
				.getMapForTriplesMap(result);
		
		Map<String,Integer> collectionTracker = new HashMap<String,Integer>();
		
		Map<String,Object> callbackParams = new HashMap<String,Object>();

		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();

			this.processOneTriple(
					querySolution,
					instance, 
					tripleMapSet,
					collectionTracker,
					callbackParams);
		}
		
		for(CompositeResultMap resultMap : result){
			this.fireAfterResultBindingCallback(instance, resultMap, callbackParams);
		}

		return instance;
	}
	
	protected void fireAfterResultBindingCallback(
			Object instance, 
			CompositeResultMap resultMap, 
			Map<String,Object> callbackParams){
		if(StringUtils.isNotBlank(resultMap.getAfterMap())){
			
			for(String callback : StringUtils.split(resultMap.getAfterMap(), ',')){
				@SuppressWarnings("unchecked")
				AfterResultBinding<Object> afterCallback = 
					this.callbackInstantiator.instantiateCallback(
							callback, AfterResultBinding.class);
				
				afterCallback.afterBinding(instance, callbackParams);
			}
			
			for(TripleMap tripleMap : resultMap.getTripleMap()){
				if(StringUtils.isNotBlank(tripleMap.getResultMapping())){
					List<CompositeResultMap> compositeResultMaps = 
							this.compositeResultMap.get(Qname.toQname(tripleMap.getResultMapping()));
					Object nestedInstance = 
							BeanUtil.getProperty(instance, tripleMap.getBeanProperty());
					
					for(CompositeResultMap nested : compositeResultMaps){
						this.fireAfterResultBindingCallback(nestedInstance, nested, callbackParams);
					}
				}
			}
		}
	}
	

	/**
	 * Bind to rows.
	 *
	 * @param resultSet the result set
	 * @param result the result
	 * @return the list
	 */
	protected List<Object> bindToRows(ResultSet resultSet, List<PerRowResultMap> result) {

		List<Object> returnList = new ArrayList<Object>();
		
		Map<String,Object> callbackParams = new HashMap<String,Object>();

		while (resultSet.hasNext()) {

			Object instance = this.processOneRow(resultSet.next(), result, callbackParams);

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
	protected Object processOneRow(
			QuerySolution querySolution, 
			List<PerRowResultMap> resultMaps, 
			Map<String,Object> callbackParams){
		Object instance;

		String className = resultMaps.get(0).getResultClass();
		try {
			instance = Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		for (PerRowResultMap resultMap : resultMaps) {
			this.handleRowMaps(
					instance, 
					querySolution, 
					resultMap.getRowMap(), 
					callbackParams);
			
			if(StringUtils.isNotBlank(resultMap.getAfterMap())){
				try {
					for(String callbackClass : StringUtils.split(resultMap.getAfterMap(), ',')){
						@SuppressWarnings("unchecked")
						AfterResultBinding<Object> afterCallback = 
								this.callbackInstantiator.instantiateCallback(callbackClass, AfterResultBinding.class);
						
						afterCallback.afterBinding(instance, callbackParams);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
			}
		}
		
		return instance;
	}
	
	private interface PredicateUriMatcher {
		public Set<TripleMap> getTripleMapSet(QuerySolution solution, String predicateUri);
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
			PredicateUriMatcher predicateUriMatcher, 
			Map<String,Integer> collectionTracker,
			Map<String,Object> callbackParams) {

			RDFNode predicate = querySolution.get("p");
	
			String predicateUri = predicate.asNode().getURI();
			Set<TripleMap> tripleMaps = predicateUriMatcher.getTripleMapSet(querySolution, predicateUri);

			if(CollectionUtils.isEmpty(tripleMaps)){
				tripleMaps = predicateUriMatcher.getTripleMapSet(querySolution, "*");
			}
	
			if(CollectionUtils.isEmpty(tripleMaps)){
				tripleMaps = new HashSet<TripleMap>();
			}
			
			Set<TripleMap> extended = predicateUriMatcher.getTripleMapSet(querySolution, "->");
			if(! CollectionUtils.isEmpty(extended)){
				tripleMaps.addAll(extended);
			}
			
			if(CollectionUtils.isEmpty(tripleMaps)){
				return;
			}
			
			for(TripleMap tripleMap : tripleMaps){
							
				if(StringUtils.isNotBlank(tripleMap.getResultMapping())){
					String composite = tripleMap.getResultMapping();
					
					List<CompositeResultMap> compositeList = this.compositeResultMap.get(Qname.toQname(composite));
					
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
					
					boolean forceSet = false;
					Object compositeObject;
					try {
						if(StringUtils.isNotBlank(property)){
						
							if(! this.isIndexedProperty(property)){
								compositeObject = BeanUtil.getSimplePropertyForced(instance, property, true);
							} else {
								compositeObject = Class.forName(compositeList.get(0).getResultClass()).newInstance();
								forceSet = true;
							}
						} else {
							compositeObject = Class.forName(compositeList.get(0).getResultClass()).newInstance();
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
			
					this.processOneTriple(
							querySolution, 
							compositeObject, 
							this.getMapForTriplesMap(compositeList), 
							collectionTracker,
							callbackParams);
					
					if(forceSet){
						this.propertySetter.setBeanProperty(
								instance,
								property, 
								compositeObject);
					}
					
					if(StringUtils.isNotBlank(tripleMap.getCallbackId())){
						
						callbackParams.put(tripleMap.getCallbackId(), compositeObject);
					} 
				} else {
					RDFNode node = querySolution.get(tripleMap.getVar());
					
					String value = this.getResultFromQuerySolution(
								node,
								tripleMap);
					
					if(value != null) {
					
						String property = tripleMap.getBeanProperty();
						
						if(StringUtils.isNotBlank(property)){
						
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
					
					if(StringUtils.isNotBlank(tripleMap.getCallbackId())){
						
						callbackParams.put(tripleMap.getCallbackId(), value);
					} 
				}
			}
	}
	
	protected String addIndexToProperty(String property, int index){
		return StringUtils.replace(property, "[]", "[" + Integer.toString(index) + "]");
	}
	
	protected boolean isIndexedProperty(String property){
		return StringUtils.contains(property, '[') && StringUtils.contains(property, ']');
	}
	
	protected boolean hasNestedCollection(String property){
		int matches = StringUtils.countMatches(property, "[]");
		if(matches > 1){
			throw new MappingException("Only ONE nested Collection property allowed.");
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
	protected void handleRowMaps(
			Object binding, 
			QuerySolution querySolution,
			RowMap[] rowMaps,
			Map<String,Object> callbackParams) {

		for (RowMap rowMap : rowMaps) {
			String resultMapping = rowMap.getResultMapping();
			
			if(StringUtils.isNotBlank(resultMapping)){
	
				List<PerRowResultMap> compositeResults = this.perRowResultMap.get(Qname.toQname(resultMapping));
				
				Object compositeInstance = 
						this.processOneRow(
								querySolution, 
								compositeResults, 
								callbackParams);

				this.propertySetter.setBeanProperty(
						binding,
						rowMap.getBeanProperty(), 
						compositeInstance);
			} else {
				RDFNode node = querySolution.get(rowMap.getVar());
	
				String value = this.getResultFromQuerySolution(
						node,
						rowMap);
				
				if(StringUtils.isNotBlank(rowMap.getBeanProperty())){
					this.propertySetter.setBeanProperty(binding,
							rowMap.getBeanProperty(), value);
				}
				
				if(StringUtils.isNotBlank(rowMap.getCallbackId())){
					callbackParams.put(rowMap.getCallbackId(), value);
				}
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
	private String getResultFromQuerySolution(
			RDFNode rdfNode,
			RowMap rowMap) {
		if (rdfNode == null) {
			return null;
		}
		
		BindingPart part = rowMap.getVarType();

		String result;

		switch (part) {
			case LOCALNAME: {
				result = rdfNode.asNode().getLocalName();
				if(StringUtils.isBlank(result)){
					result = StringUtils.substringAfterLast(rdfNode.asNode().getURI(), "/");
				}
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
				result = rdfNode.asLiteral().getString();
				break;
			}
			default: {
				throw new IllegalStateException();
			}
		}

		String modifier = rowMap.getModifier();
		
		if(StringUtils.isNotBlank(modifier)){
			@SuppressWarnings("unchecked")
			Modifier<String> modifierObject =
				this.callbackInstantiator.instantiateCallback(modifier, Modifier.class);
			
			result = modifierObject.beforeSetting(result);
		}
		return result;
	}

	/**
	 * Gets the map for triples map.
	 *
	 * @param resultMaps the result maps
	 * @return the map for triples map
	 */
	protected PredicateUriMatcher getMapForTriplesMap(
			List<CompositeResultMap> resultMaps) {
		return new DefaultPredicateUriMatcher(resultMaps);
	}
	
	private class DefaultPredicateUriMatcher implements PredicateUriMatcher {
		final Map<String, Set<TripleMap>> tripleMapSet = new HashMap<String, Set<TripleMap>>();
		final Set<Conditional> conditionals = new HashSet<Conditional>();
		

		private DefaultPredicateUriMatcher(List<CompositeResultMap> resultMaps){

			for (CompositeResultMap result : resultMaps) {
				for (TripleMap tripleMap : result.getTripleMap()) {
					String predicateUri = tripleMap.getPredicateUri();
					
					if (!tripleMapSet.containsKey(predicateUri)) {
						tripleMapSet.put(predicateUri, new HashSet<TripleMap>());
					}
					tripleMapSet.get(predicateUri).add(tripleMap);
				}
				
				this.conditionals.addAll(Arrays.asList(result.getIf()));
				
			}
		}
		
		public Set<TripleMap> getTripleMapSet(QuerySolution solution, String predicateUri) {
			Set<TripleMap> foundSet = tripleMapSet.get(predicateUri);
			if(foundSet == null){
				return null;
			}
			Set<TripleMap> returnSet = new HashSet<TripleMap>(foundSet);
			
			for(Conditional conditional : conditionals){
				
				@SuppressWarnings("unchecked")
				ConditionalTest<Object> test = (ConditionalTest<Object>) callbackInstantiator.instantiate(conditional.getFunction());

				if(test.test(solution.get(conditional.getParam()))){
					
					for (TripleMap tripleMap : conditional.getTripleMap()) {
						String innerPredicateUri = tripleMap.getPredicateUri();
						
						if (predicateUri.equals(innerPredicateUri)) {
							
							returnSet.add(tripleMap);
						}	
					}
				}
			}
			
			return returnSet;
		}
	}

}
