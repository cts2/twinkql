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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.mayo.twinkql.context.Qname;
import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.instance.BeanInstantiator;
import edu.mayo.twinkql.model.CompositeConditional;
import edu.mayo.twinkql.model.CompositeConditionalItem;
import edu.mayo.twinkql.model.CompositeResultMap;
import edu.mayo.twinkql.model.CompositeResultMapItem;
import edu.mayo.twinkql.model.ConditionalItem;
import edu.mayo.twinkql.model.NestedResultMap;
import edu.mayo.twinkql.model.PerRowConditional;
import edu.mayo.twinkql.model.PerRowConditionalItem;
import edu.mayo.twinkql.model.PerRowResultMap;
import edu.mayo.twinkql.model.PerRowResultMapItem;
import edu.mayo.twinkql.model.Reasoner;
import edu.mayo.twinkql.model.ReasonerDefinition;
import edu.mayo.twinkql.model.ResultMap;
import edu.mayo.twinkql.model.ResultMapItem;
import edu.mayo.twinkql.model.RowMap;
import edu.mayo.twinkql.model.SparqlMap;
import edu.mayo.twinkql.model.SparqlMapItem;
import edu.mayo.twinkql.model.TripleMap;
import edu.mayo.twinkql.model.TwinkqlConfig;
import edu.mayo.twinkql.model.TwinkqlConfigItem;
import edu.mayo.twinkql.model.types.BindingPart;
import edu.mayo.twinkql.result.beans.reasoning.PropertyReasoner;
import edu.mayo.twinkql.result.callback.AfterResultBinding;
import edu.mayo.twinkql.result.callback.CallbackContext;
import edu.mayo.twinkql.result.callback.ConditionalTest;
import edu.mayo.twinkql.result.callback.Modifier;

