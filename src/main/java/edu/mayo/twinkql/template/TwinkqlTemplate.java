package edu.mayo.twinkql.template;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import edu.mayo.twinkql.context.TwinkqlContext;

public class TwinkqlTemplate {
	
	@Autowired
	private TwinkqlContext twinkqlContext;

	public String queryForString(String namespace, String mapId, Map<String,Object> parameters){
		String query = this.twinkqlContext.getSparqlMap(namespace, mapId).getString();
		
		if(!CollectionUtils.isEmpty(parameters)){
			for(Entry<String,Object> entrySet : parameters.entrySet()){
				query = query.replaceAll(
						"#"+entrySet.getKey()+"#", 
						entrySet.getValue().toString());
			}
		}
		
		return query;
	}
}
