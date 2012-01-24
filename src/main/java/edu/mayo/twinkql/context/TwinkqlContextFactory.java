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
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import edu.mayo.twinkql.model.SparqlMap;

/**
 * A factory for creating TwinkqlContext objects.
 */
public class TwinkqlContextFactory {

	private String mappingFiles = "classpath:twinkql/**/*.xml";

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
		return new DefaultTwinkqlContext(this.queryExecutionProvider,
				this.loadMappingFiles());
	}

	/**
	 * Load mapping files.
	 * 
	 * @return the iterable
	 */
	protected Set<SparqlMap> loadMappingFiles() {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

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

	/**
	 * Load sparql mappings.
	 *
	 * @param resource the resource
	 * @return the sparql mappings
	 */
	protected SparqlMap loadSparqlMap(Resource resource) {
		try {
			return SparqlMap.unmarshalSparqlMap(new InputStreamReader(resource
					.getInputStream()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