/**
 * The Class ResultBindingProcessor.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class ResultBindingProcessor implements InitializingBean {
	
	private static final String MATCH_ALL_OTHERS = "*";

	@Autowired
	private BeanInstantiator beanInstantiator;

	@Autowired
	private TwinkqlContext twinkqlContext;
	
	@Autowired
	private UriParser uriParser;

	private Map<Qname, CompositeResultMap> compositeResultMaps = new HashMap<Qname, CompositeResultMap>();
	private Map<Qname, PerRowResultMap> perRowResultMaps = new HashMap<Qname, PerRowResultMap>();
	
	private Map<String,PropertyReasoner> reasoners;

	public ResultBindingProcessor() {
		super();
	}
	
	/**
	 * Instantiates a new result binding processor.
	 * 
	 * @param twinkqlContext
	 *            the twinkql context
	 */
	public ResultBindingProcessor(TwinkqlContext twinkqlContext) {
		this.twinkqlContext = twinkqlContext;
		this.beanInstantiator = new BeanInstantiator(twinkqlContext);
	
		this.afterPropertiesSet();
	}

	public void afterPropertiesSet() {
		Assert.notNull(this.twinkqlContext);
		Assert.notNull(this.beanInstantiator);
		
		this.addInDeclaredNamespaces();
		this.initCaches();
		this.initReasoners();
	}

	protected void initReasoners(){
		this.reasoners = this.getReasonerMap();
	}
	
	/**
	 * Adds the in declared namespaces.
	 */
	protected void addInDeclaredNamespaces() {
		Map<String, String> namespaceMap = this
				.getNamespaceMap(this.twinkqlContext.getTwinkqlConfig());
		if (CollectionUtils.isEmpty(namespaceMap)) {
			return;
		}

		for (SparqlMap map : this.twinkqlContext.getSparqlMaps()) {
			for (SparqlMapItem item : map.getSparqlMapItem()) {
				CompositeResultMap composite = item.getCompositeResultMap();
				if (composite != null
						&& composite.getCompositeResultMapItem() != null) {
					for (CompositeResultMapItem compositeItem : composite
							.getCompositeResultMapItem()) {
						if (compositeItem.getTripleMap() != null) {
							TripleMap tripleMap = compositeItem.getTripleMap();
							String uri = tripleMap.getPredicateUri();

							tripleMap.setPredicateUri(this.expandPrefix(uri, namespaceMap));	
						}
					}
				}
			}
		}
	}
	
	private String expandPrefix(String uri, Map<String, String> namespaceMap){
		if (uri.contains(":")) {
			String namespace = StringUtils.substringBefore(
					uri, ":");

			if (StringUtils.isNotBlank(namespace)) {
				String localPart = StringUtils
						.substringAfter(uri, ":");
				
				String namespaceMapping = namespaceMap.get(namespace);
				
				if(StringUtils.isNotBlank(namespaceMapping)){
					String ns = namespaceMapping + localPart;
				
					return ns;
				} else {
					return uri;
				}
			} else {
				return uri;
			}
		} else {
			return uri;
		}
	}

	/**
	 * Gets the namespace map.
	 *
	 * @param config the config
	 * @return the namespace map
	 */
	private Map<String, String> getNamespaceMap(TwinkqlConfig config) {
		if (config == null) {
			return null;
		}

		Map<String, String> returnMap = new HashMap<String, String>();
		if (config.getTwinkqlConfigItem() != null) {
			for (TwinkqlConfigItem item : config.getTwinkqlConfigItem()) {
				if (item.getNamespace() != null) {
					returnMap.put(item.getNamespace().getPrefix(), item
							.getNamespace().getUri());
				}
			}
		}

		return returnMap;
	}
	
	/**
	 * Handle row maps.
	 *
	 * @param binding the binding
	 * @param querySolution the query solution
	 * @param perRowResultMapItems the per row result map items
	 * @param callbackParams the callback params
	 */
	protected void handleRowMaps(
			Object binding, 
			QuerySolution querySolution,
			PerRowResultMap perRowResultMap,
			Tracker tracker) {

		for (PerRowResultMapItem item : perRowResultMap
				.getPerRowResultMapItem()) {
			if(item.getRowMap() != null) {
				RowMap rowMap = item.getRowMap();

				RDFNode node = querySolution.get(rowMap.getVar());

				String value = this.getResultFromQuerySolution(node, rowMap);

				this.setProperty(binding, value, rowMap, tracker);
			}
			
			if(item.getIf() != null){
				PerRowConditional conditional = item.getIf();
				
				@SuppressWarnings("unchecked")
				ConditionalTest<Object> test = this.beanInstantiator
						.instantiateCallback(conditional.getFunction(),
								ConditionalTest.class);

				String parameter = conditional.getParam();

				if (test.test(querySolution.get(parameter))) {
					for(PerRowConditionalItem conditionalItem : 
						conditional.getPerRowConditionalItem()){
					
						if(conditionalItem.getRowMap() != null){
							RowMap rowMap = conditionalItem.getRowMap();
							
							RDFNode node = querySolution.get(rowMap.getVar());

							String value = this.getResultFromQuerySolution(node, rowMap);

							this.setProperty(binding, value, rowMap, tracker);
							
						}
					}
					
					for(ConditionalItem conditionalItem : 
						conditional.getConditionalItem()){
						if(conditionalItem.getNestedResultMap() != null){
							NestedResultMap nestedResultMap = 
								conditionalItem.getNestedResultMap();
							
							this.handleNestedPerRowMap(binding, querySolution, tracker,
									nestedResultMap);
						}
					}
				}
			}
		}
		
		for (ResultMapItem item : perRowResultMap.getResultMapItem()) {
			NestedResultMap nestedResultMap = item.getNestedResultMap();

			this.handleNestedPerRowMap(binding, querySolution, tracker,
					nestedResultMap);
		}
	}

	protected void handleNestedPerRowMap(
			Object binding,
			QuerySolution querySolution, 
			Tracker tracker,
			NestedResultMap nestedResultMap) {
		PerRowResultMap resultMap = 
				this.perRowResultMaps.get(Qname.toQname(nestedResultMap.getResultMap()));
		
		Object result = this.processOneRow(querySolution, resultMap, tracker);
		
		this.setProperty(binding, result, nestedResultMap.getBeanProperty(), null, tracker);
	}
	
	/**
	 * Process one row.
	 *
	 * @param querySolution the query solution
	 * @param resultMaps the result maps
	 * @param callbackParams the callback params
	 * @return the object
	 */
	protected Object processOneRow(
			QuerySolution querySolution, 
			PerRowResultMap resultMaps, 
			Tracker tracker){
		Object instance;

		String className = resultMaps.getResultClass();
		try {
			instance = Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

			this.handleRowMaps(
					instance, 
					querySolution, 
					resultMaps,
					tracker);
		
			
			this.fireAfterResultBindingCallback(
					instance, 
					resultMaps, 
					tracker);

	
		
		return instance;
	}

	/**
	 * Inits the caches.
	 */
	protected void initCaches() {
		Map<Qname, CompositeResultMap> compositeResultMaps = new HashMap<Qname, CompositeResultMap>();
		Map<Qname, PerRowResultMap> perRowResultMaps = new HashMap<Qname, PerRowResultMap>();

		for (SparqlMap sparqlMap : this.twinkqlContext.getSparqlMaps()) {
			for (SparqlMapItem sparqlMapItem : sparqlMap.getSparqlMapItem()) {
				if (sparqlMapItem.getCompositeResultMap() != null) {
					CompositeResultMap compositeMap = sparqlMapItem
							.getCompositeResultMap();
					compositeResultMaps.put(
							Qname.toQname(compositeMap.getId(),
									sparqlMap.getNamespace()), compositeMap);
				}
				if (sparqlMapItem.getPerRowResultMap() != null) {
					PerRowResultMap perRowMap = sparqlMapItem
							.getPerRowResultMap();
					perRowResultMaps.put(Qname.toQname(perRowMap.getId(),
							sparqlMap.getNamespace()), perRowMap);
				}
			}

			this.compositeResultMaps = compositeResultMaps;
			this.perRowResultMaps = perRowResultMaps;
		}

		this.processExtendedResultSets(compositeResultMaps, new GetExtendingParent<CompositeResultMap>(){

			public CompositeResultMap getExtendingParent(CompositeResultMap extended) {
				return new ExtendedCompositeResultMap(extended);
			}
			
		});
		
		this.processExtendedResultSets(perRowResultMaps, new GetExtendingParent<PerRowResultMap>(){

			public PerRowResultMap getExtendingParent(PerRowResultMap extended) {
				return new ExtendedPerRowResultMap(extended);
			}
			
		});
		
	}
	
	protected <T extends ResultMap> void processExtendedResultSets(
			Map<Qname,T> resultsSets, 
			GetExtendingParent<T> getExtendingParent){
		for (Entry<Qname, T> entry : resultsSets.entrySet()) {
			T map = entry.getValue();
			if (StringUtils.isNotBlank(map.getExtends())) {
				T extended = resultsSets.get(Qname
						.toQname(map.getExtends()));

				T parent = getExtendingParent.getExtendingParent(extended);
				
				try {
					BeanUtils.copyProperties(parent, map);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}

				resultsSets.put(entry.getKey(), parent);
			}
		}
	}
	
	private interface GetExtendingParent<T extends ResultMap> {
		
		public T getExtendingParent(T extended);
	}

	/**
	 * Bind for list.
	 *
	 * @param resultSet the result set
	 * @param parameters 
	 * @param resultMap the result map
	 * @return the list
	 */
	public List<Object> bindForList(
			ResultSet resultSet, 
			Map<String, Object> parameters, 
			Qname resultMap) {
		PerRowResultMap perRowResultMap = this.perRowResultMaps.get(resultMap);
		
		Assert.notNull(perRowResultMap);
		
		return this.bindToRows(resultSet, parameters, perRowResultMap);

	}
	
	/**
	 * Bind to rows.
	 *
	 * @param resultSet the result set
	 * @param parameters 
	 * @param result the result
	 * @return the list
	 */
	protected List<Object> bindToRows(
			ResultSet resultSet, 
			Map<String, Object> parameters, 
			PerRowResultMap result) {

		List<Object> returnList = new ArrayList<Object>();

		while (resultSet.hasNext()) {
			Tracker tracker = new Tracker();
			tracker.setQueryParams(parameters);
			
			Object instance = this.processOneRow(resultSet.next(), result, tracker);

			returnList.add(instance);
		}

		return returnList;
	}
	
	/**
	 * Bind for object.
	 *
	 * @param resultSet the result set
	 * @param parameters 
	 * @param resultMap the result map
	 * @return the object
	 */
	public Object bindForObject(
			ResultSet resultSet, 
			Map<String, Object> parameters, 
			Qname resultMap) {
		CompositeResultMap compositeResultMap = this.compositeResultMaps
				.get(resultMap);

		List<QuerySolution> solutions = new ArrayList<QuerySolution>();
	
		while(resultSet.hasNext()){
			solutions.add(resultSet.next());
		}
		
		CompositeTracker tracker = new CompositeTracker(this.getExplicitlyRequestedPredicates(compositeResultMap));
		tracker.setQueryParams(parameters);
		
		return this.procesTriples(
				solutions, 
				tracker, 
				compositeResultMap);
	}
	
	/**
	 * Sets the property.
	 *
	 * @param targetObj the target obj
	 * @param result the result
	 * @param rowMap the row map
	 * @param callbacks the callbacks
	 */
	private void setProperty(Object targetObj, Object result, TripleMap rowMap, CompositeTracker tracker){
		String callbackId = rowMap.getCallbackId();
		String property = rowMap.getBeanProperty();
		
		if(this.isIndexedProperty(property)){
			Integer index = tracker.getCollectionTracker().get(property);
			if(index == null){
				index = 0;
			}
			String indexedProperty = this.addIndexToProperty(property, index);
			
			tracker.getCollectionTracker().put(property, ++index);
			
			property = indexedProperty;
		}
		
		this.setProperty(targetObj, result, property, callbackId, tracker);
	}
	
	private void setProperty(Object targetObj, Object result, RowMap rowMap, Tracker tracker){
		String callbackId = rowMap.getCallbackId();
		String property = rowMap.getBeanProperty();
		
		this.setProperty(targetObj, result, property, callbackId, tracker);
	}
	
	private void setProperty(
			Object targetObj, 
			Object result, 
			String property, 
			String callbackId, 
			Tracker tracker){

		if(StringUtils.isNotBlank(callbackId)){
			tracker.getCallbackParams().put(callbackId, result);
		}
		
		if(StringUtils.isNotBlank(property)){
			BeanUtil.setPropertyForced(targetObj,
					property, result);
		}	
	}
	
	/**
	 * Proces triples for list.
	 *
	 * @param querySolutions the query solutions
	 * @param tracker the tracker
	 * @param compositeResultMap the composite result map
	 * @return the list
	 */
	protected List<Object> procesTriplesForList(
			List<QuerySolution> querySolutions,
			CompositeTracker tracker,
			CompositeResultMap compositeResultMap) {
		List<Object> returnList = new ArrayList<Object>();

		for (QuerySolution solution : querySolutions) {
			
			String predicateUri = solution.get(
					compositeResultMap.getPredicateVar()).asNode().getURI();
			
			Object targetObj = null;
			

			for (CompositeResultMapItem item : compositeResultMap
					.getCompositeResultMapItem()) {

				if (item.getTripleMap() != null) {
					TripleMap tripleMap = item.getTripleMap();
					
					if(! this.isMatch(
							tripleMap.getReasoner(),
							predicateUri, 
							tripleMap.getPredicateUri(), 
							tracker.getRequestedPredicateUris())){
						continue;
					}
					
					if(targetObj == null){
					try{
						   targetObj = Class.forName(compositeResultMap.getResultClass()).newInstance();
							} catch (Exception e) {
								throw new IllegalStateException(e);
							} 
					}
					
					String result = this.getResultFromQuerySolution(
							solution.get(tripleMap.getVar()), tripleMap);
	
					this.setProperty(
							targetObj, 
							result, 
							tripleMap, 
							tracker);
				}

				if (item.getIf() != null) {
					CompositeConditional conditional = item.getIf();

					handleConditional(targetObj, solution, predicateUri,
							conditional, tracker);
				}
			}
			
			if(targetObj != null){
				this.fireAfterResultBindingCallback(
						targetObj, 
						compositeResultMap, 
						tracker);
				
				returnList.add(targetObj);
			}
		}
		
		for (ResultMapItem item : compositeResultMap.getResultMapItem()) {
			
			if (item.getNestedResultMap() != null) {
				throw new UnsupportedOperationException("ResultSetMappings on Nested indexed properties not supported.");
			}
		}
		
		return returnList;
	}
	
	/**
	 * Checks if is match.
	 *
	 * @param queryResultPredicateUri the query result predicate uri
	 * @param resultMappingPredicateUri the result mapping predicate uri
	 * @param explicitlyRequestedPredicateUris the explicitly requested predicate uris
	 * @return true, if is match
	 */
	private boolean isMatch(
			Reasoner[] reasoning,
			String queryResultPredicateUri, 
			String resultMappingPredicateUri, 
			Set<String> explicitlyRequestedPredicateUris){
		if(queryResultPredicateUri.equals(resultMappingPredicateUri)){
			return true;
		}
		
		Set<String> reasonedPossibleResults = new HashSet<String>();
		
		if(reasoning != null){
			for(Reasoner reason : reasoning){
				String reasonerId = reason.getId();
				
				PropertyReasoner reasoner = this.reasoners.get(reasonerId);
				
				reasonedPossibleResults.addAll(
						reasoner.reason(resultMappingPredicateUri));
			}
		}
		
		if(reasonedPossibleResults.contains(
				queryResultPredicateUri)){	
			return true;
		}
		
		if(resultMappingPredicateUri.equals(MATCH_ALL_OTHERS)){
			if(! explicitlyRequestedPredicateUris.contains(queryResultPredicateUri)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Process triple map.
	 *
	 * @param targetObj the target obj
	 * @param tripleMap the triple map
	 * @param solution the solution
	 * @param predicateUri the predicate uri
	 * @param tracker the tracker
	 */
	protected void processTripleMap(
			Object targetObj,
			TripleMap tripleMap, 
			QuerySolution solution, 
			String predicateUri, 
			CompositeTracker tracker){
		
		if(! this.isMatch(
				tripleMap.getReasoner(),
				predicateUri, 
				tripleMap.getPredicateUri(), 
				tracker.getRequestedPredicateUris())){
			return;
		}
		
		String result = this.getResultFromQuerySolution(
				solution.get(tripleMap.getVar()), tripleMap);

		this.setProperty(targetObj, result, tripleMap, tracker);
	}
	
	/**
	 * Proces triples.
	 *
	 * @param querySolutions the query solutions
	 * @param tracker the tracker
	 * @param compositeResultMap the composite result map
	 * @return the object
	 */
	protected Object procesTriples(
			List<QuerySolution> querySolutions,
			CompositeTracker tracker,
			CompositeResultMap compositeResultMap) {
		Object targetObj;

		try {
			targetObj = Class.forName(compositeResultMap.getResultClass()).newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} 
		
		for (QuerySolution solution : querySolutions) {
			
			String predicateUri = solution.get(
					compositeResultMap.getPredicateVar()).asNode().getURI();
			
			for (CompositeResultMapItem item : compositeResultMap
					.getCompositeResultMapItem()) {

				if (item.getTripleMap() != null) {
					TripleMap tripleMap = item.getTripleMap();
					
					this.processTripleMap(targetObj, tripleMap, solution,
							predicateUri, tracker);
				}

				if (item.getIf() != null) {
					CompositeConditional conditional = item.getIf();

					handleConditional(targetObj, solution, predicateUri,
							conditional, tracker);
				}
			}
		}
		
		for (ResultMapItem item : compositeResultMap.getResultMapItem()) {
			
			if (item.getNestedResultMap() != null) {
				NestedResultMap nestedResultMap = item.getNestedResultMap();
				
				this.processNestedResultMap(
						targetObj, 
						nestedResultMap, 
						querySolutions, 
						tracker);
			}
		}
		
		this.fireAfterResultBindingCallback(
				targetObj, 
				compositeResultMap, 
				tracker);

		return targetObj;
	}

	/**
	 * Handle conditional.
	 *
	 * @param targetObj the target obj
	 * @param solution the solution
	 * @param predicateUri the predicate uri
	 * @param conditional the conditional
	 * @param tracker the tracker
	 */
	protected void handleConditional(
			Object targetObj, 
			QuerySolution solution,
			String predicateUri, 
			CompositeConditional conditional, 
			CompositeTracker tracker) {
		@SuppressWarnings("unchecked")
		ConditionalTest<Object> test = this.beanInstantiator
				.instantiateCallback(conditional.getFunction(),
						ConditionalTest.class);

		String parameter = conditional.getParam();

		if (test.test(solution.get(parameter))) {
			for (CompositeConditionalItem conditionalItem : 
				conditional.getCompositeConditionalItem()){
				
				if (conditionalItem.getTripleMap() != null) {
					TripleMap tripleMap = conditionalItem.getTripleMap();

					this.processTripleMap(
							targetObj, 
							tripleMap,
							solution, 
							predicateUri, 
							tracker);
				}
			}
		
			for (ConditionalItem conditionalItem : conditional
					.getConditionalItem()) {
				
				if (conditionalItem.getNestedResultMap() != null) {
					NestedResultMap nestedResultMap = conditionalItem
							.getNestedResultMap();

					this.processNestedResultMap(targetObj,
							nestedResultMap,
							Arrays.asList(solution), tracker);			
				}
			}
		}
	}
	
	
	/**
	 * Process nested result map.
	 *
	 * @param targetObj the target obj
	 * @param nestedResultMap the nested result map
	 * @param querySolutions the query solutions
	 * @param tracker the tracker
	 */
	protected void processNestedResultMap(
			Object targetObj,
			NestedResultMap nestedResultMap, 
			List<QuerySolution> querySolutions,
			CompositeTracker tracker){
		CompositeResultMap nestedCompositeResultMap = 
				this.compositeResultMaps.get(Qname.toQname(nestedResultMap.getResultMap()));

		String property = nestedResultMap.getBeanProperty();
		if(this.isIndexedProperty(property)){
			List<Object> returnList = this.procesTriplesForList(
					new ArrayList<QuerySolution>(querySolutions), 
					tracker, 
					nestedCompositeResultMap);
			
			Integer index = tracker.getCollectionTracker().get(property);
			if(index == null){
				index = 0;
			}

			for(Object nestedObj : returnList){
				String indexedProperty = this.addIndexToProperty(property, index);
				
				tracker.getCollectionTracker().put(property, ++index);
				
				BeanUtil.setPropertyForced(targetObj, indexedProperty, nestedObj);
			}
			
		} else {
			Object nestedObj = this.procesTriples(
					new ArrayList<QuerySolution>(querySolutions), tracker, nestedCompositeResultMap);
			
			BeanUtil.setPropertyForced(targetObj, property, nestedObj);
		}	
	}
	
	/**
	 * Fire after result binding callback.
	 *
	 * @param instance the instance
	 * @param resultMap the result map
	 * @param callbackParams the callback params
	 */
	protected void fireAfterResultBindingCallback(
			Object instance, 
			ResultMap resultMap, 
			Tracker tracker){
		if(StringUtils.isNotBlank(resultMap.getAfterMap())){
			
			for(String callback : StringUtils.split(resultMap.getAfterMap(), ',')){
				@SuppressWarnings("unchecked")
				AfterResultBinding<Object> afterCallback = 
					this.beanInstantiator.instantiateCallback(
							callback, AfterResultBinding.class);
				
				CallbackContext context = new CallbackContext();
				context.setCallbackIds(tracker.getCallbackParams());
				context.setQueryParams(tracker.getQueryParams());

				afterCallback.afterBinding(instance, context);
			}
		}
	}
	
	
	/**
	 * Adds the index to property.
	 *
	 * @param property the property
	 * @param index the index
	 * @return the string
	 */
	protected String addIndexToProperty(String property, int index){
		return StringUtils.replace(property, "[]", "[" + Integer.toString(index) + "]");
	}
	
	/**
	 * Checks if is indexed property.
	 *
	 * @param property the property
	 * @return true, if is indexed property
	 */
	protected boolean isIndexedProperty(String property){
		return StringUtils.contains(property, "[]");
	}
	
	/**
	 * Adds the explicitly requested predicates.
	 *
	 * @param set the set
	 * @param predicateUri the predicate uri
	 */
	private void addExplicitlyRequestedPredicates(Set<String> set, String predicateUri){
		if(! predicateUri.equals(MATCH_ALL_OTHERS)){
			if(this.reasoners != null){
				for(PropertyReasoner reasoner : this.reasoners.values()){
					set.addAll(reasoner.reason(predicateUri));
				}
			}
			
			set.add(predicateUri);
		}
	}

	/**
	 * Gets the explicitly requested predicates.
	 *
	 * @param compositeResultMap the composite result map
	 * @return the explicitly requested predicates
	 */
	private Set<String> getExplicitlyRequestedPredicates(
			CompositeResultMap compositeResultMap) {
		Set<String> requested = new HashSet<String>();
		for (CompositeResultMapItem item : compositeResultMap
				.getCompositeResultMapItem()) {
			
			if (item.getTripleMap() != null) {
				TripleMap tripleMap = item.getTripleMap();
				this.addExplicitlyRequestedPredicates(requested, tripleMap.getPredicateUri());
			}

			if (item.getIf() != null) {
				CompositeConditional conditional = item.getIf();
				
				for(CompositeConditionalItem conditionalItem : 
					conditional.getCompositeConditionalItem()){
					
					if(conditionalItem.getTripleMap() != null){
						TripleMap tripleMap = conditionalItem.getTripleMap();
						this.addExplicitlyRequestedPredicates(requested, tripleMap.getPredicateUri());
					}
				}
				
				for (ConditionalItem conditionalItem : conditional
						.getConditionalItem()) {
					
					if(conditionalItem.getNestedResultMap() != null){
						NestedResultMap nestedResultMap = conditionalItem.getNestedResultMap();
						CompositeResultMap nested = this.compositeResultMaps.get(
								Qname.toQname(nestedResultMap.getResultMap()));
						
						requested.addAll(this.getExplicitlyRequestedPredicates(nested));
					}
				}
			}
		}
		
		for(ResultMapItem item : compositeResultMap.getResultMapItem()){
			if(item.getNestedResultMap() != null){
				NestedResultMap nestedResultMap = item.getNestedResultMap();
				CompositeResultMap nested = this.compositeResultMaps.get(
						Qname.toQname(nestedResultMap.getResultMap()));
				
				requested.addAll(this.getExplicitlyRequestedPredicates(nested));
			}
		}

		return requested;
	}
	
	private Map<String,PropertyReasoner> getReasonerMap(){
		Map<String,PropertyReasoner> returnMap = new HashMap<String,PropertyReasoner>();
		
		if(this.twinkqlContext == null ||
				this.twinkqlContext.getTwinkqlConfig() == null){
			return returnMap;
		}
		
		for(TwinkqlConfigItem item : 
			this.twinkqlContext.getTwinkqlConfig().getTwinkqlConfigItem()){
			if(item.getReasoner() != null){
				ReasonerDefinition reasoner = item.getReasoner();

				returnMap.put(reasoner.getId(), 
					this.beanInstantiator.instantiateCallback(
						reasoner.getClazz(), 
						PropertyReasoner.class));
			}
		}
		
		return returnMap;
	}

	/**
	 * Gets the result from query solution.
	 *
	 * @param rdfNode the rdf node
	 * @param rowMap the row map
	 * @return the result from query solution
	 */
	private String getResultFromQuerySolution(RDFNode rdfNode, RowMap rowMap) {
		if (rdfNode == null) {
			return null;
		}

		BindingPart part = rowMap.getVarType();

		String result;

		switch (part) {
		case LOCALNAME: {
			result = this.uriParser.getLocalPart(
				rdfNode.asNode().getURI());
			
			break;
		}
		case URI: {
			result = rdfNode.asNode().getURI();
			break;
		}
		case NAMESPACE: {
			result = this.uriParser.getNamespace(
					rdfNode.asNode().getURI());
			
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

		if (StringUtils.isNotBlank(modifier)) {
			@SuppressWarnings("unchecked")
			Modifier<String> modifierObject = this.beanInstantiator
					.instantiateCallback(modifier, Modifier.class);

			result = modifierObject.beforeSetting(result);
		}
		return result;
	}
	
	

	public TwinkqlContext getTwinkqlContext() {
		return twinkqlContext;
	}

	public void setTwinkqlContext(TwinkqlContext twinkqlContext) {
		this.twinkqlContext = twinkqlContext;
	}
}
