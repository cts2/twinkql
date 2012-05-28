package edu.mayo.twinkql.result;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

@Component
public class MatchExpressionParser {

	public MatchExpression getMatchExpression(String query){
		return EqualsMatchExpression.parse(query);
	}
	
	public static class EqualsMatchExpression implements MatchExpression {
		
		private static final String EQUALS = "=";

		private String var;
		private String matchValue;
		
		private EqualsMatchExpression(String var, String matchValue){
			super();
			this.var = var;
			this.matchValue = matchValue;
		}
		
		private static MatchExpression parse(String query){
			String[] queryParts = StringUtils.split(query, EQUALS);
			
			if(queryParts == null || queryParts.length != 2){
				throw new MatchConditionParseException(query);
			}

			return new EqualsMatchExpression(
				StringUtils.remove(queryParts[0], '?').trim(), 
				queryParts[1].trim());
		}
		


		public boolean isMatch(QuerySolution querySolution) {
			RDFNode rdfNode = querySolution.get(this.var);
			
			if(! rdfNode.isURIResource() ){
				throw new MatchConditionParseException(this.var + " when applied does not result in a URI Node.");
			}
			
			return rdfNode.asNode().getURI().equals(this.matchValue);
		}

	}
	
	public static class MatchConditionParseException extends RuntimeException {

		private static final long serialVersionUID = -4164684389047290459L;

		private MatchConditionParseException(String query){
			super("Query: " + query + " is not a valid match expression");
		}
	}
	
}
