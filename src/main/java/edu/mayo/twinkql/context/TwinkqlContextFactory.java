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
import org.exolab.castor.xml.Unmarshaller;
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
import edu.mayo.twinkql.model.Test;
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
	
	public TwinkqlContextFactory(String sparqlEndpointUrl) {
		this(sparqlEndpointUrl, null);
	}

	public TwinkqlContextFactory(String sparqlEndpointUrl, String mappingFiles) {
		this(new JenaHttpQueryExecutionProvider(sparqlEndpointUrl), mappingFiles);
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

	/**
	 * Gets the twinkql context.
	 *
	 * @return the twinkql context
	 * @throws Exception the exception
	 */
	public TwinkqlContext getTwinkqlContext() throws Exception {
		Assert.notNull(this.queryExecutionProvider,
				"Please provide a 'QueryExecutionProvider'");

		return this.doCreateTwinkqlContext();
	}
	
	/**
	 * Do create twinkql context.
	 *
	 * @return the twinkql context
	 */
	protected TwinkqlContext doCreateTwinkqlContext(){
		DefaultTwinkqlContext context = new DefaultTwinkqlContext();
		
		TwinkqlConfig twinkqlConfig = this.loadConfigurationFile();
		context.setTwinkqlConfig(twinkqlConfig);
		context.setQueryExecutionProvider(this.queryExecutionProvider);
		context.setSparqlMaps(this.loadMappingFiles());
		
		return context;
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
			throw new ContextInitializationException(
				"There was a problem loading the Twinkql Mapping Files specified by the pattern '" + this.mappingFiles +
				".' Set the 'mappingFiles' property to specify a different mapping files location. Exception was: " +
				e.getMessage());
		}
		
		if(returnList.size() == 0){
			throw new ContextInitializationException(
					"No mapping files found at: '" + this.mappingFiles +
					".' Set the 'mappingFiles' property to specify a different mapping files location.");
		}

		return returnList;
	}
	
	/**
	 * Load configuration file.
	 *
	 * @return the twinkql config
	 */
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
	
	/**
	 * Creates a new TwinkqlContext object.
	 *
	 * @return the path matching resource pattern resolver
	 */
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
					
					this.replaceMarkers(new AddToSelectItem(){

						public void addToSelectItem(Object object,
								SelectItem selectItem) {
							selectItem.setIterator((Iterator)object);
						}

						public void setId(Object object, String id) {
							((Iterator)object).setId(id);
						}
						
					}, Iterator.class, select, "<iterator ", "</iterator>", "{iteratorMarker}");
					
					this.replaceMarkers(new AddToSelectItem(){

						public void addToSelectItem(Object object,
								SelectItem selectItem) {
							selectItem.setIsNotNull((IsNotNull)object);
						}

						public void setId(Object object, String id) {
							((IsNotNull)object).setId(id);
						}
						
					}, IsNotNull.class, select, "<isNotNull ", "</isNotNull>", "{isNotNullMarker}");
		
					this.replaceMarkers(new AddToSelectItem(){

						public void addToSelectItem(Object object,
								SelectItem selectItem) {
							selectItem.setTest((Test)object);
						}

						public void setId(Object object, String id) {
							((Test)object).setId(id);
						}
						
					}, Test.class, select, "<test ", "</test>", "{testMarker}");
					
				}
			}

			return map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * The Interface AddToSelectItem.
	 *
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	private interface AddToSelectItem {
		
		/**
		 * Adds the to select item.
		 *
		 * @param object the object
		 * @param selectItem the select item
		 */
		public void addToSelectItem(Object object, SelectItem selectItem);
		
		/**
		 * Sets the id.
		 *
		 * @param object the object
		 * @param id the id
		 */
		public void setId(Object object, String id);
	}
	
	/**
	 * Replace markers.
	 *
	 * @param adder the adder
	 * @param clazz the clazz
	 * @param select the select
	 * @param xmlStart the xml start
	 * @param xmlEnd the xml end
	 * @param marker the marker
	 */
	private void replaceMarkers(
			AddToSelectItem adder, 
			Class<?> clazz, 
			Select select, 
			String xmlStart, 
			String xmlEnd, 
			String marker){
		String content = select.getContent();
		
		String[] newContents = 
				StringUtils.substringsBetween(content, marker, marker);
		
		if(newContents != null){
			for(String newContent : newContents){
				String excapedContent = this.excapeInnerXml(newContent);
				
				String adjustedContent = xmlStart + excapedContent + xmlEnd;
				
				Object obj;
				try {
					obj = Unmarshaller.unmarshal(clazz, new StringReader(adjustedContent));
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
				
				SelectItem newSelectItem = new SelectItem();
				adder.addToSelectItem(obj, newSelectItem);
				select.addSelectItem(newSelectItem);
				
				String uuid = "{"+UUID.randomUUID().toString()+"}";
				
				content = StringUtils.replaceOnce(content, marker + newContent + marker, uuid);
				adder.setId(obj, uuid);
			}
			
			select.setContent(content);
		}
	}
	
	/**
	 * Excape inner xml.
	 *
	 * @param xml the xml
	 * @return the string
	 */
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
	
	/**
	 * Load twinkql config.
	 *
	 * @param resource the resource
	 * @return the twinkql config
	 */
	protected TwinkqlConfig loadTwinkqlConfig(Resource resource) {
		try {
			String xml = IOUtils.toString(resource.getInputStream());
			return TwinkqlConfig.unmarshalTwinkqlConfig(new StringReader(this.decorateXml(xml)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Decorate xml.
	 *
	 * @param xml the xml
	 * @return the string
	 */
	protected String decorateXml(String xml){
		xml = xml.replaceAll("<iterator", "{iteratorMarker}");
		xml = xml.replaceAll("</iterator>", "{iteratorMarker}");
		xml = xml.replaceAll("<isNotNull", "{isNotNullMarker}");
		xml = xml.replaceAll("</isNotNull>", "{isNotNullMarker}");
		xml = xml.replaceAll("<test", "{testMarker}");
		xml = xml.replaceAll("</test>", "{testMarker}");

		return xml;
	}

	/**
	 * Gets the mapping files.
	 *
	 * @return the mapping files
	 */
	public String getMappingFiles() {
		return mappingFiles;
	}

	/**
	 * Gets the query execution provider.
	 *
	 * @return the query execution provider
	 */
	public QueryExecutionProvider getQueryExecutionProvider() {
		return queryExecutionProvider;
	}

	/**
	 * Sets the query execution provider.
	 *
	 * @param queryExecutionProvider the new query execution provider
	 */
	public void setQueryExecutionProvider(
			QueryExecutionProvider queryExecutionProvider) {
		this.queryExecutionProvider = queryExecutionProvider;
	}

	/**
	 * Sets the mapping files.
	 *
	 * @param mappingFiles the new mapping files
	 */
	public void setMappingFiles(String mappingFiles) {
		this.mappingFiles = mappingFiles;
	}
}
