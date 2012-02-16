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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jodd.bean.BeanUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.mayo.twinkql.context.Qname;
import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.model.CompositeResultMap;
import edu.mayo.twinkql.model.CompositeResultMapItem;
import edu.mayo.twinkql.model.Conditional;
import edu.mayo.twinkql.model.ConditionalItem;
import edu.mayo.twinkql.model.NestedResultMap;
import edu.mayo.twinkql.model.PerRowResultMap;
import edu.mayo.twinkql.model.ResultMapItem;
import edu.mayo.twinkql.model.RowMap;
import edu.mayo.twinkql.model.SparqlMap;
import edu.mayo.twinkql.model.SparqlMapItem;
import edu.mayo.twinkql.model.TripleMap;
import edu.mayo.twinkql.model.TwinkqlConfig;
import edu.mayo.twinkql.model.TwinkqlConfigItem;
import edu.mayo.twinkql.model.types.BindingPart;
import edu.mayo.twinkql.result.callback.AfterResultBinding;
import edu.mayo.twinkql.result.callback.CallbackInstantiator;
import edu.mayo.twinkql.result.callback.Modifier;

/**
 * The Class ResultBindingProcessor.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class ResultBindingProcessor {

	private CallbackInstantiator callbackInstantiator;

	private TwinkqlContext twinkqlContext;

	private Map<Qname, CompositeResultMap> compositeResultMaps = new HashMap<Qname, CompositeResultMap>();
	private Map<Qname, PerRowResultMap> perRowResultMaps = new HashMap<Qname, PerRowResultMap>();

	/**
	 * Instantiates a new result binding processor.
	 * 
	 * @param twinkqlContext
	 *            the twinkql context
	 */
	public ResultBindingProcessor(TwinkqlContext twinkqlContext) {
		this.twinkqlContext = twinkqlContext;
		this.callbackInstantiator = new CallbackInstantiator(twinkqlContext);
		this.addInDeclaredNamespaces();
		this.initCaches();
	}

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

							if (uri.contains(":")) {
								String namespace = StringUtils.substringBefore(
										uri, ":");

								if (StringUtils.isNotBlank(namespace)) {
									String localPart = StringUtils
											.substringAfter(uri, ":");
									tripleMap.setPredicateUri(namespaceMap
											.get(namespace) + localPart);
								}
							}
						}
					}
				}
			}
		}
	}

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
					perRowResultMaps.put(Qname.toQname(perRowMap.getId()),
							perRowMap);
				}
			}

			this.compositeResultMaps = compositeResultMaps;
			this.perRowResultMaps = perRowResultMaps;
		}

		for (Entry<Qname, CompositeResultMap> entry : compositeResultMaps
				.entrySet()) {
			CompositeResultMap map = entry.getValue();
			if (StringUtils.isNotBlank(map.getExtends())) {
				CompositeResultMap extended = compositeResultMaps.get(Qname
						.toQname(map.getExtends()));

				ExtendedCompositeResultMap parent = new ExtendedCompositeResultMap();
				try {
					BeanUtils.copyProperties(parent, map);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
				parent.setExtending(extended);

				compositeResultMaps.put(entry.getKey(), parent);
			}
		}
	}

	private static class ExtendedCompositeResultMap extends CompositeResultMap {

		private static final long serialVersionUID = -6125562115832012024L;

		private CompositeResultMap extending;

		private ExtendedCompositeResultMap() {
			super();
		}

		private ExtendedCompositeResultMap(CompositeResultMap extending) {
			this.extending = extending;
		}

		public void setExtending(CompositeResultMap extending) {
			this.extending = extending;
		}

		@Override
		public Enumeration<? extends CompositeResultMapItem> enumerateCompositeResultMapItem() {
			return Collections.enumeration(Arrays.asList(this
					.getCompositeResultMapItem()));
		}

		@Override
		public CompositeResultMapItem getCompositeResultMapItem(int index)
				throws IndexOutOfBoundsException {
			return this.getCompositeResultMapItem()[index];
		}

		@Override
		public CompositeResultMapItem[] getCompositeResultMapItem() {
			return (CompositeResultMapItem[]) ArrayUtils.addAll(
					super.getCompositeResultMapItem(),
					this.extending.getCompositeResultMapItem());
		}

		@Override
		public int getCompositeResultMapItemCount() {
			return this.getCompositeResultMapItem().length;
		}

		@Override
		public Iterator<? extends CompositeResultMapItem> iterateCompositeResultMapItem() {
			return Arrays.asList(this.getCompositeResultMapItem()).iterator();
		}
	}

	public Object bindForObject(ResultSet resultSet, Qname resultMap) {
		CompositeResultMap compositeResultMap = this.compositeResultMaps
				.get(resultMap);

		List<QuerySolution> solutions = new ArrayList<QuerySolution>();
		
		while(resultSet.hasNext()){
			solutions.add(resultSet.next());
		}
		
		return this.procesTriples(solutions, compositeResultMap);
	}

	protected Object procesTriples(List<QuerySolution> querySolutions,
			CompositeResultMap compositeResultMap) {
		Object targetObj;
		
		Map<String,Object> callbackParams = new HashMap<String,Object>();
		Map<String,Integer> collectionTracker = new HashMap<String,Integer>();
		
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
					
					if(! tripleMap.getPredicateUri().equals(predicateUri)){
						continue;
					}
					
					String result = this.getResultFromQuerySolution(
							solution.get(tripleMap.getVar()), tripleMap);

					String property = tripleMap.getBeanProperty();
					
					if(this.isIndexedProperty(property)){
						Integer index = collectionTracker.get(property);
						if(index == null){
							index = 0;
						}
						String indexedProperty = this.addIndexToProperty(property, index);
						
						collectionTracker.put(property, ++index);
						
						property = indexedProperty;
					}
					
					BeanUtil.setPropertyForced(targetObj,
							property, result);
				}
			}
		}
		
		for (ResultMapItem item : compositeResultMap.getResultMapItem()) {

			if (item.getNestedResultMap() != null) {
				NestedResultMap nestedResultMap = item.getNestedResultMap();
				
				CompositeResultMap nestedCompositeResultMap = 
						this.compositeResultMaps.get(Qname.toQname(nestedResultMap.getResultMap()));
				
				Object nestedObj = this.procesTriples(
						new ArrayList<QuerySolution>(querySolutions), nestedCompositeResultMap);
				
				BeanUtil.setPropertyForced(targetObj,
						nestedResultMap.getBeanProperty(), nestedObj);
			}
		}
		
		this.fireAfterResultBindingCallback(targetObj, compositeResultMap, callbackParams);

		return targetObj;
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
		}
	}
	
	
	protected String addIndexToProperty(String property, int index){
		return StringUtils.replace(property, "[]", "[" + Integer.toString(index) + "]");
	}
	
	protected boolean isIndexedProperty(String property){
		return StringUtils.contains(property, '[') && StringUtils.contains(property, ']');
	}

