package edu.mayo.twinkql.result.beans.reasoning;

import java.util.HashSet;
import java.util.Set;

public class ReasoningResult {
	
	private String result;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	public static Set<String> toStringSet(Iterable<ReasoningResult> results){
		Set<String> returnSet = new HashSet<String>();
		
		if(results == null){
			return returnSet;
		}
		
		for(ReasoningResult result : results){
			returnSet.add(result.getResult());
		}
		
		return returnSet;
	}

}
