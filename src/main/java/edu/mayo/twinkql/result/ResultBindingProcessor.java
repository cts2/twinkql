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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.mayo.twinkql.model.BindingPart;
import edu.mayo.twinkql.model.ResultMap;
import edu.mayo.twinkql.model.RowMap;
import edu.mayo.twinkql.model.TripleMap;
import edu.mayo.twinkql.model.TriplesMap;
import edu.mayo.twinkql.result.beans.PropetySetter;

/**
 * The Class ResultBindingProcessor.
 */
public class ResultBindingProcessor {
	
	private PropetySetter propetySetter = new PropetySetter();

	/**
	 * Bind.
	 * 
	 * @param resultSet
	 *            the result set
	 * @param result
	 *            the result
	 * @return the iterable< object>
	 */
	public Object bindToTriples(
			ResultSet resultSet,
			ResultMap result) {

		Object instance;
		
		try {
			instance = Class.forName(result.getResultClass()).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Map<String, Set<TripleMap>> tripleMapSet = this
				.getMapForTriplesMap(result.getTriplesMap());

		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();

			RDFNode predicate = querySolution.get(result.getTriplesMap()
					.getPredicateVar());

			RDFNode object = querySolution.get(result.getTriplesMap()
					.getObjectVar());

			this.handleTriplesMap(instance, object,
					tripleMapSet.get(predicate.asNode().getURI()));

		}

		return instance;
	}
	
	public List<Object> bindToRows(
			ResultSet resultSet,
			ResultMap result) {

		List<Object> returnList = new ArrayList<Object>();

		while (resultSet.hasNext()) {
			
			Object instance;
			
			try {
				instance = Class.forName(result.getResultClass()).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			QuerySolution querySolution = resultSet.next();

			this.handleRowMaps(instance, querySolution, result.getRowMapList());
			
			returnList.add(instance);
		}
		
		return returnList;
	}

	/**
	 * Handle triples map.
	 * 
	 * @param binding
	 *            the binding
	 * @param object
	 *            the object
	 * @param tripleMapSet
	 *            the triple map set
	 */
	protected void handleTriplesMap(Object binding, RDFNode object,
			Set<TripleMap> tripleMapSet) {

		for (TripleMap tripleMap : tripleMapSet) {
			String value = this.getResultFromQuerySolution(object,
					tripleMap.getObjectPart());

			try {
				this.propetySetter.setBeanProperty(binding, tripleMap.getBeanProperty(),
						value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	protected void handleRowMaps(
			Object binding, 
			QuerySolution querySolution,
			Iterable<RowMap> rowMapSet) {

		for (RowMap rowMap : rowMapSet) {
			RDFNode node = querySolution.get(rowMap.getVar());
			
			String value = this.getResultFromQuerySolution(
					node,
					rowMap.getVarType());

			this.propetySetter.setBeanProperty(
					binding, 
					rowMap.getBeanProperty(),
					value);
		}
	}

	/**
	 * Gets the result from query solution.
	 * 
	 * @param rdfNode
	 *            the rdf node
	 * @param objectPart
	 *            the object part
	 * @return the result from query solution
	 */
	private String getResultFromQuerySolution(RDFNode rdfNode,
			BindingPart objectPart) {
		if(rdfNode == null){
			return null;
		}

		String result;

		switch (objectPart) {
		case LOCAL_NAME: {
			result = rdfNode.asNode().getLocalName();
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
		case LITERAL_VALUE: {
			result = rdfNode.asLiteral().getString();
			break;
		}
		default: {
			throw new IllegalStateException();
		}
		}

		return result;
	}

	/**
	 * Gets the map for triples map.
	 * 
	 * @param triplesMap
	 *            the triples map
	 * @return the map for triples map
	 */
	protected Map<String, Set<TripleMap>> getMapForTriplesMap(
			TriplesMap triplesMap) {
		Map<String, Set<TripleMap>> returnMap = new HashMap<String, Set<TripleMap>>();

		for (TripleMap tripleMap : triplesMap.getTripleMapList()) {
			String predicateUri = tripleMap.getPredicateUri();

			if (!returnMap.containsKey(predicateUri)) {
				returnMap.put(predicateUri, new HashSet<TripleMap>());
			}
			returnMap.get(predicateUri).add(tripleMap);
		}

		return returnMap;
	}

}
