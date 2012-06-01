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

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * The Class DefaultMatchExpressionParser.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class DefaultMatchExpressionParser implements MatchExpressionParser {
	
	
	private static final String DOT = ".";
	private static final String METHOD = "()";
	private static final String EQUALS = "=";
	
	private Pattern methodPattern = Pattern.compile("^.*\\..*\\(\\)$");
	private Pattern equalsPattern = Pattern.compile("^.*=.*$");

	/* (non-Javadoc)
	 * @see edu.mayo.twinkql.result.MatchExpressionParser#parseMatchExpression(java.lang.String)
	 */
	public MatchExpression parseMatchExpression(String query){
		if(this.methodPattern.matcher(query).matches()){
			return this.parseRdfNodeMethod(query);
		}
		if(this.equalsPattern.matcher(query).matches()){
			return this.parseEquals(query);
		}
		
		throw new MatchConditionParseException(query);
	}
	
	/**
	 * Parses the rdf node method.
	 *
	 * @param query the query
	 * @return the match expression
	 */
	private MatchExpression parseRdfNodeMethod(String query){
		String[] queryParts = StringUtils.split(query, DOT);
		
		if(queryParts == null || queryParts.length != 2){
			throw new MatchConditionParseException(query);
		}

		return new JenaRdfNodeMatchExpression(
			StringUtils.remove(queryParts[0], '?').trim(), 
			StringUtils.removeEnd(queryParts[1].trim(), METHOD));
	}
	
	
	/**
	 * Parses the equals.
	 *
	 * @param query the query
	 * @return the match expression
	 */
	private MatchExpression parseEquals(String query){
		String[] queryParts = StringUtils.split(query, EQUALS);
		
		if(queryParts == null || queryParts.length != 2){
			throw new MatchConditionParseException(query);
		}

		return new EqualsMatchExpression(
			StringUtils.remove(queryParts[0], '?').trim(), 
			queryParts[1].trim());
	}
	
	/**
	 * The Class JenaRdfNodeMatchExpression.
	 *
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	public class JenaRdfNodeMatchExpression implements MatchExpression {

		private String var;
		private String method;
		
		/**
		 * Instantiates a new jena rdf node match expression.
		 *
		 * @param var the var
		 * @param method the method
		 */
		private JenaRdfNodeMatchExpression(String var, String method){
			super();
			this.var = var;
			this.method = method;
		}

		/* (non-Javadoc)
		 * @see edu.mayo.twinkql.result.MatchExpression#isMatch(com.hp.hpl.jena.query.QuerySolution)
		 */
		public boolean isMatch(QuerySolution querySolution) {
			RDFNode rdfNode = querySolution.get(this.var);
			
			Method method = ReflectionUtils.findMethod(RDFNode.class, this.method);
			
			return (Boolean) ReflectionUtils.invokeMethod(method, rdfNode);
		}

	}
	
	/**
	 * The Class EqualsMatchExpression.
	 *
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	public static class EqualsMatchExpression implements MatchExpression {

		private String var;
		private String matchValue;
		
		/**
		 * Instantiates a new equals match expression.
		 *
		 * @param var the var
		 * @param matchValue the match value
		 */
		private EqualsMatchExpression(String var, String matchValue){
			super();
			this.var = var;
			this.matchValue = matchValue;
		}

		/* (non-Javadoc)
		 * @see edu.mayo.twinkql.result.MatchExpression#isMatch(com.hp.hpl.jena.query.QuerySolution)
		 */
		public boolean isMatch(QuerySolution querySolution) {
			RDFNode rdfNode = querySolution.get(this.var);
			
			if(! rdfNode.isURIResource() ){
				throw new MatchConditionParseException(this.var + " when applied does not result in a URI Node.");
			}
			
			return rdfNode.asNode().getURI().equals(this.matchValue);
		}

	}
	
	/**
	 * The Class MatchConditionParseException.
	 *
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	public static class MatchConditionParseException extends RuntimeException {

		private static final long serialVersionUID = -4164684389047290459L;

		/**
		 * Instantiates a new match condition parse exception.
		 *
		 * @param query the query
		 */
		private MatchConditionParseException(String query){
			super("Query: " + query + " is not a valid match expression");
		}
	}
	
}
