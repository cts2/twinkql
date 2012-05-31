package edu.mayo.twinkql.result;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

@Component
public class DefaultMatchExpressionParser implements MatchExpressionParser {
	
	
	private static final String DOT = ".";
	private static final String METHOD = "()";
	private static final String EQUALS = "=";
	
	private Pattern methodPattern = Pattern.compile("^.*\\..*\\(\\)$");
	private Pattern equalsPattern = Pattern.compile("^.*=.*$");

	public MatchExpression parseMatchExpression(String query){
		if(this.methodPattern.matcher(query).matches()){
			return this.parseRdfNodeMethod(query);
		}
		if(this.equalsPattern.matcher(query).matches()){
			return this.parseEquals(query);
		}
		
		throw new MatchConditionParseException(query);
	}
	
	private MatchExpression parseRdfNodeMethod(String query){
		String[] queryParts = StringUtils.split(query, DOT);
		
		if(queryParts == null || queryParts.length != 2){
			throw new MatchConditionParseException(query);
		}

		return new JenaRdfNodeMatchExpression(
			StringUtils.remove(queryParts[0], '?').trim(), 
			StringUtils.removeEnd(queryParts[1].trim(), METHOD));
	}
	
	
	private MatchExpression parseEquals(String query){
		String[] queryParts = StringUtils.split(query, EQUALS);
		
		if(queryParts == null || queryParts.length != 2){
			throw new MatchConditionParseException(query);
		}

		return new EqualsMatchExpression(
			StringUtils.remove(queryParts[0], '?').trim(), 
			queryParts[1].trim());
	}
	
	public class JenaRdfNodeMatchExpression implements MatchExpression {

		private String var;
		private String method;
		
		private JenaRdfNodeMatchExpression(String var, String method){
			super();
			this.var = var;
			this.method = method;
		}

		public boolean isMatch(QuerySolution querySolution) {
			RDFNode rdfNode = querySolution.get(this.var);
			
			Method method = ReflectionUtils.findMethod(RDFNode.class, this.method);
			
			return (Boolean) ReflectionUtils.invokeMethod(method, rdfNode);
		}

	}
	
	public static class EqualsMatchExpression implements MatchExpression {

		private String var;
		private String matchValue;
		
		private EqualsMatchExpression(String var, String matchValue){
			super();
			this.var = var;
			this.matchValue = matchValue;
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
