package edu.mayo.twinkql.result.beans.reasoning;

import java.util.Set;

public interface PropertyReasoner {
	
	public Set<String> reason(String predicateUri, String objectUri);

}
