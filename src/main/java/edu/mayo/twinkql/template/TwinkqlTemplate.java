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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jodd.bean.BeanUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

import edu.mayo.twinkql.context.Qname;
import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.model.IsNotNull;
import edu.mayo.twinkql.model.Iterator;
import edu.mayo.twinkql.model.NamespaceDefinition;
import edu.mayo.twinkql.model.Select;
import edu.mayo.twinkql.model.SelectItem;
import edu.mayo.twinkql.model.SparqlMap;
import edu.mayo.twinkql.model.SparqlMapChoiceItem;
import edu.mayo.twinkql.model.SparqlMapItem;
import edu.mayo.twinkql.model.Test;
import edu.mayo.twinkql.model.TwinkqlConfig;
import edu.mayo.twinkql.model.TwinkqlConfigItem;
import edu.mayo.twinkql.model.types.TestType;
import edu.mayo.twinkql.result.MappingException;
import edu.mayo.twinkql.result.ResultBindingProcessor;

/**
 * The Class TwinkqlTemplate.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class TwinkqlTemplate implements InitializingBean {
	
	protected final Log log = LogFactory.getLog(getClass().getName());
	
	private static Pattern VAR_PATTERN = Pattern.compile("#\\{[^\\}]+\\}");

	@Autowired
	private TwinkqlContext twinkqlContext;

	@Autowired
	private ResultBindingProcessor resultBindingProcessor;

	private Map<Qname, Select> selectMap = new HashMap<Qname, Select>();

	private Set<NamespaceDefinition> prefixes = new HashSet<NamespaceDefinition>();

	public TwinkqlTemplate(){
		super();
	}
	
	/**
	 * Instantiates a new twinkql template.
	 * 
	 * @param twinkqlContext
	 *            the twinkql context
	 */
	public TwinkqlTemplate(TwinkqlContext twinkqlContext, ResultBindingProcessor resultBindingProcessor) {
		this.twinkqlContext = twinkqlContext;
		this.resultBindingProcessor = resultBindingProcessor;
		
		this.init();
	}

	public void afterPropertiesSet() throws Exception {
		this.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	protected void init() {
		Assert.notNull(this.twinkqlContext,
				"The property 'twinkqlContext' must be set!");
	
		this.initPrefixes(this.twinkqlContext.getTwinkqlConfig());
		this.initCaches();
	}

	public void initPrefixes(TwinkqlConfig config) {
		if (config == null) {
			return;
		}

		if (config.getTwinkqlConfigItem() != null) {
			for (TwinkqlConfigItem item : config.getTwinkqlConfigItem()) {
				if (item.getNamespace() != null) {
					this.prefixes.add(item.getNamespace());
				}
			}
		}
	}

	protected String buildPrefix(NamespaceDefinition def) {
		String uri = def.getUri();
		String prefix = def.getPrefix();

		return "PREFIX " + prefix + ": <" + uri + ">";
	}

	public void reinitCaches(){
		this.selectMap.clear();
		this.initCaches();
	}
	
	/**
	 * Inits the caches.
	 */
	protected void initCaches() {
		for (SparqlMap map : this.twinkqlContext.getSparqlMaps()) {

			if (map.getSparqlMapItem() != null) {
				for (SparqlMapItem item : map.getSparqlMapItem()) {
					if (item.getSparqlMapChoice() != null) {
						for (SparqlMapChoiceItem choice : item
								.getSparqlMapChoice().getSparqlMapChoiceItem()) {
							Select select = choice.getSelect();
							if (select != null) {
								this.selectMap.put(new Qname(
										map.getNamespace(), select.getId()),
										select);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Query for string.
	 * 
	 * @param namespace
	 *            the namespace
	 * @param selectId
	 *            the select id
	 * @param parameters
	 *            the parameters
	 * @return the string
	 */
	public String getSelectQueryString(String namespace, String selectId,
			Map<String, Object> parameters) {

		Select select = this.selectMap.get(new Qname(namespace, selectId));

		if (select == null) {
			throw new MappingException("SELECT Statement Namespace: "
					+ namespace + ", ID: " + selectId + " was not found.");
		}

		String queryString = this.doGetSparqlQueryString(select, parameters);

		if(log.isDebugEnabled()){
			log.debug("\n" + queryString);
		}

		return queryString;
	}

	//TODO: This method searches for prefixes in the query
	//This would most likely be more efficient with a regex of
	//of some kind.
	protected String addInKnownPrefixes(String query) {
		
		StringBuilder sb = new StringBuilder();
		for (NamespaceDefinition namespace : this.prefixes) {
			if(query.contains(namespace.getPrefix() + ":")){
				sb.append(this.buildPrefix(namespace));
				sb.append("\n");
			}
		}

		sb.append(query);

		return sb.toString();
	}
	
	protected boolean doesTestPass(Object param, TestType testType){
		switch(testType) {
			case IS_NULL :{
				return param == null;
			}
			case IS_NOT_NULL :{
				return param != null;
			}
			default : {
				throw new IllegalStateException();
			}
		}
	}

	/**
	 * Do get sparql query string.
	 * 
	 * @param select
	 *            the select
	 * @param parameters
	 *            the parameters
	 * @return the string
	 */
	protected String doGetSparqlQueryString(
			Select select,
			Map<String, Object> parameters) {
		String query = select.getContent();

		for (SelectItem selectItem : select.getSelectItem()) {

			if (selectItem.getIsNotNull() != null) {
				IsNotNull isNotNull = selectItem.getIsNotNull();
				
				String param = isNotNull.getProperty();
				
				String id = isNotNull.getId();
				
				if(parameters.get(param) != null){
					query = this.replaceMarker(id, query, isNotNull.getContent());
				} else {
					query = this.replaceMarker(id, query, "");
				}
			}
			
			if (selectItem.getTest() != null) {
				Test test = selectItem.getTest();
				
				String param = test.getProperty();
				
				String id = test.getId();
				
				TestType testType = test.getTestType();
				
				if(this.doesTestPass(parameters.get(param), testType)){
					query = this.replaceMarker(id, query, test.getContent());
				} else {
					query = this.replaceMarker(id, query, "");
				}
			}

			if (selectItem.getIterator() != null) {
				Iterator itr = selectItem.getIterator();
				
				String id = itr.getId();

				String paramProp = itr.getProperty();
				String collectionPath = itr.getCollection();

				Object iterableParam = parameters.get(paramProp);
				// return fast if empty collection
				if (iterableParam == null) {
					query = this.replaceMarker(id, query, "");
					continue;
				}

				Collection<?> collection;
				if (collectionPath.equals(".")) {
					collection = (Collection<?>) iterableParam;
				} else {
					collection = (Collection<?>) BeanUtil.getProperty(
							iterableParam, collectionPath);
				}

				// return fast if emtpy collection
				if (CollectionUtils.isEmpty(collection)) {
					query = this.replaceMarker(id, query, "");
					continue;
				}

				StringBuilder totalContent = new StringBuilder();

				totalContent.append(itr.getOpen());

				int counter = 0;
				for (Object item : collection) {
					String content = itr.getContent();

					List<String> variables = this.getVariables(content);

					for (String variable : variables) {
						String value = (String) BeanUtil
								.getProperty(
										item,
										this.stripVariableWrappingForIterator(variable));
						content = StringUtils.replace(content, variable, value);
					}

					totalContent.append(content);

					if (++counter < collection.size()) {
						totalContent.append(itr.getSeparator());
					}
				}

				totalContent.append(itr.getClose());

				query = this.replaceMarker(id, query, totalContent.toString());
			}
		}
		
		if (!CollectionUtils.isEmpty(parameters)) {
			List<String> preSetParams = this.getVariables(query);
			for (String presetParam : preSetParams) {

				String strippedVariable = this
						.stripVariableWrapping(presetParam);

				String key = StringUtils.substringBefore(strippedVariable, ".");

				Object varObject = parameters.get(key);
				
				if(varObject == null){
					throw new MappingException("Parameter: " + presetParam + " was defined in the Mapping but no substitution value was found.");
				}

				String path = StringUtils.substringAfter(strippedVariable, ".");

				String value;
				if (StringUtils.isNotBlank(path)) {
					value = (String) BeanUtil.getSimpleProperty(varObject,
							path, true);
				} else {
					value = varObject.toString();
				}

				query = query.replace(presetParam, value);

			}
		}

		query = this.addInKnownPrefixes(query);

		return query;
	}

	private String replaceMarker(String id, String query, String replacement) {
		return StringUtils.replaceOnce(query, id, replacement);
	}

	private String stripVariableWrapping(String variable) {
		variable = StringUtils.removeStart(variable, "#{");
		variable = StringUtils.removeEnd(variable, "}");

		return variable;
	}

	private String stripVariableWrappingForIterator(String variable) {
		variable = StringUtils.removeStart(
				this.stripVariableWrapping(variable), "item.");

		return variable;
	}

	private List<String> getVariables(String text) {
		List<String> returnList = new ArrayList<String>();

		Matcher matcher = VAR_PATTERN.matcher(text);
		while (matcher.find()) {
			returnList.add(matcher.group());
		}

		return returnList;
	}

	/**
	 * Select for list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param namespace
	 *            the namespace
	 * @param selectId
	 *            the select id
	 * @param parameters
	 *            the parameters
	 * @param requiredType
	 *            the required type
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> selectForList(
			String namespace, 
			String selectId,
			final Map<String, Object> parameters, 
			Class<T> requiredType) {

		return this.doBind(namespace, selectId, parameters,
				new DoBind<List<T>>() {

					public List<T> doBind(ResultSet resultSet, Qname resultMap) {
						return (List<T>) resultBindingProcessor.bindForList(
								resultSet, 
								parameters,
								resultMap);
					}
				});

	}

	/**
	 * Select for object.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param namespace
	 *            the namespace
	 * @param selectId
	 *            the select id
	 * @param parameters
	 *            the parameters
	 * @param requiredType
	 *            the required type
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public <T> T selectForObject(
			String namespace, 
			String selectId,
			final Map<String, Object> parameters, 
			Class<T> requiredType) {
		return this.doBind(namespace, selectId, parameters, new DoBind<T>() {

			public T doBind(ResultSet resultSet, Qname resultMap) {
				return (T) resultBindingProcessor.bindForObject(
						resultSet,
						parameters,
						resultMap);
			}

		});
	}

	/**
	 * The Interface DoBind.
	 * 
	 * @param <T>
	 *            the generic type
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	private interface DoBind<T> {

		/**
		 * Do bind.
		 * 
		 * @param resultSet
		 *            the result set
		 * @param resultMap
		 *            the result map
		 * @return the t
		 */
		public T doBind(ResultSet resultSet, Qname resultMap);
	}

	/**
	 * Do bind.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param namespace
	 *            the namespace
	 * @param selectId
	 *            the select id
	 * @param parameters
	 *            the parameters
	 * @param doBind
	 *            the do bind
	 * @return the t
	 */
	public <T> T doBind(String namespace, String selectId,
			Map<String, Object> parameters, DoBind<T> doBind) {
		Select select = this.selectMap.get(new Qname(namespace, selectId));

		String queryString = this.getSelectQueryString(namespace, selectId,
				parameters);

		QueryExecution queryExecution = this.twinkqlContext
				.getQueryExecution(queryString);
	
		ResultSet resultSet = queryExecution.execSelect();
		
		if(! resultSet.hasNext()){
			return null;
		} else {
			Qname resultQname = Qname.toQname(select.getResultMap(), namespace);
	
			return doBind.doBind(resultSet, resultQname);
		}
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
