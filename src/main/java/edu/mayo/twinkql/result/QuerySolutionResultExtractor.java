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
package edu.mayo.twinkql.result;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.mayo.twinkql.instance.BeanInstantiator;
import edu.mayo.twinkql.model.RowMap;
import edu.mayo.twinkql.model.types.BindingPart;
import edu.mayo.twinkql.result.callback.Modifier;

/**
 * The Class QuerySolutionResultExtractor.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class QuerySolutionResultExtractor {
	
	private UriParser uriParser = new UriParser();
	
	@Autowired
	private BeanInstantiator beanInstantiator;
	
	/**
	 * Instantiates a new query solution result extractor.
	 */
	public QuerySolutionResultExtractor() {
		super();
	}
	
	/**
	 * Instantiates a new query solution result extractor.
	 *
	 * @param beanInstantiator the bean instantiator
	 */
	public QuerySolutionResultExtractor(BeanInstantiator beanInstantiator) {
		super();
		this.beanInstantiator = beanInstantiator;
	}

	/**
	 * Gets the result from query solution.
	 *
	 * @param rdfNode the rdf node
	 * @param rowMap the row map
	 * @return the result from query solution
	 */
	public String getResultFromQuerySolution(RDFNode rdfNode, RowMap rowMap) {
		if (rdfNode == null) {
			return null;
		}

		BindingPart part = rowMap.getVarType();

		String result;

		switch (part) {
		case LOCALNAME: {
			result = this.uriParser.getLocalPart(
					rdfNode.asNode().getURI());
			break;
		}
		case URI: {
			result = rdfNode.asNode().getURI();
			break;
		}
		case NAMESPACE: {
			result = this.uriParser.getNamespace(
					rdfNode.asNode().getURI());
			break;
		}
		case LITERALVALUE: {
			result = rdfNode.asLiteral().getString();
			break;
		}
		default: {
			throw new IllegalStateException();
		}
		}

		String modifier = rowMap.getModifier();

		if (StringUtils.isNotBlank(modifier)) {
			@SuppressWarnings("unchecked")
			Modifier<String> modifierObject = this.beanInstantiator
					.instantiate(modifier, Modifier.class, false);

			result = modifierObject.beforeSetting(result);
		}
		return result;
	}
}
