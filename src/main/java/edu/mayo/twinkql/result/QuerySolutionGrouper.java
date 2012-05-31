package edu.mayo.twinkql.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.query.QuerySolution;

import edu.mayo.twinkql.model.ResultMap;

public class QuerySolutionGrouper {

	public Collection<List<QuerySolution>> separateByUniqueIds(ResultMap map, List<QuerySolution> solutions){
		if(StringUtils.isBlank(map.getUniqueResult())){
			return this.separateByUniqueIds(solutions);
		} else {
			return this.separateByUniqueIds(map.getUniqueResult(), solutions);
		}
	}
	
	private Collection<List<QuerySolution>> separateByUniqueIds(List<QuerySolution> solutions){
		List<List<QuerySolution>> returnList = new ArrayList<List<QuerySolution>>();
		
		for(QuerySolution solution : solutions){
			returnList.add(Arrays.asList(solution));
		}
		
		return returnList;
	}
	
	private Collection<List<QuerySolution>> separateByUniqueIds(String uniqueVar, List<QuerySolution> solutions){
		Map<String,List<QuerySolution>> uniqueMap = new HashMap<String,List<QuerySolution>>();
		
		for(QuerySolution solution : solutions){
			String uniqueUri = solution.get(uniqueVar).asNode().getURI();
			
			if(! uniqueMap.containsKey(uniqueUri)){
				uniqueMap.put(uniqueUri, new ArrayList<QuerySolution>());
			}
			
			uniqueMap.get(uniqueUri).add(solution);
			
		}
		return uniqueMap.values();
	}
}
