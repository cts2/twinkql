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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jodd.bean.BeanUtil;

import org.apache.commons.beanutils.BeanUtils;
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
import edu.mayo.twinkql.model.ResultMap;
import edu.mayo.twinkql.model.ResultMapChoice;
import edu.mayo.twinkql.model.ResultMapChoiceItem;
import edu.mayo.twinkql.model.RowMap;
import edu.mayo.twinkql.model.SparqlMap;
import edu.mayo.twinkql.model.SparqlMapItem;
import edu.mayo.twinkql.result.beans.reasoning.PropertyReasoner;

/**
 * The Class ResultBindingProcessor.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class ResultBindingProcessor implements InitializingBean {
	
	private static final String MATCH_ALL_OTHERS = "*";
	
	@Autowired
	private MatchExpressionParser matchExpressionParser;
	
	@Autowired
	private QuerySolutionResultExtractor querySolutionResultExtractor;
	
	@Autowired
	private BeanInstantiator beanInstantiator;

	@Autowired
	private TwinkqlContext twinkqlContext;

	private Map<Qname, ? extends ResultMap> resultMaps = new HashMap<Qname, ResultMap>();
	
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
		this.querySolutionResultExtractor = 
			new QuerySolutionResultExtractor(this.beanInstantiator);
		this.matchExpressionParser = new MatchExpressionParser();
	
		this.afterPropertiesSet();
	}

	public void afterPropertiesSet() {
		Assert.notNull(this.twinkqlContext);
		Assert.notNull(this.beanInstantiator);
		
		this.initCaches();
	}
	
	protected void initCaches() {
		Map<Qname, ExtendableResultMap> resultMaps = new HashMap<Qname, ExtendableResultMap>();

		for (SparqlMap sparqlMap : this.twinkqlContext.getSparqlMaps()) {
			for (SparqlMapItem sparqlMapItem : sparqlMap.getSparqlMapItem()) {
				if (sparqlMapItem.getResultMap() != null) {
					ExtendableResultMap resultMap = new ExtendableResultMap(
						sparqlMapItem.getResultMap());
					resultMaps.put(
							Qname.toQname(resultMap.getId(),
									sparqlMap.getNamespace()), resultMap);
				}
			}
			
			this.resultMaps = this.processExtendableResultSet(resultMaps);
		}
	}

	public <T> List<T> select(
			ResultSet resultSet,
			Qname resultMapQname) {
		List<T> returnList = new ArrayList<T>();

		ResultMap resultMap = this.resultMaps.get(resultMapQname);

		Collection<List<QuerySolution>> uniqueResults = 
			this.separateByUniqueIds(
				resultMap, 
				resultSet);
		
		for(List<QuerySolution> uniqueSet : uniqueResults){
			returnList.add( (T) this.processUniqueSet(uniqueSet, resultMap) );
		}

		return returnList;
	}
	
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
	
	private Object processUniqueSet(List<QuerySolution> uniqueSet, ResultMap resultMap){
		Object returnResult = this.createNewResult(resultMap);
		
		ResultMapChoice resultMapChoice = resultMap.getResultMapChoice();
		
		for(QuerySolution solution : uniqueSet){
			for(ResultMapChoiceItem resultMapItem : resultMapChoice.getResultMapChoiceItem()){
				RowMap rowMap = resultMapItem.getRowMap();
				if(rowMap == null){
					continue;
				}
				
				if(this.isMatch(rowMap, solution)){
					
					String result = 
							this.querySolutionResultExtractor.
								getResultFromQuerySolution(
									solution.get(rowMap.getVar()), 
									rowMap);
					
					this.setProperty(
						returnResult, 
						result, 
						rowMap,
						null);
					
				}
			}
		}
		
		return returnResult;
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
	
	private Object createNewResult(ResultMap resultMap) {
		try {
			return Class.forName(resultMap.getResultClass()).newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private boolean isMatch(RowMap rowMap, QuerySolution solution){
		if(StringUtils.isBlank(rowMap.getMatch())){
			return true;
		} else {
			MatchExpression matchExpression = 
				this.matchExpressionParser.getMatchExpression(rowMap.getMatch());
		
			return matchExpression.isMatch(solution);
		}
	}
	
	private Collection<List<QuerySolution>> separateByUniqueIds(ResultMap map, ResultSet resultSet){
		if(StringUtils.isBlank(map.getUniqueResult())){
			return this.separateByUniqueIds(resultSet);
		} else {
			return this.separateByUniqueIds(map.getUniqueResult(), resultSet);
		}
	}
	
	private Collection<List<QuerySolution>> separateByUniqueIds(ResultSet resultSet){
		List<List<QuerySolution>> returnList = new ArrayList<List<QuerySolution>>();
		
		while(resultSet.hasNext()){
			QuerySolution solution = resultSet.next();
			returnList.add(Arrays.asList(solution));
		}
		
		return returnList;
	}
	
	private Collection<List<QuerySolution>> separateByUniqueIds(String uniqueVar, ResultSet resultSet){
		Map<String,List<QuerySolution>> uniqueMap = new HashMap<String,List<QuerySolution>>();
		
		while(resultSet.hasNext()){
			QuerySolution solution = resultSet.next();
			
			String uniqueUri = solution.get(uniqueVar).asNode().getURI();
			
			if(! uniqueMap.containsKey(uniqueUri)){
				uniqueMap.put(uniqueUri, new ArrayList<QuerySolution>());
			}
			
			uniqueMap.get(uniqueUri).add(solution);
			
		}
		
		return uniqueMap.values();
	}
	
	public TwinkqlContext getTwinkqlContext() {
		return twinkqlContext;
	}

	public void setTwinkqlContext(TwinkqlContext twinkqlContext) {
		this.twinkqlContext = twinkqlContext;
	}
}
