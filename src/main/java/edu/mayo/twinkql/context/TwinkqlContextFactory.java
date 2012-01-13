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
import java.util.ArrayList;
import java.util.List;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import edu.mayo.sparqler.model.SparqlMappings;

/**
 * A factory for creating TwinkqlContext objects.
 */
public class TwinkqlContextFactory implements FactoryBean<TwinkqlContext> {

	private String mappingFiles = "classpath:twinkql/**/*.xml";

	public TwinkqlContext getObject() throws Exception {
		return new DefaultTwinkqlContext(this.loadMappingFiles());
	}

	/**
	 * Load mapping files.
	 *
	 * @return the iterable
	 */
	protected Iterable<SparqlMappings> loadMappingFiles() {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		
		List<SparqlMappings> returnList = new ArrayList<SparqlMappings>();
		
		try {
			for (org.springframework.core.io.Resource resource : resolver
					.getResources(this.mappingFiles)) {
				returnList.add(this.loadSparqlMappings(resource));
				
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (JiBXException e) {
			throw new RuntimeException(e);
		}
		
		return returnList;
	}
	
	/**
	 * Load sparql mappings.
	 *
	 * @param resource the resource
	 * @return the sparql mappings
	 * @throws JiBXException the ji bx exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected SparqlMappings loadSparqlMappings(Resource resource) throws JiBXException, IOException{
		 IBindingFactory factory = 
			        BindingDirectory.getFactory(SparqlMappings.class);
		 
		 IUnmarshallingContext unmarshaller = factory.createUnmarshallingContext();
		 return (SparqlMappings) unmarshaller.unmarshalDocument(resource.getInputStream(), null);
	}

	public Class<?> getObjectType() {
		return TwinkqlContext.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getMappingFiles() {
		return mappingFiles;
	}

	public void setMappingFiles(String mappingFiles) {
		this.mappingFiles = mappingFiles;
	}
}
