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
package edu.mayo.twinkql.context;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import edu.mayo.twinkql.model.IsNotNull;
import edu.mayo.twinkql.model.Iterator;
import edu.mayo.twinkql.model.Select;
import edu.mayo.twinkql.model.SelectItem;
import edu.mayo.twinkql.model.SparqlMap;
import edu.mayo.twinkql.model.SparqlMapChoice;
import edu.mayo.twinkql.model.SparqlMapChoiceItem;
import edu.mayo.twinkql.model.SparqlMapItem;
import edu.mayo.twinkql.model.TwinkqlConfig;

/**
 * A factory for creating TwinkqlContext objects.
 */
public class TwinkqlContextFactory {
	
	protected final Log log = LogFactory.getLog(getClass());

	private String mappingFiles = "classpath:twinkql/**/*Map.xml";
	
	private String configurationFile = "classpath:twinkql/configuration.xml";

	private QueryExecutionProvider queryExecutionProvider;

	/**
	 * The Constructor.
	 */
	public TwinkqlContextFactory() {
		super();
	}

	/**
	 * The Constructor.
	 * 
	 * @param queryExecutionProvider
	 *            the query execution provider
	 */
	public TwinkqlContextFactory(QueryExecutionProvider queryExecutionProvider) {
		this(queryExecutionProvider, null);
	}

	/**
	 * The Constructor.
	 * 
	 * @param queryExecutionProvider
	 *            the query execution provider
	 * @param mappingFiles
	 *            the mapping files
	 */
	public TwinkqlContextFactory(QueryExecutionProvider queryExecutionProvider,
			String mappingFiles) {
		this.queryExecutionProvider = queryExecutionProvider;

		if (StringUtils.isNotBlank(mappingFiles)) {
			this.mappingFiles = mappingFiles;
		}
	}

	public TwinkqlContext getTwinkqlContext() throws Exception {
		Assert.notNull(this.queryExecutionProvider,
				"Please provide a 'QueryExecutionProvider'");

		return this.doCreateTwinkqlContext();
	}
	
	protected TwinkqlContext doCreateTwinkqlContext(){
		return new DefaultTwinkqlContext(
				this.loadConfigurationFile(),
				this.queryExecutionProvider,
				this.loadMappingFiles());
	}

	/**
	 * Load mapping files.
	 * 
	 * @return the iterable
	 */
	protected Set<SparqlMap> loadMappingFiles() {
		PathMatchingResourcePatternResolver resolver = this.createPathMatchingResourcePatternResolver();
		
		Set<SparqlMap> returnList = new HashSet<SparqlMap>();

		try {
			for (org.springframework.core.io.Resource resource : resolver
					.getResources(this.mappingFiles)) {
				returnList.add(this.loadSparqlMap(resource));

			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return returnList;
	}
	
	protected TwinkqlConfig loadConfigurationFile() {
		PathMatchingResourcePatternResolver resolver = this.createPathMatchingResourcePatternResolver();

		Resource configFile = resolver.getResource(this.configurationFile);
		
		TwinkqlConfig config = null;
		if(! configFile.exists()){
			this.log.warn("No Twinql Configuration File specified. Using defaults.");
		} else {
			config = this.loadTwinkqlConfig(configFile);
		}
		
		return config;
	}
	
	protected PathMatchingResourcePatternResolver createPathMatchingResourcePatternResolver(){
		return new PathMatchingResourcePatternResolver();
	}

	/**
	 * Load sparql mappings.
	 *
	 * @param resource the resource
	 * @return the sparql mappings
	 */
	protected SparqlMap loadSparqlMap(Resource resource) {
		try {
			String xml = IOUtils.toString(resource.getInputStream());
			SparqlMap map = SparqlMap.unmarshalSparqlMap(new StringReader(this.decorateXml(xml)));
			
			for(SparqlMapItem item : map.getSparqlMapItem()){
				SparqlMapChoice choice = item.getSparqlMapChoice();
				
				if(choice == null){
					continue;
				}
				for(SparqlMapChoiceItem choiceItem : choice.getSparqlMapChoiceItem()){
					Select select = choiceItem.getSelect();
					
					String content = select.getContent();
					
					String[] newContents = 
							StringUtils.substringsBetween(content, "{iteratorMarker}", "{iteratorMarker}");
					
					if(newContents != null){
						for(String newContent : newContents){
							String excapedContent = this.excapeInnerXml(newContent);
							
							String adjustedContent = "<iterator " + excapedContent + "</iterator>";
							
							Iterator itr = Iterator.unmarshalIterator(new StringReader(adjustedContent));
							SelectItem newSelectItem = new SelectItem();
							newSelectItem.setIterator(itr);
							select.addSelectItem(newSelectItem);
							
							String uuid = "{"+UUID.randomUUID().toString()+"}";
							
							content = StringUtils.replaceOnce(content, "{iteratorMarker}"+ newContent +"{iteratorMarker}", uuid);
							itr.setId(uuid);
						}
					}
					
					newContents = 
							StringUtils.substringsBetween(content, "{isNotNullMarker}", "{isNotNullMarker}");
					
					if(newContents != null){
						for(String newContent : newContents){
							String excapedContent = this.excapeInnerXml(newContent);
							
							String adjustedContent  = "<isNotNull " + excapedContent + "</isNotNull>";
			
							IsNotNull isNotNull = IsNotNull.unmarshalIsNotNull(new StringReader(adjustedContent));
							SelectItem newSelectItem = new SelectItem();
							newSelectItem.setIsNotNull(isNotNull);
							select.addSelectItem(newSelectItem);
							
							String uuid = "{"+UUID.randomUUID().toString()+"}";
							
							content = StringUtils.replaceOnce(content, "{isNotNullMarker}"+ newContent +"{isNotNullMarker}", uuid);
							isNotNull.setId(uuid);
						}
					}

					select.setContent(content);
				}
			}
			
			return map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private String excapeInnerXml(String xml){
		String newXml = StringUtils.replace(xml, "&", "&amp;");
		newXml = StringUtils.replace(newXml, "<", "&lt;");
		
		StringBuilder sb = new StringBuilder();
		
		char[] chars = newXml.toCharArray();
		boolean pastFirst = false;
		for(int i=0;i<chars.length;i++){
			if(chars[i] == '>'){
				if(!pastFirst){
					sb.append(chars[i]);
					pastFirst = true;
				} else {
					sb.append("&gt;");
				}
			} else {
				sb.append(chars[i]);
			}
		}
		newXml = sb.toString();
		
		return newXml;
	}
	
	protected TwinkqlConfig loadTwinkqlConfig(Resource resource) {
		try {
			String xml = IOUtils.toString(resource.getInputStream());
			return TwinkqlConfig.unmarshalTwinkqlConfig(new StringReader(this.decorateXml(xml)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected String decorateXml(String xml){
		xml = xml.replaceAll("<iterator", "{iteratorMarker}");
		xml = xml.replaceAll("</iterator>", "{iteratorMarker}");
		xml = xml.replaceAll("<isNotNull", "{isNotNullMarker}");
		xml = xml.replaceAll("</isNotNull>", "{isNotNullMarker}");

		return xml;
	}

	public String getMappingFiles() {
		return mappingFiles;
	}

	public QueryExecutionProvider getQueryExecutionProvider() {
		return queryExecutionProvider;
	}

	public void setQueryExecutionProvider(
			QueryExecutionProvider queryExecutionProvider) {
		this.queryExecutionProvider = queryExecutionProvider;
	}

	public void setMappingFiles(String mappingFiles) {
		this.mappingFiles = mappingFiles;
	}
}
