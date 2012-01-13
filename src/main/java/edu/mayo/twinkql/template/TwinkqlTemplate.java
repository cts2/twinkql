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

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import edu.mayo.sparqler.model.SparqlMap;
import edu.mayo.sparqler.model.SparqlMappings;
import edu.mayo.twinkql.context.TwinkqlContext;

/**
 * The Class TwinkqlTemplate.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class TwinkqlTemplate implements InitializingBean {
	
	@Autowired
	private TwinkqlContext twinkqlContext;
	
	/**
	 * Instantiates a new twinkql template.
	 *
	 * @param twinkqlContext the twinkql context
	 */
	public TwinkqlTemplate(){
		super();
	}
	
	public TwinkqlTemplate(TwinkqlContext twinkqlContext){
		this.twinkqlContext = twinkqlContext;
	}
	
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.twinkqlContext, "The property 'twinkqlContext' must be set!");
	}

	/**
	 * Query for string.
	 *
	 * @param namespace the namespace
	 * @param mapId the map id
	 * @param parameters the parameters
	 * @return the string
	 */
	public String queryForString(String namespace, String mapId, Map<String,Object> parameters){
		SparqlMappings mappings = this.twinkqlContext.getSparqlMappings(namespace);
		
		SparqlMap map = this.findSparqlMap(mappings, mapId);
		
		String query = map.getString();
		
		if(!CollectionUtils.isEmpty(parameters)){
			for(Entry<String,Object> entrySet : parameters.entrySet()){
				query = query.replace(
						"#{"+entrySet.getKey()+"}", 
						entrySet.getValue().toString());
			}
		}
		
		return query;
	}
	
	/**
	 * Find sparql map.
	 *
	 * @param mappings the mappings
	 * @param mapId the map id
	 * @return the sparql map
	 */
	protected SparqlMap findSparqlMap(SparqlMappings mappings, String mapId){
		Assert.notNull(mappings);
		Assert.notNull(mapId);
		
		for(SparqlMap map : mappings.getSparqlMapList()){
			if(map.getId().equals(mapId)){
				return map;
			}
		}
		
		throw new SparqlMapNotFoundException(mappings.getNamespace(), mapId);
	}
}
