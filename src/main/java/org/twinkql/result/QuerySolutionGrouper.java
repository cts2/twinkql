/*
 * Copyright: (c) 2004-2012 Mayo Foundation for Medical Education and 
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
package org.twinkql.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.query.QuerySolution;

import org.twinkql.model.ResultMap;

/**
 * The Class QuerySolutionGrouper.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class QuerySolutionGrouper {

	/**
	 * Separate by unique ids.
	 *
	 * @param map the map
	 * @param solutions the solutions
	 * @return the collection
	 */
	public Collection<List<QuerySolution>> separateByUniqueIds(ResultMap map, List<QuerySolution> solutions){
		if(StringUtils.isBlank(map.getUniqueResult())){
			return this.separateByUniqueIds(solutions);
		} else {
			return this.separateByUniqueIds(map.getUniqueResult(), solutions);
		}
	}
	
	/**
	 * Separate by unique ids.
	 *
	 * @param solutions the solutions
	 * @return the collection
	 */
	private Collection<List<QuerySolution>> separateByUniqueIds(List<QuerySolution> solutions){
		List<List<QuerySolution>> returnList = new ArrayList<List<QuerySolution>>();
		
		for(QuerySolution solution : solutions){
			returnList.add(Arrays.asList(solution));
		}
		
		return returnList;
	}
	
	/**
	 * Separate by unique ids.
	 *
	 * @param uniqueVar the unique var
	 * @param solutions the solutions
	 * @return the collection
	 */
	private Collection<List<QuerySolution>> separateByUniqueIds(String uniqueVar, List<QuerySolution> solutions){
		Map<String,List<QuerySolution>> uniqueMap = new HashMap<String,List<QuerySolution>>();
		
		for(QuerySolution solution : solutions){
			if(!solution.contains(uniqueVar)){
				throw new MappingException("Unique constraint: " + uniqueVar + " is not specified as a return variable.");
			}
			String uniqueUri = solution.get(uniqueVar).asNode().getURI();
			
			if(! uniqueMap.containsKey(uniqueUri)){
				uniqueMap.put(uniqueUri, new ArrayList<QuerySolution>());
			}
			
			uniqueMap.get(uniqueUri).add(solution);
			
		}
		return uniqueMap.values();
	}
}
