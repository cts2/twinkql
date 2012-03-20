package edu.mayo.twinkql.result.beans.reasoning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.mayo.twinkql.template.TwinkqlTemplate;

@Component
public class BasePropertyReasoner implements PropertyReasoner {
	
	private static final String REASON_NS = "reasoning";
	private static final String REASON_SPARQL = "reason";
	
	private Map<String,Set<String>> cache = new HashMap<String,Set<String>>();
	
	@Autowired
	private TwinkqlTemplate twinkqlTemplate;

	public Set<String> reason(String predicateUri, String objectUri) {
		String key = this.getKey(predicateUri, objectUri);
		
		if(! this.cache.containsKey(key)){
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("predicate", predicateUri);
			params.put("object", objectUri);
				
			List<ReasoningResult> result = this.twinkqlTemplate.selectForList(
					REASON_NS, 
					REASON_SPARQL, 
					params, 
					ReasoningResult.class);
			
			Set<String> returnSet = ReasoningResult.toStringSet(result);
			this.cache.put(key, returnSet);
		}
		
		return this.cache.get(key);
	}

	public TwinkqlTemplate getTwinkqlTemplate() {
		return twinkqlTemplate;
	}

	public void setTwinkqlTemplate(TwinkqlTemplate twinkqlTemplate) {
		this.twinkqlTemplate = twinkqlTemplate;
	}
	
	private String getKey(String predicateUri, String objectUri){
		return predicateUri + objectUri;
	}
	
}
