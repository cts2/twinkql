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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.mayo.twinkql.context.Qname;
import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.instance.BeanInstantiator;
import edu.mayo.twinkql.model.Association;
import edu.mayo.twinkql.model.Conditional;
import edu.mayo.twinkql.model.ConditionalItem;
import edu.mayo.twinkql.model.ResultMap;
import edu.mayo.twinkql.model.ResultMapChoice;
import edu.mayo.twinkql.model.ResultMapChoiceItem;
import edu.mayo.twinkql.model.RowMap;
import edu.mayo.twinkql.model.SparqlMap;
import edu.mayo.twinkql.model.SparqlMapItem;
import edu.mayo.twinkql.result.callback.ConditionalTest;

/**
 * The Class ResultBindingProcessor.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class ResultBindingProcessor implements InitializingBean {

	@Autowired
	private MatchExpressionParser matchExpressionParser;
	
	@Autowired
	private QuerySolutionResultExtractor querySolutionResultExtractor;
	
	@Autowired
	private BeanInstantiator beanInstantiator;

	@Autowired
	private TwinkqlContext twinkqlContext;

	private Map<Qname, ? extends ResultMap> resultMaps = new HashMap<Qname, ResultMap>();
	
	private Map<ResultMap,String> namespaces = new HashMap<ResultMap, String>();
	
	private QuerySolutionGrouper querySolutionGrouper = new QuerySolutionGrouper();

	@Autowired
	private PropertySetter propertySetter;
	
	@Autowired
	private AfterBindingCallbackProcessor afterBindingCallbackProcessor;

	/**
	 * Instantiates a new result binding processor.
	 */
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
		this.querySolutionResultExtractor = 
			new QuerySolutionResultExtractor(this.beanInstantiator);
		this.matchExpressionParser = new DefaultMatchExpressionParser();
		this.propertySetter = new PropertySetter(this.beanInstantiator);
		this.afterBindingCallbackProcessor = 
			new AfterBindingCallbackProcessor(this.beanInstantiator);
	
		this.afterPropertiesSet();
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		Assert.notNull(this.twinkqlContext);
		Assert.notNull(this.beanInstantiator);
		
		this.initCaches();
	}
	
	/**
	 * Inits the caches.
	 */
	protected void initCaches() {
		Map<Qname, ExtendableResultMap> resultMaps = new HashMap<Qname, ExtendableResultMap>();

		for (SparqlMap sparqlMap : this.twinkqlContext.getSparqlMaps()) {
			for (SparqlMapItem sparqlMapItem : sparqlMap.getSparqlMapItem()) {
				if (sparqlMapItem.getResultMap() != null) {
					ExtendableResultMap resultMap = new ExtendableResultMap(
						sparqlMapItem.getResultMap());
					
					String namespace = sparqlMap.getNamespace();
					resultMaps.put(
						new Qname(namespace, resultMap.getId()),
						resultMap);
					
					this.namespaces.put(resultMap, namespace);
				}
			}
			
			this.resultMaps = this.processExtendableResultSet(resultMaps);
		}
	}

	/**
	 * Bind.
	 *
	 * @param <T> the generic type
	 * @param resultSet the result set
	 * @param resultMapQname the result map qname
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> bind(
			ResultSet resultSet,
			Qname resultMapQname) {
		List<T> returnList = new ArrayList<T>();
		
		//fail fast if its an invalid ResultMap Qname
		ResultMap resultMap = this.resultMaps.get(resultMapQname);
		if(resultMap == null){
			throw new ResultMapNotFoundException(resultMapQname);
		}

		List<QuerySolution> solutions = this.resolveResultSet(resultSet);

		Collection<List<QuerySolution>> uniqueResults = 
			this.querySolutionGrouper.separateByUniqueIds(
				resultMap, 
				solutions);
		
		for(List<QuerySolution> uniqueSet : uniqueResults){
			returnList.add( (T) this.processUniqueSet(uniqueSet, resultMap) );
		}

		return returnList;
	}
	
	/**
	 * Resolve result set.
	 *
	 * @param resultSet the result set
	 * @return the list
	 */
	private List<QuerySolution> resolveResultSet(ResultSet resultSet) {
		List<QuerySolution> returnList = new ArrayList<QuerySolution>();
		
		while(resultSet.hasNext()){
			returnList.add(resultSet.next());
		}
		
		return returnList;
	}
	
	/**
	 * Process extendable result set.
	 *
	 * @param resultMaps the result maps
	 * @return the map
	 */
	private Map<Qname, ExtendableResultMap> processExtendableResultSet(
			Map<Qname, ExtendableResultMap> resultMaps){
		
		for (Entry<Qname, ExtendableResultMap> entry : resultMaps.entrySet()) {
			ExtendableResultMap map = entry.getValue();
			
			if (StringUtils.isNotBlank(map.getExtends())) {
				ExtendableResultMap extended = resultMaps.get(Qname
						.toQname(map.getExtends(), entry.getKey().getNamespace() ));

				map.setExtending(extended);
			}
		}
		
		return resultMaps;
	}
	
	/**
	 * Process unique set.
	 *
	 * @param uniqueSet the unique set
	 * @param resultMap the result map
	 * @return the object
	 */
	private Object processUniqueSet(List<QuerySolution> uniqueSet, ResultMap resultMap){
		Object returnResult = this.createNewResult(resultMap);
		
		ResultMapChoice resultMapChoice = resultMap.getResultMapChoice();
		
		for(QuerySolution solution : uniqueSet){
			for(ResultMapChoiceItem resultMapItem : resultMapChoice.getResultMapChoiceItem()){
				RowMap rowMap = resultMapItem.getRowMap();
				if(rowMap != null){
					this.processRowMap(
							returnResult, 
							rowMap,
							solution);
				}
				Conditional conditional = resultMapItem.getIf();
				if(conditional != null){
					this.handleConditional(
							returnResult, 
							solution, 
							conditional, 
							null);
				}
				
				Association association = resultMapItem.getAssociation();
				if(association != null){
					Object associatedObject = 
						this.processAssociation(association, uniqueSet, this.namespaces.get(resultMap));
					
					this.propertySetter.
						setProperty(returnResult, associatedObject, association, null);
				}
			}
		}
		
		this.afterBindingCallbackProcessor.process(
				resultMap.getAfterMap(), 
				returnResult, 
				null);
		
		return returnResult;
	}

	/**
	 * Process row map.
	 *
	 * @param target the target
	 * @param rowMap the row map
	 * @param solution the solution
	 */
	private void processRowMap(Object target, RowMap rowMap, QuerySolution solution) {
		if(this.isMatch(rowMap, solution)){

			String result = 
					this.querySolutionResultExtractor.
						getResultFromQuerySolution(
							solution.get(rowMap.getVar()), 
							rowMap);
			
			this.propertySetter.
				setProperty(
					target, 
					result, 
					rowMap,
					null);
		}
	}
	
	/**
	 * Process association.
	 *
	 * @param association the association
	 * @param uniqueSet the unique set
	 * @return the object
	 */
	private Object processAssociation(Association association, List<QuerySolution> uniqueSet, String defaultNamespace) {
		Object returnObject;
		
		ResultMap resultMap = this.getResultMapFromAssociation(association, defaultNamespace);
		
		if(association.isIsCollection()){
			List<Object> returnList = new ArrayList<Object>();
			
			Collection<List<QuerySolution>> innerUniqueSets = 
				this.querySolutionGrouper.separateByUniqueIds(resultMap, uniqueSet);
			
			for(List<QuerySolution> innerUniqueSet : innerUniqueSets){
				Object result = this.processUniqueSet(innerUniqueSet, resultMap);
				returnList.add(result);
			}
			
			returnObject = returnList;
		} else {
			returnObject = this.processUniqueSet(uniqueSet, resultMap);
		}
		return returnObject;
	}
	
	private ResultMap getResultMapFromAssociation(Association association, String defaultNamespace) {
		ResultMap resultMap;
		
		if(StringUtils.isNotBlank(association.getResultMap())) {
			if(association.getResultMapChoice() != null){
				throw new MappingException(
					"Association cannot declare a 'resultMap' attribute and contain content.");
			}
			resultMap = this.resultMaps.get(
				Qname.toQname(association.getResultMap(), defaultNamespace));	
		} else {
			resultMap = association;
		}
			
		return resultMap;
	}
	
	
	
	/**
	 * Creates the new result.
	 *
	 * @param resultMap the result map
	 * @return the object
	 */
	private Object createNewResult(ResultMap resultMap) {
		String resultAliasOrClass = resultMap.getResultClass();
		
		return this.beanInstantiator.instantiate(resultAliasOrClass, true);
	}

	/**
	 * Checks if is match.
	 *
	 * @param rowMap the row map
	 * @param solution the solution
	 * @return true, if is match
	 */
	private boolean isMatch(RowMap rowMap, QuerySolution solution){
		if(StringUtils.isBlank(rowMap.getMatch())){
			return true;
		} else {
			MatchExpression matchExpression = 
				this.matchExpressionParser.parseMatchExpression(rowMap.getMatch());
		
			return matchExpression.isMatch(solution);
		}
	}
	

	
	/**
	 * Handle conditional.
	 *
	 * @param targetObj the target obj
	 * @param solution the solution
	 * @param conditional the conditional
	 * @param tracker the tracker
	 */
	private void handleConditional(
			Object targetObj, 
			QuerySolution solution,
			Conditional conditional, 
			CompositeTracker tracker) {
		
		@SuppressWarnings("unchecked")
		ConditionalTest<Object> test = this.beanInstantiator
				.instantiate(
						conditional.getFunction(),
						ConditionalTest.class, 
						false);

		String parameter = conditional.getParam();

		if (test.test(solution.get(parameter))) {
			for (ConditionalItem conditionalItem : 
				conditional.getConditionalItem()){
				
				if (conditionalItem.getRowMap() != null) {
					RowMap rowMap = conditionalItem.getRowMap();

					this.processRowMap(targetObj, rowMap, solution);
				}
			}
		}
	}
	
	/**
	 * Gets the twinkql context.
	 *
	 * @return the twinkql context
	 */
	public TwinkqlContext getTwinkqlContext() {
		return twinkqlContext;
	}

	/**
	 * Sets the twinkql context.
	 *
	 * @param twinkqlContext the new twinkql context
	 */
	public void setTwinkqlContext(TwinkqlContext twinkqlContext) {
		this.twinkqlContext = twinkqlContext;
	}
}
