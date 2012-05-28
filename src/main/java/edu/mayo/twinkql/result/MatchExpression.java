package edu.mayo.twinkql.result;

import com.hp.hpl.jena.query.QuerySolution;

public interface MatchExpression {

	public boolean isMatch(QuerySolution querySolution);
}