/*
	public Set<String> getRequestedPredicates(
			CompositeResultMap compositeResultMap) {
		Set<String> requested = new HashSet<String>();
		for (CompositeResultMapItem item : compositeResultMap
				.getCompositeResultMapItem()) {
			TripleMap tripleMap = item.getTripleMap();
			NestedResultMap nestedResultMap = item.getNestedResultMap();
			if (tripleMap != null) {
				requested.add(tripleMap.getPredicateUri());
			}
			if (nestedResultMap != null) {
				CompositeResultMap nestedCompositeResultMap = this.compositeResultMaps
						.get(Qname.toQname(nestedResultMap.getResultMap()));

				requested.addAll(this
						.getRequestedPredicates(nestedCompositeResultMap));
			}
			Conditional conditional = item.getIf();
			if (conditional != null) {
				for (ConditionalItem conditionalItem : conditional
						.getConditionalItem()) {
					TripleMap conditionalTripleMap = conditionalItem
							.getTripleMap();
					NestedResultMap conditionalNestedResultMap = item
							.getNestedResultMap();
					if (conditionalTripleMap != null) {
						requested.add(conditionalTripleMap.getPredicateUri());
					}
					if (conditionalNestedResultMap != null) {
						CompositeResultMap nestedCompositeResultMap = this.compositeResultMaps
								.get(Qname.toQname(nestedResultMap
										.getResultMap()));

						requested
								.addAll(this
										.getRequestedPredicates(nestedCompositeResultMap));
					}
				}
			}

		}

		return requested;
	}
*/
	private String getResultFromQuerySolution(RDFNode rdfNode, RowMap rowMap) {
		if (rdfNode == null) {
			return null;
		}

		BindingPart part = rowMap.getVarType();

		String result;

		switch (part) {
		case LOCALNAME: {
			result = rdfNode.asNode().getLocalName();
			if (StringUtils.isBlank(result)) {
				result = StringUtils.substringAfterLast(rdfNode.asNode()
						.getURI(), "/");
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

		if (StringUtils.isNotBlank(modifier)) {
			@SuppressWarnings("unchecked")
			Modifier<String> modifierObject = this.callbackInstantiator
					.instantiateCallback(modifier, Modifier.class);

			result = modifierObject.beforeSetting(result);
		}
		return result;
	}

}
